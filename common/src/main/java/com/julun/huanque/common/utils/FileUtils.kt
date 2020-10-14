package com.julun.huanque.common.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.julun.huanque.common.init.CommonInit
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.*
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


/**
 * Created by djp on 2016/11/22.
 */
object FileUtils {
    val TAG = "huanque"

    //SD卡保存文件使用的根文件夹名称
    const val ROOT_NAME = "/huanque/"

    //用户归因使用，保存UUID
    const val SOURCE = "source.txt"

    private val BUFF_SIZE = 2048

    // 在sd卡或app缓存路径中创建要上传的图片
    fun createPhotoFile(context: Context, photoName: String): File {
        val filePath = File.separator + "upload" + File.separator + photoName
        var file: File? = null
        if (sdCardExists) {
            file = File(Environment.getExternalStorageDirectory(), filePath)
        } else {
            file = File(context.cacheDir, filePath)
        }
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        return file
    }

    //获取存储位置 默认使用/mnt/sdcard/Android/data/com.my.app/files
    // 如果没有上面的位置使用内部/data/data/com.my.app/files
    fun getExFilePath(context: Context?): String? {
        if (context == null) {
            return null
        } else {
            var dir: File? = null
            var filesDir: File?
            var filesDirPath: String
            if (Environment.getExternalStorageState() == "mounted") {
                filesDir = context.getExternalFilesDir(null as String?)
                if (filesDir != null) {
                    filesDirPath = filesDir.absolutePath
                    if (!TextUtils.isEmpty(filesDirPath)) {
                        dir = File(filesDirPath)
                    }
                }
            }

            if (dir == null) {
                filesDir = context.filesDir
                if (filesDir != null) {
                    filesDirPath = filesDir.absolutePath
                    if (!TextUtils.isEmpty(filesDirPath)) {
                        dir = File(filesDirPath)
                    }
                }
            }

            if (dir == null) {
                return null
            } else {
                if (!dir.exists()) {
                    dir.mkdirs()
                    if (!dir.exists()) {
                        return null
                    }
                }

                return dir.absolutePath.toString()
            }
        }
    }

