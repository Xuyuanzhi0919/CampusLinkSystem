<template>
  <view class="gp-skeleton" :style="variableStr">
    <!-- 骨架屏 -->
    <view
      v-if="skeletonLoading"
      class="gp-skeleton__wrapper"
      :class="[startFadeOut ? 'fade-out' : '']"
      :style="{ padding: skeletonConfigs.padding }"
    >
      <view
        v-for="(row, rowIndex) in gridRowsArr"
        :key="rowIndex"
        class="gp-skeleton__wrapper__rows"
        :style="{
          marginBottom:
            rowIndex < gridRowsArr.length - 1 ? skeletonConfigs.gridRowsGap : 0,
          gap: skeletonConfigs.gridRowsGap,
        }"
      >
        <view
          v-for="(column, columnIndex) in gridColumnsArr"
          :key="columnIndex"
          class="gp-skeleton__wrapper__columns"
          :style="{
            flexDirection: skeletonConfigs.itemDirection,
            alignItems: skeletonConfigs.itemAlign,
          }"
        >
          <view
            v-if="skeletonConfigs.headShow"
            class="gp-skeleton__wrapper__head"
            :class="[animate ? 'animate' : '']"
            :style="{
              width: skeletonConfigs.headWidth,
              height: skeletonConfigs.headHeight,
              borderRadius: skeletonConfigs.headBorderRadius,
              marginRight:
                skeletonConfigs.itemDirection === 'row' &&
                skeletonConfigs.textShow
                  ? skeletonConfigs.itemGap
                  : 0,
              marginBottom:
                skeletonConfigs.itemDirection === 'column' &&
                skeletonConfigs.textShow
                  ? skeletonConfigs.itemGap
                  : 0,
            }"
          ></view>
          <view
            v-if="skeletonConfigs.textShow"
            class="gp-skeleton__wrapper__text"
          >
            <view
              v-for="(text, textIndex) in textRowsArr"
              :key="textIndex"
              class="gp-skeleton__wrapper__text__row"
              :class="[animate ? 'animate' : '']"
              :style="{
                width: text.width,
                height: text.height,
                borderRadius: skeletonConfigs.textBorderRadius,
                marginBottom:
                  textIndex < textRowsArr.length - 1
                    ? skeletonConfigs.textRowsGap
                    : 0,
              }"
            ></view>
          </view>
        </view>
      </view>
    </view>
    <!-- 插槽 -->
    <view v-else>
      <slot></slot>
    </view>
  </view>
</template>

<script lang="ts">
import {
  defineComponent,
  ref,
  computed,
  watch,
  onMounted,
  PropType,
} from 'vue';
import { createSkeletonConfigs } from './gp-skeleton-configs';
import type { SkeletonConfigsInterface } from './gp-skeleton-configs';

interface TextRowItem {
  width: string;
  height: string;
}

