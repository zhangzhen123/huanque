package com.julun.huanque.core.ui.live.fragment

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.UserInfoInRoom
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.UserCardViewModel
import kotlinx.android.synthetic.main.fragment_user_card.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2020/7/29 14:51
 *@描述 用户卡片
 */
class UserCardFragment : BaseDialogFragment() {

    private val mUserCardViewModel: UserCardViewModel by activityViewModels<UserCardViewModel>()

    companion object {
        fun newInstance(userId: Long): UserCardFragment {
            val fragment = UserCardFragment()
            val bundle = Bundle()
            bundle.putLong(ParamConstant.UserId, userId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getLayoutId() = R.layout.fragment_user_card

    override fun initViews() {
        mUserCardViewModel.mUserId = arguments?.getLong(ParamConstant.UserId) ?: 0

        mUserCardViewModel.userInfoData.observe(this, Observer {
            showViewByData(it ?: return@Observer)
        })

        mUserCardViewModel.queryUserInfo()
    }

    /**
     * 根据数据显示视图
     */
    private fun showViewByData(data: UserInfoInRoom) {
        val sex = data.sex
        ImageHelper.setDefaultHeaderPic(sdv_header, sex)
        ImageUtils.loadImage(sdv_header, data.headPic, 80f, 80f)
        tv_nickname.text = data.nickname
        tv_id.text = "欢鹊ID：${mUserCardViewModel.mUserId}"
        when (sex) {
            Sex.MALE -> {
                //男
            }
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
        tv_guizu_level.text = "$level"
        if (level > 0) {
            //有主播等级，需要显示
            view_guizu_level.show()
            iv_guizu.show()
            tv_guizu_title.show()
            tv_guizu_level.show()
        } else {
            //隐藏主播等级
            view_guizu_level.hide()
            iv_guizu.hide()
            tv_guizu_title.hide()
            tv_guizu_level.hide()
        }
    }

    /**
     * 显示财富等级
     */
    private fun showCaiFuLevel(level: Int) {
        tv_caifu_level.text = "$level"
        if (level > 0) {
            //有主播等级，需要显示
            view_caifu_level.show()
            iv_caifu.show()
            tv_caifu_title.show()
            tv_caifu_level.show()
        } else {
            //隐藏主播等级
            view_caifu_level.hide()
            iv_caifu.hide()
            tv_caifu_title.hide()
            tv_caifu_level.hide()
        }
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
            //显示
            tv_medal.show()
            stv_medal.show()
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
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
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
            val list = mutableListOf<String>()
            list.add("看到设法")
            list.add("看到")
            list.add("看到l了看见对方三个")
            list.add("看到l了看见对方三个")
            list.add("看到l了看")
            list.add("个")
            list.add("看到l了看见对方三个")
            list.add("看到l了看")
            val tempWidth = ScreenUtils.getScreenWidth() - 2 * dp2px(20)
            //行数
            var line = 1
            var currentWidth = 0
            list.forEach { tag ->
                val tv = TextView(context)
                    .apply {
                        text = tag
                        backgroundResource = R.drawable.bg_tag
                        textSize = 14f
                        textColor = GlobalUtils.getColor(R.color.black_666)
                        gravity = Gravity.CENTER
                    }
                tv.setPadding(dp2px(15), 0, dp2px(15), 0)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp2px(30f))
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