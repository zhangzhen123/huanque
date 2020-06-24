package com.julun.huanque.common.net.converter

import com.julun.huanque.common.utils.JsonUtil
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter
import java.io.IOException

/**
 * Created by nirack on 16-10-22.
 */
class FastJsonRequestBodyConverter<T> : Converter<T, RequestBody> {
    private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")

    @Throws(IOException::class)
    override fun convert(value: T): RequestBody {
        val content:String = JsonUtil.seriazileAsString(value)
        return RequestBody.create(MEDIA_TYPE, content)
    }

}
