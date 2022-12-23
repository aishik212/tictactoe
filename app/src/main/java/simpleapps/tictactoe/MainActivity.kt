package simpleapps.tictactoe

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.allViews
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.simpleapps.admaster.AdUtils
import com.suddenh4x.ratingdialog.AppRating
import com.suddenh4x.ratingdialog.preferences.MailSettings
import com.suddenh4x.ratingdialog.preferences.RatingThreshold
import de.hdodenhof.circleimageview.CircleImageView
import simpleapps.tictactoe.ChangeBoardColor.Companion.purchases
import simpleapps.tictactoe.ChangeBoardColor.Companion.purchasesUpdatedListener
import simpleapps.tictactoe.LeaderboardActivity.Companion.clickOnline
import simpleapps.tictactoe.Utils.G_CODE
import simpleapps.tictactoe.Utils.LoginMethod
import simpleapps.tictactoe.Utils.login
import simpleapps.tictactoe.Utils.mAuth
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), View.OnClickListener {
    var plyr1: EditText? = null
    var plyr2: EditText? = null
    var difficulty: Spinner? = null
    var player1: CharSequence = "Player 1"
    var player2: CharSequence = "Player 2"
    var cloneplayer2: CharSequence? = null
    var p1x: RadioButton? = null
    var p1o: RadioButton? = null
    var p2x: RadioButton? = null
    var p2o: RadioButton? = null
    var singleplayer: RadioButton? = null
    var twoplayer: RadioButton? = null
    var player1ax = true
    var selectedSinglePlayer = true
    var easy = true
    var medium = false
    var hard = false
    var impossible = false
    var checkboxClickListener = View.OnClickListener { view ->
        val checked = (view as RadioButton).isChecked
        if (checked) {
            val id = view.getId()
            when (id) {
                R.id.player1x -> {
                    p1o!!.isChecked = false
                    p2x!!.isChecked = false
                    p2o!!.isChecked = true
                    player1ax = true
                }
                R.id.player1o -> {
                    p1x!!.isChecked = false
                    p2o!!.isChecked = false
                    p2x!!.isChecked = true
                    player1ax = false
                }
                R.id.player2x -> {
                    p2o!!.isChecked = false
                    p1x!!.isChecked = false
                    p1o!!.isChecked = true
                    player1ax = false
                }
                R.id.player2o -> {
                    p2x!!.isChecked = false
                    p1o!!.isChecked = false
                    p1x!!.isChecked = true
                    player1ax = true
                }
                R.id.splayer -> {
                    twoplayer!!.isChecked = false
                    selectedSinglePlayer = true
                    cloneplayer2 = player2
                    plyr2!!.setText("CPU")
                    plyr2!!.isEnabled = false
                    plyr1!!.imeOptions = EditorInfo.IME_ACTION_DONE
                    plyr1!!.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE)
                    difficulty!!.isEnabled = true
                }
                R.id.tplayer -> {
                    singleplayer!!.isChecked = false
                    selectedSinglePlayer = false
                    plyr2!!.setText(cloneplayer2)
                    plyr2!!.isEnabled = true
                    plyr1!!.imeOptions = EditorInfo.IME_ACTION_NEXT
                    plyr1!!.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_NEXT)
                    difficulty!!.isEnabled = false
                }
            }
        } else {
            when (view.getId()) {
                R.id.player1x -> {
                    p1o!!.isChecked = true
                    p2x!!.isChecked = true
                    p2o!!.isChecked = false
                    player1ax = false
                }
                R.id.player1o -> {
                    p1x!!.isChecked = true
                    p2o!!.isChecked = true
                    p2x!!.isChecked = false
                    player1ax = true
                }
                R.id.player2x -> {
                    p2o!!.isChecked = true
                    p1x!!.isChecked = true
                    p1o!!.isChecked = false
                    player1ax = true
                }
                R.id.player2o -> {
                    p2x!!.isChecked = true
                    p1o!!.isChecked = true
                    p1x!!.isChecked = false
                    player1ax = false
                }
                R.id.splayer -> {
                    twoplayer!!.isChecked = true
                    selectedSinglePlayer = false
                    plyr2!!.setText(cloneplayer2)
                    difficulty!!.isEnabled = false
                    plyr1!!.imeOptions = EditorInfo.IME_ACTION_NEXT
                    plyr1!!.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_NEXT)
                }
                R.id.tplayer -> {
                    singleplayer!!.isChecked = true
                    selectedSinglePlayer = true
                    plyr2!!.setText("CPU")
                    plyr1!!.imeOptions = EditorInfo.IME_ACTION_DONE
                    plyr1!!.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE)
                    difficulty!!.isEnabled = true
                }
            }
        }
    }
    lateinit var login_view: View
    lateinit var loggedInView: View
    lateinit var searchIncludeLayout: View
    lateinit var loggedInTv: TextView
    lateinit var loggedInImv: CircleImageView
    lateinit var online_play_btn: Button
    lateinit var cancel_game_btn: Button
    lateinit var p2View: View

    companion object {

        var billingClient: BillingClient? = null
        var skuDetailsList: MutableList<SkuDetails>? = null
        var skuList: ArrayList<String>? = null


        fun reshadeLines(activity: Activity) {
            val sharedPreferences = activity.getSharedPreferences("options", 0)
            var shade = sharedPreferences.getInt("color", 0)
            val findViewById = activity.findViewById<LinearLayout>(R.id.parent)
            findViewById.allViews.iterator().forEach { view ->
                val tag = view.tag
                if (tag != null && tag.toString().startsWith("line")) {
                    reshade("$tag", view, shade, activity)
                }
            }
        }

        fun reshadeLines(activity: Activity, shade: Int) {
            val findViewById = activity.findViewById<View>(R.id.parent)
            findViewById.allViews.iterator().forEach { view ->
                val tag = view.tag
                if (tag != null && tag.toString().startsWith("line")) {
                    reshade("$tag", view, shade, activity)
                }
            }
        }

        private fun reshade(tag: String, view: View, shade: Int, activity: Activity) {
            val drawableLimit = 4
            if (shade <= drawableLimit) {
                val s = tag + (shade + 1)
                val identifier = activity.resources.getIdentifier(
                    s, "drawable", activity.packageName
                )
                setVBG(view, ResourcesCompat.getDrawable(activity.resources, identifier, null))
            } else {
                when (shade) {
                    drawableLimit + 1 -> view.setBackgroundColor(Color.BLACK)
                    drawableLimit + 2 -> view.setBackgroundColor(Color.DKGRAY)
                    drawableLimit + 3 -> view.setBackgroundColor(Color.GREEN)
                    drawableLimit + 4 -> view.setBackgroundColor(
                        ContextCompat.getColor(
                            activity, R.color.primaryColor
                        )
                    )
                    drawableLimit + 5 -> view.setBackgroundColor(
                        ContextCompat.getColor(
                            activity, R.color.secondaryColor
                        )
                    )
                }
            }
        }

        private fun setVBG(view: View, drawable: Drawable?) {
            view.background = drawable
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //apply the animation ( fade In ) to your LAyout
        val options = getSharedPreferences("options", 0)
        openCount = options?.getInt("open_count", 0) ?: 0
        options?.edit()?.putInt("open_count", ++openCount)?.apply()
/*
        if (openCount > 0 && (openCount + 1) % 2 == 1) {
            showAppOpenAd(this)
        }
*/
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(false)
        } catch (e: Exception) {

        }

        initializeRemoteConfig()
        if (intent.getBooleanExtra("EXIT", false)) {
            finish()
        }
        addItemToDifficultySpinner()
        p2View = findViewById(R.id.p2_view)
        login_view = findViewById(R.id.login_view)
        loggedInView = findViewById(R.id.loggedin_ll)
        loggedInTv = findViewById(R.id.loggedin_tv)
        loggedInImv = findViewById(R.id.loggedin_imv)
        online_play_btn = findViewById(R.id.startOnline)
        searchIncludeLayout = findViewById(R.id.search_include_layout)
        cancel_game_btn = findViewById(R.id.cancel_game_btn)
        cancel_game_btn.setOnClickListener(this)
        plyr1 = findViewById<View>(R.id.playerone) as EditText
        plyr2 = findViewById<View>(R.id.playertwo) as EditText
        p1x = findViewById<View>(R.id.player1x) as RadioButton
        p1o = findViewById<View>(R.id.player1o) as RadioButton
        p2x = findViewById<View>(R.id.player2x) as RadioButton
        p2o = findViewById<View>(R.id.player2o) as RadioButton
        singleplayer = findViewById<View>(R.id.splayer) as RadioButton
        twoplayer = findViewById<View>(R.id.tplayer) as RadioButton
        p1x!!.setOnClickListener(checkboxClickListener)
        p1o!!.setOnClickListener(checkboxClickListener)
        p2x!!.setOnClickListener(checkboxClickListener)
        p2o!!.setOnClickListener(checkboxClickListener)
        val radiogroup = findViewById<RadioGroup>(R.id.rgroup)
        radiogroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.tplayer) {
                p2View.visibility = VISIBLE
                try {
                    online_play_btn.visibility = GONE
                } catch (e: Exception) {

                }
            } else {
                p2View.visibility = GONE
                try {
                    online_play_btn.visibility = VISIBLE
                } catch (e: Exception) {

                }
            }
        }
        singleplayer!!.setOnClickListener(checkboxClickListener)
        twoplayer!!.setOnClickListener(checkboxClickListener)
        difficulty!!.isEnabled = false
        p1x!!.isChecked = true
        p2o!!.isChecked = true
        mAuth = FirebaseAuth.getInstance()
        if (mAuth != null) {
            database = Utils.getDatabase(this@MainActivity)
            uid = mAuth?.uid.toString()
        }
        plyr1!!.addTextChangedListener(object : TextWatcher {
            /*this code take player1's name characterwise i.e it takes one character at a time and
                                                                                         saved to string variable player1*/
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                player1 = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        plyr2!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                player2 = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        if (checkLogin()) {
            if (mAuth != null) {
                updateUI(mAuth!!.currentUser, true)
            } else {
                online_play_btn.text = "Login to Play Online"
            }
        } else {
            online_play_btn.text = "Login to Play Online"
        }
        /*val gifView =
            findViewById<FrameLayout>(R.id.gamel1).findViewWithTag<ImageView>("giphy")
        Glide.with(applicationContext)
            .load(ContextCompat.getDrawable(applicationContext, R.drawable.banner8)).into(gifView)*/
//        online_play_btn.performClick()
//        findViewById<Button>(R.id.change_board).performClick()

        billingClient =
            BillingClient.newBuilder(applicationContext).setListener(purchasesUpdatedListener)
                .enablePendingPurchases().build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    skuList = ArrayList<String>()
                    skuList?.add("pink_marble")
                    skuList?.add("cheetah_stripe")
                    skuList?.add("special_stripe")
                    skuList?.add("neon_stripe")
                    val params = SkuDetailsParams.newBuilder()
                    if (skuList != null) {
                        params.setSkusList(skuList!!).setType(INAPP)
                    }
                    billingClient?.querySkuDetailsAsync(
                        params.build()
                    ) { _, p1 -> skuDetailsList = p1 }
                    billingClient?.queryPurchasesAsync(INAPP) { _, p1 ->
                        purchases = p1

/*
                        if (BuildConfig.DEBUG) {
                            p1.forEach {
                                val consumeParams =
                                    ConsumeParams.newBuilder()
                                        .setPurchaseToken(it.purchaseToken)
                                        .build()
                                billingClient?.consumeAsync(
                                    consumeParams
                                ) { _, _ ->
                                    Log.d(
                                        "texts",
                                        "onBillingSetupFinished: "
                                    )
                                }
                            }
                        }
*/
                    }

                }
            }

            override fun onBillingServiceDisconnected() {

            }
        })
        loadExitIAD()
        /*showBannerAd(
            this,
            getString(R.string.BasicBannerId)
        )
        showBannerAd(this, getString(R.string.BasicBannerId))*/
        AdUtils.showAdFromChoices(findViewById(R.id.bannerAdFrame), this, lifecycle)
        if (BuildConfig.DEBUG) {
            findViewById<Button>(R.id.leaderboard).performClick()
        }
    }

    private fun loadExitIAD() {
        InterstitialAd.load(this,
            getString(R.string.ExitIntersId),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    iad = p0
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)

                }
            })
    }

    var iad: InterstitialAd? = null
    override fun onBackPressed() {
        val textView = findViewById<TextView>(R.id.loading_tv)
        if (iad != null) {
            try {
                textView.visibility = VISIBLE
            } catch (e: Exception) {

            }

            iad?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    try {
                        textView.visibility = GONE
                    } catch (e: Exception) {

                    }
                    showExitDialog()
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    try {
                        textView.visibility = GONE
                    } catch (e: Exception) {

                    }
                    showExitDialog()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    try {
                        textView.visibility = GONE
                    } catch (e: Exception) {

                    }
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({ iad?.show(this@MainActivity) }, 1500)
        } else {
            try {
                textView.visibility = GONE
            } catch (e: Exception) {

            }
            showExitDialog()
        }
    }

    //    @Override
    //    public boolean onCreateOptionsMenu(Menu menu) {
    //        // Inflate the menu; this adds items to the action bar if it is present.
    //        getMenuInflater().inflate(R.menu.menu_main, menu);
    //        return true;
    //    }

    lateinit var dialog: Dialog

    private fun showExitDialog() {
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_layout_exit)
        /*showBannerAd(
            this,
            getString(R.string.ExitDialogBannerId),
            dialog.findViewById(R.id.dialogbannerAdFrame)
        )*/
        AdUtils.showAdFromChoices(dialog.findViewById(R.id.dialogbannerAdFrame), this, lifecycle)
        dialog.setCancelable(false)
        dialog.show()
        val exit: Button = dialog.findViewById(R.id.yes_button)
        val dismiss: Button = dialog.findViewById(R.id.no_button)
        exit.setOnClickListener {
            finish()
        }
        dismiss.setOnClickListener {
            dialog.dismiss()
        }
    }

    lateinit var customTheme: AppRating.Builder
    override fun onResume() {
        super.onResume()
        val gameCount = sharedPreferences.getInt("gameCount", 0)
        Log.d("texts", "onResume: " + gameCount)
        if (gameCount > 2) {
            val mailSettings = MailSettings(
                "simpleappsofficial@gmail.com",
                "Bug Report ${getString(R.string.app_name)}",
                "Please Enter Your Feedback Here \n"
            )

            val bundle = Bundle()
            customTheme = AppRating.Builder(this).setMinimumLaunchTimes(0).setMinimumDays(0)
                .setMinimumLaunchTimesToShowAgain(0).setMinimumDaysToShowAgain(0)
                .setShowOnlyFullStars(true).setMailSettingsForFeedbackDialog(mailSettings)
                .setRateLaterButtonClickListener {
                    bundle.putString(EventKeys.RATING.name, "RATE LATER")
                    logEvent(EventKeys.RATING.name, bundle)
                }.setNoFeedbackButtonClickListener {
                    bundle.putString(EventKeys.RATING.name, "NO FEEDBACK")
                    logEvent(EventKeys.RATING.name, bundle)
                }.setConfirmButtonClickListener { userRating ->
                    bundle.putString(
                        EventKeys.RATING.name, "RATE - ${userRating.roundToInt()}"
                    )
                    logEvent(EventKeys.RATING.name, bundle)
                }.setGoogleInAppReviewCompleteListener {
                    bundle.putString(
                        EventKeys.RATING.name, "REVIEWED_DIALOG"
                    )
                    logEvent(EventKeys.RATING.name, bundle)
                }.setRatingThreshold(RatingThreshold.FOUR)
                .setCustomTheme(R.style.MyAlertDialogTheme)
            customTheme.showIfMeetsConditions()
        }
        if (clickOnline) {
            clickOnline = false
            findViewById<Button>(R.id.startOnline).performClick()
        }
    }

    enum class EventKeys {
        RATING
    }

    val sharedPreferences: SharedPreferences by lazy { getSharedPreferences(packageName, 0) }

    private fun logGameEvent(gameType: String) {
        val gameCount = sharedPreferences.getInt("gameCount", 0) + 1
        sharedPreferences.edit().putInt("gameCount", gameCount).apply()

        val bundle = Bundle()
        bundle.putString("game_type", gameType)
        bundle.putInt("game_count", gameCount)
        FirebaseAnalytics.getInstance(applicationContext).logEvent("game_type", bundle)
    }

    private fun logEvent(eventName: String, bundle: Bundle) {
        FirebaseAnalytics.getInstance(applicationContext).logEvent(eventName, bundle)
    }


    override fun onDestroy() {
        super.onDestroy()
        sanitize_user_db(database, uid)
        removeWaitListener()
    }

    private fun initializeRemoteConfig() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

            }
        }.addOnFailureListener {}
    }

    fun addItemToDifficultySpinner() {
        difficulty = findViewById<View>(R.id.difficulty) as Spinner
        val list: MutableList<String> = ArrayList()
        list.add("Easy")
        list.add("Medium")
        list.add("Hard")
        list.add("Impossible")
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val difficulty1 = difficulty
        if (difficulty1 != null) {
            difficulty1.adapter = dataAdapter
            try {
                difficulty1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        try {
                            if (parent != null) {
                                when (parent.getItemAtPosition(position).toString()) {
                                    "Easy" -> {
                                        easy = true
                                        medium = false
                                        hard = false
                                        impossible = false
                                    }
                                    "Medium" -> {
                                        easy = false
                                        medium = true
                                        hard = false
                                        impossible = false
                                    }
                                    "Hard" -> {
                                        easy = false
                                        medium = false
                                        hard = true
                                        impossible = false
                                    }
                                    "Impossible" -> {
                                        easy = false
                                        medium = false
                                        hard = false
                                        impossible = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                        }

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        medium = true
                        easy = false
                        hard = false
                        impossible = false
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    fun loginClick(view: View) {
        if (view.id == R.id.login_google) {
            login(LoginMethod.G, this)
        } else {
            login(LoginMethod.F, this)
        }
    }

    fun startgame(view: View?) {
        if (!selectedSinglePlayer) if (player2.isEmpty()) player2 = "player 2"
        if (player1.isEmpty()) player1 = "player 1"
        val players = arrayOf(player1, player2)
        val i = Intent(this, Afterstart::class.java)
        i.putExtra("easy", easy)
        i.putExtra("medium", medium)
        i.putExtra("hard", hard)
        i.putExtra("impossible", impossible)
        i.putExtra("playersnames", players)
        i.putExtra("player1ax", player1ax)
        i.putExtra("selectedsingleplayer", selectedSinglePlayer)
        if (selectedSinglePlayer) {
            logGameEvent("offline_single")
        } else {
            logGameEvent("offline_double")
        }
        findViewById<View>(R.id.startOnline).isEnabled = true
        startActivity(i)
    }


    fun startRandomCPUgame() {
        hideLoader()
        sanitize_user_db(database, uid)
        val i = Intent(this, Afterstart::class.java)
        easy = false
        medium = false
        hard = false
        impossible = false
        when ((Math.random() * 4).toInt()) {
            0 -> easy = true
            1 -> medium = true
            2 -> hard = true
            3 -> impossible = true
            else -> medium = true
        }
        if (!selectedSinglePlayer && player2.isEmpty()) {
            player2 = "player 2"
        }
        if (player1.isEmpty()) {
            player1 = "player 1"
        }


        val players = arrayOf(mAuth?.currentUser?.email ?: "You", "Other Player")
        i.putExtra("easy", easy)
        i.putExtra("medium", medium)
        i.putExtra("hard", hard)
        i.putExtra("impossible", impossible)
        i.putExtra("playersnames", players)
        i.putExtra("player1ax", true)
        i.putExtra("selectedsingleplayer", true)
        i.putExtra("type", "ONLINE_BOT")
        logGameEvent("online_bot")
        findViewById<View>(R.id.startOnline).isEnabled = true
        startActivity(i)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (G_CODE == requestCode) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                }
            } catch (e: ApiException) {
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth = FirebaseAuth.getInstance()
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    updateUI(user, true)
                } else {
                    // If sign in fails, display a message to the user.
                    updateUI(null, false)
                }
            }

    }

    private fun updateUI(user: FirebaseUser?, loggedIn: Boolean) {
        if (user != null) {
            if (!loggedIn) {
                Toast.makeText(this, "Unable To Login", Toast.LENGTH_SHORT).show()
            } else {
                online_play_btn.text = "Play Online"
                login_view.visibility = GONE
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                login_view.visibility = GONE
                loggedInView.visibility = VISIBLE
                val image = user.photoUrl
                val name = user.displayName
                Glide.with(applicationContext).load(image).into(loggedInImv)
                loggedInTv.text = "Playing as $name"
                plyr1?.setText(name)
                plyr1?.isEnabled = false
                plyr1?.visibility = GONE
            }
        }
    }


    var openCount = 0

    override fun onClick(p0: View?) {
        if (p0 != null) {
            when (p0.id) {
                R.id.leaderboard -> {
                    val b = Bundle()
                    b.putString("clickedOn", "leaderboard")
                    logEvent("click", b)
                    startActivity(Intent(this, LeaderboardActivity::class.java))
                }
                R.id.change_board -> {
                    val b = Bundle()
                    b.putString("clickedOn", "change")
                    b.putInt("openCount", openCount)
                    logEvent("click", b)
                    startActivity(Intent(this, ChangeBoardColor::class.java))
                }
                R.id.startOnline -> {
                    //True if logged in
                    if (checkLogin()) {
                        p0.isEnabled = false
                        searchUser()
                    } else {
//                        if not logged in
                        login_view.visibility = VISIBLE
                    }
                }
                R.id.cancel_game_btn -> {
                    findViewById<View>(R.id.startOnline).isEnabled = true
                    hideLoader()
                    sanitize_user_db(database, uid)
                }
                R.id.tc_tv -> {
                    try {
                        val intent = Intent(ACTION_VIEW)
                        intent.data = Uri.parse("https://simpleapps-6a092.web.app/KK/pp.html")
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(applicationContext, "No Browser Found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun checkLogin(): Boolean {
        //True if logged in
        return mAuth != null && mAuth!!.currentUser != null
    }

    lateinit var uid: String
    lateinit var database: DatabaseReference
    private lateinit var listener: ValueEventListener

    private fun searchUser() {
        if (mAuth != null) {
            uid = (mAuth?.currentUser?.uid) + ""
            sanitize_user_db(database, uid)
            listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val displayName = mAuth?.currentUser?.displayName ?: "Anonymous"
                    val waitingRef = database.child("waiting")
                    if (snapshot.childrenCount > 0) {
                        val first = snapshot.children.first()
                        val key = first.key.toString()
                        val name = first.child("name").value.toString()
                        val gameId = uid + key
                        val gameRef = database.child("game")
                        val matchRef = database.child("matches")
                        waitingRef.child(key).removeValue()
                        gameRef.child(gameId).removeValue()
                        val matchData = hashMapOf<String, String>()
                        matchData["gameid"] = gameId
                        matchData["p1"] = "$displayName(p1)"
                        matchData["p2"] = "$name(p2)"
                        matchRef.child(key).setValue(matchData).addOnSuccessListener {
                            matchRef.child(uid).setValue(matchData).addOnSuccessListener {
                                checkForMatches()
                                showLoader()
                            }
                        }
                        removeWaitListener()
                    } else {
                        val uid1 = mAuth?.currentUser?.uid
                        val child1 = waitingRef.child(uid1 + "").child("name")
                        child1.setValue(
                            "$displayName"
                        ) { _, _ ->
                            checkForMatches()
                            showLoader()
                        }
                        /*.addOnSuccessListener {
                        Log.d("texts", "onDataChange: ")
                        checkForMatches()
                        showLoader()
                    }.addOnCompleteListener {
                        Log.d("texts", "onDataChange: COMPLETE")
                    }.addOnFailureListener {
                        Log.d("texts", "onDataChange: " + it.localizedMessage)
                    }*/
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            }
            database.child("waiting").addListenerForSingleValueEvent(listener)
        }
    }

    private fun showLoader() {
        searchIncludeLayout.visibility = VISIBLE
        /*showBannerAd(
            this,
            getString(R.string.BasicBannerId),
            (findViewById(R.id.searchBannerAdFrame))
        )*/
        AdUtils.showAdFromChoices(findViewById(R.id.adView), this, lifecycle)
        startLoader()
    }

    var ctdRandom: CountDownTimer? = null
    private fun startLoader() {
        ctdRandom = object : CountDownTimer(if (BuildConfig.DEBUG) 5000 else 15000, 1000) {
            override fun onTick(p0: Long) {
                val l = p0 / 1000
                if (l < 2L) {
                    ctdRandom?.cancel()
                    startRandomCPUgame()
                }
            }

            override fun onFinish() {

            }

        }
        ctdRandom?.start()
    }

    private fun removeWaitListener() {
        try {
            database.child("waiting").removeEventListener(listener)
        } catch (e: Exception) {
        }

    }

    private fun hideLoader() {
        ctdRandom?.cancel()
        searchIncludeLayout.visibility = GONE
        try {
            child?.removeEventListener(value!!)
        } catch (e: Exception) {
        }
    }


    private fun sanitize_user_db(
        database: DatabaseReference, uid: String
    ) {
        try {
            database.child("waiting").child(uid).removeValue().addOnFailureListener {}
            database.child("matches").child(uid).removeValue()
        } catch (e: Exception) {
        }
    }

    var child: DatabaseReference? = null
    var value: ValueEventListener? = null

    private fun checkForMatches() {
        child = Utils.getDatabase(this@MainActivity).child("matches").child(mAuth?.uid + "")
        value = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    child?.removeEventListener(this)
                    hideLoader()
                    ctdRandom?.cancel()
                    val gameID = snapshot.child("gameid").value
                    val p1 = snapshot.child("p1").value.toString()
                    val p2 = snapshot.child("p2").value.toString()
                    if (!selectedSinglePlayer) if (player2.isEmpty()) player2 = "player 2"
                    if (player1.isEmpty()) player1 = "player 1"
                    val i = Intent(this@MainActivity, AfterstartOnline::class.java)
                    i.putExtra("easy", easy)
                    i.putExtra("medium", medium)
                    i.putExtra("hard", hard)
                    i.putExtra("impossible", impossible)
                    i.putExtra(
                        "playersnames", arrayOf(p1, p2)
                    )
                    if (gameID.toString().startsWith(mAuth?.currentUser?.uid.toString())) {
                        i.putExtra("player1ax", true)
                    } else {
                        i.putExtra("player1ax", false)
                    }
                    i.putExtra("selectedsingleplayer", false)
                    i.putExtra("gameId", "$gameID")
                    logGameEvent("online")
                    findViewById<View>(R.id.startOnline).isEnabled = true
                    startActivity(i)
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        }
        try {
            if (value != null) {
                child?.addValueEventListener(value as ValueEventListener)
            }
        } catch (e: Exception) {
        }
    }

}