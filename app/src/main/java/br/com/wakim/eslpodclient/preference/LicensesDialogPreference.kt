package br.com.wakim.eslpodclient.preference

import android.content.Context
import android.support.v7.preference.Preference
import android.util.AttributeSet
import br.com.wakim.eslpodclient.extensions.startActivity
import br.com.wakim.eslpodclient.settings.view.LicensesActivity

class LicensesDialogPreference: Preference {

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStylRes: Int) : super(context, attrs, defStyleAttr, defStylRes)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    override fun onClick() {
        super.onClick()
        context.startActivity<LicensesActivity>()
    }
}