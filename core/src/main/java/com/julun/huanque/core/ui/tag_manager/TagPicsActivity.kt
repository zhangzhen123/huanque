package com.julun.huanque.core.ui.tag_manager

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.bean.beans.TagDetailBean
import com.julun.huanque.common.bean.beans.TagPicBean
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.ManagerTagCode
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.StaggeredDecoration
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_tag_pics.*
import kotlinx.android.synthetic.main.activity_tag_pics.rv_pics
import kotlinx.android.synthetic.main.item_user_swip_card.*
import org.jetbrains.anko.startActivity

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/29 10:16
 *
 *@Description: TagPicsActivity 指定标签的图片集合
 *
 */
@Route(path = ARouterConstant.TAG_PICS_ACTIVITY)
class TagPicsActivity : BaseVMActivity<TagPicsViewModel>() {


    companion object {
        fun start(act: Activity, tag: UserTagBean, likeUserId: Long? = null) {
            act.startActivity<TagPicsActivity>(ManagerTagCode.TAG_INFO to tag, IntentParamKey.USER_ID.name to likeUserId)
        }

        val width = (ScreenUtils.getScreenWidth() - dp2px(25)) / 2
    }

    private var tagManagerViewModel = HuanViewModelManager.tagManagerViewModel

    override fun getLayoutId(): Int = R.layout.activity_tag_pics

    private val picListAdapter: BaseQuickAdapter<TagPicBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<TagPicBean, BaseViewHolder>(R.layout.item_tag_pic_list), LoadMoreModule {
            override fun convert(holder: BaseViewHolder, item: TagPicBean) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                val sdvLp = sdv.layoutParams as FrameLayout.LayoutParams
                logger.info("holder.adapterPosition =${holder.adapterPosition}")
                val w = width
                val h = if (holder.adapterPosition == 1) {
                    width
                } else {
                    (width * 1.411).toInt()
                }
//                sdvLp.width = w
                sdvLp.height = h
                sdv.requestLayout()
                sdv.loadImageInPx(item.applyPic, w, h)
                if (item.picNum == 1) {
                    holder.setGone(R.id.iv_top_tag, true)
                } else {
                    holder.setGone(R.id.iv_top_tag, false)
                }
            }

        }
    }

    private var currentTag: UserTagBean? = null
    private var currentLikeUserId: Long? = null
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        currentTag = intent.getSerializableExtra(ManagerTagCode.TAG_INFO) as? UserTagBean
        currentLikeUserId = intent.getLongExtra(IntentParamKey.USER_ID.name, 0L)
        if (currentLikeUserId == 0L) {
            currentLikeUserId = null
        }
        ivback.onClickNew {
            finish()
        }
        tvTitle.text = currentTag?.tagName ?: ""

        // rv_pics.layoutManager = GridLayoutManager(this, 2)
        rv_pics.addItemDecoration(StaggeredDecoration(dp2px(5)))
        rv_pics.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        initViewModel()
        ivOperation.onClickNew {
            switchLike()
        }
