package com.luxkasTech.PaintresLumiere.core.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Text produced outside the UI (typically by a ViewModel) that is only resolved to a String when
 * rendered, so ViewModels never need a `Context` to pick a localized message.
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText

    class Resource(@StringRes val id: Int, vararg val args: Any) : UiText {
        override fun equals(other: Any?) =
            other is Resource && other.id == id && other.args.contentEquals(args)

        override fun hashCode() = 31 * id + args.contentHashCode()
    }

    @Composable
    fun asString(): String = when (this) {
        is Raw -> value
        is Resource -> stringResource(id, *args)
    }
}
