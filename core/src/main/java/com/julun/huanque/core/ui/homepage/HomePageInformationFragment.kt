package com.julun.huanque.core.ui.homepage

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.bean.beans.HomePageInfo
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.bean.beans.SocialWishBean
import com.julun.huanque.common.bean.forms.InviteCompleteForm
import com.julun.huanque.common.constant.MyTagType
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomePageTagAdapter
import com.julun.huanque.core.ui.tag_manager.AuthTagPicActivity
import com.julun.huanque.core.ui.tag_manager.TagPicsActivity
import com.julun.huanque.core.ui.tag_manager.MyTagsActivity
import com.julun.huanque.core.viewmodel.HomePageViewModel
import com.julun.huanque.core.viewmodel.InviteFillViewModel
import kotlinx.android.synthetic.main.frag_home_page_information.*
import org.jetbrains.anko.textColor
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2020/12/22 14:59
 *@描述 主页资料模块
 */
class HomePageInformationFragment : BaseFragment() {
    companion object {
        fun newInstance(): HomePageInformationFragment {
            val fragment = HomePageInformationFragment()
            return fragment
        }
    }

    private val mHomePageViewModel: HomePageViewModel by activityViewModels()

    //邀请填写弹窗使用
    private val mInviteViewModel: InviteFillViewModel by activityViewModels()

    //标签adapter
    private val mTagAdapter = HomePageTagAdapter()

    //我喜欢的标签adapter
    private val mLikeTagAdapter = HomePageTagAdapter()

    //拥有的标签
    private val mTagFragment: TagFragment by lazy { TagFragment.newInstance() }

    //身材弹窗
    private val mFigureFragment: FigureFragment by lazy { FigureFragment() }

    //职业
    private val mJobFragment: JobFragment by lazy { JobFragment() }

    //学校数据
    private val mSchoolFragment: SchoolFragment by lazy { SchoolFragment() }

    //喜欢的标签
//    private val mLikeTagFragment: TagFragment by lazy { TagFragment.newInstance() }

    //邀请弹窗
    private val mInviteFillFragment: InviteFillFragment by lazy { InviteFillFragment() }

    override fun getLayoutId() = R.layout.frag_home_page_information

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        initRecyclerView()

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        ll_tag.onClickNew {
            //他拥有的标签
            if (tv_more_tag.visibility == View.VISIBLE) {
                if (mHomePageViewModel.mineHomePage) {
                    MyTagsActivity.start(requireActivity(), MyTagType.AUTH)
                } else {
                    //显示他拥有的标签弹窗
                    val list=mHomePageViewModel.homeInfoBean.value?.authTagList
                    if (list != null) {
                        mTagFragment.setParams(false,isSameSex,mHomePageViewModel.mineHomePage,mHomePageViewModel.targetUserId,list)
                        mTagFragment.show(childFragmentManager, "TagFragment")
                    }

                }
            }
        }
//        rl_tag.onClickNew {
//            ll_tag.performClick()
//        }

        ll_like_tag.onClickNew {
            if (tv_more_like_tag.visibility == View.VISIBLE) {
                //显示他拥有的标签弹窗
                if (mHomePageViewModel.mineHomePage) {
                    MyTagsActivity.start(requireActivity(), MyTagType.LIKE)
                } else {
                    mInviteViewModel.mType = InviteCompleteForm.Information
                    val list=mHomePageViewModel.homeInfoBean.value?.likeTagList
                    if (list != null) {
                        mTagFragment.setParams(true,isSameSex,mHomePageViewModel.mineHomePage,mHomePageViewModel.targetUserId,list)
                        mTagFragment.show(childFragmentManager, "TagFragment")
                    }

                }

            }
        }

        view_home_town.onClickNew {
            //家乡数据
            if ((mHomePageViewModel.homeInfoBean.value?.homeTown?.homeTownId ?: 0) > 0) {
                //有家乡数据
                HomeTownFragment().show(childFragmentManager, "HomeTownFragment")
            } else {
                //无家乡数据,邀请填写
                if (mHomePageViewModel.mineHomePage) {
                    return@onClickNew
                }
                mInviteViewModel.mType = InviteCompleteForm.Information
                mInviteFillFragment.show(childFragmentManager, "InviteFillFragment")
            }
        }

        view_stature.onClickNew {
            //身材
            if ((mHomePageViewModel.homeInfoBean.value?.figure?.height ?: 0) > 0) {
                //有身材数据
                mFigureFragment.show(childFragmentManager, "FigureFragment")
            } else {
                //无身材数据,邀请填写
                if (mHomePageViewModel.mineHomePage) {
                    return@onClickNew
                }
                mInviteViewModel.mType = InviteCompleteForm.Information
                mInviteFillFragment.show(childFragmentManager, "InviteFillFragment")
            }
        }

