<template>
  <NavBar />

  <div class="cart-container">
    <h1 class="page-title">我的购物车</h1>

    <!-- 购物车列表 -->
    <div class="cart-table">
      <!-- 表头 -->
      <div class="cart-header">
        <div class="cart-col select-col">
          <label class="checkbox-container">
            <input type="checkbox" :checked="allChecked" @change="toggleAll" />
            <span class="checkmark"></span>
          </label>
          <span>全选</span>
        </div>
        <div class="cart-col product-col">商品</div>
        <div class="cart-col price-col">单价</div>
        <div class="cart-col subtotal-col">小计</div>
        <div class="cart-col operate-col">操作</div>
      </div>

      <!-- 商品项 -->
      <div v-for="item in cartItems" :key="item.cartId" class="cart-item">
        <!-- 选中框 -->
        <div class="cart-col select-col">
          <label class="checkbox-container">
            <input
              type="checkbox"
              :checked="item.checked"
              @change="updateChecked(item.cartId, !item.checked)"
            />
            <span class="checkmark"></span>
          </label>
        </div>

        <!-- 商品信息 -->
        <div class="cart-col product-col">
          <div class="product-info">
            <img :src="item.skuPic" alt="商品图片" />
            <div class="product-details">
              <h3>{{ item.skuName }}</h3>
              <!-- 数量控制 -->
              <div class="quantity-selector">
                <button
                  @click="updateQuantity(item.cartId, item.quantity - 1)"
                  :disabled="item.quantity <= 1"
                >
                  -
                </button>
                <input
                  type="text"
                  :value="item.quantity"
                  @input="updateQuantityInput(item.cartId, $event)"
                  readonly
                />
                <button @click="updateQuantity(item.cartId, item.quantity + 1)">+</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 单价 -->
        <div class="cart-col price-col">¥{{ item.skuPrice.toFixed(2) }}</div>

        <!-- 小计 -->
        <div class="cart-col subtotal-col">¥{{ (item.skuPrice * item.quantity).toFixed(2) }}</div>

        <!-- 操作 -->
        <div class="cart-col operate-col">
          <button @click="deleteItem(item.cartId)" class="delete-btn">删除</button>
        </div>
      </div>
    </div>

    <!-- 结算栏 -->
    <div class="cart-footer">
      <div class="footer-left">
        <label class="checkbox-container">
          <input type="checkbox" :checked="allChecked" @change="toggleAll" />
          <span class="checkmark"></span>
        </label>
        <span>全选</span>
      </div>

      <div class="footer-right">
        <div class="total-info">
          <span>合计：¥{{ totalAmount }}</span>
          <span class="total-count">（共 {{ checkedCount }} 件）</span>
        </div>
        <button @click="goToConfirm" :disabled="!hasCheckedItems" class="checkout-btn">
          去结算
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { getCartItemsApi, updateCartApi, deleteCartApi } from '@/api/order'
  import type { CartItem } from '@/api/order/type'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()
  const cartItems = ref<CartItem[]>([])

  // 加载购物车
  onMounted(async () => {
    try {
      const items = await getCartItemsApi()
      cartItems.value = items
    } catch (error) {
      console.error('加载购物车失败:', error)
    }
  })

  const allChecked = computed(() => {
    return cartItems.value.length > 0 && cartItems.value.every((item) => item.checked)
  })

  const toggleAll = async () => {
    const newChecked = !allChecked.value
    try {
      for (const item of cartItems.value) {
        await updateCartApi({ cartId: item.cartId, checked: newChecked })
      }
      cartItems.value = await getCartItemsApi()
    } catch (error) {
      console.error('更新全选失败:', error)
    }
  }

  async function updateQuantity(cartId: number, quantity: number) {
    if (quantity < 1) return
    try {
      await updateCartApi({ cartId, quantity })
      cartItems.value = await getCartItemsApi()
    } catch (error) {
      console.error('更新数量失败:', error)
    }
  }

  // 输入框更新数量
  async function updateQuantityInput(cartId: number, e: Event) {
    const value = parseInt((e.target as HTMLInputElement).value)
    if (!isNaN(value) && value > 0) {
      await updateQuantity(cartId, value)
    }
  }

  // 更新选中状态
  async function updateChecked(cartId: number, checked: boolean) {
    try {
      await updateCartApi({ cartId, checked })
      cartItems.value = await getCartItemsApi()
    } catch (error) {
      console.error('更新选中状态失败:', error)
    }
  }

  // 删除商品
  async function deleteItem(cartId: number) {
    try {
      await deleteCartApi(cartId)
      cartItems.value = await getCartItemsApi()
    } catch (error) {
      console.error('删除商品失败:', error)
    }
  }

  // 计算数据
  const checkedItems = computed(() => cartItems.value.filter((item) => item.checked))

  const totalAmount = computed(() =>
    checkedItems.value.reduce((sum, item) => sum + item.skuPrice * item.quantity, 0).toFixed(2)
  )

  const checkedCount = computed(() =>
    checkedItems.value.reduce((count, item) => count + item.quantity, 0)
  )

  const hasCheckedItems = computed(() => checkedItems.value.length > 0)

  // 去结算
  function goToConfirm() {
    const checkedCartIds = cartItems.value.filter((item) => item.checked).map((item) => item.cartId)

    router.push({
      path: '/order/confirm',
      query: { cartIds: checkedCartIds.join(',') },
    })
  }
