package com.hzh.wxfriendcircleforward;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/2/3 0003.
 */

public class ForwardService extends AccessibilityService {
    private static final String TAG = "hzh";
    private List<AccessibilityNodeInfo> list;
    private Timer mTimer;
    private boolean flag = true;
    private int index;
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        list = new ArrayList<>();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String className = event.getClassName().toString();
        Log.d(TAG,"eventType="+event.getEventType()+",className="+ className);
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        Log.d(TAG,"nodeInfo="+rootNode);
        recycleLog(rootNode);
        switch (event.getEventType()){
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.d(TAG,"flag="+flag);
                if (flag){
                    if (className.equals("android.widget.LinearLayout")&&rootNode!=null&&rootNode.getContentDescription()!=null&&rootNode.getContentDescription().equals("当前所在页面,朋友圈")){
                        flag = false;
                        index = 0;
                        String title = recycleText(rootNode);//获取消息文本
                        ClipboardManager systemService = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        systemService.setText(title);//设置消息文本到剪切板
                        AccessibilityNodeInfo imgRootNode = recycleImageNode(rootNode);//获取图片集合的根节点
                        rootNode.recycle();
                        Log.d(TAG,"imgNode="+imgRootNode);
                        if (imgRootNode!=null){
                            addImageNode(imgRootNode);//添加图片节点到list
                            if (list.size()>0){
                                saveImages();//保存图片和接下来的操作
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * 点击进入大图界面
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void saveImages() {
        AccessibilityNodeInfo nodeInfo = list.get(0);
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        saveSingleImage();
    }

    /**
     * 第一张图时，延迟长按
     * 最后一张图，单击退出
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void saveSingleImage() {
        if (index == 0){
            Path path = new Path();
            path.moveTo(200,200);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path,500,800));
            GestureDescription gestureDescription = builder.build();
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    clickSaveImage();
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                }
            }, null);
        }else if (index<list.size()){
            Path path = new Path();
            path.moveTo(200,200);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path,300,800));
            GestureDescription gestureDescription = builder.build();
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    clickSaveImage();
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                }
            }, null);
        }else if (index == list.size()){
            Path path = new Path();
            path.moveTo(300,300);
            GestureDescription gestureDescription
                    = new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,100,50)).build();
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    mTimer = new Timer(true);
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            clickMore();
                        }
                    },1000);
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                }
            }, null);
        }
    }

    /**
     * 点击toolbar上的照相机图标
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void clickMore() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        Log.d(TAG,"moreNode="+rootNode);
        if (rootNode!=null){
            AccessibilityNodeInfo moreButton = getMoreButton(rootNode);
            Log.d(TAG,"moreButton="+moreButton);
            if (moreButton!=null){
                moreButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                moreButton.recycle();
                clickSelectImageFormAlbum();
            }
            rootNode.recycle();
        }
    }

    /**
     * 点击从相册中选择
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void clickSelectImageFormAlbum() {
        Path path = new Path();
        path.moveTo(getResources().getDisplayMetrics().widthPixels/2,getResources().getDisplayMetrics().heightPixels/2 + getResources().getDisplayMetrics().density*20);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path,100,50));
        GestureDescription gestureDescription = builder.build();
        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG,"SELECT---COMPLETE");
                if (mTimer!=null){
                    mTimer.cancel();
                    mTimer = null;
                }
                mTimer = new Timer(true);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        selectPictures();
                    }
                },1000);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    /**
     * 选择图片和视频界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void selectPictures() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode!=null){
            AccessibilityNodeInfo gridView = getGridView(rootNode);
            if (gridView!=null){
                getNeedView(gridView);
                gridView.recycle();
            }
            AccessibilityNodeInfo completeButton = getCompleteButton(rootNode);
            if (completeButton!=null){
                completeButton.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                completeButton.getChild(0).recycle();
                completeButton.recycle();
                if (mTimer!=null){
                    mTimer.cancel();
                    mTimer = null;
                }
                mTimer = new Timer(true);
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        sendMessage();
                    }
                },1000);
            }
            rootNode.recycle();
        }
    }

    /**
     * 发送信息界面
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendMessage() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode!=null){
            AccessibilityNodeInfo editText = getEditText(rootNode);
            if (editText!=null){
                editText.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                editText.recycle();
                flag = true;
            }
            rootNode.recycle();
        }
    }

    private AccessibilityNodeInfo getEditText(AccessibilityNodeInfo rootNode) {
        if (rootNode.getChildCount()==0){
            if (rootNode.getClassName().equals("android.widget.EditText")){
                return rootNode;
            }
        }else{
            for (int i = 0; i < rootNode.getChildCount(); i++) {
                AccessibilityNodeInfo editText = getEditText(rootNode.getChild(i));
                if (editText!=null) return editText;
            }
        }
        return null;
    }

    /**
     * 得到图片和视频界面的完成按钮
     * @param rootNode
     */
    private AccessibilityNodeInfo getCompleteButton(AccessibilityNodeInfo rootNode) {
        if (rootNode.getChildCount()>0){
            if (rootNode.getClassName().equals("android.widget.LinearLayout")&&rootNode.getChild(0).getClassName().equals("android.widget.TextView")&&rootNode.getChild(0).getText()!=null&&rootNode.getChild(0).getText().toString().startsWith("完成")){
                return rootNode;
            }else{
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    AccessibilityNodeInfo completeButton = getCompleteButton(rootNode.getChild(i));
                    if (completeButton!=null) return completeButton;
                }
            }
        }
        return null;
    }

    /**
     * 选择图片
     * @param gridView
     */
    private void getNeedView(AccessibilityNodeInfo gridView) {
        Log.d(TAG,"gridViewChildCount="+gridView.getChildCount()+",listSize="+list.size());
        for (int i = list.size()-1; i >=0; i--) {
            Log.d(TAG,"i="+i);
            AccessibilityNodeInfo child = gridView.getChild(i);
            AccessibilityNodeInfo view = null;
            for (int j = 0; j <  child.getChildCount(); j++) {
                if (child.getChild(j)!=null&&child.getChild(j).getClassName()!=null&&child.getChild(j).getClassName().equals("android.view.View")&&child.getChild(j).isClickable()){
                    view = child.getChild(j);
                    break;
                }
            }
            Log.d(TAG,"view="+view);
            if (view!=null&&view.isClickable()){
                view.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                view.recycle();
            }
        }
    }

    /**
     * 图片和视频界面的GridView
     * @param rootNode
     */
    private AccessibilityNodeInfo getGridView(AccessibilityNodeInfo rootNode) {
        if (rootNode.getChildCount()>0){
            Log.d(TAG,"getGridViewClassName="+rootNode.getClassName());
            if (rootNode.getClassName().equals("android.widget.GridView")){
                return rootNode;
            }else{
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    AccessibilityNodeInfo gridView = getGridView(rootNode.getChild(i));
                    if (gridView!=null) return gridView;
                }
            }
        }
        return null;
    }

    private AccessibilityNodeInfo getMoreButton(AccessibilityNodeInfo rootNode) {
        if (rootNode.getChildCount()>0){
            if (rootNode.getClassName().equals("android.widget.RelativeLayout")&&rootNode.isClickable()&&rootNode.getContentDescription()!=null&&rootNode.getContentDescription().toString().equals("更多功能按钮")){
                return rootNode;
            }else {
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    AccessibilityNodeInfo moreButton = getMoreButton(rootNode.getChild(i));
                    if (moreButton!=null) return moreButton;
                }
            }
        }
        return null;
    }

    /**
     * 点击保存图片按钮，屏幕向左滑动
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void clickSaveImage() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        Log.d(TAG,"saveImgNodeInfo="+nodeInfo);
        if (nodeInfo!=null){
            AccessibilityNodeInfo saveImgButton = getSaveImgButton(nodeInfo);
            if (saveImgButton!=null){
                saveImgButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                saveImgButton.recycle();
            }
            nodeInfo.recycle();
            Path path = new Path();
            path.moveTo(getResources().getDisplayMetrics().widthPixels*0.91f,300);
            path.lineTo(getResources().getDisplayMetrics().widthPixels*0.12f,300);
            GestureDescription gestureDescription
                    = new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,200,50)).build();
            dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    index++;
                    saveSingleImage();
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                }
            }, null);
        }
    }

    private AccessibilityNodeInfo getSaveImgButton(AccessibilityNodeInfo rootNode) {
        if (rootNode.getChildCount()>0){
            if (rootNode.getClassName().equals("android.widget.LinearLayout")&&rootNode.isClickable()&&rootNode.getChild(0).getChild(0).getText().toString().equals("保存图片")){
                return rootNode;
            }else {
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    AccessibilityNodeInfo saveImgButton = getSaveImgButton(rootNode.getChild(i));
                    if (saveImgButton!=null) return saveImgButton;
                }
            }
        }
        return null;
    }

    private void addImageNode(AccessibilityNodeInfo imgRootNode) {
        if (imgRootNode.getChild(0).getContentDescription()!=null){
            list.clear();
            for (int i = 0; i < imgRootNode.getChildCount(); i++) {
                if (imgRootNode.getChild(i).getContentDescription().toString().equals("图片")){
                    Log.d(TAG,"imgNode="+imgRootNode.getChild(i));
                    list.add(imgRootNode.getChild(i));
                }
            }
        }
    }

    private AccessibilityNodeInfo recycleImageNode(AccessibilityNodeInfo rootNode) {
        if (rootNode.getChildCount()>0){
            if (rootNode.getClassName().toString().equals("android.widget.FrameLayout")
                    &&rootNode.getChild(0).getClassName().toString().equals("android.view.View")
                    &&rootNode.getParent()!=null
                    &&rootNode.getParent().getClassName().toString().equals("android.widget.LinearLayout")){
                return rootNode;
            }else{
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    AccessibilityNodeInfo node = recycleImageNode(rootNode.getChild(i));
                    if (node!=null) return node;
                }
            }
        }
        return null;
    }

    private String recycleText(AccessibilityNodeInfo rootNode) {
        if (rootNode.getChildCount()>0) {
            if (rootNode.getClassName().toString().equals("android.widget.LinearLayout")
                    && rootNode.getChildCount() == 1
                    && rootNode.getChild(0).getClassName().toString().equals("android.widget.LinearLayout")
                    && rootNode.getText()!=null) {
                Log.d(TAG,"title="+rootNode.getText().toString());
                return rootNode.getText().toString();
            }else{
                for (int i = 0; i < rootNode.getChildCount(); i++) {
                    String str = recycleText(rootNode.getChild(i));
                    if (str!=null) return str;
                }
            }
        }
        return null;
    }

    private void recycleLog(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo!=null){
            if (nodeInfo.getChildCount()>0){
//                if (nodeInfo.getText()!=null){
//                    Log.d(TAG,"nodeInfoText="+nodeInfo.getText().toString());
//                }
                for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                    recycleLog(nodeInfo.getChild(i));
                }
            }else{
                if (nodeInfo.getText()!=null){
                    Log.d(TAG,"node="+nodeInfo.getText().toString());
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
