package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.RankingsResult
import com.julun.huanque.common.bean.beans.TIBean
import com.julun.huanque.common.bean.beans.UserInfoBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.PhotoHeadView
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.ScoreViewModel
import kotlinx.android.synthetic.main.fragment_score.*
import org.jetbrains.anko.textColor

/**
 * 直播间贡献榜
 * Created by djp on 2016/12/9.
 * @iterativeAuthor WanZhiYuan
 * @iterativeDate 2019/07/04
 * @iterativeVersion 4.15
 * @iterativeDetail 贵族勋章统一由后端适配，客户端只做富文本加载
 */
class ScoreFragment : BaseVMFragment<ScoreViewModel>() {
    private val PROGRAMID = "programId"
    private val RANKINGSID = "rankingsId"
    private val PAGE_LIMIT = 10//现在一次只能拉十条
    private var dataCount: Int = 0
    private var programId: Long = 0
    private var rankingsId: Int = 0

    private var emptyView: View? = null

    private val mPlayerViewModel: PlayerViewModel by activityViewModels()

    companion object {
        fun newInstance(): ScoreFragment {
            return ScoreFragment()
        }
    }

    //修改传递参数的方式以bundle方式  可以防止界面被回收后重建时参数丢失  2017/2/17
    fun setArgument(programId: Long, rankingsId: Int) {
        val bundle = Bundle()
        bundle.putLong(PROGRAMID, programId)
        bundle.putInt(RANKINGSID, rankingsId)
        arguments = bundle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        programId = arguments?.getLong(PROGRAMID) ?: 0L
        rankingsId = arguments?.getInt(RANKINGSID) ?: 0
    }

