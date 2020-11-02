package com.julun.huanque.core.ui.live.fragment

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.BlindBoxBean
import com.julun.huanque.common.bean.beans.LiveGiftDto
import com.julun.huanque.common.constant.RuleTouchType
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.widgets.TagView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.EggSettingViewModel
import kotlinx.android.synthetic.main.layout_gift_explain.*


/**
 *@创建者   dong
 *@创建时间 2020/2/13 10:25
 *@描述 礼物说明弹窗
 * @alter WanZhiYuan
 * @since 4.37
 * @date 2020/08/13
 * @detail 新增打开周星榜弹窗
 */
class GiftExplainFragment : BaseDialogFragment() {
    private var mGiftExplainViewModel: EggSettingViewModel? = null
    private var playerViewModel: PlayerViewModel? = null

    private var mCurrentGift: LiveGiftDto? = null

    //规则说明弹窗
    private var mRuleFragment: BlindBoxRuleFragment? = null

    //图片点击的跳转链接
    private var mJumpUrl = ""

//    /**
//     * 显示H5地址的弹窗
//     */
//    private var h5Fragment: H5Fragment? = null

    override fun getLayoutId() = R.layout.layout_gift_explain

    override fun needEnterAnimation() = false

    override fun initViews() {
        initListener()
        if (iv_arrow_left.marginLeft == 0 || iv_arrow_right.marginRight == 0) {
            //设置箭头的间距
            val margin = ScreenUtils.getScreenWidth() / 4 / 2 - dp2px(5f) - 18
            val leftParams = iv_arrow_left.layoutParams as? ConstraintLayout.LayoutParams
            leftParams?.leftMargin = margin

            val rightParams = iv_arrow_right.layoutParams as? ConstraintLayout.LayoutParams
            rightParams?.rightMargin = margin
        }
    }


    /**
     * 初始化相关监听
     */
    private fun initListener() {
        sdv_jump.onClickNew {
            val tips = mCurrentGift?.tips ?: return@onClickNew
            when (tips.ruleTouchType) {
                RuleTouchType.Url -> {
                    //打开H5页面
                }
                RuleTouchType.BlindRulePage -> {
                    //打开盲盒
                    val gift = mCurrentGift ?: return@onClickNew
                    playerViewModel?.blindBoxBeanData?.value =
                        BlindBoxBean(gift.giftName ?: "", gift.beans, gift.giftId.toLong(), gift.bag, gift.prodType)
//                    mRuleFragment = mRuleFragment ?: BlindBoxRuleFragment()
//                    mRuleFragment?.show(childFragmentManager, "BlindBoxRuleFragment")
//                    playerViewModel?.sendGiftFragmentDismissState?.value = true
                }
                RuleTouchType.MagicRulePage -> {
                    //打开魔法礼物
//                    mRuleFragment = mRuleFragment ?: BlindBoxRuleFragment()
//                    mRuleFragment?.show(childFragmentManager, "BlindBoxRuleFragment")
                    val gift = mCurrentGift ?: return@onClickNew
                    playerViewModel?.blindBoxBeanData?.value =
                        BlindBoxBean(gift.giftName ?: "", gift.beans, gift.giftId.toLong(), gift.bag, gift.prodType)
//                    playerViewModel?.sendGiftFragmentDismissState?.value = true
                }
                else -> {
                }
            }

//            when {
//                mJumpUrl == BusiConstant.GiftBannerType.OPEN_WISH_VIEW -> {
//                    playerViewModel?.openWishKoiDialog?.value = true
//                    dismiss()
//                }
//                mJumpUrl == BusiConstant.GiftBannerType.PlanetCasual -> {
//                    //打开休闲模式
////                    playerViewModel?.openDialog?.value = DialogTypes.DIALOG_PlANET_GAME
//                    playerViewModel?.showDialog?.value = PlanetFragment::class.java
//                    mPlanetViewModel?.mGoUseFlag = PlanetViewModel.VIEW_RELAXATION_READY
//                    dismiss()
//                    playerViewModel?.sendGiftFragmentDismissState?.value = false
//                }
//                mJumpUrl.contains(BusiConstant.GiftBannerType.PlanetAttack) -> {
//                    //打开攻击模式
//                    val defaultWeapon = mJumpUrl.split("=")
//                    if (defaultWeapon.size >= 2) {
//                        SharedPreferencesUtils.commitString(ParamConstant.DEFAULT_WEAPON_STYLE, defaultWeapon[1])
//                    }
//
////                    playerViewModel?.openDialog?.value = DialogTypes.DIALOG_PlANET_GAME
//                    playerViewModel?.showDialog?.value = PlanetFragment::class.java
//                    mPlanetViewModel?.mGoUseFlag = PlanetViewModel.VIEW_ATTACK
//                    dismiss()
//                    playerViewModel?.sendGiftFragmentDismissState?.value = false
//                }
//                mJumpUrl == BusiConstant.GiftBannerType.SEND_DANMU -> {
//                    //打开公聊
//                    val bean = BottomActionBean().apply {
//                        type = ClickType.CHAT_INPUT_BOX
//                    }
//                    playerViewModel?.sendGiftFragmentDismissState?.value = false
//                    playerViewModel?.actionBeanData?.value = bean
//                    dismiss()
//                }
//                mJumpUrl.contains(BusiConstant.DIALOG_TAG) -> {
//                    //跳转弹窗
//                    showRuleFragment(mJumpUrl)
//                    dismiss()
//                }
//                mJumpUrl.startsWith("/") -> {
//                    val intent = Intent(activity, PushWebActivity::class.java)
//                    val extra = Bundle()
//                    extra.putString(BusiConstant.PUSH_URL, LMUtils.getDomainName(mJumpUrl))
//                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, true)
//                    intent.putExtras(extra)
//                    if (ForceUtils.activityMatch(intent)) {
//                        startActivity(intent)
//                    }
//                }
//                mJumpUrl == BusiConstant.GiftBannerType.WEEK_STAR_PANEL -> {
//                    //打开周星弹窗
//                    playerViewModel?.showWeekStarDialog?.value = OnClickWeekStarInfo(giftId = mGiftId.toLong())
//                    playerViewModel?.sendGiftFragmentDismissState?.value = true
//                    dismiss()
//                }
//                mJumpUrl == BusiConstant.GiftBannerType.BLIND_BOX_PANEL -> {
//                    //打开盲盒说明弹窗
//                    BlindBoxExplainDialogFragment.newInstance(mGiftId).show(childFragmentManager,"BlindBoxExplainDialogFragment")
//                }
//                else -> {
//                    //可扩展
//                    logger.info("其他类型 type = $mJumpUrl")
//                }
//            }
        }

    }

