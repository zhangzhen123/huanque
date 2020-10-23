package com.julun.huanque.message.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.common.adapter.GiftCountAdapter
import com.julun.huanque.common.adapter.PrivateGiftAdapter
import com.julun.huanque.common.adapter.SimplePagerAdapter
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.ConsumeForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.viewmodel.FirstRechargeViewModel
import com.julun.huanque.common.widgets.GiftTitleView
import com.julun.huanque.common.widgets.happybubble.BubbleDialog
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.message.R
import com.julun.huanque.message.viewmodel.ChatSendGiftViewModel
import com.julun.huanque.message.viewmodel.PrivateConversationViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_gift_private.*
import kotlinx.android.synthetic.main.dialog_gift_private.view_top
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import org.jetbrains.anko.configuration
import org.jetbrains.anko.forEachChild
import org.jetbrains.anko.imageResource


/**
 * 送礼弹窗
 */
class PrivateSendGiftFragment : BaseDialogFragment() {
    private val viewModel: ChatSendGiftViewModel by activityViewModels()

    private val mPrivateConversationViewModel: PrivateConversationViewModel by activityViewModels()

    private val mFirstRechargeViewModel: FirstRechargeViewModel by activityViewModels()

    // 每页显示10个礼物
    private val pageLimit = 8

    // 当前选中的礼物
    private var selectedGift: ChatGift? = null

    //记录再次选择礼物时上一个已选礼物的界面adapter
    private var lastPagerAdapter: PrivateGiftAdapter? = null

    //记录发送礼物时当前的选中礼物的界面adapter
    private var sendPagerAdapter: PrivateGiftAdapter? = null

    private var currentGiftAdapter: PrivateGiftAdapter? = null

    // 选中礼物可选数量adapter
    private val selectCountAdapter = GiftCountAdapter()

    // 当前选中礼物可选数量
    private var optionCountList: List<GoodsOptionCount> = mutableListOf()
    private var currentSelectCount: Int = 0//记录当前的选中数目

    // 所有礼物面板数据
    private var goodsCfgData: ChatGiftInfo? = null

    private var curGiftIsSending: ChatGift? = null//记录当前正在执行赠送回调的礼物

    private var tabList: ArrayList<TabItemInfo> = arrayListOf()//记录大tab标签的
    private var currentPagePosition: Int = 0//记录当前的需要主动刷新全局数据后 主动切换到指定位置

    //标记经验上次刷新时间，防止消息乱序
    private var currentExpRefreshTime: Long = 0


    companion object {
        fun newInstance() = PrivateSendGiftFragment()
    }


    override fun getLayoutId(): Int {
        return R.layout.dialog_gift_private
    }

    override fun onStart() {
        super.onStart()
        this.setDialogSize(Gravity.BOTTOM, 0, ViewGroup.LayoutParams.MATCH_PARENT)
        //不需要半透明遮罩层
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }


    override fun initViews() {
        initViewModel()
        initListener()
        giftViewPager?.adapter = viewPagerAdapter
        giftViewPager?.addOnPageChangeListener(viewPagerChangeListener)

        selectCountListView.layoutManager = LinearLayoutManager(context)
        selectCountListView?.adapter = selectCountAdapter

        doLoadGiftData()

        // 数量选择列表
        selectCountAdapter.setOnItemClickListener { adapter, view, position ->
            if (!ForceUtils.isIndexNotOutOfBounds(position, optionCountList)) {
                return@setOnItemClickListener
            }
            currentSelectCount = optionCountList[position].countValue
            sendCountLabel?.text = currentSelectCount.toString()
            selectCountListView?.visibility = View.GONE

            refreshProcessDataPreview(selectedGift?.userExp ?: 0L, currentSelectCount)
        }

        // 数量选择视图默认为禁用样式
        changeCountViewToDisable()

    }

