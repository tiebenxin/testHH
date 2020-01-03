package com.yanlong.im.view.face;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.yanlong.im.R;
import com.yanlong.im.view.face.adapter.ViewPagerAdapter;
import com.yanlong.im.view.face.bean.FaceBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @version V1.0
 * @createAuthor （Geoff）
 * @createDate 2019-11-15
 * @updateAuthor
 * @updateDate
 * @description 表情视图
 * @copyright copyright(c)2019 ChangSha hm Technology Co., Ltd. Inc. All rights reserved.
 */
public class FaceView extends RelativeLayout {

    /**
     * 上下文
     */
    private Context context;
    /**
     * 父视图
     */
    private View view_Parent;
    /**
     * 表情列表切换控件
     */
    private ViewPager mViewPager;
    /**
     * 表情列表切换适配器
     */
    private ViewPagerAdapter adapter;
    /**
     * 表情切换列表
     */
    private ArrayList<View> list_Views;
    /**
     * 分页显示圆点
     */
    private RadioGroup mRadioGroup;
    /**
     * 显示选择表情类型
     */
    private RadioGroup select_RadioGroup;
    /**
     * 表情点击事件监听
     */
    private FaceViewPager.FaceClickListener faceClickListener;
    /**
     * 表情点击事件监听
     */
    private OnClickListener mOnDeleteListener;
    /**
     * 表情长按事件监听
     */
    private FaceViewPager.FaceLongClickListener faceLongClickListener;
    /**
     * 切换表情视图
     */
    private final int face_emoji_tab = 0;
    /**
     * 切换动态表情视图
     */
    private final int face_animo_tab = 1;
    /**
     * 切换自定义表情视图
     */
    private final int face_custom_tab = 2;
    /**
     * 切换自定义表情视图
     */
    private final int FACE_PIG = 3;
    /**
     * 默认选择位置
     */
    private int mCheckPostion = 0;
    /**
     * 切换自定义表情视图
     */
    private final int FACE_MAMMON = 4;
    /**
     * 切换自定义表情视图
     */
    private final int FACE_PANDA = 5;
    /**
     * 页数
     */
    private int pagerCount = -1;
    /**
     * 页大小
     */
    private int pagerSize = 20;
    /**
     * 表情组
     */
    public static String face_emoji = "emoji";
    /**
     * 动态动漫组
     */
    public static String face_animo = "animo";
    /**
     * 自定义表情组
     */
    public static String face_custom = "custom";

    /**
     * emoji表情ID列表
     */
    private static int[] face_EmojiIds = {R.mipmap.emoji_000, R.mipmap.emoji_001, R.mipmap.emoji_002, R.mipmap.emoji_003,
            R.mipmap.emoji_004, R.mipmap.emoji_005, R.mipmap.emoji_006, R.mipmap.emoji_007, R.mipmap.emoji_008, R.mipmap.emoji_009,
            R.mipmap.emoji_010, R.mipmap.emoji_011, R.mipmap.emoji_012, R.mipmap.emoji_013, R.mipmap.emoji_014, R.mipmap.emoji_015,
            R.mipmap.emoji_016, R.mipmap.emoji_017, R.mipmap.emoji_018, R.mipmap.emoji_019, R.mipmap.emoji_020, R.mipmap.emoji_021,
            R.mipmap.emoji_022, R.mipmap.emoji_023, R.mipmap.emoji_024, R.mipmap.emoji_025, R.mipmap.emoji_026, R.mipmap.emoji_027,
            R.mipmap.emoji_028, R.mipmap.emoji_029, R.mipmap.emoji_030, R.mipmap.emoji_031, R.mipmap.emoji_032, R.mipmap.emoji_033,
            R.mipmap.emoji_034, R.mipmap.emoji_035, R.mipmap.emoji_036, R.mipmap.emoji_037, R.mipmap.emoji_038, R.mipmap.emoji_039,
            R.mipmap.emoji_040, R.mipmap.emoji_041, R.mipmap.emoji_042, R.mipmap.emoji_043, R.mipmap.emoji_044, R.mipmap.emoji_045,
            R.mipmap.emoji_046, R.mipmap.emoji_047, R.mipmap.emoji_048, R.mipmap.emoji_049, R.mipmap.emoji_050, R.mipmap.emoji_051,
            R.mipmap.emoji_052, R.mipmap.emoji_053, R.mipmap.emoji_054, R.mipmap.emoji_055, R.mipmap.emoji_056, R.mipmap.emoji_057,
            R.mipmap.emoji_058, R.mipmap.emoji_059, R.mipmap.emoji_060, R.mipmap.emoji_061, R.mipmap.emoji_062, R.mipmap.emoji_063,
            R.mipmap.emoji_064, R.mipmap.emoji_065, R.mipmap.emoji_066, R.mipmap.emoji_067, R.mipmap.emoji_068, R.mipmap.emoji_069,
            R.mipmap.emoji_070, R.mipmap.emoji_071, R.mipmap.emoji_072, R.mipmap.emoji_073, R.mipmap.emoji_074, R.mipmap.emoji_075,
            R.mipmap.emoji_076, R.mipmap.emoji_077, R.mipmap.emoji_078, R.mipmap.emoji_079, R.mipmap.emoji_080, R.mipmap.emoji_081,
            R.mipmap.emoji_082, R.mipmap.emoji_083, R.mipmap.emoji_084, R.mipmap.emoji_085, R.mipmap.emoji_086, R.mipmap.emoji_087,
            R.mipmap.emoji_088, R.mipmap.emoji_089, R.mipmap.emoji_090, R.mipmap.emoji_091, R.mipmap.emoji_092, R.mipmap.emoji_093,
            R.mipmap.emoji_094, R.mipmap.emoji_095, R.mipmap.emoji_096, R.mipmap.emoji_097, R.mipmap.emoji_098, R.mipmap.emoji_099,};

