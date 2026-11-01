<!--
  HelloWorld.vue —— Vue 默认模板生成器的欢迎组件

  【说明】
  这是 Vite + Vue 项目初始化时自动生成的模板文件，
  用于演示 Vue 组件的基本结构（script setup + template + style）和 HMR 热更新。
  实际项目中一般会被删除或替换，但保留作为参考示例。

  【Props】
  - msg : 标题文字（可选 String）
-->

<script setup>
import { ref } from 'vue'

// 接收外部传入的标题文字 prop
defineProps({
  msg: String,
})

// 计数器 —— 演示响应式 ref 的基本用法
const count = ref(0)
</script>

<template>
  <!-- 大标题：显示外部传入的 msg prop -->
  <h1>{{ msg }}</h1>

  <!-- 卡片容器 —— 包含计数器按钮和 HMR 提示 -->
  <div class="card">
    <!-- 按钮：点击触发 count++，Vue 自动响应更新显示值 -->
    <button type="button" @click="count++">count is {{ count }}</button>
    <p>
      Edit
      <code>components/HelloWorld.vue</code> to test HMR
    </p>
  </div>

  <!-- 以下是 Vue + Vite 官方文档链接提示（保留原版未改）-->
  <p>
    Check out
    <a href="https://vuejs.org/guide/quick-start.html#local" target="_blank"
      >create-vue</a
    >, the official Vue + Vite starter
  </p>
  <p>
    Learn more about IDE Support for Vue in the
    <a
      href="https://vuejs.org/guide/scaling-up/tooling.html#ide-support"
      target="_blank"
      >Vue Docs Scaling up Guide</a
    >.
  </p>
  <p class="read-the-docs">Click on the Vite and Vue logos to learn more</p>
</template>

<!-- Scoped 样式 —— read-the-docs 类控制提示文字为灰色 -->
<style scoped>
.read-the-docs {
  color: #888;     /* 灰色弱化提示文字 */
}
</style>
