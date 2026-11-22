# 📱 Phonics-ta-IN (தமிழ் ஒலியியல் & வாசிப்புப் பயிற்சி)

**Phonics-ta-IN** is an advanced, interactive Android mobile application built with **Jetpack Compose** and **Kotlin**, designed specifically to help children and learners master Tamil phonics, letter sounds, vocabulary pronunciation, and continuous reading fluency (Grades 1–3).

---

## 🌟 Key Feature Set

### 1. 📖 Comprehensive Tamil Phonics Curriculum (247 Letters)
* **Vowels / Sounds (`உயிர் எழுத்துகள் [Uyir Ezhuthugal]`)**: 12 pure vowels + ஃ (`ஆய்தம் [Aytham]`), each with interactive flashcards and audio pronunciation.
* **Consonants / Sounds (`மெய் எழுத்துகள் [Mei Ezhuthugal]`)**: 18 consonant families, expanding into full combined consonant-vowel forms (`உயிர்மெய் [Uyirmei]`).
* **Multi-Modal Interactive Lessons**: Every single letter in the Tamil alphabet includes a dedicated lesson containing:
  1. **Precursor Sound**: Listen to isolated reference sound.
  2. **Listening Test**: Hear sound -> pick the correct letter.
  3. **Reading Test**: See letter -> identify the correct sound.
  4. **Tracing Canvas**: Practice handwriting and stroke order.
  5. **Speech Test**: On-device acoustic speech recognition matching for isolated letter pronunciation.

### 2. 👧 Child Profiles
* **Multiple children, separate progress**: each profile (name + avatar) has its own stars, streak, completion ticks, study time and daily goal.
* **Profile selection on launch**: with 2+ profiles the app opens on a "Who is learning?" page showing each child's stars and streak. One profile opens directly; with none, the first-run screen invites the parent to create one.
* **Created and managed in the Parent section**: add, edit (name / avatar), delete (with confirmation) and switch profiles from the Parent dashboard, or via **Switch profile** in the menu.
* **Background music on the picker**: the same looping track as the main page, carried over smoothly when a profile is chosen. The parent's music on/off setting is device-wide.
* **No dummy data**: the old hardcoded sample profile has been removed. Progress saved before profiles existed is copied into the first profile created (one time).

### 3. 🎙️ Continuous Hands-Free Read-Along Engine
* **Always-On Microphone Streaming**: Powered by `StreamingSpeechListener`, the microphone stays open continuously while reading. Users never have to tap the microphone repeatedly between pauses.
* **Real-Time Word Tracking**: Words automatically turn **green** (with celebratory confetti burst animations) when read correctly.
* **Silent & Beep-Free Recognition**: Suppresses system speech recognition chime tones for a quiet, distraction-free learning environment.
* **Paragraph Reading Format**: Displays multiple sentences (3+ sentences per view) formatted naturally like a book paragraph with compact word spacing and optimized typography (`18.sp`).

### 4. 🎯 User-Prompted Progressive Assistance Pop-up
When a word is mispronounced (highlighted in yellow):
* **1st Attempt**: Tapping the yellow word opens an assistance pop-up breaking the word down into **individual letters (graphemes)**. Tapping any letter plays its isolated sound.
* **2nd Attempt**: Breaks down the word into **syllables (அசை)** for syllable-by-syllable listening.
* **3rd+ Attempt**: Plays the **full word**.
* **Direct Practice Jump ("Practice 🎯")**: Tapping practice on any letter navigates directly to that letter's curriculum lesson and seamlessly restores reading progress upon return.

### 5. 🔲 Pre-Reading Distinct Letter Review & Grid Activity
* Automatically extracts all distinct Tamil letters from custom text or stories before reading.
* **Guided Speech Test (1-by-1)**: Step-by-step isolated sound speech testing for each unique letter in the text with a "Skip" option.
* **Distinct Letters Grid Activity**: A touch-interactive grid where learners can tap any letter to hear its sound or test pronunciation.

### 6. 📚 Grade 1–3 Full-Length Stories Library
* A curated library of classic fables and moral stories suitable for Grades 1 to 3:
  * 🐦 *காகமும் தண்ணீரும் (The Thirsty Crow)* — Grade 1
  * 🐢 *முயலும் ஆமையும் (The Hare and the Tortoise)* — Grade 1
  * 🦁 *சிங்கமும் சுண்டெலியும் (The Lion and the Mouse)* — Grade 2
  * 🦊 *நரியும் திராட்சைப் பழமும் (The Fox and the Grapes)* — Grade 2
  * 🧀 *அறிவாளி காகமும் தந்திர நரியும் (The Clever Crow & Sly Fox)* — Grade 3
* Organized via an **Icon Grid** with grade filter chips (`All`, `Grade 1`, `Grade 2`, `Grade 3`).

### 7. 📝 Custom Text Reader
* Paste or type any Tamil text (story, poem, or homework) into the app and practise reading it aloud with the exact same hands-free tracking and progressive assistance engine.
* State persistence ensures pasted text and reading progress are never lost when jumping into practice lessons.

### 8. 🎯 Test Your Knowledge (Continuous Random Word Quiz)
* **Word Difficulty Progression (Levels 1–7+)**: Dynamically draws vocabulary words from the Tamil dictionary categorized by grapheme length (Level 1: 1-letter words to Level 7+: 7+ letter words).
* **Simultaneous Dual Testing**: Combines on-device speech pronunciation recognition (`SpeechRecognizerEngine`) and multiple-choice meaning comprehension on the same active card view.
* **Fresh Test Generation**: Generates a new random sequence of words across all levels every time a test session starts or restarts.

