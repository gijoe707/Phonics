package com.example.phonics_ta_in.core.curriculum

import com.example.phonics_ta_in.core.text.TamilTextSegmenter

/**
 * A structured dictionary entry for Tamil vocabulary words.
 */
data class DictionaryWord(
    val wordTamil: String,
    val meaningTamil: String,
    val meaningEnglish: String,
    val transliteration: String,
    val illustration: String = "",
    val audioKey: String = ""
) {
    /**
     * Exact visual Tamil grapheme count (e.g., "அம்மா" -> [அ, ம், மா] -> 3 graphemes)
     */
    val graphemeCount: Int
        get() = TamilTextSegmenter.segmentGraphemes(wordTamil).size

    /**
     * Maps grapheme count directly to difficulty level (1, 2, 3, 4, 5, 6, 7+)
     */
    val difficultyLevel: Int
        get() = when (graphemeCount) {
            1 -> 1
            2 -> 2
            3 -> 3
            4 -> 4
            5 -> 5
            6 -> 6
            else -> 7
        }
}

/**
 * Master dictionary repository categorizing words by length-based difficulty levels.
 */
object TamilDictionaryRepository {

    val ALL_WORDS = listOf(
        // =====================================================================================
        // LEVEL 1: 1-Letter Words (ஓரெழுத்து ஒருமொழி)
        // =====================================================================================
        DictionaryWord("ஆ", "பசு / காளை", "Cow / Bull", "aa", "goat", "aa_sound"),
        DictionaryWord("ஈ", "ஈ (பூச்சி) / கொடு", "Fly / Give", "ii", "fly", "ii_sound"),
        DictionaryWord("கை", "உடலின் கை", "Hand", "kai", "", "kai_sound"),
        DictionaryWord("தீ", "நெருப்பு", "Fire", "thii", "", "thii_sound"),
        DictionaryWord("பூ", "மலர்", "Flower", "poo", "", "poo_sound"),
        DictionaryWord("கோ", "அரசன் / பசு", "King / Cow", "ko", "", "ko_sound"),
        DictionaryWord("வா", "வருக", "Come", "vaa", "", "vaa_sound"),
        DictionaryWord("தை", "தமிழ் மாதம் / தையல்", "Thai Month / Stitch", "thai", "", "thai_sound"),
        DictionaryWord("மை", "எழுது மை", "Ink", "mai", "", "mai_sound"),
        DictionaryWord("பை", "பொருட்கள் வைக்கும் பை", "Bag", "pai", "", "pai_sound"),
        DictionaryWord("தா", "தருவாயாக", "Give", "thaa", "", "thaa_sound"),
        DictionaryWord("போ", "செல்க", "Go", "po", "", "po_sound"),
        DictionaryWord("நீ", "முன்னிலை நபர்", "You", "nee", "", "nee_sound"),
        DictionaryWord("நோ", "துன்பம் / நோய்", "Pain / Illness", "no", "", "no_sound"),
        DictionaryWord("தூ", "தூய்மை / வெண்மை", "Pure / White", "thoo", "", "thoo_sound"),

        // =====================================================================================
        // LEVEL 2: 2-Letter Words
        // =====================================================================================
        DictionaryWord("கண்", "விழி / பார்வை உறுப்பு", "Eye", "kan", "eye", "kan_sound"),
        DictionaryWord("கல்", "பாறைத்துண்டு", "Stone", "kal", "stone", "kal_sound"),
        DictionaryWord("இலை", "மரத்தின் இலை", "Leaf", "ilai", "leaf", "ilai_sound"),
        DictionaryWord("எலி", "சிறிய விலங்கு", "Mouse / Rat", "eli", "mouse", "eli_sound"),
        DictionaryWord("ஆடு", "கால்நடை விலங்கு", "Goat", "aadu", "goat", "aadu_sound"),
        DictionaryWord("மழை", "வான் மழை", "Rain", "mazhai", "rain", "mazhai_sound"),
        DictionaryWord("மீன்", "நீர் வாழ் உயிரி", "Fish", "meen", "fish", "meen_sound"),
        DictionaryWord("ஆறு", "நதி / எண் 6", "River / Number 6", "aaru", "river", "aaru_sound"),
        DictionaryWord("ஏணி", "ஏறும் சாதனம்", "Ladder", "eni", "ladder", "eni_sound"),
        DictionaryWord("பால்", "வெண்மைப் பானம்", "Milk", "paal", "", "paal_sound"),
        DictionaryWord("கால்", "உடலின் உறுப்பு", "Leg", "kaal", "", "kaal_sound"),
        DictionaryWord("மண்", "தரை மண்", "Soil / Earth", "mann", "", "mann_sound"),
        DictionaryWord("ஊசி", "தைக்கும் ஊசி", "Needle", "oosi", "needle", "oosi_sound"),
        DictionaryWord("ஆமை", "நீர்-நில வாழ் உயிரி", "Tortoise", "aamai", "tortoise", "aamai_sound"),
        DictionaryWord("யானை", "பெரிய காடு விலங்கு", "Elephant", "yaanai", "elephant", "yaanai_sound"),
        DictionaryWord("பானை", "மண்பாண்டம்", "Pot", "paanai", "pot", "paanai_sound"),
        DictionaryWord("கிளி", "பச்சைப் பறவை", "Parrot", "kili", "parrot", "kili_sound"),
        DictionaryWord("வீடு", "வாழும் இல்லம்", "House / Home", "veedu", "", "veedu_sound"),
        DictionaryWord("காடு", "மரங்கள் நிறைந்த வனம்", "Forest", "kaadu", "", "kaadu_sound"),
        DictionaryWord("மாடு", "கால்நடை மாடு", "Cow / Cattle", "maadu", "", "maadu_sound"),
        DictionaryWord("நரி", "காட்டு நரி", "Fox", "nari", "", "nari_sound"),
        DictionaryWord("புலி", "வரிப் புலி", "Tiger", "puli", "", "puli_sound"),
        DictionaryWord("வழி", "செல்லும் பாதை", "Way / Path", "vazhi", "", "vazhi_sound"),
        DictionaryWord("ஒலி", "சத்தம் / ஓசை", "Sound", "oli", "", "oli_sound"),
        DictionaryWord("கூடு", "பறவைக் கூடு", "Nest", "koodu", "", "koodu_sound"),

        // =====================================================================================
        // LEVEL 3: 3-Letter Words
        // =====================================================================================
        DictionaryWord("அம்மா", "தாய் / அன்னை", "Mother", "ammaa", "mother_baby", "ammaa_sound"),
        DictionaryWord("அணில்", "மர அணில்", "Squirrel", "anil", "squirrel", "anil_sound"),
        DictionaryWord("அம்பு", "வில் அம்பு", "Arrow", "ambu", "arrow", "ambu_sound"),
        DictionaryWord("உரல்", "தானியம் இடிக்கும் சாதனம்", "Mortar", "ural", "mortar", "ural_sound"),
        DictionaryWord("கரடி", "காட்டு விலங்கு", "Bear", "karadi", "bear", "karadi_sound"),
        DictionaryWord("கடல்", "பெரிய நீர்நிலை", "Sea / Ocean", "kadal", "sea", "kadal_sound"),
        DictionaryWord("மரம்", "பசுமை மரம்", "Tree", "maram", "tree", "maram_sound"),
        DictionaryWord("பந்து", "விளையாட்டுப் பந்து", "Ball", "panthu", "ball", "panthu_sound"),
        DictionaryWord("ஈட்டி", "எறியும் ஆயுதம்", "Spear", "eetti", "spear", "eetti_sound"),
        DictionaryWord("ஆந்தை", "இரவுப் பறவை", "Owl", "aanthai", "owl", "aanthai_sound"),
        DictionaryWord("சங்கு", "கடல் சங்கு", "Conch", "sangu", "conch", "sangu_sound"),
        DictionaryWord("சட்டை", "அணியும் உடை", "Shirt", "sattai", "shirt", "sattai_sound"),
        DictionaryWord("தட்டு", "உணவுத் தட்டு", "Plate", "thattu", "plate", "thattu_sound"),
        DictionaryWord("நத்தை", "மெது ஊர் உயிரி", "Snail", "naththai", "snail", "naththai_sound"),
        DictionaryWord("தவளை", "நீர்நிலத் தாவும் உயிரி", "Frog", "thavalai", "frog", "thavalai_sound"),
        DictionaryWord("குதிரை", "வேகமாக ஓடும் விலங்கு", "Horse", "kuthirai", "", "kuthirai_sound"),
        DictionaryWord("காகம்", "கறுப்புப் பறவை", "Crow", "kaakam", "crow", "kaakam_sound"),
        DictionaryWord("மேகம்", "வானத்து மேகம்", "Cloud", "maegam", "", "maegam_sound"),
        DictionaryWord("வானம்", "நீல ஆகாயம்", "Sky", "vaanam", "", "vaanam_sound"),
        DictionaryWord("தாமரை", "தேசிய மலர்", "Lotus", "thaamarai", "", "thaamarai_sound"),

        // =====================================================================================
        // LEVEL 4: 4-Letter Words
        // =====================================================================================
        DictionaryWord("கப்பல்", "கடல் பிரயாணக் கலம்", "Ship", "kappal", "ship", "kappal_sound"),
        DictionaryWord("அன்னம்", "அழகிய வெண் பறவை", "Swan", "annam", "swan", "annam_sound"),
        DictionaryWord("ஊஞ்சல்", "ஆடும் ஊஞ்சல்", "Swing", "oonjal", "swing", "oonjal_sound"),
        DictionaryWord("பட்டம்", "காற்றில் பறக்கும் பட்டம்", "Kite", "pattam", "kite", "pattam_sound"),
        DictionaryWord("தங்கம்", "விலையுயர்ந்த பொன்", "Gold", "thangam", "gold", "thangam_sound"),
        DictionaryWord("வட்டம்", "வட்ட வடிவம்", "Circle", "vattam", "circle", "vattam_sound"),
        DictionaryWord("வளையல்", "கையில் அணியும் வளையல்", "Bangle", "valaiyal", "bangle", "valaiyal_sound"),
        DictionaryWord("சிங்கம்", "காட்டின் ராஜா", "Lion", "singam", "", "singam_sound"),
        DictionaryWord("விமானம்", "வானூர்தி", "Airplane", "vimaanam", "", "vimaanam_sound"),
        DictionaryWord("நூலகம்", "புத்தகச் சேமிப்பகம்", "Library", "noolakam", "", "noolakam_sound"),
        DictionaryWord("தக்காளி", "சிவப்புக் காய்கறி", "Tomato", "thakkaali", "", "thakkaali_sound"),
        DictionaryWord("மாணவர்", "கல்வி கற்கும் நபர்", "Student", "maanavar", "", "maanavar_sound"),
        DictionaryWord("ஆப்பிள்", "இனிப்பான சுவை பழம்", "Apple", "aappil", "apple", "aappil_sound"),

        // =====================================================================================
        // LEVEL 5: 5-Letter Words
        // =====================================================================================
        DictionaryWord("ஒட்டகம்", "பாலைவன விலங்கு", "Camel", "ottakam", "camel", "ottakam_sound"),
        DictionaryWord("சக்கரம்", "சுழலும் உருளை", "Wheel", "sakkaram", "wheel", "sakkaram_sound"),
        DictionaryWord("ஆலமரம்", "பெரிய நிழல் தரும் மரம்", "Banyan Tree", "aalamaram", "banyan_tree", "aalamaram_sound"),
        DictionaryWord("உண்டியல்", "பணம் சேமிக்கும் பெட்டி", "Piggy Bank", "undiyal", "piggy_bank", "undiyal_sound"),
        DictionaryWord("வெங்காயம்", "சமையல் காய்கறி", "Vengayam", "vengaayam", "", "vengaayam_sound"),
        DictionaryWord("வகுப்பறை", "பாடம் கற்கும் அறை", "Classroom", "vakupparai", "", "vakupparai_sound"),

        // =====================================================================================
        // LEVEL 6: 6-Letter Words
        // =====================================================================================
        DictionaryWord("தொலைக்காட்சி", "ஒளிபரப்புப் பெட்டி", "Television", "tholaikkaatchi", "", "tholaikkaatchi_sound"),
        DictionaryWord("வளையல்கள்", "பல வளையல்கள்", "Bangles (Plural)", "valaiyalkal", "bangle", "valaiyalkal_sound"),
        DictionaryWord("ஆப்பிள்கள்", "பல ஆப்பிள் பழங்கள்", "Apples (Plural)", "aappilkal", "apple", "aappilkal_sound"),
        DictionaryWord("தொடர்வண்டி", "ரயில் வண்டி", "Train", "thodarvandi", "", "thodarvandi_sound"),
        DictionaryWord("மாணவர்கள்", "பல மாணவர்கள்", "Students (Plural)", "maanavarkal", "", "maanavarkal_sound"),
        DictionaryWord("நீர்வீழ்ச்சி", "மலையிலிருந்து விழும் அருவி", "Waterfall", "neerveezhchchi", "", "neerveezhchchi_sound"),
        DictionaryWord("வாழைப்பழம்", "மஞ்சள் நிறப் பழம்", "Banana", "vaazhaippazham", "", "vaazhaippazham_sound"),
        DictionaryWord("செம்பருத்தி", "சிவப்பு மலர்", "Hibiscus", "semparuththi", "", "semparuththi_sound"),

        // =====================================================================================
        // LEVEL 7+: 7+ Letter Words
        // =====================================================================================
        DictionaryWord("நட்சத்திரம்", "வானில் ஒளிரும் விண்மீன்", "Star", "natchaththiram", "star", "natchaththiram_sound"),
        DictionaryWord("வண்ணத்துப்பூச்சி", "அழகிய சிறகு பூச்சி", "Butterfly", "vannaththuppoochchi", "", "butterfly_sound"),
        DictionaryWord("உருளைக்கிழங்கு", "உணவு நிலக்காய்", "Potato", "urulaikkilangu", "", "potato_sound"),
        DictionaryWord("புத்தகங்கள்", "பல நூல்கள்", "Books (Plural)", "puththakangal", "", "puththakangal_sound"),
        DictionaryWord("மருத்துவமனை", "நோய் தீர்க்கும் இடம்", "Hospital", "maruththuvamanai", "", "maruththuvamanai_sound"),
        DictionaryWord("பல்கலைக்கழகம்", "உயர் கல்வி நிறுவனம்", "University", "palkalaikkazhakam", "", "palkalaikkazhakam_sound"),
        DictionaryWord("எலுமிச்சைப்பழம்", "புளிப்புச் சுவை பழம்", "Lemon", "elumichchaippazham", "", "elumichchaippazham_sound")
    )

