package com.juancpinillar.justpi.ui.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.juancpinillar.justpi.R
import com.juancpinillar.justpi.getDigitDrawableId
import com.juancpinillar.justpi.model.PiGame
import kotlinx.android.synthetic.main.item_digit.view.digitImageView

class DigitAdapter (
        private val context: Context,
        private val game: PiGame,
        private val onDigitLongClickListener: OnDigitLongClickListener) :
        RecyclerView.Adapter<DigitAdapter.DigitViewHolder>() {

    interface OnDigitLongClickListener {
        fun onDigitLongClick(position: Int)
    }

    inner class DigitViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val digitImageView: ImageView = view.digitImageView
        init {
            digitImageView.setOnLongClickListener {
                onDigitLongClickListener.onDigitLongClick(adapterPosition)
                true
            }
        }

        fun bindDigit(position: Int) {
            val digit: String = game[position]
            Glide.with(context).load(getDigitDrawableId(context, digit)).into(digitImageView)
            val digitColorId: Int = when {
                position > game.position -> R.color.colorPrimary
                position == game.position -> R.color.colorPositive
                else -> R.color.colorSecondary
            }
            digitImageView.setColorFilter(ContextCompat.getColor(context, digitColorId))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DigitViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_digit, parent, false)
        return DigitViewHolder(view)
    }

    override fun getItemCount(): Int {
        return game.numberOfDigits
    }

    override fun onBindViewHolder(holder: DigitViewHolder, position: Int) {
        holder.bindDigit(position)
    }
}