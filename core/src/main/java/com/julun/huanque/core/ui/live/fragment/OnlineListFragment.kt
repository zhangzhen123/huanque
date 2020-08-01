package com.julun.huanque.core.ui.live.fragment

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.collection.LruCache
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.OnlineListData
import com.julun.huanque.common.bean.beans.OnlineUserInfo
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.TabTags
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration
import com.julun.huanque.common.widgets.refreshlayout.RefreshListener
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.OnLineViewModel
import kotlinx.android.synthetic.main.fragment_online_list.*
import org.jetbrains.anko.startActivity
import java.lang.ref.SoftReference

/**
 * 在线列表
 * @author WanZhiYuan
 * @since 4.24
 */
class OnlineListFragment : BaseVMFragment<OnLineViewModel>() {

    private val mPlayerViewModel: PlayerViewModel by activityViewModels()
    private var mHeadAdapter: OnlineHeadAdapter? = null
    private var mCurrGuardData: OnlineListData<OnlineUserInfo>? = null

    private val mHeadView: View by lazy {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_online_royal_head, null)
        val listView = mHeadView.findViewById<RecyclerView>(R.id.rvHeadList)
        listView?.layoutManager = GridLayoutManager(activity, 4)
        listView?.addItemDecoration(GridLayoutSpaceItemDecoration(dp2px(12f)))
        view
    }

    private var mPageTag: String? = null
    private var mRoyalUrl: String? = null
    private var mDesText: String? = null

    companion object {
        /**
         * @param position index
         * @param title pageName [TabTags]
         */
        fun newInstance(position: Int, title: String): OnlineListFragment {
            val fragment = OnlineListFragment()
            val bundle = Bundle()
            bundle.putString(IntentParamKey.PAGE_NAME.name, title)
            bundle.putInt(IntentParamKey.POSITION.name, position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun lazyLoadData() {
        loadData(QueryType.INIT)
    }

    override fun getLayoutId(): Int = R.layout.fragment_online_list
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mPageTag = arguments?.getString(IntentParamKey.PAGE_NAME.name, "")
        if (adapter.headerLayoutCount > 0) {
            adapter.removeAllHeaderView()
        }
        if (mPageTag == TabTags.TAB_TAG_ROYAL) {
            mHeadAdapter = OnlineHeadAdapter()
        }
        rvList.layoutManager = LinearLayoutManager(context)
        rvList.adapter = adapter
        prepareViewModel()
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {
                adapter.setEmptyView(MixedHelper.getEmptyView(requireContext()))
            }
            NetStateType.LOADING -> {
                adapter.setEmptyView(MixedHelper.getLoadingView(requireContext()))
            }
            NetStateType.ERROR -> {
                adapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = requireContext(),
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                adapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = requireContext(),
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            loadData(QueryType.INIT)
                        })
                )

            }
        }

    }

    override fun initEvents(rootView: View) {
        lmrlRefreshView.onRefreshListener = object : RefreshListener {
            override fun onRefresh() {
                //下拉刷新
                loadData(QueryType.REFRESH)
            }
        }
        adapter.loadMoreModule.setOnLoadMoreListener {
            //加载更多
            loadData(QueryType.LOAD_MORE)
        }
        mHeadAdapter?.onAdapterClickNew { adapter, _, position ->
            //点击贵宾席位item
            val item = adapter?.getItem(position)
            item ?: return@onAdapterClickNew
            if (item is OnlineUserInfo) {
                if (item.userId == -1L) {
                    //跳转贵族特权
                    if (TextUtils.isEmpty(mRoyalUrl)) {
                        return@onAdapterClickNew
                    }
                    activity?.startActivity<WebActivity>(
                        BusiConstant.WEB_URL to mRoyalUrl
                    )
                } else {
                    //todo
                }
            }
        }
        adapter.onAdapterClickNew { adapter, _, position ->
            //点击普通item
            val item = adapter?.getItem(position)
            item ?: return@onAdapterClickNew
            if (item is OnlineUserInfo) {
                //todo
            }
        }
    }

    private fun prepareViewModel() {
        mViewModel.listResult.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                refreshData(it.getT())
            } else if (it.state == NetStateType.ERROR) {
                ToastUtils.show("网络出现了问题~")
                if (it.getT().isPull) {

                } else {
                    adapter.loadMoreModule.loadMoreFail()
                }
            }
            lmrlRefreshView.setRefreshFinish()

        })
        mViewModel.guardSuccess.observe(this, Observer {
            it ?: return@Observer
//            if (it) {
//                if ((mPlayerViewModel?.experienceGuardTime?.value
//                        ?: 0L) > 0L
//                ) {
//                    //属于体验守护
//                    mPlayerViewModel?.stopGuardCountdown()
//                }
//                //刷新余额
//                RechargeViewModel.synchronousBalance(activity ?: return@Observer)
//                mPlayerViewModel?.closeDialog?.value = NewOnlineDialogFragment::class.java
//            }
        })
        if (mPageTag == TabTags.TAB_TAG_GUARD) {
            mPlayerViewModel?.experienceGuardTime?.observe(this, Observer {
//                it ?: return@Observer
//                if (mPlayerViewModel?.loginSuccessData?.value?.anchor != null) {
//                    //当前用户身份是主播
//                    return@Observer
//                }
//                //体验守护倒计时
//                if (!mHasLoadedOnce) {
//                    return@Observer
//                }
//                if (mPageTag == TabTags.TAB_TAG_GUARD) {
//                    if (it > 0L) {
//                        setNormalBottomView(false, it)
//                    } else {
////                        setNormalBottomView(true)
//                        mPlayerViewModel?.experienceGuardTime?.value = null
////                        mPlayerViewModel?.closeDialog?.value = DialogTypes.DIALOG_ROYAL
//                        mPlayerViewModel?.closeDialog?.value = NewOnlineDialogFragment::class.java
//                    }
//                }
            })
        }
    }

    private fun setHeadViews(data: OnlineListData<OnlineUserInfo>, hideHead: Boolean = false) {
        if (mPageTag != TabTags.TAB_TAG_ROYAL && mPageTag != TabTags.TAB_TAG_GUARD) {
            //不是贵族或者守护弹窗就不需要头布局
            if (adapter.headerLayoutCount > 0) {
                adapter.removeAllHeaderView()
            }
            return
        }
        if (hideHead) {
            if (adapter.headerLayoutCount > 0) {
                adapter.removeAllHeaderView()
            }
            return
        }
        if (adapter.headerLayoutCount <= 0) {
            adapter.addHeaderView(mHeadView)
        }
        val headTitle = mHeadView.findViewById<View>(R.id.tvHeadTitle)
        val listView = mHeadView.findViewById<RecyclerView>(R.id.rvHeadList)
        val headBottom = mHeadView.findViewById<View>(R.id.tvHeadBottom)

        val picture = mHeadView.findViewById<SimpleDraweeView>(R.id.sdvPicture)
        var isLoadPicture = false
        when (mPageTag) {
            TabTags.TAB_TAG_ROYAL -> {
                //贵族头布局配置
                if (data.isPull) {
                    //刷新页面如果没有贵宾和贵族数据，那么就展示贵族特权图片
                    if (data.royalHonorList?.isEmpty() == true && data.list.isEmpty()) {
                        headTitle?.hide()
                        listView?.hide()
                        headBottom?.hide()
                        picture?.show()
                        isLoadPicture = true
                    } else {
                        headTitle?.show()
                        listView?.show()
                        headBottom?.show()
                        picture?.hide()
                    }
                } else {
                    //加载更多贵宾数据去重并刷新
                    data.royalHonorList = mHeadAdapter?.data?.mergeNoDuplicateNew(
                        data.royalHonorList
                            ?: arrayListOf()
                    )
                        ?: arrayListOf()
                }
                when {
                    data.royalHonorList?.isEmpty() == true -> {
                        //没有贵宾数据，伪造凑够至少一排，一排四个的规则
                        for (i in 0 until 4) {
                            if (data.royalHonorList is ArrayList) {
                                (data.royalHonorList as? ArrayList<OnlineUserInfo>)?.add(OnlineUserInfo())
                            }
                        }
                    }
                    (data.royalHonorList?.size ?: 0) % 4 != 0 -> {
                        //需要补齐一排四个的规则
                        val count = (data.royalHonorList?.size ?: 0) % 4
                        for (i in count until 4) {
                            if (data.royalHonorList is ArrayList) {
                                (data.royalHonorList as? ArrayList<OnlineUserInfo>)?.add(OnlineUserInfo())
                            }
                        }
                    }
                    //剩下的表示刚刚好每一排四个item，直接展示就行了
                }
            }
            TabTags.TAB_TAG_GUARD -> {
                //守护头布局配置
                if (data.list.isNotEmpty()) {
                    if (adapter.headerLayoutCount > 0) {
                        adapter.removeAllHeaderView()
                    }
                    return
                }
                headTitle?.hide()
                listView?.hide()
                headBottom?.hide()
                picture?.show()
                isLoadPicture = true
            }
        }
        if (isLoadPicture) {
            //加载图片
            var imageUrl = if (mPageTag == TabTags.TAB_TAG_ROYAL) {
                //贵族特权图片地址
                data.privilegeUrl ?: ""
            } else {
                //守护特权图片地址
                data.guardUrl ?: ""
            }
            if (!TextUtils.isEmpty(imageUrl)) {
                //加载图片前清理之前缓存的图片资源，因为这个图片有可能后端更换了，但是地址并没有变
                val imagePipeline = Fresco.getImagePipeline()
                val uri = Uri.parse(StringHelper.getOssImgUrl(imageUrl))
                val inMemoryCache = imagePipeline.isInBitmapMemoryCache(uri)
                if (inMemoryCache) {
                    imagePipeline?.evictFromCache(uri)
                }
                ImageUtils.loadImageWithWidth(
                    picture
                        ?: return, imageUrl, ScreenUtils.getScreenWidth()
                )
            }
        } else {
            //展示贵宾列表
            listView?.adapter = mHeadAdapter
            mHeadAdapter?.setList(data.royalHonorList ?: arrayListOf())
            if (data.isPull && data.list.isEmpty()) {
                //普通列表为空那就把贵族列表title去除
                headBottom?.hide()
            }
        }
    }

    private fun refreshData(data: OnlineListData<OnlineUserInfo>) {

        var userNum: Int?
        when (mPageTag) {
            TabTags.TAB_TAG_ROYAL -> {
                //贵族
                userNum = data.royalCount
                mRoyalUrl = data.royalLevelUrl

                setHeadViews(data)
            }
            TabTags.TAB_TAG_GUARD -> {
                //守护
                mCurrGuardData = data
                userNum = data.guardCount
                setHeadViews(data)
            }
            else -> {
                //观众 or 管理
                userNum = if (mPageTag == TabTags.TAB_TAG_MANAGER) {
                    data.managerCount
                } else {
                    data.userCount
                }
            }
        }

        var map = mPlayerViewModel.updateOnLineCount.value
        map = map ?: hashMapOf()
        map[mPageTag ?: ""] = "$userNum"
        mPlayerViewModel.updateOnLineCount.value = map

        if (data.isPull) {
            adapter.setList(data.list)
        } else {
            val result = adapter.data.mergeNoDuplicateNew(data.list)
            adapter.setList(result)
        }
        if (!data.hasMore) {
            adapter.loadMoreModule.loadMoreEnd()
        } else {
            adapter.loadMoreModule.loadMoreComplete()
        }
    }


    private fun loadData(type: QueryType) {
        mViewModel.queryList(
            mPageTag ?: "", type, mPlayerViewModel.programId
        )
    }


    /**
     * 普通布局
     */
    private val adapter = object : BaseQuickAdapter<OnlineUserInfo, BaseViewHolder>(R.layout.item_online_list),LoadMoreModule {

        private var adapters: LruCache<Int, SoftReference<OnlineBadgeAdapter>>? = null
        private val MAX_SIZE = 100

        override fun convert(holder: BaseViewHolder, item: OnlineUserInfo) {


            holder.setImageResource(R.id.ivItemLevel, ImageHelper.getUserLevelImg(item.userLevel))
                .setText(R.id.tvItemNickname, item.nickname)
            ImageUtils.loadImage(holder.getView(R.id.sdvItemHead), item.headPic, 40f, 40f)
            if (!TextUtils.isEmpty(item.royalPic)) {
                ImageUtils.loadImageWithHeight_2(holder.getView(R.id.sdvItemRoyal), item.royalPic, dp2px(16f))
            } else {
                holder.setGone(R.id.sdvItemRoyal, false)
            }
            val rvBadgeList = holder.getView<RecyclerView>(R.id.rvItemList)
            rvBadgeList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            getView(holder.adapterPosition).let {
                rvBadgeList.adapter = it
                if (item.badgesPic.isEmpty()) {
                    it.replaceData(arrayListOf())
                } else {
                    it.replaceData(item.badgesPic)
                }
            }
        }

        fun getView(position: Int): OnlineBadgeAdapter {
            if (adapters == null) {
                adapters = LruCache(MAX_SIZE)
            }
            var adapter: OnlineBadgeAdapter? = adapters?.get(position)?.get()
            if (adapter == null) {
                adapter = OnlineBadgeAdapter()
                adapters?.put(position, SoftReference(adapter))
            }
            return adapter
        }
    }


}


