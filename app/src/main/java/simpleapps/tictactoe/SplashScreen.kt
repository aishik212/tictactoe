package simpleapps.tictactoe

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import simpleapps.tictactoe.Utils.AdUtils.logAdResult
import simpleapps.tictactoe.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private var loadTv: TextView? = null
    private var loadingTv: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreenBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(splashScreenBinding.root)
        val w = window // in Activity's onCreate() for instance
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        loadTv = splashScreenBinding.loadtv
        loadingTv = splashScreenBinding.loadingTv
        initializeAds()

    }

    var num = 0
    var showAd = true
    val l: Long = 6000
    private fun goToHomeAct() {
        ctd.cancel()
        val intent = Intent(this@SplashScreen, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    val ctd = object : CountDownTimer(l, 500) {
        override fun onTick(millisUntilFinished: Long) {
            loadTv?.text = "Loading"
            for (i in 0..((l - millisUntilFinished) / 1000)) {
                loadTv?.append(".")
            }
        }

        override fun onFinish() {
            showAd = false
            Log.d("texts", "onFinish: b")
            goToHomeAct()
        }
    }

    private fun initializeAds() {
        val testDeviceIds = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setTestDeviceIds(listOf("57BEA9A19CDAF54CBAA1EC9F0F138E7E")).build()
        MobileAds.setRequestConfiguration(testDeviceIds)
        MobileAds.initialize(
            this
        ) {
/*
            if (BuildConfig.DEBUG) {
                goToHomeAct()
            } else {
            }
*/
            showAppOpenAd(this)
        }


    }

    fun showAppOpenAd(activity: Activity) {
        try {
            runOnUiThread {
                ctd.start()
            }
        } catch (e: Exception) {

        }
        try {
            val request = AdRequest.Builder().build()
            runOnUiThread {
                num = 3
                when (num) {
                    0 -> {
                        AppOpenAd.load(
                            activity, activity.getString(R.string.HighappOpenID), request,
                            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback,
                        )
                    }
                    1 -> {
                        AppOpenAd.load(
                            activity, activity.getString(R.string.MedappOpenID), request,
                            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
                        )
                    }
                    2 -> {
                        AppOpenAd.load(
                            activity, activity.getString(R.string.AllappOpenID), request,
                            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
                        )
                    }
                    3 -> {
                        Log.d("texts", "showAppOpenAd: c")
                        loadSplashAd(activity, 0)
//                        goToHomeAct()
                    }
                }
                num++
            }
        } catch (e: Exception) {

        }
    }

    fun loadSplashAd(activity: Activity, adType: Int = 0) {
        if (showAd) {
            loadingTv?.visibility = VISIBLE
            val adRequest = AdRequest.Builder().build()
            var adId = activity.getString(R.string.HighRinsID)
            var adTypeText = "HIGH"

            when (adType) {
                0 -> {
                    adId =
                        activity.getString(R.string.HighRinsID)
                    adTypeText = "HIGH"
                }
                1 -> {
                    adId =
                        activity.getString(R.string.MedRinsID)
                    adTypeText = "MED"
                }
                2 -> {
                    adId =
                        activity.getString(R.string.AllRinsID)
                    adTypeText = "ALL"
                }
            }

            if (adType < 3) {
                RewardedInterstitialAd.load(
                    activity,
                    adId,
                    adRequest,
                    object : RewardedInterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: RewardedInterstitialAd) {
                            logAdResult("TTT_RIAD_$adTypeText", null, null, activity)
                            interstitialAd.fullScreenContentCallback = object :
                                FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    goToHomeAct()
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    loadSplashAd(activity, (adType + 1))
                                }
                            }
                            if (showAd) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    interstitialAd.show(
                                        activity
                                    ) { }
                                }, 1500)
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            logAdResult(
                                null,
                                "TTT_RIAD_$adTypeText",
                                loadAdError.message,
                                activity
                            )
                            loadSplashAd(activity, (adType + 1))
                        }
                    })
            } else {
                goToHomeAct()
            }
        }

    }

    private val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
        object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                logAppOpen("LOAD", null, null)
                if (showAd) {
                    ad.show(this@SplashScreen)
                    ctd.cancel()
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            logAppOpen(null, "SHOW_FAIL", p0.message)
                            showAppOpenAd(this@SplashScreen)
//                            loadSplashAd()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            logAppOpen("SHOWN", null, null)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            loadSplashAd(this@SplashScreen, 0)
                        }
                    }
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                logAppOpen(null, "FAIL", loadAdError.message)
                showAppOpenAd(this@SplashScreen)
            }
        }

    private fun logAppOpen(success: String?, fail: String?, failReason: String?) {
        when (num - 1) {
            0 -> {
                if (success != null) {
                    logAdResult("APPOPEN_HIGH_$success", null, null, this)
                } else if (fail != null) {
                    logAdResult(null, "APPOPEN_HIGH_$fail", failReason, this)
                }
            }
            1 -> {
                if (success != null) {
                    logAdResult("APPOPEN_MED_$success", null, null, this)
                } else if (fail != null) {
                    logAdResult(null, "APPOPEN_MED_$fail", failReason, this)
                }
            }
            2 -> {
                if (success != null) {
                    logAdResult("APPOPEN_ALL_$success", null, null, this)
                } else if (fail != null) {
                    logAdResult(null, "APPOPEN_ALL_$fail", failReason, this)
                }
            }
        }
    }
}