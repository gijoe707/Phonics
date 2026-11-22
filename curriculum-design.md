# Curriculum Design

## Curriculum Principles

- Teach **sound and meaning before letter-name terminology**.
- Introduce small sets that are easy to discriminate visually and auditorily.
- Blend only units that the child has learned.
- Pair every unit with a native-speaker audio asset and a clear picture keyword.
- Expose grapheme boundaries to the learner as syllable units, not as individual Unicode code points.
- Mark pronunciation and grouping decisions as **teacher review required** until approved.

## Two Ways to Learn (both valid)

| Mode | How it works |
|---|---|
| **Locked learning path** (Duolingo-style) | An ordered path that follows the teaching groups below. A node unlocks when the previous one is finished; the current node is highlighted; each node shows stars. |
| **Free jumping** | Vowel and consonant grids, quiz, stories, custom reader and direct "Practice" jumps. Anything can be opened at any time. No locks. |

Rules:

- Free jumping is never blocked by the path. Locks apply only inside the locked path.
- Both modes work fully offline and save to the same active profile (stars, streak, study time, completed letters).
- Proposed, to confirm: a letter finished through free jumping also counts toward the path, so a child is never forced to repeat it.

## Initial Tamil Inventory

| Type | Characters |
|---|---|
| Uyir vowels | அ, ஆ, இ, ஈ, உ, ஊ, எ, ஏ, ஐ, ஒ, ஓ, ஔ |
| Mei consonants | க், ங், ச், ஞ், ட், ண், த், ந், ப், ம், ய், ர், ல், வ், ழ், ள், ற், ன் |
| Aytham | ஃ |
| Uyirmei patterns | Each mei consonant combined with vowel signs, excluding or including forms that the native-teacher reviewer approves |

## Proposed Letter-Sound Teaching Groups

The order below prioritizes common, easy-to-distinguish units and usable picture keywords. It is a starting sequence, not a linguistic claim; a native Tamil curriculum reviewer must approve sound distinctions and words.

| Group | Units | Teaching purpose | Suggested keyword examples |
|---|---|---|---|
| 1 | அ, ஆ | Short/long vowel contrast | அன்னம்/approved object; ஆ |
| 2 | க், க | Common consonant and basic அ combination | கடல் or approved keyword |
| 3 | த், த | Common consonant and basic அ combination | தலை or approved keyword |
| 4 | ப், ப | Common consonant and basic அ combination | பல் or approved keyword |
| 5 | ம், ம | Common consonant and basic அ combination | மலை or approved keyword |
| 6 | இ, ஈ, இ, ஈ + earlier consonants | Vowel contrast in learned uyirmei | கி/கீ, தி/தீ, பி/பீ, மி/மீ |
| 7 | ச், ச; ட், ட | Add two contrast sets | approved keyword per unit |
| 8 | எ, ஏ, ஐ | Short/long/diphthong vowel patterns | reviewed keyword sets |
| 9 | உ, ஊ | Vowel contrast | கு/கூ and comparable forms |
| 10 | ஒ, ஓ, ஔ | Remaining long/open vowel patterns | reviewed keyword sets |
| 11 | ந், ண், ந, ண | Common nasal distinction | teacher-reviewed examples |
| 12 | ஞ், ஞ; ங், ங | Less frequent or visually demanding units | teacher-reviewed examples |
| 13 | ல், ல; ள், ள; ழ், ழ; ற், ற | Distinct r/l-series sounds | teacher-reviewed examples |
| 14 | ர், ர; ய், ய; வ், வ | Remaining common consonants | teacher-reviewed examples |
| 15 | ன், ன | Remaining consonant | teacher-reviewed examples |
| 16 | ஃ | Aytham | use only after all regular unit types and with specialist review |

**Prerequisite rule (applies to the locked path only; free jumping stays open):** Uyirmei forms do not unlock until the child demonstrates the relevant mei and vowel-sign units. Some vowel signs change the meaning and pronunciation of a consonant; native-speaker review is mandatory.

