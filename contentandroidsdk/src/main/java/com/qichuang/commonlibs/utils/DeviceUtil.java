package com.qichuang.commonlibs.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.LocaleList;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.core.app.NotificationManagerCompat;


import com.qichuang.commonlibs.basic.CustomBasicApplication;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * author: yh
 * date: 2019-08-19 18:02
 * description: 获取设备的相关信息
 */
public class DeviceUtil {

    private static Application mApplication = CustomBasicApplication.INSTANCE.getInstance();

    /**
     * 手机系统版本
     */
    public static Application getApplication () {
        if (mApplication == null) {
            mApplication = CustomBasicApplication.INSTANCE.getInstance();
        }
        return mApplication;
    }

    /**
     * 手机系统版本
     */
    public static String getSdkVersion () {
        return Build.VERSION.RELEASE;
    }

    /**
     * 手机系统API level
     */
    public static int getSdkAPILevel () {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 手机型号
     */
    public static String getPhoneModel () {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     */
    public static String getManufacturer () {
        return Build.MANUFACTURER;
    }

    /**
     * 获取手机语言
     */
    public static String getLanguage () {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        String language = locale.getLanguage() + "-" + locale.getCountry();
        return language;
    }

    /**
     * 获取本机号码(需要“android.permission.READ_PHONE_STATE”权限)
     */
    @SuppressLint ("MissingPermission")
    public static String getPhoneNumber () {
        try {
            TelephonyManager tm = (TelephonyManager) getApplication().getSystemService(Activity.TELEPHONY_SERVICE);
            if (tm != null) {
                return tm.getLine1Number();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     */
    /*@SuppressLint ("MissingPermission")
    public static String getIMEI () {
        try {
            TelephonyManager tm = (TelephonyManager) getApplication().getSystemService(Activity.TELEPHONY_SERVICE);
            if (tm != null) {
                return tm.getDeviceId();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }*/

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     */
    @SuppressLint ("MissingPermission")
    public static String getIMEI () {
        String imei = "";
        try {
            TelephonyManager tm = (TelephonyManager) getApplication().getSystemService(Activity.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                imei = tm.getDeviceId();
            } else if (Build.VERSION.SDK_INT >= 29) {
                //imei = getUDID(context);
                imei = "";
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelephonyManager tm2 = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
                imei = tm2.getImei();
            } else {
                ///反射获取
                Method method = tm.getClass().getMethod("getImei");
                imei = (String) method.invoke(tm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //LogUtils.LOGE("PhoneUtils", "imei==" + imei);
        return imei;
    }

    /**
     * 获取移动联盟针对于AndroidQ的的oaid号
     */
//    public static String getOaid () {
//        String qcadOaid = SpUtils.getString(Constants.QCAD_OAID);
//        if (!TextUtils.isEmpty(qcadOaid)) {
//            return qcadOaid;
//        } else {
//            MiitHelper.getInstanse().getDeviceIds(getApplication(), null);
//            return "";
//        }
//
//    }

    /**
     * 获取手机ID(需要“android.permission.READ_PHONE_STATE”权限)
     */
    @SuppressLint ({"MissingPermission", "HardwareIds"})
    public static String getAndroidId () {
        String ANDROID_ID = "";
        try {
            ANDROID_ID = "" + Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
            //TelephonyManager tm = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
            //if (tm == null || tm.getDeviceId() == null) {
            //    return Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
            //} else {
            //    return tm.getDeviceId();
            //}
            return ANDROID_ID;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ANDROID_ID;
    }

    /**
     * 获取唯一设备ID
     */
    @SuppressWarnings ("deprecation")
    @SuppressLint ("MissingPermission")
    public static String getDeviceToken (Context context) {
        String ANDROID_ID = "";
        try {
            ANDROID_ID = "" + Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        } catch (Exception e) {

        }
        //        if (Build.VERSION.SDK_INT < 29) {
        //            //如果android sdk小于Q
        //            String deviceid = "" + tm.getDeviceId();
        //            String SimSerialNumber = "" + tm.getSimSerialNumber();
        //            String SerialNumber = "" + Build.SERIAL;
        //            String totalid = deviceid + SimSerialNumber + ANDROID_ID + SerialNumber;
        //            LogUtils.LOGE("PhoneUtils", "ANDROID_ID==" + totalid);
        //            return totalid;
        //        }
        LogUtils.LOGE("PhoneUtils", "ANDROID_ID==" + ANDROID_ID);
        return ANDROID_ID;
    }

    /**
     * 获取手机Mac(需要“android.permission.ACCESS_WIFI_STATE”权限)
     */
    @SuppressLint ("MissingPermission")
    public static String getMAC () {
        try {
            Application application = getApplication();
            WifiManager wifiManager = (WifiManager) application.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo    wInfo       = wifiManager.getConnectionInfo();
            String      macAddress  = wInfo.getMacAddress();
            return macAddress;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获得当前应用包名
     */
    public static String getPackageName () {
        String packageNames = "";
        try {
            PackageManager pm          = getApplication().getPackageManager();
            PackageInfo    packageInfo = pm.getPackageInfo(getApplication().getPackageName(), 0);
            packageNames = packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
            return packageNames;
        }
        return packageNames;
    }

    /**
     * 获得当前应用名称
     */
    public static String getAppName () {
        try {
            PackageManager pm          = getApplication().getPackageManager();
            PackageInfo    packageInfo = pm.getPackageInfo(getApplication().getPackageName(), 0);
            int            labelRes    = packageInfo.applicationInfo.labelRes;
            return getApplication().getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 获得当前应用版本名称
     */
    public static String getVersionName () {
        try {
            PackageManager pm          = getApplication().getPackageManager();
            PackageInfo    packageInfo = pm.getPackageInfo(getApplication().getPackageName(), 0);
            //返回版本号
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得当前应用版本名称
     */
    public static int getVersionCode () {
        try {
            PackageManager pm          = getApplication().getPackageManager();
            PackageInfo    packageInfo = pm.getPackageInfo(getApplication().getPackageName(), 0);
            //返回版本号
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取运营商名字
     * getSimOperatorName()直接获取到运营商的名字,,getSimOperator()获取code,例如"46000"为移动
     */
    public static int getOperatorName () {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String simOperator = telephonyManager.getSimOperator();
                return Integer.parseInt(simOperator);
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }

    }

    /**
     * 打印不包括虚拟按键的分辨率、屏幕宽度dpi、最小宽度sw
     */
    public static int getDeviceWidth () {
        DisplayMetrics metric = getApplication().getResources().getDisplayMetrics();
        int            width  = metric.widthPixels;     // 屏幕宽度（像素）
        return width;
    }

    /**
     * 打印不包括虚拟按键的分辨率、屏幕高度dpi、最小宽度sw
     */
    public static int getDeviceHeight () {
        DisplayMetrics metric = getApplication().getResources().getDisplayMetrics();
        int            height = metric.heightPixels;     // 屏幕高度（像素）
        return height;
    }

    /**
     * 打印不包括虚拟按键的分辨率、屏幕密度dpi、最小宽度sw
     */
    public static int getDeviceDensity () {
        DisplayMetrics metric  = getApplication().getResources().getDisplayMetrics();
        float          density = metric.density;     // 屏幕密度（像素）dpi
        return (int) density;
    }

    /**
     * 获取屏幕的尺寸,如5.5尺寸,4.7尺寸
     */
    @SuppressLint ("NewApi")
    public static String getScreenSizeOfDevice2 (Activity activity) {
        try {
            Point point = new Point();
            activity.getWindowManager().getDefaultDisplay().getRealSize(point);
            DisplayMetrics dm           = getApplication().getResources().getDisplayMetrics();
            double         x            = Math.pow(point.x / dm.xdpi, 2);
            double         y            = Math.pow(point.y / dm.ydpi, 2);
            double         screenInches = Math.sqrt(x + y);
            //保留一位小数
            BigDecimal bg = new BigDecimal(screenInches).setScale(1, RoundingMode.UP);
            //BigDecimal bg2 = new BigDecimal(screenInches).setScale(2, RoundingMode.UP);
            //LogUtils.e("获取的原始屏幕尺寸=" + screenInches + "||计算屏幕尺寸=" + bg + "||计算屏幕尺寸=" + bg2);
            return String.valueOf(bg.doubleValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @SuppressLint ("NewApi")
    public static String getScreenSizeOfDevice () {
        try {
            DisplayMetrics dm           = new DisplayMetrics();
            int            width        = dm.widthPixels;
            int            height       = dm.heightPixels;
            int            dens         = dm.densityDpi;
            double         wi           = (double) width / (double) dens;
            double         hi           = (double) height / (double) dens;
            double         x            = Math.pow(wi, 2);
            double         y            = Math.pow(hi, 2);
            double         screenInches = Math.sqrt(x + y);
            return screenInches + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取当前进程名
     */
    public static String getProcessName (Context cxt, int pid) {
        ActivityManager                             am          = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    /**
     * 获取ua信息
     */
    public static String getUserAgent (Context context) {
        //api 19 之前
        if (Build.VERSION.SDK_INT < 19) {
            WebView webview = new WebView(context);

            // 得到WebSettings对象
            WebSettings settings = webview.getSettings();

            // 设置支持JavaScript
            settings.setJavaScriptEnabled(true);

            return settings.getUserAgentString();
        }

        //api >=19
        return WebSettings.getDefaultUserAgent(context);

    }

    public static int getScreenWidth (Context context) {
        int screenWith = -1;
        try {
            screenWith = context.getResources().getDisplayMetrics().widthPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenWith;
    }

    public static int getScreenHeight (Context context) {
        int screenHeight = -1;
        try {
            screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenHeight;
    }

    /**
     * 获取真实的高度,全面屏水滴屏需要
     *
     * @param context
     * @return
     */
    public static int getRealyScreenHeight (Context context) {
        int screenHeight = -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                //新的获取屏幕高度的方法
                WindowManager wm    = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Point         point = new Point();
                if (wm != null) {
                    wm.getDefaultDisplay().getRealSize(point);
                    screenHeight = point.y;
                    //LogUtils.e("换一种方法获取的屏幕高度=" + screenHeight);
                } else {
                    //原来获取屏幕高度的方法
                    screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                }

            } else {
                //原来获取屏幕高度的方法
                screenHeight = context.getResources().getDisplayMetrics().heightPixels;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return screenHeight;
    }


    /**
     * dp转px
     */
    public static int dip2px (Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density; //屏幕密度
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转dp
     */
    public static int px2dip (Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density; //屏幕密度
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px转换为sp
     */
    public static int px2sp (Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    /**
     * 将sp转换为px
     */
    public static int sp2px (Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取通知权限
     */
    public static boolean checkNotifySetting (Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19(4.4)，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知,目前暂时没有办法获取19以下的系统是否开启了某个App的通知显示权限。
        return manager.areNotificationsEnabled();
    }

    /**
     * 打开设置通知权限页面
     */
    public static void startNotificationSetting (Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else {
            toPermissionSetting(context);
        }
    }

    /**
     * 跳转到权限设置
     */
    public static void toPermissionSetting (Context activity) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            toSystemConfig(activity);
        } else {
            try {
                toApplicationInfo(activity);
            } catch (Exception e) {
                e.printStackTrace();
                toSystemConfig(activity);
            }
        }
    }

    /**
     * 应用信息界面
     */
    public static void toApplicationInfo (Context activity) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(localIntent);
    }

    /**
     * 系统设置界面
     */
    public static void toSystemConfig (Context activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * 陀螺仪参数获取
//     */
//    public static void getSensorManagerInfo (Context context, final SensorManagerCallback callBack) {
//        // 取传感器
//        final SensorManager sensorManager =
//                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//        final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);//选取传感器,陀螺仪
//        sensorManager.registerListener(new SensorEventListener() {
//            @Override
//            public void onSensorChanged (SensorEvent event) {
//                float x = event.values[0];
//                float y = event.values[1];
//                float z = event.values[2];
//                //LogUtils.e("陀螺仪返回X=" + x + "|y=" + y + "|z=" + z);
//                if (callBack != null) {
//                    callBack.getInfo(x, y, z);
//                }
//                sensorManager.unregisterListener(this, sensor);
//            }
//
//            @Override
//            public void onAccuracyChanged (Sensor sensor, int accuracy) {
//
//            }
//        }, sensor, sensorManager.SENSOR_DELAY_NORMAL);//设置监听器
//    }

    /**
     * 获取app当前的渠道号或application中指定的meta-data
     *
     * @return 如果没有获取成功(没有对应值，或者异常)，则返回值为空
     */
    public static String getAppMetaData(Context context, String key) {
        if (context == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String channelNumber = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                    if (applicationInfo.metaData != null) {
                        channelNumber = applicationInfo.metaData.getString(key);
                    }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return channelNumber;
    }

    /**
     * ping代理地址
     */
    public static boolean pingProxy(String ip) {
        try {
            if (!TextUtils.isEmpty(ip)) {
                //ping -c 3 -w 10——>代表ping 3 次 超时时间为10秒
                Process p = Runtime.getRuntime().exec("ping -c 1 -w 1 " + ip);
                return p.waitFor() == 0;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
