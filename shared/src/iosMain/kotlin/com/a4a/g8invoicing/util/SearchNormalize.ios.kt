package com.a4a.g8invoicing.util

import platform.Foundation.NSDiacriticInsensitiveSearch
import platform.Foundation.NSCaseInsensitiveSearch
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.currentLocale
import platform.Foundation.stringByFoldingWithOptions

actual fun String.normalizeForSearch(): String =
    (this as NSString).stringByFoldingWithOptions(
        options = NSDiacriticInsensitiveSearch or NSCaseInsensitiveSearch,
        locale = NSLocale.currentLocale
    )
