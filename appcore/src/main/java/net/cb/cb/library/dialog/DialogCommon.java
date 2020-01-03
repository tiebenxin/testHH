package net.cb.cb.library.dialog;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;


import net.cb.cb.library.R;
import net.cb.cb.library.base.BaseDialog;

/**
 * @author Liszt
 * @date 2019/12/4
 * Description 默认dialog
 */
public class DialogCommon extends BaseDialog {

    private TextView tvTitle, tvContent, tvLeft, tvRight;
    private IDialogListener listener;
    private boolean isRight;//是否是右边按钮为确定按钮
    private int colorSure = Color.parseColor("#32b053");
    private int colorCancel = Color.parseColor("#828282");

    public DialogCommon(Context context) {
        this(context, R.style.MyDialogTheme);
    }

    public DialogCommon(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void initView() {
        setContentView(R.layout.dialog_default);
        tvTitle = findViewById(R.id.tv_title);
        tvContent = findViewById(R.id.tv_content);
        tvLeft = findViewById(R.id.tv_left);
        tvRight = findViewById(R.id.tv_right);
        tvLeft.setOnClickListener(this);
        tvRight.setOnClickListener(this);
    }

    /**
     * @param isHas       是否有title
     * @param isRightSure 是否右边按钮是确定键
     */
    public DialogCommon setTitleAndSure(boolean isHas, boolean isRightSure) {
        tvTitle.setVisibility(isHas ? View.VISIBLE : View.GONE);
        isRight = isRightSure;
        tvRight.setTextColor(isRightSure ? colorSure : colorCancel);
        tvLeft.setTextColor(isRightSure ? colorCancel : colorSure);
        return this;
    }

    public DialogCommon setTitle(String title) {
        if (tvTitle != null && !TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(title);
        }
        return this;
    }


    public DialogCommon setLeft(String txt) {
        tvLeft.setText(txt);
        return this;
    }

    public DialogCommon setRight(String txt) {
        tvRight.setText(txt);
        return this;
    }

    /**
     * @param center 是否内容居中，默认居右
     */
    public DialogCommon setContent(String txt, boolean center) {
        tvContent.setText(txt);
        if (center) {
            tvContent.setGravity(Gravity.CENTER);
        }
        return this;
    }


    @Override
    public void processClick(View view) {
        int id = view.getId();
        if (id == tvLeft.getId()) {
            if (listener != null) {
                if (isRight) {
                    listener.onCancel();
                } else {
                    listener.onSure();
                }
                dismiss();
            }
        } else if (id == tvRight.getId()) {
            if (listener != null) {
                if (isRight) {
                    listener.onSure();
                } else {
                    listener.onCancel();
                }
                dismiss();
            }
        }
    }

    public DialogCommon setListener(IDialogListener l) {
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
