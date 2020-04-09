package com.sungbin.multitranslator

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter


class ViewPagerAdapter constructor(act: Activity, result: ResultItems): PagerAdapter() {

    var ctx: Activity = act
    var resultItems: ResultItems = result

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.viewpager_result_view, container, false)
        val tvResult = view.findViewById<TextView>(R.id.tv_result)
        when(position){
            0 -> tvResult.text = resultItems.papago
            1 -> tvResult.text = resultItems.google
            else -> tvResult.text = resultItems.daum
        }
        container.addView(view)
        return view
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        obj: Any
    ) {
        container.removeView(obj as View)
    }

    override fun getCount(): Int {
        return 3
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj as View
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "파파고"
            1 -> "구글"
            else -> "다음"
        }
    }
}