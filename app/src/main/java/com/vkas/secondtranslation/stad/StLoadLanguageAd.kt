package com.vkas.secondtranslation.stad

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.vkas.secondtranslation.base.AdBase
import com.vkas.secondtranslation.databinding.ActivityMainBinding
import com.vkas.secondtranslation.event.Constant.logTagSt
import com.vkas.secondtranslation.stbean.StAdBean
import com.vkas.secondtranslation.utils.AcclaimUtils.recordNumberOfAdClickSt
import com.vkas.secondtranslation.utils.AcclaimUtils.takeSortedAdIDSt
import com.vkas.secondtranslation.utils.KLog
import java.util.*
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.databinding.ActivityLanguageBinding
import com.vkas.secondtranslation.utils.AcclaimUtils.recordNumberOfAdDisplaysSt
import com.vkas.secondtranslation.widget.RoundCornerOutlineProvider

object StLoadLanguageAd {
    private val adBase = AdBase.getLanguageInstance()

    /**
     * 加载language原生广告
     */
    fun loadLanguageAdvertisementSt(context: Context, adData: StAdBean) {
        val id = takeSortedAdIDSt(adBase.adIndexSt, adData.st_vpn)
        KLog.d(logTagSt, "language--原生广告id=$id;权重=${adData.st_vpn.getOrNull(adBase.adIndexSt)?.st_weight}")
        val vpnNativeAds = AdLoader.Builder(
            context.applicationContext,
            id
        )
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
            .build()

        vpnNativeAds.withNativeAdOptions(adOptions)
        vpnNativeAds.forNativeAd {
            adBase.appAdDataSt = it
        }
        vpnNativeAds.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                val error =
                    """
           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """"
                adBase.isLoadingSt = false
                adBase.appAdDataSt = null
                KLog.d(logTagSt, "language--加载vpn原生加载失败: $error")

                if (adBase.adIndexSt < adData.st_vpn.size - 1) {
                    adBase.adIndexSt++
                    loadLanguageAdvertisementSt(context,adData)
                }else{
                    adBase.adIndexSt = 0
                }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                KLog.d(logTagSt, "language--加载vpn原生广告成功")
                adBase.loadTimeSt = Date().time
                adBase.isLoadingSt = false
                adBase.adIndexSt = 0
            }

            override fun onAdOpened() {
                super.onAdOpened()
                KLog.d(logTagSt, "language--点击vpn原生广告")
                recordNumberOfAdClickSt()
            }
        }).build().loadAd(AdRequest.Builder().build())
    }

    /**
     * 设置展示language原生广告
     */
    fun setDisplayLanguageNativeAdSt(activity: AppCompatActivity, binding: ActivityLanguageBinding) {
        activity.runOnUiThread {
            adBase.appAdDataSt?.let { adData ->
                if (adData is NativeAd && !adBase.whetherToShowSt && activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        adData.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater.inflate(R.layout.layout_main_native_st, null) as NativeAdView
                    // 对应原生组件
                    setCorrespondingNativeComponentSt(adData, adView)
                    binding.stAdFrame.apply {
                        removeAllViews()
                        addView(adView)
                    }
                    binding.languageAdSt = true
                    recordNumberOfAdDisplaysSt()
                    adBase.whetherToShowSt = true
                    App.nativeAdRefreshSt = false
                    adBase.appAdDataSt = null
                    KLog.d(logTagSt, "language-原生广告--展示")
                    //重新缓存
                    AdBase.getLanguageInstance().advertisementLoadingSt(activity)
                }
            }
        }
    }

    private fun setCorrespondingNativeComponentSt(nativeAd: NativeAd, adView: NativeAdView) {
        adView.mediaView = adView.findViewById(R.id.ad_media)
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        (adView.headlineView as TextView).text = nativeAd.headline
        nativeAd.mediaContent?.let {
            adView.mediaView?.apply { setImageScaleType(ImageView.ScaleType.CENTER_CROP) }
                ?.setMediaContent(it)
        }
        adView.mediaView?.clipToOutline=true
        adView.mediaView?.outlineProvider= RoundCornerOutlineProvider(8f)
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)
    }
}