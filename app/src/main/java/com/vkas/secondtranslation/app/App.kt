package com.vkas.secondtranslation.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.blankj.utilcode.util.ProcessUtils
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.jeremyliao.liveeventbus.LiveEventBus
import com.tencent.mmkv.MMKV
import com.vkas.secondtranslation.BuildConfig
import com.vkas.secondtranslation.base.AppManagerMVVM
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.ui.start.StartActivity
import com.vkas.secondtranslation.utils.ActivityUtils
import com.vkas.secondtranslation.utils.CalendarUtils
import com.vkas.secondtranslation.utils.KLog
import com.vkas.secondtranslation.utils.MmkvUtils
import com.xuexiang.xui.XUI
import com.xuexiang.xutil.XUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class App : Application(), LifecycleObserver {
    private var flag = 0
    private var job_ec : Job? =null
    private var ad_activity_ec: Activity? = null
    private var top_activity_ec: Activity? = null
    companion object {
        // app当前是否在后台
        var isBackDataSt = false

        // 是否进入后台（三秒后）
        var whetherBackgroundSt = false
        // 原生广告刷新
        var nativeAdRefreshSt = false
        val mmkvSt by lazy {
            //启用mmkv的多进程功能
            MMKV.mmkvWithID("SecondTranslation", MMKV.MULTI_PROCESS_MODE)
        }
        //当日日期
        var adDateSt = ""
        /**
         * 判断是否是当天打开
         */
        fun isAppOpenSameDaySt() {
            adDateSt = mmkvSt.decodeString(Constant.CURRENT_ST_DATE, "").toString()
            if (adDateSt == "") {
                MmkvUtils.set(Constant.CURRENT_ST_DATE, CalendarUtils.formatDateNow())
            } else {
                if (CalendarUtils.dateAfterDate(adDateSt, CalendarUtils.formatDateNow())) {
                    MmkvUtils.set(Constant.CURRENT_ST_DATE, CalendarUtils.formatDateNow())
                    MmkvUtils.set(Constant.CLICKS_ST_COUNT, 0)
                    MmkvUtils.set(Constant.SHOW_ST_COUNT, 0)
                }
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
//        initCrash()
        setActivityLifecycleSt(this)
        MobileAds.initialize(this) {}
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        if (ProcessUtils.isMainProcess()) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
            Firebase.initialize(this)
            FirebaseApp.initializeApp(this)
            XUI.init(this) //初始化UI框架
            XUtil.init(this)
            LiveEventBus
                .config()
                .lifecycleObserverAlwaysActive(true)
            //是否开启打印日志
            KLog.init(BuildConfig.DEBUG)
        }
//        Core.init(this, MainActivity::class)
//        sendTimerInformation()
        isAppOpenSameDaySt()
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        nativeAdRefreshSt =true
        job_ec?.cancel()
        job_ec = null
        //从后台切过来，跳转启动页
        if (whetherBackgroundSt && !isBackDataSt) {
            jumpGuidePage()
        }
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStopState(){
        job_ec = GlobalScope.launch {
            whetherBackgroundSt = false
            delay(3000L)
            whetherBackgroundSt = true
            ad_activity_ec?.finish()
            ActivityUtils.getActivity(StartActivity::class.java)?.finish()
        }
    }
    /**
     * 跳转引导页
     */
    private fun jumpGuidePage(){
        whetherBackgroundSt = false
        val intent = Intent(top_activity_ec, StartActivity::class.java)
        intent.putExtra(Constant.RETURN_ST_CURRENT_PAGE, true)
        top_activity_ec?.startActivity(intent)
    }
    fun setActivityLifecycleSt(application: Application) {
        //注册监听每个activity的生命周期,便于堆栈式管理
        application.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                AppManagerMVVM.get().addActivity(activity)
                if (activity !is AdActivity) {
                    top_activity_ec = activity
                } else {
                    ad_activity_ec = activity
                }
                KLog.v("Lifecycle", "onActivityCreated" + activity.javaClass.name)
            }

            override fun onActivityStarted(activity: Activity) {
                KLog.v("Lifecycle", "onActivityStarted" + activity.javaClass.name)
                if (activity !is AdActivity) {
                    top_activity_ec = activity
                } else {
                    ad_activity_ec = activity
                }
                flag++
                isBackDataSt = false
            }

            override fun onActivityResumed(activity: Activity) {
                KLog.v("Lifecycle", "onActivityResumed=" + activity.javaClass.name)
                if (activity !is AdActivity) {
                    top_activity_ec = activity
                }
            }

            override fun onActivityPaused(activity: Activity) {
                if (activity is AdActivity) {
                    ad_activity_ec = activity
                } else {
                    top_activity_ec = activity
                }
                KLog.v("Lifecycle", "onActivityPaused=" + activity.javaClass.name)
            }

            override fun onActivityStopped(activity: Activity) {
                flag--
                if (flag == 0) {
                    isBackDataSt = true
                }
                KLog.v("Lifecycle", "onActivityStopped=" + activity.javaClass.name)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                KLog.v("Lifecycle", "onActivitySaveInstanceState=" + activity.javaClass.name)

            }

            override fun onActivityDestroyed(activity: Activity) {
                AppManagerMVVM.get().removeActivity(activity)
                KLog.v("Lifecycle", "onActivityDestroyed" + activity.javaClass.name)
                ad_activity_ec = null
                top_activity_ec = null
            }
        })
    }
}