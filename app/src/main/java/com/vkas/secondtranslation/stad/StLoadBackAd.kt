package com.vkas.secondtranslation.stad

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

object StLoadBackAd {
    private val adBase = AdBase.getBackInstance()

    /**
     * 加载首页插屏广告
     */
    fun loadBackAdvertisementSt(context: Context, adData: StAdBean) {
        val adRequest = AdRequest.Builder().build()
        val id = takeSortedAdIDSt(adBase.adIndexSt, adData.st_back)
        KLog.d(logTagSt, "back--插屏广告id=$id;权重=${adData.st_back.getOrNull(adBase.adIndexSt)?.st_weight}")

        InterstitialAd.load(
            context,
            id,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let {
                        KLog.d(logTagSt, "back---连接插屏加载失败=$it") }
                    adBase.isLoadingSt = false
                    adBase.appAdDataSt = null
                    if (adBase.adIndexSt < adData.st_back.size - 1) {
                        adBase.adIndexSt++
                        loadBackAdvertisementSt(context,adData)
                    }else{
                        adBase.adIndexSt = 0
                    }
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    adBase.loadTimeSt = Date().time
                    adBase.isLoadingSt = false
                    adBase.appAdDataSt = interstitialAd
                    adBase.adIndexSt = 0
                    KLog.d(logTagSt, "back---返回插屏加载成功")
                }
            })
    }

    /**
     * back插屏广告回调
     */
    private fun backScreenAdCallback() {
        (adBase.appAdDataSt  as? InterstitialAd)?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    KLog.d(logTagSt, "back插屏广告点击")
                    recordNumberOfAdClickSt()
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    KLog.d(logTagSt, "关闭back插屏广告${App.isBackDataSt}")
                    LiveEventBus.get<Boolean>(Constant.PLUG_ST_BACK_AD_SHOW)
                        .post(App.isBackDataSt)
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
                    KLog.d(logTagSt, "back----show")
                }
            }
    }

    /**
     * 展示Connect广告
     */
    fun displayBackAdvertisementSt(activity: AppCompatActivity): Boolean {
        if (adBase.appAdDataSt == null) {
            KLog.d(logTagSt, "back--插屏广告加载中。。。")
            return false
        }
        if (adBase.whetherToShowSt || activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
            KLog.d(logTagSt, "back--前一个插屏广告展示中或者生命周期不对")
            return false
        }
        backScreenAdCallback()
        activity.lifecycleScope.launch(Dispatchers.Main) {
            (adBase.appAdDataSt as InterstitialAd).show(activity)
        }
        return true
    }
}