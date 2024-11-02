package com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug


import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp


@Immutable
class ShapesFork(
    // ShapesFork None and Full are omitted as None is a RectangleShape and Full is a CircleShape.
    val extraSmall: CornerBasedShape = ShapeDefaults.ExtraSmall,
    val small: CornerBasedShape = ShapeDefaults.Small,
    val medium: CornerBasedShape = ShapeDefaults.Medium,
    val large: CornerBasedShape = ShapeDefaults.Large,
    val extraLarge: CornerBasedShape = ShapeDefaults.ExtraLarge,
) {
    /** Returns a copy of this ShapesFork, optionally overriding some of the values. */
    fun copy(
        extraSmall: CornerBasedShape = this.extraSmall,
        small: CornerBasedShape = this.small,
        medium: CornerBasedShape = this.medium,
        large: CornerBasedShape = this.large,
        extraLarge: CornerBasedShape = this.extraLarge,
    ): ShapesFork = ShapesFork(
        extraSmall = extraSmall,
        small = small,
        medium = medium,
        large = large,
        extraLarge = extraLarge,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShapesFork) return false
        if (extraSmall != other.extraSmall) return false
        if (small != other.small) return false
        if (medium != other.medium) return false
        if (large != other.large) return false
        if (extraLarge != other.extraLarge) return false
        return true
    }

    override fun hashCode(): Int {
        var result = extraSmall.hashCode()
        result = 31 * result + small.hashCode()
        result = 31 * result + medium.hashCode()
        result = 31 * result + large.hashCode()
        result = 31 * result + extraLarge.hashCode()
        return result
    }

    override fun toString(): String {
        return "ShapesFork(" +
                "extraSmall=$extraSmall, " +
                "small=$small, " +
                "medium=$medium, " +
                "large=$large, " +
                "extraLarge=$extraLarge)"
    }
}

/**
 * Contains the default values used by [ShapesFork]
 */
/**
 * Contains the default values used by [Shapes]
 */
object ShapeDefaults {
    /** Extra small sized corner shape */
    val ExtraSmall: CornerBasedShape = com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeTokens.CornerExtraSmall

    /** Small sized corner shape */
    val Small: CornerBasedShape = com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeTokens.CornerSmall

    /** Medium sized corner shape */
    val Medium: CornerBasedShape = com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeTokens.CornerMedium

    /** Large sized corner shape */
    val Large: CornerBasedShape = com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeTokens.CornerLarge

    /** Extra large sized corner shape */
    val ExtraLarge: CornerBasedShape = com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeTokens.CornerExtraLarge
}
/** Helper function for component shape tokens. Used to grab the top values of a shape parameter. */
internal fun CornerBasedShape.top(): CornerBasedShape {
    return copy(bottomStart = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

/**
 * Helper function for component shape tokens. Used to grab the bottom values of a shape parameter.
 */
internal fun CornerBasedShape.bottom(): CornerBasedShape {
    return copy(topStart = CornerSize(0.0.dp), topEnd = CornerSize(0.0.dp))
}

/** Helper function for component shape tokens. Used to grab the start values of a shape parameter. */
internal fun CornerBasedShape.start(): CornerBasedShape {
    return copy(topEnd = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

/** Helper function for component shape tokens. Used to grab the end values of a shape parameter. */
internal fun CornerBasedShape.end(): CornerBasedShape {
    return copy(topStart = CornerSize(0.0.dp), bottomStart = CornerSize(0.0.dp))
}

/**
 * Helper function for component shape tokens. Here is an example on how to use component color
 * tokens:
 * ``MaterialTheme.shapes.fromToken(FabPrimarySmallTokens.ContainerShape)``
 */
internal fun Shapes.fromToken(value: com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens): Shape {
    return when (value) {
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerExtraLarge -> extraLarge
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerExtraLargeTop -> extraLarge.top()
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerExtraSmall -> extraSmall
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerExtraSmallTop -> extraSmall.top()
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerFull -> CircleShape
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerLarge -> large
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerLargeEnd -> large.end()
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerLargeTop -> large.top()
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerMedium -> medium
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerNone -> RectangleShape
        com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.CornerSmall -> small
    }
}

/**
 * Converts a shape token key to the local shape provided by the theme
 * The color is subscribed to [LocalShapes] changes
 */
internal val com.a4a.g8invoicing.ui.shared.bottomSheetScaffoldWithoutBug.ShapeKeyTokens.value: Shape
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes.fromToken(this)

/** CompositionLocal used to specify the default shapes for the surfaces. */
internal val LocalShapes = staticCompositionLocalOf { ShapesFork() }
