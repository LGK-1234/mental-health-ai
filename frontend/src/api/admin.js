// 引入封装好的 axios 实例（已配置 baseURL、token 请求拦截器和响应拦截器）
import service from '@/utils/request'

// 用户登录
// data：{ username: 用户名, password: 密码 }
// 成功后后端返回 token 和用户信息
export function login(data) {
    // POST /api/user/login
    return service.post('/user/login', data)
}


// 获取知识分类的树形结构（用于文章分类筛选、级联选择等）
export function categoryTree() {
    return service.get('/knowledge/category/tree')
}

// 分页查询知识文章列表
// params：分页和筛选条件，如 { pageNum, pageSize, title, categoryId, status }
// { params } 会被 axios 自动拼成 URL 查询串：?pageNum=1&pageSize=10...
export function articlePage(params) {
     return service.get('/knowledge/article/page', { params })
}

// 上传文件（如文章封面图）
// file：要上传的文件对象；businessInfo：{ businessId } 文件归属的业务数据 id
// 文件不能像普通表单那样用 JSON 传输，需要包装成 multipart/form-data
export function uploadFile(file, businessInfo) {
    const formData = new FormData()
    formData.append('file', file)                       // 真正要上传的文件
    formData.append('businessType', 'ARTICLE')         // 业务类型：文章（固定值）
    formData.append('businessId', businessInfo.businessId) // 文件归属的文章 id
    formData.append('businessField', 'cover')          // 业务字段：文章封面

    return service.post('/file/upload', formData, {
        headers: {
            // 指定为表单文件上传格式
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 新增知识文章
// data：文章表单数据（标题、内容、分类 id、封面地址等）
export function createArticle(data) {
    return service.post('/knowledge/article', data)
}

// 查询文章详情（打开编辑弹窗时用于数据回显）
// id：文章 id，放在 URL 路径中传递
export function getArticleDetail(id) {
    return service.get(`/knowledge/article/${id}`)
}

// 更新文章
// id：要修改的文章 id；data：修改后的文章数据
export function updateArticle(id, data) {
    return service.put(`/knowledge/article/${id}`, data)
}

// 修改文章状态（如发布 1 / 下架 0）
// data：通常为 { status }
export function changeArticleStatus(id, data) {
    return service.put(`/knowledge/article/${id}/status`, data)
}

// 删除文章（后端一般为逻辑删除，数据库记录仍保留）
export function deleteArticle(id) {
    return service.delete(`/knowledge/article/${id}`)
}

// 分页查询心理咨询会话记录
// params：{ pageNum, pageSize, 其他筛选条件 }
export function getConsultationPage(params) {
    return service.get('/psychological-chat/sessions', { params })
}

// 获取某次咨询会话下的聊天消息明细
// sessionId：会话 id
export function getSessionDetail(sessionId) {
    return service.get(`/psychological-chat/sessions/${sessionId}/messages`)
}

// 管理端分页查询用户情绪日志
export function getEmotionalPage(params) {
    return service.get('/emotion-diary/admin/page', { params })
}

// 删除指定的情绪日志
// id：情绪日志 id
export function deleteEmotional(id) {
    return service.delete(`/emotion-diary/admin/${id}`)
}

// 获取后台首页的综合数据分析（用户数、咨询数、情绪统计等）
export function getAnalyticsOverview() {
    return service.get(`/data-analytics/overview`)
}

// 退出登录（让后端的 token 失效；前端还需自行清除 localStorage 中的登录信息）
export function logout() {
    return service.post('/user/logout')
}