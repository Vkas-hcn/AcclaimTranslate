package com.vkas.secondtranslation.ui.translation

import android.app.Application
import com.google.gson.reflect.TypeToken
import com.google.mlkit.nl.translate.TranslateLanguage
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.base.BaseViewModel
import com.vkas.secondtranslation.databinding.ActivityTranslationBinding
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.stbean.Language
import com.vkas.secondtranslation.utils.MlKitData
import com.vkas.secondtranslation.utils.MmkvUtils
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.net.JsonUtil

class TranslationViewModel(application: Application) : BaseViewModel(application) {
    /**
     * 翻译识别文字
     */
    fun translateRecognizedText(text: String) {
        MlKitData.getInstance().translate(text)
    }

    /**
     * 初始化语言框
     */
    fun initializeLanguageBox(binding: ActivityTranslationBinding) {
        binding.edTranslationTop.text.clear()
        binding.edTranslationDown.text.clear()
        MlKitData.getInstance().sourceText.value = ""
        MlKitData.getInstance().sourceLang.value =
            App.mmkvSt.decodeString(Constant.SOURCE_LANG_ST, TranslateLanguage.ENGLISH)
                ?.let { Language(it) }
        Language(TranslateLanguage.ENGLISH)
        MlKitData.getInstance().targetLang.value =
            App.mmkvSt.decodeString(Constant.TARGET_LANG_ST, TranslateLanguage.ENGLISH)
                ?.let { Language(it) }
    }

    /**
     * 交换语言
     */
    fun exchangeLanguage() {
        val sourceLang = MlKitData.getInstance().sourceLang.value
        MlKitData.getInstance().sourceLang.value = MlKitData.getInstance().targetLang.value
        MlKitData.getInstance().targetLang.value = sourceLang
        MmkvUtils.set(Constant.SOURCE_LANG_ST, MlKitData.getInstance().sourceLang.value?.code)
        MmkvUtils.set(Constant.TARGET_LANG_ST, MlKitData.getInstance().targetLang.value?.code)
    }

    /**
     * 常用语言数据
     */
    fun commonLanguageData(): MutableList<Language> {
        var languages: MutableList<Language> = ArrayList()

        if (Utils.isNullOrEmpty(App.mmkvSt.decodeString(Constant.RECENT_DATA))) {
            languages.add(Language(TranslateLanguage.ENGLISH))
        } else {
            languages = JsonUtil.fromJson(
                App.mmkvSt.decodeString(Constant.RECENT_DATA),
                object : TypeToken<MutableList<Language>?>() {}.type
            )
        }
        return languages.distinct().toMutableList()
    }
}