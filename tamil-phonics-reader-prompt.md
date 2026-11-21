# Prompt: Tamil Phonics & Read-Aloud Learning App (Android)

## Role

You are a senior Android engineer, early-literacy curriculum designer and game-UX designer. Design and build the app described below. Work iteratively: propose the architecture first, then build milestone by milestone, and ask before making any decision that is hard to reverse.

## 1. Product Vision

An Android app for phones and tablets that teaches children to read Tamil. It combines three ideas:

1. **Read-aloud with correction:** Google "Read Aloud"-style reading that highlights word by word. When a child mispronounces or fails a word, the app breaks it into letters and syllables and pronounces each part slowly.
2. **Jolly Phonics for Tamil:** A structured letter-sound curriculum, taught with actions, stories and songs.
3. **Duolingo-style gamification:** Short lessons, pictures, levels, streaks and rewards, with difficulty that adapts to progress.

**Target users:** Children aged 4-9 learning Tamil (native or heritage learners), plus a parent/teacher view. **Platforms:** Android phone and tablet (responsive layouts, portrait and landscape). **Language of UI:** Tamil with English helper text, switchable.

## 2. Core Features

### 2.1 Phonics Foundation (prerequisite module)

Replicate the Jolly Phonics method for Tamil. Children must complete the relevant letter-sound lessons before unlocking word practice.

- **Letter groups, introduced in small sets (4-6 letters at a time):**
  - உயிர் (Uyir - Vowels / Sounds): அ ஆ இ ஈ உ ஊ எ ஏ ஐ ஒ ஓ ஔ
  - மெய் (Mei - Consonants / Sounds): க் ங் ச் ஞ் ட் ண் த் ந் ப் ம் ய் ர் ல் வ் ழ் ள் ற் ன்
  - உயிர்மெய் (Uyirmei - Combined Vowel-Consonants): consonant + vowel signs (e.g. க, கா, கி, கீ ...)
  - ஆய்தம் (Aytham - Special Letter): ஃ
- **Jolly Phonics five skills, adapted:**
  1. Learning the letter sound (not the letter name)
  2. Learning letter formation (animated stroke order, trace with finger)
  3. Blending (joining sounds to read a word)
  4. Identifying sounds in words (listen and pick the starting sound)
  5. Tricky words (irregular or compound-letter words)
- **For each letter:** a story or character, an action, a short song or chant, a picture keyword, a tracing activity and a listen-and-tap game.
- **Suggested order:** Start with common, easy-to-distinguish sounds. Propose and justify the grouping, and let me review it before building.

### 2.2 Read-Aloud with Letter-by-Letter Correction

- Display a word or sentence and read it aloud with synchronized word highlighting.
- The child reads aloud. Use speech recognition to compare against the target word, with tolerance for child voices and accents.
- **If the child gets a word wrong:**
  1. Highlight the word and split it into syllables/letters (grapheme-aware, since Tamil letters can be multi-codepoint).
  2. Play each unit slowly, one at a time, highlighting each as it plays.
  3. Blend the units back together at normal speed.
  4. Ask the child to try again. After repeated misses, mark the word for review.
- Provide a tap-to-hear mode: tap any word to hear it, tap again to hear it split into letters/syllables.
- Adjustable speech speed. Speak with a clear, child-friendly voice.

### 2.3 Progressive Word Levels

Difficulty rises as the child completes practice. Example progression (refine as needed):

| Level | Content                                                 |
|-------|---------------------------------------------------------|
| 1     | Single-letter words (e.g. ஆ, ஈ, ஐ, கை) and single sounds |
| 2     | Two-letter words                                        |
| 3     | Three-letter words                                      |
| 4     | Four-plus letter words, simple compound letters         |
| 5     | Short phrases                                           |
| 6     | Short sentences                                         |
| 7     | Short stories with comprehension questions              |

- **Unlock rule:** A level unlocks after completing a set number of exercises with a minimum accuracy (for example 80%). Make thresholds configurable.
- **Adaptive engine:** Track mistakes per letter, syllable and word. Resurface weak items using spaced repetition. Ease off after repeated failures and step up after streaks of success.

### 2.4 Duolingo-Style Pictographic Learning

- Every sound and word is paired with a clear picture, so the child learns sound, word and meaning together.
- **Exercise types** (mix and rotate):
  - Listen and pick the correct picture
  - See the picture and pick the correct word
  - Match letters or words to pictures
  - Drag letters or syllables to build a word
  - Listen and tap the first letter/sound
  - Trace the letter
  - Say the word (speech recognition)
  - Fill the missing letter
- Show the meaning in English and Tamil, with an audio clip and an optional example sentence.
- Use a content structure that lets new words, images and audio be added without app updates where possible.

### 2.5 Gamification

