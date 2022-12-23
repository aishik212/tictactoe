package simpleapps.tictactoe.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.avatarfirst.avatargenlib.AvatarGenerator
import simpleapps.tictactoe.R
import simpleapps.tictactoe.databinding.LeaderboardRowLayoutBinding
import simpleapps.tictactoe.models.ScoreModel

class LeaderAdapter(val list: List<ScoreModel>) : RecyclerView.Adapter<LeaderAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LeaderboardRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = list.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LeaderboardRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scoreModel = list[position]
        val binding = holder.binding
        binding.cardView2
        val name = scoreModel.name
        binding.userImage.setImageBitmap(
            getTextImage(
                binding.root.context, name?.get(0).toString()
            )
        )
        binding.userName.text = name
        binding.userRank.text = scoreModel.score + " Points"
    }

    private fun getTextImage(context: Context, label: String): Bitmap? {
        return AvatarGenerator.AvatarBuilder(context).setLabel(label).setAvatarSize(120)
            .setTextSize(30).toSquare().toCircle().setBackgroundColor(
                ContextCompat.getColor(
                    context, R.color.primaryDarkColor
                )
            ).build().bitmap
    }

}