    private fun initViewModel() {
        activity?.let { act ->
            mGiftExplainViewModel = ViewModelProviders.of(act).get(EggSettingViewModel::class.java)
            playerViewModel = ViewModelProviders.of(act).get(PlayerViewModel::class.java)
        }
        mGiftExplainViewModel?.mSelectGiftData?.observe(this, Observer {
            mCurrentGift = it
            showViewByData(it ?: return@Observer)
        })
        mGiftExplainViewModel?.discountStatus?.observe(this, Observer {
        })

        mGiftExplainViewModel?.anonymousState?.observe(this, Observer {
        })

        mGiftExplainViewModel?.eggLuckyState?.observe(this, Observer {
        })
    }

    private var mGiftId: Int = 0

    /**
     * 根据数据显示视图
     */
    private fun showViewByData(data: LiveGiftDto) {
        mGiftId = data.giftId ?: 0
        tv_gift_name.text = data.giftName ?: ""
        //显示标签数据
        ll_tag.removeAllViews()
        val tagData = data.tagContent?.split(",")
        tagData?.forEach {
            val tagView = TagView(requireContext())
            ll_tag.addView(tagView)
            tagView.show()
            tagView.isGiftTag = true
            tagView.setData(it)
        }
        tv_explain.text = data.tips?.text ?: ""
        if (data.tips?.ruleTouchType?.isNotEmpty() == true) {
            sdv_jump.show()
            view_jump.show()
            view_content.show()
        } else {
            sdv_jump.hide()
            view_jump.hide()
            view_content.inVisible()
        }
//        dealWithTips(data.tips ?: "", data.ruleUrl)
//        if ((data.discount ?: 0) > 0 || (data.anonymous && data.anonymous) || data.luckyOrHigh) {
//            view_content.show()
//        } else {
//            view_content.hide()
//        }
    }

