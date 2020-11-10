package com.julun.huanque.core.ui.homepage

import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.HomePageCarInfo
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.viewmodel.HomePageViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.fragment_car_detail.*

/**
 *@创建者   dong
 *@创建时间 2020/11/9 13:55
 *@描述 座驾详情弹窗
 */
class CarDetailFragment : BaseDialogFragment() {
    private val mHomePageViewModel: HomePageViewModel by activityViewModels()
    override fun getLayoutId() = R.layout.fragment_car_detail

    override fun needEnterAnimation() = false

    override fun initViews() {
        iv_close.onClickNew {
            dismiss()
        }
        tv_my_car.onClickNew {
            RNPageActivity.start(requireActivity(), RnConstant.MY_CAR_PAGE)
        }
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.CENTER, 0, ViewGroup.LayoutParams.MATCH_PARENT)
        initViewModel()
    }

    private fun initViewModel() {
        mHomePageViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showCarInfo(it.carInfo)
            }
        })
    }

    /**
     * 显示座驾数据
     */
    private fun showCarInfo(info: HomePageCarInfo) {
        ImageUtils.loadImageNoResize(sdv_car, StringHelper.getOssAudioUrl(info.dynamicUrl))
        tv_car_condition.text = "成为${info.royalName}贵族即可驾驶"
        tv_car_name.text = info.carName

    }
}