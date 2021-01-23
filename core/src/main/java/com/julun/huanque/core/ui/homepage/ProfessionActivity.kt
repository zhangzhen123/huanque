package com.julun.huanque.core.ui.homepage

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.contrarywind.interfaces.IPickerViewData
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.SaveProfessionForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.ProfessionFeatureFoodAdapter
import com.julun.huanque.core.adapter.ProfessionIncomeAdapter
import com.julun.huanque.core.utils.EditUtils
import com.julun.huanque.core.viewmodel.ProfessionViewModel
import kotlinx.android.synthetic.main.act_profession.*
import kotlinx.android.synthetic.main.act_profession.con_progress
import kotlinx.android.synthetic.main.act_profession.header_page
import kotlinx.android.synthetic.main.act_profession.progressBar
import kotlinx.android.synthetic.main.act_profession.tv_profression_title
import kotlinx.android.synthetic.main.act_profession.tv_save
import kotlinx.android.synthetic.main.act_profession.view_profess_feature
import kotlinx.android.synthetic.main.act_school.*
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2020/12/30 19:59
 *@描述 职业页面
 */
@Route(path = ARouterConstant.ProfessionActivity)
class ProfessionActivity : BaseActivity() {

    companion object {
        const val ProfessionInfoParams = "ProfessionInfo"

        /**
         * 跳转页面
         */
        fun newInstance(act: Activity, info: ProfessionInfo) {
            val intent = Intent(act, ProfessionActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                val bundle = Bundle()
                bundle.putSerializable(ProfessionInfoParams, info)
                intent.putExtras(bundle)
                act.startActivity(intent)
            }
        }
    }

    //收入
    private val mIncomeAdapter = ProfessionIncomeAdapter()
    private val mFeatureAdapter = ProfessionFeatureFoodAdapter()
    private val mViewModel: ProfessionViewModel by viewModels()

    private var pvOptions: OptionsPickerView<IPickerViewData>? = null

    //特性弹窗
    private val mProfessionMarkFragment: ProfessionMarkFragment by lazy { ProfessionMarkFragment() }

    //积极的列表
    private val upList = mutableListOf<SingleProfessionFeatureConfig>()

    //中性的列表
    private val middleList = mutableListOf<SingleProfessionFeatureConfig>()

    //消极的列表
    private val downList = mutableListOf<SingleProfessionFeatureConfig>()

    override fun getLayoutId() = R.layout.act_profession

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        var index = intent?.getIntExtra(ParamConstant.Index, -1) ?: -1

        if (index == -1) {
            con_progress.hide()
        } else {
            con_progress.show()
            header_page.textOperation.show()
//            if (index == 4) {
//                header_page.textOperation.text = "完成"
//            } else {
            header_page.textOperation.text = "跳过"
//            }
            progressBar.progress = (100 / 5) * (index + 1)
        }
        mViewModel.index = index
        mViewModel.originalProfession = intent?.getSerializableExtra(ProfessionInfoParams) as? ProfessionInfo
        header_page.textTitle.text = "职业"
        initViewModel()
        initRecyclerView()
        mViewModel.initProfession()
        showProfessionContent(mViewModel.originalProfession?.professionTypeText ?: "", mViewModel.originalProfession?.professionName ?: "")
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
        if (mViewModel.index >= 0) {
            header_page.textOperation.onClickNew {
                //跳过
                EditUtils.goToNext(this, mViewModel.index)
            }
        }

        tv_profression_title.onClickNew {
            if (pvOptions == null) {
                initOptionPicker()
            }
            pvOptions?.show()
        }


        iv_profess_feature.onClickNew {
            //职业特性换一批
            val placeData = mViewModel.featureData.value ?: return@onClickNew
            mViewModel.featureData.value = placeData
        }


        view_profess_feature.onClickNew {
            //景点
//            mHomeTownEditViewModel.markFragmentType = HomeTownEditViewModel.Place
            mProfessionMarkFragment.show(supportFragmentManager, "ProfessionMarkFragment")
        }

