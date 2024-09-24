import { defineConfig } from 'vite'
import vue from "@vitejs/plugin-vue";
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    }
  },
  plugins: [
    vue()
  ],
  server: {
    port: 8082,
    proxy: {
      '^/stress/': {
        target: 'http://localhost:8880',
      }
    }
  },
  build: {
    chunkSizeWarningLimit: 1200
  }
})