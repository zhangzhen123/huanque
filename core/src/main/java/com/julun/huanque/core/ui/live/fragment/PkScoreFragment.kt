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
import com.julun.huanque.common.bean.beans.PkUserRankInfo
import com.julun.huanque.common.bean.beans.RankingsResult
import com.julun.huanque.common.bean.beans.TIBean
import com.julun.huanque.common.bean.beans.UserInfoBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.setTFDINCondensedBold
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.PhotoHeadView
import com.julun.huanque.common.widgets.draweetext.DraweeSpanTextView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.PkScoreViewModel
import kotlinx.android.synthetic.main.fragment_score.*
import org.jetbrains.anko.textColor

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/16 14:53
 *
 *@Description: PK贡献榜
 *
 */
class PkScoreFragment : BaseVMFragment<PkScoreViewModel>() {
    private val PROGRAMID = "programId"


    private val PAGE_LIMIT = 10//现在一次只能拉十条
    private var dataCount: Int = 0
    private var programId: Long = 0
    private var pkId: Long = 0

    private var emptyView: View? = null

    private val mPlayerViewModel: PlayerViewModel by activityViewModels()

    companion object {
        const val PK_ID = "pkId"
        fun newInstance(): PkScoreFragment {
            return PkScoreFragment()
        }
    }

    //修改传递参数的方式以bundle方式  可以防止界面被回收后重建时参数丢失  2017/2/17
    fun setArgument(programId: Long, pkId: Long) {
        val bundle = Bundle()
        bundle.putLong(PROGRAMID, programId)
        bundle.putLong(PK_ID, pkId)
        arguments = bundle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        programId = arguments?.getLong(PROGRAMID) ?: 0L
        pkId = arguments?.getLong(PK_ID) ?: 0L
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
                    loadData(it.requireT())
                } else if (it.state == NetStateType.ERROR) {
                    queryError(it.isRefresh())
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
//        mPlayerViewModel.scoreDismissFlag.value = true
    }

    private fun queryRankingsResult(queryType: QueryType) {
        mViewModel.queryPkRankingsResult(programId, pkId, queryType)
    }

    private fun queryError(isRefresh: Boolean) {
        if (isRefresh) {
            ToastUtils.show("加载失败")
        } else {
            scoreAdapter.loadMoreModule.loadMoreFail()
        }

    }

    private fun finalDo() {
        contributionRefreshView?.isRefreshing = false
        cancelLoadingView()
    }

    private fun loadData(rankInfo: PkUserRankInfo) {
        mPlayerViewModel.pkDesUrl = rankInfo.ruleUrl
        scoreAdapter.setList(rankInfo.rankList)
        scoreAdapter.loadMoreModule.loadMoreEnd(true)
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
                holder.setText(R.id.contributionText, StringHelper.formatNum(item.score))
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