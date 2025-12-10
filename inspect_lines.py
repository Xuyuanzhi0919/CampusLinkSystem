from pathlib import Path
text = Path('frontend/src/pages/question/components/AnswerCard.vue').read_text(encoding='utf-8')
block = [line for line in text.split('\n') if 'footer-right' in line][:3]
print(block)
