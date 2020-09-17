# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontshrink
-dontwarn android.support.annotation.Keep
-keepclasseswithmembernames class * {  # 保持 native 方法不被混淆
    native <methods>;
}

#华为获取oaid对应混淆
#-keep class com.huawei.hms.ads.** { *; }
#-keep interface com.huawei.hms.ads.** { *; }

#MSA对应混淆
-keep, includedescriptorclasses class
com.asus.msa.SupplementaryDID.** { *; }
-keepclasseswithmembernames class com.asus.msa.SupplementaryDID.**{ *; }
-keep, includedescriptorclasses class com.asus.msa.sdid.** { *; }
-keepclasseswithmembernames class com.asus.msa.sdid.** { *; }
-keep public class com.netease.nis.sdkwrapper.Utils {
public <methods>;
}
-keep class com.bun.miitmdid.**{*;}
-keep class com.bun.lib.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class a.**{*;}