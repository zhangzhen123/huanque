package com.julun.huanque.core.ui.publish_dynamic

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.effective.android.panel.PanelSwitchHelper
import com.effective.android.panel.interfaces.ContentScrollMeasurer
import com.effective.android.panel.view.panel.PanelView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.beans.CircleGroup
import com.julun.huanque.common.bean.beans.PublishDynamicCache
import com.julun.huanque.common.bean.beans.PublishDynamicResult
import com.julun.huanque.common.bean.forms.PublishStateForm
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.interfaces.EmojiInputListener
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.bitmap.BitmapUtil
import com.julun.huanque.common.utils.device.PhoneUtils
import com.julun.huanque.common.utils.permission.PermissionUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.emotion.EmojiSpanBuilder
import com.julun.huanque.common.widgets.emotion.Emotion
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.AddPictureAdapter
import com.julun.huanque.core.ui.dynamic.CircleActivity
import com.julun.maplib.LocationService
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.tools.PictureFileUtils
import kotlinx.android.synthetic.main.activity_publish_state.*
import org.jetbrains.anko.*
import java.io.File


/**
 *
 *@author zhangzhen
 *@data 2019/2/13
 *
 * 发布状态
 * @version 1.0
 * @version 2.0
 * @author WanZhiYuan
 * @describe 广场优化，主要优化：去除禁止换行操作
 **/
@Route(path = ARouterConstant.PUBLISH_STATE_ACTIVITY)
class PublishStateActivity : BaseActivity() {
    companion object {
        const val MAXPICCOUNT = 4
        const val maxTextSize = 2000
        val DRAFT_PATH = FileUtils.getCachePath(CommonInit.getInstance().getContext()) + "/pubDraft"

    }

    private val loadingDialog: LoadingDialog by lazy { LoadingDialog(this) }
    private val viewModel: PublishViewModel by viewModels()

    //    private var selectActionCount = 0 //记录是不是用户第一次的选择相册 第一次特殊处理
    private var selectList: MutableList<LocalMedia> = mutableListOf()
    val adapter: AddPictureAdapter by lazy {
        AddPictureAdapter(this@PublishStateActivity, onAddPicClickListener)
    }

    private val dialog: MyAlertDialog by lazy { MyAlertDialog(this, true) }

    private var currentLocation: BDLocation? = null

    private var hideName = false

    private var currentGroup: CircleGroup? = null

    private lateinit var mLocationService: LocationService

