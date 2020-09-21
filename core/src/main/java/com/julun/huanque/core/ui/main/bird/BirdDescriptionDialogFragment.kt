package com.julun.huanque.core.ui.main.bird

import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.FunctionBirdDes
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_bird_description.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/9/10 10:35
 *
 *@Description: 说明页
 *
 */
class BirdDescriptionDialogFragment(private val leYuanViewModel: LeYuanViewModel) : BaseDialogFragment() {


    private val birdAdapter: BaseQuickAdapter<FunctionBirdDes, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<FunctionBirdDes, BaseViewHolder>(R.layout.item_bird_desc) {
            override fun convert(holder: BaseViewHolder, item: FunctionBirdDes) {
                val img = holder.getView<SimpleDraweeView>(R.id.sdv_bird)
                if (item.birdPic != null) {
                    img.loadImage(item.birdPic!!, 90f, 90f)
                }
                holder.setText(R.id.tv_name, item.birdName).setText(R.id.tv_desc, item.bFunction)
                    .setText(R.id.tv_source, item.source)
            }

        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_description
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 480)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun initViews() {
        initViewModel()
        birdsList.layoutManager = LinearLayoutManager(requireContext())
        birdsList.adapter = birdAdapter
        ivClose.onClickNew {
            dismiss()
        }
    }

    override fun reCoverView() {
        initViewModel()
    }

    private fun initViewModel() {
        leYuanViewModel.functionBirds.observe(this, Observer {
            birdAdapter.setNewInstance(it ?: return@Observer)
        })
        leYuanViewModel.gotFunctionBirdInfos()
    }

}