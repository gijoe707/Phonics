package com.example.phonics_ta_in.core.curriculum

import java.text.Normalizer
import com.example.phonics_ta_in.core.text.TamilTextSegmenter
import kotlin.math.abs

object CurriculumRepository {

    private const val PULLI = "\u0BCD"

    // --- Single-Letter Words with Explicit Tamil Meanings (ஓரெழுத்து ஒருமொழி) ---
    val SINGLE_LETTER_AA = SingleLetterWord("ஆ", "பசு", "Cow / Bull", "aa")
    val SINGLE_LETTER_II = SingleLetterWord("ஈ", "ஈ (பூச்சி)", "Fly / Give", "ii")
    val SINGLE_LETTER_KAI = SingleLetterWord("கை", "உடலின் கை", "Hand", "kai")
    val SINGLE_LETTER_KO = SingleLetterWord("கோ", "அரசன் / பசு", "King / Cow", "ko")
    val SINGLE_LETTER_THII = SingleLetterWord("தீ", "நெருப்பு", "Fire", "thii")
    val SINGLE_LETTER_POO = SingleLetterWord("பூ", "மலர்", "Flower", "poo")
    val SINGLE_LETTER_VAA = SingleLetterWord("வா", "வருக", "Come", "vaa")

    private fun w(
        tamil: String, english: String, start: String, translit: String,
        syllables: List<String>, illustration: String
    ) = StartingSoundWord(tamil, english, start, translit, syllables, illustration)

    // =====================================================================================
    // WORD BANK  (illustration = key of the drawn picture in WordIllustrations.kt)
    // =====================================================================================

    // --- Vowel words ---
    val WORD_AMMA = w("அம்மா", "Mother", "அ", "ammaa", listOf("அம்", "மா"), "mother_baby")
    val WORD_ANNAM = w("அன்னம்", "Swan", "அ", "annam", listOf("அன்", "னம்"), "swan")
    val WORD_ANIL = w("அணில்", "Squirrel", "அ", "anil", listOf("அ", "ணில்"), "squirrel")
    val WORD_AMBU = w("அம்பு", "Arrow", "அ", "ambu", listOf("அம்", "பு"), "arrow")

    val WORD_AADU = w("ஆடு", "Goat", "ஆ", "aadu", listOf("ஆ", "டு"), "goat")
    val WORD_AALAMARAM = w("ஆலமரம்", "Banyan Tree", "ஆ", "aalamaram", listOf("ஆ", "ல", "ம", "ரம்"), "banyan_tree")
    val WORD_AAMAI = w("ஆமை", "Tortoise", "ஆ", "aamai", listOf("ஆ", "மை"), "tortoise")
    val WORD_AANTHAI = w("ஆந்தை", "Owl", "ஆ", "aanthai", listOf("ஆன்", "தை"), "owl")
    val WORD_AAPPIL = w("ஆப்பிள்", "Apple", "ஆ", "aappil", listOf("ஆப்", "பிள்"), "apple")

    val WORD_ILAI = w("இலை", "Leaf", "இ", "ilai", listOf("இ", "லை"), "leaf")
    val WORD_IDLI = w("இட்லி", "Idli", "இ", "idli", listOf("இட்", "லி"), "idli")

    val WORD_II = w("ஈ", "Fly", "ஈ", "ii", listOf("ஈ"), "fly")
    val WORD_EETTI = w("ஈட்டி", "Spear", "ஈ", "eetti", listOf("ஈட்", "டி"), "spear")

    val WORD_URAL = w("உரல்", "Mortar", "உ", "ural", listOf("உ", "ரல்"), "mortar")
    val WORD_UNDIYAL = w("உண்டியல்", "Piggy bank", "உ", "undiyal", listOf("உண்", "டி", "யல்"), "piggy_bank")

    val WORD_OONJAL = w("ஊஞ்சல்", "Swing", "ஊ", "oonjal", listOf("ஊஞ்", "சல்"), "swing")
    val WORD_OOSI = w("ஊசி", "Needle", "ஊ", "oosi", listOf("ஊ", "சி"), "needle")

