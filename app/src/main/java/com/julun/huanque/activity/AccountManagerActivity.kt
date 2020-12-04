package com.julun.huanque.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.R
import com.julun.huanque.adapter.AccountAdapter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.SingleAccount
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.support.LoginManager
import com.julun.huanque.viewmodel.AccountManagerViewModel
import kotlinx.android.synthetic.main.act_account_manager.*


/**
 *@创建者   dong
 *@创建时间 2020/12/2 17:16
 *@描述 账号管理页面
 */
class AccountManagerActivity : BaseActivity() {
    companion object {
        //跳转创建账号页面的code
        const val CreateAccountCode = 0x1111
    }

    private val mViewModel: AccountManagerViewModel by viewModels()

    private val mAdapter = AccountAdapter()

    override fun getLayoutId() = R.layout.act_account_manager

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        header_page.textTitle.text = "账号管理"
        initRecyclerView()
        initViewModel()

        mViewModel.queryAccountList()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
        view_add.onClickNew {
            //创建分身
            val bean = mViewModel.accountBeanData.value ?: return@onClickNew
            if (bean.canAdd == BusiConstant.True) {
                //可以创建分身,跳转创建分身页面
                val intent = Intent(this, CreateAccountActivity::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    startActivityForResult(intent, CreateAccountCode)
                }
            } else {
                //不可以创建分身
                ToastUtils.show(bean.reason)
            }
        }
    }

    private fun initViewModel() {
        mViewModel.accountBeanData.observe(this, Observer {
            if (it != null) {
                mAdapter.setList(it.subList)
            }
        })
        mViewModel.loginSuccessData.observe(this, Observer {
            if (it == true) {
                mAdapter.notifyDataSetChanged()
            }
        })
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val account = adapter.getItemOrNull(position) as? SingleAccount ?: return@setOnItemClickListener
            if (account.userId != SessionUtils.getUserId()) {
                //登录分身账号
                mViewModel.accountLogin(account.userId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CreateAccountCode && resultCode == Activity.RESULT_OK) {
            mViewModel.queryAccountList()
        }
    }

}