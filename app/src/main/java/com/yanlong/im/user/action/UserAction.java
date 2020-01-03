package com.yanlong.im.user.action;

import android.content.Context;
import android.text.TextUtils;

import com.example.nim_lib.config.Preferences;
import com.example.nim_lib.controll.AVChatProfile;
import com.hm.cxpay.global.PayEnvironment;
import com.jrmf360.rplib.JrmfRpClient;
import com.jrmf360.rplib.http.model.BaseModel;
import com.jrmf360.tools.http.OkHttpModelCallBack;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.ApplyBean;
import com.yanlong.im.chat.bean.SingleMeberInfoBean;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.pay.action.PayAction;
import com.yanlong.im.pay.bean.SignatureBean;
import com.yanlong.im.user.bean.FriendInfoBean;
import com.yanlong.im.user.bean.IdCardBean;
import com.yanlong.im.user.bean.NewVersionBean;
import com.yanlong.im.user.bean.TokenBean;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.user.server.UserServer;
import com.yanlong.im.utils.DaoUtil;

import net.cb.cb.library.AppConfig;
import net.cb.cb.library.CoreEnum;
import net.cb.cb.library.bean.OnlineBean;
import net.cb.cb.library.bean.ReturnBean;
import net.cb.cb.library.utils.CallBack;
import net.cb.cb.library.utils.Installation;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.NetIntrtceptor;
import net.cb.cb.library.utils.NetUtil;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.SpUtil;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.utils.VersionUtil;
import net.cb.cb.library.utils.encrypt.AESEncrypt;
import net.cb.cb.library.utils.encrypt.MD5;

import java.util.List;

import cn.jpush.android.api.JPushInterface;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/***
 * @author jyj
 * @date 2016/12/20
 */
public class UserAction {
    private UserServer server;
    private UserDao dao = new UserDao();
    private static UserInfo myInfo;

    public UserAction() {
        server = NetUtil.getNet().create(UserServer.class);
    }
    //以下是演示
    /*public void login( Long phone, String pwd,CallBack<ReturnBean<TokenBean>> callback) {

        LoginBean bean = new LoginBean();
        bean.setPassword(pwd);
        bean.setPhone(phone);
        NetUtil.getNet().exec(server.login(bean), callback);
    }*/

    /***
     * 获取我的信息
     * @return
     */
    public static UserInfo getMyInfo() {
        if (myInfo == null) {
            myInfo = new UserDao().myInfo();
        }
        return myInfo;
    }


    /***
     * 获取个人id
     * @return
     */
    public static Long getMyId() {
        if (getMyInfo() == null) {
            return -1l;// 处理 intValue、longValue 空指针问题
        }
        return getMyInfo().getUid();
    }


    /***
     * 获取设备id
     * @param context
     * @return
     */
    public static String getDevId(Context context) {
        String uid = JPushInterface.getRegistrationID(context);
        if (TextUtils.isEmpty(uid)) {
            uid = Installation.id(context);
            new SharedPreferencesUtil(SharedPreferencesUtil.SPName.DEV_ID).save2Json(uid);
            return uid;
        }
        LogUtil.getLog().i("getDevId", uid + "");
        return uid;
    }

