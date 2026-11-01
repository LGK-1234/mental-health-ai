import { createRouter, createWebHistory } from 'vue-router'
import BackendLayout from '@/components/BackendLayout.vue'
import AuthLayout from '@/components/AuthLayout.vue'
import FrontendLayout from '@/components/FrontendLayout.vue'


// 路由配置
const backendRoutes = [
    {
        path: '/back',
        redirect: '/back/dashboard',
        component: BackendLayout,
        children: [
            {
                path: 'dashboard',
                component: () => import('@/views/dashboard.vue'),
                meta: {
                    title: '数据分析',
                    fileTitle: '数据分析',
                    sectionPath: '/back/dashboard',
                    icon: 'PieChart'
                }
            },
            {
                path: 'knowledge',
                component: () => import('@/views/knowledge.vue'),
                meta: {
                    title: '文章数据',
                    fileTitle: '文章数据',
                    sectionPath: '/back/knowledge',
                    icon: 'ChatLineSquare'
                }
            },
            {
                path: 'knowledge/new',
                name: 'knowledge-create',
                component: () => import('@/views/knowledgeEditor.vue'),
                meta: {
                    title: '文章数据',
                    fileTitle: '文章数据',
                    actionTitle: '新增',
                    sectionPath: '/back/knowledge'
                }
            },
            {
                path: 'knowledge/:id/edit',
                name: 'knowledge-edit',
                component: () => import('@/views/knowledgeEditor.vue'),
                meta: {
                    title: '文章数据',
                    fileTitle: '文章数据',
                    actionTitle: '修改',
                    sectionPath: '/back/knowledge'
                }
            },
            {
                path: 'consultations',
                component: () => import('@/views/consultations.vue'),
                meta: {
                    title: '咨询记录',
                    fileTitle: '咨询记录',
                    sectionPath: '/back/consultations',
                    icon: 'Message'
                }
            },
            {
                path: 'consultations/:id',
                name: 'consultation-detail',
                component: () => import('@/views/consultationDetail.vue'),
                meta: {
                    title: '咨询记录',
                    fileTitle: '咨询记录',
                    actionTitle: '详情',
                    sectionPath: '/back/consultations'
                }
            },
            {
                path: 'emotional',
                component: () => import('@/views/emotional.vue'),
                meta: {
                    title: '情绪日志',
                    fileTitle: '情绪日志',
                    sectionPath: '/back/emotional',
                    icon: 'User'
                }
            },
            {
                path: 'emotional/:id',
                name: 'emotional-detail',
                component: () => import('@/views/emotionalDetail.vue'),
                meta: {
                    title: '情绪日志',
                    fileTitle: '情绪日志',
                    actionTitle: '详情',
                    sectionPath: '/back/emotional'
                }
            }
        ]
    },
    {
        path: '/auth',
        redirect: '/auth/login',
        component: AuthLayout,
        children: [
            {
                path: 'login',
                component: () => import('@/views/login.vue'),
                meta: {
                    title: '登录'
                }
            },
            {
                path: 'register',
                component: () => import('@/views/register.vue'),
                meta: {
                    title: '注册'
                }
            }
        ]
    }
]

const frontendRoutes = [
    {
        path: '/',
        component: FrontendLayout,
        children: [
            {
                path: '',
                component: () => import('@/views/home.vue')
            },
            {
                path: 'consultation',
                component: () => import('@/views/consultation.vue')
            },
            {
                path: 'emotion-diary',
                component: () => import('@/views/emotionDiary.vue')
            },
            {
                path: 'knowledge',
                component: () => import('@/views/frontendKnowledge.vue')
            },
            {
                path: 'knowledge/article/:id',
                component: () => import('@/views/articleDetail.vue'),
                props: true
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes: [ ...backendRoutes, ...frontendRoutes]
})

// 路由前置守卫
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    // 当前用户是否登录
    if (token) {
        const userInfo = JSON.parse(localStorage.getItem('userInfo'))
        // 如果是后台用户
        if (userInfo.userType == 2) {
            if (to.path.startsWith('/back')) {
                next()
            } else {
                next('/back/dashboard')
            }
        } else if (userInfo.userType == 1){
            // 用户端账号只能访问前台路由
            if (to.path.startsWith('/back') || to.path.startsWith('/auth')) {
                next('/')
            } else {
                next()
            }
        }
    } else {
        if (to.path.startsWith('/back')) {
            // 如果是访问后台页面，那么跳转岛登录页
            next('/auth/login')
        } else {
            next()
        }
    }
})

export default router
