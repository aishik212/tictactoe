package simpleapps.tictactoe

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.simpleapps.admaster.AdUtils
import simpleapps.tictactoe.Utils.getDatabase
import simpleapps.tictactoe.adapter.LeaderAdapter
import simpleapps.tictactoe.databinding.LeaderboardActivityLayoutBinding
import simpleapps.tictactoe.models.ScoreModel

class LeaderboardActivity : AppCompatActivity() {
    companion object {
        var clickOnline = false
    }

    lateinit var inflate: LeaderboardActivityLayoutBinding
    val scoresList = mutableListOf<ScoreModel>()
    lateinit var leaderAdapter: LeaderAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflate = LeaderboardActivityLayoutBinding.inflate(layoutInflater)
        setContentView(inflate.root)
        val leaderRv = inflate.leaderRv
        leaderAdapter = LeaderAdapter(scoresList)
        leaderRv.adapter = leaderAdapter
        getScore()
        inflate.playMoreBtn.setOnClickListener {
            clickOnline = true
            finish()
        }
        AdUtils.showAdFromChoices(findViewById(R.id.adView), this, lifecycle)
    }


    private fun getScore() {
        val database = getDatabase(this)
        val child = database.child("SCOREBOARD").child("SCORES")
        child.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scoresList.clear()
                if (snapshot.childrenCount > 0) {
                    snapshot.children.iterator().forEach {
                        val value = it.getValue(ScoreModel::class.java)
                        if (value != null) {
                            value.uid = it.key
                            scoresList.add(value)
                        }
                    }
                    updateScore()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("texts", "onCancelled: " + error.message)

            }
        })
    }

    private fun updateScore() {
        val sortedBy = scoresList.sortedBy { it.score }.reversed()
        scoresList.clear()
        scoresList.addAll(sortedBy)
        val currentUser = FirebaseAuth.getInstance().currentUser
        leaderAdapter.notifyDataSetChanged()
        if (currentUser != null) {
            val i = scoresList.indexOfFirst { it.uid.equals(currentUser.uid) }
            if (i != -1) {
                inflate.rankTv.text = "${i + 1} Place"
            } else {
                inflate.rankTv.text = "NA"
            }
            inflate.currankTv.text = "Your Current Rank Is"
        } else {
            inflate.currankTv.text = "Login To View Your Ranking"
            inflate.rankTv.text = ""
        }
        try {
            scoresList.subList(0, 2)
            inflate.player1Imv.setImageBitmap(
                getTextImage(
                    this, scoresList[0].name?.get(0).toString()
                )
            )
            inflate.player2Imv.setImageBitmap(
                getTextImage(
                    this, scoresList[1].name?.get(0).toString()
                )
            )
            inflate.player3Imv.setImageBitmap(
                getTextImage(
                    this, scoresList[2].name?.get(0).toString()
                )
            )
        } catch (e: Exception) {

        }
    }

    private fun getTextImage(context: Context, label: String): Bitmap? {
        return AvatarGenerator.AvatarBuilder(context).setLabel(label).setAvatarSize(120)
            .setTextSize(30).toSquare().toCircle()
            .setBackgroundColor(ContextCompat.getColor(context, R.color.secondaryDarkColor))
            .build().bitmap
    }
}