    private fun initListener() {

        tv_privilege.onClickNew {
            val programId = SharedPreferencesUtils.getLong(SPParamKey.PROGRAM_ID_IN_FLOATING, 0)
            RNPageActivity.start(requireActivity(), RnConstant.WEALTH_LEVEL_PAGE, Bundle().apply {
                putLong(
                    "programId", programId
                )
            })
        }
        view_top?.onTouch { _, motionEvent ->
            if (motionEvent.action === MotionEvent.ACTION_DOWN) {
                dismiss()
                return@onTouch true
            }
            return@onTouch false
        }
        // 跳转到充值界面
        balance_layout?.onClickNew {

            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
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

// 根容器，数量选择视图隐藏
        send_gift_container?.setOnClickListener {
            hideCountListView()
        }

        iv_first_recharge.onClickNew {
            //跳转首充
            val firstRechargeBean = mFirstRechargeViewModel.firstRechargeBean.value
            if (firstRechargeBean == null) {
                mFirstRechargeViewModel.getFirstRechargeData()
            } else {
                mFirstRechargeViewModel.firstRechargeBean.value = firstRechargeBean
            }
        }

        mengdou_icon.onClickNew {
            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
        }
        // 赠送按钮
        sendActionBtn?.setOnClickListener(sendGiftClick)
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
        viewModel.giftList.observe(viewLifecycleOwner, Observer {
            //
            if (it.state == NetStateType.SUCCESS) {
                hideLoadingView()
                refreshGiftView(it.requireT())
                viewModel.giftList.value?.state = NetStateType.IDLE
            } else if (it.state == NetStateType.ERROR) {
                //dodo
            }
        })

        viewModel.sendGiftBean.observe(this, Observer {
            if (it != null) {
                mPrivateConversationViewModel.sendGiftSuccessData.value = it
                viewModel.sendGiftBean.value = null
            }

        })

        viewModel.sendResult.observe(viewLifecycleOwner, Observer {
            //
//            tv_send.isEnabled = true
            it ?: return@Observer
            logger.info("赠送返回=${it.state}")
            resetData()
            if (it.state == NetStateType.SUCCESS) {
//                refreshSendResult(it.getT())
            } else if (it.state == NetStateType.ERROR) {
                //dodo
                if (it.error?.busiCode == 1001) {
                    //余额不足
                    mPrivateConversationViewModel.balanceNotEnoughFlag.value = true
                } else {
                    ToastUtils.show(it.error?.busiMessage ?: return@Observer)
                }
                viewModel.sendResult.value = null
            }
        })


        mPrivateConversationViewModel.balance.observe(this, Observer {
            if (it != null) {
                balanceLabel.text = "$it"
            }
        })

        mPrivateConversationViewModel.userExpChangeEvent.observe(this, Observer {
            if (it != null) {
                mPrivateConversationViewModel.userExpChangeEvent.value = null
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

        mFirstRechargeViewModel.firstRechargeFlag.observe(this, Observer {
            if (it == true) {
                iv_first_recharge.show()
            } else {
                iv_first_recharge.hide()
            }
        })
        mFirstRechargeViewModel.firstRechargeBean.observe(this, Observer {
            if (it != null) {
                val fragment = ARouter.getInstance().build(ARouterConstant.FIRST_RECHARGE_FRAGMENT).navigation() as? BaseDialogFragment
                fragment?.show(requireActivity().supportFragmentManager, "FirstRechargeFragment")
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



                titleView.onPagerTitleChangeListener = object : CommonPagerTitleView.OnPagerTitleChangeListener {
                    override fun onDeselected(index: Int, totalCount: Int) {
                        gtv.setTextType(Typeface.defaultFromStyle(Typeface.NORMAL))
                    }

                    override fun onSelected(index: Int, totalCount: Int) {
                        //保存版本数据
                        val currentTab = tabList[index]
                        SharedPreferencesUtils.commitInt(currentTab.typeCode, currentTab.version)
                        gtv.setTextType(Typeface.defaultFromStyle(Typeface.BOLD))
                        val tabView = gtv
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
                    giftViewPager.show()
                    showDotter(dotter, true)
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


    // 激活发送礼物布局框
    private fun changeCountViewToEnable() {
        sendCountLayout?.isEnabled = true
    }

    // 禁用发送礼物布局框
    private fun changeCountViewToDisable() {
        sendCountLayout?.isEnabled = false
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
                    item.chatGiftId == giftId
                }.size
                if (size > 0) {
                    curPosition = pos
                    return@breaking
                }
            }
        }
        return curPosition
    }

    /**
     * 开始获取礼物数据
     */
    private fun doLoadGiftData() {
        setSelectGiftFunction(null)
        // 初始化查询礼物列表一次
        loadingText.visibility = View.VISIBLE
        viewModel.queryInfo()
    }

    private var mDispose: Disposable? = null

    /**
     *   刷新视图所有数据
     *   @param giftDataDto 礼物数据
     */
    private fun refreshGiftView(giftDataDto: ChatGiftInfo) {
        if (balanceLabel == null || loadingText == null) {
            reportCrash("SendGiftFragment当前的生命周期$currentLife 此时根view:${view} :${balanceLabel}----${loadingText}--${dotter}")
            return
        }
        mFirstRechargeViewModel.firstRechargeFlag.value = giftDataDto.firstCharge

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
        } else {
            logger.info("没有进度相关的消息 不显示")
            progress_layout.hide()
        }

        hideLoadingView()
        // 按每页10个礼物组装数据
        val viewPagerData: MutableList<PrivateGroupInfo> = mutableListOf()
        goodsCfgData?.giftGroupList?.forEach {
            tabList.add(TabItemInfo(it.typeCode, it.typeName, it.version))
            it.giftList.sliceBySubLengthNew(pageLimit).forEachIndexed { index, list ->
                viewPagerData.add(PrivateGroupInfo(list as MutableList<ChatGift>, it.typeCode, it.typeName))
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
//        realList.sliceBySubLengthNew(pageLimit).forEachIndexed { index, list ->
//            viewPagerData.add(GroupInfo(list as MutableList<LiveGiftDto>, BAG_TYPE_CODE, BAG_TYPE_NAME))
//        }

        viewPagerData.forEach { groupInfo ->
            if (groupInfo.giftList.size < pageLimit) {
                val size = pageLimit - groupInfo.giftList.size
                for (i in 1..size) {
                    groupInfo.giftList.add(ChatGift().apply { chatGiftId = BusiConstant.EMPTY_GIFT }) //空白填充 -100代表空白
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

        // 默认高亮第一页下标点点图标
        if (viewPagerData.isNotEmpty()) {
//            lightPositionDots(0)
            giftViewPager.currentItem = currentPagePosition
            //如果就在初始位置 是不会触发onPageSelected的 这里手动调取
            if (currentPagePosition == 0)
                changeTabAndDotsCount(currentPagePosition)
        }
    }


    private var mMCBubble: BubbleDialog? = null
    private var mMCDispose: Disposable? = null

    private fun refreshProcessDataPreview(userExp: Long, count: Int) {
        goodsCfgData?.let { giftData ->
            val previewNum = userExp * count
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
     * 再次加载时刷新跑道值
     */
    override fun reCoverView() {
//        setOrientationLayout()
        //手动再次添加生命周期监听
        initViewModel()
        //每次打开重新初始化 存在请求未完成 而界面关闭 导致状态一直没变
        resetData()
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
    private fun lightPositionDots(position: Int) {
        val view = dotter
        val count = view.childCount
        if (count == 0) return
        for (i in 0 until count) {
            view?.getChildAt(i)?.isEnabled = i == position
        }
    }


    private fun setBalanceLabelValue(it: Long) {
        balanceLabel?.text = "$it"
        goodsCfgData?.beans = it
    }

    override fun onResume() {
        super.onResume()
        lastPagerAdapter?.notifyDataSetChanged()
        currentGiftAdapter?.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
//        blurLayout.pauseBlur()
    }

    override fun onDestroy() {
        logger.info("onDestroy")
        mDispose?.dispose()
        mMCDispose?.dispose()
        if (mMCBubble?.isShowing == true) {
            mMCBubble?.dismiss()
        }

        super.onDestroy()
    }

    // 每页标记图标
    private val mLayoutInflater: LayoutInflater by lazy { LayoutInflater.from(activity) }

    // 发送礼物按钮点击事件
    private val sendGiftClick: View.OnClickListener by lazy {
        View.OnClickListener {
            if (selectedGift == null) {
                ToastUtils.show("选择要送出的礼物喔~")
                return@OnClickListener
            }
            hideCountListView()
            realSendGift(selectedGift ?: return@OnClickListener)
        }
    }

    /**
     * 实际赠送礼物的方法
     */
    private fun realSendGift(gift: ChatGift) {
        sendPagerAdapter = lastPagerAdapter
        curGiftIsSending = gift
        sendActionBtn?.text = "..."
        val targetId = mPrivateConversationViewModel.targetIdData.value
        if (selectedGift == null) {
            ToastUtils.show("请先选择礼物再赠送")
            return
        }
        if (targetId == null) {
            ToastUtils.show("没有可赠送目标")
            return
        }
        if (currentSelectCount == 0) {
            ToastUtils.show("请选择数量")
            return
        }
        viewModel.sendGift(targetId, selectedGift ?: return, count = currentSelectCount, fateId = mPrivateConversationViewModel.mFateID)
    }

    private fun sendGiftSuccess(data: SendGiftResult, form: ConsumeForm) {
//        //异常收集
//        if (balanceLabel == null || sendActionBtn == null) {
//            reportCrash("当前的fragment生命周期:$currentLife goodsCfgData是不是空:${goodsCfgData == null}")
//        }
//        val wealth = data.beans
//        balanceLabel?.text = "$wealth"
//        goodsCfgData?.beans = wealth
//
//        //手动改变经验属性
//        if (goodsCfgData != null && curGiftIsSending != null) {
//            val addExp = curGiftIsSending!!.userExp * form.count
//            val expShort = goodsCfgData!!.needExp - goodsCfgData!!.userExp
//            if (expShort <= addExp) {
//                //用户升级了
//            } else {
//                goodsCfgData!!.userExp = goodsCfgData!!.userExp + addExp
//                //刷新本次经验进度条 可不要 现在统一后台推送刷新
//                refreshProcessDataPreview(curGiftIsSending!!.userExp)
//            }
//        }
    }

    private fun resetData() {
        // 请求结束
        sendActionBtn?.text = "赠送"
        curGiftIsSending = null
    }

    // 选中礼物
    fun selectedCurrentGift(isByUser: Boolean = true) {
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
        refreshProcessDataPreview(selectedGift?.userExp ?: 0L, currentSelectCount)

        changeCountViewToEnable()
    }

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

    /**
     * 设置选中的礼物方法
     */
    private fun setSelectGiftFunction(dto: ChatGift?) {
        selectedGift = dto
        sendActionBtn.isEnabled = selectedGift != null
        PrivateGiftAdapter.selectedGift = dto

        if (selectedGift == null) {
            sendCountLayout.hide()
            sendActionBtn.isEnabled = false
        } else {
            sendCountLayout.show()
            sendActionBtn.isEnabled = true
        }
    }


    /**
     * 显示ViewPager数据
     */
    private fun showViewPagerData(view: RecyclerView, giftList: MutableList<ChatGift>) {
        var mAdapter = view.adapter as? PrivateGiftAdapter
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
            mAdapter = PrivateGiftAdapter()
            view.adapter = mAdapter
        }
        mAdapter.setOnItemClickListener { adapter, itemView, position ->
            try {
                hideCountListView()
                val currentGift = mAdapter.data[position]
                if (currentGift.chatGiftId == BusiConstant.EMPTY_GIFT) {
                    return@setOnItemClickListener
                }
                if (selectedGift == null || (selectedGift?.chatGiftId != null && (selectedGift?.chatGiftId != currentGift.chatGiftId))) {
                    setSelectGiftFunction(currentGift)
                    selectedCurrentGift(true)

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
        mAdapter?.setList(giftList)
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

    private val viewPagerAdapter: SimplePagerAdapter<PrivateGroupInfo, RecyclerView> by lazy {
        object : SimplePagerAdapter<PrivateGroupInfo, RecyclerView>(R.layout.view_cmp_just_grid) {
            override fun renderItem(view: RecyclerView, itemAt: PrivateGroupInfo) {
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
                currentGiftAdapter = view?.adapter as? PrivateGiftAdapter
                //
                currentPagePosition = position
                changeTabAndDotsCount(position)
            }
        }
    }
}