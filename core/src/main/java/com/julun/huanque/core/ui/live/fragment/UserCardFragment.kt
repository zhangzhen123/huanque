package com.julun.huanque.core.ui.live.fragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.BottomActionBean
import com.julun.huanque.common.bean.beans.PrivateMessageBean
import com.julun.huanque.common.bean.beans.TIBean
import com.julun.huanque.common.bean.beans.UserInfoInRoom
import com.julun.huanque.common.bean.events.OpenPrivateChatRoomEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.dialog.CardManagerDialogFragment
import com.julun.huanque.core.viewmodel.UserCardViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.rd.utils.DensityUtils
import kotlinx.android.synthetic.main.fragment_anchorisnotonline.*
import kotlinx.android.synthetic.main.fragment_user_card.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2020/7/29 14:51
 *@描述 用户卡片
 */
class UserCardFragment : BaseDialogFragment() {

    private val mUserCardViewModel: UserCardViewModel by viewModels<UserCardViewModel>()
    private val mPlayerViewModel: PlayerViewModel by activityViewModels()

    companion object {
        /**
         * @param userId 用户ID
         * @param nickname 用户昵称
         */
        fun newInstance(userId: Long, nickname: String): UserCardFragment {
            val fragment = UserCardFragment()
            val bundle = Bundle()
            bundle.putLong(ParamConstant.UserId, userId)
            bundle.putString(ParamConstant.NICKNAME, nickname)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_user_card

    override fun initViews() {
        mUserCardViewModel.mUserId = arguments?.getLong(ParamConstant.UserId) ?: 0
        mUserCardViewModel.mNickname = arguments?.getString(ParamConstant.NICKNAME) ?: ""
        mUserCardViewModel.programId = mPlayerViewModel.programId
//        state_pager_view.showLoading()
//        state_pager_view.show()
        showDefaultView()

        initViewModel()

        mUserCardViewModel.queryUserInfo()
        initListener()
        dynamicHeight()
    }


    /**
     * 动态计算一些的的宽高
     */
    private fun dynamicHeight() {
        val screentWidth = ScreenUtils.getScreenWidth()
        val headerBorder = screentWidth * 80 / 375 + 1
        val headerTopMargin = screentWidth * 29 / 375 + 1
        //修改头像宽高,以及上间距
        val params = sdv_header.layoutParams as? ConstraintLayout.LayoutParams
        params?.width = headerBorder
        params?.height = headerBorder
        params?.topMargin = headerTopMargin
        sdv_header.layoutParams = params


        //修改背景上间距
        val bgTopMargin = screentWidth * 61 / 375
        val bgParams = view_bg.layoutParams as? ConstraintLayout.LayoutParams
        bgParams?.topMargin = bgTopMargin
        view_bg.layoutParams = bgParams
    }

    /**
     * 显示默认布局
     */
    private fun showDefaultView() {
        tv_nickname.text = mUserCardViewModel.mNickname
        tv_id.text = "欢鹊ID：${mUserCardViewModel.mUserId}"
        tv_sex.hide()
        tv_location.hide()
        iv_royal.setImageDrawable(null)
        if (mUserCardViewModel.mUserId == SessionUtils.getUserId()) {
            tv_attention.isEnabled = false
            tv_private_chat.isEnabled = false
            tv_at.isEnabled = false
        } else {
            tv_attention.isEnabled = true
            tv_private_chat.isEnabled = true
            tv_at.isEnabled = true
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {

        mUserCardViewModel.loadState?.observe(this, Observer {
            it ?: return@Observer
            when (it.state) {
//                NetStateType.LOADING -> {
//                    //加载中
//                    state_pager_view.showLoading("加载中~！")
//                }
//                NetStateType.SUCCESS -> {
//                    //成功
//                    state_pager_view.hide()
//                }
//                NetStateType.IDLE -> {
//                    //闲置，什么都不做
//                }
//                else -> {
//                    //都是异常
//                    state_pager_view.showError(
//                        errorTxt = "网络异常~！",
//                        btnClick = View.OnClickListener {
//                            mUserCardViewModel.queryUserInfo()
//                        })
//                }
            }
        })

        mUserCardViewModel.userInfoData.observe(this, Observer {
            showViewByData(it ?: return@Observer)
        })

        mPlayerViewModel.followStatusData.observe(this, Observer {

            if (it != null && it.isSuccess()) {
                mUserCardViewModel.userInfoData.value?.follow = it.getT().follow != FollowStatus.False
                if (it.getT().follow != FollowStatus.False) {
                    //关注状态
                    tv_attention.text = "已关注"
                } else {
                    //取消关注状态
                    tv_attention.text = "关注"
                }
            }
            if (mUserCardViewModel.mUserId != SessionUtils.getUserId()) {
                tv_attention.isEnabled = true
            }
        })
    }

    /**
     * 初始化监听
     */
    private fun initListener() {
        sdv_header.onClickNew {
            tv_home_page.performClick()
        }
        tv_attention.onClickNew {
            if (mUserCardViewModel.userInfoData.value?.canInteractive != true) {
                Toast.makeText(context, "无法关注该用户", Toast.LENGTH_SHORT).show()
                return@onClickNew
            }
            if (mUserCardViewModel.userInfoData.value?.follow == true) {
                //已关注,取消关注
                mPlayerViewModel.unFollow(mUserCardViewModel.mUserId)
            } else {
                //未关注，关注
                mPlayerViewModel.follow(mUserCardViewModel.mUserId)
            }
            tv_attention.isEnabled = false
        }

        tv_private_chat.onClickNew {
            //私信
            if (mUserCardViewModel.userInfoData.value?.canInteractive != true) {
                Toast.makeText(context, "无法私信该用户", Toast.LENGTH_SHORT).show()
                return@onClickNew
            }
            val userInfo = mUserCardViewModel.userInfoData.value ?: return@onClickNew
            //发送粘性消息
            EventBus.getDefault().post(OpenPrivateChatRoomEvent(mUserCardViewModel.mUserId, userInfo.nickname))
        }
        tv_at.onClickNew {
            //@ 功能
            val content = mUserCardViewModel.mNickname
            val bean = BottomActionBean().apply {
                type = ClickType.CHAT_INPUT_BOX
                actionValue = content
            }
            mPlayerViewModel.actionBeanData.value = bean
//            EventBus.getDefault().post(AttentionUserEvent(content))
            dismiss()
        }
        tv_home_page.onClickNew {
            //主页
            if (mUserCardViewModel.userInfoData.value?.canInteractive != true) {
                Toast.makeText(context, "无法查看该用户的主页", Toast.LENGTH_SHORT).show()
                return@onClickNew
            }
            val userId = mUserCardViewModel.mUserId
            if (userId == SessionUtils.getUserId()) {
                //跳转我的主页
                RNPageActivity.start(
                    requireActivity(),
                    RnConstant.MINE_HOMEPAGE
                )
            } else {
                //跳转他人主页
                RNPageActivity.start(
                    requireActivity(),
                    RnConstant.PERSONAL_HOMEPAGE,
                    Bundle().apply { putLong("userId", mUserCardViewModel.mUserId) })
            }
        }

        tv_report.onClickNew {
            //举报
            val extra = Bundle()
            extra.putLong(ParamConstant.TARGET_USER_ID, mUserCardViewModel.mUserId)

            ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra).navigation()
        }

        view_caifu_level.onClickNew {
            //打开财富等级说明页
            WebActivity.startWeb(requireActivity(), "www.baidu.com")
        }
        view_guizu_level.onClickNew {
            //打开贵族等级说明页
            WebActivity.startWeb(requireActivity(), "www.baidu.com")
        }
        view_zhubo_level.onClickNew {
            //打开主播等级说明页
            WebActivity.startWeb(requireActivity(), "www.baidu.com")
        }
        ll_leyuan.onClickNew {
            //打开游戏
        }
        tv_manage.onClickNew {
            //打开管理弹窗
            val dialog = CardManagerDialogFragment.newInstance(
                programId = mUserCardViewModel.programId,
                targetUserId = mUserCardViewModel.mUserId,
                nickname = mUserCardViewModel.userInfoData.value?.nickname ?: ""
            )
            dialog.show(childFragmentManager, "CardManagerDialogFragment")
        }
    }


    /**
     * 根据数据显示视图
     */
    private fun showViewByData(data: UserInfoInRoom) {
//        state_pager_view.showSuccess()
        tv_sex.show()
        tv_location.show()
        val sex = data.sex
        ImageHelper.setDefaultHeaderPic(sdv_header, sex)
        ImageUtils.loadImage(sdv_header, data.headPic, 80f, 80f)
        tv_nickname.text = data.nickname
        tv_id.text = "欢鹊ID：${mUserCardViewModel.mUserId}"

        if (data.canReport) {
            tv_report.show()
        } else {
            tv_report.hide()
        }

        var sexDrawable: Drawable? = null
        //性别
        when (sex) {
            Sex.MALE -> {
                tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_male)
                tv_sex.textColor = Color.parseColor("#58CEFF")
            }
            Sex.FEMALE -> {
                tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_female)
                tv_sex.textColor = Color.parseColor("#FF9BC5")
            }
            else -> sexDrawable = null
        }
        if (sexDrawable != null) {
            sexDrawable.setBounds(0, 0, sexDrawable.minimumWidth, sexDrawable.minimumHeight)
            tv_sex.setCompoundDrawables(sexDrawable, null, null, null)
        } else {
            tv_sex.setCompoundDrawables(null, null, null, null)
        }
        tv_sex.text = "${data.age}"

        val city = data.city
        if (city.isEmpty()) {
            tv_location.hide()
        } else {
            tv_location.show()
            tv_location.text = city
        }

        val sign = data.mySign
        if (sign.isEmpty()) {
            tv_sign.text = GlobalUtils.getString(R.string.default_sign)
        } else {
            tv_sign.text = sign
        }

        //主播等级
        showAnchorLevel(data.anchorLevel)
        //贵族等级
        showRoyalLevel(data.royalLevel)
        //财富等级
        showCaiFuLevel(data.userLevel)
        //勋章
        showXunZhang(data.badgesPic)
        //显示小鹊列表
        showXiaoQue(data.magpieList)
        //显示标签
        showTags(data.userTags)

        //获取贵族等级图片
        val royalResource = ImageHelper.getRoyalLevelImgRound(data.royalLevel)
        if (royalResource > 0) {
            iv_royal.imageResource = royalResource
        } else {
            iv_royal.setImageDrawable(null)
        }

        if (data.follow) {
            tv_attention.text = "已关注"
        } else {
            tv_attention.text = "关注"
        }

        if (!data.hasOperate) {
            tv_manage.hide()
        } else {
            tv_manage.show()
        }
    }


    /**
     * 显示主播等级
     */
    private fun showAnchorLevel(level: Int) {
        tv_zhubo_level.text = "$level"
        if (level > 0) {
            //有主播等级，需要显示
            view_zhubo_level.show()
            iv_zhubo.show()
            tv_zhubo_title.show()
            tv_zhubo_level.show()
        } else {
            //隐藏主播等级
            view_zhubo_level.hide()
            iv_zhubo.hide()
            tv_zhubo_title.hide()
            tv_zhubo_level.hide()
        }
    }

    /**
     * 显示贵族等级
     */
    private fun showRoyalLevel(level: Int) {
        if (level > 0) {
            view_guizu_level.isSelected = true
            tv_guizu_level.text = "$level"
        } else {
            view_guizu_level.isSelected = false
//            tv_guizu_level.text = "暂无贵族"
        }

        view_guizu_level.show()
        iv_guizu.show()
        tv_guizu_title.show()
        tv_guizu_level.show()
    }

    /**
     * 显示财富等级
     */
    private fun showCaiFuLevel(level: Int) {
        tv_caifu_level.text = "$level"
        view_caifu_level.isSelected = level > 0
        view_caifu_level.show()
        iv_caifu.show()
        tv_caifu_title.show()
        tv_caifu_level.show()
    }

    /**
     * 显示勋章数据
     */
    private fun showXunZhang(badges: List<String>) {
        if (badges.isEmpty()) {
            //没有勋章，隐藏
            tv_medal.hide()
            stv_medal.hide()
        } else {
            val list = arrayListOf<TIBean>()
            tv_medal.show()
            stv_medal.show()
            badges.forEach {
                if (!TextUtils.isEmpty(it)) {
                    val image = TIBean()
                    image.type = 1
                    image.url = StringHelper.getOssImgUrl(it)
                    image.height = dp2px(16)
                    image.width = dp2px(66)
                    list.add(image)
                }
            }

            val textBean = ImageUtils.renderTextAndImage(list)
            stv_medal.renderBaseText(textBean ?: return)
        }
    }

    /**
     * 显示小鹊列表
     */
    private fun showXiaoQue(queList: List<String>) {
        if (queList.isEmpty()) {
            //没有小鹊数据
            tv_leyuan.hide()
            ll_leyuan.hide()
        } else {
            //显示小鹊相关
            tv_leyuan.show()
            ll_leyuan.show()
            tv_que_count.text = "共${queList.size}只小鹊"
            queList.asSequence().take(3).forEach {
                if (it != null) {
                    val sdv = SimpleDraweeView(activity)
                    sdv.padding = dp2px(5)
                    val lp = LinearLayout.LayoutParams(dp2px(40), dp2px(40))
                    lp.gravity = Gravity.CENTER_VERTICAL
                    sdv.layoutParams = lp
//                    ImageUtils.loadImage(iv, it, 15f, 15f)
                    ImageUtils.loadImage(sdv, it, 40f, 40f)
                    ll_leyuan.addView(sdv)
                }
            }
            //添加箭头
            val iv_arrow = ImageView(activity)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.gravity = Gravity.CENTER_VERTICAL
            iv_arrow.layoutParams = lp
            iv_arrow.setImageResource(R.mipmap.icon_arrow_msg_setting)
            ll_leyuan.addView(iv_arrow)
        }
    }

    /**
     * 显示标签
     */
    private fun showTags(tagList: List<String>) {
        if (tagList.isEmpty()) {
            //没有标签，隐藏该区域
            tv_tag.hide()
            linefeed_ll.hide()
        } else {
            //有标签，显示
            tv_tag.show()
            linefeed_ll.show()
            val tempWidth = ScreenUtils.getScreenWidth() - 2 * dp2px(20)
            //行数
            var line = 1
            var currentWidth = 0
            tagList.forEach { tag ->
                val tv = TextView(context)
                    .apply {
                        text = tag
                        backgroundResource = R.drawable.bg_tag
                        textSize = 14f
                        textColor = GlobalUtils.getColor(R.color.black_666)
                        gravity = Gravity.CENTER
                    }
                tv.setPadding(dp2px(15), 0, dp2px(15), 0)
                val params =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp2px(30f))
                params.rightMargin = dp2px(15f)
                params.topMargin = dp2px(5f)

                val needWidth = ScreenUtils.getViewRealWidth(tv)
                if (currentWidth + needWidth > tempWidth) {
                    if (line < 2) {
                        //换行
                        line++
                        currentWidth = 0
                    } else {
                        //结束
                        return@forEach
                    }
                }
                currentWidth += needWidth
                linefeed_ll.addView(tv, params)
            }

            val llParams = linefeed_ll.layoutParams
            llParams.height = dp2px(35) * line
            linefeed_ll.layoutParams = llParams
        }

    }

    override fun needEnterAnimation() = false
    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}