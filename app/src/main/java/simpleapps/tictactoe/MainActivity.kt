package simpleapps.tictactoe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import simpleapps.tictactoe.Utils.G_CODE
import simpleapps.tictactoe.Utils.LoginMethod
import simpleapps.tictactoe.Utils.TAG
import simpleapps.tictactoe.Utils.login
import simpleapps.tictactoe.Utils.mAuth
import java.util.*

import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.json.JSONObject
import simpleapps.tictactoe.Utils.adResult


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
    lateinit var loggedInTv: TextView
    lateinit var loggedInImv: CircleImageView
    lateinit var online_play_btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //apply the animation ( fade In ) to your LAyout
        initializeAds()
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
        mAuth = FirebaseAuth.getInstance()
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

    private fun initializeAds() {
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        MobileAds.initialize(
            this
        ) {
            Utils.AdUtils.showBannerAd(
                this,
                getString(R.string.admobBasicBannerId),
                findViewById(R.id.adFrame)
            )
        }
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
                Log.d("texts", "initializeRemoteConfig: $adResult")
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
        difficulty!!.adapter = dataAdapter
        difficulty!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
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
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                medium = true
                easy = false
                hard = false
                impossible = false
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
        startActivity(i)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("texts", "onActivityResult: $resultCode $requestCode")
        if (G_CODE == requestCode) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                Log.d("texts", "onActivityResult: login Success " + account.id)
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
                    Log.d(TAG, "signInWithCredential:success")
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
            }
        }
    }

    private fun checkLogin(): Boolean {
        //True if logged in
        return mAuth != null && mAuth!!.currentUser != null
    }

    private fun searchUser() {
        if (mAuth != null) {
            val database = Utils.getDatabase(this@MainActivity)
            Log.d("texts", "searchUser: " + database.ref)
            Log.d("texts", "searchUser: a")
            val uid = mAuth?.uid.toString()
            sanitize_user_db(database, uid)
            database.child("waiting")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.childrenCount > 0) {
                            val key = snapshot.children.first().key.toString()
                            val name = snapshot.children.first().child("name").value.toString()
                            database.child("waiting")
                                .child(key).removeValue()
                            val gameId = uid + key
                            database.child("game").child(gameId).removeValue()

                            var matchData = hashMapOf<String, String>()
                            matchData["gameid"] = gameId
                            matchData["p1"] = "${mAuth?.currentUser?.email}(p1)" ?: "Anonymous(p1)"
                            matchData["p2"] = "$name(p2)"
                            database.child("matches")
                                .child(key).setValue(matchData)
                            database.child("matches")
                                .child(uid).setValue(matchData)
                            database.removeEventListener(this)
                            checkForMatches()
                        } else {
                            checkForMatches()
                            Toast.makeText(
                                this@MainActivity,
                                "Please Wait for 15 Seconds, Waiting for other online players",
                                Toast.LENGTH_SHORT
                            ).show()
                            database.child("waiting")
                                .child(mAuth?.currentUser?.uid + "")
                                .child("name").setValue(
                                    mAuth?.currentUser?.email
                                        ?: "Anonymous"
                                )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("texts", "onCancelled: " + error.details)
                    }
                })
        }
    }


    private fun sanitize_user_db(
        database: DatabaseReference,
        uid: String
    ) {
        database.child("waiting").child(uid).removeValue()
        database.child("matches").child(uid).removeValue()
    }

    private fun checkForMatches() {
        val child = Utils.getDatabase(this@MainActivity).child("matches").child(mAuth?.uid + "")
        child.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    child.removeEventListener(this)
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
                    startActivity(i)
                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }
}