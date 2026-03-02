<template>
  <el-scrollbar>
    <el-menu
      style="height: 100vh"
      router
      :default-active="activeMenu"
      :collapse="isCollapse"
      class="el-menu-vertical-demo"
    >
      <MenuItem v-for="item in visibleMenus" :key="item.menuId" :item="item" />
    </el-menu>
  </el-scrollbar>
</template>

<script setup lang="ts">
  import MenuItem from './MenuItem.vue'
  import { useUserStore } from '@/stores/user'
  import eventBus from '@/utils/eventBus'
  import { useRoute } from 'vue-router'

  const userStore = useUserStore()
  const visibleMenus = computed(() => {
    const filterMenus: any = (menus: any[]) => {
      return menus
        .filter((menu) => menu.hidden !== 1 && menu.type !== 2)
        .map((menu) => ({
          ...menu,
          children: menu.children ? filterMenus(menu.children) : [],
        }))
    }
    return filterMenus(userStore.menus || [])
  })

  const route = useRoute()
  const activeMenu = computed(() => route.path)

  const isCollapse = ref(false)

  const handleCollapse = () => {
    isCollapse.value = !isCollapse.value
    console.log('test')
  }

  onMounted(() => {
    eventBus.on('menu:toggleCollapse', handleCollapse)
  })

  onUnmounted(() => {
    eventBus.off('menu:toggleCollapse', handleCollapse)
  })
</script>

<style lang="scss">
  .el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 200px;
  }
</style>
