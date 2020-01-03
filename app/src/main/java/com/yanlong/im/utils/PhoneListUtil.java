package com.yanlong.im.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import net.cb.cb.library.utils.CheckPermissionUtils;
import net.cb.cb.library.utils.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneListUtil {
    private String[] permission = new String[]{Manifest.permission.READ_CONTACTS/*, Manifest.permission.WRITE_CONTACTS*/};

    private Activity activity;
    private Event event;

    public void getPhones(Activity act, Event event) {
        activity = act;
        this.event = event;

       if( CheckPermissionUtils.requestPermissions(act, permission)){
          // getAllContacts(activity.getApplicationContext());
           getPhoneContacts(activity.getApplicationContext());
       }


    }

    public void getAllContacts(Context context) {
        ArrayList<PhoneBean> contacts = new ArrayList<PhoneBean>();

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME+" DESC");
        while (cursor.moveToNext()) {
            //新建一个联系人实例
            PhoneBean temp = new PhoneBean();
            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));
            //获取联系人姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            temp.phoneremark = name;

            //获取联系人电话号码
            Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
            while (phoneCursor.moveToNext()) {
                String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phone = phone.replace("-", "");
                phone = phone.replace(" ", "");
                temp.phone = phone;
            }

            //获取联系人备注信息
            Cursor noteCursor = context.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Nickname.NAME},
                    ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'",
                    new String[]{contactId}, null);
            if (noteCursor.moveToFirst()) {
                do {
                    String note = noteCursor.getString(noteCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                    //   temp.note = note;
                    // LogUtil.getLog().i("note:", note);
                } while (noteCursor.moveToNext());
            }
            contacts.add(temp);
            //记得要把cursor给close掉
            phoneCursor.close();
            noteCursor.close();
        }
        cursor.close();
        event.onList(contacts);

    }

    public void getPhoneContacts(Context context) {

        //联系人集合
        List<PhoneBean> data = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        //搜索字段
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.Contacts.DISPLAY_NAME};
        // 获取手机联系人
        Cursor contactsCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null, null);
        if (contactsCursor != null) {
            //key: contactId,value: 该contactId在联系人集合data的index
            Map<Integer, Integer> contactIdMap = new HashMap<>();
            while (contactsCursor.moveToNext()) {
                //获取联系人的ID
                int contactId = contactsCursor.getInt(0);
                //获取联系人的姓名
                String name = contactsCursor.getString(2);
                //获取联系人的号码
                String phoneNumber = contactsCursor.getString(1);
                //号码处理
                String replace = phoneNumber.replace(" ", "").replace("-", "").replace("+", "");
                //判断号码是否符合手机号
           /*     if (CheckUtils.checkPhoneNumber(replace)) {
                    //如果联系人Map已经包含该contactId
                    if (contactIdMap.containsKey(contactId)) {
                        //得到该contactId在data的index
                        Integer index = contactIdMap.get(contactId);
                        //重新设置号码数组
                        PhoneBean contacts = data.get(index);
                        String[] mobile = contacts.getPhone();
                        String[] mobileCopy = new String[mobile.length + 1];
                        for (int i = 0; i < mobile.length; i++) {
                            mobileCopy[i] = mobile[i];
                        }
                        mobileCopy[mobileCopy.length - 1] = replace;
                        contacts.setMobile(mobileCopy);
                    } else {*/
                        //如果联系人Map不包含该contactId
                        PhoneBean contacts = new PhoneBean();
                      //  contacts.setRecordId(contactId);
                        contacts.setName(name);
                      //  String[] strings = new String[1];
                      //  strings[0] = PhoneBean;
                        contacts.setPhone(replace);
                        data.add(contacts);
                        contactIdMap.put(contactId, data.size() - 1);
                /*    }
                }*/
            }
            contactsCursor.close();
            event.onList(data);
           // syncAvatars(data);
        }
    }




    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        CheckPermissionUtils.checkPermissionResult(new CheckPermissionUtils.OnHasGetPermissionListener() {
            @Override
            public void onSuccess() {

              //  getAllContacts(activity.getApplicationContext());
                getPhoneContacts(activity.getApplicationContext());
            }

            @Override
            public void onFail() {
                LogUtil.getLog().d("xxx", "onFail: ");

            }
        }, activity.getApplicationContext(), permission, grantResults);
    }

    public class PhoneBean {
        private String phoneremark;
        private String phone;

        public String getName() {
            return phoneremark;
        }

        public void setName(String phoneremark) {
            this.phoneremark = phoneremark;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public interface Event {
        void onList(List<PhoneBean> list);
    }

}
