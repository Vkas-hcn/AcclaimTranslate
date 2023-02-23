package com.vkas.secondtranslation.base

import android.content.Context
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.stad.*
import com.vkas.secondtranslation.stbean.StAdBean
import com.vkas.secondtranslation.utils.AcclaimUtils
import com.vkas.secondtranslation.utils.KLog
import java.util.*

class AdBase {
    companion object {
        fun getOpenInstance() = InstanceHelper.openLoadSt
        fun getHomeInstance() = InstanceHelper.homeLoadSt
        fun getTranslationInstance() = InstanceHelper.translationLoadSt
        fun getLanguageInstance() = InstanceHelper.languageLoadSt
        fun getBackInstance() = InstanceHelper.backLoadSt
        private var idCounter = 0

    }

    val id = ++idCounter

    object InstanceHelper {
        val openLoadSt = AdBase()
        val homeLoadSt = AdBase()
        val translationLoadSt = AdBase()
        val languageLoadSt = AdBase()
        val backLoadSt = AdBase()
    }

    var appAdDataSt: Any? = null

    // 是否正在加载中
    var isLoadingSt = false

    //加载时间
    var loadTimeSt: Long = Date().time

    // 是否展示
    var whetherToShowSt = false

    // openIndex
    var adIndexSt = 0

    // 是否是第一遍轮训
    var isFirstRotation: Boolean = false

    /**
     * 广告加载前判断
     */
    fun advertisementLoadingSt(context: Context) {
        App.isAppOpenSameDaySt()
        if (AcclaimUtils.isThresholdReached()) {
            KLog.d(Constant.logTagSt, "广告达到上线")
            return
        }
        if (isLoadingSt) {
            KLog.d(Constant.logTagSt, "${getInstanceName()}--广告加载中，不能再次加载")
            return
        }
        isFirstRotation = false
        if (appAdDataSt == null) {
            isLoadingSt = true
            KLog.d(Constant.logTagSt, "${getInstanceName()}--广告开始加载")
            loadStartupPageAdvertisementSt(context, AcclaimUtils.getAdServerDataSt())
        }
        if (appAdDataSt != null && !whetherAdExceedsOneHour(loadTimeSt)) {
            isLoadingSt = true
            appAdDataSt = null
            KLog.d(Constant.logTagSt, "${getInstanceName()}--广告过期重新加载")
            loadStartupPageAdvertisementSt(context, AcclaimUtils.getAdServerDataSt())
        }
    }

    /**
     * 广告是否超过过期（false:过期；true：未过期）
     */
    private fun whetherAdExceedsOneHour(loadTime: Long): Boolean =
        Date().time - loadTime < 60 * 60 * 1000

    /**
     * 加载启动页广告
     */
    private fun loadStartupPageAdvertisementSt(context: Context, adData: StAdBean) {
        adLoaders[id]?.invoke(context, adData)
    }

    private val adLoaders = mapOf<Int, (Context, StAdBean) -> Unit>(
        1 to { context, adData ->
            val adType = adData.st_open.getOrNull(adIndexSt)?.st_type
            if (adType == "screen") {
                StLoadOpenAd.loadStartInsertAdSt(context, adData)
            } else {
                StLoadOpenAd.loadOpenAdvertisementSt(context, adData)
            }
        },
        2 to { context, adData ->
            StLoadHomeAd.loadHomeAdvertisementSt(context, adData)
        },
        3 to { context, adData ->
            StLoadTranslationAd.loadTranslationAdvertisementSt(context, adData)
        },
        4 to { context, adData ->
            StLoadLanguageAd.loadLanguageAdvertisementSt(context, adData)
        },
        5 to { context, adData ->
            StLoadBackAd.loadBackAdvertisementSt(context, adData)
        }
    )

    /**
     * 获取实例名称
     */
    private fun getInstanceName(): String {
        when (id) {
            1 -> {
                return "open"
            }
            2 -> {
                return "home"
            }
            3 -> {
                return "translation"
            }
            4 -> {
                return "language"
            }
            5 -> {
                return "back"
            }
            else -> {
                return ""
            }
        }
    }
}