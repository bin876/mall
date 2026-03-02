<template>
  <NavBar />

  <div class="product-detail-container">
    <div class="product-main">
      <div class="product-images">
        <div class="main-image">
          <img :src="currentMainImage" alt="商品主图" />
        </div>

        <div class="image-list">
          <button
            class="scroll-btn prev"
            @click="scrollImages(-1)"
            :disabled="imageStartIndex === 0"
          >
            ‹
          </button>
          <div class="image-thumbnails" ref="thumbnailsContainer">
            <img
              v-for="(img, index) in currentImages"
              :key="index"
              :src="img.url"
              :alt="`商品图${index + 1}`"
              :class="{ active: imageStartIndex === index }"
              @click="setMainImage(index)"
            />
          </div>
          <button
            class="scroll-btn next"
            @click="scrollImages(1)"
            :disabled="imageStartIndex >= currentImages.length - 4"
          >
            ›
          </button>
        </div>
      </div>

      <div class="product-info">
        <div class="product-title">
          <h1>{{ spuDetail.spu.name }}</h1>
          <p class="subtitle">{{ spuDetail.spu.description }}</p>
        </div>

        <!-- 秒杀区域 - 根据当前SKU状态智能显示 -->
        <div v-if="isSeckillAvailable" class="seckill-section">
          <div class="seckill-header">
            <span class="seckill-tag">限时秒杀</span>
            <div class="countdown" v-if="countdown > 0">
              {{ formatCountdown(countdown) }}
            </div>
          </div>

          <div class="seckill-price-info">
            <span class="seckill-price">¥{{ seckillInfo.seckillPrice.toFixed(2) }}</span>
            <span class="original-price">¥{{ currentSku?.price?.toFixed(2) }}</span>
          </div>

          <div class="seckill-stock-info">
            <span class="stock-text">剩余库存: {{ availableStock }} 件</span>
            <el-progress
              :percentage="getStockPercentage()"
              :stroke-width="6"
              :color="getStockColor()"
            />
          </div>

          <div class="seckill-status">
            <span v-if="seckillStatus === 0" class="status-not-started">活动未开始</span>
            <span v-else-if="seckillStatus === 2" class="status-ended">活动已结束</span>
            <span v-else-if="availableStock <= 0" class="status-sold-out">已售罄</span>
          </div>
        </div>

        <!-- 秒杀SKU已选但活动未开始/已结束 -->
        <div
          v-else-if="seckillInfo && seckillInfo.skuId === currentSku?.skuId"
          class="seckill-hint"
        >
          <el-alert :type="seckillStatus === 0 ? 'info' : 'warning'" :closable="false">
            {{ seckillStatus === 0 ? '此规格参与秒杀，活动未开始' : '秒杀活动已结束' }}
          </el-alert>
        </div>

        <!-- 普通价格信息 -->
        <div v-else class="product-price-stock">
          <span class="price">¥{{ currentSku?.price?.toFixed(2) || '0.00' }}</span>
          <span class="stock">库存：{{ currentSku?.stock || 0 }} 件</span>
        </div>

        <div class="sku-selection">
          <div v-for="attr in spuDetail.spuAttrs" :key="attr.attrId" class="sku-row">
            <span class="sku-label">{{ attr.attrName }}：</span>
            <span class="sku-value">{{ attr.attrValue }}</span>
          </div>

          <div v-for="saleAttr in saleAttrs" :key="saleAttr.attrName" class="sku-row">
            <span class="sku-label">{{ saleAttr.attrName }}：</span>
            <div class="sku-options">
              <span
                v-for="value in saleAttr.values"
                :key="value"
                :class="{
                  'sku-option': true,
                  selected: selectedAttrs[saleAttr.attrName] === value,
                }"
                @click="selectAttr(saleAttr.attrName, value)"
              >
                {{ value }}
              </span>
            </div>
          </div>
        </div>

        <div class="cart-actions">
          <div class="quantity-selector">
            <button @click="decreaseQuantity" :disabled="quantity <= 1">-</button>
            <input type="text" :value="quantity" @input="updateQuantity" readonly />
            <button @click="increaseQuantity" :disabled="quantity >= (currentSku?.stock || 0)">
              +
            </button>
          </div>

          <div class="action-buttons">
            <!-- 秒杀按钮 -->
            <template v-if="seckillInfo && currentSku">
              <el-button
                class="seckill-btn"
                :type="getSeckillBtnType()"
                :disabled="isSeckillDisabled()"
                @click="handleSeckill"
              >
                {{ getSeckillBtnText() }}
              </el-button>
            </template>

            <template v-else-if="currentSku">
              <button
                class="add-to-cart-btn"
                @click="addToCart"
                :disabled="quantity > (currentSku.stock || 0)"
              >
                {{ currentSku.stock && currentSku.stock > 0 ? '加入购物车' : '库存不足' }}
              </button>

              <button
                class="buy-now-btn"
                @click="buyNow"
                :disabled="quantity > (currentSku.stock || 0)"
              >
                立即购买
              </button>
            </template>

            <div class="collect-actions">
              <div>
                <el-icon
                  :size="20"
                  @click="toggleCollect"
                  :style="{ color: isCollected ? '#f7c948' : '#666' }"
                >
                  <component :is="isCollected ? StarFilled : Star" />
                </el-icon>
              </div>
              <div>收藏</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 其他商品详情内容保持不变 -->
    <div class="product-details">
      <div class="spu-attrs">
        <h3>商品参数</h3>
        <div class="attrs-grid">
          <div v-for="attr in spuDetail.spuAttrs" :key="attr.attrId" class="attr-item">
            <span class="attr-name">{{ attr.attrName }}</span>
            <span class="attr-value">{{ attr.attrValue }}</span>
          </div>
        </div>
      </div>

      <div class="product-detail-content">
        <h3>商品详情</h3>
        <div v-if="spuDetail.spu.detail" v-html="spuDetail.spu.detail" class="detail-html"></div>
      </div>
    </div>
  </div>

  <!-- 评论区域 - 保持不变 -->
  <div class="product-detail-container comment-section">
    <h2 class="section-title">商品评论</h2>

    <!-- 评论统计 -->
    <div class="comment-stats">
      <span>好评率: {{ formatPositiveRate() }}</span>
      <span>评论数: {{ totalComments }}</span>
    </div>

    <div class="comment-form">
      <div class="form-header">
        <img :src="userStore.userInfo?.avatarUrl || '/default-avatar.png'" class="avatar" />
        <span>{{ userStore.userInfo?.username }}</span>
      </div>
      <el-input
        v-model="commentContent"
        type="textarea"
        placeholder="分享您的购买体验..."
        :rows="3"
        maxlength="500"
        show-word-limit
      />
      <div class="form-actions">
        <el-button type="primary" @click="submitComment" :loading="submitLoading">
          发表评论
        </el-button>
      </div>
    </div>

    <div v-if="comments.length > 0" class="comment-list">
      <div v-for="rootComment in comments" :key="rootComment.commentId" class="root-comment">
        <div class="comment-item">
          <div class="comment-header">
            <img :src="rootComment.memberAvatar" class="avatar" @error="handleAvatarError" />
            <div class="user-info">
              <span class="username">{{ rootComment.memberUsername }}</span>
              <span class="time">{{ formatDate(rootComment.createTime) }}</span>
            </div>
            <div class="actions">
              <el-button type="text" @click="showReplyForm(rootComment.commentId, 'root')">
                回复
              </el-button>
              <el-button
                type="text"
                v-if="rootComment.memberId === currentMemberId"
                @click="deleteComment(rootComment.commentId)"
              >
                删除
              </el-button>
            </div>
          </div>
          <div class="comment-content">
            {{ rootComment.content }}
          </div>
        </div>

        <div
          v-if="activeReplyType === 'root' && activeReplyId === rootComment.commentId"
          class="reply-form"
        >
          <el-input
            v-model="replyContent"
            type="textarea"
            placeholder="回复此评论..."
            :rows="2"
            maxlength="300"
            show-word-limit
          />
          <div class="form-actions">
            <el-button @click="cancelReply">取消</el-button>
            <el-button type="primary" @click="submitReply(rootComment)" :loading="replyLoading">
              发送
            </el-button>
          </div>
        </div>

        <div
          v-if="rootComment.children && rootComment.children.length > 0"
          class="children-comments"
        >
          <div
            v-for="childComment in rootComment.children"
            :key="childComment.commentId"
            class="comment-item child-item"
          >
            <div class="comment-header">
              <img :src="childComment.memberAvatar" class="avatar" @error="handleAvatarError" />
              <div class="user-info">
                <span class="username">{{ childComment.memberUsername }}</span>
                <span class="time">{{ formatDate(childComment.createTime) }}</span>
              </div>
              <div class="actions">
                <el-button type="text" @click="showReplyForm(childComment.commentId, 'child')">
                  回复
                </el-button>
                <el-button
                  type="text"
                  v-if="childComment.memberId === currentMemberId"
                  @click="deleteComment(childComment.commentId)"
                >
                  删除
                </el-button>
              </div>
            </div>
            <div class="comment-content">
              <span v-if="childComment.toMemberUsername" class="reply-target">
                @{{ childComment.toMemberUsername }}:
              </span>
              {{ childComment.content }}
            </div>
          </div>

          <!-- 子评论的回复框 -->
          <div
            v-for="childComment in rootComment.children"
            :key="`reply-${childComment.commentId}`"
          >
            <div
              v-if="activeReplyId === childComment.commentId && activeReplyType === 'child'"
              class="reply-form"
            >
              <el-input
                v-model="replyContent"
                type="textarea"
                placeholder="回复此评论..."
                :rows="2"
                maxlength="300"
                show-word-limit
              />
              <div class="form-actions">
                <el-button @click="cancelReply">取消</el-button>
                <el-button
                  type="primary"
                  @click="submitReply(childComment)"
                  :loading="replyLoading"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="no-replies">
          <span>暂无回复</span>
        </div>
      </div>
    </div>

    <div v-else class="empty-comments">
      <el-empty description="暂无评论，快来抢沙发吧！" />
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="loadComments"
      class="pagination"
    />
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox, ElAlert } from 'element-plus'
  import { useUserStore } from '@/stores/user'
  import { getSpuDetailApi } from '@/api/product'
  import { addToCartApi } from '@/api/order'
  import { getCommentListApi, createCommentApi, deleteCommentApi } from '@/api/comment'
  import { getSeckillInfoApi, doSeckillApi, getSeckillStockApi } from '@/api/seckill'
  import type { SpuDetail } from '@/api/product/type'
  import type { CommentDTO, CommentCreateDTO, CommentQueryDTO } from '@/api/comment/type'
  import type { SeckillInfoDTO, SeckillRequest } from '@/api/seckill/type'
  import router from '@/router'
  import NavBar from '@/components/navbar.vue'
  import { Star, StarFilled } from '@element-plus/icons-vue'
  import { checkCollectedApi, collectProductApi, cancelCollectBySpuIdApi } from '@/api/collect'
  import { ElButton } from 'element-plus'

  const route = useRoute()
  const spuId = Number(route.params.spuId)
  const userStore = useUserStore()

  // 商品详情相关
  const spuDetail = ref<SpuDetail>({
    spu: {
      spuId: 0,
      name: '',
      description: '',
      categoryId: 0,
      brandId: 0,
      weight: 0,
      publishStatus: 0,
      detail: '',
      defaultImg: '',
    },
    spuImages: [],
    spuAttrs: [],
    skus: [],
  })

  const isCollected = ref(false)

  const selectedAttrs = ref<Record<string, string>>({})
  const quantity = ref(1)
  const imageStartIndex = ref(0)
  const thumbnailsContainer = ref<HTMLDivElement | null>(null)

  // 秒杀相关状态
  const seckillInfo = ref<SeckillInfoDTO | null>(null)
  const availableStock = ref(0)
  const countdown = ref(0)
  const loading = ref(false)
  const timerRef = ref<NodeJS.Timeout | null>(null)
  const originalSeckillSkuId = ref<number | null>(null) // 存储原始秒杀SKU ID
  const seckillLoadedForSpu = ref(false) // 标记SPU的秒杀信息是否已加载

  // 评论相关状态
  const comments = ref<CommentDTO[]>([])
  const total = ref(0)
  const totalComments = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(5)
  const commentContent = ref('')
  const submitLoading = ref(false)
  const activeReplyId = ref<number | null>(null)
  const activeReplyType = ref<'root' | 'child' | null>(null)
  const replyContent = ref('')
  const replyLoading = ref(false)

  const saleAttrs = computed(() => {
    if (spuDetail.value.skus.length === 0) return []

    const baseAttrNames = new Set(spuDetail.value.spuAttrs.map((attr) => attr.attrName))
    const saleAttrMap = new Map<string, Set<string>>()

    spuDetail.value.skus.forEach((sku) => {
      sku.attrs.forEach((attr) => {
        if (!baseAttrNames.has(attr.attrName)) {
          if (!saleAttrMap.has(attr.attrName)) {
            saleAttrMap.set(attr.attrName, new Set())
          }
          saleAttrMap.get(attr.attrName)!.add(attr.attrValue)
        }
      })
    })

    return Array.from(saleAttrMap.entries()).map(([attrName, values]) => ({
      attrName,
      values: Array.from(values),
    }))
  })

  const currentSku = computed(() => {
    if (spuDetail.value.skus.length === 0) return null

    const selectedCount = Object.keys(selectedAttrs.value).length
    const requiredCount = saleAttrs.value.length

    if (selectedCount === 0 && requiredCount === 0) {
      return spuDetail.value.skus[0] || null
    }

    if (selectedCount !== requiredCount) {
      return null
    }

    return (
      spuDetail.value.skus.find((sku) => {
        return Object.entries(selectedAttrs.value).every(([attrName, attrValue]) => {
          return sku.attrs.some(
            (attr) => attr.attrName === attrName && attr.attrValue === attrValue
          )
        })
      }) || null
    )
  })

  const isSeckillAvailable = computed(() => {
    return (
      seckillInfo.value &&
      currentSku.value &&
      seckillInfo.value.skuId === currentSku.value.skuId &&
      seckillStatus.value === 1 &&
      availableStock.value > 0
    )
  })

  const currentImages = computed(() => {
    if (currentSku.value && currentSku.value.images.length > 0) {
      return [...currentSku.value.images].sort((a, b) => a.sort - b.sort)
    }
    return [...spuDetail.value.spuImages].sort((a, b) => a.sort - b.sort)
  })

  const currentMainImage = computed(() => {
    return currentImages.value[imageStartIndex.value]?.url || spuDetail.value.spu.defaultImg
  })

  const currentMemberId = computed(() => {
    return userStore.userInfo?.memberId
  })

  // 秒杀状态计算
  const seckillStatus = computed(() => {
    return seckillInfo.value?.status || 0
  })

  const scrollImages = (direction: number) => {
    const VISIBLE_THUMBNAILS = 4
    const maxIndex = Math.max(0, currentImages.value.length - VISIBLE_THUMBNAILS)
    imageStartIndex.value = Math.max(0, Math.min(maxIndex, imageStartIndex.value + direction))
  }

  const setMainImage = (index: number) => {
    imageStartIndex.value = index
  }

  const decreaseQuantity = () => {
    if (quantity.value > 1) {
      quantity.value--
    }
  }

  const increaseQuantity = () => {
    if (currentSku.value && quantity.value < currentSku.value.stock) {
      quantity.value++
    }
  }

  const updateQuantity = (e: Event) => {
    const value = parseInt((e.target as HTMLInputElement).value)
    if (!isNaN(value) && value > 0) {
      quantity.value = Math.min(value, currentSku.value?.stock || 1)
    }
  }

  const selectAttr = async (attrName: string, value: string) => {
    const oldSku = currentSku.value
    selectedAttrs.value[attrName] = value

    // 等待SKU更新
    await nextTick()

    const newSku = currentSku.value
    if (!oldSku || !newSku || oldSku.skuId === newSku.skuId) {
      return
    }

    // SKU已变化，重置图片索引
    imageStartIndex.value = 0

    // 如果切换到秒杀SKU，确保秒杀信息加载
    if (seckillLoadedForSpu.value && originalSeckillSkuId.value === newSku.skuId) {
      await loadSeckillInfoForCurrentSku(newSku.skuId)
    }
  }

  const addToCart = async () => {
    if (!currentSku.value) return

    try {
      await addToCartApi({
        skuId: currentSku.value.skuId,
        quantity: quantity.value,
      })
      ElMessage.success('已加入购物车！')
    } catch (error) {
      console.error('加购失败:', error)
      ElMessage.error('加入购物车失败')
    }
  }

  const buyNow = () => {
    if (!currentSku.value) return

    router.push({
      path: '/order/confirm',
      query: {
        skuId: currentSku.value.skuId,
        quantity: quantity.value,
      },
    })
  }

  const getSeckillBtnType = () => {
    if (seckillStatus.value !== 1) return 'info'
    if (availableStock.value <= 0) return 'info'
    return 'danger'
  }

  const isSeckillDisabled = () => {
    return loading.value || seckillStatus.value !== 1 || availableStock.value <= 0
  }

  const getSeckillBtnText = () => {
    if (loading.value) return '秒杀中...'
    if (!seckillInfo.value) return '查看秒杀'
    if (seckillStatus.value === 0) return '活动未开始'
    if (seckillStatus.value === 2) return '活动已结束'
    if (availableStock.value <= 0) return '已售罄'
    if (seckillInfo.value.skuId !== currentSku.value?.skuId) return '查看秒杀'
    return '立即秒杀'
  }

  const handleSeckill = async () => {
    if (!currentSku.value) {
      ElMessage.warning('请选择规格')
      return
    }

    // 如果当前SKU不是秒杀SKU，切换到秒杀SKU
    if (seckillInfo.value && seckillInfo.value.skuId !== currentSku.value.skuId) {
      // 找到秒杀SKU对应的属性值
      const seckillSku = spuDetail.value.skus.find((sku) => sku.skuId === seckillInfo.value!.skuId)
      if (seckillSku) {
        // 重置选择
        selectedAttrs.value = {}

        // 设置秒杀SKU的属性
        const baseAttrNames = new Set(spuDetail.value.spuAttrs.map((attr) => attr.attrName))
        seckillSku.attrs.forEach((attr) => {
          if (!baseAttrNames.has(attr.attrName)) {
            selectedAttrs.value[attr.attrName] = attr.attrValue
          }
        })

        await nextTick()
        ElMessage.info('已切换到秒杀规格')
        return
      }
    }

    // 正常秒杀逻辑
    if (!seckillInfo.value || availableStock.value <= 0) {
      ElMessage.warning('商品已售罄或活动未开始')
      return
    }

    if (!userStore.token) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    loading.value = true
    try {
      const request: SeckillRequest = {
        skuId: seckillInfo.value.skuId,
      }

      const result = await doSeckillApi(request)

      if (result) {
        ElMessage.success('秒杀成功！正在生成订单...')

        // 更新本地库存显示
        availableStock.value = Math.max(0, availableStock.value - 1)

        // 3秒后跳转到订单页面
        setTimeout(() => {
          router.push('/user/orders')
        }, 3000)
      }
    } catch (error: any) {
      ElMessage.error(error?.msg || '秒杀失败，请重试')
    } finally {
      loading.value = false
    }
  }

  const getStockPercentage = () => {
    if (!seckillInfo.value) return 0
    const total = seckillInfo.value.totalStock
    const available = availableStock.value
    return total > 0 ? Math.round((available / total) * 100) : 0
  }

  const getStockColor = () => {
    const percentage = getStockPercentage()
    if (percentage > 50) return '#e40000'
    if (percentage > 20) return '#ff9900'
    return '#999999'
  }

  const formatCountdown = (ms: number) => {
    const totalSeconds = Math.floor(ms / 1000)
    const hours = Math.floor(totalSeconds / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const seconds = totalSeconds % 60

    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  }

  const fetchCurrentStock = async () => {
    if (!seckillInfo.value) return

    try {
      const response = await getSeckillStockApi(seckillInfo.value.skuId)
      availableStock.value = response.stock
    } catch (error) {
      console.error('获取库存失败:', error)
    }
  }

  const updateCountdown = () => {
    if (!seckillInfo.value) {
      countdown.value = 0
      return
    }

    const now = new Date()
    const start = new Date(seckillInfo.value.startTime)
    const end = new Date(seckillInfo.value.endTime)

    if (now < start) {
      countdown.value = start.getTime() - now.getTime()
    } else if (now < end) {
      countdown.value = end.getTime() - now.getTime()

      if (countdown.value > 0 && availableStock.value > 0) {
        fetchCurrentStock()
      }
    } else {
      countdown.value = -1
    }
  }

  const loadSeckillInfoForCurrentSku = async (skuId: number) => {
    try {
      const info = await getSeckillInfoApi(spuId)
      if (info && info.skuId === skuId) {
        seckillInfo.value = info
        availableStock.value = info.availableStock
        originalSeckillSkuId.value = info.skuId

        if (timerRef.value) {
          clearInterval(timerRef.value)
        }
        timerRef.value = setInterval(updateCountdown, 1000)
        updateCountdown()
        return true
      }
      return false
    } catch (error) {
      seckillInfo.value = null
      availableStock.value = 0
      originalSeckillSkuId.value = null
      return false
    }
  }

  const loadSeckillInfo = async () => {
    try {
      const info = await getSeckillInfoApi(spuId)
      if (info) {
        seckillInfo.value = info
        availableStock.value = info.availableStock
        originalSeckillSkuId.value = info.skuId
        seckillLoadedForSpu.value = true

        if (timerRef.value) {
          clearInterval(timerRef.value)
        }
        timerRef.value = setInterval(updateCountdown, 1000)
        updateCountdown()
      }
    } catch (error) {
      seckillInfo.value = null
      availableStock.value = 0
    }
  }

  // 监控SKU变化
  watch(currentSku, async (newSku) => {
    if (!newSku) {
      seckillInfo.value = null
      availableStock.value = 0
      return
    }

    // 场景1：当前有秒杀信息
    if (seckillInfo.value) {
      if (seckillInfo.value.skuId === newSku.skuId) {
        // 同一个秒杀SKU，更新库存
        fetchCurrentStock()
      } else {
        // 不同SKU，清除当前秒杀信息
        seckillInfo.value = null
        availableStock.value = 0
      }
      return
    }

    // 场景2：当前无秒杀信息，但这个SPU有秒杀活动
    if (seckillLoadedForSpu.value && originalSeckillSkuId.value === newSku.skuId) {
      // 切换回秒杀SKU，重新加载秒杀信息
      await loadSeckillInfoForCurrentSku(newSku.skuId)
      return
    }

    // 场景3：从未加载过秒杀信息，尝试加载
    if (!seckillLoadedForSpu.value) {
      await loadSeckillInfoForCurrentSku(newSku.skuId)
    }
  })

  const formatPositiveRate = () => {
    return '98%'
  }

  const loadComments = async (page = currentPage.value) => {
    try {
      const query: CommentQueryDTO = {
        spuId: spuId,
        pageNum: page,
        pageSize: pageSize.value,
      }

      const res = await getCommentListApi(query)
      comments.value = (res.records || []).map((comment) => ({
        ...comment,
        children: Array.isArray(comment.children) ? comment.children : [],
      }))

      total.value = res.total || 0
      totalComments.value = res.total || 0
      currentPage.value = page
    } catch (error) {
      console.error('加载评论失败:', error)
      ElMessage.error('加载评论失败，请稍后重试')
      comments.value = []
    }
  }

  const submitComment = async () => {
    if (!commentContent.value.trim()) {
      ElMessage.warning('请输入评论内容')
      return
    }

    submitLoading.value = true
    try {
      const dto: CommentCreateDTO = {
        spuId: spuId,
        content: commentContent.value.trim(),
        toCommentId: null,
        toCommentUserId: null,
      }
      await createCommentApi(dto)
      ElMessage.success('评论成功')
      commentContent.value = ''
      loadComments(1)
    } catch (error: any) {
      ElMessage.error(error?.msg || '评论失败')
    } finally {
      submitLoading.value = false
    }
  }

  const showReplyForm = (commentId: number, type: 'root' | 'child') => {
    if (activeReplyId.value === commentId && activeReplyType.value === type) {
      cancelReply()
      return
    }

    activeReplyId.value = commentId
    activeReplyType.value = type
    replyContent.value = ''
  }

  const cancelReply = () => {
    activeReplyId.value = null
    activeReplyType.value = null
    replyContent.value = ''
  }

  const submitReply = async (targetComment: CommentDTO) => {
    if (!replyContent.value.trim()) {
      ElMessage.warning('请输入回复内容')
      return
    }

    replyLoading.value = true
    try {
      const dto: CommentCreateDTO = {
        spuId: spuId,
        content: replyContent.value.trim(),
        toCommentId: targetComment.commentId,
        toCommentUserId: targetComment.memberId,
      }
      await createCommentApi(dto)
      ElMessage.success('回复成功')
      cancelReply()
      loadComments(currentPage.value)
    } catch (error: any) {
      ElMessage.error(error?.msg || '回复失败')
    } finally {
      replyLoading.value = false
    }
  }

  const deleteComment = async (commentId: number) => {
    ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteCommentApi(commentId)
        ElMessage.success('删除成功')
        loadComments(currentPage.value)
      } catch (error: any) {
        ElMessage.error(error?.msg || '删除失败')
      }
    })
  }

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString)
    const now = new Date()
    const diff = now.getTime() - date.getTime()

    if (diff < 24 * 60 * 60 * 1000) {
      return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }

    if (diff < 7 * 24 * 60 * 60 * 1000) {
      const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
      return days[date.getDay()]
    }

    return date.toLocaleDateString()
  }

  const handleAvatarError = (e: Event) => {
    ;(e.target as HTMLImageElement).src = '/default-avatar.png'
  }

  const checkCollectStatus = async () => {
    try {
      const collected = await checkCollectedApi(spuId)

      console.log(collected)

      isCollected.value = collected
    } catch (error) {
      console.error('检查收藏状态失败:', error)
    }
  }

  const toggleCollect = async () => {
    if (loading.value) return

    loading.value = true
    try {
      if (isCollected.value) {
        await cancelCollectBySpuIdApi(spuId!)
        isCollected.value = false
        ElMessage.success('已取消收藏')
      } else {
        await collectProductApi({ spuId: spuId })
        isCollected.value = true
        ElMessage.success('收藏成功')
      }
    } catch (error: any) {
      ElMessage.error(error.msg || '操作失败')
    } finally {
      loading.value = false
    }
  }

  onMounted(async () => {
    try {
      checkCollectStatus()

      const data = await getSpuDetailApi(spuId)
      spuDetail.value = data

      await loadSeckillInfo()

      await loadComments()

      if (data.skus.length > 0) {
        const firstSku = data.skus[0]
        const baseAttrNames = new Set(data.spuAttrs.map((attr) => attr.attrName))
        const salesAttrs: Record<string, string> = {}

        firstSku?.attrs.forEach((attr) => {
          if (!baseAttrNames.has(attr.attrName)) {
            salesAttrs[attr.attrName] = attr.attrValue
          }
        })

        selectedAttrs.value = salesAttrs
      }

      await nextTick()
      if (thumbnailsContainer.value) {
        thumbnailsContainer.value.scrollLeft = 0
      }

      if (userStore.token) {
        await userStore.fetchUserInfo()
      }
    } catch (error) {
      console.error('加载商品详情失败:', error)
      ElMessage.error('加载商品详情失败')
    }
  })

  onUnmounted(() => {
    if (timerRef.value) {
      clearInterval(timerRef.value)
      timerRef.value = null
    }
    // 重置秒杀状态
    seckillInfo.value = null
    availableStock.value = 0
    originalSeckillSkuId.value = null
    seckillLoadedForSpu.value = false
    countdown.value = 0
  })