## Five-Skill Lesson Sequence

Each unit uses the same 8-12 minute structure:

1. **Hear and see** - warm, slow audio plus a culturally relevant picture.
2. **Say and act** - short action and native-speaker chant.
3. **Listen and choose** - hear the unit and select the matching picture or symbol.
4. **Trace** - animated stroke order, finger tracing, and completion tolerance.
5. **Blend** - join known units in approved syllables or words.
6. **Find the sound** - locate the unit in a spoken-word prompt.
7. **Recall** - mini spaced-repetition check.
8. **Reward** - stars only; no harsh failure state.

### Letter Formation

- Use animated stroke order and a finger-trace validator.
- A completed trace can be accepted from a stroke even if it differs slightly from the template.
- Do not gate reading mastery solely on perfect handwriting; require the formation activity but treat it as an optional objective for motor-skill development.

### Tricky Words

Maintain a separate `trickyWords` list because Tamil compound letters and irregular pronunciations require content-specific rules. A tricky word should include:

- Display text
- Grapheme units
- Approved syllable split
- Audio asset
- Review reason
- Native-teacher review status

## Exercise Mix

| Exercise | Default share | Minimum skill coverage |
|---|---|---|
| Listen + pick picture | 20% | Discrimination and meaning |
| See picture + pick word/unit | 15% | Recognition |
| Match unit to picture | 10% | Association |
| Drag units to build word | 15% | Blending and order |
| Listen + tap first unit | 10% | Starting-sound identification |
| Trace | 10% | Formation |
| Say word/unit | 15% | Speech and correction |
| Fill missing unit | 5% | Recall |

Rotate without repeating the same format more than twice consecutively. Every lesson should include listening, a picture, a recall prompt, and at least one active practice item.

## Progressive Levels

| Level | Content | Exit target |
|---|---|---|
| 1 | Vowels and first mei/uyirmei units | 80% across 10 required units |
| 2 | Two-unit words built from mastered sounds | 80% accuracy across 12 words |
| 3 | Three-unit words and common compounds | 80% accuracy across 15 words |
| 4 | Longer words and approved compound patterns | 80% accuracy across 18 words |
| 5 | Short phrases | 80% accuracy across 12 phrases |
| 6 | Short sentences | 80% accuracy across 10 sentences |
| 7 | Short stories and comprehension | 80% comprehension and reading accuracy |

The thresholds and required exercise counts are configuration values. Defaults:

```json
{
  "accuracyThreshold": 0.80,
  "minimumAttempts": 5,
  "newItemsPerSession": 3,
  "maxFailuresBeforeEaseOff": 3,
  "successStreakForStepUp": 5,
  "retryLimitBeforeMarkReview": 3,
  "reviewLapseLimit": 2
}
```

## Adaptive Engine Rules

1. Schedule every mastered or new unit as a review card.
2. Failed items return in the same session once and enter a shorter review interval afterward.
3. After 3 consecutive failures, reduce the next prompt count and add a listen/blend support step.
4. After 5 consecutive successful attempts for an item, extend the interval and allow a harder word.
5. Remove an item from the active weak list only after a successful review; do not mark it mastered after one success.
6. Keep adaptation explainable to the parent dashboard and configurable for classroom use.

No child should lose XP or require payment for retries.

## 200-300 Word Bank Plan

Create 200-300 approved child-friendly words in equal progression rather than one size-fits-all list.

| Stage | Suggested count | Example use |
|---|---|---|
| Level 1 | 30-40 | Learned vowels and basic uyirmei |
| Level 2 | 40-50 | Two-unit words |
| Level 3 | 50-60 | Three-unit words |
| Level 4 | 40-50 | Longer/compound patterns |
| Level 5 | 20-30 | Phrases |
| Level 6 | 20-30 | Sentences |
| Level 7 | 10-20 | Story passages |

Each bank item requires approved Tamil text, transliteration, segment split, meaning, image asset, audio asset, difficulty, and teacher-review status. Example strings must not be treated as validated curriculum content until native review.

