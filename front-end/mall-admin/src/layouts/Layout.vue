<template>
  <el-container class="layout-container">
    <el-aside width="collapse">
      <LayoutMenu />
    </el-aside>

    <el-container>
      <el-header style="background-color: #fff; border-bottom: 1px solid #dcdfe6">
        <LayoutHeader />
      </el-header>
      <el-main>
        <LayoutMain />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
  import LayoutMenu from './Menu.vue'
  import LayoutHeader from './LayoutHeader.vue'
  import LayoutMain from './LayoutMain.vue'
</script>

<style scoped lang="scss">
  .layout-container {
    height: 100vh;
  }
</style>
