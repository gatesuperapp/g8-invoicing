package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.ui.theme.ColorVioletLight

@Composable
fun WebsiteFooter(
    prefix: String,
    linkLabel: String,
    onClickLink: () -> Unit,
) {
    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = Color.DarkGray)) { append(prefix) }
        withStyle(SpanStyle(color = ColorVioletLight, fontWeight = FontWeight.SemiBold)) {
            append(linkLabel)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.clickable { onClickLink() },
            text = annotated,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}
