package com.luxkasTech.PaintresLumiere.core.messages

import com.luxkasTech.PaintresLumiere.core.common.UiText

enum class ToastType { Success, Warning, Error }

/** Optional icon override; by default the icon follows [ToastType]. */
enum class ToastIcon { Cart }

data class ToastMessage(
    val id: Long,
    val title: UiText,
    val subtitle: UiText? = null,
    val type: ToastType,
    val icon: ToastIcon? = null,
)
