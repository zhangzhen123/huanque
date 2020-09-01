package com.julun.huanque.common.bean.beans

import java.math.BigInteger

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/8/18 16:21
 *
 *@Description: BirdBean
 *
 */

data class BirdHomeInfo(
    var cash: String = "",
    var coinsPerSec: BigInteger = BigInteger.ZERO,
    var functionInfo: BirdFunctionInfo = BirdFunctionInfo(),
    var totalCoins: BigInteger = BigInteger.ZERO,
    var unlockUpgrade: UnlockUpgrade = UnlockUpgrade(),
    var upgradeList: MutableList<UpgradeBirdBean> = mutableListOf()
)

data class BirdFunctionInfo(
    var cowherd: FunctionBird = FunctionBird(),
    var mystical: FunctionBird = FunctionBird(),
    var redpacket: FunctionBird = FunctionBird(),
    var wealth: FunctionBird = FunctionBird(),//财神
    var weaver: FunctionBird = FunctionBird()//织女
)

data class UnlockUpgrade(
    var upgradeCoins: BigInteger = BigInteger.ZERO,
    var upgradeIcon: String = "",
    var upgradeLevel: Int = 0,
    var upgradeName: String = ""
)

/**
 * 功能鹊
 */
data class FunctionBird(
    var functionIcon: String = "",
    var functionName: String = "",
    var functionNum: String = ""
)

data class BuyBirdResult(
    var coinsPerSec: BigInteger = BigInteger.ZERO,
    var currentUpgrade: UpgradeBirdBean = UpgradeBirdBean(),
    var totalCoins: BigInteger = BigInteger.ZERO,
    var unlockUpgrade: UnlockUpgrade? = null
)

data class UpgradeBirdBean(
    var onlineCoinsPerSec: BigInteger = BigInteger.ZERO,
    var programCoinsPerSec: BigInteger = BigInteger.ZERO,
    var startSeconds: Long = 0,
    var upgradeIcon: String = "",
    var upgradeId: Long? = null,
    var upgradeLevel: Int = 0,
    var upgradeName: String = "",
    var upgradePos: Int = 0,
    var upgradeSaleCoins: BigInteger = BigInteger.ZERO,
    //本地字段 当正在操作时 会有一个蒙层效果
    var isActive: Boolean = false,
    //创建时间 用于后面每次计算收益
    var createTime: Long = System.currentTimeMillis() / 100
)

data class CombineResult(
    var resultType: String = "",
    var unlockUpgrade: UnlockUpgrade? = null,//合并操作后 棋盘升级
    var currentUpgrade: UpgradeBirdBean? = null,//合并成功的升级鹊
    var functionInfo: FunctionBird? = null//合成功能鹊后 刷新整个棋盘
) {
    companion object {
        //MovePos：单向移动
        //SwapPos：互换位置
        //Upgrade：合成升级(客户端需移除合并的两只鹊，并插入合成后的升级鹊)
        //Function：产生功能鹊(客户端需移除合并的两只鹊)
        const val MovePos = "MovePos"
        const val SwapPos = "SwapPos"
        const val Upgrade = "Upgrade"
        const val Function = "Function"
    }
}

data class RecycleResult(
    var coinsPerSec: BigInteger = BigInteger.ZERO,
    var totalCoins: BigInteger = BigInteger.ZERO
)

data class UpgradeShopBirdBean(
    var unlocked: Boolean = false,
    var upgradeCoins: BigInteger = BigInteger.ZERO,
    var upgradeIcon: String = "",
    var upgradeLevel: Int = 0,
    var upgradeName: String = ""
)

data class BirdShopInfo(var upgradeList: MutableList<UpgradeShopBirdBean> = mutableListOf())

data class BirdTaskInfo(
    var activeInfo: BirdActiveInfo = BirdActiveInfo(),
    var taskList: MutableList<BirdTask> = mutableListOf<BirdTask>()
)

data class BirdActiveInfo(
    var activeValue: Int = 0,
    var awardList: List<BirdAward> = listOf(),
    var maxActiveValue: Int = 0
)

data class BirdTask(
    var awardActive: Int = 0,
    var currentNum: Int = 0,
    var jumpType: String = "",
    var targetNum: Int = 0,
    var taskCode: String = "",
    var taskDesc: String = "",
    var taskName: String = "",
    var taskStatus: String = "",
    var taskStatusText: String = "",
    var taskImage: String = ""
)

data class BirdAward(
    var activeCode: String = "",
    var awardName: String = "",
    var awardStatus: String = "",
    var targetActive: Int = 0
)

data class BirdTaskReceiveResult(
    var activeValue: Int = 0,
    var awardCoins: Int = 0,
    var taskStatus: String = "",
    var taskStatusText: String = ""
)