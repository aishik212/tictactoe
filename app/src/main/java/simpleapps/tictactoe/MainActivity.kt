package simpleapps.tictactoe

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
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
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import simpleapps.tictactoe.Utils.G_CODE
import simpleapps.tictactoe.Utils.LoginMethod
import simpleapps.tictactoe.Utils.TAG
import simpleapps.tictactoe.Utils.adResult
import simpleapps.tictactoe.Utils.login
import simpleapps.tictactoe.Utils.mAuth
import java.util.*


class MainActivity : Activity(), View.OnClickListener {
    var plyr1: EditText? = null
    var plyr2: EditText? = null
    var difficulty: Spinner? = null
    var player1: CharSequence = "Player 1"
    var player2: CharSequence = "Player 2"
    var cloneplayer2: CharSequence? = null
    var p1x: CheckBox? = null
    var p1o: CheckBox? = null
    var p2x: CheckBox? = null
    var p2o: CheckBox? = null
    var singleplayer: CheckBox? = null
    var twoplayer: CheckBox? = null
    var player1ax = true
    var selectedSinglePlayer = false
    var easy = true
    var medium = false
    var hard = false
    var impossible = false
    var checkboxClickListener = View.OnClickListener { view ->
        val checked = (view as CheckBox).isChecked
        if (checked) {
            val id = view.getId()
            if (id == R.id.player1x) {
                p1o!!.isChecked = false
                p2x!!.isChecked = false
                p2o!!.isChecked = true
                player1ax = true
            } else if (id == R.id.player1o) {
                p1x!!.isChecked = false
                p2o!!.isChecked = false
                p2x!!.isChecked = true
                player1ax = false
            } else if (id == R.id.player2x) {
                p2o!!.isChecked = false
                p1x!!.isChecked = false
                p1o!!.isChecked = true
                player1ax = false
            } else if (id == R.id.player2o) {
                p2x!!.isChecked = false
                p1o!!.isChecked = false
                p1x!!.isChecked = true
                player1ax = true
            } else if (id == R.id.splayer) {
                twoplayer!!.isChecked = false
                selectedSinglePlayer = true
                cloneplayer2 = player2
                plyr2!!.setText("CPU")
                plyr2!!.isEnabled = false
                plyr1!!.imeOptions = EditorInfo.IME_ACTION_DONE
                plyr1!!.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE)
                difficulty!!.isEnabled = true
            } else if (id == R.id.tplayer) {
                singleplayer!!.isChecked = false
                selectedSinglePlayer = false
                plyr2!!.setText(cloneplayer2)
                plyr2!!.isEnabled = true
                plyr1!!.imeOptions = EditorInfo.IME_ACTION_NEXT
                plyr1!!.setImeActionLabel("NEXT", EditorInfo.IME_ACTION_NEXT)
                difficulty!!.isEnabled = false
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //apply the animation ( fade In ) to your LAyout
        initializeAds()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        initializeRemoteConfig()
        if (intent.getBooleanExtra("EXIT", false)) {
            finish()
        }
        addItemToDifficultySpinner()
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
        p1x = findViewById<View>(R.id.player1x) as CheckBox
        p1o = findViewById<View>(R.id.player1o) as CheckBox
        p2x = findViewById<View>(R.id.player2x) as CheckBox
        p2o = findViewById<View>(R.id.player2o) as CheckBox
        singleplayer = findViewById<View>(R.id.splayer) as CheckBox
        twoplayer = findViewById<View>(R.id.tplayer) as CheckBox
        p1x!!.setOnClickListener(checkboxClickListener)
        p1o!!.setOnClickListener(checkboxClickListener)
        p2x!!.setOnClickListener(checkboxClickListener)
        p2o!!.setOnClickListener(checkboxClickListener)
        singleplayer!!.setOnClickListener(checkboxClickListener)
        twoplayer!!.setOnClickListener(checkboxClickListener)
        difficulty!!.isEnabled = false
        p1x!!.isChecked = true
        p2o!!.isChecked = true
        twoplayer!!.isChecked = true
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
    }

    private fun logGameEvent(gameType: String) {
        val bundle = Bundle()
        bundle.putString("game_type", gameType)
        FirebaseAnalytics.getInstance(applicationContext).logEvent("game_type", bundle)
    }

    private fun initializeAds() {
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        MobileAds.initialize(
            this
        ) {
            Utils.AdUtils.showBannerAd(
                this,
                getString(R.string.admobBasicBannerId)
            )
        }
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
                adResult = JSONObject(remoteConfig.getString("showAds"))
                initializeAds()
            }
        }
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
            difficulty1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    try {
                        val temp = parent.getItemAtPosition(position).toString()
                        when (temp) {
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
                    } catch (e: Exception) {
                        Log.d("texts", "onItemSelected: " + e.localizedMessage)
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    medium = true
                    easy = false
                    hard = false
                    impossible = false
                }
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
        if (!selectedSinglePlayer) {
            if (player2.isEmpty()) {
                player2 = "player 2"
            }
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
        logGameEvent("online_bot")
        startActivity(i)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (G_CODE == requestCode) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                Log.d(
                    "texts",
                    "onActivityResult: " + e.localizedMessage + " " + e.message + " " + e.status + " " + e.statusCode
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth = FirebaseAuth.getInstance()
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser;
                    updateUI(user, true);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.d(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null, false);
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
                loggedInTv.text = "Playing in as $name"
            }
        }
    }

