package com.julun.huanque.realname.net.service

import com.julun.huanque.common.basic.Root
import com.julun.huanque.realname.net.bean.RealNameBean
import com.julun.huanque.realname.net.form.RealNameForm
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author WanZhiYuan
 * @since 1.0.0
 * @date 2020/07/09
 */
interface RealNameService {
    /**
     * 获取实名认证Token 实名&头像
     */
    @POST("user/acct/data/getCertificationToken")
    fun getCertificationToken(@Body form : RealNameForm) : Observable<Root<RealNameBean>>

    /**
     * 保存认证结果 实名&头像
     */
    @POST("user/acct/data/saveCertificationRes")
    fun saveCertificationRes(@Body form : RealNameForm) : Observable<Root<Void>>
}