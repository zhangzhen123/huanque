package com.julun.huanque.common.helper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import androidx.core.content.ContextCompat
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.constant.ParamConstant
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object UUidHelper {

    /*以下是用户归因使用*/
    //从文件获取UUID
    private fun getUUIDFromFile(file: File): String {
        if (!file.exists()) {
            return ""
        }
        val fr = FileReader(file)
        var str = ""
        try {
            str = fr.readText()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                fr?.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return str
        }
    }

    /**
     * 获取UUID 按照 SD文件-》externalFilesDir(SP)-》externalCacheDir
     */
    fun getUUID(): String {
        var sdUUID = ""
        if (FileUtils.sdCardExists && ContextCompat.checkSelfPermission(CommonInit.getInstance().getApp(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //SD卡存在，从SD卡读取文件
            val sdFile = File("${Environment.getExternalStorageDirectory().path}${FileUtils.ROOT_NAME}", FileUtils.SOURCE)
            sdUUID = getUUIDFromFile(sdFile)
        }

        //从SP读取
        val spUUID = SharedPreferencesUtils.getString(ParamConstant.UUID, "")

        //从SD卡和SP都没有读取到数据，从externalCacheDir读取
        val externalCacheFile = File(CommonInit.getInstance().getApp().externalCacheDir, FileUtils.SOURCE)
        val externalUUID = getUUIDFromFile(externalCacheFile)

        if (sdUUID == spUUID && sdUUID == externalUUID) {
            //三个地方获取的UUID相同，要么都保存了UUID，要么都没有保存，直接返回，不需要其它操作
            return sdUUID
        }
        //进入到这一步表示3个地方保存的数据存在差异，需要还原其它位置数据
        val realUUID = when {
            sdUUID.isNotEmpty() -> sdUUID
            spUUID.isNotEmpty() -> spUUID
            else -> externalUUID
        }
        if (realUUID != sdUUID) {
            //SD文件修复
            if (FileUtils.sdCardExists && ContextCompat.checkSelfPermission(CommonInit.getInstance().getApp(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //SD卡存在，从SD卡读取文件
                val file = File("${Environment.getExternalStorageDirectory().path}${FileUtils.ROOT_NAME}", FileUtils.SOURCE)
                saveUUID(realUUID, file)
            }
        }
        if (realUUID != spUUID) {
            //SP文件修复
            SharedPreferencesUtils.commitString(ParamConstant.UUID, realUUID)
        }
        if (realUUID != externalUUID) {
            //externalCacheDir文件修复
            val externalCacheFile = File(CommonInit.getInstance().getApp().externalCacheDir, FileUtils.SOURCE)
            saveUUID(realUUID, externalCacheFile)
        }

        return realUUID
    }

    /**
     * 之前没有UUD的用户，三个地方存储UUID
     */
    fun saveGlobalUUID(uuid: String) {
        //1 保存SD卡文件
        if (FileUtils.sdCardExists && ContextCompat.checkSelfPermission(CommonInit.getInstance().getApp(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //SD卡存在，从SD卡读取文件
            val file = File("${Environment.getExternalStorageDirectory().path}${FileUtils.ROOT_NAME}", FileUtils.SOURCE)
            saveUUID(uuid, file)
        }
        //2 保存SP文件
        SharedPreferencesUtils.commitString(ParamConstant.UUID, uuid)
        //3 保存到缓存
        val externalCacheFile = File(CommonInit.getInstance().getApp().externalCacheDir, FileUtils.SOURCE)
        saveUUID(uuid, externalCacheFile)

    }

    /**
     * 保存UUID
     */
    private fun saveUUID(uuid: String, file: File) {

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        if (!file.exists()) {
            return
        }
        val fw = FileWriter(file)

        try {
            fw.flush()
            fw.write(uuid)
            fw.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fw.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}