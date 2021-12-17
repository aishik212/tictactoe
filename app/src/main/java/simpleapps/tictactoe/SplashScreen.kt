package simpleapps.tictactoe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val w = window // in Activity's onCreate() for instance
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        showAppOpenAd()
/*
        Handler().postDelayed({
            goToMainAct()
        }, 1500)
*/
    }

    private fun goToMainAct() {
        val intent = Intent(this@SplashScreen, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun showAppOpenAd() {
        val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    ad.show(this@SplashScreen)
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            hideLoad()
                            goToMainAct()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            Handler().postDelayed({
                                hideLoad()
                            }, 850)
                            Handler().postDelayed({
                                goToMainAct()
                            }, 1050)
                        }
                    }
                }

                private fun hideLoad() {
                    findViewById<TextView>(R.id.loadtv).text = getString(R.string.load_complete)
                    findViewById<View>(R.id.loadpb).visibility = GONE
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    hideLoad()
                    goToMainAct()
                }
            }
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            this@SplashScreen, this@SplashScreen.getString(R.string.appOpenID), request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
        )
    }

}