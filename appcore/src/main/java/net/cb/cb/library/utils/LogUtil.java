package net.cb.cb.library.utils;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private static LogUtil log;
    private static boolean isOpen = true;
    private static int LOG_MAXLENGTH = 2000;

    public void init(boolean open) {
        isOpen = open;
        if (isOpen) {
            Log.i("Log", "=================调试日志:开启================");
        }
        createDir(FileConfig.PATH_LOG);
    }

    private void sp(String TAG, String msg, int state) {
        if (!isOpen)
            return;
        if (TAG == null || TAG.length() < 1) {
            TAG = "log";
        }
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAXLENGTH;
        for (int i = 0; i < 100; i++) {
            //剩下的文本还是大于规定长度则继续重复截取并输出
            if (strLength > end) {
                p(TAG + i, msg.substring(start, end), state);

                start = end;
                end = end + LOG_MAXLENGTH;
            } else {

                p(TAG, msg.substring(start, strLength), state);
                break;
            }
        }
    }

    private void p(String TAG, String msg, int state) {
        TAG = "a===" + TAG;
        switch (state) {
            case 0:
                Log.i(TAG, msg);
                break;
            case 1:
                Log.d(TAG, msg);
                break;
            case 2:
                Log.e(TAG, msg);
                break;
        }

    }

    public void e(String tag, String msg) {
        sp(tag, msg, 2);
    }

    public void e(String msg) {
        sp("=", msg, 2);
    }

    public void d(String tag, String msg) {
        sp(tag, msg, 1);
    }

    public void i(String tag, String msg) {
        sp(tag, msg, 0);
    }

    public static LogUtil getLog() {
        log = log == null ? new LogUtil() : log;

        return log;
    }

    /**
     * 写入错误日志
     *
     * @param ex
     */
    public static void writeError(Throwable ex) {
        try {
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy_MM_dd");
            SimpleDateFormat momentFormat = new SimpleDateFormat("HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
            String day = dayFormat.format(curDate);
            String moment = momentFormat.format(curDate);
            StringBuffer timeDivider = new StringBuffer();
            StringBuffer overDivider = new StringBuffer();
            for (int i = 0; i < 20; i++) {
                timeDivider.append("-");
                overDivider.append("=");
            }
            StringBuffer sb = new StringBuffer();
            StackTraceElement[] element = ex.getCause().getStackTrace();
            sb.append(moment + "\n");
            sb.append(timeDivider.toString() + "\n");
            sb.append(ex.getMessage() + "\n");
            for (int i = 0; i < element.length; i++) {
                sb.append(element[i].toString() + "\n");
            }
            sb.append(overDivider.toString() + "\n");
            File file = new File(FileConfig.PATH_LOG + "err" + day);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream ops = new FileOutputStream(file, true);
            ops.write(sb.toString().getBytes());
            ops.flush();
            ops.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入日志
     *
     * @param value
     */
    public synchronized static void writeLog(final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy_MM_dd");
                    SimpleDateFormat momentFormat = new SimpleDateFormat("HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                    String day = dayFormat.format(curDate);
                    String moment = momentFormat.format(curDate);
                    StringBuffer sb = new StringBuffer();
                    sb.append(moment + "  " + value + "\n");
                    File file = new File(FileConfig.PATH_LOG + "log_" + day+".txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream ops = new FileOutputStream(file, true);
                    ops.write(sb.toString().getBytes());
                    ops.flush();
                    ops.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 写入红包日志
     *
     * @param value
     */
    public synchronized static void writeEnvelopeLog(final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy_MM_dd");
                    SimpleDateFormat momentFormat = new SimpleDateFormat("yyyy_MM_dd HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                    String day = dayFormat.format(curDate);
                    String moment = momentFormat.format(curDate);
                    StringBuffer sb = new StringBuffer();
                    sb.append(moment + "  " + value + "\n");
                    File file = new File(FileConfig.PATH_LOG + "envelope" +".txt");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream ops = new FileOutputStream(file, true);
                    ops.write(sb.toString().getBytes());
                    ops.flush();
                    ops.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static String createDir(String dirPath) {
        //因为文件夹可能有多层，比如:  a/b/c/ff.txt  需要先创建a文件夹，然后b文件夹然后...
        try {
            File file = new File(dirPath);
            if (file.getParentFile().exists()) {
                file.mkdir();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());
                file.mkdir();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dirPath;
    }

}
