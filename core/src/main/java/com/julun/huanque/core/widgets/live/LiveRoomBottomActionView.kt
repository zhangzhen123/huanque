package com.julun.huanque.core.widgets.live

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.julun.huanque.common.bean.beans.BottomActionBean
import com.julun.huanque.common.bean.beans.PrivateMessageBean
import com.julun.huanque.common.constant.ClickType
import com.julun.huanque.common.constant.GameType
import com.julun.huanque.common.constant.ScreenType
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel

import kotlinx.android.synthetic.main.view_live_bottom_action.view.*
import org.jetbrains.anko.imageResource


/**
 * 底部按钮栏
 * Created by djp on 2016/11/29.
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/09/04
 * @iterativeVersion 4.19
 * @iterativeDetail 迭代详情：增加横屏屏蔽入口，粉丝团引导气泡实现方案修改
 */
class LiveRoomBottomActionView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs), View.OnClickListener {
    private var isAnchor = false

    //是否是横屏状态
    private var mHorizontal = false

    //与PlayerActivity通信使用
    private var playerViewModel: PlayerViewModel? = null
//    private var mConfigViewModel: PlayerConfigViewModel? = null

    //屏蔽状态info
//    private var mShieldSetting: ShieldSettingBean? = null

    //私聊体验权限
    private var mExperience = false

    //游戏类型
    private var mGameType: String? = null


    private fun initEvents() {
        sayImage.setOnClickListener(this)
        privateMessageLayout.setOnClickListener(this)
        giftImage.setOnClickListener(this)
        gameImage.setOnClickListener(this)
        iv_beautify.setOnClickListener(this)
        rl_more.setOnClickListener(this)
        iv_to_v.setOnClickListener(this)
        iv_shield.setOnClickListener(this)

        rl_welfare.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val bean = BottomActionBean()
        when (v?.id) {
            R.id.sayImage -> {
                if (!ClickListenerUtils.isDoubleClick) {
                    bean.type = ClickType.CHAT_INPUT_BOX
                    bean.actionValue = ""
                }
            }
            R.id.privateMessageLayout -> {
                if (!ClickListenerUtils.isDoubleClick) {
                    bean.type = ClickType.PRIVATE_MESSAGE
                    bean.actionValue = PrivateMessageBean()
//                    playerViewModel?.privateMessageView?.value = PrivateMessageBean()
                }
            }
            R.id.giftImage -> {
                if (!ClickListenerUtils.isDoubleClick) {
                    bean.type = ClickType.GIFT
                }
            }
//            R.id.exitImage -> {
//                if (!ClickListenerUtils.isDoubleClick) {
//                    bean.type = ClickType.CLOSE
//                }
//            }
            R.id.gameImage -> {
                if (!ClickListenerUtils.isDoubleClick) {
                    when (mGameType) {
                        GameType.FactoryCar -> {
                            //豪车工厂
                            bean.type = ClickType.LUXURY_CAR
                        }
                        GameType.Planet -> {
                            //星球争霸
                            bean.type = ClickType.PLANTE
                        }
                        else -> {
                            //打开游戏面板
                            bean.type = ClickType.GAME
                        }
                    }
                }
            }
            R.id.iv_beautify -> {
                if (!ClickListenerUtils.isDoubleClick) {
                    bean.type = ClickType.PUBLISH_SETTING
                }
            }
            R.id.rl_more -> {
                if (!ClickListenerUtils.isDoubleClick) {
                    if (isAnchor) {
//                        bean.type = ClickType.ANCHOR_MORE_SETTING
                        bean.type = ClickType.ANCHOR_MORE_SETTING
                        bean.actionValue = true
                    } else {
                        bean.type = ClickType.USER_MORE_SETTING
                    }
                }
            }
            R.id.iv_to_v -> {
                if (!ClickListenerUtils.isDoubleClick) {
                    bean.type = ClickType.SWITCH_SCREEN
                    bean.actionValue = ScreenType.SP
                }
            }
            R.id.iv_shield -> { //屏蔽功能
                if (!ClickListenerUtils.isDoubleClick) {
                    if (isAnchor) {
//                        showShieldPopup(iv_shield)
                    } else {
                        if (!iv_shield.isSelected) {
                            //打开屏蔽
                            ToastUtils.show("弹幕和礼物特效已屏蔽")
                        } else {
                            //关闭屏蔽
                            ToastUtils.show("弹幕和礼物特效已开启")
                        }
//                        val settingBean = ShieldSettingBean()
//                        settingBean.isAchor = isAnchor
//                        settingBean.isShieldAll = !iv_shield.isSelected
//                        playerViewModel?.shieldSetting?.value = settingBean
//                        ShieldSettingUtil.saveShieldSetting(settingBean)
                    }
                }
            }
            R.id.rl_welfare -> {
//                playerViewModel?.taskView?.value = ""

            }
            else -> {
            }
        }
        playerViewModel?.actionBeanData?.value = bean
    }

