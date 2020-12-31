package com.julun.huanque.core.ui.tag_manager

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.ManagerTagBean
import com.julun.huanque.common.bean.beans.TagUserPic
import com.julun.huanque.common.bean.beans.TagUserPicListBean
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.ManagerTagCode
import com.julun.huanque.common.layoutmanager.stacklayout.StackAlign
import com.julun.huanque.common.layoutmanager.stacklayout.StackLayoutConfig
import com.julun.huanque.common.layoutmanager.stacklayout.StackLayoutManager
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.viewmodel.TagManagerViewModel
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_tag_user_pics.*
import org.jetbrains.anko.startActivity

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/12/29 16:36
 *
 *@Description: TagUserPicsActivity 指定标签指定用户的图片集合查看
 *
 */
class TagUserPicsActivity : BaseVMActivity<TagUserPicsViewModel>() {

    companion object {
        fun start(act: Activity, tagId: Int, likeUserId: Long) {
            act.startActivity<TagUserPicsActivity>(ManagerTagCode.TAG_INFO to tagId, IntentParamKey.USER_ID.name to likeUserId)
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_tag_user_pics

    private val tagManagerViewModel: TagManagerViewModel = HuanViewModelManager.tagManagerViewModel
    private var currentTagId: Int? = null
    private var currentLikeUserId: Long? = null

    private val picList = mutableListOf<TagUserPic>()

    private val picListAdapter: BaseQuickAdapter<TagUserPic, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<TagUserPic, BaseViewHolder>(R.layout.item_tag_user_pic, picList), LoadMoreModule {
            override fun convert(holder: BaseViewHolder, item: TagUserPic) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                sdv.loadImageNoResize(item.applyPic)
                val sdv_tag = holder.getView<SimpleDraweeView>(R.id.sdv_tag)
                sdv_tag.loadImage(cTagUserPicListBean?.tagIcon ?: "", 16f, 16f)
                holder.setText(R.id.tv_tag, cTagUserPicListBean?.tagName)
                holder.setText(R.id.tv_index, "${holder.adapterPosition + 1}/${picListAdapter.data.size}")
            }

        }
    }


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {


        overridePendingTransition(0, 0)
        currentTagId = intent.extras?.getInt(ManagerTagCode.TAG_INFO)
        currentLikeUserId = intent.extras?.getLong(IntentParamKey.USER_ID.name)
        val config = StackLayoutConfig()
        config.secondaryScale = 1f
        config.scaleRatio = 0.4f
        config.maxStackCount = 2
        config.initialStackCount = 2
        config.space = dp2px(10)
        config.align = StackAlign.RIGHT
        rv_pics.layoutManager = StackLayoutManager(config)


        rv_pics.adapter = picListAdapter

        initViewModel()

        ct_root_layout.onClickNew {
            finish()
        }

        tv_btn_like.onClickNew {
            //todo
            cTagUserPicListBean ?: return@onClickNew
            tagManagerViewModel.tagLike(
                ManagerTagBean(
                    tagId = cTagUserPicListBean!!.tagId,
                    tagIcon = cTagUserPicListBean!!.tagIcon
                )
            )
        }
        zan_layout.onClickNew {
            val info = cTagUserPicListBean ?: return@onClickNew
            val tagId = currentTagId ?: return@onClickNew
            currentLikeUserId ?: return@onClickNew
            if (info.praise) {
                mViewModel.tagCancelPraise(tagId, currentLikeUserId!!)
            } else {
                mViewModel.tagPraise(tagId, currentLikeUserId!!)
            }

        }
        iv_guide_close.onClickNew {
            add_tag_guide_layout.hide()
            zan_layout.show()
        }
    }

    private var cTagUserPicListBean: TagUserPicListBean? = null
    private fun initViewModel() {

        mViewModel.tagUserPics.observe(this, Observer {
            if (it.isSuccess()) {
                cTagUserPicListBean = it.requireT()
                renderData(it.requireT())
            }
        })
        mViewModel.tagPraise.observe(this, Observer {
            if (it.isSuccess()) {

                iv_zan.isActivated = it.requireT()
                val info = cTagUserPicListBean ?: return@Observer
                info.praise = it.requireT()
                if (it.requireT()) {
                    info.praiseNum++
                    zan_num.text = "${info.praiseNum}"
                } else {
                    info.praiseNum--
                    zan_num.text = "${info.praiseNum}"
                }
            }
        })
        tagManagerViewModel.tagChangeStatus.observe(this, Observer {
            if (it.isSuccess()) {


                if (it.isNew() && it.requireT().like) {
                    ToastUtils.showToastCustom(R.layout.layout_toast_tag, action = { view, t ->
                        val tv = view.findViewById<TextView>(R.id.toastContent)
                        t.duration = Toast.LENGTH_SHORT
                        t.setGravity(Gravity.CENTER, 0, 0)
                        tv.text = "已喜欢"
                    })
                }
                val tag = it.requireT()
                if(tag.tagId!=currentTagId){
                    return@Observer
                }
                if (tag.like) {
                    add_tag_guide_layout.hide()
                    zan_layout.show()
                } else {
                    add_tag_guide_layout.show()
                    tv_tag_name.text = tag.tagName
                    zan_layout.hide()
                }

            } else if (it.state == NetStateType.ERROR) {
//                if (it.isNew()) {
//                    ToastUtils.show("网络异常")
//                }
            }
        })


        currentTagId ?: return
        currentLikeUserId ?: return
        mViewModel.requestTagUserList(QueryType.INIT, tagId = currentTagId!!, friendId = currentLikeUserId!!)
    }

    private fun renderData(info: TagUserPicListBean) {
        picList.addAll(info.authPicList)
        picListAdapter.notifyDataSetChanged()
        rv_pics.post {
            rv_pics.scrollToPosition(picListAdapter.data.size)
        }

        zan_num.text = "${info.praiseNum}"

        iv_zan.isActivated = info.praise

        if (info.like) {
            add_tag_guide_layout.hide()
            zan_layout.show()
        } else {
            add_tag_guide_layout.show()
            tv_tag_name.text = info.tagName
            zan_layout.hide()
        }

    }

    override fun showLoadState(state: NetState) {

    }
}