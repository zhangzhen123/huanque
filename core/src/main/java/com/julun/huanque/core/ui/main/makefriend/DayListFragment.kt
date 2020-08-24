package com.julun.huanque.core.ui.main.makefriend

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.FlowerDayListBean
import com.julun.huanque.common.bean.beans.SingleFlowerDayListBean
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.PlayerFrom
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.helper.ImageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.recycler.decoration.FlowerDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.PlumFlowerListAdapter
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.viewmodel.PlumFlowerViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_day_list.*
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/8/21 20:14
 *@描述 日榜Fragment
 */
class DayListFragment(val type: String) : BaseFragment() {

    companion object {
        //今日
        val TODAY = "TODAY"

        //昨日
        val YESTERDAY = "YESTERDAY"
    }

    //花魁ViewModel
    private val mViewModel: PlumFlowerViewModel by viewModels<PlumFlowerViewModel>()

    private val mAdapter = PlumFlowerListAdapter()

    private var mHeaderView: View? = null

    //头部相关视图
    private var sdvFirst: SimpleDraweeView? = null
    private var tvNicknameFirst: TextView? = null
    private var tvScoreFirst: TextView? = null

    private var sdvSecond: SimpleDraweeView? = null
    private var tvNicknameSecond: TextView? = null
    private var tvScoreSecond: TextView? = null

    private var sdvThird: SimpleDraweeView? = null
    private var tvNicknameThird: TextView? = null
    private var tvScoreThird: TextView? = null

    private var tvRanking: TextView? = null

    override fun getLayoutId() = R.layout.fragment_day_list

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        statePage.show()
        statePage.showLoading()
        initViewModel()
        initRecyclerView()