    /**
     * 显示未读数量
     * @param actionByExperience 是否由私信体验触发的调用
     */
    fun togglePrivateRedPointView(count: Int, actionByExperience: Boolean = false) {
        if (actionByExperience) {
            if (mExperience && privateChatBadgeText.visibility == View.VISIBLE) {
                return
            } else if (!mExperience) {
                //私信体验到期,如果是没有数字的红点，需要隐藏
                val content = privateChatBadgeText.text.toString().trim()
                if (content.isNotEmpty()) {
                    //红点里面有文案，直接返回
                    return
                }
            }
        }
        when {
            count == 0 -> privateChatBadgeText.text = "   "
            count < 100 -> privateChatBadgeText.text = "$count"
            else -> privateChatBadgeText.text = "99+"
        }
        if (count > 0 || mExperience) {
            privateChatBadgeText.visibility = View.VISIBLE
        } else {
            privateChatBadgeText.visibility = View.GONE
        }

    }

    fun setIsAnchor(isAnchor: Boolean) {
        this.isAnchor = isAnchor
        initViews()
    }


    //非活动页面，将bottomview中的子view设置weight = 1
    //活动页面，将bottomview中的子view设置weight = 0
    private fun setViewWeight(view: View, weight: Float) {
        val layoutParams = view.layoutParams as? LinearLayout.LayoutParams
        layoutParams?.weight = weight
        view.layoutParams = layoutParams
    }

    fun initViews() {
        if (isAnchor) {
            //主播身份
            if (mHorizontal) {
                //横屏状态
                sayImage.hide()
                iv_shield.show()
                privateChatImage.hide()
                iv_beautify.hide()
                privateMessageLayout.hide()
            } else {
                sayImage.show()
                iv_shield.hide()
                giftImage.hide()
                privateChatImage.show()
                iv_beautify.show()
                privateMessageLayout.show()
            }
            rl_welfare.hide()
        } else {
            sayImage.show()
            privateChatImage.show()
            //用户身份
            iv_beautify.hide()
            if (mHorizontal) {
                //横屏状态
                iv_shield.show()
                privateMessageLayout.hide()
                rl_more.hide()
//                rl_welfare.hide()
                iv_to_v.show()
            } else {
                iv_shield.hide()
                privateMessageLayout.show()
//                rl_welfare.show()
                rl_more.show()
                iv_to_v.hide()
            }
        }
    }

    private fun initViewModel() {
        val activity = context as? PlayerActivity ?: return
        playerViewModel = ViewModelProvider(activity).get(PlayerViewModel::class.java)
//        mConfigViewModel = ViewModelProviders.of(activity).get(PlayerConfigViewModel::class.java)
//            playerViewModel?.getFansIconParams?.observe(it, Observer {
//                if (it != null && it) {
//                    getFansIconParams()
//                }
//            })
//            playerViewModel?.loginState?.observe(it, Observer {
//                it ?: return@Observer
//                if (it == true) {
//                    //登录成功
//                    playerViewModel?.getShieldSetting()
//                }
//            })
//        playerViewModel?.shieldSetting?.observe(activity, Observer {
//            it ?: return@Observer
//            mShieldSetting = it
//            if (!isAnchor) {
//                iv_shield.isSelected = mShieldSetting?.isShieldAll ?: false
//                val bean = BottomActionBean()
//                bean.type = ClickType.SHIELD
//                bean.actionValue = iv_shield.isSelected
//                playerViewModel?.actionBeanData?.value = bean
//            } else {
//                iv_shield.isSelected = it.isShieldAll || it.isShieldGift || it.isShieldEnter || it.isShieldStreamer
//            }
//        })
//        playerViewModel?.isShowFansBubbleLayout?.observe(activity, Observer {
//            it ?: return@Observer
//            if (mConfigViewModel?.horizonState?.value == true) {
//                if (mFansBubble?.isShowing == true) {
//                    mFansBubble?.dismiss()
//                }
//                return@Observer
//            }
//            if (it) {
//                showFansPopup(fans_acton)
//            } else {
//                if (mFansBubble?.isShowing == true) {
//                    mFansBubble?.dismiss()
//                }
//            }
//        })
//        mConfigViewModel?.horizonState?.observe(activity, Observer {
//            it ?: return@Observer
//            if (it) {
//                if (mFansBubble?.isShowing == true) {
//                    mFansBubble?.dismiss()
//                }
//            }
//        })
    }

    /**
     * 显示任务完成标识位
     */
    fun showRedPoint() {
        if (isAnchor) {
            //横屏不显示领取按钮
            return
        }
        tv_task_enable.show()
    }

