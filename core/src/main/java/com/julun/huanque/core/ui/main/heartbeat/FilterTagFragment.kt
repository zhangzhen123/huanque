package com.julun.huanque.core.ui.main.heartbeat

import android.animation.Animator
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import com.julun.huanque.common.base.BaseBottomSheetFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.FilterGroupTag
import com.julun.huanque.common.bean.beans.FilterTag
import com.julun.huanque.common.bean.beans.FilterTagBean
import com.julun.huanque.common.bean.beans.FilterWish
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.sliceFromStart
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.common.widgets.recycler.decoration.VerticalItemDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.MainConnectViewModel
import kotlinx.android.synthetic.main.fragment_filter_tag.*
import java.lang.StringBuilder

/**
 * 标签过滤弹窗
 */
class FilterTagFragment : BaseBottomSheetFragment() {

    companion object {
        const val MIN_DISTANCE = 1L//最小1km
        const val MAX_DISTANCE = 200L//最大200km
        const val MIN_AGE = 18//最小18岁
        const val MAX_AGE = 50//最大50岁
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_filter_tag
    }

    val viewModel: FilterTagViewModel by viewModels()

    val connectViewModel: MainConnectViewModel by activityViewModels()

    private val observer by lazy {
        Observer<NetState> { state ->
            if (state != null) {
                when (state.state) {
                    NetStateType.SUCCESS -> {//showSuccess()
                        state_pager_view.showSuccess()
                    }
                    NetStateType.LOADING -> {//showLoading()
                        state_pager_view.showLoading()
                    }
                    NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                        state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                            viewModel.queryInfo(QueryType.INIT)
                        })
                    }

                }

            }


        }
    }

    override fun initViews() {

        initViewModel()
        rv_social_type.adapter = socialsAdapter
        rv_social_type.itemAnimator = null
        rv_social_type.layoutManager = GridLayoutManager(requireContext(), 2)
        rv_social_type.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(15)))
//        rv_social_type.setHasFixedSize(true)
        rv_social_type.isNestedScrollingEnabled = false

        rv_tags.adapter = parentTagAdapter
        parentTagAdapter.addFooterView(LayoutInflater.from(context).inflate(R.layout.view_bottom_holder, null))
        rv_tags.layoutManager = LinearLayoutManager(requireContext())
        rv_tags.addItemDecoration(VerticalItemDecoration(dp2px(30)))
