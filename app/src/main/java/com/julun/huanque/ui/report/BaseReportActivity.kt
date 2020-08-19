package com.julun.huanque.ui.report

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.ManagerInfo
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.NetUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.adapter.AddPictureAdapter
import com.julun.huanque.viewmodel.CommonReportViewModel
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.permissions.RxPermissions
import com.luck.picture.lib.tools.PictureFileUtils
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.activity_report.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/3/16 10:07
 *
 *@Description: 整合举报页面
 *
 */
abstract class BaseReportActivity : BaseActivity() {
    companion object {
        const val MAX_PIC_COUNT = 3
        const val maxTextSize = 200
    }

    val loadingDialog: LoadingDialog by lazy { LoadingDialog(this) }
    val viewModel: CommonReportViewModel by viewModels()
    var selectList: MutableList<LocalMedia> = mutableListOf()
    private val adapter: AddPictureAdapter by lazy {
        AddPictureAdapter(this@BaseReportActivity, onAddPicClickListener)
    }
    val reportTypeAdapter by lazy {
        object : BaseQuickAdapter<ManagerInfo, BaseViewHolder>(R.layout.item_report_type) {
            override fun convert(h: BaseViewHolder, info: ManagerInfo) {
                val text = h.getView<TextView>(R.id.item_text_report)
                text.text = info.itemName
                text.isSelected = currentSelect == h.adapterPosition
            }

        }
    }

    //当前举报选中类型
    var currentSelect: Int = -1

    override fun getLayoutId(): Int {
        return R.layout.activity_report
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        initViewModel()
        header_view.initHeaderView(titleTxt = "举报")


//        report_types.layoutManager = FlexboxLayoutManager(this)
//        report_types.adapter = reportTypeAdapter
        report_types.layoutManager = GridLayoutManager(this, 3)
        report_types.adapter = reportTypeAdapter
        report_types.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(15f)))

        adapter.setSelectMax(MAX_PIC_COUNT)
        adapter.setHeight(dp2px(94))
        val manager = GridLayoutManager(this@BaseReportActivity, 3, RecyclerView.VERTICAL, false)

        addPicListView.layoutManager = manager
        addPicListView.addItemDecoration(GridLayoutSpaceItemDecoration(dp2px(1.5f)))
        addPicListView.adapter = adapter


        addPicListView.setHasFixedSize(true)
        addPicListView.isNestedScrollingEnabled = false


    }

    private fun initViewModel() {
        viewModel.reportStateResult.observe(this, Observer {
            if (it.state==NetStateType.SUCCESS) {
                publishSuccess()
            } else if(it.state==NetStateType.ERROR){
                publishFail()
            }
            apply_button.isEnabled = true
            if (loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
        })
        viewModel.reportList.observe(this, Observer {
            reportTypeAdapter.setList(it ?: return@Observer)
        })

    }

    private val dialog: MyAlertDialog by lazy { MyAlertDialog(this, true) }

    override fun onBackPressed() {
        if (selectList.size > 0 || input_text.text.toString().isNotEmpty()) {
            dialog.showAlertWithOKAndCancel("退出此次编辑？", MyAlertDialog.MyDialogCallback(
                    onCancel = {
                    },
                    onRight = {
                        finish()
                    }), okText = "退出", hasTitle = false)
        } else {
            finish()
        }
    }

    var currentContent: String = ""

    override fun initEvents(rootView: View) {
        header_view.imageViewBack.onClickNew {
            onBackPressed()
        }
        val listener = View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                logger.info("触摸隐藏")
                hideSoftInput()
            }
            false
        }
        addPicListView.setOnTouchListener(listener)
        content_view.setOnTouchListener(listener)
        apply_button.onClickNew {
            if (!NetUtils.isNetConnected()) {
                ToastUtils.show("当前网络不好 就稍后重试")
                return@onClickNew
            }
            currentContent = input_text.text.toString()
            startPublish()

        }
        reportTypeAdapter.setOnItemClickListener { _, _, position ->
            currentSelect = position
            reportTypeAdapter.notifyDataSetChanged()
            checkBtnEnable()
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
                            PictureSelector.create(this@BaseReportActivity).themeStyle(R.style.picture_me_style_multi).openExternalPreview(position, selectList)
//                        ImageActivity.externalPicturePreview(this@PublishStateActivity,position, selectList)
                    }
                }
            }

            //每次删除都要重新判断是否符合发布条件
            override fun onItemDeleteClick(position: Int) {
                hideSoftInput()
                checkBtnEnable()
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
                    ToastUtils.show(getString(R.string.report_out_of_range))
                    s.delete(selectionStart - 1, selectionEnd)
                    val tempSelection = selectionEnd
                    input_text.text = s
                    input_text.setSelection(tempSelection)
                } else {
                    textLimit.text = "${temp?.length}/200"
                }
                checkBtnEnable()
            }
        })
    }
    private fun checkBtnEnable(){
        apply_button.isEnabled = !(input_text.text.toString().isEmpty()||currentSelect==-1||selectList.isEmpty())
    }
    private fun hideSoftInput() {
//        KPSwitchConflictUtil.hidePanelAndKeyboard(emojiView)
        ScreenUtils.hideSoftInput(this)
    }


    abstract fun startPublish()

    open fun publishSuccess() {
//        if (loadingDialog.isShowing) {
//            loadingDialog.dismiss()
//        }
        hideSoftInput()
        finish()
        clearPictureCache()
        Toast.makeText(applicationContext, "举报成功", Toast.LENGTH_SHORT).show()
    }

    protected fun publishFail() {
//        apply_button.isEnabled = true
//        if (loadingDialog.isShowing) {
//            loadingDialog.dismiss()
//        }
        Toast.makeText(applicationContext, "提交失败，请稍后重试", Toast.LENGTH_SHORT).show()
    }

    //清除压缩和裁剪留下的缓存
    private fun clearPictureCache() {
        // 清空图片缓存，包括裁剪、压缩后的图片 注意:必须要在上传完成后调用 必须要获取权限
        val permissions = RxPermissions(this)

        permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(object :  io.reactivex.rxjava3.core.Observer<Boolean> {

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(aBoolean: Boolean) {
                if (aBoolean) {
                    PictureFileUtils.deleteCacheDirFile(this@BaseReportActivity)
                    PictureFileUtils.deleteExternalCacheDirFile(this@BaseReportActivity)
                }
            }

            override fun onError(e: Throwable) {}

            override fun onComplete() {}
        })
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
                    checkBtnEnable()
                }
            }
        } else {
        }

    }

    fun goToPictureSelectPager() {
        PictureSelector.create(this@BaseReportActivity)
                .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .theme(R.style.picture_me_style_multi)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .maxSelectNum(MAX_PIC_COUNT)// 最大图片选择数量
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
            goToPictureSelectPager()
        }

    }

}
