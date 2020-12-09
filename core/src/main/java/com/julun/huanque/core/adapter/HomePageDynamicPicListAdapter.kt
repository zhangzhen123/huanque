package com.julun.huanque.core.adapter

import android.os.Build
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.bean.beans.CircleGroup
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.bean.beans.HomePageProgram
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.core.R
import com.julun.huanque.core.widgets.PlayerConstraintLayout
import com.julun.huanque.core.widgets.SingleVideoView
import com.julun.huanque.core.widgets.SurfaceVideoViewOutlineProvider
import kotlinx.android.synthetic.main.act_home_page.*

/**
 *@创建者   dong
 *@创建时间 2020/11/5 10:47
 *@描述 主页动态使用的Adapter
 */
class HomePageDynamicPicListAdapter : BaseDelegateMultiAdapter<Any, BaseViewHolder>() {
    //是否是本人主页
    var mineHomePage = false

    companion object {
        //显示更多的标记位
        const val Tag_More = "Tag_More"

        //直播画面
        const val Living = 0x001

        //其他样式
        const val Normal = 0x002
    }

    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<Any>() {
            override fun getItemType(data: List<Any>, position: Int): Int {
                return when (data.getOrNull(position)) {
                    is HomePageProgram -> {
                        Living
                    }
                    else -> {
                        Normal
                    }
                }
            }
        })
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()?.addItemType(Living, R.layout.recycler_item_homepage_dynamic_living)
            ?.addItemType(Normal, R.layout.recycler_item_homepage_dynamic_pic)
    }

    override fun convert(holder: BaseViewHolder, item: Any) {
        val itemType = holder.itemViewType
//        val root = holder.itemView
//        val params = root.layoutParams
//        params.height = dp2px(74)
//        params.width = dp2px(77)
//        root.layoutParams = params

        if (itemType == Living) {
            //直播中样式
            if (item is HomePageProgram) {
                //显示直播中画面

                val player_con = holder.getView<PlayerConstraintLayout>(R.id.player_con)
                player_con.setmHomeProgramInfo(item)

                val sdv_living = holder.getView<SimpleDraweeView>(R.id.sdv_living)
                val tv_living = holder.getView<TextView>(R.id.tv_living)
                if (item.living == BusiConstant.True) {
                    //开播中
                    sdv_living.show()
                    tv_living.text = "直播中"
                    ImageUtils.loadGifImageLocal(sdv_living, R.mipmap.living_home_page_player)

                } else {
                    sdv_living.hide()
                    tv_living.text = "未开播"
                }


            }
        } else if (itemType == Normal) {
            //其他样式
            if (item is String) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.sdv)
                val view_more = holder.getView<View>(R.id.view_more)
                val tv_more = holder.getView<View>(R.id.tv_more)
                val iv_more = holder.getView<View>(R.id.iv_more)
                if (item != Tag_More) {
                    //显示图片
                    sdv.loadImage(item, 74f, 74f)
                    sdv.show()
                    view_more.hide()
                    tv_more.hide()
                    iv_more.hide()
                } else {
                    //显示查看更多
                    sdv.hide()
                    view_more.show()
                    tv_more.show()
                    iv_more.show()
                }
            }
        }

    }
}