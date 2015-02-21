package com.example.android.myomap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

public class MapsActivity extends ActionBarActivity {


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private TextView mGestureTextView;
    private TextView mLockStateTextView;
    private TextView mRpyTextView;
    private float mRoll;
    private float mPitch;
    private float mYaw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mLockStateTextView = (TextView) findViewById(R.id.lock_textview);
        mGestureTextView = (TextView) findViewById(R.id.gesture_textview);
        mRpyTextView = (TextView) findViewById(R.id.rpy_textview);

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
        setUpMap();
    }

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mGestureTextView.setTextColor(Color.CYAN);
        }
        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            mGestureTextView.setTextColor(Color.RED);
        }
        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mGestureTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
        }
        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mGestureTextView.setText(R.string.hello_world);
        }
        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateTextView.setText(R.string.unlocked);
        }
        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateTextView.setText(R.string.locked);
        }
        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            mRoll = (float) Math.toDegrees(Quaternion.roll(rotation));
            mPitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            mYaw = (float) Math.toDegrees(Quaternion.yaw(rotation));
            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                mRoll *= -1;
                mPitch *= -1;
            }

            if(myo.getPose() == Pose.FIST) {
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.zoomBy(mRoll / 10));
                }
//                mMap.animateCamera(CameraUpdateFactory.scrollBy(mYaw/10, mPitch/10));

                mRpyTextView.setText("roll: " + mRoll + "\npitch: " + mPitch + "\nyaw: " + mYaw);
            }

             if(myo.getPose() == Pose.FINGERS_SPREAD) {
                 float relYaw = 90 - mYaw;
                 mMap.animateCamera(CameraUpdateFactory.scrollBy(0, mPitch * 100));
             }
//            mMap.animateCamera(CameraUpdateFactory.scrollBy(roll, pitch));
            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            mRpyTextView.setText("roll: " + mRoll + "\npitch: " + mPitch + "\nyaw: " + mYaw);
        }
        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            if (mMap != null) {
                switch (pose) {
                    case UNKNOWN:
                        mGestureTextView.setText(getString(R.string.hello_world));
                        break;
                    case REST:
                    case DOUBLE_TAP:
                        int restTextId = R.string.hello_world;
                        switch (myo.getArm()) {
                            case LEFT:
                                restTextId = R.string.arm_left;
                                break;
                            case RIGHT:
                                restTextId = R.string.arm_right;
                                break;
                        }
                        mGestureTextView.setText(getString(restTextId));
                        break;
                    case FIST:
                        //mMap.animateCamera(CameraUpdateFactory.zoomBy(mRoll));
                        mGestureTextView.setText(getString(R.string.pose_fist));
                        break;
                    case WAVE_IN:
                        //mMap.animateCamera(CameraUpdateFactory.zoomOut());
                        mGestureTextView.setText(getString(R.string.pose_wavein));
                        break;
                    case WAVE_OUT:
                        //mMap.animateCamera(CameraUpdateFactory.scrollBy(((float) 60.5), (float) 45.5));
                        mGestureTextView.setText(getString(R.string.pose_waveout));
                        break;
                    case FINGERS_SPREAD:
                        //mMap.animateCamera(CameraUpdateFactory.scrollBy(((float) -60.5), (float) -45.5));
                        mGestureTextView.setText(getString(R.string.pose_fingersspread));
                        break;
                }
            }
            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);
                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);
        if (isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.action_scan == id) {
            onScanActionSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onScanActionSelected() {
        // Launch the ScanActivity to scan for Myos to connect to.
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mMap = googleMap;
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
                        }
                    });
        }
    }

}
