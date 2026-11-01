<!--
  Navbar.vue — 旧版管理后台顶栏（已被 BackendLayout 的 file-tabs 取代）
  保留此文件用于兼容或参考，不再作为主布局的一部分使用
-->
<template>
    <div class="navbar">
        <div class="flex-box">
            <!-- 折叠按钮：切换侧边栏展开/收起 -->
            <el-button @click="handleCollapse">
                <!-- @click添加一个方法 -->
                <el-icon><Expand /></el-icon>
            </el-button>
            <!-- 页面标题：从路由 meta 读取 -->
            <p class="page-title">{{ route.meta.title }}</p>
        </div>
        <div class="flex-box">
            <!-- 右上角用户头像下拉菜单 -->
            <el-dropdown @command="handleCommand">
                <div class="flex-box">
                    <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                    <p class="user-name">admin</p>
                    <el-icon><ArrowDown /></el-icon>
                    <!-- 下拉菜单图标 -->
                </div>
                <template #dropdown>
                    <el-dropdown-menu>
                        <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { logout } from '@/api/admin'

const router = useRouter()
const route = useRoute()

// 处理下拉菜单命令：目前只有 logout
const handleCommand = (command) => {
    console.log(command)
    if (command === 'logout') {
        // 弹确认框后调用后端退出接口
        ElMessageBox.confirm('确定退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
            customClass: 'archive-message-box'
        }).then(() => {
            logout().then(() => {
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                router.push('/auth/login')
            })
        })
    }
}

// 切换侧边栏折叠状态
const handleCollapse = () => {
    useAdminStore().toggleCollapse()
    //方法调用
   
    console.log(useAdminStore().isCollapse)
}
</script>

<style lang="scss" scoped>
.navbar {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 15px;
    background: white;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    border-bottom: 1px solid #e5e7eb;
    .flex-box {
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .page-title {
        margin-left: 20px;
        font-size: 26px;
        font-weight: bold;
        color: #1f2937;
    }
}
</style>
