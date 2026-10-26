package com.example.exercise_app;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import java.util.List;

public class PoseGraphic extends GraphicOverlay.Graphic {

    private final Pose pose;
    private final Paint pointPaint;
    private final Paint linePaint;

    public PoseGraphic(GraphicOverlay overlay, Pose pose) {
        super(overlay);
        this.pose = pose;

        pointPaint = new Paint();
        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStrokeWidth(12.0f);
        pointPaint.setStyle(Paint.Style.FILL);

        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(6.0f);
        linePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void draw(Canvas canvas) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) return;

        for (PoseLandmark landmark : landmarks) {
            canvas.drawCircle(
                    translateX(landmark.getPosition().x),
                    translateY(landmark.getPosition().y),
                    8.0f,
                    pointPaint
            );
        }

        drawConnection(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER);
        drawConnection(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP);
        drawConnection(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP);
        drawConnection(canvas, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP);

        drawConnection(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW);
        drawConnection(canvas, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST);

        drawConnection(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW);
        drawConnection(canvas, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST);

        drawConnection(canvas, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE);
        drawConnection(canvas, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE);

        drawConnection(canvas, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE);
        drawConnection(canvas, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE);
    }

    private void drawConnection(Canvas canvas, int startLandmarkType, int endLandmarkType) {
        PoseLandmark startLandmark = pose.getPoseLandmark(startLandmarkType);
        PoseLandmark endLandmark = pose.getPoseLandmark(endLandmarkType);

        if (startLandmark != null && endLandmark != null) {
            canvas.drawLine(
                    translateX(startLandmark.getPosition().x),
                    translateY(startLandmark.getPosition().y),
                    translateX(endLandmark.getPosition().x),
                    translateY(endLandmark.getPosition().y),
                    linePaint
            );
        }
    }
}