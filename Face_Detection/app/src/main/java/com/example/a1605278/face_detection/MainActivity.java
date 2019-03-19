package com.example.a1605278.face_detection;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.a1605278.face_detection.Helper.GraphicOverlay;
import com.example.a1605278.face_detection.Helper.RectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;


public class MainActivity extends AppCompatActivity {

    CameraView cameraview;
    GraphicOverlay  graphicOverlay;
    Button bt;
    android.app.AlertDialog waitingDialog;



    @Override
    protected void onResume() {
        super.onResume();
        cameraview.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraview.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraview=(CameraView) findViewById(R.id.camera_view);
        graphicOverlay=(GraphicOverlay)findViewById(R.id.graphic_overlay);
        bt=(Button) findViewById(R.id.btn_detect);

        waitingDialog= new SpotsDialog.Builder().setContext(this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build();
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraview.start();
                cameraview.captureImage();
                graphicOverlay.clear();
            }
        });

        cameraview.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                    waitingDialog.show();

                    Bitmap bitmap=cameraKitImage.getBitmap();
                    bitmap=Bitmap.createScaledBitmap(bitmap,cameraview.getWidth(),cameraview.getHeight(), false);
                    cameraview.stop();

                    runFaceDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

    }

    private void runFaceDetector(Bitmap bitmap) {
        FirebaseVisionImage image=FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions options=new  FirebaseVisionFaceDetectorOptions.Builder()
                .build();
        FirebaseVisionFaceDetector detector=FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                            processfaceresult(firebaseVisionFaces);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processfaceresult(List<FirebaseVisionFace> firebaseVisionFaces) {
        int count=0;
        for(FirebaseVisionFace face : firebaseVisionFaces)
        {
            Rect bounds=face.getBoundingBox();

            RectOverlay rect=new RectOverlay(graphicOverlay,bounds);
            graphicOverlay.add(rect);
            count++;

        }

        waitingDialog.dismiss();
        Toast.makeText(this,String.format("Detected %d faces in image",count),Toast.LENGTH_SHORT).show();
    }
}
