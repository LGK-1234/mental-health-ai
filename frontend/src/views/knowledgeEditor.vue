<template>
  <div class="article-editor-page" v-loading="pageLoading">
    <PageHead
      :title="isEdit ? '修改文章资料' : '建立文章资料'"
      kicker="ARTICLE RECORD"
      :subtitle="isEdit ? '在这里修改内容、发布状态或删除文章。' : '填写资料后建立新文章，默认以草稿状态保存。'"
    >
      <template #buttons>
        <el-button @click="goBack"><el-icon><Back /></el-icon>返回文章目录</el-button>
      </template>
    </PageHead>

    <div class="record-banner">
      <div><span>DOCUMENT NO.</span><strong>{{ isEdit ? articleId : 'WAITING FOR FILING' }}</strong></div>
      <div class="status-block">
        <span>CURRENT STATUS</span>
        <strong class="status-stamp" :class="`status-${formData.status}`">{{ statusText }}</strong>
      </div>
    </div>

    <el-form ref="formRef" class="article-form" :model="formData" :rules="rules" label-position="top">
      <section class="form-sheet">
        <header class="section-heading">
          <span class="section-number">01</span>
          <div><p>BASIC INFORMATION</p><h3>基础资料</h3></div>
        </header>

        <div class="basic-grid">
          <el-form-item label="文章标题" prop="title">
            <el-input v-model="formData.title" placeholder="请输入文章标题" maxlength="200" show-word-limit clearable />
          </el-form-item>

          <el-form-item label="所属分类" prop="categoryId">
            <el-select v-model="formData.categoryId" placeholder="请选择分类" clearable>
              <el-option v-for="item in categories" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item class="full-field" label="文章摘要" prop="summary">
            <el-input v-model="formData.summary" type="textarea" placeholder="简要说明文章内容（可选）" maxlength="1000" show-word-limit :rows="4" />
          </el-form-item>

          <el-form-item class="full-field" label="资料标签" prop="tags">
            <el-select v-model="formData.tagArray" placeholder="输入或选择标签" multiple filterable allow-create default-first-option>
              <el-option v-for="tag in commonTags" :key="tag" :label="tag" :value="tag" />
            </el-select>
          </el-form-item>

          <el-form-item class="full-field" label="封面资料">
            <div class="cover-upload">
              <el-upload action="#" :before-upload="beforeUpload" :http-request="handleUploadRequest" :show-file-list="false" accept="image/*">
                <div v-if="!imgUrl" class="cover-placeholder">
                  <el-icon><Picture /></el-icon><strong>装订封面图片</strong><span>点击选择图片，大小不超过 5MB</span>
                </div>
                <img v-else :src="imgUrl" class="cover-image" alt="文章封面" />
              </el-upload>
              <el-button v-if="imgUrl" class="remove-cover" text type="danger" @click="handleRemove">移除封面</el-button>
            </div>
          </el-form-item>
        </div>
      </section>

      <section class="form-sheet editor-sheet">
        <header class="section-heading">
          <span class="section-number">02</span>
          <div><p>ARTICLE CONTENT</p><h3>正文资料</h3></div>
        </header>
        <el-form-item label="文章内容" prop="content">
          <RichTextEditor
            v-model="formData.content"
            placeholder="请输入文章内容，可使用标题、列表、加粗等格式。"
            :maxCharCount="5000"
            :showSecurityTip="false"
            min-height="460px"
            @change="handleContentChange"
            @created="handleEditorCreated"
          />
        </el-form-item>
      </section>

      <div class="action-dock">
        <div class="primary-actions">
          <el-button type="primary" :loading="saving" @click="handleSubmit">{{ isEdit ? '保存修改' : '建立资料' }}</el-button>
        </div>

        <div v-if="isEdit" class="record-actions">
          <span>资料处置</span>
          <el-button v-if="formData.status !== 1" class="publish-button" :loading="statusLoading" @click="handleStatusChange(1)">发布文章</el-button>
          <el-button v-else class="unpublish-button" :loading="statusLoading" @click="handleStatusChange(2)">下架文章</el-button>
          <el-button class="delete-button" :loading="deleting" @click="handleDelete">删除资料</el-button>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHead from '@/components/PageHead.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { categoryTree, changeArticleStatus, createArticle, deleteArticle, getArticleDetail, updateArticle, uploadFile } from '@/api/admin'
import { fileBaseUrl } from '@/config/index.js'

const route = useRoute()
const router = useRouter()
const articleId = computed(() => route.params.id)
const isEdit = computed(() => Boolean(articleId.value))
const statusText = computed(() => ({ 0: '草稿', 1: '已发布', 2: '已下线' })[formData.status] || '草稿')

