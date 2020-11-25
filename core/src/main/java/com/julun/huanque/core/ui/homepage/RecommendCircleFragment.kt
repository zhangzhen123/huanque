package com.julun.huanque.core.ui.homepage

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.AttentionCircleAdapter
import kotlinx.android.synthetic.main.fragment_attention_circle.*

/**
 *@创建者   dong
 *@创建时间 2020/11/24 10:20
 *@描述 关注圈子Fragment
 */
class RecommendCircleFragment : BaseFragment() {
    companion object{
        fun newInstance() = RecommendCircleFragment()
    }

    private var mAttentionCircleAdapter = AttentionCircleAdapter()

    override fun getLayoutId() = R.layout.fragment_attention_circle

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initRecyclerView()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAttentionCircleAdapter
    }

}