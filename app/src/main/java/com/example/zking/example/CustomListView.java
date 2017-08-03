package com.example.zking.example;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by Administrator on 2017/7/25 0025.
 */
public class CustomListView extends ListView {
    //刷新状态              刷新前
    public static final int REFRESH_BEFORE=1;
    public static final int REFRESHING=2; //刷新中
    public static final int REFRESH_END=3; //刷新完成



    //记录刷新状态  1 刷新前、2 刷新中、3刷新成功
    private static int refreshState=REFRESH_BEFORE;
    //刷新监听事件接口
    private OnRefreshListener listener=null;
    //刷新动画
    private AnimationDrawable animDra;
    private Context context;
    //下拉的HeaderView
    private View headView;
    //HeaderView的高度
    private int headViewHeight;
    //下拉提示语
    private TextView pullText;
    private TextView progress_Text;
    //刷新图标
    private ImageView pullImage;
    private ImageView progress_bar;
    //控制高度的布局
    private RelativeLayout pullLayout;
    //布局管理器
    private ViewGroup.LayoutParams lp;

    //标记标签
    //记录是否第一次改变高度
    boolean firstFlag=true;
    //记录改变下拉图标的状态
    boolean changePullImageFlag=true;
        //按下的Y轴 释放的Y轴 下拉高度（按下-释放）
    float flagYDown,flagYMove,flagY;

