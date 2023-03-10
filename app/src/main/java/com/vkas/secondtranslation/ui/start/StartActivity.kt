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
import com.vkas.secondtranslation.base.BaseActivity
import com.vkas.secondtranslation.base.BaseViewModel
import com.vkas.secondtranslation.databinding.ActivityStartBinding
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.event.Constant.logTagSt
import com.vkas.secondtranslation.ui.main.MainActivity
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
        binding.pbStartSt.setProgressDuration(2000)
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
                KLog.d(logTagSt, "??????????????????-??????==${this.lifecycle.currentState}")
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
                MmkvUtils.set(Constant.PROFILE_ST_DATA, auth.getString("ec_ser"))
                MmkvUtils.set(Constant.PROFILE_ST_DATA_FAST, auth.getString("ec_smar"))
                MmkvUtils.set(Constant.AROUND_ST_FLOW_DATA, auth.getString("ecAroundFlow_Data"))
                MmkvUtils.set(Constant.ADVERTISING_ST_DATA, auth.getString("ec_ad"))

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
     * ????????????
     */
    private fun jumpPage() {
        // ????????????????????????????????????????????????????????????finish?????????
        if (!isCurrentPage) {
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

    /**
     * ????????????
     */
//    private fun loadAdvertisement() {
//        // ??????
//        AdBase.getOpenInstance().adIndexSt = 0
//        AdBase.getOpenInstance().advertisementLoadingSt(this)
//        rotationDisplayOpeningAdSt()
//        // ????????????
//        AdBase.getHomeInstance().adIndexSt = 0
//        AdBase.getHomeInstance().advertisementLoadingSt(this)
//        // ???????????????
//        AdBase.getResultInstance().adIndexSt = 0
//        AdBase.getResultInstance().advertisementLoadingSt(this)
//        // ????????????
//        AdBase.getConnectInstance().adIndexSt = 0
//        AdBase.getConnectInstance().advertisementLoadingSt(this)
//        // ??????????????????
//        AdBase.getBackInstance().adIndexSt = 0
//        AdBase.getBackInstance().advertisementLoadingSt(this)
//    }

    /**
     * ????????????????????????
     */
//    private fun rotationDisplayOpeningAdSt() {
//        jobOpenAdsSt = lifecycleScope.launch {
//            try {
//                withTimeout(10000L) {
//                    delay(1000L)
//                    while (isActive) {
//                        val showState = StLoadOpenAd
//                            .displayOpenAdvertisementSt(this@StartStActivity)
//                        if (showState) {
//                            jobOpenAdsSt?.cancel()
//                            jobOpenAdsSt = null
//                        }
//                        delay(1000L)
//                    }
//                }
//            } catch (e: TimeoutCancellationException) {
//                KLog.e("TimeoutCancellationException I'm sleeping $e")
//                jumpPage()
//            }
//        }
//    }

    /**
     * ???????????????
     */
    private fun preloadedAdvertisement() {
//        App.isAppOpenSameDaySt()
//        if (isThresholdReached()) {
//            KLog.d(logTagSt, "??????????????????")
            lifecycleScope.launch {
                delay(2000L)
                liveJumpHomePage.postValue(true)
            }
//        } else {
//            loadAdvertisement()
//        }
    }

    override fun onHorizontalProgressStart(view: View?) {
    }

    override fun onHorizontalProgressUpdate(view: View?, progress: Float) {
    }

    override fun onHorizontalProgressFinished(view: View?) {
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode == KeyEvent.KEYCODE_BACK
    }
}