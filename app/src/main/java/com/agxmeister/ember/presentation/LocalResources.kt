package com.agxmeister.ember.presentation

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalAppResources = compositionLocalOf<Resources> {
    error("LocalAppResources not provided")
}

@Composable
fun appString(@StringRes id: Int): String = LocalAppResources.current.getString(id)

@Composable
fun appString(@StringRes id: Int, vararg args: Any): String =
    LocalAppResources.current.getString(id, *args)
