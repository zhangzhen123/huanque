package com.julun.huanque.core.ui.homepage

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.bean.beans.HomePageInfo
import com.julun.huanque.common.bean.beans.HomeTagBean
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomePageTagAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.act_home_page.*
import kotlinx.android.synthetic.main.frag_home_page_information.*
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

    //标签adapter
    private val mTagAdapter = HomePageTagAdapter()

    //我喜欢的标签adapter
    private val mLikeTagAdapter = HomePageTagAdapter()

    override fun getLayoutId() = R.layout.frag_home_page_information

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        initRecyclerView()

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)

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
        if (bean.homeTownProvince.isNotEmpty()) {
            homeTownStr.append(bean.homeTownProvince)
        }
        if (homeTownStr.isNotEmpty()) {
            homeTownStr.append("/")
        }
        homeTownStr.append(bean.homeTownCity)
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
        val weight = bean.weight
        val height = bean.height
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
        if (bean.constellation.isEmpty()) {
            tv_constellation.text = "-"
        } else {
            tv_constellation.text = bean.constellation
        }
        //职业
        if (bean.professionName.isEmpty()) {
            tv_job.text = "-"
        } else {
            tv_job.text = bean.professionName
        }
        //毕业院校
        if (bean.school.isEmpty()) {
            tv_school.text = "-"
        } else {
            tv_school.text = bean.school
        }
        //社交意愿
        tv_social

        val tagList = bean.authTagList
        tv_tag_count.text = "${tagList.size}"
        val realTagList = mutableListOf<HomeTagBean>()
        if (tagList.size > 4) {
            tagList.take(4).forEach {
                realTagList.add(it)
            }
        } else {
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
        if (likeTagList.size > 4) {
            likeTagList.take(4).forEach {
                realLikeTagList.add(it)
            }
        } else {
            realLikeTagList.addAll(likeTagList)
        }
        if (realLikeTagList.size < 4) {
            //添加引导布局
            realLikeTagList.add(HomeTagBean())
        }
        mLikeTagAdapter.setList(realLikeTagList)

    }
}