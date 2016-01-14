package mrtech.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by sphynx on 2016/1/13.
 */
public final class BroadcastSender {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public final static String PARAM_CELL_ID = "CELL_ID";
    public final static String PARAM_PLAY_ID = "PLAY_ID";
    public final static String ACTION_PLAY_ALL = "mrtech.tv.playAll";
    public final static String ACTION_STOP_ALL = "mrtech.tv.stopAll";
    public final static String ACTION_PLAY = "mrtech.tv.play";
    public final static String ACTION_STOP = "mrtech.tv.stop";
    public final static String ACTION_SET_ROUTER = "mrtech.tv.setRouter";

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

}
