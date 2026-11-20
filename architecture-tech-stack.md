# Architecture and Tech-Stack Proposal

## Recommendation

Build a native **Kotlin + Jetpack Compose** Android app using **MVVM at feature boundaries**, **MVI for the read-aloud and adaptive-learning state machines**, and a modular offline-first architecture.

This is the preferred option because speech, accessibility, text segmentation, tablet layouts, Google Play Families requirements, and low-end Android-device support are best handled by platform APIs. The app should avoid a hybrid runtime for the MVP.

## Module Split

| Module | Responsibility |
|---|---|
| `app` | Application setup, navigation, dependency injection, window-size configuration |
| `feature:onboarding` | Profiles, language preferences, consent, parent PIN |
| `feature:curriculum` | Letter, syllable, word, phrase, and story lessons |
| `feature:readaloud` | Recording flow, word timing, correction overlay, retry flow |
| `feature:gamelayer` | XP, stars, streaks, hearts, rewards, learning path |
| `feature:parent` | Dashboard, goals, reports, profile management |
| `core:curriculum` | Immutable content models and adaptive-learning engine |
| `core:audio` | TTS abstractions, asset cache, playback queue, synchronized highlighting |
| `core:speech` | Recognition abstractions, result normalization, confidence policy |
| `core:text` | Grapheme-aware Tamil segmentation and transliteration helpers |
| `core:data` | Room entities, repositories, DataStore settings, JSON import |
| `core:ui` | Theme, typography, accessibility, responsive layouts |

## State and Navigation

- Use Jetpack ViewModel for profile and repository coordination.
- Use `sealed interface` MVI state/effects for the read-aloud state machine:
  `Idle -> Ready -> Listening -> Evaluating -> Correct -> NeedsCorrection -> Review`.
- Use Compose Navigation with a single-activity host.
- Use `WindowSizeClass` and adaptive grids, with separate one-handed phone and two-pane tablet layouts.
- Keep lesson runtime state recoverable across process death.

## Offline-First Data

- Bundle the MVP curriculum and approved audio assets with the APK or split install delivery.
- Store curriculum JSON in versioned, validated resources.
- Import content into Room for local progress or keep the JSON as the source and Room as runtime cache.
- Use repository APIs that can later accept remote content.
- Cache optional cloud assets only after a successful download and integrity check.

## Core Data Model

```json
{
  "schemaVersion": 1,
  "units": [
    {
      "id": "uyirmei-ka",
      "kind": "uyirmei",
      "text": "க",
      "baseConsonant": "க்",
      "vowelSign": "அ",
      "nativeSegments": ["க்", "அ"],
      "pronunciationKey": "ka",
      "imageKey": "kaar",
      "audioAssetKey": "unit_uyirmei_k",
      "difficulty": 1,
      "tags": ["early", "common"]
    }
  ],
  "words": [
    {
      "id": "word-kai",
      "text": "கை",
      "transliteration": "kai",
      "nativeSegments": [],
      "syllables": ["கை"],
      "difficulty": 1,
      "englishMeaning": "hand",
      "tamilMeaning": "",
      "imageKey": "hand",
      "audioAssetKey": "word_kai",
      "exampleSentence": null
    }
  ]
}
```

The segmentation fields must be produced by the tested grapheme utility, not hand-maintained. Review Tamil fields requiring linguistic judgment with a native teacher.

## TTS and Audio

**Recommendation:**

1. Pre-record all MVP letter, uyirmei, syllable, and feedback audio with a native Tamil speaker.
2. Use Android `TextToSpeech` with `ta-IN` as the fallback for full words and sentences.
3. Add a provider interface (`System`, `NeuralAsset`, future `Sarvam`/Google/Azure) and route asset-backed items before TTS.
4. Generate supplemental words/sentences only after native-speaker review.

