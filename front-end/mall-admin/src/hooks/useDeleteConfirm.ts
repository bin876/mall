import { ElMessage, ElMessageBox } from 'element-plus'

interface DeleteConfirmOptions<T = any> {
  api: (id: number | string) => Promise<T>
  id: number | string
  title?: string
  content?: string
  successMessage?: string
  onSuccess?: () => void
}

export function useDeleteConfirm() {
  const confirmAndDelete = async <T = any>(options: DeleteConfirmOptions<T>) => {
    const {
      api,
      id,
      title = '提示',
      content = '确定要删除该数据吗？',
      successMessage = '删除成功',
      onSuccess,
    } = options

    try {
      await ElMessageBox.confirm(content, title, {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      })

      await api(id)
      ElMessage.success(successMessage)
      onSuccess?.()
    } catch (e) {}
  }

  return { confirmAndDelete }
}
