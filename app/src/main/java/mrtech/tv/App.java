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
        RouterManager.getInstance().addRouter(new Router("router","H9DBV0-46UO0S-97UQDF-GPIN8N-C2AJOV-BB2"));
    }
}
