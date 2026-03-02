<template>
  <NavBar />

  <div class="search-container">
    <div class="search-section">
      <div class="search-container-inner">
        <div class="search-box">
          <input
            v-model="searchParams.keyword"
            type="text"
            placeholder="请输入商品名称"
            @keyup.enter="handleSearch"
          />
          <button @click="handleSearch">搜索</button>
        </div>
        <div class="cart-button">
          <router-link to="/cart">我的购物车</router-link>
        </div>
      </div>
    </div>

    <div class="filter-section">
      <div class="mb-2">分类筛选</div>
      <div class="category-selector">
        <el-select
          v-model="selectedFirstLevel"
          placeholder="请选择一级分类"
          @change="handleFirstLevelChange"
          style="width: 200px"
        >
          <el-option
            v-for="category in categories"
            :key="category.categoryId"
            :label="category.name"
            :value="category.categoryId"
          />
        </el-select>

        <el-select
          v-model="selectedSecondLevel"
          placeholder="请选择二级分类"
          @change="handleSecondLevelChange"
          :disabled="!selectedFirstLevel"
          style="width: 200px; margin-left: 10px"
        >
          <el-option
            v-for="category in secondLevelCategories"
            :key="category.categoryId"
            :label="category.name"
            :value="category.categoryId"
          />
        </el-select>

        <el-select
          v-model="searchParams.categoryId"
          placeholder="请选择三级分类"
          :disabled="!selectedSecondLevel"
          @change="handleThirdLevelChange"
          style="width: 200px; margin-left: 10px"
        >
          <el-option
            v-for="category in thirdLevelCategories"
            :key="category.categoryId"
            :label="category.name"
            :value="category.categoryId"
          />
        </el-select>

        <el-button @click="clearCategory" style="margin-left: 10px">清空</el-button>
      </div>
    </div>

    <div class="filter-section">
      <div>品牌筛选</div>
      <div class="brand-selector">
        <div
          v-for="brand in brands"
          :key="brand.id"
          class="brand-item"
          :class="{ selected: searchParams.brandId === brand.id }"
          @click="selectBrand(brand.id)"
        >
          <img :src="brand.logo" :alt="brand.name" />
          <span>{{ brand.name }}</span>
        </div>
        <div v-if="brands.length === 0" class="no-brands">暂无品牌</div>
        <!-- <el-button @click="clearBrand" style="margin-top: 10px">清空品牌</el-button> -->
      </div>
    </div>

    <div class="filter-section">
      <div class="price-sort-container">
        <div class="price-range">
          <div>价格区间</div>
          <div class="price-inputs">
            <el-input
              v-model="searchParams.minPrice"
              placeholder="最低价"
              style="width: 120px"
              @input="handlePriceInput"
            />
            <span style="margin: 0 10px">-</span>
            <el-input
              v-model="searchParams.maxPrice"
              placeholder="最高价"
              style="width: 120px"
              @input="handlePriceInput"
            />
          </div>
          <el-button @click="clearPrice" style="margin-left: 10px">清空</el-button>
        </div>

        <div class="sort-options">
          <div>排序</div>
          <el-select v-model="searchParams.sort" placeholder="综合排序" style="width: 150px">
            <el-option value="default" label="综合" />
            <el-option value="sales_desc" label="销量" />
            <el-option value="price_asc" label="价格升序" />
            <el-option value="price_desc" label="价格降序" />
          </el-select>
        </div>
      </div>
    </div>

    <div class="product-section">
      <div class="product-filters">
        <span>共 {{ total }} 件商品</span>
        <el-button @click="resetAllFilters">清空所有筛选</el-button>
      </div>

      <div class="product-list">
        <div v-for="spu in spuList" :key="spu.spuId" class="product-item">
          <router-link :to="`/product/${spu.spuId}`">
            <img :src="spu.defaultImg" :alt="spu.name" />
            <div>
              <div>
                <p class="product-name">{{ spu.name }}</p>
              </div>
              <div class="flex items-baseline">
                <div class="product-price">¥{{ spu.minPrice }}</div>
                <div class="product-sales pl-2">销量 {{ spu.saleCount }}</div>
              </div>
            </div>
          </router-link>
        </div>
      </div>

      <div class="pagination flex justify-center">
        <el-pagination
          v-model:current-page="searchParams.pageNum"
          v-model:page-size="searchParams.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted, watch } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { getCategoryTreeApi, getBrandListApi, getProductListApi } from '@/api/product'
  import type { CategoryTree } from '@/api/product/type'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()
  const route = useRoute()

  const categories = ref<CategoryTree[]>([])
  const secondLevelCategories = ref<CategoryTree[]>([])
  const thirdLevelCategories = ref<CategoryTree[]>([])
  const selectedFirstLevel = ref<number | null>(null)
  const selectedSecondLevel = ref<number | null>(null)

  const brands = ref<any[]>([])

  const searchParams = reactive<any>({
    categoryId: undefined,
    brandId: undefined,
    keyword: '',
    minPrice: undefined,
    maxPrice: undefined,
    sort: 'default',
    pageNum: 1,
    pageSize: 10,
  })

  const spuList = ref<any[]>([])
  const total = ref(0)

  onMounted(async () => {
    const query = route.query
    if (query.categoryId) {
      searchParams.categoryId = Number(query.categoryId)
      await loadCategoryPath()
      await loadBrands()
    }
    if (query.keyword) {
      searchParams.keyword = query.keyword as string
    }

    await loadCategories()

    await loadProducts()
  })

  watch(
    () => ({ ...searchParams }),
    () => {
      searchParams.pageNum = 1
      loadProducts()
    },
    { deep: true }
  )

  // 加载分类树
  const loadCategories = async () => {
    try {
      const data = await getCategoryTreeApi()
      categories.value = data
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }

  // 一级分类变化
  const handleFirstLevelChange = (categoryId: number) => {
    selectedFirstLevel.value = categoryId
    selectedSecondLevel.value = null
    searchParams.categoryId = undefined
    secondLevelCategories.value = []
    thirdLevelCategories.value = []
    brands.value = []
    spuList.value = []
    total.value = 0

    // 获取二级分类
    const first = categories.value.find((c) => c.categoryId === categoryId)
    if (first && first.children) {
      secondLevelCategories.value = first.children
    }
  }

  // 二级分类变化
  const handleSecondLevelChange = (categoryId: number) => {
    selectedSecondLevel.value = categoryId
    searchParams.categoryId = undefined
    thirdLevelCategories.value = []
    brands.value = []

    // 获取三级分类
    const second = secondLevelCategories.value.find((c) => c.categoryId === categoryId)
    if (second && second.children) {
      thirdLevelCategories.value = second.children
    }
  }

  // 加载品牌（关键修正！）
  const loadBrands = async () => {
    if (!searchParams.categoryId) return

    // 关键：获取三级分类对应的二级分类ID
    const secondLevelId = findSecondLevelId(searchParams.categoryId)
    if (!secondLevelId) return

    try {
      const data = await getBrandListApi({
        categoryId: secondLevelId, // ← 使用二级分类ID查询品牌
        pageNum: 1,
        pageSize: 100,
      })
      console.log(data)

      brands.value = data.records || []
    } catch (error) {
      console.error('加载品牌失败:', error)
    }
  }

  // 根据三级分类ID找二级分类ID
  const findSecondLevelId = (thirdId: number): number | null => {
    for (const first of categories.value) {
      for (const second of first.children || []) {
        if (second.children?.some((third) => third.categoryId === thirdId)) {
          return second.categoryId
        }
      }
    }
    return null
  }

  // 选择品牌
  const selectBrand = (brandId: number) => {
    searchParams.brandId = brandId === searchParams.brandId ? undefined : brandId
  }

  // 价格输入处理
  const handlePriceInput = () => {
    if (searchParams.minPrice !== undefined && searchParams.minPrice !== '') {
      searchParams.minPrice = Number(searchParams.minPrice)
    } else {
      searchParams.minPrice = undefined
    }
    if (searchParams.maxPrice !== undefined && searchParams.maxPrice !== '') {
      searchParams.maxPrice = Number(searchParams.maxPrice)
    } else {
      searchParams.maxPrice = undefined
    }
  }

  // 加载商品
  const loadProducts = async () => {
    try {
      const data = await getProductListApi(searchParams)

      console.log(data.records)

      spuList.value = data.records || []
      total.value = data.total || 0
    } catch (error) {
      console.error('加载商品失败:', error)
    }
  }

  const handlePageChange = () => {
    loadProducts()
  }
  const handlePageSizeChange = () => {
    loadProducts()
  }

  const handleThirdLevelChange = async (categoryId: number) => {
    searchParams.categoryId = categoryId
    await loadBrands()
  }

  const clearCategory = () => {
    selectedFirstLevel.value = null
    selectedSecondLevel.value = null
    searchParams.categoryId = undefined
    secondLevelCategories.value = []
    thirdLevelCategories.value = []
    brands.value = []
    spuList.value = []
    total.value = 0
  }

  const clearBrand = () => {
    searchParams.brandId = undefined
  }

  const clearPrice = () => {
    searchParams.minPrice = undefined
    searchParams.maxPrice = undefined
  }

  const resetAllFilters = () => {
    Object.assign(searchParams, {
      categoryId: undefined,
      brandId: undefined,
      keyword: '',
      minPrice: undefined,
      maxPrice: undefined,
      sort: 'default',
      pageNum: 1,
      pageSize: 10,
    })
    selectedFirstLevel.value = null
    selectedSecondLevel.value = null
    secondLevelCategories.value = []
    thirdLevelCategories.value = []
    brands.value = []
  }

  const handleSearch = () => {
    searchParams.pageNum = 1
    loadProducts()
  }

  const loadCategoryPath = async () => {
    if (!searchParams.categoryId) return

    for (const first of categories.value) {
      for (const second of first.children || []) {
        const third = second.children?.find((c) => c.categoryId === searchParams.categoryId)
        if (third) {
          selectedFirstLevel.value = first.categoryId
          secondLevelCategories.value = first.children || []
          selectedSecondLevel.value = second.categoryId
          thirdLevelCategories.value = second.children || []
          return
        }
      }
    }
  }
</script>

<style scoped>
  .search-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .search-section {
    padding: 20px 0;
    background: #fff;
    margin-bottom: 20px;
  }

  .search-container-inner {
    max-width: 800px;
    margin: 0 auto;
    display: flex;
    gap: 20px;
    align-items: center;
  }

  .search-box {
    flex: 1;
    display: flex;
  }

  .search-box input {
    flex: 1;
    height: 36px;
    padding: 0 12px;
    border: 1px solid #ccc;
    border-right: none;
    font-size: 14px;
  }

  .search-box button {
    height: 36px;
    padding: 0 20px;
    background: #e40000;
    color: #fff;
    border: 1px solid #e40000;
    cursor: pointer;
    font-size: 14px;
  }

  .cart-button a {
    display: inline-block;
    padding: 8px 15px;
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    text-decoration: none;
    color: #333;
    font-size: 14px;
  }

  .filter-section {
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    padding: 15px;
    margin-bottom: 20px;
  }

  .filter-section h3 {
    font-size: 16px;
    margin: 0 0 15px 0;
    color: #333;
  }

  .category-selector {
    display: flex;
    align-items: center;
  }

  .brand-selector {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    padding: 10px 0;
  }

  .brand-item {
    width: 80px;
    text-align: center;
    cursor: pointer;
    padding: 10px;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    transition: all 0.2s;
  }

  .brand-item:hover {
    border-color: #999;
  }

  .brand-item.selected {
    border-color: #e40000;
    background: #fef0f0;
  }

  .brand-item img {
    width: 40px;
    height: 40px;
    object-fit: contain;
    margin-bottom: 5px;
  }

  .brand-item span {
    font-size: 12px;
    color: #333;
  }

  .no-brands {
    color: #999;
    font-size: 14px;
  }

  .price-sort-container {
    display: flex;
    gap: 30px;
    align-items: flex-end;
  }

  .price-range,
  .sort-options {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .price-inputs {
    display: flex;
    align-items: center;
  }

  .product-section {
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    padding: 20px;
  }

  .product-filters {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
  }

  .product-list {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-bottom: 20px;
  }

  .product-item {
    width: 200px;
    border: 1px solid #eee;
    padding: 10px;
  }

  .product-item img {
    width: 100%;
    height: 150px;
    object-fit: cover;
    margin-bottom: 8px;
  }

  .product-name {
    font-weight: bold;
    margin: 0 0 8px 0;
    font-size: 14px;
    line-height: 1.4;
  }

  .product-price {
    color: #e40000;
    font-size: 16px;
    margin: 0 0 5px 0;
  }

  .product-sales {
    color: #666;
    font-size: 12px;
    margin: 0;
  }

  .pagination {
    text-align: right;
  }
</style>