    //定义一个Handler来处理子线程给我们返回的信息
    Handler myHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
             switch(msg.what){
                 case REFRESH_END:
                     progress_Text.setText("刷新成功！");
                     animDra.stop();
                     progress_bar.setBackgroundResource(R.drawable.obu);
                     new Thread(){
                         @Override
                         public void run() {
                             try {
                                 this.sleep(500);
                                 myHandler.sendEmptyMessage(2);
                             } catch (InterruptedException e) {
                                 e.printStackTrace();
                             }
                         }
                     }.start();
                     break;
                 case 2:
                     //状态设为 刷新完毕
                     refreshState=REFRESH_END;
                     changeViewHeight(headViewHeight,0);
                     if(listener!=null){
                         listener.refreshAfter(CustomListView.this);
                     }
                     break;
             }
        }
    };


    public CustomListView(Context context) {
        super(context);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        headView=(View)LayoutInflater.from(context).inflate(R.layout.listview_head_layout,null);
        pullLayout=(RelativeLayout) headView.findViewById(R.id.pullLayout);
        lp=pullLayout.getLayoutParams();
        headView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //判断是否是第一次改变布局，如果是的话，则把布局的高度设为0
                if(firstFlag){
                    headViewHeight=v.getHeight();
                    lp.height=0;
                    pullLayout.setLayoutParams(lp);
                    firstFlag=false;
                }
            }
        });
        //设置HeaderView
        this.addHeaderView(headView);
        initHeadView();
    }

    //初始化控件
    void initHeadView(){
        //圆形刷新动画
        animDra = (AnimationDrawable) headView.findViewById(R.id.progress_bar).getBackground();
        //圆形刷新图标
        progress_bar=(ImageView) headView.findViewById(R.id.progress_bar);
        //刷新提示文本
        progress_Text=(TextView) headView.findViewById(R.id.progress_Text);
        //下滑的提示文本
        pullText=(TextView)headView.findViewById(R.id.pullText);
        //下滑箭头图标
        pullImage=(ImageView)headView.findViewById(R.id.refreshIcon);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //判断是否是最顶部和是否处于刷新状态
        if(this.getFirstVisiblePosition()==0&&refreshState!=REFRESHING){
        switch(ev.getAction()){
            case MotionEvent.ACTION_DOWN://手指按下时
                flagYDown=ev.getY();
                //按下时隐藏圆形进度条和提示文本，显示下拉箭头图、提示文本
                progress_bar.setVisibility(View.GONE);
                progress_Text.setVisibility(View.GONE);
                pullText.setVisibility(View.VISIBLE);
                pullImage.setBackgroundResource(R.drawable.rjd_down);
                refreshState=1;
                if(listener!=null){
                    listener.refreshBefore(CustomListView.this);
                }
                break;
            case MotionEvent.ACTION_MOVE://手指滑动时
                this.setSelection(0);
                flagYMove=ev.getY();
                //下拉的高度
                flagY=(float)((flagYMove-flagYDown)*0.3);//这里*0.3是设置下拉的难易度，*的数越大越容易下拉
                //判断下拉的高度是否大于ListView头布局原始的高度
                if(flagY>headViewHeight){
                    pullText.setText("松开立即刷新");
                    //设置上下小箭头的旋转动画
                    //changePullImageFlag，设置一个标签，防止多次设置动画，当下拉的高宽改变时才设置动画
                    if(changePullImageFlag){
                        changePullIconAnimation(1,pullImage);
                        changePullImageFlag=false;
                    }
                }else{
                    pullText.setText("下拉刷新");
                    if(changePullImageFlag==false){
                        changePullIconAnimation(0,pullImage);
                        changePullImageFlag=true;
                    }
                }
                //判断一下下拉的高度是否大于0
               if(flagY>0){
                       lp.height=(int)flagY;
                       pullLayout.setLayoutParams(lp);
                }
                break;
            case MotionEvent.ACTION_UP://手指放开时
                //判断下拉的高度是否大于ListView头布局原始的高度
                if(flagY>headViewHeight){
                    pullText.setVisibility(View.GONE);
                    pullImage.setBackgroundResource(R.color.pullImage);
                    //将圆形进度条，和提示文字显示出来
                    progress_bar.setVisibility(View.VISIBLE);
                    progress_Text.setVisibility(View.VISIBLE);
                    progress_Text.setText("正在刷新...");
                    //调用改变高度的缓冲动画的方法，并传入参数
                    changeViewHeight(lp.height,headViewHeight);
                    progress_bar.setBackgroundResource(R.drawable.progress_bar);
                    animDra=(AnimationDrawable) progress_bar.getBackground();
                    animDra.start();
                    //将状态设置为 正在刷新
                    refreshState=REFRESHING;
                    //调用正在刷新
                    if(listener!=null){
                        listener.refreshStart(CustomListView.this);
                    }
                }else{
                        changeViewHeight(lp.height,0);
                }
                break;
        }
        }
        return super.dispatchTouchEvent(ev);
    }



    //改变高度的缓冲动画           开始动画的高度     结束动画的高度
    public void changeViewHeight(int startHeight,int endHeight){
        ValueAnimator va;
        //判断开始的高度和结束的高度是否大于30大于30就设置启用弹跳效果
        if(startHeight>headViewHeight||startHeight<30){
         va= ValueAnimator.ofInt(startHeight,endHeight);
        }else{
                                  //开始高度  结束高度 弹跳的高度  结束高度
           va= ValueAnimator.ofInt(startHeight,endHeight,15,endHeight);
        }
        //监听动画改变的事件
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h =(Integer)valueAnimator.getAnimatedValue();
                //动态更新高度
                lp.height=h;
                pullLayout.setLayoutParams(lp);
            }
        });
        va.setDuration(200);
        //开始动画
        va.start();
    }

   //  改变下拉箭头图标动画           状态  0转上去（松开立即刷新） 1转下来（下拉刷新）
    public void changePullIconAnimation(int state,ImageView view){
        Animation anim;
        if(state==1){
            anim=AnimationUtils.loadAnimation(this.getContext(),R.anim.pullimage);
        }else{
            anim=AnimationUtils.loadAnimation(this.getContext(),R.anim.pullimage_down);
        }
        anim.setFillAfter(true);
        view.startAnimation(anim);
    }

    //设置刷新监听
    public void setOnRefreshListener(OnRefreshListener listener){
        this.listener=listener;
    }


}
