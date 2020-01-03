package com.yanlong.im.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.Result;
import com.luck.picture.lib.tools.DateUtils;
import com.yanlong.im.chat.action.MsgAction;
import com.yanlong.im.chat.bean.Group;
import com.yanlong.im.chat.bean.MemberUser;
import com.yanlong.im.chat.ui.AddGroupActivity;
import com.yanlong.im.chat.ui.ChatActivity;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.ui.MyselfInfoActivity;
import com.yanlong.im.user.ui.UserInfoActivity;

import net.cb.cb.library.bean.EventExitChat;
import net.cb.cb.library.bean.QRCodeBean;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Response;

public class QRCodeManage {
    public static final String TAG = "QRCodeManage";
    //YLIM://ADDFRIEND?id=xxx
    //YLIM://ADDGROUP?id=xxx
    public static final String HEAD = "YLIM:"; //二维码头部
    public static final String ID = "id"; //群id
    public static final String UID = "uid"; //用户ID
    public static final String TIME = "time"; //时间戳
    public static final String NICK_NAME = "nickname";

    public static final String ADD_FRIEND_FUNCHTION = "ADDFRIEND"; //添加好友
    public static final String ADD_GROUP_FUNCHTION = "ADDGROUP"; //添加群

    public static final String DOWNLOAD_APP_URL = "https://www.zln365.com"; //下载地址

    /**
     * 扫描二维码转换bean
     *
     * @param QRCode 二维码字符串
     * @return 二维码bean
     */
    public static QRCodeBean getQRCodeBean(Context context, String QRCode) {
        QRCodeBean bean = null;
        LogUtil.getLog().e(TAG, "二维码" + QRCode);
        if (!TextUtils.isEmpty(QRCode)) {
            String oneStrs[] = QRCode.split("//");
            if (oneStrs == null || oneStrs.length > 2) {
                ToastUtil.show(context, "错误二维码");
            } else {
                if (!oneStrs[0].contains(HEAD)) {
                    ToastUtil.show(context, "错误二维码");
                } else {
                    bean = new QRCodeBean();
                    bean.setHead(oneStrs[0]);
                    String twoStrs[] = oneStrs[1].split("\\?");
                    if (twoStrs != null && twoStrs.length >= 2) {
                        bean.setFunction(twoStrs[0]);
                        String threeStrs[] = twoStrs[1].split("&");
                        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
                        if (threeStrs != null && threeStrs.length > 0) {
                            for (int i = 0; i < threeStrs.length; i++) {
                                String fourStrs[] = threeStrs[i].split("=");
                                parameters.put(fourStrs[0], fourStrs[1]);
                            }
                        }
                        bean.setParameter(parameters);
                    }
                }
            }
        }
        return bean;
    }

    /**
     * bean 转二维码
     *
     * @param bean 二维码bean
     * @return 二维码
     */
    public static String getQRcodeStr(QRCodeBean bean) {
        StringBuffer code = new StringBuffer();
        if (bean != null) {
            code.append(bean.getHead() + "//" + bean.getFunction() + "?");
            if (bean.getParameter() != null && bean.getParameter().size() > 0) {
                for (Map.Entry<String, String> value : bean.getParameter().entrySet()) {
                    code.append(value.getKey() + "=" + value.getValue() + "&");
                }
                code.delete(code.length() - 1, code.length());
            }
        }
        return code.toString();
    }


    /**
     * 公用二维码跳转功能管理
     */
    public static void goToActivity(final Activity activity, QRCodeBean bean) {
        if (bean != null) {
            if (bean.getFunction().equals(ADD_FRIEND_FUNCHTION)) {
                if (!TextUtils.isEmpty(bean.getParameterValue(ID))) {
                    Long uid = UserAction.getMyInfo().getUid();
                    if (bean.getParameterValue(ID).equals(uid + "")) {
                        Intent intent = new Intent(activity, MyselfInfoActivity.class);
                        activity.startActivity(intent);
                    } else {
                        Intent intent = new Intent(activity, UserInfoActivity.class);
                        intent.putExtra(UserInfoActivity.ID, Long.valueOf(bean.getParameterValue(ID)));
                        activity.startActivity(intent);
                    }
                }
            } else if (bean.getFunction().equals(ADD_GROUP_FUNCHTION)) {
                LogUtil.getLog().e(TAG, "time------->" + DateUtils.timeStamp2Date(Long.valueOf(bean.getParameterValue(TIME)), null));
                if (DateUtils.isPastDue(Long.valueOf(bean.getParameterValue(TIME)))) {
                    ToastUtil.show(activity, "二维码已过期");
                } else {
                    if (!TextUtils.isEmpty(bean.getParameterValue(ID)) && !TextUtils.isEmpty(bean.getParameterValue(UID))) {
                        taskGroupInfo(bean.getParameterValue(ID), bean.getParameterValue(UID), bean.getParameterValue(NICK_NAME), activity);
                    }
                }
            }
        }
    }