        view_constellation.onClickNew {
            //星座
            val conType = mHomePageViewModel.homeInfoBean.value?.constellationType
            if (conType?.isNotEmpty() == true) {
                //有星座数据
                ConstellationFragment.newInstance(conType).show(childFragmentManager, "ConstellationFragment")
            } else {
                //没有星座数据
                //显示邀请弹窗
                if (mHomePageViewModel.mineHomePage) {
                    return@onClickNew
                }
                mInviteViewModel.mType = InviteCompleteForm.Information
                mInviteFillFragment.show(childFragmentManager, "InviteFillFragment")
            }
        }
        view_job.onClickNew {
            //职业
            val profession = mHomePageViewModel.homeInfoBean.value?.profession
            if (profession?.professionName?.isNotEmpty() == true && profession?.professionTypeText?.isNotEmpty()) {
                mJobFragment.show(childFragmentManager, "JobFragment")
            } else {
                //显示邀请弹窗
                if (mHomePageViewModel.mineHomePage) {
                    return@onClickNew
                }
                mInviteViewModel.mType = InviteCompleteForm.Information
                mInviteFillFragment.show(childFragmentManager, "InviteFillFragment")
            }
        }

        view_wish.onClickNew {
            //学校
            if (mHomePageViewModel.homeInfoBean.value?.schoolInfo?.school?.isNotEmpty() == true) {
                //有学校数据
                mSchoolFragment.show(childFragmentManager, "SchoolFragment")
            } else {
                //没有学校数据
                //显示邀请弹窗
                if (mHomePageViewModel.mineHomePage) {
                    return@onClickNew
                }
                mInviteViewModel.mType = InviteCompleteForm.Information
                mInviteFillFragment.show(childFragmentManager, "InviteFillFragment")
            }
        }

        view_social.onClickNew {
            //社交意愿
            val wishListCount = mHomePageViewModel.homeInfoBean.value?.wishList?.size ?: 0
            if (wishListCount == 0) {
                //没有社交意愿
                //显示邀请弹窗
                if (mHomePageViewModel.mineHomePage) {
                    return@onClickNew
                }
                mInviteViewModel.mType = InviteCompleteForm.Information
                mInviteFillFragment.show(childFragmentManager, "InviteFillFragment")
            } else {
                //有社交意愿
                SocialWishFragment.newInstance(wishListCount).show(childFragmentManager, "SocialWishFragment")
            }
        }