</script>

<style scoped>
  .product-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .product-main {
    display: flex;
    gap: 40px;
    margin-bottom: 40px;
  }

  .product-images {
    width: 500px;
    flex-shrink: 0;
  }

  .main-image {
    width: 500px;
    height: 500px;
    border: 1px solid #e5e5e5;
    margin-bottom: 10px;
    overflow: hidden;
  }

  .main-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .image-list {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .scroll-btn {
    width: 24px;
    height: 60px;
    background: #f5f5f5;
    border: 1px solid #e5e5e5;
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
    color: #333;
  }

  .scroll-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .image-thumbnails {
    display: flex;
    gap: 8px;
    overflow: hidden;
    width: 440px;
  }

  .image-thumbnails img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border: 1px solid #e5e5e5;
    cursor: pointer;
  }

  .image-thumbnails img.active {
    border-color: #e40000;
  }

  .product-info {
    flex: 1;
  }

  .product-title h1 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
  }

  .subtitle {
    font-size: 14px;
    color: #666;
    margin: 0 0 20px 0;
  }

  .seckill-section {
    background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
    padding: 20px;
    border-radius: 8px;
    color: white;
    margin-bottom: 20px;
  }

  .seckill-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }

  .seckill-tag {
    background: rgba(255, 255, 255, 0.2);
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: bold;
  }

  .countdown {
    font-family: 'Courier New', monospace;
    font-size: 18px;
    font-weight: bold;
    background: rgba(0, 0, 0, 0.2);
    padding: 4px 8px;
    border-radius: 4px;
  }

  .seckill-price-info {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 15px;
  }

  .seckill-price {
    font-size: 28px;
    font-weight: bold;
  }

  .original-price {
    font-size: 16px;
    text-decoration: line-through;
    opacity: 0.7;
  }

  .seckill-stock-info {
    margin-bottom: 15px;
  }

  .stock-text {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
  }

  .seckill-status {
    text-align: center;
    margin-top: 10px;
  }

  .status-not-started,
  .status-ended,
  .status-sold-out {
    font-size: 14px;
    font-weight: bold;
    opacity: 0.8;
  }

  .seckill-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 10px;
  }

  .seckill-btn.el-button--info {
    background: #cccccc;
    border-color: #cccccc;
    color: #666666;
  }

  .seckill-btn.el-button--danger {
    background: #ff4d4f;
    border-color: #ff4d4f;
  }

  .seckill-btn:disabled {
    opacity: 0.6;
  }

  .seckill-hint {
    margin-bottom: 20px;
  }

  :deep(.el-progress-bar__outer) {
    background-color: rgba(255, 255, 255, 0.2) !important;
  }

  :deep(.el-progress-bar__inner) {
    background-color: #ffffff !important;
    transition: width 0.3s ease !important;
  }

  .product-price-stock {
    margin-bottom: 20px;
  }

  .price {
    font-size: 24px;
    font-weight: 600;
    color: #e40000;
    margin-right: 20px;
  }

  .stock {
    color: #666;
    font-size: 14px;
  }

  .sku-selection {
    margin-bottom: 20px;
  }

  .sku-row {
    margin-bottom: 15px;
    display: flex;
  }

  .sku-label {
    width: 80px;
    color: #333;
    font-weight: bold;
  }

  .sku-value {
    color: #666;
  }

  .sku-options {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .sku-option {
    padding: 5px 12px;
    border: 1px solid #ccc;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
  }

  .sku-option:hover {
    border-color: #999;
  }

  .sku-option.selected {
    border-color: #e40000;
    color: #e40000;
    font-weight: bold;
  }

  .cart-actions {
    display: flex;
    flex-direction: column;
    gap: 15px;
    margin-top: 20px;
  }

  .quantity-selector {
    display: flex;
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

  .action-buttons {
    display: flex;
    gap: 10px;
  }

  .add-to-cart-btn {
    flex: 1;
    padding: 10px;
    background: #fff;
    color: #e40000;
    border: 1px solid #e40000;
    border-radius: 4px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
  }

  .add-to-cart-btn:hover:not(:disabled) {
    background: #fef0f0;
  }

  .add-to-cart-btn:disabled {
    background: #f5f5f5;
    color: #ccc;
    cursor: not-allowed;
  }

  .buy-now-btn {
    flex: 1;
    padding: 10px;
    background: #e40000;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
  }

  .buy-now-btn:hover:not(:disabled) {
    background: #c30000;
  }

  .buy-now-btn:disabled {
    background: #b0b0b0;
    cursor: not-allowed;
  }

  .collect-actions {
    font-size: 10px;
    font-weight: 600;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    color: #666;
  }

  .collect-actions:hover {
    color: #333;
  }
  .product-details {
    border-top: 1px solid #e5e5e5;
    padding-top: 30px;
  }

  .spu-attrs h3,
  .product-detail-content h3 {
    font-size: 18px;
    margin: 0 0 20px 0;
    color: #333;
  }

  .attrs-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
    margin-bottom: 30px;
  }

  .attr-item {
    display: flex;
    padding: 10px;
    background: #f9f9f9;
    border-radius: 4px;
  }

  .attr-name {
    width: 100px;
    font-weight: bold;
    color: #333;
  }

  .attr-value {
    color: #666;
  }

  .detail-html {
    line-height: 1.6;
    font-size: 14px;
  }

  .detail-html img {
    max-width: 100%;
    height: auto;
  }

  /* 评论区域样式 */
  .comment-section {
    padding-top: 20px;
    border-top: 1px solid #e5e5e5;
  }

  .section-title {
    font-size: 24px;
    color: #333;
    margin-bottom: 20px;
  }

  .comment-stats {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
    color: #999;
    font-size: 14px;
  }

  .comment-form {
    background: #f8f9fa;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 30px;
  }

  .form-header {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
  }

  .avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    margin-right: 10px;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
  }

  .comment-list {
    display: flex;
    flex-direction: column;
    gap: 25px;
  }

  .root-comment {
    border-left: 2px solid #e40000;
    padding-left: 20px;
  }

  .comment-item {
    background: #fff;
    border-radius: 4px;
    padding: 15px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    margin-bottom: 15px;
  }

  .comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }

  .user-info {
    flex: 1;
  }

  .username {
    font-weight: bold;
    color: #333;
  }

  .time {
    color: #999;
    font-size: 13px;
    margin-left: 8px;
  }

  .actions {
    display: flex;
    gap: 10px;
  }

  .comment-content {
    line-height: 1.6;
    color: #333;
  }

  .reply-target {
    color: #e40000;
    margin-right: 5px;
  }

  .children-comments {
    border-left: 1px dashed #dcdfe6;
    margin-left: 30px;
    padding-left: 20px;
    margin-top: 15px;
  }

  .child-item {
    background: #f9f9f9;
    margin-bottom: 10px;
  }

  .reply-form {
    margin-top: 15px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 4px;
  }

  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 10px;
  }

  .no-replies {
    text-align: center;
    color: #999;
    padding: 10px;
    font-size: 13px;
  }

  .empty-comments {
    text-align: center;
    padding: 40px 0;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

  /* 修复ElAlert样式 */
  :deep(.seckill-hint .el-alert) {
    padding: 8px 15px;
  }

  :deep(.seckill-hint .el-alert__content) {
    font-size: 14px;
    line-height: 1.5;
  }
</style>
