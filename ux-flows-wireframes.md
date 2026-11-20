# UX Flows and Wireframes

## UX Principles

- Lead with **listen, look, say, act** rather than text instructions.
- Keep one primary action per screen.
- Use the same action, sound, picture, and reward language throughout a lesson.
- Highlight only the current target; avoid flashing or rapidly moving effects.
- Provide a visual fallback for every audio cue and a text fallback for every image.
- Use large touch targets and high contrast.
- Never present retries as failure or make a missed answer remove rewards.

## Shared Navigation

### Phone

- Single-column navigation.
- Bottom navigation: **Learn**, **Practice**, **Rewards**, **Parent**.
- Back, close, and progress controls remain in the top app bar.
- The parent area requires the profile PIN.

### Tablet

- Navigation rail or left learning map.
- Split lesson workspace with content at left and practice guidance at right.
- Read-aloud text stays in the primary pane and correction controls remain visible beside it.
- Use one-handed thumb-safe bottom actions in portrait and landscape.

## Onboarding

```text
Phone                              Tablet
+----------------------+           +------------------+------------------+
| [Logo]               |           | Learn Tamil      | Family setup     |
|                      |           |                  |                  |
| Welcome              |           | [illustration]   | 1. Add child      |
| Choose language      |           |                  | 2. Daily goal     |
| [Tamil] [English]    |           |                  | 3. Tamil use      |
|                      |           |                  |                  |
| [Add child]          |           |                  | [Continue]       |
+----------------------+           +------------------+------------------+
```

### Flow

1. Choose UI language.
2. Explain microphone use and ask for parent permission.
3. Add one child profile: name/label, age band, and current Tamil exposure.
4. Set a 5-20 minute daily goal.
5. Run a short, optional sound or letter baseline.
6. Enter the learning path.

Default settings should remain usable if the child skips the baseline.

## Child Profile and Avatar

```text
+------------------------------+
| Who is learning?             |
|                              |
| [A] Aun    [A] Appa?         |
| [New profile]                |
|                              |
| [Add sticker/avatar]         |
+------------------------------+
```

Profiles stay local. The avatar is chosen after setup and can be changed from the profile menu.

## Learning Path

```text
+------------------------------+
| [Streak] 4   [XP] 320        |
|                              |
|  [Goal progress] 8/10 min    |
|                              |
|          (Group 3)           |
|             [lesson]         |
|          /                  |
|      [done]  --  [done]      |
|                              |
|  [Current lesson]            |
|  "Listen and blend"          |
|  [Start]                     |
|                              |
|  [locked] Group 4            |
+------------------------------+
```

Rules:

- Show no more than 7 visible lesson nodes.
- Completed nodes use stars; current nodes use one clear action; locked nodes explain the prerequisite.
- A lock icon is paired with text and audio, not icon-only.
- Daily progress is visible without interrupting the lesson.

## Lesson Screen

```text
Phone
+------------------------------+
| X   Unit 3/8       [2 hearts]|
| [progress bar]               |
|                              |
|       [big picture]          |
|                              |
|     "Listen"  [sound]        |
|                              |
| [choice] [choice] [choice]   |
|                              |
|          [Check]             |
+------------------------------+
```

Lesson interactions:

1. The current model is introduced with picture, spoken unit, and action.
2. A short exercise is presented.
3. Correct answer: one celebration, then next.
4. Wrong answer: gentle hint, support action, retry.
5. Repeated miss: mark for review and continue.

No modal is used for an ordinary retry. Use an inline message with a **Listen again** action.

## Trace Screen

```text
+------------------------------+
| Trace: க                     |
|                              |
|  [animated letter outline]   |
|  [finger tracing canvas]     |
|                              |
| [Hear]  [Clear]  [Check]     |
+------------------------------+
```

For low-level devices, reduce the stroke animation frame count and pre-render the letter outline.

## Read-Aloud Screen

