package br.com.wakim.eslpodclient.preference

import android.content.Context
import android.support.v7.preference.DialogPreference
import android.util.AttributeSet

class LicensesDialogPreference: DialogPreference {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    override fun onClick() {
        // TODO
    }
}