package com.a4a.g8invoicing

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.a4a.g8invoicing.ui.MainCompose
import com.a4a.g8invoicing.ui.states.InvoiceState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gives more detail on the "A resource failed to call close." error
        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )

        // Compulsory for the bottom sheet modal to not overlap native navbar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MainCompose(
                onSendReminder = { invoice ->
                    sendReminderEmail(invoice)
                }
            )
        }
    }

    private fun sendReminderEmail(invoice: InvoiceState) {
        try {
            val clientEmails = invoice.documentClient?.emails
                ?.map { it.email.text }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

            val documentNumber = invoice.documentNumber.text
            val totalAmount = invoice.documentTotalPrices?.totalPriceWithTax
                ?.toStringExpanded() ?: "0"
            val dueDate = invoice.dueDate

            val subject = getString(R.string.send_reminder_email_subject, documentNumber)
            val body = getString(R.string.send_reminder_email_content, documentNumber, totalAmount, dueDate)

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, clientEmails.toTypedArray())
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            // On Android 11+, resolveActivity() returns null due to package visibility restrictions
            // Use startActivity directly with try-catch instead
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            // No email app available - silently ignore
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}






