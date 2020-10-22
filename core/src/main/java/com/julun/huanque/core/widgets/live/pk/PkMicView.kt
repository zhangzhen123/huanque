package com.julun.huanque.core.widgets.live.pk

import android.animation.*
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.common.utils.svga.SVGAHelper
import com.julun.huanque.common.viewmodel.VideoChangeViewModel
import com.julun.huanque.common.widgets.live.WebpGifView
import com.julun.huanque.common.widgets.svgaView.SVGAPlayerView
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.live.PlayerViewModel
import com.julun.huanque.core.ui.live.manager.PlayerViewManager
import com.julun.huanque.core.viewmodel.PKViewModel
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.android.synthetic.main.view_pk_mic.view.*
import org.jetbrains.anko.*
import java.util.*

/**
 *
 *@author zhangzhen
 *@data 2018/11/15
 *
 * pk+连麦
 * @alter WanZhiYuan
 * @since 4.34
 * @date 2020/04/15
 * @detail 增加斗地主连麦PK功能
 **/
class PkMicView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val logger = ULog.getLogger("PkMicView")


    private lateinit var pKViewModel: PKViewModel

    private lateinit var playerViewModel: PlayerViewModel

    private lateinit var videoViewModel: VideoChangeViewModel

    private var currentPkType: Int = -1//当前的PK是二人还是三人

    private var currentPKData: PKInfoBean? = null

    private val myHandler: Handler by lazy { Handler() }

    //PK结果图标下移距离
    val pkResultIconDy = PlayerViewManager.LIVE_HEIGHT / 2 - dp2pxf(115) / 2 - dp2pxf(25)

    companion object {
        const val TWO_PK = 2
        const val THREE_PK = 3
        const val LANDLORD = 4
        const val VS_PLAY_TIME = 1930L

        //乌云动画时间
        const val CLOUD_PLAY_TIME = 666L

        //加速火箭动画时间
        const val SPEED_PLAY_TIME = 400L


    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pk_mic, this)
        initViewModel()
        val listener = object : PkPropView.Listener {
            override fun callback(programId: Long) {
                if (playerViewModel.programId == programId) {
                    pKViewModel.userProp(programId)
                }
            }
        }
        //二人pk数值
        pk_prop_01.mListener = listener
        pk_prop_02.mListener = listener
        pk_prop_03.mListener = listener
        pk2_process_text01.setTFDinAltB()
        pk2_process_text02.setTFDinAltB()
        pk2_process_text01.isNeedFormat = false
        pk2_process_text02.isNeedFormat = false


        //三人pk
        pk3_process_text_01.setTFDinAltB()
        pk3_process_text_02.setTFDinAltB()
        pk3_process_text_03.setTFDinAltB()
        pk3_process_text_01.isNeedFormat = false
        pk3_process_text_02.isNeedFormat = false
        pk3_process_text_03.isNeedFormat = false

        pk_dan_level_img01.onClickNew {
            //todo
//            playerViewModel.showDialog.value = PkRankMainDialogFragment::class.java
        }
        pk_dan_level_img02.onClickNew {
//            playerViewModel.showDialog.value = PkRankMainDialogFragment::class.java
        }

        //todo test
//        var index = 1
//        this.onClickNew {
//            currentPkType = TWO_PK
//            two_pk_container.setLeftViewData("user/head/b0295e5d-5c2d-45a8-928f-5333c880f489.jpg", "张三")
//            two_pk_container.setRightViewData("user/head/05c8af9f-1bea-4907-8f50-5a4b26e14a87.jpg", "李四")
//            two_pk_container.startAnimation {
//                playTitleAni(PKInfoBean().apply {
//                    detailList = arrayListOf(PKUser(finalResult = PKResultType.WIN), PKUser())
//                    currRound = 1
//                }, false)
//                postDelayed({
//                    playTitleAni(PKInfoBean().apply {
//                        detailList = arrayListOf(PKUser(finalResult = PKResultType.LOSE), PKUser())
//                        currRound = 2
//                    }, false)
//                }, 3000)
//                postDelayed({
//                    playTitleAni(PKInfoBean().apply {
//                        detailList = arrayListOf(PKUser(finalResult = PKResultType.WIN), PKUser())
//                        currRound = 3
//                    }, false)
//                }, 6000)
//
//
//
//                pk_result_layout.show()
//                pk_win_layout_03.hide()
//                playCupAni(2, index)
//
//                two_pk_container.postDelayed({
//                    playFinalResultAni(1, PKResultType.LOSE)
//                    playFinalResultAni(2, PKResultType.WIN)
//                }, 1000)
//
//            }
//            index++
//            if (index == 4)
//                index = 0
//        }

        pk_props_panel.onClickNew {
            playerViewModel.gotoWeb.value = GoToUrl(false, AppHelper.getDomainName(context.getString(R.string.pk_prop_address)))
        }

        //斗地主相关
