package com.julun.huanque.core.ui.homepage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.FigureBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.FigureViewModel
import kotlinx.android.synthetic.main.act_figure.*
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal
import java.util.ArrayList
import kotlin.math.max

/**
 *@创建者   dong
 *@创建时间 2021/1/8 10:09
 *@描述 身材
 */
class FigureActivity : BaseActivity() {

    companion object {
        const val Tag = "FigureActivity"
        const val Figure = "Figure"
        fun newInstance(act: Activity, figureBean: FigureBean, index: Int = -1, tagList: ArrayList<String>? = null) {
            val intent = Intent(act, FigureActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                val bundle = Bundle()
                bundle.putSerializable(Figure, figureBean)
                bundle.putInt(ParamConstant.Index, index)
                if (tagList != null) {
                    bundle.putStringArrayList(ParamConstant.Tag_List, tagList)
                }
                intent.putExtras(bundle)
                act.startActivity(intent)
            }
        }
    }

    private val mViewModel: FigureViewModel by viewModels()
    override fun getLayoutId() = R.layout.act_figure

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.textTitle.text = "身材"
        initViewModel()
        mViewModel.initFigure()
        view_height.setupValue = 1
        view_weight.setupValue = 1


    }

    override fun initEvents(rootView: View) {
        header_page.imageViewBack.onClickNew {
            finish()
        }
        tv_save.onClickNew {
            val currentHeight = mViewModel.currentHeight.value ?: return@onClickNew
            val currentWeight = mViewModel.currentWeight.value ?: return@onClickNew
            if (mViewModel.originHeight != currentHeight || mViewModel.originWeight != currentWeight) {
                //数据有变化
                mViewModel.updateFigure(currentHeight, currentWeight)
            } else {
                finish()
            }
        }

        view_height.setOnItemChangedListener { index, value ->
            mViewModel.currentHeight.postValue(value)
        }

        view_weight.setOnItemChangedListener { index, value ->
            mViewModel.currentWeight.postValue(value)
        }

        val figureBean = intent?.getSerializableExtra(Figure) as? FigureBean
        val oHeight = figureBean?.height ?: 0
        val oWeight = figureBean?.weight ?: 0
        mViewModel.originHeight = oHeight
        mViewModel.originWeight = oWeight
        val realHeight = if (oHeight != 0) oHeight else getDefaultHeight()


        val realWeight = if (oWeight != 0) oWeight else getDefaultWeight()

        view_height.post {
            view_height.currentLineIndex = realHeight - view_height.startLineValue
            view_weight.currentLineIndex = realWeight - view_weight.startLineValue
        }
    }

    /**
     * 获取默认高度
     */
    private fun getDefaultHeight() = if (SessionUtils.getSex() == Sex.MALE) 170 else 165

    /**
     * 获取默认体重
     */
    private fun getDefaultWeight() = if (SessionUtils.getSex() == Sex.MALE) 60 else 50


    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.figureData.observe(this, Observer {
            if (it != null) {
                sdv_figure.loadImage(it.myFigurePic)
                tv_figure.text = it.figure
            }
        })

        mViewModel.currentHeight.observe(this, Observer {
            if (it != null) {
                tv_height.text = "${it}cm"
                getBmiLevelType()
            }
        })
        mViewModel.currentWeight.observe(this, Observer {
            if (it != null) {
                tv_weight.text = "${it}kg"
                getBmiLevelType()
            }
        })
        mViewModel.currentFigureConfig.observe(this, Observer {
            if (it != null) {
                sdv_figure.loadImage(it.figurePic)
                tv_figure.text = it.figure
            }
        })
        mViewModel.processData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                EventBus.getDefault().post(it)
                finish()
            }
        })
    }


    /**
     * 换算体质
     */
    fun getBmiLevelType() {
        val originalHeight = mViewModel.currentHeight.value ?: return
        val originalWeight = mViewModel.currentWeight.value ?: return
        var weight = originalWeight * 1.0
        var height = originalHeight / 100.0
        val tmp: BigDecimal = BigDecimal(height).setScale(2, BigDecimal.ROUND_HALF_UP)
        height = tmp.toDouble()
        // 体质 = 体重(kg) / (身高m * 身高m)
        val bmi = weight / (height * height)
        val bmiValue = bmi.toInt()

        mViewModel.figureList.forEach {
            if (bmiValue > it.min && bmiValue <= it.max) {
                mViewModel.currentFigureConfig.value = it
                return
            }
        }
    }


    private fun getNextClass(tag: String): Class<out BaseActivity>? {
        return when (tag) {
            HomeTownActivity.Tag -> {
                HomeTownActivity::class.java
            }
            FigureActivity.Tag -> {
                FigureActivity::class.java
            }
            UpdateBirthdayActivity.Tag -> {
                UpdateBirthdayActivity::class.java
            }
            SchoolActivity.Tag->{
                SchoolActivity::class.java
            }
            ProfessionActivity.Tag->{
                ProfessionActivity::class.java
            }
            else->{
                return null
            }
        }
    }

}