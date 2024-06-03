package com.example.button;

import android.app.Service;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class FloatingButtonService extends Service {

    private WindowManager windowManager;
    private View floatingButtonLayout;
    private View floatingButton;
    private WindowManager.LayoutParams params;
    private boolean isDraggingEnabled = true; // Flag to toggle between dragging and resizing

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatingButtonLayout = LayoutInflater.from(this).inflate(R.layout.floating_button_with_handles, null);
        floatingButton = floatingButtonLayout.findViewById(R.id.floating_button);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.x = 0;
        params.y = 100;

        windowManager.addView(floatingButtonLayout, params);

        // Attach the listeners
        floatingButton.setOnTouchListener(new FloatingButtonTouchListener());
        floatingButtonLayout.findViewById(R.id.handle_top_left).setOnTouchListener(new HandleTouchListener());
        floatingButtonLayout.findViewById(R.id.handle_top_right).setOnTouchListener(new HandleTouchListener());
        floatingButtonLayout.findViewById(R.id.handle_bottom_left).setOnTouchListener(new HandleTouchListener());
        floatingButtonLayout.findViewById(R.id.handle_bottom_right).setOnTouchListener(new HandleTouchListener());

        floatingButton.setOnClickListener(v -> {
            // Toggle the mode between dragging and resizing
            isDraggingEnabled = !isDraggingEnabled;
            String mode = isDraggingEnabled ? "Dragging enabled" : "Resizing enabled";
            Toast.makeText(FloatingButtonService.this, mode, Toast.LENGTH_SHORT).show();
        });
    }

    private class FloatingButtonTouchListener implements View.OnTouchListener {
        private int lastAction;
        private long startClickTime;
        private static final int MAX_CLICK_DURATION = 200;
        private float initialTouchX;
        private float initialTouchY;
        private int initialX;
        private int initialY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (isDraggingEnabled) {
                // Disable the button when dragging is enabled
                floatingButton.setEnabled(true);
                floatingButton.setClickable(false);
                floatingButton.setFocusable(false);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        startClickTime = System.currentTimeMillis();
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_UP:
                        long clickDuration = System.currentTimeMillis() - startClickTime;
                        if (clickDuration < MAX_CLICK_DURATION) {
                            v.performClick();
                        }
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingButtonLayout, params);
                        lastAction = event.getAction();
                        return true;
                }
            }
            return false;
        }
    }

    private class HandleTouchListener implements View.OnTouchListener {
        private float initialTouchX;
        private float initialTouchY;
        private int initialWidth;
        private int initialHeight;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!isDraggingEnabled) {
                // Disable the button
                floatingButton.setEnabled(false);
                floatingButton.setClickable(false);
                floatingButton.setFocusable(false);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        initialWidth = floatingButton.getLayoutParams().width;
                        initialHeight = floatingButton.getLayoutParams().height;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - initialTouchX;
                        float dy = event.getRawY() - initialTouchY;

                        int newWidth = initialWidth;
                        int newHeight = initialHeight;

                        if (v.getId() == R.id.handle_top_left || v.getId() == R.id.handle_bottom_left) {
                            newWidth = Math.max(initialWidth - (int) dx, 100);
                        } else {
                            newWidth = Math.max(initialWidth + (int) dx, 100);
                        }

                        if (v.getId() == R.id.handle_top_left || v.getId() == R.id.handle_top_right) {
                            newHeight = Math.max(initialHeight - (int) dy, 100);
                        } else {
                            newHeight = Math.max(initialHeight + (int) dy, 100);
                        }

                        floatingButton.getLayoutParams().width = newWidth;
                        floatingButton.getLayoutParams().height = newHeight;
                        floatingButton.requestLayout();
                        return true;
                    case MotionEvent.ACTION_UP:
                        // Re-enable the button when resizing is finished
                        floatingButton.setEnabled(true);
                        floatingButton.setClickable(true);
                        floatingButton.setFocusable(true);
                        return true;
                }
            }
            return false;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            float alpha = intent.getFloatExtra("EXTRA_ALPHA", -1);
            if (alpha != -1) {
                updateButtonTransparency(alpha);
            }

            int color = intent.getIntExtra("EXTRA_COLOR", -1);
            if (color != -1) {
                updateButtonColor(color);
            }
        }
        return START_STICKY;
    }

    private void updateButtonTransparency(float alpha) {
        if (floatingButton != null) {
            floatingButton.setAlpha(alpha);

            // Update the transparency of the handles
            View handleTopLeft = floatingButtonLayout.findViewById(R.id.handle_top_left);
            View handleTopRight = floatingButtonLayout.findViewById(R.id.handle_top_right);
            View handleBottomLeft = floatingButtonLayout.findViewById(R.id.handle_bottom_left);
            View handleBottomRight = floatingButtonLayout.findViewById(R.id.handle_bottom_right);

            handleTopLeft.setAlpha(alpha);
            handleTopRight.setAlpha(alpha);
            handleBottomLeft.setAlpha(alpha);
            handleBottomRight.setAlpha(alpha);
        }
    }

    private void updateButtonColor(int color) {
        if (floatingButton != null) {
            floatingButton.setBackgroundTintList(ColorStateList.valueOf(color));

            // Update the color of the handles
            View handleTopLeft = floatingButtonLayout.findViewById(R.id.handle_top_left);
            View handleTopRight = floatingButtonLayout.findViewById(R.id.handle_top_right);
            View handleBottomLeft = floatingButtonLayout.findViewById(R.id.handle_bottom_left);
            View handleBottomRight = floatingButtonLayout.findViewById(R.id.handle_bottom_right);

            handleTopLeft.setBackgroundColor(color);
            handleTopRight.setBackgroundColor(color);
            handleBottomLeft.setBackgroundColor(color);
            handleBottomRight.setBackgroundColor(color);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingButtonLayout != null) {
            windowManager.removeView(floatingButtonLayout);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
