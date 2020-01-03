package net.cb.cb.library.view;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.cb.cb.library.R;

public class AlertWait2 extends DialogFragment {
    private TextView txtTitle;
    private ProgressBar progressNum;
    private net.cb.cb.library.view.WaitView viewNet;
    private FragmentManager manager;

 private boolean show=false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fgm_wait, container, false);
        this.setCancelable(false);
        txtTitle = (TextView) v.findViewById(R.id.txt_title);
        progressNum = (ProgressBar) v.findViewById(R.id.progress_num);
        viewNet = (net.cb.cb.library.view.WaitView) v.findViewById(R.id.view_net);
        return v;
    }

    public void init(FragmentManager manager) {
        this.manager = manager;
    }

    public void show() {

        show( null, false);
    }

    public void show( String titile, boolean isNum) {
        show=true;
        if (titile == null) {
            txtTitle.setVisibility(View.GONE);
        } else {
            txtTitle.setText(titile);
            txtTitle.setVisibility(View.VISIBLE);
        }
        if (isNum) {
            progressNum.setVisibility(View.VISIBLE);
            viewNet.setVisibility(View.GONE);
        } else {
            progressNum.setVisibility(View.GONE);
            viewNet.setVisibility(View.VISIBLE);
        }
        super.show(manager, "wait");
    }

    @Override
    public void dismiss() {
        if(show){
            show=false;
            super.dismiss();
        }

    }
}
