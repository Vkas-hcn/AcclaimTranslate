package com.vkas.secondtranslation.ui.translation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.vkas.secondtranslation.BR
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.base.BaseActivity
import com.vkas.secondtranslation.databinding.ActivityTranslationBinding
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.event.Constant.logTagSt
import com.vkas.secondtranslation.ui.language.LanguageActivity
import com.vkas.secondtranslation.utils.KLog
import com.vkas.secondtranslation.utils.MlKitData
import com.vkas.secondtranslation.widget.StLoadingDialog
import com.xuexiang.xutil.app.ActivityUtils
import com.xuexiang.xutil.tip.ToastUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import android.text.TextUtils
import com.vkas.secondtranslation.utils.AcclaimUtils
import com.vkas.secondtranslation.utils.CopyUtils


class TranslationActivity : BaseActivity<ActivityTranslationBinding, TranslationViewModel>() {
    private lateinit var ptLoadingDialog: StLoadingDialog
    private var jobNativeAdsSt: Job? = null
    private var jobBack: Job? = null
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_translation
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.presenter = Presenter()
        binding.inTranslationTitleSt.let {
            it.imgBack.setOnClickListener {
                if (binding.isTranslationEdit == true) {
                    binding.isTranslationEdit = false
                } else {
//                    returnToHomePage()
                    finish()
                }
            }
        }

        binding.edTranslationTopTranslation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun afterTextChanged(s: Editable) {
                    if(s.isNotEmpty()){
                        binding.tvButtonTranslation.background = getDrawable(R.drawable.bg_translation_en)
                        binding.tvButtonTranslation.setTextColor(getColor(R.color.tv_translation_en))
                    }else{
                        binding.tvButtonTranslation.background = getDrawable(R.drawable.bg_translation_dis)
                        binding.tvButtonTranslation.setTextColor(getColor(R.color.tv_translation_dis))
                    }
            }
        })

    }

    override fun initData() {
        super.initData()
        liveEventBusReceive()
        ptLoadingDialog = StLoadingDialog(this)
        viewModel.initializeLanguageBox(binding)
        updateLanguageItem()
        MlKitData.getInstance().fetchDownloadedModels()
//        StLoadTranslationAd.getInstance().whetherToShowSt = false
//        StLoadTranslationAd.getInstance().advertisementLoadingSt(this)
        initTranslationAd()
    }

    private fun liveEventBusReceive() {
        //插屏关闭后跳转
//        LiveEventBus
//            .get(Constant.PLUG_PT_TRANSLATION_SHOW, Boolean::class.java)
//            .observeForever {
//                StLoadBackAd.getInstance().advertisementLoadingSt(this)
//                finish()
//            }
    }

    private fun initTranslationAd() {
//        jobNativeAdsPt = lifecycleScope.launch {
//            while (isActive) {
//                PtLoadTranslationAd.getInstance()
//                    .setDisplayNativeAdPt(this@TranslationActivity, binding)
//                if (PtLoadHomeAd.getInstance().whetherToShowPt) {
//                    jobNativeAdsPt?.cancel()
//                    jobNativeAdsPt = null
//                }
//                delay(1000L)
//            }
//
//        }
    }

    override fun initViewObservable() {
        super.initViewObservable()
        showTranslationResult()
    }

    private fun showTranslationResult() {
        MlKitData.getInstance().sourceText.observe(this, {
            binding.edTranslationDown.setText(it)
            ptLoadingDialog.dismiss()
            binding.isTranslationEdit = false
        })
    }

    /**
     * 更新语言项
     */
    private fun updateLanguageItem() {
        binding.tvLanguageLeft.text =
            Locale(MlKitData.getInstance().sourceLang.value?.code).displayLanguage
        binding.tvLanguageTopName.text =
            Locale(MlKitData.getInstance().sourceLang.value?.code).displayLanguage
        binding.tvLanguageTopNameTranslation.text =
        Locale(MlKitData.getInstance().sourceLang.value?.code).displayLanguage
        binding.tvLanguageRight.text =
            Locale(MlKitData.getInstance().targetLang.value?.code).displayLanguage
        binding.tvLanguageDownName.text =
            Locale(MlKitData.getInstance().targetLang.value?.code).displayLanguage
        AcclaimUtils.langIconMap[MlKitData.getInstance().sourceLang.value?.code]?.let {
            binding.imgFlagLeft.setImageResource(
                it
            )
        }
        AcclaimUtils.langIconMap[MlKitData.getInstance().targetLang.value?.code]?.let {
            binding.imgFlagRight.setImageResource(
                it
            )
        }
    }

    /**
     * 交换翻译结果
     */
    fun exchangeTranslationResults() {
        if (binding.edTranslationDown.text.isNullOrEmpty()) {
            return
        }
        val edTranslationTop = binding.edTranslationTop.text
        binding.edTranslationTop.text = binding.edTranslationDown.text
        binding.edTranslationDown.setText(edTranslationTop)
    }

    /**
     * 返回主页
     */
//    private fun returnToHomePage() {
//        App.isAppOpenSameDaySt()
//        if (PixelUtils.isThresholdReached()) {
//            KLog.d(logTagSt, "广告达到上线")
//            finish()
//            return
//        }
//        StLoadBackAd.getInstance().advertisementLoadingSt(this)
//        jobBack = GlobalScope.launch {
//            try {
//                withTimeout(3000L) {
//                    while (isActive) {
//                        val showState =
//                            StLoadBackAd.getInstance()
//                                .displayBackAdvertisementSt(this@TranslationActivity)
//                        if (showState) {
//                            jobBack?.cancel()
//                            jobBack = null
//                        }
//                        delay(1000L)
//                    }
//                }
//            } catch (e: TimeoutCancellationException) {
//                KLog.d(logTagSt, "translation-back---插屏超时")
//                if (jobBack != null) {
//                    finish()
//                }
//            }
//        }
//    }
    inner class Presenter {
        fun toLanguage(type: Int) {
            ActivityUtils.startActivityForResult(
                this@TranslationActivity,
                LanguageActivity().javaClass,
                Constant.JUMP_LANGUAGE_PAGE,
                Constant.JUMP_LANGUAGE_PARAMETERS,
                type
            )
        }

        fun toExchange() {
            if (binding.selectedSourceLang == 1) {
                binding.selectedSourceLang = 2
            } else {
                binding.selectedSourceLang = 1
            }
            viewModel.exchangeLanguage()
            updateLanguageItem()
//            exchangeTranslationResults()
        }

        fun toDelete() {
            binding.edTranslationTopTranslation.setText("")
            binding.edTranslationTop.setText("")
        }

        fun toTranslation() {
            if (binding.edTranslationTopTranslation.text.trim().isEmpty()) {
                ToastUtils.toast(getString(R.string.please_enter_the_translation_content))
                return
            }
            lifecycleScope.launch {
                ptLoadingDialog.show()
                delay(500L)
                viewModel.translateRecognizedText(binding.edTranslationTopTranslation.text.toString())
            }
            binding.edTranslationTop.text = binding.edTranslationTopTranslation.text
        }

        fun toTranslationPage() {
            binding.isTranslationEdit = true
        }
        fun toCopyTxt():Boolean{
            if (binding.edTranslationDown.text.isNullOrEmpty()) {
                return false
            }
            CopyUtils.copyClicks(this@TranslationActivity,binding.edTranslationDown.text.toString())
            return true
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.JUMP_LANGUAGE_PAGE) {
            updateLanguageItem()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            returnToHomePage()
            finish()
        }
        return true
    }
}