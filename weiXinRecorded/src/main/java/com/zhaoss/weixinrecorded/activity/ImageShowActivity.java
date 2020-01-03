package com.zhaoss.weixinrecorded.activity;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhaoss.weixinrecorded.R;
import com.zhaoss.weixinrecorded.util.DimenUtils;
import com.zhaoss.weixinrecorded.util.Utils;
import com.zhaoss.weixinrecorded.view.CutView;
import com.zhaoss.weixinrecorded.view.TouchView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageShowActivity  extends BaseActivity {
    private ImageView activity_img_show_img,iv_show_next,iv_show_delete;
    private String path;
    private RelativeLayout activity_show_rl_big,rl_pen,rl_back,rl_edit_text,rl_text,rl_text_cut;
    private LinearLayout ll_color;
    private TextView tv_finish_video,tv_finish,tv_close,tv_tag,tv_hint_delete;
    private com.zhaoss.weixinrecorded.view.MyPaintView mypaintview;
    private EditText et_tag;
    private CutView activity_img_show_cut;
    private TextureView textureView_cut;

    private int windowWidth;
    private int windowHeight;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_show);
        path=(String) getIntent().getExtras().get("imgpath");
        windowWidth = Utils.getWindowWidth(mContext);
        windowHeight = Utils.getWindowHeight(mContext);
        initView();
        initEvent();
        activity_img_show_img.setImageURI(Uri.parse(path));
    }

    private void initEvent() {
        initColors();
//        findViewById(R.id.iv_show_next).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                Bitmap bitmap= loadBitmapFromView(activity_show_rl_big);
//                String savePath= saveImage(bitmap,100);
//                intent.putExtra("showResult", true);
//                intent.putExtra("showPath", savePath);
//                setResult(RESULT_OK, intent);
//                finish();
//            }
//        });
        et_tag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                tv_tag.setText(s.toString());
            }
        });
        tv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null!=et_tag.getText()&&et_tag.getText().toString().length()>0){
                    addTextToWindow();
                }
                rl_edit_text.setVisibility(View.GONE);
                hiddenPopSoft();
            }
        });
        tv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_edit_text.setVisibility(View.GONE);
                hiddenPopSoft();
            }
        });
        tv_finish_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bitmap bitmap= loadBitmapFromView(activity_show_rl_big);
                String savePath= saveImage(bitmap,100);
                intent.putExtra("showResult", true);
                intent.putExtra("showPath", savePath);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        findViewById(R.id.rl_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.iv_show_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("showResult", false);
                intent.putExtra("showPath", "");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        rl_pen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_color.getVisibility()==View.VISIBLE){
                    ll_color.setVisibility(View.INVISIBLE);
                }else{
                    ll_color.setVisibility(View.VISIBLE);
                    mypaintview.setPenColor(getResources().getColor(colors[0]));
                    mypaintview.setVisibility(View.VISIBLE);
                }

            }
        });
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null!=mypaintview){
                    if (mypaintview.canUndo()){
                        mypaintview.undo();
                    }
                }
            }
        });
        rl_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl_edit_text.setVisibility(View.VISIBLE);
                showSoftInputFromWindow(et_tag);
                startAnim(rl_edit_text.getY(), 0, null);
