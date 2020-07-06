package com.julun.huanque.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.CommonLoadingDialog
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.fragment.PersonalInformationProtectionFragment
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.viewmodel.FillInformationViewModel
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import kotlinx.android.synthetic.main.act_fill_information.*
import org.jetbrains.anko.sdk23.listeners.textChangedListener
import java.text.SimpleDateFormat
import java.util.*


/**
 *@创建者   dong
 *@创建时间 2020/7/3 16:47
 *@描述 填写资料页面
 */
class FillInformationActivity : BaseActivity() {

    companion object {
        fun newInstance(activity: Activity) {
            val intent = Intent(activity, FillInformationActivity::class.java)
            activity.startActivity(intent)
        }
    }

    val loadingDialog: CommonLoadingDialog by lazy { CommonLoadingDialog.newInstance("") }
    private val mPersonalInformationProtectionFragment = PersonalInformationProtectionFragment()

    private var mViewModel: FillInformationViewModel? = null

    private var pvTime: TimePickerView? = null

    override fun getLayoutId() = R.layout.act_fill_information

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        findViewById<TextView>(R.id.tvTitle).text = "消息设置"
        initTimePicker()
        mViewModel?.currentStatus?.value = FillInformationViewModel.FIRST
        //隐私协议弹窗
        mPersonalInformationProtectionFragment.show(supportFragmentManager, "PersonalInformationProtectionFragment")
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mViewModel = ViewModelProvider(this).get(FillInformationViewModel::class.java)

        mViewModel?.updateInformationState?.observe(this, Observer {
            if (it != null) {
                if (it) {
                    //加载中，加载完成
                    loadingDialog.show(supportFragmentManager, "CommonLoadingDialog")
                } else {
                    loadingDialog.dismiss()
                }
            }
        })

        mViewModel?.currentStatus?.observe(this, Observer {
            if (it != null) {
                when (it) {
                    FillInformationViewModel.FIRST -> {
                        //显示第一步
                        con_first.show()
                        con_second.hide()
                    }
                    FillInformationViewModel.SECOND -> {
                        //显示第二步
                        con_first.hide()
                        con_second.show()
                    }
                    else -> {

                    }
                }
            }
        })
    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew { finish() }
        et_nickname.textChangedListener {
            afterTextChanged { judgeNextEnable() }
        }

        tv_next.onClickNew {
            var sexType = ""
            if (iv_male.isSelected) {
                sexType = "Male"
            } else if (iv_female.isSelected) {
                sexType = "Female"
            }
            val nickname = et_nickname.text.toString()
            val birthday = getTime(mViewModel?.birthdayData ?: return@onClickNew) ?: return@onClickNew
            if (sexType.isEmpty() || nickname.isEmpty() || birthday.isEmpty()) {
                return@onClickNew
            }

            mViewModel?.updateInformation(sexType, birthday, nickname, et_invitation_code.text.toString())
        }
        con_root.onClickNew {
            closeKeyBoard()
        }
        iv_male.onClickNew {
            //选中男
            showSex(true)
        }
        iv_female.onClickNew {
            //选中女
            showSex(false)
        }
        tv_bir.onClickNew {
            pvTime?.show(tv_bir)
        }
        iv_default_header.onClickNew {
            //点击头像，模拟上传成功
            mViewModel?.headerSuccess()
            startActivity(Intent(this, LoginActivity::class.java))
//            checkPermissions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                logger.info("收到图片")
                val selectList = PictureSelector.obtainMultipleResult(data)
                for (media in selectList) {
                    Log.i("图片-----》", media.path)
                }
                if (selectList.size > 0) {
                    val media = selectList[0]
                    val path: String?
                    path = if (media.isCut && !media.isCompressed) {
                        // 裁剪过
                        media.cutPath
                    } else if (media.isCompressed || media.isCut && media.isCompressed) {
                        // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                        media.compressPath
                    } else {
                        media.path
                    }
                    logger.info("收到图片:" + path)
                    //todo
                    //                uploadPhoto(path ?: return)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.info("图片返回出错了")
        }
    }

    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    /**
     *
     */
    private fun goToPictureSelectPager() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
//                .theme(R.style.picture_me_style)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
            .minSelectNum(1)// 最小选择数量
            .imageSpanCount(4)// 每行显示个数
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(true)// 是否可预览图片
            .isCamera(true)// 是否显示拍照按钮
            .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
            .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
            //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
            .enableCrop(true)// 是否裁剪
            .compress(true)// 是否压缩
            .synOrAsy(true)//同步true或异步false 压缩 默认同步
            //.compressSavePath(getPath())//压缩图片保存地址
            .glideOverride(120, 120)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            .isGif(false)// 是否显示gif图片
//                    .selectionMedia(selectList)// 是否传入已选图片
            .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
            //.cropCompressQuality(90)// 裁剪压缩质量 默认100
            .minimumCompressSize(100)// 小于100kb的图片不压缩
            .cropWH(200, 200)// 裁剪宽高比，设置如果大于图片本身宽高则无效
            .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
            .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
            .isDragFrame(false)
            .circleDimmedLayer(true)// 是否圆形裁剪
            .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
            .rotateEnabled(false) // 裁剪是否可旋转图片
            .scaleEnabled(true)// 裁剪是否可放大缩小图片
            .forResult(PictureConfig.CHOOSE_REQUEST)

