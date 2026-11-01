<template>
  <section class="auth-record" aria-labelledby="login-title">
    <header class="record-heading">
      <span class="record-index">01</span>
      <div>
        <p class="record-kicker">身份核验资料</p>
        <h2 id="login-title">登录</h2>
      </div>
    </header>

    <el-form
      ref="ruleFormRef"
      class="record-form"
      :model="formData"
      :rules="rules"
      label-position="top"
      @submit.prevent="submitForm(ruleFormRef)"
    >
      <el-form-item label="用户名或邮箱" prop="username">
        <el-input
          v-model="formData.username"
          size="large"
          placeholder="请输入用户名或邮箱"
          autocomplete="username"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="formData.password"
          size="large"
          placeholder="请输入密码"
          type="password"
          autocomplete="current-password"
          show-password
          @keyup.enter="submitForm(ruleFormRef)"
        />
      </el-form-item>

      <el-button
        class="file-submit"
        size="large"
        native-type="submit"
        :loading="submitting"
      >
        登录并打开档案
      </el-button>
    </el-form>

    <p class="switch-record">
      还没有档案？
      <router-link to="/auth/register">抽取注册资料</router-link>
    </p>
  </section>
</template>
<script setup>
import { ref, reactive } from 'vue'
import { login } from '@/api/admin'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const ruleFormRef = ref()
const submitting = ref(false)

const formData = reactive({
    username: '',
    password: ''
})
const rules = reactive({
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
    ]
})

const router = useRouter()
const submitForm = async (formEl) => {
  if (!formEl || submitting.value) return

  const valid = await formEl.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const result = await login(formData)
    const data = result?.data?.data ?? result?.data ?? result

    if (!data?.token || !data?.userInfo) {
      ElMessage.error(data?.message || data?.msg || '登录失败，请检查账户信息')
      return
    }

    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))

    if (data.userInfo.userType === 2) {
      router.push('/back/dashboard')
    } else {
      router.push('/')
    }
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.response?.data?.msg || '登录失败，请稍后重试')
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
  margin-bottom: 22px;
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

.record-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-form-item__label) {
    height: auto;
    margin-bottom: 7px;
    padding: 0;
    color: #403832;
    font-size: 13px;
    font-weight: 700;
    line-height: 1.4;
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
  transform: rotate(-0.1deg);
  transition: transform 140ms ease, box-shadow 140ms ease, background-color 140ms ease;

  &:hover,
  &:focus {
    color: #fffaf0;
    border-color: #352e29;
    background: #773027;
    transform: translate(-1px, -1px) rotate(-0.1deg);
    box-shadow: 4px 4px 0 #352e29;
  }

  &:active {
    transform: translate(2px, 2px);
    box-shadow: 1px 1px 0 #352e29;
  }
}

.switch-record {
  margin: 20px 0 0;
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
  .record-heading {
    margin-bottom: 18px;
  }

  h2 {
    font-size: 24px;
  }
}
</style>
