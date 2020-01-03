package com.zhaoss.weixinrecorded.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lansosdk.videoeditor.LanSoEditor;
import com.lansosdk.videoeditor.LanSongFileUtil;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;
import com.libyuv.LibyuvUtil;
import com.zhaoss.weixinrecorded.R;
import com.zhaoss.weixinrecorded.util.CameraHelp;
import com.zhaoss.weixinrecorded.util.MyVideoEditor;
import com.zhaoss.weixinrecorded.util.RecordUtil;
import com.zhaoss.weixinrecorded.util.RxJavaUtil;
import com.zhaoss.weixinrecorded.util.Utils;
import com.zhaoss.weixinrecorded.view.LineProgressView;
import com.zhaoss.weixinrecorded.view.RecordView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.hardware.Camera.getNumberOfCameras;


public class RecordedLocalActivity extends BaseActivity  {

    public static final String INTENT_PATH = "intent_path";
    public static final String INTENT_VIDEO_WIDTH= "intent_width";
    public static final String INTENT_PATH_HEIGHT = "intent_height";
    public static final String INTENT_PATH_TIME = "intent_time";
    public static final String INTENT_DATA_TYPE = "result_data_type";

    public static final int RESULT_TYPE_VIDEO = 1;
    public static final int RESULT_TYPE_PHOTO = 2;

    public static final int REQUEST_CODE_KEY = 100;

    public static final float MAX_VIDEO_TIME = 18f*1000;  //最大录制时间
    public static final float MIN_VIDEO_TIME = 1f*1000;  //最小录制时间

    private SurfaceView surfaceView;
    private RecordView recordView;
    private ImageView iv_delete;
    private ImageView iv_next;
    private ImageView iv_change_camera;
    private LineProgressView lineProgressView;
    private ImageView iv_flash_video,iv_delete_back;
    private TextView iv_recorded_edit;
    private TextView editorTextView;
    private TextView tv_hint;

    private ArrayList<String> segmentList = new ArrayList<>();//分段视频地址
    private ArrayList<String> aacList = new ArrayList<>();//分段音频地址
    private ArrayList<Long> timeList = new ArrayList<>();//分段录制时间

    //是否在录制视频
    private AtomicBoolean isRecordVideo = new AtomicBoolean(false);
    //拍照
    private AtomicBoolean isShotPhoto = new AtomicBoolean(false);
    private CameraHelp mCameraHelp = new CameraHelp();
    private SurfaceHolder mSurfaceHolder;
    private MyVideoEditor mVideoEditor = new MyVideoEditor();
    private RecordUtil recordUtil;

    private int executeCount;//总编译次数
    private float executeProgress;//编译进度
    private String audioPath;
    private RecordUtil.OnPreviewFrameListener mOnPreviewFrameListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_recorded_local);

        LanSoEditor.initSDK(this, null);
//        LanSongFileUtil.setFileDir("/sdcard/WeiXinRecorded/"+System.currentTimeMillis()+"/");
        LibyuvUtil.loadLibrary();

        initUI();
        initData();
//        initMediaRecorder();
        initRecord();