//                popupEditText();
            }
        });
        rl_text_cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_color.setVisibility(View.INVISIBLE);
                if (activity_img_show_cut.getVisibility()==View.VISIBLE){
                    activity_img_show_cut.setVisibility(View.GONE);
                }else{
                    activity_img_show_cut.setVisibility(View.VISIBLE);
                }
            }
        });
        textureView_cut.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                activity_img_show_cut.setMargin(textureView_cut.getLeft(), textureView_cut.getTop(), textureView_cut.getRight()-textureView_cut.getWidth(), textureView_cut.getBottom()-textureView_cut.getHeight());
            }
        });
    }


    private InputMethodManager manager;
    boolean isFirstShowEditText;
    /**
     * 弹出键盘
     */
    public void popupEditText() {
//
//        isFirstShowEditText = true;
//        et_tag.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (isFirstShowEditText) {
//                    isFirstShowEditText = false;
//                    et_tag.setFocusable(true);
//                    et_tag.setFocusableInTouchMode(true);
//                    et_tag.requestFocus();
//                    isFirstShowEditText = !manager.showSoftInput(et_tag, 0);
//                }
//            }
//        });
        manager.showSoftInput(et_tag, 0);
    }

    /**
     * 收起键盘
     */
    public void hiddenPopSoft(){
        manager.hideSoftInputFromWindow(et_tag.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private int dp100;
    private void addTextToWindow() {
        dp100 = (int) getResources().getDimension(R.dimen.dp100);
        TouchView touchView = new TouchView(getApplicationContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(tv_tag.getWidth(), tv_tag.getHeight());
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        touchView.setLayoutParams(layoutParams);
        Bitmap bitmap = Bitmap.createBitmap(tv_tag.getWidth(), tv_tag.getHeight(), Bitmap.Config.ARGB_8888);
        tv_tag.draw(new Canvas(bitmap));
        touchView.setBackground(new BitmapDrawable(bitmap));

        touchView.setLimitsX(0, windowWidth);
        touchView.setLimitsY(0, windowHeight - dp100 / 2);
        touchView.setOnLimitsListener(new TouchView.OnLimitsListener() {
            @Override
            public void OnOutLimits(float x, float y) {
                tv_hint_delete.setTextColor(Color.RED);
            }

            @Override
            public void OnInnerLimits(float x, float y) {
                tv_hint_delete.setTextColor(Color.WHITE);
            }
        });
        touchView.setOnTouchListener(new TouchView.OnTouchListener() {
            @Override
            public void onDown(TouchView view, MotionEvent event) {
                tv_hint_delete.setVisibility(View.VISIBLE);
//                changeMode(false);
            }

            @Override
            public void onMove(TouchView view, MotionEvent event) {

            }

            @Override
            public void onUp(TouchView view, MotionEvent event) {
                tv_hint_delete.setVisibility(View.GONE);
//                changeMode(true);
                if (view.isOutLimits()) {
                    activity_show_rl_big.removeView(view);
                }
            }
        });

        activity_show_rl_big.addView(touchView);

        et_tag.setText("");
        tv_tag.setText("");
    }

    private void startAnim(float start, float end, AnimatorListenerAdapter listenerAdapter) {

        ValueAnimator va = ValueAnimator.ofFloat(start, end).setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                rl_edit_text.setY(value);
            }
        });
        if (listenerAdapter != null) va.addListener(listenerAdapter);
        va.start();
    }



    private void initView() {
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        activity_img_show_img=findViewById(R.id.activity_img_show_img);
//        iv_show_next=findViewById(R.id.iv_show_next);
        iv_show_delete=findViewById(R.id.iv_show_delete);
        activity_show_rl_big=findViewById(R.id.activity_show_rl_big);
        rl_pen=findViewById(R.id.rl_pen);
        ll_color=findViewById(R.id.ll_color);
        mypaintview=findViewById(R.id.activity_show_mypaintview);
        rl_back = findViewById(R.id.rl_back);
        tv_finish_video = findViewById(R.id.tv_finish_video);
        rl_edit_text = findViewById(R.id.rl_edit_text);
        rl_text = findViewById(R.id.rl_text);
        tv_finish = findViewById(R.id.tv_finish);
        tv_close = findViewById(R.id.tv_close);
        et_tag = findViewById(R.id.et_tag);
        tv_tag = findViewById(R.id.tv_tag);
        tv_hint_delete = findViewById(R.id.tv_hint_delete);
        activity_img_show_cut = findViewById(R.id.activity_img_show_cut);
        rl_text_cut = findViewById(R.id.rl_text_cut);
        textureView_cut = findViewById(R.id.textureView_cut);
    }
    private Bitmap loadBitmapFromView(View v) {
        if (activity_img_show_cut.getVisibility()==View.VISIBLE){
            int w = v.getWidth();
            int h = v.getHeight();
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            float[] cutArr = activity_img_show_cut.getCutArr();
//            Bitmap cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0],(int)cutArr[1],activity_img_show_cut.getRectWidth(),activity_img_show_cut.getRectHeight());
//            Bitmap cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0],activity_img_show_cut.getRectHeight()-(int)cutArr[1],(int)cutArr[2]-(int)cutArr[0],activity_img_show_cut.getRectHeight()-((int)cutArr[3]-(int)cutArr[1]));
            Log.e("TAG",cutArr[0]+"-----"+cutArr[1]+"-----"+cutArr[2]+"-----"+cutArr[3]+"-----");
            Canvas c = new Canvas(bmp);
            c.drawColor(Color.WHITE);
//            /** 如果不设置canvas画布为白色，则生成透明 */
            v.layout(0, 0,w, h);
            v.draw(c);
            int px= (int)DimenUtils.dp2px(60);
            Bitmap cutBitmap=null;

            if ((30+cutArr[2])>=bmp.getWidth()||(px+cutArr[3])>bmp.getHeight()){
//                cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-((int)cutArr[0]+30),(int)cutArr[3]-(int)cutArr[1]);
                if ((30+cutArr[2])>=bmp.getWidth()&&(px+cutArr[3])>bmp.getHeight()){
                    cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-((int)cutArr[0]+30),(int)cutArr[3]-((int)cutArr[1]+px));
                }else{
                    if ((30+cutArr[2])>=bmp.getWidth()){
                        cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-((int)cutArr[0]+30),(int)cutArr[3]-(int)cutArr[1]);
                    }else{
                        cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-(int)cutArr[0],(int)cutArr[3]-((int)cutArr[1]+px));
                    }
                }
            }else{
                cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-(int)cutArr[0],(int)cutArr[3]-(int)cutArr[1]);
            }
