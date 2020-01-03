package com.yanlong.im.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配工具类
 * 
 * @Description TODO
 * @author CodeApe
 * @version 1.0
 * @date 2013-11-9
 * @Copyright: Copyright (c) 2013 Shenzhen Tentinet Technology Co., Ltd. Inc.
 *             All rights reserved.
 * 
 */
public class PatternUtil {

	/** Emoji表情正则表达式 */
	public static final String PATTERN_FACE_EMOJI = "\\[emoji_[0-9]{3}\\]";
	/** 自定义表情正则表达式 */
	public static final String PATTERN_FACE_CUSTOMER = "\\[animation_[a-z]{5}_[0-9]{3}\\]";
	/** Emoji表情名字长度 */
	public static final int FACE_EMOJI_LENGTH = 11;
	/** 自定义表情名字长度 */
	public static final int FACE_CUSTOMER_LENGTH = 21;

	/**
	 * 正则校验手机号码
	 * 
	 * @version 1.0
	 * @createTime 2013-11-9,下午9:34:24
	 * @updateTime 2013-11-9,下午9:34:24
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param mobileNum
	 *            手机号码
	 * @return true 正确手机号码；false 非法手机号码
	 */
	public static boolean isValidMobilePhone(String mobileNum) {
//		LogUtil.out("mobile Number", mobileNum, false);
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9])|(17[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobileNum);
		return m.matches();
	}

	/**
	 * 判断是否纯数字
	 * 
	 * @version 1.0
	 * @createTime 2013-12-18,上午10:42:31
	 * @updateTime 2013-12-18,上午10:42:31
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param source
	 *            需要判断的源字符串
	 * @return true 纯数字，false非纯数字
	 */
	public static boolean isNumer(String source) {
		Pattern p = Pattern.compile("\\d*");
		Matcher m = p.matcher(source);
		return m.matches();
	}

	/**
	 * 判断是纯字母串
	 * 
	 * @version 1.0
	 * @createTime 2013-12-18,上午10:45:33
	 * @updateTime 2013-12-18,上午10:45:33
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param source
	 *            判断的源字符串
	 * @return true是纯字母字符串，false非纯字母字符串
	 */
	public static boolean isChar(String source) {
		Pattern p = Pattern.compile("[a-z]*[A-Z]*");
		Matcher m = p.matcher(source);
		return m.matches();
	}

	/**
	 * 是否纯特殊符号串
	 * 
	 * @version 1.0
	 * @createTime 2013-12-18,上午10:47:36
	 * @updateTime 2013-12-18,上午10:47:36
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param source
	 *            纯特殊符号串
	 * @return true纯特殊符号串，false非纯特殊符号串
	 */
	public static boolean isSymbol(String source) {
		Pattern p = Pattern.compile("[{\\[(<~!@#$%^&*()_+=-`|\"?,./;'\\>)\\]}]*");
		Matcher m = p.matcher(source);
		return m.matches();
	}

	/**
	 * 是否合法帐号
	 * 
	 * @version 1.0
	 * @createTime 2013-12-18,下午4:43:04
	 * @updateTime 2013-12-18,下午4:43:04
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param source 帐号
	 * @return
	 */
	public static boolean isValidAccount(String source) {
		Pattern p = Pattern.compile("^(?![0-9])[a-zA-Z0-9]+$");
		Matcher m = p.matcher(source);
		return m.matches() && source.length() >= 2 && source.length() <= 16;
	}

	/**
	 * 是否正确密码
	 * 
	 * @version 1.0
	 * @createTime 2013-12-18,上午11:11:35
	 * @updateTime 2013-12-18,上午11:11:35
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param source
	 *            需要判断的密码
	 * @return true 合格的密码，false不合法的密码
	 */
	public static boolean isValidPassword(String source) {
		Pattern p = Pattern.compile("[\\d*[a-z]*[A-Z]*[{\\[(<~!@#$%^&*()_+=-`|\"?,./;'\\>)\\]}]*]*");
		Matcher m = p.matcher(source);
		return m.matches() && source.length() >= 5 && source.length() <= 16;
	}
	
	/**
	 * 是否是正确的邮箱格式
	 *
	 * @version 1.0
	 * @createTime 2014年1月22日,上午11:54:05
	 * @updateTime 2014年1月22日,上午11:54:05
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 *
	 * @param source 需要验证的邮箱
	 * @return
	 */
	public static boolean isValidEmail(String source) {
		Pattern p = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
		Matcher m = p.matcher(source);
		return m.matches();
	}

	/**
	 * 判断是否符合正则的字符串
	 * 
	 * @version 1.0
	 * @createTime 2013-12-18,上午11:18:04
	 * @updateTime 2013-12-18,上午11:18:04
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param source
	 *            需要判断的源字符串
	 * @param pattern
	 *            用于判读的正则表达式
	 * @return true if the source is valid of pattern,else return false
	 */
	public static boolean isValidPattern(String source, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(source);
		return m.matches();
	}

	/**
	 * 判断是否是一个表情
	 * 
	 * @version 1.0
	 * @createTime 2013-11-23,下午11:55:53
	 * @updateTime 2013-11-23,下午11:55:53
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param name
	 *            表情名字
	 * @return true 是表情，false不是表情
	 */
	public static boolean isExpression(String name) {
		Pattern p = Pattern.compile(PATTERN_FACE_EMOJI);
		Matcher m = p.matcher(name);
		return m.matches();
	}


}
