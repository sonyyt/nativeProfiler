package seLab.maven4JDT;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jboss.forge.roaster.Problem;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;


public class packagePermissionProcessor{
	private String permissionRequest = "<uses-permission android:name=\" #permissionNeedle# \"/>\n"; 
	private String filePath; 
	private String permissionFileTemplate = 
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
			"<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" + 
			"    package=\"#packageName#\">\n" + 
			"    #apkPermissionNeedle#\n " +
			"    <application\n" + 
			"        android:allowBackup=\"true\"\n" + 
			"        android:icon=\"@mipmap/ic_launcher\"\n" + 
			"        android:label=\"@string/app_name\"\n" + 
			"        android:roundIcon=\"@mipmap/ic_launcher_round\"\n" + 
			"        android:supportsRtl=\"true\"\n" + 
			"        android:theme=\"@style/AppTheme\">\n" + 
			"        <activity android:name=\".MainActivity\">\n" + 
			"            <intent-filter>\n" + 
			"                <action android:name=\"android.intent.action.MAIN\" />\n" + 
			"\n" + 
			"                <category android:name=\"android.intent.category.LAUNCHER\" />\n" + 
			"            </intent-filter>\n" + 
			"        </activity>\n" + 
			"    </application>\n" + 
			"\n" + 
			"</manifest>";
	
	//read permission File
	public packagePermissionProcessor(String packageName) {
		permissionFileTemplate = permissionFileTemplate.replace("#packageName#",packageName);
	}
	
	
	public void requestPermissions(String[] permissions) {
		String permissionRequestStr = "";
		for(String req:permissions) {
			String tmp = permissionRequest;
			permissionRequestStr += tmp.replace("#persmissionNeedle#", req);
		}

		
		permissionFileTemplate = permissionFileTemplate.replace("<!--apkPermissionNeedle-->", permissionRequestStr);	
	}
	
	public void export(String file) {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(file));
			out.print(permissionFileTemplate);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}