package com.julun.huanque.core.ui.homepage

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.SingleProfessionFeatureConfig
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.MarkProfessionFeatureAdapter
import com.julun.huanque.core.viewmodel.ProfessionViewModel
import kotlinx.android.synthetic.main.frag_culture_mark.*

/**
 *@创建者   dong
 *@创建时间 2021/1/4 17:20
 *@描述 职业特点标记过的弹窗
 */
class ProfessionMarkFragment : BaseBottomSheetFragment() {
    private val mProfessionViewModel: ProfessionViewModel by activityViewModels()
    private val mAdapter = MarkProfessionFeatureAdapter()

    //全部数据
    private var mTotalList = mutableListOf<SingleProfessionFeatureConfig>()
    override fun getLayoutId() = R.layout.frag_feature_mark

    override fun initViews() {
        initRecyclerView()
        tv_certain.onClickNew {
            //保存操作
            mProfessionViewModel.featureData.value = mTotalList
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
        mAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext(), "快去添加职业特性吧～", imgResId = R.mipmap.icon_edit_empty_profession))
    }

    override fun onStart() {
        super.onStart()

        initViewModel()
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        //职业特点数据
        mProfessionViewModel.featureData.observe(this, Observer {
            if (it != null) {
                mTotalList = mutableListOf()
                it.forEach { oscc ->
                    val scc = SingleProfessionFeatureConfig(oscc.professionFeatureCode, oscc.professionFeatureText,oscc.professionFeatureType, oscc.mark)
                    mTotalList.add(scc)
                }
                showRecyclerView()
                updateTitle()
            }
        })
    }

    /**
     * 显示RecyclerView数据
     */
    private fun showRecyclerView() {
        val markList = mutableListOf<SingleProfessionFeatureConfig>()
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
        val markList = mutableListOf<SingleProfessionFeatureConfig>()
        mTotalList.forEach {
            if (it.mark == BusiConstant.True) {
                markList.add(it)
            }
        }
        val title = "职业特性 ${markList.size}"
        tv_title.text = title

        if (markList.size > 0) {
            tv_attention.show()
        } else {
            tv_attention.hide()
        }
    }

}