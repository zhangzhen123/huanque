package com.julun.huanque.core.ui.main.bird

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.FunctionBirdDes
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_bird_function.*
import org.jetbrains.anko.backgroundResource

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/10 10:35
 *
 *@Description: 说明页
 *
 */
class BirdFunctionDialogFragment : BaseDialogFragment() {

    private val mViewModel by activityViewModels<BirdFunctionViewModel>()
    private var currentBirdDes: FunctionBirdDes? = null

    companion object {
        fun newInstance(bird: FunctionBirdDes): BirdFunctionDialogFragment {
            val args = Bundle()
            args.putSerializable("bird", bird)
            val fragment = BirdFunctionDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_function
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(gravity = Gravity.CENTER, marginWidth = 45, height = ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun setBird(bird: FunctionBirdDes) {
        arguments?.putSerializable("bird", bird)
    }
    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(R.style.dialog_center_open_ani)
    }
    override fun initViews() {
        renderData()
        initViewModel()
        ivClose.onClickNew {
            dismiss()
        }
        tv_bird_fly.onClickNew {
            currentBirdDes?.let { bird ->
                tv_bird_fly.isEnabled = false
                mViewModel.fly(bird.type)
            }
        }
    }

    fun renderData() {
        val birdDes = arguments?.getSerializable("bird") as? FunctionBirdDes
        currentBirdDes = birdDes
        birdDes?.let { bird ->
            bird.birdPic?.let {
                sdv_bird.loadImage(it, 80f, 80f)
            }
            tv_name.text = bird.birdName
            tv_desc.text = bird.bFunction
            tv_source.text = bird.source
            if (bird.num != null && bird.num!! > 0) {
//                tv_bird_fly.isEnabled = true
                tv_bird_fly.backgroundResource = R.mipmap.bg_bird_btn_green

            } else {
//                tv_bird_fly.isEnabled = false
                tv_bird_fly.backgroundResource = R.mipmap.bg_bird_btn_enable
            }
        }
    }

    override fun reCoverView() {
        renderData()
        initViewModel()
    }

    private fun initViewModel() {
        mViewModel.flyResult.observe(this, Observer {
            it ?: return@Observer
            if (it.isSuccess()) {
                dismiss()
                when (it.requireT().resultType) {
                    "Function" -> {
                        val mBirdGotFunctionDialogFragment = BirdGotFunctionDialogFragment.newInstance(it.requireT().functionInfo?:return@Observer)
                        mBirdGotFunctionDialogFragment.show(requireActivity(), "BirdGotFunctionDialogFragment")
                    }
                    "Cash" -> {
                        val content = "恭喜您成功领取了\n ${it.requireT().cash}元零钱"
                        val dialog = BirdGotMoneyDialogFragment.newInstance(content)
                        dialog.show(requireActivity(), "BirdGotMoneyDialogFragment")
                    }
                }

            }
            tv_bird_fly.isEnabled = true
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.flyResult.value = null
    }


}