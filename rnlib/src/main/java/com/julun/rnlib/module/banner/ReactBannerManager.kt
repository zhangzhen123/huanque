package com.julun.rnlib.module.banner

import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.viewpager.widget.ViewPager
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerModule
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.EventDispatcher
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.rnlib.R


class ReactBannerManager : SimpleViewManager<BGABanner?>() {

    private var banner: BGABanner? = null
    private var eventDispatcher: EventDispatcher? = null

    companion object {
        const val REACT_CLASS = "RNCBannerView"
    }

    @NonNull
    override fun getName(): String {
        return REACT_CLASS
    }

    @NonNull
    override fun createViewInstance(@NonNull reactContext: ThemedReactContext): BGABanner {
        eventDispatcher = reactContext.getNativeModule(UIManagerModule::class.java).eventDispatcher
        val banner =
            LayoutInflater.from(reactContext).inflate(R.layout.hq_rn_view_banner, null) as BGABanner
        banner!!.setAdapter(BGABanner.Adapter<FrameLayout, String> { _, itemView, pic, _ ->
            val image = itemView.findViewById<SimpleDraweeView>(R.id.sdv_image)
            image?.let {
                if (pic != null) {
                    ImageUtils.loadImageNoResize(it, pic)
                }
            }
        })
        // 点击事件
        banner!!.setDelegate { banner, _, _, position ->
            eventDispatcher?.dispatchEvent(BannerItemClickEvent(banner!!.id, position))
        }
        banner!!.setIsNeedShowIndicatorOnOnlyOnePage(false)
        banner!!.setRNViewPagerStatus(true)
        return banner!!
    }

    // 设置数据源（暂只支持纯图片的数据源List<String>）
    @ReactProp(name = "list")
    fun setPicList(banner: BGABanner, list: ReadableArray) {
        if (list.size() > 0) {
            val picList: MutableList<String?> = ArrayList()
            for (i in 0 until list.size()) {
                picList.add(list.getString(i))
            }
            // 自定义布局：以中心对准填满整个视图
            banner.setData(R.layout.item_rn_banner_image, picList, null)

            // 滑动切换事件
            banner.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    if (position >= 0 && position < list.size()) {
                        eventDispatcher?.dispatchEvent(
                            BannerIndexChangeEvent(
                                banner.id,
                                position
                            )
                        )
                    }
                }

            })
        }
    }

    // 设置自动播放
    @ReactProp(name = "autoPlay", defaultBoolean = true)
    fun setAutoPlay(banner: BGABanner, autoPlay: Boolean) {
        banner.setAutoPlayAble(autoPlay);
    }

    // 设置自动播放间隔
    @ReactProp(name = "autoPlayInterval", defaultInt = 3000)
    fun setAutoPlayInterval(banner: BGABanner, autoPlayInterval: Int) {
        banner.setAutoPlayInterval(autoPlayInterval)
    }

    // 设置当前激活item
    @ReactProp(name = "index", defaultInt = 0)
    fun setIndex(banner: BGABanner, index: Int) {
        banner.currentItem = index;
    }

    // center：底部居中，right：底部居右
    @ReactProp(name = "indicatorPosition")
    fun setIndicatorPosition(banner: BGABanner, indicatorPosition: String?) {
        if (indicatorPosition?.equals("center") ?: ("center" == null)) {
            banner.setPointGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM);
        } else if (indicatorPosition?.equals("right")!!) {
            banner.setPointGravity(Gravity.RIGHT or Gravity.BOTTOM);

        }
    }

    // 覆盖在每个item图片上的背景颜色：在图片之上覆盖一层白色透明颜色
    @ReactProp(name = "overlayColor")
    fun setIndicatorBottom(banner: BGABanner, overlayColor: String?) {

    }

    // 设置指示器距离底部距离（默认指示器位置为底部居中）
    @ReactProp(name = "indicatorBottom")
    fun setIndicatorBottom(banner: BGABanner, indicatorBottom: Int?) {
        if (indicatorBottom != null) {
            banner.setPointTopBottomMargin(indicatorBottom)
        }
    }

    // 无限循环
    @ReactProp(name = "infinite", defaultBoolean = true)
    fun setInfinite(banner: BGABanner, infinite: Boolean?) {
    }

    // 是否显示分页指示器
    @ReactProp(name = "showIndicator")
    fun setShowIndicator(banner: BGABanner, showIndicator: Boolean?) {

    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        return MapBuilder.of(
            BannerItemClickEvent.EVENT_NAME, MapBuilder.of("registrationName", "onPageClick"),
            BannerIndexChangeEvent.EVENT_NAME, MapBuilder.of("registrationName", "onIndexChange")
        );
    }
}
