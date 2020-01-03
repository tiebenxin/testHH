package com.yanlong.im.chat.ui.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.baoyz.widget.PullRefreshLayout;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.yanlong.im.R;
import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.ScrollConfig;
import com.yanlong.im.chat.bean.VoiceMessage;
import com.yanlong.im.chat.manager.MessageManager;
import com.yanlong.im.chat.server.UpLoadService;
import com.yanlong.im.chat.ui.ChatInfoActivity;
import com.yanlong.im.chat.ui.GroupInfoActivity;
import com.yanlong.im.chat.ui.GroupRobotActivity;
import com.yanlong.im.chat.ui.GroupSelectUserActivity;
import com.yanlong.im.chat.ui.cell.FactoryChatCell;
import com.yanlong.im.chat.ui.cell.ICellEventListener;
import com.yanlong.im.chat.ui.cell.MessageAdapter;
import com.yanlong.im.databinding.ActivityChat2Binding;
import com.yanlong.im.user.action.UserAction;
import com.yanlong.im.user.bean.UserInfo;
import com.yanlong.im.user.dao.UserDao;
import com.yanlong.im.user.ui.SelectUserActivity;
import com.yanlong.im.user.ui.UserInfoActivity;
import com.yanlong.im.utils.audio.AudioRecordManager;
import com.yanlong.im.utils.audio.IAdioTouch;
import com.yanlong.im.utils.audio.IAudioRecord;
import com.yanlong.im.utils.socket.SocketData;

import net.cb.cb.library.base.BaseMvpActivity;
import net.cb.cb.library.utils.CheckPermission2Util;
import net.cb.cb.library.utils.DensityUtil;
import net.cb.cb.library.utils.InputUtil;
import net.cb.cb.library.utils.NetUtil;
import net.cb.cb.library.utils.ScreenUtils;
import net.cb.cb.library.utils.SharedPreferencesUtil;
import net.cb.cb.library.utils.SoftKeyBoardListener;
import net.cb.cb.library.utils.StringUtil;
import net.cb.cb.library.utils.TimeToString;
import net.cb.cb.library.view.ActionbarView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * @author Liszt
 * @date 2019/9/19
 * Description
 */
