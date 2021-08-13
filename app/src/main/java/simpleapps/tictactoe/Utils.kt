package simpleapps.tictactoe

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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


    object AdUtils {
        @JvmStatic
        fun showBannerAd(activity: Activity, adId: String, view: FrameLayout) {
            Log.d("texts", "showBannerAd: $adId")
            val adView = AdView(activity)
            adView.adUnitId = adId
            adView.adSize = getAdSize(activity.windowManager, view, activity)
            val adRequest = AdRequest
                .Builder()
                .build()
            adView.loadAd(adRequest)
            view.removeAllViews()
            view.addView(adView)
            view.visibility = VISIBLE
        }

        fun getAdSize(
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