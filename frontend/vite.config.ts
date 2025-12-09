import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],

  // 开发服务器配置
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 不重写路径，因为后端context-path已经是/api/v1
      }
    }
  },

  // CSS 预处理器配置
  css: {
    preprocessorOptions: {
      scss: {
        // 静默 Sass 弃用警告（Dart Sass 3.0 之前的过渡期）
        // 'import' - @import 规则将被移除
        // 'global-builtin' - 全局内置模块将被移除
        silenceDeprecations: ['import', 'global-builtin'],
      }
    }
  },

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
