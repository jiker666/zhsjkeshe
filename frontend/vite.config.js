import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiBase = env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  return {
    plugins: [vue()],
    server: {
      port: Number(env.VITE_DEV_PORT || 5173),
      strictPort: true,
      proxy: {
        '/api': {
          target: apiBase.replace(/\/api\/?$/, ''),
          changeOrigin: true
        }
      }
    }
  }
})
