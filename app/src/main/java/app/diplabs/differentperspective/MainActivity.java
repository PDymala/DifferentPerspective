package app.diplabs.differentperspective;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int MY_CAMERA_REQUEST_CODE = 100;
    public static final String TAG = "DifferentPerspective_Main";
    private final int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;

    private JavaCamera2View javaCameraView;
    private SeekBar seekbarHorizontal;
    private SeekBar seekBarVertical;

    private boolean showGrid = false;
    private boolean showView = true;
    private double widthPercentSmaller = 0.0;
    private double heightPercentSmaller = 0.0;
    private Mat mRgba;


    static {
        System.loadLibrary("opencv_java4");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        if (permisions()) {
            initializeUI();
            initializeCamera(javaCameraView, activeCamera);

        }
        ;


    }

    private void initializeUI() {

        javaCameraView = findViewById(R.id.camera_view);

        seekbarHorizontal = findViewById(R.id.seekBarHorizontal);
        seekBarVertical = findViewById(R.id.seekBarVertical);

        // Get the progress value of the SeekBar
        // using setOnSeekBarChangeListener() method

        seekbarHorizontal.setOnSeekBarChangeListener(
                new SeekBar
                        .OnSeekBarChangeListener() {

                    // When the progress value has changed
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {

                        int seekBarValue = seekBar.getProgress();

                        widthPercentSmaller = ((double) seekBarValue) / 100;

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                        // This method will automatically
                        // called when the user touches the SeekBar
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        // This method will automatically
                        // called when the user
                        // stops touching the SeekBar
                    }
                });


        seekBarVertical.setOnSeekBarChangeListener(
                new SeekBar
                        .OnSeekBarChangeListener() {

                    // When the progress value has changed
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {

                        int seekBarValue = seekBar.getProgress();

                        heightPercentSmaller = ((double) seekBarValue) / 100;

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                        // This method will automatically
                        // called when the user touches the SeekBar
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        // This method will automatically
                        // called when the user
                        // stops touching the SeekBar
                    }
                });


    }


    private void initializeCamera(JavaCamera2View javaCameraView, int activeCamera) {

        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);


        javaCameraView.enableView();

    }


    private boolean permisions() {
        // checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");

            return true;
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission denied");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera(javaCameraView, activeCamera);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if (javaCameraView != null) {
            javaCameraView.enableView();
        }


    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }


    @Override
    protected void onPause() {
        super.onPause();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }

    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();


        // Get the dimensions of the frame
        int width = mRgba.width();
        int height = mRgba.height();

        // Define the size of the shape (50% of the frame width and height)
        int shapeWidth = (int) (width *0.6);
        int shapeHeight = (int) (height *0.6);

        // Calculate the top-left corner of the shape so it's centered
        int startX = (width - shapeWidth) / 2;
        int startY = (height - shapeHeight) / 2;


        // Define the four points for the original shape
        Point[] points = new Point[4];
        points[0] = new Point(startX, startY);
        points[1] = new Point(startX + shapeWidth, startY);
        points[2] = new Point(startX + shapeWidth, startY + shapeHeight);
        points[3] = new Point(startX, startY + shapeHeight);

// Define the color for the original shape
        Scalar color = new Scalar(255, 0, 0, 255); // Blue color in RGBA

// Calculate the new width and height for the morphed shape
        double newWidth = shapeWidth * (1 - widthPercentSmaller);
        double newHeight = shapeHeight * (1 - heightPercentSmaller);

// Calculate the offsets to center the morphed shape
        double offsetX = (shapeWidth - newWidth) / 2;
        double offsetY = (shapeHeight - newHeight) / 2;

// Define the four points for the morphed shape, centered within the original shape
        Point[] pointsMorph = new Point[4];
        pointsMorph[0] = new Point(startX + offsetX, startY + offsetY);
        pointsMorph[1] = new Point(startX + offsetX + newWidth, startY + offsetY);
        pointsMorph[2] = new Point(points[2].x, startY + offsetY + newHeight);
        pointsMorph[3] = new Point(points[3].x, startY + offsetY + newHeight);

        // Define the color for the morphed shape
        Scalar colorMorph = new Scalar(0, 255, 0, 255); // Green color in RGBA


        if (showView) {


            // Perspective transformation from pointsMorph to points
            Mat src = new MatOfPoint2f(pointsMorph[0], pointsMorph[1], pointsMorph[2], pointsMorph[3]);
            Mat dst = new MatOfPoint2f(points[0], points[1], points[2], points[3]);

            Mat transform = Imgproc.getPerspectiveTransform(src, dst);
            Mat transformed = new Mat();
            Imgproc.warpPerspective(mRgba, transformed, transform, mRgba.size());

            // Create a mask from the area defined by points
            Mat mask = Mat.zeros(mRgba.size(), CvType.CV_8UC1);
            List<Point> pointList = Arrays.asList(points);
            MatOfPoint pointsMat = new MatOfPoint();
            pointsMat.fromList(pointList);
            List<MatOfPoint> pointsList = new ArrayList<>();
            pointsList.add(pointsMat);
            Imgproc.fillPoly(mask, pointsList, new Scalar(255));

            // Copy the area defined by points from transformed to mRgba
            Mat temp = new Mat();
            transformed.copyTo(temp, mask);
            temp.copyTo(mRgba, mask);

            //relese memo
            src.release();
            dst.release();
            transform.release();
            transformed.release();
            mask.release();
            temp.release();
            pointsMat.release();
            pointsList.forEach(Mat::release);

        }

        if (showGrid) {

            // Draw the points and the lines connecting them for the original shape
            for (int i = 0; i < points.length; i++) {
                // Draw the point
                Imgproc.circle(mRgba, points[i], 5, color, -1);

                // Draw the line to the next point
                Imgproc.line(mRgba, points[i], points[(i + 1) % points.length], color, 2);

            }

            // Draw the points and the lines connecting them for the morphed shape
            for (int i = 0; i < pointsMorph.length; i++) {
                // Draw the point
                Imgproc.circle(mRgba, pointsMorph[i], 5, colorMorph, -1);

                // Draw the line to the next point
                Imgproc.line(mRgba, pointsMorph[i], pointsMorph[(i + 1) % pointsMorph.length], colorMorph, 2);

            }


        }


        return mRgba;


    }

    public void showView(View view){
        showView = !showView;
    }
    public void customButton(View view){
        if (showGrid) {
            showGrid = false;
            seekBarVertical.setVisibility(View.INVISIBLE);
            seekbarHorizontal.setVisibility(View.INVISIBLE);
        }
        else{
            showGrid = true;
            seekBarVertical.setVisibility(View.VISIBLE);
            seekbarHorizontal.setVisibility(View.VISIBLE);
        }
    }

    public void set80degrees(View view){

        widthPercentSmaller = 0.1;
        heightPercentSmaller = 0.8;
        seekbarHorizontal.setProgress((int) (widthPercentSmaller*100));
        seekBarVertical.setProgress((int) (heightPercentSmaller*100));
    }

    public void set70degrees(View view){
        widthPercentSmaller = 0.1;
        heightPercentSmaller = 0.7;
        seekbarHorizontal.setProgress((int) (widthPercentSmaller*100));
        seekBarVertical.setProgress((int) (heightPercentSmaller*100));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (javaCameraView != null) {
            javaCameraView.enableView();
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }


    }


}