export default defineComponent({
  name: 'gp-skeleton',
  props: {
    // 骨架屏类型
    type: {
      type: String,
      default: '',
    },
    // 是否展示骨架组件
    loading: {
      type: Boolean,
      default: true,
    },
    // 是否开启动画效果
    animate: {
      type: Boolean,
      default: true,
    },
    // 动画效果持续时间，单位秒
    animateTime: {
      type: [Number, String],
      default: 1.8,
    },
    // 是否开启淡出动画
    fadeOut: {
      type: Boolean,
      default: true,
    },
    // 淡出效果持续时间，单位秒
    fadeOutTime: {
      type: [Number, String],
      default: 0.5,
    },
    // 骨架的背景色
    bgColor: {
      type: String,
      default: '#EAEDF5',
    },
    // 骨架的动画高亮背景色
    highlightBgColor: {
      type: String,
      default: '#F9FAFF',
    },
    // 自定义配置
    configs: {
      type: Object as PropType<SkeletonConfigsInterface>,
      default: () => ({}),
    },
  },

  setup(props) {
    // 组件内部状态
    const skeletonLoading = ref(props.loading);
    const startFadeOut = ref(false);
    const skeletonConfigs = ref<SkeletonConfigsInterface>(props.configs || {});

    // 获取配置函数
    const configsFunctions = createSkeletonConfigs();

    // 动态计算行列数组
    const gridRowsArr = computed(() => {
      const count = Number(skeletonConfigs.value?.gridRows || 0);
      const arr = [];
      for (let i = 0; i < count; i++) {
        arr.push(i);
      }
      return arr;
    });

    const gridColumnsArr = computed(() => {
      const count = Number(skeletonConfigs.value?.gridColumns || 0);
      const arr = [];
      for (let i = 0; i < count; i++) {
        arr.push(i);
      }
      return arr;
    });

    // 计算文本行数组
    const textRowsArr = computed(() => {
      if (!skeletonConfigs.value?.textShow) return [];

      // 检查高度不支持百分比
      if (
        typeof skeletonConfigs.value.textHeight === 'string' &&
        /%$/.test(skeletonConfigs.value.textHeight)
      ) {
        console.error('gp-skeleton: textHeight参数不支持百分比单位');
      }

      const rows: TextRowItem[] = [];
      if (!skeletonConfigs.value.textRows) return rows;

      for (let i = 0; i < skeletonConfigs.value.textRows; i++) {
        const { gridRows, textWidth, textHeight } = skeletonConfigs.value;
        let item: TextRowItem = { width: '', height: '' };

        // 处理文本宽度
        if (Array.isArray(textWidth)) {
          item.width =
            textWidth[i] || (i === (gridRows || 0) - 1 ? '70%' : '100%');
        } else {
          item.width = i === (gridRows || 0) - 1 ? '70%' : textWidth || '100%';
        }

        // 处理文本高度
        if (Array.isArray(textHeight)) {
          item.height = textHeight[i] || '30rpx';
        } else {
          item.height = textHeight || '30rpx';
        }

        // 调整宽度单位
        if (typeof item.width === 'string' && !/%$/.test(item.width)) {
          item.width = addUnit(item.width);
        }

        // 调整高度单位
        item.height = addUnit(item.height);

        rows.push(item);
      }

      return rows;
    });

    // CSS变量计算
    const variableStr = computed(() => {
      const keys = [
        'animateTime',
        'fadeOutTime',
        'bgColor',
        'highlightBgColor',
      ];
      const variableArr = keys.map(item => {
        if (item.indexOf('Time') > -1) {
          return `--${item}:${props[item as keyof typeof props]}s`;
        } else {
          return `--${item}:${props[item as keyof typeof props]}`;
        }
      });

      return variableArr.join(';');
    });

    // 监听loading状态
    watch(
      () => props.loading,
      value => {
        if (value) {
          skeletonLoading.value = true;
        } else {
          if (props.fadeOut) {
            startFadeOut.value = true;
            setTimeout(() => {
              skeletonLoading.value = false;
              startFadeOut.value = false;
            }, Number(props.fadeOutTime) * 1000);
          } else {
            skeletonLoading.value = false;
          }
        }
      },
      { immediate: true }
    );

    // 监听类型
    watch(
      () => props.type,
      value => {
        if (value === 'banner') {
          skeletonConfigs.value = configsFunctions.bannerConfigs(props.configs);
        } else if (value === 'info') {
          skeletonConfigs.value = configsFunctions.infoConfigs(props.configs);
        } else if (value === 'text') {
          skeletonConfigs.value = configsFunctions.textConfigs(props.configs);
        } else if (value === 'menu') {
          skeletonConfigs.value = configsFunctions.menuConfigs(props.configs);
        } else if (value === 'list') {
          skeletonConfigs.value = configsFunctions.listConfigs(props.configs);
        } else if (value === 'waterfall') {
          skeletonConfigs.value = configsFunctions.waterfallConfigs(
            props.configs
          );
        } else {
          skeletonConfigs.value = props.configs || {};
        }
      },
      { immediate: true }
    );

    // 工具函数
    /**
     * 添加单位，如果有rpx，upx，%，px等单位结尾或者值为auto，直接返回，否则加上px单位结尾
     * @param value 需要添加单位的值
     * @param unit 添加的单位名 比如px
     */
    function addUnit(value: string | number = 'auto', unit = 'px'): string {
      value = String(value);
      return /^[\+-]?(\d+\.?\d*|\.\d+|\d\.\d+e\+\d+)$/.test(value)
        ? `${value}${unit}`
        : value;
    }

    // 组件挂载
    onMounted(() => {
      // 可以在这里添加需要执行的逻辑
    });

    return {
      skeletonLoading,
      startFadeOut,
      skeletonConfigs,
      gridRowsArr,
      gridColumnsArr,
      textRowsArr,
      variableStr,
    };
  },
});
</script>

<style lang="scss" scoped>
@mixin background {
  background: linear-gradient(
    90deg,
    var(--bgColor) 25%,
    var(--highlightBgColor) 37%,
    var(--bgColor) 50%
  );
  background-size: 400% 100%;
}

.gp-skeleton {
  width: 100%;
  box-sizing: border-box;

  &__wrapper {
    display: flex;
    flex-direction: column;

    &__rows {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    &__columns {
      display: flex;
      align-items: center;
      flex: 1;
    }

    &__head {
      width: 100%;
      @include background;
    }

    &__text {
      flex: 1;
      width: 100%;
      &__row {
        @include background;
      }
    }
  }

  .fade-out {
    opacity: 0;
    animation: fadeOutAnim var(--fadeOutTime);
  }

  @keyframes fadeOutAnim {
    from {
      opacity: 1;
    }
    to {
      opacity: 0;
    }
  }

  .animate {
    animation: skeletonAnim var(--animateTime) ease infinite;
  }

  @keyframes skeletonAnim {
    0% {
      background-position: 100% 50%;
    }

    100% {
      background-position: 0 50%;
    }
  }
}
</style>
