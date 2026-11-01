import { defineStore } from 'pinia'
//调用defineStore函数或者方法，创建一个名为admin的store
import { ref } from 'vue'

export const useAdminStore = defineStore('admin', () => {
    //export对外进行一个暴露。
    const isCollapse = ref(false)
    //默认是展开的用ref(false)表示，声明一个变量isCollapse

    const toggleCollapse = () => {
        //箭头函数，切换isCollapse的值
        isCollapse.value = !isCollapse.value
    }

    return {
        isCollapse,
        toggleCollapse
        //对外进行一个暴露return出去。
    }
})