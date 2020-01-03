package com.hm.cxpay.dailog;

import android.content.Context;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.hm.cxpay.R;
import com.hm.cxpay.bean.BankBean;
import com.hm.cxpay.ui.redenvelope.AdapterSelectPayStyle;

import net.cb.cb.library.base.BaseDialog;

import java.util.List;

/**
 * @author Liszt
 * @date 2019/12/4
 * Description 选择支付方式弹窗
 */
public class DialogSelectPayStyle extends BaseDialog {

    private ImageView ivBack;
    private RecyclerView recyclerView;
    private AdapterSelectPayStyle adapterBankList;
    private AdapterSelectPayStyle.ISelectPayStyleListener listener;

    public DialogSelectPayStyle(Context context, int theme) {
        super(context, theme);
    }

    public DialogSelectPayStyle(Context context) {
        this(context, R.style.MyDialogTheme);
    }

    public void bindData(List<BankBean> list, BankBean selectBank) {
        if (adapterBankList != null && list != null) {
            if (selectBank != null) {
                int position = list.indexOf(selectBank);
                if (position >= 0) {
                    adapterBankList.setSelectPosition(position + 1);
                }
            }
            adapterBankList.bindData(list);
        }
    }

    @Override
    public void initView() {
        setContentView(R.layout.dialog_select_pay_style);
        ivBack = findViewById(R.id.iv_close);
        recyclerView = findViewById(R.id.recyclerView);
        //添加分割线
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        adapterBankList = new AdapterSelectPayStyle(getContext());
        recyclerView.setAdapter(adapterBankList);
        if (listener != null) {
            adapterBankList.setListener(listener);
        }
        ivBack.setOnClickListener(this);
    }

    @Override
    public void processClick(View view) {
        int id = view.getId();
        if (id == ivBack.getId()) {
            dismiss();
        }
    }

    public void setListener(AdapterSelectPayStyle.ISelectPayStyleListener l) {
        listener = l;
        adapterBankList.setListener(listener);
    }


}