    val WORD_ERUMBU = w("எறும்பு", "Ant", "எ", "erumbu", listOf("எ", "றும்", "பு"), "ant")
    val WORD_ELI = w("எலி", "Mouse", "எ", "eli", listOf("எ", "லி"), "mouse")

    val WORD_ENI = w("ஏணி", "Ladder", "ஏ", "eni", listOf("ஏ", "ணி"), "ladder")

    val WORD_AINTHU = w("ஐந்து", "Five", "ஐ", "ainthu", listOf("ஐந்", "து"), "five")

    val WORD_OTTAKAM = w("ஒட்டகம்", "Camel", "ஒ", "ottakam", listOf("ஒட்", "ட", "கம்"), "camel")

    val WORD_ODAM = w("ஓடம்", "Boat", "ஓ", "odam", listOf("ஓ", "டம்"), "boat")

    val WORD_AUVAIYAR = w("ஔவையார்", "Avvaiyar", "ஔ", "auvaiyar", listOf("ஔ", "வை", "யார்"), "avvaiyar")

    // --- Consonant words ---
    // க் sits INSIDE these words
    val WORD_SAKKARAM = w("சக்கரம்", "Wheel", "க்", "sakkaram", listOf("சக்", "க", "ரம்"), "wheel")
    val WORD_KAAKKAI = w("காக்கை", "Crow", "க்", "kaakkai", listOf("காக்", "கை"), "crow")
    val WORD_KOKKU = w("கொக்கு", "Crane", "க்", "kokku", listOf("கொக்", "கு"), "crane")

    val WORD_KADAL = w("கடல்", "Sea", "க", "kadal", listOf("க", "டல்"), "sea")
    val WORD_KAPPAL = w("கப்பல்", "Ship", "க", "kappal", listOf("கப்", "பல்"), "ship")
    val WORD_KAN = w("கண்", "Eye", "க", "kan", listOf("கண்"), "eye")
    val WORD_KARADI = w("கரடி", "Bear", "க", "karadi", listOf("க", "ரடி"), "bear")

    private val WORD_SANGU = w("சங்கு", "Conch", "ங", "sangu", listOf("சங்", "கு"), "conch")
    private val WORD_THANGAM = w("தங்கம்", "Gold", "ங", "thangam", listOf("தங்", "கம்"), "gold")
    private val WORD_SATTAI = w("சட்டை", "Shirt", "ச", "sattai", listOf("சட்", "டை"), "shirt")
    private val WORD_NJAYIRU = w("ஞாயிறு", "Sun", "ஞ", "njaayiru", listOf("ஞா", "யி", "று"), "sun")
    private val WORD_PATTAM = w("பட்டம்", "Kite", "ட", "pattam", listOf("பட்", "டம்"), "kite")
    private val WORD_VATTAM = w("வட்டம்", "Circle", "ட", "vattam", listOf("வட்", "டம்"), "circle")
    private val WORD_MANI = w("மணி", "Bell", "ண", "mani", listOf("ம", "ணி"), "bell")
    private val WORD_PANAM = w("பணம்", "Money", "ண", "panam", listOf("ப", "ணம்"), "coin")
    private val WORD_THAVALAI = w("தவளை", "Frog", "த", "thavalai", listOf("த", "வ", "ளை"), "frog")
    private val WORD_THATTU = w("தட்டு", "Plate", "த", "thattu", listOf("தட்", "டு"), "plate")
    private val WORD_NATHTHAI = w("நத்தை", "Snail", "ந", "naththai", listOf("நத்", "தை"), "snail")
    private val WORD_NATCHATHIRAM = w("நட்சத்திரம்", "Star", "ந", "natchaththiram", listOf("நட்", "சத்", "தி", "ரம்"), "star")
    private val WORD_PANTHU = w("பந்து", "Ball", "ப", "panthu", listOf("பந்", "து"), "ball")
    private val WORD_MALAI = w("மலை", "Mountain", "ம", "malai", listOf("ம", "லை"), "mountain")
    private val WORD_MARAM = w("மரம்", "Tree", "ம", "maram", listOf("ம", "ரம்"), "tree")
    private val WORD_YAANAI = w("யானை", "Elephant", "ய", "yaanai", listOf("யா", "னை"), "elephant")
    private val WORD_KAL = w("கல்", "Stone", "ல", "kal", listOf("கல்"), "stone")
    private val WORD_VANDI = w("வண்டி", "Cart", "வ", "vandi", listOf("வண்", "டி"), "cart")
    private val WORD_MAZHAI = w("மழை", "Rain", "ழ", "mazhai", listOf("ம", "ழை"), "rain")
    private val WORD_PAZHAM = w("பழம்", "Fruit", "ழ", "pazham", listOf("ப", "ழம்"), "apple")
    private val WORD_KILI = w("கிளி", "Parrot", "ள", "kili", listOf("கி", "ளி"), "parrot")
    private val WORD_VALAIYAL = w("வளையல்", "Bangle", "ள", "valaiyal", listOf("வ", "ளை", "யல்"), "bangle")
    private val WORD_AARU = w("ஆறு", "River", "ற", "aaru", listOf("ஆ", "று"), "river")
    private val WORD_MEEN = w("மீன்", "Fish", "ன", "meen", listOf("மீன்"), "fish")
    private val WORD_PAANAI = w("பானை", "Pot", "ன", "paanai", listOf("பா", "னை"), "pot")

