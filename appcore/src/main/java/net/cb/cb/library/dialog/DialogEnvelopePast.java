package net.cb.cb.library.dialog;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.cb.cb.library.R;
import net.cb.cb.library.base.BaseDialog;

/**
 * @author Liszt
 * @date 2019/12/4
 * Description 红包过期提醒
 */
public class DialogEnvelopePast extends BaseDialog {

    private TextView tvTitle, tvContent, tvRight;
    private IDialogListener listener;
    private int colorSure = Color.parseColor("#32b053");
    private int colorCancel = Color.parseColor("#828282");
    private ImageView ivClose;

    public DialogEnvelopePast(Context context) {
        this(context, R.style.MyDialogTheme);
    }

    public DialogEnvelopePast(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void initView() {
        setContentView(R.layout.dialog_envelope_past);
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        tvRight = findViewById(R.id.tv_right);
        ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(this);
        tvRight.setOnClickListener(this);
    }


    public DialogEnvelopePast setRight(String txt) {
        tvRight.setText(txt);
        return this;
    }

    public DialogEnvelopePast setContent(String txt) {
        tvContent.setText(txt);
        return this;
    }


    @Override
    public void processClick(View view) {
        int id = view.getId();
        if (id == ivClose.getId()) {
            if (listener != null) {
                listener.onCancel();
                dismiss();
            }
        } else if (id == tvRight.getId()) {
            if (listener != null) {
                listener.onSure();
                dismiss();
            }
        }
    }

    public DialogEnvelopePast setListener(IDialogListener l) {
        listener = l;
        return this;
    }

    public interface IDialogListener {
        //确定
        void onSure();

        //取消
        void onCancel();
    }
}
