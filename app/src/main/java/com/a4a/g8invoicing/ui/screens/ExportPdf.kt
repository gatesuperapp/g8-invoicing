package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@Composable
fun ExportPdf() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Black)
    ) {
        Row {
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.export_close)
            )
        }
        Text(
            text = stringResource(R.string.export_ongoing),
            style = MaterialTheme.typography.headlineLarge
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .aspectRatio(1f / 1.414f)
                .background(Color.White)
        ) {
            ProgressBar()
        }

    }
}

@Composable
fun ProgressBar() {
    var loading by remember { mutableStateOf(false) }

    Button(onClick = { loading = true }, enabled = !loading) {
        Text("Start loading")
    }

    if (!loading) return

    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}