    /**
     * emoji表情名称
     */
    private static String[] face_EmojiNames = {"[emoji_000]", "[emoji_001]", "[emoji_002]", "[emoji_003]", "[emoji_004]", "[emoji_005]",
            "[emoji_006]", "[emoji_007]", "[emoji_008]", "[emoji_009]", "[emoji_010]", "[emoji_011]", "[emoji_012]", "[emoji_013]", "[emoji_014]",
            "[emoji_015]", "[emoji_016]", "[emoji_017]", "[emoji_018]", "[emoji_019]", "[emoji_020]", "[emoji_021]", "[emoji_022]", "[emoji_023]",
            "[emoji_024]", "[emoji_025]", "[emoji_026]", "[emoji_027]", "[emoji_028]", "[emoji_029]", "[emoji_030]", "[emoji_031]", "[emoji_032]",
            "[emoji_033]", "[emoji_034]", "[emoji_035]", "[emoji_036]", "[emoji_037]", "[emoji_038]", "[emoji_039]", "[emoji_040]", "[emoji_041]",
            "[emoji_042]", "[emoji_043]", "[emoji_044]", "[emoji_045]", "[emoji_046]", "[emoji_047]", "[emoji_048]", "[emoji_049]", "[emoji_050]",
            "[emoji_051]", "[emoji_052]", "[emoji_053]", "[emoji_054]", "[emoji_055]", "[emoji_056]", "[emoji_057]", "[emoji_058]", "[emoji_059]",
            "[emoji_060]", "[emoji_061]", "[emoji_062]", "[emoji_063]", "[emoji_064]", "[emoji_065]", "[emoji_066]", "[emoji_067]", "[emoji_068]",
            "[emoji_069]", "[emoji_070]", "[emoji_071]", "[emoji_072]", "[emoji_073]", "[emoji_074]", "[emoji_075]", "[emoji_076]", "[emoji_077]",
            "[emoji_078]", "[emoji_079]", "[emoji_080]", "[emoji_081]", "[emoji_082]", "[emoji_083]", "[emoji_084]", "[emoji_085]", "[emoji_086]",
            "[emoji_087]", "[emoji_088]", "[emoji_089]", "[emoji_090]", "[emoji_091]", "[emoji_092]", "[emoji_093]", "[emoji_094]", "[emoji_095]",
            "[emoji_096]", "[emoji_097]", "[emoji_098]", "[emoji_099]",};
    /**
     * 动态表情ID列表
     */
    private static int[] animo_emojiIds = {R.mipmap.animation_emoti_000, R.mipmap.animation_emoti_001, R.mipmap.animation_emoti_002,
            R.mipmap.animation_emoti_003, R.mipmap.animation_emoti_004, R.mipmap.animation_emoti_005, R.mipmap.animation_emoti_006, R.mipmap.animation_emoti_007,};
    /**
     * 动态表情名称
     */
    private static String[] animo_emojiNames = {"[animation_emoti_000]", "[animation_emoti_001]", "[animation_emoti_002]", "[animation_emoti_003]",
            "[animation_emoti_004]", "[animation_emoti_005]", "[animation_emoti_006]","[animation_emoti_007]",};
    /**
     * 动态表情ID列表
     */
    private static int[] animo_mamonIds = {R.mipmap.animation_mamon_000, R.mipmap.animation_mamon_001, R.mipmap.animation_mamon_002,
            R.mipmap.animation_mamon_003, R.mipmap.animation_mamon_004, R.mipmap.animation_mamon_005, R.mipmap.animation_mamon_006,R.mipmap.animation_mamon_007,};
    /**
     * 动态表情名称
     */
    private static String[] animo_mamonNames = {"[animation_mamon_000]", "[animation_mamon_001]", "[animation_mamon_002]", "[animation_mamon_003]",
            "[animation_mamon_004]", "[animation_mamon_005]", "[animation_mamon_006]", "[animation_mamon_007]",};

