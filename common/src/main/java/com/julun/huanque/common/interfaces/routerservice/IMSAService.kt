package com.julun.huanque.common.interfaces.routerservice

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * 系统相关服务接口
 * @author WanZhiYuan
 * @since 4.31
 * @date 2020/04/27 0027
 */
interface IMSAService : IProvider {
    fun getOaid(): String
}
