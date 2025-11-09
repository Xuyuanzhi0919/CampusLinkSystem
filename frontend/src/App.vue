<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
onLaunch(() => {
  console.log("App Launch");
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
