package com.dsk.musicbuddy.ui.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dsk.musicbuddy.ui.view.LibraryFragment
import com.dsk.musicbuddy.ui.view.PlayListFragment

//class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
//
//    override fun createFragment(position: Int): Fragment {
//        return when (position) {
//            0 -> LibraryFragment()
//            1 -> PlayListFragment()
//            else -> Fragment()
//        }
//    }
//
//    override fun getItemCount(): Int = 2
//}

class ViewPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val tabCount = 5

    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        when (position) {
            0 -> fragment = LibraryFragment()
            1 -> fragment = PlayListFragment()
//            2 -> fragment = FragmentC()
//            3 -> fragment = FragmentD()
//            4 -> fragment = FragmentE()
            else -> Fragment()
        }

        return fragment!!
    }

    override fun getCount(): Int {
        return tabCount
    }

//    override fun getPageTitle(position: Int): CharSequence {
//        return "Tab " + (position + 1)
//    }

    var fragments = arrayOfNulls<Fragment>(5)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val createdFragment = super.instantiateItem(container, position) as Fragment
        if (fragments.size < position) {
            try {
                fragments[position] = createdFragment
            } catch (e: ArrayIndexOutOfBoundsException) {
                e.printStackTrace()
            }
        }
        return createdFragment
    }
}