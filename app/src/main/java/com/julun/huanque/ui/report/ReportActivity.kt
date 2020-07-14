package com.julun.huanque.ui.report

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.julun.huanque.R
import com.julun.huanque.common.bean.forms.ReportForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.manager.aliyunoss.OssUpLoadManager
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.ToastUtils
import com.luck.picture.lib.config.PictureMimeType
import kotlinx.android.synthetic.main.activity_report.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2019/8/8 19:53
 *
 *@Description :举报页面
 *
 */
@Route(path = ARouterConstant.REPORT_ACTIVITY)
class ReportActivity : BaseReportActivity() {
    //举报需要参数
    private var reportType: Int = 0//举报类型 是举报主播还是用户 0是用户 1是主播
    private var mTargetUserId: Long = 0


    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        mTargetUserId = intent.getLongExtra(ParamConstant.TARGET_USER_ID, 0L)
        reportType = intent.getIntExtra(ParamConstant.REPORT_TYPE, 0)
        super.initViews(rootView, savedInstanceState)
        if (reportType == 0) {
            viewModel.queryReportList()
        } else if (reportType == 1) {
            viewModel.queryReportList()
        }

    }


    override fun startPublish() {

        val curType = reportTypeAdapter.getItem(currentSelect)
        if (curType == null) {
            ToastUtils.show("请选择举报类型")
            return
        }
        if (currentContent.isEmpty()) {
            ToastUtils.show("举报内容不能为空")
            return
        }
        if (mTargetUserId == 0L) {
            ToastUtils.show("被举报的用户id无效")
            return
        }
        if (selectList.isEmpty()) {
            ToastUtils.show("请添加图片再提交")
            return
        }
        val pathList = mutableListOf<String>()
        selectList.forEach {
            val isGif = PictureMimeType.isGif(it.pictureType)
            //动图直接上传原图
            if (isGif) {
                pathList.add(it.path)
            } else {
                pathList.add(it.compressPath)
            }

        }
        pathList.forEach { path ->
            //大于3M的不给上传
            val size = FileUtils.getFileOrFilesSize(path, FileUtils.SIZETYPE_KB)//单位kb
            logger.info("当前图片的大小：${size}kb")
            if (size > 3 * 1024) {
                ToastUtils.show(resources.getString(R.string.pic_size_is_out))
                return
            }
        }
        val form: ReportForm = ReportForm().apply {
            this.detail = input_text.text.toString()

            this.reportType = curType.itemValue
            this.userId = mTargetUserId
        }
//        val formAnchor: SaveReportProgramForm = SaveReportProgramForm().apply {
//            this.reason = currentContent
//            this.programId = mProgramId
//            this.reportTypeValue = curType.itemValue
//        }

        apply_button.isEnabled = false

        if (pathList.isNotEmpty()) {
            if (!loadingDialog.isShowing) {
                loadingDialog.showDialog(false)
            }
            val savePos = OssUpLoadManager.REPORT_USER_POSITION


            OssUpLoadManager.uploadFiles(pathList, savePos) { code, list ->
                if (code == OssUpLoadManager.CODE_SUCCESS) {
                    logger.info("结果的：$list")
                    if (list == null || list.isEmpty()) {
                        publishFail()
                        return@uploadFiles
                    }
                    val imgList = StringBuilder()
                    list.forEachIndexed { index, img ->
                        if (index != list.size - 1) {
                            imgList.append(img).append(",")
                        } else {
                            imgList.append(img)
                        }
                    }
                    logger.info("表单：" + imgList.toString())
                    //下一步

                    if (reportType == 0) {
                        form.pics = imgList.toString()
                        viewModel.reportLiveUser(form)
                    }/* else {
                        formAnchor.pics = imgList.toString()
                        viewModel.reportLiveAnchor(formAnchor)

                    }*/
                } else {
                    publishFail()
                }
            }
        } else {
            if (reportType == 0) {
                viewModel.reportLiveUser(form)
            } /*else {
                viewModel.reportLiveAnchor(formAnchor)
            }*/
        }
    }

    override fun publishSuccess() {
        super.publishSuccess()
//        statistics()
    }

    /**
     * 举报统计
     */
//    private fun statistics() {
//        if (reportType == 0) {
//            LingMengService.getService(IStatistics::class.java)?.onReportClick("用户", "用户:$mTargetUserId")
//        } else {
//            LingMengService.getService(IStatistics::class.java)?.onReportClick("主播", "主播:$mProgramId")
//        }
//    }
}
