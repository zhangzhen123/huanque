package com.julun.huanque.core.ui.homepage

import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.EvaluateTags
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.RecomEvaluateAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.fragment_home_page_evaluate.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

/**
 *@创建者   dong
 *@创建时间 2020/10/26 19:49
 *@描述 主页使用  评论弹窗
 */
class HomePageEvaluateFragment : BaseDialogFragment() {
    private var maxCount = 3
    private val mHomePageViewModel: HomePageViewModel by activityViewModels()
    private val mRecomEvaluateAdapter = RecomEvaluateAdapter()

    //原始的评价列表
    private val mOriEvaluate = mutableListOf<String>()

    //显示的评价列表
    private val mShowEvaluate = mutableListOf<String>()

    override fun getLayoutId() = R.layout.fragment_home_page_evaluate

    override fun initViews() {
        initRecyclerView()
        initEvents()
    }

    /**
     * 初始化监听
     */
    private fun initEvents() {
        tv_done.onClickNew {
            if (mOriEvaluate.isEmpty() && mShowEvaluate.isEmpty()) {
                ToastUtils.show("你还未添加任何评论哦")
                return@onClickNew
            }
            mHomePageViewModel.evaluteateFriend(mShowEvaluate)
        }
    }


    private fun initRecyclerView() {
        recyclerView_recome.layoutManager = GridLayoutManager(context, 4)
        recyclerView_recome.adapter = mRecomEvaluateAdapter
        mRecomEvaluateAdapter.setOnItemClickListener { adapter, view, position ->
            val str = mRecomEvaluateAdapter.getItemOrNull(position)
            if (str != null) {
                if (mShowEvaluate.contains(str)) {
                    //移除评论
                    mShowEvaluate.remove(str)
                    mRecomEvaluateAdapter.evaluateList = mShowEvaluate
                    adapter.notifyItemChanged(position)
                } else {
                    //添加评价
                    if (mShowEvaluate.size >= 3) {
                        showOutToast()
                        return@setOnItemClickListener
                    }
                    mShowEvaluate.add(str)
                    mRecomEvaluateAdapter.evaluateList = mShowEvaluate
                    adapter.notifyItemChanged(position)
                }
                updateCount()
                judgeDoneEnable()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mHomePageViewModel.evaluateContent.value = null
        mRecomEvaluateAdapter.setList(null)
        initViewModel()
        setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun initViewModel() {
        mHomePageViewModel.evaluateTagsBean.observe(this, Observer {
            maxCount = it?.maxEvaluateCnt ?: 3
            showViewByData(it ?: return@Observer)
        })
//        mHomePageViewModel.evaluateContent.observe(this, Observer {
//            if (it != null) {
//                mShowEvaluate.add(it)
//            }
//        })
        mHomePageViewModel.evaluateFlag.observe(this, Observer {
            if (it == true) {
                mHomePageViewModel.evaluateFlag.value = false
                dismiss()
            }
        })
    }

    /**
     * 根据数据 显示视图
     */
    private fun showViewByData(bean: EvaluateTags) {
        val evaluateList = bean.myEvaluateTagList
        val recomTagList = bean.recomTagList
        val realMyTagList = mutableListOf<String>()

        evaluateList.forEach {
            if (recomTagList.contains(it)) {
                realMyTagList.add(it)
            }
        }
        mOriEvaluate.clear()
        mOriEvaluate.addAll(realMyTagList)
        mShowEvaluate.clear()
        mShowEvaluate.addAll(realMyTagList)
        mRecomEvaluateAdapter.evaluateList = realMyTagList
        mRecomEvaluateAdapter.setList(recomTagList)
        updateCount()
        judgeDoneEnable()
    }


    /**
     * 判断完成按钮是否可用
     */
    private fun judgeDoneEnable() {
        if (mOriEvaluate.size != mShowEvaluate.size) {
            tv_done.isSelected = true
        }
        //接下来判断是否相互包含
        mOriEvaluate.forEach {
            if (!mShowEvaluate.contains(it)) {
                tv_done.isSelected = true
                return
            }
        }
        mShowEvaluate.forEach {
            if (!mOriEvaluate.contains(it)) {
                tv_done.isSelected = true
                return
            }
        }
        tv_done.isSelected = false
    }

    /**
     * 更新已选中数量
     */
    private fun updateCount() {
        tv_my_evaluate.text = "我的评价 (${mShowEvaluate.size}/${maxCount})"
    }

    private fun showOutToast() {
        ToastUtils.show("最多添加${maxCount}个评论哦")
    }


}