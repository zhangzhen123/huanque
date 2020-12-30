package com.julun.huanque.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.julun.huanque.R
import com.julun.huanque.adapter.TagPicAdapter
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.viewmodel.SelectTagViewModel
import kotlinx.android.synthetic.main.frag_choose_tag.*

/**
 *@创建者   dong
 *@创建时间 2020/12/17 14:20
 *@描述 选择标签Fragment
 */
class SelectTagFragment(var type: String = "") : BaseFragment() {

    private val mTagViewModel: SelectTagViewModel by activityViewModels()
    private val mAdapter = TagPicAdapter()

    override fun getLayoutId() = R.layout.frag_choose_tag

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initRecyclerView()
        initViewModel()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mTagViewModel.loginTagData.observe(this, Observer {
            if (it != null) {
                val tagTabList = it.tagTypeList
                tagTabList.forEach { tabBean ->
                    if (tabBean.tagType == type) {
                        //找到对应的tag列表
                        mAdapter.setList(tabBean.tagList)
                        return@Observer
                    }
                }
            }
        })
    }

    private fun initRecyclerView() {
        recycler_view.adapter = mAdapter
        recycler_view.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        recycler_view.startScroll()
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val realCount = mAdapter.data.size
            if (realCount <= 0) {
                return@setOnItemClickListener
            }
            val tagBean =
                mAdapter.getItemOrNull(position % realCount) ?: return@setOnItemClickListener
            tagBean.selected = !tagBean.selected
            mAdapter.notifyDataSetChanged()
            //修改外界数量
            mTagViewModel.updateSelectCountFlag.value = true
        }
    }
}