    /**
     * 创建文件， 如果不存在则创建，否则返回原文件的File对象
     *
     * @param path 文件路径
     * @return 创建好的文件对象, 返回为空表示失败
     */
    @Synchronized
    fun createFile(path: String): File? {
        if (TextUtils.isEmpty(path)) {
            return null
        }

        val file = File(path)
        if (file.isFile) {
            return file
        }

        val parentFile = file.parentFile
        if (parentFile != null && (parentFile.isDirectory || parentFile.mkdirs())) {
            try {
                if (file.createNewFile()) {
                    return file
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return null
    }

    /**
     * 返回指定文件 如果已经存在删除重新创建 保证文件是新的
     */
    fun getNewFile(path: String, name: String): File? {
        val file = File(path, name)
        if (file.exists()) {
            file.delete()
        }
        try {
            file.createNewFile()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 删除文件或目录
     *
     * @param path 文件或目录。
     * @return true 表示删除成功，否则为失败
     */
    @Synchronized
    fun delete(path: File?): Boolean {
        if (null == path) {
            return true
        }

        if (path.isDirectory) {
            val files = path.listFiles()
            if (null != files) {
                for (file in files) {
                    if (!delete(file)) {
                        return false
                    }
                }
            }
        }
        return !path.exists() || path.delete()
    }

    //写入数据到文件
    fun writeToFile(content: String, filePath: String) {
        var fileWriter: FileWriter? = null
        try {
            fileWriter = FileWriter(filePath, true)
            fileWriter.write(content)
            fileWriter.flush()
        } catch (t: Throwable) {
            t.printStackTrace()
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    // 得到app存放图片目录File对象
    fun getAppImgDir(context: Context): File {
        val path = File.separator + "huanque_img"
        var file: File? = null
        if (sdCardExists) {
            file = File(Environment.getExternalStorageDirectory(), path)
        } else {
            file = File(context.cacheDir, path)
        }
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

    val sdCardExists: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    /**
     * 压缩文件
     *
     * @param fs          需要压缩的文件
     * @param zipFilePath 被压缩后存放的路径
     * @return 成功返回 true，否则 false
     */
    fun zipFiles(fs: Array<File>?, zipFilePath: String): Boolean {
        if (fs == null) {
            throw NullPointerException("fs == null")
        }
        var result = false
        var zos: ZipOutputStream? = null
        try {
            zos = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFilePath)))
            for (file in fs) {
                if (file == null || !file.exists()) {
                    continue
                }
                if (file.isDirectory) {
                    recursionZip(zos, file, file.name + File.separator)
                } else {
                    recursionZip(zos, file, "")
                }
            }
            result = true
            zos!!.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                if (zos != null) {
                    zos!!.closeEntry()
                    zos!!.close()
                }
            } catch (e1: IOException) {
                e1.printStackTrace()
            }

        }
        return result
    }

    @Throws(Exception::class)
    private fun recursionZip(zipOut: ZipOutputStream, file: File, baseDir: String) {
        var baseDir = baseDir
        if (file.isDirectory) {
            val files = file.listFiles()
            for (fileSec in files!!) {
                if (fileSec == null) {
                    continue
                }
                if (fileSec.isDirectory) {
                    baseDir = file.name + File.separator + fileSec.name + File.separator
                    recursionZip(zipOut, fileSec, baseDir)
                } else {
                    recursionZip(zipOut, fileSec, baseDir)
                }
            }
        } else {
            val buf = ByteArray(BUFF_SIZE)
            val input = BufferedInputStream(FileInputStream(file))
            zipOut.putNextEntry(ZipEntry(baseDir + file.name))
//            var len: Int=-1
//            while ({len = input.read(buf); len}() != -1) {
//                zipOut.write(buf, 0, len)
//            }
            var len: Int = -1
            while ((input.read(buf).apply { len = this }) != -1) {
                zipOut.write(buf, 0, len)
            }
            input.close()
        }
    }

    /**
     * 存储Json文件
     *
     * @param context
     * @param json
     * json字符串
     * @param fileName
     * 存储的文件名
     * @param append
     * true 增加到文件末，false则覆盖掉原来的文件
     */
    fun writeJson(
        c: Context, json: String, fileName: String,
        append: Boolean
    ) {

        val cacheRoot = if (Environment.getExternalStorageState() === Environment.MEDIA_MOUNTED)
            c.externalCacheDir
        else
            c.cacheDir
        var fos: FileOutputStream? = null
        var os: ObjectOutputStream? = null
        try {
            val ff = File(cacheRoot, fileName)
            val boo = ff.exists()
            fos = FileOutputStream(ff, append)
            os = ObjectOutputStream(fos)
            if (append && boo) {
                val fc = fos!!.channel
                fc.truncate(fc.position() - 4)

            }

            os.writeObject(json)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {

            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (os != null) {

                try {
                    os.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

        }

    }

    /**
     * 存储对象文件
     *
     * @param context
     * @param bean  序列化的实体类
     * @param fileName 存储的文件名
     */
    fun writeObject(c: Context, bean: Any, fileName: String) {

        val cacheRoot = if (Environment.getExternalStorageState() === Environment.MEDIA_MOUNTED)
            c.externalCacheDir
        else
            c.cacheDir
        var fos: FileOutputStream? = null
        var os: ObjectOutputStream? = null
        try {
            val ff = File(cacheRoot, fileName)
            fos = FileOutputStream(ff)
            os = ObjectOutputStream(fos)
            if (!ff.exists()) {
                //如果文件不存在，重新写
                ff.mkdirs()
            }

            os.writeObject(bean)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {

            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            if (os != null) {

                try {
                    os.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

        }

    }

    fun <T> readObjectFromFile(c: Context, fileName: String): T? {
        val cacheRoot = if (Environment.getExternalStorageState() === Environment.MEDIA_MOUNTED)
            c.externalCacheDir
        else
            c.cacheDir
        var fos: FileInputStream? = null
        var os: ObjectInputStream? = null
        var reBean: Any? = null
        try {
            val ff = File(cacheRoot, fileName)
            if (!ff.exists()) {
                //如果文件不存在
                reBean = null
            } else {
                fos = FileInputStream(ff)
                os = ObjectInputStream(fos)
                reBean = os?.readObject()
            }

//            val bean = os?.readObject()
//            return bean

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            fos?.close()
            os?.close()
            return reBean as? T
        }
    }


    /**
     * 删除对象文件
     *
     * @param context
     * @param fileName 存储的文件名
     */
    fun deleteObject(c: Context, fileName: String) {

        val cacheRoot = if (Environment.getExternalStorageState() === Environment.MEDIA_MOUNTED)
            c.externalCacheDir
        else
            c.cacheDir
        try {
            val ff = File(cacheRoot, fileName)
            if (ff.exists()) {
                ff.delete()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {

        }
    }


    /**
     * 获取app缓存路径
     * @param context
     * @return
     */
    fun getCachePath(context: Context): String {
        return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
            //外部存储可用
            context.externalCacheDir?.path ?: context.cacheDir.path
        } else {
            //外部存储不可用
            context.cacheDir.path
        }
    }


    /**
     * 含子目录的文件压缩
     *
     * @throws Exception
     */
    // 第一个参数就是需要解压的文件，第二个就是解压的目录
    fun upZipFile(zipFile: String, folderPath: String): Boolean {
        var zfile: ZipFile? = null
        try {
            // 转码为GBK格式，支持中文
            zfile = ZipFile(zipFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        val zList = zfile!!.entries()
        var ze: ZipEntry? = null
        val buf = ByteArray(1024)
        while (zList.hasMoreElements()) {
            ze = zList.nextElement()
            // 列举的压缩文件里面的各个文件，判断是否为目录
            if (ze!!.isDirectory) {
                val dirstr = folderPath + ze.name
                dirstr.trim { it <= ' ' }
                val f = File(dirstr)
                f.mkdir()
                continue
            }
            var os: OutputStream? = null
            var fos: FileOutputStream? = null
            // ze.getName()会返回 script/start.script这样的，是为了返回实体的File
            val realFile = getRealFileName(folderPath, ze.name)
            try {
                fos = FileOutputStream(realFile)
            } catch (e: FileNotFoundException) {
                return false
            }

            os = BufferedOutputStream(fos)
            var `is`: InputStream? = null
            try {
                `is` = BufferedInputStream(zfile!!.getInputStream(ze))
            } catch (e: IOException) {
                return false
            }


            // 进行一些内容复制操作
            try {
                var readLen = `is`.read(buf, 0, 1024)
                while (readLen != -1) {
                    os.write(buf, 0, readLen)
                    readLen = `is`.read(buf, 0, 1024)
                }
            } catch (e: IOException) {
                return false
            }

            try {
                `is`.close()
                os.close()
            } catch (e: IOException) {
                return false
            }

        }
        try {
            zfile!!.close()
        } catch (e: IOException) {
            return false
        }

        return true
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir
     * 指定根目录
     * @param absFileName
     * 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    fun getRealFileName(baseDir: String, absFileName: String): File {
        var absFileName = absFileName
        absFileName = absFileName.replace("\\", "/")
        val dirs = absFileName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var ret = File(baseDir)
        var substr: String? = null
        if (dirs.size > 1) {
            for (i in 0 until dirs.size - 1) {
                substr = dirs[i]
                ret = File(ret, substr)
            }

            if (!ret.exists())
                ret.mkdirs()
            substr = dirs[dirs.size - 1]
            ret = File(ret, substr)
            return ret
        } else {
            ret = File(ret, absFileName)
        }
        return ret
    }


    /**
     * @param folder 文件夹名称
     * @param name 文件名称
     */
    fun createFile(folder: String, name: String) {
        Single.just(1)
            .subscribeOn(Schedulers.io())
            .subscribe({
                val file = File("$folder/$name")
                if (!file.exists()) {
                    file.createNewFile()
                }
            }, {})
    }

    //获取文件后缀
    fun getFilextension(file: String): String {
        return file.substringAfterLast('.', "")
    }

    fun bitmap2File(bitmap: Bitmap, fileName: String, context: Context): File? {
        val path = getExFilePath(context) + "/" + fileName + ".png" //默认放在/app/files中
        val file = createFile(path)
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    /******* 以下是文件大小相关**********************************************/
    val SIZETYPE_B = 1//获取文件大小单位为B的double值
    val SIZETYPE_KB = 2//获取文件大小单位为KB的double值
    val SIZETYPE_MB = 3//获取文件大小单位为MB的double值
    val SIZETYPE_GB = 4//获取文件大小单位为GB的double值

    /**
     * 获取文件指定文件的指定单位的大小
     *
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    fun getFileOrFilesSize(filePath: String, sizeType: Int): Double {
        val file = File(filePath)
        var blockSize: Long = 0
        try {
            if (file.isDirectory) {
                blockSize = getFileSizes(file)
            } else {
                blockSize = getFileSize(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return formatFileSize(blockSize, sizeType)
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    fun getAutoFileOrFilesSize(filePath: String): String {
        val file = File(filePath)
        var blockSize: Long = 0
        try {
            if (file.isDirectory) {
                blockSize = getFileSizes(file)
            } else {
                blockSize = getFileSize(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return formatFileSize(blockSize)
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun getFileSize(file: File): Long {
        var size: Long = 0
        if (file.exists()) {
            var fis: FileInputStream? = null
            fis = FileInputStream(file)
            size = fis.available().toLong()
        } else {
            file.createNewFile()
        }
        return size
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun getFileSizes(f: File): Long {
        var size: Long = 0
        val flist = f.listFiles()
        for (i in flist!!.indices) {
            if (flist[i].isDirectory) {
                size = size + getFileSizes(flist[i])
            } else {
                size = size + getFileSize(flist[i])
            }
        }
        return size
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private fun formatFileSize(fileS: Long): String {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        val wrongSize = "0B"
        if (fileS == 0L) {
            return wrongSize
        }
        if (fileS < 1024) {
            fileSizeString = df.format(fileS.toDouble()) + "B"
        } else if (fileS < 1048576) {
            fileSizeString = df.format(fileS.toDouble() / 1024) + "KB"
        } else if (fileS < 1073741824) {
            fileSizeString = df.format(fileS.toDouble() / 1048576) + "MB"
        } else {
            fileSizeString = df.format(fileS.toDouble() / 1073741824) + "GB"
        }
        return fileSizeString
    }

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
    private fun formatFileSize(fileS: Long, sizeType: Int): Double {
        val df = DecimalFormat("#.00")
        var fileSizeLong = 0.0
        when (sizeType) {
            SIZETYPE_B -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble()))
            SIZETYPE_KB -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble() / 1024))
            SIZETYPE_MB -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble() / 1048576))
            SIZETYPE_GB -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble() / 1073741824))
            else -> {
            }
        }
        return fileSizeLong
    }

    /******* 以上是文件大小相关***************************************************/

    /**
     * 兼容方法
     * 保存图片到相册
     */
    open fun saveBitmapToDCIM(bitmap: Bitmap, bitName: String): Boolean {
        val fileName: String
        val file: File
        val brand: String = Build.BRAND
        fileName = if (brand == "xiaomi") { // 小米手机brand.equals("xiaomi")
            Environment.getExternalStorageDirectory().path + "/DCIM/Camera/" + bitName
        } else if (brand.equals("Huawei", ignoreCase = true)) {
            Environment.getExternalStorageDirectory().path + "/DCIM/Camera/" + bitName
        } else { // Meizu 、Oppo
            Environment.getExternalStorageDirectory().path + "/DCIM/" + bitName
        }
        //        fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName;
        file = if (Build.VERSION.SDK_INT >= 29) {
            //            boolean isTrue = saveSignImage(bitName, bitmap);
            saveSignImage(bitName, bitmap)
            return true
            //            file= getPrivateAlbumStorageDir(NewPeoActivity.this, bitName,brand);
            //            return isTrue;
        } else {
            Log.v("saveBitmap brand", "" + brand)
            createFile(fileName)?:File(fileName)
        }
        if (file.exists()) {
            file.delete()
        }
        val out: FileOutputStream
        try {
            out = FileOutputStream(file)
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush()
                out.close()
                // 插入图库
                if (Build.VERSION.SDK_INT >= 29) {
                    val values = ContentValues()
                    values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    val uri: Uri? = CommonInit.getInstance().getContext().contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )
                } else {
                    MediaStore.Images.Media.insertImage(
                        CommonInit.getInstance().getContext().contentResolver,
                        file.absolutePath,
                        bitName,
                        null
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e("FileNotFoundException", "FileNotFoundException:$e")
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            Log.e("IOException", "IOException:$e")
            e.printStackTrace()
            return false
        } catch (e: java.lang.Exception) {
            Log.e("IOException", "IOException:" + e.message.toString())
            e.printStackTrace()
            return false

// 发送广播，通知刷新图库的显示
        }
        //        if(Build.VERSION.SDK_INT >= 29){
//            copyPrivateToDownload(this,file.getAbsolutePath(),bitName);
//        }
        CommonInit.getInstance().getContext()
            .sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$fileName")))
        return true
    }

    //将文件保存到公共的媒体文件夹
    //这里的filepath不是绝对路径，而是某个媒体文件夹下的子路径，和沙盒子文件夹类似
    //这里的filename单纯的指文件名，不包含路径
    private fun saveSignImage( /*String filePath,*/
        fileName: String?, bitmap: Bitmap
    ) {
        try {
            //设置保存参数到ContentValues中
            val contentValues = ContentValues()
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            //兼容Android Q和以下版本
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
                //RELATIVE_PATH是相对路径不是绝对路径
                //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/")
                //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Music/signImage");
            } else {
                contentValues.put(
                    MediaStore.Images.Media.DATA,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                )
            }
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG")
            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            val uri: Uri? = CommonInit.getInstance().getContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                val outputStream: OutputStream? = CommonInit.getInstance().getContext().contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


}