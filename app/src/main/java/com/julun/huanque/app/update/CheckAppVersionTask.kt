package com.julun.huanque.app.update

import android.content.Intent
import android.os.Environment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.beans.CheckVersionResult
import com.julun.huanque.common.bean.forms.FindNewsForm
import com.julun.huanque.common.constant.UpdateType
import com.julun.huanque.common.helper.StorageHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.net.Requests
import com.julun.huanque.common.net.services.AppService
import com.julun.huanque.common.suger.handleWithResponse
import com.julun.huanque.common.suger.installApk
import com.julun.huanque.common.utils.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author nirack
 * Created by nirack on 16-12-19.
 */

private enum class DownloadStatus {
    DONE, NONE, DOWNLOADING
}

object AppChecker {

    private val appChecker: CheckAppVersionTask by lazy {
        CheckAppVersionTask({
            processStatus = 1
        }, {
            ULog.i("当前的更新返回：$it")
            processStatus = if (it) 1 else 2
        }, {
            ULog.i("当前的更新返回错误")
            if (!mIsAuto)
                ToastUtils.showErrorMessage("更新遇到麻烦了，请稍后重试...")
            processStatus = 2
        })
    }
    var mIsAuto: Boolean = false//记录是不是自动检测更新的标识
    private var processStatus = 0 //0代表开始，1代表更新中,2更新完成（无论成功失败）
    fun startCheck(isAuto: Boolean) {
        mIsAuto = isAuto
        if (processStatus == 1) {
            ToastUtils.showSuccessMessage("当前正在更新中...")
        } else {
            appChecker.doCheck()
        }
    }
}

/**
 * 首先请求后台,检查是否需要更新
 * 在需要更新的时候,根据更新地址 下载文件,然后用后台返回的 md5 码进行校验
 *
 * case 1: 不存在文件,未开始下载,则立刻开始下载
 * case 2: 已经存在文件,并且校验完整,直接开始安装
 * case 3: 已经存在文件,校验不正确 这里又有两种情况
 *          3.1 : 新文件正在下载,未完成,所以校验码对应不上
 *              3.1.1 : 同一次回话里,已经开启了任务,此时可以直接获取当前下载的download Id,
 *              3.1.2 : 之前点击的下载,系统会继续下载任务,但是app已经被强制杀掉,无法直接获取下载的id
 *          3.2 : 原先的老文件
 *
 */
