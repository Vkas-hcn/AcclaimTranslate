package com.vkas.secondtranslation.ui.language

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.secondtranslation.BR
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.base.AdBase
import com.vkas.secondtranslation.base.BaseActivity
import com.vkas.secondtranslation.databinding.ActivityLanguageBinding
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.stad.StLoadLanguageAd
import com.vkas.secondtranslation.stad.StLoadTranslationAd
import com.vkas.secondtranslation.stbean.Language
import com.vkas.secondtranslation.ui.translation.TranslationViewModel
import com.vkas.secondtranslation.utils.AcclaimUtils
import com.vkas.secondtranslation.utils.KLog
import com.vkas.secondtranslation.utils.MlKitData
import com.vkas.secondtranslation.utils.MmkvUtils
import com.xuexiang.xutil.net.JsonUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import android.view.ViewGroup
import com.xuexiang.xui.utils.ResUtils
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.display.DensityUtils
import com.xuexiang.xutil.tip.ToastUtils


class LanguageActivity : BaseActivity<ActivityLanguageBinding, TranslationViewModel>() {
    private lateinit var allAdapter: LanguageAdapter
    private lateinit var recentlyAdapter: LanguageRecentlyAdapter
    private lateinit var allLanguageData: MutableList<Language>
    private lateinit var recentlyLanguageData: MutableList<Language>
    private var jobNativeAdsSt: Job? = null

