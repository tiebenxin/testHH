package com.yanlong.im.chat.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.bean.VideoMessage;
import com.yanlong.im.chat.dao.MsgDao;
import com.yanlong.im.utils.socket.SocketData;

import net.cb.cb.library.bean.EventUpImgLoadEvent;
import net.cb.cb.library.bean.EventUpVideoLoadEvent;
import net.cb.cb.library.utils.ImgSizeUtil;
import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.utils.UpFileAction;
import net.cb.cb.library.utils.UpFileUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


public class UpLoadService extends Service {
    public static Queue<UpProgress> queue = new LinkedList<>();
    public static HashMap<String, Integer> pgms = new HashMap<>();
    private UpFileAction upFileAction = new UpFileAction();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public static Integer getProgress(String msgId) {
        if (pgms.containsKey(msgId)) {
            int pg = pgms.get(msgId);
//            LogUtil.getLog().d("getProgress", "getProgress: " + msgId + "  val:" + pg);
            return pg;
        }

        return null;
    }

    private static void updateProgress(String msgId, Integer pg) {
        Integer progress = pgms.get(msgId);
//        LogUtil.getLog().i("ChatActivity", "upload进度--msgId=" + msgId + "--progress=" + progress + "--pg=" + pg);
        if (progress == null || pg > progress) {
            pgms.put(msgId, pg);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        LogUtil.getLog().i("ChatActivity", "UploadService--onCreate");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (queue.size() > 0) {
                    UpProgress upProgress = queue.poll();
                    LogUtil.getLog().d("ChatActivity--上传", "上传: " + upProgress.getId());
                    upFileAction.upFileSyn(UpFileAction.PATH.IMG, getApplicationContext(), upProgress.getCallback(), upProgress.getFile());
                }
                stopSelf();
                LogUtil.getLog().d("ChatActivity-上传", "上传结束");
            }
        }).start();

    }

    private static long oldUptime = 0;

    private static MsgDao msgDao = new MsgDao();

    public static void onAdd(final String id, String file, final Boolean isOriginal, final Long toUId, final String toGid, final long time) {
        final UpProgress upProgress = new UpProgress();
        upProgress.setId(id);
        //  upProgress.setProgress(0);
        upProgress.setFile(file);
        // updateProgress(id, 0);
        updateProgress(id, new Random().nextInt(5)+1);//发送图片后默认给个进度，显示阴影表示正在上传
        final ImgSizeUtil.ImageSize img = ImgSizeUtil.getAttribute(file);
        // LogUtil.getLog().d("TAG", "----------onAdd: "+img.getSizeStr());
        //LogUtil.getLog().d("TAG", "----------: "+img.getWidth());
        upProgress.setCallback(new UpFileUtil.OssUpCallback() {

            @Override
            public void success(final String url) {
                EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                // upProgress.setProgress(100);
                updateProgress(id, 100);
                eventUpImgLoadEvent.setMsgid(id);
                eventUpImgLoadEvent.setState(1);
                eventUpImgLoadEvent.setUrl(url);
                eventUpImgLoadEvent.setOriginal(isOriginal);
                Object msgbean = SocketData.send4Image(id, toUId, toGid, url, isOriginal, img, time);

//                ImageMessage image = SocketData.createImageMessage(id, url, isOriginal, img);
//                MsgAllBean msgBean = SocketData.createMessageBean(toUId, toGid, ChatEnum.EMessageType.IMAGE, ChatEnum.ESendStatus.SENDING, time, image);
//                SocketData.sendAndSaveMessage(msgBean);
//                SocketData.saveMessage(msgBean);

                eventUpImgLoadEvent.setMsgAllBean(msgbean);
                EventBus.getDefault().post(eventUpImgLoadEvent);
                // LogUtil.getLog().d("tag", "success : ===============>"+id);
                //  myback.success(url);


            }

            @Override
            public void fail() {
                EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                //  LogUtil.getLog().d("tag", "fail : ===============>"+id);
                //alert.dismiss();
                // ToastUtil.show(getContext(), "上传失败,请稍候重试");

                //  upProgress.setProgress(100);
                // updateProgress(id, 100);

                System.out.println(UpLoadService.class.getSimpleName() + "--");
                updateProgress(id, 0);
                eventUpImgLoadEvent.setMsgid(id);
                eventUpImgLoadEvent.setState(-1);
                eventUpImgLoadEvent.setUrl("");
                eventUpImgLoadEvent.setOriginal(isOriginal);
                eventUpImgLoadEvent.setMsgAllBean(msgDao.fixStataMsg(id, ChatEnum.ESendStatus.ERROR));//写库
                EventBus.getDefault().post(eventUpImgLoadEvent);


                // myback.fail();
            }

            @Override
            public void inProgress(long progress, long zong) {
                if (System.currentTimeMillis() - oldUptime < 100) {
                    return;
                }
                EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                // LogUtil.getLog().d("tag", "inProgress : ===============>"+id);
                oldUptime = System.currentTimeMillis();

                int pg = new Double(progress / (zong + 0.0f) * 100.0).intValue();

                // upProgress.setProgress(new Double(pg);
                updateProgress(id, pg);
                eventUpImgLoadEvent.setMsgid(id);
                eventUpImgLoadEvent.setState(0);
                eventUpImgLoadEvent.setUrl("");
                eventUpImgLoadEvent.setOriginal(isOriginal);
                EventBus.getDefault().post(eventUpImgLoadEvent);

                //  myback.inProgress(upProgress.getProgress(), 0);
            }
        });


        queue.offer(upProgress);
    }


    private static String netBgUrl;

    public static void onAddVideo(final Context mContext, final String id, final String file, String bgUrl, final Boolean isOriginal, final Long toUId, final String toGid, final long time, final VideoMessage videoMessage) {
        // 上传预览图时，默认给1-5的上传进度，解决一开始上传不显示进度问题
        updateProgress(id, new Random().nextInt(5)+1);
        uploadImageOfVideo(mContext, bgUrl, new UpLoadCallback() {
            @Override
            public void success(String url) {
                netBgUrl = url;
                UpFileAction upFileAction = new UpFileAction();
                upFileAction.upFile(UpFileAction.PATH.VIDEO, mContext, new UpFileUtil.OssUpCallback() {
                    @Override
                    public void success(String url) {
                        //                alert.dismiss();
                        //                String gid = getIntent().getExtras().getString("gid");
                        //                taskGroupInfoSet(gid, url, null, null);
//                        doUpVideoPro(id,url,netBgUrl,isOriginal,toUId,toGid,time,videoMessage);
                        EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                        // upProgress.setProgress(100);
                        updateProgress(id, 100);
                        eventUpImgLoadEvent.setMsgid(id);
                        eventUpImgLoadEvent.setState(1);
                        eventUpImgLoadEvent.setUrl(url);
                        eventUpImgLoadEvent.setOriginal(isOriginal);
                        Object msgbean = SocketData.sendVideo(id, toUId, toGid, url, netBgUrl, isOriginal, time, (int) videoMessage.getWidth(), (int) videoMessage.getHeight(), videoMessage.getLocalUrl());
                        ((MsgAllBean) msgbean).getVideoMessage().setLocalUrl(videoMessage.getLocalUrl());
//                        MsgDao dao = new MsgDao();
//                        dao.fixVideoLocalUrl(id, videoMessage.getLocalUrl());
                        eventUpImgLoadEvent.setMsgAllBean(msgbean);
                        EventBus.getDefault().post(eventUpImgLoadEvent);

//                        Object msgbeanVideo = SocketData.发送视频信息(id, toUId, toGid, url,netBgUrl,isOriginal, videoMessage, time,(int)videoMessage.getWidth(),(int)videoMessage.getHeight());
                    }

                    @Override
                    public void fail() {
//                alert.dismiss();
//                ToastUtil.show(getContext(), "上传失败!");
                        EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                        //  LogUtil.getLog().d("tag", "fail : ===============>"+id);
                        //alert.dismiss();
                        // ToastUtil.show(getContext(), "上传失败,请稍候重试");

                        //  upProgress.setProgress(100);
                        updateProgress(id, 100);
                        eventUpImgLoadEvent.setMsgid(id);
                        eventUpImgLoadEvent.setState(-1);
                        eventUpImgLoadEvent.setUrl("");
                        eventUpImgLoadEvent.setOriginal(isOriginal);
                        eventUpImgLoadEvent.setMsgAllBean(msgDao.fixStataMsg(id, 1));//写库
                        EventBus.getDefault().post(eventUpImgLoadEvent);
                    }

                    @Override
                    public void inProgress(long progress, long zong) {
                        if (System.currentTimeMillis() - oldUptime < 100) {
                            return;
                        }
                        EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                        // LogUtil.getLog().d("tag", "inProgress : ===============>"+id);
                        oldUptime = System.currentTimeMillis();

                        int pg = new Double(progress / (zong + 0.0f) * 100.0).intValue();
                        Log.i("ChatActivity","pg:"+pg);
                        // upProgress.setProgress(new Double(pg);
                        updateProgress(id, pg);
                        eventUpImgLoadEvent.setMsgid(id);
                        eventUpImgLoadEvent.setState(0);
                        eventUpImgLoadEvent.setUrl("");
                        eventUpImgLoadEvent.setOriginal(isOriginal);
                        EventBus.getDefault().post(eventUpImgLoadEvent);
                    }
                }, file);
            }

            @Override
            public void fail() {
                EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                //  LogUtil.getLog().d("tag", "fail : ===============>"+id);
                //alert.dismiss();
                // ToastUtil.show(getContext(), "上传失败,请稍候重试");
                System.out.println(UpLoadService.class.getSimpleName() + "图片上传失败");
                //  upProgress.setProgress(100);
                updateProgress(id, 100);
                eventUpImgLoadEvent.setMsgid(id);
                eventUpImgLoadEvent.setState(-1);
                eventUpImgLoadEvent.setUrl("");
                eventUpImgLoadEvent.setOriginal(isOriginal);
                eventUpImgLoadEvent.setMsgAllBean(msgDao.fixStataMsg(id, 1));//写库
                EventBus.getDefault().post(eventUpImgLoadEvent);
            }

        });
//        queue.offer(upProgress);

    }

    public interface UpLoadCallback {

        void success(String url);

        void fail();

    }

    /*
     * 上传视频预览图
     * */
    private static void uploadImageOfVideo(Context mContext, String file, final UpLoadCallback upLoadCallback) {
        UpFileAction upFileAction = new UpFileAction();
        upFileAction.upFile(UpFileAction.PATH.VIDEO, mContext, new UpFileUtil.OssUpCallback() {
            @Override
            public void success(String url) {
//                alert.dismiss();
//                String gid = getIntent().getExtras().getString("gid");
//                taskGroupInfoSet(gid, url, null, null);
                upLoadCallback.success(url);
            }

            @Override
            public void fail() {
//                alert.dismiss();
//                ToastUtil.show(getContext(), "上传失败!");
                upLoadCallback.fail();
            }

            @Override
            public void inProgress(long progress, long zong) {
                LogUtil.getLog().e("TAG",progress+"---------"+zong);
                Log.e("TAG", progress + "---------" + zong);
            }
        }, file);

    }

    private static void doUpVideoPro(final String id, String file, final String video_bg, final Boolean isOriginal, final Long toUId, final String toGid, final long time, final VideoMessage videoMessage) {

        final UpProgress upProgress = new UpProgress();
        upProgress.setId(id);
        //  upProgress.setProgress(0);
        upProgress.setFile(file);
        updateProgress(id, 0);
        long img = ImgSizeUtil.getVideoSize(file);
        // LogUtil.getLog().d("TAG", "----------onAdd: "+img.getSizeStr());
        //LogUtil.getLog().d("TAG", "----------: "+img.getWidth());
        upProgress.setCallback(new UpFileUtil.OssUpCallback() {

            @Override
            public void success(final String url) {
                EventUpVideoLoadEvent eventUpImgLoadEvent = new EventUpVideoLoadEvent();
                // upProgress.setProgress(100);
                updateProgress(id, 100);
                eventUpImgLoadEvent.setMsgid(id);
                eventUpImgLoadEvent.setState(1);
                eventUpImgLoadEvent.setUrl(url);
                eventUpImgLoadEvent.setOriginal(isOriginal);
                Object msgbean = SocketData.sendVideo(id, toUId, toGid, url, video_bg, isOriginal, time, (int) videoMessage.getWidth(), (int) videoMessage.getHeight(), videoMessage.getLocalUrl());

                eventUpImgLoadEvent.setMsgAllBean(msgbean);
                EventBus.getDefault().post(eventUpImgLoadEvent);
                // LogUtil.getLog().d("tag", "success : ===============>"+id);
                //  myback.success(url);
            }

            @Override
            public void fail() {
                EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                //  LogUtil.getLog().d("tag", "fail : ===============>"+id);
                //alert.dismiss();
                // ToastUtil.show(getContext(), "上传失败,请稍候重试");
                //  upProgress.setProgress(100);
                updateProgress(id, 100);
                eventUpImgLoadEvent.setMsgid(id);
                eventUpImgLoadEvent.setState(-1);
                eventUpImgLoadEvent.setUrl("");
                eventUpImgLoadEvent.setOriginal(isOriginal);
                eventUpImgLoadEvent.setMsgAllBean(msgDao.fixStataMsg(id, 1));//写库
                EventBus.getDefault().post(eventUpImgLoadEvent);
                // myback.fail();
            }

            @Override
            public void inProgress(long progress, long zong) {
                if (System.currentTimeMillis() - oldUptime < 100) {
                    return;
                }
                EventUpImgLoadEvent eventUpImgLoadEvent = new EventUpImgLoadEvent();
                // LogUtil.getLog().d("tag", "inProgress : ===============>"+id);
                oldUptime = System.currentTimeMillis();

                int pg = new Double(progress / (zong + 0.0f) * 100.0).intValue();

                // upProgress.setProgress(new Double(pg);
                updateProgress(id, pg);
                eventUpImgLoadEvent.setMsgid(id);
                eventUpImgLoadEvent.setState(0);
                eventUpImgLoadEvent.setUrl("");
                eventUpImgLoadEvent.setOriginal(isOriginal);
                EventBus.getDefault().post(eventUpImgLoadEvent);
                //  myback.inProgress(upProgress.getProgress(), 0);
            }
        });

        queue.offer(upProgress);


    }


    public static class UpProgress {
        private String id;
        private String file;
        private int progress;
        private UpFileUtil.OssUpCallback callback;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public UpFileUtil.OssUpCallback getCallback() {
            return callback;
        }

        public void setCallback(UpFileUtil.OssUpCallback callback) {
            this.callback = callback;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }
    }

}
