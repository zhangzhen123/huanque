package com.julun.huanque.core.ui.live.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.ConsumeForm
import com.julun.huanque.common.bean.forms.RechargeRuleQueryForm
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.widgets.CircleBarView
import com.julun.huanque.common.widgets.GiftTitleView
import com.julun.huanque.common.widgets.happybubble.BubbleDialog
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.common.adapter.GiftAdapter
import com.julun.huanque.core.adapter.GiftCountAdapter
import com.julun.huanque.common.adapter.SimplePagerAdapter
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.viewmodel.EggSettingViewModel
import com.julun.huanque.core.viewmodel.SendGiftViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.trello.rxlifecycle4.android.FragmentEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_gift.*
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import org.jetbrains.anko.configuration
import org.jetbrains.anko.forEachChild
import org.jetbrains.anko.imageResource
import java.util.concurrent.TimeUnit


/**
 * 送礼弹窗
 */
class SendGiftFragment : BaseDialogFragment() {
    //背包的typecode
    private val BAG_TYPE_CODE = "BAG"

    //背包的typeName
    private val BAG_TYPE_NAME = "背包"
    private var viewModel: SendGiftViewModel? = null
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private val mEggSettingViewModel: EggSettingViewModel by activityViewModels()


    // 每页显示10个礼物
    private val pageLimit = 8

    // 礼物数量容器
    private var mSendCountLayout: LinearLayout? = null

    // 当前选中的礼物
    private var selectedGift: LiveGiftDto? = null

    //记录再次选择礼物时上一个已选礼物的界面adapter
    private var lastPagerAdapter: GiftAdapter? = null

    //记录发送礼物时当前的选中礼物的界面adapter
    private var sendPagerAdapter: GiftAdapter? = null

    private var currentGiftAdapter: GiftAdapter? = null

    // 所有礼物面板数据
    private var goodsCfgData: GiftDataDto? = null

    //背包数据
    private var mBagData = mutableListOf<LiveGiftDto>()

    // 当前选中礼物可选数量
    private var optionCountList: List<GoodsOptionCount> = mutableListOf()
    private var sendRequesting = false

    private var curGiftIsSending: LiveGiftDto? = null//记录当前正在执行赠送回调的礼物

    private var expRatio: Double = 1.0 //当前的经验倍数 默认1.0

    private var resultFragment: EggResultFragment? = null

    private var tabList: ArrayList<TabItemInfo> = arrayListOf()//记录大tab标签的
    private var currentPagePosition: Int = 0//记录当前的需要主动刷新全局数据后 主动切换到指定位置
    private var currentSelectCount: Int = 0//记录当前的选中数目
    private var currentExpRefreshTime: Long = 0

    // 本次应付金额
    private var tmpShouldFee: Long = 0

    // 当前跑道价值
    private var runwayCurrentValue: Long = 0

    //背包需要刷新标记位
    private var bagNeedRefresh = false


    companion object {
        const val GiftBox = "GiftBox"//礼盒

        //        const val CJBWL_EGG=-59
//        const val BWL_EGG=-63
        fun newInstance() = SendGiftFragment()
    }


    override fun getLayoutId(): Int {
        return R.layout.dialog_gift
    }

