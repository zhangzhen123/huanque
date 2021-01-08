package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.ConstellationInfo
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.ConstellationViewModel
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.frag_constellation.*

/**
 *@创建者   dong
 *@创建时间 2020/12/29 14:26
 *@描述 星座弹窗
 */
class ConstellationFragment : BaseBottomSheetFragment() {
    companion object {

        /**
         * 更新生日页面使用
         */
        fun newInstance(type: String): ConstellationFragment {
            val fragment = ConstellationFragment()
            val bundle = Bundle().apply {
                putString(ParamConstant.TYPE, type)
            }
            fragment.arguments = bundle
            return fragment
        }

    }

    private val mHomePageViewModel: HomePageViewModel by activityViewModels()

    private val mConstellationViewModel: ConstellationViewModel by viewModels()

    override fun getLayoutId() = R.layout.frag_constellation

    override fun initViews() {
        val type = arguments?.getString(ParamConstant.TYPE) ?: ""
        if (type.isNotEmpty()) {
            mConstellationViewModel.queryConstellation(type)
        }
    }

    override fun getHeight() = dp2px(423)
    override fun onStart() {
        super.onStart()
//        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val win = dialog?.window ?: return
        win.setWindowAnimations(R.style.dialog_bottom_bottom_style)
        initViewModel()
        val parent = view?.parent
        if (parent is View) {
            parent.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomePageViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it.constellationInfo)
            }
        })
        mConstellationViewModel.constellationData.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }
        })
    }

    /**
     * 显示数据
     */
    private fun showViewByData(info: ConstellationInfo) {
        sdv_constell.loadImage(info.constellationPic, 130f, 130f)
        tv_constell.text = info.constellationName
        tv_constell_time.text = info.hitText
        tv_match.text = info.pairConstellation
        tv_introduce.text = info.constellationDesc
    }


}