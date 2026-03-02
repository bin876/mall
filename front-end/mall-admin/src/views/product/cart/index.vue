<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="用户ID">
        <el-input
          v-model.number="queryParams.memberId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter="() => fetchCartList()"
        />
      </el-form-item>

      <el-form-item label="SKU名称">
        <el-input
          v-model="queryParams.skuName"
          placeholder="请输入SKU名称"
          clearable
          @keyup.enter="() => fetchCartList()"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchCartList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="mb-4 flex items-center">
      <el-button
        type="danger"
        @click="handleBatchDelete"
        :disabled="selectedCartIds.length === 0"
        :loading="batchDeleteLoading"
      >
        批量删除
      </el-button>
      <el-button
        type="warning"
        @click="handleClearUserCart"
        :disabled="!queryParams.memberId"
        :loading="clearLoading"
        class="ml-2"
      >
        清空用户购物车
      </el-button>

      <div class="ml-auto text-sm text-gray-500">共 {{ total }} 条记录</div>
    </div>

    <el-table
      :data="cartList"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
      v-loading="loading"
    >
      <el-table-column type="selection" width="50" />

      <el-table-column prop="cartId" label="购物车ID" width="120" />

      <el-table-column label="用户信息" min-width="150">
        <template #default="scope">
          <div>
            <div class="font-medium">ID: {{ scope.row.memberId }}</div>
            <div class="text-gray-600">用户名: {{ scope.row.memberUsername }}</div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="商品信息" min-width="220">
        <template #default="scope">
          <div class="flex items-center">
            <img
              v-if="scope.row.skuPic"
              :src="scope.row.skuPic"
              alt="商品图片"
              class="cart-image"
              @error="handleImageError(scope.row)"
            />
            <span v-else class="no-image">-</span>
            <div class="ml-3">
              <div class="font-medium line-clamp-2">{{ scope.row.skuName }}</div>
              <div class="text-gray-500 text-sm">SKU ID: {{ scope.row.skuId }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="价格" width="100">
        <template #default="scope">
          <span class="text-red-600 font-medium">¥{{ formatPrice(scope.row.skuPrice) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="数量" width="80">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.quantity }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.checked ? 'success' : 'danger'">
            {{ scope.row.checked ? '已选中' : '未选中' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="更新时间" width="180">
        <template #default="scope">
          {{ scope.row.updateTime }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100" fixed="right">
        <template #default="scope">
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
  </el-card>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { CartQueryParams, CartItem } from '@/api/cart/type'
  import { cartListApi, batchDeleteCartsApi, clearUserCartApi } from '@/api/cart'

  const route = useRoute()

  const queryParams = reactive<CartQueryParams>({
    memberId: null,
    skuName: '',
    pageNum: 1,
    pageSize: 10,
  })

  const cartList = ref<CartItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const selectedCartIds = ref<number[]>([])

  const loading = ref(false)
  const batchDeleteLoading = ref(false)
  const clearLoading = ref(false)

  const fetchCartLine = async (page: number = 1, size: number = 10) => {
    loading.value = true
    try {
      const params: CartQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await cartListApi(params)
      cartList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取购物车列表失败'
      ElMessage.error(errorMsg)
      console.error('获取购物车列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      memberId: null,
      skuName: '',
      pageNum: 1,
      pageSize: 10,
    })
    fetchCartList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchCartList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchCartList(page, pageSize.value)
  }

  const handleSelectionChange = (selection: CartItem[]) => {
    selectedCartIds.value = selection.map((cart) => cart.cartId)
  }

  const formatPrice = (price: number): string => {
    return price.toFixed(2)
  }

  const handleImageError = (row: CartItem) => {
    row.skuPic = ''
  }

  const handleBatchDelete = () => {
    if (selectedCartIds.value.length === 0) return

    ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCartIds.value.length} 项购物车数据吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(async () => {
      batchDeleteLoading.value = true
      try {
        await batchDeleteCartsApi(selectedCartIds.value)
        ElMessage.success('批量删除成功')
        fetchCartList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '批量删除失败'
        ElMessage.error(errorMsg)
      } finally {
        batchDeleteLoading.value = false
      }
    })
  }

  const handleClearUserCart = () => {
    if (!queryParams.memberId) {
      ElMessage.warning('请先输入用户ID')
      return
    }

    ElMessageBox.confirm(`确定要清空用户 ID ${queryParams.memberId} 的所有购物车数据吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      clearLoading.value = true
      try {
        await clearUserCartApi(queryParams.memberId!)
        ElMessage.success('清空购物车成功')
        fetchCartList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '清空购物车失败'
        ElMessage.error(errorMsg)
      } finally {
        clearLoading.value = false
      }
    })
  }

  const handleDelete = (cart: CartItem) => {
    ElMessageBox.confirm(`确定要删除该购物车项吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await batchDeleteCartsApi([cart.cartId])
        ElMessage.success('删除成功')
        fetchCartList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  const fetchCartList = fetchCartLine

  onMounted(() => {
    fetchCartList()
  })
</script>

<style scoped>
  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .cart-image {
    width: 48px;
    height: 48px;
    object-fit: contain;
    border-radius: 4px;
  }

  .no-image {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 24px;
  }

  .ml-3 {
    margin-left: 12px;
  }
</style>