        sdvFirst = mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_first)
        tvNicknameFirst = mHeaderView?.findViewById<TextView>(R.id.tv_nickname_first)
        tvScoreFirst = mHeaderView?.findViewById<TextView>(R.id.tv_score_first)

        sdvSecond = mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_second)
        tvNicknameSecond = mHeaderView?.findViewById<TextView>(R.id.tv_nickname_second)
        tvScoreSecond = mHeaderView?.findViewById<TextView>(R.id.tv_score_second)

        sdvThird = mHeaderView?.findViewById<SimpleDraweeView>(R.id.sdv_third)
        tvNicknameThird = mHeaderView?.findViewById<TextView>(R.id.tv_nickname_third)
        tvScoreThird = mHeaderView?.findViewById<TextView>(R.id.tv_score_third)

        tvRanking = mHeaderView?.findViewById<TextView>(R.id.tv_ranking)

        showDefaultView()
        if (type == TODAY) {
            mViewModel.getToadyList()
        } else if (type == YESTERDAY) {
            mViewModel.getYesterdayList()
        }


    }

    override fun initEvents(rootView: View) {
        swipeLayout.setOnRefreshListener {
            if (type == TODAY) {
                mViewModel.getToadyList()
            } else if (type == YESTERDAY) {
                mViewModel.getYesterdayList()
            }
        }
        sdvFirst?.onClickNew {
            val singleData = getSingleData(0) ?: return@onClickNew
            click(singleData)
        }

        tvNicknameFirst?.onClickNew { sdvFirst?.performClick() }
        tvScoreFirst?.onClickNew { sdvFirst?.performClick() }

        sdvSecond?.onClickNew {
            val singleData = getSingleData(1) ?: return@onClickNew
            click(singleData)
        }

        tvNicknameSecond?.onClickNew { sdvSecond?.performClick() }
        tvScoreSecond?.onClickNew { sdvSecond?.performClick() }

        sdvThird?.onClickNew {
            val singleData = getSingleData(2) ?: return@onClickNew
            click(singleData)
        }

        tvNicknameThird?.onClickNew { sdvThird?.performClick() }
        tvScoreThird?.onClickNew { sdvThird?.performClick() }
    }

    private fun initViewModel() {

        mViewModel.loadState.observe(this, Observer {
            swipeLayout.isRefreshing = false
            when (it.state) {
                NetStateType.SUCCESS -> {
                }
                NetStateType.ERROR -> {
                    statePage.showError()
                }
            }
        })

        mViewModel.listData.observe(this, object : Observer<FlowerDayListBean> {
            override fun onChanged(bean: FlowerDayListBean?) {
                if (bean == null) {
                    return
                }
                bean.rankList.forEachIndexed { index, data ->
                    data.ranking = "${index + 1}"
                }
                //显示通用数据
                showViewByData(bean)
                if (bean.rankList.isNotEmpty()) {
                    statePage.showSuccess()
                } else {
                    statePage.showEmpty(emptyTxt = "昨日榜空空如也")
                }
//                }
            }
        })
    }

    /**
     * 显示默认视图
     */
    private fun showDefaultView() {
        ImageHelper.setDefaultHeaderPic(sdv_header, SessionUtils.getSex())
        tv_num.text = "-"
        tv_nickname.text = SessionUtils.getNickName()
        tv_num.setTFDINCondensedBold()

        ImageHelper.setDefaultHeaderPic(sdvFirst ?: return, Sex.FEMALE)
        tvNicknameFirst?.text = "-"
        tvScoreFirst?.text = "-"

        ImageHelper.setDefaultHeaderPic(sdvSecond ?: return, Sex.FEMALE)
        tvNicknameSecond?.text = "-"
        tvScoreSecond?.text = "-"

        ImageHelper.setDefaultHeaderPic(sdvThird ?: return, Sex.FEMALE)
        tvNicknameThird?.text = "-"
        tvScoreThird?.text = "-"
    }

    private fun getSingleData(index: Int): SingleFlowerDayListBean? {
        val listData = mViewModel.listData.value?.rankList
        if (listData?.isNotEmpty() == true) {
            if (ForceUtils.isIndexNotOutOfBounds(index, listData)) {
                return listData[index]
            } else {
                return null
            }

        } else {
            return null
        }

    }

    /**
     * 根据数据显示视图
     */
    private fun showViewByData(bean: FlowerDayListBean) {
        val ranking = bean.rankInfo.ranking
        if (type == TODAY) {
            startTodayCountDown(bean.ttl)
        }

        if (ranking == "未上榜") {
            if (type == YESTERDAY) {
                tvRanking?.text = "很遗憾你没有入榜"
            }
            tv_num.text = "-"
        } else {
            if (type == YESTERDAY) {
                tvRanking?.text = "恭喜你荣获昨日榜第${ranking}名"
            }
            tv_num.text = ranking
        }
        tv_score.text = StringHelper.formatNumber(bean.rankInfo.score)

        if (ForceUtils.isIndexNotOutOfBounds(0, bean.rankList)) {
            val firstUser = bean.rankList[0]
            sdvFirst?.loadImage("${firstUser.headPic}${BusiConstant.OSS_160}")
            tvNicknameFirst?.text = firstUser.nickname
            tvScoreFirst?.text = StringHelper.formatNumber(firstUser.score)
            val livingFirstTag = mHeaderView?.findViewById<SimpleDraweeView>(R.id.living_first_tag)
            if (livingFirstTag != null) {
                if (firstUser.living == BusiConstant.True) {
                    livingFirstTag.show()
                    ImageUtils.loadGifImageLocal(livingFirstTag, R.mipmap.anim_living)
                } else {
                    livingFirstTag.hide()
                }
            }
        }
        if (ForceUtils.isIndexNotOutOfBounds(1, bean.rankList)) {
            val secondUser = bean.rankList[1]
            sdvSecond?.loadImage("${secondUser.headPic}${BusiConstant.OSS_160}")
            tvNicknameSecond?.text = secondUser.nickname
            tvScoreSecond?.text = StringHelper.formatNumber(secondUser.score)
            val livingSecondTag = mHeaderView?.findViewById<SimpleDraweeView>(R.id.living_second_tag)
            if (livingSecondTag != null) {
                if (secondUser.living == BusiConstant.True) {
                    livingSecondTag.show()
                    ImageUtils.loadGifImageLocal(livingSecondTag, R.mipmap.anim_living)
                } else {
                    livingSecondTag.hide()
                }
            }
        }
        if (ForceUtils.isIndexNotOutOfBounds(2, bean.rankList)) {
            val thirdUser = bean.rankList[2]
            sdvThird?.loadImage("${thirdUser.headPic}${BusiConstant.OSS_160}")
            tvNicknameThird?.text = thirdUser.nickname
            tvScoreThird?.text = StringHelper.formatNumber(thirdUser.score)
            val livingThirdTag = mHeaderView?.findViewById<SimpleDraweeView>(R.id.living_third_tag)
            if (livingThirdTag != null) {
                if (thirdUser.living == BusiConstant.True) {
                    livingThirdTag.show()
                    ImageUtils.loadGifImageLocal(livingThirdTag, R.mipmap.anim_living)
                } else {
                    livingThirdTag.hide()
                }
            }
        }
        val normalList = mutableListOf<SingleFlowerDayListBean>()
        bean.rankList.forEachIndexed { index, data ->
            if (index >= 3) {
                normalList.add(data)
            }
        }

        mAdapter.setList(normalList)
        if (normalList.isNotEmpty()) {
            tv_empty_content.hide()
        } else {
            tv_empty_content.show()
        }
    }

    private var mTodayCountDownDisposable: Disposable? = null

    /**
     * 显示今日榜单倒计时
     */
    private fun startTodayCountDown(count: Long) {
        mTodayCountDownDisposable?.dispose()
        mTodayCountDownDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .doOnSubscribe {
                val ranking = tvRanking ?: return@doOnSubscribe
                val paint = ranking.paint
                val content = "距离日榜截至： 24：99：99"
                val width = paint.measureText(content)
                val params = ranking.layoutParams
                params.width = width.toInt()
                ranking.layoutParams = params
                ranking.gravity = Gravity.LEFT

            }
            .take(count + 1)
            .bindUntilEvent(this, FragmentEvent.DESTROY_VIEW)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                tvRanking?.text = "距离日榜截至： ${TimeUtils.countDownTimeFormat(count - it)}"
            }, {})
    }

    /**
     * 初始化RecyvlerView
     */
    private fun initRecyclerView() {
        mHeaderView = LayoutInflater.from(context).inflate(R.layout.view_daylist_header, null)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = mAdapter

        mHeaderView?.let { view ->
            mAdapter.addHeaderView(view)
            val params = view.layoutParams
            params.height = ScreenUtils.getScreenWidth() * 978 / 1125 - dp2px(64)
            view.layoutParams = params
        }

        recyclerView.addItemDecoration(FlowerDecoration())

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val singleData = getSingleData(position + 3) ?: return@setOnItemClickListener
            click(singleData)
        }
    }

    /**
     * 列表点击事件
     */
    private fun click(bean: SingleFlowerDayListBean) {
        val userId = bean.userId
        if (bean.living == BusiConstant.True) {
            //开播状态，跳转直播间
            PlayerActivity.start(requireActivity(), userId, "", PlayerFrom.FlowerRank)
        } else {
            //未开播状态，跳转主页
            if (userId == SessionUtils.getUserId()) {
                //跳转我的主页
                RNPageActivity.start(requireActivity(), RnConstant.MINE_HOMEPAGE)
            } else {
                //跳转他人主页
                RNPageActivity.start(requireActivity(), RnConstant.PERSONAL_HOMEPAGE, Bundle().apply { putLong("userId", userId) })
            }
        }
    }

}