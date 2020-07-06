package com.julun.huanque.common.bean.forms


/**
 * Created by djp on 2016/11/17.
 */
class PublishStateForm : SessionForm() {
    var content: String = ""//
    var pic:String=""//图片地址，多个以英文逗号分隔
    var picSize:String="" //	图片尺寸，宽高以英文逗号分隔 ，一张图时传递值
}
class PublishVideoForm : SessionForm() {
    var content: String = ""//
    var url:String =""//视频地址
    var pic:String=""//图片地址，多个以英文逗号分隔
    var picSize:String="" //封面尺寸，宽高以英文逗号分隔
    var duration:Long=0 //视频时长 单位 秒
}
class CheckForm(var content:String)