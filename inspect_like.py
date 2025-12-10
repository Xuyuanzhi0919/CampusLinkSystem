from pathlib import Path
text = Path('frontend/src/pages/question/components/AnswerCard.vue').read_text(encoding='utf-8')
for line in text.split('\n'):
    if 'action-icon' in line and 'answer.isLiked' in line:
        print(line.encode('unicode_escape'))
