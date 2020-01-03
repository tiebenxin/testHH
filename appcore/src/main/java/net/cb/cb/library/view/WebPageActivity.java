package net.cb.cb.library.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import net.cb.cb.library.R;


/***
 * 专门用来查看网页的窗口
 *
 * @author 姜永健
 * @date 2016年7月21日
 */
public class WebPageActivity extends AppActivity {
    private static final String TAG = "WebPageActivity";
    /**
     * 参数:打开的url
     */
    public static final String AGM_URL = "url";
    /***
     * 参数:界面标题
     */
    public static final String AGM_TITLE = "title";
    private ActionbarView actionbar;
    private HeadView headView;
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webpage);
        headView = findViewById(R.id.headView);
        actionbar = headView.getActionbar();
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        initEvent();
        initData();
    }

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
    }

    private void initData() {
        Intent it = getIntent();
        if (it != null) {
      /* 	String title = it.getStringExtra(AGM_TITLE);
       	title=title==null?"消息详情":title;
            actionbar.setTitle(title);*/

            String url = it.getStringExtra(AGM_URL);//+"/"+language;
            if (url != null)
                initContentWeb(webView, url);
        }

    }

    private void initContentWeb(WebView webView, String url) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavascriptInterface(this), "imagelistner");
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        webView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                return true;
            }
        });
        Log.i(TAG, "装载网页>>>>:" + url);
        webView.loadUrl(url);
    }

    private void addImageClickListner(WebView webView) {
        webView.loadUrl("javascript:(function(){" + "var objs = document.getElementsByTagName(\"img\"); "
                + "for(var i=0;i<objs.length;i++)  " + "{" + "    objs[i].onclick=function()  " + "    {  "
                + "        window.imagelistner.openImage(this.src);  " + "    }  " + "}" + "})()");
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // return super.shouldOverrideUrlLoading(view, url);
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url));
                startActivity(intent);
                return true;
            }
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            view.getSettings().setJavaScriptEnabled(true);
            super.onPageFinished(view, url);
            String title = view.getTitle();
            actionbar.setTitle(title);
            //  addImageClickListner(view);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            view.getSettings().setJavaScriptEnabled(true);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

        }
    }

    public class JavascriptInterface {
        private Context context;

        public JavascriptInterface(Context context) {
            this.context = context;
        }

        @android.webkit.JavascriptInterface
        public void openImage(String img) {
            turnToPhotoScan(img);
        }

        /**
         * 跳转到图片查看器
         *
         * @param url
         */
        private void turnToPhotoScan(String url) {
 /*           ArrayList<String> urlPath = new ArrayList<>();
            urlPath.add(url);
            Intent it = new Intent(WebViewPageActivity.this, PhotoScanActivity.class);
            it.putStringArrayListExtra(PhotoScanActivity.KEY_ALL_PICS_PATH, urlPath);
            it.putExtra(PhotoScanActivity.KEY_SHOW_POSITION, 0);
            startActivity(it);*/
        }
    }
}