    // =====================================================================================
    // UNITS
    // =====================================================================================

    private fun unit(
        id: String, text: String, kind: UnitKind, translit: String,
        words: List<StartingSoundWord>, description: String,
        single: SingleLetterWord? = null
    ) = PhonicsUnit(
        id = id,
        text = text,
        kind = kind,
        transliteration = translit,
        phonicsSound = "/$translit/",
        keywordTamil = words.firstOrNull()?.wordTamil.orEmpty(),
        keywordEnglish = words.firstOrNull()?.wordEnglish.orEmpty(),
        audioKey = "${translit}_sound",
        singleLetterWord = single,
        startingWords = words,
        description = description
    )

    // --- Vowels (உயிர் எழுத்துகள்) ---
    val UNIT_A = unit("unit_a", "அ", UnitKind.UYIR, "a", listOf(WORD_AMMA, WORD_ANNAM, WORD_ANIL, WORD_AMBU), "Short vowel sound /a/")
    val UNIT_AA = unit(
        "unit_aa", "ஆ", UnitKind.UYIR, "aa",
        listOf(WORD_AADU, WORD_AALAMARAM, WORD_AAMAI, WORD_AANTHAI, WORD_AAPPIL),
        "Long vowel sound /aa/ and Single-Letter Word meaning (ஆ = Cow)", SINGLE_LETTER_AA
    )
    val UNIT_I = unit("unit_i", "இ", UnitKind.UYIR, "i", listOf(WORD_ILAI, WORD_IDLI), "Short vowel sound /i/")
    val UNIT_II = unit("unit_ii", "ஈ", UnitKind.UYIR, "ii", listOf(WORD_II, WORD_EETTI), "Long vowel sound /ii/ (ஈ = Fly)", SINGLE_LETTER_II)
    val UNIT_U = unit("unit_u", "உ", UnitKind.UYIR, "u", listOf(WORD_URAL, WORD_UNDIYAL), "Short vowel sound /u/")
    val UNIT_UU = unit("unit_uu", "ஊ", UnitKind.UYIR, "uu", listOf(WORD_OONJAL, WORD_OOSI), "Long vowel sound /uu/")
    val UNIT_E = unit("unit_e", "எ", UnitKind.UYIR, "e", listOf(WORD_ERUMBU, WORD_ELI), "Short vowel sound /e/")
    val UNIT_EE = unit("unit_ee", "ஏ", UnitKind.UYIR, "ee", listOf(WORD_ENI), "Long vowel sound /ee/")
    val UNIT_AI = unit("unit_ai", "ஐ", UnitKind.UYIR, "ai", listOf(WORD_AINTHU), "Vowel sound /ai/")
    val UNIT_O = unit("unit_o", "ஒ", UnitKind.UYIR, "o", listOf(WORD_OTTAKAM), "Short vowel sound /o/")
    val UNIT_OO = unit("unit_oo", "ஓ", UnitKind.UYIR, "oo", listOf(WORD_ODAM), "Long vowel sound /oo/")
    val UNIT_AU = unit("unit_au", "ஔ", UnitKind.UYIR, "au", listOf(WORD_AUVAIYAR), "Vowel sound /au/")

