from pathlib import Path
text = Path('frontend/src/pages/question/components/AnswerCard.vue').read_text(encoding='utf-8')
idx = text.find('.action-icon')
print(text[idx:idx+400].encode('unicode_escape'))
