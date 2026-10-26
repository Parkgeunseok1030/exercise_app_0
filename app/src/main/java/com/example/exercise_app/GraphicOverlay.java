package com.example.exercise_app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import androidx.camera.core.CameraSelector;
import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {
    private final Object lock = new Object();
    private int previewWidth;
    private int previewHeight;
    private float scaleFactor = 1.0f;
    private float offsetX = 0f;
    private float offsetY = 0f;
    private int facing = CameraSelector.LENS_FACING_FRONT;
    private final List<Graphic> graphics = new ArrayList<>();

    public abstract static class Graphic {
        private GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public float translateX(float x) {
            float scaledX = x * overlay.scaleFactor;
            if (overlay.facing == CameraSelector.LENS_FACING_FRONT) {
                return overlay.getWidth() - (scaledX + overlay.offsetX);
            }
            return scaledX + overlay.offsetX;
        }

        public float translateY(float y) {
            return (y * overlay.scaleFactor) + overlay.offsetY;
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void clear() {
        synchronized (lock) { graphics.clear(); }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) { graphics.add(graphic); }
        postInvalidate();
    }

    public void setCameraInfo(int previewWidth, int previewHeight, int facing) {
        synchronized (lock) {
            this.previewWidth = previewWidth;
            this.previewHeight = previewHeight;
            this.facing = facing;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {
            if (previewWidth != 0 && previewHeight != 0) {
                scaleFactor = Math.max(
                        (float) getWidth() / previewWidth,
                        (float) getHeight() / previewHeight
                );

                offsetX = (getWidth() - (previewWidth * scaleFactor)) / 2.0f;
                offsetY = (getHeight() - (previewHeight * scaleFactor)) / 2.0f;
            }

            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }
}