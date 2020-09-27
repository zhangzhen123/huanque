package com.julun.huanque.app.update

import android.app.DownloadManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import androidx.core.content.FileProvider
import com.julun.huanque.common.bean.beans.CheckVersionResult
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.utils.MD5Util
import com.julun.huanque.common.utils.SPUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.ULog
import org.jetbrains.anko.downloadManager
import java.io.File


/**
 * 下载、、更新apk的service
 */
class UpgradeApkService : Service() {

    interface DownloadListener {
        fun onDownloadResult(isSuccess: Boolean): Unit
    }

    inner class DownloadCompleteReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            //这里可以取得下载的id，这样就可以知道哪个文件下载完成了。适用与多个下载任务的监听,现在一次只有一个文件下载，不做判断了
            lastDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            ULog.v("下载监控", "" + lastDownloadId)
            if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                processDownloadResult()
            }
        }

        //处理下载之后的结果,此时接收到的 ACTION_DOWNLOAD_COMPLETE 仅仅表示本次下载完成, 可能是真正的成功,也可能是失败,甚至是取消
        private fun processDownloadResult() {
            val query = DownloadManager.Query()
            query.setFilterById(lastDownloadId)
            val cursor = downloadManager.query(query)
            if (cursor != null && cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                val titleIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                val fileSizeIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val bytesDLIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val title = cursor.getString(titleIdx)
                val fileSize = cursor.getInt(fileSizeIdx)
                val bytesDownloaded = cursor.getInt(bytesDLIdx)

                // Translate the pause reason to friendly text.
                val reason = cursor.getInt(reasonIdx)
                val sb = StringBuilder()
                sb.append(title).append("\n")
                sb.append("Downloaded ").append(bytesDownloaded).append(" / ").append(fileSize)

                // Display the status
                ULog.d("下载监控", sb.toString())
                when (status) {
                    DownloadManager.STATUS_PAUSED -> {
                        ULog.v("下载监控", "STATUS_PAUSED")
                        //正在下载，不做任何事情
                        ULog.v("下载监控", "STATUS_RUNNING , bytesDownloaded -> " + bytesDownloaded)
                    }
                    DownloadManager.STATUS_PENDING -> {
                        ULog.v("下载监控", "STATUS_PENDING")
                        ULog.v("下载监控", "STATUS_RUNNING , bytesDownloaded -> " + bytesDownloaded)
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        ULog.v("下载监控", "STATUS_RUNNING , bytesDownloaded -> " + bytesDownloaded)
//                        if (listener != null) {}
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        //完成
                        ULog.v("下载监控", "下载完成")
//                        application.unregisterReceiver(receiver)
                        val realFilePath: String = getRealPathAccording2Id(lastDownloadId)
                        val file = File(realFilePath)
                        /*
                        //这里竟然还会有找不到文件的问题.....
                        if(file == null){
                        }
                        if(!file.exists()){//文件不存在
                        }
                        */
                        var fileMD5String = ""
                        if (file.exists()) {
                            fileMD5String = MD5Util.getFileMD5String(file)
                        }
                        //比较md5不区分大小写
                        if (fileMD5String.equals(SPUtils.getString(NEW_VERSION_MD5, ""), true)) {
                            if (installAfterDownload) {
                                installApk(file)
                            }
                            downLoadListener?.onDownloadResult(true)
                        } else {//文件下载的不正确
                            downloadManager.remove(lastDownloadId)
                            //下载的文件有错误
                            try {
                                file.delete()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                reportCrash("下载完成之后文件有错误,但是删除失败", e)
                            }
                            downLoadListener?.onDownloadResult(false)
//                            stopSelf()
                        }

                        //下载完成.... 可以安装
//                        if (listener != null) {  listener.onFihish()  }
                    }
                    DownloadManager.STATUS_FAILED -> {
                        //清除已下载的内容，重新下载
                        ULog.v("下载监控", "STATUS_FAILED")
                        downloadManager.remove(lastDownloadId)
                        //如果下载的地址不正确,可能是 404,还有很多其他的错误类型......
                        ULog.e("下载失败 -->> $reason ")
                        downLoadListener?.onDownloadResult(false)
//                        stopSelf()
                    }
                }
            }
        }

    }

    fun getRealPathAccording2Id(downloadId: Long): String {
        val myDownloadQuery = DownloadManager.Query()
        myDownloadQuery.setFilterById(downloadId)

        var path = ""
        val myDownload = downloadManager.query(myDownloadQuery)
        if (myDownload != null && myDownload.moveToFirst()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //android 7 以上的安全问题
                path = myDownload.getString(myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).replace("file://", "")
            } else {
                val fileNameIdx: Int = myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)
                path = myDownload.getString(fileNameIdx)
            }

