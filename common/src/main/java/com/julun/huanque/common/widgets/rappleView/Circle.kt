package com.julun.huanque.common.widgets.rappleView

/**
 * 圆的对象
 * Created by 玉光 on 2017-12-1.
 */
class Circle {
    /**
     * 圆心点的x坐标
     */
    var centerX = 0f

    /**
     * 圆心点的Y坐标
     */
    var centerY = 0f

    /**
     * 圆的半径
     */
    var radius = 0f

    /**
     * 透明度
     */
    var alpha = (0.006 * 255).toInt()

    /**
     * 描边透明度
     */
    var strokeAlpha: Int = 255

    constructor() {}
    constructor(centerX: Float, centerY: Float, radius: Float, alpha: Int) {
        this.centerX = centerX
        this.centerY = centerY
        this.radius = radius
        this.alpha = alpha
    }
}