//        rv_tags.setHasFixedSize(true)
        rv_tags.isNestedScrollingEnabled = false
        (rv_tags.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        socialsAdapter.setOnItemClickListener { _, _, position ->
            val item = socialsAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            item.selected = !item.selected
            socialsAdapter.notifyItemChanged(position)
        }

        parentTagAdapter.setOnItemChildClickListener { _, view, position ->
            val item = parentTagAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            when (view.id) {
                R.id.fl_title -> {
                    if(item.tagList.size>4){
                        item.isFold = !item.isFold
                        parentTagAdapter.notifyItemChanged(position)
                    }
                }
            }
        }
        tv_male.onClickNew {
            setSexType(Sex.MALE)
        }
        tv_female.onClickNew {
            setSexType(Sex.FEMALE)
        }

        ensure_filter.onClickNew {
            val currentInfo = currentInfo ?: return@onClickNew
            val tagIds = StringBuilder()
            val wishs = StringBuilder()
            when (currentInfo.sexType) {
                Sex.MALE -> {
                    currentInfo.typeMap.Male.forEach { pTag ->
                        pTag.tagList.forEach { tag ->
                            if (tag.mark) {
                                tagIds.append(tag.tagId).append(",")
                            }
                        }
                    }
                }
                Sex.FEMALE -> {
                    currentInfo.typeMap.Female.forEach { pTag ->
                        pTag.tagList.forEach { tag ->
                            if (tag.mark) {
                                tagIds.append(tag.tagId).append(",")
                            }
                        }
                    }
                }

            }
            currentInfo.wishList.forEach { w ->
                if (w.selected) {
                    wishs.append(w.wishType).append(",")
                }

            }
            var tagIdsStr = tagIds.toString()
            tagIdsStr = tagIdsStr.removeSuffix(",")
            var wishStr = wishs.toString()
            wishStr = wishStr.removeSuffix(",")

            viewModel.saveSearchConfig(
                currentInfo.sexType,
                currentInfo.minAge,
                currentInfo.maxAge,
                currentInfo.distance,
                wishStr,
                tagIdsStr
            )
        }
        sb_distance.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(rangeSeekBar: RangeSeekBar, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
//                logger.info("leftValue=${leftValue} rightValue=${rightValue} isFromUser=${isFromUser}")
                if (!isFromUser) {
                    return
                }
                val info = currentInfo ?: return
                var dis = 0L
                dis = when (leftValue) {
                    0f -> {
                        MIN_DISTANCE
                    }
                    else -> {
                        (MAX_DISTANCE * leftValue / 100).toLong()
                    }
                }
                if (dis >= MAX_DISTANCE) {
                    tv_distance.text = "${dis}km+"
                } else {
                    tv_distance.text = "${dis}km"
                }

                info.distance = dis * 1000
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

        })
        sb_range_age.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(rangeSeekBar: RangeSeekBar, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
//                logger.info("sb_range_age leftValue=${leftValue} rightValue=${rightValue} isFromUser=${isFromUser}")
                if (!isFromUser) {
                    return
                }

                val info = currentInfo ?: return
                var leftAge = 0
                val range = MAX_AGE - MIN_AGE
                leftAge = when (leftValue) {
                    0f -> {
                        MIN_AGE
                    }
                    else -> {
                        MIN_AGE + (range * leftValue / 100).toInt()
                    }
                }
                var rightAge = 0
                rightAge = when (rightValue) {
                    0f -> {
                        MIN_AGE
                    }
                    else -> {
                        MIN_AGE + (range * rightValue / 100).toInt()
                    }
                }
                info.minAge = leftAge
                info.maxAge = rightAge

                if (rightAge == MAX_AGE) {
                    tv_age.text = "${leftAge}-${rightAge}+"
                } else {
                    tv_age.text = "${leftAge}-${rightAge}"
                }


            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {

            }

        })

        viewModel.requestInfo(QueryType.INIT)
    }

    override fun getHeight(): Int {
        return dp2px(516)
    }

    private fun initViewModel() {

        viewModel.filterTagBean.observe(viewLifecycleOwner, Observer {
            if (it.isSuccess()) {
                renderData(it.requireT())
            }

        })
        viewModel.saveResult.observe(viewLifecycleOwner, Observer {
            if (it.isSuccess()) {
                connectViewModel.refreshNearby.value = true
                dismiss()
            }

        })
        viewModel.loadState.observe(this, observer)
    }

    private var currentInfo: FilterTagBean? = null
    private fun renderData(tagBean: FilterTagBean) {
        currentInfo = tagBean
        if (tagBean.distance >= MAX_DISTANCE * 1000) {
            tv_distance.text = "${tagBean.distance / 1000}km+"
        } else {
            tv_distance.text = "${tagBean.distance / 1000}km"
        }

        if (tagBean.maxAge >= MAX_AGE) {
            tv_age.text = "${tagBean.minAge}-${tagBean.maxAge}+"
        } else {
            tv_age.text = "${tagBean.minAge}-${tagBean.maxAge}"
        }

        socialsAdapter.setList(tagBean.wishList)
        if (tagBean.distance <= MIN_DISTANCE) {
            sb_distance.setProgress(0f)
        } else {
            sb_distance.setProgress(tagBean.distance / 1000f / MAX_DISTANCE * 100)
        }

        var minAgeValue = 0f
        var maxAgeValue = 0f
        var rangeAge = MAX_AGE - MIN_AGE
        if (tagBean.minAge <= MIN_AGE) {
            minAgeValue = 0f

        } else {
            minAgeValue = (tagBean.minAge - MIN_AGE).toFloat() / rangeAge * 100f
        }

        if (tagBean.maxAge <= MIN_AGE) {
            maxAgeValue = 0f
        } else {
            maxAgeValue = (tagBean.maxAge - MIN_AGE).toFloat() / rangeAge * 100f
        }
        sb_range_age.setProgress(minAgeValue, maxAgeValue)

        setSexType(tagBean.sexType)
    }


    private fun setSexType(sexType: String) {
        currentInfo ?: return
        currentInfo!!.sexType = sexType
        if (sexType == Sex.MALE) {
            tv_male.isSelected = true
            tv_female.isSelected = false
        } else {
            tv_male.isSelected = false
            tv_female.isSelected = true
        }

        if (sexType == Sex.MALE) {
            parentTagAdapter.setList(currentInfo!!.typeMap.Male)
        } else {
            parentTagAdapter.setList(currentInfo!!.typeMap.Female)
        }
    }

    private val socialsAdapter: BaseQuickAdapter<FilterWish, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<FilterWish, BaseViewHolder>(R.layout.item_filter_child_tag) {

            override fun convert(holder: BaseViewHolder, item: FilterWish) {
                val text = holder.getView<TextView>(R.id.tv_tag)
                text.text = "${item.wishTypeText}"
                text.isSelected = item.selected
            }

        }
    }

    inner class FilterTagAdapter : BaseQuickAdapter<FilterTag, BaseViewHolder>(R.layout.item_filter_child_tag) {
        override fun convert(holder: BaseViewHolder, item: FilterTag) {
            val text = holder.getView<TextView>(R.id.tv_tag)
            text.text = "${item.tagName}"
            text.isSelected = item.mark
        }
    }

    //统一处理子标签列表的点击监听
    private var tagListener: OnItemClickListener = OnItemClickListener { adapter, view, position ->
        val tagItem = adapter.getItemOrNull(position) as? FilterTag
        if (tagItem != null) {
            tagItem.mark = !tagItem.mark
            adapter.notifyItemChanged(position)
        }

    }
    private val parentTagAdapter: BaseQuickAdapter<FilterGroupTag, BaseViewHolder> by lazy {

        object : BaseQuickAdapter<FilterGroupTag, BaseViewHolder>(R.layout.item_filter_parent_tag) {
            init {
                addChildClickViewIds(R.id.fl_title)
            }

            override fun convert(holder: BaseViewHolder, item: FilterGroupTag) {
                holder.setText(
                    R.id.tv_group_tag,
                    "${item.tagTypeText} ${item.tagList.filter { it.mark }.size}/${item.tagList.size}"
                )
                val rv = holder.getView<RecyclerView>(R.id.rv_child_tag)
                rv.layoutManager = GridLayoutManager(context, 4)
                if (rv.itemDecorationCount <= 0) {
                    rv.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(15)))
                }
                val fAdapter = rv.adapter as? FilterTagAdapter ?: FilterTagAdapter()
                rv.adapter = fAdapter
                rv.itemAnimator = null
                if (item.tagList.size <= 4) {
                    holder.setGone(R.id.iv_arrow, true)
                    fAdapter.setList(item.tagList)
                } else {
                    holder.setGone(R.id.iv_arrow, false)
                    if (item.isFold) {
                        holder.setImageResource(R.id.iv_arrow, R.mipmap.arrow_down_grey)
                        fAdapter.setList(item.tagList.sliceFromStart(4, false))
                    } else {
                        holder.setImageResource(R.id.iv_arrow, R.mipmap.arrow_up_grey)
                        fAdapter.setList(item.tagList)
                    }

                }
                fAdapter.setOnItemClickListener { _, view, position ->
                    val tagItem = fAdapter.getItemOrNull(position)
                    if (tagItem != null) {
                        tagItem.mark = !tagItem.mark
                        fAdapter.notifyItemChanged(position)
                    }
                    holder.setText(
                        R.id.tv_group_tag,
                        "${item.tagTypeText} ${item.tagList.filter { it.mark }.size}/${item.tagList.size}"
                    )
                }

            }
        }


    }
}