//        startRecordLocal();
    }

    private void initRecord() {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
                mCameraHelp.openCamera(mContext, Camera.CameraInfo.CAMERA_FACING_BACK, mSurfaceHolder);
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraHelp.release();
            }
        });
        mCameraHelp.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                if(isShotPhoto.get()){
                    isShotPhoto.set(false);
                    shotPhoto(data);

                }else{
                    if(isRecordVideo.get() && mOnPreviewFrameListener!=null){
                        mOnPreviewFrameListener.onPreviewFrame(data);
                    }
                }
            }
        });
    }

    private void initUI() {

        surfaceView = findViewById(R.id.surfaceView);
        recordView = findViewById(R.id.recordView);
        iv_delete = findViewById(R.id.iv_delete);
        iv_next = findViewById(R.id.iv_next);
        iv_recorded_edit = findViewById(R.id.iv_recorded_edit);
        iv_change_camera = findViewById(R.id.iv_camera_mode);
        lineProgressView =  findViewById(R.id.lineProgressView);
        tv_hint = findViewById(R.id.tv_hint);
        iv_flash_video = findViewById(R.id.iv_flash_video);
        iv_delete_back = findViewById(R.id.iv_delete_back);

        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                int width = surfaceView.getWidth();
                int height = surfaceView.getHeight();
                float viewRatio = width*1f/height;
                float videoRatio = 9f/16f;
                ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
                if(viewRatio > videoRatio){
                    layoutParams.width = width;
                    layoutParams.height = (int) (width/viewRatio);
                }else{
                    layoutParams.width = (int) (height*viewRatio);
                    layoutParams.height = height;
                }
                surfaceView.setLayoutParams(layoutParams);
            }
        });
    }


    private void shotPhoto(final byte[] nv21){

//        TextView textView = showProgressDialog();
//        textView.setText("图片截取中");
        RxJavaUtil.run(new RxJavaUtil.OnRxAndroidListener<String>() {
            @Override
            public String doInBackground() throws Throwable {

                boolean isFrontCamera = mCameraHelp.getCameraId()== Camera.CameraInfo.CAMERA_FACING_FRONT;
                int rotation;
                if(isFrontCamera){
                    rotation = 270;
                }else{
                    rotation = 90;
                }

                byte[] yuvI420 = new byte[nv21.length];
                byte[] tempYuvI420 = new byte[nv21.length];

                int videoWidth =  mCameraHelp.getHeight();
                int videoHeight =  mCameraHelp.getWidth();

                LibyuvUtil.convertNV21ToI420(nv21, yuvI420, mCameraHelp.getWidth(), mCameraHelp.getHeight());
                LibyuvUtil.compressI420(yuvI420, mCameraHelp.getWidth(), mCameraHelp.getHeight(), tempYuvI420,
                        mCameraHelp.getWidth(), mCameraHelp.getHeight(), rotation, isFrontCamera);

                Bitmap bitmap = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_8888);

                LibyuvUtil.convertI420ToBitmap(tempYuvI420, bitmap, videoWidth, videoHeight);

                String photoPath = LanSongFileUtil.DEFAULT_DIR+System.currentTimeMillis()+".jpeg";
                FileOutputStream fos = new FileOutputStream(photoPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                return photoPath;
            }
            @Override
            public void onFinish(String result) {
                closeProgressDialog();

                Intent intent =new Intent(RecordedLocalActivity.this,ImageShowActivity.class);
                intent.putExtra("imgpath",result);
                startActivityForResult(intent,90);
            }
            @Override
            public void onError(Throwable e) {
                closeProgressDialog();
                Toast.makeText(getApplicationContext(), "图片截取失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initData() {

        lineProgressView.setMinProgress(MIN_VIDEO_TIME / MAX_VIDEO_TIME);
        recordView.setOnGestureListener(new RecordView.OnGestureListener() {
            @Override
            public void onDown() {
                //长按录像
                recordTime = System.currentTimeMillis();
                isRecordVideo.set(true);
//                startRecord();
                startRecordLocal();
                startRecordSendPart();
                goneRecordLayout();
            }
            @Override
            public void onUp() {
                if(isRecordVideo.get()){
                    isRecordVideo.set(false);
//                    upEvent();
                }
                mRecorder.stop();
                mRecorder.release();
                initRecorderState();
            }
            @Override
            public void onClick() {
                if(segmentList.size() == 0){
                    isShotPhoto.set(true);
//                    Toast.makeText(RecordedActivity.this,"长按录制",Toast.LENGTH_SHORT).show();
                }
            }
        });

        iv_recorded_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorTextView = showProgressDialog();
                executeCount = segmentList.size()+4;
                finishVideo(2);
            }
        });

        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorTextView = showProgressDialog();
                executeCount = segmentList.size()+4;
                finishVideo(1);
            }
        });
        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                deleteSegment();
            }
        });
        iv_flash_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraHelp.changeFlash();
                if (mCameraHelp.isFlashOpen()) {
//                    iv_flash_video.setImageResource(R.mipmap.video_flash_open);
                } else {
//                    iv_flash_video.setImageResource(R.mipmap.video_flash_close);
                }
            }
        });

        iv_change_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraHelp.getCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK){
                    mCameraHelp.openCamera(mContext, Camera.CameraInfo.CAMERA_FACING_FRONT, mSurfaceHolder);
                }else{
                    mCameraHelp.openCamera(mContext, Camera.CameraInfo.CAMERA_FACING_BACK, mSurfaceHolder);
                }
