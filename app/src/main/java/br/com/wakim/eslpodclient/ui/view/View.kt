package br.com.wakim.eslpodclient.ui.view

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar

interface View {
    fun showMessage(@StringRes messageResId: Int): Snackbar
    fun showMessage(@StringRes messageResId: Int, action: String, clickListener : (() -> Unit)?)
}