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
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.CustomListener
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.view.OptionsPickerView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.UpdateUserInfoForm
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomeTownFoodAdapter
import com.julun.huanque.core.viewmodel.HomeTownEditViewModel
import kotlinx.android.synthetic.main.act_home_town.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

/**
 *@创建者   dong
 *@创建时间 2020/12/30 19:59
 *@描述 家乡数据
 */
class HomeTownActivity : BaseActivity() {

    companion object {
        const val Tag = "HomeTownActivity"

        /**
         * 跳转页面
         */
        fun newInstance(act: Activity, homeTownName: String, index: Int = -1, tagList: ArrayList<String>? = null) {
            val intent = Intent(act, HomeTownActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                val bundle = Bundle()
                bundle.putSerializable(ParamConstant.Home_Town_Name, homeTownName)
                bundle.putInt(ParamConstant.Index, index)
                if (tagList != null) {
                    bundle.putStringArrayList(ParamConstant.Tag_List, tagList)
                }
                intent.putExtras(bundle)
                act.startActivity(intent)
            }
        }
    }

    //省份数据
    private val provinceItems = mutableListOf<EditCityBean>()

    //城市数据
    private val cityItems = mutableListOf<MutableList<EditCityBean>>()

    //美食Adapter
    private val mFoodAdapter = HomeTownFoodAdapter()
    private val mPlaceAdapter = HomeTownFoodAdapter()
    private val mHomeTownEditViewModel: HomeTownEditViewModel by viewModels()

    private var pvOptions: OptionsPickerView<EditCityBean>? = null

    //人文弹窗
    private val mCultureMarkFragment: CultureMarkFragment by lazy { CultureMarkFragment() }

    override fun getLayoutId() = R.layout.act_home_town

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        var index = intent?.getIntExtra(ParamConstant.Index, -1) ?: -1

        mHomeTownEditViewModel.index = index
        intent?.getStringArrayListExtra(ParamConstant.Tag_List)?.let {
            mHomeTownEditViewModel.tagList.clear()
            mHomeTownEditViewModel.tagList.addAll(it)
        }

        if (index == -1) {
            con_progress.hide()
        } else {
            con_progress.show()
            header_page.textOperation.show()
            header_page.textOperation.text = "跳过"
            progressBar.progress = (100 / 5) * (index + 1)
        }

        val cityName = intent?.getStringExtra(ParamConstant.Home_Town_Name) ?: ""
        tv_profression.text = cityName