    private var chekType: Int = 1
    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_language
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
        val bundle = intent.extras
        chekType = bundle?.getInt(Constant.JUMP_LANGUAGE_PARAMETERS)!!
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.presenter = Presenter()
        binding.inTranslationTitleSt.let {
            it.imgBack.setOnClickListener {
                finish()
            }
            it.editSearchView.setEditSearchListener { editString ->
                allLanguageData.forEach { all ->
                    all.searchForMatches =
                        Locale(all.code.lowercase(Locale.getDefault())).displayLanguage.contains(
                            editString.lowercase(Locale.getDefault())
                        )
                }
                allLanguageData.forEach { all ->
                    if (!all.searchForMatches) {
                        all.searchForMatches = all.code.lowercase(Locale.getDefault())
                            .contains(editString.lowercase(Locale.getDefault()))
                    }
                }
                allAdapter.notifyDataSetChanged()
            }
        }

    }

    override fun initData() {
        super.initData()
        liveEventBusReceive()
        binding.selectedSourceLang = chekType
        updateLanguageItem()
        initAllRecyclerView()
        initRecentlyRecyclerView()
        recentLanguageCursor()
        AdBase.getLanguageInstance().whetherToShowSt = false
        AdBase.getLanguageInstance().advertisementLoadingSt(this)
        initLanguageAd()
    }

    private fun initLanguageAd() {
        jobNativeAdsSt = lifecycleScope.launch {
            while (isActive) {
                StLoadLanguageAd.setDisplayLanguageNativeAdSt(this@LanguageActivity, binding)
                if (AdBase.getLanguageInstance().whetherToShowSt) {
                    jobNativeAdsSt?.cancel()
                    jobNativeAdsSt = null
                }
                delay(1000L)
            }
        }
    }

    private fun liveEventBusReceive() {
        LiveEventBus
            .get(Constant.DOWNLOADING_ST, Language::class.java)
            .observeForever { downloadData ->
                MlKitData.getInstance().availableLanguages.forEach { all ->
                    if (all.code == downloadData.code) {
                        all.downloadStatus = downloadData.downloadStatus
                    }
                }
                allAdapter.notifyDataSetChanged()
            }

        LiveEventBus
            .get(Constant.SEARCH_BAR_HIDDEN, Boolean::class.java)
            .observeForever {
                binding.searchStatus = false
                val layoutParams: ViewGroup.LayoutParams =  binding.llLanguageDow.layoutParams
                layoutParams.height = DensityUtils.dip2px(270f)
                binding.llLanguageDow.layoutParams = layoutParams
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecentlyRecyclerView() {
        recentlyLanguageData = ArrayList()
        recentlyLanguageData = viewModel.commonLanguageData()
        recentlyAdapter = LanguageRecentlyAdapter(recentlyLanguageData)
        binding.rvRecently.layoutManager = LinearLayoutManager(this)
        binding.recentlyAdapter = recentlyAdapter
        recentlyAdapter.addChildClickViewIds(R.id.img_down_state)
        recentlyAdapter.setOnItemClickListener { _, _, position ->
            recentlyLanguageData.getOrNull(position)?.run {
                setLanguageOptions(this, binding.selectedSourceLang as Int)
            }
            recentlyAdapter.notifyDataSetChanged()
            finish()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initAllRecyclerView() {
        allLanguageData = ArrayList()
        allLanguageData = MlKitData.getInstance().availableLanguages
        allLanguageData.forEach { all ->
            all.searchForMatches = true
            MlKitData.getInstance().availableModels.value?.forEach { models ->
                if (all.code == models) {
                    all.downloadStatus = 2
                }
            }
        }
        allAdapter = LanguageAdapter(allLanguageData)
        binding.layoutManager = LinearLayoutManager(this)
        binding.allAdapter = allAdapter
        allAdapter.addChildClickViewIds(R.id.img_down_state)
        allAdapter.setOnItemClickListener { _, _, position ->
            allLanguageData.getOrNull(position)?.run {
                if (this.downloadStatus != 2) {
                    return@setOnItemClickListener
                }
                setLanguageOptions(this, binding.selectedSourceLang as Int)
            }
            allAdapter.notifyDataSetChanged()
            if (binding.searchStatus == true) {
                binding.searchStatus = false
            } else {
                finish()
            }
        }
        allAdapter.setOnItemChildClickListener { _, view, position ->
            if (view.id == R.id.img_down_state) {
                allLanguageData.getOrNull(position)?.let {
                    // 下载
                    if (it.downloadStatus == 0) {
                        it.downloadStatus = 1
                        MlKitData.getInstance().downloadLanguage(it)
                        allAdapter.notifyDataSetChanged()
                    }
                    //删除
                    if (it.downloadStatus == 2) {
                        deletePopUpFrame(it)
                    }
                }
            }
        }
    }

    /**
     * 设置语言选项
     */
    private fun setLanguageOptions(language: Language, leftOrRight: Int) {
        when (leftOrRight) {
            1 -> {
                MlKitData.getInstance().sourceLang.value = language
                MmkvUtils.set(Constant.SOURCE_LANG_ST, language.code)
            }
            2 -> {
                MlKitData.getInstance().targetLang.value = language
                MmkvUtils.set(Constant.TARGET_LANG_ST, language.code)
            }
        }
        recentlyLanguageData.remove(language)
        recentlyLanguageData.add(0, language)
        if (recentlyLanguageData.size > 1) {
            recentlyLanguageData.removeAt(recentlyLanguageData.size - 1)
        }
        allLanguageData.map { it.isCheck = false }
        language.isCheck=true
        recentlyLanguageData.getOrNull(0)?.isCheck = true
        MmkvUtils.set(Constant.RECENT_DATA, JsonUtil.toJson(recentlyLanguageData))
        updateLanguageItem()
    }

    /**
     * 更新语言项
     */
    private fun updateLanguageItem() {
        binding.tvLanguageLeft.text =
            Locale(MlKitData.getInstance().sourceLang.value?.code).displayLanguage
        binding.tvLanguageRight.text =
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
     * 删除弹框
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun deletePopUpFrame(language: Language) {
        val dialog: AlertDialog? = AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_title))
            .setMessage(getString(R.string.delete_message))
            //设置对话框的按钮
            .setNegativeButton("NO") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("YES") { dialog, _ ->
                dialog.dismiss()
                MlKitData.getInstance().deleteLanguage(language)
                language.downloadStatus = 0
                recentlyLanguageData.remove(language)

                if (binding.tvLanguageLeft.text == Locale(language.code).displayLanguage) {
                    setLanguageOptions(Language(TranslateLanguage.ENGLISH), 1)
                }
                if (binding.tvLanguageRight.text == Locale(language.code).displayLanguage) {
                    setLanguageOptions(Language(TranslateLanguage.ENGLISH), 2)
                }
                recentlyAdapter.notifyDataSetChanged()
                allAdapter.notifyDataSetChanged()
            }.create()
        dialog?.show()
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        dialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
    }

    override fun initViewObservable() {
        super.initViewObservable()
    }

    inner class Presenter {
        fun toLanguage(type: Int) {
            binding.selectedSourceLang = type
            recentLanguageCursor()
        }

        fun toExchange() {
            if (binding.selectedSourceLang == 1) {
                binding.selectedSourceLang = 2
            } else {
                binding.selectedSourceLang = 1
            }
            viewModel.exchangeLanguage()
            updateLanguageItem()
            recentLanguageCursor()
        }

        fun toSea() {
            binding.searchStatus = true
            val layoutParams: ViewGroup.LayoutParams =  binding.llLanguageDow.layoutParams
            layoutParams.height = DensityUtils.dip2px(500f)
            binding.llLanguageDow.layoutParams = layoutParams
        }
    }

    /**
     *  最近语言光标
     */
    @SuppressLint("NotifyDataSetChanged")
    fun recentLanguageCursor() {
        when (binding.selectedSourceLang) {
            1 -> {
                binding.linLeft.background = getDrawable(R.drawable.ic_lanage_left_chek)
                binding.linRight.background = getDrawable(R.drawable.ic_lanage_right)
                recentlyLanguageData[0]= MlKitData.getInstance().sourceLang.value?.code?.let {
                    Language(
                        it
                    )
                }!!
                recentlyLanguageData[0].isCheck =true
                allLanguageData.map {
                    it.isCheck = MlKitData.getInstance().sourceLang.value?.code ==it.code
                }
            }
            2 -> {
                binding.linLeft.background = getDrawable(R.drawable.ic_lanage_left)
                binding.linRight.background = getDrawable(R.drawable.ic_language_right_chek)
                recentlyLanguageData[0]= MlKitData.getInstance().targetLang.value?.code?.let {
                    Language(
                        it
                    )
                }!!
                recentlyLanguageData[0].isCheck =true
                allLanguageData.map {
                    it.isCheck = MlKitData.getInstance().targetLang.value?.code ==it.code
                }
            }
        }
        recentlyAdapter.notifyDataSetChanged()
        allAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(300)
            if (lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            if (App.nativeAdRefreshSt) {
                AdBase.getLanguageInstance().whetherToShowSt = false
                if (AdBase.getTranslationInstance().appAdDataSt != null) {
                    KLog.d(Constant.logTagSt, "onResume------>1")
                    StLoadLanguageAd.setDisplayLanguageNativeAdSt(this@LanguageActivity, binding)
                } else {
                    binding.languageAdSt= false
                    KLog.d(Constant.logTagSt, "onResume------>2")
                    AdBase.getLanguageInstance().advertisementLoadingSt(this@LanguageActivity)
                    initLanguageAd()
                }
            }
        }
    }


}