    /**
     * Retrieves all words matching the specified difficulty level (1 to 7).
     * If requested level has fewer than 3 words, returns words closest to that level.
     */
    fun getWordsByLevel(level: Int): List<DictionaryWord> {
        val targetLevel = level.coerceIn(1, 7)
        val matches = ALL_WORDS.filter { it.difficultyLevel == targetLevel }
        return if (matches.isNotEmpty()) matches else ALL_WORDS.filter { it.difficultyLevel <= targetLevel }
    }

    /**
     * Generates 3 distractor choices for a multiple-choice meaning question.
     */
    fun getDistractorsForWord(target: DictionaryWord, count: Int = 3): List<DictionaryWord> {
        return ALL_WORDS.filter { it.wordTamil != target.wordTamil }
            .shuffled()
            .take(count)
    }

    /**
     * Retrieves overall dictionary count by level.
     */
    fun getWordCountByLevel(): Map<Int, Int> {
        return (1..7).associateWith { level ->
            getWordsByLevel(level).size
        }
    }

    /**
     * Dynamically picks a few random words from EACH difficulty level (1 to 7)
     * to construct a balanced, continuous test session progressing from Level 1 to Level 7+.
     */
    fun generateRandomTestSet(wordsPerLevel: Int = 2): List<DictionaryWord> {
        val testList = mutableListOf<DictionaryWord>()
        for (lvl in 1..7) {
            val levelPool = getWordsByLevel(lvl).shuffled()
            testList.addAll(levelPool.take(wordsPerLevel))
        }
        return testList
    }
}