    override fun onStart() {
        super.onStart()
        this.setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.MATCH_PARENT)
        //不需要半透明遮罩层
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }


    override fun initViews() {

//        setSelectGiftFunction(null)
        mSendCountLayout = sendCountLayout
//        mSendCountLabel = sendCountLabel
//        notificationSwitch = rootContainer!!.notificationSwitch_old
        initViewModel()
        initListener()
        giftViewPager?.adapter = viewPagerAdapter
        giftViewPager?.addOnPageChangeListener(viewPagerChangeListener)

        selectCountListView.layoutManager = LinearLayoutManager(context)
        selectCountListView?.adapter = selectCountAdapter
        //背包
        bagViewPager?.adapter = mBagViewPagerAdapter
        bagViewPager?.addOnPageChangeListener(mBagViewPagerChangeListener)
        // 数量选择列表
        selectCountAdapter.setOnItemClickListener { adapter, view, position ->
            if (!ForceUtils.isIndexNotOutOfBounds(position, optionCountList)) {
                return@setOnItemClickListener
            }
            currentSelectCount = optionCountList[position].countValue
            sendCountLabel?.text = currentSelectCount.toString()
            selectCountListView?.visibility = View.GONE

            val shouldFee = selectedGift!!.realRunwayBeans * currentSelectCount
            refreshProcessDataPreview(selectedGift?.userExp ?: 0L, currentSelectCount)
        }
        // 数量选择视图默认为禁用样式
        changeCountViewToDisable()
        tmpShouldFee = 0

        doLoadGiftData()

    }

    private fun initListener() {

        gtv_package.onClickNew {
            //点击背包
            if (gtv_package.isSelected) {
                return@onClickNew
            }
            if (goodsCfgData?.bagChange == true || mBagData.isEmpty()) {
                //刷新背包数据
                viewModel?.getBagData(playerViewModel.programId)
                goodsCfgData?.bagChange = false
            }
            selPackage(true)
            //选中背包tab
            tabList.forEachIndexed { index, tab ->
                if (tab.typeCode == BAG_TYPE_CODE) {
                    //背包
                    magic_indicator.onPageSelected(index)
                }
            }
            giftViewPager.inVisible()
            showDotter(dotter, false)
            bagViewPager.show()
            showDotter(dotter_bag, true)
        }
        tv_privilege.onClickNew {
            RNPageActivity.start(requireActivity(), RnConstant.WEALTH_LEVEL_PAGE, Bundle().apply { putLong("programId", playerViewModel.programId) })
        }
        // 礼物数量容器控制 数量listview显示隐藏
        sendCountLayout?.onTouch { view, motionEvent ->
            if (motionEvent.action === MotionEvent.ACTION_DOWN) {
                if (selectedGift != null) {
                    doCountListLoadData()
                    return@onTouch true
                }
            }
            return@onTouch false
        }
        view_top?.onTouch { _, motionEvent ->
            if (motionEvent.action === MotionEvent.ACTION_DOWN) {
                dismiss()
                return@onTouch true
            }
            return@onTouch false
        }
        // 根容器，数量选择视图隐藏
        send_gift_container?.setOnClickListener {
            hideCountListView()
        }
        // 跳转到充值界面
        balance_layout?.onClickNew {

            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
        }

//        svga_player?.onClick {
//            activity.startActivityForResult(Intent(activity, RechargeCenterActivity::class.java), LiveBusiConstant.RECHARGE_REQUEST_CODE)
//            dismiss()
//        }
        mengdou_icon.onClickNew {
            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
        }
        // 赠送按钮
        sendActionBtn?.setOnClickListener(sendGiftClick)
        //连送按钮
        liansong?.findViewById<ImageView>(R.id.ivBtnSendGifts)?.setOnClickListener(sendGiftClick)
        liansong.setListener(object : CircleBarView.OnAnimationListener {
            override fun changeProgress(interpolatedTime: Float) {
            }

            override fun onFinish() {
                logger.info("连送按钮播完隐藏")
                hideLiansong()
            }
        })
    }

    private fun refreshPackage() {
        if (goodsCfgData?.bagChange == true || mBagData.isEmpty()) {
            //如果状态本身就是要刷新，那就不管了
            return
        }
        //直接触发刷新操作
        viewModel?.getBagData(playerViewModel.programId)
    }

    /**
     * 显示指示器
     * @param show 显示还是隐藏
     */
    private fun showDotter(view: ViewGroup, show: Boolean) {
        if (view.childCount >= 2 && show) {
            view.show()
        } else {
            view.hide()
        }
    }

    /**
     * 初始化ViewModel相关
     */
    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SendGiftViewModel::class.java)
        viewModel?.data?.observe(this, Observer {
            refreshGiftView(it ?: return@Observer)
        })
        viewModel?.bagData?.observe(this, Observer {
            it?.forEach { dto ->
                if (dto.changeMark) {
                    bagNeedRefresh = true
                    return@forEach
                }
            }
            showBagData(it ?: return@Observer)
        })
        viewModel?.bagChangeState?.observe(this, Observer {
            if (it == true && gtv_package?.isSelected != true) {
                //未选中背包
                gtv_package?.showDot(true)
                goodsCfgData?.bagChange = true
                playerViewModel.bagChangeFlag.value = it
            } else {
                playerViewModel.bagChangeFlag.value = false
            }
        })
        viewModel?.hideLoading?.observe(this, Observer { hideLoadingView() })

        viewModel?.sendGiftSuccess?.observe(this, Observer { result ->
            result?.let {
                //显示送礼成功相关
                if (it.bagChange) {
                    if (it.form?.fromBag == BusiConstant.True) {
                        //从背包送出,直接刷新背包
                        viewModel?.getBagData(playerViewModel.programId)
                    } else {
                        viewModel?.bagChangeState?.value = true
                    }
                }

                sendGiftSuccess(it, it.form ?: return@Observer)
                playerViewModel.timeInternal(it.ttl * 1000L)
                //显示砸蛋结果数据
                if (it.prizeBeans > 0 && it.prizeList.isNotEmpty()) {
                    showResult(it)
                    //刷新礼物数量
                    if (!it.bagChange) {
                        //没有新的背包礼物产生，直接更新背包数据
                        refreshNewGiftCount(it)
                    }
                } else {
                    //不是砸蛋的情况下，直接回复默认状态
                    resetData()
                }
                it.feedbackList?.let { list ->
                    if (list.isNotEmpty()) {
                        refreshNewGiftCount(it)
                        showBoxReward(list)
                    }
                }
                viewModel?.sendGiftSuccess?.value = null
            }
        })
        viewModel?.sendGiftError?.observe(this, Observer {
            resetData()
            sendGiftError(it ?: return@Observer)
        })

        mEggSettingViewModel?.updateTipsData?.observe(this, Observer { tipsList ->
            if (tipsList != null) {
                val giftIds = mutableListOf<Int>()
                //将List转为Map，减少一层for循环
                val tipsMap = mutableMapOf<Int, String>()
                tipsList.forEach {
                    giftIds.add(it.giftId)
                    tipsMap.put(it.giftId, it.tips)
                }
                val groupList = goodsCfgData?.giftGroupInfoList
                if (groupList != null) {
                    //获取到所有的礼物数据
                    A@ for (group in groupList) {
                        for (gift in group.giftList) {
                            val gId = gift.giftId
                            if (gId != null && giftIds.contains(gId)) {
                                //检索到需要刷新的礼物，更新tips
                                gift.tips = tipsMap[gId]
                                //移除对应的礼物ID
                                giftIds.remove(gId)
                                if (gId == selectedGift?.giftId) {
                                    showSelectGiftWithId(gId)
                                }
                            }
                            if (giftIds.isEmpty()) {
                                //表示所有需要更新的ID已经处理完成，不需要再进行接下来的for循环
                                break@A
                            }
                        }
                    }
                }
                //更新Tips数据
                mEggSettingViewModel?.updateTipsData?.value = null
            }
        })

