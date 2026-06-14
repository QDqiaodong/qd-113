import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '127.0.0.1',
    port: 18041,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:19041',
        changeOrigin: true
      }
    }
  },
  preview: {
    host: '127.0.0.1',
    port: 18041,
    strictPort: true
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})
