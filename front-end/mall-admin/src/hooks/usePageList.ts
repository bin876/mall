import type { PageResult } from '@/api/type'

export function usePageList<T>(
  fetchApi: (params: { pageNum: number; pageSize: number }) => Promise<PageResult<T>>
) {
  const list = ref<T[]>([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  const loading = ref(false)

  const fetchList = async () => {
    loading.value = true
    try {
      const res = await fetchApi({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
      })
      list.value = res.records || []
      total.value = res.total || 0
    } finally {
      loading.value = false
    }
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchList()
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchList()
  }

  return {
    list,
    total,
    currentPage,
    pageSize,
    loading,
    fetchList,
    handleSizeChange,
    handleCurrentChange,
  }
}
