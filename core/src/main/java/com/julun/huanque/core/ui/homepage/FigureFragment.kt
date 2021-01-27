package com.julun.huanque.core.ui.homepage

import android.graphics.Color
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.bean.beans.FigureBean
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.TagListAdapter
import com.julun.huanque.core.viewmodel.HomePageViewModel
import kotlinx.android.synthetic.main.frag_figure.*
import kotlinx.android.synthetic.main.frag_tag.*
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2020/12/28 9:30
 *@描述 身材弹窗
 */
class FigureFragment : BaseBottomSheetFragment() {

    private val mHomeViewModel: HomePageViewModel by activityViewModels()


    override fun getLayoutId() = R.layout.frag_figure


    override fun initViews() {
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


    override fun getHeight() = dp2px(453)

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomeViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it.figure)
                val female = it.sex == Sex.FEMALE
                tv_height_title.isSelected = female
                tv_weight_title.isSelected = female
                ll_figure.isSelected = female
            }
        })
    }

    /**
     * 显示身材数据
     */
    private fun showViewByData(data: FigureBean) {
        sdv_figure.loadImage(data.figurePic, 90f, 275f)
        tv_height.text = "${data.height}cm"
        tv_weight.text = "${data.weight}kg"
        val suggest = StringBuilder()
        data.suggest.forEach {
            if (suggest.isNotEmpty()) {
                suggest.append("\n")
            }
            suggest.append(it)
        }
        tv_suggest.text = suggest.toString()
        tv_figure.text = data.figure
    }


}