//        viewModel.sendFinal.observe(this, Observer { sendFinal() })
        playerViewModel?.balance?.observe(this, Observer {
            logger.info("Player balance = $it")
            setBalanceLabelValue(it ?: return@Observer)
        })

        if (playerViewModel?.loginState?.value != true) {
            //处于为登录状态添加次监听
            playerViewModel?.loginState?.observe(this, Observer {
                if (it == true) {
                    //登录成功
                    doLoadGiftData()
                }
            })
        }
        playerViewModel?.liansongTime?.observe(this, Observer {
            if (it != null) {
                (liansong.findViewById<CircleBarView>(R.id.cbvGiftCircleView)).mPostInvalidate(it)
                if (it == 1f) {
                    hideLiansong()
                }
            }
        })

        playerViewModel?.userExpChangeEvent?.observe(this, Observer {
            if (it != null) {
                logger.info("刷新经验值：${it.newExp} level=${it.newLevel}")
                goodsCfgData?.let { goods ->
                    if (currentExpRefreshTime < it.time) {
                        goods.userExp = it.newExp
                        goods.needExp = it.needExpValue
                        goods.userLevel = it.newLevel
                        currentExpRefreshTime = it.time
                        refreshProcessDataPreview(
                            selectedGift?.userExp
                                ?: return@Observer, currentSelectCount
                        )
                    } else {
                        logger.info("消息过时了：${it.time}")
                    }
                }
            }
        })

        playerViewModel?.openGiftViewWithSelect?.observe(this, Observer {
            if (it != null) {
                showSelectGiftWithId(it)
            }
        })


        playerViewModel?.refreshGift?.observe(this, Observer {
            //刷新礼物面板数据
            if (it == true) {
                doLoadGiftData()
                playerViewModel?.refreshGift?.value = null
            }
        })

        playerViewModel?.openGiftAndSelPack?.observe(this, Observer {
            //选中背包
            if (viewModel?.isRefreshing?.value == true) return@Observer
            if (it == true) {
                gtv_package.performClick()
                playerViewModel?.openGiftAndSelPack?.value = null
            }
        })
        playerViewModel?.refreshGiftPackage?.observe(this, Observer {
            //刷新礼物背包
            if (viewModel?.isRefreshing?.value == true) return@Observer
            if (it == true) {
                refreshPackage()
                playerViewModel?.refreshGiftPackage?.value = null
            }
        })

    }


    /**
     * 初始化指示器
     */
    private fun initMagicIndicator(list: ArrayList<TabItemInfo>) {
        val mCommonNavigator = CommonNavigator(activity)
        mCommonNavigator.scrollPivotX = 0.65f
        mCommonNavigator.isAdjustMode = false
        mCommonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return list.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val currentTab = list[index]
                val titleView = CommonPagerTitleView(context)
                val gtv = GiftTitleView(context)
                titleView.setContentView(gtv)
                gtv.setContent(currentTab.typeName)
                if (currentTab.typeCode == BAG_TYPE_CODE) {
                    //背包数据
                    gtv.showDot(false)
                    gtv.showLine(false)
                } else {
                    //除了背包以外的其他tab
                    gtv.showLine(true)
                    val currentVersion = currentTab.version
                    val localVersion = SharedPreferencesUtils.getInt(currentTab.typeCode, 0)
                    if (localVersion < currentVersion) {
                        //tab版本增加了，显示红点
                        gtv.showDot(true)
                    } else {
                        gtv.showDot(false)
                    }
                }



                titleView.onPagerTitleChangeListener = object : CommonPagerTitleView.OnPagerTitleChangeListener {
                    override fun onDeselected(index: Int, totalCount: Int) {
                        gtv.setTextType(Typeface.defaultFromStyle(Typeface.NORMAL))
                    }

                    override fun onSelected(index: Int, totalCount: Int) {
                        //保存版本数据
                        val currentTab = tabList[index]
                        SharedPreferencesUtils.commitInt(currentTab.typeCode, currentTab.version)
                        gtv.setTextType(Typeface.defaultFromStyle(Typeface.BOLD))
                        if (ForceUtils.isIndexNotOutOfBounds(index, tabList)) {
                            selPackage(currentTab.typeCode == BAG_TYPE_CODE)
                        }
                        val tabView = if (currentTab.typeCode == BAG_TYPE_CODE) {
                            gtv_package
                        } else {
                            if (giftViewPager.visibility != View.VISIBLE) {
                                giftViewPager.show()
                                bagViewPager.hide()
                            }
                            gtv
                        }
                        tabView.showDot(false)
                    }

                    override fun onLeave(index: Int, totalCount: Int, leavePercent: Float, leftToRight: Boolean) {
                        gtv.isSelected = leavePercent < 0.5f
                    }

                    override fun onEnter(index: Int, totalCount: Int, enterPercent: Float, leftToRight: Boolean) {
                        gtv.isSelected = enterPercent >= 0.5f
                    }
                }

                titleView.setOnClickListener {
                    val curTab = list[index]

                    if (curTab.typeCode == BAG_TYPE_CODE) {
                        //背包
                        return@setOnClickListener
                    }
                    giftViewPager.show()
                    showDotter(dotter, true)
                    bagViewPager.inVisible()
                    refreshBag()
                    showDotter(dotter_bag, false)
                    magic_indicator.onPageSelected(index)
                    val position = getCurrentTabPosition(curTab.typeCode)
                    giftViewPager.currentItem = position
                }
                return titleView
            }

            override fun getIndicator(context: Context): IPagerIndicator? {
                return null
            }
        }
        magic_indicator.navigator = mCommonNavigator
    }


    /**
     * 选中背包
     */
    private fun selPackage(sel: Boolean) {
        if (sel) {
            gtv_package.isSelected = true
            gtv_package.setTextType(Typeface.defaultFromStyle(Typeface.BOLD))
        } else {
            gtv_package.isSelected = false
            gtv_package.setTextType(Typeface.defaultFromStyle(Typeface.NORMAL))
        }
    }

    //根据tab获取 该tab第一页的位置
    private fun getCurrentTabPosition(typeCode: String): Int {
        var curPosition = 0
        //从前往后匹配
        run breaking@{
            viewPagerAdapter.getData().forEachIndexedOrdered { pos, data ->
                if (data.typeCode == typeCode) {
                    curPosition = pos
                    return@breaking
                }
            }
        }
        return curPosition
    }

    //根据giftId获取礼物所在页的位置
    private fun getCurrentPositionWithGiftId(giftId: Int): Int {
        var curPosition = 0
        //从前往后匹配
        run breaking@{
            viewPagerAdapter.getData().forEachIndexedOrdered { pos, data ->
                val size = data.giftList.filter { item ->
                    item.giftId == giftId
                }.size
                if (size > 0) {
                    curPosition = pos
                    return@breaking
                }
            }
        }
        return curPosition
    }

    private fun showSelectGiftWithId(giftId: Int) {
        if (viewPagerAdapter.getData().isEmpty()) {
            return
        }
        currentPagePosition = getCurrentPositionWithGiftId(giftId)
//        }
        // 切换到指定页面
        giftViewPager.currentItem = currentPagePosition
        //如果就在初始位置 是不会触发onPageSelected的 这里手动调取
        if (currentPagePosition == 0) {
            changeTabAndDotsCount(currentPagePosition)
        }

        //选中相应的礼物
        run breaking@{
            viewPagerAdapter.getData().forEachIndexedOrdered { pos, groupInfo ->
                val last = groupInfo.giftList.size - 1
                val rv = viewPagerAdapter.getViewAt(pos)
                currentGiftAdapter = rv?.adapter as? GiftAdapter
                for (i in 0..last) {
                    if (giftId == groupInfo.giftList[i].giftId) {
                        //自动选中
                        setSelectGiftFunction(groupInfo.giftList[i])
                        selectedCurrentGift(true, rv?.getChildAt(i))
                        currentGiftAdapter?.notifyDataSetChanged()
                        lastPagerAdapter = currentGiftAdapter
                        logger.info("选中相应礼物item=$giftId 跳出循环")
                        return@breaking
                    }
                }
            }
        }
    }

    /**
     * 余额不足时的弹窗
     */
    private fun showRechargeDialog(giftValue: Long) {
//        dismiss()
        playerViewModel?.notEnoughBalance?.value = NotEnoughBalanceBean(RechargeRuleQueryForm.SEND_GIFT, beans = giftValue)
//        playerActivity?.showNotEnoughBalanceAlert(RechargeRuleQueryForm.SEND_GIFT, giftValue)
    }


    /**
     * 开始获取礼物数据
     */
    private fun doLoadGiftData() {
        setSelectGiftFunction(null)
        // 初始化查询礼物列表一次
        loadingText.visibility = View.VISIBLE
        viewModel?.doLoadGiftData(playerViewModel.programId)
    }

    private var mDispose: Disposable? = null

    /**
     *   刷新视图所有数据
     *   @param giftDataDto 礼物数据
     */
    private fun refreshGiftView(giftDataDto: GiftDataDto) {
        if (balanceLabel == null || loadingText == null) {
            reportCrash("SendGiftFragment当前的生命周期$currentLife 此时根view:${view} :${balanceLabel}----${loadingText}--${dotter}")
            return
        }
        mEggSettingViewModel?.eggLuckyState?.value = giftDataDto.luckyHitEgg
        mEggSettingViewModel?.anonymousState?.value = giftDataDto.anonymousHitEgg
        mEggSettingViewModel?.discountStatus?.value = giftDataDto.discountFirst



        viewPagerAdapter.clear()
        dotter.removeAllViews()
        giftViewPager?.removeAllViews()
        tabList.clear()
        goodsCfgData = giftDataDto
        //
        if (giftDataDto.needExp != 0L || giftDataDto.userExp != 0L || giftDataDto.userLevel != 0) {
            progress_layout.show()
            val expShort = giftDataDto.needExp - giftDataDto.userExp
            var process = 0
            //已有的经验值 * 100 /总经验值 = 进度条展示的百分比长度
            if (giftDataDto.needExp != 0L)
                process = (giftDataDto.userExp * 100 / giftDataDto.needExp).toInt()
            user_progress.setProcess(process)

            iv_express_lack.text = "距离${giftDataDto.userLevel + 1}级还差${StringHelper.formatUserScore(expShort)}鹊币"

            tv_current_level.text = "LV.${giftDataDto.userLevel}"
            tv_next_level.text = "LV.${giftDataDto.userLevel + 1}"
            send_gift_container.post {
                val height = (rootView?.height ?: 0) + dp2px(45f)
                logger.info("rootHeight=$height")
                playerViewModel?.sendGiftHigh?.value = height
            }
        } else {
            logger.info("没有进度相关的消息 不显示")
            progress_layout.hide()
        }

        hideLoadingView()
        // 当前用户余额
        balanceLabel?.text = "${goodsCfgData?.beans}"
        runwayCurrentValue = giftDataDto.runwayMaxBeans
        // 所有礼物数量选择集合
//        optionCountMap = goodsCfgData!!.optionCountMap!!
        // 按每页10个礼物组装数据
        val viewPagerData: MutableList<GroupInfo> = mutableListOf()
        goodsCfgData?.giftGroupInfoList?.forEach {
            tabList.add(TabItemInfo(it.typeCode, it.typeName, it.version))
            it.giftList.sliceBySubLengthNew(pageLimit).forEachIndexed { index, list ->
                viewPagerData.add(GroupInfo(list as MutableList<LiveGiftDto>, it.typeCode, it.typeName))
            }
        }
//        //添加背包数据
//        val realList = if (bagData.isEmpty()) {
//            //添加一个空白数据
//            mutableListOf<LiveGiftDto>()
//        } else {
//            bagData
//        }
//        //添加一个空白tab
        tabList.add(TabItemInfo(BAG_TYPE_CODE, ""))
//        realList.sliceBySubLengthNew(pageLimit).forEachIndexed { index, list ->
//            viewPagerData.add(GroupInfo(list as MutableList<LiveGiftDto>, BAG_TYPE_CODE, BAG_TYPE_NAME))
//        }

        viewPagerData.forEach { groupInfo ->
            if (groupInfo.giftList.size < pageLimit) {
                val size = pageLimit - groupInfo.giftList.size
                for (i in 1..size) {
                    groupInfo.giftList.add(LiveGiftDto().apply { giftId = BusiConstant.EMPTY_GIFT }) //空白填充 -100代表空白
                }
            }
        }
        initMagicIndicator(tabList)
//       = goodsCfgData?.gifts?.sliceBySubLength(pageLimit) ?: return
        val lp = LinearLayout.LayoutParams(dp2px(5), dp2px(5))
        lp.setMargins(dp2px(4).toInt(), 0, dp2px(4).toInt(), 0)
//        viewPagerAdapter.notifyDataSetChanged()
        viewPagerData.forEachIndexed { index, list ->
            viewPagerAdapter.addItem(list)
            dotter.addView(getDotImg(index), lp)
        }
        viewPagerAdapter.notifyDataSetChanged()
        giftViewPager?.offscreenPageLimit = viewPagerData.size
        //贵族直接切换到指定tab
//        val currentUser = playerViewModel.loginSuccessData.value?.user
//        if (currentUser != null && currentUser.royalLevel > 0) {
        //以后台字段showTab为准
        currentPagePosition = getCurrentTabPosition(giftDataDto.showTab)

        if (playerViewModel?.openGiftViewWithSelect?.value == null) {
            //需要默认选中第一个礼物
            goodsCfgData?.giftGroupInfoList?.forEach { groupInfo ->
                if (groupInfo.typeCode == giftDataDto.showTab) {
                    //找到对应的tab
                    val giftList = groupInfo.giftList
                    if (giftList.isNotEmpty() && ForceUtils.isIndexNotOutOfBounds(0, giftList)) {
                        playerViewModel?.openGiftViewWithSelect?.value = giftList[0].giftId
                    }
                    return@forEach
                }
            }
        }
//        }
        // 默认高亮第一页下标点点图标
        if (viewPagerData.isNotEmpty()) {
//            lightPositionDots(0)
            giftViewPager.currentItem = currentPagePosition
            //如果就在初始位置 是不会触发onPageSelected的 这里手动调取
            if (currentPagePosition == 0)
                changeTabAndDotsCount(currentPagePosition)
        }
        val selectId = playerViewModel?.openGiftViewWithSelect?.value
//        if (selectId == null) {
//            mDispose = Observable.timer(500, TimeUnit.MILLISECONDS)
//                    .bindUntilEvent(this, FragmentEvent.DESTROY)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe {
//                        if ((rootContainer?.giftViewPager?.childCount ?: 0) > currentPagePosition) {
//                            val view: RecyclerView? = rootContainer?.giftViewPager?.getChildAt(currentPagePosition) as? RecyclerView
//                            currentGiftAdapter = view?.adapter as? GiftAdapter
//                            if (selectedGift == null) {
//                                logger.info("mDispose 自动选中")
//                                setSelectGiftFunction(currentGiftAdapter?.data?.get(0))
//                                val itemView = view?.getChildAt(0)
//                                currentGiftAdapter?.notifyDataSetChanged()
//                                selectedCurrentGift(false, itemView)
//                                lastPagerAdapter = currentGiftAdapter
//                            }
//                        }
//                    }
//
//        } else {
        //自动选中某一个礼物
        if (selectId != null)
            showSelectGiftWithId(selectId)
//        }

        //音速少女需求，如果现在礼物弹窗整个重置了，那么等接口数据回来了再来处理背包问题
        val isSelPackage = playerViewModel?.openGiftAndSelPack?.value
        if (isSelPackage == true) {
            playerViewModel?.openGiftAndSelPack?.value = isSelPackage
        }
        val isRefreshPackage = playerViewModel?.refreshGiftPackage?.value
        if (isRefreshPackage == true) {
            playerViewModel?.refreshGiftPackage?.value = isRefreshPackage
        }
    }

    /**
     * 显示背包数据
     */
    private fun showBagData(bagList: MutableList<LiveGiftDto>) {
        mBagData = bagList
        dotter_bag.removeAllViews()
        bagViewPager.removeAllViews()
        mBagViewPagerAdapter.clear()
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(dp2px(5).toInt(), 0, dp2px(5).toInt(), 0)
        val groupList = bagList.sliceBySubLengthNew(pageLimit)
        val realGroupList = mutableListOf<List<LiveGiftDto>>()
        if (groupList.isNotEmpty()) {
            realGroupList.addAll(groupList)
        } else {
            realGroupList.add(mutableListOf<LiveGiftDto>())
        }
        realGroupList.forEachIndexed { index, list ->
            mBagViewPagerAdapter.addItem(list as MutableList<LiveGiftDto>)
            dotter_bag.addView(getDotImg(index), lp)
        }
        mBagViewPagerAdapter.notifyDataSetChanged()
        bagViewPager?.offscreenPageLimit = mBagViewPagerAdapter.count
        lightPositionDots(0, true)
        if (dotter_bag.childCount <= 1) {
            dotter_bag.hide()
        } else {
            dotter_bag.show()
        }
    }

    private var mMCBubble: BubbleDialog? = null
    private var mMCDispose: Disposable? = null

    private fun refreshProcessDataPreview(userExp: Long, count: Int) {
        goodsCfgData?.let { giftData ->
            val previewNum = userExp * count * expRatio
            var process = 0
            var secProcess = 0
            //已有的经验值 * 100 /总经验值 = 进度条展示的百分比长度
            if (giftData.needExp != 0L) {
                process = (giftData.userExp * 100 / giftData.needExp).toInt()
                secProcess = ((giftData.userExp + previewNum) * 100 / giftData.needExp).toInt()
            }

            val expShort = giftData.needExp - giftData.userExp

            iv_express_lack.text = "距离${giftData.userLevel + 1}级还差${StringHelper.formatUserScore(expShort)}鹊币"

            tv_current_level.text = "LV.${giftData.userLevel}"
            tv_next_level.text = "LV.${giftData.userLevel + 1}"
            user_progress.setProcess(process)
            user_progress.setSecondProcess(secProcess)
        }
    }


    private fun hideLoadingView() {
        loadingText?.visibility = View.GONE
    }

    /**
     * 刷新背包数据
     */
    private fun refreshBag() {
        if (bagNeedRefresh) {
            val bagList = mBagData
            val removeList = mutableListOf<LiveGiftDto>()
            bagList.forEach {
                it.changeMark = false
                if (it.bagCount <= 0) {
                    removeList.add(it)
                }
            }
            removeList.forEach {
                bagList.remove(it)
            }
            viewModel?.bagData?.value = bagList
            bagNeedRefresh = false
        }
    }


    /**
     * 再次加载时刷新跑道值
     */
    override fun reCoverView() {
//        setOrientationLayout()
        //手动再次添加生命周期监听
        initViewModel()
        //每次打开重新初始化 存在请求未完成 而界面关闭 导致状态一直没变
        resetData()
    }

    //记录当前的屏幕方向
    var currentOrientation = -1


    // 加载礼物数量列表
    private fun doCountListLoadData() {
        if (selectCountListView?.visibility != View.GONE) {
            selectCountListView?.visibility = View.GONE
        } else {
            selectCountAdapter.setList(optionCountList)
            selectCountListView?.visibility = View.VISIBLE
        }
    }

    // 隐藏选择数量的视图
    private fun hideCountListView() {
        if (selectCountListView?.visibility != View.GONE) {
            selectCountListView?.visibility = View.GONE
        }
    }

    // viewPager分页圆点
    private fun getDotImg(position: Int): ImageView {
        val dotImg = mLayoutInflater.inflate(R.layout.view_face_group_dot, null).findViewById<ImageView>(R.id.face_dot) as ImageView
        dotImg.id = position
        dotImg.imageResource = R.drawable.selector_gift_dot
        return dotImg
    }

    //切换时改变指示点的数目和大tab状态
    private fun changeTabAndDotsCount(position: Int) {

        val cur = viewPagerAdapter.getItemAt(position)
        //改变tab
        var currentIndex = 0
        tabList.forEachIndexed { index, tabItemInfo ->
            if (tabItemInfo.typeCode == cur.typeCode) {
                currentIndex = index
            }
        }
        magic_indicator.onPageSelected(currentIndex)
        //改变dot
        var showCount = 0
        viewPagerAdapter.getData().forEachIndexedOrdered { index, groupInfo ->
            if (cur.typeCode != groupInfo.typeCode) {
                dotter.getChildAt(index)?.hide()
            } else {
                showCount++
                dotter.getChildAt(index)?.show()
            }
        }
        lightPositionDots(position)
        //如果只有一个dot全部隐藏
        if (showCount <= 1) {
            dotter.forEachChild {
                it.hide()
            }
        }
    }

    /**
     * 当前分页圆点高亮
     * @param bag 是否是背包
     */
    private fun lightPositionDots(position: Int, bag: Boolean = false) {
        val view = if (bag) dotter_bag else dotter
        val count = view.childCount
        if (count == 0) return
        for (i in 0 until count) {
            view?.getChildAt(i)?.isEnabled = i == position
        }
    }

    // 激活发送礼物布局框
    private fun changeCountViewToEnable() {
        sendCountLayout?.isEnabled = true
    }

    // 禁用发送礼物布局框
    private fun changeCountViewToDisable() {
        sendCountLayout?.isEnabled = false
    }


    private fun setBalanceLabelValue(it: Long) {
        balanceLabel?.text = "$it"
        goodsCfgData?.beans = it
    }
