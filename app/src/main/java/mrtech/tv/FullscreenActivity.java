package mrtech.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import mrtech.core.BroadcastSender;
import mrtech.core.RuntimePool;
import mrtech.fragment.FourViewFragment;
import mrtech.fragment.NineViewFragment;
import mrtech.fragment.SingleViewFragment;
import mrtech.fragment.SixViewFragment;
import mrtech.smarthome.ipc.IPCManager;
import mrtech.smarthome.ipc.IPCamera;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import rx.Subscription;
import rx.functions.Action1;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private boolean readyExit;
    private View mControlMenu;
    private View mSettingsBtn;
    private BroadcastReceiver mBroadcastReceiver;
    private Fragment mCurrentFragment;
    private long mLastChangeFragmentTime;

    private static void trace(String msg) {
        Log.e(FullscreenActivity.class.getName(), msg);
    }

    private RouterManager mRouterManager;
    private Context mContext;
    private ArrayList<Subscription> subscriptions = new ArrayList<>();
    private HashMap<Integer, Class<? extends Fragment>> mFragments = new HashMap<>();
    private List<IPCamera> mCameraList = new ArrayList<>();
    private ArrayList<String> mSelectedCameraIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.hide();
        initBroadcastReceiver();
        initRouterManager();
        initFragment();
        initControls();
    }

    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadcastSender.ACTION_SELECT_CAMERA);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BroadcastSender.ACTION_SELECT_CAMERA)) {
                    final int cellId = intent.getIntExtra(BroadcastSender.PARAM_CELL_ID, -1);
                    if (cellId == -1) { //未指定cellId，即发给主屏幕。
                        final String playId = intent.getStringExtra(BroadcastSender.PARAM_PLAY_ID);
                        final boolean selected = intent.getBooleanExtra(BroadcastSender.PARAM_SELECTED, false);
                        if (playId != null) {
                            if (selected) {
                                if (!mSelectedCameraIds.contains(playId))
                                    mSelectedCameraIds.add(playId);
                            } else {
                                if (mSelectedCameraIds.contains(playId))
                                    mSelectedCameraIds.remove(playId);
                            }
                        }
                    }
                }
            }
        };
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void initFragment() {
        mFragments.put(1, SingleViewFragment.class);
        mFragments.put(4, FourViewFragment.class);
        mFragments.put(6, SixViewFragment.class);
        mFragments.put(9, NineViewFragment.class);
    }

    private void initControls() {
        mSettingsBtn = findViewById(R.id.set_router_btn);
        mSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                builder.setTitle(getText(R.string.input_router_sn));

                final EditText input = new EditText(FullscreenActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String sn = input.getText().toString();
                        new AsyncTask<Void, Void, Router>() {
                            @Override
                            protected Router doInBackground(Void... params) {
                                Router router = new Router(getText(R.string.router).toString(), sn);
                                mRouterManager.addRouter(router);
                                return mRouterManager.getRouter(sn);
                            }

                            @Override
                            protected void onPostExecute(Router router) {
                                if (!router.getRouterSession().isSNValid()) {
                                    mRouterManager.removeRouter(router);
                                    Toast.makeText(FullscreenActivity.this, getText(R.string.invalid_sn), Toast.LENGTH_SHORT).show();
                                } else {
                                    mRouterManager.removeAllRouters(true);
                                    setRouter(router);
                                }
                            }
                        }.execute();
                        hideStatusBar();
                    }
                });

                builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        hideStatusBar();
                    }
                });

                builder.show();
            }
        });
        findViewById(R.id.single_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViews(1);
            }
        });
        findViewById(R.id.four_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViews(4);
            }
        });
        findViewById(R.id.six_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViews(6);
            }
        });
        findViewById(R.id.nine_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViews(9);
            }
        });


        findViewById(R.id.reset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Router router = RuntimePool.getValue(Router.class);
                if (router != null) {
                    loadIPC(router);
                } else {
                    Toast.makeText(FullscreenActivity.this, R.string.no_router, Toast.LENGTH_SHORT).show();
                    mSettingsBtn.callOnClick();
                }
            }
        });
        setViews(1);
        mControlMenu = findViewById(R.id.fullscreen_content_controls);
        toggleControlMenu(false);
    }

    private void initRouterManager() {
        mRouterManager = RouterManager.getInstance();
        mRouterManager.loadRouters();
        if (mRouterManager.getRouterList().size() > 0) {
            setRouter(mRouterManager.getRouterList().get(0));
        }
        //路由器验证成功后自动加载摄像头。
        final Subscription subscription = mRouterManager.getEventManager().subscribeRouterStatusChangedEvent(new Action1<Router>() {
            @Override
            public void call(final Router router) {
                if (router.getRouterSession().isAuthenticated()) {
                    loadIPC(router);
                }
            }
        });
        subscriptions.add(subscription);
    }

    private void loadIPC(final Router router) {
        if (router == null) return;
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FullscreenActivity.this, R.string.loading_camera, Toast.LENGTH_SHORT).show();
            }
        });
        router.getRouterSession().getCameraManager().reloadIPCAsync(false, new Action1<Throwable>() {
            @Override
            public void call(final Throwable throwable) {
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (throwable != null) {
                            throwable.printStackTrace();
                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, router.getName() + getText(R.string.get_camera_failed) + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            final List<IPCamera> cameraList = router.getRouterSession().getCameraManager().getIPCManager().getCameraList();
                            if (cameraList.size() > 0) {
                                mCameraList = cameraList;
//                                Toast.makeText(mContext, router.getName() + "加载摄像头" + cameraList.size(), Toast.LENGTH_SHORT).show();
                                setViews(cameraList.size());
                            }
                        }
                    }
                });
            }
        });
    }

    private Fragment selectFragment(int size) {
        final Set<Integer> integers = mFragments.keySet();
        int result = 1;
        int match = size;
        for (Integer integer : integers) {
            int m = integer - size;
            if (m >= 0 && m <= match) {
                result = integer;
                match = m;
            }
            if (match == 0) break;
        }
        try {
            return mFragments.get(result).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void changFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // 将 cell_view_container View 中的内容替换为此 Fragment ，
        // 然后将该事务添加到返回堆栈，以便用户可以向后导航
        transaction.replace(R.id.cell_view_container, fragment);
        // 执行事务
        transaction.commit();

        mCurrentFragment = fragment;

    }

    private synchronized void setViews(final int size) {
        final long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - mLastChangeFragmentTime < 500) return;
        mLastChangeFragmentTime = currentTimeMillis;
        final Fragment fragment = selectFragment(size);
        if (fragment == null) return;
        final boolean selectedCamera = mSelectedCameraIds.size() > 0;
        if (!fragment.equals(mCurrentFragment)) {
            changFragment(fragment);
        } else {
            if (!selectedCamera) return;
            BroadcastSender.sendStopAllAction(mContext);
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (selectedCamera) {
                    final Router router = RuntimePool.getValue(Router.class);
                    if (router != null) {
                        final IPCManager ipcManager = router.getRouterSession().getCameraManager().getIPCManager();
                        for (int i = 0; i < mSelectedCameraIds.size(); i++) {
                            final String deviceId = mSelectedCameraIds.get(i);
                            final IPCamera camera = ipcManager.getCamera(deviceId);
                            if (camera != null) {
                                BroadcastSender.sendPlayAction(mContext, i, deviceId);
                            }
                        }
                    }
                    mSelectedCameraIds.clear();
                } else {
                    for (int i = 0; i < mCameraList.size(); i++) {
                        if (i >= size) break;
                        final IPCamera camera = mCameraList.get(i);
                        BroadcastSender.sendPlayAction(mContext, i, camera.getDeviceId());
                    }
                }
                return null;
            }
        }.execute();

    }

    private void setRouter(Router router) {
        final Router value = RuntimePool.getValue(Router.class);
        if (value == null || !value.equals(router)) {
            BroadcastSender.sendStopAllAction(mContext);
            RuntimePool.setValue(Router.class, router);
            BroadcastSender.sendSetRouterAction(mContext);
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        hideStatusBar();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!readyExit) {
                readyExit = true;
                Toast.makeText(this, "再按一下退出", Toast.LENGTH_SHORT).show();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            //igne
                        }
                        readyExit = false;
                        return null;
                    }
                }.execute();
                return true;
            } else {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleControlMenu(mControlMenu.getVisibility() == View.GONE);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mControlMenu.getVisibility() == View.VISIBLE)
                toggleControlMenu(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        mContext.unregisterReceiver(mBroadcastReceiver);
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
        super.onDestroy();
    }

    private void toggleControlMenu(boolean show) {
        if (show) {
            mControlMenu.setVisibility(View.VISIBLE);
            findViewById(R.id.single_view_btn).requestFocus();
        } else {
            mControlMenu.setVisibility(View.GONE);
            mControlMenu.requestFocus();
        }
    }

    private void hideStatusBar() {
        findViewById(R.id.container).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

}
