package com.julun.huanque.core.ui.live.fragment

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSONObject
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.BaseData
import com.julun.huanque.common.bean.TplBean
import com.julun.huanque.common.bean.beans.GiftRuleAward
import com.julun.huanque.common.bean.beans.GiftRuleBean
import com.julun.huanque.common.bean.beans.NotEnoughBalanceBean
import com.julun.huanque.common.bean.forms.ConsumeForm
import com.julun.huanque.common.bean.forms.RechargeRuleQueryForm
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.constant.ProdType
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.Transformer
import com.julun.huanque.core.ui.live.adapter.BlindBoxRuleGiftAdapter
import com.julun.huanque.core.viewmodel.EggSettingViewModel
import com.julun.huanque.core.viewmodel.SendGiftViewModel
import com.julun.huanque.core.widgets.MarqueeTextView
import com.trello.rxlifecycle4.android.lifecycle.kotlin.bindUntilEvent
import github.hellocsl.layoutmanager.gallery.GalleryLayoutManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_blind_box_rule.*
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.textColor
import java.lang.StringBuilder


/**
 *@创建者   dong
 *@创建时间 2020/10/28 14:54
 *@描述 盲盒类型说明弹窗
 */
class BlindBoxRuleFragment : BaseDialogFragment() {
    private val mPlayerViewModel: PlayerViewModel by activityViewModels()
    private val mGiftExplainViewModel: EggSettingViewModel by activityViewModels()

    //送礼Fragment
    private val mSendGiftViewModel: SendGiftViewModel by viewModels()

    //当前显示的奖励礼物
    private var mCurrentGiftRuleAward: GiftRuleAward? = null

    //画廊Manager
    private var mGalleryLayoutManager: GalleryLayoutManager? = null


    //可以获得的礼物Adapter
    private var mAdapter = BlindBoxRuleGiftAdapter()

    override fun getLayoutId() = R.layout.fragment_blind_box_rule

    override fun initViews() {
        initRecyclerView()

        tv_send.onClickNew {
            //赠送礼物
            mSendGiftViewModel.sendGift(
                ConsumeForm(
                    mPlayerViewModel.programId,
                    1,
                    (mPlayerViewModel.blindBoxBeanData.value?.giftId ?: 0).toInt(),
                    BusiConstant.False, mPlayerViewModel.blindBoxBeanData.value?.prodType ?: ProdType.Gift
                )
            )
            tv_send.isEnabled = false
        }
    }