public class ChatActivity3 extends BaseMvpActivity<ChatModel, ChatView, ChatPresenter> implements ICellEventListener, ChatView {
    public static final String AGM_TOUID = "toUId";
    public static final String AGM_TOGID = "toGId";
    private ChatModel mChatModel;
    private boolean isGroup;
    private LinearLayoutManager layoutManager;
    private MessageAdapter adapter;
    private ActivityChat2Binding ui;
    private ActionbarView actionbar;
    private String gid;
    private long uid = -1;
    private Integer font_size;
    private int lastPosition;
    private int lastOffset;
    private boolean isSoftShow;
    private List<View> emojiLayout;
    private final CheckPermission2Util permission2Util = new CheckPermission2Util();
    private int survivalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ui = DataBindingUtil.setContentView(this, R.layout.activity_chat2);
        init();
    }

    private void init() {
        initIntent();
        initEvent();
        intAdapter();
        initUIAndListener();
        survivalTime = new UserDao().getReadDestroy(uid,gid);
    }

    private void initEvent() {
        presenter.registerIMListener();
    }


    @Override
    protected void onStart() {
        super.onStart();
        presenter.checkLockMessage();
        presenter.loadAndSetData();
        presenter.initUnreadCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //激活当前会话
        setCurrentSession();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //取消激活会话
        MessageManager.getInstance().setSessionNull();

    }

    private void setCurrentSession() {
        if (isGroup) {
            MessageManager.getInstance().setSessionGroup(gid);
        } else {
            MessageManager.getInstance().setSessionSolo(uid);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        presenter.unregisterIMListener();
        super.onDestroy();
    }

    private void initIntent() {
        gid = getIntent().getStringExtra(AGM_TOGID);
        uid = getIntent().getLongExtra(AGM_TOUID, 0);
        uid = uid == 0 ? -1L : uid;
        isGroup = StringUtil.isNotNull(gid);
        mChatModel.init(gid, uid);
        presenter.init(this);
    }

    @Override
    public ChatModel createModel() {
        mChatModel = new ChatModel();
        return mChatModel;
    }

    @Override
    public ChatView createView() {
        return this;
    }

    @Override
    public ChatPresenter createPresenter() {
        return new ChatPresenter();
    }

    private void intAdapter() {
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        ui.recyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(context, this, isGroup);
        adapter.setCellFactory(new FactoryChatCell(context, adapter, this));
        ui.recyclerView.setAdapter(adapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initUIAndListener() {
        actionbar = ui.headView.getActionbar();
        addViewPagerEvent();
        font_size = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.FONT_CHAT).get4Json(Integer.class);
        actionbar.getBtnRight().setImageResource(R.mipmap.ic_chat_more);
        if (isGroup) {
            actionbar.getBtnRight().setVisibility(View.GONE);
            ui.viewChatBottom.setVisibility(View.VISIBLE);
        } else {
            actionbar.getBtnRight().setVisibility(View.VISIBLE);
            if (uid == 1L) {
                ui.viewChatBottom.setVisibility(View.GONE);
            } else {
                ui.viewChatBottom.setVisibility(View.VISIBLE);
            }
        }

        actionbar.setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                if (isGroup) {//群聊,单聊
                    toGroupInfoActivity();
                } else {
                    if (uid == 1L) {
                        toUserInfoActivity();
                    } else {
                        toChatInfoActivity();
                    }
                }
            }
        });

        //发送普通消息
        ui.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //test 8.21测试发送
                // if(AppConfig.DEBUG){
                presenter.doSendText(ui.edtChat, isGroup,survivalTime);
            }
        });
        ui.edtChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    ui.btnSend.setVisibility(View.VISIBLE);
                } else {
                    ui.btnSend.setVisibility(View.GONE);
                }
                if (isGroup && !mChatModel.isHaveDraft()) {
                    if (count == 1 && (s.charAt(s.length() - 1) == "@".charAt(0) || s.charAt(s.length() - (s.length() - start)) == "@".charAt(0))) { //添加一个字
                        //跳转到@界面
                        Intent intent = new Intent(ChatActivity3.this, GroupSelectUserActivity.class);
                        intent.putExtra(GroupSelectUserActivity.TYPE, 1);
                        intent.putExtra(GroupSelectUserActivity.GID, gid);
                        startActivityForResult(intent, GroupSelectUserActivity.RET_CODE_SELECTUSR);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ui.btnFunc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ui.viewFuncRoot.viewFunc.getVisibility() == View.VISIBLE) {
                    hideBt();
                } else {
                    showBtType(0);
                }
            }
        });
        ui.btnEmj.setTag(0);
        ui.btnEmj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (ui.viewEmojiPager.emojiPagerCon.getVisibility() == View.VISIBLE) {
//                    hideBt();
//                    InputUtil.showKeyboard(ui.edtChat);
//                    ui.btnEmj.setImageLevel(0);
//                } else {
//                    showBtType(1);
//                    ui.btnEmj.setImageLevel(1);
//                }
            }
        });

        //todo  emoji表情处理
