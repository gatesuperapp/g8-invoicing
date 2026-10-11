package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.product_categories_color_picker_title
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import org.jetbrains.compose.resources.stringResource

/**
 * 10-swatch palette for product categories. Deliberately mid-saturation (not
 * flat pastels): the first version used Material-50/100 pastels which washed
 * out against each other once more than three categories existed. These
 * sit around Material-200/300 so each swatch keeps its identity while
 * still reading as a muted colour chip (not a flashy status pill). First
 * entry is the default — categories created without a user-picked colour
 * fall back to this at render time (DB stores color=0 as "unset").
 */
val CategoryPastelPalette: List<Long> = listOf(
    0xFF64B5F6L, // blue (default)
    0xFFF06292L, // pink
    0xFF9575CDL, // purple
    0xFF81C784L, // green
    0xFFFF8A65L, // orange
    0xFFFFD54FL, // amber
    0xFFE57373L, // red
    0xFF4DB6ACL, // teal
    0xFFBA68C8L, // magenta
    0xFF90A4AEL, // slate
)

const val CATEGORY_COLOR_UNSET: Long = 0L

/**
 * Resolves the ARGB stored in DB to the Compose Color used for rendering.
 * [CATEGORY_COLOR_UNSET] (0) → first palette entry.
 */
fun resolveCategoryColor(stored: Long): Color =
    if (stored == CATEGORY_COLOR_UNSET) Color(CategoryPastelPalette.first())
    else Color(stored)

/**
 * Modal picker — 10 pastel swatches in a 5×2 grid. Tap selects + dismisses.
 */
@Composable
fun CategoryColorPickerDialog(
    currentColor: Long,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(AppColors.surface, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    // Same 18sp dialog-title size the AppDialog shells use, so
                    // this picker reads as a sibling modal (was textScreenTitle
                    // default which came in heavier than the surrounding UI).
                    text = stringResource(Res.string.product_categories_color_picker_title),
                    style = MaterialTheme.typography.textScreenTitle.copy(fontSize = 18.sp),
                    color = AppColors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Column(
                    modifier = Modifier.padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val rows = CategoryPastelPalette.chunked(5)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            row.forEach { argb ->
                                val isSelected = argb == currentColor ||
                                    (currentColor == CATEGORY_COLOR_UNSET && argb == CategoryPastelPalette.first())
                                CategorySwatch(
                                    color = Color(argb),
                                    selected = isSelected,
                                    onClick = {
                                        onPick(argb)
                                        onDismiss()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    // Selected ring tracks the theme so it stays legible on every swatch:
    // near-black in light mode, off-white in dark. Pure white on light
    // disappeared on pastel yellows; pure white in dark reads fine.
    val selectedRingColor = if (AppColors.isDark) Color.White else Color.Black
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) selectedRingColor else AppColors.divider,
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
    )
}

/**
 * Inline colour square used as a prefix on each row of the category
 * management screen. Click opens the [CategoryColorPickerDialog].
 */
@Composable
fun CategoryColorSquare(
    color: Long,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(resolveCategoryColor(color))
            .border(1.dp, AppColors.divider, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .height(24.dp),
    )
}
