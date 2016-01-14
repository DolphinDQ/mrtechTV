package mrtech.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import mrtech.core.BroadcastSender;
import mrtech.core.RuntimePool;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCPlayer;
import mrtech.smarthome.router.Router;
import mrtech.tv.R;


public class CellFragment extends Fragment {

    private String mPlayId;
    //    private OnFragmentInteractionListener mListener;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private int mCellId;
    private Router mRouter;
    private IPCPlayer mPlayer;

    public CellFragment() {
        // Required empty public constructor
    }

    public static CellFragment newInstance(int cellId, String playId) {
        CellFragment fragment = new CellFragment();
        Bundle args = new Bundle();
        args.putString(BroadcastSender.PARAM_PLAY_ID, playId);
        args.putInt(BroadcastSender.PARAM_CELL_ID, cellId);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArguments();
    }
    @Override
    public void onStart() {
        super.onStart();
        initBroadcastListener();
    }
    @Override
    public void onStop() {
        super.onStop();
        mContext.unregisterReceiver(mBroadcastReceiver);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((TextView) getView().findViewById(R.id.text_view)).setText("Index:" + mCellId);
        initRouter();
    }

    private void initArguments() {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mCellId = arguments.getInt(BroadcastSender.PARAM_CELL_ID);
            play(arguments.getString(BroadcastSender.PARAM_PLAY_ID));
        }
    }

    private void initBroadcastListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastSender.ACTION_PLAY_ALL);
        intentFilter.addAction(BroadcastSender.ACTION_PLAY);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra(BroadcastSender.PARAM_CELL_ID, -1) != mCellId) return;
                if (intent.getAction().equals(BroadcastSender.ACTION_PLAY_ALL)) {
                    Toast.makeText(mContext, intent.getStringExtra(BroadcastSender.PARAM_PLAY_ID), Toast.LENGTH_SHORT).show();
                    play(intent.getStringExtra(BroadcastSender.PARAM_PLAY_ID));
                }
            }
        };
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private Void play(@Nullable String playId) {
        if (playId == null) {
            if (mPlayId == null) return null;
        } else {
            if (mPlayId.equals(playId)) return null;
            mPlayId = playId;
        }
        getPlayer();
        return null;
    }

    private void stop() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cell, container, false);
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
//        mListener = null;
    }

    public IPCPlayer getPlayer() {
        new Handler(getActivity().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                initRouter();
            }
        });
        return mPlayer;
    }

    private void initRouter() {
        final Router router = RuntimePool.getValue(Router.class);
        final GLSurfaceView glSurfaceView = (GLSurfaceView) getView().findViewById(R.id.gl_view);
        if (router == null) {
            stop();
            mPlayer = IPCManager.getInstance().createCameraPlayer(glSurfaceView);
        } else {
            if (!router.equals(mRouter)) {
                try {
                    mRouter = router;
                    mPlayer = router.getRouterSession()
                            .getCameraManager()
                            .getIPCManager()
                            .createCameraPlayer(glSurfaceView);
                } catch (Exception e) {
                    e.printStackTrace();
                    mRouter = null;
                    mPlayer = null;
                }
            }
        }
    }

//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p/>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }


}
