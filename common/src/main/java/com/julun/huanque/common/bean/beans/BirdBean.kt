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
    var cowherd: BirdCowherd = BirdCowherd(),
    var mystical: BirdMystical = BirdMystical(),
    var redpacket: BirdRedPacket = BirdRedPacket(),
    var wealth: BirdWealth = BirdWealth(),//财神
    var weaver: BirdWeaver = BirdWeaver()//织女
)

data class UnlockUpgrade(
    var upgradeCoins: BigInteger = BigInteger.ZERO,
    var upgradeIcon: String = "",
    var upgradeLevel: Int = 0,
    var upgradeName: String = ""
)

data class BirdCowherd(
    var functionIcon: String = "",
    var functionName: String = "",
    var functionNum: String = ""
)

data class BirdMystical(
    var functionIcon: String = "",
    var functionName: String = "",
    var functionNum: String = ""
)

data class BirdRedPacket(
    var functionIcon: String = "",
    var functionName: String = "",
    var functionNum: String = ""
)

data class BirdWealth(
    var functionIcon: String = "",
    var functionName: String = "",
    var functionNum: String = ""
)

data class BirdWeaver(
    var functionIcon: String = "",
    var functionName: String = "",
    var functionNum: String = ""
)

data class BuyBirdResult(
    var coinsPerSec: BigInteger = BigInteger.ZERO,
    var currentUpgrade: UpgradeBirdBean = UpgradeBirdBean(),
    var totalCoins: BigInteger = BigInteger.ZERO,
    var unlockUpgrade: UnlockUpgrade = UnlockUpgrade()
)

data class UpgradeBirdBean(
    var onlineCoinsPerSec: BigInteger = BigInteger.ZERO,
    var programCoinsPerSec: BigInteger = BigInteger.ZERO,
    var startSeconds: Long = 0,
    var upgradeIcon: String = "",
    var upgradeId: Int = 0,
    var upgradeLevel: Int = 0,
    var upgradeName: String = "",
    var upgradePos: Int = 0
)
