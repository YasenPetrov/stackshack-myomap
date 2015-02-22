package com.example.android.myomap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

public class CollectionDemoActivity extends FragmentActivity {


    private TextView mGestureTextView;
    private TextView mLockStateTextView;
    private TextView mRpyTextView;

    private static final String CAMERA_POSITION_KEY = "cameraPosition";
    private static final String LOG_TAG = CollectionDemoActivity.class.getSimpleName();

    private static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //private TextView mGestureTextView;
    //private TextView mLockStateTextView;
    //private TextView mRpyTextView;

    private Arm mArm;
    private float mRoll;
    private float mPitch;
    private float mYaw;
    private float mYawOnSpread = 0;
    private boolean mUpdateYawOnSpread = false;
    private String yawText; //temp
    private String pitchText;
    private static final LatLng staLatLng = new LatLng(56.340107, -2.808320);
    private static final LatLng ediLatLng = new LatLng(55.944184, -3.186597);
    private static final LatLng glaLatLng = new LatLng(55.871706, -4.287941);
    // when zooming, substract this constant from the roll for
    // more natural arm position when zooming
    private final float ROLL_CORRECTION = 36;
    private static SupportMapFragment mapFragment;
    private static Button scanButton;
    private static TweetFragment mTweetFragment;
    ////////////////////////////////////////////////
    private final static int SLIDING_FRAGMENTS = 3;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link android.support.v4.app.FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_demo);


        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        // 
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());

        // Set up action bar.
//        final ActionBar actionBar = getActionBar();

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
//        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_scan :
//                onScanActionSelected();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            switch (i) {
                case 0:
                    fragment = new MyMapFragment();
                    break;
                case 1:
                    fragment = new TweetFragment();
                    mTweetFragment = (TweetFragment) fragment;
                    break;
                default:
                    Log.v(LOG_TAG, String.valueOf(i));
                    fragment = new DemoObjectFragment();
                    Bundle args = new Bundle();
                    args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1); // Our object is just an integer :-P
                    fragment.setArguments(args);
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return SLIDING_FRAGMENTS;
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            switch (pos){
                case 0:
                    return "MAP";
                case 1:
                    return "TWEET AT US";
                default:
                    return "TOP SECRET";
            }
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {
        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            //mGestureTextView.setTextColor(Color.CYAN);
            scanButton.setVisibility(View.INVISIBLE);
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            scanButton.setVisibility(View.VISIBLE);
            // Set the text color of the text view to red when a Myo disconnects.
            //mGestureTextView.setTextColor(Color.RED);
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mArm = myo.getArm();
            if (mArm == Arm.LEFT) {
                //mGestureTextView.setText(R.string.arm_left);
            } else {
                //mGestureTextView.setText(R.string.arm_right);
            }
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mArm = Arm.UNKNOWN;
            //mGestureTextView.setText(R.string.hello_world);
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
//            mLockStateTextView.setText(R.string.unlocked);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
//            mLockStateTextView.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {

            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            mRoll = (float) Math.toDegrees(Quaternion.roll(rotation));
            mPitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            mYaw = (float) Math.toDegrees(Quaternion.yaw(rotation));
            if (mUpdateYawOnSpread){
                mUpdateYawOnSpread = false;
                mYawOnSpread = mYaw;
            }
            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                mRoll *= -1;
                mPitch *= -1;
            }
            //Log.v("Our pitch: ", "" + mPitch);

            // Move cursor in tweet fragment
            if(mViewPager.getCurrentItem() == 1 && myo.getPose() == Pose.FIST) {
                mTweetFragment.moveCursorButton((int) mPitch);
            }

