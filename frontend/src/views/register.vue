<template>
  <section class="auth-record" aria-labelledby="register-title">
    <header class="record-heading">
      <span class="record-index">02</span>
      <div>
        <p class="record-kicker">新建个人档案</p>
        <h2 id="register-title">注册</h2>
      </div>
    </header>

    <el-form
      ref="submitFormRef"
      class="record-form"
      label-position="top"
      :model="formData"
      :rules="rules"
      @submit.prevent="submitForm(submitFormRef)"
    >
      <div class="form-grid">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="设置唯一用户名"
            size="large"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="填写常用邮箱"
            size="large"
            autocomplete="email"
          />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="可选" size="large" autocomplete="nickname" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="可选" size="large" autocomplete="tel" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            placeholder="请输入密码"
            size="large"
            type="password"
            autocomplete="new-password"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="formData.confirmPassword"
            placeholder="再次输入密码"
            size="large"
            type="password"
            autocomplete="new-password"
            show-password
            @keyup.enter="submitForm(submitFormRef)"
          />
        </el-form-item>
      </div>

      <el-button
        class="file-submit"
        size="large"
        native-type="submit"
        :loading="submitting"
      >
        建立个人档案
      </el-button>
    </el-form>

    <p class="switch-record">
      已经有档案？
      <router-link to="/auth/login">放回并抽取登录资料</router-link>
    </p>
  </section>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { register } from '@/api/frontend'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const submitting = ref(false)
const formData = reactive({
    "username": "",
    "email": "",
    "nickname": "",
    "phone": "",
    "password": "",
    "confirmPassword": "",
    "gender": 0, // 性别1为男,2为女
    "userType": 1 // 1为普通用户
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ],
    confirmPassword: [
        { validator: validateConfirmPassword, trigger: ['blur', 'change'] }
    ]
})

const submitFormRef = ref(null)
const submitForm = async (formEl) => {
  if (!formEl || submitting.value) return

  const valid = await formEl.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const result = await register(formData)
    const data = result?.data?.data ?? result?.data ?? result

    if (data?.code === 'BUSINESS_ERROR' || data?.success === false) {
      ElMessage.error(data.message || data.msg || '注册信息已存在')
      return
    }

    ElMessage.success('注册成功，请登录')
    router.push('/auth/login')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.response?.data?.msg || '注册失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.auth-record {
  position: relative;
  z-index: 1;
}

.record-heading {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 18px;
}

.record-index {
  display: grid;
  width: 43px;
  height: 43px;
  place-items: center;
  color: #8f3d32;
  border: 1px solid #8f3d32;
  border-radius: 50%;
  font-family: 'Courier New', monospace;
  font-size: 15px;
  font-weight: 700;
}

.record-kicker {
  margin: 0 0 1px;
  color: #786b60;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

h2 {
  margin: 0;
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-size: 27px;
  font-weight: 800;
  line-height: 1.15;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
}

.record-form {
  :deep(.el-form-item) {
    margin-bottom: 14px;
  }

  :deep(.el-form-item__label) {
    height: auto;
    margin-bottom: 6px;
    padding: 0;
    color: #403832;
    font-size: 12px;
    font-weight: 700;
    line-height: 1.35;
  }

  :deep(.el-input__wrapper) {
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.38);
    box-shadow: 0 0 0 1.5px #514840 inset !important;
    transition: background-color 180ms ease, box-shadow 180ms ease;
  }

  :deep(.el-input__wrapper:hover),
  :deep(.el-input__wrapper.is-focus) {
    background: rgba(255, 255, 255, 0.7);
    box-shadow: 0 0 0 2px #8f3d32 inset !important;
  }

  :deep(.el-input__inner) {
    color: #352e29;
    font-family: 'Microsoft YaHei', sans-serif;
  }
}

.file-submit {
  width: 100%;
  height: 44px;
  margin-top: 5px;
  color: #fffaf0;
  border: 2px solid #352e29;
  border-radius: 3px 6px 4px 7px;
  background: #8f3d32;
  box-shadow: 3px 3px 0 #352e29;
  font-weight: 700;
  letter-spacing: 0.08em;
  transform: rotate(0.08deg);
  transition: transform 140ms ease, box-shadow 140ms ease, background-color 140ms ease;

  &:hover,
  &:focus {
    color: #fffaf0;
    border-color: #352e29;
    background: #773027;
    transform: translate(-1px, -1px) rotate(0.08deg);
    box-shadow: 4px 4px 0 #352e29;
  }

  &:active {
    transform: translate(2px, 2px);
    box-shadow: 1px 1px 0 #352e29;
  }
}

.switch-record {
  margin: 18px 0 0;
  color: #786b60;
  text-align: center;
  font-size: 13px;

  a {
    color: #8f3d32;
    font-weight: 800;
    text-decoration-thickness: 1px;
    text-underline-offset: 4px;
  }
}

@media (max-width: 520px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .record-heading {
    margin-bottom: 18px;
  }

  h2 {
    font-size: 24px;
  }
}
</style>
