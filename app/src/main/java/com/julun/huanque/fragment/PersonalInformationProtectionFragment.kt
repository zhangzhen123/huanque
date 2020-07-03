package com.julun.huanque.fragment

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClick
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ToastUtils
import kotlinx.android.synthetic.main.fragment_personal_information_protection.*

/**
 *@创建者   dong
 *@创建时间 2020/7/3 10:48
 *@描述 个人信息保护指引弹窗
 */
class PersonalInformationProtectionFragment : BaseDialogFragment() {
    override fun getLayoutId() = R.layout.fragment_personal_information_protection

    override fun initViews() {
        initListener()
        val style = SpannableStringBuilder()
        //设置文字
        val str = GlobalUtils.getString(R.string.agreement_content)
        val replaceStr = GlobalUtils.getString(R.string.app_name)
        val content = String.format(str, replaceStr, replaceStr)
        style.append(content)
        updateTextColorAndClick(style, content, GlobalUtils.getString(R.string.agreement_conent_clickable), "")
        updateTextColorAndClick(style, content, GlobalUtils.getString(R.string.agreement_register_conent_clickable), "")
        tv_content.text = style
        //配置给TextView
        tv_content.movementMethod = LinkMovementMethod.getInstance()
        tv_content.highlightColor = GlobalUtils.getColor(android.R.color.transparent)
        tv_content.text = style
    }

    private fun initListener() {
        tv_agree.onClickNew {
            dismiss()
        }
        tv_exit.onClickNew { dismiss() }
    }


    override fun onStart() {
        super.onStart()
        setWindowConfig()
    }

    private fun setWindowConfig() {
        this.setDialogSize(Gravity.CENTER, 35)
        //不需要半透明遮罩层
        val win = dialog?.window ?: return
//        win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        win.setWindowAnimations(R.style.dialog_bottom_bottom_style)
    }


    /**
     * 修改文案颜色并添加点击跳转
     * @param sb
     * @param content 全部文案内容
     * @param condition 识别的问题
     * @param address 跳转地址
     */
    private fun updateTextColorAndClick(sb: SpannableStringBuilder, content: String, condition: String, address: String) {
        if (content.contains(condition)) {
            //设置部分文字点击事件
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    //点击事件
                    ToastUtils.show("获取到点击事件")
//                    val extra = Bundle()
//                    extra.putString(BusiConstant.PUSH_URL, LMUtils.getDomainName(address))
//                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_DO_NOT_GO_HOME.name, true)
//                    ARouter.getInstance().build(ARouterConstant.WEB_ACTIVITY)
//                        .with(extra).navigation()
                    ///rules/protocolUserPrivacy.html
                }
            }
            val startIndex = content.indexOf(condition)

            sb.setSpan(clickableSpan, startIndex, startIndex + condition.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)


            //设置部分文字颜色
            val foregroundColorSpan = ForegroundColorSpan(GlobalUtils.getColor(R.color.agreement_blue))
            sb.setSpan(foregroundColorSpan, startIndex, startIndex + condition.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }
}