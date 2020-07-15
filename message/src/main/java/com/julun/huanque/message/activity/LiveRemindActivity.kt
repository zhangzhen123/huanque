package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.constant.ActivityCodes
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.LiveRemindViewModel
import kotlinx.android.synthetic.main.activity_live_remind.*
import org.jetbrains.anko.actionMenuView

/**
 * 开播提醒
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/15 0015
 */
class LiveRemindActivity : BaseActivity() {

    private var mViewModel:LiveRemindViewModel?=null

    private var mIsChange: Boolean = false

    override fun getLayoutId(): Int = R.layout.activity_live_remind

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        prepareViewModel()
    }

    override fun initEvents(rootView: View) {
        ivback.onClickNew {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (mIsChange) {
            setResult(ActivityCodes.RESPONSE_CODE_REFRESH)
        }
        super.onBackPressed()
    }

    private fun prepareViewModel(){
        mViewModel = ViewModelProvider(this).get(LiveRemindViewModel::class.java)
        mViewModel?.loadState?.observe(this, Observer {
            it?:return@Observer
            when(it.state){
                NetStateType.LOADING ->{
                    //加载中
                }
                NetStateType.SUCCESS ->{
                    //成功
                }
                NetStateType.IDLE->{
                    //闲置，什么都不做
                }
                else ->{
                    //都是异常

                }
            }
        })
    }
}
