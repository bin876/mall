<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">新增菜单</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="菜单名称">
        <el-input
          v-model="queryParams.title"
          placeholder="请输入菜单名称"
          clearable
          @keyup.enter="() => fetchMenuList()"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="() => fetchMenuList()"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchMenuList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="menuTableData"
      border
      row-key="menuId"
      style="width: 100%"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column prop="title" label="菜单名称" min-width="150" />
      <el-table-column prop="name" label="路由名称" min-width="120" />
      <el-table-column prop="path" label="路由路径" min-width="150" />
      <el-table-column prop="component" label="组件路径" min-width="150" />
      <el-table-column prop="permissionCode" label="权限标识" min-width="150" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 0" type="info">目录</el-tag>
          <el-tag v-else-if="scope.row.type === 1" type="primary">菜单</el-tag>
          <el-tag v-else-if="scope.row.type === 2" type="warning">按钮</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="hidden" label="隐藏" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.hidden === 0 ? 'success' : 'danger'">
            {{ scope.row.hidden === 0 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button
            size="small"
            type="danger"
            link
            @click="handleDelete(scope.row)"
            :disabled="scope.row.children && scope.row.children.length > 0"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="createDialogVisible"
      :title="createForm.type === 0 ? '新增目录' : createForm.type === 1 ? '新增菜单' : '新增按钮'"
      width="600px"
      @close="handleCloseCreate"
      top="10px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="父级菜单" prop="parentId">
          <el-cascader
            v-model="createForm.parentId"
            :options="menuTreeOptions"
            :props="{ checkStrictly: true, emitPath: false, value: 'menuId', label: 'title' }"
            placeholder="请选择父级菜单"
            style="width: 100%"
          >
            <template #default="{ node, data }">
              <span>{{ data.title }}</span>
              <span v-if="data.type === 0" style="color: #999; margin-left: 10px">(目录)</span>
              <span v-else-if="data.type === 1" style="color: #999; margin-left: 10px">(菜单)</span>
            </template>
          </el-cascader>
        </el-form-item>

        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="createForm.type" @change="handleTypeChange">
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="菜单名称" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="路由名称" prop="name" v-if="createForm.type !== 0">
          <el-input v-model="createForm.name" placeholder="请输入路由名称（Vue Router name）" />
        </el-form-item>

        <el-form-item label="路由路径" prop="path" v-if="createForm.type !== 0">
          <el-input v-model="createForm.path" placeholder="请输入路由路径" />
        </el-form-item>

        <el-form-item label="组件路径" prop="component" v-if="createForm.type === 1">
          <el-input v-model="createForm.component" placeholder="请输入组件路径" />
        </el-form-item>

        <el-form-item label="权限标识" prop="permissionCode" v-if="createForm.type !== 0">
          <el-select
            v-model="createForm.permissionCode"
            filterable
            placeholder="请选择权限标识（可选）"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.permissionId"
              :label="`${permission.permissionCode} - ${permission.permissionName}`"
              :value="permission.permissionCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="菜单图标" prop="icon">
          <el-input v-model="createForm.icon" placeholder="请输入图标名称（如：user）" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="createForm.sort" :min="0" :max="999" />
        </el-form-item>

        <el-form-item label="是否隐藏" prop="hidden">
          <el-switch v-model="createForm.hidden" :active-value="1" :inactive-value="0" />
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

    <el-dialog
      v-model="editDialogVisible"
      :title="editForm.type === 0 ? '编辑目录' : editForm.type === 1 ? '编辑菜单' : '编辑按钮'"
      width="600px"
      @close="handleCloseEdit"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="editForm.type" disabled>
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="菜单名称" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="路由名称" prop="name" v-if="editForm.type !== 0">
          <el-input v-model="editForm.name" placeholder="请输入路由名称（Vue Router name）" />
        </el-form-item>

        <el-form-item label="路由路径" prop="path" v-if="editForm.type !== 0">
          <el-input v-model="editForm.path" placeholder="请输入路由路径" />
        </el-form-item>

        <el-form-item label="组件路径" prop="component" v-if="editForm.type === 1">
          <el-input v-model="editForm.component" placeholder="请输入组件路径" />
        </el-form-item>

        <el-form-item label="权限标识" prop="permissionCode" v-if="editForm.type !== 0">
          <el-select
            v-model="editForm.permissionCode"
            filterable
            placeholder="请选择权限标识（可选）"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.permissionId"
              :label="`${permission.permissionCode} - ${permission.permissionName}`"
              :value="permission.permissionCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="菜单图标" prop="icon">
          <el-input v-model="editForm.icon" placeholder="请输入图标名称（如：user）" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="editForm.sort" :min="0" :max="999" />
        </el-form-item>

        <el-form-item label="是否隐藏" prop="hidden">
          <el-switch v-model="editForm.hidden" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
  import { ref, reactive, onMounted, computed } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { menuListApi, createMenuApi, updateMenuApi, deleteMenuApi } from '@/api/menu'
  import { permissionSimpleListApi } from '@/api/permission'
  import type { MenuQueryParams, MenuItem, MenuCreateDto, MenuUpdateDto } from '@/api/menu/type'
  import { useUserStore } from '@/stores/user'
  import type { SimplePermissionItem } from '@/api/permission/type'

  const userStore = useUserStore()
  const route = useRoute()

  const queryParams = reactive<MenuQueryParams>({
    title: '',
    status: null,
    pageNum: 1,
    pageSize: 100,
  })

  const menuList = ref<MenuItem[]>([])
  const menuTableData = ref<MenuItem[]>([])
  const menuTreeOptions = ref<MenuItem[]>([])

  const createDialogVisible = ref(false)
  const editDialogVisible = ref(false)
  const createLoading = ref(false)
  const editLoading = ref(false)

  const createFormRef = ref<FormInstance>()
  const editFormRef = ref<FormInstance>()

  const createForm = reactive<MenuCreateDto>({
    parentId: 0,
    title: '',
    name: '',
    path: '',
    component: '',
    permissionCode: '',
    icon: '',
    type: 1,
    sort: 0,
    hidden: 0,
  })

  const editForm = reactive<MenuUpdateDto>({
    menuId: 0,
    title: '',
    name: '',
    path: '',
    component: '',
    permissionCode: '',
    icon: '',
    type: 1,
    sort: 0,
    hidden: 0,
    status: 1,
  })

  const permissionOptions = ref<SimplePermissionItem[]>([])

  const createRules = reactive<FormRules>({
    title: [
      { required: true, message: '请输入菜单名称', trigger: 'blur' },
      { max: 100, message: '菜单名称长度不能超过100个字符', trigger: 'blur' },
    ],
    name: [
      {
        validator: (_, value) => {
          if (createForm.type !== 0 && !value) {
            return new Error('请输入路由名称')
          }
          return true
        },
        trigger: 'blur',
      },
    ],
    path: [
      {
        validator: (_, value) => {
          if (createForm.type !== 0 && !value) {
            return new Error('请输入路由路径')
          }
          return true
        },
        trigger: 'blur',
      },
    ],
    component: [
      {
        validator: (_, value) => {
          if (createForm.type === 1 && !value) {
            return new Error('请输入组件路径')
          }
          return true
        },
        trigger: 'blur',
      },
    ],
  })

  const editRules = reactive<FormRules>({
    title: [
      { required: true, message: '请输入菜单名称', trigger: 'blur' },
      { max: 100, message: '菜单名称长度不能超过100个字符', trigger: 'blur' },
    ],
    name: [
      {
        validator: (_, value) => {
          if (editForm.type !== 0 && !value) {
            return new Error('请输入路由名称')
          }
          return true
        },
        trigger: 'blur',
      },
    ],
    path: [
      {
        validator: (_, value) => {
          if (editForm.type !== 0 && !value) {
            return new Error('请输入路由路径')
          }
          return true
        },
        trigger: 'blur',
      },
    ],
    component: [
      {
        validator: (_, value) => {
          if (editForm.type === 1 && !value) {
            return new Error('请输入组件路径')
          }
          return true
        },
        trigger: 'blur',
      },
    ],
  })

  const fetchPermissionOptions = async () => {
    try {
      permissionOptions.value = await permissionSimpleListApi()
    } catch (error) {
      console.error('获取权限选项失败:', error)
      ElMessage.error('获取权限选项失败')
    }
  }

  const fetchMenuList = async () => {
    try {
      const params: MenuQueryParams = {
        ...queryParams,
        pageNum: 1,
        pageSize: 100,
      }

      const res = await menuListApi(params)
      menuList.value = res.records || []
      buildMenuTableData()
      buildMenuTreeOptions()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取菜单列表失败'
      ElMessage.error(errorMsg)
      console.error('获取菜单列表失败:', error)
    }
  }

  const resetQuery = () => {
    // TODO pageSize
    Object.assign(queryParams, {
      title: '',
      status: null,
      pageNum: 1,
      pageSize: 100,
    })
    fetchMenuList()
  }

  const buildMenuTableData = () => {
    const map = new Map()
    const roots: MenuItem[] = []

    menuList.value.forEach((menu) => {
      map.set(menu.menuId, { ...menu, children: [] })
    })

    menuList.value.forEach((menu) => {
      const node = map.get(menu.menuId)
      if (menu.parentId === 0) {
        roots.push(node)
      } else {
        const parent = map.get(menu.parentId)
        if (parent) {
          parent.children.push(node)
        }
      }
    })

    menuTableData.value = roots
  }

  const buildMenuTreeOptions = () => {
    const map = new Map<number, MenuItem>()

    const root: MenuItem = {
      menuId: 0,
      parentId: 0,
      title: '顶级菜单',
      name: '',
      path: '',
      component: '',
      permissionCode: '',
      icon: '',
      type: 0,
      sort: 0,
      hidden: 0,
      status: 1,
      createTime: '',
      updateTime: '',
      children: [],
    }

    const roots: MenuItem[] = [root]

    menuList.value.forEach((menu) => {
      map.set(menu.menuId, {
        ...menu,
        children: [],
      })
    })

    menuList.value.forEach((menu) => {
      const node = map.get(menu.menuId)
      if (!node) return

      if (menu.parentId === 0) {
        root.children.push(node)
      } else {
        const parent = map.get(menu.parentId)
        if (parent) {
          parent.children.push(node)
        }
      }
    })

    menuTreeOptions.value = roots
  }

  const handleTypeChange = () => {
    if (createForm.type === 0) {
      createForm.name = ''
      createForm.path = ''
      createForm.component = ''
      createForm.permissionCode = ''
    } else if (createForm.type === 2) {
      createForm.component = ''
    }
    createFormRef.value?.validateField()
  }

  const handleCreate = async () => {
    createDialogVisible.value = true
    await fetchPermissionOptions()
  }

  const handleCloseCreate = () => {
    createDialogVisible.value = false
    createFormRef.value?.resetFields()
    Object.assign(createForm, {
      parentId: 0,
      title: '',
      name: '',
      path: '',
      component: '',
      permissionCode: '',
      icon: '',
      type: 1,
      sort: 0,
      hidden: 0,
    })
  }

  const handleCreateSubmit = async () => {
    try {
      const valid = await createFormRef.value?.validate()
      if (!valid) return

      createLoading.value = true
      await createMenuApi(createForm)
      ElMessage.success('菜单创建成功')

      await userStore.getMenu()

      createDialogVisible.value = false
      fetchMenuList()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '创建菜单失败'
      ElMessage.error(errorMsg)
    } finally {
      createLoading.value = false
    }
  }

  const handleEditSubmit = async () => {
    try {
      const valid = await editFormRef.value?.validate()
      if (!valid) return

      editLoading.value = true
      await updateMenuApi(editForm)
      ElMessage.success('菜单更新成功')

      await userStore.getMenu()

      editDialogVisible.value = false
      fetchMenuList()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '更新菜单失败'
      ElMessage.error(errorMsg)
    } finally {
      editLoading.value = false
    }
  }

  const handleEdit = async (row: MenuItem) => {
    Object.assign(editForm, {
      menuId: row.menuId,
      title: row.title || '',
      name: row.name || '',
      path: row.path || '',
      component: row.component || '',
      permissionCode: row.permissionCode || '',
      icon: row.icon || '',
      type: row.type || 1,
      sort: row.sort || 0,
      hidden: row.hidden || 0,
      status: row.status || 1,
    })

    editDialogVisible.value = true
    await fetchPermissionOptions()
  }

  const handleCloseEdit = () => {
    editDialogVisible.value = false
    editFormRef.value?.resetFields()
  }

  const handleDelete = (row: MenuItem) => {
    if (row.children && row.children.length > 0) {
      ElMessage.warning('请先删除子菜单')
      return
    }

    ElMessageBox.confirm(`确定要删除菜单 "${row.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteMenuApi(row.menuId)
        ElMessage.success('菜单删除成功')

        await userStore.getMenu()

        fetchMenuList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除菜单失败'
        ElMessage.error(errorMsg)
        console.error('删除菜单失败:', error)
      }
    })
  }

  onMounted(() => {
    fetchMenuList()
  })
</script>

<style scoped>
  .el-pagination {
    display: flex;
    justify-content: flex-end;
  }
</style>
