<template>
  <div class="home-container">
    <NavBar />

    <div class="search-section">
      <div class="search-container">
        <div class="search-box">
          <input
            v-model="searchKeyword"
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

    <div class="main-section">
      <aside class="sidebar">
        <div
          class="category-item"
          v-for="category in categories"
          :key="category.categoryId"
          @mouseenter="activeCategory = category.categoryId"
          @mouseleave="activeCategory = null"
        >
          <div class="category-name">
            {{ category.name }}
          </div>
          <div class="sub-categories" v-if="activeCategory === category.categoryId">
            <div class="sub-categories-container">
              <div class="sub-category-list">
                <div
                  v-for="child in category.children"
                  :key="child.categoryId"
                  class="sub-category-item"
                >
                  {{ child.name }}
                </div>
              </div>
              <div class="grand-category-list">
                <div
                  v-for="child in category.children"
                  :key="child.categoryId"
                  class="grand-category-row"
                >
                  <template v-for="(grand, index) in child.children" :key="grand.categoryId">
                    <router-link :to="`/search?categoryId=${grand.categoryId}`">
                      {{ grand.name }}
                    </router-link>
                  </template>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>

      <main class="main-content">
        <div class="home-banner" v-if="banners.length > 0">
          <el-carousel
            height="300px"
            indicator-position="outside"
            :autoplay="true"
            :interval="5000"
          >
            <el-carousel-item v-for="banner in banners" :key="banner.bannerId">
              <a :href="banner.targetUrl" target="_blank" rel="noopener noreferrer">
                <img :src="banner.imageUrl" :alt="banner.title" />
              </a>
            </el-carousel-item>
          </el-carousel>
        </div>
        <div v-else class="banner-placeholder">轮播图</div>
      </main>

      <aside class="user-panel">
        <div v-if="userStore.token" class="user-row">
          <img
            :src="userStore.userInfo?.avatarUrl || '/default-avatar.png'"
            alt="头像"
            @click="goToProfile"
            style="cursor: pointer"
          />
          <span @click="goToProfile" style="cursor: pointer">
            {{ userStore.userInfo?.username }}
          </span>
        </div>
        <div v-else class="user-row">
          <div>欢迎登录</div>
          <router-link to="/login" style="font-size: 12px; color: #e40000">立即登录</router-link>
        </div>

        <div v-if="userStore.token" class="user-row">
          <span @click="handleSwitchAccount">切换账号</span>
          <span @click="handleLogout" class="logout-link">退出</span>
        </div>
        <div v-else class="user-row">
          <router-link to="/register" style="font-size: 12px">立即注册</router-link>
        </div>

        <div class="user-row">
          <router-link to="/user/orders">
            <span>我的订单</span>
          </router-link>
          <router-link to="/user/collect">
            <span>我的收藏</span>
          </router-link>
          <router-link to="/">
            <span>浏览记录</span>
          </router-link>
        </div>
      </aside>
    </div>

    <div class="recommend-section">
      <h2>推荐商品</h2>
      <div class="product-list flex justify-center">
        <div v-for="spu in spuList" :key="spu.spuId" class="product-item">
          <router-link :to="`/product/${spu.spuId}`">
            <img :src="spu.defaultImg" :alt="spu.name" />
            <div>
              <div>
                <p class="product-name">{{ spu.name }}</p>
              </div>
              <div class="flex justify-between">
                <div class="flex items-baseline">
                  <div class="product-price">¥{{ spu.minPrice }}</div>
                  <div class="product-sales pl-2">销量 {{ spu.saleCount }}</div>
                </div>
              </div>
            </div>
          </router-link>
        </div>
      </div>

      <div class="pagination flex justify-center">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { useUserStore } from '@/stores/user'
  import { getCategoryTreeApi, getRecommendSpuApi } from '@/api/product'
  import { getBannerListApi } from '@/api/banner'
  import type { CategoryTree, SPU } from '@/api/product/type'
  import type { BannerItem } from '@/api/banner/type'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()
  const userStore = useUserStore()

  const banners = ref<BannerItem[]>([])

  const searchKeyword = ref('')
  const handleSearch = () => {
    if (searchKeyword.value.trim()) {
      router.push(`/search?keyword=${encodeURIComponent(searchKeyword.value)}`)
    }
  }

  const categories = ref<CategoryTree[]>([])
  const activeCategory = ref<number | null>(null)

  const spuList = ref<SPU[]>([])
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  const goToProfile = () => {
    router.push('/user/profile')
  }

  const loadBanners = async () => {
    try {
      const res = await getBannerListApi()
      banners.value = res || []
    } catch (error) {
      console.error('加载轮播图失败:', error)
    }
  }

  onMounted(async () => {
    try {
      const categoryData = await getCategoryTreeApi()
      categories.value = categoryData

      await Promise.all([loadSpuList(), loadBanners()])

      if (userStore.token) {
        await userStore.fetchUserInfo()
      }
    } catch (error) {
      console.error('首页初始化失败:', error)
    }
  })

  async function loadSpuList() {
    try {
      const data = await getRecommendSpuApi({
        pageNum: page.value,
        pageSize: pageSize.value,
      })
      spuList.value = data.records

      console.log(spuList.value)

      total.value = data.total
    } catch (error) {
      console.error('加载商品失败:', error)
    }
  }

  const handlePageChange = (newPage: number) => {
    page.value = newPage
    loadSpuList()
  }

  const handleSizeChange = (newSize: number) => {
    pageSize.value = newSize
    loadSpuList()
  }

  const handleLogout = async () => {
    await userStore.logout()
    router.push('/login')
  }

  const handleSwitchAccount = async () => {
    await userStore.logout()
    router.push('/login')
  }
