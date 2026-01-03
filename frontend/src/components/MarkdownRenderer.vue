<template>
  <view class="markdown-body" v-html="renderedHtml"></view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
// @ts-ignore
import hljs from 'highlight.js/lib/core'
// @ts-ignore
import javascript from 'highlight.js/lib/languages/javascript'
// @ts-ignore
import typescript from 'highlight.js/lib/languages/typescript'
// @ts-ignore
import python from 'highlight.js/lib/languages/python'
// @ts-ignore
import java from 'highlight.js/lib/languages/java'
// @ts-ignore
import cpp from 'highlight.js/lib/languages/cpp'
// @ts-ignore
import css from 'highlight.js/lib/languages/css'
// @ts-ignore
import xml from 'highlight.js/lib/languages/xml'
// @ts-ignore
import bash from 'highlight.js/lib/languages/bash'
// @ts-ignore
import sql from 'highlight.js/lib/languages/sql'
// @ts-ignore
import json from 'highlight.js/lib/languages/json'

// 注册常用语言
hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('python', python)
hljs.registerLanguage('java', java)
hljs.registerLanguage('cpp', cpp)
hljs.registerLanguage('c++', cpp)
hljs.registerLanguage('css', css)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('shell', bash)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('json', json)

const props = defineProps<{
  content: string
}>()

// 配置 marked
marked.setOptions({
  gfm: true,
  breaks: true,
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (e) {
        console.error('代码高亮失败:', e)
      }
    }
    return code
  }
})

const renderedHtml = computed(() => {
  if (!props.content) return ''

  try {
    return marked(props.content)
  } catch (e) {
    console.error('Markdown 渲染失败:', e)
    return props.content
  }
})
</script>

<style lang="scss">
@import '@/styles/variables.scss';

// Markdown 样式
.markdown-body {
  font-size: 15px;
  line-height: 1.75;
  color: $gray-900;
  word-wrap: break-word;

  // 标题
  h1, h2, h3, h4, h5, h6 {
    margin-top: 20px;
    margin-bottom: 12px;
    font-weight: 600;
    line-height: 1.3;
    color: $gray-900;
  }

  h1 {
    font-size: 24px;
    padding-bottom: 8px;
    border-bottom: 2px solid $gray-200;
  }

  h2 {
    font-size: 20px;
    padding-bottom: 6px;
    border-bottom: 1px solid $gray-200;
  }

  h3 {
    font-size: 18px;
  }

  h4 {
    font-size: 16px;
  }

  h5, h6 {
    font-size: 15px;
  }

  // 段落
  p {
    margin-top: 0;
    margin-bottom: 10px;
  }

  // 强调
  strong, b {
    font-weight: 600;
    color: $gray-900;
  }

  em, i {
    font-style: italic;
  }

  // 链接
  a {
    color: $primary;
    text-decoration: none;
    border-bottom: 1px solid transparent;
    transition: all 0.2s;

    &:hover {
      border-bottom-color: $primary;
    }
  }

  // 列表
  ul, ol {
    margin-top: 0;
    margin-bottom: 10px;
    padding-left: 24px;
  }

  li {
    margin-bottom: 4px;
    line-height: 1.6;

    &::marker {
      color: $primary;
    }
  }

  ul li {
    list-style-type: disc;
  }

  // 嵌套列表
  ul ul,
  ol ul {
    list-style-type: circle;
  }

  ul ul ul,
  ol ul ul,
  ul ol ul,
  ol ol ul {
    list-style-type: square;
  }

  // 代码块
  code {
    padding: 2px 6px;
    background: rgba($primary, 0.08);
    border-radius: 4px;
    font-size: 14px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    color: $primary;
  }

  pre {
    margin-top: 0;
    margin-bottom: 12px;
    padding: 14px 16px;
    background: $gray-900;
    border-radius: 8px;
    overflow-x: auto;
    box-shadow: 0 2px 8px rgba($black, 0.1);

    code {
      padding: 0;
      background: transparent;
      border-radius: 0;
      font-size: 13px;
      color: #e6edf3;
      line-height: 1.6;
      display: block;
    }
  }

  // 引用
  blockquote {
    margin: 12px 0;
    padding: 8px 16px;
    border-left: 4px solid $primary;
    background: $primary-50;
    color: $gray-700;
    border-radius: 0 4px 4px 0;

    p:last-child {
      margin-bottom: 0;
    }
  }

  // 分隔线
  hr {
    margin: 20px 0;
    border: none;
    border-top: 2px solid $gray-200;
  }

  // 表格
  table {
    width: 100%;
    margin-bottom: 12px;
    border-collapse: collapse;
    border-spacing: 0;
    font-size: 14px;

    thead {
      background: $gray-50;
    }

    th, td {
      padding: 10px 12px;
      border: 1px solid $gray-200;
      text-align: left;
    }

    th {
      font-weight: 600;
      color: $gray-900;
    }

    tr:hover {
      background: rgba($primary, 0.04);
    }
  }

  // 图片
  img {
    max-width: 100%;
    height: auto;
    border-radius: 6px;
    margin: 8px 0;
  }

  // 任务列表
  input[type="checkbox"] {
    margin-right: 6px;
  }

  // 水平滚动优化
  pre,
  code,
  table {
    -webkit-overflow-scrolling: touch;
  }
}

// Highlight.js 代码高亮主题（GitHub Dark）
.markdown-body {
  pre code {
    .hljs-comment,
    .hljs-quote {
      color: #8b949e;
      font-style: italic;
    }

    .hljs-keyword,
    .hljs-selector-tag,
    .hljs-subst {
      color: #ff7b72;
    }

    .hljs-number,
    .hljs-literal,
    .hljs-variable,
    .hljs-template-variable,
    .hljs-tag .hljs-attr {
      color: #79c0ff;
    }

    .hljs-string,
    .hljs-doctag {
      color: #a5d6ff;
    }

    .hljs-title,
    .hljs-section,
    .hljs-selector-id {
      color: #d2a8ff;
      font-weight: 600;
    }

    .hljs-type,
    .hljs-class .hljs-title {
      color: #ffa657;
    }

    .hljs-tag,
    .hljs-name,
    .hljs-attribute {
      color: #7ee787;
    }

    .hljs-regexp,
    .hljs-link {
      color: #a5d6ff;
    }

    .hljs-symbol,
    .hljs-bullet {
      color: #f0883e;
    }

    .hljs-built_in,
    .hljs-builtin-name {
      color: #ffa657;
    }

    .hljs-meta {
      color: #8b949e;
    }

    .hljs-deletion {
      background: #490202;
    }

    .hljs-addition {
      background: #0e4429;
    }

    .hljs-emphasis {
      font-style: italic;
    }

    .hljs-strong {
      font-weight: 600;
    }
  }
}
</style>
