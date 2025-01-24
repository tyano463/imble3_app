/**
 * ログ出力に関する処理
 */
package com.yano.interplan.imble3_app;

import android.util.Log;

import java.util.Optional;

/**
 * ログ出力クラス
 */
public class DLog {
    /**
     * logcat の tag
     */
    private static final String TAG = "IMBLE3";
    /* 16進数文字列出力用 */
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    /* シングルトン用 */
    private static DLog instance = null;

    /**
     * コンストラクタガード
     */
    private DLog() {
    }

    /**
     * タグ作成用
     *
     * @param packageName such as "jp.co.interplan.SampleApp"
     * @return such as "SampleApp"
     */
    private static String getAppName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return "";
        }
        String[] parts = packageName.split("\\.");
        return parts[parts.length - 1];
    }

    /**
     * 呼び出し元の取得
     * @return 呼び出し元Stack情報
     */
    public Tuple getCallee() {
        return getCallee(3);
    }

    /**
     * 呼び出し元の取得
     *
     * @param level 呼び出し階層
     * @return 呼び出し元情報文字列
     */
    public Tuple getCallee(int level) {
        StackTraceElement[] ste = new Throwable().getStackTrace();
        String tag = TAG;
        if (ste.length <= level) {
            return new Tuple(tag, "unknown ");
        }
        StackTraceElement elm = ste[level];
        String method = elm.getMethodName();
        tag = elm.getClassName()
                .replaceAll(".*\\.(.*)", "$1")
                .replaceAll("\\$.*", "");
        while (method.indexOf('$') >= 0) {
            method = ste[level++].getMethodName();
        }
        String msg =
                elm.getFileName()
                        + " ("
                        + elm.getLineNumber()
                        + ") "
                        + elm.getMethodName()
                        + " ";
        return new Tuple(tag, msg);
    }

    /**
     * シングルトンインターフェース
     *
     * @return DLogインスタンス
     */
    private static DLog getInstance() {
        if (DLog.instance == null) {
            DLog.instance = new DLog();
        }
        return DLog.instance;
    }

    /**
     * android の logcatにログ出力を行う
     *
     * @param s ログ出力文字列
     */
    public static void dlog(String s) {
        Tuple t = getInstance().getCallee();
        Log.d(t.tag, t.msg + s);
    }

    /**
     * android の logcatにログ出力を行う
     *
     * @param s  ログ出力文字列
     * @param th コールスタック
     */
    public static void dlog(String s, Throwable th) {
        Tuple t = getInstance().getCallee();
        Log.d(t.tag, t.msg + s, th);
    }

    /**
     * バイト配列を１6進文字列に変換
     *
     * @param bytes byte配列
     * @return 16進文字列
     */
    public static String b2s(byte[] bytes) {
        return b2s(bytes, true);
    }

    /**
     * バイト配列を１6進文字列に変換(改行選択)
     *
     * @param bytes byte配列
     * @param lf true: 16byte毎に改行, false: 改行しない
     * @return 16進文字列
     */
    public static String b2s(byte[] bytes, boolean lf) {
        if (bytes == null || bytes.length == 0) return "";

        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            if (lf && (j % 16) == 15) {
                hexChars[j * 3 + 2] = '\n';
            } else {
                hexChars[j * 3 + 2] = ' ';
            }
        }
        return new String(hexChars);
    }

    /**
     * logcat特有のtagとメッセージ用構造体
     */
    public static class Tuple {
        public String tag;
        public String msg;

        public Tuple(String t, String m) {
            this.tag = t;
            this.msg = m;
        }
    }
}