//        tvScore01.isNeedFormat = false
//        tvScore02.isNeedFormat = false
//        tvScore03.isNeedFormat = false
//        tvScore01.setTFDinAltB()
//        tvScore02.setTFDinAltB()
//        tvScore03.setTFDinAltB()
    }

    private fun initViewModel() {
        val activity = context as? PlayerActivity ?: return
        pKViewModel = ViewModelProviders.of(activity).get(PKViewModel::class.java)
        playerViewModel = ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
        videoViewModel = ViewModelProviders.of(activity).get(VideoChangeViewModel::class.java)
        pKViewModel.pkStarting.observe(activity, Observer {
            if (it != null) {
                //-2代表计时结束
                when (it.pkType) {
                    PKType.LANDLORD -> {
//                        if (it.seconds == -2) {
//                            landlordsEnd(it)
//                        } else {
//                            landlordsStart(it)
//                        }
                    }
                    else -> {
                        if (it.seconds == -2 || it.endRound) {
                            showPkStatic(it)
                        } else {
                            playPkStarting(it)
                        }
                    }
                }
                pKViewModel.pkStarting.value = null
                if (it.propInfo != null) {
                    setPkPropsData(false, it.propInfo!!)
                }
                if (it.giftTaskInfo != null) {
                    setPkGiftTaskData(false, it.giftTaskInfo!!)
                }
                if (it.scoreTaskInfo != null) {
                    setPkScoreTaskData(false, it.scoreTaskInfo!!)
                }
            }
        })
        playerViewModel.chatModeState.observe(activity, Observer {
            showPkTitleImg()
        })

        pKViewModel.pkTime.observe(activity, Observer {
            pk_time.text = it ?: return@Observer
        })
//            pKViewModel.pkData.observe(it, Observer {
//                if (it != null) {
//                    //
//                    this.justSetPk(it, true)
//                    pKViewModel.pkData.value = null
//                }
//            })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releasePkView()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            pk2_process_01.post { pk2_process_01.requestLayout() }
            pk2_process_02.post { pk2_process_02.requestLayout() }
//            logger.info("更新布局  weight = ${(pk2_process_01.layoutParams as? ConstraintLayout.LayoutParams)?.horizontalWeight}")
        }
    }

    /**
     *
     * 总的入口2 刷新pk进度
     * 仅仅是展示
     */
    private fun showPkStatic(data: PKInfoBean) {
        this.show()
        logger.info("展示结果 不做动画")
        //设置pk数据
        data.needAnim = false
        justSetPk(data)
        //设置pk结果显示
        resetPkResultView()

        pk_result_layout.show()
        //静态展示奖杯
        if (data.detailList != null) {
            data.detailList!!.forEachIndexed { index, item ->
                if (PKResultType.WIN == item.roundResult) {
                    playCupAni(1 + index, item.winRound, false)
                }
            }

        }
        //如果PK结束 展示PK结果
        if (data.roundFinish) {
            when (currentPkType) {
                TWO_PK -> {
                    pk_win_layout_03.hide()
                }
                THREE_PK -> {
                    pk_win_layout_03.show()
                }
            }

            if (data.detailList != null) {
                data.detailList!!.forEachIndexed { index, item ->
                    showFinalResultStatic(index + 1, item.finalResult)
                }

            }
        }

        //展示其他内容
//        showPkTitleImg()
        myHandler.postDelayed({
            videoViewModel.showVideoInfo.value = true
        }, VS_PLAY_TIME)

    }

    /**
     * 总的入口1 开始pk
     */
    private fun playPkStarting(pkinfo: PKInfoBean) {
        this.show()
        pkinfo.needAnim = false
        currentPKData = pkinfo
        logger.info("开始播放pkStarting")
        //对于上一次PK没正常结束的情况
        if (pkinfo.currRound == 1) {
            if (pk_result_layout.isVisible())
                pk_result_layout.hide()
        }


        currentPkType = pkinfo.detailList?.size ?: -1
        if (currentPkType <= 0) {
            logger.info("pk参数有误")
            return
        }

        //现在分三小局 每一小局开始都要重置PK条以及PK条相关缓存数据
        pkDateList.clear()
        //置0是为了防止过滤掉新一轮的消息
        lastDataOfPk?.totalScore = 0L

        //每次一定先把本主播排在首位
        val curProgramId = playerViewModel.programId
        var self: PKUser? = null
        pkinfo.detailList?.forEach { it ->
            if (it.programId == curProgramId)
                self = it
        }

        if (self != null) {
            pkinfo.detailList?.remove(self!!)
            pkinfo.detailList?.add(0, self!!)
        }
        //给段位信息排序
        pkinfo.stageList?.let { stageList ->
            var stageSelf: PkStage? = null
            stageList.forEach {
                if (it.programId == curProgramId) {
                    stageSelf = it
                }
            }
            if (stageSelf != null) {
                stageList.remove(stageSelf!!)
                stageList.add(0, stageSelf!!)
            }
        }
        //第一时间进行倒计时 动画做完再开始就延迟了
        if (pkinfo.seconds != null && pkinfo.seconds!! > 0) {
            //设置定时器 先不展示 动画完成再展示
//            pk_time_layout.show()
            pKViewModel.closeCountDown()
            pKViewModel.countDown(getTimeTitle(pkinfo), pkinfo.seconds)
        }
        when (currentPkType) {
            TWO_PK -> {
                pk2_process.show()
                pk3_process.hide()
                setStageData(pkinfo.stageList)
                queueNotifyPk(currentPKData ?: return)
                if (pkinfo.currRound == 1) {
                    startTwoPkAnimator(pkinfo)
                } else {
                    playTitleAni(pkinfo, true)
                    showViewAfterAni()
                }
            }
            THREE_PK -> {
                pk2_process.hide()
                pk3_process.show()
                queueNotifyPk(currentPKData ?: return)
                if (pkinfo.currRound == 1) {
                    startThreePkAnimator(pkinfo)
                } else {
                    playTitleAni(pkinfo, true)
                    showViewAfterAni()
                }
            }
        }
    }

    fun justSetPk(pkData: PKInfoBean) {
        if (pkData.pkType == PKType.LANDLORD) {
//            landlordsScoreChange(pkData)
            return
        }
        currentPKData = pkData
        currentPkType = pkData.detailList?.size ?: -1
        if (currentPkType <= 0) {
            logger.info("pk参数有误")
            return
        }
        //每次一定先把本主播排在首位
        val curProgramId = playerViewModel.programId
        var self: PKUser? = null
        pkData.detailList?.forEach { it ->
            if (it.programId == curProgramId)
                self = it
        }
        if (self != null) {
            pkData.detailList?.remove(self!!)
            pkData.detailList?.add(0, self!!)
        }
        //给段位信息排序
        pkData.stageList?.let { stageList ->
            var stageSelf: PkStage? = null
            stageList.forEach {
                if (it.programId == curProgramId) {
                    stageSelf = it
                }
            }
            if (stageSelf != null) {
                stageList.remove(stageSelf!!)
                stageList.add(0, stageSelf!!)
            }
        }
        //第一时间进行倒计时 动画做完再开始就延迟了
        if (pkData.seconds != null && pkData.seconds!! > 0) {
            //设置定时器
            pk_time_layout.show()
            pKViewModel.closeCountDown()
            pKViewModel.countDown(getTimeTitle(pkData), pkData.seconds)
        } else {
            pk_time_layout.hide()
        }
        when (currentPkType) {
            TWO_PK -> {
                pk2_process.show()
                pk3_process.hide()
//                pk_title_img.setImageResource(R.mipmap.pk_title_two)
                val stageList = pkData.stageList
                setStageData(stageList)
            }
            THREE_PK -> {
                pk2_process.hide()
                pk3_process.show()
//                pk_title_img.setImageResource(R.mipmap.pk_title_three)
            }
        }
        queueNotifyPk(pkData)
    }

    //展示PK结束环节
    fun showPkEndRound(info: PKInfoBean) {
        playTitleAni(info, true)
        //第一时间进行倒计时 动画做完再开始就延迟了
        if (info.seconds != null && info.seconds!! > 0) {
            //设置定时器
            pk_time_layout.show()
            pKViewModel.closeCountDown()
            pKViewModel.countDown(getTimeTitle(info), info.seconds)
        } else {
            pk_time_layout.hide()
        }

    }

    //展示pk结果
    fun showPkResult(data: PKResultEventNew) {
        val info = data.pkInfo ?: return
        if (info.pkType == PKType.LANDLORD) {
//            landlordsResult(data)
            return
        }

        logger.info("展示pk结果 回合${info.currRound}")
        resetPkResultView()
        //防止pk动画还没完成但是结果已经来了(取消动画 隐藏布局)
        pk2Set?.cancel()
        pk3Set?.cancel()
        two_pk_container.hide()
        three_pk_container.hide()

        //关闭倒计时
        pKViewModel.closeCountDown()
        pk_time_layout.hide()
        //隐藏道具
        pk_props_layout.hide()
        //隐藏道具面板
        pk_props_panel.hide()
        //此时直接隐藏云雾
        pk_cloud.hide()
        myHandler.removeCallbacks(cloudRunnable)

        //注意   每次一定先把本主播排在首位
        val curProgramId = playerViewModel.programId
        var self: PKUser? = null
        info.detailList?.forEach { it ->
            if (it.programId == curProgramId)
                self = it
        }
        if (self != null) {
            info.detailList?.remove(self!!)
            info.detailList?.add(0, self!!)
        }
        //做PK段位结果动画
//        var showDanResult = false
//        data.stageDetailList?.let { stageList ->
//            var stageSelf: PkStageDetail? = null
//            run outs@{
//                stageList.forEach {
//                    if (it.programId == curProgramId) {
//                        stageSelf = it
//                        return@outs
//                    }
//                }
//            }
//            if (stageSelf != null) {
//                showDanResult = true
//                playDanResultAnimation(stageSelf!!)
//            }
//        }
        //这里将pk结果转化成pk变化 再刷新下pk值
//        val pkData = PKInfoBean().apply {
//            detailList = arrayListOf()
//            var total = 0L
//            info.detailList?.forEach { user ->
//                val newUser = PKUser()
//                newUser.roundScore = user.score
//                detailList?.add(newUser)
//                total += user.score
//            }
//            totalScore = total
//        }
        queueNotifyPk(info)
        playTitleAni(info, false)


        when (currentPkType) {
            TWO_PK -> {
                pk_win_layout_03.hide()
                //播放奖杯获取动画
                var hasCupAni = false
                if (info.detailList != null && info.detailList!!.size >= 2) {
                    info.detailList!!.forEachIndexed { index, item ->
                        if (item.roundResult == PKResultType.WIN) {
                            hasCupAni = true
                            playCupAni(1 + index, item.winRound)
                        }
                    }
                }
                //播放结果动画
                if (info.roundFinish) {
                    var delay = 0L
                    //如果当前有奖杯要播 延迟执行
                    if (hasCupAni) {
                        delay = 500L
                    }
                    myHandler.postDelayed({
                        if (info.detailList != null && info.detailList!!.size >= 2) {
                            info.detailList!!.forEachIndexed { index, item ->
                                playFinalResultAni(index + 1, item.finalResult)
                            }
                        }

                    }, delay)
                }

            }
            THREE_PK -> {
                pk_win_layout_03.show()
                //播放奖杯获取动画
                if (info.detailList != null && info.detailList!!.size >= 3) {
                    info.detailList!!.forEachIndexed { index, item ->
                        if (item.roundResult == PKResultType.WIN) {
                            playCupAni(1 + index, item.winRound)
                        }
                    }
                }
                //播放结果动画
                if (info.roundFinish) {
                    if (info.detailList != null && info.detailList!!.size >= 3) {
                        info.detailList!!.forEachIndexed { index, item ->
                            playFinalResultAni(index + 1, item.finalResult)
                        }
                    }
                }

            }
        }




        pk_result_layout.show()

    }

    /**
     * 设置PK段位赛相关
     */
    private fun setStageData(stageList: ArrayList<PkStage>?) {
        if (stageList != null) {
            pk_dan_level_img01.show()
            pk_dan_level_img02.show()
            val dan1 = stageList.getOrNull(0)?.icon ?: ""
            pk_dan_level_img01.loadImage(dan1, 26f, 26f)
            val dan2 = stageList.getOrNull(1)?.icon ?: ""
            pk_dan_level_img02.loadImage(dan2, 26f, 26f)
        } else {
            pk_dan_level_img01.hide()
            pk_dan_level_img02.hide()
        }
    }

    /**
     *   播放SVGA动画  [showAni]是否需要动画特效 不需要的话就直接展示并返回
     */

    private fun startPlaySvga(logo: ImageView, svga: SVGAPlayerView, logoRes: Int, url: String?, showAni: Boolean = true) {
        if (url == null) {
            return
        }
        //如果不需要动画 直接展示
        if (!showAni) {
            myHandler.postDelayed({
                logo?.setImageResource(logoRes)
                logo?.show()
            }, 200)
            return
        }
        val callbacks: SVGAParser.ParseCompletion = object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                svga.setVideoItem(videoItem)
                svga.startAnimation()
            }

            override fun onError() {}
        }
        svga.callback = object : SVGACallback {
            override fun onFinished() {
                myHandler.postDelayed({
                    logo?.setImageResource(logoRes)
                    logo?.show()
                }, 200)
            }

            override fun onPause() {

            }

            override fun onRepeat() {

            }

            override fun onStep(frame: Int, percentage: Double) {

            }

        }
        svga.show()
        SVGAHelper.startParse(url, callbacks)
    }

    /**
     * 播放最终胜负结果[index] 1,2,3 注意从1开始
     */
    private fun playFinalResultAni(index: Int, result: String) {
        var targetView: ImageView? = null
        var bgView: ImageView? = null
        when (index) {
            1 -> {
                targetView = pk_iv_result_logo_01
                bgView = pk_iv_result_bg_01
            }
            2 -> {
                targetView = pk_iv_result_logo_02
                bgView = pk_iv_result_bg_02
            }
            3 -> {
                targetView = pk_iv_result_logo_03
                bgView = pk_iv_result_bg_03
            }
        }
        targetView ?: return
        bgView ?: return
        targetView.show()
        bgView.show()
        targetView.translationY = 0f
        bgView.translationY = 0f
        when (result) {
            PKResultType.WIN -> {
                targetView.imageResource = R.mipmap.icon_pk_result_win
                bgView.imageResource = R.mipmap.bg_pk_result_win
            }
            PKResultType.DRAW -> {
                targetView.imageResource = R.mipmap.icon_pk_result_draw
                bgView.imageResource = R.mipmap.bg_pk_result_draw
            }

            PKResultType.LOSE -> {
                targetView.imageResource = R.mipmap.icon_pk_result_lose
                bgView.imageResource = R.mipmap.bg_pk_result_lose
            }

        }

        val ani11 = ObjectAnimator.ofFloat(targetView, View.SCALE_X, 0f, 1.2f, 1.0f)
        val ani12 = ObjectAnimator.ofFloat(targetView, View.SCALE_Y, 0f, 1.2f, 1.0f)
        val ani13 = ObjectAnimator.ofFloat(bgView, View.SCALE_X, 0f, 1.2f, 1.0f)
        val ani14 = ObjectAnimator.ofFloat(bgView, View.SCALE_Y, 0f, 1.2f, 1.0f)
        val ani1 = AnimatorSet()
        ani1.interpolator = AccelerateInterpolator()
        ani1.duration = 750
        ani1.playTogether(ani11, ani12, ani13, ani14)

        val ani2 = ObjectAnimator.ofFloat(bgView, View.ROTATION, 0f, 360f).apply {
            this.repeatMode = ValueAnimator.RESTART
            this.repeatCount = ValueAnimator.INFINITE
            this.interpolator = LinearInterpolator()
            this.duration = 5000L
        }
        val ani31 = ObjectAnimator.ofFloat(targetView, View.ALPHA, 0f, 1f)
        val ani32 = ObjectAnimator.ofFloat(bgView, View.ALPHA, 0f, 1f)
        val ani3 = AnimatorSet()
        ani3.playTogether(ani31, ani32)
        ani3.duration = 500

//        logger.info("结果图标下移=${pkResultIconDy}")
        val ani41 = ObjectAnimator.ofFloat(targetView, View.TRANSLATION_Y, 0f, pkResultIconDy)
        val ani42 = ObjectAnimator.ofFloat(bgView, View.TRANSLATION_Y, 0f, pkResultIconDy)
        val ani4 = AnimatorSet()
        ani4.playTogether(ani41, ani42)
        ani4.startDelay = 1450
        ani4.duration = 300
        ani4.addListener(onEnd = {
            ani2.cancel()
            bgView?.hide()
        })
//        finalResultAniSet?.cancel()
        val set = AnimatorSet()
        set.play(ani1).with(ani2).with(ani3).before(ani4)
        set.start()
    }

    /**
     * 展示PK结果静态的
     */
    private fun showFinalResultStatic(index: Int, result: String) {
        var targetView: ImageView? = null
        when (index) {
            1 -> {
                targetView = pk_iv_result_logo_01
            }
            2 -> {
                targetView = pk_iv_result_logo_02
            }
            3 -> {
                targetView = pk_iv_result_logo_03
            }
        }
        targetView ?: return
        targetView.show()
        targetView.translationY = pkResultIconDy
        when (result) {
            PKResultType.WIN -> {
                targetView.imageResource = R.mipmap.icon_pk_result_win
            }
            PKResultType.DRAW -> {
                targetView.imageResource = R.mipmap.icon_pk_result_draw
            }

            PKResultType.LOSE -> {
                targetView.imageResource = R.mipmap.icon_pk_result_lose
            }
            else -> {
                targetView.hide()
            }

        }
    }

    /**
     *  播放一轮胜利后获取奖杯的动画
     *  [index] 1,2,3 注意从1开始 要做动画的位置
     *  [winRound]当前获取到的奖杯数
     */
    private fun playCupAni(index: Int, winRound: Int, needAni: Boolean = true) {
        if (currentPkType == TWO_PK) {
            pk_ll_cups_02.gravity = Gravity.END
            pk_ll_cups_02.rightPadding = dp2px(5)
        } else {
            pk_ll_cups_02.gravity = Gravity.START
            pk_ll_cups_02.rightPadding = 0
        }
        post {

            logger.info("playCupAni index=$index winRound=$winRound")
            var targetView: ImageView? = null
            var bigImageView: ImageView? = null
            when (index) {
                1 -> {
                    bigImageView = pk_iv_big_cup_01
                    when (winRound) {
                        1 -> {
                            targetView = pk_iv_cup_11
                        }
                        2 -> {
                            pk_iv_cup_11.show()
                            targetView = pk_iv_cup_12
                        }
                        3 -> {
                            pk_iv_cup_11.show()
                            pk_iv_cup_12.show()
                            targetView = pk_iv_cup_13
                        }
                    }

                }
                2 -> {
                    bigImageView = pk_iv_big_cup_02
                    when (winRound) {
                        1 -> {
                            if (currentPkType == TWO_PK) {
                                targetView = pk_iv_cup_23
                            } else {
                                targetView = pk_iv_cup_21
                            }

                        }
                        2 -> {
                            if (currentPkType == TWO_PK) {
                                pk_iv_cup_23.show()
                            } else {
                                pk_iv_cup_21.show()
                            }
                            targetView = pk_iv_cup_22
                        }
                        3 -> {
                            if (currentPkType == TWO_PK) {
                                pk_iv_cup_23.show()
                                pk_iv_cup_22.show()
                                targetView = pk_iv_cup_21
                            } else {
                                pk_iv_cup_21.show()
                                pk_iv_cup_22.show()
                                targetView = pk_iv_cup_23
                            }

                        }
                    }

                }
                3 -> {
                    bigImageView = pk_iv_big_cup_03
                    when (winRound) {
                        1 -> {
                            targetView = pk_iv_cup_31
                        }
                        2 -> {
                            pk_iv_cup_31.show()
                            targetView = pk_iv_cup_32
                        }
                        3 -> {
                            pk_iv_cup_31.show()
                            pk_iv_cup_32.show()
                            targetView = pk_iv_cup_33
                        }
                    }

                }
            }
            bigImageView ?: return@post
            targetView ?: return@post
            if (!needAni) {
                logger.info("不需要动画 直接展示")
                targetView.show()
                return@post
            }
            bigImageView.show()
            //重置偏移量 以及缩放值
            bigImageView.translationX = 0f
            bigImageView.translationY = 0f
            bigImageView.scaleX = 1f
            bigImageView.scaleY = 1f
            val ani11 = ObjectAnimator.ofFloat(bigImageView, View.SCALE_X, 0f, 1.1f, 1.0f)
            val ani12 = ObjectAnimator.ofFloat(bigImageView, View.SCALE_Y, 0f, 1.1f, 1.0f)
            val ani1 = AnimatorSet()
            ani1.interpolator = AccelerateInterpolator()
            ani1.duration = 650
            ani1.playTogether(ani11, ani12)


            val location1 = IntArray(2)
            bigImageView.getLocationOnScreen(location1)
            val location2 = IntArray(2)
            targetView.getLocationOnScreen(location2)
            val dx = location1[0] - location2[0] + (bigImageView.width - targetView.width) / 2
            val dy = location1[1] - location2[1] + (bigImageView.height - targetView.height) / 2

//            logger.info("当前奖杯的坐标---x=${location1[0]} y=${location1[1]} ")
//            logger.info("当前奖杯平移---dx=${dx} dy=${dy} ")
//            val scale = targetView.width.toFloat() / bigImageView.width
            val ani21 = ObjectAnimator.ofFloat(bigImageView, View.TRANSLATION_X, 0f, -dx.toFloat())
            val ani22 = ObjectAnimator.ofFloat(bigImageView, View.TRANSLATION_Y, 0f, -dy.toFloat())

//            val anim21 = ValueAnimator.ofInt(dp2px(50), dp2px(5))
//            val bllp = bigImageView.layoutParams as ConstraintLayout.LayoutParams
//            anim21.addUpdateListener { valueAnimate ->
//                logger.info("anim21 ${valueAnimate.animatedValue}")
//                bllp.bottomMargin = valueAnimate.animatedValue as Int
//                bigImageView.requestLayout()
//            }
//            val anim22 = ValueAnimator.ofInt(originLeftMargn, dp2px(5 + (5 + 29) * (winRound-1)))
//            anim22.addUpdateListener { valueAnimate ->
//                logger.info("anim22 ${valueAnimate.animatedValue}")
//                bllp.leftMargin = valueAnimate.animatedValue as Int
//                bigImageView.requestLayout()
//            }
            val ani23 = ObjectAnimator.ofFloat(bigImageView, View.SCALE_X, 1.0f, 0.5f)
            val ani24 = ObjectAnimator.ofFloat(bigImageView, View.SCALE_Y, 1.0f, 0.5f)
            val ani2 = AnimatorSet()
            ani2.duration = 500 * 2
            ani2.startDelay = 450
            ani2.playTogether(ani21, ani22, ani23, ani24)
            val cupAniAniSet = AnimatorSet()
            cupAniAniSet.playSequentially(ani1, ani2)
            cupAniAniSet.addListener(onEnd = {
                bigImageView?.hide()
//                targetView.imageResource = R.mipmap.icon_pk_cup
                targetView?.show()
            })
            cupAniAniSet.start()

        }
    }

    /**
     * 播放标题动画
     */
    private var titleAniSet: AnimatorSet? = null

    /**
     * [start]是不是小局开始
     */
    private fun playTitleAni(info: PKInfoBean, start: Boolean) {
        val round = info.currRound
        var title = ""
        val first = info.detailList?.getOrNull(0) ?: return
        if (start) {
            when (round) {
                1 -> {
                    title = "第一小局开始"
                }
                2 -> {
                    title = "第二小局开始"
                }
                3 -> {
                    title = "第三小局开始"
                }
            }

        } else {
            val txt = when (first.roundResult) {
                PKResultType.WIN -> {
                    "我方获胜"
                }
                PKResultType.DRAW -> {
                    "双方平局"
                }
                PKResultType.LOSE -> {
                    "对方获胜"
                }
                else -> ""

            }
            when (round) {
                1 -> {
                    title = "第一小局 $txt"
                }
                2 -> {
                    title = "第二小局 $txt"
                }
                3 -> {
                    title = "第三小局 $txt"
                }
            }

        }
        if (info.punishRound) {
            if (first.finalResult == PKResultType.WIN) {
                title = "对方主播接受惩罚"
            } else {
                title = "我方主播接受惩罚"
            }
        }
        if (info.endRound) {
            title = "即将退出PK"
        }
        pk_title.text = title
        pk_title.show()
        if (titleAniSet == null) {
            val ani11 = ObjectAnimator.ofFloat(pk_title, View.SCALE_X, 0f, 1.0f)
            val ani12 = ObjectAnimator.ofFloat(pk_title, View.SCALE_Y, 0f, 1.0f)
            val ani1 = AnimatorSet()
            ani1.playTogether(ani11, ani12)
            ani1.interpolator = AnticipateOvershootInterpolator()
            ani1.duration = 700
            val ani2 = ObjectAnimator.ofFloat(pk_title, View.ALPHA, 0f, 1f)
            ani2.duration = 400

            val ani3 = ObjectAnimator.ofFloat(pk_title, View.ALPHA, 1f, 0f)
            ani3.startDelay = 900
            ani3.duration = 400
            titleAniSet = AnimatorSet()
            titleAniSet!!.play(ani2).with(ani1).before(ani3)
            titleAniSet!!.addListener(onEnd = {
                pk_title?.hide()
            })
        }
        titleAniSet?.cancel()
        titleAniSet?.start()
    }

    //防止泄露
    private fun releasePkView() {
        pk2Set?.cancel()
        pk3Set?.cancel()
        pKViewModel.closeCountDown()
//        pk_win_sign_01.callback = null
//        pk_win_sign_02.callback = null
//        pk_win_sign_03.callback = null
        pk_vs_ani.callback = null
        isNotify = false
        myHandler.removeCallbacksAndMessages(null)
        pkDateList.clear()
        titleAniSet?.cancel()
    }

    //重新初始化 pk结束调用
    fun resetPkView() {
        logger.info("resetPkView")
        pk2Set?.cancel()
        pk3Set?.cancel()
        pKViewModel.closeCountDown()
        lastDataOfPk = null
        currentPKData = null
        currentPkType = -1
        isNotify = false
        pkDateList.clear()
        //2人PK复原
        val lp21 = pk2_process_01.layoutParams as ConstraintLayout.LayoutParams
        lp21.horizontalWeight = 1f
        pk2_process_text01.text = ""
        pk2_process_text02.text = ""
//        pk2_process_text01.isNeedFormat = false
//        pk2_process_text02.isNeedFormat = false

        //三人pk复原
        pk3_process_text_01.text = ""
        pk3_process_text_02.text = ""
        pk3_process_text_03.text = ""
        val lp31 = pk3_process_01.layoutParams as FrameLayout.LayoutParams
        val lp32 = pk3_process_02.layoutParams as FrameLayout.LayoutParams
        val lp33 = pk3_process_03.layoutParams as FrameLayout.LayoutParams
        lp31.width = dip(16)
        lp32.width = dip(16)
        lp33.width = dip(16)



        pk2_process.hide()
        pk3_process.hide()
        pk_time_layout.hide()
//        pk2_title_img.hide()

        two_pk_container.hide()
        three_pk_container.hide()
        pk_result_layout.hide()

        //新增PK道具复原
        pk_props_panel.reset()
        //隐藏道具
        pk_props_layout.hide()
        //隐藏道具面板
        pk_props_panel.hide()
        //此时直接隐藏云雾
        myHandler.removeCallbacks(cloudRunnable)
        pk_cloud.hide()
        //隐藏段位结果
        pk_dan_result.hide()
        //此时直接隐藏飞机特效
        myHandler.removeCallbacks(speedRunnable)
        resetSpeed()
        this.hide()
        //关闭正在播放的结果动画
//        pk_win_sign_01.stopAnimation()
//        pk_win_sign_02.stopAnimation()
//        pk_win_sign_03.stopAnimation()
        //隐藏左右两个段位赛图标
        pk_dan_level_img01?.hide()
        pk_dan_level_img02?.hide()

        //斗地主
//        landlordView.hide()
        // 重置奖杯
        resetPkCupView()
        //重置PK结果
        resetPkResultView()
    }

    /**
     * 重置奖杯视图相关
     */
    private fun resetPkCupView() {
        pk_iv_big_cup_01.inVisible()
        pk_iv_big_cup_02.inVisible()
        pk_iv_big_cup_03.inVisible()

        pk_ll_cups_01.inVisibleAll()
        pk_ll_cups_02.inVisibleAll()
        pk_ll_cups_03.inVisibleAll()

    }

    /**
     * 重置pk结果相关
     */
    private fun resetPkResultView() {
        pk_iv_result_logo_01.hide()
        pk_iv_result_logo_02.hide()
        pk_iv_result_logo_03.hide()

        pk_iv_result_bg_01.hide()
        pk_iv_result_bg_02.hide()
        pk_iv_result_bg_03.hide()
        pk_result_layout.hide()
    }

    private var pk2Set: AnimatorSet? = null

    private fun startTwoPkAnimator(pkinfo: PKInfoBean) {
        pkinfo.detailList?.let { list ->
            if (list.size >= 2) {
                two_pk_container.setLeftViewData(list[0].headPic, list[0].nickname)
                two_pk_container.setRightViewData(list[1].headPic, list[1].nickname)
                two_pk_container.startAnimation {
                    playTitleAni(pkinfo, true)
                    showViewAfterAni()
                }
            }
        }

    }

    private var pk3Set: AnimatorSet? = null
    private fun startThreePkAnimator(pkinfo: PKInfoBean) {
        two_pk_container.hide()
        three_pk_container.show()
//        pk_title_img.setImageResource(R.mipmap.pk_title_three)
        //赋值
        pkinfo.detailList?.let { list ->
            if (list.size >= 3) {
                pk3_anchor_name_01.text = "${list[0].nickname}"
                pk3_anchor_name_02.text = "${list[1].nickname}"
                pk3_anchor_name_03.text = "${list[2].nickname}"
                ImageUtils.loadImage(pk3_anchor_img01, list[0].headPic ?: return, 30f, 30f)
                ImageUtils.loadImage(pk3_anchor_img02, list[1].headPic ?: return, 30f, 30f)
                ImageUtils.loadImage(pk3_anchor_img03, list[2].headPic ?: return, 30f, 30f)
            }
        }
        val screenWidth = ScreenUtils.screenWidthFloat
        val pk3size = screenWidth / 3 - dip(5) * 2 //三人展示的最大宽度
        val pk3sizeSmall = dip(44)
        val aniDuration31 = 400L

        val ctp1 = pk3_container_01.layoutParams as ConstraintLayout.LayoutParams
        val ctp2 = pk3_container_02.layoutParams as ConstraintLayout.LayoutParams
        val ctp3 = pk3_container_03.layoutParams as ConstraintLayout.LayoutParams
        if (pk3Set != null) {
            logger.info("已经存在set3 执行重置")
            pk3Set?.cancel()
            pk3_container_01.alpha = 1.0f
            pk3_container_02.alpha = 1.0f
            pk3_container_03.alpha = 1.0f
            pk3Set?.start()
            return
        }

        val animator31 = ValueAnimator.ofInt(pk3sizeSmall, pk3size.toInt(), (pk3size - dip(8)).toInt())
        animator31.duration = aniDuration31
        animator31.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                pk3_container_01.show()
                pk3_container_02.show()
                pk3_container_03.show()
            }

            override fun onAnimationEnd(animation: Animator?) {
                pk_vs_ani.show()
                pk_vs_ani.startAnimation()
            }
        })
        animator31.addUpdateListener { value ->
            ctp1.width = value.animatedValue as Int
            ctp2.width = value.animatedValue as Int
            ctp3.width = value.animatedValue as Int
            pk3_container_01.requestLayout()
            pk3_container_02.requestLayout()
            pk3_container_03.requestLayout()
        }

        val animator32 = ValueAnimator.ofFloat(1.0f, 0.0f)
        animator32.duration = 100L
        animator32.startDelay = VS_PLAY_TIME
        animator32.addUpdateListener { value ->
            val al = value.animatedValue as Float
            pk3_container_01.alpha = al
            pk3_container_02.alpha = al
            pk3_container_03.alpha = al
        }
        animator32.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                pk_vs_ani.hide()
            }

            override fun onAnimationEnd(animation: Animator?) {
                pk3_container_01.hide()
                pk3_container_02.hide()
                pk3_container_03.hide()


                playTitleAni(pkinfo, true)
                showViewAfterAni()
            }
        })
        pk3Set = AnimatorSet()
        pk3Set?.playSequentially(animator31, animator32)
        pk3Set?.start()
    }

    private fun showViewAfterAni() {
        pk_time_layout.show()
        //
        showPkTitleImg()

        videoViewModel.showVideoInfo.value = true
    }

    private fun showPkTitleImg() {
//        when (currentPkType) {
//            THREE_PK, LANDLORD -> {
//                pk2_title_img.hide()
//            }
//            TWO_PK -> {
//                if (currentPKData?.logoPic != null)
//                    ImageUtils.loadImage(pk2_title_img, currentPKData!!.logoPic!!, 54f, 38f)
//                else
//                    ImageUtils.loadImageLocal(pk2_title_img, R.mipmap.pk_normal_flag)
//                if (playerViewModel?.chatModeState?.value != true) {
//                    pk2_title_img.show()
//                } else {
//                    pk2_title_img.hide()
//                }
//            }
//        }
    }

    private fun getTimeTitle(pkData: PKInfoBean): String {
        var title = ""
        when (pkData.currRound) {
            1 -> {
                title = "R1"
            }
            2 -> {
                title = "R2"
            }
            3 -> {
                title = "R3"
            }
        }
        if (pkData.punishRound) {
            title = "惩罚"
        }
        if (pkData.endRound) {
            title = "结束"
        }
        return title
    }

    private val pkDateList: LinkedList<PKInfoBean> = LinkedList()    //等待播放的队列
    private var isNotify = false
    private fun queueNotifyPk(pkData: PKInfoBean) {
        pkDateList.add(pkData)
        if (isNotify) {
            return
        }
        when (pkData.pkType) {
            PKType.LANDLORD -> {
//                notifyLandlordsProgress()
            }
            else -> {
                notifyPkProcess()
            }
        }
    }

    private var lastDataOfPk: PKInfoBean? = null//记录上一次的数据

    //改变pk数值
    private fun notifyPkProcess() {
//        playSpeedAnimation(80,1111)
        if (pkDateList.isEmpty()) {
            logger.info("没有刷新任务了")
            return
        }
        val pkData: PKInfoBean = pkDateList.pop()
        if (/*lastDataOfPk != null &&*/ pkData.totalScore ?: 0L < lastDataOfPk?.totalScore ?: 0L) {
            logger.info("pk消息顺序错了 不再刷新pk值 cur:$pkData last:$lastDataOfPk")
            reportCrash("pk消息顺序错了 不再刷新pk值cur:$pkData last:$lastDataOfPk")
            return
        }
//        logger.info("刷新pk数值 队列=${pkDateList.size}")
        isNotify = true
        when (currentPkType) {
            TWO_PK -> {
//                pk2_title_img.show()
//                //
//                if (pkData.logoPic != null)
//                    ImageUtils.loadImage(pk2_title_img, pkData.logoPic!!, 54f, 38f)
//                else
//                    ImageUtils.loadImageLocal(pk2_title_img, R.mipmap.pk_normal_flag)
                bezier_02.hide()
                //二人PK  只改变第一个玩家的占比 //假设第二个权重一直为1
                val lp1 = pk2_process_01.layoutParams as ConstraintLayout.LayoutParams
                val start: Float?
                val end: Float?
                //如果lastDataOfPk为空 可能代表重置了
//                val isReset = lastDataOfPk == null
                var lastPlayer1: PKUser? = null
                var lastPlayer2: PKUser? = null

                var player1: PKUser? = null

                var player2: PKUser? = null
                //上一次的数据
                lastDataOfPk?.detailList?.let { list ->
                    if (list.size >= 2) {
                        lastPlayer1 = list[0]
                        lastPlayer2 = list[1]
                    }
                }

                pkData.detailList?.let { list ->
                    if (list.size >= 2) {
                        player1 = list[0]
                        player2 = list[1]
                    }
                }

                if (player1 != null && player2 != null) {
                    val playerScore1 = player1?.roundScore ?: 0
                    val playerScore2 = player2?.roundScore ?: 0
                    val lastPlayerScore1 = lastPlayer1?.roundScore ?: 0
                    val lastPlayerScore2 = lastPlayer2?.roundScore ?: 0
                    pk2_process_text01.setNumberString("$lastPlayerScore1", "$playerScore1")
                    pk2_process_text02.setNumberString("$lastPlayerScore2", "$playerScore2")

                    //score1/score2得出是score2的多少倍
                    start = if (lastPlayerScore1 == 0L || lastPlayerScore2 == 0L) {
                        (lastPlayerScore1.toFloat() + 1) / (lastPlayerScore2 + 1)
                    } else {
                        lastPlayerScore1.toFloat() / lastPlayerScore2
                    }
                    end = if (playerScore1 == 0L || playerScore2 == 0L) {
                        (playerScore1.toFloat() + 1) / (playerScore2 + 1)
                    } else {
                        playerScore1.toFloat() / playerScore2
                    }
                    //有变化时才做动画 isReset
                    if (end != start /*|| isReset*/) {
                        val animator21 = ValueAnimator.ofFloat(start, end)
                        animator21.duration = 300L
                        animator21.addUpdateListener { value ->
                            //                            logger.info("进度的权重刷新${value.animatedValue}")
                            lp1.horizontalWeight = value.animatedValue as Float
                            pk2_process_01.requestLayout()
                            pk2_process_02.requestLayout()
                        }
                        animator21.start()
                        animator21.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                isNotify = false
                                notifyPkProcess()
                            }
                        })
                    } else {
                        isNotify = false
                        notifyPkProcess()
                    }

                    val difScore1 = playerScore1 - lastPlayerScore1
                    val difScore2 = playerScore2 - lastPlayerScore2

                    //飘数字
                    if (difScore1 > 0 && pkData.needAnim)
                        bezier_01.addNumberView(difScore1, ContextCompat.getColor(context, R.color.pk_color_01))
                    if (difScore2 > 0 && pkData.needAnim)
                        bezier_03.addNumberView(difScore2, ContextCompat.getColor(context, R.color.pk_color_03))
                    //新增PK道具变化
                    val playerPropScore1 = player1?.propScore ?: 0L
                    val playerPropScore2 = player2?.propScore ?: 0L
                    val lastPlayerPropScore1 = lastPlayer1?.propScore ?: 0L
                    val lastPlayerPropScore2 = lastPlayer2?.propScore ?: 0L
                    notifyPkProp(lastPlayerPropScore1, lastPlayerPropScore2, playerPropScore1, playerPropScore2)
                }

            }
            THREE_PK -> {
//                pk2_title_img.hide()
                bezier_02.show()
                val lp1 = pk3_process_01.layoutParams as FrameLayout.LayoutParams
                val lp2 = pk3_process_02.layoutParams as FrameLayout.LayoutParams
                val lp3 = pk3_process_03.layoutParams as FrameLayout.LayoutParams
                val screenWidth = ScreenUtils.screenWidthFloat
                val maxProcessWidth = screenWidth / 3 - dip(3) * 2 - dip(16) //进度条的最大宽度 16是保留宽度
                logger.info("maxProcessWidth:$maxProcessWidth")

                val maxLimit = 0.95f
                //三人PK 先不加
                var lastPlayer1: PKUser? = null
                var lastPlayer2: PKUser? = null
                var lastPlayer3: PKUser? = null

                var player1: PKUser? = null
                var player2: PKUser? = null
                var player3: PKUser? = null

                //上一次的数据
                lastDataOfPk?.detailList?.let { list ->
                    if (list.size >= 3) {
                        lastPlayer1 = list[0]
                        lastPlayer2 = list[1]
                        lastPlayer3 = list[2]
                    }
                }

                pkData.detailList?.let { list ->
                    if (list.size >= 3) {
                        player1 = list[0]
                        player2 = list[1]
                        player3 = list[2]
                    }
                }
                if (player1 != null && player2 != null && player3 != null) {
                    val playerScore1 = player1?.roundScore ?: 0
                    val playerScore2 = player2?.roundScore ?: 0
                    val playerScore3 = player3?.roundScore ?: 0

                    val lastPlayerScore1 = lastPlayer1?.roundScore ?: 0
                    val lastPlayerScore2 = lastPlayer2?.roundScore ?: 0
                    val lastPlayerScore3 = lastPlayer3?.roundScore ?: 0

                    pk3_process_text_01.setNumberString("$lastPlayerScore1", "$playerScore1")
                    pk3_process_text_02.setNumberString("$lastPlayerScore2", "$playerScore2")
                    pk3_process_text_03.setNumberString("$lastPlayerScore3", "$playerScore3")


                    // 最低宽度 dip(16)
                    val total = pkData.totalScore


                    //111111111111111111111-------------------------------------------------------------------------
                    //占满了换背景
                    if (total != null) {
                        val rate1 = (playerScore1.toFloat() / total.toFloat())
                        logger.info("score1=$rate1")
                        if (rate1 >= maxLimit) {
                            pk3_process_01.setBackgroundResource(R.drawable.pk_process_color_01_all)
                        } else {
                            pk3_process_01.setBackgroundResource(R.drawable.pk_process_color_01)
                        }
                    }
                    //只有变化大于0时才做动画
                    val difScore1 = playerScore1 - lastPlayerScore1
                    val width1 = if (playerScore1 > 0 && total != null) {
                        dip(16) + (playerScore1.toFloat() / total.toFloat() * maxProcessWidth).toInt()
                    } else {
                        dip(16)
                    }
                    val ani1 = ValueAnimator.ofInt(lp1.width, width1)
                    ani1.addUpdateListener { valueAnimate ->
                        lp1.width = valueAnimate.animatedValue as Int
                    }
                    ani1.duration = 200
                    ani1.start()
                    //飘数字
                    if (difScore1 > 0 && pkData.needAnim)
                        bezier_01.addNumberView(difScore1, ContextCompat.getColor(context, R.color.pk_color_01))
                    //22222222222222222222222222-------------------------------------------------------------------
                    //占满了换背景
                    if (total != null) {
                        val rate2 = (playerScore2.toFloat() / total.toFloat())
                        logger.info("score2=$rate2")
                        if (rate2 >= maxLimit) {
                            pk3_process_02.setBackgroundResource(R.drawable.pk_process_color_02_all)
                        } else {
                            pk3_process_02.setBackgroundResource(R.drawable.pk_process_color_02)
                        }
                    }
                    val difScore2 = playerScore2 - lastPlayerScore2
                    val width2 = if (playerScore2 > 0 && total != null) {
                        dip(16) + (playerScore2.toFloat() / total.toFloat() * maxProcessWidth).toInt()
                    } else {
                        dip(16)
                    }

                    val ani2 = ValueAnimator.ofInt(lp2.width, width2)
                    ani2.addUpdateListener { valueAnimate ->
                        lp2.width = valueAnimate.animatedValue as Int
                    }
                    ani2.duration = 200
                    ani2.start()
                    if (difScore2 > 0 && pkData.needAnim)
                        bezier_02.addNumberView(difScore2, ContextCompat.getColor(context, R.color.pk_color_02))
                    //3333333333333333333333333-----------------------------------------------------------------------
                    //占满了换背景
                    if (total != null) {
                        val rate3 = (playerScore3.toFloat() / total.toFloat())
                        logger.info("score3=$rate3")
                        if (rate3 >= maxLimit) {
                            pk3_process_03.setBackgroundResource(R.drawable.pk_process_color_03_all)
                        } else {
                            pk3_process_03.setBackgroundResource(R.drawable.pk_process_color_04)
                        }
                    }
                    val difScore3 = playerScore3 - lastPlayerScore3
                    val width3 = if (player3?.roundScore != null && total != null) {
                        dip(16) + (player3!!.roundScore!!.toFloat() / total.toFloat() * maxProcessWidth).toInt()
                    } else {
                        dip(16)
                    }

                    val ani3 = ValueAnimator.ofInt(lp3.width, width3)
                    ani3.addUpdateListener { valueAnimate ->
                        lp3.width = valueAnimate.animatedValue as Int
                    }
                    ani3.duration = 200
                    //
                    ani3.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            isNotify = false
                            notifyPkProcess()
                        }
                    })
                    ani3.start()
                    if (difScore3 > 0 && pkData.needAnim)
                        bezier_03.addNumberView(difScore3, ContextCompat.getColor(context, R.color.pk_color_03))

                }

            }
        }

        lastDataOfPk = pkData

    }

    /**
     * [isPush]是不是推送的消息 不是的话处理方式会有区别
     * 设置PK道具数据
     */
    fun setPkPropsData(isPush: Boolean, propInfo: PkPropInfo) {
        logger.info("设置PK道具数据=$propInfo")
        when (propInfo.status) {
            PKPropStatus.Competing -> {
                pk_props_panel.showPropGrabIng(propInfo)
            }
            PKPropStatus.Fail -> {
                if (isPush) {
                    pk_props_panel.showPropGrabResult(false, propInfo)
                    pk_props_panel.delayToHide()
                }
            }
            PKPropStatus.UnUse -> {
                val success = playerViewModel.programId == propInfo.programId
                if (isPush) {
                    pk_props_panel.showPropGrabResult(success, propInfo)
                    pk_props_panel.delayToHide {
                        //开始展示获取的道具
                        showPropView(propInfo)
                    }
                } else {
                    //进直播间附带的道具直接展示
                    logger.info("进直播间附带的道具直接展示")
                    showPropView(propInfo)
                }

            }
            PKPropStatus.Effect -> {
                showPropView(propInfo)
                if (propInfo.propType == PKPropType.Smoke) {
                    //是本人持有的 就不隐藏
                    if (propInfo.userId.toLong() == SessionUtils.getUserId()) {
                        logger.info("烟雾道具使用者能看见")
                        return
                    }
                    if (playerViewModel.isAnchor && playerViewModel.programId == propInfo.programId) {
                        logger.info("烟雾道具使用者一方的主播能看见")
                        return
                    }
                    if (isPush) {
                        showCloud(propInfo.ttl)
                    } else {
                        showCloudStatic(propInfo.ttl)
                    }
                } else if (propInfo.propType == PKPropType.Blood || propInfo.propType == PKPropType.SmallBlood) {
                    playSpeedAnimation(propInfo.ttl, propInfo.programId)
                }

            }
            PKPropStatus.Used -> {
                showPropView(propInfo)
            }

        }

    }

    //展示PK道具
    private fun showPropView(propInfo: PkPropInfo) {
        logger.info("展示PK道具=$propInfo")
        pk_props_layout.show()
        pk_prop_01.inVisible()
        pk_prop_02.inVisible()
        when (currentPkType) {
            TWO_PK -> {
                pk_prop_03.hide()
            }
            THREE_PK -> {
                pk_prop_03.inVisible()
            }
        }
        currentPKData?.detailList?.forEachIndexed { index, pkUser ->
            if (propInfo.programId == pkUser.programId) {
                when (index) {
                    0 -> {
                        pk_prop_01.show()
                        pk_prop_01.setPropData(propInfo, playerViewModel.programId)
                    }
                    1 -> {
                        pk_prop_02.show()
                        pk_prop_02.setPropData(propInfo, playerViewModel.programId)
                    }
                    2 -> {
                        pk_prop_03.show()
                        pk_prop_03.setPropData(propInfo, playerViewModel.programId)
                    }
                }
                return
            }
        }

    }

    /**
     *
     * 刷新PK道具数值
     */
    private fun notifyPkProp(lastS1: Long, lastS2: Long, s1: Long, s2: Long) {
        if (s1 == 0L && s2 == 0L) {
            return
        }
        logger.info("刷新PK道具数值s1=$s1 s2=$s2")
        pk_props_panel.notifyPropValue(lastS1, lastS2, s1, s2)
    }

    /**
     *
     * [isPush]是不是推送的消息 不是的话处理方式会有区别
     * 设置PK礼物任务数据
     */
    fun setPkGiftTaskData(isPush: Boolean, taskInfo: PkGiftTaskInfo) {
        logger.info("setPkGiftTaskData=$taskInfo")
        when (taskInfo.status) {
            PKTaskStatus.Doing -> {
                pk_props_panel.showGiftTasking(taskInfo)
            }
            PKTaskStatus.Fail -> {
                if (isPush) {
                    pk_props_panel.showGiftTaskResult(false, taskInfo)
                }
            }
            PKTaskStatus.Finished -> {
                if (isPush) {
                    pk_props_panel.showGiftTaskResult(true, taskInfo)
                }
            }
        }

    }

    /**
     *
     * [isPush]是不是推送的消息 不是的话处理方式会有区别
     * 设置PK积分任务数据
     */
    fun setPkScoreTaskData(isPush: Boolean, taskInfo: PkScoreTaskInfo) {
        logger.info("setPkScoreTaskData=$taskInfo")
        when (taskInfo.status) {
            PKTaskStatus.Doing -> {
                pk_props_panel.showScoreTasking(taskInfo)
            }
            PKTaskStatus.Fail -> {
                if (isPush) {
                    pk_props_panel.showScoreTaskResult(false, taskInfo)
                }
            }
            PKTaskStatus.Finished -> {
                if (isPush) {
                    pk_props_panel.showScoreTaskResult(true, taskInfo)
                }
            }
        }

    }

    private val cloudRunnable: Runnable by lazy {
        Runnable {
            pk_cloud.hide()
            bezier_layout.show()
        }
    }

    private fun showCloud(stayTime: Long) {
        pk_cloud.show()
        pk_cloud.startAnimation()
        bezier_layout.hide()
        val delay = CLOUD_PLAY_TIME + stayTime * 1000L
        myHandler.removeCallbacks(cloudRunnable)
        myHandler.postDelayed(cloudRunnable, delay)
    }

    //直接展示静态
    private fun showCloudStatic(stayTime: Long) {
        pk_cloud.show()
        pk_cloud.stepToPercentage(1.0, false)
        bezier_layout.hide()
        val delay = stayTime * 1000L
        myHandler.removeCallbacks(cloudRunnable)
        myHandler.postDelayed(cloudRunnable, delay)
    }

    private fun playDanResultAnimation(pkStageDetail: PkStageDetail) {
        logger.info("开始播放段位结果")
        pk_dan_result.show()
        //设置数据
        val win = pkStageDetail.result == PKResultType.WIN
        var name = if (pkStageDetail.pkType == PKType.LANDLORD) {
            if (pkStageDetail.landlord == true) {
                "地主"
            } else {
                "平民"
            }
        } else {
            ""
        }
        if (win) {
            rl_dan_mvp.show()
            if (pkStageDetail.mvpHeadPic.isEmpty() || pkStageDetail.mvpNickName.isEmpty()) {
                rl_dan_mvp.hide()
            } else {
                rl_dan_mvp.show()
                pk_mvp_header.loadImage(pkStageDetail.mvpHeadPic, 32f, 32f)
                val mvpName = if (pkStageDetail.mvpNickName.length > 5) {
                    "${pkStageDetail.mvpNickName.substring(0, 5)}…"
                } else {
                    pkStageDetail.mvpNickName
                }
                tv_dan_mvp_name.text = mvpName
            }

            ll_dan_score.backgroundResource = R.mipmap.bg_pk_dan_success
            if (pkStageDetail.continueWinCnt > 1 && TextUtils.isEmpty(name)) {
                tv_dan_result.text = "连胜${pkStageDetail.continueWinCnt}场"
            } else {
                tv_dan_result.text = "${name}胜利"
            }
        } else {
            rl_dan_mvp.hide()
            ll_dan_score.backgroundResource = R.mipmap.bg_pk_dan_fail
            tv_dan_result.text = "${name}失败"
        }
        val score = if (pkStageDetail.stagePkScore >= 0L) {
            "段位分 +${pkStageDetail.stagePkScore}"
        } else {
            "段位分 ${pkStageDetail.stagePkScore}"
        }
        tv_dan_score.text = score
        pk_dan_level.loadImage(pkStageDetail.bigIcon, 200f, 200f)


        val firstAni = AnimatorSet()
        val aniX01 = ObjectAnimator.ofFloat(pk_dan_level, View.SCALE_X, 0f, 1.05f)
        val aniY01 = ObjectAnimator.ofFloat(pk_dan_level, View.SCALE_Y, 0f, 1.05f)
        firstAni.duration = 500L
        //先快后慢
        firstAni.interpolator = DecelerateInterpolator()
        firstAni.playTogether(aniX01, aniY01)

        val secondAni = AnimatorSet()
        val aniX02 = ObjectAnimator.ofFloat(pk_dan_level, View.SCALE_X, 1.05f, 0.925f)
        val aniY02 = ObjectAnimator.ofFloat(pk_dan_level, View.SCALE_Y, 1.05f, 0.925f)
        secondAni.duration = 600L
        secondAni.interpolator = LinearInterpolator()
        secondAni.playTogether(aniX02, aniY02)
        //每次初始化不显示
        pk_dan_level.alpha = 0f
        ll_dan_score.alpha = 0f
        val thirdAni = AnimatorSet()
        //
        if (win) {
            rl_dan_mvp.alpha = 0f
            val aniAlpha1 = ObjectAnimator.ofFloat(pk_dan_level, View.ALPHA, 0f, 1f).setDuration(150)
            val aniAlpha2 = ObjectAnimator.ofFloat(rl_dan_mvp, View.ALPHA, 0f, 1f).setDuration(330)
            val aniAlpha3 = ObjectAnimator.ofFloat(ll_dan_score, View.ALPHA, 0f, 1f).setDuration(330)
            thirdAni.playTogether(aniAlpha1, aniAlpha2, aniAlpha3)
        } else {
            val aniAlpha1 = ObjectAnimator.ofFloat(pk_dan_level, View.ALPHA, 0f, 1f).setDuration(150)
            val aniAlpha3 = ObjectAnimator.ofFloat(ll_dan_score, View.ALPHA, 0f, 1f).setDuration(330)
            thirdAni.playTogether(aniAlpha1, aniAlpha3)
        }

        firstAni.addListener(onEnd = {
            if (ViewCompat.isAttachedToWindow(this)) {
                secondAni.start()
            }
        })
        pk_dan_bg_ani.setCallBack(object : WebpGifView.GiftViewPlayCallBack {
            //不管成功加载或者失败加载  都开始本地动画
            override fun onRelease() {
            }

            override fun onStart() {
                thirdAni.start()
                firstAni.start()
                //隐藏时间改成以 后台返回为准
                myHandler.postDelayed({
                    pk_dan_result?.hide()
                }, pkStageDetail.closeTtl * 1000L)
            }

            override fun onError() {
                thirdAni.start()
                firstAni.start()
            }

            override fun onEnd() {
            }
        })
        pk_dan_bg_ani.setURI(StringHelper.getOssImgUrl(pkStageDetail.resultAnimBgUrl ?: return), 1)
        if (win) {
            pk_dan_fg_ani.show()
            pk_dan_fg_ani.setURI(
                StringHelper.getOssImgUrl(
                    pkStageDetail.resultAnimForeUrl
                        ?: return
                ), 1
            )
        } else {
            pk_dan_fg_ani.hide()
        }

    }

    private val speedRunnable: Runnable by lazy {
        Runnable {
            resetSpeed()
        }
    }

    private fun resetSpeed() {
        pk2_speed_process_01.hide()
        pk2_speed_process_02.hide()
        pk2_speed_img1.hide()
        pk2_speed_img2.hide()
        pk2_title_01.backgroundResource = R.drawable.pk_process_color_01
        pk2_process_01.backgroundResource = R.drawable.pk_process_color_01

        pk2_title_02.backgroundResource = R.drawable.pk_process_color_03
        pk2_process_02.backgroundResource = R.drawable.pk_process_color_03
    }

    /**
     * 播放道具加倍特效
     */
    private fun playSpeedAnimation(ttl: Long, programId: Long) {
        //默认的游标设置不可见
        imageView4.visibility = View.INVISIBLE
        val curProgramId = playerViewModel.programId
        val isMe = curProgramId == programId
        if (isMe) {
            pk2_speed_process_01.alpha = 0f
            pk2_speed_process_01.show()
            pk2_speed_process_02.hide()

            pk2_speed_img1.alpha = 0f
            pk2_speed_img1.show()
            pk2_speed_img2.hide()
            //去除默认进度条的颜色
            pk2_title_01.background = null
            pk2_process_01.background = null

            ImageUtils.loadGifImageLocal(pk2_speed_img1, R.mipmap.pk_speed_icon)
            val ani1 = ObjectAnimator.ofFloat(pk2_speed_img1, View.ALPHA, 0f, 1f)
            val ani2 = ObjectAnimator.ofFloat(pk2_speed_img1, View.TRANSLATION_X, -DensityHelper.dp2pxf(20), 0f)
            val ani3 = ObjectAnimator.ofFloat(pk2_speed_process_01, View.ALPHA, 0f, 1f)
            val aniSet = AnimatorSet()
            aniSet.playTogether(ani1, ani2, ani3)
            aniSet.duration = SPEED_PLAY_TIME
            aniSet.start()
        } else {
            pk2_speed_process_01.hide()
            pk2_speed_process_02.alpha = 0f
            pk2_speed_process_02.show()

            pk2_speed_img1.hide()
            pk2_speed_img2.alpha = 0f
            pk2_speed_img2.show()
            //去除默认进度条的颜色
            pk2_title_02.background = null
            pk2_process_02.background = null
            ImageUtils.loadGifImageLocal(pk2_speed_img2, R.mipmap.pk_speed_icon)
//            pk2_speed_img2.rotationY=-180f
            val ani1 = ObjectAnimator.ofFloat(pk2_speed_img2, View.ALPHA, 0f, 1f)
            val ani2 = ObjectAnimator.ofFloat(pk2_speed_img2, View.TRANSLATION_X, DensityHelper.dp2pxf(20), 0f)
            val ani3 = ObjectAnimator.ofFloat(pk2_speed_process_02, View.ALPHA, 0f, 1f)
            val aniSet = AnimatorSet()
            aniSet.playTogether(ani1, ani2, ani3)
            aniSet.duration = SPEED_PLAY_TIME
            aniSet.start()

        }
        val delay = SPEED_PLAY_TIME + ttl * 1000L
        myHandler.removeCallbacks(speedRunnable)
        myHandler.postDelayed(speedRunnable, delay)
    }

    //=========================================== 斗地主 ==============================================