    //百度地图监听的Listener
    private var mLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            logger.info("location type=${location?.locType}")
            if (null != location
                && location.locType != BDLocation.TypeServerError
                && location.locType != BDLocation.TypeCriteriaException
                && location.locType != BDLocation.TypeNetWorkException
                && location.locType != BDLocation.TypeOffLineLocationFail
            ) {
                logger.info("location=${location.addrStr}")
                currentLocation = location
                tv_location.text = location.city
                stopLocation()
            } else {
                tv_location.text = "定位中"
            }
        }
    }
    private var checkLocationCount = 0

    /**
     * 检查定位权限
     */
    private fun checkLocationPermission() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .subscribe({ permission ->
                //不管有没有给权限 都不影响百度定位 只不过不给权限会不太准确
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        startLocation()
                        iv_location.isSelected = true
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        // Oups permission denied
                        logger.info("获取定位被拒绝")
                        iv_location.isSelected = false
                        tv_location.text = "定位"
                    }
                    else -> {
                        logger.info("获取定位被永久拒绝")
                        iv_location.isSelected = false
                        tv_location.text = "定位"
                        if (checkLocationCount > 0)
                            MyAlertDialog(this).showAlertWithOKAndCancel(
                                "欢鹊不能确定你的位置，你可以在设置中开启位置信息权限。",
                                okText = "去设置",
                                noText = "取消",
                                callback = MyAlertDialog.MyDialogCallback(onRight = {
                                    PhoneUtils.getPermissionSetting(packageName).let {
                                        if (ForceUtils.activityMatch(it)) {
                                            try {
                                                startActivity(it)
                                            } catch (e: Exception) {
                                                reportCrash("跳转权限或者默认设置页失败", e)
                                            }
                                        }
                                    }

                                })
                            )
                    }
                }
                checkLocationCount++
            }, { it.printStackTrace() })
    }

    override fun onStop() {
        super.onStop()
        stopLocation()
    }

    private fun startLocation() {
        tv_location.text = "定位中"
        mLocationService.registerListener(mLocationListener)
        mLocationService.start()
    }

    /**
     * 停止定位
     */
    private fun stopLocation() {
        mLocationService.stop()
        mLocationService.unregisterListener(mLocationListener)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_publish_state
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        goToPictureSelectPager()
    }

    private var mHelper: PanelSwitchHelper? = null

    override fun onStart() {
        super.onStart()
        if (mHelper == null) {
            mHelper = PanelSwitchHelper.Builder(this) //可选
                .addKeyboardStateListener {
                    onKeyboardChange { visible, height ->
                        //可选实现，监听输入法变化
                        if (visible) {
                            emojiImage.imageResource = R.mipmap.chat_emoji_input
                        } else {
                            emojiImage.imageResource = R.mipmap.chat_key_input
                        }
                    }
                }
                .addEditTextFocusChangeListener {
                    onFocusChange { _, hasFocus ->
                        //可选实现，监听输入框焦点变化
                        if (hasFocus) {
//                            scrollToBottom()
                        }
                    }
                }
                .addContentScrollMeasurer(object : ContentScrollMeasurer {
                    override fun getScrollDistance(defaultDistance: Int) = 0

                    override fun getScrollViewId() = R.id.scroll_container
                })

                .addPanelChangeListener {
                    onKeyboard {
                        //可选实现，输入法显示回调
                        logger.info("唤起系统输入法")
                        ll_input.show()
                        mHelper?.toKeyboardState(true)
//                        liveViewManager.hideHeaderForAnimation()
//                        iv_emoji.isSelected = false
//                        scrollToBottom()
                    }
                    onNone {
                        logger.info("隐藏所有面板")
                        ll_input.hide()
                        //收起键盘的时候 重置操作事件
                    }
                    onPanel { view ->
//                        liveViewManager.hideHeaderForAnimation()
                        //可选实现，面板显示回调
//                        if (view is PanelView) {
//                            iv_emoji.isSelected = view.id == R.id.panel_emotion
//                            scrollToBottom()
//                        }
                    }
                    onPanelSizeChange { panelView, _, _, _, width, height ->
                        //可选实现，输入法动态调整时引起的面板高度变化动态回调
                        if (panelView is PanelView) {
                            when (panelView.id) {
//                            R.id.panel_emotion -> {
//                                val pagerView = findViewById<EmotionPagerView>(R.id.view_pager)
//                                val viewPagerSize: Int = height - DensityHelper.dpToPx(30)
//                                pagerView.buildEmotionViews(
//                                    findViewById<PageIndicatorView>(R.id.pageIndicatorView),
//                                    edit_text,
//                                    Emotions.getEmotions(),
//                                    width,
//                                    viewPagerSize
//                                )
//                            }
                            }
                        }
                    }
                }
                .logTrack(false)
                .build()                     //可选，默认false，是否默认打开输入法
            mHelper?.setContentScrollOutsideEnable(true)
        }

    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        header_page.initHeaderView(titleTxt = "发布动态", operateTxt = "发布")
        header_page.imageViewBack.hide()
        header_page.textLeft.textSize = 14f
        header_page.textLeft.textColorResource = R.color.black_666
        header_page.textLeft.show()
        header_page.textLeft.onClickNew {
            onBackPressed()
        }

        header_page.textOperation.backgroundResource = R.drawable.sel_btn_publish
        val hlp = header_page.textOperation.layoutParams
        hlp.height = dp2px(30)
        hlp.width = dp2px(58)
        header_page.textOperation.isEnabled = false
        adapter.setSelectMax(MAXPICCOUNT)
        val manager = GridLayoutManager(this@PublishStateActivity, 3, RecyclerView.VERTICAL, false)

        addPicListView.layoutManager = manager
        addPicListView.addItemDecoration(GridLayoutSpaceItemDecoration(dp2px(1.5f)))
        adapter.setList(selectList)
        addPicListView.adapter = adapter



        addPicListView.setHasFixedSize(true)
        addPicListView.isNestedScrollingEnabled = false

        mLocationService = LocationService(this.applicationContext)
        //如果有缓存 提取缓存
        val draft = StorageHelper.getPubStateCache()
        if (draft != null) {
            if (draft.groupId != null && draft.groupName != null) {
                currentGroup = CircleGroup(groupName = draft.groupName!!, groupId = draft.groupId!!)
                setCircleStatus()
            }

            hideName = draft.anonymous == BooleanType.TRUE
            iv_hide_name.isSelected = hideName

            input_text.setText(draft.content)
            selectList.addAll(draft.selectList)
            adapter.notifyDataSetChanged()
            checkoutPublishEnable()

        }
        val circle = intent?.extras?.get(PublicStateCode.CIRCLE_DATA) as? CircleGroup
        if (circle != null) {
            currentGroup = circle
            logger.info("我是附带的圈子=${currentGroup?.groupName}")
            setCircleStatus()
        }

//        iv_location.isSelected = true
        checkLocationPermission()
    }

    private fun initViewModel() {
        viewModel.publisStateResult.observe(this, Observer {
            if (it.isSuccess()) {
                publishSuccess(it.requireT())
            } else {
                publishFail()
            }
        })
    }

    private fun checkoutPublishEnable() {
        header_page.textOperation.isEnabled = input_text.text.isNotEmpty() || selectList.isNotEmpty()
    }

    private fun setCircleStatus() {
        if (currentGroup != null) {
            tv_add_circle_name.text = "${currentGroup!!.groupName}"
            tv_add_circle_forward.hide()
            iv_circle_delete.show()
        } else {
            tv_add_circle_name.text = "添加圈子"
            tv_add_circle_forward.show()
            iv_circle_delete.hide()
        }
    }

    override fun onBackPressed() {
        if (selectList.size > 0 || input_text.text.toString().isNotEmpty()) {
            dialog.showAlertWithOKAndCancel(
                "是否保存草稿？", MyAlertDialog.MyDialogCallback(
                    onCancel = {
                        clearDraft()
                        finish()
                    },
                    onRight = {
                        saveDraft()
                        finish()
                    }), okText = "保存", noText = "不保存", hasTitle = true
            )
        } else {
            finish()
        }
    }

    /**
     * 保存草稿
     */
    private fun saveDraft() {
        val cache = PublishDynamicCache().apply {
            this.content = input_text.text.toString()

            this.groupName = currentGroup?.groupName
            this.groupId = currentGroup?.groupId


            this.anonymous = if (hideName) {
                BooleanType.TRUE
            } else {
                BooleanType.FALSE
            }
            //对于压缩的图片 保存新路径
            if (this@PublishStateActivity.selectList.isNotEmpty()) {
                val fileDir = FileUtils.createFileDir(DRAFT_PATH) ?: return
                this@PublishStateActivity.selectList.forEach {
                    if (it.isCompressed) {
                        val srcFile = File(it.compressPath)
                        //已经保存到缓存目录的 就不再复制
                        if (srcFile.exists() && srcFile.parent != DRAFT_PATH) {
                            FileUtils.copyFileToDirectory(srcFile, fileDir)
                            it.compressPath = DRAFT_PATH + "/" + srcFile.name
                        }

                    }
                }
                this.selectList = this@PublishStateActivity.selectList
            }

        }
        StorageHelper.setPubStateCache(cache)
    }

    /**
     * 清空草稿
     */
    private fun clearDraft() {
        //todo 删除保存的草稿
        StorageHelper.removePubStateCache()
    }

    override fun initEvents(rootView: View) {
        header_page.imageViewBack.onClickNew {
            onBackPressed()
        }
        val listener = View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                logger.info("触摸隐藏")
                hideSoftInput()
            }
            false
        }
