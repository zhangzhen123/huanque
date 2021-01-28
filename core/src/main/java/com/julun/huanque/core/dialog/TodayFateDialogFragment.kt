package com.julun.huanque.core.dialog

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.TodayFateInfo
import com.julun.huanque.common.bean.beans.TodayFateItem
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.ErrorCodes
import com.julun.huanque.common.constant.BalanceNotEnoughType
import com.julun.huanque.common.constant.Sex
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.fragment.BalanceNotEnoughFragment
import com.julun.huanque.core.viewmodel.TodayFateViewModel
import kotlinx.android.synthetic.main.dialog_today_fate_girl.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/27 16:31
 *
 *@Description: 今日缘分配对
 *
 */
class TodayFateDialogFragment : BaseDialogFragment() {

    val mViewModel: TodayFateViewModel by activityViewModels()

    override fun getLayoutId(): Int = R.layout.dialog_today_fate_girl

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = false
        super.onCreate(savedInstanceState)
    }

    override fun configDialog() {
        setDialogSize(gravity = Gravity.CENTER, marginWidth = 28, height = ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    override fun initViews() {

        girlList.layoutManager = GridLayoutManager(requireContext(), 2)
        initViewModel()
        girlList.adapter = matchesAdapter
        girlList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(10)))
        girlList.isNestedScrollingEnabled = false
        (girlList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        matchesAdapter.setOnItemClickListener { _, _, position ->
            val item = matchesAdapter.getItemOrNull(position)
            if (item != null) {
                item.select = !item.select
                matchesAdapter.notifyItemChanged(position)
                upgradePrice()
            }
        }
        tv_price.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG//中划线
        tv_price.paint.isAntiAlias = true
        close.onClickNew {
            dismiss()
        }
        go_action.onClickNew {
            currentFateInfo ?: return@onClickNew
            val list = currentFateInfo!!.fateList.filter { it.select }
            var ids = ""
            list.forEachIndexed { index, item ->
                if (index != list.size - 1) {
                    ids += item.userId + ","
                } else {
                    ids += item.userId
                }

            }
            if (list.isEmpty()) {
                ToastUtils.show2("请勾选要宠幸的妹子哦")
            } else {
                mViewModel.quickAccost(ids)
            }

        }
//        mViewModel.requestInfo()

    }

    override fun reCoverView() {
        initViewModel()
    }

    private fun initViewModel() {

        mViewModel.matchesInfo.observe(this, Observer {
            it ?: return@Observer
            if (it.isSuccess()) {
                loadDataSuccess(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail()
            }
        })
        mViewModel.closeFateDialog.observe(this, Observer {
            if (it != null) {
                dismiss()
                mViewModel.closeFateDialog.value = null
            }
        })

        mViewModel.quickAccostError.observe(this, Observer {
            if (it != null) {
                mViewModel.quickAccostError.value = null
                if (it is ResponseError) {
                    when (it.busiCode) {
                        ErrorCodes.BALANCE_NOT_ENOUGH -> {
                            dismiss()
                            BalanceNotEnoughFragment.newInstance(BalanceNotEnoughType.Small).show(requireActivity(), "BalanceNotEnoughFragment")
                        }
                    }
                }
            }
        })

    }

    private var currentFateInfo: TodayFateInfo? = null
    private fun loadDataSuccess(data: TodayFateInfo) {
        currentFateInfo = data
        matchesAdapter.setNewInstance(data.fateList)
        upgradePrice()

    }

    private fun loadDataFail() {
        ToastUtils.show("刷新失败")
    }

    private fun upgradePrice() {
        currentFateInfo ?: return
        val price: Long = currentFateInfo!!.originalPrice
        val discount: Long = currentFateInfo!!.unitPrice
        val fateList: MutableList<TodayFateItem> = currentFateInfo!!.fateList
        val size = fateList.filter { it.select }.size
        tv_price.text = "${price * size}鹊币"
        if (discount == 0L) {
            tv_discount.text = "限时免费"
        } else {
            tv_discount.text = "限时${discount * size}"
        }
    }

    override fun setWindowAnimations() {
        dialog?.window?.setWindowAnimations(com.julun.huanque.common.R.style.dialog_center_open_ani)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.matchesInfo.value = null
        mViewModel.closeFateDialog.value = null
        mViewModel.closeFateDialogTag.value = true
    }

    private val matchesAdapter =
        object : BaseQuickAdapter<TodayFateItem, BaseViewHolder>(R.layout.item_today_matches_list) {

            override fun convert(holder: BaseViewHolder, item: TodayFateItem) {
                holder.setText(R.id.anchor_nickname, item.nickname)
                ImageUtils.loadImage(
                    holder.getView(R.id.anchorPicture)
                        ?: return, item.headPic + BusiConstant.OSS_350, 150f, 150f
                )
                if (item.city.isEmpty()) {
                    holder.setGone(R.id.anchor_city, true)
                } else {
                    holder.setGone(R.id.anchor_city, false)
                    holder.setText(R.id.anchor_city, item.city)
                }
                val sex = holder.getView<TextView>(R.id.tv_sex)
                sex.text = "${item.age}"
                when (item.sex) {//Male、Female、Unknow

                    Sex.FEMALE -> {
                        val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_sex_female)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#FF9BC5")
                        sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                    }
                    else -> {
                        val drawable = ContextCompat.getDrawable(context, R.mipmap.icon_sex_male)
                        if (drawable != null) {
                            drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                            sex.setCompoundDrawables(drawable, null, null, null)
                        }
                        sex.textColor = Color.parseColor("#58CEFF")
                        sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                    }
                }
                ImageUtils.loadImageLocal(holder.getView(R.id.bg_shadow), R.mipmap.bg_shadow_home_item)

                val authTag = holder.getView<SimpleDraweeView>(R.id.iv_auth_tag)
                if (item.authMark.isNotEmpty()) {
                    authTag.show()
                    ImageUtils.loadImageWithHeight_2(authTag, item.authMark, dp2px(13))
                } else {
                    authTag.hide()
                }
                holder.getView<ImageView>(R.id.iv_select).isSelected = item.select
            }
        }

}