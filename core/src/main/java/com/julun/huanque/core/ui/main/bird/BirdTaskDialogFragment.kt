package com.julun.huanque.core.ui.main.bird

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMDialogFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.BirdAward
import com.julun.huanque.common.bean.beans.BirdLiveAward
import com.julun.huanque.common.bean.beans.BirdTask
import com.julun.huanque.common.bean.beans.BirdTaskInfo
import com.julun.huanque.common.bean.events.HideBirdEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.audio.SoundPoolManager
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_bird_tasks.*
import kotlinx.android.synthetic.main.view_bird_task_award.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.backgroundColor

/**
 * [leYuanViewModel]将主面板的viewModel传过来
 */
class BirdTaskDialogFragment(private val leYuanViewModel: LeYuanViewModel?) : BaseVMDialogFragment<BirdTaskViewModel>() {

    constructor() : this(null)

    private var currentItem: BirdTask? = null
    private val taskAdapter: BirdTaskAdapter by lazy { BirdTaskAdapter() }
    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_tasks
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 480)
//        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private var isInLivePage = false
    override fun initViews() {
        isInLivePage = requireActivity() is PlayerActivity
        initViewModel()
        tasksList.layoutManager = LinearLayoutManager(requireContext())
        tasksList.adapter = taskAdapter
        ivClose.onClickNew {
            dismiss()
        }
        taskAdapter.addFooterView(LayoutInflater.from(context).inflate(R.layout.view_bottom_holder, null))
        taskAdapter.setOnItemChildClickListener { _, _, position ->
            currentItem = null
            val item = taskAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            when (item.taskStatus) {
                BirdTaskStatus.NotFinish -> {
                    when (item.jumpType) {
                        BirdTaskJump.FriendHome -> {
                            this@BirdTaskDialogFragment.dismiss()
                            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                                .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
                        }
                        BirdTaskJump.InviteFriend -> {
                            this@BirdTaskDialogFragment.dismiss()
                            RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
                        }

                        BirdTaskJump.LiveRoom -> {
                            mViewModel.randomLive()
                            currentItem = item
                        }
                        BirdTaskJump.Message -> {
                            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                                .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX).navigation()
                            this@BirdTaskDialogFragment.dismiss()
                            EventBus.getDefault().post(HideBirdEvent())
                        }
                        BirdTaskJump.AnonyVoiceCall -> {
                            ARouter.getInstance().build(ARouterConstant.ANONYMOUS_VOICE_ACTIVITY).navigation()
                            this@BirdTaskDialogFragment.dismiss()
                            EventBus.getDefault().post(HideBirdEvent())

                        }

                    }

                }
                BirdTaskStatus.NotReceive -> {
                    mViewModel.receiveTask(item.taskCode)
                }
            }

        }
        mRefreshLayout.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)
        award_01.onClickNew {
            val data = award_01.currentData ?: return@onClickNew
            logger.info("award_01 ${data.awardStatus}")
            if (data.awardStatus == BirdTaskStatus.NotReceive) {
                mViewModel.receiveAward(data.activeCode)
            }

        }
        award_02.onClickNew {
            val data = award_02.currentData ?: return@onClickNew
            logger.info("award_02 ${data.awardStatus}")
            if (data.awardStatus == BirdTaskStatus.NotReceive) {
                mViewModel.receiveAward(data.activeCode)
            }
        }
        award_03.onClickNew {
            val data = award_03.currentData ?: return@onClickNew
            logger.info("award_03 ${data.awardStatus}")
            if (data.awardStatus == BirdTaskStatus.NotReceive) {
                mViewModel.receiveAward(data.activeCode)
            }
        }
        state_pager_view.backgroundColor = Color.TRANSPARENT
        mViewModel.queryInfo()
    }

    override fun reCoverView() {
        initViewModel()
        mViewModel.queryInfo()
    }

    private fun initViewModel() {

        mViewModel.taskInfo.observe(this, Observer {
            mRefreshLayout.isRefreshing = false
            if (it.isSuccess()) {
                val data = it.requireT()
                renderData(data)

            }

        })
        mViewModel.receiveTaskResult.observe(this, Observer {
            if (it.isSuccess()) {
//                MyAlertDialog(requireActivity()).showAlertWithOK(
//                    "恭喜您成功领取了${it.requireT().awardCoins}金币",
//                    title = "领取成功",
//                    okText = "知道了"
//                )
                val content = "恭喜您成功领取了\n ${StringHelper.formatBigNum(it.requireT().awardCoins)}金币"
                val dialog = BirdGotMoneyDialogFragment.newInstance(content)
                dialog.show(requireActivity(), "BirdGotMoneyDialogFragment")
                if (activity !is PlayerActivity) {
                    SoundPoolManager.instance.play(LeYuanFragment.BIRD_COIN)
                }

                mViewModel.queryInfo(QueryType.REFRESH)
                leYuanViewModel?.refreshCoins()
            } else if (it.state == NetStateType.ERROR) {
                ToastUtils.show("${it.error?.busiMessage}")
            }
        })
        mViewModel.receiveActiveAward.observe(this, Observer {
            if (it.isSuccess()) {
                mViewModel.queryInfo(QueryType.REFRESH)
                leYuanViewModel?.refreshCoins()
            } else if (it.state == NetStateType.ERROR) {
                ToastUtils.show("${it.error?.busiMessage}")
            }
        })

        mViewModel.mRandomRoom.observe(this, Observer {
            if (it.isSuccess()) {
                val item = currentItem ?: return@Observer
                val time = StringHelper.getQueryString(item.taskParams, "seconds")?.toLongOrNull()
                if (time != null) {
                    val info = BirdLiveAward(time, item.awardType, item.taskCode)
                    ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY)
                        .withSerializable(ParamConstant.BIRD_AWARD_INFO, info)
                        .withString(ParamConstant.FROM, PlayerFrom.Magpie)
                        .withLong(IntentParamKey.PROGRAM_ID.name, it.requireT().programId).navigation()
                } else {
                    ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY)
                        .withString(ParamConstant.FROM, PlayerFrom.Magpie)
                        .withLong(IntentParamKey.PROGRAM_ID.name, it.requireT().programId).navigation()
                }
                if (isInLivePage) {
                    //直接关闭弹窗
                    EventBus.getDefault().post(HideBirdEvent())
                } else {
                    //关闭养鹊独立页面
                    requireActivity().finish()
                }
                dismiss()
            } else if (it.state == NetStateType.ERROR) {
                ToastUtils.show("${it.error?.busiMessage}")
            }
        })

    }

    private fun renderData(data: BirdTaskInfo) {
        taskAdapter.setNewInstance(data.taskList)
        tv_active.text = "${data.activeInfo.activeValue}"
        //换算成100基数
        val process = 100f * data.activeInfo.activeValue / data.activeInfo.maxActiveValue
        logger.info("当前的完成进度=$process")
        val plp = view_process.layoutParams as ConstraintLayout.LayoutParams
        val pBlp = view_process_bg.layoutParams as ConstraintLayout.LayoutParams
        plp.horizontalWeight = process
        pBlp.horizontalWeight = 100f - process
        view_process.post {
            view_process.requestLayout()
            view_process_bg.requestLayout()
        }

        award_01.renderData(data.activeInfo.awardList.getOrNull(0))
        award_02.renderData(data.activeInfo.awardList.getOrNull(1))
        award_03.renderData(data.activeInfo.awardList.getOrNull(2))
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
                state_pager_view.showSuccess()
                mRefreshLayout.show()
                taskAdapter.setEmptyView(
                    MixedHelper.getEmptyView(
                        requireContext()
                    )
                )

            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.queryInfo()
                })
            }
        }
    }

}

class BirdTaskAwardView : FrameLayout {

    private val logger = ULog.getLogger("BirdTaskAwardView")

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.view_bird_task_award, this)
    }

    var currentData: BirdAward? = null
    fun renderData(award: BirdAward?) {
        if (award == null) {
            return
        }
        currentData = award
        sdv_gift.loadImage(award.awardPic, 32f, 26f)
        tv_num.text = "x${award.awardCount}"
        when (award.awardStatus) {
            BirdTaskStatus.Received -> {
                masking.show()
                iv_finish.show()
                tv_receive.hide()
                this.isEnabled = false
            }
            BirdTaskStatus.NotReceive -> {
                masking.hide()
                iv_finish.hide()
                tv_receive.show()
                this.isEnabled = true
            }
            else -> {
                masking.hide()
                iv_finish.hide()
                tv_receive.hide()
                this.isEnabled = true
            }
        }

    }

}