```text
Phone
+--------------------------------+
| X   Read aloud       [Settings]|
| [word] [word] [TARGET] [word]  |
|                                |
|       [tap target to hear]     |
|                                |
| [Mic on - say the sentence]    |
| [Try again] [I said it]        |
+--------------------------------+

Tablet
+----------------------+----------------------+
| Sentence             | Support              |
| [word] [word]        | [big target]         |
| [TARGET] [word]      | [picture]            |
|                      | [hear] [split]       |
| [progress]           | [try] [I said it]    |
+----------------------+----------------------+
```

### Read-Aloud Flow

1. Highlight the current word and play it once.
2. Start recording with an obvious, accessible microphone state.
3. Normalize recognition text and identify target mismatches by grapheme clusters.
4. If accurate, advance and celebrate briefly.
5. If uncertain, offer **Try again**, **I said it**, or **Hear the word**.
6. If incorrect, open the correction state.

## Letter-by-Letter Correction

```text
+------------------------------+
| Let's break it apart         |
|                              |
|   [க]  [ை]                   |
|                              |
| Hear each part:              |
| [க] [play]   [ை] [play]     |
|                              |
| [Blend] --> [கை]             |
|                              |
| [Try the whole word]         |
+------------------------------+
```

The app:

1. Splits the word through the grapheme-aware utility.
2. Highlights one unit at playback.
3. Plays units slowly with their approved unit audio.
4. Blends the units in sequence with approved whole-word audio.
5. Reopens the microphone and asks for another attempt.

After the configurable retry limit, mark the item for spaced review but keep the child in control.

## Picture Exercise

```text
+------------------------------+
| "Which one is மாமா?"         |
|                              |
| [picture A] [picture B]      |
| [picture C] [picture D]      |
|                              |
| [Hear again]                 |
+------------------------------+
```

Every picture exercise has an audio prompt and a visible label in Tamil and optionally English.

## Reward Screen

```text
+------------------------------+
| Great practice!              |
|                              |
|  [avatar]                    |
|  +20 XP   2 stars            |
|                              |
| [sticker]                    |
|                              |
| [Continue] [View sticker]    |
+------------------------------+
```

Rewards:

- Show XP and stars immediately.
- Give a random collectible only after the lesson is complete.
- Keep the shop free and non-monetary.
- Do not hide practice content behind extra payment or failed days.

## Parent Dashboard

```text
+------------------------------------+
| PIN   [cancel] [unlock]            |
+------------------------------------+

+-----------------------------------------------+
| Aun                   [PIN] [Settings]        |
| Today 12/15 min    Streak 4                  |
|                                               |
| Letters mastered 18/250    Needs practice 6   |
| [line chart]           [unit heat map]       |
|                                               |
| Weak items                                    |
|  கி  ம  ரு   [Listen] [Practice]              |
|                                               |
| [Set goal] [Manage profiles] [Export report]  |
+-----------------------------------------------+
```

Tablet:

```text
+-----------------------+------------------------+
| Profiles              | Selected child          |
| [Aun] [Maya] [+]      | 7 days / 45 minutes     |
|                       | mastered / weak chart   |
|                       | unit list               |
|                       | goal / export controls  |
+-----------------------+------------------------+
```

## Settings

- UI language
- Audio speed
- Sound and haptics
- High contrast and text scaling
- Microphone mode
- Offline audio
- Daily reminder time
- PIN
- Data export and deletion

All settings should expose the same behavior in both phone and tablet layouts.

## Accessibility

- Minimum 48dp touch targets, ideally 56dp in child screens.
- 4.5:1 contrast for meaningful text and 3:1 for large text and controls.
- TalkBack labels for Tamil symbols and for pictures.
- Avoid requiring drag-only interaction; provide an equivalent tap sequence.
- Provide text and picture alternatives for sound prompts.
- Respect system text scaling up to the tested maximum.
- Never communicate correctness by color alone.
- Use reduced-motion variants for every celebration.

## Analytics and Privacy UX

- The parent view explains exactly what progress is stored.
- Cloud speech is disabled until the parent enables it.
- Microphone permission is requested only immediately before recording.
- Exported reports contain the selected child's progress only.
- The app provides a visible delete/reset action.