### 9. 📊 Parent / Teacher Dashboard
* Live metrics for the **active profile**: letters completed (`X / 247`), minutes learned today vs. daily goal, streak, weekly Mon–Sun time chart.
* Priority weak-sounds list with direct "Practice 🎯" jumps, and a 247-letter heat map with filters.
* Daily goal settings, background music switch, profile management, and a shareable progress report.
* No PIN or parental gate, by design: the Parent area opens directly. Deleting a profile asks for confirmation.

---

## 🧭 Principles (settled decisions)

* **100% offline, by user choice**: every feature must work without a network. Any online extra (cloud speech, downloadable packs, backup) is optional and used only if the user turns it on.
* **Two ways to learn, both valid**: a locked, Duolingo-style learning path (planned) and free jumping to any letter or activity (built). Neither replaces the other.
* **No parent PIN and no parental gate.**
* **No ads, trackers, analytics or crash reporting.**

---

## 🚨 Before Play Store Upload (Priority)

The full checklist lives in [`task.artifact.md`](task.artifact.md). Headlines:

* **Identity & build**: replace `com.example` application id, set up release signing (no debug key), build an AAB, review `minSdk` (currently 33), test the R8 release build on a real device.
* **Store assets**: real launcher icon, store name (Tamil + English), icon / feature graphic / screenshots, listing copy.
* **Policy & legal**: hosted privacy policy, Data safety form, Families / children's audience declaration, resolve the CC BY-SA background-music licence, complete developer account verification.
* **First run & errors**: microphone permission flow, offline Tamil voice / speech-pack check, graceful error states.
* **QA gate**: low-end device and tablet testing, update-from-older-build test, full unit test run, internal / closed testing track first.

---

## 📋 Implementation Progress & TODO Roadmap

### Completed Features (`[x]`)
- `[x]` **Tamil Phonics Curriculum**: All 247 letters (உயிர் [Uyir - Vowels], மெய் [Mei - Consonants], உயிர்மெய் [Uyirmei - Combined Forms], ஆய்தம் [Aytham - Special Letter]) with flashcards, audio, tracing, and sound matching.
- `[x]` **Grapheme & Syllable Engine**: Unicode Tamil grapheme segmentation (`TamilTextSegmenter`), syllable splitting (`TamilSyllableSegmenter`), and transliteration (`TamilTransliteration`).
- `[x]` **Hands-Free Read-Along Engine**: Always-on mic streaming, real-time word highlighting, paragraph view, and zero chime interference.
- `[x]` **Progressive Word Assistance**: Multi-level tap assistance (grapheme split, syllable split, whole word audio) and direct lesson practice jumps.
- `[x]` **Pre-Reading Review Grid**: Distinct letter extraction, step-by-step sound test, and interactive touch grid.
- `[x]` **Stories & Custom Reader**: Grade 1–3 story library and custom text reading with state persistence.
- `[x]` **Test Your Knowledge**: Continuous random word quiz across levels 1 to 7+ with simultaneous speech + meaning testing.
- `[x]` **Child Profiles**: Create / edit / delete in the Parent section, selection page at launch, per-profile stars / streak / ticks / study time / daily goal, music on the picker, dummy profile removed.
- `[x]` **Parent / Teacher Dashboard (`feature:parent`)**: Live metrics for the active profile, daily time chart, weak sounds heat map (247-letter grid), priority practice jumps ("Practice 🎯"), daily goal settings, and progress report export.
- `[x]` **Unit Test Suite**: Segmentation, dictionary classification, tracker logic, progress rules, profile rules, analytics grid and sound matching.

### Pending Features (`[ ]`)
- `[ ]` **Play Store readiness** (see the priority section above).
- `[ ]` **Locked Learning Path**: Duolingo-style path where nodes unlock in order, alongside the existing free jumping to any letter or activity.
- `[ ]` **Child Onboarding Flow**: Welcome, microphone permission explanation, Tamil voice check, language toggle (Tamil UI / English helper text).
- `[ ]` **Progress from every activity**: Quiz, stories and vocabulary practice should add stars / streak / time to the active profile (only lessons do today).
- `[ ]` **Reward Shop & Sticker Book**: Non-monetary rewards, collectible stickers, and avatar customizations.
- `[ ]` **Adaptive Spaced Repetition**: Database tracking for weak sounds, real per-letter accuracy, and automated resurfacing in upcoming practice sets.
- `[ ]` **Visual polish**: Brand palette and dark-mode check, bundled Tamil font, child home redesign, screen transitions, strings moved to `strings.xml`, accessibility pass.
- `[ ]` **Tablet Multi-Pane Layouts**: Responsive navigation rail and two-pane side-by-side reading layouts.

---

## 🛠️ Tech Stack & Architecture

* **UI Framework**: Jetpack Compose, Material 3, Material Design Icons.
* **Architecture**: Compose state-driven navigation with unidirectional data flow, clean separation of core speech engines, profile / progress storage and feature screens.
* **Storage**: Per-profile progress and the profile list in `SharedPreferences` (a Room database is planned for spaced repetition).
* **Speech & Audio**: Android `SpeechRecognizer` streaming pipeline with acoustic sound matching (`AcousticSoundMatcher`), Android `TextToSpeech` (Tamil `ta-IN`), `AudioManager` stream volume management, and looping background music (`BackgroundMusicPlayer`).
* **Text Processing**: Custom Unicode grapheme clustering (`TamilTextSegmenter`), syllable segmentation (`TamilSyllableSegmenter`), and transliteration (`TamilTransliteration`).

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Phonics-ta-IN.git
   ```
2. Open the project in **Android Studio** (Hedgehog or newer).
3. Sync Gradle and run the app on an Android device or emulator (API 33+ required at present, microphone permission enabled, and the offline Tamil voice and speech data installed so the app can run without a network).
4. On first launch, create a child profile when prompted. More profiles can be added from the Parent dashboard.
