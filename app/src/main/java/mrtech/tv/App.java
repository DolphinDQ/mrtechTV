package mrtech.tv;

import com.tencent.bugly.crashreport.CrashReport;

import mrtech.smarthome.SmartHomeApp;
import mrtech.smarthome.router.Router;
import mrtech.smarthome.router.RouterManager;

/**
 * Created by sphynx on 2016/1/12.
 */
public class App extends SmartHomeApp {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(this, "900018908", false);
        RouterManager.getInstance().addRouter(new Router("router","6YP84F-50XKQ2-DX2V4T-BZ8L6C-UAX9NY-9D2"));
    }
}