//            mRpyTextView.setText("roll: " + mRoll + "\npitch: " + mPitch + "\nrelYaw: " +
//                    (mYaw - mYawOnSpread));

            if(myo.getPose() == Pose.FIST) {
                Log.v(LOG_TAG, "FIST!");
                if (mMap != null) {
                    float zoomRoll = (mRoll - ROLL_CORRECTION) / 10;
                    mMap.animateCamera(CameraUpdateFactory.zoomBy(zoomRoll));
                }
//                mMap.animateCamera(CameraUpdateFactory.scrollBy(mYaw/10, mPitch/10));

            }

            if(myo.getPose() == Pose.FINGERS_SPREAD) {
                if(mMap != null) {
                    mMap.stopAnimation();
                }
                float maxPitch = 4200;
                float maxYaw = 4200;
                float relYaw = mYaw - mYawOnSpread;
                float scrollYaw = -relYaw * 170;
//                yawText = String.valueOf(scrollYaw)
                float scrollPitch = mPitch * 210;
                Log.v(LOG_TAG, ("mYaw :" + mYaw + "\nmYawOnSpread: " + mYawOnSpread + "\n scrollYaw: " + scrollYaw));
                if (Math.abs(scrollPitch) > maxPitch) {
                    Log.v(LOG_TAG, "Pitch ping: " + scrollPitch);
                    scrollPitch = maxPitch * Math.signum(scrollPitch);
                }
                if (Math.abs(scrollYaw) > maxYaw) {
                    Log.v(LOG_TAG, "Yaw ping: " + scrollYaw);
                    scrollYaw = maxYaw * Math.signum(scrollYaw);
                }
                mMap.animateCamera(CameraUpdateFactory.scrollBy(scrollYaw, scrollPitch));

                //mMap.animateCamera
            }
//            mMap.animateCamera(CameraUpdateFactory.scrollBy(roll, pitch));
            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
//            mRpyTextView.setText("roll: " + mRoll + "\npitch: " + mPitch + "\nyaw: " + mYaw);
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            if (mMap != null) {
                mMap.stopAnimation();
            }
            switch (pose) {
                case UNKNOWN:
                    //mGestureTextView.setText(getString(R.string.hello_world));
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
                    //mGestureTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    Log.v(LOG_TAG, "Fist");
                    //mMap.animateCamera(CameraUpdateFactory.zoomBy(mRoll));
                    //mGestureTextView.setText(getString(R.string.pose_fist));
                    break;
                case WAVE_IN:
                    Log.v(LOG_TAG, "Wave IN");
                    //mMap.animateCamera(CameraUpdateFactory.zoomOut());
                    //mGestureTextView.setText(getString(R.string.pose_wavein));
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                    break;
                case WAVE_OUT:
                    Log.v(LOG_TAG, "Wave OUT");
                    //mMap.animateCamera(CameraUpdateFactory.scrollBy(((float) 60.5), (float) 45.5));
                    //mGestureTextView.setText(getString(R.string.pose_waveout));
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
                    break;
                case FINGERS_SPREAD:
                    Log.v(LOG_TAG, "Fingers Spread");
                    mUpdateYawOnSpread = true;
                    //mMap.animateCamera(CameraUpdateFactory.scrollBy(((float) -60.5), (float) -45.5));
                    //mGestureTextView.setText(getString(R.string.pose_fingersspread));
//                        mUpdateYawOnSpread = true;
                    break;
//                }
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

    public static class MyMapFragment extends com.google.android.gms.maps.SupportMapFragment {
        CameraPosition mCameraPosition;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View rootView = inflater.inflate(R.layout.fragment_map, container, false);

            scanButton = (Button) rootView.findViewById(R.id.scan_button);
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onScanActionSelected();
                }
            });

            return rootView;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.v(LOG_TAG, "OnCreate");
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
//            final CameraPosition cameraPosition;
            FragmentManager fm = getChildFragmentManager();
            mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.map, mapFragment).commit();

           if(savedInstanceState != null && savedInstanceState.containsKey(CAMERA_POSITION_KEY)) {
                    mCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION_KEY);
                } else mCameraPosition = null;
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {

                        mMap = map;
                        if(mCameraPosition != null) {
                            Log.v(LOG_TAG, "CameraPos :" + mCameraPosition.toString());
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
                        } else {
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    staLatLng, (float) 14.5, 0, 0)));
                        }
                        setUniMarkers();
                    }
                });
            }
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onSaveInstanceState(Bundle savedInstanceState) {
            super.onSaveInstanceState(savedInstanceState);
            if (mMap != null) {
                savedInstanceState.putParcelable(CAMERA_POSITION_KEY, mMap.getCameraPosition());
            }
        }



        private void setUniMarkers(){
            mMap.addMarker(new MarkerOptions().position(staLatLng));
            mMap.addMarker(new MarkerOptions().position(ediLatLng));
            mMap.addMarker(new MarkerOptions().position(glaLatLng));
        }


        //temp delete after, not sure how to add menu at the moment
        private void onScanActionSelected() {
            // Launch the ScanActivity to scan for Myos to connect to.
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            startActivity(intent);
        }
    }
}
