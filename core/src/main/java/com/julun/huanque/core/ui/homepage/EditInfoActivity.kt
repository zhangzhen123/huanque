package com.julun.huanque.core.ui.homepage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.forms.SaveSchoolForm
import com.julun.huanque.common.bean.forms.UpdateUserInfoForm
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomePageTagAdapter
import com.julun.huanque.core.ui.record_voice.VoiceSignActivity
import com.julun.huanque.core.viewmodel.EditInfoViewModel
import kotlinx.android.synthetic.main.act_edit_info.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.textColor
import kotlin.math.max

/**
 *@创建者   dong
 *@创建时间 2020/12/29 19:17
 *@描述 编辑资料页面
 */
class EditInfoActivity : BaseActivity() {
    private val mEditInfoViewModel: EditInfoViewModel by viewModels()

    //进度条的总宽度
    private val TOTAL_PROGRESS_WIDTH = ScreenUtils.getScreenWidth() - dp2px(141f)

    //标签adapter
    private val mTagAdapter = HomePageTagAdapter()

    //我喜欢的标签adapter
    private val mLikeTagAdapter = HomePageTagAdapter()

    private var mBarHeiht = 0

    override fun isRegisterEventBus() = true

    override fun getLayoutId() = R.layout.act_edit_info

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mBarHeiht = StatusBarUtil.getStatusBarHeight(this)
        header_page.textTitle.text = "编辑资料"
        header_page.textOperation.show()
        header_page.textOperation.text = "保存"
        header_page.textOperation.isEnabled = false
        initRecyclerView()
        initViewModel()
        mEditInfoViewModel.getBasicInfo()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        header_page.imageViewBack.onClickNew {
            finish()
        }
        header_page.textOperation.onClickNew {
            //保存
        }
        tv_nickname_title.onClickNew {
            //昵称
            UpdateNicknameActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.nickname ?: "")
        }
        tv_sign_title.onClickNew {
            UpdateSignActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.mySign ?: "")
        }
        tv_voice_title.onClickNew {
            val intent = Intent(this, VoiceSignActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                mEditInfoViewModel.needFresh = true
                startActivity(intent)
            }
        }
        iv_close_progress.onClickNew {
            //隐藏资料完成度布局
            con_progress.hide()
        }

        tv_home_town_title.onClickNew {
            //选择家乡
            val info = mEditInfoViewModel.basicInfo.value ?: return@onClickNew
            val homeTownStr = StringBuilder()
            if (info.homeTown.homeTownProvince.isNotEmpty()) {
                homeTownStr.append(info.homeTown.homeTownProvince)
            }
            if (homeTownStr.isNotEmpty()) {
                homeTownStr.append("/")
            }
            homeTownStr.append(info.homeTown.homeTownCity)
            HomeTownActivity.newInstance(this, homeTownStr.toString())
        }
        tv_age_constellation_title.onClickNew {
            //年龄  星座
            val basicInfo = mEditInfoViewModel.basicInfo.value ?: return@onClickNew
            UpdateBirthdayActivity.newInstance(this, basicInfo.birthday)
        }

        tv_school_title.onClickNew {
            //学校
            SchoolActivity.newInstance(this, mEditInfoViewModel.basicInfo.value?.schoolInfo ?: return@onClickNew)
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mEditInfoViewModel.basicInfo.observe(this, Observer {
            showViewByData(it ?: return@Observer)
        })
    }

    /**
     * 显示数据
     */
    private fun showViewByData(info: HomePageInfo) {
        updateProgress(info.perfection)

        val normalColor = GlobalUtils.getColor(R.color.black_333)
        val greyColor = GlobalUtils.getColor(R.color.black_999)
        tv_nickname.text = info.nickname
        //签名
        val sign = info.mySign
        if (sign.isEmpty()) {
            tv_sign.text = "编辑个签，展示我的独特态度"
            tv_sign.textColor = greyColor
        } else {

            tv_sign.textColor = normalColor
            if (sign.length <= 20) {
                tv_sign.text = sign
            } else {
                tv_sign.text = sign.substring(1, 20).plus("...")
            }
        }

        //语音签名
        val voiceBean = info.voice
        if (voiceBean.voiceUrl.isEmpty() && voiceBean.voiceStatus.isEmpty()) {
            //未录制过音频
            tv_voice.text = "把你唱给Ta听"
            tv_voice.textColor = greyColor
        } else {
            //录制过音频
            tv_voice.textColor = greyColor
            when (voiceBean.voiceStatus) {
                VoiceBean.Wait -> {
                    //等待审核
                    tv_voice.text = "等待审核"
                }
                VoiceBean.Reject -> {
                    //被拒绝
                    tv_voice.text = "审核不通过"
                }
                VoiceBean.Pass -> {
                    //审核通过
                    tv_voice.text = "${info.voice.length}秒录音"
                }
            }

        }

        //基本资料
        val homeTownStr = StringBuilder()
        if (info.homeTown.homeTownProvince.isNotEmpty()) {
            homeTownStr.append(info.homeTown.homeTownProvince)
        }
        if (homeTownStr.isNotEmpty()) {
            homeTownStr.append("/")
        }
        homeTownStr.append(info.homeTown.homeTownCity)
        tv_home_town.text = homeTownStr.toString()

        //年龄和星座
        val ageConstell = StringBuilder()
        ageConstell.append(info.age)
        ageConstell.append("/")
        ageConstell.append(info.constellationInfo.constellationName)
        tv_age_constellation.text = ageConstell.toString()

        //身材
        val weight = info.figure.weight
        val height = info.figure.height
        val whBuilder = StringBuilder()
        if (height > 0) {
            whBuilder.append("${height}cm")
        }
        if (weight > 0) {
            if (height > 0) {
                whBuilder.append("/")
            }
            whBuilder.append("${weight}kg")
        }
        tv_figure.text = whBuilder.toString()

        tv_school.text = info.schoolInfo.school
        tv_job.text = info.profession.professionName

        val wishList = info.wishList
        val wishStr = StringBuilder()
        wishList.forEach {
            if (wishStr.isNotEmpty()) {
                wishStr.append("/")
            }
            wishStr.append(it.wishTypeText)
        }
        if (wishStr.length <= 20) {
            tv_social_wish.text = wishStr.toString()
        } else {
            tv_social_wish.text = wishStr.substring(0, 20).plus("...")
        }

        val tagList = info.myAuthTag.showTagList
        tv_tag_count.text = "${info.myAuthTag.markTagNum}"
        val realTagList = mutableListOf<HomeTagBean>()
        if (tagList.size >= 4) {
            tv_more_tag.show()
            tagList.take(4).forEach {
                realTagList.add(it)
            }
        } else {
            tv_more_tag.hide()
            realTagList.addAll(tagList)
        }
        mTagAdapter.setList(realTagList)

        val likeTagList = info.myLikeTag.showTagList
        tv_like_tag_count.text = "${info.myLikeTag.markTagNum}"
        val realLikeTagList = mutableListOf<HomeTagBean>()
        if (likeTagList.size >= 4) {
            tv_more_like_tag.show()
            likeTagList.take(4).forEach {
                realLikeTagList.add(it)
            }
        } else {
            tv_more_like_tag.hide()
            realLikeTagList.addAll(likeTagList)
        }
        mLikeTagAdapter.setList(realLikeTagList)

        tv_user_id.text = "欢鹊ID ${info.userId}"
    }

    /**
     * 设置进度条宽度
     */
    private fun updateProgress(progress: Int) {
        view_progress_placeholder
        progressBar.progress = progress
        val placeViewWidtth = TOTAL_PROGRESS_WIDTH * progress / 100
        val placeParams = view_progress_placeholder.layoutParams
        placeParams.width = max(placeViewWidtth, 1)
        view_progress_placeholder.layoutParams = placeParams
        tv_progress.text = "${progress}%"
        tv_progress.postInvalidate()
    }


    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recycler_view_tag.layoutManager = GridLayoutManager(this, 4)
        recycler_view_tag.adapter = mTagAdapter
        mTagAdapter.setOnItemClickListener { adapter, view, position ->
//            ll_tag.performClick()
        }

        recycler_view_like_tag.layoutManager = GridLayoutManager(this, 4)
        recycler_view_like_tag.adapter = mLikeTagAdapter

    }

    override fun onRestart() {
        super.onRestart()
        if (mEditInfoViewModel.needFresh) {
            mEditInfoViewModel.getBasicInfo()
            mEditInfoViewModel.needFresh = false
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun userInfoUpdate(info: UpdateUserInfoForm) {
        val nickname = info.nickname
        if (nickname?.isNotEmpty() == true) {
            //昵称变动
            tv_nickname.text = nickname
            mEditInfoViewModel.basicInfo.value?.nickname = nickname
        }
        val sign = info.mySign
        if (sign?.isNotEmpty() == true) {
            //个性签名变动
            tv_sign.text = sign
            tv_sign.textColor = GlobalUtils.getColor(R.color.black_333)
            mEditInfoViewModel.basicInfo.value?.mySign = sign
        }
        val cityName = info.cityName
        val provinceName = info.provinceName
        if (cityName?.isNotEmpty() == true && provinceName?.isNotEmpty() == true) {
            //城市名称
            mEditInfoViewModel.basicInfo.value?.homeTown?.homeTownProvince = provinceName
            mEditInfoViewModel.basicInfo.value?.homeTown?.homeTownCity = cityName
            //更新文案
            val homeTownStr = StringBuilder()
            homeTownStr.append(info.provinceName)
            homeTownStr.append("/")
            homeTownStr.append(info.cityName)
            tv_home_town.text = homeTownStr.toString()
        }
        val birthday = info.birthday
        if (birthday != null) {
            val birthDate = TimeUtils.string2Date("yyyy-MM-dd", birthday) ?: return
            val age = TimeUtils.getAgeByDate(birthDate)
            val constellationName = ConstellationUtils.getConstellation(birthDate).name
            if (constellationName.isNotEmpty()) {
                mEditInfoViewModel.basicInfo.value?.let {
                    it.birthday = birthday
                    it.age = age
                    it.constellationInfo.constellationName = constellationName
                }
                val ageConstell = StringBuilder()
                ageConstell.append(age)
                ageConstell.append("/")
                ageConstell.append(constellationName)
                tv_age_constellation.text = ageConstell.toString()
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun userProcesshange(bean: UserProcessBean) {
        mEditInfoViewModel.basicInfo.value?.perfection = bean.perfection
        updateProgress(bean.perfection)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun schoolChange(bean: SchoolInfo) {
        tv_school.text = bean.school
        mEditInfoViewModel.basicInfo.value?.schoolInfo?.let {
            if (bean.startYear.isNotEmpty()) {
                it.startYear = bean.startYear
            }
            if (bean.educationCode.isNotEmpty()) {
                it.educationCode = bean.educationCode
            }
            if (bean.education.isNotEmpty()) {
                it.education = bean.education
            }
            if (bean.school.isNotEmpty()) {
                it.school = bean.school
            }
        }
    }

}