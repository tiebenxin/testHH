package net.cb.cb.library.utils;

import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

/****
 * 计时器线程类
 * 这是一个全局的计时器类,启用一个线程来处理所有秒级控制
 * step 1: init初始化
 * step 2:OnRespond回调 其中会返回i,根据i来处理多秒判断也是可以的
 * step 3:cancel 销毁
 * @author 姜永健
 * @date 2015年11月6日
 */
public class TimerUtil {
	private static final String TAG="TimerUtil";

	private static TimerUtil util;
	private static ConcurrentHashMap<String, OnRespond> timeId;
	private static long COUNT = 0l;
	private static Timer timer;

	private TimerUtil() {
	}
/***
 * 
 * @param tag 计时器唯一标示
 * @param respond 回调
 * @return
 */
	public static TimerUtil init(String tag, OnRespond respond) {
		if (util == null) {
			util = new TimerUtil();
			timeId = new ConcurrentHashMap<String, OnRespond>();
		}

		Log.i(TAG, "timer加入:" +tag);
		timeId.put(tag, respond);
		util.isNeedTrimer();
		return util;
	}

	private static boolean isRun = false;

	/***
	 * 是否需要
	 */
	private void isNeedTrimer() {
		if (timeId == null || timeId.size() < 1) {

			Log.i(TAG, "停止线程");
			// 停止线程
			if(isRun){
			timer.cancel();
			isRun = false;
			}
		} else {

			if (!isRun) {
			
				Log.i(TAG, "启动线程");
				isRun = true;
				timer = new Timer();
				// 启用线程
				timer.schedule(new TimerTask() {
					public void run() {
						
						for (Entry<String, OnRespond> entry : timeId.entrySet()) {
							String appFieldDefId = entry.getKey();
							OnRespond onRespond = entry.getValue();
							if (onRespond != null)
								onRespond.respond(COUNT);
						}

						COUNT++;
					}
				}, 0, 1000);
			}/* else {
				LogUtil.getLog().d("a=", "已经启动过了");
			}*/
		}
	}

	/***
	 * 销毁时需要
	 */
	public void cancel(String tag) {
		//1.22 jyj
		if(tag==null)
			return;
	
		Log.i(TAG, ">>cancel:"+tag);
		
		if(util!=null&&timeId.get(tag)!=null){
			
		timeId.remove(tag);
		isNeedTrimer();
		}
	}

	public interface OnRespond {
		/***
		 * 响应 i为活动的数
		 */
        void respond(long i);
	}

	

}
