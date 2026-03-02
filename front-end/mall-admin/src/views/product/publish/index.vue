<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">发布商品</span>
        <el-button type="primary" @click="handlePublish" :loading="publishLoading">
          发布商品
        </el-button>
      </div>
    </template>

    <el-form ref="spuFormRef" :model="spuForm" label-width="100px" @submit.prevent>
      <el-form-item label="商品名称" prop="name" :rules="spuRules.name">
        <el-input v-model="spuForm.name" placeholder="请输入商品名称" />
      </el-form-item>

      <el-form-item label="商品描述" prop="description" :rules="spuRules.description">
        <el-input
          v-model="spuForm.description"
          placeholder="请输入商品描述"
          type="textarea"
          :rows="3"
        />
      </el-form-item>

      <el-form-item label="商品分类" prop="categoryId" :rules="spuRules.categoryId">
        <el-cascader
          v-model="spuForm.categoryId"
          :options="categoryTreeOptions"
          :props="{ value: 'categoryId', label: 'name', children: 'children' }"
          placeholder="请选择三级分类"
          @change="handleCategoryChange"
          style="width: 300px"
        />
      </el-form-item>

      <el-form-item label="品牌" prop="brandId" :rules="spuRules.brandId">
        <el-select
          v-model="spuForm.brandId"
          placeholder="请选择品牌"
          style="width: 200px"
          filterable
        >
          <el-option
            v-for="brand in brandOptions"
            :key="brand.id"
            :label="brand.name"
            :value="brand.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="商品重量" prop="weight" :rules="spuRules.weight">
        <el-input-number
          v-model="spuForm.weight"
          :min="0"
          :precision="4"
          :step="0.001"
          :controls="false"
        />
      </el-form-item>

      <el-form-item label="商品状态" prop="publishStatus" :rules="spuRules.publishStatus">
        <el-radio-group v-model="spuForm.publishStatus">
          <el-radio :value="0">下架</el-radio>
          <el-radio :value="1">上架</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        label="基本属性"
        v-if="spuForm.basicAttrs.length > 0"
        class="attributes-section"
      >
        <el-row :gutter="16">
          <el-col
            v-for="(attr, index) in spuForm.basicAttrs"
            :key="attr.attrId"
            :span="12"
            class="mb-3"
          >
            <el-form-item
              :label="attr.attrName"
              :prop="`basicAttrs.${index}.attrValue`"
              :rules="[
                {
                  required: true,
                  message: `请选择${attr.attrName}`,
                  trigger: 'change',
                },
              ]"
            >
              <el-select
                v-if="attr.valueList && attr.valueList.length > 0"
                v-model="spuForm.basicAttrs[index].attrValue"
                placeholder="请选择属性值"
                style="width: 100%"
              >
                <el-option
                  v-for="value in attr.valueList"
                  :key="value"
                  :label="value"
                  :value="value"
                />
              </el-select>
              <el-input
                v-else
                v-model="spuForm.basicAttrs[index].attrValue"
                placeholder="请输入属性值"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form-item>

      <el-form-item label="SPU图片">
        <el-upload
          v-for="(image, index) in spuForm.spuImages"
          :key="index"
          class="spu-image-uploader"
          :show-file-list="false"
          :on-success="() => handleSpuImageSuccess(index)"
          :before-upload="beforeUpload"
          :http-request="(options) => uploadSpuImage(options, index)"
        >
          <img v-if="image.url" :src="image.url" class="spu-image-preview" />
          <div v-else class="upload-placeholder-small">
            <el-icon class="upload-icon"><Upload /></el-icon>
            <div class="upload-text">点击上传图片 {{ index + 1 }}</div>
          </div>
        </el-upload>

        <el-button type="primary" size="small" @click="addSpuImage" class="mt-2">
          添加图片
        </el-button>

        <div class="text-gray-500 text-sm mt-1">建议尺寸：800x800，仅支持 JPG/PNG/WebP</div>
      </el-form-item>

      <el-form-item label="商品详情" prop="detail" :rules="spuRules.detail">
        <div style="border: 1px solid #ccc">
          <Toolbar
            style="border-bottom: 1px solid #ccc"
            :editor="editorRef"
            :default-config="toolbarConfig"
            :mode="mode"
          />
          <Editor
            style="height: 300px; overflow-y: hidden"
            v-model="spuForm.detail"
            :default-config="editorConfig"
            :mode="mode"
            @on-created="handleCreated"
          />
        </div>
      </el-form-item>

      <el-form-item label="销售属性" v-if="spuForm.saleAttrs.length > 0" class="attributes-section">
        <el-row :gutter="16">
          <el-col
            v-for="(attr, index) in spuForm.saleAttrs"
            :key="attr.attrId"
            :span="12"
            class="mb-3"
          >
            <el-form-item
              :label="attr.attrName"
              :prop="`saleAttrs.${index}.selectedValues`"
              :rules="[
                {
                  required: true,
                  validator: validateSaleAttr,
                  trigger: 'change',
                },
              ]"
            >
              <el-checkbox-group v-model="spuForm.saleAttrs[index].selectedValues">
                <el-checkbox v-for="value in attr.valueList" :key="value" :label="value">
                  {{ value }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form-item>

      <el-form-item v-if="skus.length > 0" label="SKU列表">
        <el-table :data="skus" border style="width: 100%">
          <el-table-column label="规格组合">
            <template #default="scope">
              {{ getSpecCombination(scope.row.specifications) }}
            </template>
          </el-table-column>

          <el-table-column label="SKU名称" width="180">
            <template #default="scope">
              <el-input
                v-model="scope.row.name"
                placeholder="SKU名称"
                @input="handleSkuNameChange(scope.$index, $event)"
              />
            </template>
          </el-table-column>

          <el-table-column label="价格（元）" width="120">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.price"
                :min="0"
                :precision="2"
                controls-position="right"
                style="width: 100%"
              />
            </template>
          </el-table-column>

          <el-table-column label="库存" width="100">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.stock"
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </template>
          </el-table-column>

          <el-table-column label="SKU图片" width="120">
            <template #default="scope">
              <el-button size="small" @click="openSkuImageDialog(scope.$index)" type="primary" link>
                {{ scope.row.images && scope.row.images.length > 0 ? '查看/编辑' : '上传图片' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form-item>
    </el-form>

    <el-dialog
      v-model="skuImageDialogVisible"
      title="管理SKU图片"
      width="600px"
      @close="closeSkuImageDialog"
    >
      <div v-if="currentSkuIndex !== null">
        <el-upload
          v-for="(image, index) in skus[currentSkuIndex].images"
          :key="index"
          class="sku-detail-uploader"
          :show-file-list="false"
          :on-success="() => handleSkuDetailImageSuccess(currentSkuIndex, index)"
          :before-upload="beforeUpload"
          :http-request="(options) => uploadSkuDetailImage(options, currentSkuIndex, index)"
        >
          <img v-if="image.url" :src="image.url" class="sku-detail-preview" />
          <div v-else class="upload-placeholder">
            <el-icon class="upload-icon"><Upload /></el-icon>
            <div class="upload-text">点击上传图片 {{ index + 1 }}</div>
          </div>
        </el-upload>

        <el-button type="primary" size="small" @click="addSkuImage(currentSkuIndex)" class="mt-2">
          添加图片
        </el-button>

        <div class="text-gray-500 text-sm mt-1">建议尺寸：800x800，仅支持 JPG/PNG/WebP</div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeSkuImageDialog">取消</el-button>
          <el-button type="primary" @click="confirmSkuImageDialog">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { watch } from 'vue'

  import { ElMessage } from 'element-plus'
  import type { FormInstance, UploadRequestOptions, UploadRawFile } from 'element-plus'
  import { Upload } from '@element-plus/icons-vue'
  import '@wangeditor/editor/dist/css/style.css'
  import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

  import { publishSpuApi, uploadSpuImageApi, uploadSkuImageApi } from '@/api/product'
  import { getCategoryTreeApi } from '@/api/category'
  import { brandListApi } from '@/api/brand'
  import { getCategoryAttrsApi } from '@/api/attr'

  import type { CategoryTree, CategoryItem } from '@/api/category/type'
  import type { BrandItem } from '@/api/brand/type'
  import type { PublishSpuRequest, SPUImage, SKUImage, SKUDTO } from '@/api/product/type'

  const spuFormRef = ref<FormInstance>()

  const editorRef = ref()
  const mode = ref<'default' | 'simple'>('default')
  const toolbarConfig = ref({})
  const editorConfig = ref({
    placeholder: '请输入商品详情...',
  })

  const handleCreated = (editor: any) => {
    editorRef.value = editor
  }

  const brandQueryParams = reactive({
    name: '',
    pageNum: 1,
    pageSize: 1000,
  })

  const categoryTreeOptions = ref<CategoryTree[]>([])
  const brandOptions = ref<BrandItem[]>([])

  const spuForm = reactive({
    name: '',
    description: '',
    categoryId: null as number | null,
    brandId: null as number | null,
    weight: 0,
    publishStatus: 1,
    detail: '',
    spuImages: [{ url: '', sort: 0 }] as SPUImage[],
    basicAttrs: [] as Array<{
      attrId: number
      attrName: string
      attrValue: string
      valueList: string[]
    }>,
    saleAttrs: [] as Array<{
      attrId: number
      attrName: string
      valueList: string[]
      selectedValues: string[]
    }>,
  })

  interface LocalSKUDTO {
    specifications: Array<{ attrId: number; attrName: string; attrValue: string }>
    name: string
    price: number
    stock: number
    images: SKUImage[]
  }

  const skus = ref<LocalSKUDTO[]>([])

  const skuImageDialogVisible = ref(false)
  const currentSkuIndex = ref<number | null>(null)

  const spuRules = reactive({
    name: [
      { required: true, message: '请输入商品名称', trigger: 'blur' },
      { min: 2, max: 100, message: '商品名称长度在2-100个字符', trigger: 'blur' },
    ],
    description: [
      { required: true, message: '请输入商品描述', trigger: 'blur' },
      { max: 500, message: '商品描述不能超过500个字符', trigger: 'blur' },
    ],
    categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
    brandId: [{ required: true, message: '请选择品牌', trigger: 'change' }],
    weight: [{ required: true, message: '请输入商品重量', trigger: 'blur' }],
    detail: [{ required: true, message: '请输入商品详情', trigger: 'blur' }],
    publishStatus: [{ required: true, message: '请选择商品状态', trigger: 'change' }],
  })

  const validateSaleAttr = (rule: any, value: string[], callback: any) => {
    if (!value || value.length === 0) {
      const propPath = rule.fullField
      const match = propPath.match(/saleAttrs\.(\d+)\.selectedValues/)
      if (match) {
        const index = parseInt(match[1])
        const attrName = spuForm.saleAttrs[index]?.attrName || '销售属性'
        callback(new Error(`至少选择一个${attrName}`))
      } else {
        callback(new Error('请选择销售属性'))
      }
    } else {
      callback()
    }
  }

  const publishLoading = ref(false)

  const fetchCategoryTree = async () => {
    try {
      categoryTreeOptions.value = await getCategoryTreeApi()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取分类树失败'
      ElMessage.error(errorMsg)
    }
  }

  const fetchBrandList = async () => {
    try {
      const res = await brandListApi(brandQueryParams)
      brandOptions.value = res.records || []
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取品牌列表失败'
      ElMessage.error(errorMsg)
    }
  }

  const handleCategoryChange = async (categoryValue: number | number[]) => {
    const categoryId = Array.isArray(categoryValue)
      ? categoryValue[categoryValue.length - 1]
      : categoryValue

    if (categoryId) {
      spuForm.categoryId = categoryId
      try {
        const attrs = await getCategoryAttrsApi(categoryId)

        spuForm.basicAttrs = (attrs.basicAttrs || []).map((attr) => ({
          attrId: attr.attrId,
          attrName: attr.attrName,
          attrValue: '',
          valueList: attr.valueList ? attr.valueList.split(',') : [],
        }))

        spuForm.saleAttrs = (attrs.saleAttrs || []).map((attr) => ({
          attrId: attr.attrId,
          attrName: attr.attrName,
          valueList: attr.valueList ? attr.valueList.split(',') : [],
          selectedValues: [],
        }))
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '获取分类属性失败'
        ElMessage.error(errorMsg)
      }
    }
  }

  const cartesianProduct = <T,>(arrays: T[][]): T[][] => {
    return arrays.reduce((acc, curr) => acc.flatMap((d) => curr.map((e) => [...d, e])), [
      [],
    ] as T[][])
  }

  const handleSkuNameChange = (index: number, value: string) => {
    if (skus.value[index]) {
      skus.value[index].name = value
    }
  }

  const getSpecCombination = (specifications: Array<{ attrValue: string }>): string => {
    return specifications.map((spec) => spec.attrValue).join(' / ')
  }

  const beforeUpload = (file: UploadRawFile) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    const maxSize = 5 * 1024 * 1024 // 5MB

    if (!allowedTypes.includes(file.type)) {
      ElMessage.error('只支持 JPG/PNG/GIF/WebP 格式的图片')
      return false
    }

    if (file.size > maxSize) {
      ElMessage.error('文件大小不能超过 5MB')
      return false
    }

    return true
  }

  const addSpuImage = () => {
    spuForm.spuImages.push({ url: '', sort: spuForm.spuImages.length })
  }

  const uploadSpuImage = async (options: UploadRequestOptions, index: number) => {
    const file = options.file as UploadRawFile
    const formData = new FormData()
    formData.append('file', file)

    try {
      const url = await uploadSpuImageApi(formData)
      spuForm.spuImages[index].url = url
      spuForm.spuImages[index].sort = index
      options.onSuccess?.({})
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? 'SPU图片上传失败'
      ElMessage.error(errorMsg)
      options.onError?.(error, options)
    }
  }

  const handleSpuImageSuccess = (index: number) => {}

  const openSkuImageDialog = (skuIndex: number) => {
    currentSkuIndex.value = skuIndex
    skuImageDialogVisible.value = true
  }

  const closeSkuImageDialog = () => {
    skuImageDialogVisible.value = false
    currentSkuIndex.value = null
  }

  const confirmSkuImageDialog = () => {
    skuImageDialogVisible.value = false
  }

  const addSkuImage = (skuIndex: number) => {
    if (skus.value[skuIndex]) {
      skus.value[skuIndex].images.push({ url: '', sort: skus.value[skuIndex].images.length })
    }
  }

  const uploadSkuDetailImage = async (
    options: UploadRequestOptions,
    skuIndex: number,
    imageIndex: number
  ) => {
    const file = options.file as UploadRawFile
    const formData = new FormData()
    formData.append('file', file)

    try {
      const url = await uploadSkuImageApi(formData)

      if (skus.value[skuIndex] && skus.value[skuIndex].images[imageIndex]) {
        skus.value[skuIndex].images[imageIndex].url = url
        skus.value[skuIndex].images[imageIndex].sort = imageIndex
      }
      options.onSuccess?.({})
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? 'SKU图片上传失败'
      ElMessage.error(errorMsg)
      options.onError?.(error)
    }
  }

  const handleSkuDetailImageSuccess = (skuIndex: number, imageIndex: number) => {}

  const handlePublish = () => {
    spuFormRef.value?.validate(async (valid) => {
      if (valid && spuForm.categoryId && spuForm.brandId) {
        publishLoading.value = true
        try {
          const validSpuImages = spuForm.spuImages.filter((img) => img.url.trim())
          if (validSpuImages.length === 0) {
            ElMessage.error('请至少上传一张SPU图片')
            publishLoading.value = false
            return
          }

          const emptyBasicAttrs = spuForm.basicAttrs.filter((attr) => !attr.attrValue.trim())
          if (emptyBasicAttrs.length > 0) {
            ElMessage.error(`请填写${emptyBasicAttrs[0].attrName}`)
            publishLoading.value = false
            return
          }

          if (skus.value.length > 0) {
            const skuWithoutImages = skus.value.filter(
              (sku) =>
                !sku.images || sku.images.length === 0 || !sku.images.some((img) => img.url.trim())
            )
            if (skuWithoutImages.length > 0) {
              ElMessage.error('请为每个SKU至少上传一张图片')
              publishLoading.value = false
              return
            }
          }

          const request: PublishSpuRequest = {
            name: spuForm.name,
            description: spuForm.description,
            categoryId: spuForm.categoryId,
            brandId: spuForm.brandId,
            weight: spuForm.weight,
            detail: spuForm.detail,
            publishStatus: spuForm.publishStatus,
            spuImages: validSpuImages,
            basicAttrs: spuForm.basicAttrs.map((attr) => ({
              attrId: attr.attrId,
              attrValue: attr.attrValue,
            })),
            skus:
              skus.value.length > 0
                ? skus.value.map((sku) => {
                    const validSkuImages = (sku.images || []).filter((img) => img.url.trim())
                    return {
                      name: sku.name,
                      price: sku.price,
                      stock: sku.stock,
                      images: validSkuImages,
                      saleAttrs: sku.specifications.map((spec) => ({
                        attrId: spec.attrId,
                        attrValue: spec.attrValue,
                      })),
                    }
                  })
                : [],
          }

          await publishSpuApi(request)
          ElMessage.success('商品发布成功')
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '商品发布失败'
          ElMessage.error(errorMsg)
        } finally {
          publishLoading.value = false
        }
      }
    })
  }

  const generateSkus = () => {
    const selectedAttrs = spuForm.saleAttrs.filter((attr) => attr.selectedValues.length > 0)
    if (selectedAttrs.length === 0) {
      skus.value = []
      return
    }

    const combinations = cartesianProduct(selectedAttrs.map((attr) => attr.selectedValues))
    const newSkus = combinations.map((combination) => {
      const spec = selectedAttrs.map((attr, index) => ({
        attrId: attr.attrId,
        attrName: attr.attrName,
        attrValue: combination[index],
      }))

      const existed = skus.value.find(
        (s) => JSON.stringify(s.specifications) === JSON.stringify(spec)
      )

      return existed
        ? existed
        : {
            specifications: spec,
            name: `${spuForm.name} ${combination.join(' ')}`.trim(),
            price: 0,
            stock: 0,
            images: [],
          }
    })
    skus.value = newSkus
  }

  watch(
    () => spuForm.saleAttrs.map((a) => a.selectedValues),
    () => generateSkus(),
    { deep: true }
  )

  onMounted(() => {
    fetchCategoryTree()
    fetchBrandList()
  })
</script>

<style scoped>
  .attributes-section {
    border-top: 1px solid #ebeef5;
    border-bottom: 1px solid #ebeef5;
    padding: 16px 0;
    margin: 24px 0;
  }

  .mb-3 {
    margin-bottom: 12px;
  }

  .spu-image-uploader,
  .sku-detail-uploader {
    display: inline-block;
    margin-right: 16px;
    margin-bottom: 16px;
    cursor: pointer;
  }

  .spu-image-preview,
  .sku-detail-preview {
    width: 80px;
    height: 80px;
    display: block;
    border-radius: 4px;
    object-fit: contain;
    border: 1px solid #dcdfe6;
    padding: 5px;
    background: #fff;
  }

  .upload-placeholder-small,
  .upload-placeholder {
    width: 80px;
    height: 80px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #fafafa;
    cursor: pointer;
    transition: border-color 0.3s;
  }

  .upload-placeholder-small:hover,
  .upload-placeholder:hover {
    border-color: #409eff;
  }

  .upload-icon {
    font-size: 24px;
    color: #999;
    margin-bottom: 4px;
  }

  .upload-text {
    color: #666;
    font-size: 10px;
    text-align: center;
  }

  .sku-detail-uploader {
    width: 80px;
    height: 80px;
  }
</style>
