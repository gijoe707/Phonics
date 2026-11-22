# Phonics-ta-IN Project Tasks & Roadmap

## 🚨 PRIORITY: Before Play Store Upload

Nothing below is optional. The app cannot be published (or should not be) until these are done.

### 🔐 Build, identity & signing
- `[ ]` **Change applicationId** from `com.example.phonics_ta_in` (Play rejects `com.example`) to a real, language-neutral id tied to the developer domain, e.g. `app.openpaperlabs.phonics` (developer domain `openpaperlabs.app`). It cannot change after publishing, so decide with the multi-language plan in mind (`task.multi-language.md`, section 2). The code `namespace` can stay as it is
- `[ ]` **Release signing**: create an upload keystore, replace `signingConfig = debug` in `app/build.gradle.kts`, keep keystore/passwords out of the repo (`.gitignore`) and back them up safely
- `[ ]` **Build an AAB** (`bundleRelease`), not an APK, for the Play upload
- `[ ]` **Versioning**: set a real `versionName` / `versionCode` policy for updates
- `[ ]` **Stop committing build output**: `app/debug/*.apk`, `app/release/*.apk` and `mapping.txt` should be git-ignored (keep `mapping.txt` for Play's deobfuscation upload)
- `[ ]` **minSdk review**: currently 33, which excludes many Indian phones on Android 10-12. Check which APIs need 33 and lower it if possible (re-test speech and audio on the lower versions, and keep fully offline speech working)
- `[x]` **Release-build test (R8 on)** on a real device: speech recognition, TTS, audio, tracing, profiles, parent dashboard, `keepRules/rules.keep`. Done; the earlier release crash is fixed. Re-run it on the final signed AAB before upload

### 🎨 Store identity & assets
- `[ ]` **Launcher icon**: replace the default Android robot with a real adaptive icon (foreground, background, monochrome/themed)
- `[ ]` **App name**: friendly store name (proposed: BoliMoli, see Settled Decisions), plus Tamil label via `values-ta/strings.xml`
- `[ ]` **Play Store graphics**: 512x512 icon, 1024x500 feature graphic, phone screenshots (and tablet if supported)
- `[ ]` **Store listing copy**: short and full description (Tamil and English), category (Education), contact email `support@openpaperlabs.app`

### ⚖️ Policy & legal (kids' app + microphone)
- `[ ]` **Privacy policy**: hosted at a public URL (e.g. `openpaperlabs.app/phonics/privacy`). Must cover microphone use, what is stored on the device (profiles, progress), no ads, no trackers, no crash reporting, no network use for core features, no data sold or shared
- `[ ]` **Data safety form** in Play Console (microphone, on-device storage, no data shared)
- `[ ]` **Target audience & content**: declare children as an audience and complete the Families policy requirements; confirm "no ads", no external links and no purchases (there is no parental gate, so none may exist)
- `[ ]` **Resolve music licence**: background track is CC BY-SA 3.0 (ShareAlike can be awkward inside an app). Replace with a CC0 / CC BY / own track, or confirm compliance. Keep the in-app Licenses page accurate either way
- `[ ]` **Third-party licences**: list any other libraries/assets used on the Licenses screen
- `[ ]` **Play Console developer account**: choose developer name "OpenPaper Labs" (the umbrella for the learning app and the question paper app), finish identity verification, and check the current closed-testing requirement for new accounts before planning the production release

### 🛡️ First-run experience & error handling
- `[ ]` **Microphone permission flow**: kid-friendly explanation before the system prompt, and a graceful screen when denied (lessons that need the mic must not break)
- `[ ]` **Tamil voice / speech pack check**: detect a missing offline `ta-IN` TTS voice or speech recognition pack and guide the user to install it (needed for 100% offline use), with the "I said it" self-check as the fallback, instead of silent failure
- `[ ]` **Error and empty states**: no mic, offline, speech engine unavailable, no profiles

### ✅ Final QA gate
- `[ ]` Test on at least one low-end device and one tablet; portrait and landscape
- `[ ]` Test fresh install, update from an older build (profile migration), and rotation / process death
- `[ ]` Full lesson run for every letter category; read-along; custom reader; quiz
- `[ ]` Run the full unit test suite and fix anything red
- `[ ]` Upload to an internal / closed testing track first

---

## ✅ Settled Decisions

- **Two ways to learn, both valid:** a new locked, Duolingo-style learning path AND free jumping to any letter or activity (vowel / consonant grids, quiz, stories, custom reader, "Practice" jumps). Neither replaces the other.
- **No parent PIN and no parental gate.** The Parent dashboard, profile management and Licenses open directly. Deleting a profile still asks for confirmation.
- **No crash reporting, analytics, trackers or ads.**
- **100% offline, by user choice:** every feature must work without a network. Any online feature (cloud speech, downloadable packs, backup) is optional and only used if the user turns it on.
- **Brand:** umbrella developer name "OpenPaper Labs" with the domain `openpaperlabs.app`, covering the Tamil learning app and the question paper generator.
- **App brand (proposed, confirm):** the app is called **BoliMoli**, with three mascots: Boli, Moli and Kili. Learning zones are named after real linguistic terms (Swara Valley, Dhwani Jungle, Akshara Land, Lipi Lab) and sit on top of the existing screens. See "BoliMoli Worlds" below.
- **Voice games never punish:** the speech check accepts or gently retries; the mascots never say or show "wrong".

---

## Master Feature & Task Progress

### 📖 Curriculum & Phonics Core
- `[x]` Vowels (`உயிர் எழுத்துகள்`) 12 letters + Aytham (`ஃ`) with flashcards & audio
- `[x]` Consonants (`மெய் எழுத்துகள்`) 18 letters x 13 Uyirmei combinations
- `[x]` Interactive 5-Skill Lesson Sequence (Listen, Read, Trace, Speech Match)
- `[x]` Letter stroke order handwriting canvas (`TracingCanvas`)
- `[x]` On-device acoustic sound matcher (`AcousticSoundMatcher`)

**Speech sound check: match the child's mic sound to the Android TTS sound of each letter**
The child's isolated sound is recorded and compared with how TTS says the same letter; word recognition stays as the automatic fallback.
- `[x]` Record one isolated sound from the mic (`IsolatedSoundRecorder`, 16 kHz, stops on silence; the mic is released before the word recognizer starts)
- `[x]` Render the TTS sound of every letter to a reference (`AppAudioEngine.synthesizeToFile`, `WavDecoder`, `ReferenceSoundBank`; built one at a time in the background and cached)
- `[x]` Compare with MFCC + DTW and vocal-tract warping (`SoundFingerprint`, `AcousticSoundMatcher`). A child's voice sits higher than the TTS voice, so the child's audio is analysed with 8 frequency warps
- `[x]` Decide by ranking against the sibling letters' TTS sounds (the target must be closest or practically tied), not by a fixed distance
- `[x]` Leave the short/long twin out of the comparison (`SoundSiblings`: அ/ஆ, க/கா ...), because the two differ in length, not in sound quality
- `[x]` Voicing check for vowels, so a hiss such as "sh" is not accepted as a vowel
- `[x]` Wire into the lesson speech test (`SayLetterView`): sound check first, then the word recognizer as the fallback, with the existing "I said it" self-check
- `[x]` Unit tests written: `WavDecoderTest`, `SoundSiblingsTest`, `AcousticSoundMatcherTest` (synthetic voices)
- `[ ]` **Build the project and fix any compile errors** (written without a Gradle build; not compiled yet)
- `[ ]` **Run the new unit tests** and fix anything red
- `[ ]` **Test on a real phone with a child's voice;** read Logcat tag `SoundMatch` and tune `TIE_MARGIN`, `STAND_OUT_RATIO` and `MIN_VOICED_FRACTION`. The design was only checked on synthetic voices
- `[ ]` Check that the Tamil TTS actually renders each single letter and each consonant; a letter with no usable TTS audio falls back to words
- `[ ]` Tune consonants (unvoiced k, ch, t, p) separately; the voicing requirement is off for them
- `[ ]` Known limit to decide on: a short vowel cannot be told from its long twin by sound quality. Option: add a duration cue, or accept both
- `[ ]` Handle the case where speech recognition is unavailable on the device (the word fallback currently just waits)

### 🎙️ Read-Aloud & Progressive Assistance Engine
- `[x]` Always-on microphone streaming (`StreamingSpeechListener`)
- `[x]` Real-time synchronized word highlighting & paragraph reading layout
- `[x]` Silent / beep-free system chime suppression
- `[x]` User-prompted assistance pop-up (grapheme split -> syllable split -> full audio)
- `[x]` Pre-reading distinct letter review grid & 1-by-1 speech test
- `[x]` Grade 1-3 Stories library (Thirsty Crow, Hare & Tortoise, Lion & Mouse, etc.)
- `[x]` Custom text reader with state persistence

### 🎯 Progressive Levels & Dictionary Quiz
- `[x]` Tamil Dictionary Repository categorized by grapheme length (Levels 1 to 7+)
- `[x]` Dynamic continuous random test set generator (`generateRandomTestSet`)
- `[x]` Single activity screen (`TestYourKnowledgeScreen`)
- `[x]` Simultaneous Speech Pronunciation & Meaning Comprehension testing
- `[x]` Star counter, progress tracking, and instant feedback

### 👧 Child Profiles
- `[x]` Create / edit / delete child profiles (name + avatar) in the Parent section
- `[x]` Profile selection page at launch when there are 2+ profiles; one profile opens directly; first-run create prompt when there are none
- `[x]` Background music plays on the profile selection page (same player as the main page)
- `[x]` Stars, streak, completion ticks, study minutes and daily goal saved per profile
- `[x]` Removed the hardcoded dummy profile ("அன்பு (Aun)")
- `[x]` "Switch profile" in the menu; active profile shown in the menu header
- `[x]` Pre-profile progress is copied into the first profile created (one time)
- `[ ]` Quiz, stories and vocabulary practice still don't record stars / streak / time to the profile (only lessons do)

### 🗺️ Locked Learning Path (new activity)
- `[ ]` Add a locked, Duolingo-style learning path as a new activity on the home screen: nodes unlock in order, the current node is highlighted, stars shown per node
- `[ ]` Keep free jumping fully open at all times (letter grids, quiz, stories, custom reader, "Practice" jumps). Both modes are valid
- `[ ]` Both modes save to the same active profile (stars, streak, study time, completed letters)
- `[ ]` Decide: does a letter finished through free jumping also unlock path nodes? (proposed: yes, so nothing is repeated)
- `[ ]` Make the path work fully offline like the rest of the app
- `[ ]` Clarify naming: the home screen is titled "Learning Path" today, so rename it (e.g. "Home") once the real path exists
- `[ ]` Lay the locked path over the four BoliMoli worlds (see "BoliMoli Worlds" below); free jumping stays open

### 🌍 BoliMoli Worlds (themed learning zones)
Proposed, to confirm. Each world is named after a real linguistic term and built on screens that already exist, so it is mostly new wrappers and games, not new content. Everything works offline, has no failure state, and follows the settled decisions above.

| Stage | World | Linguistic focus (Tamil term) | Kid voice activity |
|---|---|---|---|
| 1 | Swara Valley | Vowels (Uyir / Swara) | Say "Aaa" loudly to wake a sleeping animal |
| 2 | Dhwani Jungle | Consonants (Mei / Vyanjana) | Repeat crisp sounds (Ka, Cha, Ta, Pa) like an echo game |
| 3 | Akshara Land | Syllables (Uyirmei / Akshara) | Snap a vowel and a consonant together and say the syllable |
| 4 | Lipi Lab | Script (Lipi) | Hear a sound, tap the right letter, then trace it |
| Hub | Boli & Moli's Playground | Everyday words, streaks, rewards | Say Amma, Appa, Pani, Namaste |

**Brand and mascots**
- `[ ]` Confirm BoliMoli as the app name (store name, splash, launcher label, `values-ta` label). Decide the `applicationId` together with it, since it cannot change after publishing
- `[ ]` **Kili (கிளி, parrot):** define role, look, voice and personality. Assumed: the echo friend who repeats sounds back. Confirm whether Kili is a third mascot or the main guide
- `[ ]` **Boli and Moli:** choose the animals (suggested: a peacock and an elephant), names, colours and catchphrases
- `[ ]` Draw the mascots as Compose vector art in the style of `WordIllustrations.kt` (no image assets, small APK)
- `[ ]` Reaction set: cheer, hint, listening, sleepy; each with a reduced-motion version. No sad or failure reactions

**Boli & Moli's Playground (home hub)**
- `[ ]` Home screen with Boli and Moli showing streak, XP and daily goal (replaces the plain menu look; see "Child home screen redesign")
- `[ ]` Everyday words practice: Amma, Appa, Pani, Namaste, using the word bank and the speech check
- `[ ]` Outfits and stickers unlocked after a finished lesson (free, no purchases; same item as the sticker album)

**Swara Valley (vowels, voice-driven)**
- `[ ]` Live voice level (and pitch) from the mic: add a level callback to `IsolatedSoundRecorder`; pitch can reuse the autocorrelation in `SoundFingerprint.voicedFraction`
- `[ ]` Game: say "Aaa" loudly to wake a sleeping animal (accepted by the sound check)
- `[ ]` Game: hold a note so a character jumps or flies; keep it forgiving
- `[ ]` Cover all 12 vowels with the existing Uyir units

**Dhwani Jungle (consonants, echo game)**
- `[ ]` Echo game: Kili says a sound, the child repeats it (sound check against the TTS reference)
- `[ ]` Cover the 18 mei consonants; tune the unvoiced ones on a real device (see the sound check items)

**Akshara Land (blending)**
- `[ ]` Puzzle-piece screen: a consonant and a vowel snap together into a syllable
- `[ ]` The child says the combined syllable; sound check with the twin excluded
- `[ ]` Use `CONSONANT_FAMILIES` (mei + 12 uyirmei forms); play each part, then the blend (matches the letter-by-letter correction flow)

**Lipi Lab (script and tracing)**
- `[ ]` Hear a sound, tap the letter, then trace it (reuse `ListenAndTapView` and `TracingCanvas`)
- `[ ]` More tracing templates: only அ, ஆ and க have stroke templates today
- `[ ]` Offer Lipi Lab for a letter right after the child speaks its sound

**World map**
- `[ ]` Island map as the entry to the four worlds, with the locked path and free jumping both available
- `[x]` World names and one-line descriptions per UI language are in `res/values*/strings_lands.xml` (English, Tamil, Malayalam, Telugu, Kannada, Hindi, Marathi, Bengali, Gujarati, Punjabi, Odia, Assamese, Urdu), using each language's own words: native words in the Dravidian languages, Sanskrit-rooted names (Swara / Dhwani / Akshara / Lipi) only in Hindi, Marathi, Bengali and Gujarati, each in its own spelling, and everyday words in Punjabi, Odia, Assamese and Urdu. English names: Vowel Grove, Echo Forest, Joining Island, Letter Porch, Boli & Moli's Playground. Brand name "Boli & Moli" stays in Latin script (in Tamil "போலி" means "fake")
- `[x]` Language list registered for per-app language (`res/xml/locales_config.xml`, `android:localeConfig` in the manifest)
- `[ ]` **Native-speaker review of every `strings_lands.xml`** (all are drafts; Odia and Assamese are lowest confidence)
- `[ ]` Use the land strings on the home map and world screens with `stringResource(R.string.land_*)`
- `[ ]` In-app language picker that sets the app language (`LocaleManager.applicationLocales` on Android 13+, AppCompat on older), so the choice is remembered and the land names follow it
- `[ ]` Move the rest of the hard-coded UI text into `strings.xml` per language; only the land names are localised so far, so a language picker would still show mostly English/Tamil screens
- `[ ]` Urdu is right-to-left: check layouts mirror correctly before offering it

### 🛡️ Parent / Teacher Area & Live Analytics Dashboard
- `[x]` PIN protection removed for instant dashboard access (decision: no PIN and no parental gate)
- `[x]` Real-time progress metrics connected to the active profile (letters mastered `X / 247`, time learned today)
- `[x]` Synchronized daily goal timer (updating main screen goals in real-time)
- `[x]` Daily learning time weekly bar chart (Mon to Sun)
- `[x]` Priority weak sounds list with direct `Practice Now 🎯` lesson navigation
- `[x]` 247-Letter Mastery Heat Map Grid with status badges and filters
- `[x]` Progress report export & sharing (`Intent.ACTION_SEND`)
- `[ ]` Real per-letter accuracy (heat map is currently mastered / needs-practice only, based on finished lessons)

### 🎮 Gamification & Customization
- `[x]` XP counter, daily streak tracker, and stars earned
- `[ ]` Sticker album & non-monetary collectible rewards shop
- `[ ]` Lesson-completion celebration (confetti / sound) outside the read-along screen
- `[ ]` Child onboarding setup (welcome, language toggle)
- `[ ]` First five minutes: meet Boli, Moli and Kili, Kili explains the mic just before the first recording, the child's first win is saying "Aaa", then parent setup (child name, daily goal, language)

### 🌟 Engagement & Reward Plan (from the app review, in build order)
The goal is to add the reward layer and variety on top of the read-along core. The order below is the build order. None of this blocks the Play Store upload; the Priority section at the top still comes first. Items already built are ticked, and items that exist elsewhere in this file are linked rather than repeated.

**Step 1: Stability**
- `[x]` Release-build crash fixed and release build tested on a real device (R8 on)

**Step 2: Streaming read-along and custom text (friction fix)**
- `[x]` Always-on streaming read-along and custom text reader (done, see the Read-Aloud section)

**Step 3: Saved progress and stars**
- `[x]` Real saved progress per profile: stars per lesson (1 to 3), streak, daily goal and study time (no hardcoded values left)
- `[ ]` Path that unlocks level by level (see "Locked Learning Path" above; free jumping stays open)
- `[ ]` Forgiving streak: one missed day must not break it (e.g. a grace day), with unit tests in `ProgressLogic`. Today a single missed day resets the streak
- `[ ]` Show the streak, stars and daily-goal progress where the child sees them (home screen and after each lesson)

**Step 4: Mascot and sound effects**
- `[ ]` Design the friendly mascots (name, look, voice) following the illustration style guide. Proposed set: Boli, Moli and Kili (see "BoliMoli Worlds")
- `[ ]` Mascot appears on the home screen, in lessons, in read-along and on the reward screen
- `[ ]` Mascot cheers on success and gives hints
- `[ ]` Mascot does the "say it again" model for a missed word: it says the word, then reading resumes
- `[ ]` Record short mascot voice lines (native speaker, offline); keep spoken praise minimal
- `[ ]` Short sound effects for correct, try again and lesson complete, instead of spoken praise; they follow the mute button and the parent's music/sound setting
- `[ ]` Gentle feedback rules: never say or show "wrong" (or its Tamil equivalent). Audit every on-screen text and audio line for this
- `[ ]` Feedback flow in read-along: yellow word, helper says the word, reading resumes. Keep the existing tap-for-help flow as well
- `[ ]` Add a check mark or icon beside the green and yellow word states, so colour is never the only cue (see the accessibility backlog)
- `[ ]` Lesson-completion celebration and reward screen use the mascot (see "Lesson-completion celebration" above and the post-lesson reward screen in the UX backlog)

**Step 5: More activity types and stories**
- `[ ]` Lesson length target: 3 to 5 minutes and 5 to 8 tasks per lesson; measure real lesson times and adjust
- `[ ]` Mix task types inside a lesson and avoid repeating the same type back to back: listen and tap, match the sound to the letter, build a word from syllables, trace with a finger, read-along (see the exercise-type audit and 8-step lesson items in the curriculum backlog)
- `[ ]` Graded, illustrated stories with each word highlighted as it is read (extend the Grade 1-3 library; see "More stories" in the content backlog)
- `[ ]` "Listen first, then you read" mode for stories and the custom reader: the app reads the page, then the child reads it
- `[x]` Custom text reader for parents and teachers (already built)
- `[ ]` Tamil and English UI toggle (see the strings.xml and onboarding items; the child-facing text stays Tamil-first)

**Pictures and mnemonics (Jolly Phonics style)**
- `[ ]` Audit what exists today (word illustrations, keywords, actions) for each of the 247 letters
- `[ ]` Every letter gets an image, a keyword (e.g. அ for அம்மா) and a short action or story (see the per-letter story / action item in the curriculum backlog)
- `[ ]` Keyword and image shown and spoken in the lesson's first step

**Making it work for children's voices**
- `[ ]` Be lenient in matching: review the thresholds in `AcousticSoundMatcher` and `WordMatching` against children's recordings (see the child-voice pilot in the audio backlog)
- `[ ]` Never stuck: after two tries, the mascot says the word and the child moves on. Check what read-along does today after repeated misses
- `[ ]` Keep each child's list of letters and words they miss and bring them back later (see the Room and spaced repetition item)
- `[ ]` Voice prompts for every instruction, big buttons and icons on all child screens, since young children cannot read the instructions (see the touch-target and accessibility items)

**Retention**
- `[ ]` Collectible stickers (see "Sticker album" above)
- `[ ]` Avatar items: unlockable accessories for the profile avatar, earned through stars and streaks
- `[ ]` Weekly reward chest that opens after meeting the weekly goal (free, offline, no purchases, no ads)
- `[x]` Parent area with weak letters, time spent and progress (built). The review suggested putting it behind a PIN; that is not adopted, since the settled decision is no PIN and no gate
- `[x]` Ad-free and fully offline (settled decisions)

### 🎨 Polish & UX
- `[ ]` Brand palette: turn off dynamic colour, replace hardcoded colours with theme tokens, check dark mode
- `[ ]` Bundle a Tamil font (e.g. Noto Sans Tamil) for consistent rendering
- `[ ]` Child home screen redesign: daily-goal ring, mascot, bigger icons, the Locked Path as the main card; Parent and Licenses can live in the menu only
- `[ ]` Screen transitions and animations
- `[ ]` Move UI text to `strings.xml` (Tamil / English toggle)
- `[ ]` Accessibility: content descriptions, large-font check, tap targets

### ⚙️ Architecture & Adaptive Engine
- `[x]` Grapheme-aware Tamil text segmenter (`TamilTextSegmenter`) with unit tests
- `[x]` Unit tests for progress rules, profile rules and analytics grid
- `[ ]` Room database persistent weak-sound tracking & spaced repetition engine
- `[ ]` Tablet window size class adaptive multi-pane layout
- `[ ]` Compose UI tests for the profile flow

---

## 🗂️ Backlog: Non-Priority (no immediate attention needed)

Good ideas and cleanups for after the first Play Store release. Nothing here blocks the upload.

### 📚 Content & Curriculum
- `[ ]` Record native-speaker audio for letters and words to replace TTS where quality is weak
- `[ ]` Grantha letters (ஜ, ஷ, ஸ, ஹ, க்ஷ, ஸ்ரீ), which are outside the current 247
- `[ ]` Numbers (எண்கள்), days, months and colours vocabulary packs with illustrations
- `[ ]` More stories (Grade 4+), poems and rhymes with read-along
- `[ ]` Simple sentence-building and word-building activities
- `[ ]` Handwriting practice for whole words, not just single letters
- `[ ]` Seasonal / festival themed content packs

### 🎮 Learning Features
- `[ ]` Mini-games (matching, letter hunt, sound bingo)
- `[ ]` Daily challenge and streak milestone badges
- `[ ]` Adaptive difficulty based on each child's results
- `[ ]` Age / grade setting per profile to tailor content
- `[ ]` Optional daily practice reminder (local notification, works offline; needs the notification permission)
- `[ ]` Printable worksheets and completion certificates (PDF)
- `[ ]` Weekly progress summary for parents

### 👩‍🏫 Parent / Teacher Extras
- `[ ]` Classroom mode: many children, class-level report
- `[ ]` Export / import progress to move to a new phone
- `[ ]` Report as PDF, not just shared text
- `[ ]` Per-profile daily screen-time reminder (soft only: with no PIN, a hard limit can be bypassed)

### 📱 Platform & Reach
- `[ ]` Tablet, Chromebook and foldable layouts (links to the multi-pane item above)
- `[ ]` Home-screen widget showing streak and stars
- `[ ]` Optional cloud backup / sync (opt-in only, never required; needs a privacy review first)
- `[ ]` Reuse the engine for other Indian languages (Hindi, Telugu, Malayalam, Kannada). Full plan in `task.multi-language.md`

### 🧱 Code Quality & Tech Debt
- `[ ]` Move app-level state out of `TamilPhonicsApp` into ViewModels (profile, progress, music)
- `[ ]` Migrate to Navigation Compose with proper back stack handling
- `[ ]` Move progress from SharedPreferences to Room (do together with spaced repetition)
- `[ ]` Add dependency injection if the app keeps growing (Hilt or Koin)
- `[ ]` Remove unused code (e.g. `DailyUsageStat`) and replace magic numbers / hardcoded dp values
- `[ ]` Add lint / ktlint / detekt and fix the warnings
- `[ ]` Compose `@Preview`s for the main screens
- `[ ]` Instrumented and screenshot tests for key flows
- `[ ]` Review `allowBackup` and the backup rules for profile data

### ⚡ Performance & Size
- `[ ]` Baseline profiles for faster startup
- `[ ]` Profile battery and CPU use of the always-on speech streaming
- `[ ]` Reduce APK / AAB size (audio assets, unused resources)

### 🔧 Build & Release Process
- `[ ]` CI pipeline (build + unit tests on every push)
- `[ ]` Automate release build and versioning

### 📣 Growth & Support
- `[ ]` Simple website at `openpaperlabs.app` with a page per app (e.g. `/phonics`) and `support@openpaperlabs.app`
- `[ ]` In-app feedback link (only if the Families policy allows it without a gate; otherwise skip and rely on the store listing)
- `[ ]` Store listing translations and a short promo video
- `[ ]` Rate-the-app prompt (same condition as the feedback link; otherwise skip)

### 📝 Documentation
- `[x]` Update `architecture-tech-stack.md`, `curriculum-design.md` and `ux-flows-wireframes.md` to include profiles, locked path + free jumping, offline-first, and the no-PIN / no-crash-reporting decisions
- `[ ]` Replace the placeholder git URL in the README once the repo is hosted

### 📐 Backlog from Design Docs: Curriculum & Learning Design
From `curriculum-design.md` and the original prompt. Not yet built or only partly built.
- `[ ]` Guided learning path content design: use the 16 teaching groups from the design doc as the locked path order (free letter grids stay open as the second way to learn)
- `[ ]` Lock nodes explained by text and audio, not just an icon
- `[ ]` Grow the lesson from 5 steps to the designed 8: add Say-and-act (action / chant), Blend, Find-the-sound and a short Recall check
- `[ ]` Audit exercise types against the design mix: drag-to-build word, fill the missing letter, listen and tap the first letter, match letter to picture, see picture and pick the word
- `[ ]` Levels 5-7 from the design: phrases, short sentences, and stories with comprehension questions
- `[ ]` Configurable unlock thresholds (e.g. 80% accuracy, minimum attempts, new items per session) instead of fixed values
- `[ ]` Optional baseline placement check at onboarding
- `[ ]` Tricky-words list (compound or irregular words) with a review reason
- `[ ]` Per-letter story character, action, and short song / chant (the Jolly Phonics idea)
- `[ ]` Grow the word bank to 200-300 approved words by level (target counts are in the design doc), each with image, audio, meaning and syllable split
- `[ ]` Tracing: accept a slightly different but complete stroke, treat perfect handwriting as optional, and use lighter animation on low-end devices

### 🧑‍🏫 Backlog: Native Review & Content Governance
- `[ ]` Native Tamil teacher review of: teaching order, short/long sound distinctions, keyword for every letter, syllable and stroke rules, tricky words, aytham teaching, grantha coverage
- `[ ]` Track a review status per letter and word (pending / approved)
- `[ ]` Illustration style guide, plus sourcing and licensing plan for pictures
- `[ ]` Audio recording spec (voice, speed, format) for letters, syllables, words and feedback phrases
- `[ ]` Move the curriculum out of Kotlin into versioned JSON with a validator (NFC text, grapheme clusters match the segmenter, no missing audio / image, no unknown prerequisites)
- `[ ]` Optional, user-chosen content packs: download once, then work fully offline (never required for core use)

### 🔊 Backlog: Audio & Speech Architecture
- `[ ]` Audio provider interface (system TTS, pre-recorded assets, optional neural / cloud only if the user opts in) that tries recorded assets first
- `[ ]` Speech recognition provider interface, so engines can be swapped
- `[ ]` Collect a small pilot set of child voices (with consent) and compare the current recognizer with on-device Indic models (offline is required; cloud only as an opt-in extra)
- `[ ]` Adjustable speech speed setting
- `[ ]` Never store or expose raw recognition audio / logs; any cloud speech stays off unless the user turns it on
- `[ ]` Check the read-aloud flow against the designed state machine (Idle, Ready, Listening, Evaluating, Correct, NeedsCorrection, Review) and make it an explicit state model

### 🧩 Backlog: Architecture Items from the Proposal
- `[ ]` Split into modules (`feature:*`, `core:*`) if the app keeps growing; it is a single module today
- `[ ]` Use DataStore for settings instead of SharedPreferences
- `[ ]` Store append-only attempt records in Room and keep the adaptive engine a pure function of a snapshot
- `[ ]` Make lesson runtime state survive process death (a lesson restarts today)
- `[ ]` Room migration tests once Room is added

### 🖥️ Backlog: UX from the Wireframes
- `[ ]` Phone bottom navigation (Learn, Practice, Rewards, Parent) instead of, or alongside, the drawer menu
- `[ ]` Learning path map: at most 7 visible nodes, goal progress always visible
- `[ ]` Post-lesson reward screen: +XP, stars and a collectible sticker
- `[ ]` Gentle inline retry ("Listen again") instead of modals for ordinary wrong answers
- `[ ]` Settings screen: UI language, audio speed, sound and haptics, high contrast, text scaling, microphone mode, offline mode (user choice), reminder time, data export and deletion
- `[ ]` Onboarding extras: child age band, Tamil exposure at home, and daily goal chosen at setup
- `[ ]` Read-aloud tablet layout with a support panel (big target word, picture, hear / split / try again / "I said it")
- `[ ]` Parent privacy screen explaining exactly what is stored, with a visible delete-all-data action
- `[ ]` Exported report contains only the selected child's progress

### ♿ Backlog: Accessibility & Testing from the Design Docs
- `[ ]` Touch targets of at least 48dp (56dp on child screens)
- `[ ]` Contrast 4.5:1 for text, 3:1 for large text and controls
- `[ ]` TalkBack labels for Tamil letters and pictures
- `[ ]` Tap alternative for every drag interaction
- `[ ]` Never show correctness by colour alone (e.g. the heat map's mastered / needs-practice colours need a text or icon cue)
- `[ ]` Reduced-motion variant for celebrations and confetti
- `[ ]` Accessibility test pass: TalkBack, contrast, touch targets, large text
- `[ ]` Speech and audio tests using recorded Tamil samples

### ❓ Backlog: Open Product Decisions (record the answers in the docs)
- `[ ]` Voice style: Indian or Sri Lankan Tamil
- `[ ]` Who records and approves audio and illustrations
- `[ ]` Confirm the Play Families policy implications of having no parental gate: no external links, purchases or ads, and check the "share report" action in the Parent dashboard
- `[ ]` Business model: confirm free and ad-free, and whether subscriptions are ruled out
- `[ ]` Course start: vowels first, or a mixed vowel / consonant sequence
- `[ ]` Tracing tolerance that counts as success
- `[ ]` Any classroom or curriculum standards to follow, and the target age range
- `[ ]` BoliMoli: confirm the app name, the role of Kili, the animals for Boli and Moli, and the `applicationId` (e.g. `app.openpaperlabs.bolimoli` instead of `app.openpaperlabs.phonics`)

### 📝 Backlog: Doc Reconciliation (stale statements found)
- `[x]` `tamil-phonics-reader-prompt.md`: PIN gate and test-count mentions fixed, release checklist points to the priority section, offline and no-PIN decisions recorded
- `[x]` `architecture-tech-stack.md`: settled decisions and a "current vs proposal" table added; PIN, cloud and analytics wording updated
- `[x]` `ux-flows-wireframes.md`: PIN removed from wireframes and settings, "18/250" fixed to 247, locked path + free jumping added, implementation status table added
- `[x]` `curriculum-design.md`: "Two ways to learn" section and implementation status table added
- `[ ]` Re-check all four design docs when the locked path, onboarding and settings screen are built

### 📴 Backlog: Offline-First (100% offline, by user choice)
- `[ ]` Offline mode setting: when on, the app uses only on-device speech and audio and never touches the network; any online extra stays off until the user turns it on
- `[ ]` Airplane-mode QA pass of every screen and feature: lessons, tracing, read-along, stories, custom reader, quiz, vocabulary, profiles, dashboard, report export. Suggest promoting this to the priority QA gate
- `[ ]` Keep the manifest free of the INTERNET permission unless an opt-in online feature is added; reflect that in the privacy policy and Data safety answers
- `[ ]` On-device speech: verify recognition with the offline Tamil language pack, detect when it is missing, and guide the user through the one-time download (their choice), with "I said it" and the acoustic matcher as the fallback
- `[ ]` Offline Tamil TTS voice: detect missing voice data and guide installation; prefer bundled recorded audio for letters and syllables so they need no TTS at all
- `[ ]` Bundle all curriculum, audio and images in the app, with no first-run downloads
- `[ ]` Test phones without Google speech services or with limited offline packs, and make the fallback path obvious
- `[ ]` Progress export / import through a file (no cloud) so moving phones also works offline
- `[ ]` Mention "works fully offline" in the store listing
