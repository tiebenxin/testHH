package net.cb.cb.library.utils;

import android.os.Environment;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-20
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class FileConfig {
// 公用文件路径
    /** 应用根目录 */
    public static final String PATH_BASE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/yanlong/";
    /** 应用Log日志 */
    public static final String PATH_LOG = PATH_BASE + "Log/";
}
