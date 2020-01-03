package com.yanlong.im.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;

import com.yanlong.im.view.face.FaceView;
import com.yanlong.im.view.face.wight.HotelListImageSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExpressionUtil {

	/** emoji表情默认大小 */
	public static int DEFAULT_SMALL_SIZE = 14;

	/** emoji表情默认大小 */
	public static int DEFAULT_SIZE = 18;

	/** 自定义表情默认大小 */
	public static int DEFAULT_CUSTOMER_SIZE = 60;

	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
	 * 
	 * @param context
	 * @param spannableString
	 * @param patten
	 * @param start
	 * @throws SecurityException
	 * @throws NumberFormatException
	 * @throws IllegalArgumentException
	 */
	public static void dealExpression(Context context, int size, SpannableString spannableString, Pattern patten, int start) throws SecurityException,
			NumberFormatException, IllegalArgumentException {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			String key = matcher.group();
			if (matcher.start() < start) {
				continue;
			}
			Bitmap bitmap = null;
			if (FaceView.map_FaceEmoji.containsKey(key)) {
				bitmap = BitmapFactory.decodeResource(context.getResources(), Integer.parseInt(FaceView.map_FaceEmoji.get(key).toString()));
				bitmap = Bitmap.createScaledBitmap(bitmap, dip2px( context,size), dip2px( context,size), true);
//				bitmap = getBitmapFromDrawable(context, Integer.parseInt(FaceView.map_FaceEmoji.get(key).toString()));
			}
			if (bitmap != null) {
				HotelListImageSpan imageSpan = new HotelListImageSpan(context, bitmap);
				// 通过图片资源id来得到bitmap，用一个ImageSpan来包装
				int end = matcher.start() + key.length();
				// 计算该图片名字的长度，也就是要替换的字符串的长度
				// spannableString.setSpan(imageSpan, matcher.start(), end,
				// Spannable.SPAN_INCLUSIVE_EXCLUSIVE); // 将该图片替换字符串中规定的位置中
				spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 将该图片替换字符串中规定的位置中
				if (end < spannableString.length()) { // 如果整个字符串还未验证完，则继续。。
					dealExpression(context, size, spannableString, patten, end);
				}
				break;
			}
		}
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 失量图转Bitmap
	 *
	 * @param context
	 * @param drawableId
	 * @return
	 */
	public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
		Drawable drawable = ContextCompat.getDrawable(context, drawableId);
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);

			return bitmap;
		} else {
			throw new IllegalArgumentException("unsupported drawable type");
		}
	}

	/**
	 * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
	 * 
	 * @version 1.0
	 * @createTime 2013-11-27,上午9:27:59
	 * @updateTime 2013-11-27,上午9:27:59
	 * @createAuthor CodeApe
	 * @updateAuthor CodeApe
	 * @updateInfo (此处输入修改内容,若无修改可不写.)
	 * 
	 * @param context
	 *            上下文
	 * @param str
	 * @param pattern
	 *            正则表达式
	 * @return
	 */
	public static SpannableString getExpressionString(Context context, int size, String str, String pattern) {
		SpannableString spannableString = new SpannableString(str);
		Pattern sinaPatten = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE); // 通过传入的正则表达式来生成一个pattern
		try {
			dealExpression(context, size, spannableString, sinaPatten, 0);
		} catch (Exception e) {
		}
		return spannableString;
	}

	/**
	 * 
	 * 描述：
	 * 
	 * createTime 2014-3-23 下午2:47:18 createAuthor 健兴
	 * 
	 * updateTime 2014-3-23 下午2:47:18 updateAuthor 健兴 updateInfo
	 * 
	 * @param context
	 * @param size 表情大小
	 * @param str 表情资源id
	 * @return
	 */
	public static SpannableString getExpressionString(Context context, int size, String str) {
		String pattern = PatternUtil.PATTERN_FACE_EMOJI; // 正则表达式，用来判断消息内是否有表情
		SpannableString spannableString = new SpannableString(str);
		try {
			Pattern sinaPatten = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE); // 通过传入的正则表达式来生成一个pattern
			dealExpression(context, size, spannableString, sinaPatten, 0);

			sinaPatten = Pattern.compile(PatternUtil.PATTERN_FACE_CUSTOMER, Pattern.CASE_INSENSITIVE);// 自定义表情
			dealExpression(context, ExpressionUtil.DEFAULT_CUSTOMER_SIZE, spannableString, sinaPatten, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return spannableString;
	}

	public static SpannableString getExpressionString(Context context, int size,SpannableString spannableString) {
		String pattern = PatternUtil.PATTERN_FACE_EMOJI; // 正则表达式，用来判断消息内是否有表情
		try {
			Pattern sinaPatten = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE); // 通过传入的正则表达式来生成一个pattern
			dealExpression(context, size, spannableString, sinaPatten, 0);

			sinaPatten = Pattern.compile(PatternUtil.PATTERN_FACE_CUSTOMER, Pattern.CASE_INSENSITIVE);// 自定义表情
			dealExpression(context, ExpressionUtil.DEFAULT_CUSTOMER_SIZE, spannableString, sinaPatten, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return spannableString;
	}
	
	
}