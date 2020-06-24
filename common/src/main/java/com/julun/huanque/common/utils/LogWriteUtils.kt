package com.julun.huanque.common.utils

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.julun.huanque.common.init.CommonInit
import java.io.File
import java.io.FileFilter
import java.util.*


/**
 *
 *@author zhangzhen
 *@data 2018/11/12
 *
 **/
object LogWriteUtils {
    private val logger = ULog.getLogger("LogWriteUtils")
    private val handlerThread: HandlerThread = HandlerThread("logWrite-thread")
    private var handler: Handler
    private val WHAT = 9999
    private val logDir = "lmLog"
    private val LOG_FILES_MAX_NUM = 3 //文件最多有3个
    private val LOG_FILE_MAX_SIZE = 1024 * 20 //文件最大20k太大没法看了

    private var mCurrentLogFile: File? = null

    init {
        handlerThread.start()
        //在这个线程中创建一个handler对象
        handler = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    WHAT -> {
                        logger.info("日志： " + msg.obj + "  线程： " + Thread.currentThread().name)
                        val log = msg.obj as String
                        writeLogToFile(log)
                    }
                }


            }
        }

    }

    private fun getAppLogDir(): String? {
        val dirPath = FileUtils.getExFilePath(CommonInit.getInstance().getContext())
        dirPath?.let {
            val dir = File(dirPath + File.separator + logDir)
            dir.mkdirs()
            return if (dir.exists())
                dir.path
            else
                null
        }
        return null
    }

    //保存日志
    fun addLog(tag: String, log: String?) {
        log?.let {
            handler.sendMessage(handler.obtainMessage().apply {
                this.what = WHAT
                this.obj = formatLog(tag, log)
            })
        }
    }

    private fun formatLog(tag: String, msg: String): String {
        return String.format("%s %s: %s\n", DateHelper.format(Date()), tag, msg)
    }


    fun writeLogToFile(logMessage: String) {
        if (mCurrentLogFile == null || mCurrentLogFile!!.length() >= LOG_FILE_MAX_SIZE) {
            mCurrentLogFile = getNewLogFile()
        }
        if (mCurrentLogFile?.exists() == true)
            FileUtils.writeToFile(logMessage, mCurrentLogFile!!.path)
    }

    private fun getNewLogFile(): File? {
        val dir = File(getAppLogDir())
        val files = dir.listFiles(fileFilter)
        if (files == null || files.isEmpty()) {
            // 创建新文件
            return createNewLogFile()
        }
        val sortedFiles = sortFiles(files)
        if (files.size > LOG_FILES_MAX_NUM) {
            // 删掉最老的文件
            FileUtils.delete(sortedFiles[0])
        }
        // 取最新的文件，看写没写满
        val lastLogFile = sortedFiles[sortedFiles.size - 1]
        return if (lastLogFile.length() < LOG_FILE_MAX_SIZE) {
            lastLogFile
        } else {
            // 创建新文件
            createNewLogFile()
        }
    }

    private fun createNewLogFile(): File? {
        return FileUtils.createFile(getAppLogDir() + "/Log" + DateHelper.dateToStrYmvod(Date()) + ".txt")
    }

    private fun sortFiles(files: Array<File>): List<File> {

        val fileList=arrayListOf<File>()
        files.forEach {
            fileList.add(it)
        }
        Collections.sort(fileList, Comparator { file1: File, file2: File ->
            val result = if (file1.lastModified() < file2.lastModified()) {
                -1
            } else {
                1
            }
            result

        })
        return fileList
    }


    private val fileFilter = FileFilter { file ->
        val tmp = file.name.toLowerCase()
        tmp.startsWith("log") && tmp.endsWith(".txt")
    }

    //关闭日志收集
    fun closeWriteLog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            handlerThread.quitSafely()
        } else {
            handlerThread.quit()
        }
        handler.removeCallbacksAndMessages(null)
    }
}