//    /**
//     * 斗地主开始
//     * @author WanZhiYuan
//     * @date 2020/04/15
//     */
//    private fun landlordsStart(data: PKInfoBean) {
//        logger.info("斗地主开始")
//
//        //对于上一次PK没正常结束的情况
//        if (pk_result_layout.isVisible())
//            pk_result_layout.hide()
//        this.show()
//
//        currentPKData = data
//        currentPkType = LANDLORD
//        data.needAnim = false
//        data.detailList = detailSort(data.detailList ?: arrayListOf())
//        if (data.seconds != null && data.seconds!! > 0) {
//            pKViewModel.closeCountDown()
//            pKViewModel.countDown(pk_time, data.seconds)
//        }
//
//        pk2_process.show()
//        pk3_process.hide()
//        pk_title_img.imageResource = R.mipmap.pk_title_two
//        showViewAfterAni()
////        landlordView.show()
//
//        queueNotifyPk(data)
//
////        logger.info("斗地主开始播放vs动画")
////        pk_vs_ani.callback = object : SVGACallback {
////            override fun onFinished() {
////                logger.info("斗地主vs动画播放完毕")
////            }
////
////            override fun onPause() {}
////            override fun onRepeat() {}
////            override fun onStep(frame: Int, percentage: Double) {}
////        }
////        pk_vs_ani.show()
////        pk_vs_ani.startAnimation()
//    }
//
//    /**
//     * 斗地主分数改变
//     * @author WanZhiYuan
//     * @date 2020/04/15
//     */
//    private fun landlordsScoreChange(data: PKInfoBean) {
//        currentPKData = data
//        if (data.seconds != null && data.seconds!! > 0) {
//            pk_time_layout.show()
//            pKViewModel.closeCountDown()
//            pKViewModel.countDown(pk_time, data.seconds)
//        } else {
//            pk_time_layout.hide()
//        }
//        pk2_process.show()
//        pk3_process.hide()
//        pk_title_img.setImageResource(R.mipmap.pk_title_two)
//
//        data.detailList = detailSort(data.detailList ?: arrayListOf())
//
//        queueNotifyPk(data)
//    }
//
//    /**
//     * 斗地主结束
//     * @author WanZhiYuan
//     * @date 2020/04/15
//     */
//    private fun landlordsEnd(data: PKInfoBean) {
//        logger.info("展示斗地主结果 不做动画")
//
//        this.show()
////        landlordView.show()
//
//        data.needAnim = false
//        currentPKData = data
//        currentPkType = LANDLORD
//        data.detailList = detailSort(data.detailList ?: arrayListOf())
//
//        if (data.seconds != null && data.seconds!! > 0) {
//            //设置定时器
//            pk_time_layout.show()
//            pKViewModel.closeCountDown()
//            pKViewModel.countDown(pk_time, data.seconds)
//        } else {
//            pk_time_layout.hide()
//        }
//        pk2_process.show()
//        pk3_process.hide()
//        pk_title_img.setImageResource(R.mipmap.pk_title_two)
//        showPkTitleImg()
//        myHandler.postDelayed({
//            videoViewModel.showVideoInfo.value = true
//        }, VS_PLAY_TIME)
//
//        queueNotifyPk(data)
//
//        ivIconScore01.inVisiable()
//        ivIconScore02.hide()
//        ivIconScore03.hide()
//        tvScore01.hide()
//        tvScore02.hide()
//        tvScore03.hide()
////        var isDraw = true
//        data.detailList?.forEachIndexed { index, pkUser ->
//            if (PKResultType.WIN == pkUser.result) {
////                isDraw = false
//                when (index) {
//                    0 -> {
//                        ivIconScore01.show()
//                        ivIconScore01.imageResource = R.mipmap.pk_logo_win
//                    }
//                    1 -> {
//                        ivIconScore02.show()
//                        ivIconScore02.imageResource = R.mipmap.pk_logo_win
//                    }
//                    else -> {
//                        ivIconScore03.show()
//                        ivIconScore03.imageResource = R.mipmap.pk_logo_win
//                    }
//                }
//            }
//        }
////        if (isDraw) {
////            //平局
////            ivIconScore01.show()
////            ivIconScore01.imageResource = R.mipmap.pk_logo_draw
////            ivIconScore02.show()
////            ivIconScore02.imageResource = R.mipmap.pk_logo_draw
////            ivIconScore03.show()
////            ivIconScore03.imageResource = R.mipmap.pk_logo_draw
////        }
//    }
//
//    /**
//     * 斗地主最终结果
//     * @author WanZhiYuan
//     * @date 2020/04/15
//     */
//    private fun landlordsResult(data: PKResultEvent) {
//        val pkData = PKInfoBean().apply {
//            detailList = arrayListOf()
//            pkType = data.pkType
//            var total = 0L
//            data.detailList?.forEach { user ->
//                detailList?.add(PKUser().apply {
//                    programId = user.programId
//                    landlord = user.landlord
//                    result = user.result
//                    score = user.score
//                    total += user.score
//                    if (user.landlord == true) {
//                        landlordScore = user.score
//                    } else if (user.landlord == false) {
//                        civilianScore += user.score
//                    }
//                })
//            }
//            totalScore = total
//        }
//        pkData.detailList = detailSort(pkData.detailList ?: arrayListOf())
//
//        //做PK段位结果动画
//        val indexOf = data.stageDetailList?.indexOf(PkStageDetail().apply {
//            programId = playerViewModel.programId
//        }) ?: -1
//        if (indexOf != -1) {
//            val stageSelf = data.stageDetailList?.get(indexOf)
//            stageSelf?.let {
//                it.pkType = data.pkType
//                playDanResultAnimation(it)
//            }
//        }
//
//        queueNotifyPk(pkData)
//
//        ivIconScore01.inVisiable()
//        ivIconScore02.hide()
//        ivIconScore03.hide()
//        tvScore01.hide()
//        tvScore02.hide()
//        tvScore03.hide()
////        var isLose = true
//        pkData.detailList?.forEachIndexed { index, pkResultUser ->
//            if (PKResultType.WIN == pkResultUser.result) {
////                isLose = false
//                when (index) {
//                    0 -> {
//                        startPlaySvga(ivIconScore01, svgaPlayerView01, R.mipmap.pk_logo_win, "svga/pk_win.svga", false)
//                    }
//                    1 -> {
//                        startPlaySvga(ivIconScore02, svgaPlayerView02, R.mipmap.pk_logo_win, "svga/pk_win.svga", false)
//                    }
//                    else -> {
//                        startPlaySvga(ivIconScore03, svgaPlayerView03, R.mipmap.pk_logo_win, "svga/pk_win.svga", false)
//                    }
//                }
//            }
//        }
////        if (isLose) {
////            startPlaySvga(ivIconScore01, svgaPlayerView01, R.mipmap.pk_logo_draw, "svga/pk_draw.svga", false)
////            startPlaySvga(ivIconScore02, svgaPlayerView02, R.mipmap.pk_logo_draw, "svga/pk_draw.svga", false)
////            startPlaySvga(ivIconScore03, svgaPlayerView03, R.mipmap.pk_logo_draw, "svga/pk_draw.svga", false)
////        }
//
//        pKViewModel.closeCountDown()
//        pk_time_layout.hide()
//        myHandler.removeCallbacks(cloudRunnable)
//    }
//
//    /**
//     * 斗地主进度变化
//     * @author WanZhiYuan
//     * @date 2020/04/15
//     */
//    private fun notifyLandlordsProgress() {
//        if (pkDateList.isEmpty()) {
//            logger.info("没有刷新任务了")
//            return
//        }
//        val pkData: PKInfoBean = pkDateList.pop()
//        if (pkData.totalScore ?: 0L < lastDataOfPk?.totalScore ?: 0L) {
//            logger.info("pk消息顺序错了 不再刷新pk值 cur:$pkData last:$lastDataOfPk")
//            reportCrash("pk消息顺序错了 不再刷新pk值cur:$pkData last:$lastDataOfPk")
//            return
//        }
//        if (pkData.detailList?.isEmpty() == true) {
//            return
//        }
//
//        isNotify = true
//
//        var lastPlayer1: PKUser? = null
//        var lastPlayer2: PKUser? = null
//        var lastPlayer3: PKUser? = null
//
//        var player1: PKUser? = null
//        var player2: PKUser? = null
//        var player3: PKUser? = null
//
//        //上一次的数据
//        lastDataOfPk?.detailList?.let { list ->
//            if (list.size >= 3) {
//                lastPlayer1 = list[0]
//                lastPlayer2 = list[1]
//                lastPlayer3 = list[2]
//            }
//        }
//
//        pkData.detailList?.let { list ->
//            if (list.size >= 3) {
//                player1 = list[0]
//                player2 = list[1]
//                player3 = list[2]
//            }
//        }
//
//        //设置进度条
////        pk2_process_text01.isNeedFormat = false
////        pk2_process_text02.isNeedFormat = false
//        val landlordScore = pkData.landlordScore
//        val civilianScore = pkData.civilianScore
//        val lastLandlordScore = lastDataOfPk?.landlordScore ?: 0
//        val lastCivilianScore = lastDataOfPk?.civilianScore ?: 0
//        var start: Float
//        var end: Float
//        if (player1?.landlord == true) {
//            //地主
//            pk2_process_01.background = getProgressDrawable(true, R.color.pk_color_01)
//            pk2_title_01.background = getProgressDrawable(true, R.color.pk_color_01)
//            pk2_title_01.text = "地主"
//            pk2_process_02.background = getProgressDrawable(false, R.color.pk_color_03)
//            pk2_title_02.background = getProgressDrawable(false, R.color.pk_color_03)
//            pk2_title_02.text = "平民"
//            pk2_process_text01.setNumberString("$lastLandlordScore", "$landlordScore")
//            pk2_process_text02.setNumberString("$lastCivilianScore", "$civilianScore")
//            start = if (lastLandlordScore == 0L || lastCivilianScore == 0L) {
//                (lastLandlordScore.toFloat() + 1) / (lastCivilianScore + 1)
//            } else {
//                lastLandlordScore.toFloat() / lastCivilianScore
//            }
//            end = if (landlordScore == 0L || civilianScore == 0L) {
//                (landlordScore.toFloat() + 1) / (civilianScore + 1)
//            } else {
//                landlordScore.toFloat() / civilianScore
//            }
//        } else {
//            //平民
//            pk2_process_01.background = getProgressDrawable(true, R.color.pk_color_03)
//            pk2_title_01.background = getProgressDrawable(true, R.color.pk_color_03)
//            pk2_title_01.text = "平民"
//            pk2_process_02.background = getProgressDrawable(false, R.color.pk_color_01)
//            pk2_title_02.background = getProgressDrawable(false, R.color.pk_color_01)
//            pk2_title_02.text = "地主"
//            pk2_process_text01.setNumberString("$lastCivilianScore", "$civilianScore")
//            pk2_process_text02.setNumberString("$lastLandlordScore", "$landlordScore")
//            start = if (lastLandlordScore == 0L || lastCivilianScore == 0L) {
//                (lastCivilianScore.toFloat() + 1) / (lastLandlordScore + 1)
//            } else {
//                lastCivilianScore.toFloat() / lastLandlordScore
//            }
//            end = if (landlordScore == 0L || civilianScore == 0L) {
//                (civilianScore.toFloat() + 1) / (landlordScore + 1)
//            } else {
//                civilianScore.toFloat() / landlordScore
//            }
//        }
//
//        val lp1 = pk2_process_01.layoutParams as ConstraintLayout.LayoutParams
//        if (end != start) {
//            val animator21 = ValueAnimator.ofFloat(start, end)
//            animator21.duration = 300L
//            animator21.addUpdateListener { value ->
//                lp1.horizontalWeight = value.animatedValue as Float
//                pk2_process_01.requestLayout()
//                pk2_process_02.requestLayout()
//            }
//            animator21.start()
//            animator21.addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator?) {
//                    isNotify = false
//                    notifyLandlordsProgress()
//                }
//            })
//        } else {
//            isNotify = false
//            notifyLandlordsProgress()
//        }
//
//        //设置分数
//        val playerScore1 = player1?.score ?: 0
//        val playerScore2 = player2?.score ?: 0
//        val playerScore3 = player3?.score ?: 0
//
//        val lastPlayerScore1 = lastPlayer1?.score ?: 0
//        val lastPlayerScore2 = lastPlayer2?.score ?: 0
//        val lastPlayerScore3 = lastPlayer3?.score ?: 0
//
//        ivIconScore01.show()
//        ivIconScore02.show()
//        ivIconScore03.show()
//        tvScore01.show()
//        tvScore02.show()
//        tvScore03.show()
////        tvScore01.isNeedFormat = false
////        tvScore02.isNeedFormat = false
////        tvScore03.isNeedFormat = false
////        tvScore01.setMoneyTypeFace()
////        tvScore02.setMoneyTypeFace()
////        tvScore03.setMoneyTypeFace()
//        if (player2?.landlord == true) {
//            tvScore01.background = getSingleDrawable(isLeft = true, isLandlord = false)
//            tvScore02.background = getSingleDrawable(isLeft = false, isLandlord = true)
//            tvScore03.background = getSingleDrawable(isLeft = false, isLandlord = false)
//            ivIconScore01.imageResource = R.mipmap.lm_core_icon_pk_rank_pleb
//            ivIconScore02.imageResource = R.mipmap.lm_core_icon_pk_rank_landlord
//            ivIconScore03.imageResource = R.mipmap.lm_core_icon_pk_rank_pleb
//        } else {
//            tvScore01.background = getSingleDrawable(isLeft = true, isLandlord = true)
//            tvScore02.background = getSingleDrawable(isLeft = false, isLandlord = false)
//            tvScore03.background = getSingleDrawable(isLeft = false, isLandlord = false)
//            ivIconScore01.imageResource = R.mipmap.lm_core_icon_pk_rank_landlord
//            ivIconScore02.imageResource = R.mipmap.lm_core_icon_pk_rank_pleb
//            ivIconScore03.imageResource = R.mipmap.lm_core_icon_pk_rank_pleb
//        }
//        tvScore01.setNumberString("$lastPlayerScore1", "$playerScore1")
//        tvScore02.setNumberString("$lastPlayerScore2", "$playerScore2")
//        tvScore03.setNumberString("$lastPlayerScore3", "$playerScore3")
//
//        //设置飘数字
//        //11111111111111111111111111-------------------------------------------------------------------
//        val difScore1 = playerScore1 - lastPlayerScore1
//        //飘数字
//        if (difScore1 > 0 && pkData.needAnim)
//            bezier01.addNumberView(difScore1, ContextCompat.getColor(context, R.color.pk_color_01))
//        //22222222222222222222222222-------------------------------------------------------------------
//        val difScore2 = playerScore2 - lastPlayerScore2
//        if (difScore2 > 0 && pkData.needAnim)
//            bezier02.addNumberView(difScore2, ContextCompat.getColor(context, R.color.pk_color_03))
//        //3333333333333333333333333-----------------------------------------------------------------------
//        val difScore3 = playerScore3 - lastPlayerScore3
//        if (difScore3 > 0 && pkData.needAnim)
//            bezier03.addNumberView(difScore3, ContextCompat.getColor(context, R.color.pk_color_03))
//
//
//        lastDataOfPk = pkData
//    }
//
//    private val mLandlordColors = intArrayOf(GlobalUtils.formatColor("#FFBB00"), GlobalUtils.formatColor("#D74F00"))
//    private val mPlebColors = intArrayOf(GlobalUtils.formatColor("#5BAEFF"), GlobalUtils.formatColor("#5263B0"))
//
//    /**
//     * 获取单个分数背景
//     * @author WanZhiYuan
//     * @date 2020/04/15
//     */
//    private fun getSingleDrawable(isLeft: Boolean, isLandlord: Boolean): GradientDrawable {
//        val scoreDrawable = GradientDrawable()
//        if (isLandlord) {
//            scoreDrawable.colors = mLandlordColors
//        } else {
//            scoreDrawable.colors = mPlebColors
//        }
//        scoreDrawable.shape = GradientDrawable.RECTANGLE
//        scoreDrawable.cornerRadii = if (isLeft) {
//            scoreDrawable.orientation = GradientDrawable.Orientation.RIGHT_LEFT
//            getCornerRadii(0, 100, 0, 100)
//        } else {
//            scoreDrawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
//            getCornerRadii(100, 0, 100, 0)
//        }
//        return scoreDrawable
//    }
//
//    /**
//     * 获取顶部PK进度背景
//     * @author WanZhiYuan
//     * @date 2020/04/15
//     */
//    private fun getProgressDrawable(isLeft: Boolean, @ColorRes color: Int): GradientDrawable {
//        val progressDrawable = GradientDrawable()
//        progressDrawable.shape = GradientDrawable.RECTANGLE
//        progressDrawable.cornerRadii = if (isLeft) {
//            getCornerRadii(4, 0, 4, 0)
//        } else {
//            getCornerRadii(0, 4, 0, 4)
//        }
//        progressDrawable.setColor(GlobalUtils.getColor(color))
//        return progressDrawable
//    }
//
//    private fun getCornerRadii(leftTop: Int, rightTop: Int,
//                               leftBottom: Int, rightBottom: Int): FloatArray {
//        //这里返回的一个浮点型的数组，一定要有8个元素，不然会报错
//        return floatArrayOf(DensityHelper.dp2pxf(leftTop), DensityHelper.dp2pxf(leftTop),
//                DensityHelper.dp2pxf(rightTop), DensityHelper.dp2pxf(rightTop),
//                DensityHelper.dp2pxf(rightBottom), DensityHelper.dp2pxf(rightBottom),
//                DensityHelper.dp2pxf(leftBottom), DensityHelper.dp2pxf(leftBottom))
//    }
//
//    /**
//     * 列表排序
//     */
//    private fun detailSort(list: ArrayList<PKUser>): ArrayList<PKUser> {
//        if (list.isEmpty()) {
//            return list
//        }
//        list.sortList(kotlin.Comparator { o1, o2 ->
//            val a1 = if (o1.programId == playerViewModel.programId) 1 else 0
//            val a2 = if (o2.programId == playerViewModel.programId) 1 else 0
//            var result = a2 - a1
//            if (result == 0) {
//                val b1 = if (o1.landlord == true) 1 else 0
//                val b2 = if (o2.landlord == true) 1 else 0
//                result = b2 - b1
//            }
//            result
//        })
//        return list
//    }
}