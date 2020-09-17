package com.julun.huanque.core.ui.live.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.events.HideBirdEvent
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.main.bird.LeYuanFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BirdDialogFragment : BaseDialogFragment() {


    private val birdFragment: LeYuanFragment by lazy { LeYuanFragment.newInstance() }
    override fun getLayoutId(): Int {
        return R.layout.fragment_le_yuan_bird
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(gravity = Gravity.CENTER,width = ViewGroup.LayoutParams.MATCH_PARENT, height = ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun initViews() {

        initViewModel()
        val transition = childFragmentManager.beginTransaction()
        transition.replace(R.id.fragment_container, birdFragment).commit()
    }

    private fun initViewModel() {
    }

    override fun reCoverView() {
        initViewModel()
    }

    override fun isRegisterEventBus(): Boolean {
        return true
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun close(event:HideBirdEvent){
        dismiss()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(DialogFragment.STYLE_NO_FRAME, com.julun.huanque.common.R.style.DialogTransparent)
    }
}