/*    // 刷新余额
    private fun refreshBalanceLabelIfNeed() {
        if (needRefreshBalance) {
            logger.info("开始刷新余额")
            needRefreshBalance = false
            refreshBalanceViewModel.refreshBalance()
        }
    }
    */
    /**
     * 此处暂时只有聊天发送弹幕的时候消耗萌豆通知这里
     * 送礼物的还使用原来的方法,直接更新
     * 如果还有别的地方需要,在做诸如并发冲突之类的处理
     *//*
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onLingMengBeanConsumed(event: BeanConsumeEvent) {

        if (isVisible) {
            logger.info("收到需要刷新余额通知 立即刷新")
            //不再进行累加 直接请求后台再刷新
            refreshBalanceViewModel.refreshBalance()
        } else {
            logger.info("收到需要刷新余额通知 标记刷新")
            markNeedToRefreshBalance()
        }
        EventBus.getDefault().removeStickyEvent(event)
    }

    fun markNeedToRefreshBalance() {
        needRefreshBalance = true
    }
*/
    override fun onResume() {
        super.onResume()
        lastPagerAdapter?.notifyDataSetChanged()
        currentGiftAdapter?.notifyDataSetChanged()
        //兼容线上闪退
        send_gift_container?.post {
            val height = (rootView?.height ?: 0) + dp2px(45f)
            logger.info("send_gift_container H=$height")
            playerViewModel?.sendGiftHigh?.value = height
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        logger.info("onDismiss")
        hideCountListView()
        super.onDismiss(dialog)
    }

    override fun onCancel(dialog: DialogInterface) {
        hideCountListView()
        super.onCancel(dialog)
    }

    override fun onStop() {
        super.onStop()
//        blurLayout.pauseBlur()
    }

    override fun onDestroy() {
        logger.info("onDestroy")
        mDispose?.dispose()
        playerViewModel?.sendGiftHigh?.value = -1
        playerViewModel?.openGiftViewWithSelect?.value = null
        mMCDispose?.dispose()
        if (mMCBubble?.isShowing == true) {
            mMCBubble?.dismiss()
        }

        mEggSettingViewModel?.mCurrentGiftLastClickShowExplainBean?.value?.giftId = 0
        super.onDestroy()
    }

    // 每页标记图标
    private val mLayoutInflater: LayoutInflater by lazy { LayoutInflater.from(activity) }

    // 发送礼物按钮点击事件
    private val sendGiftClick: View.OnClickListener by lazy {
        View.OnClickListener {
            // 上次发送礼物还没结束
            if (sendRequesting) {
                return@OnClickListener
            }
            if (selectedGift == null) {
                ToastUtils.show("选择要送出的礼物喔~")
                return@OnClickListener
            }

            selectedGift?.let { gift ->
                // 隐藏数量选择框
                hideCountListView()

                try {
                    val count = sendCountLabel!!.text.toString().toInt()

                    if (count <= 0) {
                        return@OnClickListener
                    }

                    if (gift.bag && count > gift.bagCount) {
                        //背包赠送,并且背包数量不足用户选择的数量
                        MyAlertDialog(requireActivity()).showAlertWithOKAndCancel(
                            "是否一次性送出${gift.bagCount}个${gift.giftName}？",
                            MyAlertDialog.MyDialogCallback(onRight = {
                                realSendGift(gift, gift.bagCount)
                            }, onCancel = {
                            }), "提示", "ALL IN", noText = "再想想"
                        )
                        return@OnClickListener
                    }
                    realSendGift(gift, count)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }

            }
        }
    }

    /**
     * 实际赠送礼物的方法
     */
    private fun realSendGift(gift: LiveGiftDto, count: Int) {
        val fromBag = if (gift.bag) {
            BusiConstant.True
        } else {
            BusiConstant.False
        }

        val form = ConsumeForm(
            giftId = gift.giftId,
            count = count,
            programId = this.playerViewModel.programId,
            fromBag = fromBag,
            prodType = gift.prodType
        )
        sendPagerAdapter = lastPagerAdapter
        sendRequesting = true
        curGiftIsSending = gift
        sendActionBtn?.text = "..."
        viewModel?.sendGift(form)
    }

    private fun sendGiftSuccess(data: SendGiftResult, form: ConsumeForm) {
        //异常收集
        if (balanceLabel == null || sendActionBtn == null) {
            reportCrash("当前的fragment生命周期:$currentLife goodsCfgData是不是空:${goodsCfgData == null}")
        }
        val wealth = data.beans
        balanceLabel?.text = "$wealth"
        goodsCfgData?.beans = wealth

        if (form.fromBag == BusiConstant.True) {
            // 刷新背包逻辑
            mBagData.forEach {
                if (it.giftId == form.giftId && it.prodType == form.prodType) {
                    val count = data.bagCount
                    it.bagCount = count
                    if (count <= 0) {
                        it.couldSend = false
                        setSelectGiftFunction(selectedGift)
                        bagNeedRefresh = true
                    }
                    mBagViewPagerAdapter.notifyDataSetChanged()
                    return@forEach
                }
            }
        }
        //bagCntMap

        if (selectedGift?.bag == true && (selectedGift?.bagCount ?: 0) <= 0) {
            hideLiansong()
        } else {
            showLiansong(data.level /*selectedGift?.pic ?: ""*/)
        }
        //手动改变经验属性
        if (goodsCfgData != null && curGiftIsSending != null) {
            val addExp = curGiftIsSending!!.userExp * form.count
            val expShort = goodsCfgData!!.needExp - goodsCfgData!!.userExp
            if (expShort <= addExp) {
                //用户升级了
            } else {
                goodsCfgData!!.userExp = goodsCfgData!!.userExp + addExp
                //刷新本次经验进度条 可不要 现在统一后台推送刷新
                refreshProcessDataPreview(curGiftIsSending!!.userExp, form.count)
            }
        }
    }

    //连送按钮
    private fun showLiansong(level: Int/*, icon: String*/) {
        liansong.showButton(level/*, icon*/)
//        val llp2 = line_2.layoutParams as RelativeLayout.LayoutParams
        sendCountLayout.visibility = View.INVISIBLE
        sendActionBtn.visibility = View.INVISIBLE
//        llp2.rightMargin = dip(80)
//        line_2.requestLayout()
    }

    private fun hideLiansong() {
        if (send_gift_container == null || liansong.visibility == View.GONE) return
        liansong.hide()

        sendActionBtn.show()
        if (sendActionBtn.isEnabled) {
            sendCountLayout.show()
        } else {
            sendCountLayout.hide()
        }
    }

    /**
     * 显示砸蛋结果
     */
    private fun showResult(result: EggHitSumResult) {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            return
        }
        playerViewModel.eggResultData.value = result
        resultFragment = resultFragment ?: EggResultFragment()

        resultFragment?.show(childFragmentManager, "EggResultFragment")

        timer(500L, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .bindUntilEvent(this, FragmentEvent.DESTROY)
            .subscribe { resetData() }
    }

    /**
     * 显示礼盒奖励
     */
    private fun showBoxReward(list: ArrayList<BoxGainGift>) {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            return
        }
    }

    /**
     * 刷新赠送魔法礼物之后的背包数量
     */
    @SuppressLint("CheckResult")
    private fun refreshNewGiftCount(result: SendGiftResult) {
        Observable.just(result)
            .flatMap<GiftAdapter> {
                val countMap = it.bagCntMap
                val adapterList = ArrayList<GiftAdapter>()
                //获取有变化的礼物ID
                val changeGiftIds = countMap?.keys
                    ?: return@flatMap Observable.fromIterable(adapterList)
                val count = bagViewPager.childCount
                (0 until count).forEach {
                    val adapter = (bagViewPager.getChildAt(it) as? RecyclerView)?.adapter as? GiftAdapter
                    adapter?.data?.forEach { dto ->
                        val changeKey = "${dto.prodType}${dto.giftId}"
                        if (changeGiftIds.contains(changeKey)) {
                            //该礼物有变化
                            dto.bagCount = countMap[changeKey] ?: 0
                            adapterList.add(adapter)
                        }

                    }

                }
                Observable.fromIterable(adapterList)
            }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .bindUntilEvent(this, FragmentEvent.DESTROY)
            .subscribe { it?.notifyDataSetChanged() }
    }

    /**
     * 超级蛋刷新以及进度以及当前背包数目刷新
     */
    private fun refreshGiftItemBySelect(result: SendGiftResult) {
        //刷新背包数目的
        val newBagCount: Int = result.bagCount
        viewPagerAdapter.getData().filter { it.typeCode == curGiftIsSending?.typeCode }.forEach {
            it.giftList.filter { ft ->
                ft.giftId == result.form?.giftId
            }.forEach { rt ->
                if (rt.giftId == result.form?.giftId) {
                    rt.bagCount = newBagCount
                    if (!result.popMsg.isEmpty()) {
                        rt.popMsg = result.popMsg
                    }
                }
            }
        }
        //刷新背包模块数据
        mBagViewPagerAdapter.getData().forEach {
            //需要删除的礼物
            val removeGift = mutableListOf<LiveGiftDto>()
            it.filter { ft ->
                ft.giftId == result.form?.giftId
            }.forEach { rt ->
                if (rt.giftId == result.form?.giftId) {
                    if (newBagCount == 0) {
                        removeGift.add(rt)
                    } else {
                        rt.bagCount = newBagCount
                    }
                }
            }
            removeGift.forEach { rg -> it.remove(rg) }
        }
        if (result.needFlushGift) {
//       viewModel.doLoadGiftData(programId)
            //变换超级蛋
            result.giftChange?.let { giftChange ->
                giftChange.vo?.let { gift ->
                    viewPagerAdapter.getData().forEachIndexedOrdered { pos, groupInfo ->
                        if (groupInfo.typeCode == gift.typeCode) {
                            val rv = viewPagerAdapter.getViewAt(pos)
                            val last = groupInfo.giftList.size - 1
                            for (i in 0..last) {
                                if (groupInfo.giftList[i].giftId == giftChange.giftId) {
                                    val itemView = rv?.getChildAt(i)
//                                    groupInfo.gifts.add(0,gift)
                                    groupInfo.giftList.set(i, gift)

                                    //刷新ViewModel里面保存的数据
                                    val groupInfo = goodsCfgData?.giftGroupInfoList
                                    if (groupInfo != null) {
                                        A@ for (group in groupInfo) {
                                            for ((index, t) in group.giftList.withIndex()) {
                                                if (t.giftId == gift.giftId) {
                                                    group.giftList.set(index, gift)
                                                    logger.info("DXC 成功替换Viewmodel里面的值 index = $index")
                                                    break@A
                                                }

                                            }
                                        }
                                    }

                                    //自动选中
                                    setSelectGiftFunction(gift)
                                    selectedCurrentGift(true, itemView)
                                    logger.info("修改相应礼物item=${gift.giftName}")
                                }
                            }
                        }
                    }
                }
            }
        }

//        val count = giftViewPager.childCount
//        (0 until count).forEach {
//            val adapter = (giftViewPager.getChildAt(it) as? RecyclerView)?.adapter as? GiftAdapter
//            if (result.processInfo!=null&&adapter!=null){
//                adapter.data.forEach { dto ->
//                    if(dto.processInfo!=null){
//                        adapter.notifyDataSetChanged()
//                    }
//                }
//            }
//
//        }

    }

    private fun sendGiftError(it: Throwable) {
        if (it is ResponseError) {
            when (it.busiCode) {
                ErrorCodes.BALANCE_NOT_ENOUGH -> {
                    try {
                        val giftValue = (selectedGift?.beans
                            ?: 0) * currentSelectCount
                        showRechargeDialog(giftValue)
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                }
                else -> {
                    ToastUtils.show(it.busiMessage)
                }
            }
        }
//        resetData()
    }

    //最终返回 一次送礼请求最多3秒 无论成功失败这里都要重置发送按钮
    fun sendFinal() {
        logger.info("送礼最终返回了")
        resetData()
    }

    private fun resetData() {
        // 请求结束
        sendActionBtn?.text = "赠送"
        curGiftIsSending = null
        sendRequesting = false
    }


    // 选中礼物可选数量adapter
    private val selectCountAdapter = GiftCountAdapter()

    // 选中礼物
    fun selectedCurrentGift(isByUser: Boolean = true, currentItemView: View?) {
        // 当前选中礼物是否能上跑道
        hideCountListView()

        // 可选数量集合(倒序：数量最大的在最上面)
        optionCountList = selectedGift?.countItemList ?: return
        if (optionCountList.isEmpty()) {
            return
        }
        // 默认选中第1个数量值
        val defaultPosition = optionCountList.size - 1
        if (defaultPosition >= 0) {
            currentSelectCount = optionCountList[defaultPosition].countValue
            sendCountLabel!!.text = currentSelectCount.toString()
        }
        if (isByUser) {
            refreshProcessDataPreview(selectedGift?.userExp ?: 0L, currentSelectCount)
        }

//        if (optionCountList.size > 1) {
        changeCountViewToEnable()
//        } else {
//            changeCountViewToDisable()
//        }

//        selectedGift?.let {
//            if (it.typeCode == GiftBox) {
//                showOrHideSpecialNotice(false)
//            } else {
//                showOrHideSpecialNotice(true)
//            }
//        }


//        if ((viewModel.sendGiftSuccess.value?.prizeBeans ?: 0) > 0) {
//            val count = giftViewPager.childCount
//            (0 until count).forEach {
//                try {
//                    (giftViewPager.getChildAt(it) as? RecyclerView)?.adapter?.notifyDataSetChanged()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
    }

    /**
     * 设置选中的礼物方法
     */
    private fun setSelectGiftFunction(dto: LiveGiftDto?) {
        selectedGift = dto
        if (selectedGift == null || dto?.couldSend == false) {
            sendCountLayout.hide()
            sendActionBtn.isEnabled = false
        } else {
            sendCountLayout.show()
            sendActionBtn.isEnabled = true
        }
        GiftAdapter.selectedGift = dto
        mEggSettingViewModel?.mSelectGiftData?.value = dto
    }


    /**
     * 显示ViewPager数据
     * @param bag 是否是背包
     */
    private fun showViewPagerData(view: RecyclerView, giftList: MutableList<LiveGiftDto>, bag: Boolean = false) {
        var mAdapter = view.adapter as? GiftAdapter
//                mAdapter?.data?.clear()
//                mAdapter?.notifyDataSetChanged()
        val orientation = context?.configuration?.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            view.layoutManager = GridLayoutManager(context, 8)
        } else {
            view.layoutManager = GridLayoutManager(context, 4)
        }
        if (view.itemDecorationCount == 0) {
            view.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(1f)))
        }
        if (mAdapter == null) {
            mAdapter = GiftAdapter()
            view.adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, itemView, position ->
            try {
                hideCountListView()

                val currentGift = mAdapter.data[position]
                if (currentGift.giftId == BusiConstant.EMPTY_GIFT) {
                    hideCountListView()
                    return@setOnItemClickListener
                }
                //设置连送按钮
                if (currentGift.giftId != selectedGift?.giftId) {
                    hideLiansong()
                }
                if (selectedGift == null || (selectedGift?.giftId != null && (selectedGift?.giftId != currentGift.giftId || selectedGift?.bag != currentGift.bag))) {
                    setSelectGiftFunction(currentGift)
                    selectedCurrentGift(true, itemView)

                    adapter.notifyDataSetChanged()

                    //每一次选择礼物都会进行界面刷新 刷新上一个已选界面以及当前新的界面  如果上一个界面就是当前界面就不需要刷新了
                    if (lastPagerAdapter != null && lastPagerAdapter != adapter) {
                        lastPagerAdapter?.notifyDataSetChanged()
                    }
                    //记录上一个界面的adapter
                    lastPagerAdapter = mAdapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (bag) {
            //显示空页面
            val emptyText = LayoutInflater.from(context).inflate(R.layout.empty_gift_bag, null)
            emptyText.layoutParams = ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(), ViewGroup.LayoutParams.MATCH_PARENT)
            mAdapter.setEmptyView(emptyText)
        }
        mAdapter?.setList(giftList)
    }

    //背包使用的Adapter
    private val mBagViewPagerAdapter: SimplePagerAdapter<MutableList<LiveGiftDto>, RecyclerView> by lazy {
        object : SimplePagerAdapter<MutableList<LiveGiftDto>, RecyclerView>(R.layout.view_cmp_just_grid) {
            override fun renderItem(view: RecyclerView, itemAt: MutableList<LiveGiftDto>) {
                showViewPagerData(view, itemAt, true)
            }

            override fun getItemPosition(`object`: Any): Int {
                return POSITION_NONE
            }
        }
    }

    private val viewPagerAdapter: SimplePagerAdapter<GroupInfo, RecyclerView> by lazy {
        object : SimplePagerAdapter<GroupInfo, RecyclerView>(R.layout.view_cmp_just_grid) {
            override fun renderItem(view: RecyclerView, itemAt: GroupInfo) {
                showViewPagerData(view, itemAt.giftList)
            }

            override fun getItemPosition(`object`: Any): Int {
                return POSITION_NONE
            }
        }
    }
    private val viewPagerChangeListener: ViewPager.OnPageChangeListener by lazy {
        object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                hideCountListView()
            }

            override fun onPageSelected(position: Int) {
                //点亮小圆点
//                lightPositionDots(position)
                val view: RecyclerView? = giftViewPager.getChildAt(position) as? RecyclerView
                currentGiftAdapter = view?.adapter as? GiftAdapter
                //
                currentPagePosition = position
                changeTabAndDotsCount(position)
            }
        }
    }

    /**
     * 背包使用
     */
    private val mBagViewPagerChangeListener: ViewPager.OnPageChangeListener by lazy {
        object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                lightPositionDots(position, true)
            }
        }

    }
}