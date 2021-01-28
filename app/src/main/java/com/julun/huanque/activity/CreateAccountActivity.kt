package com.julun.huanque.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.activity.viewModels
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.bean.events.CreateAccountSuccess
import com.julun.huanque.common.bean.forms.CreateAccountForm
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.interfaces.EventListener
import com.julun.huanque.common.suger.inVisible
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.viewmodel.CreateAccountViewModel
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import kotlinx.android.synthetic.main.act_create_account.*
import kotlinx.android.synthetic.main.act_create_account.con_root
import kotlinx.android.synthetic.main.act_create_account.et_nickname
import kotlinx.android.synthetic.main.act_create_account.iv_clear_nickname
import kotlinx.android.synthetic.main.act_create_account.sdv_header
import kotlinx.android.synthetic.main.act_create_account.tv_next
import kotlinx.android.synthetic.main.fragment_update_info.*
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

/**
 *@创建者   dong
 *@创建时间 2020/12/2 19:54
 *@描述 创建分身页面
 */
class CreateAccountActivity : BaseActivity() {

    private val mViewModel: CreateAccountViewModel by viewModels()

    private val mLoadingDialog: LoadingDialog by lazy { LoadingDialog(this) }

    private var pvTime: TimePickerView? = null

    override fun getLayoutId() = R.layout.act_create_account

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initTimePicker()
        initViewModel()
        header_page.textTitle.text = "创建分身"
        val sex = SessionUtils.getSex()
        if (sex == Sex.FEMALE) {
            //女性
            tv_male.isSelected = false
            tv_female.isSelected = true
        } else if (sex == Sex.MALE) {
            //男性
            tv_male.isSelected = true
            tv_female.isSelected = false
        }
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
        tv_date.onClickNew {
            //显示生日弹窗
            pvTime?.show(tv_date)
        }

        con_root.mEventListener = object : EventListener {
            override fun onDispatch(ev: MotionEvent?) {
                //昵称有变化再请求，没有变化 无需请求
                if (ev?.action == MotionEvent.ACTION_DOWN) {
                    //隐藏键盘
                    ScreenUtils.hideSoftInput(et_nickname)
                    et_nickname.clearFocus()
                }
                if (ev?.action == MotionEvent.ACTION_DOWN && mViewModel.nicknameChange) {
                    val nickname = et_nickname.text.toString()
                    if (mViewModel.nicknameChange && mViewModel.nicknameEnable.value != true && nickname.isNotEmpty()) {
                        mViewModel.checkNickName(nickname)
                    }
                }
            }
        }

        et_nickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mViewModel.nicknameEnable.value = false
                mViewModel.nicknameChange = true
                val contentLength = s?.length ?: 0
                if (contentLength > 0) {
                    iv_clear_nickname.show()
                } else {
                    iv_clear_nickname.inVisible()
                }
                judgeNextEnable()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        sdv_header.onClickNew {
            //选择头像
            checkPermissions()
        }

        iv_clear_nickname.onClickNew {
            et_nickname.setText("")
        }

        tv_next.onClickNew {
            if (mViewModel.nicknameEnable.value == true) {
                val headerPic = mViewModel.headerPicData.value ?: ""
                if(headerPic.isEmpty()){
                    return@onClickNew
                }
                val nickname = et_nickname.text.toString()
                val birthday = getTime(mViewModel.birthdayDate ?: return@onClickNew) ?: return@onClickNew
                mViewModel.subCreate(CreateAccountForm(nickname, headerPic, birthday))
            }
        }
    }

    private fun initViewModel() {
        mViewModel.headerPicData.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                if (it.contains("http")) {
                    sdv_header.setImageURI(it)
                } else {
                    sdv_header.loadImage(it, 80f, 80f)
                }
                judgeNextEnable()
            }
        })
        mViewModel.loadingDismissState.observe(this, androidx.lifecycle.Observer {
            if (it == true) {
                mLoadingDialog.dismiss()
            }
        })
        mViewModel.createAccountSuccess.observe(this, androidx.lifecycle.Observer {
            if (it == true) {
                setResult(Activity.RESULT_OK)
                EventBus.getDefault().post(CreateAccountSuccess())
                finish()
            }
        })
        mViewModel.nicknameEnable.observe(this, androidx.lifecycle.Observer {
            if (it == true) {
                judgeNextEnable()
            }
        })
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
            mViewModel?.birthdayDate = date
            tv_date.text = getTime(date)
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

    /**
     * 判断下一步是否可用
     */
    private fun judgeNextEnable() {
        val header = mViewModel.headerPicData.value ?: ""
        val nicknameEnable = mViewModel.nicknameEnable.value == true
        val birEnable = mViewModel?.birthdayDate != null

        tv_next.isSelected = nicknameEnable && birEnable && header.isNotEmpty()
    }


    private fun checkPermissions() {
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

    /**
     *
     */
    private fun goToPictureSelectPager() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .theme(R.style.picture_me_style_single)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
            .minSelectNum(1)// 最小选择数量
            .imageSpanCount(4)// 每行显示个数
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(false)// 是否可预览图片
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
//            .cropWH(200, 200)// 裁剪宽高比，设置如果大于图片本身宽高则无效
            .withAspectRatio(3, 4)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
            .freeStyleCropEnabled(false)// 裁剪框是否可拖拽
            .isDragFrame(false)
//            .circleDimmedLayer(true)// 是否圆形裁剪
//            .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
//            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
//            .rotateEnabled(false) // 裁剪是否可旋转图片
            .scaleEnabled(true)// 裁剪是否可放大缩小图片
            .forResult(PictureConfig.CHOOSE_REQUEST)

        //结果回调onActivityResult code
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
                    logger.info("收到图片:$path")
                    if (!mLoadingDialog.isShowing) {
                        mLoadingDialog.showDialog()
                    }

                    mViewModel.uploadHead(path)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.info("图片返回出错了")
        }
    }
}