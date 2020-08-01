package com.julun.huanque.common.bean.forms
import com.julun.huanque.common.constant.BooleanType
import java.io.Serializable
/**
 * 创建pk表单 {"duration":null,"giftId":null,"joinNum":null,"programIds":""}
 */
class CreatePkForm(var giftId: Int? = null, var joinNum: Int, var duration: Int, var programIds: String) : SessionForm()

/**
 * 接受pk表单
 */
class AcceptPkForm(var giftId: Int? = null, var pkId: Int) : SessionForm()

/**
 * 拒绝pk表单
 */
class RejectPkForm(var pkId: Int) : SessionForm()

/**
 * 查看战绩的pk表单
 */
class PKHistoryForm(var offset: Int = 0, var result: String = "") : SessionForm()

/**
 * 创建连麦form
 */
class RoomMicCreateForm(var joinNum: Int, var programIds: String) : Serializable

/**
 * 连麦id Form
 */
class RoomMicIdForm(var micId: Long) : Serializable

//查询pk信息的form
class PkGuessQueryForm(var programId: Long) : Serializable

//PK竞猜的form
class PkGuessForm(var guessType: String, var mecoins: Long, var programId: Long) : Serializable

//PK信息使用的表单
class PKInfoForm {
    var programId: Long = 0
    private var isPush: String = BooleanType.FALSE

    fun getIsPush() = isPush

    fun setIsPush(push: String) {
        isPush = push
    }
}


