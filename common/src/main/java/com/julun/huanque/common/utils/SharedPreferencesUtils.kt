package com.julun.huanque.common.utils

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.julun.huanque.common.helper.StringHelper
import java.lang.reflect.Type
/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/6/18 16:03
 *
 *@Description: 此sp工具处理初始化时关键的内容 普通存储内容不要存在这里
 *
 */
object SharedPreferencesUtils {
    private var preferences: SharedPreferences? = null

    val sharePreferences: SharedPreferences
        get() {
            if (preferences == null) {
                synchronized(SharedPreferences::class.java) {
                    if (preferences == null) {
                       throw Exception("sharePreferences没有初始化，请检查使用时机是否正确")
                    }
                }
            }
            return preferences!!
        }

    //初始化shared
    fun init(context: Application){
        if (preferences == null) {
            synchronized(SharedPreferences::class.java) {
                if (preferences == null) {
                    preferences = PreferenceManager.getDefaultSharedPreferences(context)
                }
            }
        }
    }
    private val editor: SharedPreferences.Editor
        get() = sharePreferences.edit()

    fun getString(key: String, value: String): String {
        return sharePreferences.getString(key, value)?:value
    }

    fun getBoolean(key: String, value: Boolean): Boolean {
        return sharePreferences.getBoolean(key, value)
    }

    fun getInt(key: String, value: Int): Int {
        return sharePreferences.getInt(key, value)
    }

    fun getLong(key: String, value: Long): Long {
        return sharePreferences.getLong(key, value)
    }

    fun getFloat(key: String, value: Float): Float {
        return sharePreferences.getFloat(key, value)
    }

    fun getStringSet(key: String, value: Set<String>): Set<String>? {
        return sharePreferences.getStringSet(key, value)
    }

    fun commitString(key: String, value: String) {
        val editor = editor.putString(key, value)
        commit(editor)
    }

    fun commitInt(key: String, value: Int) {
        val editor = editor.putInt(key, value)
        commit(editor)
    }

    fun commitFloat(key: String, value: Float) {
        val editor = editor.putFloat(key, value)
        commit(editor)
    }

    fun commitLong(key: String, value: Long) {
        val editor = editor.putLong(key, value)
        commit(editor)
    }

    fun commitBoolean(key: String, value: Boolean) {
        val editor = editor.putBoolean(key, value)
        commit(editor)
    }

    fun commitStringSet(key: String, value: Set<String>) {
        val editor = editor.putStringSet(key, value)
        commit(editor)
    }

    fun remove(key: String) {
        editor.remove(key)
    }

    private fun commit(editor: SharedPreferences.Editor) {
            editor.apply()
    }



    fun commitObject(key: String, source:Any) {
        val editor = editor.putString(key, JsonUtil.serializeAsString(source))
        commit(editor)
    }

    fun <T> getObject(key: String,klass:Type):T? {
        val string: String = getString(key, "")
        if(StringHelper.isEmpty(string)) return null
        return if(StringHelper.isEmpty(string)){null}else {
            JsonUtil.deserializeAsObject(string,klass)}
    }



}
