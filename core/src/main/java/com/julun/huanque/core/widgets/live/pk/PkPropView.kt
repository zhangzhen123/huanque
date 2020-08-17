package com.julun.huanque.lmcore.basic.widgets.live

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

import com.julun.huanque.common.bean.beans.PkPropInfo
import com.julun.huanque.common.constant.PKPropStatus
import com.julun.huanque.common.constant.PKPropType
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ULog
import com.julun.huanque.core.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.view_pk_prop.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import java.util.concurrent.TimeUnit


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/8/21 16:29
 *
 *@Description : pk显示道具 关联[PkMicView]
 *
 */
class PkPropView : LinearLayout {

    private var propCdDispose: Disposable? = null
    private val logger = ULog.getLogger("PkPropView")

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_pk_prop, this)
        this.orientation = VERTICAL
        onClickNew {
            if (canUse) {
                logger.info("可以使用---")
                mListener?.callback(currentProgramId)
                canUse=false
            }
        }
    }

    var mListener: Listener? = null
    var canUse: Boolean = false

    /**
     * 道具所属的直播间
     */
    private var currentProgramId=0L

    /**
     * [propInfo]道具信息  [localProgramId]当前用户所在的直播间
     */
    fun setPropData(propInfo: PkPropInfo,localProgramId:Long) {
        currentProgramId=propInfo.programId
        sdv_pk_prop_img.loadImage(propInfo.propPic, 40f, 40f)
        //xxx持有
        tv_pk_prop_name.textColor = Color.WHITE
        var name = "${propInfo.nickname}"
        if (name.length > 3) {
            name = "${name.substring(0, 3)}…"
        }
        if(propCdDispose?.isDisposed== false){
            propCdDispose?.dispose()
        }
        canUse = false
        tv_pk_prop_name.backgroundResource=R.drawable.shape_pk_prop_name
        when (propInfo.status) {
            PKPropStatus.UnUse -> {
                val myId = SessionUtils.getUserId()
                if (propInfo.userId == myId&&localProgramId==propInfo.programId) {
                    //可使用
                    tv_pk_prop_name.textColor = ContextCompat.getColor(context, R.color.black_333)
                    tv_pk_prop_name.backgroundResource=R.drawable.shape_pk_prop_name2
                    tv_pk_prop_name.text = "可使用"
                    canUse = true
                } else {
                    tv_pk_prop_name.textColor = ContextCompat.getColor(context, R.color.white)
                    val sp = SpannableStringBuilder("${name}持有")
                    val colorY = ContextCompat.getColor(context, R.color.colorAccent_lib)
                    val foregroundColorSpan = ForegroundColorSpan(colorY)
                    sp.setSpan(foregroundColorSpan, 0, name.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv_pk_prop_name.text = sp
                }
            }
            PKPropStatus.Effect -> {
                //生效中
                val time = propInfo.ttl
                tv_pk_prop_name.textColor = ContextCompat.getColor(context, R.color.colorAccent_lib)
                propCdDispose = Observable.intervalRange(0, time, 0, 1L, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    val t = time - it
                    if (propInfo.propType == PKPropType.Blood) {
                        tv_pk_prop_name.text = "双倍${t}S"
                    }else if(propInfo.propType==PKPropType.SmallBlood){
                        tv_pk_prop_name.text = "1.2倍${t}S"
                    } else if (propInfo.propType == PKPropType.Smoke) {
                        tv_pk_prop_name.text = "遮挡${t}S"
                    }

                }

            }
            PKPropStatus.Used -> {
                //已使用
                tv_pk_prop_name.textColor = ContextCompat.getColor(context, R.color.black_999)
                tv_pk_prop_name.text = "已使用"
                delayToHide()
            }
        }


    }
    private var hideDispose: Disposable?=null
    private fun delayToHide() {
        if (hideDispose?.isDisposed == false) {
            hideDispose?.dispose()
        }
        hideDispose = Observable.timer(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            this.hide()
        }
    }
    override fun onDetachedFromWindow() {
        if (propCdDispose?.isDisposed == false) {
            propCdDispose?.dispose()
        }
        if(hideDispose?.isDisposed==false){
            hideDispose?.dispose()
        }
        mListener=null
        super.onDetachedFromWindow()
    }

    interface Listener {
        fun callback(programId: Long)
    }
}
