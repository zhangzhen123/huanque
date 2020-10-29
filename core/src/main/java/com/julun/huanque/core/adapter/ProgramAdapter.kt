package com.julun.huanque.core.adapter

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.AdInfoBean
import com.julun.huanque.common.bean.beans.MultiBean
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.huanque.core.R
import java.math.RoundingMode
import java.text.DecimalFormat

class ProgramAdapter : BaseMultiItemQuickAdapter<MultiBean, BaseViewHolder>(), LoadMoreModule {
    init {
        addItemType(ProgramItemType.BANNER, R.layout.item_program_banner)
        addItemType(ProgramItemType.NORMAL, R.layout.item_live_square_anchor_list)
    }

    override fun convert(holder: BaseViewHolder, item: MultiBean) {
        when (holder.itemViewType) {
            ProgramItemType.NORMAL -> {
                val bean = item.content
                if (bean is ProgramLiveInfo) {
                    convertNormal(holder, bean)
                }

            }
            ProgramItemType.BANNER -> {
                val bean = item.content
                if (bean is MutableList<*>) {
                    val list = mutableListOf<AdInfoBean>()
                    bean.forEach { ad ->
                        if (ad is AdInfoBean) {
                            list.add(ad)
                        }
                    }
                    convertBanner(holder, list)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        logger("onCreateViewHolder itemViewType:$viewType")
        val holder = super.onCreateViewHolder(parent, viewType)
        if (viewType == ProgramItemType.BANNER) {
            val banner = holder.getView<BGABanner>(R.id.banner)
            val screenWidth = ScreenUtils.getScreenWidth() - dp2px(10) * 2
            //修改banner高度原来0.3022 现在0.38356
            val lp = banner.layoutParams
            lp.height = (screenWidth * 0.38356).toInt()
//                lp.width = screenWidth
        }
        return holder
    }

    private fun convertBanner(holder: BaseViewHolder, ads: MutableList<AdInfoBean>) {
        val banner = holder.getView<BGABanner>(R.id.banner)
        banner.setAdapter(bannerAdapter)
        banner.setDelegate(bannerItemCick)
        banner.setData(ads, null)
        // 默认是自动轮播的，当只有一条广告数据时，就不轮播了
        banner.setAutoPlayAble(ads.size > 1)
    }

    private fun convertNormal(holder: BaseViewHolder, item: ProgramLiveInfo) {
        holder.setText(R.id.anchor_nickname, item.programName)
        val textHot = holder.getView<TextView>(R.id.user_count)
        textHot.setTFDinCdc2()
        if (item.heatValue < 10000) {
            textHot.text = "${item.heatValue}"
            holder.setGone(R.id.user_count_w, true)
        } else {
            val format = DecimalFormat("#.0")
            format.roundingMode = RoundingMode.HALF_UP
            textHot.text = "${format.format((item.heatValue / 10000.0))}"
            holder.setGone(R.id.user_count_w, false)
        }
        ImageUtils.loadImage(
            holder.getView(R.id.anchorPicture)
                ?: return, item.coverPic + BusiConstant.OSS_350, 150f, 150f
        )
        if (item.city.isEmpty()) {
            holder.setGone(R.id.anchor_city, true)
        } else {
            holder.setGone(R.id.anchor_city, false)

            holder.setText(R.id.anchor_city, item.city)
        }
        ImageUtils.loadImageLocal(holder.getView(R.id.bg_shadow), R.mipmap.bg_shadow_home_item)
        val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
        val tv_author_status = holder.getView<TextView>(R.id.tv_author_status)

        if (item.rightTopTag.isNotEmpty()) {
            sdv_pic.show()
            ImageUtils.loadImageWithHeight_2(sdv_pic, StringHelper.getOssImgUrl(item.rightTopTag), dp2px(16))
            tv_author_status.hide()
        } else {
            sdv_pic.hide()
            if (item.isLiving) {
                holder.setVisible(R.id.tv_author_status, true).setText(R.id.tv_author_status, "直播中")
            } else {
                holder.setGone(R.id.tv_author_status, true)
            }
        }
    }

    // 轮播adapter,填充广告图片数据3
    private val bannerAdapter by lazy {
        BGABanner.Adapter<SimpleDraweeView, AdInfoBean> { banner, itemView, model, position ->
            val hierarchy = GenericDraweeHierarchyBuilder.newInstance(context.resources)
                .setRoundingParams(RoundingParams.fromCornersRadius(dp2pxf(6)))
                .build()
            itemView.hierarchy = hierarchy

            when (model?.resType) {
                BannerResType.Pic -> {
                    val screenWidth = ScreenUtils.screenWidthFloat.toInt() - dp2px(15f) * 2
                    val height = dp2px(72f)
                    ImageUtils.loadImageInPx(itemView, model.resUrl, screenWidth, height)
                }
            }

        }
    }

    //点击广告图片
    private val bannerItemCick by lazy {
        BGABanner.Delegate<ImageView, AdInfoBean> { bgaBanner, itemView, model, position ->
            when (model?.touchType) {
                BannerTouchType.Url -> {
                    val extra = Bundle()
                    extra.putString(BusiConstant.WEB_URL, model.touchValue)
                    extra.putBoolean(IntentParamKey.EXTRA_FLAG_GO_HOME.name, false)
//                    context.jump(WebActivity::class.java, extra = extra)
                    ARouter.getInstance().build(ARouterConstant.WEB_ACTIVITY).with(extra).navigation()
                }
                BannerTouchType.Toast -> {
                    //弹窗类型
                    if (model.touchValue == "FirstRecharge") {
                        //todo
//                        val rechargeInfo = mFirstRechargeViewModel?.oneYuanInfo?.value
//                            ?: return@Delegate
//                        OneYuanDialogFragment.newInstance(rechargeInfo).showPositive(childFragmentManager, "OneYuanDialogFragment")
                    }
                }
                BannerTouchType.Room -> {
                    val roomId = model.touchValue.toLongOrNull()
                    val extra = Bundle()
                    if (roomId != null)
                        extra.putLong(IntentParamKey.PROGRAM_ID.name, roomId)
                    ARouter.getInstance().build(ARouterConstant.PLAYER_ACTIVITY).with(extra).navigation()
                }
            }
        }
    }
}
