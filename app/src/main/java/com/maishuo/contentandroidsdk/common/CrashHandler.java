package com.maishuo.contentandroidsdk.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.maishuo.contentandroidsdk.utils.SysSDCardCacheDir;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrashHandler implements UncaughtExceptionHandler {

    private static final String TAG = CrashHandler.class.getSimpleName();
    private Context mContext;
    // 系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;
    // 异常处理实例
    private static CrashHandler handlerInstance;
    // 错误报告文件的扩展名
    private static final String CRASH_REPORTER_EXTENSION = ".txt";
    private HashMap<String, String> infos = new HashMap<String, String>();

    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    /**
     * 构造方法私有化
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     *
     * @return
     */
    public static CrashHandler getInstance() {
        if (handlerInstance == null) {
            handlerInstance = new CrashHandler();
        }
        return handlerInstance;
    }

    /**
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器,
     * 设置该CustomExceptionHandler为程序的默认处理器
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // Sleep一会后结束程序
            // 来让线程停止一会是为了显示Toast信息给用户，然后Kill程序
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error : ", e);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return true;
        }
        final String msg = ex.getLocalizedMessage();
        // 收集设备信息
        collectDeviceInfo(mContext);
        saveCrashInfoToFile(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return
     */
    private String saveCrashInfoToFile(Throwable ex) {
        ex.printStackTrace();
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String filePath = "";
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String loggerDivider = "----------ERROR----" + time + "-" + timestamp + "--------------";
            String fileName = "crash" + time + CRASH_REPORTER_EXTENSION;

            // 保存文件
            if (isHavedSDcard()) {
                // 如果存在SD卡，就使用SD卡
                filePath = SysSDCardCacheDir.getLogDir().getAbsolutePath();
            } else {
                // 如果没有SD卡，就使用内部存储
                filePath = Environment.getDownloadCacheDirectory().toString() + "/log";
            }
            File targetFile = new File(filePath, fileName);// 指定文件存储目录为SD卡，文件名
            if (!targetFile.exists()) {
                File dir = new File(targetFile.getParent());
                dir.mkdirs();
                targetFile.createNewFile();
            }
            // 流对象
            RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
            raf.seek(targetFile.length());
            raf.write(loggerDivider.getBytes());
            raf.write(sb.toString().getBytes());
            raf.close();
            Log.e(TAG, "错误日志写入文件成功");
            Log.e(TAG, "文件名为：" + fileName);
            Log.e(TAG, "文件路径为：" + targetFile.getAbsolutePath());
            String value = String.format("错误日志输入成功,目录为:%s", targetFile.getAbsolutePath());
            Toast.makeText(AppApplication.Companion.getInstance(), value, Toast.LENGTH_LONG).show();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 判断SD卡是否存在
    public boolean isHavedSDcard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
}