//        for (int j = 0; j < emojiLayout.size(); j++) {
//
//            GridLayout viewEmojiItem = (GridLayout) emojiLayout.get(j).findViewById(R.id.view_emoji);
//            for (int i = 0; i < viewEmojiItem.getChildCount(); i++) {
//                if (viewEmojiItem.getChildAt(i) instanceof TextView) {
//                    final TextView tv = (TextView) viewEmojiItem.getChildAt(i);
//                    tv.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ui.edtChat.getText().insert(ui.edtChat.getSelectionEnd(), tv.getText());
//                        }
//                    });
//                } else {
//                    viewEmojiItem.getChildAt(i).setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            int keyCode = KeyEvent.KEYCODE_DEL;
//                            KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
//                            KeyEvent keyEventUp = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
//                            ui.edtChat.onKeyDown(keyCode, keyEventDown);
//                            ui.edtChat.onKeyUp(keyCode, keyEventUp);
//                        }
//                    });
//                }
//            }
//        }

        //处理键盘
        SoftKeyBoardListener kbLinst = new SoftKeyBoardListener(this);
        kbLinst.setOnSoftKeyBoardChangeListener(new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int h) {
                hideBt();
                ui.viewChatBottom.setPadding(0, 0, 0, h);


                ui.btnEmj.setImageLevel(0);
                scrollBottom();
                isSoftShow = true;
            }

            @Override
            public void keyBoardHide(int h) {
                ui.viewChatBottom.setPadding(0, 0, 0, 0);
                isSoftShow = false;
            }
        });

        ui.viewFuncRoot.viewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                permission2Util.requestPermissions(ChatActivity3.this, new CheckPermission2Util.Event() {
                    @Override
                    public void onSuccess() {
                        PictureSelector.create(ChatActivity3.this)
                                .openCamera(PictureMimeType.ofImage())
                                .compress(true)
                                .forResult(PictureConfig.REQUEST_CAMERA);
                    }

                    @Override
                    public void onFail() {

                    }
                }, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});


            }
        });

        ui.viewFuncRoot.viewPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureSelector.create(ChatActivity3.this)
                        .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()
                        .selectionMode(PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                        .previewImage(false)// 是否可预览图片 true or false
                        .isCamera(false)// 是否显示拍照按钮 ture or false
                        .compress(true)// 是否压缩 true or false
                        .isGif(true)
                        .selectArtworkMaster(true)
                        .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
            }
        });

        //支付宝红包
        ui.viewFuncRoot.viewRbZfb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.sendRb();
            }
        });
        ui.viewFuncRoot.viewTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doTrans();
            }
        });

        //戳一下
        ui.viewFuncRoot.viewAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.doStamp(survivalTime);

            }
        });
        //名片
        ui.viewFuncRoot.viewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go(SelectUserActivity.class);
                startActivityForResult(new Intent(getContext(), SelectUserActivity.class), SelectUserActivity.RET_CODE_SELECTUSR);
            }
        });

        //语音
        ui.btnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //申请权限 7.2
                permission2Util.requestPermissions(ChatActivity3.this, new CheckPermission2Util.Event() {
                    @Override
                    public void onSuccess() {
                        startVoice(null);
                    }

                    @Override
                    public void onFail() {

                    }
                }, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE});

            }
        });

        ui.txtVoice.setOnTouchListener(new IAdioTouch(this, new IAdioTouch.MTouchListener() {
            @Override
            public void onDown() {
                ui.txtVoice.setText("松开 结束");
                ui.txtVoice.setBackgroundResource(R.drawable.bg_edt_chat2);
                ui.btnVoice.setEnabled(false);
                ui.btnEmj.setEnabled(false);
                ui.btnFunc.setEnabled(false);

            }

            @Override
            public void onMove() {
                //   txtVoice.setText("滑动 取消");
                //  txtVoice.setBackgroundResource(R.drawable.bg_edt_chat2);
            }

            @Override
            public void onUp() {
                ui.txtVoice.setText("按住 说话");
                ui.txtVoice.setBackgroundResource(R.drawable.bg_edt_chat);
                ui.btnVoice.setEnabled(true);
                ui.btnEmj.setEnabled(true);
                ui.btnFunc.setEnabled(true);
            }
        }));

        AudioRecordManager.getInstance(this).setAudioRecordListener(new IAudioRecord(this, ui.headView, new IAudioRecord.UrlCallback() {
            @Override
            public void completeRecord(String file, int duration) {
                VoiceMessage voice = SocketData.createVoiceMessage(SocketData.getUUID(), file, duration);
                MsgAllBean msg = SocketData.sendFileUploadMessagePre(voice.getMsgId(), uid, gid, SocketData.getFixTime(), voice, ChatEnum.EMessageType.VOICE);
                mChatModel.getListData().add(msg);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyAndScrollBottom();
                    }
                });
                presenter.uploadVoice(file, msg);
            }
        }));

        //群助手
        ui.viewFuncRoot.viewChatRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  ToastUtil.show(getContext(),"群助手");
                if (mChatModel.getGroup() == null)
                    return;

                startActivity(new Intent(getContext(), GroupRobotActivity.class)
                        .putExtra(GroupRobotActivity.AGM_GID, gid)
                        .putExtra(GroupRobotActivity.AGM_RID, mChatModel.getGroup().getRobotid())
                );
            }
        });

        if (isGroup) {//去除群的控件
            ui.viewFuncRoot.viewFunc.removeView(ui.viewFuncRoot.viewAction);
            //viewFunc.removeView(viewTransfer);
            ui.viewFuncRoot.viewChatRobot.setVisibility(View.INVISIBLE);
        } else {
            ui.viewFuncRoot.viewFunc.removeView(ui.viewFuncRoot.viewChatRobot);
        }
        ui.viewFuncRoot.viewFunc.removeView(ui.viewFuncRoot.viewRb);
        //test 6.26
        ui.viewFuncRoot.viewFunc.removeView(ui.viewFuncRoot.viewTransfer);

        ui.viewRefresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.loadAndSetMoreData();
                ui.viewRefresh.setRefreshing(false);
            }
        });

        ui.recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == SCROLL_STATE_IDLE) {

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        //获取可视的第一个view
                        lastPosition = layoutManager.findLastVisibleItemPosition();
                        View topView = layoutManager.getChildAt(lastPosition);
                        if (topView != null) {
                            //获取与该view的底部的偏移量
                            lastOffset = topView.getBottom();
                        }

                        saveScrollPosition();
                    }
                }
            }
        });

        //6.15 先加载完成界面,后刷数据
        actionbar.post(new Runnable() {
            @Override
            public void run() {
                presenter.setAndClearDraft();
            }
        });


        //9.17 进去后就清理会话的阅读数量
        mChatModel.clearUnreadCount();
        MessageManager.getInstance().notifyRefreshMsg();

    }

    @Override
    public void setDraft(String draft) {
        ui.edtChat.setText(draft);
    }

    private void saveScrollPosition() {
        if (lastPosition > 0) {
            SharedPreferencesUtil sp = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.SCROLL);
            ScrollConfig config = new ScrollConfig();
            config.setUserId(UserAction.getMyId());
            if (uid <= 0) {
                config.setChatId(gid);
            } else {
                config.setUid(uid);
            }
            config.setLastPosition(lastPosition);
            config.setLastOffset(lastOffset);
            if (mChatModel.getTotalSize() > 0) {
                config.setTotalSize(mChatModel.getTotalSize());
            }
            sp.save2Json(config, "scroll_config");
        }
    }

    /*
     * notifyAndScrollBottom
     * */
    public void notifyAndScrollBottom() {
        if (adapter != null) {
            adapter.bindData(mChatModel.getListData());
        }
        scrollListView(true);
    }

    /***
     * 开始语音
     */
    private void startVoice(Boolean open) {
        if (open == null) {
            open = ui.txtVoice.getVisibility() == View.GONE ? true : false;
        }
        if (open) {
            showBtType(2);
        } else {
            showVoice(false);
            hideBt();
        }
    }

    /***
     * 隐藏底部所有面板
     */
    private void hideBt() {
        ui.viewFuncRoot.viewFunc.setVisibility(View.GONE);
//        ui.viewEmojiPager.emojiPagerCon.setVisibility(View.GONE);
    }


    private void addViewPagerEvent() {
//        emojiLayout = new ArrayList<>();
//        View view1 = LayoutInflater.from(this).inflate(R.layout.part_chat_emoji, null);
//        View view2 = LayoutInflater.from(this).inflate(R.layout.part_chat_emoji2, null);
//        emojiLayout.add(view1);
//        emojiLayout.add(view2);
//        ui.viewEmojiPager.emojiPager.setAdapter(new EmojiAdapter(emojiLayout, ui.edtChat));
//        ui.viewEmojiPager.emojiPager.addOnPageChangeListener(new PageIndicator(this, (LinearLayout) findViewById(R.id.dot_hor), 2));
    }

    /***
     * 底部显示面板
     */
    private void showBtType(final int type) {
        ui.btnEmj.setImageLevel(0);
        InputUtil.hideKeyboard(ui.edtChat);
        showVoice(false);
        ui.viewFuncRoot.viewFunc.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideBt();
                switch (type) {
                    case 0://功能面板
                        //第二种解决方案
                        ui.viewFuncRoot.viewFunc.setVisibility(View.VISIBLE);
                        break;
                    case 1://emoji面板
//                        ui.viewEmojiPager.emojiPagerCon.setVisibility(View.VISIBLE);
                        break;
                    case 2://语音
                        showVoice(true);
                        break;
                }
                //滚动到结尾 7.5
                scrollBottom();
            }
        }, 50);
    }

    private void showVoice(boolean show) {
        if (show) {//开启语音
            ui.txtVoice.setVisibility(View.VISIBLE);
            ui.edtChat.setVisibility(View.GONE);
        } else {//关闭语音
            ui.txtVoice.setVisibility(View.GONE);
            ui.edtChat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initUnreadCount(String s) {
        actionbar.setTxtLeft(s, R.drawable.shape_unread_bg, DensityUtil.sp2px(ChatActivity3.this, 5));
    }

    @Override
    public void replaceListDataAndNotify(MsgAllBean bean) {
        int position = mChatModel.getListData().indexOf(bean);
        if (position >= 0 && position < mChatModel.getListData().size()) {
            adapter.updateItemAndRefresh(bean);
            adapter.notifyItemChanged(position, position);
        }
    }

    @Override
    public void startUploadServer(MsgAllBean bean, String file, boolean isOrigin) {
        UpLoadService.onAdd(bean.getMsg_id(), file, isOrigin, mChatModel.getUid(), mChatModel.getGid(), bean.getTimestamp());
        startService(new Intent(getContext(), UpLoadService.class));
    }

    private void toUserInfoActivity() {
        startActivity(new Intent(getContext(), UserInfoActivity.class).putExtra(UserInfoActivity.ID, uid).putExtra(UserInfoActivity.JION_TYPE_SHOW, 1));
    }

    private void toGroupInfoActivity() {
        startActivity(new Intent(getContext(), GroupInfoActivity.class).putExtra(GroupInfoActivity.AGM_GID, gid));
    }

    private void toChatInfoActivity() {
        startActivity(new Intent(getContext(), ChatInfoActivity.class).putExtra(ChatInfoActivity.AGM_FUID, uid));
    }

    @Override
    public void onEvent(int type, MsgAllBean message, Object... args) {

    }

    @Override
    public void setAndRefreshData(List<MsgAllBean> l) {
        adapter.bindData(l);
        ui.recyclerView.scrollToPosition(adapter.getItemCount() - 1);

    }

    /*
     * @param isMustBottom 是否必须滑动到底部
     * */
    @Override
    public void scrollListView(boolean isMustBottom) {
        if (mChatModel.getListData() != null) {
            int length = mChatModel.getListData().size();//刷新后当前size；
            if (isMustBottom) {
                ui.recyclerView.scrollToPosition(length);
            } else {
                if (lastPosition >= 0 && lastPosition < length) {
                    if (isSoftShow || lastPosition == length - 1 || isCanScrollBottom()) {//允许滑动到底部，或者当前处于底部，canScrollVertically是否能向上 false表示到了底部
                        ui.recyclerView.scrollToPosition(length);
                    }
                } else {
                    SharedPreferencesUtil sp = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.SCROLL);
                    if (sp != null) {
                        ScrollConfig config = sp.get4Json(ScrollConfig.class, "scroll_config");
                        if (config != null) {
                            if (config.getUserId() == UserAction.getMyId()) {
                                if (config.getUid() > 0 && config.getUid() == uid) {
                                    lastPosition = config.getLastPosition();
                                    lastOffset = config.getLastOffset();
                                } else if (!TextUtils.isEmpty(config.getChatId()) && !TextUtils.isEmpty(gid) && config.getChatId().equals(gid)) {
                                    lastPosition = config.getLastPosition();
                                    lastOffset = config.getLastOffset();
                                }
                            }
                        }
                    }
                    if (lastPosition >= 0 && lastPosition < length) {
                        if (isSoftShow || lastPosition == length - 1 || isCanScrollBottom()) {//允许滑动到底部，或者当前处于底部
                            ui.recyclerView.scrollToPosition(length);
                        } else {
                            layoutManager.scrollToPositionWithOffset(lastPosition, lastOffset);
                        }
                    } else {
                        ui.recyclerView.scrollToPosition(length);
                    }
                }
            }
        }
    }

    @Override
    public void notifyDataAndScrollBottom(boolean isScrollBottom) {
        adapter.notifyDataSetChanged();
        scrollListView(isScrollBottom);
    }

    @Override
    public void bindData(List<MsgAllBean> l) {
        if (adapter != null) {
            adapter.bindData(l);
        }
    }

    @Override
    public void scrollToPositionWithOff(int position, int offset) {
        layoutManager.scrollToPositionWithOffset(position, offset);
    }

    /*
     * 判断是否滑动过屏幕一般高度
     * */
    private boolean isCanScrollBottom() {
        if (isNoFullScreen()) {
            return true;
        }
        if (lastPosition < 0) {
            SharedPreferencesUtil sp = new SharedPreferencesUtil(SharedPreferencesUtil.SPName.SCROLL);
            if (sp != null) {
                ScrollConfig config = sp.get4Json(ScrollConfig.class, "scroll_config");
                if (config != null) {
                    if (config.getUserId() == UserAction.getMyId()) {
                        if (config.getUid() > 0 && config.getUid() == uid) {
                            lastPosition = config.getLastPosition();
                            lastOffset = config.getLastOffset();
                        } else if (!TextUtils.isEmpty(config.getChatId()) && config.getChatId().equals(gid)) {
                            lastPosition = config.getLastPosition();
                            lastOffset = config.getLastOffset();
                        }
                    }
                }
            }
        }

        if (lastPosition >= 0) {
            int targetHeight = ScreenUtils.getScreenHeight(this) / 2;//屏幕一般高度
            int size = mChatModel.getListData().size();
//            int onCreate = size - 1;
            int height = 0;
            for (int i = lastPosition; i < size - 1; i++) {
//                View view = mtListView.getLayoutManager().findViewByPosition(i);//获取不到不可见item
                View view = adapter.getItemViewByPosition(i);
                if (view == null) {
                    break;
                }
                int w = View.MeasureSpec.makeMeasureSpec(ScreenUtils.getScreenWidth(this), View.MeasureSpec.EXACTLY);
                int h = View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.UNSPECIFIED);
                view.measure(w, h);
                if (height + lastOffset < targetHeight) {
                    height += view.getMeasuredHeight();
                } else {
                    //当滑动距离高于屏幕高度的一般，终止当前循环
                    break;
                }
//                LogUtil.getLog().i(ChatActivity.class.getSimpleName(), "isCanScrollBottom -- lastPosition=" + lastPosition + "--height=" + height);
            }
            if (height + lastOffset <= targetHeight) {
                return true;
            }
        }
        return false;
    }

    /*
     * 未填充屏幕
     * */
    private boolean isNoFullScreen() {
        if (!ui.recyclerView.canScrollVertically(1) && !ui.recyclerView.canScrollVertically(-1)) {//既不能上滑也不能下滑，即未满屏的情况
            return true;
        }
        return false;
    }

    private void scrollBottom() {
        ui.recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollListView(true);
            }
        }, 100);

    }

    /*
     * 是否已经退出
     * */
    @Override
    public void setBanView(boolean isExited) {
        actionbar.getBtnRight().setVisibility(isExited ? View.GONE : View.VISIBLE);
        ui.tvBan.setVisibility(isExited ? VISIBLE : GONE);
        ui.viewChatBottomC.setVisibility(isExited ? GONE : VISIBLE);
    }

    @Override
    public void setRobotView(boolean isMaster) {
        if (isMaster) {
            ui.viewFuncRoot.viewChatRobot.setVisibility(View.VISIBLE);
        } else {
            ui.viewFuncRoot.viewChatRobot.removeView(ui.viewFuncRoot.viewRb);
        }
    }

    /*
     * taskSessionInfo
     * */
    @Override
    public void initTitle() {
        String title = "";
        if (mChatModel.isGroup()) {
            title = mChatModel.getGroupName();
            presenter.taskGroupConf();
        } else {
            UserInfo info = mChatModel.getUserInfo();
            title = info.getName4Show();
            if (info.getLastonline() > 0) {
                if(NetUtil.isNetworkConnected()){
                    actionbar.setTitleMore(TimeToString.getTimeOnline(info.getLastonline(), info.getActiveType(), true),true);
                }else {
                    actionbar.setTitleMore(TimeToString.getTimeOnline(info.getLastonline(), info.getActiveType(), true),false);
                }
            }
        }
        actionbar.setChatTitle(title);
    }

    @Override
    public void updateOnlineStatus() {
        String title = "";
        if (!isGroup) {
            UserInfo info = mChatModel.getUserInfo();
            title = info.getName4Show();
            if (info.getLastonline() > 0) {
                if(NetUtil.isNetworkConnected()){
                    actionbar.setTitleMore(TimeToString.getTimeOnline(info.getLastonline(), info.getActiveType(), true),true);
                }else {
                    actionbar.setTitleMore(TimeToString.getTimeOnline(info.getLastonline(), info.getActiveType(), true),false);
                }
            }
            actionbar.setTitle(title);
        }
    }
}
