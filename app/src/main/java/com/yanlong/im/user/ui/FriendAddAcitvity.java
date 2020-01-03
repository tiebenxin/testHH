package com.yanlong.im.user.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.yanlong.im.R;
import com.yanlong.im.utils.QRCodeManage;

import net.cb.cb.library.bean.QRCodeBean;
import net.cb.cb.library.utils.ToastUtil;
import net.cb.cb.library.view.ActionbarView;
import net.cb.cb.library.view.AppActivity;
import net.cb.cb.library.zxing.activity.CaptureActivity;

/***
 * 添加朋友
 */
public class FriendAddAcitvity extends AppActivity {
    public final static int PERMISSIONS = 10000;

    private net.cb.cb.library.view.HeadView headView;
    private ActionbarView actionbar;
    private LinearLayout viewSearch;
    private LinearLayout viewMatch;
    private LinearLayout viewQr;
    private LinearLayout viewWc;



    //自动寻找控件
    private void findViews() {
        headView =  findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        viewSearch =  findViewById(R.id.view_search);
        viewMatch =  findViewById(R.id.view_match);
        viewQr =  findViewById(R.id.view_qr);
        viewWc =  findViewById(R.id.view_wc);
        UMShareAPI.get(this);
    }


    //自动生成的控件事件
    private void initEvent() {
        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {

            }
        });

        viewMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                go(FriendMatchActivity.class);
            }
        });

        viewQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // 申请权限
                    ActivityCompat.requestPermissions(FriendAddAcitvity.this,
                            new String[]{Manifest.permission.CAMERA},
                            CaptureActivity.REQ_PERM_CAMERA);
                    return;
                }
                // 二维码扫码
                Intent intent = new Intent(FriendAddAcitvity.this, CaptureActivity.class);
                startActivityForResult(intent, CaptureActivity.REQ_QR_CODE);
            }
        });
        viewWc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareWX();
            }
        });

        viewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(FindFriendActivity.class);
            }
        });

    }

    private void shareWX(){
        if(Build.VERSION.SDK_INT>=23){
            String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE,Manifest.permission.READ_LOGS,Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.SET_DEBUG_APP,Manifest.permission.SYSTEM_ALERT_WINDOW,Manifest.permission.GET_ACCOUNTS,Manifest.permission.WRITE_APN_SETTINGS};
            ActivityCompat.requestPermissions(this,mPermissionList,PERMISSIONS);
        }

        UMImage thumb =  new UMImage(this, R.mipmap.icon_share_logo);
        thumb.setThumb(thumb);
        UMWeb web = new UMWeb("https://changxin.zhixun6.com/fx/index.html");
        web.setTitle("我们一起用“常信”来聊天吧");//标题
        web.setThumb(thumb);  //缩略图
        web.setDescription("我正在使用常信，一款为有共同兴趣爱好用户定制打造的聊天交友软件");//描述
        new ShareAction(FriendAddAcitvity.this)
                .setPlatform(SHARE_MEDIA.WEIXIN)//传入平台
                .setCallback(new UMShareListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onResult(SHARE_MEDIA share_media) {

                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
                        if(throwable.getMessage().contains("2008")){
                            ToastUtil.show(context,"请安装微信");
                        }
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media) {

                    }
                })
                .withMedia(web)//分享内容
                .share();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);
        findViews();
        initEvent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
            QRCodeManage.goToPage(this,scanResult);
//            QRCodeBean bean = QRCodeManage.getQRCodeBean(this,scanResult);
//            QRCodeManage.goToActivity(this,bean);
        }else if(requestCode == PERMISSIONS && resultCode == RESULT_OK){ //分享权限返回
            ToastUtil.show(this,"失败");
        }
    }





}
