package com.julun.huanque.core.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.helper.DensityHelper.Companion.dp2px
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ImageUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import java.io.File

/**
 * author：luck
 * project：PictureSelector
 * package：com.luck.pictureselector.adapter
 * email：893855882@qq.com
 * data：16/7/27
 */
class AddPictureAdapter(
    private val context: Context,
    /**
     * 点击添加图片跳转
     */
    private val mOnAddPicClickListener: OnAddPicClickListener
) : RecyclerView.Adapter<AddPictureAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var list: MutableList<LocalMedia> = arrayListOf()
    private var selectMax = 9
    var isPublishVideo = false
    private var mItemClickListener: OnItemClickListener? = null

    interface OnAddPicClickListener {
        fun onAddPicClick()
    }

    private var mHeight: Int = 0

    init {
        //13.5+13.5+1.5*4 日料够了 这里计算的实际显示有问题
        mHeight = (ScreenUtils.getScreenWidth() - dp2px(36)) / 3
    }

    fun setHeight(height:Int){
        mHeight=height
    }
    fun setSelectMax(selectMax: Int) {
        this.selectMax = selectMax
    }

    fun setList(list: MutableList<LocalMedia>) {
        this.list = list
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var ll_parent: View
        internal var mImg: SimpleDraweeView
        internal var ll_del: LinearLayout
        internal var video_play_view: ImageView
        internal var ll_failure_notice :View

        init {
            ll_parent = view.findViewById(R.id.add_img_parent)
            mImg = view.findViewById<View>(R.id.fiv) as SimpleDraweeView
            ll_del = view.findViewById<View>(R.id.ll_del) as LinearLayout
            video_play_view = view.findViewById<ImageView>(R.id.iv_video_play) as ImageView
            ll_failure_notice=view.findViewById<View>(R.id.ll_failure_notice)
        }
    }

    override fun getItemCount(): Int {
        return if (list.size < selectMax) {
            list.size + 1
        } else {
            list.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowAddItem(position)) {
            TYPE_CAMERA
        } else {
            TYPE_PICTURE
        }
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = mInflater.inflate(
            R.layout.item_add_image,
            viewGroup, false
        )
        val viewHolder = ViewHolder(view)
        val pp = viewHolder.ll_parent.layoutParams
        if (isPublishVideo) {
            pp.width = dp2px(114f)
            pp.height = dp2px(152f)
            ULog.i("视频显示 改变宽度：${pp.width}")
        } else {
            pp.width = mHeight
            pp.height = mHeight
            ULog.i("图片显示 改变宽度：$mHeight")
        }
        return viewHolder
    }

    private fun isShowAddItem(position: Int): Boolean {
        val size = if (list.size == 0) 0 else list.size
        return position == size
    }

    /**
     * 设置值
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        //少于8张，显示继续添加的图标
        if (getItemViewType(position) == TYPE_CAMERA) {
            if (isPublishVideo) {
                ImageUtils.loadImageLocal(viewHolder.mImg, R.mipmap.icon_upload_image_02)
            } else {
                //以后扩展用的
                ImageUtils.loadImageLocal(viewHolder.mImg, R.mipmap.icon_upload_image_02)
            }
            viewHolder.mImg.onClickNew { mOnAddPicClickListener.onAddPicClick() }
            viewHolder.ll_del.visibility = View.INVISIBLE
        } else {
            viewHolder.ll_del.visibility = View.VISIBLE
            viewHolder.ll_del.onClickNew {
                val index = viewHolder.adapterPosition
                // 这里有时会返回-1造成数据下标越界,具体可参考getAdapterPosition()源码，
                // 通过源码分析应该是bindViewHolder()暂未绘制完成导致，知道原因的也可联系我~感谢
                if (index != RecyclerView.NO_POSITION) {
                    list.removeAt(index)
                    notifyItemRemoved(index)
                    notifyItemRangeChanged(index, list.size)
                    mItemClickListener?.onItemDeleteClick(position)
                }
            }
            val media = list[position]
            val mimeType = media.getMimeType()
            var path = ""
            if (media.isCut() && !media.isCompressed()) {
                // 裁剪过
                path = media.getCutPath()
            } else if (media.isCompressed() || media.isCut() && media.isCompressed()) {
                // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                path = media.compressPath
            } else {
                // 原图
                path = media.path
            }
            //动图只使用原图
            val isGif = PictureMimeType.isGif(media.pictureType)
            if (isGif) {
                path = media.path
            }
            // 图片
            if (media.isCompressed()) {
                Log.i("compress image result:", "${File(media.compressPath).length() / 1024} k")
                Log.i("压缩地址::", media.compressPath)
            }

            Log.i("原图地址::", media.getPath())
            val pictureType = PictureMimeType.isPictureType(media.getPictureType())
            if (media.isCut()) {
                Log.i("裁剪地址::", media.getCutPath())
            }
            viewHolder.video_play_view.visibility = if (pictureType == PictureConfig.TYPE_VIDEO)
                View.VISIBLE
            else
                View.GONE
            ImageUtils.loadNativeFilePath(viewHolder.mImg, path)
            //itemView 的点击事件
            if (mItemClickListener != null) {
                viewHolder.itemView.setOnClickListener { v ->
                    val adapterPosition = viewHolder.adapterPosition
                    mItemClickListener!!.onItemClick(adapterPosition, v)
                }
            }

            if(media.isFail){
                viewHolder.ll_failure_notice.show()
            }else{
                viewHolder.ll_failure_notice.hide()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, v: View)
        fun onItemDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mItemClickListener = listener
    }

    companion object {
        const val TYPE_CAMERA = 1
        const val TYPE_PICTURE = 2
    }
}