- XP, stars (1-3 per lesson), levels and a learning path map
- Daily streaks and gentle reminders
- Hearts or retries (child-friendly: no harsh failure states)
- Collectible characters or stickers, an avatar and a reward shop (non-monetary)
- Celebratory animations and sounds, with a toggle for sound and haptics
- Daily goal setting by parent or child
- Multiple child profiles on one device

### 2.6 Parent / Teacher View

- Progress dashboard: letters mastered, weak sounds, time spent, streaks
- Set daily goals and manage profiles
- Optional PIN gate for settings
- Export progress report

## 3. Technical Requirements

- **Platform:** Android phones and tablets. Recommend Kotlin with Jetpack Compose, or justify an alternative such as Flutter. Use adaptive layouts (window size classes).
- **TTS:**
  - Default to Android `TextToSpeech` with the Tamil locale (`ta-IN`).
  - **Prefer pre-recorded or high-quality neural audio for individual letters and syllables.** System TTS pronounces isolated letters and short syllables poorly, so build an audio asset pipeline: a native-speaker recording or neural-TTS generation, a review step, then bundling or caching. Use live TTS for full words and sentences where quality is acceptable.
  - Allow a pluggable TTS provider (system, Azure, Google Cloud, Sarvam or similar), and recommend which to test.
- **Speech recognition:** Evaluate options for Tamil child speech (Android `SpeechRecognizer`, Google Cloud Speech-to-Text, Whisper-based or Indic models). Explain accuracy trade-offs and offer a fallback "I said it" self-check mode when recognition confidence is low.
- **Text handling:** Grapheme-aware segmentation for Tamil. Build a utility that splits a word into letters (உயிர் [Uyir - Vowels], மெய் [Mei - Consonants], உயிர்மெய் [Uyirmei - Combined Vowel-Consonants]) and syllables (அசை [Asai]) correctly. Provide unit tests with a word list.
- **Offline-first:** Core lessons and audio must work offline. Cloud features are optional.
- **Storage:** Room for progress, DataStore for settings, structured JSON or SQLite for the curriculum content.
- **Architecture:** MVVM or MVI, a clear module split (curriculum, audio, speech, gamification, UI), dependency injection, and a testable adaptive-learning engine.
- **Performance and safety:** Low-end device support, small installation size, no ads, no third-party trackers, minimal data collection. Follow COPPA-style and Google Play "Designed for Families" requirements.
- **Accessibility:** Large touch targets, high contrast, scalable text, and audio-first interaction for non-readers.

## 4. Content Requirements

- A complete Tamil letter inventory with the suggested teaching order and sound grouping.
- An initial word bank of 200-300 child-friendly words organized by length and difficulty, each with: Tamil text, transliteration, syllable split, English meaning, image keyword and audio reference.
- Image style guidance: simple, friendly, culturally relevant illustrations with consistent style (propose sourcing options and licensing).
- Audio style guidance: warm, slow, clear voice. Specify recording or generation requirements for letters, syllables, words and feedback phrases.
- Flag any pronunciation or linguistic points that need review by a native Tamil teacher.

## 5. Deliverables

Work in this order, and wait for my feedback after steps 1 and 2:

1. `[x]` **Architecture and tech-stack proposal** with trade-offs, including the TTS, speech recognition and audio-asset decisions.
2. `[x]` **Curriculum design:** letter groups, lesson order, level thresholds and exercise mix, plus the content data schema.
3. `[x]` **UX flows and wireframes** for phone and tablet: onboarding, learning path, lesson screens, read-aloud and correction screen, parent dashboard.
4. `[x]` **Tamil text-segmentation module** with unit tests.
5. **MVP implementation**, in these milestones:
   - `[x]` **M1:** Letter-sound lessons for all letter groups, with tracing and listen-and-tap games
   - `[x]` **M2:** Read-aloud with word highlighting and letter-by-letter / syllable correction
   - `[x]` **M3:** Progressive difficulty levels (1-7+ letter words), continuous speech+meaning test, XP, streaks, and stars
   - `[x]` **M4:** Pictographic exercises, expanded dictionary word bank, parent dashboard (`ParentDashboardScreen`) with PIN gate, heat map, and goal management
6. `[x]` **Testing plan:** unit tests (73 tests passing), UI structure, and grapheme validation.
7. `[ ]` **Release checklist** for Google Play (Families policy, privacy policy, content rating).

## 6. Constraints and Working Style

- Keep explanations concise and direct. Use tables and Markdown for structured outputs.
- Give your recommendation first, then the alternatives.
- State assumptions explicitly and list open questions at the end of each step.
- Do not invent linguistic facts. Mark anything that needs verification by a native Tamil teacher.

## 7. Open Questions (answer or propose defaults)

1. Target age range and whether the child already speaks Tamil at home
2. Fully offline, or is cloud TTS and speech recognition acceptable?
3. Free, ad-free, or paid? Any subscription plans?
4. Who will record or approve the audio and illustrations?
5. Tamil dialect or accent preference for the voice (e.g. Indian Tamil vs Sri Lankan Tamil)
