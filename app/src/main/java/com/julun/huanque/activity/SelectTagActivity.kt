package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.R
import com.julun.huanque.adapter.LoginTagTabAdapter
import com.julun.huanque.adapter.TagFragmentAdapter
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.LoginTagInfo
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.StatusBarUtil
import com.julun.huanque.viewmodel.SelectTagViewModel
import kotlinx.android.synthetic.main.act_select_tag.*
import java.lang.StringBuilder


/**
 *@创建者   dong
 *@创建时间 2020/12/23 13:47
 *@描述 选择标签页面
 */
class SelectTagActivity : BaseActivity() {
    private val mViewModel: SelectTagViewModel by viewModels()
    private val mTagAdapter = LoginTagTabAdapter()

    override fun getLayoutId() = R.layout.act_select_tag

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        StatusBarUtil.setColor(this, GlobalUtils.formatColor("#00FFFFFF"))

        mViewModel.loginTagData.value =
            intent?.getSerializableExtra(SelectSexActivity.LoginTagInfo) as? LoginTagInfo

        initViewModel()
        if (mViewModel.loginTagData.value == null) {
            //从远端获取数据
            mViewModel.getTagInfo()
        }
    }

    private fun initViewModel() {
        mViewModel.updateSelectCountFlag.observe(this, Observer {
            if (it == true) {
                mTagAdapter.notifyDataSetChanged()
            }
        })
        mViewModel.currentSelectIndex.observe(this, Observer {
            if (it != null) {
                if (it == mViewModel.totalTypeTabCount - 1) {
                    tv_next.text = "完成"
                } else {
                    tv_next.text = "下一步(${it + 1}/${mViewModel.totalTypeTabCount})"
                }

                val data = mTagAdapter.data
                data.forEachIndexed { index, tagTab ->
                    tagTab.selected = it == index
                }
                mTagAdapter.notifyDataSetChanged()
                view_pager2.currentItem = it
            }
        })

        mViewModel.loginTagData.observe(this, Observer {
            if (it != null) {
                showViewByData()
            }
        })
    }


    override fun initEvents(rootView: View) {
        tv_next.onClickNew {
            //下一步
            val currentIndex = mViewModel.currentSelectIndex.value ?: 0
            val done = currentIndex + 1 == mViewModel.totalTypeTabCount
            if (done) {
                //点击的完成
                tv_jump.performClick()
            } else {
                //点击下一步
                mViewModel.currentSelectIndex.value = currentIndex + 1
            }
        }

        tv_jump.onClickNew {
            //跳过
            val updateBean = mViewModel.loginTagData.value ?: return@onClickNew
            val idStr = StringBuilder()
            updateBean.tagTypeList.forEach { tabBean ->
                tabBean.tagList.forEach { tag ->
                    if (tag.selected) {
                        if (idStr.isNotEmpty()) {
                            idStr.append(",")
                        }
                        idStr.append("${tag.tagId}")
                    }
                }

            }
            if (idStr.isNotEmpty()) {
                mViewModel.saveTags(idStr.toString())
            }
            val intent = Intent(this, SelectSocialWishActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                val bundle = Bundle().apply {
                    putSerializable(SelectSexActivity.LoginTagInfo, updateBean)
                }
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView() {
        recycler_view.layoutManager =
            GridLayoutManager(this, mViewModel.loginTagData.value?.tagTypeList?.size ?: 3)
        recycler_view.adapter = mTagAdapter
        mTagAdapter.setOnItemClickListener { adapter, view, position ->
            mViewModel.currentSelectIndex.value = position
        }
    }

    /**
     * 初始化ViewPager2
     */
    private fun initViewPager2(typeList: MutableList<String>) {
        view_pager2.isUserInputEnabled = false
        view_pager2.adapter = TagFragmentAdapter(this, typeList)
        view_pager2.offscreenPageLimit = typeList.size
    }

    /**
     * 显示数据
     */
    private fun showViewByData() {
        mViewModel.loginTagData.value?.let {
            initRecyclerView()
            val tagTabList = it.tagTypeList
            if (ForceUtils.isIndexNotOutOfBounds(0, tagTabList)) {
                tagTabList[0].selected = true
                mTagAdapter.setList(tagTabList)
            }
            val typeList = mutableListOf<String>()
            tagTabList.forEach { tabBean ->
                typeList.add(tabBean.tagType)
            }
            initViewPager2(typeList)
            mViewModel.totalTypeTabCount = typeList.size
            mViewModel.currentSelectIndex.value = 0
        }

    }

    override fun onBackPressed() {
//        super.onBackPressed()
    }
}