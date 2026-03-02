<template>
  <RouterView v-slot="{ Component }">
    <keep-alive>
      <component :is="Component" :key="$route.fullPath" />
    </keep-alive>

    <el-backtop :right="50" :bottom="70" target=".el-main" />
  </RouterView>
</template>
