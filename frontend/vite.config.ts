import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],

  // 优化配置
  optimizeDeps: {
    // 排除pdfjs-dist，让其在运行时加载
    exclude: ['pdfjs-dist']
  },

  // Worker配置
  worker: {
    format: 'es'
  },

  // 构建配置
  build: {
    // 确保worker文件被正确处理
    rollupOptions: {
      output: {
        // 保持worker文件的模块格式
        manualChunks: undefined
      }
    }
  }
});
