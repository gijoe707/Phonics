# Multi-Language Tasks: Parameterizing the App for Indian Languages

**Goal:** turn the Tamil-only app into a language-driven app, so the same engine (lessons, tracing, read-along, quiz, stories, profiles, dashboard) can teach other Indian languages by adding a *language pack* rather than rewriting code.

**Status:** planning only. Nothing here is built. Tamil stays the first and only shipped language until the Tamil Play Store release is done (see `task.artifact.md`).

**Principles carried over from `task.artifact.md` (they apply to every language):**
- 100% offline: every language pack works without a network. Any download is a one-time, user-chosen step.
- No PIN, no parental gate, no crash reporting, no analytics, no trackers, no ads.
- Two ways to learn in every language: the locked learning path and free jumping.
- No linguistic content ships without review by a native speaker or teacher of that language.

---

## 🚦 1. Decisions Needed First

These shape everything else. Record the answers in `architecture-tech-stack.md`.

- `[ ]` **One app or many?** Option A: one app with a language picker (single listing, shared updates, larger download). Option B: one Play listing per language built from product flavors (small downloads, separate reviews and store pages, more release work). Option C: one app plus per-language asset packs.
- `[ ]` **Which languages, in what order?** Suggested order is in section 11; confirm it.
- `[ ]` **Brand and name:** "Phonics-ta-IN" has the Tamil code in it. Decide a language-neutral app name and whether each language gets its own display name (e.g. "Phonics Hindi"). The developer name is already decided: "OpenPaper Labs", domain `openpaperlabs.app`.
- `[ ]` **Learning language vs interface language:** the learner may study Hindi while the app text is in English or Tamil. Decide which UI languages are supported and how helper text (meanings, instructions) is chosen.
- `[ ]` **Delivery of language packs under the offline rule:** bundled in the app, install-time asset packs, or on-demand packs the user downloads once (user's choice). Decide the app size budget per language.
- `[ ]` **Streak, stars and XP:** shared across languages for a child, or separate per language?
- `[ ]` **Urdu and other right-to-left scripts:** in scope or out? (They need a different text and tracing engine, see section 11.)
- `[ ]` **Other scripts for the same child:** can one profile learn several languages at once?

---

## ⚠️ 2. Cheap Now, Costly Later (do before the Tamil Play release)

Published identifiers and saved-data formats are hard to change after real users exist.

- `[ ]` **applicationId:** choose a language-neutral id (e.g. `app.openpaperlabs.phonics`) if more languages are even possible. It cannot change after publishing.
- `[ ]` **Stored data format:** decide how progress is scoped by language (e.g. `<profileId>` + `<languageId>` in the storage name or key) and implement it before release, so no migration is needed later. Unit ids today (`unit_a`, `unit_uyirmei_1_4`) are Tamil-only and need a language namespace.
- `[ ]` **Profile record:** add a `languageId` (default `ta`) to the saved profile format now.
- `[ ]` **Do not add more Tamil hard-coding:** build the locked learning path, onboarding and settings screen reading from a language config, even if only Tamil exists.
- `[ ]` **Externalize UI strings** (already in the main backlog). Use the same step to separate "learning-language text" from "interface text".

---

## 🧱 3. Language Abstraction (code)

Today the Tamil assumptions live in names and code such as `TamilGrapheme`, `TamilTextSegmenter`, `TamilSyllableSegmenter`, `TamilTransliteration`, `TamilDictionaryRepository`, `CurriculumRepository`, `UnitKind` (UYIR, MEI, UYIRMEI, AYTHAM) and the `ta-IN` speech and voice settings.

- `[ ]` **Define a `LanguagePack` / `LanguageConfig`:** id, display names (native + English), locale tags (TTS and speech), script, writing direction, font, UI strings, unit inventory, teaching groups, dictionary, stories, audio assets, tracing data, matching rules.
- `[ ]` **Provider interfaces** that each language implements: `GraphemeSegmenter`, `SyllableSegmenter`, `Transliterator`, `SoundMatchingRules`, `CurriculumSource`, `DictionarySource`.
- `[ ]` **Shared Brahmic engine:** most Indian scripts build letters from a consonant, a vowel sign and a virama (pulli / halant). Build one configurable segmenter for these scripts, with per-language exceptions, instead of one class per language.
- `[ ]` **Generalize `UnitKind`:** Tamil terms (uyir, mei, uyirmei, aytham) become generic kinds (vowel, consonant, consonant+vowel syllable, special sign) with a per-language display name.
- `[ ]` **Generalize the letter grids:** `VowelsScreen`, `ConsonantsScreen` and `ConsonantFamilyScreen` assume Tamil's 12 + 1 vowels and 18 consonant families of 13 forms. Drive counts, grouping and titles from the language config (some languages group consonants in rows of five, and have more letters).
- `[ ]` **Remove Tamil constants from UI:** subtitles such as "18 letters x 13 sounds", the 247 total, and Tamil titles in the menu and home cards come from config.
- `[ ]` **Rename the code:** move Tamil-specific classes under a `language/ta` package; keep shared classes language-neutral.
- `[ ]` **Language registry:** a single place that lists installed language packs and returns the active one.
- `[ ]` **Keep Tamil behaving exactly as today** (regression tests before and after the refactor).

---

## 📦 4. Data-Driven Content

- `[ ]` **Content format:** versioned JSON (or equivalent) per language for units, teaching groups, words, sentences and stories, with the validation rules from `curriculum-design.md`. This also serves the existing "move curriculum out of Kotlin" backlog item.
- `[ ]` **Move Tamil into the same format first** and load it through the new path, so Tamil proves the design.
- `[ ]` **Teaching groups per language** (the Tamil design has 16 groups); each language needs its own reviewed order, since sounds and difficulty differ.
- `[ ]` **Dictionary per language** graded by grapheme length (the app's quiz levels 1 to 7+), with English meaning and, where useful, a meaning in the learner's helper language.
- `[ ]` **Stories per language:** original or public-domain stories at Grade 1-3 level, with licence recorded. Check whether a translated fable can be reused across languages.
- `[ ]` **Language-neutral pictures:** key images by concept (e.g. `hand`) so one illustration serves every language; add the word per language.
- `[ ]` **Content validation tool:** NFC normalization, grapheme clusters match the segmenter, no missing audio or image, no unknown prerequisites, per language.

---

## 🔊 5. Speech & Audio per Language

- `[ ]` **Locale per language:** TTS and speech-recognition locale tags (e.g. `hi-IN`, `te-IN`, `kn-IN`, `ml-IN`, `bn-IN`, `mr-IN`, `gu-IN`, `pa-IN`, `or-IN`), taken from the language config.
- `[ ]` **Availability check per language:** is the offline voice installed, is the offline recognition pack installed, and what happens if not (guide the user; fall back to "I said it"). Availability will differ by language and phone.
- `[ ]` **Reference sound bank per language** (the `ReferenceSoundBank` idea): native-speaker recordings of each letter, with the recording spec from the main backlog.
- `[ ]` **Acoustic matcher per language:** check how `AcousticSoundMatcher` and `SoundSiblings` depend on Tamil sounds, and define per-language confusable-sound sets and tolerances.
- `[ ]` **Word matching rules per language** (`WordMatching`): pronunciation variants such as dropped inherent vowels, nasal marks and regional forms need per-language handling.
- `[ ]` **Audio engine:** make `AppAudioEngine` and the TTS routing choose recorded asset first, then offline TTS, per language.
- `[ ]` **Pilot child voices per language** before trusting recognition accuracy.

---

## ✍️ 6. Tracing, Fonts & UI

- `[ ]` **Tracing data per script:** check how `TracingCanvas` gets its letter shapes and stroke order, and what each new script needs (stroke order is script-specific and needs native review).
- `[ ]` **Bundle a font per script** (e.g. Noto Sans for Devanagari, Telugu, Kannada, Malayalam, Bengali, Gujarati, Gurmukhi, Odia) so rendering does not depend on the phone.
- `[ ]` **Check glyph sizing and line height per script:** tall or stacked glyphs (conjuncts, Malayalam, Telugu, Kannada) need different tile and text sizes than Tamil.
- `[ ]` **Right-to-left support** if Urdu is in scope (layout mirroring, tracing direction, joined letter forms).
- `[ ]` **Language picker** in onboarding and in the menu / settings, and a way to switch learning language per profile.
- `[ ]` **Theme and icons:** keep one brand palette; optionally a small accent per language.
- `[ ]` **Menu, home cards and titles** come from the language pack (native name + English).
- `[ ]` **UI language independent of the learning language** (see section 1).

---

## 👧 7. Profiles, Progress & Dashboard

- `[ ]` **Profile has a learning language** (and optionally several); the picker and drawer header show it.
- `[ ]` **Progress is scoped by profile and language** (stars, completed units, study time per language).
- `[ ]` **Migration:** existing Tamil data becomes `ta` progress for the same profile with no loss.
- `[ ]` **Parent dashboard per language:** totals (`X / <language total>`), heat map and weak sounds for the active language; language switcher on the dashboard.
- `[ ]` **Report export** names the language.
- `[ ]` **Streak policy** implemented as decided in section 1.
- `[ ]` **Locked learning path per language,** driven by that language's teaching groups.
- `[ ]` **Deleting a profile** deletes all its language progress.

---

## 🧪 8. Tests

- `[ ]` **Tamil regression suite stays green** through the refactor (existing segmentation, dictionary, matching, progress and profile tests).
- `[ ]` **Golden tests per language:** expected letter counts, grapheme and syllable splits on a reviewed word list.
- `[ ]` **Content validation tests** run over every language pack in CI.
- `[ ]` **Parameterized tests:** one shared test suite run against each language pack's rules.
- `[ ]` **Speech and audio tests** with recorded samples per language.
- `[ ]` **Airplane-mode QA** for each language pack, including first install of a pack.
- `[ ]` **UI tests** for language picker, per-language progress and profile switching.

---

## 🧑‍🏫 9. Content & Review Pipeline

- `[ ]` **Native reviewer per language** (teacher or linguist): teaching order, keywords, sound distinctions, segmentation rules, stroke order, tricky words, stories.
- `[ ]` **Audio recording** per language (voice, speed, format, same spec as Tamil).
- `[ ]` **Illustration reuse and licensing** confirmed for every language.
- `[ ]` **Review status** tracked per unit and word, per language.
- `[ ]` **Dialect decisions** per language (standard form vs regional), recorded in the docs.
- `[ ]` **Licence check** for every story, song and font added.

### Language onboarding checklist (copy for each new language)
- `[ ]` Script and Unicode ranges listed; NFC rules confirmed
- `[ ]` Unit inventory and consonant / vowel-sign chart reviewed
- `[ ]` Teaching groups and locked path order reviewed
- `[ ]` Segmenter and syllable rules reviewed and tested
- `[ ]` Transliteration scheme chosen
- `[ ]` Word bank and meanings reviewed
- `[ ]` Stories reviewed and licensed
- `[ ]` Audio recorded and checked
- `[ ]` Tracing data and stroke order checked
- `[ ]` TTS and speech availability tested on real phones, offline
- `[ ]` Font bundled and glyph sizing checked
- `[ ]` Airplane-mode pass
- `[ ]` Store text translated (if separate listing)

---

## 🚀 10. Build, Delivery & Store

- `[ ]` **Chosen delivery model implemented** (flavors, one app with packs, or asset packs) and tested for offline install.
- `[ ]` **App size budget** per language and total; compress audio and images.
- `[ ]` **Per-language store listing** text, screenshots and category notes (Tamil and English first; others as they ship).
- `[ ]` **Privacy policy and Data safety answers** stay valid for every language (no new data collected).
- `[ ]` **Versioning:** how language packs and the app version relate; behavior when a pack is newer than the app.
- `[ ]` **Release checklist per language** (a short version of the Play checklist in `task.artifact.md`).
- `[ ]` **Update docs:** architecture, curriculum design, UX flows and README to describe language packs.

---

## 🗺️ 11. Language Notes (starting points, to be verified by native reviewers)

These are planning notes about script structure, not linguistic rulings. Each needs review by a native speaker or teacher.

| Language | Script | Things to plan for |
|---|---|---|
| Tamil | Tamil | Done. Smaller consonant set (18); reference for the data model |
| Hindi | Devanagari | Larger consonant set arranged in groups; vowel signs (matras); conjunct consonants; nasal marks and dot (nukta) letters; inherent vowel behavior affects pronunciation and word matching |
| Marathi | Devanagari | Shares the script with Hindi, so letters, font and segmenter can be reused; sounds, words and stories are separate |
| Telugu | Telugu | Many letters and vowel-sign combinations; stacked conjuncts; tall glyphs |
| Kannada | Kannada | Similar structure to Telugu; consonant + vowel-sign chart; tall glyphs |
| Malayalam | Malayalam | Special final-consonant letters (chillu); complex conjuncts; spelling convention choice (traditional or reformed) |
| Bengali (and Assamese) | Bengali-Assamese | Conjuncts; vowel signs placed before, after and around the consonant; Assamese has its own letters |
| Gujarati | Gujarati | Similar to Devanagari without the top line; conjuncts |
| Punjabi | Gurmukhi | Own nasal and doubling marks; tone-related letters |
| Odia | Odia | Rounded letters, conjuncts; tracing needs care |
| Urdu | Perso-Arabic | Right-to-left, joined letter shapes that change by position, no vowel signs in normal text. Needs a different segmenter, tracing and layout. Treat as a separate, later project |

### Suggested rollout order (confirm in section 1)
1. **Tamil** (shipped first, used to prove the language-pack design)
2. **Hindi**, then **Marathi** (shared script)
3. **Telugu, Kannada, Malayalam** (the other Dravidian languages; related structure)
4. **Bengali, Gujarati, Punjabi, Odia**
5. **Urdu** (only if right-to-left is approved)

---

## ✅ 12. Definition of Done for a New Language

- All unit, word and story content reviewed and approved by a native reviewer
- Letter lessons, tracing, read-along, quiz and stories work end to end
- Locked path and free jumping both work
- Works fully in airplane mode, including voice and speech setup guidance
- Profiles, progress, dashboard and report show the language correctly
- Tests and content validation green for the language, Tamil regression still green
- Store listing and licences ready