const formRef = ref()
const pageLoading = ref(false)
const saving = ref(false)
const statusLoading = ref(false)
const deleting = ref(false)
const categories = ref([])
const imgUrl = ref('')
const businessId = ref(null)
const editorInstance = ref(null)

const formData = reactive({
  id: '', title: '', content: '', coverImage: '', categoryId: '', summary: '',
  tags: '', tagArray: [], status: 0
})

const rules = reactive({
  title: [
    { required: true, message: '请输入文章标题', trigger: 'blur' },
    { max: 200, message: '文章标题最多 200 个字符', trigger: 'blur' }
  ],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'change' }]
})

const commonTags = [
  '情绪管理', '焦虑', '抑郁', '压力', '睡眠', '冥想', '正念',
  '放松', '心理健康', '自我成长', '人际关系', '工作压力', '学习方法', '生活技巧'
]

const resolveCoverUrl = (path) => !path ? '' : (/^https?:\/\//.test(path) ? path : `${fileBaseUrl}${path}`)

const loadCategories = async () => {
  const data = await categoryTree()
  categories.value = data.map(item => ({ label: item.categoryName, value: item.id }))
}

const loadArticle = async () => {
  if (!isEdit.value) return
  const data = await getArticleDetail(articleId.value)
  Object.assign(formData, data, {
    tagArray: data.tags ? data.tags.split(',').filter(Boolean) : [],
    status: Number(data.status ?? 0)
  })
  businessId.value = data.id
  imgUrl.value = resolveCoverUrl(data.coverImage)
  if (editorInstance.value && data.content) {
    await nextTick()
    editorInstance.value.setHtml(data.content)
  }
}

const beforeUpload = (file) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return false
  }
  if (file.size / 1024 / 1024 >= 5) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleUploadRequest = async ({ file }) => {
  if (!businessId.value) businessId.value = crypto.randomUUID?.() || `article-${Date.now()}`
  const fileRes = await uploadFile(file, { businessId: businessId.value })
  formData.coverImage = fileRes.filePath
  imgUrl.value = resolveCoverUrl(fileRes.filePath)
}

const handleRemove = () => {
  imgUrl.value = ''
  formData.coverImage = ''
}

const handleContentChange = ({ html }) => { formData.content = html }
const handleEditorCreated = (editor) => {
  editorInstance.value = editor
  if (formData.content) nextTick(() => editor.setHtml(formData.content))
}

const buildSubmitData = () => {
  const data = { ...formData, tags: formData.tagArray.join(',') }
  delete data.tagArray
  if (!isEdit.value && businessId.value) data.id = businessId.value
  if (!data.id) delete data.id
  return data
}

const handleSubmit = async () => {
  if (saving.value) return
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEdit.value) {
      await updateArticle(articleId.value, buildSubmitData())
      ElMessage.success('文章资料已更新')
    } else {
      await createArticle(buildSubmitData())
      ElMessage.success('文章资料已建立')
      router.push('/back/knowledge')
    }
  } finally {
    saving.value = false
  }
}

const handleStatusChange = async (nextStatus) => {
  const publishing = nextStatus === 1
  try {
    await ElMessageBox.confirm(`确认${publishing ? '发布' : '下架'}文章“${formData.title}”吗？`, `${publishing ? '发布' : '下架'}确认`, {
      confirmButtonText: `确认${publishing ? '发布' : '下架'}`,
      cancelButtonText: '取消',
      type: publishing ? 'info' : 'warning',
      customClass: 'archive-message-box'
    })
  } catch { return }

  statusLoading.value = true
  try {
    await changeArticleStatus(articleId.value, { status: nextStatus })
    formData.status = nextStatus
    ElMessage.success(publishing ? '文章已发布' : '文章已下架')
  } finally { statusLoading.value = false }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(`删除后将无法在目录中找到“${formData.title}”，确认继续吗？`, '删除文章资料', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'archive-message-box',
      confirmButtonClass: 'archive-delete-confirm'
    })
  } catch { return }

  deleting.value = true
  try {
    await deleteArticle(articleId.value)
    ElMessage.success('文章资料已删除')
    router.push('/back/knowledge')
  } finally { deleting.value = false }
}

const goBack = () => router.push('/back/knowledge')

onMounted(async () => {
  pageLoading.value = true
  try {
    await loadCategories()
    await loadArticle()
  } finally { pageLoading.value = false }
})
</script>

