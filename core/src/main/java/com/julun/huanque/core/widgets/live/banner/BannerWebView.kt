package com.julun.huanque.core.widgets.live.banner

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import com.julun.huanque.common.bean.beans.RoomBanner
import com.julun.huanque.common.constant.BannerResType
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.MessageBanner
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.widgets.ActionBean
import com.julun.huanque.common.widgets.BaseWebView
import com.julun.huanque.common.widgets.WebViewListener
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import kotlinx.android.synthetic.main.view_web_banner.view.*


/**
 * viewpager  配合  webview实现banner
 */
/*以下情况需根据touchType来判断是打开原生页面还是H5页面(App处理)。
非活动面板，直接点击直播间Banner时。
当获取直播间活动面板状态为False时。
活动面板中H5页面点击查看详情时(事件名称：roomBanner)。
*/
class BannerWebView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    var programId = ""
    //banner翻转结束监听
    private val mBannerRotationFinishListener: BannerRotationFinishListener
    //点击事件
    private val clickListener: BannerClickListener

    init {
        context?.let {
            LayoutInflater.from(it).inflate(R.layout.view_web_banner, this)
        }
        mBannerRotationFinishListener = object : BannerRotationFinishListener {
            override fun onFinish() {
                //结束翻转动画，开始滑动
                scrollJudge()
            }
        }
        clickListener = object : BannerClickListener {
            override fun click(model: RoomBanner) {
                doAction(model)
                stopScroll()
            }

        }

    }

    var bannerListener: BannerListener? = null

    private var datas: ArrayList<RoomBanner>? = null
    //当前webview显示的数据
    private var tempWebData: RoomBanner? = null

    //webView的监听
    private val mWebViewListener = object : WebViewListener {
        override fun jumpToRecharge() {
        }

        override fun perFormAppAction(actionBean: ActionBean) {
            when {
                actionBean.action == BaseWebView.ROOMBANNER -> {
                    //跳转H5
                    tempWebData?.let { tpd ->
                        bannerListener?.doAction(tpd)
                    }
                }
                actionBean.action == BaseWebView.JUMPTOROOM -> {
                    if (actionBean.param != null) {
                        val roomId = actionBean.param!!["roomId"] as Int
                        context?.let {
                            val act = (context as Activity)
                            val intent = Intent(act, PlayerActivity::class.java)
                            intent.putExtra(IntentParamKey.PROGRAM_ID.name, roomId)
                            act.startActivity(intent)
                        }
                    }
                }
                actionBean.action == BaseWebView.OPENUSERHOMEPAGE -> {
                    if (actionBean.param != null) {
                        val roomId = actionBean.param!!["roomId"] as Int
                        val userId = actionBean.param!!["userId"] as Int
                        context?.let {
//                            val act = (context as? Activity)
//                            UserHomePageActivity.newInstance(act ?: return, userId, "$roomId")
                        }
                    }
                }
            }
        }

        override fun onLoadComplete() {
        }

        override fun getRoomId() = programId

        override fun onUrlAction(url: String) {
        }

        override fun titleChange(title: String) {

        }

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isInEditMode) return
        banner?.setAllowUserScrollable(true)
        banner?.setAdapter(bannerAdapter)