    val VOWEL_UNITS: List<PhonicsUnit> = listOf(
        UNIT_A, UNIT_AA, UNIT_I, UNIT_II, UNIT_U, UNIT_UU,
        UNIT_E, UNIT_EE, UNIT_AI, UNIT_O, UNIT_OO, UNIT_AU
    )

    // --- Consonants (மெய் + உயிர்மெய்) ---
    val UNIT_IK = unit(
        "unit_ik", "க்", UnitKind.MEI, "k",
        listOf(WORD_SAKKARAM, WORD_KAAKKAI, WORD_KOKKU), "Consonant sound /k/ with pulli"
    )
    val UNIT_KA = unit(
        "unit_ka", "க", UnitKind.UYIRMEI, "ka",
        listOf(WORD_KADAL, WORD_KAPPAL, WORD_KAN, WORD_KARADI), "Combined consonant-vowel sound /ka/"
    )

    private class ConsonantSpec(val stem: String, val translit: String, val words: List<StartingSoundWord>)

    /** (மெய் unit, உயிர்மெய் unit) for each of the 18 consonants, in Tamil alphabet order. */
    val CONSONANT_PAIRS: List<Pair<PhonicsUnit, PhonicsUnit>> by lazy {
        val specs = listOf(
            ConsonantSpec("ங", "ng", listOf(WORD_SANGU, WORD_THANGAM)),
            ConsonantSpec("ச", "ch", listOf(WORD_SATTAI, WORD_SAKKARAM)),
            ConsonantSpec("ஞ", "nj", listOf(WORD_NJAYIRU)),
            ConsonantSpec("ட", "t", listOf(WORD_PATTAM, WORD_VATTAM)),
            ConsonantSpec("ண", "n", listOf(WORD_MANI, WORD_PANAM)),
            ConsonantSpec("த", "th", listOf(WORD_THAVALAI, WORD_THATTU)),
            ConsonantSpec("ந", "nh", listOf(WORD_NATHTHAI, WORD_NATCHATHIRAM)),
            ConsonantSpec("ப", "p", listOf(WORD_PANTHU, WORD_PATTAM)),
            ConsonantSpec("ம", "m", listOf(WORD_MALAI, WORD_MARAM)),
            ConsonantSpec("ய", "y", listOf(WORD_YAANAI)),
            ConsonantSpec("ர", "r", listOf(WORD_MARAM, WORD_KARADI)),
            ConsonantSpec("ல", "l", listOf(WORD_ILAI, WORD_KAL)),
            ConsonantSpec("வ", "v", listOf(WORD_VATTAM, WORD_VANDI)),
            ConsonantSpec("ழ", "zh", listOf(WORD_MAZHAI, WORD_PAZHAM)),
            ConsonantSpec("ள", "l", listOf(WORD_KILI, WORD_VALAIYAL)),
            ConsonantSpec("ற", "r", listOf(WORD_ERUMBU, WORD_AARU)),
            ConsonantSpec("ன", "n", listOf(WORD_MEEN, WORD_PAANAI))
        )
        val others = specs.mapIndexed { i, s ->
            val mei = unit(
                "unit_mei_${i + 2}", s.stem + PULLI, UnitKind.MEI, s.translit,
                s.words, "Consonant sound /${s.translit}/ with pulli"
            )
            val uyirmei = unit(
                "unit_uyirmei_${i + 2}", s.stem, UnitKind.UYIRMEI, s.translit + "a",
                s.words, "Combined consonant-vowel sound /${s.translit}a/"
            )
            mei to uyirmei
        }
        listOf(UNIT_IK to UNIT_KA) + others
    }

