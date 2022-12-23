package simpleapps.tictactoe

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.jetbrains.annotations.Nullable


object Utils {
    enum class LoginMethod {
        G, F
    }


    const val G_CODE = 1;
    const val F_CODE = 2;

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
            else -> {

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
                activity, activity.getString(R.string.HighappOpenID), request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback
            )
        }


        @JvmStatic
        fun showBannerAd(activity: Activity, adId: String) {
            try {
                val view = activity.findViewById<FrameLayout>(R.id.bannerAdFrame)
                val adView = AdView(activity)
                view.removeAllViews()
                val textView = getLoadingView(activity)
                view.removeAllViews()
                view.setBackgroundColor(ContextCompat.getColor(activity, R.color.primaryColor))
                view.addView(textView)
                view.visibility = VISIBLE
                adView.adUnitId = adId
                adView.setAdSize(getAdSize(activity.windowManager, view, activity))
                val adRequest = AdRequest
                    .Builder()
                    .build()
                adView.adListener = object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
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
                /*if (adResult != null && adResult!!.getBoolean("banner")) {
                } else {
                    view.visibility = GONE
                }*/
            } catch (e: Exception) {
            }

        }

        private fun getLoadingView(activity: Activity): TextView {
            val textView = TextView(activity)
            textView.text = "Loading Ad..."
            textView.gravity = Gravity.CENTER
            textView.textSize = 24F
            textView.setTextColor(Color.BLACK)
            textView.setBackgroundColor(Color.WHITE)
            val layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(MATCH_PARENT, 200)
            layoutParams.setMargins(1)
            textView.layoutParams = layoutParams
            return textView
        }

        @JvmStatic
        fun showBannerAd(activity: Activity, adId: String, view: FrameLayout) {
            try {
                val adView = AdView(activity)
                adView.adUnitId = adId
                adView.setAdSize(getAdSize(activity.windowManager, view, activity))
                val adRequest = AdRequest
                    .Builder()
                    .build()
                val textView = getLoadingView(activity)
                view.removeAllViews()
                view.addView(textView)
                view.setBackgroundColor(ContextCompat.getColor(activity, R.color.primaryColor))
                view.visibility = VISIBLE
                adView.adListener = object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
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
            } catch (e: Exception) {
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


        fun logAdResult(
            adType: String?,
            errorAdType: String?,
            error: String?,
            activity: Activity?,
        ) {
            var error = error
            val b = Bundle()
            try {
                error = error!!.substring(0, 25)
            } catch (e: java.lang.Exception) {
            }
            if (adType != null) {
                b.putString("AdType", adType)
            }
            if (error != null && errorAdType != null) {
                b.putString("Error", errorAdType)
                b.putString("ErrorMessage", "$errorAdType $error")
            }
            Log.d("texts", "logAdResult: " + adType + " " + error + " " + errorAdType)
            if (activity != null) {
                FirebaseAnalytics.getInstance(activity).logEvent("AdLog", b)
                if (adType != null && adType == "AdKinowa") {
                    val bundle = Bundle()
                    bundle.putString("AdKinowa", "Success")
                    FirebaseAnalytics.getInstance(activity).logEvent("AdKinowa", bundle)
                }
            }
        }
    }
}