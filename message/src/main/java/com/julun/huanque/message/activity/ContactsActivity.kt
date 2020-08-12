package com.julun.huanque.message.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.UserDataTab
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ContactsTabType
import com.julun.huanque.common.constant.FollowStatus
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.NumberFormatUtils
import com.julun.huanque.common.widgets.ColorFlipPagerTitleView
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.ProgramFragmentAdapter
import com.julun.huanque.message.viewmodel.ContactsActivityViewModel
import kotlinx.android.synthetic.main.act_contacts.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.jetbrains.anko.textSizeDimen

/**
 *@创建者   dong
 *@创建时间 2020/7/2 17:52
 *@描述 联系人页面
 */
@Route(path = ARouterConstant.ContactsActivity)
class ContactsActivity : BaseActivity() {
    companion object {
        fun newInstance(activity: Activity, defaultType: String) {
            val intent = Intent(activity, ContactsActivity::class.java)
            intent.putExtra(ParamConstant.DEFAULT_TYPE, defaultType)
            activity.startActivity(intent)
        }
    }

    private lateinit var mCommonNavigator: CommonNavigator
    private var mActivityViewModel: ContactsActivityViewModel? = null
    private var mPagerAdapter: ProgramFragmentAdapter? = null

    //默认选中的tab标识
    private var mDefaultType = ""

    //关注tab索引
    private var mFollowPosition = -1

    override fun getLayoutId() = R.layout.act_contacts

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mDefaultType = intent?.getStringExtra(ParamConstant.DEFAULT_TYPE) ?: ""
        findViewById<TextView>(R.id.tvTitle).text = "联系人"
        initViewModel()
        mActivityViewModel?.getContacts()
        mPagerAdapter = ProgramFragmentAdapter(supportFragmentManager, this)
        pager.adapter = mPagerAdapter
        initMagicIndicator()
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                //选中对应的tab
                if (mActivityViewModel?.followNeedRefresh == true && position == mFollowPosition) {
                    //关注列表需要刷新，同时选中了关注tab,刷新关注列表
                    mActivityViewModel?.followRefreshFlag?.value = true
                }
            }

        })
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mActivityViewModel = ViewModelProvider(this).get(ContactsActivityViewModel::class.java)
        mActivityViewModel?.tabListData?.observe(this, Observer {
            if (it != null) {
                refreshTabList(it)
                it.forEachIndexed { index, bean ->
                    if (bean.userDataTabType == ContactsTabType.Follow) {
                        mFollowPosition = index
                        return@Observer
                    }
                }
            }
        })

        mActivityViewModel?.followChangeFlag?.observe(this, Observer {
            if (it != null) {
                mActivityViewModel?.followNeedRefresh = true
                val type = it.type
                val formerFollow = it.formerFollow
                val currentFollow = it.follow
//                if (type == ContactsTabType.Follow) {
                val tabList = mActivityViewModel?.tabListData?.value ?: return@Observer
                //关注列表,粉丝列表  操作一致
                when (currentFollow) {
                    FollowStatus.False -> {
                        //未关注状态
                        if (formerFollow == FollowStatus.Mutual) {
                            //之前处于相互关注状态，关注数 减1  好友数 减1
                            tabList.forEach {
                                if (it.userDataTabType == ContactsTabType.Follow) {
                                    it.count -= 1
                                } else if (it.userDataTabType == ContactsTabType.Friend) {
                                    it.count -= 1
                                }
                            }
                        } else {
                            //之前未处于相互关注状态，关注数 减1
                            tabList.forEach {
                                if (it.userDataTabType == ContactsTabType.Follow) {
                                    it.count -= 1
                                }
                            }
                        }
                    }
                    FollowStatus.True -> {
                        //关注成功，未处于相互关注状态  关注数 加1
                        tabList.forEach {
                            if (it.userDataTabType == ContactsTabType.Follow) {
                                it.count += 1
                            }
                        }
                    }
                    FollowStatus.Mutual -> {
                        //互相关注状态   好友数 加1  关注数 加1
                        tabList.forEach {
                            if (it.userDataTabType == ContactsTabType.Follow) {
                                it.count += 1
                            } else if (it.userDataTabType == ContactsTabType.Friend) {
                                it.count += 1
                            }
                        }
                    }
                    else -> {
                    }
                }

                mPagerAdapter?.setTypeList(tabList)
                //必须先执行刷新
                mCommonNavigator.notifyDataSetChanged()
//                } else if (type == ContactsTabType.Fan) {
//                    //粉丝列表
//                    when (currentFollow) {
//                        FollowStatus.False -> {
//                            //未关注状态
//                            if (formerFollow == FollowStatus.Mutual) {
//                                //之前处于相互关注状态，关注数 减1  好友数 减1
//                            } else {
//                                //之前未处于相互关注状态，关注数 减1
//                            }
//                        }
//                        FollowStatus.True -> {
//                            //关注成功，未处于相互关注状态  关注数 加1
//                        }
//                        FollowStatus.Mutual -> {
//                            //互相关注状态   好友数 加1  关注数 加1
//                        }
//                        else -> {
//                        }
//                    }
//                }

            }
        })