</script>

<style scoped>
  .home-container {
    min-height: 100vh;
  }

  .search-section {
    padding: 20px 0;
    background: #fff;
  }

  .search-container {
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

  /* 主体区域 */
  .main-section {
    display: flex;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px 0;
    gap: 20px;
  }

  /* 左：侧边分类 */
  .sidebar {
    width: 200px;
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    flex-shrink: 0;
  }

  .category-item {
    position: relative;
    padding: 8px 15px;
    cursor: pointer;
  }

  .category-item:hover {
    background: #f5f5f5;
  }

  .category-name {
    font-size: 14px;
  }

  .sub-categories {
    position: absolute;
    top: 0;
    left: 100%;
    width: 500px;
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    padding: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    z-index: 10;
  }

  .sub-categories-container {
    display: flex;
    gap: 20px;
  }

  .sub-category-list {
    width: 100px;
    border-right: 1px solid #e5e5e5;
    padding-right: 10px;
  }

  .sub-category-item {
    padding: 5px 0;
    font-size: 12px;
    color: #333;
  }

  .grand-category-list {
    flex: 1;
    padding-left: 10px;
  }

  .grand-category-row {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    margin-bottom: 8px;
  }

  .grand-category-row a {
    color: #333;
    text-decoration: none;
    white-space: nowrap;
  }

  .grand-category-row a:hover {
    color: #e40000;
  }

  /* 中：主内容区 */
  .main-content {
    flex: 1;
  }

  .home-banner {
    width: 100%;
    border-radius: 4px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .home-banner img {
    width: 100%;
    height: 300px;
    object-fit: cover;
    display: block;
  }

  .banner-placeholder {
    height: 300px;
    background: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
  }

  /* 右：用户区域 */
  .user-panel {
    width: 200px;
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    padding: 15px;
    flex-shrink: 0;
  }

  .user-row {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 15px;
    align-items: center;
  }

  .user-row img {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    margin-right: 10px;
  }

  .user-row span {
    font-size: 12px;
    cursor: pointer;
  }

  .user-row span:hover {
    color: #e40000;
  }

  .logout-link {
    color: #e40000;
  }

  .logout-link:hover {
    text-decoration: underline;
  }

  .recommend-section {
    max-width: 1200px;
    margin: 0 auto 40px;
    padding: 0 20px;
  }

  .recommend-section h2 {
    font-size: 20px;
    margin: 20px 0;
    color: #333;
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
  }

  .product-price {
    color: #e40000;
    font-size: 16px;
    margin: 0;
  }

  .product-sales {
    color: #666;
    font-size: 12px;
    margin: 0;
  }
</style>