  /*  public void getDevId(EventDevID event){

        int reTime = 0;
        String uid = null;
        try {
            while (reTime < 5*10) {
                uid = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.DEV_ID).get4Json(String.class);
                if (uid != null) {
                    event.onDevId(uid);
                    break;
                } else {
                    LogUtil.getLog().i("youmeng", "等待DevId"+reTime);
                    Thread.sleep(200);
                }
                reTime++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/


    public void updateUserinfo2DB(UserInfo userInfo) {
        userInfo.setuType(1);
        dao.updateUserinfo(userInfo);
    }

    /**
     * 账号密码登录
     */
    public void login(final String phone, String pwd, String devid, final CallBack<ReturnBean<TokenBean>> callback) {

        cleanInfo();
        NetUtil.getNet().exec(server.login(MD5.md5(pwd), phone, devid, "android", VersionUtil.getPhoneModel(), StringUtil.getChannelName(AppConfig.getContext())), new CallBack<ReturnBean<TokenBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<TokenBean>> call, Response<ReturnBean<TokenBean>> response) {
                if (response.body() != null && response.body().isOk() && StringUtil.isNotNull(response.body().getData().getAccessToken())) {//保存token
                    if (response.body().getData() != null) {
                        doNeteaseLogin(response.body().getData().getNeteaseAccid(), response.body().getData().getNeteaseToken());
                        saveNeteaseAccid(response.body().getData().getNeteaseAccid(), response.body().getData().getNeteaseToken());
                    }
                    initDB("" + response.body().getData().getUid());
                    setToken(response.body().getData());
                    //如果是手机号码登录，则删除上次常信号登陆的账号
                    new SharedPreferencesUtil(SharedPreferencesUtil.SPName.IM_ID).save2Json("");
                    getMyInfo4Web(response.body().getData().getUid(), "");
                }

                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ReturnBean<TokenBean>> call, Throwable t) {
                super.onFailure(call, t);
                callback.onFailure(call, t);
            }
        });
    }


    /**
     * 常信号密码登录
     */
    public void login4Imid(final String imid, String pwd, String devid, final CallBack<ReturnBean<TokenBean>> callback) {

        cleanInfo();
        NetUtil.getNet().exec(server.login4Imid(MD5.md5(pwd), imid, devid, "android", VersionUtil.getPhoneModel(), StringUtil.getChannelName(AppConfig.getContext())), new CallBack<ReturnBean<TokenBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<TokenBean>> call, Response<ReturnBean<TokenBean>> response) {
                if (response.body() != null && response.body().isOk() && StringUtil.isNotNull(response.body().getData().getAccessToken())) {//保存token
                    if (response.body().getData() != null) {
                        doNeteaseLogin(response.body().getData().getNeteaseAccid(), response.body().getData().getNeteaseToken());
                        saveNeteaseAccid(response.body().getData().getNeteaseAccid(), response.body().getData().getNeteaseToken());
                    }

                    initDB("" + response.body().getData().getUid());
                    setToken(response.body().getData());
                    getMyInfo4Web(response.body().getData().getUid(), imid);
                }

                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ReturnBean<TokenBean>> call, Throwable t) {
                super.onFailure(call, t);
                callback.onFailure(call, t);
            }
        });
    }


    /***
     * 拉取服务器的自己的信息到数据库
     */
    private void getMyInfo4Web(Long usrid, String imid) {
        NetUtil.getNet().exec(server.getUserInfo(usrid), new CallBack<ReturnBean<UserInfo>>() {
            @Override
            public void onResponse(Call<ReturnBean<UserInfo>> call, Response<ReturnBean<UserInfo>> response) {
                if (response.body() != null && response.body().isOk()) {
                    UserInfo userInfo = response.body().getData();
                    new SharedPreferencesUtil(SharedPreferencesUtil.SPName.IMAGE_HEAD).save2Json(userInfo.getHead() + "");
                    //保存手机或常信号登录
                    if (StringUtil.isNotNull(imid)) {
                        new SharedPreferencesUtil(SharedPreferencesUtil.SPName.IM_ID).save2Json(imid);
                    }
                    new SharedPreferencesUtil(SharedPreferencesUtil.SPName.PHONE).save2Json(userInfo.getPhone());
                    new SharedPreferencesUtil(SharedPreferencesUtil.SPName.UID).save2Json(userInfo.getUid());
                    userInfo.toTag();
                    if (!TextUtils.isEmpty(userInfo.getBankReqSignKey())) {
                        String key = userInfo.getBankReqSignKey();
                        String result = AESEncrypt.encrypt(key);
//                        String s = AESEncrypt.decrypt(result);
                        userInfo.setBankReqSignKey(result);
                    }
                    updateUserinfo2DB(userInfo);
                    MessageManager.getInstance().notifyRefreshUser(userInfo);
                }
            }
        });

    }

    /**
     * 获取用户信息
     */
    public void getUserInfo4Id(Long usrid, final CallBack<ReturnBean<UserInfo>> callBack) {
        NetUtil.getNet().exec(server.getUserInfo(usrid), new CallBack<ReturnBean<UserInfo>>() {
            @Override
            public void onResponse(Call<ReturnBean<UserInfo>> call, Response<ReturnBean<UserInfo>> response) {
                super.onResponse(call, response);
                //写入用户信息到数据库
                if (response.body() != null) {
                    UserInfo userInfo = response.body().getData();
                    if (userInfo != null && userInfo.getUid() != null) {
                        UserInfo local = dao.findUserInfo(usrid);
                        if (local == null) {
                            dao.updateUserinfo(userInfo);
                        }
                        boolean hasChange = MessageManager.getInstance().updateUserAvatarAndNick(userInfo.getUid(), userInfo.getHead(), userInfo.getName());
                        if (hasChange) {
                            MessageManager.getInstance().notifyRefreshFriend(true, userInfo.getUid(), CoreEnum.ERosterAction.UPDATE_INFO);
                        }
                        callBack.onResponse(call, response);
                    } else {
                        callBack.onFailure(call, new Throwable());
                    }
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<UserInfo>> call, Throwable t) {
                super.onFailure(call, t);
                callBack.onFailure(call, t);
            }
        });
    }

    /***
     * 获取单个用户信息并且缓存到数据库
     * @param usrid
     */
    public void getUserInfoAndSave(Long usrid, @ChatEnum.EUserType final int type, final CallBack<ReturnBean<UserInfo>> cb) {
        NetUtil.getNet().exec(server.getUserInfo(usrid), new CallBack<ReturnBean<UserInfo>>() {
            @Override
            public void onResponse(Call<ReturnBean<UserInfo>> call, Response<ReturnBean<UserInfo>> response) {
                if (response.body() != null && response.body().isOk()) {
                    UserInfo userInfo = response.body().getData();
                    userInfo.toTag();
                    if (userInfo.getStat() != 0) {//优先设置为好友
                        userInfo.setuType(type);
                    } else {
                        userInfo.setuType(ChatEnum.EUserType.FRIEND);
                    }
                    dao.updateUserinfo(userInfo);
                    MessageManager.getInstance().updateSessionTopAndDisturb("", usrid, userInfo.getIstop(), userInfo.getDisturb());
                    MessageManager.getInstance().updateCacheUser(userInfo);
                    cb.onResponse(call, response);
                } else {
                    cb.onFailure(call, new Throwable());
                    MessageManager.getInstance().removeLoadUids(usrid);
                }
            }

            @Override
            public void onFailure(Call<ReturnBean<UserInfo>> call, Throwable t) {
                super.onFailure(call, t);
                cb.onFailure(call, new Throwable());
                MessageManager.getInstance().removeLoadUids(usrid);
            }
        });

    }

    /***
     * 无网登录
     */
    public void login4tokenNotNet(TokenBean token) {
        initDB("" + token.getUid());
        setToken(token);
//        NetIntrtceptor.headers = Headers.of("X-Access-Token", token.getAccessToken());
//        TokenManager.initToken(token.getAccessToken());
//        PayEnvironment.getInstance().setToken(token.getAccessToken());

    }

    public void login4token(String dev_id, final Callback<ReturnBean<TokenBean>> callback) {
        //判断有没有token信息
        TokenBean token = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.TOKEN).get4Json(TokenBean.class);
        if (token == null || !StringUtil.isNotNull(token.getAccessToken())) {
            callback.onFailure(null, null);
            return;
        }

        //设置token
        setToken(token);
//        NetIntrtceptor.headers = Headers.of("X-Access-Token", token.getAccessToken());
//        TokenManager.initToken(token.getAccessToken());
//        PayEnvironment.getInstance().setToken(token.getAccessToken());
        //或者把token传给后端

        NetUtil.getNet().exec(server.login4token(dev_id, "android"), new CallBack<ReturnBean<TokenBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<TokenBean>> call, Response<ReturnBean<TokenBean>> response) {
                if (response.body() != null && response.body().isOk() && StringUtil.isNotNull(response.body().getData().getAccessToken())) {//保存token
                    initDB("" + response.body().getData().getUid());
                    setToken(response.body().getData());
                    getMyInfo4Web(response.body().getData().getUid(), "");
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, null);
                }


            }

            @Override
            public void onFailure(Call<ReturnBean<TokenBean>> call, Throwable t) {
                super.onFailure(call, t);
                callback.onFailure(call, t);
            }
        });

    }

    /***
     * 登出
     */
    public void loginOut() {
        cleanInfo();
        NetUtil.getNet().exec(server.loginOut("android"), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null) {
                    return;
                }
                LogUtil.getLog().d("logout", response.body().getMsg());
            }
        });
    }

    /***
     * 清理信息
     */
    public void cleanInfo() {
        myInfo = null;
        new SharedPreferencesUtil(SharedPreferencesUtil.SPName.TOKEN).clear();
    }


    /***
     * 应用和保存token,添加到http请求头
     */
    private void setToken(TokenBean token) {
        long validTime = System.currentTimeMillis() + TimeToString.DAY * 7;
        token.setValidTime(validTime);
        new SharedPreferencesUtil(SharedPreferencesUtil.SPName.TOKEN).save2Json(token);
        NetIntrtceptor.headers = Headers.of("X-Access-Token", token.getAccessToken());
        PayEnvironment.getInstance().setToken(token.getAccessToken());
//        TokenManager.initToken(token.getAccessToken());
    }


    /***
     * 配置要使用的DB
     */
    private void initDB(String uuid) {
        LogUtil.getLog().i("dbinfo", ">>>>>>>>>>>>>>>>>>>初始数据库:" + "db_user_" + uuid);
        DaoUtil.get().initConfig("db_user_" + uuid);
    }

    /***
     * 好友添加
     */
    public void friendApply(Long uid, String sayHi, String contactName, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.requestFriend(uid, sayHi, contactName), callback);
    }

    /***
     * 好友同意
     */
    public void friendAgree(Long uid, String contactName, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.acceptFriend(uid, contactName), callback);
    }

    /***
     * 加黑名单
     */
    public void friendBlack(Long uid, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.addBlackList(uid), callback);

    }

    /***
     * 移除黑名单
     */
    public void friendBlackRemove(Long uid, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.removeBlackList(uid), callback);
    }

    /***
     * 删除好友
     */
    public void friendDel(Long uid, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.friendDel(uid), callback);

    }

    /***
     * 好友备注
     */
    public void friendMark(Long uid, String mkn, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.friendMkName(uid, mkn), callback);
    }


    /**
     * 删除待同意好友
     */
    public void delRequestFriend(Long uid, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.delRequestFriend(uid), callback);
    }

    /**
     * 通讯录
     */
    public void friendGet4Me(final CallBack<ReturnBean<List<UserInfo>>> callback) {
        NetUtil.getNet().exec(server.normalFriendsGet(), new CallBack<ReturnBean<List<UserInfo>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<UserInfo>>> call, Response<ReturnBean<List<UserInfo>>> response) {

                if (response.body() == null)
                    return;

                if (response.body().isOk()) {
                    List<UserInfo> list = response.body().getData();
                    //更新库
                    dao.friendMeUpdate(list);

                }
                callback.onResponse(call, response);

            }

            @Override
            public void onFailure(Call<ReturnBean<List<UserInfo>>> call, Throwable t) {
                super.onFailure(call, t);
                callback.onResponse(call, null);
            }
        });
    }

    /***
     * 申请列表
     */
    public void friendGet4Apply(CallBack<ReturnBean<List<ApplyBean>>> callback) {
        NetUtil.getNet().exec(server.requestFriendsGet(), callback);
    }

    /***
     * 黑名单
     */
    public void friendGet4Black(CallBack<ReturnBean<List<UserInfo>>> callback) {
        NetUtil.getNet().exec(server.blackListFriendsGet(), callback);
    }

    /**
     * 获取所有好友列表请求
     */
    public void friendGet4All(CallBack<ReturnBean<List<UserInfo>>> callback) {
        NetUtil.getNet().exec(server.getAllFriendsGet(), callback);
    }


    private PayAction payAction = new PayAction();

    private void upMyinfoToPay() {
        payAction.SignatureBean(new CallBack<ReturnBean<SignatureBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<SignatureBean>> call, Response<ReturnBean<SignatureBean>> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    SignatureBean sign = response.body().getData();
                    String token = sign.getSign();
                    JrmfRpClient.updateUserInfo(myInfo.getUid() + "", token, myInfo.getName(), myInfo.getHead(), new OkHttpModelCallBack<BaseModel>() {
                        @Override
                        public void onSuccess(BaseModel baseModel) {

                        }

                        @Override
                        public void onFail(String s) {

                        }
                    });


                }
            }
        });
    }

    /**
     * 设置用户个人资料
     */
    public void myInfoSet(final String imid, final String avatar, final String nickname, final Integer gender, final CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.userInfoSet(imid, avatar, nickname, gender), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    myInfo = dao.findUserInfo(getMyId());
                    if (!TextUtils.isEmpty(imid))
                        myInfo.setImid(imid);
                    if (!TextUtils.isEmpty(avatar))
                        myInfo.setHead(avatar);
                    if (!TextUtils.isEmpty(nickname))
                        myInfo.setName(nickname);
                    if (gender != null)
                        myInfo.setSex(gender);
                    updateUserinfo2DB(myInfo);
                    upMyinfoToPay();
                }
                callback.onResponse(call, response);
            }
        });
    }

    /**
     * 修改用户组合开关
     */
    public void userMaskSet(Integer switchval, Integer avatar, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.userMaskSet(switchval, avatar), callback);
    }


    /**
     * 获取短信验证码
     *
     * @param businessType 登录login  注册register  修改密码password
     */
    public void smsCaptchaGet(String phone, String businessType, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.smsCaptchaGet(phone, businessType), callback);
    }

    /***
     * 根据key搜索所有的好友
     */
    public List<UserInfo> searchUser4key(String key) {

        return dao.searchUser4key(key);
    }


    /**
     * 用户注册
     */
    public void register(String phone, String captcha, String devid, final CallBack<ReturnBean<TokenBean>> callback) {
        cleanInfo();
        NetUtil.getNet().exec(server.register(phone, captcha, "android", devid, VersionUtil.getPhoneModel(), StringUtil.getChannelName(AppConfig.getContext())), new CallBack<ReturnBean<TokenBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<TokenBean>> call, Response<ReturnBean<TokenBean>> response) {
                super.onResponse(call, response);
                if (response.body() != null && response.body().isOk() && StringUtil.isNotNull(response.body().getData().getAccessToken())) {//保存token
                    initDB("" + response.body().getData().getUid());
                    setToken(response.body().getData());
                    getMyInfo4Web(response.body().getData().getUid(), "");
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ReturnBean<TokenBean>> call, Throwable t) {
                super.onFailure(call, t);
                callback.onFailure(call, t);
            }
        });
    }


    /**
     * 手机号验证码登录
     */
    public void login4Captch(final String phone, String captcha, String devid, final CallBack<ReturnBean<TokenBean>> callback) {
        cleanInfo();
        NetUtil.getNet().exec(server.login4Captch(phone, captcha, "android", devid, VersionUtil.getPhoneModel(), StringUtil.getChannelName(AppConfig.getContext())), new Callback<ReturnBean<TokenBean>>() {
            @Override
            public void onResponse(Call<ReturnBean<TokenBean>> call, Response<ReturnBean<TokenBean>> response) {
                if (response.body() != null && response.body().isOk() && StringUtil.isNotNull(response.body().getData().getAccessToken())) {//保存token
                    initDB("" + response.body().getData().getUid());
                    setToken(response.body().getData());
                    getMyInfo4Web(response.body().getData().getUid(), "");
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ReturnBean<TokenBean>> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }


    /**
     * 根据常信号获取个人资料
     */
    public void getUserInfoByImid(String imid, CallBack<ReturnBean<UserInfo>> callback) {
        NetUtil.getNet().exec(server.getUserInfoByImid(imid), callback);
    }


    /**
     * 根据关键字匹配常信号或手机号获取用户信息
     */
    public void getUserInfoByKeyword(String keyWord, CallBack<ReturnBean<List<UserInfo>>> callback) {
        NetUtil.getNet().exec(server.getUserInfoByKeyword(keyWord), callback);
    }

    /**
     * 修改用户密码
     */
    public void setUserPassword(String newPassword, String oldPassword, CallBack<ReturnBean> callback) {

        NetUtil.getNet().exec(server.setUserPassword(MD5.md5(newPassword), MD5.md5(oldPassword)), callback);
    }

    /**
     * 通讯录匹配
     */
    public void getUserMatchPhone(String phoneList, CallBack<ReturnBean<List<FriendInfoBean>>> callback) {
        NetUtil.getNet().exec(server.getUserMatchPhone(phoneList), callback);
    }

    /**
     * 手机号验证码重置密码
     */
    public void changePasswordBySms(String phone, Integer captcha, String password, CallBack<ReturnBean> callback) {

        NetUtil.getNet().exec(server.changePasswordBySms(phone, captcha, MD5.md5(password)), callback);
    }

    /**
     * 获取认证信息
     */
    public void getIdCardInfo(CallBack<ReturnBean<IdCardBean>> callback) {
        NetUtil.getNet().exec(server.getIdCardInfo(), callback);
    }

    /**
     * 实名认证
     */
    public void realNameAuth(String idNumber, String idType, String name, final CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.realNameAuth(idNumber, idType, name), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    myInfo = dao.findUserInfo(getMyId());
                    myInfo.setAuthStat(1);
                    updateUserinfo2DB(myInfo);
                }
                callback.onResponse(call, response);
            }
        });
    }


    /**
     * 更新职业类别
     */
    public void setJobType(String jobType, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.setJobType(jobType), callback);
    }


    /**
     * 更新证件有效期
     */
    public void setExpiryDate(String expiryDate, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.setExpiryDate(expiryDate), callback);
    }

    /**
     * 更新证件照片
     */
    public void setCardPhoto(String cardBack, String cardFront, final CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.setCardPhoto(cardBack, cardFront), new CallBack<ReturnBean>() {
            @Override
            public void onResponse(Call<ReturnBean> call, Response<ReturnBean> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    myInfo = dao.findUserInfo(getMyId());
                    myInfo.setAuthStat(2);
                    updateUserinfo2DB(myInfo);
                }
                callback.onResponse(call, response);
            }
        });
    }


    /**
     * 版本更新
     */
    public void getNewVersion(String channelName, CallBack<ReturnBean<NewVersionBean>> callback) {
        NetUtil.getNet().exec(server.getNewVersion("android", channelName), callback);
    }

    /*
     * 获取本地用户信息
     * */
    public UserInfo getUserInfoInLocal(Long uid) {
        if (uid == null) {
            return null;
        }
        return dao.findUserInfo(uid);
    }


    /**
     * 初始化用户密码
     */
    public void initUserPassword(String password, CallBack<ReturnBean> callback) {

        NetUtil.getNet().exec(server.initUserPassword(MD5.md5(password)), callback);
    }

    /**
     * 投诉
     */
    public void userComplaint(int complaintType, String illegalDescription, String illegalImage, String respondentGid, String respondentUid, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.userComplaint(complaintType, illegalDescription, illegalImage, respondentGid, respondentUid), callback);
    }


    /**
     * 获取通讯录好友在线状态
     */
    public void getUsersOnlineStatus(final CallBack<ReturnBean<List<OnlineBean>>> callback) {
        NetUtil.getNet().exec(server.getUsersOnlineStatus(), new CallBack<ReturnBean<List<OnlineBean>>>() {
            @Override
            public void onResponse(Call<ReturnBean<List<OnlineBean>>> call, Response<ReturnBean<List<OnlineBean>>> response) {
                if (response.body() == null)
                    return;
                if (response.body().isOk()) {
                    List<OnlineBean> list = response.body().getData();
                    dao.updateUsersOnlineStatus(list);
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ReturnBean<List<OnlineBean>>> call, Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    /**
     * 初始化用户意见反馈
     */
    public void userOpinion(String opinionDescription, String opinionImage, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.userOpinion(opinionDescription, opinionImage), callback);
    }

    /**
     * 保存网易云登录账号与Token，用于在网易初始化的时候自动登录
     *
     * @param neteaseAccid
     * @param neteaseToken
     */
    private void saveNeteaseAccid(String neteaseAccid, String neteaseToken) {
        SpUtil spUtil = SpUtil.getSpUtil();
        spUtil.putSPValue(Preferences.KEY_USER_ACCOUNT, neteaseAccid);
        spUtil.putSPValue(Preferences.KEY_USER_TOKEN, neteaseToken);
    }

    /**
     * 网易云登录
     *
     * @param neteaseAccid 账号
     * @param neteaseToken Token
     */
    public void doNeteaseLogin(String neteaseAccid, String neteaseToken) {
        LoginInfo info = new LoginInfo(neteaseAccid, neteaseToken);
        RequestCallback<LoginInfo> callback =
                new RequestCallback<LoginInfo>() {
                    @Override
                    public void onSuccess(LoginInfo param) {
                        if (param != null) {
                            AVChatProfile.setAccount(param.getAccount());
                        }
                        LogUtil.getLog().d("MainActivity", "网易云登录onSuccess");
                        LogUtil.writeLog(">>>>>>>>>网易云登录onSuccess>>>>>>>>>>>> ");
                    }

                    @Override
                    public void onFailed(int code) {
                        LogUtil.getLog().d("MainActivity", "网易云登录onFailed:" + code);
                        LogUtil.writeLog(">>>>>>>>>网易云登录onFailed>>>>>>>>>>>> code:"+code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        LogUtil.getLog().d("MainActivity", "网易云登录exception:" + exception.getMessage());
                        LogUtil.writeLog(">>>>>>>>>网易云登录exception>>>>>>>>>>>> exception:"+exception.getMessage());
                    }
                    // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用
                };
        NIMClient.getService(AuthService.class).login(info)
                .setCallback(callback);
    }


    /**
     * 设置已读开关
     */
    public void friendsSetRead(long uid, int read, CallBack<ReturnBean> callback) {
        NetUtil.getNet().exec(server.friendsSetRead(uid, read), callback);
    }

    /**
     * 获取单个群成员信息
     *
     * @param gid      群id
     * @param uid      群成员ID
     * @param callback
     */
    public void getSingleMemberInfo(String gid, int uid, Callback<ReturnBean<SingleMeberInfoBean>> callback) {
        NetUtil.getNet().exec(server.getSingleMemberInfo(gid, uid), callback);
    }

}
