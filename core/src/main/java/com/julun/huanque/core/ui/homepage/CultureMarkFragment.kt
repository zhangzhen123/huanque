package com.julun.huanque.core.ui.homepage

import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.SingleCultureConfig
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.MarkCultureAdapter
import com.julun.huanque.core.viewmodel.HomeTownEditViewModel
import kotlinx.android.synthetic.main.frag_culture_mark.*

/**
 *@创建者   dong
 *@创建时间 2021/1/4 17:20
 *@描述 人文标记过的弹窗
 */
class CultureMarkFragment : BaseBottomSheetFragment() {
    private val mHomeTownEditViewModel: HomeTownEditViewModel by activityViewModels()
    private val mAdapter = MarkCultureAdapter()

    //全部数据
    private var mTotalList = mutableListOf<SingleCultureConfig>()
    override fun getLayoutId() = R.layout.frag_culture_mark

    override fun initViews() {
        initRecyclerView()
        tv_certain.onClickNew {
            //保存操作
            if (mHomeTownEditViewModel.markFragmentType == HomeTownEditViewModel.Food) {
                //美食
                val foodData = mHomeTownEditViewModel.foodCultureData.value
                foodData?.cultureConfigList = mTotalList
                mHomeTownEditViewModel.foodCultureData.value = foodData
            } else if (mHomeTownEditViewModel.markFragmentType == HomeTownEditViewModel.Place) {
                //景点
                val placeData = mHomeTownEditViewModel.placeCultureData.value
                placeData?.cultureConfigList = mTotalList
                mHomeTownEditViewModel.placeCultureData.value = placeData
            }
            dismiss()
        }
    }

    override fun getHeight() = dp2px(306)

    private fun initRecyclerView() {
        recycler_view.layoutManager = GridLayoutManager(requireContext(), 4)
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val tempData = mAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            tempData.mark = BusiConstant.False
            mAdapter.removeAt(position)
            updateTitle()
        }
    }

    override fun onStart() {
        super.onStart()

        initViewModel()
        if (mHomeTownEditViewModel.markFragmentType == HomeTownEditViewModel.Food) {
            mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(), "快去添加在家乡吃过的美食吧～", imgResId = R.mipmap.icon_edit_empty_food))
        } else if (mHomeTownEditViewModel.markFragmentType == HomeTownEditViewModel.Place) {
            mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(), "快去添加在家乡去过的景点吧～", imgResId = R.mipmap.icon_edit_empty_place))
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        if (mHomeTownEditViewModel.markFragmentType == HomeTownEditViewModel.Food) {
            //美食
            mHomeTownEditViewModel.foodCultureData.observe(this, Observer {
                if (it != null) {
                    mTotalList = mutableListOf()
                    it.cultureConfigList.forEach { oscc ->
                        val scc = SingleCultureConfig(oscc.logId, oscc.cityId, oscc.name, oscc.coverPic, oscc.mark)
                        mTotalList.add(scc)
                    }
                    showRecyclerView()
                    updateTitle()
                }
            })
        } else if (mHomeTownEditViewModel.markFragmentType == HomeTownEditViewModel.Place) {
            //景点
            mHomeTownEditViewModel.placeCultureData.observe(this, Observer {
                if (it != null) {
                    mTotalList = mutableListOf()
                    it.cultureConfigList.forEach { oscc ->
                        val scc = SingleCultureConfig(oscc.logId, oscc.cityId, oscc.name, oscc.coverPic, oscc.mark)
                        mTotalList.add(scc)
                    }
                    showRecyclerView()
                    updateTitle()
                }
            })
        }
    }

    /**
     * 显示RecyclerView数据
     */
    private fun showRecyclerView() {
        val markList = mutableListOf<SingleCultureConfig>()
        mTotalList.forEach {
            if (it.mark == BusiConstant.True) {
                markList.add(it)
            }
        }
        mAdapter.setList(markList)
    }

    /**
     * 更新标题数据
     */
    private fun updateTitle() {
        val markList = mutableListOf<SingleCultureConfig>()
        mTotalList.forEach {
            if (it.mark == BusiConstant.True) {
                markList.add(it)
            }
        }
        val title = if (mHomeTownEditViewModel.markFragmentType == HomeTownEditViewModel.Food) {
            //美食
            "吃过的美食 ${markList.size}"
        } else if (mHomeTownEditViewModel.markFragmentType == HomeTownEditViewModel.Place) {
            //景点
            "去过的景点 ${markList.size}"
        } else {
            ""
        }
        tv_title.text = title

        if (markList.size > 0) {
            tv_attention.show()
        } else {
            tv_attention.hide()
        }
    }

}