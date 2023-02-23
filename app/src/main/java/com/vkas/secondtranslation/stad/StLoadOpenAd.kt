package com.vkas.secondtranslation.stad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.base.AdBase
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.event.Constant.logTagSt
import com.vkas.secondtranslation.stbean.StAdBean
import com.vkas.secondtranslation.utils.AcclaimUtils.recordNumberOfAdClickSt
import com.vkas.secondtranslation.utils.AcclaimUtils.recordNumberOfAdDisplaysSt
import com.vkas.secondtranslation.utils.AcclaimUtils.takeSortedAdIDSt
import com.vkas.secondtranslation.utils.KLog
import com.xuexiang.xutil.net.JsonUtil
import java.util.*

object StLoadOpenAd {
    private val adBase = AdBase.getOpenInstance()
    /**
     * 加载启动页广告
     */
    private fun loadStartupPageAdvertisementSt(context: Context, adData: StAdBean) {
        if (adData.st_open.getOrNull(adBase.adIndexSt)?.st_type == "screen") {
            loadStartInsertAdSt(context, adData)
        } else {
            loadOpenAdvertisementSt(context, adData)
        }
    }

    /**
     * 加载开屏广告
     */
    fun loadOpenAdvertisementSt(context: Context, adData: StAdBean) {
        KLog.e("loadOpenAdvertisementSt", "adData().st_open=${JsonUtil.toJson(adData.st_open)}")
        KLog.e(
            "loadOpenAdvertisementSt",
            "id=${JsonUtil.toJson(takeSortedAdIDSt(adBase.adIndexSt, adData.st_open))}"
        )

        val id = takeSortedAdIDSt(adBase.adIndexSt, adData.st_open)

        KLog.d(logTagSt, "open--开屏广告id=$id;权重=${adData.st_open.getOrNull(adBase.adIndexSt)?.st_weight}")
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            id,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    adBase.loadTimeSt = Date().time
                    adBase.isLoadingSt = false
                    adBase.appAdDataSt = ad

                    KLog.d(logTagSt, "open--开屏广告加载成功")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adBase.isLoadingSt = false
                    adBase.appAdDataSt = null
                    if (adBase.adIndexSt < adData.st_open.size - 1) {
                        adBase.adIndexSt++
                        loadStartupPageAdvertisementSt(context, adData)
                    } else {
                        adBase.adIndexSt = 0
                        if(!adBase.isFirstRotation){
                            AdBase.getOpenInstance().advertisementLoadingSt(context)
                            adBase.isFirstRotation =true
                        }
                    }
                    KLog.d(logTagSt, "open--开屏广告加载失败: " + loadAdError.message)
                }
            }
        )
    }


    /**
     * 开屏广告回调
     */
    private fun advertisingOpenCallbackSt() {
        if (adBase.appAdDataSt !is AppOpenAd) {
            return
        }
        (adBase.appAdDataSt as AppOpenAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                //取消全屏内容
                override fun onAdDismissedFullScreenContent() {
                    KLog.d(logTagSt, "open--关闭开屏内容")
                    adBase.whetherToShowSt = false
                    adBase.appAdDataSt = null
                    if (!App.whetherBackgroundSt) {
                        LiveEventBus.get<Boolean>(Constant.OPEN_CLOSE_JUMP)
                            .post(true)
                    }
                }

                //全屏内容无法显示时调用
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    adBase.whetherToShowSt = false
                    adBase.appAdDataSt = null
                    KLog.d(logTagSt, "open--全屏内容无法显示时调用")
                }

                //显示全屏内容时调用
                override fun onAdShowedFullScreenContent() {
                    adBase.appAdDataSt = null
                    adBase.whetherToShowSt = true
                    recordNumberOfAdDisplaysSt()
                    adBase.adIndexSt = 0
                    KLog.d(logTagSt, "open---开屏广告展示")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    KLog.d(logTagSt, "open---点击open广告")
                    recordNumberOfAdClickSt()
                }
            }
    }

    /**
     * 展示Open广告
     */
    fun displayOpenAdvertisementSt(activity: AppCompatActivity): Boolean {

        if (adBase.appAdDataSt == null) {
            KLog.d(logTagSt, "open---开屏广告加载中。。。")
            return false
        }
        if (adBase.whetherToShowSt || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagSt, "open---前一个开屏广告展示中或者生命周期不对")
            return false
        }
        if (adBase.appAdDataSt is AppOpenAd) {
            advertisingOpenCallbackSt()
            (adBase.appAdDataSt as AppOpenAd).show(activity)
        } else {
            startInsertScreenAdCallbackSt()
            (adBase.appAdDataSt as InterstitialAd).show(activity)
        }
        return true
    }

    /**
     * 加载启动页插屏广告
     */
    fun loadStartInsertAdSt(context: Context, adData: StAdBean) {
        val adRequest = AdRequest.Builder().build()
        val id = takeSortedAdIDSt(adBase.adIndexSt, adData.st_open)
        KLog.d(
            logTagSt,
            "open--插屏广告id=$id;权重=${adData.st_open.getOrNull(adBase.adIndexSt)?.st_weight}"
        )

        InterstitialAd.load(
            context,
            id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let { KLog.d(logTagSt, "open---连接插屏加载失败=$it") }
                    adBase.isLoadingSt = false
                    adBase.appAdDataSt = null
                    if (adBase.adIndexSt < adData.st_open.size - 1) {
                        adBase.adIndexSt++
                        loadStartupPageAdvertisementSt(context, adData)
                    } else {
                        adBase.adIndexSt = 0
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    adBase.loadTimeSt = Date().time
                    adBase.isLoadingSt = false
                    adBase.appAdDataSt = interstitialAd
                    KLog.d(logTagSt, "open--启动页插屏加载完成")
                }
            })
    }

    /**
     * StartInsert插屏广告回调
     */
    private fun startInsertScreenAdCallbackSt() {
        if (adBase.appAdDataSt !is InterstitialAd) {
            return
        }
        (adBase.appAdDataSt as InterstitialAd).fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    KLog.d(logTagSt, "open--插屏广告点击")
                    recordNumberOfAdClickSt()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    KLog.d(logTagSt, "open--关闭StartInsert插屏广告${App.isBackDataSt}")
                    if (!App.whetherBackgroundSt) {
                        LiveEventBus.get<Boolean>(Constant.OPEN_CLOSE_JUMP)
                            .post(true)
                    }
                    adBase.appAdDataSt = null
                    adBase.whetherToShowSt = false
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    KLog.d(logTagSt, "Ad failed to show fullscreen content.")
                    adBase.appAdDataSt = null
                    adBase.whetherToShowSt = false
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    KLog.e("TAG", "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    adBase.appAdDataSt = null
                    recordNumberOfAdDisplaysSt()
                    // Called when ad is shown.
                    adBase.whetherToShowSt = true
                    adBase.adIndexSt = 0
                    KLog.d(logTagSt, "open----插屏show")
                }
            }
    }
}