</script>

<style scoped>
  .cart-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0 0 20px 0;
  }

  /* 购物车表格 */
  .cart-table {
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    overflow: hidden;
    background: #fff;
  }

  /* 表头 */
  .cart-header {
    display: flex;
    background: #f5f5f5;
    border-bottom: 1px solid #e5e5e5;
    padding: 12px 15px;
    font-weight: bold;
    color: #333;
  }

  /* 商品项 */
  .cart-item {
    display: flex;
    padding: 15px;
    border-bottom: 1px solid #eee;
  }

  .cart-item:last-child {
    border-bottom: none;
  }

  /* 列定义 */
  .cart-col {
    display: flex;
    align-items: center;
    padding: 0 10px;
  }

  .select-col {
    width: 80px;
    justify-content: center;
  }

  .product-col {
    flex: 1;
  }

  .price-col {
    width: 100px;
    justify-content: flex-end;
  }

  .subtotal-col {
    width: 100px;
    justify-content: flex-end;
    color: #e40000;
    font-weight: bold;
  }

  .operate-col {
    width: 80px;
    justify-content: center;
  }

  /* 商品信息 */
  .product-info {
    display: flex;
    gap: 15px;
    align-items: flex-start;
    width: 100%;
  }

  .product-info img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border: 1px solid #eee;
  }

  .product-details {
    flex: 1;
  }

  .product-details h3 {
    font-size: 14px;
    font-weight: normal;
    color: #333;
    margin: 0 0 10px 0;
    line-height: 1.4;
  }

  /* 数量选择器 */
  .quantity-selector {
    display: flex;
    align-items: center;
    border: 1px solid #ccc;
    border-radius: 4px;
    overflow: hidden;
    width: fit-content;
  }

  .quantity-selector button {
    width: 28px;
    height: 28px;
    background: #f5f5f5;
    border: none;
    cursor: pointer;
    font-size: 14px;
    font-weight: bold;
  }

  .quantity-selector button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .quantity-selector input {
    width: 40px;
    height: 28px;
    text-align: center;
    border: none;
    outline: none;
    font-size: 12px;
  }

  /* 删除按钮 */
  .delete-btn {
    background: none;
    border: none;
    color: #999;
    cursor: pointer;
    font-size: 12px;
    padding: 0;
  }

  .delete-btn:hover {
    color: #e40000;
  }

  /* 结算栏 */
  .cart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    padding: 15px 20px;
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
  }

  .footer-left {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
  }

  .footer-right {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .total-info {
    font-size: 16px;
    color: #333;
  }

  .total-count {
    color: #666;
    font-size: 14px;
    margin-left: 8px;
  }

  .checkout-btn {
    padding: 10px 24px;
    background: #e40000;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
  }

  .checkout-btn:hover:not(:disabled) {
    background: #c30000;
  }

  .checkout-btn:disabled {
    background: #b0b0b0;
    cursor: not-allowed;
  }

  /* 复选框样式 */
  .checkbox-container {
    display: inline-block;
    position: relative;
    padding-left: 24px;
    cursor: pointer;
    font-size: 14px;
    user-select: none;
  }

  .checkbox-container input {
    position: absolute;
    opacity: 0;
    cursor: pointer;
    height: 0;
    width: 0;
  }

  .checkmark {
    position: absolute;
    top: 0;
    left: 0;
    height: 16px;
    width: 16px;
    background-color: #fff;
    border: 1px solid #ccc;
    border-radius: 2px;
  }

  .checkbox-container:hover input ~ .checkmark {
    border-color: #999;
  }

  .checkbox-container input:checked ~ .checkmark {
    background-color: #e40000;
    border-color: #e40000;
  }

  .checkmark:after {
    content: '';
    position: absolute;
    display: none;
  }

  .checkbox-container input:checked ~ .checkmark:after {
    display: block;
  }

  .checkbox-container .checkmark:after {
    left: 5px;
    top: 1px;
    width: 5px;
    height: 10px;
    border: solid white;
    border-width: 0 2px 2px 0;
    transform: rotate(45deg);
  }
</style>
