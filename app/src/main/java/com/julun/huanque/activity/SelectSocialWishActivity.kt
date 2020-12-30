package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.julun.huanque.R
import com.julun.huanque.adapter.SocialWishAdapter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.LoginTagInfo
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.viewmodel.SelectSocialWishViewModel
import kotlinx.android.synthetic.main.act_select_social_wish.*
import java.lang.StringBuilder


/**
 *@创建者   dong
 *@创建时间 2020/12/23 19:27
 *@描述 选择社交意愿页面
 */
class SelectSocialWishActivity : BaseActivity() {
    private val mViewModel: SelectSocialWishViewModel by viewModels()
    private val mAdapter = SocialWishAdapter()

    override fun getLayoutId() = R.layout.act_select_social_wish

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        StatusBarUtil.setColor(this, GlobalUtils.formatColor("#00FFFFFF"))


        mViewModel.loginTagData.value =
            intent?.getSerializableExtra(SelectSexActivity.LoginTagInfo) as? LoginTagInfo
        mAdapter.setList(mViewModel.loginTagData.value?.socialWishList)

        initRecyclerView()
    }

    override fun initEvents(rootView: View) {
        tv_jump.onClickNew {
            val wishIds = StringBuilder()
            mViewModel.loginTagData.value?.socialWishList?.forEach { wishBean ->
                if (wishIds.isNotEmpty()) {
                    wishIds.append(",")
                }
                wishIds.append(wishBean.wishType)
            }
            jumpMainActivity(wishIds.toString())
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = mAdapter
        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            if (view.id == R.id.view_social) {
                //点击了文本,跳转首页
                val type =
                    mAdapter.getItemOrNull(position)?.wishType ?: return@setOnItemChildClickListener
                jumpMainActivity(type)
            }
        }
    }

    /**
     * 跳转首页
     */
    private fun jumpMainActivity(wishId: String) {
        if (wishId.isNotEmpty()) {
            //调用接口
            mViewModel.saveWish(wishId)
        }
        val intent = Intent(this, MainActivity::class.java)
        if (ForceUtils.activityMatch(intent)) {
            SessionUtils.setWishComplete(BusiConstant.True)
            startActivity(intent)
            finish()
        }
    }


    override fun onBackPressed() {
//        super.onBackPressed()
    }
}