//        banner?.setDelegate(bannerItemCick)
    }

    @SuppressLint("CheckResult")
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        scrollJudge()
    }

    /**
     * 显示广告数据
     */
    fun showAds(datas: ArrayList<RoomBanner>) {
        this.datas = datas
        banner?.setData(R.layout.item_room_banner, datas, null)
        if (ForceUtils.isIndexNotOutOfBounds(0, datas)) {
            tempWebData = datas[0]
            tempWebData?.position = 0
        }
        banner?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                datas?.let {
                    if (ForceUtils.isIndexNotOutOfBounds(position, it)) {
                        tempWebData = it[position]
                        tempWebData?.position = position
                    } else {
                        tempWebData = null
                    }
                }
            }

        })
        var tempHeight = DensityHelper.dp2px(46)
        val sb = StringBuilder()
        datas.forEach { roomBanner ->
            tempHeight = Math.max(tempHeight, roomBanner.maxHeight)
            sb.append(roomBanner.adCode).append(",")
        }
        if (sb.isNotEmpty()) {
            val codes = sb.deleteCharAt(sb.length - 1)
            doCheck(codes.toString())
        }
        //修改ViewPager高度
        val params = banner.layoutParams
        //+上指示器高度
        params.height = tempHeight + DensityHelper.dp2px(14f)
        banner.layoutParams = params
        // 默认是自动轮播的，当只有一条广告数据时，就不轮播了
        scrollJudge()
    }

    /**
     * 更具数量判断是否需要滚动
     */
    private fun scrollJudge() {
        val tempData = datas ?: return
        //滚动条件  第一条数据不是显示WebView,并且没有View在做翻转动画
        var scroll = false
        if (ForceUtils.isIndexNotOutOfBounds(0, tempData)) {
            val tempData = tempData[0]
            if (!tempData.showWeb) {
                //第一条数据，显示WebView，需要滚动
                //是否进行动画
                var rotation = false
                banner.views.forEach {
                    rotation = (it as? SingleBannerView)?.isRotation() ?: false
                    if (rotation) {
                        return@forEach
                    }
                }
                if (!rotation) {
                    scroll = true
                }
            }
        }

        ULog.i("DXCS scroll = $scroll")
        if (scroll) {
//            banner?.setAutoPlayAble(true)
            banner.isOnlyUser = false
            banner.startAutoPlay()
        } else {
//            banner?.setAutoPlayAble(false)
            banner.isOnlyUser = true
            stopScroll()
        }
    }

    // 轮播adapter,填充广告图片数据3
    private val bannerAdapter by lazy {
        BGABanner.Adapter<SingleBannerView, RoomBanner> { banner, itemView, model, position ->
            if(itemView.mBannerClickListener == null){
                itemView.setBannerRotationFinishListener(mBannerRotationFinishListener)
                itemView.mBannerClickListener = clickListener
                itemView.setWebViewListener(mWebViewListener)
            }
            when (model?.resType) {
                BannerResType.Pic -> {
                    val localData = itemView.getData()
                    if (localData?.toString() == model.toString()) {
                        //数据一致，直接返回
                        return@Adapter
                    }
                    itemView.showData(model, programId, position == banner?.viewPager?.currentItem)
//                    ImageUtils.loadImageInPx(itemView.findViewById(R.id.sdv), model.resUrl, dip(90), dip(46))
                }
            }
        }
    }

    private val bannerItemCick by lazy {
        BGABanner.Delegate<SingleBannerView, RoomBanner> { bgaBanner, itemView, model, position ->
            if (model != null) {
                doAction(model)
            }
            stopScroll()
        }
    }

    /**
     * 执行操作
     */
    private fun doAction(bean: RoomBanner) {
        doClickAction(bean)
    }

    private fun doCheck(codes: String) {
        bannerListener?.let {
            stopScroll()
        }
        bannerListener?.doCheck(codes)
    }


    /**
     * 检测banner之后处理
     */
    fun checkResult(adCodeList: MutableList<String>?) {
        if (adCodeList == null || adCodeList.size == 0) {
            scrollJudge()
            return
        }
        var firstPop = -1
        datas?.forEachIndexed { index, singleBannerData ->
            val code = singleBannerData.adCode
            val contains = adCodeList.contains(code)
            singleBannerData.showWeb = contains
            if (firstPop == -1 && contains) {
                //需要反转banne，并且firstPop未赋值
                firstPop = index
            }
        }

        if (firstPop == -1) {
            scrollJudge()
            return
        }
        //当前停留的ITEM
        val currentItem = banner?.viewPager?.currentItem ?: return
        val views = banner?.views ?: return
        datas?.forEachIndexed { index, data ->
            if (ForceUtils.isIndexNotOutOfBounds(index, views)) {
                val tempView = views[index]
                (tempView as? SingleBannerView)?.showData(data, programId, (index == currentItem % (views.size)) && data.showWeb)
            }
        }
        scrollJudge()
    }


    /**
     * 执行banner内置的touch操作
     * 如果传null，表示banner回复滚动，隐藏webview
     */
    private fun doClickAction(bean: RoomBanner?) {
        scrollJudge()
        bannerListener?.doAction(bean ?: return)
    }


    /**
     * 停止banner的自动滚动以及手动滑动
     */
    private fun stopScroll() {
        banner?.stopAutoPlay()
    }

    fun showMessage(message: String) {
        try {
            val config = JsonUtil.deserializeAsObject<HashMap<String, Any>>(message, HashMap::class.java)
            val show = "${config["eventCode"]}"
            val tempAdCode = show.substring(show.indexOf("_") + 1)
            if (tempAdCode.isEmpty()) {
                //不能成功获取adcode
                return
            }
            //获取对应的View
            var rightView: SingleBannerView? = null
            var rightData: RoomBanner? = null
            var rightIndex: Int? = null
            val views = banner.views
            views.forEachIndexed { index, it ->
                val tempView = it as? SingleBannerView
                if (tempAdCode == tempView?.getData()?.adCode) {
                    //获取到对应的页面
                    rightData = tempView.getData()
                    rightView = tempView
                    rightIndex = index
                    return@forEachIndexed
                }
            }
            if (rightView == null || rightData == null) {
                //接收到的消息，不存在于显示的banner当中，直接返回
                return
            }
            if (show.startsWith(MessageBanner.PopupShow)) {
                //显示
                //当前停留的ITEM
                if (rightView?.isWebViewShow() == true) {
                    //webview显示，刷新数据
                    rightView?.refreshWeb("${config["context"]}")
                } else {
                    //webview未显示，直接显示webView就可以
                    rightData?.showWeb = true
                    val currentItem = banner?.viewPager?.currentItem ?: return
                    val currentPosition = currentItem % (views.size)
                    rightView?.showData(rightData
                            ?: return, programId, rightIndex == currentPosition)
                    if (rightIndex == 0 && rightIndex != currentItem) {
                        //1号位变为显示状态,并且当前不处于1号位,定位到1号位
                        banner?.viewPager?.currentItem = currentItem - currentPosition
                    }
                }
            } else if (show.startsWith(MessageBanner.PopupHide)) {
                //隐藏
                if (rightView?.isWebViewShow() == true) {
                    //webview显示，需要进行隐藏webview操作
                    rightData?.showWeb = false
                    val currentItem = banner?.viewPager?.currentItem ?: return
                    rightView?.showData(rightData
                            ?: return, programId, (rightIndex == currentItem % (views.size)))
                } else {
                    //webview未显示，什么都不用操作
                }
            }
            scrollJudge()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}