    private static void taskGroupInfo(final String gid, final String inviter, final String inviterName, final Activity activity) {
        new MsgAction().groupInfo(gid, new CallBack<ReturnBean<Group>>() {
            @Override
            public void onResponse(Call<ReturnBean<Group>> call, Response<ReturnBean<Group>> response) {

                if (response.body() != null) {
                    if (response.body().isOk()) {
                        boolean isNot = false;
                        Group bean = response.body().getData();
                        RealmList<MemberUser> users = bean.getUsers();
                        UserInfo userInfo = new UserAction().getMyInfo();
                        for (MemberUser user : users) {
                            if (userInfo.getUid().longValue() == user.getUid()) {
                                isNot = true;
                            }
                        }

                        if (!isNot) {
                            toAddGourp(gid, inviter, inviterName, activity);
                        } else {
                            EventBus.getDefault().post(new EventExitChat());
                            Intent intent = new Intent(activity, ChatActivity.class);
                            intent.putExtra(ChatActivity.AGM_TOGID, gid);
                            activity.startActivity(intent);
                        }

                    } else {
                        toAddGourp(gid, inviter, inviterName, activity);
                    }
                } else {
                    toAddGourp(gid, inviter, inviterName, activity);
                }
            }
        });
    }

    private static void toAddGourp(String gid, String inviter, String inviterName, Activity activity) {
        Intent intent = new Intent(activity, AddGroupActivity.class);
        intent.putExtra(AddGroupActivity.INVITER, inviter);
        intent.putExtra(AddGroupActivity.GID, gid);
        intent.putExtra(AddGroupActivity.INVITER_NAME, inviterName);
        activity.startActivity(intent);
    }


    public static void toZhifubao(Context mContext, Result result) {
        if (result == null) {
            ToastUtil.show(mContext, "识别二维码失败");
        } else {
            String text = result.getText();
            if (text.contains("qr.alipay.com") || text.contains("QR.ALIPAY.COM")) {
                openAliPay2Pay(mContext, text);
            } else if (text.contains(DOWNLOAD_APP_URL)) {
                openUri(mContext,text);
            } else {
                QRCodeBean bean = QRCodeManage.getQRCodeBean(mContext, text);
                QRCodeManage.goToActivity((Activity) mContext, bean);
            }

        }
    }


    public static void goToPage(Context mContext, String result) {
        if (result == null) {
            ToastUtil.show(mContext, "识别二维码失败");
        } else {
            if (result.contains("qr.alipay.com") || result.contains("QR.ALIPAY.COM")) {
                openAliPay2Pay(mContext, result);
            } else if (result.contains(DOWNLOAD_APP_URL)) {
                openUri(mContext,result);
            } else {
                QRCodeBean bean = QRCodeManage.getQRCodeBean(mContext, result);
                QRCodeManage.goToActivity((Activity) mContext, bean);
            }
        }
    }


    //判断是否安装支付宝
    private static void openAliPay2Pay(Context mContext, String qrCode) {
        if (openAlipayPayPage(mContext, qrCode)) {

        } else {
            ToastUtil.show(mContext, "请安装支付宝");
        }
    }

    //打开支付宝
    public static boolean openAlipayPayPage(Context context, String qrcode) {
        try {
            qrcode = URLEncoder.encode(qrcode, "utf-8");
        } catch (Exception e) {
        }
        try {
            final String alipayqr = "alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + qrcode;
            openUri(context, alipayqr + "%3F_s%3Dweb-other&_t=" + System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private static void openUri(Context context, String s) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
        context.startActivity(intent);
    }


    public static String getTime(int distanceDay) {
        String time = "";
        Date date = new Date(System.currentTimeMillis());
        String changeTime = DateUtils.getOldDateByDay(date, distanceDay, "yyyy-MM-dd HH:mm:ss");
        time = DateUtils.date2TimeStamp(changeTime, "yyyy-MM-dd HH:mm:ss");
        LogUtil.getLog().e(TAG, "生成时间戳------>" + time);
        return time;
    }


}
