package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.ui.states.EmailState
import com.a4a.g8invoicing.ui.theme.inputField

data class EmailListInput(
    val emails: List<EmailState>,
    val placeholder: String = "",
    val onAddEmail: (String) -> Unit = {},
    val onRemoveEmail: (Int) -> Unit = {},
    val maxEmails: Int = 4
)

@Composable
fun FormInputCreatorEmailList(
    input: EmailListInput,
) {
    var newEmailText by remember { mutableStateOf("") }
    val nonEmptyEmails = input.emails.filter { it.email.text.isNotEmpty() }
    val canAddMore = nonEmptyEmails.size < input.maxEmails

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Show existing emails as chips (Column + Row instead of FlowRow for KMP compatibility)
        if (nonEmptyEmails.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                nonEmptyEmails.forEachIndexed { index, emailState ->
                    EmailChip(
                        text = emailState.email.text,
                        onRemoveClick = { input.onRemoveEmail(index) }
                    )
                }
            }
        }

        // Input field for adding new email
        if (canAddMore) {
            BasicTextField(
                value = newEmailText,
                onValueChange = { newEmailText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (nonEmptyEmails.isNotEmpty()) 4.dp else 0.dp),
                textStyle = MaterialTheme.typography.inputField,
                cursorBrush = SolidColor(Color.Black),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newEmailText.isNotBlank() && isValidEmail(newEmailText)) {
                            input.onAddEmail(newEmailText.trim())
                            newEmailText = ""
                        }
                    }
                ),
                decorationBox = { innerTextField ->
                    if (newEmailText.isEmpty()) {
                        Text(
                            text = input.placeholder,
                            style = MaterialTheme.typography.inputField
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun EmailChip(
    text: String,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            lineHeight = 16.sp,
        )
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Supprimer $text",
            modifier = Modifier
                .padding(start = 4.dp)
                .size(18.dp)
                .clickable { onRemoveClick() },
            tint = Color.Gray
        )
    }
}

private fun isValidEmail(email: String): Boolean {
    // Simple email validation
    return email.contains("@") && email.contains(".")
}