## Content Schema

```json
{
  "schemaVersion": 1,
  "locale": "ta-IN",
  "reviewStatus": "draft",
  "units": [
    {
      "id": "unit-ka",
      "kind": "uyirmei",
      "text": "க",
      "baseConsonant": "க்",
      "vowelSign": "அ",
      "tamilSegments": ["க்", "அ"],
      "graphemeClusters": ["க"],
      "syllables": ["க"],
      "transliteration": "ka",
      "meaningTamil": "",
      "meaningEnglish": "",
      "keywordImage": "content/images/ka.webp",
      "audio": {
        "unit": "content/audio/ka-unit.wav",
        "word": null,
        "feedback": null
      },
      "difficulty": 1,
      "prerequisites": ["vowel-a"],
      "teacherReview": "pending"
    }
  ],
  "words": [
    {
      "id": "word-example",
      "text": "",
      "transliteration": "",
      "graphemeClusters": [],
      "syllables": [],
      "difficulty": 1,
      "meaningEnglish": "",
      "meaningTamil": "",
      "image": "content/images/example.webp",
      "audio": "content/audio/example.wav",
      "exampleSentence": null,
      "prerequisites": [],
      "tags": [],
      "tricky": false,
      "teacherReview": "pending"
    }
  ],
  "lessons": [
    {
      "id": "lesson-group-1",
      "title": "",
      "unitIds": [],
      "prerequisites": [],
      "exerciseTemplateIds": [],
      "unlock": {
        "accuracy": 0.80,
        "requiredExercises": 5
      }
    }
  ],
  "thresholdProfiles": {
    "default": {
      "accuracyThreshold": 0.80,
      "minimumAttempts": 5,
      "newItemsPerSession": 3,
      "maxFailuresBeforeEaseOff": 3,
      "successStreakForStepUp": 5,
      "retryLimitBeforeMarkReview": 3
    }
  }
}
```

## Data Validation

Reject content when:

- Required audio or image is missing.
- Tamil text is not NFC-normalized.
- Grapheme clusters disagree with the segmentation utility.
- A unit has no native-teacher review status.
- A lesson references an unknown prerequisite or unit.
- A word or sentence has a transliteration but no approved meaning.

## Items Requiring Native Review

- Complete teaching order for mei and uyirmei.
- Short/long vowel and consonant sound distinctions.
- Keyword words for every unit.
- The meaning of the aytham placeholder and teaching sequence.
- Grantha, compound, and tricky-word coverage.
- Dialect choice and voice direction.
- Stroke and syllable segmentation rules.

## Implementation Status

| Area | Design | Current build |
|---|---|---|
| Lesson | 8 steps | 5 steps: precursor sound, listening test, reading test, tracing, speech test |
| Learning order | 16 groups, locked unlocking | Free jumping built (letter grids, quiz, stories, custom reader); locked path still to build |
| Levels | 7 levels: units, words, phrases, sentences, stories | Quiz levels 1-7+ by word length; Grade 1-3 stories; phrase and sentence levels not built |
| Exercise mix | 8 exercise types by share | Partly built; audit against the table pending |
| Word bank | 200-300 approved words with image and audio | Tamil dictionary repository grouped by grapheme length; count and assets to be checked |
| Tricky words | Separate reviewed list | Not built |
| Teacher review | Required for all units and words | Pending |
| Content storage | Versioned, validated JSON | Defined in Kotlin |
| Adaptive engine | Spaced repetition and ease-off rules | Not built |

## Open Questions

1. Should the first course start with vowels or with a mixed vowel/consonant sequence?
2. Does the family prefer Indian, Sri Lankan, or a neutral modern Tamil recording style?
3. What motor-skill level should count as successful tracing?
4. Are there classroom or curriculum standards that the lesson sequence must follow?
5. Should the initial 200-300 word bank include exact numbers by difficulty, or should content expansion happen after a pilot?
