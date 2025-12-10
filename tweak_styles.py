from pathlib import Path
import re
path = Path('frontend/src/pages/question/components/AnswerCard.vue')
text = path.read_text(encoding='utf-8')
new_block = '''.action-icon {
  margin-right: $sp-1;
  font-size: $font-size-base;
}

.accept-btn {
  color: #166534;
  background: #ecfdf3;
  border: 2rpx solid #86efac;
  transition: background $duration-base, border-color $duration-base, color $duration-base;
}

.accept-btn:hover {
  background: #d1fae5;
  border-color: #4ade80;
}

.ghost-danger {
  color: #b91c1c;
  background: transparent;
  border: 2rpx solid #fca5a5;
  transition: background $duration-base, border-color $duration-base, color $duration-base;
}

.ghost-danger:hover {
  background: rgba(#f87171, 0.08);
  border-color: #f87171;
}

.action-label {
  font-size: $font-size-sm;
}

'''
text = re.sub(r'\.action-icon \{[\s\S]*?\}\s*\n\n// \u5220\u9664\u6309\u94ae\u7279\u6b8a\u6837\u5f0f[\s\S]*?\.btn-delete \{[\s\S]*?\}\s*\n\n', new_block, text)
path.write_text(text, encoding='utf-8')
