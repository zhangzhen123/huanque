package com.julun.huanque.core.ui.main.heartbeat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.common.widgets.cardlib.CardLayoutManager
import com.julun.huanque.common.widgets.cardlib.CardSetting
import com.julun.huanque.common.widgets.cardlib.CardTouchHelperCallback
import com.julun.huanque.common.widgets.cardlib.OnSwipeCardListener
import com.julun.huanque.common.widgets.cardlib.utils.ReItemTouchHelper
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_nearby.*

class NearbyFragment : BaseLazyFragment() {
    companion object {
        fun newInstance() = NearbyFragment()
    }

    val mViewModel: NearbyViewModel by viewModels()


    private lateinit var mReItemTouchHelper: ReItemTouchHelper
    private val list = mutableListOf<String>()
    override fun lazyLoadData() {
        mViewModel.requestProgramList(QueryType.INIT, null)
    }

    override fun getLayoutId(): Int = R.layout.fragment_nearby

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        val setting: CardSetting = object : CardSetting() {
            override fun couldSwipeOutDirection(): Int {
                return ReItemTouchHelper.LEFT or ReItemTouchHelper.RIGHT
            }

            override fun getCardRotateDegree(): Float {
                return 20f
            }

            override fun enableHardWare(): Boolean {
                return false
            }

            override fun getSwipeOutAnimDuration(): Int {
                return 200
            }

            override fun getStackDirection(): Int {
                return ReItemTouchHelper.DOWN
            }
        }
        setting.setSwipeListener(object : OnSwipeCardListener<String> {
            override fun onSwiping(
                viewHolder: RecyclerView.ViewHolder?,
                dx: Float,
                dy: Float,
                direction: Int,
                ratio: Float
            ) {
                val holder: BaseViewHolder = viewHolder as BaseViewHolder
                when (direction) {
                    ReItemTouchHelper.DOWN -> {
                        Log.e("aaa", "swiping direction=down")
                        holder.getView<ImageView>(R.id.iv_like).alpha = 0f
                        holder.getView<ImageView>(R.id.iv_dislike).alpha = 0f
                    }
                    ReItemTouchHelper.UP -> {
                        holder.getView<ImageView>(R.id.iv_like).alpha = 0f
                        holder.getView<ImageView>(R.id.iv_dislike).alpha = 0f
                        Log.e("aaa", "swiping direction=up")
                    }
                    ReItemTouchHelper.LEFT -> {
                        Log.e("aaa", "swiping direction=left")
                        holder.getView<ImageView>(R.id.iv_dislike).alpha = ratio
                    }
                    ReItemTouchHelper.RIGHT -> {
                        Log.e("aaa", "swiping direction=right")
                        holder.getView<ImageView>(R.id.iv_like).alpha = ratio
                    }
                }
            }

            override fun onSwipedOut(viewHolder: RecyclerView.ViewHolder?, o: String, direction: Int) {
                val holder: BaseViewHolder = viewHolder as BaseViewHolder
                holder.getView<ImageView>(R.id.iv_like).alpha = 0f
                holder.getView<ImageView>(R.id.iv_dislike).alpha = 0f
                when (direction) {
                    ReItemTouchHelper.DOWN -> {

                    }
                    ReItemTouchHelper.UP -> {

                    }
                    ReItemTouchHelper.LEFT -> {

                    }
                    ReItemTouchHelper.RIGHT -> {

                    }
                }
            }

            override fun onSwipedClear() {

            }

        })
        val helperCallback: CardTouchHelperCallback<*> = CardTouchHelperCallback<String>(
            mRecyclerView,
            list, setting
        )
        mReItemTouchHelper = ReItemTouchHelper(helperCallback)
        val layoutManager = CardLayoutManager(mReItemTouchHelper, setting)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.adapter = cardsAdapter
    }

    private fun initViewModel() {
        mViewModel.dataList.observe(this, Observer {
            if (it.isSuccess()) {
                val dataList = it.requireT().programList
                dataList.forEach {
                    list.add(it.coverPic)
                }
                cardsAdapter.notifyDataSetChanged()
            }
        })
    }

    private val cardsAdapter: BaseQuickAdapter<String, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_user_swip_card, list) {

            override fun convert(holder: BaseViewHolder, item: String) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                sdv.loadImageNoResize(item)

            }
        }
    }

}