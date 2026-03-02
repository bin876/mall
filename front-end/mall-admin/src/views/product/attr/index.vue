<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">新增属性</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="属性名称">
        <el-input
          v-model="queryParams.attrName"
          placeholder="请输入属性名称"
          clearable
          @keyup.enter="() => fetchAttrList()"
        />
      </el-form-item>

      <el-form-item label="属性类型">
        <el-select
          v-model="queryParams.attrType"
          placeholder="请选择类型"
          clearable
          @change="() => fetchAttrList()"
        >
          <el-option label="销售属性" :value="0" />
          <el-option label="基本属性" :value="1" />
        </el-select>
      </el-form-item>

      <el-form-item label="所属分类">
        <el-cascader
          v-model="queryParams.categoryId"
          :options="categoryOptions"
          :props="{ value: 'categoryId', label: 'name', children: 'children' }"
          placeholder="请选择分类"
          clearable
          @change="() => fetchAttrList()"
          style="width: 200px"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchAttrList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="attrList" border style="width: 100%">
      <el-table-column prop="attrName" label="属性名称" />
      <el-table-column label="属性类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.attrType === 0 ? 'warning' : 'success'">
            {{ scope.row.attrType === 0 ? '销售' : '基本' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="值类型" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.valueType === 0 ? 'info' : 'primary'">
            {{ scope.row.valueType === 0 ? '单选' : '多选' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="valueList" label="可选值" min-width="150">
        <template #default="scope">
          <span v-if="scope.row.valueList">{{ scope.row.valueList }}</span>
          <span v-else class="text-gray-400">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="所属分类" />
      <el-table-column prop="createTime" label="创建时间" width="180" />

      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="danger" link @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50, 100]"
      style="margin-top: 20px; justify-content: flex-end"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog
      v-model="createDialogVisible"
      title="新增属性"
      width="500px"
      @close="handleCloseCreate"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
        style="padding-right: 20px"
      >
        <el-form-item label="属性名称" prop="attrName">
          <el-input v-model="createForm.attrName" placeholder="请输入属性名称" />
        </el-form-item>

        <el-form-item label="属性类型" prop="attrType">
          <el-radio-group v-model="createForm.attrType">
            <el-radio :value="0">销售属性</el-radio>
            <el-radio :value="1">基本属性</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="值类型" prop="valueType">
          <el-radio-group v-model="createForm.valueType">
            <el-radio :value="0">单选</el-radio>
            <el-radio :value="1">多选</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="可选值列表" prop="valueList">
          <el-input
            v-model="createForm.valueList"
            placeholder="用英文逗号分隔，如：黑色,白色,蓝色"
            type="textarea"
            :rows="2"
          />
        </el-form-item>

        <el-form-item label="所属分类" prop="categoryId">
          <el-cascader
            v-model="createForm.categoryId"
            :options="categoryOptions"
            :props="{ value: 'categoryId', label: 'name', children: 'children' }"
            placeholder="请选择三级分类"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseCreate">取消</el-button>
          <el-button type="primary" @click="handleCreateSubmit" :loading="createLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑属性" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 20px"
      >
        <el-form-item label="属性名称" prop="attrName">
          <el-input v-model="editForm.attrName" placeholder="请输入属性名称" />
        </el-form-item>

        <el-form-item label="属性类型" prop="attrType">
          <el-radio-group v-model="editForm.attrType" disabled>
            <el-radio :value="0">销售属性</el-radio>
            <el-radio :value="1">基本属性</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="值类型" prop="valueType">
          <el-radio-group v-model="editForm.valueType">
            <el-radio :value="0">单选</el-radio>
            <el-radio :value="1">多选</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="可选值列表" prop="valueList">
          <el-input
            v-model="editForm.valueList"
            placeholder="用英文逗号分隔，如：黑色,白色,蓝色"
            type="textarea"
            :rows="2"
          />
        </el-form-item>

        <el-form-item label="所属分类" prop="categoryId">
          <el-cascader
            v-model="editForm.categoryId"
            :options="categoryOptions"
            :props="{ value: 'categoryId', label: 'name', children: 'children' }"
            placeholder="请选择三级分类"
            style="width: 100%"
            disabled
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseEdit">取消</el-button>
          <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { attrListApi, createAttrApi, updateAttrApi, deleteAttrApi } from '@/api/attr'
  import { getCategoryTreeApi } from '@/api/category'
  import type { CategoryTree } from '@/api/category/type'
  import type { AttrItem, AttrQueryParams } from '@/api/attr/type'

  const route = useRoute()

  const queryParams = reactive({
    attrName: '',
    attrType: null as number | null,
    categoryId: null as number | null,
  })

  const categoryOptions = ref<CategoryTree[]>([])

  const attrList = ref<AttrItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)

  const createDialogVisible = ref(false)
  const editDialogVisible = ref(false)
  const createLoading = ref(false)
  const editLoading = ref(false)

  const createFormRef = ref<FormInstance>()
  const editFormRef = ref<FormInstance>()

  const createForm = reactive({
    attrName: '',
    attrType: 0,
    valueType: 0,
    valueList: '',
    categoryId: 0,
  })

  const editForm = reactive({
    attrId: 0,
    attrName: '',
    attrType: 0,
    valueType: 0,
    valueList: '',
    categoryId: 0,
  })

  const createRules = reactive<FormRules>({
    attrName: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
    attrType: [{ required: true, message: '请选择属性类型', trigger: 'change' }],
    valueType: [{ required: true, message: '请选择值类型', trigger: 'change' }],
    categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  })

  const editRules = reactive<FormRules>({
    attrName: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
    valueType: [{ required: true, message: '请选择值类型', trigger: 'change' }],
  })

  const fetchCategories = async () => {
    try {
      const categories = await getCategoryTreeApi()
      categoryOptions.value = categories
    } catch (error) {
      console.error('获取分类失败:', error)
    }
  }

  const fetchAttrList = async (page: number = 1, size: number = 10) => {
    try {
      const params: AttrQueryParams = {
        pageNum: page,
        pageSize: size,
        ...queryParams,
        categoryId: Array.isArray(queryParams.categoryId)
          ? queryParams.categoryId[queryParams.categoryId.length - 1]
          : queryParams.categoryId,
      }

      const res = await attrListApi(params)
      attrList.value = res.records || []
      total.value = res.total || 0
    } catch (error) {
      console.error('获取属性列表失败:', error)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      attrName: '',
      attrType: null,
      categoryId: null,
    })
    fetchAttrList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchAttrList(currentPage.value, size)
  }
  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchAttrList(page, pageSize.value)
  }

  const handleCreate = () => {
    createDialogVisible.value = true
  }
  const handleCloseCreate = () => {
    createDialogVisible.value = false
    createFormRef.value?.resetFields()
  }
  const handleCreateSubmit = () => {
    createFormRef.value?.validate(async (valid) => {
      if (valid) {
        createLoading.value = true
        try {
          const categoryId = Array.isArray(createForm.categoryId)
            ? createForm.categoryId[createForm.categoryId.length - 1]
            : createForm.categoryId

          await createAttrApi({
            ...createForm,
            categoryId,
          })
          ElMessage.success('创建成功')
          createDialogVisible.value = false
          fetchAttrList()
        } catch (error) {
          console.error('创建属性失败:', error)
        } finally {
          createLoading.value = false
        }
      }
    })
  }

  const handleEdit = (row: AttrItem) => {
    editForm.attrId = row.attrId
    editForm.attrName = row.attrName
    editForm.attrType = row.attrType
    editForm.valueType = row.valueType
    editForm.valueList = row.valueList || ''
    editForm.categoryId = row.categoryId ?? 0
    editDialogVisible.value = true
  }
  const handleCloseEdit = () => {
    editDialogVisible.value = false
    editFormRef.value?.resetFields()
  }
  const handleEditSubmit = () => {
    editFormRef.value?.validate(async (valid) => {
      if (valid) {
        editLoading.value = true
        try {
          await updateAttrApi({
            ...editForm,
            categoryId: editForm.categoryId,
          })
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          fetchAttrList()
        } catch (error) {
          console.error('更新属性失败:', error)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: AttrItem) => {
    ElMessageBox.confirm(`确定要删除属性 "${row.attrName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteAttrApi([row.attrId])
        ElMessage.success('删除成功')
        fetchAttrList()
      } catch (error) {
        console.error('删除属性失败:', error)
      }
    })
  }

  onMounted(() => {
    fetchCategories()
    fetchAttrList()
  })
</script>
