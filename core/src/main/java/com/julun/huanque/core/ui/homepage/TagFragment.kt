package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onAdapterClickNew
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.TagListAdapter
import com.julun.huanque.core.ui.tag_manager.AuthTagPicActivity
import com.julun.huanque.core.ui.tag_manager.TagPicsActivity
import kotlinx.android.synthetic.main.frag_tag.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

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
        private const val IsMe = "IsMe"
        private const val TargetUserId = "targetUserId"

        private const val TagList = "tagList"
        fun newInstance(): TagFragment {
            val bundle = Bundle()
            val fragment = TagFragment()
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(
            like: Boolean,
            sameSex: Boolean,
            isMe: Boolean,
            targetUserId: Long,
            tagList: ArrayList<UserTagBean>
        ): TagFragment {
            val bundle = Bundle()
            val fragment = TagFragment()
            fragment.arguments = bundle.apply {
                putBoolean(LikeTag, like)
                putBoolean(SameSex, sameSex)
                putBoolean(IsMe, isMe)
                putLong(TargetUserId, targetUserId)

                putSerializable(TagList, tagList)
            }
            return fragment
        }
    }

    fun setParams(
        like: Boolean,
        sameSex: Boolean,
        isMe: Boolean,
        targetUserId: Long, tagList: ArrayList<UserTagBean>
    ) {
        arguments?.apply {
            putBoolean(LikeTag, like)
            putBoolean(SameSex, sameSex)
            putBoolean(IsMe, isMe)
            putLong(TargetUserId, targetUserId)
            putSerializable(TagList, tagList)
        }
    }

    private var tagList: ArrayList<UserTagBean>? = null

    //喜欢的标签 标记位
    private var mLike = false

    //同性
    private var mSameSex = false

    private var targetUserId: Long = 0L

    private var isMe: Boolean = false

    //    private val mHomeViewModel: HomePageViewModel by activityViewModels()
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLike = arguments?.getBoolean(LikeTag) ?: false
        mSameSex = arguments?.getBoolean(SameSex) ?: false
        isMe = arguments?.getBoolean(IsMe) ?: false
        targetUserId = arguments?.getLong(TargetUserId, 0) ?: 0
        tagList = arguments?.getSerializable(TagList) as? ArrayList<UserTagBean>

        if (!isMe) {
            if (mSameSex) {
                if (mLike) {
                    mAdapter.mLikeStyle = true
                } else {
                    mAdapter.mLikeStyle = false
                }
            } else {
                if (mLike) {
                    mAdapter.mLikeStyle = false
                } else {
                    mAdapter.mLikeStyle = true
                }
            }

        }
//        mAdapter.mLikeStyle = mLike

        if (mLike) {
            tv_title.text = "TA喜欢的标签 ${tagList?.size ?: 0}"

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
            tv_title.text = "TA拥有的标签 ${tagList?.size ?: 0}"
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
//        val authTagList =
//            if (mLike) {
//                it.likeTagList
//            } else {
//                it.authTagList
//            }
        var likeCount = 0
        tagList?.forEach {
            if (it.mark == BusiConstant.True) {
                likeCount++
            }
        }
        tagList?.let { list ->
            Collections.sort(list, object : Comparator<UserTagBean> {
                override fun compare(o1: UserTagBean?, o2: UserTagBean?): Int {
                    if (o1?.mark == BusiConstant.True) {
                        return -1
                    } else {
                        return 1
                    }
                }
            })


        }

        mAdapter.setList(tagList)
        tv_love_count.text = "$likeCount"
    }

    override fun initViews() {
        initRecyclerView()
    }


    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(requireContext(), 4)
        recycler_view.adapter = mAdapter
        val footView = View(requireContext())
        mAdapter.setFooterView(footView)
        val footParams = footView.layoutParams
        footParams.height = dp2px(50)
        footView.layoutParams = footParams

        mAdapter.onAdapterClickNew { _, _, position ->
            val item = mAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
//            AuthTagPicActivity.start(requireActivity(), item.tagId, mHomeViewModel.mineHomePage, mLike, isSameSex)


            if (!isMe) {
                if (mSameSex) {
                    if (mLike) {
                        TagPicsActivity.start(requireActivity(), item, targetUserId)
                    } else {
                        AuthTagPicActivity.start(requireActivity(), item.tagId, isMe, mLike, mSameSex)
                    }
                } else {
                    if (mLike) {
                        AuthTagPicActivity.start(requireActivity(), item.tagId, isMe, mLike, mSameSex)
                    } else {
                        TagPicsActivity.start(requireActivity(), item, targetUserId)
                    }
                }

            }
//            if (mLike) {
//                //喜欢的标签
//                if (mSameSex) {
//                    //同性 跳转列表页
//                    TagPicsActivity.start(requireActivity(), item, targetUserId)
//                } else {
//                    //异性 跳转认证标签页
//                    AuthTagPicActivity.start(requireActivity(), item.tagId, isMe, true, mSameSex)
//                }
//            } else {
//                //拥有的标签
//                TagPicsActivity.start(requireActivity(), item, targetUserId)
//            }
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
//        mHomeViewModel.homeInfoBean.observe(this, Observer {
//            if (it != null) {
//                val authTagList =
//                    if (mLike) {
//                        it.likeTagList
//                    } else {
//                        it.authTagList
//                    }
//                var likeCount = 0
//                authTagList.forEach {
//                    if (it.mark == BusiConstant.True) {
//                        likeCount++
//                    }
//                }
//                mAdapter.setList(authTagList)
//                tv_love_count.text = "$likeCount"
//            }
//        })
    }

}