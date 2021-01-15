package com.julun.huanque.core.ui.homepage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomeTownCircumListAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import com.julun.huanque.core.viewmodel.HomeTownViewModel
import com.trello.rxlifecycle4.android.lifecycle.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.frag_home_towm_circum.*
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/12/28 20:35
 *@描述 家乡周边Fragment
 */
class HomeTownCircumFragment : BaseFragment() {
    companion object {
        fun newInstance(type: String): HomeTownCircumFragment {
            val fragment = HomeTownCircumFragment()
            fragment.arguments = Bundle().apply { putString(ParamConstant.TYPE, type) }
            return fragment
        }
    }

    //家乡ViewModel
    private val mHomeTownViewModel: HomeTownViewModel by activityViewModels()
    private val mHomePageViewModel: HomePageViewModel by activityViewModels()
    private var mType = ""
    private val mAdapter = HomeTownCircumListAdapter()
    override fun getLayoutId() = R.layout.frag_home_towm_circum

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mType = arguments?.getString(ParamConstant.TYPE, "") ?: ""
        mAdapter.markContent = when (mType) {
            "Food" -> {
                if (mHomePageViewModel.mineHomePage) {
                    "吃过"
                } else {
                    "TA吃过"
                }

            }
            "Place" -> {
                if (mHomePageViewModel.mineHomePage) {
                    "去过"
                } else {
                    "TA去过"
                }

            }
            else -> {
                ""
            }
        }
        initRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        initViewModel()
    }


    private fun initRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler_view.adapter = mAdapter
        recycler_view.isNestedScrollingEnabled = false
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomeTownViewModel.homeTownInfo.observe(this, Observer {
            logger.info("Culture 显示数据")
            it?.cultureList?.forEach { sc ->
                if (sc.cultureType == mType) {
                    mAdapter.setList(sc.cultureConfigList)
                    return@Observer
                }
            }
        })
    }


}