//        content_view.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
//                hideSoftInput()
//        }
        addPicListView.setOnTouchListener(listener)
        content_view.setOnTouchListener(listener)
        header_page.textOperation.onClickNew {
            if (!NetUtils.isNetConnected()) {
                ToastUtils.show(this@PublishStateActivity.resources.getString(R.string.upload_no_network))
                return@onClickNew
            }
            header_page.textOperation.isEnabled = false
            startPublish()

        }
        adapter.setOnItemClickListener(object : AddPictureAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, v: View) {
                if (selectList.size > 0) {
                    hideSoftInput()
                    val media = selectList[position]
                    val pictureType = media.pictureType
                    val mediaType = PictureMimeType.pictureToVideo(pictureType)
                    when (mediaType) {
                        1 ->
                            // 预览图片 可自定长按保存路径
                            PictureSelector.create(this@PublishStateActivity).themeStyle(R.style.picture_me_style_multi)
                                .openExternalPreview(position, selectList)
//                        ImageActivity.externalPicturePreview(this@PublishStateActivity,position, selectList)
                    }
                }
            }

            //每次删除都要重新判断是否符合发布条件
            override fun onItemDeleteClick(position: Int) {
                hideSoftInput()
                checkoutPublishEnable()
            }
        })

        input_text.addTextChangedListener(object : TextWatcher {
            private var temp: CharSequence? = null
            private var selectionStart: Int = 0
            private var selectionEnd: Int = 0

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                temp = s
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                selectionStart = input_text.selectionStart
                selectionEnd = input_text.selectionEnd
                if (temp != null && temp!!.length > maxTextSize) {
                    ToastUtils.show(getString(R.string.state_out_of_range))
                    s.delete(selectionStart - 1, selectionEnd)
                    val tempSelection = selectionEnd
                    input_text.text = s
                    input_text.setSelection(tempSelection)
                }
                checkoutPublishEnable()
            }
        })

        panel_emotion.mListener = object : EmojiInputListener {
            override fun onClick(type: String, emotion: Emotion) {
                val currentLength = input_text.text.length
                val emojiLength = emotion.text.length
                if (currentLength + emojiLength > maxTextSize) {
                    Toast.makeText(this@PublishStateActivity, "输入长度超限", Toast.LENGTH_SHORT).show()
                    return
                }
                val start: Int = input_text.selectionStart
                val editable: Editable = input_text.editableText
                val emotionSpannable: Spannable = EmojiSpanBuilder.buildEmotionSpannable(
                    this@PublishStateActivity,
                    emotion.text
                )
                editable.insert(start, emotionSpannable)
            }

            override fun onLongClick(type: String, view: View, emotion: Emotion) {
                //长按,显示弹窗
                showEmojiSuspend(type, view, emotion)
            }

            override fun onActionUp() {
                mEmojiPopupWindow?.dismiss()
            }

            override fun onClickDelete() {
                //点击了删除事件
                deleteInputEmoji()
            }

            override fun showPrivilegeFragment(code: String) {
            }
        }
        ll_add_circle.onClickNew {
            startActivityForResult<CircleActivity>(
                ActivityRequestCode.SELECT_CIRCLE,
                ParamConstant.TYPE to CircleGroupType.Circle_Choose
            )
        }

        ll_location.onClickNew {
            if (!iv_location.isSelected) {
                checkLocationPermission()
            } else {
                tv_location.text = "定位"
                iv_location.isSelected = !iv_location.isSelected
            }

        }

        ll_hide_name.onClickNew {
            hideName = !hideName
            iv_hide_name.isSelected = hideName
        }

        iv_circle_delete.onClickNew {
            currentGroup = null
            setCircleStatus()
        }


    }

    //悬浮表情
    private var mEmojiPopupWindow: PopupWindow? = null

    /**
     * 显示表情悬浮效果
     */
    private fun showEmojiSuspend(type: String, view: View, emotion: Emotion) {
        if (mEmojiPopupWindow?.isShowing == true) {
            return
        }
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val content = emotion.text
        var dx = 0
        var dy = 0
        var rootView: View? = null
        when (type) {
            EmojiType.NORMAL -> {
                rootView =
                    LayoutInflater.from(this).inflate(R.layout.fragment_normal_emoji_suspend, null)
                mEmojiPopupWindow = PopupWindow(rootView, dip(50), dip(66))
                val drawable = GlobalUtils.getDrawable(R.drawable.bg_emoji_suspend)
                mEmojiPopupWindow?.setBackgroundDrawable(drawable)
                dx = location[0] + (view.width - dip(50)) / 2
                dy = location[1] - dip(66) + dip(13)
                rootView.findViewById<ImageView>(R.id.iv_emoji)?.imageResource = emotion.drawableRes
            }
            else -> {

            }
        }

        if (rootView == null) {
            return
        }


        val name = content.substring(content.indexOf("[") + 1, content.indexOf("]"))
        rootView.findViewById<TextView>(R.id.tv_emoji)?.text = name

        mEmojiPopupWindow?.isOutsideTouchable = false
        mEmojiPopupWindow?.showAtLocation(view, Gravity.TOP or Gravity.LEFT, dx, dy)
    }

    // 删除光标所在前一位(不考虑切换到emoji时的光标位置，直接删除最后一位)
    private fun deleteInputEmoji() {
        val keyCode = KeyEvent.KEYCODE_DEL
        val keyEventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val keyEventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        input_text.onKeyDown(keyCode, keyEventDown)
        input_text.onKeyUp(keyCode, keyEventUp)
    }

    private fun hideSoftInput() {
//        KPSwitchConflictUtil.hidePanelAndKeyboard(emojiView)
        ScreenUtils.hideSoftInput(input_text)
    }

    private fun startPublish() {
        val pathList = mutableListOf<String>()
        selectList.forEach {
            val isGif = PictureMimeType.isGif(it.pictureType)
            //动图直接上传原图
            if (isGif) {
                pathList.add(it.path)
            } else {
                pathList.add(it.compressPath)
            }
        }
        val form: PublishStateForm = PublishStateForm()
        form.apply {
            this.content = input_text.text.toString()
            this.anonymous = if (hideName) {
                BooleanType.TRUE
            } else {
                BooleanType.FALSE
            }
            if (iv_location.isSelected) {
                this.city = currentLocation?.city
                this.lat = currentLocation?.latitude
                this.lng = currentLocation?.longitude
            }
            this.groupId = currentGroup?.groupId
        }
        if (pathList.isNotEmpty()) {
            if (!loadingDialog.isShowing) {
                loadingDialog.showDialog(false)
            }
            OssUpLoadManager.uploadFiles(pathList, OssUpLoadManager.POST_POSITION) { code, list ->
                if (code == OssUpLoadManager.CODE_SUCCESS) {

                    logger.info("结果的：$list")
                    if (list == null || list.isEmpty()) {
                        publishFail()
                        return@uploadFiles
                    }
                    //记录每个图片上传成功后的ossUrl
                    selectList.forEachIndexed { index, localMedia ->
                        localMedia.ossUrl = list.getOrNull(index)
                    }
                    val imgList = StringBuilder()
                    var picSize = ""
                    list.forEachIndexed { index, img ->
                        if (index != list.size - 1) {
                            imgList.append(img).append(",")
                        } else {
                            imgList.append(img)
                        }
                    }
                    if (list.size == 1) {
                        val first = selectList[0]
                        if (first.width == 0 || first.height == 0) {
                            val arry = BitmapUtil.getImageWH(first.compressPath)
                            if (arry != null && arry.size >= 2) {
                                picSize = "${arry[0]},${arry[1]}"
                            }
                        } else {
                            picSize = "${first.width},${first.height}"
                        }
                    }
                    logger.info("表单：" + imgList.toString() + "  宽高" + picSize)
                    //下一步
                    form.apply {
                        this.pics = imgList.toString()
                        if (picSize.isNotBlank()) {
                            this.picSize = picSize
                        }
                    }
                    viewModel.publishState(form)
                } else {
                    publishFail()
                }
            }
        } else {
            viewModel.publishState(form)
        }

    }

    private fun publishSuccess(result: PublishDynamicResult) {
        header_page.textOperation.isEnabled = true
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        if (result.result) {
            hideSoftInput()
            finish()
            clearPictureCache()
            ToastUtils.show(getString(R.string.publish_success))
        } else {
            //发布失败
            ToastUtils.show2(result.message)
            val errorList = result.failPicList
            selectList.forEach {
                if (result.failPicList.contains(it.ossUrl)) {
                    it.isFail = true
                }
            }
            adapter.notifyDataSetChanged()
            //文字违规特殊显示
            renderFailTextSpan(result.failText)

        }

    }

    fun renderFailTextSpan(failText: List<String>) {
        val content = input_text.text.toString()
        val emotionSpannable: Spannable = EmojiSpanBuilder.buildEmotionSpannable(
            this@PublishStateActivity,
            content
        )
        failText.forEach { text ->
            val start = content.indexOf(text, ignoreCase = true)
            val end = start + text.length
            if (start != -1) {
                val colorY = Color.parseColor("#FF3F3F")
                val foregroundColorSpan = ForegroundColorSpan(colorY)
                emotionSpannable.setSpan(foregroundColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        input_text.setText(emotionSpannable)
    }

    private fun publishFail() {
        header_page.textOperation.isEnabled = true
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        ToastUtils.show(getString(R.string.publish_state_fail))
    }

    //清除压缩和裁剪留下的缓存
    private fun clearPictureCache() {
        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        if (PermissionUtils.checkSinglePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PictureFileUtils.deleteCacheDirFile(this@PublishStateActivity)
            PictureFileUtils.deleteExternalCacheDirFile(this@PublishStateActivity)
        }
        clearDraft()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    // 图片选择结果回调
                    selectList = PictureSelector.obtainMultipleResult(data)
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                    for (media in selectList) {
                        Log.i("图片-----》", media.path)
                    }
                    adapter.setList(selectList)
                    adapter.notifyDataSetChanged()
                    //每次选择图片回来重新判断条件
                    checkoutPublishEnable()
                }
                ActivityRequestCode.SELECT_CIRCLE -> {
                    currentGroup = data?.extras?.get(PublicStateCode.CIRCLE_DATA) as? CircleGroup
                    logger.info("我是选择的结果=${currentGroup?.groupName}")
                    setCircleStatus()
                }
            }
        } else {
            //此种情况是用户没有任何选择的返回 如果此时原界面是第一次打开的情况 直接关闭
//            if (selectActionCount <= 1) {
//                finish()
//            }
        }

    }

    private fun checkWritePermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        goToPictureSelectPager()
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger.info("获取权限被永久拒绝")
                        val message = "无法获取到相机/存储权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }

    private fun goToPictureSelectPager() {
//        selectActionCount++
        PictureSelector.create(this@PublishStateActivity)
            .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .theme(R.style.picture_me_style_multi)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
            .maxSelectNum(MAXPICCOUNT)// 最大图片选择数量
            .minSelectNum(1)// 最小选择数量
            .imageSpanCount(4)// 每行显示个数
            .selectionMode(PictureConfig.MULTIPLE)
            .previewImage(true)// 是否可预览图片
//                        .previewVideo(cb_preview_video.isChecked())// 是否可预览视频
            .isCamera(true)// 是否显示拍照按钮
            .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
            .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
            //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
//                        .enableCrop(cb_crop.isChecked())// 是否裁剪
            .compress(true)// 是否压缩
            .synOrAsy(true)//同步true或异步false 压缩 默认同步
            //.compressSavePath(getPath())//压缩图片保存地址
            //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
            .glideOverride(150, 150)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            .isGif(false)// 是否显示gif图片
            .selectionMedia(selectList)// 是否传入已选图片
            .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
            //.cropCompressQuality(90)// 裁剪压缩质量 默认100
            .minimumCompressSize(100)// 小于100kb的图片不压缩
            //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
            //.rotateEnabled(true) // 裁剪是否可旋转图片
            //.scaleEnabled(true)// 裁剪是否可放大缩小图片
            //.videoQuality()// 视频录制质量 0 or 1
            //.videoSecond()//显示多少秒以内的视频or音频也可适用
            //.recordVideoSecond()//录制视频秒数 默认60s
            .forResult(PictureConfig.CHOOSE_REQUEST)   //结果回调onActivityResult code
    }

    private val onAddPicClickListener = object : AddPictureAdapter.OnAddPicClickListener {
        override fun onAddPicClick() {
            hideSoftInput()
            checkWritePermissions()
        }

    }

}
