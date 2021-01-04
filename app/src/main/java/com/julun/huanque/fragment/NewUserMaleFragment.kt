package com.julun.huanque.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.SingleNewUserGiftBean
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_newuser_male.*

/**
 *@创建者   dong
 *@创建时间 2020/9/19 10:42
 *@描述 新手礼包
 */
class NewUserMaleFragment : BaseDialogFragment() {

    private val mMainViewModel: MainViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_newuser_male

    override fun initViews() {
        iv_get.onClickNew {
            mMainViewModel.receiveNewUserBag()
            dismiss()
        }
        val params = view_bottom.layoutParams
        params.width = ScreenUtils.getScreenWidth() * 305 / 375
        view_bottom.layoutParams = params

        initViewModel()
    }

    private fun initViewModel() {
        mMainViewModel.newUserBean.observe(this, Observer {
            if (it != null) {
                val list = it.bagList
                if (list.isNotEmpty()) {
                    //显示列表弹窗
                    if (ForceUtils.isIndexNotOutOfBounds(0, list)) {
                        val firstBean = list[0]
                        gift_left.show()
                        showGiftView(gift_left, firstBean)
                    } else {
                        gift_left.hide()
                    }

                    if (ForceUtils.isIndexNotOutOfBounds(1, list)) {
                        val firstBean = list[1]
                        gift_middle.show()
                        showGiftView(gift_middle, firstBean)
                    } else {
                        gift_middle.hide()
                    }

                    if (ForceUtils.isIndexNotOutOfBounds(2, list)) {
                        val firstBean = list[2]
                        gift_right.show()
                        showGiftView(gift_right, firstBean)
                    } else {
                        gift_right.hide()
                    }
                }

            }
        })
    }

    /**
     * 显示单个礼物视图
     */
    private fun showGiftView(view: View, giftBean: SingleNewUserGiftBean) {
        view.findViewById<SimpleDraweeView>(R.id.sdv_gift).loadImage(giftBean.prodPic, 63f, 52f)
        view.findViewById<TextView>(R.id.tv_gift_enable_time).text = "有效期${giftBean.validDays}天"
        view.findViewById<TextView>(R.id.tv_gift_name).text =
            "${giftBean.prodName}*${giftBean.count}"
    }

    override fun order() = 300

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
    }

    override fun configDialog() {
        setDialogSize(Gravity.CENTER, ViewGroup.LayoutParams.MATCH_PARENT)
    }

}