package com.julun.huanque.message.fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseVMDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.ChatGift
import com.julun.huanque.common.bean.beans.ChatGiftInfo
import com.julun.huanque.common.bean.beans.ChatGroupGift
import com.julun.huanque.common.bean.beans.ChatSendResult
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.svga.SVGAHelper
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.ChatSendGiftViewModel
import com.julun.huanque.message.viewmodel.PrivateConversationViewModel
import kotlinx.android.synthetic.main.fragment_chat_send_gift.*
import kotlinx.coroutines.launch
import org.jetbrains.anko.imageResource

/**
 *@创建者   dong
 *@创建时间 2020/7/9 15:22
 *@描述 聊天页面 送礼对象
 */
class ChatSendGiftFragment : BaseVMDialogFragment<ChatSendGiftViewModel>() {


    private var currentPagePosition: Int = 0
    private var currentSelectGift: ChatGift? = null

    //记录
    private var currentGiftAdapter: ChatGiftListAdapter? = null
    private val mPrivateConversationViewModel: PrivateConversationViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.fragment_chat_send_gift

    override fun initViews() {

        view_pager.adapter = viewPagerAdapter
        view_pager.registerOnPageChangeCallback(viewPagerChangeListener)

        tv_send.onClickNew {
            sendGift()
        }

    }

    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT)
        initViewModel()
        mViewModel.queryInfo()
    }

    private fun initViewModel() {
        mViewModel.giftList.observe(viewLifecycleOwner, Observer {
            //
            if (it.state == NetStateType.SUCCESS) {
                loadData(it.getT())
            } else if (it.state == NetStateType.ERROR) {
                //dodo
            }
        })
        mViewModel.sendResult.observe(viewLifecycleOwner, Observer {
            //
//            tv_send.isEnabled = true
            it ?: return@Observer
            logger.info("赠送返回=${it.state}")
            if (it.state == NetStateType.SUCCESS) {
//                refreshSendResult(it.getT())
            } else if (it.state == NetStateType.ERROR) {
                //dodo
                if (it.error?.busiCode == 1001) {
                    //余额不足
                    showBalanceNotEnough()
                } else {
                    ToastUtils.show(it.error?.busiMessage ?: return@Observer)
                }
                mViewModel.sendResult.value = null
            }
        })

        mViewModel.sendGiftBean.observe(this, Observer {
            if (it != null) {
                mPrivateConversationViewModel.sendGiftSuccessData.value = it
                mViewModel.sendGiftBean.value = null
            }

        })
        mPrivateConversationViewModel.balance.observe(this, Observer {
            if (it != null) {
                tv_balance.text = "$it"
            }
        })

    }


    private fun loadData(info: ChatGiftInfo) {
        tv_gift_tips.text = info.tips
//        tv_balance.text = "${info.beans}"
        viewPagerAdapter.setNewInstance(info.viewPagerData)
        initDotLayout(info.viewPagerData.size)
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
                state_pager_view.showSuccess()
            }
            NetStateType.LOADING -> {//showLoading()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.queryInfo()
                })
            }

        }

    }

    /**
     * 初始化小点指示器
     */
    private fun initDotLayout(count: Int) {
        ll_dot.removeAllViews()
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(dp2px(5), 0, dp2px(5), 0)
        repeat(count) {
            val dotImg = LayoutInflater.from(context).inflate(R.layout.view_face_group_dot, null)
                .findViewById<ImageView>(R.id.face_dot)
            dotImg.imageResource = R.drawable.selector_chat_gift_dot
            ll_dot.addView(dotImg, lp)
        }
    }

    /**
     * 当前分页圆点高亮
     */
    private fun lightPositionDots(position: Int) {
        val count = ll_dot.childCount
        if (count == 0) return
        for (i in 0 until count) {
            ll_dot?.getChildAt(i)?.isActivated = i == position
        }
    }

    private fun sendGift() {
        logger.info("点击了赠送")
        val targetId = mPrivateConversationViewModel.targetIdData.value
        if (currentSelectGift == null) {
            ToastUtils.show("请先选择礼物再赠送")
            return
        }
        if (targetId == null) {
            ToastUtils.show("没有可赠送目标")
            return
        }
//        val giftId: Int = currentSelectGift?.chatGiftId ?: return

        mViewModel.sendGift(targetId, currentSelectGift ?: return)
//        tv_send.isEnabled = false
    }

    //设置一个公共的缓存池 提高效率
    private val mGiftViewPool: RecyclerView.RecycledViewPool by lazy {
        RecyclerView.RecycledViewPool()
    }
    private val viewPagerAdapter: BaseQuickAdapter<ChatGroupGift, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<ChatGroupGift, BaseViewHolder>(R.layout.pager_chat_gift) {
            override fun convert(holder: BaseViewHolder, item: ChatGroupGift) {
                val rv = holder.getView<RecyclerView>(R.id.rv_chat_gifts)
                (rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                rv.layoutManager = GridLayoutManager(context, 4)
                if (rv.itemDecorationCount <= 0) {
                    rv.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(12)))
                }
                rv.setRecycledViewPool(mGiftViewPool)
                val adapter = ChatGiftListAdapter(item.gifts)
                rv.adapter = adapter
                adapter.setOnItemClickListener { _, _, position ->
                    currentSelectGift = adapter.getItemOrNull(position)
                    adapter.notifyDataSetChanged()
                    currentGiftAdapter?.notifyDataSetChanged()
                    currentGiftAdapter = adapter
                    tv_send.isEnabled = true
                }

            }

        }
    }

    private val viewPagerChangeListener: ViewPager2.OnPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                logger.info("onPageSelected=$position")
                currentPagePosition = position
                lightPositionDots(position)
            }
        }
    }

    inner class ChatGiftListAdapter(data: MutableList<ChatGift>) :
        BaseQuickAdapter<ChatGift, BaseViewHolder>(R.layout.item_chat_gift, data = data) {

        override fun convert(holder: BaseViewHolder, item: ChatGift) {
            val giftImg = holder.getView<SimpleDraweeView>(R.id.sdv_gift)
            if (currentSelectGift?.chatGiftId == item.chatGiftId) {
                giftImg.loadImage(item.selPic, 70f, 70f)
                giftImg.setBackgroundResource(R.drawable.bg_stroke_btn2)
            } else {
                giftImg.loadImage(item.pic, 70f, 70f)
                giftImg.setBackgroundResource(0)
            }
            holder.setText(R.id.tv_gift_name, item.giftName).setText(R.id.tv_gift_price, "${item.beans}鹊币")

        }
    }

    /**
     * 显示余额不足弹窗
     */
    private fun showBalanceNotEnough() {
        activity?.let { act ->
            MyAlertDialog(act).showAlertWithOK(
                "您的鹊币余额不足",
                MyAlertDialog.MyDialogCallback(onRight = {
                    //跳转充值页面
                    (act as? BaseActivity)?.lifecycleScope?.launch {
                        SVGAHelper.logger.info("threadName = ${Thread.currentThread().name}")
                        ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
                    }

                }), "余额不足", "去充值"
            )
        }
    }
}