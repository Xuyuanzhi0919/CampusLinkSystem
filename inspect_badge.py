from pathlib import Path
text = Path('frontend/src/pages/question/components/BestAnswerBadge.vue').read_text(encoding='utf-8')
print(text.encode('unicode_escape'))