//                iv_flash_video.setImageResource(R.mipmap.video_flash_close);
            }
        });
    }
    private String aacPath;
    public void finishVideo(final int type){

                switch (type){
                    case 1:
                        Intent intentMas = new Intent();
                        intentMas.putExtra(INTENT_PATH, path);
                        intentMas.putExtra(INTENT_VIDEO_WIDTH,mCameraHelp.getHeight() );
                        intentMas.putExtra(INTENT_PATH_HEIGHT,mCameraHelp.getWidth() );
//                        intentMas.putExtra(INTENT_PATH_TIME,(int)countTime);
                        intentMas.putExtra(INTENT_PATH_TIME,(int)Long.parseLong(getVideoAtt(path)));
                        intentMas.putExtra(INTENT_DATA_TYPE, RESULT_TYPE_VIDEO);
                        setResult(RESULT_OK, intentMas);
                        finish();
                        break;
                    case 2:
                        Intent intent = new Intent(mContext, EditVideoActivity.class);
                        intent.putExtra(INTENT_PATH, path);
                        startActivityForResult(intent, REQUEST_CODE_KEY);
                        break;

            }

    }

    private String getVideoAtt(String mUri) {
        String duration = null;
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            if (mUri != null) {
//                HashMap<String, String> headers = null;
//                if (headers == null)
//                {
//                    headers = new HashMap<String, String>();
//                    headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.4.2; zh-CN; MW-KW-001 Build/JRO03C) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 UCBrowser/1.0.0.001 U4/0.8.0 Mobile Safari/533.1");
//                }
                FileInputStream inputStream = new FileInputStream(new File(mUri).getAbsolutePath());
                mmr.setDataSource(inputStream.getFD());
//                mmr.setDataSource(mUri, headers);
            } else {
                //mmr.setDataSource(mFD, mOffset, mLength);
            }
            duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);//时长(毫秒)

        } catch (Exception ex) {
            Log.e("TAG", "MediaMetadataRetriever exception " + ex);
        } finally {
            mmr.release();
        }
        return duration;
    }

    MyVideoEditor myVideoEditor = new MyVideoEditor();

    private void clearProgress() {
        recordView.updateProgress(0);
    }

    private void goneRecordLayout(){
//        tv_hint.setVisibility(View.GONE);
        iv_delete.setVisibility(View.GONE);
        iv_next.setVisibility(View.GONE);
        iv_recorded_edit.setVisibility(View.GONE);
    }

    private long videoDuration;
    private long recordTime;
    private String videoPath;

    private void startRecord(){

        RxJavaUtil.run(new RxJavaUtil.OnRxAndroidListener<Boolean>() {
            @Override
            public Boolean doInBackground() throws Throwable {
                videoPath = LanSongFileUtil.DEFAULT_DIR+System.currentTimeMillis()+".h264";
                audioPath = LanSongFileUtil.DEFAULT_DIR+System.currentTimeMillis()+".pcm";
                final boolean isFrontCamera = mCameraHelp.getCameraId()== Camera.CameraInfo.CAMERA_FACING_FRONT;
                final int rotation;
                if(isFrontCamera){
                    rotation = 270;
                }else{
                    rotation = 90;
                }
                recordUtil = new RecordUtil(videoPath, audioPath, mCameraHelp.getWidth(), mCameraHelp.getHeight(), rotation, isFrontCamera);
                return true;
            }
            @Override
            public void onFinish(Boolean result) {
                if(recordView.isDown()){
                    mOnPreviewFrameListener = recordUtil.start();
                    videoDuration = 0;
                    lineProgressView.setSplit();
                    recordTime = System.currentTimeMillis();
                    runLoopPro();
                }else{
                    recordUtil.release();
                    recordUtil = null;
                }
            }
            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private MediaRecorder mRecorder;
    private Camera camera;
    private String path;
    private void startRecordLocal(){
//           int count=  Camera.getNumberOfCameras();
           if (null==mRecorder){
               mRecorder=new MediaRecorder();
           }
        mRecorder.reset();
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
//        Camera.Parameters parameters= camera.getParameters();
//        parameters.setPreviewSize(1920,1080);
        if (camera != null) {
//            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            camera.unlock();
            mRecorder.setCamera(camera);
        }


    }
    private void startRecordSendPart(){
        try {
            // 这两项需要放在setOutputFormat之前
            mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // Set output file format
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            // 这两项需要放在setOutputFormat之后
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//            final Camera.Parameters p = camera.getParameters();
//            List<Camera.Size> previewSizes=p.getSupportedPreviewSizes();
            mRecorder.setVideoSize(640, 480);
//            mRecorder.setVideoSize(1920, 1080);
//            mRecorder.setVideoSize(previewSizes.get(0).width, previewSizes.get(0).height);
            mRecorder.setVideoFrameRate(70);
            mRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
            mRecorder.setOrientationHint(90);

            //设置记录会话的最大持续时间（毫秒）
            mRecorder.setMaxDuration(30 * 1000);
            mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

            path = getSDPath();
            if (path != null) {
//                File dir = new File(path + "/recordtest");
                File dir = new File("/sdcard/WeiXinRecorded/"+System.currentTimeMillis()+"/");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                path = dir + "/" + getDate() + ".mp4";
                mRecorder.setOutputFile(path);
                mRecorder.prepare();
                mRecorder.start();
//                mStartedFlg = true;
//                mBtnStartStop.setText("Stop");
                runLoopPro();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SurfaceHolder holder = surfaceView.getHolder();
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR);           // 小时
        int second = ca.get(Calendar.SECOND);       // 秒

        String date = "" + year + (month + 1) + day + hour + minute + second;
        Log.d("TAG", "date:" + date);

        return date;
    }

    /**
     * 获取SD path
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }

    private long countTime;
    private void runLoopPro(){

        RxJavaUtil.loop(20, new RxJavaUtil.OnRxLoopListener() {
            @Override
            public Boolean takeWhile(){
                return isRecordVideo.get();
//                return recordUtil!=null && recordUtil.isRecording();
            }
            @Override
            public void onExecute() {
                long currentTime = System.currentTimeMillis();
                videoDuration += currentTime - recordTime;
                recordTime = currentTime;
                countTime= videoDuration;
                Log.e("TAG",countTime+"");
                for (long time : timeList) {
                    countTime += time;
                }
                if (countTime <= MAX_VIDEO_TIME) {
                    lineProgressView.setProgress(countTime/ MAX_VIDEO_TIME);
                    recordView.updateProgress(countTime/MAX_VIDEO_TIME*360);
                    tv_hint.setText(countTime/1000+"秒");
                }else{
                    upEvent();
                    iv_next.callOnClick();
                }
            }
            @Override
            public void onFinish() {
                segmentList.add(videoPath);
                aacList.add(audioPath);
                timeList.add(videoDuration);
                initRecorderState();
            }
            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                lineProgressView.removeSplit();
            }
        });
    }

    private void upEvent(){
        if(recordUtil != null) {
            recordUtil.stop();
            recordUtil = null;
        }
        initRecorderState();
    }


    /**
     * 初始化视频拍摄状态
     */
    private void initRecorderState(){

        if(segmentList.size() > 0){
//            tv_hint.setText("长按录像");
        }else{
//            tv_hint.setText("长按录像 点击拍照");
        }
        tv_hint.setText("长按继续录制");
        tv_hint.setVisibility(View.VISIBLE);

//        if (lineProgressView.getSplitCount() > 0) {
//            iv_delete.setVisibility(View.VISIBLE);
//        }else{
//            iv_delete.setVisibility(View.GONE);
//        }

        if (lineProgressView.getProgress()* MAX_VIDEO_TIME < MIN_VIDEO_TIME) {
            iv_next.setVisibility(View.GONE);
            iv_delete.setVisibility(View.GONE);
            iv_recorded_edit.setVisibility(View.GONE);
            iv_delete_back.setVisibility(View.VISIBLE);
        } else {
            iv_next.setVisibility(View.VISIBLE);
            iv_delete.setVisibility(View.VISIBLE);
            iv_recorded_edit.setVisibility(View.VISIBLE);
            iv_delete_back.setVisibility(View.GONE);

        }
    }

    /**
     * 清除录制信息
     */
    private void cleanRecord(){

        recordView.initState();
        lineProgressView.cleanSplit();
        segmentList.clear();
        aacList.clear();
        timeList.clear();

        executeCount = 0;
        executeProgress = 0;

        iv_delete.setVisibility(View.INVISIBLE);
        iv_next.setVisibility(View.INVISIBLE);
        iv_recorded_edit.setVisibility(View.INVISIBLE);
//        iv_flash_video.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cleanRecord();
        if(mCameraHelp != null){
            mCameraHelp.release();
        }
        if(recordUtil != null) {
            recordUtil.stop();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK && data!=null){
            if(requestCode == REQUEST_CODE_KEY){
                Intent intent = new Intent();
                intent.putExtra(INTENT_PATH, data.getStringExtra(INTENT_PATH));
                intent.putExtra(INTENT_VIDEO_WIDTH, data.getIntExtra(INTENT_VIDEO_WIDTH,720));
                intent.putExtra(INTENT_PATH_HEIGHT, data.getIntExtra(INTENT_PATH_HEIGHT,1080));
                intent.putExtra(INTENT_PATH_TIME, data.getIntExtra(INTENT_PATH_TIME,10));
                intent.putExtra(INTENT_DATA_TYPE, RESULT_TYPE_VIDEO);
                setResult(RESULT_OK, intent);
                finish();
            }else if(requestCode==90){
                boolean result=data.getBooleanExtra("showResult",false);
                if (result){
                    Intent intent = new Intent();
                intent.putExtra(INTENT_PATH, data.getStringExtra("showPath"));
                intent.putExtra(INTENT_DATA_TYPE, RESULT_TYPE_PHOTO);
                setResult(RESULT_OK, intent);
                finish();
                }
            }
        }else{
            cleanRecord();
            initRecorderState();
        }
    }

}
