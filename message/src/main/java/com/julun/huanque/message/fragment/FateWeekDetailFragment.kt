package com.julun.huanque.message.fragment

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.adapter.FateWeekDetailAdapter
import com.julun.huanque.message.viewmodel.YuanFenViewModel
import kotlinx.android.synthetic.main.fragment_fate_week_detail.*

/**
 *@创建者   dong
 *@创建时间 2020/10/12 20:01
 *@描述 派单周详情数据
 */
class FateWeekDetailFragment : BaseDialogFragment() {

    private val mAdapter = FateWeekDetailAdapter()
    private val mYuanFenViewModel: YuanFenViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_fate_week_detail

    override fun initViews() {
        initRecyclerView()
        iv_arrow_up.onClickNew {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
        //不需要半透明遮罩层
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        initViewModel()
    }

    private fun initViewModel() {
        mYuanFenViewModel.fateWeekInfoBean.observe(this, Observer {
            if (it != null) {
                tv_left.text = it.result
                tv_middle.text = "${it.totalNum}"
                tv_right.text = "${it.replyNum}"
                mAdapter.setList(it.itemList)
            }
        })
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter
    }

}