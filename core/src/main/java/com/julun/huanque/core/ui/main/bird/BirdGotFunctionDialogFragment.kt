package com.julun.huanque.core.ui.main.bird

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.FunctionBird
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_bird_got_func_bird.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/10 10:35
 *
 *@Description: 获取到功能鹊的弹窗
 *
 * 修改 也支持解锁显示
 *
 */
class BirdGotFunctionDialogFragment : BaseDialogFragment() {

    private var currentBird: FunctionBird? = null

    companion object {
        fun newInstance(bird: FunctionBird): BirdGotFunctionDialogFragment {
            val args = Bundle()
            args.putSerializable("bird", bird)
            val fragment = BirdGotFunctionDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_got_func_bird
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(gravity = Gravity.CENTER, marginWidth = 45, height = ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun setBird(bird: FunctionBird) {
        arguments?.putSerializable("bird", bird)
    }

    override fun initViews() {
        renderData()
        tv_ok.onClickNew {
            dismiss()
        }
    }

    private fun renderData() {
        val birdDes = arguments?.getSerializable("bird") as? FunctionBird
        currentBird = birdDes
        birdDes?.let { bird ->
            sdv_bird.loadImage(bird.functionIcon, 80f, 80f)
            if(bird.level!=null){
                tv_name.text = "Lv${bird.level} "+bird.functionName
            }else{
                tv_name.text = bird.functionName
            }
        }
    }

    override fun reCoverView() {
        renderData()
    }


    override fun onDestroy() {
        super.onDestroy()
    }


}