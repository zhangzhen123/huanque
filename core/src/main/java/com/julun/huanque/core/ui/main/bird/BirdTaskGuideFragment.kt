package com.julun.huanque.core.ui.main.bird

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseVMDialogFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.BirdLiveAward
import com.julun.huanque.common.bean.beans.TaskGuideInfo
import com.julun.huanque.common.bean.events.HideBirdEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.loadImageLocal
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.fragment_bird_guide_task.*
import org.greenrobot.eventbus.EventBus

/**
 *
 */
class BirdTaskGuideFragment : BaseVMDialogFragment<BirdTaskViewModel>() {

    companion object {
        fun newInstance(task: TaskGuideInfo, programId: Long?): BirdTaskGuideFragment {
            val args = Bundle()
            args.putSerializable("task", task)
            if (programId != null)
                args.putLong("programId", programId)
            val fragment = BirdTaskGuideFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun setTask(task: TaskGuideInfo) {
        arguments?.putSerializable("task", task)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_guide_task
    }


    override fun configDialog() {
        setDialogSize(gravity = Gravity.CENTER, marginWidth = 45, height = ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    private var currentTask: TaskGuideInfo? = null
    private var programId: Long? = null
    private var isInLivePage = false
    override fun initViews() {
        isInLivePage = requireActivity() is PlayerActivity
        initViewModel()
        currentTask = arguments?.getSerializable("task") as? TaskGuideInfo
        programId = arguments?.getLong("programId")
        renderData(currentTask)
        close.onClickNew {
            dismiss()
        }
        tv_ok.onClickNew {
            val item = currentTask ?: return@onClickNew

            when (item.jumpType) {
                BirdTaskJump.FriendHome -> {
                    this@BirdTaskGuideFragment.dismiss()
                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                        .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
                }
                BirdTaskJump.InviteFriend -> {
                    this@BirdTaskGuideFragment.dismiss()
                    RNPageActivity.start(requireActivity(), RnConstant.INVITE_FRIENDS_PAGE)
                }

                BirdTaskJump.LiveRoom -> {
                    mViewModel.randomLive(item.taskCode, programId)
                }
                BirdTaskJump.Message -> {
                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                        .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MESSAGE_FRAGMENT_INDEX).navigation()
                    this@BirdTaskGuideFragment.dismiss()
                    EventBus.getDefault().post(HideBirdEvent())
                }
                BirdTaskJump.AnonyVoiceCall -> {
                    ARouter.getInstance().build(ARouterConstant.ANONYMOUS_VOICE_ACTIVITY).navigation()
                    this@BirdTaskGuideFragment.dismiss()
                    EventBus.getDefault().post(HideBirdEvent())

                }
                BirdTaskJump.Recharge -> {
                    ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
                    this@BirdTaskGuideFragment.dismiss()
                    EventBus.getDefault().post(HideBirdEvent())

                }

            }

        }


    }

    override fun reCoverView() {
        initViewModel()
        currentTask = arguments?.getSerializable("task") as? TaskGuideInfo
        renderData(currentTask)
    }

    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(R.style.dialog_center_open_ani)
    }

    private fun renderData(data: TaskGuideInfo?) {
        data ?: return
        tv_title.text = data.taskGuideName
        tv_desc.text = data.taskGuideDesc
        tv_ok.text = data.jumpTypeText
        when (data.awardType) {
            BirdTaskAwardType.Small -> {
                sdv_coins.loadImageLocal(R.mipmap.icon_bird_coin_little)
            }
            BirdTaskAwardType.Middle -> {
                sdv_coins.loadImageLocal(R.mipmap.icon_bird_coin_middle)
            }
            BirdTaskAwardType.Big -> {
                sdv_coins.loadImageLocal(R.mipmap.icon_bird_coin_big)
            }
        }
    }

    private fun initViewModel() {

        mViewModel.mRandomRoom.observe(this, Observer {
            if (it.isSuccess()) {
                val item = currentTask ?: return@Observer
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


    override fun showLoadState(state: NetState) {
    }

}

