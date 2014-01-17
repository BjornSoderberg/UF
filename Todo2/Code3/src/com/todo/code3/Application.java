package com.todo.code3;

import org.acra.*;
import org.acra.annotation.*;

// Change formUri when the server is set up
@ReportsCrashes(formKey = "", formUri = "http://www.backendofyourchoice.com/reportpath")
public class Application extends android.app.Application {
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}

	// Use this php code to save the information. Implement this when the server is set up.
	
	// <?php
	// // Outputs all POST parameters to a text file. The file name is the
	// date_time of the report reception
	// $fileName = date('Y-m-d_H-i-s').'.txt';
	// $file = fopen($fileName,'w') or die('Could not create report file: ' .
	// $fileName);
	// foreach($_POST as $key => $value) {
	// $reportLine = $key." = ".$value."\n";
	// fwrite($file, $reportLine) or die ('Could not write to report file ' .
	// $reportLine);
	// }
	// fclose($file);
	// ?>
}
