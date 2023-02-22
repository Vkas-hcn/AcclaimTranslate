package com.vkas.secondtranslation.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vkas.secondtranslation.R;

import java.util.Objects;

public class StLoadingDialog extends Dialog {
    private static final String TAG = "LoadingDialog";
    private String mMessage; // 加载中文字
    private int mImageId; // 旋转图片id
    private boolean mCancelable;
    private RotateAnimation mRotateAnimation;
    public StLoadingDialog(@NonNull Context context) {
        super(context);
    }

    public StLoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected StLoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }
    private void initView() {
        setContentView(R.layout.st_dialog_loading);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置窗口大小
        WindowManager windowManager = getWindow().getWindowManager();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        // 设置窗口背景透明度
        attributes.alpha = 1f;
        // 设置窗口宽高为屏幕的三分之一（为了更好地适配，请别直接写死）
        attributes.width = screenWidth/3;
        attributes.height = screenWidth/4;
        getWindow().setAttributes(attributes);
        setCancelable(mCancelable);
    }
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            // 屏蔽返回键
            return mCancelable;
        }
        return super.onKeyDown(keyCode, event);
    }
}