    // --- Stroke Paths for Tracing (only these letters have tracing templates so far) ---
    val STROKES_A = listOf(
        listOf(
            NormalizedPoint(0.35f, 0.25f),
            NormalizedPoint(0.45f, 0.20f),
            NormalizedPoint(0.50f, 0.30f),
            NormalizedPoint(0.40f, 0.35f),
            NormalizedPoint(0.30f, 0.25f)
        ),
        listOf(
            NormalizedPoint(0.35f, 0.25f),
            NormalizedPoint(0.65f, 0.25f),
            NormalizedPoint(0.75f, 0.45f),
            NormalizedPoint(0.60f, 0.55f),
            NormalizedPoint(0.30f, 0.55f)
        ),
        listOf(
            NormalizedPoint(0.35f, 0.55f),
            NormalizedPoint(0.70f, 0.55f)
        ),
        listOf(
            NormalizedPoint(0.70f, 0.35f),
            NormalizedPoint(0.70f, 0.85f)
        )
    )

    val STROKES_AA = STROKES_A + listOf(
        listOf(
            NormalizedPoint(0.70f, 0.85f),
            NormalizedPoint(0.55f, 0.85f),
            NormalizedPoint(0.45f, 0.75f),
            NormalizedPoint(0.55f, 0.65f),
            NormalizedPoint(0.70f, 0.75f)
        )
    )

    val STROKES_KA = listOf(
        listOf(
            NormalizedPoint(0.25f, 0.25f),
            NormalizedPoint(0.75f, 0.25f)
        ),
        listOf(
            NormalizedPoint(0.75f, 0.25f),
            NormalizedPoint(0.75f, 0.75f),
            NormalizedPoint(0.50f, 0.75f),
            NormalizedPoint(0.50f, 0.45f),
            NormalizedPoint(0.25f, 0.45f),
            NormalizedPoint(0.25f, 0.85f)
        )
    )

    private fun strokesFor(unitId: String): List<List<NormalizedPoint>> = when (unitId) {
        "unit_a" -> STROKES_A
        "unit_aa" -> STROKES_AA
        "unit_ka" -> STROKES_KA
        else -> emptyList()
    }

    // =====================================================================================
    // FULL ALPHABET - 12 uyir + aytham + 18 mei + 216 uyirmei = 247 letters; each opens its own lesson
    // =====================================================================================

    private const val AYTHAM_TEXT = "\u0B83"

    /** Combining vowel signs in vowel order. அ has no sign: it is inherent in the consonant stem. */
    private val VOWEL_SIGNS = listOf(
        "", "\u0BBE", "\u0BBF", "\u0BC0", "\u0BC1", "\u0BC2",
        "\u0BC6", "\u0BC7", "\u0BC8", "\u0BCA", "\u0BCB", "\u0BCC"
    )

    /** ஃ (aytham). No example words yet; the curriculum asks for specialist review before adding any. */
    val UNIT_AYTHAM = unit(
        "unit_aytham", AYTHAM_TEXT, UnitKind.AYTHAM, "ak",
        emptyList(), "Aytham /ak/ - special letter (needs native-teacher review)"
    )

    /** Every word in the bank. Used to find example words for each combined letter. */
    val WORD_BANK: List<StartingSoundWord> by lazy {
        listOf(
            WORD_AMMA, WORD_ANNAM, WORD_ANIL, WORD_AMBU,
            WORD_AADU, WORD_AALAMARAM, WORD_AAMAI, WORD_AANTHAI, WORD_AAPPIL,
            WORD_ILAI, WORD_IDLI, WORD_II, WORD_EETTI,
            WORD_URAL, WORD_UNDIYAL, WORD_OONJAL, WORD_OOSI,
            WORD_ERUMBU, WORD_ELI, WORD_ENI, WORD_AINTHU,
            WORD_OTTAKAM, WORD_ODAM, WORD_AUVAIYAR,
            WORD_SAKKARAM, WORD_KAAKKAI, WORD_KOKKU,
            WORD_KADAL, WORD_KAPPAL, WORD_KAN, WORD_KARADI,
            WORD_SANGU, WORD_THANGAM, WORD_SATTAI, WORD_NJAYIRU,
            WORD_PATTAM, WORD_VATTAM, WORD_MANI, WORD_PANAM,
            WORD_THAVALAI, WORD_THATTU, WORD_NATHTHAI, WORD_NATCHATHIRAM,
            WORD_PANTHU, WORD_MALAI, WORD_MARAM, WORD_YAANAI,
            WORD_KAL, WORD_VANDI, WORD_MAZHAI, WORD_PAZHAM,
            WORD_KILI, WORD_VALAIYAL, WORD_AARU, WORD_MEEN, WORD_PAANAI
        )
    }

