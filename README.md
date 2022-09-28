# Android SDK 接入文档

 

**2021-1-21**

| 版本号 | 日期       | 字段 | 说明                            |
| ------ | ---------- | ---- | --------------------------- |
| 0.8.2  | -          | -    | release版本                     |
|

# 一、背景 

## 1.1 开发环境

| 字段      | 说明                                      |
| -------- | ----------------------------------------  |
| 开发工具  | Android  Studio + Gradle  4.1.2 + JDK 1.8  |
| 部署目标  | Android  5.0(21)及以上的版本                |
| 支持设备  | Android  5.0(21)系统及以上的手机             |
| 开发语言  | kotlin                                      |
| 开发环境  | 支持androidX的编译                 |


## 1.2 其他 
1.本文档建议阅读器为[Typora](https://www.typora.io/)
**2.请仔细阅读本文档并参考app进行sdk集成**


# 二、项目集成

## 2.2、接入配置 (详情见Demo)

### 2.2.1 导包
```
把contentandroidsdk_V0.8.2_release.aar及ivoice包com.corpize.sdk.ivoice_V1.1.2.aar导入工程libs下方；

```
###  

### 2.2.2 接入build.gradle配置

引入 gradle_dependencies.gradle
app gradle 中添加

在build.gradle中配置aar

```
apply from: '../gradle_dependencies.gradle'

android{
    defaultConfig{
        ndk{
            //加载需要的so库架构
            abiFilters 'armeabi-v7a', 'x86', 'arm64-v8a', 'x86_64'/*, 'armeabi'*/
        }
}

sourceSets {
    main{
        jniLibs.srcDirs = ['libs']
        }
    }
 
    repositories {
        flatDir {
            dirs 'libs'
        }
    }
}
 
Dependencies{
    //ad sdk need implementation start
    implementation(name: 'com.corpize.sdk.ivoice_V1.1.2', ext: 'aar')
 
    //library
    implementation(name: 'contentandroidsdk_V0.8.2_release', ext: 'aar')
}
```

**注:以上依赖版本请参考实际开发环境及1.2版本依赖进行添加**

### 2.2.3 配置AndroidManifest.xml文件 配置对应的权限
```
<!--需要权限 -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />

<application>
    <provider
         android:name="com.corpize.sdk.ivoice.utils.downloadinstaller.QcDownloadProvider"
         android:authorities="${applicationId}.QcDownloadProvider"
         android:exported="false"
         android:grantUriPermissions="true"
         tools:replace="android:authorities">
     
    <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/qc_paths"
          tools:replace="android:resource" />
    </provider>
</application>
```

## 2.3、接入代码

### 2.3.1 初始化 

1、在application的onCreate()方法中调用初始化方法

1.mid为媒体ID,为应用专属id,详情咨询商务.
2.contentId 为内容id.
3.dnt为是否允许广告追踪,0不允许,1允许
```
//初始化广告
ContentAndroidSDK.init(this, oaid,mid,contentId,dnt);
```

2、嵌套内容SDK的Activity添加Fragment

private var adSDKManager: ContentAdSDKManager? = null
//回调后把Fragment放入Activity容器
ContentAndroidSDK.addOnContentInfo(
            this,
            null,
            object : OnContentAndroidSdkInitListener {
                override fun onInit(manager: ContentAdSDKManager?, fragment: Fragment?) {
                    adSDKManager = manager
                    commitFragment(R.id.main_simple_content_layout, fragment)
                }
            })
//页面结束释放内存
override fun onDestroy() {
        super.onDestroy()
        adSDKManager?.destroy()
    }

# 三、注 意
```
因为广告的获取需要用到手机设备信息，定位权限等权限，App版本在Android 6.0以上的需要动态申请权限。
SDK中只包含了最基本的权限申请，Demo代码里是一个基本的权限申请示例，
请开发者根据自己的场景合理地编写代码来实现权限申请。

App版本在Android6.0以下的忽略本条提示。
```

# 四、混淆

```
# ------------------------------------------通用区域----------------------------------------------------

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
-keep class com.qichuang.roomlib.**{*;}

```

 

 