    override fun onStart() {
        super.onStart()
        tv_send.isEnabled = true
        initViewModel()
        this.setDialogSize(Gravity.BOTTOM, 0, 440)
        //不需要半透明遮罩层
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mPlayerViewModel.blindBoxBeanData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                showSendBtn(it.giftName, it.giftPrice)
                //调用接口获取数据
                mGiftExplainViewModel.giftRule(it.giftId)
            }
        })

        mGiftExplainViewModel.mRuleGiftBean.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                //显示数据
                showViewByData(it)
            }
        })

        mGiftExplainViewModel.barrages.observe(this, Observer { list ->
            if (list != null) {
                Observable.just(list)
                    .map { oriList ->
                        //                                val map = JsonUtil.toJsonMap(it)
//                                MessageUtil.decodeMessageContent(JsonUtil.seriazileAsString((map?.get("content") ?: "")))
                        var realList = mutableListOf<TplBean>()
                        oriList.forEach {
                            val baseData: BaseData = JsonUtil.deserializeAsObject(it, BaseData::class.java)
                            val jsonObject = baseData.data as JSONObject
                            val jsonString = jsonObject.toJSONString()
                            val tplBean = JsonUtil.deserializeAsObject<TplBean>(jsonString, TplBean::class.java)
                            tplBean.preProcess()
                            realList.add(tplBean)
                        }
                        realList
                    }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .bindUntilEvent(this, Lifecycle.Event.ON_DESTROY)
                    .subscribe({
                        showAttentionView(it)
                    }, { it.printStackTrace() })
                mGiftExplainViewModel?.barrages?.value = null
            }
        })

        mSendGiftViewModel.sendGiftSuccess.observe(this, Observer { it ->
            if (it != null) {
                //显示砸蛋结果数据
                if (it.prizeBeans > 0 && it.prizeList.isNotEmpty()) {
                    mPlayerViewModel.eggResultData.value = it
                    //背包需要刷新
                    mPlayerViewModel.refreshGift.value = true
                }
                if (SPUtils.getBoolean(SPParamKey.Blind_Box_Show, true) && it.feedbackList?.isNotEmpty() == true) {
                    mPlayerViewModel.sendBlindBoxResultData.value = it
                }
                //赠送按钮可以使用
                tv_send.isEnabled = true
            }
        })
        mSendGiftViewModel.sendGiftError.observe(this, Observer {
            if (it != null) {
                //赠送按钮可以使用
                tv_send.isEnabled = true
                sendGiftError(it)
            }
        })
    }

    /**
     * 初始化Banner
     */
    private fun initRecyclerView() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //停止
                    val position = mGalleryLayoutManager?.curSelectedPosition ?: return
                    setSelectPosition(position)
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = mGalleryLayoutManager?.curSelectedPosition ?: return
                setSelectPosition(position)
            }
        })

        mGalleryLayoutManager = GalleryLayoutManager(GalleryLayoutManager.HORIZONTAL)
        mGalleryLayoutManager?.attach(recyclerView)
        //设置滑动缩放效果
        mGalleryLayoutManager?.setItemTransformer(Transformer())
        recyclerView.adapter = mAdapter
    }

    /**
     * 设置选中的Position
     */
    private fun setSelectPosition(position: Int) {
        val currentBean = mAdapter.getItemOrNull(position) ?: return
        if (currentBean.awardName == mCurrentGiftRuleAward?.awardName) {
            //同样的礼物
            return
        }

        tv_gift_count.text = "${currentBean.awardName}*${currentBean.count}"
        tv_value.text = "价值：${currentBean.beans}鹊币"
        tv_probability.text = "概率：${currentBean.awardRatio}"

        mCurrentGiftRuleAward = currentBean
    }


    /**
     * 显示赠送按钮文案
     */
    private fun showSendBtn(giftName: String, giftPrice: Long) {
        tv_send.text = "送${giftName} ${giftPrice}鹊币/次"
    }

    /**
     * 根据数据显示视图
     */
    private fun showViewByData(bean: GiftRuleBean) {
        ImageUtils.loadImageWithWidth(sdv_bg, StringHelper.getOssImgUrl(bean.ruleBgPic), ScreenUtils.getScreenWidth())
        mAdapter.setList(bean.awardList)
        if (bean.awardList.isNotEmpty()) {
            //显示第一个礼物
            setSelectPosition(0)
        }
    }

    /**
     * 显示提示视图
     */
    private fun showAttentionView(tplList: MutableList<TplBean>) {
        val barrierBuilder = StringBuilder()
        tplList.take(5).forEach {
//            val tvContent = LayoutInflater.from(requireContext()).inflate(R.layout.tv_blind_rule_attention, null) as? MarqueeTextView
//            tvContent?.text = it.realTxt
//            tvContent?.setMarqueeEnable(true)
//            view_flipper.addView(tvContent)
            if (barrierBuilder.isNotEmpty()) {
                barrierBuilder.append("     ")
            }
            barrierBuilder.append(it.realTxt)
        }
//        view_flipper.startFlipping()
        tv_barrier.text = barrierBuilder.toString()
        tv_barrier.setMarqueeEnable(true)
    }

    /**
     * 送礼报错处理
     */
    private fun sendGiftError(it: Throwable) {
        if (it is ResponseError) {
            when (it.busiCode) {
                ErrorCodes.BALANCE_NOT_ENOUGH -> {
                    try {
                        mPlayerViewModel?.notEnoughBalance?.value = NotEnoughBalanceBean(RechargeRuleQueryForm.SEND_GIFT, beans = 0)
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                }
                else -> {
                    ToastUtils.show(it.busiMessage)
                }
            }
        }
//        resetData()
    }

}