    /** Letters that are complete words on their own (ஓரெழுத்து ஒருமொழி), keyed by NFC text. */
    private val SINGLE_LETTER_WORDS: Map<String, SingleLetterWord> by lazy {
        listOf(
            SINGLE_LETTER_AA, SINGLE_LETTER_II, SINGLE_LETTER_KAI, SINGLE_LETTER_KO,
            SINGLE_LETTER_THII, SINGLE_LETTER_POO, SINGLE_LETTER_VAA
        ).associateBy { Normalizer.normalize(it.letter, Normalizer.Form.NFC) }
    }

    /**
     * Example words for a combined letter: words that START with it; if none, words that contain it.
     * Nothing is invented: a letter with no match simply has no example words yet.
     */
    private fun exampleWordsFor(
        letter: String,
        wordGraphemes: Map<StartingSoundWord, List<String>>
    ): List<StartingSoundWord> {
        val starting = WORD_BANK.filter { wordGraphemes[it]?.firstOrNull() == letter }
        if (starting.isNotEmpty()) return starting
        return WORD_BANK.filter { wordGraphemes[it]?.contains(letter) == true }
    }

    /**
     * The 18 consonant families. Each holds the mei plus all 12 uyirmei forms (13 letters).
     * The mei and the 'a' form come from [CONSONANT_PAIRS] unchanged; the other 11 forms are generated
     * as stem + vowel sign, with transliteration = consonant sound + vowel sound (e.g. k + ii = kii).
     */
    val CONSONANT_FAMILIES: List<ConsonantFamily> by lazy {
        val wordGraphemes: Map<StartingSoundWord, List<String>> = WORD_BANK.associateWith { w ->
            TamilTextSegmenter.segmentGraphemes(w.wordTamil).map { it.text }
        }

        CONSONANT_PAIRS.mapIndexed { i, (mei, aForm) ->
            val c = i + 1
            val stem = aForm.text
            val forms = VOWEL_SIGNS.indices.map { v ->
                if (v == 0) {
                    aForm
                } else {
                    val text = Normalizer.normalize(stem + VOWEL_SIGNS[v], Normalizer.Form.NFC)
                    val translit = mei.transliteration + VOWEL_UNITS[v].transliteration
                    unit(
                        id = "unit_uyirmei_${c}_$v",
                        text = text,
                        kind = UnitKind.UYIRMEI,
                        translit = translit,
                        words = exampleWordsFor(text, wordGraphemes),
                        description = "Combined consonant-vowel sound /$translit/",
                        single = SINGLE_LETTER_WORDS[text]
                    )
                }
            }
            ConsonantFamily(index = c, mei = mei, uyirmei = forms)
        }
    }

    /** All 247 letters in teaching order: 12 uyir, ஃ, then each consonant family (mei + 12 uyirmei). */
    val ALL_UNITS: List<PhonicsUnit> by lazy {
        VOWEL_UNITS + UNIT_AYTHAM + CONSONANT_FAMILIES.flatMap { it.units }
    }

    private val UNITS_BY_ID: Map<String, PhonicsUnit> by lazy { ALL_UNITS.associateBy { it.id } }

    fun getUnit(unitId: String): PhonicsUnit? = UNITS_BY_ID[unitId]

    /** Finds the unitId corresponding to a Tamil letter or grapheme cluster string. */
    fun findUnitIdForLetter(letterText: String): String? {
        if (letterText.isBlank()) return null
        val exact = ALL_UNITS.firstOrNull { it.text == letterText }
        if (exact != null) return exact.id
        val grapheme = TamilTextSegmenter.classifyGrapheme(letterText)
        if (grapheme.baseConsonant != null) {
            val baseMatch = ALL_UNITS.firstOrNull { it.text == grapheme.baseConsonant }
            if (baseMatch != null) return baseMatch.id
        }
        return null
    }

    /** @param index 1-based consonant index (க் = 1 ... ன் = 18) */
    fun getFamily(index: Int): ConsonantFamily? = CONSONANT_FAMILIES.getOrNull(index - 1)

    // =====================================================================================
    // EXERCISE BUILDER
    // =====================================================================================