        tv_user_id.onClickNew {
            //用户ID
            GlobalUtils.copyToSharePlate(requireContext(), "${mHomePageViewModel.targetUserId}", "已复制欢鹊ID")
        }

    }

    override fun onStart() {
        super.onStart()
        initViewModel()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view_tag.layoutManager = GridLayoutManager(context, 4)
        recycler_view_tag.adapter = mTagAdapter
        mTagAdapter.setOnItemClickListener { adapter, view, position ->
//            ll_tag.performClick()
            val item = mTagAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            if (item.tagId == 0) {
                //空布局，显示邀请
                if (mHomePageViewModel.mineHomePage) {
                    return@setOnItemClickListener
                }
                mInviteViewModel.mType = InviteCompleteForm.AuthTag
                mInviteFillFragment.show(childFragmentManager, "InviteFillFragment")
            } else {
                if (mHomePageViewModel.mineHomePage) {
                    //我的主页
                    AuthTagPicActivity.start(requireActivity(), item.tagId, mHomePageViewModel.mineHomePage, false, true)
                } else {
                    if (isSameSex) {
                        AuthTagPicActivity.start(requireActivity(), item.tagId, mHomePageViewModel.mineHomePage, false, isSameSex)
                    } else {
                        TagPicsActivity.start(requireActivity(), item, mHomePageViewModel.targetUserId)
                    }
                }
            }

        }

        recycler_view_like_tag.layoutManager = GridLayoutManager(context, 4)
        recycler_view_like_tag.adapter = mLikeTagAdapter
        mLikeTagAdapter.like = true
        mLikeTagAdapter.setOnItemClickListener { adapter, view, position ->
//            ll_tag.performClick()
            val item = mLikeTagAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            if (item.tagId == 0) {
                //空布局，显示邀请
                if (mHomePageViewModel.mineHomePage) {
                    return@setOnItemClickListener
                }
                mInviteViewModel.mType = InviteCompleteForm.AuthTag
                mInviteFillFragment.show(childFragmentManager, "InviteFillFragment")
            } else {
                if (mHomePageViewModel.mineHomePage) {
                    //我的主页
//                    AuthTagPicActivity.start(requireActivity(), item.tagId, mHomePageViewModel.mineHomePage, true, false)
                    TagPicsActivity.start(requireActivity(), item, mHomePageViewModel.targetUserId)
                } else {
                    if (isSameSex) {
                        //同性
                        TagPicsActivity.start(requireActivity(), item, mHomePageViewModel.targetUserId)
                    } else {
                        //异性
                        AuthTagPicActivity.start(requireActivity(), item.tagId, mHomePageViewModel.mineHomePage, true, isSameSex)
                    }
                }
            }
        }

//        recycler_view_tag_mine.layoutManager = GridLayoutManager(context, 4)


    }

    private var isSameSex: Boolean = false

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomePageViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
                mInviteViewModel.userId = it.currUserId
            }
        })
    }

    private fun showViewByData(bean: HomePageInfo) {
        isSameSex = bean.sex == bean.currSexType
        //家乡
        val homeTownStr = StringBuilder()
        if (bean.homeTown.homeTownProvince.isNotEmpty()) {
            homeTownStr.append(bean.homeTown.homeTownProvince)
        }
        if (homeTownStr.isNotEmpty()) {
            homeTownStr.append("/")
        }
        homeTownStr.append(bean.homeTown.homeTownCity)
        when {
            homeTownStr.length > 6 -> {
                tv_home_town.text = "${homeTownStr.substring(0, 6)}..."
            }
            homeTownStr.isNotEmpty() -> {
                tv_home_town.text = homeTownStr.toString()
            }
            else -> {
                tv_home_town.text = "-"
            }
        }

        //身材
        val weight = bean.figure.weight
        val height = bean.figure.height
        val whBuilder = StringBuilder()
        if (height > 0) {
            whBuilder.append("${height}cm")
        }
        if (weight > 0) {
            if (height > 0) {
                whBuilder.append("/")
            }
            whBuilder.append("${weight}kg")
        }
        if (whBuilder.isNotEmpty()) {
            tv_stature.text = whBuilder.toString()
        } else {
            tv_stature.text = "-"
        }

        //星座
        val constellationName = bean.constellation
        if (constellationName.isEmpty()) {
            tv_constellation.text = "-"
        } else {
            tv_constellation.text = constellationName
        }
        //职业
        val professionNanme = bean.profession.professionName
        if (professionNanme.isEmpty()) {
            tv_job.text = "-"
        } else {
            tv_job.text = professionNanme
        }
        //毕业院校
        val school = bean.schoolInfo.school
        if (school.isEmpty()) {
            tv_school.text = "-"
        } else {
            tv_school.text = school
        }
        //社交意愿
        vf_social.removeAllViews()
        if (bean.wishList.isEmpty()) {
            bean.wishList.add(SocialWishBean("", "-"))
        }
        bean.wishList.forEach {
            val tempTv = TextView(requireContext())
            tempTv.apply {
                textSize = 12f
                textColor = GlobalUtils.getColor(R.color.black_333)
                text = it.wishTypeText
                gravity = Gravity.CENTER_VERTICAL
                bold()
            }
            vf_social.addView(tempTv, ViewGroup.LayoutParams.MATCH_PARENT, dp2px(20))
        }

        if (bean.wishList.size > 1) {
            vf_social.startFlipping()
        } else {
            vf_social.stopFlipping()
        }

        if (mHomePageViewModel.mineHomePage) {
            mTagAdapter.mine = true
            mLikeTagAdapter.mine = true
            tv_tag_title.text = "我拥有的标签"
            tv_like_tag_title.text = "我喜欢的标签"
        } else {
            mTagAdapter.mine = false
            mLikeTagAdapter.mine = false
            tv_tag_title.text = "TA拥有的标签"
            tv_like_tag_title.text = "TA喜欢的标签"
        }


        val tagList = if (mHomePageViewModel.mineHomePage) {
            bean.myAuthTag.showTagList
        } else {
            bean.authTagList
        }
        tv_tag_count.text = "${tagList.size}"
        val realTagList = mutableListOf<UserTagBean>()
        if (tagList.size >= 4) {
            tv_more_tag.show()
            tagList.take(4).forEach {
                realTagList.add(it)
            }
        } else {
            tv_more_tag.hide()
            realTagList.addAll(tagList)
        }
        if (!mHomePageViewModel.mineHomePage) {
            if (realTagList.size < 4) {
                //添加引导布局
                realTagList.add(UserTagBean())
            }
        }

        mTagAdapter.setList(realTagList)

        val likeTagList = if (mHomePageViewModel.mineHomePage) {
            bean.myLikeTag.showTagList
        } else {
            bean.likeTagList
        }
        tv_like_tag_count.text = "${likeTagList.size}"
        val realLikeTagList = mutableListOf<UserTagBean>()
        if (likeTagList.size >= 4) {
            tv_more_like_tag.show()
            likeTagList.take(4).forEach {
                realLikeTagList.add(it)
            }
        } else {
            tv_more_like_tag.hide()
            realLikeTagList.addAll(likeTagList)
        }
        if (mHomePageViewModel.mineHomePage) {
            if (realLikeTagList.size < 4) {
                //添加引导布局
                realLikeTagList.add(UserTagBean())
            }
        }

        mLikeTagAdapter.setList(realLikeTagList)

        tv_user_id.text = "欢鹊ID ${bean.userId}"


    }
}