    override fun getLayoutId(): Int = R.layout.fragment_score
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        MixedHelper.setSwipeRefreshStyle(contributionRefreshView)
    }

    /**
     * 初始化ViewModel相关
     */
    private fun initViewModel() {

        if (!mViewModel.refreshData.hasObservers()) {
            mViewModel.refreshData.observe(this, Observer {
                if (it.state == NetStateType.SUCCESS) {
                    loadData(it.getT())
                } else if (it.state == NetStateType.ERROR) {
                    queryError(it.getT())
                }
                finalDo()
            })
        }

    }

    override fun initEvents(rootView: View) {
        mlistview.adapter = scoreAdapter
        mlistview.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)


        scoreAdapter.setOnItemClickListener { _, _, position ->
            val user = this.scoreAdapter.getItem(position)
            openPlayerInfo(user ?: return@setOnItemClickListener)
        }
        contributionRefreshView.setOnRefreshListener {
            dataCount = 0
            queryRankingsResult(QueryType.REFRESH)
        }
        scoreAdapter.loadMoreModule.setOnLoadMoreListener {
            dataCount += PAGE_LIMIT
            queryRankingsResult(QueryType.LOAD_MORE)
        }

    }

    override fun lazyLoadData() {
        queryRankingsResult(QueryType.INIT)
    }


    private fun openPlayerInfo(user: RankingsResult) {
        //显示用户名片
        mPlayerViewModel.userInfoView.value = UserInfoBean(user.userId, nickname = user.nickname)
        mPlayerViewModel.scoreDismissFlag.value = true
    }

    private fun queryRankingsResult(queryType: QueryType) {
        when (rankingsId) {
            10001 -> {//本场
                mViewModel.queryRankingsResultBtThis(programId, queryType)
            }
            10002 -> {//本周
                mViewModel.queryRankingsResultBtWeek(programId, queryType)
            }
            10003 -> {//本月
                mViewModel.queryRankingsResultBtMonth(programId, queryType)
            }
        }
    }

    private fun queryError(data: RootListData<RankingsResult>) {
        if (data.isPull) {
            ToastUtils.show("加载失败")
        } else {
            scoreAdapter.loadMoreModule.loadMoreFail()
        }

    }

    private fun finalDo() {
        contributionRefreshView?.isRefreshing = false
        cancelLoadingView()
    }

    private fun loadData(rankList: RootListData<RankingsResult>) {
        if (rankList.isPull) {
            scoreAdapter.setList(rankList.list)
        } else {
            //合并、去重加排序(从大到小)
            val list = scoreAdapter.data.mergeNoDuplicateNew(rankList.list)
                .sortList(Comparator { r1: RankingsResult, r2: RankingsResult ->
                    val result = (r2.score - r1.score).toInt()
                    result
                })
//            val list = adapter.data.mergeNoDuplicateNew(rankList.list).sortedByDescending {
//                it.score
//            }
            scoreAdapter.setList(list)
        }

        if (rankList.hasMore) {
            scoreAdapter.loadMoreModule.loadMoreComplete()
        } else {
            if (rankList.isPull)
                scoreAdapter.loadMoreModule.loadMoreEnd(true)
            else
                scoreAdapter.loadMoreModule.loadMoreEnd()
        }
    }

    private val scoreAdapter: BaseQuickAdapter<RankingsResult, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<RankingsResult, BaseViewHolder>(R.layout.item_score), LoadMoreModule {
            override fun convert(holder: BaseViewHolder, item: RankingsResult) {

                val nickNameText = holder.getView<TextView>(R.id.nickNameText)
                nickNameText.text = item.nickname
                if (item.nickcolor.isNotEmpty()) {
                    nickNameText.textColor = GlobalUtils.formatColor(item.nickcolor)
                } else {
                    nickNameText.textColor = GlobalUtils.getColor(R.color.black_333)
                }
                val position = holder.layoutPosition - headerLayoutCount
                holder.setText(R.id.contributionText, "${/*StringHelper.formatNumber(*/item.score}鹊币")
                //勋章
                renderImage(item, holder.getView<DraweeSpanTextView>(R.id.image_text))

                // 榜单前3
                if (position < 3) {
                    holder.setVisible(R.id.rankImage, true).setGone(R.id.rankText, true)
                        .setImageResource(R.id.rankImage, ImageHelper.getRankResId(position))
                } else {
                    holder.getView<TextView>(R.id.rankText).setTFDINCondensedBold()
                    holder.setVisible(R.id.rankText, true).setGone(R.id.rankImage, true)
                        .setText(R.id.rankText, (position + 1).toString())
                }
                val head = holder.getView<PhotoHeadView>(R.id.headImage)
//                //添加边框
//                val roundingParams = RoundingParams.fromCornersRadius(5f)
//                roundingParams.roundAsCircle = true
//                if (position < 3) {
//                    val color =
//                        when (position) {
//                            0 -> Color.parseColor("#FFE471")
//                            1 -> Color.parseColor("#E3EDFD")
//                            2 -> Color.parseColor("#FCD2A3")
//                            else -> Color.TRANSPARENT
//                        }
//                    roundingParams.setBorder(
//                        color,
//                        dp2px(1).toFloat()
//                    )
//
//                } else {
//                    roundingParams.setBorder(Color.TRANSPARENT, 0f)
////                    holder.getView<SimpleDraweeView>(R.id.headImage).setPadding(dp2px(1), dp2px(1), dp2px(1), dp2px(1))
//                }
//                holder.getView<SimpleDraweeView>(R.id.headImage).hierarchy.roundingParams = roundingParams
//                ImageUtils.loadImage(head, item.headPic, 50f, 50f)
                head.setImage(
                    headUrl = item.headPic + BusiConstant.OSS_160,
                    headSize = 46,
                    frameUrl = item.headFrame,
                    frameWidth = 74,
                    frameHeight = 74
                )
            }

            /**
             * 设置用户的活动勋章
             */
            private fun renderImage(item: RankingsResult, imageTest: DraweeSpanTextView) {

                val list = arrayListOf<TIBean>()
                val userLevelImage = TIBean()
                userLevelImage.type = 1
                userLevelImage.imgRes = ImageHelper.getUserLevelImg(item.userLevel)
                userLevelImage.height = DensityHelper.dp2px(16f)
                userLevelImage.width = DensityHelper.dp2px(10f)
                list.add(userLevelImage)

                if (!TextUtils.isEmpty(item.royalPic)) {
                    val royalLevelImage = TIBean()
                    royalLevelImage.type = 1
                    royalLevelImage.url = StringHelper.getOssImgUrl(item.royalPic)
                    royalLevelImage.height = DensityHelper.dp2px(16f)
                    royalLevelImage.width = DensityHelper.dp2px(10f)
                    list.add(royalLevelImage)
                }
                val textBean = ImageHelper.renderTextAndImage(list)
                if (textBean == null) {
                    imageTest.hide()
                } else {
                    imageTest.show()
                    imageTest.renderBaseText(textBean)
                }
            }
        }
    }

    private fun cancelLoadingView() {
        activity ?: return
        if (emptyView == null) {
            emptyView = MixedHelper.getEmptyView(requireActivity(), "争做第一个上榜的人吧")
            scoreAdapter.setEmptyView(emptyView!!)
        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {
//                scoreAdapter.setEmptyView(MixedHelper.getEmptyView(requireContext()))
            }
            NetStateType.LOADING -> {
                scoreAdapter.setEmptyView(MixedHelper.getLoadingView(requireContext()))
            }
            NetStateType.ERROR -> {
                scoreAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = requireContext(),
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                scoreAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = requireContext(),
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            queryRankingsResult(QueryType.INIT)
                        })
                )

            }
        }

    }
}