//            val fileUriIdx = myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
//            val fileUri = myDownload.getString(fileUriIdx)
        }
        myDownload?.close()

        return path
    }

    // 根据DownloadManager下载的Id，查询DownloadManager某个Id的下载任务状态。
    private fun queryStatus(downLoadId: Long): String {
        val query = DownloadManager.Query()
        query.setFilterById(downLoadId)
        val cursor = downloadManager.query(query)

        var statusMsg = ""
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            when (status) {
                DownloadManager.STATUS_PAUSED -> {
                    statusMsg = "STATUS_PAUSED"
                    statusMsg = "STATUS_PENDING"
                    statusMsg = "STATUS_RUNNING"
                }
                DownloadManager.STATUS_PENDING -> {
                    statusMsg = "STATUS_PENDING"
                    statusMsg = "STATUS_RUNNING"
                }
                DownloadManager.STATUS_RUNNING -> statusMsg = "STATUS_RUNNING"
                DownloadManager.STATUS_SUCCESSFUL -> statusMsg = "STATUS_SUCCESSFUL"
                DownloadManager.STATUS_FAILED -> statusMsg = "STATUS_FAILED"

                else -> statusMsg = "未知状态"
            }

            ToastUtils.showErrorMessage(statusMsg)

        } else {//没有这个下载任务
//            ToastUtils(huanqueApp.getApp()).showErrorMessage("没有 id为 < $downLoadId > 的下载任务信息 ")
        }

        return statusMsg
    }

    /**
     * 安卓系统下载类
     */
    internal val manager: DownloadManager by lazy { getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    /**
     * 接收下载完的广播
     */
    internal val receiver: DownloadCompleteReceiver by lazy { DownloadCompleteReceiver() }

    /**
     * 开始下载
     */
    private fun startDownload(data: CheckVersionResult) {

        //设置下载地址
        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(data.installUrl))



        // 下载时，通知栏显示途中
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        // 显示下载界面
        request.setVisibleInDownloadsUi(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        // 设置下载后文件存放的位置
        val targetFileName = "huanque-${data.newVersion.replace(".", "_")}.apk"
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, targetFileName)
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,targetFileName)
        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("欢鹊应用更新-V-${data.newVersion}")
        request.setDescription("欢鹊应用更新下载")
        ULog.i("下载监控..... 开始下载 ...")
        // 将下载请求放入队列
        val downLoadId: Long = manager.enqueue(request)
        //将最后的下载id写入本地
        SPUtils.commitLong(LAST_DOWNLOAD_ID, downLoadId)
    }

    fun installApk(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //获取下载之前存入本地的正在下再等版本号信息
        val version = SPUtils.getString(NEW_VERSION_NAME, "")
        if (StringHelper.isEmpty(version)) {
            //   留给安装程序处理这个错误吧...
//            throw RuntimeException("超级严重的错误 , 找不到需要安装的文件... ,下载之前没有 先写入本地")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val uriForFile: Uri = FileProvider.getUriForFile(this, "${CommonInit.getInstance().getApp().packageName}.fileprovider", file)
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.setDataAndType(uriForFile, "application/vnd.android.package-archive")
        } else {
            val fromFile: Uri = Uri.fromFile(file)
            intent.setDataAndType(fromFile, "application/vnd.android.package-archive")
        }

//        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP;  //注意本行的FLAG设置
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION or FLAG_ACTIVITY_NEW_TASK)

        ToastUtils.showSuccessMessage("即将退出程序并将安装新版本")
        startActivity(intent)
        /*
        huanqueApp.ACTIVITIES_STACK.values.forEach {
            it?.finish()
        }
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(0)
        */
    }

    override fun onCreate() {
        super.onCreate()
    }

    private var dataPassedBy: CheckVersionResult? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //intent可能为空
        val info = intent?.getSerializableExtra(PARAM_CHECK_VERSION_INFO)
        var data: CheckVersionResult? = null
        if (info != null) {
            data = info as CheckVersionResult
            dataPassedBy = data
            //        val targetFileName = "huanque-${data.newVersion}.apk"
//        val folder: File? = huanqueApp.getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
//        targetDownloadFile = File(folder,targetFileName)

            //将正确的 md5码 , 版本号 写入本地,避免应用被杀掉,重启服务之后无法判断文件是否正确的下载了
            SPUtils.commitString(NEW_VERSION_NAME, data.newVersion)
            SPUtils.commitString(NEW_VERSION_MD5, data.md5Code!!)
/*

        val lastDownloadId: Long = SPUtils.getLong(LAST_DOWNLOAD_ID, -1)
        val status = queryStatus(lastDownloadId)

        if(StringHelper.isEmpty(status)){//这时候应该是任务被删除了
        }
*/
        }


        //此处增加是否下载器可用的判断
        val downloadManagerEnable = DownloadManagerResolver.resolve(this.baseContext)
        if (downloadManagerEnable&&data!=null) {
            //是否正在下载
            if (data.startDownload) {
                startDownload(data)
            }

            //注册下载广播
            val filter: IntentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
//        filter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS)

//        unregisterReceiver(receiver)
            registerReceiver(receiver, filter)
        } else {
            //此处说明下载器不能用 但是也要返回正确 目的就是为了通知上游 如果返回错误会启动错误重新下载机制
            downLoadListener?.onDownloadResult(true)
        }
//        downloadApp(downloadUrl)
        return START_REDELIVER_INTENT
    }

    private var downloadTimes = 0//尝试下载的次数

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        const val PARAM_CHECK_VERSION_INFO = "PARAM_CHECK_VERSION_INFO"
        private var installAfterDownload: Boolean = false
        /** 最后一次下载的id **/
        const val LAST_DOWNLOAD_ID = "LAST_DOWNLOAD_ID"
        const val NEW_VERSION_MD5 = "NEW_VERSION_MD5"
        const val NEW_VERSION_NAME = "NEW_VERSION_NAME"
        const val IGNORED_VERSION = "IGNORED_VERSION"//被忽略的版本
        var lastDownloadId: Long = -1

        fun setInstallWhenDownloadComplete(flag: Boolean = true): Unit {
            installAfterDownload = flag
        }


        private var downLoadListener: DownloadListener? = null
        fun registerErrorListener(liseter: DownloadListener): Unit {
            downLoadListener = liseter
        }

        fun unRegisterErrorListener(): Unit {
            downLoadListener = null
        }
    }
}
