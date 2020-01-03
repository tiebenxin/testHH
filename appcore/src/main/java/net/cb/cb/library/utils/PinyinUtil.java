package net.cb.cb.library.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-12-06
 * @updateAuthor
 * @updateDate
 * @description 拼音工具类
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class PinyinUtil {

    private static String[] surName = {
            "赵 Z", "钱 Q", "孙 S", "李 L", "周 Z", "吴 W", "郑 Z", "王 W", "冯 F", "陈 C",
            "楮 C", "卫 W", "蒋 J", "沈 S", "韩 H", "杨 Y", "朱 Z", "秦 Q", "尤 Y", "许 X",
            "何 H", "吕 L", "施 S", "张 Z", "孔 K", "曹 C", "严 Y", "华 H", "金 J", "魏 W",
            "陶 T", "姜 J", "戚 Q", "谢 X", "邹 Z", "喻 Y", "柏 B", "水 S", "窦 D", "章 Z",
            "云 Y", "苏 S", "潘 P", "葛 G", "奚 X", "范 F", "彭 P", "郎 L", "鲁 L", "韦 W",
            "昌 C", "马 M", "苗 M", "凤 F", "花 H", "方 F", "俞 Y", "任 R", "袁 Y", "柳 L",
            "酆 L", "鲍 B", "史 S", "唐 T", "费 F", "廉 L", "岑 C", "薛 X", "雷 L", "贺 H",
            "倪 N", "汤 T", "滕 T", "殷 Y", "罗 L", "毕 B", "郝 H", "邬 W", "安 A", "常 C",
            "乐 L", "于 Y", "时 S", "傅 F", "皮 P", "卞 B", "齐 Q", "康 K", "伍 W", "余 Y",
            "元 Y", "卜 B", "顾 G", "孟 M", "平 P", "黄 H", "和 H", "穆 M", "萧 X", "尹 Y",
            "姚 Y", "邵 S", "湛 Z", "汪 W", "祁 Q", "毛 M", "禹 Y", "狄 D", "米 M", "贝 B",
            "明 M", "臧 Z", "计 J", "伏 F", "成 C", "戴 D", "谈 T", "宋 S", "茅 M", "庞 P",
            "熊 X", "纪 J", "舒 S", "屈 Q", "项 X", "祝 Z", "董 D", "梁 L", "杜 D", "阮 R",
            "蓝 L", "闽 M", "席 X", "季 J", "麻 M", "强 Q", "贾 J", "路 L", "娄 L", "危 W",
            "江 J", "童 T", "颜 Y", "郭 G", "梅 M", "盛 S", "林 L", "刁 D", "锺 Z", "徐 X",
            "丘 Q", "骆 L", "高 G", "夏 X", "蔡 C", "田 T", "樊 F", "胡 H", "凌 N", "霍 H",
            "虞 Y", "万 W", "支 Z", "柯 K", "昝 Z", "管 G", "卢 L", "莫 M", "经 J", "房 F",
            "裘 Q", "缪 M", "干 G", "解 J", "应 Y", "宗 Z", "丁 D", "宣 X", "贲 B", "邓 D",
            "郁 Y", "单 D", "杭 H", "洪 H", "包 B", "诸 Z", "左 Z", "石 S", "崔 C", "吉 J",
            "钮 N", "龚 G", "程 C", "嵇 J", "邢 X", "滑 H", "裴 P", "陆 L", "荣 R", "翁 W",
            "荀 X", "羊 Y", "於 Y", "惠 H", "甄 Z", "麹 Q", "家 J", "封 F", "芮 R", "羿 Y",
            "储 C", "靳 X", "汲 J", "邴 B", "糜 M", "松 S", "井 J", "段 D", "富 F", "巫 W",
            "乌 W", "焦 J", "巴 B", "弓 G", "牧 M", "隗 W", "山 S", "谷 G", "车 C", "侯 H",
            "宓 F", "蓬 P", "全 Q", "郗 C", "班 B", "仰 Y", "秋 Q", "仲 Z", "伊 Y", "宫 G",
            "宁 N", "仇 C", "栾 L", "暴 B", "甘 G", "斜 X", "厉 L", "戎 J", "祖 Z", "武 W",
            "符 F", "刘 L", "景 J", "詹 Z", "束 S", "龙 L", "叶 Y", "幸 X", "司 S", "韶 S",
            "郜 G", "黎 L", "蓟 J", "薄 B", "印 Y", "宿 S", "白 B", "怀 H", "蒲 P", "邰 T",
            "从 C", "鄂 Y", "索 S", "咸 X", "籍 J", "赖 L", "卓 Z", "蔺 L", "屠 T", "蒙 M",
            "池 C", "乔 Q", "阴 Y", "郁 Y", "胥 X", "能 N", "苍 C", "双 S", "闻 W", "莘 S",
            "党 D", "翟 Z", "谭 T", "贡 G", "劳 L", "逄 F", "姬 J", "申 S", "扶 F", "堵 D",
            "冉 R", "宰 Z", "郦 L", "雍 Y", "郤 X", "璩 Q", "桑 S", "桂 K", "濮 P", "牛 N",
            "寿 S", "通 T", "边 B", "扈 H", "燕 Y", "冀 J", "郏 J", "浦 P", "尚 S", "农 N",
            "温 W", "别 B", "庄 Z", "晏 Y", "柴 C", "瞿 Q", "阎 Y", "充 C", "慕 M", "连 L",
            "茹 R", "习 X", "宦 H", "艾 A", "鱼 Y", "容 R", "向 X", "古 G", "易 Y", "慎 S",
            "戈 G", "廖 L", "庾 Y", "终 Z", "暨 J", "居 J", "衡 H", "步 B", "都 D", "耿 G",
            "满 M", "弘 H", "匡 K", "国 G", "文 W", "寇 K", "广 G", "禄 L", "阙 Q", "东 D",
            "欧 O", "殳 S", "沃 W", "利 L", "蔚 W", "越 Y", "夔 K", "隆 L", "师 S", "巩 K",
            "厍 K", "聂 N", "晁 C", "勾 G", "敖 A", "融 R", "冷 L", "訾 Z", "辛 X", "阚 K",
            "那 N", "简 J", "饶 R", "空 K", "曾 Z", "毋 W", "沙 S", "乜 N", "养 Y", "鞠 J",
            "须 X", "丰 F", "巢 C", "关 G", "蒯 K", "相 X", "查 Z", "后 H", "荆 J", "红 H",
            "游 Y", "竺 Z", "权 Q", "逑 Q", "盖 G", "益 Y", "桓 H", "公 G", "仉 Z", "督 D",
            "晋 J", "楚 C", "阎 Y", "法 F", "汝 R", "鄢 Y", "涂 T", "钦 Q", "归 G", "海 H",
            "岳 Y", "帅 S", "缑 G", "亢 K", "况 K", "后 H", "有 Y", "琴 Q", "商 S", "牟 M",
            "佘 S", "佴 N", "伯 B", "赏 S", "墨 M", "哈 H", "谯 Q", "笪 D", "年 N", "爱 A",
            "阳 Y", "佟 D", "种 C"
//            "万俟", "司马", "上官", "欧阳", "夏侯",
//            "诸葛", "闻人", "东方", "赫连", "皇甫",
//            "尉迟", "公羊", "澹台", "公冶", "宗政",
//            "濮阳", "淳于", "单于", "太叔", "申屠",
//            "公孙", "仲孙", "轩辕", "令狐", "锺离",
//            "宇文", "长孙", "慕容", "鲜于", "闾丘",
//            "司徒", "司空", "丌官", "司寇", "南宫",
//            "子车", "颛孙", "端木", "巫马", "公西",
//            "漆雕", "乐正", "壤驷", "公良", "拓拔",
//            "夹谷", "宰父", "谷梁", "段干", "百里",
//            "东郭", "南门", "呼延", "羊舌", "微生",
//            "梁丘", "左丘", "东门", "西门"
    };

    /**
     * 获取姓名是多音字的第一个字母
     * @param value
     * @return
     */
    public static String getUserName(String value) {
        String result = "";
        for (String str : surName) {
            if (str.contains(value)) {
                String firstName[] = str.split(" ");
                result = firstName[1];
                break;
            }
        }
        return result;
    }


    /**
     * 汉字转换位汉语拼音首字母，英文字符不变，特殊字符丢失 支持多音字，生成方式如（长沙市长:cssc,zssz,zssc,cssz）
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToFirstSpell(String chines) {
        StringBuffer pinyinName = new StringBuffer();
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    // 取得当前汉字的所有全拼
                    String[] strs = PinyinHelper.toHanyuPinyinStringArray(
                            nameChar[i], defaultFormat);
                    if (strs != null) {
                        for (int j = 0; j < strs.length; j++) {
                            // 取首字母
                            pinyinName.append(strs[j].charAt(0));
                            if (j != strs.length - 1) {
                                pinyinName.append(",");
                            }
                        }
                    }
                    // else {
                    // pinyinName.append(nameChar[i]);
                    // }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinyinName.append(nameChar[i]);
            }
            pinyinName.append(" ");
        }
        // return pinyinName.toString();
        return parseTheChineseByObject(discountTheChinese(pinyinName.toString()));
    }

    /**
     * 解析并组合拼音，对象合并方案(推荐使用)
     *
     * @return
     */
    private static String parseTheChineseByObject(
            List<Map<String, Integer>> list) {
        Map<String, Integer> first = null; // 用于统计每一次,集合组合数据
        // 遍历每一组集合
        for (int i = 0; i < list.size(); i++) {
            // 每一组集合与上一次组合的Map
            Map<String, Integer> temp = new Hashtable<String, Integer>();
            // 第一次循环，first为空
            if (first != null) {
                // 取出上次组合与此次集合的字符，并保存
                for (String s : first.keySet()) {
                    for (String s1 : list.get(i).keySet()) {
                        String str = s + s1;
                        temp.put(str, 1);
                    }
                }
                // 清理上一次组合数据
                if (temp != null && temp.size() > 0) {
                    first.clear();
                }
            } else {
                for (String s : list.get(i).keySet()) {
                    String str = s;
                    temp.put(str, 1);
                }
            }
            // 保存组合数据以便下次循环使用
            if (temp != null && temp.size() > 0) {
                first = temp;
            }
        }
        String returnStr = "";
        if (first != null) {
            // 遍历取出组合字符串
            for (String str : first.keySet()) {
                returnStr += (str + ",");
            }
        }
        if (returnStr.length() > 0) {
            returnStr = returnStr.substring(0, returnStr.length() - 1);
        }
        return returnStr;
    }

    /**
     * 去除多音字重复数据
     *
     * @param theStr
     * @return
     */
    private static List<Map<String, Integer>> discountTheChinese(String theStr) {
        // 去除重复拼音后的拼音列表
        List<Map<String, Integer>> mapList = new ArrayList<Map<String, Integer>>();
        // 用于处理每个字的多音字，去掉重复
        Map<String, Integer> onlyOne = null;
        String[] firsts = theStr.split(" ");
        // 读出每个汉字的拼音
        for (String str : firsts) {
            onlyOne = new Hashtable<String, Integer>();
            String[] china = str.split(",");
            // 多音字处理
            for (String s : china) {
                Integer count = onlyOne.get(s);
                if (count == null) {
                    onlyOne.put(s, new Integer(1));
                } else {
                    onlyOne.remove(s);
                    count++;
                    onlyOne.put(s, count);
                }
            }
            mapList.add(onlyOne);
        }
        return mapList;
    }

}
