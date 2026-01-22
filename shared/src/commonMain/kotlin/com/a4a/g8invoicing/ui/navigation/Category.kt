package com.a4a.g8invoicing.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.*
import org.jetbrains.compose.resources.StringResource

sealed class Category(
    val route: String,
    val resourceId: StringResource,
    val icon: ImageVector?,
    val iconDescription: String?,
) {
    data object Clients : Category(Screen.ClientOrIssuerList.name, Res.string.appbar_client_list, null, null)
    data object Products : Category(Screen.ProductList.name, Res.string.appbar_products, null, null)
    data object Invoices : Category(Screen.InvoiceList.name, Res.string.appbar_invoices, null, null)
    data object CreditNotes : Category(Screen.CreditNoteList.name, Res.string.appbar_credit_notes, null, null)
    data object DeliveryNotes : Category(Screen.DeliveryNoteList.name, Res.string.appbar_delivery_notes, null, null)
    data object G8 : Category(Screen.About.name, Res.string.drawer_g8, null, null)
}
