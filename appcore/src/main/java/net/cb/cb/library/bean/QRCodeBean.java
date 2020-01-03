package net.cb.cb.library.bean;

import net.cb.cb.library.base.BaseBean;

import java.util.LinkedHashMap;
import java.util.Map;

public class QRCodeBean extends BaseBean {
    //二维码头部
    private String head;

    //功能选项
    private String function;

    //参数集合
    private LinkedHashMap<String,String> parameter = new LinkedHashMap<>();

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public LinkedHashMap<String, String> getParameter() {
        return parameter;
    }

    public void setParameter(LinkedHashMap<String, String> parameter) {
        this.parameter = parameter;
    }

    public void setParameterValue(String key,String value){
        parameter.put(key,value);
    }


    /**
     * 获取map中参数值
     * @param key
     * */
    public String getParameterValue(String key){
        if(parameter != null){
            for(Map.Entry<String, String> value: parameter.entrySet()){
                if(value.getKey().equals(key)){
                    return value.getValue();
                }
            }
        }
       return "";
    }

}