//        tv_like.onClickNew {
//            switchLike()
//        }
        rv_pics.adapter = picListAdapter


        picListAdapter.setOnItemClickListener { _, _, position ->
            val item = picListAdapter.getItemOrNull(position)
            if (item != null) {
                logger.info("查看大图")
                TagUserPicsActivity.start(this, item.tagId, item.userId)
            }
        }

        picListAdapter.loadMoreModule.setOnLoadMoreListener {
            queryData(QueryType.LOAD_MORE)
        }

        mRefreshLayout.setOnRefreshListener {
            queryData(QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)
        queryData(QueryType.INIT)

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        tv_like_bottom.onClickNew {
            ivOperation.performClick()
        }
    }

    private fun switchLike() {
        val detail = mViewModel.tagDetail.value?.getT() ?: return
        if (detail.like) {
            tagManagerViewModel.tagCancelLike(UserTagBean(tagId = detail.tagId, tagName = detail.tagName))
        } else {
            tagManagerViewModel.tagLike(UserTagBean(tagId = detail.tagId, tagName = detail.tagName))
        }

    }

    private fun initViewModel() {
        mViewModel.tagDetail.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderData(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail(it.isRefresh())
            }
            mRefreshLayout.isRefreshing = false
        })
        tagManagerViewModel.tagChangeStatus.observe(this, Observer {
            if (it.isSuccess()) {


                if (it.isNew() && it.requireT().like) {
                    ToastUtils.showToastCustom(R.layout.layout_toast_tag, action = { view, t ->
                        val tv = view.findViewById<TextView>(R.id.toastContent)
                        t.duration = Toast.LENGTH_SHORT
                        t.setGravity(Gravity.CENTER, 0, 0)
                        tv.text = "已喜欢"
                    })
                }
                val tag = it.requireT()
                if (tag.tagId != currentTag?.tagId) {
                    return@Observer
                }
                val detail = mViewModel.tagDetail.value?.getT() ?: return@Observer
                detail.like = tag.like
                if (tag.like) {
                    ivOperation.setImageResource(R.mipmap.icon_tag_like_02)
//                    tv_like.text = "取消喜欢"
                    showBottomView(true)
                } else {
                    ivOperation.setImageResource(R.mipmap.icon_tag_dislike_02)
//                    tv_like.text = "喜欢"
                    showBottomView(false)
                }

            } else if (it.state == NetStateType.ERROR) {
//                if (it.isNew()) {
//                    ToastUtils.show("网络异常")
//                }
            }
        })
    }

    /**
     * 显示底部视图
     */
    private fun showBottomView(like: Boolean) {
        if (like) {
            tv_like_bottom.hide()
            view_bottom.hide()
        } else {
            tv_like_bottom.show()
            view_bottom.show()
        }
    }


    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("加载失败")
        } else {
            picListAdapter.loadMoreModule.loadMoreFail()
        }
    }


    private fun renderData(listData: TagDetailBean) {

        val list = listData.authPage.list

        if (listData.authPage.isPull) {
//            sdv_tag.loadImage(listData.tagIcon, 15f, 15f)
//            tv_tag.text = listData.tagName
            tv_like_bottom.text = "喜欢${listData.tagName}"
            tvTitle.text=listData.tagName
            tv_num.text = "(认证${listData.authNum}人)"
//            tv_desc.text = listData.tagDesc
            showBottomView(listData.like)
            if (listData.like) {
                ivOperation.setImageResource(R.mipmap.icon_tag_like_02)
//                tv_like.text = "取消喜欢"
            } else {
                ivOperation.setImageResource(R.mipmap.icon_tag_dislike_02)
//                tv_like.text = "喜欢"
            }
            val programList = list.distinct()
            picListAdapter.setList(programList)

            rv_pics.post {
                rv_pics.scrollToPosition(0)
            }
        } else {
            val programList = list.removeDuplicate(picListAdapter.data)
            picListAdapter.addData(programList)
        }
        if (listData.authPage.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (list.isEmpty()) {
                picListAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                picListAdapter.loadMoreModule.loadMoreComplete()
            }
        } else {
            //防止底部没有边距
            picListAdapter.loadMoreModule.loadMoreEnd()
        }
        if (picListAdapter.data.isEmpty()) {
            picListAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    this,
                    msg = "暂无数据"/* ,
                    btnTex = "前往",
                   onClick = View.OnClickListener {
                        logger.info("跳转到交友")
                        ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                            .withInt(IntentParamKey.TARGET_INDEX.name, 0).navigation()
                    }*/
                )
            )
        }
    }

    private fun queryData(type: QueryType) {
        val tag = currentTag ?: return
        mViewModel.requestTagList(type, tag.tagId, currentLikeUserId)
    }

    override fun showLoadState(state: NetState) {

        when (state.state) {
            NetStateType.SUCCESS -> {
                //由于上面在renderData中已经设置了空白页 这里不需要了
//                picListAdapter.setEmptyView(MixedHelper.getEmptyView(this))
//                ct_head_layout.show()
            }
            NetStateType.LOADING -> {
                picListAdapter.setEmptyView(MixedHelper.getLoadingView(this))
            }
            NetStateType.ERROR -> {
//                ct_head_layout.hide()
                picListAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                picListAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            queryData(QueryType.INIT)
                        })
                )

            }
        }


    }

}