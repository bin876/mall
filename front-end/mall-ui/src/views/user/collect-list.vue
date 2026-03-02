<template>
  <NavBar />

  <div class="user-layout">
    <UserSidebar />
    <div class="main-content">
      <div class="collect-container">
        <h2 class="page-title">我的收藏</h2>

        <div v-if="collects.length > 0" class="collect-list">
          <div class="collect-header">
            <span>共 {{ total }} 件商品</span>
            <el-button
              type="danger"
              size="small"
              :disabled="selectedIds.length === 0"
              @click="batchDelete"
            >
              批量删除
            </el-button>
          </div>

          <el-checkbox-group v-model="selectedIds">
            <div v-for="collect in collects" :key="collect.collectId" class="collect-item">
              <el-checkbox :label="collect.collectId" />
              <div class="collect-content">
                <div class="collect-image">
                  <img :src="collect.spuPic || '/default-product.jpg'" alt="商品图片" />
                </div>
                <div class="collect-info">
                  <h3 class="collect-name">{{ collect.spuName }}</h3>
                  <div class="collect-price">¥{{ collect.spuPrice.toFixed(2) }}</div>
                  <div class="collect-time">收藏时间: {{ formatDate(collect.createTime) }}</div>
                </div>
              </div>
              <div class="collect-actions">
                <el-button type="primary" size="small" @click="goToProduct(collect.spuId)">
                  查看商品
                </el-button>
                <el-button type="danger" size="small" @click="deleteCollect(collect.collectId)">
                  删除
                </el-button>
              </div>
            </div>
          </el-checkbox-group>
        </div>

        <div v-else class="empty-collect">
          <el-empty description="暂无收藏商品" />
          <router-link to="/">
            <el-button type="primary" style="margin-top: 20px">去逛逛</el-button>
          </router-link>
        </div>

        <el-pagination
          v-if="total > 0"
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadCollects"
          class="pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { useRouter } from 'vue-router'
  import { getCollectListApi, cancelCollectApi, batchCancelCollectApi } from '@/api/collect'
  import type { CollectDTO } from '@/api/collect/type'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()

  const collects = ref<CollectDTO[]>([])
  const selectedIds = ref<number[]>([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  // 加载收藏列表
  const loadCollects = async (page = currentPage.value) => {
    try {
      const res = await getCollectListApi(page, pageSize.value)
      collects.value = res.records
      total.value = res.total
      currentPage.value = page
      selectedIds.value = [] // 清空选择
    } catch (error) {
      console.error('加载收藏列表失败:', error)
      ElMessage.error('加载失败')
    }
  }

  // 格式化时间
  const formatDate = (dateString: string): string => {
    return dateString.replace('T', ' ').replace(/\.\d+/, '')
  }

  // 跳转到商品详情
  const goToProduct = (spuId: number) => {
    router.push(`/product/${spuId}`)
  }

  // 删除单个收藏
  const deleteCollect = async (collectId: number) => {
    ElMessageBox.confirm('确定要删除这个收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await cancelCollectApi(collectId)
        ElMessage.success('删除成功')
        loadCollects()
      } catch (error: any) {
        ElMessage.error(error.msg || '删除失败')
      }
    })
  }

  const batchDelete = async () => {
    if (selectedIds.value.length === 0) return

    ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个收藏吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await batchCancelCollectApi(selectedIds.value)
        ElMessage.success('批量删除成功')
        loadCollects()
      } catch (error: any) {
        ElMessage.error(error.msg || '批量删除失败')
      }
    })
  }

  onMounted(() => {
    loadCollects()
  })
</script>

<style scoped>
  .collect-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .page-title {
    font-size: 24px;
    color: #333;
    margin-bottom: 20px;
  }

  .collect-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
  }

  .collect-list {
    background: #fff;
    border-radius: 4px;
    padding: 20px;
  }

  .collect-item {
    display: flex;
    align-items: center;
    padding: 15px 0;
    border-bottom: 1px solid #f5f5f5;
  }

  .collect-item:last-child {
    border-bottom: none;
  }

  .collect-content {
    display: flex;
    flex: 1;
    gap: 20px;
    margin: 0 20px;
  }

  .collect-image {
    width: 100px;
    height: 100px;
    flex-shrink: 0;
  }

  .collect-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border: 1px solid #eee;
  }

  .collect-info {
    flex: 1;
  }

  .collect-name {
    font-size: 16px;
    color: #333;
    margin-bottom: 10px;
    line-height: 1;
  }

  .collect-price {
    color: #e40000;
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;

    line-height: 1;
  }

  .collect-time {
    color: #999;
    font-size: 12px;
  }

  .collect-actions {
    display: flex;
    gap: 10px;
  }

  .empty-collect {
    text-align: center;
    padding: 60px 0;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
</style>
