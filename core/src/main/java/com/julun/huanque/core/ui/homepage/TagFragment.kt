package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.TagListAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.frag_tag.*

/**
 *@创建者   dong
 *@创建时间 2020/12/28 9:30
 *@描述 用户标签Fragment
 */
class TagFragment : BaseBottomSheetFragment() {
    companion object {
        //是否是喜欢标签弹窗
        private const val LikeTag = "LikeTag"
        fun newInstance(like: Boolean): TagFragment {
            val bundle = Bundle()
            val fragment = TagFragment()
            fragment.arguments = bundle.apply { putBoolean(LikeTag, like) }
            return fragment
        }
    }

    //喜欢的标签 标记位
    private var mLike = false

    private val mHomeViewModel: HomePageViewModel by activityViewModels()
    private val mAdapter = TagListAdapter()
    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.frag_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLike = arguments?.getBoolean(LikeTag) ?: false
        mAdapter.like = mLike
        initViews()
    }

    override fun getLayoutId() = R.layout.frag_tag


    override fun initViews() {
        if (mLike) {
            tv_like_former.text = "你已认证 "
            tv_like_later.text = " 个TA喜欢的标签"
        } else {
            tv_like_former.text = "TA拥有 "
            tv_like_later.text = " 个你已喜欢的标签"
        }
        initRecyclerView()
    }


    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(requireContext(), 4)
        recycler_view.adapter = mAdapter
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


    override fun getHeight() = dp2px(480)

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomeViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                val authTagList =
                    if (mLike) {
                        it.likeTagList
                    } else {
                        it.authTagList
                    }
                var likeCount = 0
                authTagList.forEach {
                    if (it.mark == BusiConstant.True) {
                        likeCount++
                    }
                }
                mAdapter.setList(authTagList)
                tv_love_count.text = "$likeCount"
            }
        })
    }

}