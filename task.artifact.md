# Phonics-ta-IN Project Tasks & Roadmap

## Master Feature & Task Progress

### 📖 Curriculum & Phonics Core
- `[x]` Vowels (`உயிர் எழுத்துகள்`) 12 letters + Aytham (`ஃ`) with flashcards & audio
- `[x]` Consonants (`மெய் எழுத்துகள்`) 18 letters x 13 Uyirmei combinations
- `[x]` Interactive 5-Skill Lesson Sequence (Listen, Read, Trace, Speech Match)
- `[x]` Letter stroke order handwriting canvas (`TracingCanvas`)
- `[x]` On-device acoustic sound matcher (`AcousticSoundMatcher`)

### 🎙️ Read-Aloud & Progressive Assistance Engine
- `[x]` Always-on microphone streaming (`StreamingSpeechListener`)
- `[x]` Real-time synchronized word highlighting & paragraph reading layout
- `[x]` Silent / beep-free system chime suppression
- `[x]` User-prompted assistance pop-up (grapheme split -> syllable split -> full audio)
- `[x]` Pre-reading distinct letter review grid & 1-by-1 speech test
- `[x]` Grade 1–3 Stories library (Thirsty Crow, Hare & Tortoise, Lion & Mouse, etc.)
- `[x]` Custom text reader with state persistence

### 🎯 Progressive Levels & Dictionary Quiz
- `[x]` Tamil Dictionary Repository categorized by grapheme length (Levels 1 to 7+)
- `[x]` Dynamic continuous random test set generator (`generateRandomTestSet`)
- `[x]` Single activity screen (`TestYourKnowledgeScreen`)
- `[x]` Simultaneous Speech Pronunciation & Meaning Comprehension testing
- `[x]` Star counter, progress tracking, and instant feedback

### 🛡️ Parent / Teacher Area & Live Analytics Dashboard
- `[x]` PIN protection removed for instant dashboard access
- `[x]` Real-time progress metrics connected to app state (letters mastered `X / 247`, accuracy, time learned today)
- `[x]` Synchronized daily goal timer (updating main screen goals in real-time)
- `[x]` Daily learning time weekly bar chart (Mon to Sun)
- `[x]` Priority weak sounds list with direct `Practice Now 🎯` lesson navigation
- `[x]` 247-Letter Mastery Heat Map Grid with status badges and filters
- `[x]` Progress report export & sharing (`Intent.ACTION_SEND`)

### 🎮 Gamification & Customization
- `[x]` XP counter, daily streak tracker, and stars earned
- `[ ]` Sticker album & non-monetary collectible rewards shop
- `[ ]` Character avatar selector & child onboarding setup

### ⚙️ Architecture & Adaptive Engine
- `[x]` Grapheme-aware Tamil text segmenter (`TamilTextSegmenter`) with 73 passing unit tests
- `[ ]` Room database persistent weak-sound tracking & spaced repetition engine
- `[ ]` Tablet window size class adaptive multi-pane layout
