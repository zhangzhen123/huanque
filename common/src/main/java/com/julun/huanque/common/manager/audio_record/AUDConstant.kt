package com.julun.huanque.common.manager.audio_record

import android.media.AudioFormat

object AUDConstant {
    /**
     * 采样率 现在能够保证所有的设备上实用的采样率是 44100hz 但是其他的采样率(22050, 16000, 11025）在一些设备上也可以使用
     */
    const val SAMPLE_RATE_IN_HZ = 44100

    /***
     * 声道数  CHANNEL_IN_MONO and CHANNEL_IN_STEREO  其中 CHANNEL_IN_MONO 是可以保证在所有设备能够使用
     */
    const val CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_MONO

    /***
     * 返回的音频数据的格式， ENCODING_PCM_8BIT ENCODING_PCM_16BIT and ENCODING_PCM_FLOAT
     */
    const val AUDIO_FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT
}