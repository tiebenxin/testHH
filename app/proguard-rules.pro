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

#指定代码的压缩级别
-optimizationpasses 5

#包明不混合大小写
-dontusemixedcaseclassnames

#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses

#优化  不优化输入的类文件
-dontoptimize

#预校验
-dontpreverify

#混淆时是否记录日志
-verbose

# 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#混淆前后的映射
-printmapping mapping.txt

#support.v4/v7包不混淆
-keep class android.support.** { *; }
-keep class android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.** { *; }
-keep public class * extends android.support.v7.**
-keep interface android.support.v7.app.** { *; }
# 忽略警告
-dontwarn android.support.**

# 保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.FragmentActivity
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keepnames class * implements java.io.Serializable
#-keepnames class * implements android.os.Parcelable

-keep class com.google.gson.** {*;}

-keep class **.R$* {
 *;
}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
 # 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# 保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
# 保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# 保持 BaseEvent 不被混淆
-keep public class * extends net.cb.cb.library.event.BaseEvent{
    *;
}

# 保持 BaseBean 不被混淆
-keep public class * extends net.cb.cb.library.base.BaseBean{
    *;
}

# 保持 RealmObject 不被混淆
-keep public class * extends io.realm.RealmObject{
    *;
}

# 保持 MsgBean 不被混淆
-keep public interface * extends com.google.protobuf.MessageOrBuilder{*;}
-keep public class com.yanlong.im.utils.socket.MsgBean{*;}
-keep public class * extends com.google.protobuf.GeneratedMessageV3{*;}
# 保持 MsgBean下面的内部类不被混淆
-keep public class com.yanlong.im.utils.socket.MsgBean$*{*;}


#webview
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

#databinding
-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

# OkHttp3
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

# retrofit
-dontwarn okio.**
-dontwarn javax.annotation.**

# Okio
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }

#rx
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
-dontnote rx.internal.util.PlatformDependent

#EventBus
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
    void on*Event*(**);
}
#####EventBus混淆配置
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

-keepattributes Signature
# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable


#=================百度地图======================
-keep class com.baidu.** {*;}
-keep class mapsdkvi.com.** {*;}
-dontwarn com.baidu.**
#=================百度地图======================

#=================网易云=======================
-dontwarn com.netease.**
-keep class com.netease.** {*;}
#如果你使用全文检索插件，需要加入
-dontwarn org.apache.lucene.**
-keep class org.apache.lucene.** {*;}
#=================网易云=======================

#=================友盟=======================
-keep class com.umeng.** {*;}
#=================友盟=======================

#=================Bugly=======================
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
#=================Bugly=======================

#=================激光推送=======================
-dontwarn cn.jiguang.**
-keep class cn.jiguang.**{*;}

-dontwarn cn.jpush.a.**
-keep class cn.jpush.a.**{*;}

-dontwarn cn.jpush.android.**
-keep class cn.jpush.android.**{*;}
#=================激光推送=======================

#=================pingyin4j=======================
-dontwarn net.soureceforge.pinyin4j.**
-dontwarn demo.**
-keep class net.sourceforge.pinyin4j.** { *;}
-keep class demo.** { *;}
#=================pingyin4j=======================

#=================lansosdk=======================
-dontwarn com.lansosdk.**
-dontwarn com.libyuv.**
-keep class com.lansosdk.** { *; }
-keep class com.libyuv.** { *; }
#=================lansosdk=======================

#=================jrmf=======================
-dontwarn com.jrmf360.**
-keep class com.jrmf360.** { *; }
#=================jrmf=======================



-keep class com.alibaba.sdk.android.oss.** { *; }
-dontwarn okio.**
-dontwarn org.apache.commons.codec.binary.**


#=================arouter=======================
-keep public class com.alibaba.android.arouter.routes.**{*;}
-keep public class com.alibaba.android.arouter.facade.**{*;}
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}

-keepnames class com.yanlong.im.pay.ui.record.RedEnvelopeRecordActivity
-keepnames class com.yanlong.im.pay.ui.record.RedpacketRecordActivity
-keepnames class com.yanlong.im.pay.ui.record.SingleRedPacketDetailsActivity
-keepnames class com.yanlong.im.user.ui.HelpActivity
#=================arouter=======================





