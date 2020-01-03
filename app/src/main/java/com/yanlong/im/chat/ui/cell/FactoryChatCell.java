package com.yanlong.im.chat.ui.cell;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.yanlong.im.chat.ChatEnum;

import static com.yanlong.im.chat.ChatEnum.EChatCellLayout.IMAGE_RECEIVED;
import static com.yanlong.im.chat.ChatEnum.EChatCellLayout.IMAGE_SEND;
import static com.yanlong.im.chat.ChatEnum.EChatCellLayout.RED_ENVELOPE_RECEIVED;
import static com.yanlong.im.chat.ChatEnum.EChatCellLayout.TEXT_RECEIVED;
import static com.yanlong.im.chat.ChatEnum.EChatCellLayout.TEXT_SEND;

/*
 * @author Liszt
 * Description  ChatCell 工厂类
 * */
public class FactoryChatCell {

    private final Context mContext;
    private final ICellEventListener mListener;
    private final MessageAdapter mAdapter;
    private int position;

    public FactoryChatCell(Context context, MessageAdapter adapter, ICellEventListener listener) {
        mContext = context;
        mListener = listener;
        mAdapter = adapter;
    }

    public void setPosition(int p) {
        position = p;
    }


    /*
     * 3. 根据布局创建cell实例对象
     * */
//    public ChatCellBase createCell(ChatEnum.EChatCellLayout layout, ViewGroup viewGroup) {
//        ChatCellBase cell = null;
//        switch (layout) {
//            case TEXT_RECEIVED:
//            case TEXT_SEND:
//                cell = new ChatCellText(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case IMAGE_RECEIVED:
//            case IMAGE_SEND:
//                cell = new ChatCellImage(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case RED_ENVELOPE_RECEIVED:
//            case RED_ENVELOPE_SEND:
//                cell = new ChatCellRedEnvelope(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case TRANSFER_RECEIVED:
//            case TRANSFER_SEND:
//                cell = new ChatCellRedEnvelope(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case NOTICE:
//                cell = new ChatCellNotice(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case CARD_RECEIVED:
//            case CARD_SEND:
//                cell = new ChatCellBusinessCard(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case VOICE_RECEIVED:
//            case VOICE_SEND:
//                cell = new ChatCellVoice(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case STAMP_RECEIVED:
//            case STAMP_SEND:
//                cell = new ChatCellStamp(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case AT_RECEIVED:
//            case AT_SEND:
//                cell = new ChatCellText(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            case ASSISTANT:
//                cell = new ChatCellText(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//
//
//            case UNRECOGNIZED:
//                cell = new ChatCellUnrecognized(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//            default:
//                cell = new ChatCellUnrecognized(mContext, layout, mListener, mAdapter, viewGroup);
//                break;
//        }
//        return cell;
//
//    }

    public ChatCellBase createCell(ChatEnum.EChatCellLayout layout, View view) {
        ChatCellBase cell = null;
        switch (layout) {
            case TEXT_RECEIVED:
            case TEXT_SEND:
                cell = new ChatCellText(mContext, view, mListener, mAdapter);
                break;
            case IMAGE_RECEIVED:
            case IMAGE_SEND:
                cell = new ChatCellImage(mContext, view, mListener, mAdapter);
                break;
            case RED_ENVELOPE_RECEIVED:
            case RED_ENVELOPE_SEND:
                cell = new ChatCellRedEnvelope(mContext, view, mListener, mAdapter);
                break;
            case TRANSFER_RECEIVED:
            case TRANSFER_SEND:
                cell = new ChatCellRedEnvelope(mContext, view, mListener, mAdapter);
                break;
            case NOTICE:
                cell = new ChatCellNotice(mContext, view, mListener, mAdapter);
                break;
            case CARD_RECEIVED:
            case CARD_SEND:
                cell = new ChatCellBusinessCard(mContext, view, mListener, mAdapter);
                break;
            case VOICE_RECEIVED:
            case VOICE_SEND:
                cell = new ChatCellVoice(mContext, view, mListener, mAdapter);
                break;
            case STAMP_RECEIVED:
            case STAMP_SEND:
                cell = new ChatCellStamp(mContext, view, mListener, mAdapter);
                break;
            case AT_RECEIVED:
            case AT_SEND:
                cell = new ChatCellText(mContext, view, mListener, mAdapter);
                break;
            case ASSISTANT:
                cell = new ChatCellText(mContext, view, mListener, mAdapter);
                break;
            case LOCK:
                cell = new ChatCellLock(mContext, view, mListener, mAdapter);
                break;
            case UNRECOGNIZED:
                cell = new ChatCellUnrecognized(mContext, view, mListener, mAdapter);
                break;
            default:
                cell = new ChatCellUnrecognized(mContext, view, mListener, mAdapter);
                break;
        }
        return cell;

    }
}
