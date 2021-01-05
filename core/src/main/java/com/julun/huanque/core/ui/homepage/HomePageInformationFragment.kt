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
import com.julun.huanque.common.bean.beans.HomeTagBean
import com.julun.huanque.common.bean.beans.SocialWishBean
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomePageTagAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.act_home_page.*
import kotlinx.android.synthetic.main.frag_home_page_information.*
import kotlinx.android.synthetic.main.fragment_program_container.*
import org.jetbrains.anko.textColor
import java.lang.StringBuilder
import java.time.format.TextStyle

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

    //标签adapter
    private val mTagAdapter = HomePageTagAdapter()

    //我喜欢的标签adapter
    private val mLikeTagAdapter = HomePageTagAdapter()

    //拥有的标签
    private val mTagFragment: TagFragment by lazy { TagFragment() }

    //身材弹窗
    private val mFigureFragment: FigureFragment by lazy { FigureFragment() }

    //星座
    private val mConstellationFragment: ConstellationFragment by lazy { ConstellationFragment() }

    //社交意愿
    private val mSocialWishFragment: SocialWishFragment by lazy { SocialWishFragment() }

    //职业
    private val mJobFragment: JobFragment by lazy { JobFragment() }

    //学校数据
    private val mSchoolFragment: SchoolFragment by lazy { SchoolFragment() }

    //喜欢的标签
    private val mLikeTagFragment: TagFragment by lazy { TagFragment.newInstance(true) }

    override fun getLayoutId() = R.layout.frag_home_page_information

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        initRecyclerView()

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        ll_tag.onClickNew {
            //他拥有的标签
            if (tv_more_tag.visibility == View.VISIBLE) {
                //显示他拥有的标签弹窗
                mTagFragment.show(childFragmentManager, "TagFragment")
            }
        }
//        rl_tag.onClickNew {
//            ll_tag.performClick()
//        }

        ll_like_tag.onClickNew {
            if (tv_more_like_tag.visibility == View.VISIBLE) {
                //显示他拥有的标签弹窗
                mLikeTagFragment.show(childFragmentManager, "TagFragment")
            }
        }

        view_home_town.onClickNew {
            //家乡数据
            HomeTownFragment().show(childFragmentManager, "HomeTownFragment")
        }

        view_stature.onClickNew {
            //身材
            mFigureFragment.show(childFragmentManager, "FigureFragment")
        }

        view_constellation.onClickNew {
            //星座
            mConstellationFragment.show(childFragmentManager, "FigureFragment")
        }
        view_job.onClickNew {
            //职业
            mJobFragment.show(childFragmentManager, "JobFragment")
        }

        view_school.onClickNew {
            //学校
            mSchoolFragment.show(childFragmentManager, "SchoolFragment")
        }

        view_social.onClickNew {
            //社交意愿
            mSocialWishFragment.show(childFragmentManager, "SocialWishFragment")
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
        }

        recycler_view_like_tag.layoutManager = GridLayoutManager(context, 4)
        recycler_view_like_tag.adapter = mLikeTagAdapter

    }


    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomePageViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }
        })
    }

    private fun showViewByData(bean: HomePageInfo) {
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
        val constellationName = bean.constellationInfo.constellationName
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

        val tagList = bean.authTagList
        tv_tag_count.text = "${tagList.size}"
        val realTagList = mutableListOf<HomeTagBean>()
        if (tagList.size >= 4) {
            tv_more_tag.show()
            tagList.take(4).forEach {
                realTagList.add(it)
            }
        } else {
            tv_more_tag.hide()
            realTagList.addAll(tagList)
        }
        if (realTagList.size < 4) {
            //添加引导布局
            realTagList.add(HomeTagBean())
        }
        mTagAdapter.setList(realTagList)

        val likeTagList = bean.likeTagList
        tv_like_tag_count.text = "${likeTagList.size}"
        val realLikeTagList = mutableListOf<HomeTagBean>()
        if (likeTagList.size >= 4) {
            tv_more_like_tag.show()
            likeTagList.take(4).forEach {
                realLikeTagList.add(it)
            }
        } else {
            tv_more_like_tag.hide()
            realLikeTagList.addAll(likeTagList)
        }
        if (realLikeTagList.size < 4) {
            //添加引导布局
            realLikeTagList.add(HomeTagBean())
        }
        mLikeTagAdapter.setList(realLikeTagList)

        tv_user_id.text = "欢鹊ID ${bean.userId}"
    }
}