//        mActivityViewModel?.followStatusData?.observe(this, Observer {
//            if (it != null) {
//
//            }
//        })
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew {
            finish()
        }
    }


    /**
     * 初始化指示器
     */
    private fun initMagicIndicator() {
        magic_indicator.setBackgroundColor(Color.parseColor("#fafafa"))
        mCommonNavigator = CommonNavigator(this)
        mCommonNavigator.isEnablePivotScroll
        mCommonNavigator.scrollPivotX = 0.65f
        mCommonNavigator.isAdjustMode = true
        mCommonNavigator.isSkimOver = true
        mPagerAdapter?.let {
            mCommonNavigator.adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int {
                    return it.count
                }

                override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                    val simplePagerTitleView = ColorFlipPagerTitleView(context)
                    simplePagerTitleView.textSizeDimen = R.dimen.text_size_big
                    val userTab = it.getTypeList()[index]
                    val count = userTab.count
                    if (count >= 10000) {
                        val iCount = count / 1000
                        val dCount = iCount / 10.toDouble()
                        simplePagerTitleView.text = "${userTab.userTabName}${NumberFormatUtils.formatWithdecimal1(dCount)}W"
                    } else {
                        simplePagerTitleView.text = "${userTab.userTabName}$count"
                    }

                    simplePagerTitleView.normalColor = GlobalUtils.getColor(R.color.black_999)
                    simplePagerTitleView.selectedColor = GlobalUtils.getColor(R.color.black_333)
                    simplePagerTitleView.setOnClickListener { pager.currentItem = index }
                    if (mCommonNavigator.isAdjustMode) {
                        //固定模式，不设置padding
                        simplePagerTitleView.setPadding(0, 0, 0, 0)
                    } else {
                        //自适应模式设置padding
                        val padding = UIUtil.dip2px(this@ContactsActivity, 10.0)
                        simplePagerTitleView.setPadding(padding, 0, padding, 0)
                    }
                    return simplePagerTitleView
                }

                override fun getIndicator(context: Context): IPagerIndicator {
                    val indicator = LinePagerIndicator(context)
                    indicator.mode = LinePagerIndicator.MODE_EXACTLY
                    indicator.lineHeight = UIUtil.dip2px(context, 3.0).toFloat()
                    indicator.lineWidth = UIUtil.dip2px(context, 29.0).toFloat()
                    indicator.roundRadius = UIUtil.dip2px(context, 3.0).toFloat()
                    indicator.startInterpolator = AccelerateInterpolator()
                    indicator.endInterpolator = DecelerateInterpolator(2.0f)
                    indicator.yOffset = DensityHelper.dp2px(4f).toFloat()
                    indicator.setColors(GlobalUtils.getColor(R.color.primary_color))
                    return indicator
                }
            }
        }
        magic_indicator.navigator = mCommonNavigator
        ViewPagerHelper.bind(magic_indicator, pager)
    }

    /**
     * 刷新tab
     */
    private fun refreshTabList(tabList: MutableList<UserDataTab>) {
        if (tabList.size > 5) {
            mCommonNavigator.isAdjustMode = false
        }
        pager.offscreenPageLimit = tabList.size
        mPagerAdapter?.setTypeList(tabList)
        //必须先执行刷新
        mCommonNavigator.notifyDataSetChanged()
        mPagerAdapter?.notifyDataSetChanged()

        if (mDefaultType.isNotEmpty()) {
            tabList.forEachIndexed { index, newProgramTab ->
                if (mDefaultType == newProgramTab.userDataTabType) {
                    switchToTab(index)
                    return@forEachIndexed
                }
            }
        }
    }

    /**
     *  手动切换到指定tab位置
     */
    private fun switchToTab(position: Int) {
        val count = mPagerAdapter?.count ?: return
        if (count >= position)
            pager.currentItem = position
    }
}