        //结果回调onActivityResult code
    }

    /**
     * 选中性别
     */
    private fun showSex(male: Boolean) {
        iv_male.isSelected = male
        iv_female.isSelected = !male
        judgeNextEnable()
    }

    /**
     * 判断下一步是否可用
     */
    private fun judgeNextEnable() {
        val sexEnable = iv_male.isSelected || iv_female.isSelected
        val nicknameEnable = et_nickname.text.toString().isNotEmpty()
        val birEnable = mViewModel?.birthdayData != null

        tv_next.isEnabled = sexEnable && nicknameEnable && birEnable
    }

    fun closeKeyBoard() {
        ScreenUtils.hideSoftInput(this)
    }


    private fun initTimePicker() { //Dialog 模式下，在底部弹出
        /**
         * @description
         *
         * 注意事项：
         * 1.自定义布局中，id为 optionspicker 或者 timepicker 的布局以及其子控件必须要有，否则会报空指针.
         * 具体可参考demo 里面的两个自定义layout布局。
         * 2.因为系统Calendar的月份是从0-11的,所以如果是调用Calendar的set方法来设置时间,月份的范围也要是从0-11
         * setRangDate方法控制起始终止时间(如果不设置范围，则使用默认时间1900-2100年，此段代码可注释)
         */

        val currentDate = Calendar.getInstance() //系统当前时间
        val currentYear = currentDate.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)
        val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

        val startDate = Calendar.getInstance()
        startDate.set(currentYear - 110, currentMonth, currentDay)

        val endDate = Calendar.getInstance()
        endDate.set(currentYear - 18, currentMonth, currentDay)


        val selectedDate = Calendar.getInstance() //系统当前时间
        selectedDate.set(1995, 0, 1)

//        val startDate = Calendar.getInstance()
//        startDate.set(2014, 1, 23)
//        val endDate = Calendar.getInstance()
//        endDate.set(2027, 2, 28)

        pvTime = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
            mViewModel?.birthdayData = date
            tv_bir.text = getTime(date)
            judgeNextEnable()
        })
            .setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setSubmitColor(GlobalUtils.getColor(R.color.black_333))
            .setCancelColor(GlobalUtils.getColor(R.color.black_333))
            .setSubmitText("完成")
            .setTitleText("选择生日")
            .setTitleSize(16)
            .setSubCalSize(14)
            .setTitleColor(GlobalUtils.getColor(R.color.black_333))
            .setTimeSelectChangeListener { Log.i("pvTime", "onTimeSelectChanged") }
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
            .addOnCancelClickListener { Log.i("pvTime", "onCancelClickListener") }
            .setItemVisibleCount(7) //若设置偶数，实际值会加1（比如设置6，则最大可见条目为7）
            .setLineSpacingMultiplier(4.0f)
            .isAlphaGradient(true)
            .setDividerColor(0x00000000)
            .build()
        val mDialog: Dialog = pvTime?.dialog ?: return
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )
        params.leftMargin = 0
        params.rightMargin = 0
        pvTime?.dialogContainerLayout?.layoutParams = params
        val dialogWindow = mDialog.window ?: return
        dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim) //修改动画样式
        dialogWindow.setGravity(Gravity.BOTTOM) //改成Bottom,底部显示
        dialogWindow.setDimAmount(0.3f)
    }

    private fun getTime(date: Date): String? { //可根据需要自行截取数据显示
        Log.d("getTime()", "choice date millis: " + date.time)
        val format = SimpleDateFormat("yyyy-MM-dd")
        return format.format(date)
    }


}