    /**
     * Builds the exercise set for ONE letter (each of the 247 letters has its own):
     * precursor sound -> listening -> reading -> (writing if a tracing template exists) -> speech
     * -> (single-letter-word meaning, where the letter is itself a word).
     */
    fun getExercisesForUnit(unitId: String): List<LessonExercise> =
        getUnit(unitId)?.let { buildUnitExercises(it) } ?: emptyList()

    /**
     * Sibling letters of the same kind. Used as the wrong-answer pool of the multiple-choice tests (the child must
     * discriminate similar sounds) and as the comparison set of the acoustic speech check.
     */
    fun poolFor(unit: PhonicsUnit): List<PhonicsUnit> = when (unit.kind) {
        UnitKind.UYIR, UnitKind.AYTHAM -> VOWEL_UNITS
        UnitKind.MEI -> CONSONANT_FAMILIES.map { it.mei }
        UnitKind.UYIRMEI -> CONSONANT_FAMILIES
            .firstOrNull { family -> family.uyirmei.any { it.id == unit.id } }
            ?.uyirmei
            ?: emptyList()
    }

    private fun optionsFor(unit: PhonicsUnit, prefix: String): List<ExerciseOption> {
        val pool = poolFor(unit)
        val idx = pool.indexOfFirst { it.id == unit.id }.coerceAtLeast(0)
        val distractors = pool
            .filter { it.id != unit.id }
            .sortedBy { abs(pool.indexOf(it) - idx) }
            .take(2)
        val all = listOf(unit) + distractors
        // Rotate so the correct answer isn't always first (aytham isn't in its pool, so give it a fixed offset)
        val shift = (if (pool.any { it.id == unit.id }) idx else 1) % all.size
        val ordered = all.drop(shift) + all.take(shift)
        return ordered.mapIndexed { n, u ->
            ExerciseOption("${prefix}_${unit.id}_$n", u.text, "", isCorrect = u.id == unit.id)
        }
    }

    private fun buildUnitExercises(unit: PhonicsUnit): List<LessonExercise> {
        val list = mutableListOf<LessonExercise>()

        list += LessonExercise(
            id = "ex_${unit.id}_precursor",
            type = ExerciseType.PRECURSOR_HEAR_SOUND,
            targetUnit = unit,
            promptText = "Precursor Sound: Listen to the isolated letter sound ${unit.phonicsSound}"
        )
        list += LessonExercise(
            id = "ex_${unit.id}_listening",
            type = ExerciseType.LISTENING_SKILL_TEST,
            targetUnit = unit,
            promptText = "Skill 1 (Listening): Listen to the sound and pick the letter you hear",
            options = optionsFor(unit, "listen")
        )
        list += LessonExercise(
            id = "ex_${unit.id}_reading",
            type = ExerciseType.READING_SKILL_TEST,
            targetUnit = unit,
            promptText = "Skill 2 (Reading): Which letter makes the sound ${unit.phonicsSound}?",
            options = optionsFor(unit, "read")
        )

        val strokes = strokesFor(unit.id)
        if (strokes.isNotEmpty()) {
            list += LessonExercise(
                id = "ex_${unit.id}_writing",
                type = ExerciseType.WRITING_SKILL_TEST,
                targetUnit = unit,
                promptText = "Skill 3 (Writing): Trace the letter '${unit.text}' with your finger",
                templateStrokes = strokes
            )
        }

        list += LessonExercise(
            id = "ex_${unit.id}_speech",
            type = ExerciseType.SPEECH_PRECURSOR_TEST,
            targetUnit = unit,
            promptText = "Skill 4 (Speech): Say the letter sound ${unit.phonicsSound}, or one of the example words"
        )

        unit.singleLetterWord?.let { single ->
            list += LessonExercise(
                id = "ex_${unit.id}_single_meaning",
                type = ExerciseType.SINGLE_LETTER_MEANING_TEST,
                targetUnit = unit,
                singleLetterWord = single,
                promptText = "Single-Letter Word Meaning (ஓரெழுத்து ஒருமொழி): '${single.letter}' means ${single.meaningEnglish} (${single.meaningTamil})"
            )
        }

        return list
    }
}
