# 📱 Phonics-ta-IN (தமிழ் ஒலியியல் & வாசிப்புப் பயிற்சி)

**Phonics-ta-IN** is an advanced, interactive Android mobile application built with **Jetpack Compose** and **Kotlin**, designed specifically to help children and learners master Tamil phonics, letter sounds, vocabulary pronunciation, and continuous reading fluency (Grades 1–3).

---

## 🌟 Key Feature Set

### 1. 📖 Comprehensive Tamil Phonics Curriculum (247 Letters)
* **Vowels (`உயிர் எழுத்துகள்`)**: 12 pure vowels + ஃ (Aytham), each with interactive flashcards and audio pronunciation.
* **Consonants (`மெய் எழுத்துகள்`)**: 18 consonant families, expanding into full combined consonant-vowel forms (`உயிர்மெய்`).
* **Multi-Modal Interactive Lessons**: Every single letter in the Tamil alphabet includes a dedicated lesson containing:
  1. **Precursor Sound**: Listen to isolated reference sound.
  2. **Listening Test**: Hear sound -> pick the correct letter.
  3. **Reading Test**: See letter -> identify the correct sound.
  4. **Tracing Canvas**: Practice handwriting and stroke order.
  5. **Speech Test**: On-device acoustic speech recognition matching for isolated letter pronunciation.

### 2. 🎙️ Continuous Hands-Free Read-Along Engine
* **Always-On Microphone Streaming**: Powered by `StreamingSpeechListener`, the microphone stays open continuously while reading. Users never have to tap the microphone repeatedly between pauses.
* **Real-Time Word Tracking**: Words automatically turn **green** (with celebratory confetti burst animations) when read correctly.
* **Silent & Beep-Free Recognition**: Suppresses system speech recognition chime tones for a quiet, distraction-free learning environment.
* **Paragraph Reading Format**: Displays multiple sentences (3+ sentences per view) formatted naturally like a book paragraph with compact word spacing and optimized typography (`18.sp`).

### 3. 🎯 User-Prompted Progressive Assistance Pop-up
When a word is mispronounced (highlighted in yellow):
* **1st Attempt**: Tapping the yellow word opens an assistance pop-up breaking the word down into **individual letters (graphemes)**. Tapping any letter plays its isolated sound.
* **2nd Attempt**: Breaks down the word into **syllables (அசை)** for syllable-by-syllable listening.
* **3rd+ Attempt**: Plays the **full word**.
* **Direct Practice Jump ("Practice 🎯")**: Tapping practice on any letter navigates directly to that letter's curriculum lesson and seamlessly restores reading progress upon return.

### 4. 🔲 Pre-Reading Distinct Letter Review & Grid Activity
* Automatically extracts all distinct Tamil letters from custom text or stories before reading.
* **Guided Speech Test (1-by-1)**: Step-by-step isolated sound speech testing for each unique letter in the text with a "Skip" option.
* **Distinct Letters Grid Activity**: A touch-interactive grid where learners can tap any letter to hear its sound or test pronunciation.

### 5. 📚 Grade 1–3 Full-Length Stories Library
* A curated library of classic fables and moral stories suitable for Grades 1 to 3:
  * 🐦 *காகமும் தண்ணீரும் (The Thirsty Crow)* — Grade 1
  * 🐢 *முயலும் ஆமையும் (The Hare and the Tortoise)* — Grade 1
  * 🦁 *சிங்கமும் சுண்டெலியும் (The Lion and the Mouse)* — Grade 2
  * 🦊 *நரியும் திராட்சைப் பழமும் (The Fox and the Grapes)* — Grade 2
  * 🧀 *அறிவாளி காகமும் தந்திர நரியும் (The Clever Crow & Sly Fox)* — Grade 3
* Organized via an **Icon Grid** with grade filter chips (`All`, `Grade 1`, `Grade 2`, `Grade 3`).

### 6. 📝 Custom Text Reader
* Paste or type any Tamil text (story, poem, or homework) into the app and practise reading it aloud with the exact same hands-free tracking and progressive assistance engine.
* State persistence ensures pasted text and reading progress are never lost when jumping into practice lessons.

---

## 🛠️ Tech Stack & Architecture

* **UI Framework**: Jetpack Compose, Material 3, Material Design Icons.
* **Architecture**: Modern Android MVVM / unidirectional data flow, clean separation of core speech engines and feature screens.
* **Speech & Audio**: Android `SpeechRecognizer` streaming pipeline with acoustic sound matching (`AcousticSoundMatcher`), Android `TextToSpeech` (Tamil `ta-IN`), and `AudioManager` stream volume management.
* **Text Processing**: Custom Unicode grapheme clustering (`TamilTextSegmenter`), syllable segmentation (`TamilSyllableSegmenter`), and transliteration (`TamilTransliteration`).

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Phonics-ta-IN.git
   ```
2. Open the project in **Android Studio** (Hedgehog or newer).
3. Sync Gradle and run the app on an Android device or emulator (API 33+ recommended with microphone permission enabled).