    /**
     * 动态表情ID列表
     */
    private static int[] animo_pandaIds = {R.mipmap.animation_panda_000, R.mipmap.animation_panda_001, R.mipmap.animation_panda_002,
            R.mipmap.animation_panda_003, R.mipmap.animation_panda_004, R.mipmap.animation_panda_005, R.mipmap.animation_panda_006,R.mipmap.animation_panda_007,};
    /**
     * 动态表情名称
     */
    private static String[] animo_pandaNames = {"[animation_panda_000]", "[animation_panda_001]", "[animation_panda_002]", "[animation_panda_003]",
            "[animation_panda_004]", "[animation_panda_005]", "[animation_panda_006]","[animation_panda_007]",};

    /**
     * 自定义表情
     */
    private static File[] custom_files;
    /**
     * 所有表情列表
     */
    private ArrayList<FaceBean> list_AllBeans;
    /**
     * 当前页表情列表
     */
    private ArrayList<FaceBean> list_CurrentBeans = new ArrayList<FaceBean>();

    /**
     * emoji 表情键值对，key表情名称，value表情对应的图片资源Id
     */
    public static HashMap<String, Object> map_FaceEmoji;

    public FaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FaceView(Context context) {
        super(context);
        init(context);
    }

    /**
     * 初始化界面
     *
     * @param context
     * @version 1.0
     * @createTime 2013-10-20,下午4:51:48
     * @updateTime 2013-10-20,下午4:51:48
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    private void init(Context context) {
        this.context = context;

        view_Parent = LayoutInflater.from(this.context).inflate(R.layout.view_face, null);
        this.addView(view_Parent);

        mViewPager = view_Parent.findViewById(R.id.view_face_viewpager);
        mRadioGroup = findViewById(R.id.view_face_radiogroup);
        select_RadioGroup = findViewById(R.id.view_face_select);
        initFaceList(face_emoji_tab);
        widgetListener();
    }

    /**
     * 初始化表情键值对
     *
     * @version 1.0
     * @createTime 2013-11-24,上午12:11:11
     * @updateTime 2013-11-24,上午12:11:11
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public static void initFaceMap() {
        map_FaceEmoji = new HashMap<String, Object>();
        for (int i = 0; i < face_EmojiIds.length; i++) {
            map_FaceEmoji.put(face_EmojiNames[i], face_EmojiIds[i]);
        }
        for (int i = 0; i < animo_emojiIds.length; i++) {
            map_FaceEmoji.put(animo_emojiNames[i], animo_emojiIds[i]);
        }
        for (int i = 0; i < animo_mamonIds.length; i++) {
            map_FaceEmoji.put(animo_mamonNames[i], animo_mamonIds[i]);
        }
        for (int i = 0; i < animo_pandaIds.length; i++) {
            map_FaceEmoji.put(animo_pandaNames[i], animo_pandaIds[i]);
        }
    }

    /**
     * 初始化表情列表
     *
     * @version 1.0
     * @createTime 2013-11-23,下午2:45:16
     * @updateTime 2013-11-23,下午2:45:16
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public void initFaceList(int type_emoji) {
        // 表情类型
//		 int faceType = FaceAdapter.FACE_TYPE_ANIMO;
        // LogUtil.outThrowable("初始化表情列表");
        list_AllBeans = new ArrayList<FaceBean>();
        list_AllBeans.clear();
        switch (type_emoji) {
            case face_emoji_tab:
                pagerSize = 20;
                for (int i = 0; i < face_EmojiIds.length; i++) {
                    FaceBean bean = new FaceBean();
                    bean.setGroup(face_emoji);
                    bean.setResId(face_EmojiIds[i]);
                    bean.setName(face_EmojiNames[i]);
                    list_AllBeans.add(bean);
                }
                mRadioGroup.setVisibility(VISIBLE);
                break;
            case FACE_PIG:
                pagerSize = 8;
                for (int i = 0; i < animo_emojiIds.length; i++) {
                    FaceBean bean = new FaceBean();
                    bean.setGroup(face_animo);
                    bean.setResId(animo_emojiIds[i]);
                    bean.setName(animo_emojiNames[i]);
                    list_AllBeans.add(bean);
                }
                mRadioGroup.setVisibility(GONE);
                break;
            case FACE_MAMMON:
                pagerSize = 8;
                for (int i = 0; i < animo_mamonIds.length; i++) {
                    FaceBean bean = new FaceBean();
                    bean.setGroup(face_animo);
                    bean.setResId(animo_mamonIds[i]);
                    bean.setName(animo_mamonNames[i]);
                    list_AllBeans.add(bean);
                }
                mRadioGroup.setVisibility(GONE);
                break;
            case FACE_PANDA:
                pagerSize = 8;
                for (int i = 0; i < animo_pandaIds.length; i++) {
                    FaceBean bean = new FaceBean();
                    bean.setGroup(face_animo);
                    bean.setResId(animo_pandaIds[i]);
                    bean.setName(animo_pandaNames[i]);
                    list_AllBeans.add(bean);
                }
                mRadioGroup.setVisibility(GONE);
                break;
            case face_custom_tab:
//			pagerSize = 8;
//			File file = new File(FileConfig.PATH_USER_FAVORITES);
//			custom_files = file.listFiles();
//			if (custom_files != null && custom_files.length > 0) {
//				for (int i = 0; i < custom_files.length; i++) {
//					if (custom_files[i].getName().equals(".nomedia")) {
//						continue;
//					}
//					FaceBean bean = new FaceBean();
//					bean.setGroup(face_custom);
//					bean.setResId(i);
//					bean.setPath(custom_files[i].getAbsolutePath());
//					list_AllBeans.add(bean);
//				}
//			} else {
////				LogUtil.out("没有自定义");
//
//			}
//			 faceType = FaceAdapter.FACE_TYPE_CUSTOM;
                break;

            default:
                break;
        }

//		LogUtil.out("list size =>" + list_AllBeans.size());
        // list_AllBeans = new ArrayList<FaceBean>();

        // 如果表情列表为空，直接返回
        if (list_AllBeans.size() <= 0) {
            mViewPager.removeAllViews();
            mRadioGroup.removeAllViews();
            return;
        }

        // 计算表情页数
        pagerCount = list_AllBeans.size() % pagerSize == 0 ? list_AllBeans.size() / pagerSize : list_AllBeans.size() / pagerSize + 1;

        // 设置圆点和分页列表
        mRadioGroup.removeAllViews();
        list_Views = new ArrayList<View>();
        for (int i = 0; i < pagerCount; i++) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setId(i);
            radioButton.setButtonDrawable(R.drawable.radio_dot_selector);
            radioButton.setEnabled(false);
            RadioGroup.LayoutParams radioParams = new RadioGroup.LayoutParams(20, 20);
            radioParams.leftMargin = 10;
            radioButton.setLayoutParams(radioParams);
            mRadioGroup.addView(radioButton);

            FaceViewPager view = new FaceViewPager(context, type_emoji);
            view.setOnItemClikListener(faceClickListener);
            view.setOnItemLongClickListener(faceLongClickListener);
            view.setOnDeleteListener(mOnDeleteListener);
            list_Views.add(view);
        }

        // 设置分页列表
        adapter = new ViewPagerAdapter(list_Views);
        mViewPager.setAdapter(adapter);
        // 预加载1页
        mViewPager.setOffscreenPageLimit(1);

        // 设置当前显示列表
        if (list_AllBeans.size() > pagerSize) {
            list_CurrentBeans.clear();
            list_CurrentBeans.addAll(list_AllBeans.subList(0, pagerSize));
        } else {
            list_CurrentBeans.clear();
            list_CurrentBeans.addAll(list_AllBeans.subList(0, list_AllBeans.size()));
        }
        mRadioGroup.check(0);
        ((FaceViewPager) list_Views.get(0)).setFaceList(list_CurrentBeans);

    }

    /**
     * 是否允许显示
     *
     * @param enabled true允许，false不显示
     * @version 1.0
     * @createTime 2014年1月7日, 下午2:49:24
     * @updateTime 2014年1月7日, 下午2:49:24
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public void setSelectEnable(boolean enabled) {
        if (enabled) {
            select_RadioGroup.setVisibility(View.VISIBLE);
        } else {
            select_RadioGroup.setVisibility(View.GONE);
        }
    }

    /**
     * 组件监听模块
     *
     * @version 1.0
     * @createTime 2013-11-23,下午2:57:53
     * @updateTime 2013-11-23,下午2:57:53
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    private void widgetListener() {

        // 列表切换监听事件
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mCheckPostion = position;
                mRadioGroup.check(position);
                int currentItem = mViewPager.getCurrentItem();
                FaceViewPager view = (FaceViewPager) list_Views.get(currentItem);
                if (view.list_FaceBeans.size() > 0) {// 当前页的表情列表不为空，即是表情列表已经加载过，无需重新加载
                    return;
                }
                list_CurrentBeans.clear();
                if ((currentItem * pagerSize + pagerSize) >= list_AllBeans.size() && list_AllBeans.size() >= (currentItem * pagerSize)) {
                    list_CurrentBeans.addAll(list_AllBeans.subList(currentItem * pagerSize, list_AllBeans.size()));
                } else {
                    if (list_AllBeans.size() >= (currentItem * pagerSize + pagerSize)) {
                        list_CurrentBeans.clear();
                        list_CurrentBeans.addAll(list_AllBeans.subList(currentItem * pagerSize, currentItem * pagerSize + pagerSize));
                    }
                }
                view.setFaceList(list_CurrentBeans);

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });
        // 表情类型单选
        select_RadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.face_emoji:
                        initFaceList(face_emoji_tab);
                        break;
                    case R.id.animo_emoji:
                        initFaceList(face_animo_tab);
                        break;
                    case R.id.custom_emoji:
                        initFaceList(face_custom_tab);
                        break;
                    case R.id.cb_face_pig:// 猪
                        initFaceList(FACE_PIG);
                        break;
                    case R.id.cb_face_mammon:// 财神
                        initFaceList(FACE_MAMMON);
                        break;
                    case R.id.cb_face_panda:// 熊猫
                        initFaceList(FACE_PANDA);
                        break;
                    default:
                        break;
                }

            }
        });
//         select_RadioGroup.check(R.id.face_emoji);

    }

    /**
     * 设置表情列表点击事件
     *
     * @param listener
     * @version 1.0
     * @createTime 2013-11-23,下午10:59:53
     * @updateTime 2013-11-23,下午10:59:53
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public void setOnItemClickListener(FaceViewPager.FaceClickListener listener) {
        this.faceClickListener = listener;
        if (list_Views == null) {
            return;
        }
        for (int i = 0; i < list_Views.size(); i++) {
            FaceViewPager view = (FaceViewPager) list_Views.get(i);
            view.setOnItemClikListener(this.faceClickListener);
        }
    }

    /**
     * 设置表情列表长按事件
     *
     * @param listener
     * @version 1.0
     * @createTime 2013-12-30,上午10:59:55
     * @updateTime 2013-12-30,上午10:59:55
     * @createAuthor liujingguo
     * @updateAuthor liujingguo
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public void setOnItemLongClickListener(FaceViewPager.FaceLongClickListener listener) {
        this.faceLongClickListener = listener;
        if (list_Views == null) {
            return;
        }
        for (int i = 0; i < list_Views.size(); i++) {
            FaceViewPager view = (FaceViewPager) list_Views.get(i);
            view.setOnItemLongClickListener(this.faceLongClickListener);
        }
    }

    /**
     * @version 1.0
     * @createTime 2013-12-30,下午4:44:29
     * @updateTime 2013-12-30,下午4:44:29
     * @createAuthor liujingguo
     * @updateAuthor liujingguo
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public void updateView() {
        initFaceList(face_custom_tab);
    }

    /**
     * 设置删除按钮点击事件
     *
     * @param listener
     * @version 1.0
     * @createTime 2013-11-23,下午11:01:24
     * @updateTime 2013-11-23,下午11:01:24
     * @createAuthor CodeApe
     * @updateAuthor CodeApe
     * @updateInfo (此处输入修改内容, 若无修改可不写.)
     */
    public void setOnDeleteListener(OnClickListener listener) {
        mOnDeleteListener = listener;
        if (list_Views == null) {
            return;
        }
        for (int i = 0; i < list_Views.size(); i++) {
            FaceViewPager view = (FaceViewPager) list_Views.get(i);
            view.setOnDeleteListener(listener);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (list_Views == null) {
            select_RadioGroup.check(R.id.face_emoji);
        }
    }

}
