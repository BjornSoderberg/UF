package se.nextapp.task.free;

import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "", formUri = "http://www.nextapp.se/crash/save_log.php")
public class Application extends android.app.Application {
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}
}