        tv_save.onClickNew {
            //保存
            val curPromissionId = mViewModel.professionData.value?.professionId ?: return@onClickNew
            if (curPromissionId == 0) {
                ToastUtils.show("请先选择职业")
                return@onClickNew
            }
            //
            val form = SaveProfessionForm()
            //判断收入是否有变动
            val curIncomeCode = mViewModel.professionData.value?.incomeCode ?: ""

            //判断职业特性是否有变动
            val curFeatureTypes = mutableListOf<String>()
            mViewModel.featureData.value?.forEach {
                if (it.mark == BusiConstant.True) {
                    curFeatureTypes.add(it.professionFeatureCode)
                }
            }
            curFeatureTypes.sort()

            val curTypesStr = StringBuilder()
            curFeatureTypes.forEach {
                if (curTypesStr.isNotEmpty()) {
                    curTypesStr.append(",")
                }
                curTypesStr.append(it)
            }
            val oriTypesStr = StringBuilder()
            mViewModel.oriFeatureTypes.forEach {
                if (oriTypesStr.isNotEmpty()) {
                    oriTypesStr.append(",")
                }
                oriTypesStr.append(it)
            }

            val oriPromissionId = mViewModel.originalProfession?.professionId


            if (curIncomeCode != mViewModel.oriIncomeCode || oriTypesStr.toString() != curTypesStr.toString() || curPromissionId != oriPromissionId) {
                //收入有变动
                form.income = curIncomeCode
                form.professionFeatureCodes = curTypesStr.toString()
                form.professionId = curPromissionId
                mViewModel.saveProfession(form)
            } else {
                EditUtils.goToNext(this, mViewModel.index)
            }
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel.professionData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                mIncomeAdapter.incomeCode = it.incomeCode
                mIncomeAdapter.setList(it.incomeConfigList)
                val markIdList = mutableListOf<String>()
                it.myFeatureList.forEach { spfc ->
                    markIdList.add(spfc.professionFeatureCode)
                }
                it.featureConfigList.forEach { spfc ->
                    val contains = markIdList.contains(spfc.professionFeatureCode)
                    spfc.mark = if (contains) BusiConstant.True else BusiConstant.False
                }
                mViewModel.featureData.value = it.featureConfigList
            }
        })
        mViewModel.featureData.observe(this, Observer {
            if (it != null) {
                showFeatureView(it)
            }
        })
        mViewModel.processData.observe(this, Observer {
            if (it != null) {
                EventBus.getDefault().post(it)
                EditUtils.goToNext(this, mViewModel.index)
            }
        })
    }

    /**
     * 显示职业和行业数据
     * @param contentH 行业名称
     * @param contentZ 职业名称
     */
    private fun showProfessionContent(contentH: String, contentZ: String) {
        mViewModel.nameH = contentH
        mViewModel.nameZ = contentZ
        if (contentH.isNotEmpty() && contentZ.isNotEmpty()) {
            tv_home_town.text = "$contentH/$contentZ"
        }
//        if (contentZ.isNotEmpty()) {
//            tv_home_town.text = contentZ
//        }
    }


    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_item_income.layoutManager = GridLayoutManager(this, 4)
        recycler_item_income.adapter = mIncomeAdapter
        mIncomeAdapter.setOnItemClickListener { adapter, view, position ->
            val tempIncome = adapter.getItem(position) as? SingleIncome ?: return@setOnItemClickListener
            if (mIncomeAdapter.incomeCode == tempIncome.incomeCode) {
                mIncomeAdapter.incomeCode = ""
                mViewModel.professionData.value?.incomeCode = ""
                mIncomeAdapter.notifyDataSetChanged()
                return@setOnItemClickListener
            }
            mIncomeAdapter.incomeCode = tempIncome.incomeCode
            mViewModel.professionData.value?.incomeCode = tempIncome.incomeCode
            mIncomeAdapter.notifyDataSetChanged()
        }

        recycler_item_profess_feature.layoutManager = GridLayoutManager(this, 4)
        recycler_item_profess_feature.adapter = mFeatureAdapter
        mFeatureAdapter.setEmptyView(MixedHelper.getEmptyView(this, "太棒了！家乡的景点你都去过了", true))
        mFeatureAdapter.setOnItemClickListener { adapter, view, position ->
            if (mViewModel.curFeatureCount >= mViewModel.maxFeatureCount) {
                ToastUtils.show("最多只能选择${mViewModel.maxFeatureCount}个职业特性")
                return@setOnItemClickListener
            }
            val data = mFeatureAdapter.data
            val tempData = mFeatureAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            tempData.mark = BusiConstant.True

            val totalList = when (tempData.professionFeatureType) {
                SingleProfessionFeatureConfig.Positive -> {
                    //积极
                    upList
                }
                SingleProfessionFeatureConfig.Middle -> {
                    //中性
                    middleList
                }
                else -> {
                    //消极
                    downList
                }
            }
            val replaceData = getReplaceData(totalList, data)
            if (replaceData == null) {
                //没有可以替换的数据
                mFeatureAdapter.removeAt(position)
            } else {
                data[position] = replaceData
                mFeatureAdapter.notifyItemChanged(position)
            }
            showTotalFeatureNum()
        }
    }

    /**
     * 获取插入的数据
     */
    private fun getReplaceData(
        wholeList: MutableList<SingleProfessionFeatureConfig>,
        showList: MutableList<SingleProfessionFeatureConfig>
    ): SingleProfessionFeatureConfig? {

        val canChooseList = mutableListOf<SingleProfessionFeatureConfig>()
        wholeList.forEach {
            if (it !in showList && it.mark != BusiConstant.True) {
                canChooseList.add(it)
            }
        }
        return if (canChooseList.size == 0) {
            null
        } else {
            canChooseList.random()
        }

    }


    /**
     * 显示数量
     */
    private fun showTotalFeatureNum() {
        val totalList = mViewModel.featureData.value ?: return
        showChange(mFeatureAdapter, iv_profess_feature)

        var markCount = 0
        totalList.forEach {
            if (it.mark == BusiConstant.True) {
                markCount++
            }
        }
        mViewModel.curFeatureCount = markCount
//        if (markCount == 0) {
//            tv_profess_feature_num.text = ""
//        } else {
        tv_profess_feature_num.text = "$markCount"
//        }
    }


    /**
     * 显示职业特点数据
     * @param featureList 职业特点列表
     */
    private fun showFeatureView(featureList: MutableList<SingleProfessionFeatureConfig>) {
        showTotalFeatureNum()
        upList.clear()
        middleList.clear()
        downList.clear()
        featureList.forEach {
            when (it.professionFeatureType) {
                SingleProfessionFeatureConfig.Positive -> {
                    //积极
                    upList.add(it)
                }
                SingleProfessionFeatureConfig.Middle -> {
                    //中性
                    middleList.add(it)
                }
                SingleProfessionFeatureConfig.Negative -> {
                    //消极
                    downList.add(it)
                }

            }
        }

        val showList = mutableListOf<SingleProfessionFeatureConfig>()
        //添加正向
//        if (upList.size <= 4) {
//            showList.addAll(upList)
//        } else {
        //获取4个正向
        upList.asSequence().filter { it.mark != BusiConstant.True }.take(4).forEach {
            showList.add(it)
        }
//        }
        //添加中性
//        if (middleList.size <= 2) {
//            showList.addAll(middleList)
//        } else {
        //获取两个中性
        middleList.asSequence().filter { it.mark != BusiConstant.True }.take(2).forEach {
            showList.add(it)
        }
//        }
        //添加负性
//        if (downList.size <= 2) {
//            showList.addAll(downList)
//        } else {
        //获取两个负向
        downList.asSequence().filter { it.mark != BusiConstant.True }.take(2).forEach {
            showList.add(it)
        }
//        }
        //随机打乱
        val realList = mutableListOf<SingleProfessionFeatureConfig>()
        val count = showList.size
        while (realList.size != count) {
            val tempData = showList.random()
            if (tempData !in realList) {
                realList.add(tempData)
            }
        }
        mFeatureAdapter.setList(realList)
        showChange(mFeatureAdapter, iv_profess_feature)
    }

    /**
     * 判断 换一批功能是否显示
     */
    private fun showChange(adapter: ProfessionFeatureFoodAdapter, iv: ImageView) {
        if (adapter.itemCount >= 8) {
            iv.show()
        } else {
            iv.hide()
        }
    }

    /**
     * 初始化操作按钮
     */
    private fun initOptionPicker() { //Dialog 模式下，在底部弹出
        //职业大类别列表
        val perfessionConfigList = mViewModel.professionData.value?.professionConfigList ?: return
        val firstList = mutableListOf<IPickerViewData>()
        val secondList = mutableListOf<MutableList<IPickerViewData>>()
        perfessionConfigList.forEach {
            firstList.add(it)
            val tempList = mutableListOf<IPickerViewData>()
            it.professionList.forEach { sp ->
                tempList.add(sp)
            }
            secondList.add(tempList)
        }


        /**
         * @description
         *
         * 注意事项：
         * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
         * 具体可参考demo 里面的两个自定义layout布局。
         * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
         */

        pvOptions = OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, v ->
            //返回的分别是三个级别的选中位置
            val firstBean = firstList.getOrNull(options1) as? SingleProfessionConfig ?: return@OnOptionsSelectListener
            val tempProfession = secondList.getOrNull(options1)?.getOrNull(options2) as? SingleProfession ?: return@OnOptionsSelectListener

            //显示职业文案
            showProfessionContent(firstBean.professionTypeText, tempProfession.professionName)
            mViewModel.professionData.value?.professionId = tempProfession.professionId
        })
            .setTitleText("城市选择")
            .setContentTextSize(20)//设置滚轮文字大小
            .setDividerColor(Color.LTGRAY)//设置分割线的颜色
            .setBgColor(Color.WHITE)
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(GlobalUtils.getColor(R.color.black_333))
            .setTextColorOut(GlobalUtils.getColor(R.color.black_999))
            .setLayoutRes(R.layout.view_edit_profession, CustomListener { view ->
                val tvSubmit = view.findViewById<View>(R.id.tv_certain)
                tvSubmit.setOnClickListener {
                    pvOptions?.returnData()
                    pvOptions?.dismiss()
                }
            })
            .setItemVisibleCount(7) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(2.0f)
            .isAlphaGradient(true)
            .setDividerColor(0x00000000)
            .isRestoreItem(true)//切换时是否还原，设置默认选中第一项。
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setOutSideColor(0x33000000) //设置外部遮罩颜色
            .build()
        pvOptions?.setPicker(firstList, secondList) //二级选择器
        setDefault()

        val mDialog: Dialog = pvOptions?.dialog ?: return
        val params = FrameLayout.LayoutParams(
            ScreenUtils.getScreenWidth(), dp2px(320f), Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        pvOptions?.dialogContainerLayout?.layoutParams = params
        val dialogWindow = mDialog.window ?: return
        dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
        dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
        dialogWindow.setDimAmount(0.3f)

    }

    private fun setDefault() {
        //职业数据
        val professionData = mViewModel.professionData.value ?: return
        //职业大类别列表
        val perfessionConfigList = mViewModel.professionData.value?.professionConfigList ?: return
        if (professionData.professionId == 0) {
            pvOptions?.setSelectOptions(0, 0)//默认选中项
        } else {
            //
            var firstPosition = 0
            var secondPosition = 0
            perfessionConfigList.forEachIndexed { index, singleProfessionConfig ->
                singleProfessionConfig.professionList.forEachIndexed { innerIndex, singleProfession ->
                    if (singleProfession.professionId == professionData.professionId) {
                        //找到对应的职业
                        firstPosition = index
                        secondPosition = innerIndex
                        pvOptions?.setSelectOptions(firstPosition, secondPosition)//默认选中项
                        return
                    }
                }
            }
            pvOptions?.setSelectOptions(0, 0)//默认选中项
        }
    }


}