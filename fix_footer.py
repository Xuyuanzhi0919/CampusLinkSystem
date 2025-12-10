from pathlib import Path
import re
path = Path('frontend/src/pages/question/components/AnswerCard.vue')
text = path.read_text(encoding='utf-8')
new_footer = '''      <view class="footer-right">
        <!-- ?????????????????? -->
        <CButton
          v-if="canAccept && !answer.isAccepted"
          type="success"
          size="sm"
          plain
          class="accept-btn"
          @click="handleAccept"
        >
          <ClIcon name="icon-check" size="xs" class="action-icon" />
          <text class="action-label">??</text>
        </CButton>

        <!-- ?????????????? -->
        <CButton
          v-if="isMyAnswer"
          type="ghost"
          size="sm"
          class="ghost-danger"
          @click="handleDelete"
        >
          <ClIcon name="icon-trash-2" size="xs" class="action-icon" />
          <text class="action-label">??</text>
        </CButton>
      </view>'''
text = re.sub(r'<view class="footer-right">[\s\S]*?</view>', new_footer, text, count=1)
path.write_text(text, encoding='utf-8')