class CheckAppVersionTask(
    private var preprocess: () -> Unit = {},
    private var callback: (waitForDownloading: Boolean) -> Unit = {},
    private var callbackError: () -> Unit = {}
) {
    /**
     * 接口是否正在请求，过滤二次调用
     */
    private var request = false

    /**
     *检查是否更新
     */
    fun doCheck() {
        if (request) {
            //正在请求，直接返回
            return
        }
        val form = FindNewsForm().apply {
            startAdsVersion = StorageHelper.getAdVersion()
        }
        //保存调用时间
//        SPUtils.commitLong(KeyConstant.FINDNEWS_KEY, System.currentTimeMillis())

        Requests.create(AppService::class.java).findNews(form).doOnSubscribe {
            preprocess()
            request = true
        }.handleWithResponse(
            {
                request = false
                if (it.refreshStartAds) {
                    //todo 刷新广告
                }
                process(it.version)

            }, {
                request = false
                callbackError()
                it.printStackTrace()
                ULog.e("findNews 请求错误了")
            }, intArrayOf(-1, 500, 501)
        )

    }


    private fun process(data: CheckVersionResult?) {
        if (data == null) {
            callbackError()
            return
        }
        val ignoredVersion: String = SPUtils.getString(UpgradeApkService.IGNORED_VERSION, "")

        //忽略的版本,直接返回
        if (data.newVersion.replace(".", "") <= ignoredVersion) {
//           如果是自动更新 根据用户的忽略 直接跳过
            if (AppChecker.mIsAuto) {
                callback(false)
                return
            }
        }
        if (UpdateType.None == data.updateType) {//不需要下载
            ULog.i("DXC  已经是最新版本")
            callback(false)
            if (!AppChecker.mIsAuto)
                ToastUtils.showSuccessMessage("当前已是最高版本，无需更新")
            return
        }

        val targetFileName = "huanque-${data.newVersion.replace(".", "_")}.apk"

        val filesDir: File? = CommonInit.getInstance().getApp().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        //检查文件
        if (filesDir != null && filesDir.exists()) {
            //删除其他的文件的灵萌的文件,,,这样需要保证  不同时下载多个apk文件,不然会造成 huanque开头的 apk文件会有多个
            filesDir.listFiles().forEach {
                val name = it.name
//                println("文件下载监控.. 检查文件 .. 找到了下载好了的文件.....  ${it.absolutePath}")
                //删除灵萌的apk安装文件
                if (!name.equals(targetFileName) && name.indexOf("huanque") >= 0) {
                    it.delete()
                }
            }

            var updateFlag = DownloadStatus.DONE//默认认为  此时文件正确无误的下载,可以执行安装

            var targetFile: File = File(filesDir, targetFileName)
            if (targetFile != null && targetFile.exists()) {
                val fileMD5String = MD5Util.getFileMD5String(targetFile)
                if (!data.md5Code!!.equals(fileMD5String, true)) {//有文件存在,但是md5验证失败，重启下载任务,主要是继续监听下载事件
                    updateFlag = DownloadStatus.DOWNLOADING
                }
            } else {//文件不存在,可以直接下载
                updateFlag = DownloadStatus.NONE
                data.startDownload = true
            }

            data.localFile = targetFile
            //这里加延迟 防止在切换activity时弹窗
            Observable.timer(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    showTips(updateFlag, data)
                }

        } else {
            // 系统的下载目录 , 如果是空值,怎么处理?
            callbackError()
            reportCrash("没有找到系统的下载目录,需要查证是什么情况,.......")
        }

    }

    private fun showTips(flag: DownloadStatus, data: CheckVersionResult) {

        val newVersionDesc: String = data.updateContent ?: ""
        val activity = CommonInit.getInstance().getCurrentActivity()
        val newVersion = "V${data.newVersion}"
        val isForce = UpdateType.Force == (data.updateType)
        if (activity != null && !activity.isFinishing) {
            VersionUpdateDialog(activity).showUpdateDialog(
                versionInfo = newVersionDesc, versionNum = newVersion, versionDate = "",
                isForce = isForce, callback = VersionUpdateDialog.MyDialogCallback(
                    onCancel = {
                        SPUtils.commitString(UpgradeApkService.IGNORED_VERSION, data.newVersion.replace(".", ""))
                        callback(false)
                    },
                    onOk = {
                        if (DownloadStatus.DONE == flag) {//已经完整的下载了整个文件
                            val act = CommonInit.getInstance().getCurrentActivity()
                            if (act != null) {
                                act.installApk(data.localFile!!)
                                callback(false)
                            } else {
                                callbackError()
                            }

                        } else {
                            if (StringHelper.isEmpty(data.installUrl) || StringHelper.isEmpty(data.md5Code)) {
                                ULog.i("更新地址竟然有问题")
                                callbackError()
                            } else {
                                ToastUtils.showSuccessMessage("下载完毕之后将会自动为您安装新的版本")
                                UpgradeApkService.setInstallWhenDownloadComplete()
                                startDownloadService(data)
                                callback(true)
                            }
                        }

                    })
            )
        } else {
            callbackError()
        }
    }

    private var downloadTimes = 0//尝试下载的次数

    fun startDownloadService(data: CheckVersionResult): Unit {

        UpgradeApkService.registerErrorListener(object : UpgradeApkService.DownloadListener {
            override fun onDownloadResult(isSuccess: Boolean) {
                if (isSuccess) {
                    ULog.i("下载成功且文件正确")
                    callback(false)
                    UpgradeApkService.unRegisterErrorListener()
                } else {
                    val force = data.updateType == UpdateType.Force
                    if (force) {
                        if (downloadTimes < 3) {
                            ToastUtils.show("下载出错,即将为您重新下载,")
                            data.startDownload = true
                            startDownloadService(data)
                            ULog.e("下载出错,即将为您重新下载, $downloadTimes")
                            downloadTimes++
                        } else {
                            val message = "尝试多次仍然无法正确下载需要的文件\n请联系客服"
                            val activity = CommonInit.getInstance().getCurrentActivity()
                            if (activity != null)
                                MyAlertDialog(activity, false).showAlertWithOK(message, MyAlertDialog.MyDialogCallback(
                                    {
                                        downloadTimes = 0
//                                    System.exit(1)
                                        callbackError()
                                        ActivitiesManager.finishApp()
                                    }
                                ), "下载新版本app失败")
                            UpgradeApkService.unRegisterErrorListener()
                        }
                    } else {
                        callbackError()
                        UpgradeApkService.unRegisterErrorListener()
                    }
                }

            }
        })
        val activity = CommonInit.getInstance().getCurrentActivity()
        if (activity != null) {
            var intent = Intent(activity, UpgradeApkService::class.java)
            intent.putExtra(UpgradeApkService.PARAM_CHECK_VERSION_INFO, data)
            intent.action = "com.huanque.downService.start" //随便起个名字，在AndroidManifest中定义的
            activity.startService(intent)
        }
    }
}
