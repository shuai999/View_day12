package com.view.day12;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

/**
 * Email 2185134304@qq.com
 * Created by JackChen on 2018/2/5.
 * Version 1.0
 * Description:
 */
public class SlidingMeun extends HorizontalScrollView {


    //菜单宽度
    private int mMenuWidth;
    //内容的View、菜单的View
    private View mContentView , mMenuView ;
    private GestureDetector mGestureDetector;
    // GestureDetector 处理快速滑动


    //菜单是否打开
    private boolean mMenuIsOpen = false ;
    //是否拦截
    private boolean mIsIntercept = false ;
    private View mShadowView;

    public SlidingMeun(Context context) {
        this(context, null);
    }

    public SlidingMeun(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingMeun(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //初始化自定义属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlidingMeun);
        //右边的一小段距离   50dp转为px
        float rightMargin = typedArray.getDimension(R.styleable.SlidingMeun_meunRightMargin, ScreenUtils.dip2px(context, 50));

        //菜单页宽度 = 屏幕宽度 - 右边一小段距离(自定义属性)
        mMenuWidth = (int) (getScreenWidth(context) - rightMargin);
        typedArray.recycle();


        //处理快速滑动
        mGestureDetector = new GestureDetector(context , mGestureListener);

    }


    /**
     * 由于这里只需要判断onFling()方法，所以就只实现这个方法即可
     */
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Log.e("TAG" , "velocityX -> " + velocityX) ;
            //在这里只需要关注快速滑动，只要快速滑动就会回调这个方法
            if (mMenuIsOpen){
                //打开的时候往右边快速滑动 ，需要关闭
                if (velocityX < 0){
                    closeMenu();
                    return true ;
                }
            }else{
                //关闭的时候往左边快速滑动 ，需要打开
                if (velocityX > 0){
                    openMenu();
                    return true ;
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    } ;



    // 1.直接运行后宽度不对（乱套了），指定宽高
    @Override
    protected void onFinishInflate() {
        //这个方法是布局解析完毕后 即setContentView之后调用 也就是XML文件解析完毕后调用
        super.onFinishInflate();

        /* ========== 指定宽高 START ========== */
        //1.内容页的宽度 = 屏幕的宽度
        //由于activity_main布局是 LinearLayout包裹了layout_home_menu和layout_home_content，
        // 所以先获取LinearLayout，然后从LinearLayout中获取2个子View

        //获取LinearLayout  这个为什么不是根布局 com.view.day12.SlidingMeun
        ViewGroup container = (ViewGroup) getChildAt(0);

        //这里获取LinearLayout容器中所有子View个数，判断只能放置2个子View，如果不是2个则抛异常
        int childCount = container.getChildCount();
        if (childCount != 2){
            throw new RuntimeException("只能放置两个子View!") ;
        }
        mMenuView = container.getChildAt(0);//获取LinearLayout的第一个子View，即菜单页
        ViewGroup.LayoutParams menuParams = mMenuView.getLayoutParams();//设置宽高只能通过 LayoutParams
        menuParams.width = mMenuWidth ;
        mMenuView.setLayoutParams(menuParams);//7.0以下手机必须采用下边的方式




        //思路就是：把内容部分单独提取出来，然后在外边套一层阴影，最后再把容器放回到原来的位置


        //把内容部分单独提取出来,然后根布局中移除出去
        //2.菜单页宽度 = 屏幕宽度 - 右边一小段距离(自定义属性)
        mContentView = container.getChildAt(1); //获取LinearLayout的第二个子View，即内容页
        ViewGroup.LayoutParams contentParams = mContentView.getLayoutParams();
        container.removeView(mContentView);


        //然后在外边套一层阴影，contentContainer这个是new了一个容器，
        RelativeLayout contentContainer = new RelativeLayout(getContext()) ;
        contentContainer.addView(mContentView); //先把内容部分自己加进来
        mShadowView = new View(getContext());  //然后再去new一个阴影，设置半透明后，再次把阴影添加到容器中
        mShadowView.setBackgroundColor(Color.parseColor("#55000000"));  //半透明
        contentContainer.addView(mShadowView);

        //最后再把容器放回到原来的位置
        contentParams.width = getScreenWidth(getContext()) ;
        contentContainer.setLayoutParams(contentParams);
        container.addView(contentContainer);    //设置setLayoutParams后，最后把容器放回到原来的位置
        mShadowView.setAlpha(0.0f);
        //7.0以下手机必须采用下边的方式
//        mContentView.setLayoutParams(contentParams);

        /* ========== 指定宽高 END ========== */





        //2. 初始化进来是关闭的还是打开的 发现没用 因为这个方法是在onLayout()之前执行的
//        scrollTo(mMenuWidth , 0);
    }


    //4.处理右边的缩放、左边的缩放和透明度，需要不断的获取当前滚动的位置
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        //算一个梯度值
        float scale = 1f * l/mMenuWidth ;

        //控制阴影 0-1
        float alphaScale = 1 - scale ;
        mShadowView.setAlpha(alphaScale);


//        Log.e("TAG", "l -> " + l);// 变化是 mMenuWidth - 0
//        // 算一个梯度值
//        float scale = 1f * l / mMenuWidth;// scale 变化是 1 - 0
//        // 右边的缩放: 最小是 0.7f, 最大是 1f
//        float rightScale = 0.7f + 0.3f * scale;
//        // 设置右边的缩放,默认是以中心点缩放
//        // 设置缩放的中心点位置
//        ViewCompat.setPivotX(mContentView, 0);
//        ViewCompat.setPivotY(mContentView, mContentView.getMeasuredHeight() / 2);
//        ViewCompat.setScaleX(mContentView,rightScale);
//        ViewCompat.setScaleY(mContentView, rightScale);
//
//        // 菜单的缩放和透明度
//        // 透明度是 半透明到完全透明  0.5f - 1.0f
//        float leftAlpha = 0.5f + (1-scale)*0.5f;
//        ViewCompat.setAlpha(mMenuView,leftAlpha);
//        // 缩放 0.7f - 1.0f
//        float leftScale = 0.7f + (1-scale)*0.3f;
//        ViewCompat.setScaleX(mMenuView,leftScale);
//        ViewCompat.setScaleY(mMenuView, leftScale);
//
//        // 最后一个效果 退出这个按钮刚开始是在右边，安装我们目前的方式永远都是在左边
//        // 设置平移，先看一个抽屉效果
//        // ViewCompat.setTranslationX(mMenuView,l);
//        // 平移 l*0.7f
        ViewCompat.setTranslationX(mMenuView, 0.6f*l);
    }


    //最开始执行onFinishInflate()，它是在布局解析完毕后就执行的，也就是说它在onCreate()方法中执行的，
    // 而onLayout()是在onResume()方法之后执行的，View的绘制流程都是在onResume之后执行的
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //2.初始化进来是关闭的: 一开始进来传递菜单宽度，即mMenuWidth，表示关闭的
        // 等摆放完毕后才可以调用scrollTo()方法
        scrollTo(mMenuWidth , 0);
    }



