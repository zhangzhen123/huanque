package com.julun.huanque.core.ui.dynamic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.DynamicComment
import com.julun.huanque.common.bean.beans.DynamicDetailInfo
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.audio_record.AudioRecordManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.DynamicListAdapter
import com.julun.huanque.core.adapter.DynamicPhotosAdapter
import kotlinx.android.synthetic.main.activity_dynamic_details.*
import kotlinx.android.synthetic.main.layout_header_dynamic_detail.view.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/24 14:26
 *
 *@Description: DynamicDetailActivity 动态详情
 *
 */
@Route(path = ARouterConstant.DYNAMIC_DETAIL_ACTIVITY)
class DynamicDetailActivity : BaseVMActivity<DynamicDetailViewModel>() {

    companion object {
        fun start(activity: Activity, postId: Long) {
            val intent = Intent(activity, DynamicDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putLong(IntentParamKey.POST_ID.name, postId)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }

    private val headerLayout: View by lazy {
        LayoutInflater.from(this).inflate(R.layout.layout_header_dynamic_detail, null)
    }
    var postId: Long = 0L
    override fun getLayoutId(): Int = R.layout.activity_dynamic_details


    override fun setHeader() {
        headerPageView.initHeaderView(titleTxt = "动态", operateImg = R.mipmap.icon_more_black_01)
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        postId = intent.getLongExtra(IntentParamKey.POST_ID.name, 0L)
        if (postId == 0L) {
            ToastUtils.show2("无效的ID")
            finish()
        }
        initViewModel()
        headerLayout.hide()
        commentAdapter.addHeaderView(headerLayout)
        commentAdapter.headerWithEmptyEnable = true

        //预留一个很大的底部空白
        val foot = LayoutInflater.from(this).inflate(R.layout.view_bottom_holder, null)
        val lp = foot.findViewById<View>(R.id.view_id).layoutParams
        lp.height = ScreenUtils.getScreenHeight()
        commentAdapter.addFooterView(foot)
        commentAdapter.footerWithEmptyEnable = true
        rv_comments.layoutManager = LinearLayoutManager(this)
        rv_comments.adapter = commentAdapter
        mRefreshLayout.setOnRefreshListener {
            mViewModel.queryDetail(postId, QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initEvents(rootView: View) {
        headerPageView.imageViewBack.onClickNew {
            finish()
        }
        headerPageView.imageOperation.onClickNew {

        }

        rv_comments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isUserDo = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserDo = true
                }
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val viewHead: View? = linearLayoutManager.findViewByPosition(0)
                val view: View? = linearLayoutManager.findViewByPosition(1)

//                logger.info("viewHead=${viewHead?.id}  view=${view?.top} ")
                if (viewHead == null || view == null) {
                    if (!ic_sticky_comment.isVisible() && isUserDo) {
                        logger.info("此时需要显示粘性布局")
                        ic_sticky_comment.show()
                    }
                    return
                }
                if (viewHead == commentAdapter.headerLayout) {
                    val top = view.top

                    if (top <= dp2px(40)) {
                        ic_sticky_comment.show()
                    } else {
                        ic_sticky_comment.hide()
                    }
                }

            }
        })
    }


    private fun initViewModel() {
        mViewModel.dynamicDetailInfo.observe(this, Observer {
            if (it.isSuccess()) {
                renderData(it.requireT())
            } else {
                loadDataFail(it.isRefresh())
            }
            mRefreshLayout.isRefreshing = false
        })
        mViewModel.queryDetail(postId, QueryType.INIT)
    }

    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("刷新失败")
        } else {
            commentAdapter.loadMoreModule.loadMoreFail()
        }
    }

    private var isCommentMode: Boolean = false
    private fun renderData(info: DynamicDetailInfo) {

        if (info.post?.userId == SessionUtils.getUserId()) {
            headerPageView.textTitle.text = "我的动态"
        } else {
            headerPageView.textTitle.text = "Ta的动态"
        }

        val posterInfo = info.post
        if (posterInfo != null) {
            headerLayout.show()
            ImageHelper.setDefaultHeaderPic(headerLayout.header_pic, posterInfo.sex)
            headerLayout.header_pic.loadImage(posterInfo.headPic + BusiConstant.OSS_160, 46f, 46f)
            val name = if (posterInfo.nickname.length > 10) {
                "${posterInfo.nickname.substring(0, 10)}…"
            } else {
                posterInfo.nickname
            }
            val authTag = headerLayout.sd_auth_tag
            if (posterInfo.authMark.isNotEmpty()) {
                authTag.show()
                ImageUtils.loadImageWithHeight_2(authTag, posterInfo.authMark, dp2px(16))
            } else {
                authTag.hide()
            }
            headerLayout.tv_mkf_name.text = name
            headerLayout.tv_time.text = posterInfo.postTime
            headerLayout.tv_location.text = posterInfo.city
            if (posterInfo.city.isEmpty()) {
                headerLayout.tv_location.hide()
            } else {
                headerLayout.tv_location.show()
            }
            headerLayout.tv_dyc_content.text = posterInfo.content
            if (posterInfo.group == null) {
                headerLayout.tv_circle_name.hide()
            } else {
                headerLayout.tv_circle_name.show()
                headerLayout.tv_circle_name.text = posterInfo.group!!.groupName
            }

            val rl = posterInfo.pics.map { PhotoBean(url = it) }.toMutableList()
            val list = if (rl.size > 4) {
                rl.subList(0, 4)
            } else {
                rl
            }
            when {
                list.isNotEmpty() -> {

                    if (list.size == 1) {
                        headerLayout.rv_photos.hide()
                        headerLayout.sdv_photo.show()
                        val sgSdv = headerLayout.sdv_photo
                        val rvLp = sgSdv.layoutParams as ConstraintLayout.LayoutParams
                        val pic = list[0].url
                        val map = StringHelper.parseUrlParams(pic)
                        var h = map["h"]?.toIntOrNull() ?: DynamicListAdapter.SINGLE_PHOTO_DEFAULT
                        var w = map["w"]?.toIntOrNull() ?: DynamicListAdapter.SINGLE_PHOTO_DEFAULT
                        if (h > w) {
                            if (h > DynamicListAdapter.SINGLE_PHOTO_MAX_HEIGHT) {
                                w = w * DynamicListAdapter.SINGLE_PHOTO_MAX_HEIGHT / h
                                h = DynamicListAdapter.SINGLE_PHOTO_MAX_HEIGHT
                            } else if (h < DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE) {
                                //最小不能小于最小网格
//                            w = w * SINGLE_PHOTO_MINI_SIZE / h
//                            h = SINGLE_PHOTO_MINI_SIZE
                                w = DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE
                                h = DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE
                            }
                        } else {
                            if (w > DynamicListAdapter.SINGLE_PHOTO_MAX_WIDTH) {
                                h = DynamicListAdapter.SINGLE_PHOTO_MAX_WIDTH * h / w
                                w = DynamicListAdapter.SINGLE_PHOTO_MAX_WIDTH
                            } else if (h < DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE) {
                                //最小不能小于最小网格
//                            w = w * SINGLE_PHOTO_MINI_SIZE / h
//                            h = SINGLE_PHOTO_MINI_SIZE
                                w = DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE
                                h = DynamicListAdapter.SINGLE_PHOTO_MINI_SIZE
                            }
                        }
                        rvLp.height = h
                        rvLp.width = w
                        sgSdv.requestLayout()
                        sgSdv.loadImageInPx(pic, w, height = h)
                    } else {
                        headerLayout.rv_photos.show()
                        headerLayout.sdv_photo.hide()
                        val rv = headerLayout.rv_photos
                        rv.setHasFixedSize(true)
                        val rvLp = rv.layoutParams as ConstraintLayout.LayoutParams
                        if (list.size >= 4) {
                            rvLp.width = DynamicListAdapter.Width_4
                        } else {
                            rvLp.width = 0
                        }
                        val spanCount = when (list.size) {
//                        1 -> {
//                            1
//                        }
                            2, 3 -> {
                                3
                            }
                            4 -> {

                                2
                            }
                            else -> 3
                        }
                        rv.layoutManager = GridLayoutManager(this, spanCount)
                        if (rv.itemDecorationCount <= 0) {
                            rv.addItemDecoration(
                                GridLayoutSpaceItemDecoration2(DynamicListAdapter.space)
                            )
                        }

                        val mPhotosAdapter = DynamicPhotosAdapter()
                        rv.adapter = mPhotosAdapter

                        mPhotosAdapter.setList(list)
                        mPhotosAdapter.totalList = rl
                        mPhotosAdapter.setOnItemClickListener { adapter, view, position ->
                            //todo
                            logger.info("点击了第几个图片：$position")
                        }
                    }

                }
                else -> {
                    headerLayout.rv_photos.hide()
                    headerLayout.sdv_photo.hide()
                }
            }

        } else {
            headerLayout.hide()
        }


        val list = info.comments.distinct()
        commentAdapter.setList(list)

        rv_comments.post {
            rv_comments.scrollToPosition(0)
        }

        if (info.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (list.isEmpty()) {
                commentAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                commentAdapter.loadMoreModule.loadMoreComplete()
            }
        } else {
            //防止底部没有边距
            commentAdapter.loadMoreModule.loadMoreEnd()
        }
        if (commentAdapter.data.isEmpty()) {
            commentAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    this,
                    msg = "暂无评论，快去抢沙发吧～",
                    isImageHide = true
                ).apply {
                    val ll = this as LinearLayout
                    val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(150))
                    this.layoutParams = lp
                }
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        AudioRecordManager.destroy()
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {
                //由于上面在renderData中已经设置了空白页 这里不需要了
//                commentAdapter.setEmptyView(MixedHelper.getEmptyView(this))
            }
            NetStateType.LOADING -> {
                commentAdapter.setEmptyView(MixedHelper.getLoadingView(this))
            }
            NetStateType.ERROR -> {
                commentAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryDetail(postId, QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                commentAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            mViewModel.queryDetail(postId, QueryType.INIT)
                        })
                )

            }
        }

    }

    private val commentAdapter =
        object : BaseQuickAdapter<DynamicComment, BaseViewHolder>(R.layout.item_dynamic_comment_list),
            LoadMoreModule {
            override fun convert(holder: BaseViewHolder, item: DynamicComment) {
                logger.info("commentAdapter=${item.commentId}")
            }

        }


}