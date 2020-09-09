package com.julun.huanque.core.ui.main.bird

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.BirdAward
import com.julun.huanque.common.bean.beans.BirdTaskInfo
import com.julun.huanque.common.bean.beans.PkPropInfo
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_bird_tasks.*
import kotlinx.android.synthetic.main.view_bird_task_award.view.*
import kotlinx.android.synthetic.main.view_pk_prop.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import java.util.concurrent.TimeUnit

/**
 * [leYuanViewModel]将主面板的viewModel传过来 供商店调用
 */
class BirdTaskDialogFragment(private val leYuanViewModel: LeYuanViewModel) : BaseVMDialogFragment<BirdTaskViewModel>() {


    private val taskAdapter: BirdTaskAdapter by lazy { BirdTaskAdapter() }
    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_tasks
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 500)
    }

    override fun initViews() {
        initViewModel()
        tasksList.layoutManager = LinearLayoutManager(requireContext())
        tasksList.adapter = taskAdapter
        ivClose.onClickNew {
            dismiss()
        }
        taskAdapter.setOnItemChildClickListener { _, _, position ->
            val item = taskAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            when (item.taskStatus) {
                BirdTaskStatus.NotFinish -> {
                    when (item.jumpType) {
                        BirdTaskJump.FriendHome -> {
                            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                                .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
                        }
                        BirdTaskJump.InviteFriend -> {
                            RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
                        }

                        BirdTaskJump.LiveRoom -> {
                            ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).navigation()
                        }
                        BirdTaskJump.Message -> {
                            ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                                .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX).navigation()
                        }

                    }
                    this@BirdTaskDialogFragment.dismiss()
                }
                BirdTaskStatus.NotReceive -> {
                    mViewModel.receiveTask(item.taskCode)
                }
            }

        }
        mRefreshLayout.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }

        award_01.onClickNew {
            logger.info("award_01")

        }
        award_02.onClickNew {
            logger.info("award_02")
        }
        award_03.onClickNew {
            logger.info("award_03")
        }
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
                MyAlertDialog(requireActivity()).showAlertWithOK(
                    "恭喜您成功领取了${it.requireT().awardCoins}金币",
                    title = "领取成功",
                    okText = "知道了"
                )
                mViewModel.queryInfo(QueryType.REFRESH)
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
        view_process_bg.requestLayout()
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

    fun renderData(award: BirdAward?) {
        if (award == null) {
            return
        }
        sdv_gift.loadImage(award.awardPic, 32f, 26f)
        tv_num.text = "x${award.awardCount}"
        when (award.awardStatus) {
            BirdTaskStatus.Received -> {
                masking.show()
                iv_finish.show()
                tv_num.hide()
                this.isEnabled = false
            }
            else -> {
                masking.hide()
                iv_finish.hide()
                tv_num.show()
                this.isEnabled = true
            }
        }

    }
}