    //3. 手指抬起是二选一，要么打开要么关闭
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        //如果有拦截事件就不要执行自己的onTouchEvent
        if (mIsIntercept){
            return true ;
        }

        //如果快速滑动方法触发了就不要执行下边代码了
        //为了onFling()这个方法可以回调，就必须在onTouchEvent中写这个，目的是为了把onTouchEvent()交给mGestureDetector去处理
        if (mGestureDetector.onTouchEvent(ev)){
            return true ;
        }

        //1.获取手指滑动的速率，当它大于一定的速率就认为是快速滑动，用GestureDetector来处理快速滑动(这个是系统提供好的类)
        //2.处理事件拦截 + ViewGroup 事件分发的源码实践
        //3.当菜单打开的时候，手指触摸右边内容部分需要关闭菜单，还需要拦截事件(打开情况下点击内容页不会相应点击事件)
        if (ev.getAction() == MotionEvent.ACTION_UP){
            //只需要管手指抬起的距离，根据我们当前滚动的距离来判断
            int currentScrollX = (int) getScrollX();

            if (currentScrollX > mMenuWidth/2){
                //关闭菜单
                closeMenu();
            }else{
                //打开菜单，即侧滑滚动到0的位置
                openMenu() ;
            }

            //确保super.onTouchEvent(ev) 不会执行
            return true;

        }
        return super.onTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        mIsIntercept = false;

        //当菜单打开的时候手指触摸右边内容部分 需要关闭菜单，并且还需要拦截事件 (菜单打开的情况下点击内容页不会相应点击事件)
        if (mMenuIsOpen){
            float currentX = ev.getX() ;
            if (currentX > mMenuWidth){
                //1.关闭菜单
                closeMenu();
                //2.子View不需要响应任何事件(点击和触摸) , 拦截子View事件
                //如果返回true代表我会拦截子View的事件 但是我会响应 onTouchEvent()事件
                mIsIntercept = true ;
                return true ;
            }
        }
        return super.onInterceptTouchEvent(ev);

    }

    /**
     * 打开菜单
     */
    private void openMenu(){
        smoothScrollTo(0 , 0);
        mMenuIsOpen = true ;
    }

    /**
     * 关闭菜单
     */
    private void closeMenu(){
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false ;
    }


    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    private int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * Dip into pixels
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}