    /**
     * 隐藏任务完成标识位
     */
    fun hideRedPoint() {
        tv_task_enable.hide()
    }

    /**获取礼物图标的中间位置**/
    fun getGiftCenterX(): Int {
        val position = IntArray(2)
        giftImage.getLocationInWindow(position)
        return position[0] + giftImage.width / 2
    }

    /**隐藏GiftView**/
    fun hideGiftView() {
        giftImage.visibility = View.INVISIBLE
    }

    /**显示GiftView**/
    fun showGiftView() {
        giftImage.show()
    }

    /**
     * 显示礼物tips角标
     */
    fun showGiftTips(text: String? = null) {
        if (!text.isNullOrEmpty()) {
            tv_gift_tips.text = text
        }
        tv_gift_tips.show()
    }

    /**
     * 隐藏礼物tips角标
     */
    fun hideGiftTips() {
        tv_gift_tips.hide()
    }

    fun getGiftView(): View {
        return giftImage
    }

    fun getMoreView(): View {
        return rl_more
    }

    init {
        initViewModel()
        LayoutInflater.from(context).inflate(R.layout.view_live_bottom_action, this)
        initEvents()
        initViews()
    }

    /**
     * 设置游戏图标
     */
//    fun setShowGame(gameList: MutableList<SingleGame>) {
//        if (isAnchor) {
//            rl_game.hide()
//        } else {
//            mGameType = getGameShowType(gameList)
//            when (mGameType) {
//                null -> {
//                    rl_game.hide()
//                    return
//                }
//                GameType.Planet -> {
//                    //星球霸主
//                    gameImage.imageResource = R.drawable.selector_planet_image
//                }
//                GameType.GAME -> {
//                    //游戏
//                    gameImage.imageResource = R.drawable.selector_game_image
//                }
//                GameType.FactoryCar -> {
//                    //豪车
//                    gameImage.imageResource = R.drawable.selector_car_image
//                }
//                else -> {
//                    //其他不支持的游戏类型
//                    gameImage.imageResource = R.drawable.selector_game_image
//                }
//            }
//            rl_game.show()
//        }
//    }


    /**
     * 设置是否是横屏状态
     */
    fun setHorizontal(h: Boolean) {
        if (h == mHorizontal) {
            return
        }
        mHorizontal = h
        initViews()
    }

//    private var mShieldBubble: ShieldDialog? = null


    /**
     * 是否有私聊体验权限
     */
    fun privateExperience(experience: Boolean) {
        mExperience = experience
        togglePrivateRedPointView(0, true)
    }


    /**
     * 游戏图标显示红点
     */
    fun showGameRedPoint(show: Boolean) {
        if (show) {
            tv_light_count.show()
        } else {
            tv_light_count.hide()
        }
    }


    /**
     * 展示屏蔽弹窗 -> 主播端
     */
//    private fun showShieldPopup(clickView: View) {
//        if (mShieldBubble == null) {
//            val bl = LayoutInflater.from(context).inflate(R.layout.view_bubblelayout_shield, null)
//            mShieldBubble = ShieldDialog(context)
//            if (bl is BubbleLayout) {
//                mShieldBubble?.setBubbleLayout(bl)
//            }
//            mShieldBubble?.setClickListener(object : ShieldOnClickListener {
//                override fun onClick(bean: ShieldSettingBean) {
//                    bean.isAchor = isAnchor
//                    playerViewModel?.parseStringAndSave(bean)
//                }
//            })
//            mShieldSetting?.let {
//                mShieldBubble?.setCheckBean(it)
//            }
//        }
//        mShieldBubble?.setClickedView(clickView)
//        mShieldBubble?.show()
//    }


//    private var mFansBubble: BubbleDialog? = null

    /**
     * 展示粉丝团引导气泡
     */
//    private fun showFansPopup(clickView: View) {
//        if (mFansBubble == null) {
//            val bl = LayoutInflater.from(context).inflate(R.layout.view_bubblelayout_fans, null)
//            val contentView = LayoutInflater.from(context).inflate(R.layout.view_fans_reminder, null)
//            mFansBubble = BubbleDialog(context).addContentView(contentView)
//                .setPosition(BubbleDialog.Position.TOP)
//                .setTransParentBackground()
//                .setThroughEvent(true, false)
//                .autoPosition(Auto.UP_AND_DOWN)
//            if (bl is BubbleLayout) {
//                mFansBubble?.setBubbleLayout(bl)
//            }
//        }
//        mFansBubble?.setClickedView(clickView)
//        mFansBubble?.show()
//    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        if (mFansBubble?.isShowing == true) {
//            mFansBubble?.dismiss()
//        }
    }
}