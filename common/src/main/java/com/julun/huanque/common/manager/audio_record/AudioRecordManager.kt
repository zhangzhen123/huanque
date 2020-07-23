package com.julun.huanque.common.manager.audio_record

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.suger.logger
import com.julun.huanque.common.utils.FileUtils
import com.julun.huanque.common.utils.ToastUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit


object AudioRecordManager {


    private var dispose: Disposable? = null

    //    private var mAudioRecord: AudioRecord? = null
    private var isRecording = false
    private val permissions = arrayOf<String>(
        Manifest.permission.RECORD_AUDIO
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    //录制的音频文件临时名称
    private const val PCM_TEMP_NAME = "record_temp.pcm"
    private const val AUDIO_RECORD_NAME = "audio_record.wav"
    private val PATH: String = CommonInit.getInstance().getContext().externalCacheDir?.absolutePath ?: ""
    private var totalTime: Long = 0L
    private var mRecordCallBack: RecordCallBack? = null
    private var mAudioThread: AudioThread? = null
    fun startRecord(callBack: RecordCallBack? = null) {
        logger("startRecord")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(CommonInit.getInstance().getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ToastUtils.show("请先给录音权限")
                return

            }
//            if (ContextCompat.checkSelfPermission(CommonInit.getInstance().getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//
//                return
//
//            }

        }
        mRecordCallBack = callBack

        if (isRecording) {
            ToastUtils.show("当前正在录制中，请稍后")
            return
        }
        //如果存在，先停止线程
        if (mAudioThread != null) {
            mAudioThread!!.done()
            mAudioThread = null
        }
        //开启线程录制
        //开启线程录制
        mAudioThread = AudioThread()
        mAudioThread?.start()
        dispose = Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            totalTime = it+1
            mRecordCallBack?.process(it+1)
        }
//        mAudioRecord?.startRecording()
        isRecording = true

    }

    internal class AudioThread : Thread() {
        private val record: AudioRecord
        private val minBufferSize: Int = AudioRecord.getMinBufferSize(
            AUDConstant.SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_STEREO,
            AUDConstant.AUDIO_FORMAT
        )
        private var isDone = false
        override fun run() {
            super.run()
            var fos: FileOutputStream? = null
            var wavFos: FileOutputStream? = null
            var wavRaf: RandomAccessFile? = null
            try {
                //创建 pcm 文件
                val pcmFile: File = FileUtils.getNewFile(PATH, PCM_TEMP_NAME) ?: return
                //创建 wav 文件
                val wavFile: File = FileUtils.getNewFile(PATH, AUDIO_RECORD_NAME) ?: return
                fos = FileOutputStream(pcmFile)
                wavFos = FileOutputStream(wavFile)

                //先写头部，刚才是，我们并不知道 pcm 文件的大小
                val headers: ByteArray = PcmToWavUtil.generateWavFileHeader(
                    0,
                    AUDConstant.SAMPLE_RATE_IN_HZ.toLong(),
                    record.channelCount
                )
                wavFos.write(headers, 0, headers.size)

                //开始录制
                record.startRecording()
                val buffer = ByteArray(minBufferSize)
                while (!isDone) {
                    //读取数据
                    val read = record.read(buffer, 0, buffer.size)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        //写 pcm 数据
                        fos.write(buffer, 0, read)
                        //写 wav 格式数据
                        wavFos.write(buffer, 0, read)
                    }
                }
                //录制结束
                record.stop()
                record.release()
                fos.flush()
                wavFos.flush()

                //修改头部的 pcm文件 大小
                wavRaf = RandomAccessFile(wavFile, "rw")
                val header: ByteArray = PcmToWavUtil.generateWavFileHeader(
                    pcmFile.length(),
                    AUDConstant.SAMPLE_RATE_IN_HZ.toLong(),
                    record.channelCount
                )
                wavRaf.seek(0)
                wavRaf.write(header)

                Observable.just(wavFile).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    mRecordCallBack?.finish(wavFile, totalTime)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Observable.just(e).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    mRecordCallBack?.finish(null)
                }

            } finally {
                isRecording = false
                CloseUtils.close(fos, wavFos, wavRaf)
            }
        }

        fun done() {
            interrupt()
            isDone = true
        }

        init {
            /**
             * 获取最小 buffer 大小
             * 采样率为 44100，双声道，采样位数为 16bit
             */
            //使用 AudioRecord 去录音
            record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                AUDConstant.SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_STEREO,
                AUDConstant.AUDIO_FORMAT,
                minBufferSize
            )
        }
    }


    /***
     * 停止录制
     */
    fun stopRecord() {
        if (!isRecording) {
            logger("根本没有开始录音")
            return
        }

        if (mAudioThread != null) {
            mAudioThread!!.done()
        }
        logger("stopRecord")
        dispose?.dispose()

    }

    fun destroy() {
        if (mAudioThread != null) {
            mAudioThread!!.done()
            mAudioThread = null
        }
        mRecordCallBack = null
        clearRecordFile()
    }

    /**
     * 清理录音留下的文件
     */
    private fun clearRecordFile() {
        val pcmFile = File(CommonInit.getInstance().getContext().externalCacheDir, PCM_TEMP_NAME)
        val wavFile =
            File(CommonInit.getInstance().getContext().externalCacheDir, AUDIO_RECORD_NAME)
        if (pcmFile.exists()) {
            pcmFile.delete()
        }
        if (wavFile.exists()) {
            wavFile.delete()
        }
    }

    interface RecordCallBack {
        fun process(time: Long)

        //wavFile为null代表录音失败 没有返回
        fun finish(wavFile: File?, totalTime: Long? = null)
    }
}