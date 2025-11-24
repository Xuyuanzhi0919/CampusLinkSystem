<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
import { useUserStore } from '@/stores/user';

// #ifdef H5
import { setupRouter } from './router/index'
// #endif

// 初始化用户状态
const userStore = useUserStore();

onLaunch(() => {
  console.log("App Launch");

  // 从本地存储恢复用户信息
  userStore.init();
  console.log('用户登录状态:', userStore.isLoggedIn);

  // H5 端路由守卫初始化
  // #ifdef H5
  setupRouter();
  console.log('H5 路由守卫已启动');
  // #endif

  // 监听页面不存在
  uni.onPageNotFound((res) => {
    console.log('页面不存在:', res.path);
    // 重定向到 404 页面
    uni.redirectTo({
      url: '/pages/error/404',
      fail: () => {
        // 如果 404 页面也不存在，返回首页
        uni.reLaunch({
          url: '/pages/home/index'
        });
      }
    });
  });
});

onShow(() => {
  console.log("App Show");
});

onHide(() => {
  console.log("App Hide");
});
</script>

<style lang="scss">
/* ========== 全局样式 ========== */

/* ========== 全局基调 Design Tokens（符合 uiDesign/index.md）========== */
:root {
  /* 品牌主色 - #2563EB（比现有蓝更稳）*/
  --cl-primary: #2563EB;
  --cl-primary-50: #EFF6FF;
  --cl-primary-100: #DBEAFE;
  --cl-primary-200: #BFDBFE;
  --cl-primary-600: #2563EB;
  --cl-primary-700: #1D4ED8;

  /* 辅助紫 - #6C5CE7（只用于强调）*/
  --cl-accent-purple: #6C5CE7;

  /* 点缀橙 - #F59E0B（用于"答/提醒"，饱和度收 10%）*/
  --cl-accent-orange: #F59E0B;

  /* 其他辅色 */
  --cl-accent: #FB923C;         /* 温暖橙 */
  --cl-info: #2563EB;           /* 信息蓝 */
  --cl-success: #16A34A;        /* 任务绿（文档中的 #16A34A）*/
  --cl-success-100: #DCFCE7;    /* 浅绿 */
  --cl-warning: #F59E0B;        /* 柔和黄 */
  --cl-warning-100: #FEF3C7;    /* 浅黄 */
  --cl-error: #EF4444;          /* 柔和红 */

  /* 中性色（深灰蓝系 - 纸质感）*/
  --cl-gray-900: #0F172A;       /* 深灰蓝（主文本，文档中的 #0F172A）*/
  --cl-gray-800: #334155;
  --cl-gray-700: #475569;
  --cl-gray-600: #64748B;       /* 副文本 */
  --cl-gray-500: #94A3B8;
  --cl-gray-400: #CBD5E1;
  --cl-gray-300: #E2E8F0;
  --cl-gray-200: #EAEAEA;       /* 分隔线 */
  --cl-gray-100: #F1F5F9;
  --cl-gray-50:  #F8FAFC;

  /* 背景与表面（纸质感）*/
  --cl-bg: #FAFAFC;             /* 页面背景（文档中的 #FAFAFC）*/
  --cl-surface: #FFFFFF;        /* 卡片/容器 */
  --cl-surface-2: #FBFBFE;      /* 次级容器（右栏背景）*/

  /* 文字 */
  --cl-text: var(--cl-gray-900);
  --cl-text-sub: var(--cl-gray-600);
  --cl-divider: var(--cl-gray-200);

  /* 阴影（文档规范：Elev.1 和 Elev.2）*/
  --shadow-elev-1: 0 2px 8px rgba(0, 0, 0, 0.05);   /* Elev.1 */
  --shadow-elev-2: 0 6px 20px rgba(0, 0, 0, 0.08);  /* Elev.2 */
  --shadow-hover: 0 4px 12px rgba(0, 0, 0, 0.08);

  /* 兼容旧变量 */
  --shadow-1: var(--shadow-elev-1);
  --shadow-2: var(--shadow-elev-2);
  --shadow-3: 0 8px 24px rgba(0, 0, 0, 0.06);

  /* 圆角体系（文档规范：16 容器 / 12 卡片 / 10 控件）*/
  --radius-container: 16px;  /* 容器 */
  --radius-card: 12px;       /* 卡片 */
  --radius-control: 10px;    /* 控件 */

  /* 兼容旧变量 */
  --radius-xl: 20px;
  --radius-lg: 16px;
  --radius-md: 12px;
  --radius-sm: 8px;

  /* 栅格与间距（文档规范：容器 24 / 模块 32-40 / 卡片 16 / 行 12-16）*/
  --sp-container: 24px;      /* 容器内边距 */
  --sp-module: 32px;         /* 模块间距（32-40）*/
  --sp-module-lg: 40px;      /* 模块间距（大）*/
  --sp-card: 16px;           /* 卡片间距 */
  --sp-row: 12px;            /* 行间距（12-16）*/
  --sp-row-lg: 16px;         /* 行间距（大）*/

  /* 兼容旧变量（8pt 系统）*/
  --sp-4: 4px; --sp-8: 8px; --sp-12: 12px; --sp-16: 16px; --sp-20: 20px;
  --sp-24: 24px; --sp-32: 32px; --sp-40: 40px;

  /* 字体规范（文档规范：H1 22-24/600, H2 18/600, 正文 14-16/400, 说明 12/400）*/
  --fz-h1: 24px;      /* H1: 22-24 / 600 */
  --fz-h2: 18px;      /* H2: 18 / 600 */
  --fz-body: 16px;    /* 正文: 14-16 / 400 */
  --fz-body-sm: 14px; /* 正文（小）*/
  --fz-caption: 12px; /* 说明: 12 / 400 */

  /* 兼容旧变量 */
  --fz-12: 12px;  /* 副信息 */
  --fz-14: 14px;  /* 正文 */
  --fz-16: 16px;  /* 主标题 */
  --fz-18: 18px;  /* 大标题 */
  --fz-20: 20px;  /* 特大标题 */
  --fz-24: 24px;

  /* 动效（文档规范：入场 240ms ease-out / Hover 150ms / 切换 200ms）*/
  --transition-enter: 240ms cubic-bezier(0.2, 0.8, 0.2, 1);  /* 入场 */
  --transition-hover: 150ms cubic-bezier(0.2, 0.8, 0.2, 1);  /* Hover */
  --transition-switch: 200ms cubic-bezier(0.2, 0.8, 0.2, 1); /* 切换 */

  /* 可访问性（文档规范：焦点环 2px 主色 35% 透明）*/
  --focus-ring: 2px solid rgba(37, 99, 235, 0.35);
}

/* ========== 全局字体与排版（方案 A）========== */
html, body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB',
               'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  color: var(--cl-text);
  background: var(--cl-bg);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 标题层级 */
.h1 { font-size: var(--fz-20); font-weight: 700; line-height: 1.3; }
.h2 { font-size: var(--fz-18); font-weight: 600; line-height: 1.4; }
.h3 { font-size: var(--fz-16); font-weight: 500; line-height: 1.5; }
.p  { font-size: var(--fz-14); line-height: 1.6; color: var(--cl-text-sub); }

/* 隐藏原生 tabBar，使用自定义 tabBar */
uni-tabbar {
  display: none !important;
}

/* 移除页面底部的 padding（tabBar 占位） */
uni-page-body {
  padding-bottom: 56px !important; /* 自定义 tabBar 高度 */
}

/* PC端隐藏自定义 tabBar（使用悬浮导航） */
@media (min-width: 751px) {
  .custom-tabbar {
    display: none !important;
  }

  uni-page-body {
    padding-bottom: 0 !important;
  }
}
</style>
