package simpleapps.tictactoe

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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


    object AdUtils {
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
    }
}