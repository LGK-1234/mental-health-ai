import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    // 5173 已被其他项目的 dev server 占用，这里固定用 5174
    port: 5174,
    strictPort: false,
    // 忽略编辑器原子保存产生的临时文件，避免 Windows 下 EBUSY 导致 dev server 崩溃
    watch: {
      ignored: ['**/node_modules/**', '**/.git/**', '**/*.tmp', '**/.*.tmpdir/**', '**/.*.tmpdir']
    },
    proxy: {
      // 本地后端（原来的 159.75.169.224:1235 是作者的远程服务）
      '/api': {
        target: 'http://localhost:1236',
        changeOrigin: true
      }
    }
  }
})
