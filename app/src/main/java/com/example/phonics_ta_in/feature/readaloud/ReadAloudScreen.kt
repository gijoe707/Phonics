package com.example.phonics_ta_in.feature.readaloud

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val STORIES_LIBRARY = listOf(
    ReadAloudStory(
        id = "story_crow_pitcher",
        titleTamil = "காகமும் தண்ணீரும்",
        titleEnglish = "The Thirsty Crow",
        gradeLevel = "Grade 1",
        iconEmoji = "🐦",
        description = "A clever crow uses pebbles to raise the water level in a pot.",
        sentences = listOf(
            ReadAloudSentence("s1_1", "ஒரு தாகமுள்ள காகம் தண்ணீரைத் தேடியது", listOf("ஒரு", "தாகமுள்ள", "காகம்", "தண்ணீரைத்", "தேடியது"), "A thirsty crow searched for water"),
            ReadAloudSentence("s1_2", "அது ஒரு பானையில் கொஞ்சம் தண்ணீரைக் கண்டது", listOf("அது", "ஒரு", "பானையில்", "கொஞ்சம்", "தண்ணீரைக்", "கண்டது"), "It found a little water in a pot"),
            ReadAloudSentence("s1_3", "தண்ணீரை எட்ட சிறிய கற்களைப் போட்டது", listOf("தண்ணீரை", "எட்ட", "சிறிய", "கற்களைப்", "போட்டது"), "It dropped small pebbles to reach the water"),
            ReadAloudSentence("s1_4", "தண்ணீர் மேலே வந்தது காகம் மகிழ்ந்து குடித்தது", listOf("தண்ணீர்", "மேலே", "வந்தது", "காகம்", "மகிழ்ந்து", "குடித்தது"), "Water rose up and the crow drank happily"),
            ReadAloudSentence("s1_5", "உழைப்பும் புத்திசாலித்தனமும் எப்போதும் வெற்றி தரும்", listOf("உழைப்பும்", "புத்திசாலித்தனமும்", "எப்போதும்", "வெற்றி", "தரும்"), "Hard work and intelligence always bring success")
        )
    ),
    ReadAloudStory(
        id = "story_hare_tortoise",
        titleTamil = "முயலும் ஆமையும்",
        titleEnglish = "The Hare and the Tortoise",
        gradeLevel = "Grade 1",
        iconEmoji = "🐢",
        description = "A slow and steady tortoise wins the race against a speedy hare.",
        sentences = listOf(
            ReadAloudSentence("s2_1", "ஒரு காட்டில் முயலும் ஆமையும் வாழ்ந்தன", listOf("ஒரு", "காட்டில்", "முயலும்", "ஆமையும்", "வாழ்ந்தன"), "A hare and a tortoise lived in a forest"),
            ReadAloudSentence("s2_2", "இரண்டும் சேர்ந்து ஓட்டப்பந்தயம் வைத்தன", listOf("இரண்டும்", "சேர்ந்து", "ஓட்டப்பந்தயம்", "வைத்தன"), "Both held a running race together"),
            ReadAloudSentence("s2_3", "முயல் வேகமாக ஓடி வழியில் தூங்கியது", listOf("முயல்", "வேகமாக", "ஓடி", "வழியில்", "தூங்கியது"), "The hare ran fast and slept on the way"),
            ReadAloudSentence("s2_4", "ஆமை மெதுவாக நடந்து வெற்றி பெற்றது", listOf("ஆமை", "மெதுவாக", "நடந்து", "வெற்றி", "பெற்றது"), "The tortoise walked slowly and won"),
            ReadAloudSentence("s2_5", "பொறுமையும் விடாமுயற்சியும் வெற்றியைத் தரும்", listOf("பொறுமையும்", "விடாமுயற்சியும்", "வெற்றியைத்", "தரும்"), "Patience and perseverance bring victory")
        )
    ),
    ReadAloudStory(
        id = "story_lion_mouse",
        titleTamil = "சிங்கமும் சுண்டெலியும்",
        titleEnglish = "The Lion and the Mouse",
        gradeLevel = "Grade 2",
        iconEmoji = "🦁",
        description = "A tiny mouse helps a mighty lion escape from a hunter's net.",
        sentences = listOf(
            ReadAloudSentence("s3_1", "ஒரு பெரிய சிங்கம் அடர்ந்த காட்டில் தூங்கியது", listOf("ஒரு", "பெரிய", "சிங்கம்", "அடர்ந்த", "காட்டில்", "தூங்கியது"), "A big lion slept in a dense forest"),
            ReadAloudSentence("s3_2", "ஒரு சிறிய சுண்டெலி சிங்கத்தின் மேல் ஓடியது", listOf("ஒரு", "சிறிய", "சுண்டெலி", "சிங்கத்தின்", "மேல்", "ஓடியது"), "A small mouse ran over the lion"),
            ReadAloudSentence("s3_3", "சிங்கம் கோபத்துடன் எலியைப் பிடித்தது", listOf("சிங்கம்", "கோபத்துடன்", "எலியைப்", "பிடித்தது"), "The lion angrily caught the mouse"),
            ReadAloudSentence("s3_4", "எலி பயந்து மன்னிப்புக் கேட்டு தப்பியது", listOf("எலி", "பயந்து", "மன்னிப்புக்", "கேட்டு", "தப்பியது"), "The mouse begged for mercy and escaped"),
            ReadAloudSentence("s3_5", "சில நாட்கள் கழித்து சிங்கம் வலையில் சிக்கியது", listOf("சில", "நாட்கள்", "கழித்து", "சிங்கம்", "வலையில்", "சிக்கியது"), "Days later the lion got trapped in a net"),
            ReadAloudSentence("s3_6", "எலி தன் கூர்மையான பற்களால் வலையைக் கடித்தது", listOf("எலி", "தன்", "கூர்மையான", "பற்களால்", "வலையைக்", "கடித்தது"), "The mouse bit the net with sharp teeth"),
            ReadAloudSentence("s3_7", "சிங்கம் விடுதலையாகி எலிக்கு நன்றி கூறியது", listOf("சிங்கம்", "விடுதலையாகி", "எலிக்கு", "நன்றி", "கூறியது"), "The lion became free and thanked the mouse")
        )
    ),
    ReadAloudStory(
        id = "story_fox_grapes",
        titleTamil = "நரியும் திராட்சைப் பழமும்",
        titleEnglish = "The Fox and the Grapes",
        gradeLevel = "Grade 2",
        iconEmoji = "🦊",
        description = "A hungry fox learns that making excuses doesn't satisfy hunger.",
        sentences = listOf(
            ReadAloudSentence("s4_1", "ஒரு பசியுள்ள நரி அழகான தோட்டத்திற்கு வந்தது", listOf("ஒரு", "பசியுள்ள", "நரி", "அழகான", "தோட்டத்திற்கு", "வந்தது"), "A hungry fox came to a beautiful garden"),
            ReadAloudSentence("s4_2", "அங்கே கொடியில் சுவையான திராட்சைப் பழங்கள் தொங்கின", listOf("அங்கே", "கொடியில்", "சுவையான", "திராட்சைப்", "பழங்கள்", "தொங்கின"), "Delicious grapes were hanging on the vine there"),
            ReadAloudSentence("s4_3", "நரி பலமுறை உயரமாகக் குதித்துப் பார்த்தது", listOf("நரி", "பலமுறை", "உயரமாகக்", "குதித்துப்", "பார்த்தது"), "The fox jumped high many times"),
            ReadAloudSentence("s4_4", "பழங்களை எட்ட முடியாமல் நரி களைப்படைந்தது", listOf("பழங்களை", "எட்ட", "முடியாமல்", "நரி", "களைப்படைந்தது"), "Unable to reach the fruit the fox grew tired"),
            ReadAloudSentence("s4_5", "இந்த திராட்சைப் பழங்கள் புளிக்கும் என்று கூறிச் சென்றது", listOf("இந்த", "திராட்சைப்", "பழங்கள்", "புளிக்கும்", "என்று", "கூறிச்", "சென்றது"), "It left saying these grapes are sour")
        )
    ),
    ReadAloudStory(
        id = "story_clever_crow",
        titleTamil = "அறிவாளி காகமும் தந்திர நரியும்",
        titleEnglish = "The Clever Crow and Sly Fox",
        gradeLevel = "Grade 3",
        iconEmoji = "🧀",
        description = "A smart crow outsmarts a flattering fox attempting to steal its food.",
        sentences = listOf(
            ReadAloudSentence("s5_1", "ஒரு காகம் மரக்கிளையில் அமர்ந்து வடை தின்றது", listOf("ஒரு", "காகம்", "மரக்கிளையில்", "அமர்ந்து", "வடை", "தின்றது"), "A crow sat on a branch eating a vada"),
            ReadAloudSentence("s5_2", "ஒரு தந்திரமான நரி அந்த வடையைப் பறிக்க நினைத்தது", listOf("ஒரு", "தந்திரமான", "நரி", "அந்த", "வடையைப்", "பறிக்க", "நினைத்தது"), "A sly fox wanted to snatch that vada"),
            ReadAloudSentence("s5_3", "நரி காகத்தின் இனிமையான குரலைப் புகழ்ந்து பேசியது", listOf("நரி", "காகத்தின்", "இனிமையான", "குரலைப்", "புகழ்ந்து", "பேசியது"), "The fox praised the crow's sweet voice"),
            ReadAloudSentence("s5_4", "எனக்காக ஒரு பாட்டுப் பாடு என்று நரி கேட்டது", listOf("எனக்காக", "ஒரு", "பாட்டுப்", "பாடு", "என்று", "நரி", "கேட்டது"), "The fox asked it to sing a song"),
            ReadAloudSentence("s5_5", "காகம் வடையைக் காலடியில் கெட்டியாகப் பிடித்துக் கொண்டது", listOf("காகம்", "வடையைக்", "காலடியில்", "கெட்டியாகப்", "பிடித்துக்", "கொண்டது"), "The crow firmly held the vada under its foot"),
            ReadAloudSentence("s5_6", "காகம் தைரியமாக கா கா என்று சத்தமிட்டது", listOf("காகம்", "தைரியமாக", "கா", "கா", "என்று", "சத்தமிட்டது"), "The crow cawed bravely kraa kraa"),
            ReadAloudSentence("s5_7", "நரியின் தந்திரம் தோற்று ஏமாற்றத்துடன் ஓடியது", listOf("நரியின்", "தந்திரம்", "தோற்று", "ஏமாற்றத்துடன்", "ஓடியது"), "The fox's trick failed and it ran away disappointed")
        )
    )
)

