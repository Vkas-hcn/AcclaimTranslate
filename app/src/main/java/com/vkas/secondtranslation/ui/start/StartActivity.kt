package com.vkas.secondtranslation.ui.start

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.secondtranslation.BR
import com.vkas.secondtranslation.BuildConfig
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.base.AdBase
import com.vkas.secondtranslation.base.BaseActivity
import com.vkas.secondtranslation.base.BaseViewModel
import com.vkas.secondtranslation.databinding.ActivityStartBinding
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.event.Constant.logTagSt
import com.vkas.secondtranslation.stad.StLoadOpenAd
import com.vkas.secondtranslation.ui.main.MainActivity
import com.vkas.secondtranslation.utils.AcclaimUtils.isThresholdReached
import com.vkas.secondtranslation.utils.KLog
import com.vkas.secondtranslation.utils.MmkvUtils
import com.xuexiang.xui.widget.progress.HorizontalProgressView
import kotlinx.coroutines.*

class StartActivity : BaseActivity<ActivityStartBinding, BaseViewModel>(),
    HorizontalProgressView.HorizontalProgressUpdateListener {
    companion object {
        var isCurrentPage: Boolean = false
    }

    private var liveJumpHomePage = MutableLiveData<Boolean>()
    private var liveJumpHomePage2 = MutableLiveData<Boolean>()
    private var jobOpenAdsSt: Job? = null

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_start
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
        isCurrentPage = intent.getBooleanExtra(Constant.RETURN_ST_CURRENT_PAGE, false)

    }

    override fun initToolbar() {
        super.initToolbar()
    }

    override fun initData() {
        super.initData()
        binding.pbStartSt.setProgressViewUpdateListener(this)
        binding.pbStartSt.setProgressDuration(10000)
        binding.pbStartSt.startProgressAnimation()
        liveEventBusSt()
//        lifecycleScope.launch(Dispatchers.IO) {
//            EasyConnectUtils.getIpInformation()
//        }
        getFirebaseDataSt()
        jumpHomePageData()
    }

    private fun liveEventBusSt() {
        LiveEventBus
            .get(Constant.OPEN_CLOSE_JUMP, Boolean::class.java)
            .observeForever {
                KLog.d(logTagSt, "关闭开屏内容-接收==${this.lifecycle.currentState}")
                if (this.lifecycle.currentState == Lifecycle.State.STARTED) {
                    jumpPage()
                }
            }
    }

    private fun getFirebaseDataSt() {
        if (BuildConfig.DEBUG) {
            preloadedAdvertisement()
//            lifecycleScope.launch {
//                val ips = listOf("192.168.0.1", "8.8.8.8", "114.114.114.114")
//                val fastestIP = findFastestIP(ips)
//                KLog.e("TAG", "Fastest IP: $fastestIP")
//                delay(1500)
//                MmkvUtils.set(
//                    Constant.ADVERTISING_ST_DATA,
//                    ResourceUtils.readStringFromAssert("elAdDataFireBase.json")
//                )
//            }
            return
        } else {
            preloadedAdvertisement()
            val auth = Firebase.remoteConfig
            auth.fetchAndActivate().addOnSuccessListener {
                MmkvUtils.set(Constant.PROFILE_ST_DATA, auth.getString("st_ser"))
                MmkvUtils.set(Constant.PROFILE_ST_DATA_FAST, auth.getString("st_smar"))
                MmkvUtils.set(Constant.AROUND_ST_FLOW_DATA, auth.getString("stAroundFlow_Data"))
                MmkvUtils.set(Constant.ADVERTISING_ST_DATA, auth.getString("STAD_Data"))

            }
        }
    }

    override fun initViewObservable() {
        super.initViewObservable()
    }

    private fun jumpHomePageData() {
        liveJumpHomePage2.observe(this, {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                KLog.e("TAG", "isBackDataSt==${App.isBackDataSt}")
                delay(300)
                if (lifecycle.currentState == Lifecycle.State.RESUMED) {
                    jumpPage()
                }
            }
        })
        liveJumpHomePage.observe(this, {
            liveJumpHomePage2.postValue(true)
        })
    }

    /**
     * 跳转页面
     */
    private fun jumpPage() {
        // 不是后台切回来的跳转，是后台切回来的直接finish启动页
        if (!isCurrentPage) {
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

    /**
     * 加载广告
     */
    private fun loadAdvertisement() {
        // 开屏
        AdBase.getOpenInstance().adIndexSt = 0
        AdBase.getOpenInstance().advertisementLoadingSt(this)
        rotationDisplayOpeningAdSt()
        // 首页原生
        AdBase.getHomeInstance().adIndexSt = 0
        AdBase.getHomeInstance().advertisementLoadingSt(this)
        // 翻译原生
        AdBase.getTranslationInstance().adIndexSt = 0
        AdBase.getTranslationInstance().advertisementLoadingSt(this)
        // 语言原生
        AdBase.getLanguageInstance().adIndexSt = 0
        AdBase.getLanguageInstance().advertisementLoadingSt(this)
        // 服务器页插屏
        AdBase.getBackInstance().adIndexSt = 0
        AdBase.getBackInstance().advertisementLoadingSt(this)
    }

    /**
     * 轮训展示开屏广告
     */
    private fun rotationDisplayOpeningAdSt() {
        jobOpenAdsSt = lifecycleScope.launch {
            try {
                withTimeout(10000L) {
                    delay(3000L)
                    while (isActive) {
                        val showState = StLoadOpenAd
                            .judgeConditionsOpenAd(this@StartActivity)
                        if (showState) {
                            jobOpenAdsSt?.cancel()
                            jobOpenAdsSt = null
                            binding.pbStartSt.stopProgressAnimation()
                            binding.pbStartSt.progress = 100F
                        }
                        delay(1000L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                KLog.e("TimeoutCancellationException I'm sleeping $e")
                jumpPage()
            }
        }
    }

    /**
     * 预加载广告
     */
    private fun preloadedAdvertisement() {
        App.isAppOpenSameDaySt()
        if (isThresholdReached()) {
            KLog.d(logTagSt, "广告达到上线")
            lifecycleScope.launch {
                delay(3000L)
                binding.pbStartSt.stopProgressAnimation()
                binding.pbStartSt.progress = 100F
                liveJumpHomePage.postValue(true)
            }
        } else {
            loadAdvertisement()
        }
    }

    override fun onHorizontalProgressStart(view: View?) {
    }

    override fun onHorizontalProgressUpdate(view: View?, progress: Float) {

    }

    override fun onHorizontalProgressFinished(view: View?) {
        App.isAppOpenSameDaySt()
        if (!isThresholdReached()) {
            StLoadOpenAd
                .displayOpenAdvertisementSt(this@StartActivity)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK
    }
}