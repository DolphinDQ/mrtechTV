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
import android.widget.CompoundButton;
import android.widget.Switch;

import mrtech.core.BroadcastSender;
import mrtech.core.RuntimePool;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCPlayer;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.ipc.Models.IPCStateChanged;
import mrtech.smarthome.ipc.Models.IPCStatus;
import mrtech.smarthome.ipc.VideoRenderer;
import mrtech.smarthome.router.Router;
import mrtech.tv.R;
import rx.Subscription;
import rx.functions.Action1;


public class CellFragment extends Fragment {

    private String mPlayId;
    //    private OnFragmentInteractionListener mListener;
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    private int mCellId;
    private Router mRouter;
    private IPCPlayer mPlayer;
    private IPCManager mIPCManager;
    private Subscription mSubscription;
    private VideoRenderer mRenderer;
    private View mCellSelected;
    private Switch mCellSelector;

    public CellFragment() {
        // Required empty public constructor
    }

    public void setCellId(int cellId) {
        mCellId = cellId;
    }

    public static Bundle createArguments(int cellId, String playId) {
        Bundle args = new Bundle();
        args.putString(BroadcastSender.PARAM_PLAY_ID, playId);
        args.putInt(BroadcastSender.PARAM_CELL_ID, cellId);
        return args;
    }

    public static CellFragment newInstance(int cellId, String playId) {
        CellFragment fragment = new CellFragment();
        fragment.setArguments(createArguments(cellId, playId));
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArguments();
    }

    @Override
    public void onStart() {
        initBroadcastReceiver();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        play(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        stop();
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ((TextView) getView().findViewById(R.id.text_view)).setText("Index:" + mCellId);
        final GLSurfaceView glSurfaceView = (GLSurfaceView) getView().findViewById(R.id.gl_view);
        mRenderer = IPCManager.initGLSurfaceView(glSurfaceView);
        mCellSelected = getView().findViewById(R.id.cell_selected);
//        getView().findViewById(R.id.cell_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(), "full", Toast.LENGTH_SHORT).show();
//            }
//        });
        mCellSelector = (Switch) getView().findViewById(R.id.cell_btn);
        mCellSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BroadcastSender.sendSelectCameraAction(getContext(), null, mPlayId, isChecked);
                if (isChecked) {
                    mCellSelected.setVisibility(View.VISIBLE);
                } else {
                    mCellSelected.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initArguments() {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mCellId = arguments.getInt(BroadcastSender.PARAM_CELL_ID);
            play(arguments.getString(BroadcastSender.PARAM_PLAY_ID));
        }
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastSender.ACTION_PLAY_ALL);
        intentFilter.addAction(BroadcastSender.ACTION_PLAY);
        intentFilter.addAction(BroadcastSender.ACTION_STOP_ALL);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BroadcastSender.ACTION_PLAY_ALL)) {
                    play(null);
                }
                if (intent.getAction().equals(BroadcastSender.ACTION_STOP_ALL)) {
                    stop();
                }
                if (intent.getIntExtra(BroadcastSender.PARAM_CELL_ID, -1) != mCellId) return;
                if (intent.getAction().equals(BroadcastSender.ACTION_PLAY)) {
                    final String playId = intent.getStringExtra(BroadcastSender.PARAM_PLAY_ID);
                    play(playId);
                }
            }
        };
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);

    }

    private void play(@Nullable String playId) {
        if (playId == null) {
            if (mPlayId == null) return;
        } else {
            if (playId.equals(mPlayId)) return;
            mPlayId = playId;
        }
        final IPCPlayer player = getPlayer();
        if (player == null) return;
        final IPCamera camera = mIPCManager.getCamera(playId);
        if (mSubscription != null) mSubscription.unsubscribe();
        mSubscription = mIPCManager.createEventManager(camera).subscribeCameraStatus(new Action1<IPCStateChanged>() {
            @Override
            public void call(IPCStateChanged ipcStateChanged) {
                if (ipcStateChanged.getStatus() == IPCStatus.CONNECTED) {
                    player.play(mIPCManager.getCamera(ipcStateChanged.getCameraId()));
                }
            }
        });
        player.play(camera);
        setPlayControls(true);
    }

    private void setPlayControls(final boolean isPlay) {
        new Handler(getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final View view = getView();
                if (view == null) return;
                final View title = view.findViewById(R.id.cell_title);
                if (isPlay) {
                    title.setVisibility(View.GONE);
                } else {
                    title.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void stop() {
        setPlayControls(false);
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
        if (mPlayer != null)
            mPlayer.stop();
//        if (mCellSelector != null && mCellSelector.isChecked()) {
//            mCellSelector.setChecked(false);
//        }
        if (mRenderer != null) {
        }
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
        initRouter();
        return mPlayer;
    }

    private void initRouter() {
        final Router router = RuntimePool.getValue(Router.class);
        try {
            if (router == null) {
                stop();
                mPlayer = null;
            } else {
                if (!router.equals(mRouter) || mPlayer == null) {
                    mRouter = router;
                    mIPCManager = router.getRouterSession()
                            .getCameraManager()
                            .getIPCManager();
                    mPlayer = mIPCManager
                            .createCameraPlayer(mRenderer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
