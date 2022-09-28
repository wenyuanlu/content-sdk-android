#---------------------------------基本指令----------------------------------
 # 设置混淆的压缩比率 0 ~ 7
    -optimizationpasses 5
    # 混淆后类名都为小写   Aa aA
    -dontusemixedcaseclassnames
    # 指定不去忽略非公共库的类
    -dontskipnonpubliclibraryclasses
    #不做预校验的操作
    -dontpreverify
    # 混淆时不记录日志
    -verbose
    # 混淆采用的算法.
    -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
    #保留代码行号，方便异常信息的追踪
    -keepattributes SourceFile,LineNumberTable
    #dump文件列出apk包内所有class的内部结构
    -dump class_files.txt
    #seeds.txt文件列出未混淆的类和成员
    -printseeds seeds.txt
    #usage.txt文件列出从apk中删除的代码
    -printusage unused.txt
    #mapping文件列出混淆前后的映射
    -printmapping mapping.txt
    #保护注解
    -keepattributes *Annotation*
#----------------------------------------------------------------------------

#---------------------------------默认保留区，避免混淆Android基本组件---------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class * extends android.support.annotation.*
-keep public class * extends android.support.v7.*
-keep public class * extends android.graphics.*
-keep public class * extends java.lang.Exception
-keep public class * extends android.webkit.WebView
-keep class android.view.**{*;}

#不提示V4包下错误警告
-dontwarn android.support.v4.**
#保持下面的V4兼容包的类不被混淆
-keep class android.support.v4.*{*;}
#避免混淆所有native的方法,涉及到C、C++
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}

#避免混淆枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#避免混淆自定义控件类的get/set方法和构造函数
#混淆保护自己项目的部分代码以及引用的第三方jar包library-end##################
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
#避免混淆序列化类
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#不混淆Serializable和它的实现子类、其成员变量
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#使用GSON、fastjson等框架时，所写的JSON对象类不混淆，否则无法将JSON解析成对应的对象
-keepclassmembers class * {
        public <init>(org.json.JSONObject);
}
#不混淆资源类
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}

#移除log
-assumenosideeffects class android.util.Log{
    public static int v(...);
    public static int i(...);
    public static int d(...);
    public static int w(...);
    public static int e(...);
}
#避免混淆泛型 如果混淆报错建议关掉
-keepattributes Signature

# 源文件和行号的信息不混淆
-keepattributes SourceFile,LineNumberTable
#----------------------------------------------------------------------------

#---------------------------------webview------------------------------------
# 不混淆 WebView 的 JS 接口
-keepattributes *JavascriptInterface*
# 不混淆 WebView 的类的所有的内部类
-keepclassmembers class com.hjq.demo.ui.activity.BrowserActivity$*{
    *;
}
# 不混淆 WebChromeClient 中的 openFileChooser 方法
-keepclassmembers class * extends android.webkit.WebChromeClient{
   public void openFileChooser(...);
}
-keepclassmembers class  * extends android.webkit.WebView {
   public *;
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}
#----------------------------------------------------------------------------


#---------------------------------2.第三方包-------------------------------
#SmartRefreshLayoutS 没有使用到：序列化、反序列化、JNI、反射，所以并不需要添加混淆过滤代码，并且已经混淆测试通过
#Picasso

# EventBus3
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}


#kotlin
-keep class kotlin.* { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keep class **.R$* {*;}

# OkHttp3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}

# If you do NOT use SQLCipher:
-dontwarn net.sqlcipher.database.**
# If you do NOT use RxJava:
-dontwarn rx.**

#gson
-dontwarn com.google.**
-keep class com.google.gson.* {*;}

#rxjava
-dontwarn java.util.concurrent.Flow*

-keep class com.linkedin.* { *; }
-keep class com.android.dingtalk.share.ddsharemodule.* { *; }
-keepattributes Signature

-keepattributes *Annotation*

-keep public class **.R$* {
    public static final int *;
}
#----------------------------------------------------------------------------

## ---------Retrofit混淆方法---------------
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
# OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal**{*;}
-dontwarn okio.**
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keep class com.maishuo.haohai.api** { *;}
#retrofit module
-keep class com.qichuang.bean** { *; }
-keep class com.qichuang.config** { *; }
-keep class com.qichuang.dialog** { *; }
-keep class com.qichuang.glide** { *; }
-keep class com.qichuang.retrofit** { *; }

# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef** {
    rx.internal.util.atomic.LinkedQueueNode** producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef** {
    rx.internal.util.atomic.LinkedQueueNode** consumerNode;
}

# BaseRecyclerViewAdapterHelper
-keep class com.chad.library.adapter.* {*;}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder**
-keepclassmembers public class * extends com.chad.library.adapter.base.BaseViewHolder** {
           <init>(android.view.View);
}

# Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod

#immersionbar混淆规则
-keep class com.gyf.immersionbar.* {*;}
-dontwarn com.gyf.immersionbar.**

# ViewBinding
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
  public static * inflate(android.view.LayoutInflater);
}
#----------------------------------------------------------------------------
#dowanload
-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria**{*;}
-keep class **$$DownloadListenerProxy{ *; }
-keep class **$$UploadListenerProxy{ *; }
-keep class **$$DownloadGroupListenerProxy{ *; }
-keep class **$$DGSubListenerProxy{ *; }
-keepclasseswithmembernames class * {
    @Download.* <methods>;
    @Upload.* <methods>;
    @DownloadGroup.* <methods>;
}

#----------------------------------------------------------------------------
#腾讯x5
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**

-keep class com.tencent.smtt.** {
    *;
}

-keep class com.tencent.tbs.** {
    *;
}

#ivoice
-keep class com.corpize.sdk.ivoice.**{*;}

#内容sdk
-keep class com.maishuo.haohai.main.**{*;}
-keep class com.maishuo.haohai.main.event.** {*;}
-keep class com.maishuo.haohai.main.service.** {*;}
-keep class com.maishuo.haohai.api.**{*;}
-keep class com.maishuo.haohai.common.**{*;}
-keep class com.maishuo.haohai.manager.**{*;}
-keep class com.maishuo.haohai.listener.**{*;}
-keep class com.qichuang.commonlibs.**{*;}
-keep class com.qichuang.retrofitlibs.**{*;}
