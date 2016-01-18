package mrtech.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by sphynx on 2016/1/13.
 */
public final class BroadcastSender {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public final static String PARAM_CELL_ID = "CELL_ID";
    public final static String PARAM_PLAY_ID = "PLAY_ID";
    public final static String PARAM_SELECTED = "SELECTED";
    public final static String ACTION_PLAY_ALL = "mrtech.tv.PlayAll";
    public final static String ACTION_STOP_ALL = "mrtech.tv.StopAll";
    public final static String ACTION_PLAY = "mrtech.tv.Play";
    public final static String ACTION_STOP = "mrtech.tv.Stop";
    public final static String ACTION_SET_ROUTER = "mrtech.tv.SetRouter";
    public final static String ACTION_SELECT_CAMERA = "mrtech.tv.SelectCamera";

    public static void sendPlayAllAction(Context context) {
        context.sendBroadcast(new Intent(ACTION_PLAY_ALL));
    }

    public static void sendStopAllAction(Context context) {
        context.sendBroadcast(new Intent(ACTION_STOP_ALL));
    }

    public static void sendPlayAction(Context context, int cellId, String playId) {
        Intent intent = new Intent(ACTION_PLAY);
        intent.putExtra(PARAM_CELL_ID, cellId);
        intent.putExtra(PARAM_PLAY_ID, playId);
        context.sendBroadcast(intent);
    }

    public static void sendStopAction(Context context, int cellId) {
        Intent intent = new Intent(ACTION_STOP);
        intent.putExtra(PARAM_CELL_ID, cellId);
        context.sendBroadcast(intent);
    }

    public static void sendSetRouterAction(Context context) {
        context.sendBroadcast(new Intent(ACTION_SET_ROUTER));
    }

    /**
     * @param context
     * @param cellId   null表示，发送给主界面。
     * @param playId
     * @param selected
     */
    public static void sendSelectCameraAction(Context context, @Nullable Integer cellId, String playId, boolean selected) {
        Intent intent = new Intent(ACTION_SELECT_CAMERA);
        if (cellId != null)
            intent.putExtra(PARAM_CELL_ID, cellId);
        intent.putExtra(PARAM_PLAY_ID, playId);
        intent.putExtra(PARAM_SELECTED, selected);
        context.sendBroadcast(intent);
    }

}
