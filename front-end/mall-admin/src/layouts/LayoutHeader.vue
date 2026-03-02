<template>
  <div class="header-container">
    <div class="left-section">
      <div class="menu-toggle" @click="toggleMenu">
        <el-icon>
          <component :is="isMenuExpanded ? 'Expand' : 'Fold'" />
        </el-icon>
      </div>

      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item>
          <el-dropdown trigger="hover" @command="handleRootCommand">
            <span class="dropdown-trigger">
              <span>首页</span>
              <el-icon class="arrow-icon"><ArrowDownBold /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in visibleTopLevelMenus"
                  :key="item.menuId"
                  :command="item.path"
                >
                  {{ item.title }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-breadcrumb-item>

        <template v-for="(crumb, index) in breadcrumbItems" :key="index">
          <el-breadcrumb-item
            v-if="crumb.hasDropdown && index < breadcrumbItems.length - 1"
            class="dropdown-item"
          >
            <el-dropdown trigger="hover" @command="(path) => router.push(path)">
              <span class="dropdown-trigger">
                <span>{{ crumb.title }}</span>
                <el-icon class="arrow-icon"><ArrowDownBold /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="child in crumb.children"
                    :key="child.path"
                    :command="child.path"
                  >
                    {{ child.title }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-breadcrumb-item>

          <el-breadcrumb-item v-else :to="{ path: crumb.path }">
            <span :class="{ 'active-item': index === breadcrumbItems.length - 1 }">
              {{ crumb.title }}
            </span>
          </el-breadcrumb-item>
        </template>
      </el-breadcrumb>
    </div>

    <div class="right-section">
      <div class="action-item" @click="toggleFullScreen">
        <el-icon><FullScreen /></el-icon>
      </div>
      <div class="action-item" @click="drawer = true">
        <el-icon><Operation /></el-icon>
      </div>
      <div>
        <el-dropdown trigger="hover">
          <div class="user-info">
            <el-avatar :size="28" class="avatar">
              <img :src="userStore.userInfo?.avatarUrl || '/default-avatar.png'" alt="avatar" />
            </el-avatar>
            <span class="username">{{ 'User' }}</span>
            <span class="dropdown-trigger">
              <el-icon class="arrow-icon" :size="12"><ArrowDownBold /></el-icon>
            </span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="logout">
                <el-icon><SwitchButton style="font-size: 14px" /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>

  <el-drawer v-model="drawer" title="I am the title" :with-header="false">
    <span>Hi there!</span>
  </el-drawer>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/stores/user'
  import eventBus from '@/utils/eventBus'

  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()

  const isMenuExpanded = ref(true)
  const drawer = ref(false)

  onMounted(() => {
    const savedState = localStorage.getItem('menu-expanded')
    isMenuExpanded.value = savedState ? JSON.parse(savedState) : true
  })

  const visibleTopLevelMenus: any = computed(() =>
    userStore.menus.filter((menu: any) => menu.hidden !== 1 && menu.type !== 2)
  )

  const filterVisibleMenus = (menus: any[]): any[] => {
    return menus
      .filter((menu) => menu.hidden !== 1 && menu.type !== 2)
      .map((menu) => ({
        ...menu,
        children: menu.children ? filterVisibleMenus(menu.children) : [],
      }))
  }

  const breadcrumbItems = computed(() => {
    const currentPath = route.path.replace(/\/+/g, '/')
    const allMenus = filterVisibleMenus(userStore.menus)

    const findMenuChain = (menus: any[], parentPath = ''): any[] => {
      for (const menu of menus) {
        const fullPath = parentPath ? `${parentPath}/${menu.path}`.replace(/\/+/g, '/') : menu.path

        if (fullPath === currentPath) {
          return [menu]
        }

        if (currentPath.startsWith(fullPath + '/') && menu.children) {
          const childChain = findMenuChain(menu.children, fullPath)
          if (childChain.length > 0) {
            return [menu, ...childChain]
          }
        }
      }
      return []
    }

    const chain = findMenuChain(allMenus)

    return chain.map((menu, index, arr) => {
      let currentPath = ''
      for (let i = 0; i <= index; i++) {
        const p = arr[i].path
        currentPath = i === 0 ? p : `${currentPath}/${p}`.replace(/\/+/g, '/')
      }

      const isLast = index === arr.length - 1
      const hasValidChildren = menu.children && menu.children.length > 0

      return {
        title: menu.title,
        path: currentPath,
        hasDropdown: !isLast && hasValidChildren,
        children:
          !isLast && hasValidChildren
            ? menu.children.map((child: any) => {
                const childPath = `${currentPath}/${child.path}`.replace(/\/+/g, '/')
                return {
                  title: child.title,
                  path: childPath,
                }
              })
            : undefined,
      }
    })
  })

  const toggleMenu = () => {
    isMenuExpanded.value = !isMenuExpanded.value
    localStorage.setItem('menu-expanded', String(isMenuExpanded.value))
    eventBus.emit('menu:toggleCollapse')
  }

  const toggleFullScreen = () => {
    const element = document.documentElement
    if (!document.fullscreenElement) {
      element.requestFullscreen().catch((err) => {
        console.log(`Error attempting to enable full-screen mode: ${err.message}`)
      })
    } else {
      document.exitFullscreen()
    }
  }

  const handleRootCommand = (path: string) => router.push(path)

  const logout = async () => {
    try {
      await ElMessageBox.confirm('您确定要退出登录吗？', '', {
        type: 'warning',
      })
      await userStore.logout()
      router.push('/login')
    } catch (error) {
      console.warn(error)
    }
  }
</script>

<style scoped lang="scss">
  .header-container {
    height: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-sizing: border-box;

    .left-section {
      display: flex;
      align-items: center;
      gap: 20px;
      min-width: 0;

      .menu-toggle {
        cursor: pointer;
        display: flex;
        align-items: center;
        color: var(--el-text-color-primary);
        transition: color 0.3s;

        &:hover {
          color: var(--el-color-primary);
        }
      }

      .breadcrumb {
        font-size: 14px;
        flex: 1;
        min-width: 0;

        :deep(.el-breadcrumb__item) {
          display: flex;
          align-items: center;
          white-space: nowrap;

          &:first-child {
            flex-shrink: 0;
          }

          .dropdown-trigger {
            display: flex;
            align-items: center;
            cursor: pointer;
            outline: none;
            color: var(--el-text-color-regular);
            max-width: 100%;
            overflow: hidden;
            text-overflow: ellipsis;

            &:hover {
              color: var(--el-color-primary);
            }

            .arrow-icon {
              margin-left: 4px;
              font-size: 12px;
              opacity: 0.7;
              transition: transform 0.3s;
            }
          }

          .el-icon-arrow-down {
            transform: rotate(180deg);
          }

          .active-item {
            font-weight: 600;
            color: var(--el-text-color-primary);
          }

          &:last-child {
            .dropdown-trigger {
              cursor: default;
              color: var(--el-text-color-primary);

              &:hover {
                color: var(--el-text-color-primary);
              }

              .arrow-icon {
                display: none;
              }
            }
          }
        }
      }
    }

    .right-section {
      display: flex;
      align-items: center;
      gap: 16px;

      .action-item {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        border-radius: 4px;
        color: var(--el-text-color-regular);
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background-color: var(--el-fill-color-light);
          color: var(--el-color-primary);
        }

        i {
          font-size: 16px;
        }
      }

      .user-info {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 4px 8px;
        border-radius: 4px;
        transition: background-color 0.3s;

        &:hover {
          background-color: var(--el-fill-color-light);
        }

        .avatar {
          background-color: var(--el-fill-color-light);
        }

        .username {
          font-size: 14px;
          color: var(--el-text-color-primary);
          max-width: 100px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  @media (max-width: 992px) {
    .breadcrumb {
      display: none !important;
    }
  }
</style>
