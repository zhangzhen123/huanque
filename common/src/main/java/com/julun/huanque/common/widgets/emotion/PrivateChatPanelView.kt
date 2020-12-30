package com.julun.huanque.common.widgets.emotion

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.effective.android.panel.view.panel.IPanelView
import com.julun.huanque.common.R
import com.julun.huanque.common.adapter.PanelAdapter
import com.julun.huanque.common.constant.EmojiType
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.widgets.SinglePanelView
import kotlinx.android.synthetic.main.panel_view_private_chat.view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.view

/**
 *@创建者   dong
 *@创建时间 2020/7/16 15:29
 *@描述 表情面板
 */
class PrivateChatPanelView(context: Context, attrs: AttributeSet?) : IPanelView, ConstraintLayout(context, attrs) {

    private var triggerViewId = 0
    private var isToggle = true
    private var mAdapter: PanelAdapter? = null

    //点击表情的回调
    private var mListener: EmojiInputListener? = null

    private var mCommonNavigator: CommonNavigator? = null

    private var mNeedLevel = 0

    private var mCurrentLevel = 0

    //是否包含官方
    private var hasManager: Boolean = false

    fun setListener(l: EmojiInputListener) {
        mListener = l
        mAdapter?.outEmojiInputListener = l
    }

    init {
        if (context != null && attrs != null) {
            LayoutInflater.from(context).inflate(R.layout.panel_view_private_chat, this, true)

            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CusPanelView, 0, 0)
            triggerViewId = typedArray.getResourceId(R.styleable.CusPanelView_cus_panel_trigger, -1)
            isToggle = typedArray.getBoolean(R.styleable.CusPanelView_cus_panel_toggle, isToggle)
            typedArray.recycle()

            initListener()
            initViewPager()
            initMagicIndicator()
        }
    }

    /**
     * 初始化ViewPager
     */
    private fun initViewPager() {
        val typeList = mutableListOf<String>()
        typeList.add(EmojiType.NORMAL)
        typeList.add(EmojiType.PREROGATIVE)
        typeList.add(EmojiType.ANIMATION)
        mAdapter = PanelAdapter(typeList, context)
        viewPager.adapter = mAdapter
        viewPager.offscreenPageLimit = 3

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }


            override fun onPageSelected(position: Int) {
                val childCount = viewPager.childCount
                if (position < childCount) {
                    val tempView = viewPager.getChildAt(position)
                    if (tempView is SinglePanelView && tempView.type == EmojiType.PREROGATIVE) {
                        //亲密度表情
                        tempView.setIntimate(mNeedLevel, mCurrentLevel,hasManager)
                    }
                }
            }

        })
    }

    /**
     * 设置专属表情所需要等级
     * @param needLevel 解锁专属表情所需等级
     * @param currentLevel 当前两人的亲密度等级
     */
    fun setIntimate(needLevel: Int, currentLevel: Int, hasManager: Boolean) {
        mNeedLevel = needLevel
        mCurrentLevel = currentLevel
        this.hasManager = hasManager
        val childCount = viewPager.childCount
        (0 until childCount).forEach {
            val tempView = viewPager.getChildAt(it)
            if (tempView is SinglePanelView && tempView.type == EmojiType.PREROGATIVE) {
                tempView.setIntimate(needLevel, currentLevel,hasManager)
                return@forEach
            }
        }
    }


    override fun getBindingTriggerViewId(): Int = triggerViewId

    override fun isTriggerViewCanToggle(): Boolean = isToggle

    override fun isShowing(): Boolean = isShown

    override fun assertView() {
        if (triggerViewId == -1) {
            throw RuntimeException("PanelView -- you must set 'panel_layout' and panel_trigger by Integer id")
        }
    }

    /**
     * 设置监听
     */
    private fun initListener() {
        viewPager.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                if (ev?.action == MotionEvent.ACTION_UP) {
                    mListener?.onActionUp()
                }
            }

        }

        iv_delete.onClickNew {
            mListener?.onClickDelete()
        }
    }


    /**
     * 初始化指示器
     */
    private fun initMagicIndicator() {
        mCommonNavigator = CommonNavigator(context)
        mCommonNavigator?.isAdjustMode = false
        mCommonNavigator?.isSkimOver = true
        mAdapter?.let {
            mCommonNavigator?.adapter = object : CommonNavigatorAdapter() {
                override fun getCount(): Int {
                    return it.count
                }

                override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                    val resource = when (index) {
                        0 -> {
                            R.mipmap.icon_emoji_normal
                        }
                        1 -> {
                            R.mipmap.icon_emoji_intimate
                        }
                        else -> {
                            R.mipmap.icon_emoji_animation
                        }
                    }
                    val title = CommonPagerTitleView(context)
                    val iv = ImageView(context).apply {
                        imageResource = resource
                        scaleType = ImageView.ScaleType.CENTER
                    }
                    title.backgroundResource = R.drawable.bg_panel_icon
                    val params = FrameLayout.LayoutParams(dip(48), dip(48))
                    title.setContentView(iv, params)
                    title.setOnClickListener {
                        viewPager.currentItem = index
                    }
                    return title
                }

                override fun getIndicator(context: Context): IPagerIndicator {
                    val indicator = LinePagerIndicator(context)
                    indicator.mode = LinePagerIndicator.MODE_EXACTLY
                    indicator.lineHeight = dp2px(38).toFloat()
                    indicator.lineWidth = dp2px(48).toFloat()
                    indicator.roundRadius = dp2pxf(19)
                    indicator.startInterpolator = AccelerateInterpolator()
                    indicator.endInterpolator = DecelerateInterpolator(2.0f)
                    indicator.yOffset = dp2pxf(5)
                    indicator.setColors(GlobalUtils.getColor(R.color.enabled))
                    return indicator
                }
            }
        }
        magic_indicator.navigator = mCommonNavigator
        ViewPagerHelper.bind(magic_indicator, viewPager)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        assertView()
    }
}