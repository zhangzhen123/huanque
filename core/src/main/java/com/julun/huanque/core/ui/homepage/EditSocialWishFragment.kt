package com.julun.huanque.core.ui.homepage

import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.SocialWishBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.EditSocialWishAdapter
import com.julun.huanque.core.viewmodel.EditInfoViewModel
import kotlinx.android.synthetic.main.frag_edit_social_wish.*
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2021/1/7 11:29
 *@描述 编辑页面 社交意愿
 */
class EditSocialWishFragment : BaseBottomSheetFragment() {
    private val mEditInfoViewModel: EditInfoViewModel by activityViewModels()
    private val mAdapter = EditSocialWishAdapter()
    override fun getLayoutId() = R.layout.frag_edit_social_wish

    override fun initViews() {
        initRecyclerView()
        tv_save.onClickNew {
            val originWishes = mEditInfoViewModel.basicInfo.value?.wishList ?: return@onClickNew
            val originWishtypes = mutableListOf<String>()
            originWishes.forEach {
                originWishtypes.add(it.wishType)
            }
            originWishtypes.sort()
            val oriStr = StringBuilder()
            originWishtypes.forEach {
                if (oriStr.isNotEmpty()) {
                    oriStr.append(",")
                }
                oriStr.append(it)
            }

            val totalWishes = mEditInfoViewModel.basicInfo.value?.wishConfigList ?: return@onClickNew
            val totalWishtypes = mutableListOf<String>()
            totalWishes.forEach {
                if (it.selected == BusiConstant.True) {
                    totalWishtypes.add(it.wishType)
                }
            }
            totalWishtypes.sort()

            val curStr = StringBuilder()
            totalWishtypes.forEach {
                if (curStr.isNotEmpty()) {
                    curStr.append(",")
                }
                curStr.append(it)
            }

            if (oriStr != curStr) {
                //数据发生变化
                mEditInfoViewModel.saveSocialWish(curStr.toString())
            } else {
                dismiss()
            }
        }

    }


    override fun getHeight() = dp2px(306)

    override fun onStart() {
        super.onStart()
        initViewModel()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position ->

            val tempBean = adapter.getItemOrNull(position) as? SocialWishBean ?: return@setOnItemClickListener
            if (tempBean.selected == BusiConstant.True) {
                //取消选中
                val selectCount = getSelectCount()
                if (selectCount <= 1) {
                    return@setOnItemClickListener
                }

                tempBean.selected = BusiConstant.False
            } else {
                tempBean.selected = BusiConstant.True
            }

            adapter.notifyDataSetChanged()
        }
    }

    /**
     * 获取选中的数量
     */
    private fun getSelectCount(): Int {
        var count = 0
        mAdapter.data.forEach {
            if (it.selected == BusiConstant.True) {
                count++
            }
        }
        return count
    }


    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mEditInfoViewModel.basicInfo.observe(this, Observer {
            if (it != null) {
                val wishTypes = mutableListOf<String>()
                it.wishList.forEach { swb ->
                    wishTypes.add(swb.wishType)
                }
                it.wishConfigList.forEach { swb ->
                    swb.selected = if (wishTypes.contains(swb.wishType)) BusiConstant.True else BusiConstant.False
                }
                mAdapter.setList(it.wishConfigList)
            }
        })

        mEditInfoViewModel.socialSuccessData.observe(this, Observer {
            if (it == true) {
                mEditInfoViewModel.socialSuccessData.value = null
                dismiss()
            }
        })
    }


}