    /**
     * 处理tips
     * @param rule 跳转链接。
     */
    private fun dealWithTips(tips: String, rule: String) {
//        if (tips.isNotEmpty()) {
//            val tipsArray = tips.split("#")
//            if (tipsArray.size > 1) {
////                view_jump.show()
//                sdv_jump.show()
//            } else {
////                view_jump.hide()
//                sdv_jump.hide()
//            }
//            tipsArray.forEachIndexed { index, content ->
//                when (index) {
//                    0 -> {
//                        if (rule.isNotEmpty()) {
//                            //配置点击事件
//                            val style = SpannableStringBuilder(content)
//                            val ruleText = "规则说明"
//                            style.append(ruleText)
//                            //设置部分文字点击事件
//                            val clickableSpan = object : ClickableSpan() {
//                                override fun onClick(widget: View) {
//                                    showRuleFragment(rule, Gravity.BOTTOM)
//                                }
//                            }
//                            style.setSpan(clickableSpan, style.length - ruleText.length, style.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//
//                            //设置部分文字颜色
//                            val foregroundColorSpan = ForegroundColorSpan(GlobalUtils.getColor(R.color.primary_color));
//                            style.setSpan(foregroundColorSpan, style.length - ruleText.length, style.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                            //配置给TextView
//                            tv_explain.movementMethod = LinkMovementMethod.getInstance()
//                            tv_explain.text = style
//                        } else {
//                            tv_explain.text = content
//                        }
//                    }
//                    1 -> {
//                        mJumpUrl = content
//                        if (index == tipsArray.size - 1) {
//                            ImageUtils.loadImageLocal(sdv_jump, R.mipmap.icon_gift_rule)
//                            val params = sdv_jump.layoutParams as? ConstraintLayout.LayoutParams
//                            params?.dimensionRatio = "27:12"
//                            sdv_jump.layoutParams = params
//                        }
//                    }
//                    2 -> {
//                        if (index == tipsArray.size - 1) {
//                            ImageUtils.loadImageLocal(sdv_jump, R.mipmap.icon_gift_rule)
//                            val params = sdv_jump.layoutParams as? ConstraintLayout.LayoutParams
//                            params?.dimensionRatio = "27:12"
//                            sdv_jump.layoutParams = params
//                        }
//                    }
//                    3 -> {
//                        val params = sdv_jump.layoutParams as? ConstraintLayout.LayoutParams
//                        params?.dimensionRatio = null
//                        sdv_jump.layoutParams = params
//                        ImageUtils.loadImageWithHeight_2(sdv_jump, content, dp2px(40f))
//                    }
//                    else -> {
//                    }
//                }
//            }
//        } else {
//            //隐藏按钮相关
////            view_jump.hide()
//            sdv_jump.hide()
//        }
    }


    /**
     * 显规则弹窗
     */
    private fun showRuleFragment(rule: String, gravity: Int = Gravity.CENTER) {
//        activity?.let { act ->
//            h5Fragment = h5Fragment ?: H5Fragment.newInstance()
//            h5Fragment?.mUrl = LMUtils.getDomainName(rule)
//            h5Fragment?.mGravity = gravity
//            if (gravity == Gravity.BOTTOM) {
//                //设置横向全屏
//                h5Fragment?.setParams(DensityUtils.px2dp(ScreenUtils.getScreenWidth().toFloat()).toFloat(), 0f, 0)
//            }
//            h5Fragment?.show(act.supportFragmentManager, "H5Fragment")
//        }
        dismiss()
    }


    override fun onStart() {
        super.onStart()
        initViewModel()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        //设置弹窗的位置
        val positionArray = mGiftExplainViewModel?.locationData?.value
        if (positionArray != null && positionArray.size >= 4) {
            //位置数据可用
            val dX: Int
            val gravity: Int
            if (positionArray[0] < ScreenUtils.getScreenWidth() / 2) {
                //弹窗显示在屏幕左侧
                dX = positionArray[0]
                gravity = Gravity.BOTTOM or Gravity.LEFT
                iv_arrow_left.show()
                iv_arrow_right.hide()
            } else {
                //弹窗显示在屏幕右侧
                gravity = Gravity.BOTTOM or Gravity.RIGHT
                dX = ScreenUtils.getScreenWidth() - (positionArray[0] + positionArray[2])
                iv_arrow_left.hide()
                iv_arrow_right.show()
            }

            val dY = ScreenUtils.getScreenHeightHasVirtualKey() - ScreenUtils.getNavigationBarHeightIfRoom(requireContext()) - positionArray[1]
            val window = dialog?.window ?: return
            val params = window.attributes
            params.gravity = gravity
            params.width = dp2px(210f)
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = dY
            params.x = dX + dp2px(5f)
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            window.attributes = params
        } else {
            dismiss()
        }
    }
}