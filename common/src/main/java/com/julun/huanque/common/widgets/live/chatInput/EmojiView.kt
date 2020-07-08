package com.julun.huanque.common.widgets.live.chatInput

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

import com.julun.huanque.common.R
import com.julun.huanque.common.widgets.kpswitch.widget.KPSwitchPanelLinearLayout
import com.julun.huanque.common.widgets.recycler.decoration.GridDecoration

import org.jetbrains.anko.*
import java.util.*

/**
 * Created by djp on 2016/11/30.
 */
class EmojiView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : KPSwitchPanelLinearLayout(context, attrs) {

    var viewPager: ViewPager? = null
    var faceDotsLayout: LinearLayout? = null
    // 所有表情图标集合
    private var emojiList: ArrayList<Emoji>? = null
    // viewpager 页面集合
    private var viewPagerItems: ArrayList<View>? = null
    // 每页显示7列
    private val columns = 7
    // 每页显示3行
    private val rows = 5
    // 每页显示表情图标个数
    private var pageNum = 0    //每页显示数量
    private var layoutInflater: LayoutInflater? = null
    private var listener: OnEmojiClickListener? = null

    init {
//        this.gravity=Gravity.CENTER
        this.orientation = VERTICAL
        this.backgroundColor = Color.WHITE
        //这里添加分割线
        view {
            layoutParams = LinearLayout.LayoutParams(matchParent, 1)
            this.backgroundColor = ContextCompat.getColor(context, R.color.divide_color)
        }
        //这是表情列表父容器
        verticalLayout {
            this.gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)

            viewPager = ViewPager(context)
            viewPager?.padding = dip(10)
            viewPager?.lparams {
                this.width = matchParent
                this.height = 64 * 5 + dip(16 * 7 + 10 * 2)
//                this.weight = 12f
            }
            addView(viewPager)

            //anko现在不能适配androidx 所以viewpager这样写有问题了，需要修改
//            viewPager = viewPager {
//                padding = dip(10)
//                gravity = Gravity.CENTER
//            }.lparams {
//                this.width = matchParent
//                this.height = 0
//                this.weight = 12f
//            }

            //这是底部的页面标识父容器，小圆点
            faceDotsLayout = linearLayout {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_HORIZONTAL
            }.lparams {
                this.width = matchParent
                this.height = wrapContent
//                this.weight = 1f
                this.bottomMargin = dip(5)
            }
        }

        // 每页显示20个表情图标，加一个删除图标
        pageNum = columns * rows - 1
        viewPagerItems = ArrayList<View>()
        if (!isInEditMode)
            this.initView()
    }

    // 总共页数
    private val pagerCount: Int
        get() {
            val size = emojiList!!.size
            return if (size % pageNum == 0) size / pageNum else size / pageNum + 1
        }

    private fun initView() {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(this.context)
        }
        emojiList = EmojiUtil.getEmojiList()
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(dip(5), 0, dip(5), 0)
        for (i in 0..pagerCount - 1) {
            viewPagerItems!!.add(getViewPagerItem(i))
            faceDotsLayout!!.addView(getDotImg(i), lp)
        }
        val faceAdapter = FaceVpAdapter(viewPagerItems!!)
        viewPager?.adapter = faceAdapter
        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                lightPositionDots(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
        })
        lightPositionDots(0)
    }

    // 每页标记图标
    private fun getDotImg(position: Int): ImageView {
        val dotImg = layoutInflater!!.inflate(R.layout.view_face_group_dot, null).findViewById<ImageView>(R.id.face_dot) as ImageView
        dotImg.id = position
        return dotImg
    }

    private fun getViewPagerItem(nums: Int): View {
        val view = layoutInflater!!.inflate(R.layout.view_face_gridview, null)
        val gridView = view.findViewById(R.id.faceGridView) as androidx.recyclerview.widget.RecyclerView

        gridView.layoutManager = GridLayoutManager(context, columns)

        // 每页显示图标集合
        val pageEmojiList = ArrayList<Emoji>()
        val fromIndex = nums * pageNum
        val toIndex = if (pageNum * (nums + 1) > emojiList!!.size) emojiList!!.size else pageNum * (nums + 1)
        pageEmojiList.addAll(emojiList!!.subList(fromIndex, toIndex))
        // 每页最后一个放删除按钮
        val deleteEmoji = Emoji()
        deleteEmoji.imageUri = R.mipmap.face_delete
        pageEmojiList.add(deleteEmoji)

        val emojiAdapter = EmojiAdapter(pageEmojiList)
        gridView.adapter = emojiAdapter
        gridView.addItemDecoration(GridDecoration(dip(16), dip(16)))

        // 单击表情执行的操作
        val pageEmojuCount = pageEmojiList.size
        emojiAdapter.setOnItemClickListener { adapter, view, position ->
            if (position < pageEmojuCount - 1) {
                listener?.onEmojiClick(pageEmojiList[position])
            } else {
                listener?.onEmojiDelete()
            }
        }

        return gridView
    }

    // 高亮显示被选中标记图标
    private fun lightPositionDots(position: Int) {
        val count = faceDotsLayout!!.childCount
        if (count == 0) return
        for (i in 0..count - 1) {
            if (i != position) {
                faceDotsLayout!!.getChildAt(i).isSelected = false
            } else {
                faceDotsLayout!!.getChildAt(i).isSelected = true
            }
        }
    }

    private inner class FaceVpAdapter(private val views: ArrayList<View>) : androidx.viewpager.widget.PagerAdapter() {

        override fun destroyItem(arg0: ViewGroup, arg1: Int, arg2: Any) {
            (arg0 as androidx.viewpager.widget.ViewPager).removeView(arg2 as View)
        }

        override fun getCount(): Int {
            return views.size
        }

        override fun instantiateItem(arg0: ViewGroup, arg1: Int): Any {
            (arg0 as androidx.viewpager.widget.ViewPager).addView(views[arg1])
            return views[arg1]
        }

        override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
            return arg0 === arg1
        }
    }

    private inner class EmojiAdapter(datas: MutableList<Emoji>) : BaseQuickAdapter<Emoji, BaseViewHolder>(R.layout.item_face) {
        init {
            setNewInstance(datas)
        }

        override fun convert(helper: BaseViewHolder, item: Emoji) {
            helper.setImageResource(R.id.face_image, item.imageUri)
        }
    }

    fun setEmojiClickListener(listener: OnEmojiClickListener) {
        this.listener = listener
    }

    interface OnEmojiClickListener {
        fun onEmojiDelete()

        fun onEmojiClick(emoji: Emoji)
    }

}