@Composable
fun ReadAloudScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenLesson: ((String) -> Unit)? = null,
    readingPosition: Int = 0,
    onReadingPositionChange: (Int) -> Unit = {}
) {
    var selectedStory by rememberSaveable(stateSaver = androidx.compose.runtime.saveable.Saver(
        save = { it?.id },
        restore = { id -> STORIES_LIBRARY.find { it.id == id } }
    )) { mutableStateOf<ReadAloudStory?>(null) }

    var gradeFilter by remember { mutableStateOf("All") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FD))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (selectedStory != null) {
                    selectedStory = null
                } else {
                    onBack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedStory != null) selectedStory!!.titleTamil else "தமிழ் கதைகள் (Tamil Stories)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4)
                )
                Text(
                    text = if (selectedStory != null) selectedStory!!.titleEnglish else "Select a story for Grade 1 - 3 practice",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedStory == null) {
            // Grade Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val grades = listOf("All", "Grade 1", "Grade 2", "Grade 3")
                grades.forEach { grade ->
                    val isSelected = gradeFilter == grade
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF6750A4) else Color(0xFFEADDFF),
                        modifier = Modifier.clickable { gradeFilter = grade }
                    ) {
                        Text(
                            text = grade,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF21005D),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredStories = remember(gradeFilter) {
                if (gradeFilter == "All") STORIES_LIBRARY else STORIES_LIBRARY.filter { it.gradeLevel == gradeFilter }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredStories, key = { it.id }) { story ->
                    StoryCard(
                        story = story,
                        onClick = { selectedStory = story }
                    )
                }
            }
        } else {
            ReadAlongPlayer(
                sentences = selectedStory!!.sentences,
                onFinished = {
                    onReadingPositionChange(0)
                    selectedStory = null
                },
                onOpenLesson = onOpenLesson,
                initialPosition = readingPosition,
                onPositionChanged = onReadingPositionChange,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}

@Composable
private fun StoryCard(
    story: ReadAloudStory,
    onClick: () -> Unit
) {
    val gradeColor = when (story.gradeLevel) {
        "Grade 1" -> Color(0xFF2E7D32)
        "Grade 2" -> Color(0xFF1565C0)
        else -> Color(0xFF6750A4)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color(0xFFF3EDF7)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(story.iconEmoji, fontSize = 32.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = gradeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = story.gradeLevel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = gradeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "${story.sentences.size} sentences",
                        fontSize = 12.sp,
                        color = Color(0xFF79747E)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = story.titleTamil,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )

                Text(
                    text = story.titleEnglish,
                    fontSize = 14.sp,
                    color = Color(0xFF6750A4),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = story.description,
                    fontSize = 13.sp,
                    color = Color(0xFF49454F)
                )
            }
        }
    }
}
