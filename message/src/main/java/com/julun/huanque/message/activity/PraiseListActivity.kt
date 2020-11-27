package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.CommentListAdapter
import com.julun.huanque.message.adapter.PraiseListAdapter
import kotlinx.android.synthetic.main.act_comment_list.*

/**
 *@创建者   dong
 *@创建时间 2020/11/27 16:09
 *@描述 点赞列表页面
 */
class PraiseListActivity : BaseActivity() {
    private val mAdapter = PraiseListAdapter()
    override fun getLayoutId() = R.layout.act_comment_list

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.textTitle.text = "点赞"
        initRecyclrView()
        mAdapter?.loadMoreModule?.isEnableLoadMore = true
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclrView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter
    }
}