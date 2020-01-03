package net.cb.cb.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cb.cb.library.R;


/***
 * 星条控件
 * 
 * @author 姜永健
 * @date 2016年8月23日
 */
public class ScoreStarView extends LinearLayout {
	private float scoreStar;
	private ImageView[] Star = new ImageView[5];
	private int resId4Hover[] = { R.mipmap.store_icon_star_hover1, R.mipmap.store_icon_star_hover2,
			R.mipmap.store_icon_star_hover3, R.mipmap.store_icon_star_hover4};
	private int resIdBg[] = {  R.mipmap.store_icon_star_normal,R.mipmap.store_icon_big_star_normal  };
	private int resId[] = {R.mipmap.store_icon_star_hover, R.mipmap.store_icon_big_star_hover};
	private int resId4HoverBig[] = { R.mipmap.store_icon_big_star_hover1, R.mipmap.store_icon_big_star_hover2,
			R.mipmap.store_icon_big_star_hover3, R.mipmap.store_icon_big_star_hover4 };

	/***
	 * 类型,样式
	 ***<enum name="Snormal" value="0" /> 
	 ***<enum name="Sbig" value="1" /> 
	 ***<enum name="typeShow" value="0" /> 
	 ***<enum name="typeDoing" value="1" />
	 */
	private int Type, Style;

	public ScoreStarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScoreStarView);
		Type = typedArray.getInt(R.styleable.ScoreStarView_ssType, 0);
		Style = typedArray.getInt(R.styleable.ScoreStarView_ssStyle, 0);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rootView = inflater.inflate(R.layout.view_score_start, this);
		Star[0] = rootView.findViewById(R.id.imageView);
		Star[1] = rootView.findViewById(R.id.imageView1);
		Star[2] = rootView.findViewById(R.id.imageView2);
		Star[3] = rootView.findViewById(R.id.imageView3);
		Star[4] = rootView.findViewById(R.id.imageView4);
		initView();
		
	}

	private float oldscore = 0;

	private void initView() {

		for (int i = 0; i < Star.length; i++) {
			Star[i].setBackgroundResource(resIdBg[Style]);
			Star[i].setImageResource(R.color.transparent);
		}
		if (Type == 1){
			setScore(1);
			this.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_MOVE:
					case MotionEvent.ACTION_DOWN:
						float e = (float) Math.ceil(event.getX() / ScoreStarView.this.getMeasuredWidth() * 5) / 5;
						e=e<1/5f?1/5f:e;
						if (e != oldscore) {
							oldscore = e;

							setScore(e);
						}
						break;
					}
					return true;
				}
			});
		}
	}

	/***
	 * 设置星级[0-1]
	 * 
	 * @param score
	 */
	public void setScore(float score) {
		int s = (int) (score * 5);
		// 满心
		for (int i = 0; i < Star.length; i++) {
			if (i < s) {
				Star[i].setImageResource(resId[Style]);
			} else {
				Star[i].setImageResource(R.color.transparent);
			}
		}

		int res[] = Style == 0 ? resId4Hover : resId4HoverBig;

		// 为了填满半心
		float fill = score * 5 - s;
		if (fill > 0 && fill <= 0.25) {
			Star[s].setImageResource(res[0]);
		} else if (fill > 0.25 && fill <= 0.5) {
			Star[s].setImageResource(res[1]);
		} else if (fill > 0.5 && fill <= 0.75) {
			Star[s].setImageResource(res[2]);
		} else if (fill > 0.75 && fill < 1) {
			Star[s].setImageResource(res[3]);
		}
		scoreStar=score;
	}
	/***
	 * 获取星级[0-1]
	 * @return
	 */
	public float getScore(){
		return scoreStar;
	}
}