        header_page.textTitle.text = "家乡"
        initViewModel()
        initRecyclerView()
        mHomeTownEditViewModel.queryHomeTownInfo()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }

        if (mHomeTownEditViewModel.index >= 0) {
            header_page.textOperation.onClickNew {
                //跳过
                goToNext()
            }
        }

        tv_profression_title.onClickNew {
            if (pvOptions == null) {
                initOptionPicker()
            }
            pvOptions?.show()
        }

        iv_food_change.onClickNew {
            //美食换一批
            val foodData = mHomeTownEditViewModel.foodCultureData.value ?: return@onClickNew
            mHomeTownEditViewModel.foodCultureData.value = foodData
        }

        iv_view_change.onClickNew {
            //景点换一批
            val placeData = mHomeTownEditViewModel.placeCultureData.value ?: return@onClickNew
            mHomeTownEditViewModel.placeCultureData.value = placeData
        }

        view_income.onClickNew {
            //美食
            mHomeTownEditViewModel.markFragmentType = HomeTownEditViewModel.Food
            mCultureMarkFragment.show(supportFragmentManager, "CultureMarkFragment")
        }

        view_profess_feature.onClickNew {
            //景点
            mHomeTownEditViewModel.markFragmentType = HomeTownEditViewModel.Place
            mCultureMarkFragment.show(supportFragmentManager, "CultureMarkFragment")
        }

        tv_save.onClickNew {
            //保存
            //判断数据是否变动过
            val currentCityId = mHomeTownEditViewModel.homeTownData.value?.homeTownId ?: return@onClickNew
            val oldCityId = mHomeTownEditViewModel.oldHownTownId

            val currentFoodIds = mutableListOf<Long>()
            mHomeTownEditViewModel.foodCultureData.value?.cultureConfigList?.forEach { scc ->
                if (scc.mark == BusiConstant.True) {
                    currentFoodIds.add(scc.logId)
                }
            }
            mHomeTownEditViewModel.placeCultureData.value?.cultureConfigList?.forEach { scc ->
                if (scc.mark == BusiConstant.True) {
                    currentFoodIds.add(scc.logId)
                }
            }
            currentFoodIds.sort()
            val oldIds = StringBuilder()
            mHomeTownEditViewModel.oldCultureIds.forEach { oId ->
                if (oldIds.isNotEmpty()) {
                    oldIds.append(",")
                }
                oldIds.append("$oId")
            }
            val curIds = java.lang.StringBuilder()
            currentFoodIds.forEach { cId ->
                if (curIds.isNotEmpty()) {
                    curIds.append(",")
                }
                curIds.append("$cId")
            }
            if (currentCityId != oldCityId || oldIds != curIds) {
                //数据发生变化，需要调用接口
                mHomeTownEditViewModel.saveHomeTown(currentCityId, curIds.toString())
            } else {
                //数据没有发生变化，直接返回
                goToNext()
            }

        }
    }

    /**
     * 跳转下一级页面
     */
    private fun goToNext() {
        if (mHomeTownEditViewModel.index >= 4) {
            //已经是最后一个
            finish()
        } else {
            //需要继续跳转
            val tag = mHomeTownEditViewModel.tagList.getOrNull(mHomeTownEditViewModel.index + 1)
            val targetClass = getNextClass(tag)
            if (targetClass != null) {
                //跳转下一次页面
                val intent = Intent(this, targetClass)
                if (ForceUtils.activityMatch(intent)) {
                    val bundle = Bundle()
                    bundle.putInt(ParamConstant.Index, mHomeTownEditViewModel.index + 1)
                    bundle.putStringArrayList(ParamConstant.Tag_List, mHomeTownEditViewModel.tagList)
                    startActivity(intent)
                }
            }
            finish()
        }
    }


    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomeTownEditViewModel.homeTownData.observe(this, Observer {
            if (it != null) {
                showCityName(it.homeTownProvince, it.homeTownCity)
            }
        })
        mHomeTownEditViewModel.foodCultureData.observe(this, Observer {
            if (it != null) {
                showFoodView(it)
            }
        })
        mHomeTownEditViewModel.placeCultureData.observe(this, Observer {
            if (it != null) {
                showPlaceView(it)
            }
        })
        mHomeTownEditViewModel.processData.observe(this, Observer {
            if (it != null) {
                //数据更新成功
                if (mHomeTownEditViewModel.oldHownTownId != mHomeTownEditViewModel.homeTownData.value?.homeTownId) {
                    //城市名称发生变化，更新数据
                    EventBus.getDefault()
                        .post(UpdateUserInfoForm(provinceName = mHomeTownEditViewModel.provinceName, cityName = mHomeTownEditViewModel.cityName))
                }
                EventBus.getDefault().post(it)
                goToNext()
            }
        })
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_item_food.layoutManager = GridLayoutManager(this, 4)
        recycler_item_food.adapter = mFoodAdapter
        mFoodAdapter.setEmptyView(MixedHelper.getEmptyView(this, "太棒了！家乡的美食你都吃过了", true))
        mFoodAdapter.setOnItemChildClickListener { adapter, view, position ->
            val data = mFoodAdapter.data
            val tempData = mFoodAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            tempData.mark = BusiConstant.True
            val totalList = mHomeTownEditViewModel.foodCultureData.value?.cultureConfigList ?: return@setOnItemChildClickListener
            val replaceData = getReplaceData(totalList, data)
            if (replaceData == null) {
                //没有可以替换的数据
                mFoodAdapter.removeAt(position)
            } else {
                data[position] = replaceData
                mFoodAdapter.notifyItemChanged(position)
            }
            showTotalFoodNum(totalList, tv_eat_food_num)
        }

        recycler_item_view.layoutManager = GridLayoutManager(this, 4)
        recycler_item_view.adapter = mPlaceAdapter
        mPlaceAdapter.setEmptyView(MixedHelper.getEmptyView(this, "太棒了！家乡的景点你都去过了", true))
        mPlaceAdapter.setOnItemChildClickListener { adapter, view, position ->
            val data = mPlaceAdapter.data
            val tempData = mPlaceAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            tempData.mark = BusiConstant.True
            val totalList = mHomeTownEditViewModel.placeCultureData.value?.cultureConfigList ?: return@setOnItemChildClickListener
            val replaceData = getReplaceData(totalList, data)
            if (replaceData == null) {
                //没有可以替换的数据
                mPlaceAdapter.removeAt(position)
            } else {
                data[position] = replaceData
                mPlaceAdapter.notifyItemChanged(position)
            }
            showTotalFoodNum(totalList, tv_view_num)
        }
    }

    /**
     * 获取插入的数据
     */
    private fun getReplaceData(wholeList: MutableList<SingleCultureConfig>, showList: MutableList<SingleCultureConfig>): SingleCultureConfig? {
        if (wholeList.size <= showList.size) {
            return null
        }

        val canChooseList = mutableListOf<SingleCultureConfig>()
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
     * 显示数据
     */
    private fun showCityName(provinceName: String, cityName: String) {
        val homeTownStr = StringBuilder()
        if (provinceName.isNotEmpty()) {
            homeTownStr.append(provinceName)
        }
        if (homeTownStr.isNotEmpty()) {
            homeTownStr.append("/")
        }
        homeTownStr.append(cityName)
        mHomeTownEditViewModel.provinceName = provinceName
        mHomeTownEditViewModel.cityName = cityName
        tv_profression.text = homeTownStr.toString()
    }

    /**
     * 显示美食布局
     */
    private fun showFoodView(foodBean: SingleCulture) {
        tv_income.text = foodBean.cultureTypeText
        showTotalFoodNum(foodBean.cultureConfigList, tv_eat_food_num)
        val foodNoMarkList = mutableListOf<SingleCultureConfig>()

        foodBean.cultureConfigList.filter { it.mark != BusiConstant.True }
            .forEach {
                foodNoMarkList.add(it)
            }


        val noMarkList = mutableListOf<SingleCultureConfig>()
        if (foodNoMarkList.size <= 8) {
            noMarkList.addAll(foodNoMarkList)
        } else {
            while (noMarkList.size < 8) {
                val singleCulture = foodNoMarkList.random()
                if (singleCulture !in noMarkList) {
                    noMarkList.add(singleCulture)
                }
            }
        }

        mFoodAdapter.setList(noMarkList)
        showChange(mFoodAdapter, iv_food_change)
    }

    /**
     * 显示数量
     */
    private fun showTotalFoodNum(list: List<SingleCultureConfig>, tv: TextView) {
        if (tv == tv_eat_food_num) {
            //美食
            showChange(mFoodAdapter, iv_food_change)
        } else {
            showChange(mPlaceAdapter, iv_view_change)
        }

        var markCount = 0
        list.forEach {
            if (it.mark == BusiConstant.True) {
                markCount++
            }
        }
        if (markCount == 0) {
            tv.text = ""
        } else {
            tv.text = "$markCount"
        }
    }


    /**
     * 显示景点数据
     */
    private fun showPlaceView(placeBean: SingleCulture) {
        tv_view_watch.text = placeBean.cultureTypeText
        showTotalFoodNum(placeBean.cultureConfigList, tv_view_num)

        val placeNoMarkList = mutableListOf<SingleCultureConfig>()

        placeBean.cultureConfigList.filter { it.mark != BusiConstant.True }
            .forEach {
                placeNoMarkList.add(it)
            }
        val noMarkList = mutableListOf<SingleCultureConfig>()
        if (placeNoMarkList.size <= 8) {
            noMarkList.addAll(placeNoMarkList)
        } else {
            while (noMarkList.size < 8) {
                val singleCulture = placeNoMarkList.random()
                if (singleCulture !in noMarkList) {
                    noMarkList.add(singleCulture)
                }
            }
        }
        mPlaceAdapter.setList(noMarkList)
        showChange(mPlaceAdapter, iv_view_change)
    }

    /**
     * 判断 换一批功能是否显示
     */
    private fun showChange(adapter: HomeTownFoodAdapter, iv: ImageView) {
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
        val cityList = mHomeTownEditViewModel.homeTownData.value?.cityConfigList ?: return
        //列表排序
        Collections.sort(cityList, object : Comparator<EditCityBean> {
            override fun compare(o1: EditCityBean?, o2: EditCityBean?): Int {
                if (o1 == null || o2 == null) {
                    return 0
                }
                return o1.provinceOrderNum - o2.provinceOrderNum
            }
        })
        //身份数据
        val provinceNames = mutableListOf<String>()
        //省份下属的城市列表
        var cityListOfProvince = mutableListOf<EditCityBean>()
        cityList.forEach {
            val provinceName = it.province
            if (provinceName !in provinceNames) {
                //一个新的省份
                if (cityListOfProvince.isNotEmpty()) {
                    cityItems.add(cityListOfProvince)
                }
                cityListOfProvince = mutableListOf()
                provinceNames.add(provinceName)
                val tempProvincesBean = EditCityBean().apply { city = provinceName }
                provinceItems.add(tempProvincesBean)
            }
            //添加城市数据
            cityListOfProvince.add(it)
        }
        if (cityListOfProvince.isNotEmpty()) {
            //省份所属的城市数据不为空,添加
            cityItems.add(cityListOfProvince)
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
            val tempCity = cityItems.getOrNull(options1)?.getOrNull(options2) ?: return@OnOptionsSelectListener
            val currentCityId = mHomeTownEditViewModel.homeTownData.value?.homeTownId ?: 0
            if (tempCity.cityId == currentCityId) {
                //ID一致，直接返回
                return@OnOptionsSelectListener
            }
            mHomeTownEditViewModel.homeTownData.value?.apply {
                homeTownId = tempCity.cityId
                homeTownProvince = tempCity.province
                homeTownCity = tempCity.city
            }
            showCityName(tempCity.province, tempCity.city)
            mHomeTownEditViewModel.getCityCulture(tempCity.cityId)
        })
            .setTitleText("城市选择")
            .setContentTextSize(20)//设置滚轮文字大小
            .setDividerColor(Color.LTGRAY)//设置分割线的颜色
            .setSelectOptions(0, 1)//默认选中项
            .setBgColor(Color.WHITE)
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .setTitleBgColor(Color.DKGRAY)
            .setTitleColor(Color.LTGRAY)
            .setCancelColor(Color.YELLOW)
            .setSubmitColor(Color.YELLOW)
            .setTextColorCenter(GlobalUtils.getColor(R.color.black_333))
            .setTextColorOut(GlobalUtils.getColor(R.color.black_999))
            .setLayoutRes(R.layout.dialog_edit_city, CustomListener { view ->
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
        pvOptions?.setPicker(provinceItems, cityItems) //二级选择器

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

    private fun getNextClass(tag: String?): Class<out BaseActivity>? {
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
            SchoolActivity.Tag -> {
                SchoolActivity::class.java
            }
            ProfessionActivity.Tag -> {
                ProfessionActivity::class.java
            }
            else -> {
                return null
            }
        }
    }

}