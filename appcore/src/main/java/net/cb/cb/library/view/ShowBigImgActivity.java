package net.cb.cb.library.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.cb.cb.library.R;

/***
 * @author jyj
 * @date 2017/6/9
 */
public class ShowBigImgActivity extends AppActivity {
    private ImageView imgBig;
    private Button btnCommit;
    private FrameLayout frameLayout;
    private net.cb.cb.library.view.HeadView headView;
    public final static String AGM_URI = "uri_pic";
    public final static String POSTION = "postion";
    private int postion = 100;

    //自动寻找控件
    private void findViews() {
        imgBig = findViewById(R.id.img_big);
        btnCommit = findViewById(R.id.btn_commit);
        headView = findViewById(R.id.headView);
        frameLayout = findViewById(R.id.frame_layout);
    }


    //自动生成的控件事件
    private void initEvent() {
        imgBig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        int img_id = getIntent().getIntExtra(AGM_URI, 0);
        postion = getIntent().getIntExtra(POSTION, 0);
        if (img_id != 0) {
            frameLayout.setBackgroundResource(img_id);
        }

        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent().putExtra(POSTION, postion));
                finish();
            }
        });
        headView.getActionbar().setTxtRight("设置");
        headView.getActionbar().setOnListenEvent(new ActionbarView.ListenEvent() {
            @Override
            public void onBack() {
                onBackPressed();
            }

            @Override
            public void onRight() {
                setResult(RESULT_OK, new Intent().putExtra(POSTION, postion));
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_big_pic);
        findViews();
        initEvent();


    }
}
