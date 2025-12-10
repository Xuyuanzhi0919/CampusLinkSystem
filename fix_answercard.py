from pathlib import Path
import re

path = Path('frontend/src/pages/question/components/AnswerCard.vue')
text = path.read_text(encoding='utf-8')

# Normalize mistaken double quotes from previous replacements
text = text.replace('""', '"')

# More menu icon block
text = re.sub(
    r'<view class="more-actions" @click="showMoreMenu">[\s\S]*?</view>',
    '<view class="more-actions" @click="showMoreMenu">\n        <ClIcon name="icon-more" size="sm" class="more-icon" color="#9CA3AF" />\n      </view>',
    text,
    flags=re.S,
)

# Accept button block
text = re.sub(
    r'<CButton\s+v-if="canAccept[\s\S]*?handleAccept"[\s\S]*?</CButton>',
    '        <CButton\n          v-if="canAccept && !answer.isAccepted"\n          type="success"\n          size="sm"\n          plain\n          class="accept-btn"\n          @click="handleAccept"\n        >\n          <ClIcon name="icon-check" size="xs" class="action-icon" />\n          <text class="action-label">\u91c7\u7eb3</text>\n        </CButton>',
    text,
    flags=re.S,
)

# Delete button block
text = re.sub(
    r'<CButton\s+v-if="isMyAnswer"[\s\S]*?handleDelete"[\s\S]*?</CButton>',
    '        <CButton\n          v-if="isMyAnswer"\n          type="ghost"\n          size="sm"\n          class="ghost-danger"\n          @click="handleDelete"\n        >\n          <ClIcon name="icon-trash-2" size="xs" class="action-icon" />\n          <text class="action-label">\u5220\u9664</text>\n        </CButton>',
    text,
    flags=re.S,
)

# Best answer badge class
text = text.replace('<BestAnswerBadge />', '<BestAnswerBadge class="answer-badge" />')

# Accepted state styling
accepted_block = re.search(r'  &--accepted \{[\s\S]*?\n  \}', text)
if accepted_block:
    text = text.replace(
        accepted_block.group(0),
        '  &--accepted {\n    background-color: #FFFDF5 !important;\n    border-color: #FACC15 !important;\n    box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.06) !important;\n    position: relative;\n    overflow: hidden;\n\n    &::before {\n      content: "";\n      position: absolute;\n      top: 0;\n      left: 0;\n      right: 0;\n      height: 6rpx;\n      background: linear-gradient(90deg, #FACC15 0%, #FDE68A 100%);\n    }\n\n    .answer-content {\n      color: $gray-900;\n    }\n  }',
        1,
    )

# Accepted badge wrapper and badge spacing
text = text.replace(
    '.accepted-badge-wrapper {\n  margin-bottom: $sp-2;\n}',
    '.accepted-badge-wrapper {\n  display: inline-flex;\n  margin-bottom: $sp-3;\n}\n\n.answer-badge {\n  display: inline-flex;\n}',
)

# Action icon spacing + new button variants
text = text.replace(
    '.action-icon {\n  margin-right: $sp-1;\n}\n',
    '.action-icon {\n  margin-right: $sp-1;\n}\n\n.accept-btn {\n  color: #166534;\n  background: #ecfdf3;\n  border: 2rpx solid #86efac;\n  transition: background $duration-base, border-color $duration-base, color $duration-base;\n}\n\n.accept-btn:hover {\n  background: #d1fae5;\n  border-color: #4ade80;\n}\n\n.ghost-danger {\n  color: #b91c1c;\n  background: transparent;\n  border: 2rpx solid #fca5a5;\n  transition: background $duration-base, border-color $duration-base, color $duration-base;\n}\n\n.ghost-danger:hover {\n  background: rgba(#f87171, 0.08);\n  border-color: #f87171;\n}\n',
)

path.write_text(text, encoding='utf-8')
