package com.example.phonics_ta_in.feature.curriculum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonics_ta_in.core.curriculum.ConsonantFamily
import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import com.example.phonics_ta_in.core.curriculum.PhonicsUnit
import com.example.phonics_ta_in.core.curriculum.UnitKind

private const val GRID_COLUMNS = 4

// ---------------------------------------------------------------------------------------------
// Page 1: Vowels (12 uyir + ஃ aytham)
// ---------------------------------------------------------------------------------------------

/** Vowels on their own page. Each tile opens that letter's own exercise set. */
@Composable
fun VowelsScreen(
    onBack: () -> Unit,
    onOpenUnit: (unitId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    GridPage(
        titleTamil = "உயிர் எழுத்துகள்",
        subtitle = "Vowels - tap a sound to practise it",
        onBack = onBack,
        modifier = modifier
    ) {
        items(CurriculumRepository.VOWEL_UNITS, key = { it.id }) { unit ->
            UnitTile(unit = unit, onClick = { onOpenUnit(unit.id) })
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionLabel(titleTamil = "ஆய்த எழுத்து", subtitle = "Aytham - special letter")
        }
        item(key = CurriculumRepository.UNIT_AYTHAM.id) {
            UnitTile(
                unit = CurriculumRepository.UNIT_AYTHAM,
                onClick = { onOpenUnit(CurriculumRepository.UNIT_AYTHAM.id) }
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Page 2: Consonants (18) - a grid of grids. Each tile opens that consonant's own page.
// ---------------------------------------------------------------------------------------------

@Composable
fun ConsonantsScreen(
    onBack: () -> Unit,
    onOpenFamily: (familyIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GridPage(
        titleTamil = "மெய் எழுத்துகள்",
        subtitle = "Consonants - tap one to see all its sounds",
        onBack = onBack,
        modifier = modifier
    ) {
        items(CurriculumRepository.CONSONANT_FAMILIES, key = { it.index }) { family ->
            FamilyTile(family = family, onClick = { onOpenFamily(family.index) })
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Page 3: One consonant expanded: க் then க, கா, கி, கீ, கு, கூ, கெ, கே, கை, கொ, கோ, கௌ
// ---------------------------------------------------------------------------------------------

@Composable
fun ConsonantFamilyScreen(
    familyIndex: Int,
    onBack: () -> Unit,
    onOpenUnit: (unitId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val family = CurriculumRepository.getFamily(familyIndex)
    if (family == null) {
        // Shouldn't happen (index comes from the consonant grid); fall back instead of crashing.
        LaunchedEffect(Unit) { onBack() }
        return
    }

    GridPage(
        titleTamil = "${family.mei.text}  ·  மெய் & உயிர்மெய்",
        subtitle = "Consonant ${family.mei.phonicsSound} - ${family.units.size} sounds",
        onBack = onBack,
        modifier = modifier
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionLabel(titleTamil = "மெய்", subtitle = "Consonant with pulli")
        }
        item(key = family.mei.id) {
            UnitTile(unit = family.mei, onClick = { onOpenUnit(family.mei.id) })
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionLabel(titleTamil = "உயிர்மெய்", subtitle = "Consonant + each vowel (${family.uyirmei.size})")
        }
        items(family.uyirmei, key = { it.id }) { unit ->
            UnitTile(unit = unit, onClick = { onOpenUnit(unit.id) })
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Shared pieces
// ---------------------------------------------------------------------------------------------

@Composable
private fun GridPage(
    titleTamil: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.grid.LazyGridScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FD))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = titleTamil,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

@Composable
private fun SectionLabel(titleTamil: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 6.dp)) {
        Text(
            text = titleTamil,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFF49454F)
        )
    }
}

/** A single letter. Colour shows the kind: vowel, mei, uyirmei, aytham. */
@Composable
private fun UnitTile(
    unit: PhonicsUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (background, letterColor) = when (unit.kind) {
        UnitKind.UYIR -> Color(0xFFEADDFF) to Color(0xFF21005D)
        UnitKind.MEI -> Color(0xFFD6EEF8) to Color(0xFF0D3B66)
        UnitKind.UYIRMEI -> Color(0xFFE0F2E9) to Color(0xFF1B5E20)
        UnitKind.AYTHAM -> Color(0xFFFFE0B2) to Color(0xFFE65100)
    }
    TileCard(
        background = background,
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = unit.text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = letterColor
        )
        Text(
            text = unit.phonicsSound,
            fontSize = 11.sp,
            color = Color(0xFF49454F)
        )
    }
}

/** A consonant on the consonants page: shows the mei and how many sounds are inside. */
@Composable
private fun FamilyTile(
    family: ConsonantFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TileCard(
        background = Color(0xFFD6EEF8),
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = family.mei.text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D3B66)
        )
        Text(
            text = "${family.mei.phonicsSound} · ${family.units.size}",
            fontSize = 11.sp,
            color = Color(0xFF49454F)
        )
    }
}

@Composable
private fun TileCard(
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}
