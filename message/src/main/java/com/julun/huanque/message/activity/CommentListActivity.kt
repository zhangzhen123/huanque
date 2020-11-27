package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.CommentListAdapter
import kotlinx.android.synthetic.main.act_comment_list.*

/**
 *@创建者   dong
 *@创建时间 2020/11/27 16:09
 *@描述 评论列表页面
 */
class CommentListActivity : BaseActivity() {
    private val mAdapter = CommentListAdapter()
    override fun getLayoutId() = R.layout.act_comment_list

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.textTitle.text = "评论"
        initRecyclrView()
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