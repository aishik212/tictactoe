package simpleapps.tictactoe

import android.app.Activity
import android.content.Context
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
        fun showBannerAd() {

        }
    }
}