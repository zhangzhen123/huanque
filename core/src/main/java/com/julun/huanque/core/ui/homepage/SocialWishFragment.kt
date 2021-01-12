package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.SocialDetailAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.frag_social_wish.*

/**
 *@创建者   dong
 *@创建时间 2020/12/29 14:26
 *@描述 职业弹窗
 */
class SocialWishFragment : BaseBottomSheetFragment() {

    companion object {
        /**
         * @param count 社交意愿数量
         */
        fun newInstance(count: Int): SocialWishFragment {
            val fragment = SocialWishFragment()
            val bundle = Bundle()
            bundle.putInt(ParamConstant.Count, count)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val mHomePageViewModel: HomePageViewModel by activityViewModels()
    private val mAdapter = SocialDetailAdapter()

    //社交意愿数量  默认为1
    private var count = 1
    private var mHeight = 0
    override fun getLayoutId() = R.layout.frag_social_wish

    override fun initViews() {
        initRecyclerView()
        val params = recycler_view.layoutParams as? ConstraintLayout.LayoutParams
        params?.height = mHeight - dp2px(46)
        recycler_view.layoutParams = params
    }

    override fun getHeight(): Int {
        count = arguments?.getInt(ParamConstant.Count) ?: 1
        //计算高度
        mHeight = dp2px(10 + 5 + 46 + 90 * count)
        return mHeight
    }

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
                mAdapter.setList(it.wishList)
            }
        })
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = mAdapter
    }


}