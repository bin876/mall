<template>
  <el-sub-menu v-if="hasChildren" :index="fullPath">
    <template #title>
      <el-icon v-if="item.icon">
        <component :is="item.icon" />
      </el-icon>
      <span>{{ item.title }}</span>
    </template>

    <MenuItem
      v-for="child in item.children"
      :key="child.menuId"
      :item="child"
      :base-path="fullPath"
    />
  </el-sub-menu>

  <el-menu-item v-else :index="fullPath">
    <el-icon v-if="item.icon">
      <component :is="item.icon" />
    </el-icon>
    <span>{{ item.title }}</span>
  </el-menu-item>
</template>

<script setup lang="ts">
  import { computed } from 'vue'

  const props = defineProps({
    item: { type: Object, required: true },
    basePath: { type: String, default: '' },
  })

  const fullPath = computed(() => {
    const p = props.item.path.startsWith('/')
      ? props.item.path
      : `${props.basePath}/${props.item.path}`
    return p.replace(/\/+/g, '/')
  })

  const hasChildren = computed(() => {
    return (
      props.item.children &&
      props.item.children.length > 0 &&
      props.item.children.some((child: any) => child.hidden !== 1)
    )
  })
</script>