    override fun onClick(p0: View?) {
        if (p0 != null) {
            when (p0.id) {
                R.id.startOnline -> {
                    //True if logged in
                    if (checkLogin()) {
                        searchUser()
                    } else {
//                        if not logged in
                        login_view.visibility = VISIBLE
                    }
                }
                R.id.cancel_game_btn -> {
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
                        waitingRef
                            .child(uid1 + "")
                            .child("name").setValue(
                                displayName
                            ).addOnSuccessListener {
                                checkForMatches()
                                showLoader()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("texts", "onCancelled: " + error.details)
                }
            }
            database.child("waiting")
                .addListenerForSingleValueEvent(listener)
        }
    }

    private fun showLoader() {
        searchIncludeLayout.visibility = VISIBLE
        startLoader()
    }

    var ctdRandom: CountDownTimer? = null
    private fun startLoader() {
        ctdRandom = object : CountDownTimer(15000, 1000) {
            override fun onTick(p0: Long) {
                val l = p0 / 1000
                Log.d("texts", "onTick: $p0 $l")
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
            Log.d("texts", "removeWaitListener: " + e.localizedMessage)
        }

    }

    private fun hideLoader() {
        ctdRandom?.cancel()
        searchIncludeLayout.visibility = GONE
    }


    private fun sanitize_user_db(
        database: DatabaseReference,
        uid: String
    ) {
        try {
            Log.d("texts", "sanitize_user_db: " + database.child("waiting").child(uid).ref)
            database.child("waiting").child(uid).removeValue().addOnFailureListener {
                Log.d("texts", "sanitize_user_db: " + it.localizedMessage)
            }
            database.child("matches").child(uid).removeValue()
        } catch (e: Exception) {
            Log.d("texts", "sanitize_user_db: " + e.localizedMessage)
        }
    }

    private fun checkForMatches() {
        val child = Utils.getDatabase(this@MainActivity).child("matches").child(mAuth?.uid + "")
        child.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    child.removeEventListener(this)
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
                        "playersnames",
                        arrayOf(p1, p2)
                    )
                    if (gameID.toString().startsWith(mAuth?.currentUser?.uid.toString())) {
                        i.putExtra("player1ax", true)
                    } else {
                        i.putExtra("player1ax", false)
                    }
                    i.putExtra("selectedsingleplayer", false)
                    i.putExtra("gameId", "$gameID")
                    logGameEvent("online")
                    startActivity(i)
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }
}