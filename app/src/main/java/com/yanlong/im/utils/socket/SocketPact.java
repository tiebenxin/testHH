package com.yanlong.im.utils.socket;

import java.util.ArrayList;
import java.util.List;

/***
 * 协议的定义
 */
public class SocketPact {
    //包头2位
    private static final byte[] P_HEAD = {0x20, 0x19};
    //长度2位
    //   private byte[] p_length = new byte[2];
    //校验位4位(未使用)
    private static byte[] P_CHECK = new byte[4];
    //版本2位,第一字节为大版本,第二位小版本
    private static byte[] P_VERSION = {0x01, 0x00};
    //类型2位
    private static byte[] P_TYPE = new byte[2];


    //数据类型枚举
    public enum DataType {
        PROTOBUF_MSG, PROTOBUF_HEARTBEAT, AUTH, ACK, OTHER;
    }

    public static byte[] getPakage(DataType type, byte[] context) {

        //内容长度
        int contextSize = context == null ? 0 : context.length;

        //长度后面的包长
        byte[] d_length = intTobyte2(P_CHECK.length + P_VERSION.length + P_TYPE.length + contextSize);

        //类型
        byte[] d_type = new byte[2];

        switch (type) {
            case PROTOBUF_MSG://普通消息
                d_type = new byte[]{0x00, tobyte(1, 0)};
                break;
            case PROTOBUF_HEARTBEAT://心跳
                d_type = new byte[]{0x00, tobyte(1, 1)};
                break;
            case AUTH://鉴权
                d_type = new byte[]{0x00, tobyte(1, 2)};
                break;
            case ACK://回馈
                d_type = new byte[]{0x00, tobyte(1, 3)};
                break;

        }

        //包大小
        int d_size = P_HEAD.length + d_length.length + P_CHECK.length + P_VERSION.length + d_type.length + contextSize;

        byte[] rtData = new byte[d_size];
        System.arraycopy(P_HEAD, 0, rtData, 0, 2);
        System.arraycopy(d_length, 0, rtData, 2, 2);
        System.arraycopy(P_CHECK, 0, rtData, 4, 4);
        System.arraycopy(P_VERSION, 0, rtData, 8, 2);
        System.arraycopy(d_type, 0, rtData, 10, 2);
        if (context != null) {
            System.arraycopy(context, 0, rtData, 12, contextSize);
        }


        return rtData;
    }

    /***
     * 时候是包头
     * @param data
     * @return
     */
    public static boolean isHead(byte[] data) {
        if (data == null || data.length < 2)
            return false;

/*        byte[] d = new byte[2];
        d[0] = data[0];
        d[1] = data[1];*/

        return P_HEAD[0] == data[0] && P_HEAD[1] == data[1];
    }


    /***
     * 获取长度
     * @return
     */
    public static int getLength(byte[] data) {
        byte[] d = new byte[2];
        d[0] = data[2];
        d[1] = data[3];

        return byte2Toint(d);
    }

    /***
     * 获取消息类型
     * @return
     */
    public static DataType getType(byte[] data) {
        if (data.length >= 12) {
            byte[] d = new byte[2];
            d[0] = data[10];//暂时不用
            d[1] = data[11];

            int h = byteH4(d[1]);
            int l = byteL4(d[1]);

            if (h == 1 && l == 0) {
                return DataType.PROTOBUF_MSG;
            } else if (h == 1 && l == 1) {

                return DataType.PROTOBUF_HEARTBEAT;
            } else if (h == 1 && l == 3) {
                return DataType.ACK;
            } else if (h == 1 && l == 2) {
                return DataType.AUTH;
            }
        }


        return DataType.OTHER;


    }


    //------------------------转换工具-------------------

    /***
     * 合并数组
     * @param values
     * @return
     */
    public static byte[] listToBytes(List<byte[]> values) {
        int length_byte = 0;
        for (int i = 0; i < values.size(); i++) {
            length_byte += values.get(i).length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.size(); i++) {
            byte[] b = values.get(i);
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    /***
     * 拆分数组
     * @return
     */
    public static List<byte[]> bytesToLists(byte[] data, int... sp_length) {
        List<byte[]> list = new ArrayList<>();
        int i = 0;
        for (int l : sp_length) {
            byte[] t = new byte[l];
            System.arraycopy(data, i, t, 0, t.length);
            list.add(t);
            i = l;
        }

        int exl = data.length - i;
        byte[] ex = new byte[exl];
        System.arraycopy(data, i, ex, 0, exl);
        if (ex.length > 0) {
            list.add(ex);
        }

        return list;
    }

    /***
     * 合并数组
     * @param values
     * @return
     */
    public static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (int i = 0; i < values.length; i++) {
            length_byte += values[i].length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (int i = 0; i < values.length; i++) {
            byte[] b = values[i];
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }


    /***
     * int转为2byte
     * @param val
     * @return
     */
    private static byte[] intTobyte2(int val) {
        byte[] data = new byte[2];
        data[0] = (byte) ((val >> 8) & 0xff);
        data[1] = (byte) (val & 0xff);
        return data;
    }

    /***
     * 2byt转为int,读取长度
     * @param data
     * @return
     */
    public static int byte2Toint(byte[] data) {
        int i = ((data[0] << 8) & 0x0000ff00) | (data[1] & 0x000000ff);
        return i;

    }


    /***
     * 合并字节
     * @param h
     * @param l
     * @return
     */
    private static byte tobyte(int h, int l) {
        return (byte) ((h << 4) & 0xf0 | (l & 0x0f));
    }

    //高4位
    public static int byteH4(byte bt) {
        int val = (bt & 0xf0) >> 4;
        return val;
    }

    //低4位
    public static int byteL4(byte bt) {
        int val = bt & 0x0f;
        return val;
    }

    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append("" + hex + " ");
        }
        return sb.toString();
    }
}
