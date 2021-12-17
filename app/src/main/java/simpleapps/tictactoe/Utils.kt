package simpleapps.tictactoe

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.greedygame.core.adview.general.AdLoadCallback
import com.greedygame.core.adview.general.GGAdview
import com.greedygame.core.models.general.AdErrors
import org.jetbrains.annotations.Nullable
import org.json.JSONObject


object Utils {
    enum class LoginMethod {
        G, F
    }


    const val G_CODE = 1;
    const val F_CODE = 2;
    const val TAG = "texts";

    @JvmStatic
    var mAuth: FirebaseAuth? = null

    @JvmStatic
    fun login(
        method: @Nullable LoginMethod?,
        mainActivity: MainActivity
    ) {
        when (method) {
            LoginMethod.G -> {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(mainActivity.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(mainActivity, gso)
                val signInIntent = googleSignInClient.signInIntent
                mainActivity.startActivityForResult(signInIntent, G_CODE)
            }
            LoginMethod.F -> {

            }
        }
    }


    @JvmStatic
    fun getDatabase(context: Context): DatabaseReference {
        var replace: String = context.packageName.replace(".", "_")
        if (BuildConfig.DEBUG) {
            replace += "_debug"
        }
        return FirebaseDatabase.getInstance("https://simpleapps-6a092-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference(replace)
    }

    @JvmStatic
    var adResult: JSONObject? = null
    var gameViewType: Long = 1
    fun setGameView(activity: Activity) {
        val view1 = activity.findViewById<View>(R.id.gamel1)
        val view2 = activity.findViewById<View>(R.id.gamel2)
        val view3 = activity.findViewById<View>(R.id.gamel3)
        val view4 = activity.findViewById<View>(R.id.gamel4)
        view1.visibility = GONE
        view2.visibility = GONE
        view3.visibility = GONE
        view4.visibility = GONE
        when (gameViewType.toInt()) {
            2 -> view2.visibility = VISIBLE
            3 -> view3.visibility = VISIBLE
            4 -> view4.visibility = VISIBLE
            else -> {
                val gifView = view1.findViewWithTag<ImageView>("giphy")
                Glide.with(activity).load(ContextCompat.getDrawable(activity, R.drawable.banner8))
                    .into(gifView)
                view1.visibility = VISIBLE
            }
        }
    }


    object AdUtils {
        @JvmStatic
        fun showAppOpenAd(activity: Activity) {
            val loadCallback: AppOpenAdLoadCallback = object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    ad.show(activity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {}
            }
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                activity, activity.getString(R.string.appOpenID), request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
            )
        }


        @JvmStatic
        fun showBannerAd(activity: Activity, adId: String) {
            try {
                val view = activity.findViewById<FrameLayout>(R.id.bannerAdFrame)
                if (adResult != null && adResult!!.getBoolean("banner")) {
                    val adView = AdView(activity)
                    adView.adUnitId = adId
                    adView.adSize = getAdSize(activity.windowManager, view, activity)
                    val adRequest = AdRequest
                        .Builder()
                        .build()
                    adView.adListener = object : AdListener() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            Log.d("texts", "onAdFailedToLoad: " + p0.message)
                            view.visibility = GONE
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            view.removeAllViews()
                            view.addView(adView)
                            view.visibility = VISIBLE
                        }
                    }
                    adView.loadAd(adRequest)
                } else {
                    view.visibility = GONE
                }
            } catch (e: Exception) {
                Log.d("texts", "showBannerAd: " + e.localizedMessage)
            }

        }

        @JvmStatic
        fun showBannerAd(activity: Activity, adId: String, v: FrameLayout) {
            try {
                if (adResult != null && adResult!!.getBoolean("banner")) {
                    val adView = AdView(activity)
                    adView.adUnitId = adId
                    adView.adSize = getAdSize(activity.windowManager, v, activity)
                    val adRequest = AdRequest
                        .Builder()
                        .build()
                    adView.adListener = object : AdListener() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            Log.d("texts", "onAdFailedToLoad: " + p0.message)
                            v.visibility = GONE
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            v.removeAllViews()
                            v.addView(adView)
                            v.visibility = VISIBLE
                        }
                    }
                    adView.loadAd(adRequest)
                } else {
                    v.visibility = GONE
                }
            } catch (e: Exception) {
                Log.d("texts", "showBannerAd: " + e.localizedMessage)
            }

        }

        private fun getAdSize(
            windowManager: WindowManager,
            ad_view_container: FrameLayout,
            activity: Activity
        ): AdSize {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = ad_view_container.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
        }


        @JvmStatic
        fun showSDKXBannerAd(activity: Activity, adId: String) {
            val v = activity.findViewById<FrameLayout>(R.id.bannerAdFrame)
            val ggAdView = GGAdview(activity).apply {
                unitId = adId  //Replace with your Ad Unit ID here
                adsMaxHeight = 250 //Value is in pixels, not in dp
            }

            val layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, 200)
            v.addView(ggAdView, layoutParams)
            ggAdView.loadAd(object : AdLoadCallback {
                override fun onAdLoaded() {
                    v.visibility = VISIBLE
                }

                override fun onAdLoadFailed(cause: AdErrors) {
                    v.visibility = GONE
                    Log.d("texts", "Ad Load Failed $cause")
                }

                override fun onUiiOpened() {
                    Log.d("texts", "Uii Opened")
                }

                override fun onUiiClosed() {
                    Log.d("texts", "Uii Closed")
                }

                override fun onReadyForRefresh() {
                    Log.d("texts", "Ad ready for refresh")
                }
            })
        }

        @JvmStatic
        fun showSDKXBannerAd(activity: Activity, adId: String, v: FrameLayout) {
            val ggAdView = GGAdview(activity).apply {
                unitId = adId  //Replace with your Ad Unit ID here
                adsMaxHeight = 250 //Value is in pixels, not in dp
            }

            val layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, 200)
            v.addView(ggAdView, layoutParams)
            ggAdView.loadAd(object : AdLoadCallback {
                override fun onAdLoaded() {
                    v.visibility = VISIBLE
                }

                override fun onAdLoadFailed(cause: AdErrors) {
                    v.visibility = GONE
                    Log.d("texts", "Ad Load Failed $cause")
                }

                override fun onUiiOpened() {
                    Log.d("texts", "Uii Opened")
                }

                override fun onUiiClosed() {
                    Log.d("texts", "Uii Closed")
                }

                override fun onReadyForRefresh() {
                    Log.d("texts", "Ad ready for refresh")
                }
            })
        }
    }
}