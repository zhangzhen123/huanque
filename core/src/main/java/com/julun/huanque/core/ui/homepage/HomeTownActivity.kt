package com.julun.huanque.core.ui.homepage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.EditHomeTownBean
import com.julun.huanque.common.bean.beans.SingleCulture
import com.julun.huanque.common.bean.beans.SingleCultureConfig
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomeTownFoodAdapter
import com.julun.huanque.core.viewmodel.HomeTownEditViewModel
import com.julun.huanque.core.widgets.MarqueeTextView
import kotlinx.android.synthetic.main.act_edit_info.*
import kotlinx.android.synthetic.main.act_home_town.*
import kotlinx.android.synthetic.main.act_home_town.header_page
import kotlinx.android.synthetic.main.act_home_town.tv_home_town
import kotlinx.android.synthetic.main.fragment_blind_box_rule.*
import java.lang.StringBuilder

/**
 *@创建者   dong
 *@创建时间 2020/12/30 19:59
 *@描述 家乡数据
 */
class HomeTownActivity : BaseActivity() {

    companion object {
        /**
         * 跳转页面
         */
        fun newInstance(act: Activity, homeTownName: String) {
            val intent = Intent(act, HomeTownActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.Home_Town_Name, homeTownName)
                act.startActivity(intent)
            }
        }
    }

    //美食Adapter
    private val mFoodAdapter = HomeTownFoodAdapter()
    private val mPlaceAdapter = HomeTownFoodAdapter()
    private val mHomeTownEditViewModel: HomeTownEditViewModel by viewModels()

    //    private val foodNoMarkList = mutableListOf<SingleCultureConfig>()
//    private val placeNoMarkList = mutableListOf<SingleCultureConfig>()
    override fun getLayoutId() = R.layout.act_home_town

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val cityName = intent?.getStringExtra(ParamConstant.Home_Town_Name) ?: ""
        tv_home_town.text = cityName
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

    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mHomeTownEditViewModel.homeTownData.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
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
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_item_food.layoutManager = GridLayoutManager(this, 4)
        recycler_item_food.adapter = mFoodAdapter
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
    private fun showViewByData(info: EditHomeTownBean) {
        val homeTownStr = StringBuilder()
        if (info.homeTownProvince.isNotEmpty()) {
            homeTownStr.append(info.homeTownProvince)
        }
        if (homeTownStr.isNotEmpty()) {
            homeTownStr.append("/")
        }
        homeTownStr.append(info.homeTownCity)
        tv_home_town.text = homeTownStr.toString()
    }

    /**
     * 显示美食布局
     */
    private fun showFoodView(foodBean: SingleCulture) {
        tv_eat_food.text = "${foodBean.cultureTypeText}"
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


}