<style scoped lang="scss">
.record-banner{display:flex;justify-content:space-between;gap:24px;margin-bottom:22px;padding:14px 17px;border:1px dashed rgba(53,46,41,.58);background:rgba(189,169,130,.16)}
.record-banner>div{display:flex;flex-direction:column;gap:4px}.record-banner span{color:#74675d;font:700 8px 'Courier New',monospace;letter-spacing:.12em}.record-banner strong{font-size:13px}.status-block{align-items:flex-end}.status-stamp{padding:3px 8px;border:1px solid currentColor;border-radius:2px;transform:rotate(-1deg)}.status-0{color:#74675d}.status-1{color:#41654b}.status-2{color:#8f3d32}
.form-sheet{box-sizing:border-box;margin-bottom:22px;padding:22px;border:1.5px solid rgba(53,46,41,.64);border-radius:3px 7px 4px 8px;background:rgba(255,253,248,.42);box-shadow:3px 4px 0 rgba(53,46,41,.07)}
.section-heading{display:flex;align-items:center;gap:12px;margin-bottom:20px;padding-bottom:12px;border-bottom:1px dashed rgba(53,46,41,.35)}.section-heading p{margin:0 0 2px;color:#8f3d32;font:700 8px 'Courier New',monospace;letter-spacing:.12em}.section-heading h3{margin:0;color:#352e29;font:800 19px 'Noto Serif SC','Songti SC',SimSun,serif}.section-number{width:36px;height:36px;display:grid;place-items:center;color:#8f3d32;border:1px solid #8f3d32;border-radius:50%;font:700 11px 'Courier New',monospace}
.basic-grid{display:grid;grid-template-columns:minmax(0,1.8fr) minmax(220px,1fr);gap:0 20px}.full-field{grid-column:1/-1}.article-form :deep(.el-form-item__label){height:auto;margin-bottom:7px;padding:0;color:#403832;font-size:12px;font-weight:800;line-height:1.4}.article-form :deep(.el-select){width:100%}
.cover-upload{width:min(320px,100%)}.cover-upload :deep(.el-upload){width:100%;display:block;overflow:hidden;border:1.5px dashed rgba(53,46,41,.56);border-radius:3px;background:rgba(239,227,204,.3)}.cover-placeholder{min-height:150px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px;color:#74675d}.cover-placeholder .el-icon{margin-bottom:3px;color:#8f3d32;font-size:28px}.cover-placeholder strong{color:#352e29;font-family:'Noto Serif SC','Songti SC',SimSun,serif}.cover-placeholder span{font-size:11px}.cover-image{width:100%;height:172px;display:block;object-fit:cover}.remove-cover{margin-top:8px;color:#766442}
.editor-sheet,.editor-sheet :deep(.el-form-item),.editor-sheet :deep(.el-form-item__content),.editor-sheet :deep(.rich-text-editor){box-sizing:border-box;width:100%}.editor-sheet :deep(.el-form-item__content){display:block}.editor-sheet :deep(.rich-text-editor){border:1.5px solid rgba(53,46,41,.6);border-radius:2px;background:rgba(255,255,255,.52)}.editor-sheet :deep(.editor-container),.editor-sheet :deep(.w-e-text-container){width:100%}.editor-sheet :deep(.w-e-toolbar),.editor-sheet :deep(.editor-footer){background:rgba(239,227,204,.52)}
.action-dock{position:sticky;bottom:0;z-index:4;display:flex;align-items:center;justify-content:space-between;gap:22px;padding:16px 18px;border:1.5px solid #352e29;border-radius:3px 7px 4px 8px;background:rgba(239,227,204,.96);box-shadow:4px 5px 0 rgba(53,46,41,.12);backdrop-filter:blur(8px)}.primary-actions,.record-actions{display:flex;align-items:center;gap:8px}.record-actions>span{margin-right:3px;color:#74675d;font:700 9px 'Courier New','Microsoft YaHei',monospace;letter-spacing:.08em}.publish-button{color:#41654b;border-color:#41654b;background:rgba(65,101,75,.08)}.unpublish-button{color:#8a612b;border-color:#8a612b;background:rgba(138,97,43,.08)}.delete-button{color:#fffaf0;border-color:#514634;background:#8f7b56}.delete-button:hover,.delete-button:focus{color:#fffaf0;border-color:#352e29;background:#766442}
@media(max-width:880px){.basic-grid{grid-template-columns:1fr}.full-field{grid-column:auto}.action-dock{align-items:stretch;flex-direction:column}.primary-actions,.record-actions{justify-content:flex-end;flex-wrap:wrap}}
@media(max-width:620px){.form-sheet{padding:16px}.record-banner{align-items:flex-start;flex-direction:column}.status-block{align-items:flex-start}.primary-actions,.record-actions{display:grid;grid-template-columns:1fr 1fr}.record-actions>span{grid-column:1/-1}.primary-actions :deep(.el-button),.record-actions :deep(.el-button){width:100%;margin:0}}
</style>