**Trade-offs:** pre-recorded assets provide the most consistent child-facing pronunciation but require larger delivery assets and manual review. Neural/cloud providers can cover expansion faster but introduce cost, connectivity, and privacy concerns. Live TTS remains a fallback and is not acceptable as the sole source for isolated Tamil units.

## Speech Recognition

**MVP recommendation:** Android `SpeechRecognizer` behind a provider interface, plus a controlled comparison layer.

Accept the MVP recognizer result only after normalization, then compare it with the target using Tamil grapheme units and configurable edit/tolerance thresholds. Preserve an in-app `I said it` self-check fallback for low-confidence or no-network cases.

Evaluate `Google Cloud Speech-to-Text` and a local/quantized Indic model only after a pilot dataset of child voices is available. A cloud model may improve accuracy but increases network and privacy requirements; on-device models reduce latency and data transfer but need device/storage benchmarks.

Do not expose raw recognition audio or logs unless explicitly configured and consented.

## Tamil Segmentation

Use the Unicode grapheme-break algorithm plus a validated Tamil-specific decomposition pipeline. An initial implementation will use `java.text.BreakIterator`/Unicode grapheme clusters, normalize text, and require a separate dictionary of linguistically approved letter and syllable units.

Tamil segmentation rules must be teacher-reviewed; the app should not infer every syllable boundary solely from Unicode code points or a generic library.

Tests will cover:

- Combining marks and vowel signs
- Base consonant + vowel sign
- Pulli/consonant clusters
- Grantha or special characters where in scope
- Edge cases and normalization

## Learning Engine

Keep scoring deterministic and auditable:

- Track per-unit outcomes: attempts, successes, failures, lapses, and next review time.
- Use minimum accuracy and configurable unlock thresholds.
- Use short spaced-repetition intervals on the first pass and longer intervals after successes.
- Ease difficulty after repeated misses and raise it only after a success streak.
- Never harshly penalize retries or use monetized lives.

Room should store append-only attempt records and derived progress state. The adaptive engine should accept a snapshot and return a result without directly depending on Android or UI classes.

## Security and Families Safety

- PIN-gate parent settings and profiles.
- Keep cloud features opt-in and collect only progress information needed for the experience.
- No ads, trackers, or third-party analytics in the first release.
- Prepare an account-free profile model and an explicit parent/teacher privacy notice.
- Obtain legal and Families-policy review before any account, cloud recording, or analytics feature.

## Testing Baseline

- JVM tests for segmentation, scoring, unlock rules, and spaced repetition.
- Instrumentation tests for Room migrations and repositories.
- Compose UI tests for correction, navigation, and window-size behavior.
- Speech and audio tests using recorded Tamil pilot samples.
- Accessibility checks for TalkBack, contrast, touch targets, and text scaling.

## Alternatives Considered

| Alternative | Why not the first recommendation |
|---|---|
| Flutter | Cross-platform benefits are limited for a native Android-only MVP; platform speech/TTS integration and low-end Android behavior are still needed. |
| Pure Compose MVI | MVI is suitable for complex state machines but would add avoidable ceremony to CRUD features. MVVM remains the default outside read-aloud. |
| Cloud-only speech/TTS | Violates the offline-first baseline and may expose children's audio unnecessarily. |
| One global Room JSON curriculum blob | Impedes migration, validation, and per-unit progress tooling. |

## Assumptions

- Android-only first release, with phones and tablets.
- Tamil UI and English helper text.
- Indian Tamil is the default audio dialect until the user chooses otherwise.
- The MVP is free and ad-free.
- Core lessons, assets, recognition fallback, and progress work offline.
- Multiple child profiles are local to the device.

## Open Questions

1. Does “fully offline” include speech recognition, or is limited cloud recognition acceptable?
2. Is Indian Tamil or Sri Lankan Tamil the expected spoken standard?
3. Who owns and approves the native-speaker recordings and curriculum?
4. Should profiles use a shared parent PIN or separate per-profile PINs?
5. Are A/B and subscription models excluded, or only ads?
