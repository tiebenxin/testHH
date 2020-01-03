package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yanlong.im.chat.ChatEnum;
import com.yanlong.im.chat.bean.MsgAllBean;
import com.yanlong.im.chat.server.UpLoadService;

import net.cb.cb.library.utils.LogUtil;
import net.cb.cb.library.view.MultiListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final ICellEventListener eventListener;
    private List<MsgAllBean> mList;
    private FactoryChatCell factoryChatCell;
    private final boolean isGroup;//是否群聊

    private Map<Integer, View> viewMap = new HashMap<>();

    public MessageAdapter(Context c, ICellEventListener l, boolean isG) {
        context = c;
        eventListener = l;
        mList = new ArrayList<>();
        isGroup = isG;

    }

    public MessageAdapter setCellFactory(FactoryChatCell factory) {
        factoryChatCell = factory;
        return this;
    }

    public void bindData(List<MsgAllBean> list) {
//        if (mList == null) {
//            mList = list;
//        } else {
//            if (page == 0) {
//                mList.clear();
//            }
//            mList.addAll(0, list);
//            mList = list;
//        }
        mList = list;
        this.notifyDataSetChanged();
    }

    public boolean isGroup() {
        return isGroup;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        ChatEnum.EChatCellLayout layout = ChatEnum.EChatCellLayout.fromOrdinal(viewType);
        View view = LayoutInflater.from(context).inflate(layout.LayoutId, viewGroup, false);
        ChatCellBase cell = factoryChatCell.createCell(layout, view);
        return cell;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ChatCellBase cellBase = (ChatCellBase) viewHolder;
        cellBase.putMessage(mList.get(position), position);
        viewMap.put(position, cellBase.itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position, @NonNull List payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(viewHolder, position, payloads);
        } else {
            MsgAllBean msg = mList.get(position);
            if (msg.getMsg_type() == ChatEnum.EMessageType.IMAGE) {
                ChatCellImage imageCell = (ChatCellImage) viewHolder;
                imageCell.updateMessage(msg);
                int progress = UpLoadService.getProgress(msg.getMsg_id());
                imageCell.updateProgress(msg.getSend_state(), progress);
            } else if (msg.getMsg_type() == ChatEnum.EMessageType.VOICE) {
                ChatCellVoice voiceCell = (ChatCellVoice) viewHolder;
                voiceCell.updateVoice();
            } else if(msg.getMsg_type() == ChatEnum.EMessageType.MSG_VIDEO){
                ChatCellVideo videoCell=(ChatCellVideo) viewHolder;
                videoCell.updateMessage(msg);
                int progress = UpLoadService.getProgress(msg.getMsg_id());
                videoCell.updateProgress(msg.getSend_state(), progress);
            }
            else {
                onBindViewHolder(viewHolder, position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;

    }

    @Override
    public int getItemViewType(int position) {
        if (mList != null && mList.size() > position) {
            return mList.get(position).getChatCellLayoutId().ordinal();
        }
        return super.getItemViewType(position);
    }

    //获取某位置消息
    public MsgAllBean getPositionMessage(int position) {
        if (mList != null && mList.size() > position) {
            return mList.get(position);
        }
        return null;
    }

    //局部刷新
    public void updateItemAndRefresh(MsgAllBean bean) {
        int position = mList.indexOf(bean);
        if (position >= 0 && position < mList.size()) {
            mList.remove(position);
            mList.add(position, bean);
//            mList.set(position,bean);
            this.notifyItemChanged(position, position);
        }
    }

    public View getItemViewByPosition(int position) {
        if (!viewMap.isEmpty()) {
            return viewMap.get(position);
        }
        return null;
    }

}
