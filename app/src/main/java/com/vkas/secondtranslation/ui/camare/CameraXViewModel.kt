package com.vkas.secondtranslation.ui.camare

import android.app.Application
import com.google.mlkit.nl.translate.TranslateLanguage
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.app.App.Companion.mmkvSt
import com.vkas.secondtranslation.base.BaseViewModel
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.stbean.Language
import com.vkas.secondtranslation.utils.MlKitData
import com.vkas.secondtranslation.utils.MmkvUtils

class CameraXViewModel (application: Application) : BaseViewModel(application) {
    /**
     * 翻译识别文字
     */
    fun translateRecognizedText(text:String){
        MlKitData.getInstance().translate(text)
    }
    /**
     * 交换语言
     */
    fun exchangeLanguage() {
        val sourceLang = MlKitData.getInstance().sourceLang.value
        MlKitData.getInstance().sourceLang.value = MlKitData.getInstance().targetLang.value
        MlKitData.getInstance().targetLang.value = sourceLang
        MmkvUtils.set(Constant.SOURCE_LANG_ST,MlKitData.getInstance().sourceLang.value?.code)
        MmkvUtils.set(Constant.TARGET_LANG_ST,MlKitData.getInstance().targetLang.value?.code)
    }
    /**
     * 初始化语言框
     */
    fun initializeLanguageBox(){
        MlKitData.getInstance().sourceLang.value =
            mmkvSt.decodeString(Constant.SOURCE_LANG_ST, TranslateLanguage.ENGLISH)
                ?.let { Language(it) }
        Language(TranslateLanguage.ENGLISH)
        MlKitData.getInstance().targetLang.value =
            mmkvSt.decodeString(Constant.TARGET_LANG_ST, TranslateLanguage.ENGLISH)
                ?.let { Language(it) }
    }
}