/**
 * 贵宾头布局
 */
class OnlineHeadAdapter : BaseQuickAdapter<OnlineUserInfo, BaseViewHolder>(R.layout.item_online_head_list) {

    override fun convert(holder: BaseViewHolder, item: OnlineUserInfo) {

        if (item.userId == -1L) {
            holder.setText(R.id.tvHeadNickname, "虚位以待")
                .setGone(R.id.ivHeadBorder, false)
            ImageUtils.loadImageLocal(holder.getView(R.id.sdvHeadImage), R.mipmap.important_placeholder)
        } else {
            holder.setText(R.id.tvHeadNickname, item.nickname)
                .setVisible(R.id.ivHeadBorder, true)
            ImageUtils.loadImage(holder.getView(R.id.sdvHeadImage), item.headPic, 46f, 46f)
        }
        if (item.royalLevel != -1) {
            holder.setVisible(R.id.ivHeadRoyal, true)
                .setImageResource(R.id.ivHeadRoyal, ImageHelper.getRoyalLevelImgRound(item.royalLevel))
        } else {
            holder.setGone(R.id.ivHeadRoyal, false)
        }
    }
}

/**
 * 在线列表勋章
 */
class OnlineBadgeAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_online_badge_list), LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: String) {

        ImageUtils.loadImageWithHeight_2(holder.getView(R.id.sdvItemBadge), item, dp2px(15f))
    }
}