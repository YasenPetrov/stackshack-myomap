package com.example.android.myomap;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TweetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TweetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TweetFragment extends Fragment {

    private final String LOG_TAG = TweetFragment.class.getSimpleName();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button cursorButton;
    private Button likeButton;
    private Button hateButton;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TweetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TweetFragment newInstance(String param1, String param2) {
        TweetFragment fragment = new TweetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TweetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_tweet, container, false);
        cursorButton = (Button) rootView.findViewById(R.id.tweet_button);
        likeButton = (Button) rootView.findViewById(R.id.like_button);
        hateButton = (Button) rootView.findViewById(R.id.hate_button);
        cursorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("aFa: ", "" + v.getBottom());
                v.setBottom(v.getBottom() + 100);
                v.setTop(v.getTop() + 100);
                Log.v("aFa: ", "vBottom" + v.getBottom() + " hateTop: " + rootView.findViewById(R.id.hate_button).getTop());

                if (v.getBottom() >= rootView.findViewById(R.id.hate_button).getTop()) {
                    Toast.makeText(getActivity(), "Profit!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
    }

    public void moveCursorButton(int value) {
        cursorButton.setBottom(cursorButton.getBottom() + value);
        cursorButton.setTop(cursorButton.getTop() + value);
        if(cursorButton. getTop() <= likeButton.getBottom()) {
            sendTweet("You're epic!");
        }
        if(cursorButton. getBottom() >= hateButton.getTop()) {
            sendTweet("You guys suck!");
        }
    }


    private void sendTweet(String tweetBody) {
        Log.v(LOG_TAG, "tweet!");
        String tweetString = "#myomap @stacshack\n" + tweetBody;
        String tweetUrl =
                String.format("https://twitter.com/intent/tweet?text=%s",
                        Utility.urlEncode(tweetString));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));

        // Narrow down to official Twitter app, if available:
        List<ResolveInfo> matches = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter")) {
                intent.setPackage(info.activityInfo.packageName);
            }
        }

        startActivity(intent);
        getFragmentManager().popBackStack();

    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    public class HeightAnimation extends Animation {
        protected final int originalHeight;
        protected final View view;
        protected float perValue;

        public HeightAnimation(View view, int fromHeight, int toHeight) {
            this.view = view;
            this.originalHeight = fromHeight;
            this.perValue = (toHeight - fromHeight);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            view.getLayoutParams().height = (int) (originalHeight + perValue * interpolatedTime);
            view.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
