package com.juancpinillar.justpi.ui.adapters

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.juancpinillar.justpi.R

class GamePageAdapter(private val context: Context) : PagerAdapter() {
    private enum class GamePage(val titleId: Int, val layoutId: Int) {
        PLAY(R.string.play_page_title, R.layout.page_play),
        DIGITS(R.string.digits_page_title, R.layout.page_digits)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val gamePage: GamePage = GamePage.values()[position]
        val itemView: View = LayoutInflater.from(context)
                .inflate(gamePage.layoutId, container, false)
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return GamePage.values().size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val gamePage: GamePage = GamePage.values()[position]
        return context.resources.getString(gamePage.titleId)
    }
}