//            if ((px+cutArr[3])>bmp.getHeight()){
//                cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-(int)cutArr[0],activity_img_show_cut.getRectHeight()-px);
//            }else{
//                cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-(int)cutArr[0],activity_img_show_cut.getRectHeight()-px);
//            }

//            if ((30+(int)cutArr[2])>=bmp.getWidth()){
//                cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,bmp.getWidth()-((int)cutArr[0]+30),(int)cutArr[3]-(int)cutArr[1]);
//            }else{
//                cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-(int)cutArr[0],(int)cutArr[3]-(int)cutArr[1]);
//            }
//            if ((px+(int)cutArr[3])>bmp.getHeight()){
//                cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-(int)cutArr[0],bmp.getHeight()-((int)cutArr[1]+px));
//            }else{
//                cutBitmap=Bitmap.createBitmap(bmp,(int)cutArr[0]+30,(int)cutArr[1]+px,(int)cutArr[2]-(int)cutArr[0],(int)cutArr[3]-(int)cutArr[1]);
//            }
            return cutBitmap;

        }else{
            int w = v.getWidth();
            int h = v.getHeight();
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);

            c.drawColor(Color.WHITE);
//            /** 如果不设置canvas画布为白色，则生成透明 */
//
            v.layout(0, 0, w, h);
            v.draw(c);
            return bmp;
        }
    }



    private String editVideo(){

        //得到裁剪后的margin值
        float[] cutArr = activity_img_show_cut.getCutArr();
        float left = cutArr[0];
        float top = cutArr[1];
        float right = cutArr[2];
        float bottom = cutArr[3];
        int cutWidth = activity_img_show_cut.getRectWidth();
        int cutHeight= activity_img_show_cut.getRectHeight();

        //计算宽高缩放比
        float leftPro = left/cutWidth;
        float topPro = top/cutHeight;
        float rightPro = right/cutWidth;
        float bottomPro = bottom/cutHeight;


//        return  myVideoEditor.executeCropVideoFrame(path, cropWidth, cropHeight, x, y);
        return  "";
    }




    private static String saveImage(Bitmap bmp, int quality) {
        if (bmp == null) {
            return null;
        }
        File appDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (appDir == null) {
            return null;
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    private int[] drawableBg = new int[]{R.drawable.color1, R.drawable.color2, R.drawable.color3, R.drawable.color4, R.drawable.color5};
    private int[] colors = new int[]{R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5};
    private int currentColorPosition=0;
    private void initColors() {

        int dp20 = (int) getResources().getDimension(R.dimen.dp20);
        int dp25 = (int) getResources().getDimension(R.dimen.dp25);

        for (int x = 0; x < drawableBg.length; x++) {
            RelativeLayout relativeLayout = new RelativeLayout(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            relativeLayout.setLayoutParams(layoutParams);

            View view = new View(this);
            view.setBackgroundDrawable(getResources().getDrawable(drawableBg[x]));
            RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(dp20, dp20);
            layoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
            view.setLayoutParams(layoutParams1);
            relativeLayout.addView(view);

            final View view2 = new View(this);
            view2.setBackgroundResource(R.mipmap.color_click);
            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(dp25, dp25);
            layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
            view2.setLayoutParams(layoutParams2);
            if (x != 0) {
                view2.setVisibility(View.GONE);
            }
            relativeLayout.addView(view2);

            final int position = x;
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentColorPosition != position) {
                        view2.setVisibility(View.VISIBLE);
                        ViewGroup parent = (ViewGroup) v.getParent();
                        ViewGroup childView = (ViewGroup) parent.getChildAt(currentColorPosition);
                        childView.getChildAt(1).setVisibility(View.GONE);
//                        tv_video.setNewPaintColor(getResources().getColor(colors[position]));
                        mypaintview.setPenColor(getResources().getColor(colors[position]));
                        currentColorPosition = position;
                    }
//                    mypaintview.setVisibility(View.VISIBLE);
                }
            });

            ll_color.addView(relativeLayout, x);
        }
    }

    public void showSoftInputFromWindow(EditText editText){
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if (rl_edit_text.getVisibility()==View.VISIBLE){
                    rl_edit_text.setVisibility(View.GONE);
                }else{
                    finish();
                }
                break;
        }
//        return super.onKeyDown(keyCode, event);
        return false;
    }
}
