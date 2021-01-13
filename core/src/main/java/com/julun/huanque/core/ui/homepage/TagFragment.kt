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
import com.julun.huanque.common.suger.onAdapterClickNew
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.TagListAdapter
import com.julun.huanque.core.ui.tag_manager.AuthTagPicActivity
import com.julun.huanque.core.ui.tag_manager.TagPicsActivity
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

        //同性
        private const val SameSex = "SameSex"
        fun newInstance(like: Boolean, sameSex: Boolean): TagFragment {
            val bundle = Bundle()
            val fragment = TagFragment()
            fragment.arguments = bundle.apply {
                putBoolean(LikeTag, like)
                putBoolean(SameSex, sameSex)
            }
            return fragment
        }
    }

    //喜欢的标签 标记位
    private var mLike = false

    //同性
    private var mSameSex = false
    private val mHomeViewModel: HomePageViewModel by activityViewModels()
    private val mAdapter = TagListAdapter()
    private var mBottomSheetBehavior: BottomSheetBehavior<View>? = null

//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        return inflater.inflate(R.layout.frag_tag, container, false)
//    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
////        initViews()
//    }

    override fun getLayoutId() = R.layout.frag_tag


    override fun initViews() {
        mLike = arguments?.getBoolean(LikeTag) ?: false
        mAdapter.like = mLike
        mSameSex = arguments?.getBoolean(SameSex) ?: false

        if (mLike) {
            tv_title.text = "TA喜欢的标签"

            if (mSameSex) {
                //同性
                tv_like_former.text = "你们有 "
                tv_like_later.text = " 个共同喜欢的标签"
            } else {
                //异性
                tv_like_former.text = "你已认证 "
                tv_like_later.text = " 个TA喜欢的标签"
            }
        } else {
            //Ta拥有的标签
            tv_title.text = "TA拥有的标签"
            if (mSameSex) {
                //同性
                tv_like_former.text = "你已认证 "
                tv_like_later.text = " 个TA拥有的标签"
            } else {
                //异性
                tv_like_former.text = "TA拥有 "
                tv_like_later.text = " 个你已喜欢的标签"
            }

        }
        initRecyclerView()
    }


    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(requireContext(), 4)
        recycler_view.adapter = mAdapter
        mAdapter.onAdapterClickNew { _, _, position ->
            val item = mAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
//            AuthTagPicActivity.start(requireActivity(), item.tagId, mHomeViewModel.mineHomePage, mLike, isSameSex)
            if (mLike) {
                //喜欢的标签
                if (mSameSex) {
                    //同性 跳转列表页
                    TagPicsActivity.start(requireActivity(), item, mHomeViewModel.targetUserId)
                } else {
                    //异性 跳转认证标签页
                    AuthTagPicActivity.start(requireActivity(), item.tagId, mHomeViewModel.mineHomePage, true, mSameSex)
                }
            } else {
                //拥有的标签
                TagPicsActivity.start(requireActivity(), item, mHomeViewModel.targetUserId)
            }
        }
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