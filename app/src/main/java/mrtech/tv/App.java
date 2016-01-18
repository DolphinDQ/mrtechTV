package mrtech.tv;

import android.os.Handler;
import android.widget.Toast;

import mrtech.smarthome.SmartHomeApp;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;
import rx.functions.Action1;

/**
 * Created by sphynx on 2016/1/12.
 */
public class App extends SmartHomeApp {
    @Override
    public void onCreate() {
        super.onCreate();
        RouterManager.getInstance().addRouter(new Router("router","6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2"));
    }
}
