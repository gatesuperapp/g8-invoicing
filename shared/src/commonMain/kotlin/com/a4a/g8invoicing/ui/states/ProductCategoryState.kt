package com.a4a.g8invoicing.ui.states

/**
 * User-defined product category (2.1 premium module "product_categories").
 * [color] is a packed ARGB int; 0 means "use the default pastel blue at
 * render time" so the exact hex isn't frozen into the DB rows the first
 * time the user adds a category.
 */
data class ProductCategoryState(
    val id: Int? = null,
    val name: String = "",
    val color: Long = 0L,
    val sortOrder: Int = 0,
)
