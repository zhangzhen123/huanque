package com.julun.huanque

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.ui.main.LeYuanFragment
import com.julun.huanque.ui.main.MainFragment
import com.julun.huanque.ui.main.MessageFragment
import com.julun.huanque.ui.main.MineFragment

class MainActivity : AppCompatActivity() {

    private val mMainFragment = MainFragment.newInstance()
    private val mLeYuanFragment: LeYuanFragment by lazy { LeYuanFragment.newInstance() }

    private val mMessageFragment: MessageFragment by lazy { MessageFragment.newInstance() }
    private val mMineFragment: MineFragment by lazy { MineFragment.newInstance() }

    private val MAIN_FRAGMENT_INDEX = 0
    private val LEYUAN_FRAGMENT_INDEX = 1
    private val MESSAGE_FRAGMENT_INDEX = 2
    private val MINE_FRAGMENT_INDEX = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionUtils.setSessionId("37d24f40f29b4330af383c336dee8eee")//test
        setContentView(R.layout.main_activity)
        showFragmentNew(0)
    }

    /**
     * 切换fragment
     */
    private fun showFragmentNew(index: Int): Boolean {
        //切换我的收藏和个人中心时1,2前检测登录状态
        val oldFragment: Fragment? = getCurrentFragment()
        val newFragment: Fragment = getFragmentByIndex(index) ?: return false
        // 新的framgnet已经显示了（比如loginFragment，未登录时从个人中心切换到关注）
        val transaction = supportFragmentManager.beginTransaction()
        if (oldFragment != null) {
            transaction.hide(oldFragment)
        }
        if (!newFragment.isAdded) {
            transaction.add(R.id.container, newFragment, "$index")
        }
        transaction.show(newFragment).commitAllowingStateLoss()
        //使用此方式在主线程中立即执行事务队列所有事务，同步当前的状态,确保来回快速切换的时候事务不会堆积在队列中异步执行， 避免卡顿问题
        supportFragmentManager.executePendingTransactions()
        return true
    }

    /**
     * 根据index获取fragment
     */
    private fun getFragmentByIndex(index: Int): Fragment? {
        return when (index) {
            MAIN_FRAGMENT_INDEX -> mMainFragment
            LEYUAN_FRAGMENT_INDEX -> mLeYuanFragment
            MESSAGE_FRAGMENT_INDEX -> mMessageFragment
            MINE_FRAGMENT_INDEX -> mMineFragment
            else -> {
                null
            }
        }
    }

    /**
     * 获取当前的fragment
     */
    private fun getCurrentFragment(): Fragment? {
        if (mMainFragment.isVisible) {
            return mMainFragment
        }
        if (mLeYuanFragment.isVisible) {
            return mLeYuanFragment
        }
        if (mMessageFragment.isVisible) {
            return mMessageFragment
        }
        if (mMineFragment.isVisible) {
            return mMineFragment
        }
        return null
    }
}