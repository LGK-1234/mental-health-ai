<!--
  TableSearch.vue — 通用搜索表单组件
  拟物风格的"检索单"，传入 formItem 配置即可动态生成查询条件
  使用示例：
    <TableSearch :formItem="[
      { label: '名称', prop: 'name', comp: 'input' },
      { label: '状态', prop: 'status', comp: 'select', options: [...] }
    ]" @search="handleSearch" />
-->
<template>
  <section class="search-slip">
    <!-- === 检索单头部 === -->
    <header class="slip-heading">
      <div>
        <span class="slip-code">SEARCH REQUEST</span>
        <h3>{{ props.title }}</h3>
      </div>
      <span class="slip-number">FORM · 01</span>
    </header>

    <!-- === 搜索表单 === -->
    <el-form ref="ruleFormRef" :model="formData" class="search-form" label-position="top">
      <div class="field-grid">
        <el-form-item
          v-for="item in formItemAttrs"
          :key="item.prop"
          :label="item.label"
          :prop="item.prop"
        >
          <!-- 动态组件：comp 字段决定渲染 el-input 还是 el-select -->
          <component
            :is="isComp(item.comp)"
            v-model="formData[item.prop]"
            :placeholder="item.placeholder"
            clearable
          >
            <!-- select 类型时渲染选项列表，默认带一个"全部"选项 -->
            <template v-if="item.comp === 'select'">
              <el-option label="全部" value="" />
            <!--el-option可以选择的元素-->
              <el-option
                v-for="opt in item.options"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </template>
          </component>
        </el-form-item>
      </div>

      <!-- 操作按钮区 -->
      <div class="search-actions">
        <el-button type="primary" @click="handleSearch">检索资料</el-button>
        <el-button @click="handleReset(ruleFormRef)">清空条件</el-button>
      </div>
    </el-form>
  </section>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
//computed计算属性

const props = defineProps({
  formItem: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: '资料检索'
  }
})
const emit = defineEmits(['search'])

// 深拷贝 formItem，避免直接修改 props
const formItemAttrs = computed(() => {
  return props.formItem.map(item => ({ ...item }))
})

const ruleFormRef = ref()
const formData = reactive({})

// 组件类型映射：把缩写 'input' / 'select' 映射为 Element Plus 组件名
const isComp = (comp) => {
  return {
    input: 'elInput',
    select: 'elSelect'
  }[comp]
}

// 点击"检索资料"：发射 search 事件并携带当前表单数据
const handleSearch = () => {
  emit('search', { ...formData })
}

// 点击"清空条件"：重置表单后立即触发一次搜索（获取全量数据）
const handleReset = (formEl) => {
  //先重置表单，然后再出发查询
  if (!formEl) return
  formEl.resetFields()
  emit('search', { ...formData })
}
</script>

<style scoped lang="scss">
/* ===== 检索单整体：虚线边框 + 拟物阴影 ===== */
.search-slip {
  position: relative;
  box-sizing: border-box;
  margin-bottom: 26px;
  padding: 19px 20px 17px;
  border: 1.5px dashed rgba(53, 46, 41, 0.72);
  border-radius: 3px 7px 4px 8px;
  background: rgba(239, 227, 204, 0.42);
  box-shadow: 3px 4px 0 rgba(53, 46, 41, 0.07);

  /* 左右两侧的"装订孔"装饰 */
  &::before,
  &::after {
    content: '';
    position: absolute;
    top: 48%;
    width: 12px;
    height: 22px;
    border: 1.5px solid rgba(53, 46, 41, 0.72);
    border-radius: 50%;
    background: #fffaf0;
  }

  &::before {
    left: -7px;
    border-left-color: transparent;
  }

  &::after {
    right: -7px;
    border-right-color: transparent;
  }
}

/* ===== 检索单头部 ===== */
.slip-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px dashed rgba(53, 46, 41, 0.35);

  h3 {
    margin: 3px 0 0;
    color: #352e29;
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
    font-size: 17px;
  }
}

.slip-code,
.slip-number {
  color: #8f3d32;
  font-family: 'Courier New', monospace;
  font-size: 8px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.slip-number {
  color: #74675d;
}

/* ===== 表单字段网格 ===== */
.field-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(170px, 1fr));
  gap: 13px 18px;
}

/* 深度定制 Element Plus 表单样式 */
.search-form {
  :deep(.el-form-item) {
    width: 100%;
    display: block;
    margin-bottom: 0;
  }

  :deep(.el-form-item__content) {
    width: 100%;
    display: block;
    line-height: normal;
  }

  :deep(.el-form-item__label) {
    height: auto;
    margin-bottom: 6px;
    padding: 0;
    color: #403832;
    font-size: 12px;
    font-weight: 800;
    line-height: 1.4;
  }

  :deep(.el-input),
  :deep(.el-select) {
    width: 100%;
  }
}

.search-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

/* ===== 响应式 ===== */
@media (max-width: 1080px) {
  .field-grid {
    grid-template-columns: repeat(2, minmax(170px, 1fr));
  }
}

@media (max-width: 620px) {
  .search-slip {
    padding: 16px;
  }

  .field-grid {
    grid-template-columns: 1fr;
  }

  .search-actions {
    :deep(.el-button) {
      flex: 1;
      margin: 0;
    }
  }
}
</style>
