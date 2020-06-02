package com.android.captivePortal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    CheckBox onBoot;
    Button startStop;
    SharedPreferences myPrefs;
    //CheckedTextView ch;
    SharedPreferences.Editor myPrefsPrefsEditor;
    //TextView stdOut;
    static final  String myPref="onBoot";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onBoot=findViewById(R.id.setOnBoot);
        startStop=findViewById(R.id.startStop);
        //ch=findViewById(R.id.ch);
        //stdOut=findViewById(R.id.stdout);
        myPrefs=this.getSharedPreferences(myPref,Context.MODE_PRIVATE);
        boolean checkSetting=myPrefs.getBoolean("setBoot",false);
        boolean installed=myPrefs.getBoolean("installed",false);
        boolean enabled=myPrefs.getBoolean("enabled",false);
        onBoot.setChecked(checkSetting);
        if (enabled){
            startStop.setText("stop");
        }
        else{
            startStop.setText("start");
        }
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        if (!isRootGiven()){
            Toast.makeText(this,"root permission needed",Toast.LENGTH_SHORT).show();
        }
        String pathDir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/captive_portal/www/";
        boolean indexExist=new File(pathDir, "index.html").exists();
        if (!installed || !indexExist)
        //if (true)
         {
            String[] files = {
                    "lighttpd",
                    "capstart",
                    "capstop",
                    "lighttpd.conf",
                    "dns.conf",
                    "dns.pid",
                    "php",
                    "php.ini",
                    "index.html"
            };
            for (String s : files) {
                try {
                    AssetManager assetManager = getAssets();
                    InputStream in = assetManager.open(s);
                    File dataFile = new File(getFilesDir().getPath(), s);
                    DataOutputStream out = new DataOutputStream(new FileOutputStream(dataFile.getAbsolutePath()));
                    byte[] buf = new byte[80000];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                } catch (IOException e) {
                    Toast.makeText(this,"unable to extract assets",Toast.LENGTH_SHORT).show();
                    android.util.Log.e("Error", e.toString());
                }
            }
            if(!isRootGiven()){
                Toast.makeText(this,"root permission needed",Toast.LENGTH_SHORT).show();
            }

                try {
                    Runtime.getRuntime().exec("su");
                    Runtime.getRuntime().exec("chmod -R 755 " + getFilesDir()).waitFor();
                    String out=this.getFilesDir().getAbsolutePath()+"/tmp";
                    File f = new File(out);
                    /*
                    if(!f.mkdir()){
                        Toast.makeText(this,"unable to create folder tmp",Toast.LENGTH_SHORT).show();
                    }*/
                    if(!indexExist) {
                        out = Environment.getExternalStorageDirectory().getAbsolutePath() + "/captive_portal/";
                        CharSequence exStorageErr = "unable to write on external storage";
                        f = new File(out);
                        if (!f.mkdir()) {
                            Toast.makeText(this, exStorageErr, Toast.LENGTH_SHORT).show();
                        }
                        f = new File(out + "www/");
                        if (!f.mkdir()) {
                            Toast.makeText(this, exStorageErr, Toast.LENGTH_SHORT).show();
                        }
                        f = new File(out + "logs/");
                        if (!f.mkdir()) {
                            Toast.makeText(this, exStorageErr, Toast.LENGTH_SHORT).show();
                        }
                        Runtime.getRuntime().exec("mv " + getFilesDir().getAbsolutePath() + "/index.html " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/captive_portal/www/");
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "root permissions not granted", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "root permissions not granted", Toast.LENGTH_SHORT).show();
                }

             myPrefs=this.getSharedPreferences(myPref,Context.MODE_PRIVATE);
             myPrefsPrefsEditor=myPrefs.edit();
             myPrefsPrefsEditor.putBoolean("installed",true);
             myPrefsPrefsEditor.apply();
        }


    }
    public void startStop(View view) {
        if(!isRootGiven()){
            Toast.makeText(this,"root permissions needed",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!myPrefs.getBoolean("enabled",false)){
            startStop.setText("stop");
            myPrefs=this.getSharedPreferences(myPref,Context.MODE_PRIVATE);
            myPrefsPrefsEditor=myPrefs.edit();
            myPrefsPrefsEditor.putBoolean("enabled",true);
            myPrefsPrefsEditor.apply();
            try {
                Runtime.getRuntime().exec("su");
                Runtime.getRuntime().exec(getFilesDir().getPath() + "/capstart").waitFor();
                //InputStream stdout = proc.getInputStream();
                //InputStream stderr = proc.getErrorStream();
                //BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
                //StringBuilder result=new StringBuilder();
                //String line;
                //while((line = br.readLine())!=null)
                    //result.append(line);
                //stdOut.setText(result.toString());
                //br = new BufferedReader(new InputStreamReader(stderr));
                //while ((line = br.readLine())!=null)
                    //System.out.println("stderr: "+line);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    else{
            myPrefs=this.getSharedPreferences(myPref,Context.MODE_PRIVATE);
            myPrefsPrefsEditor=myPrefs.edit();
            myPrefsPrefsEditor.putBoolean("enabled",false);
            myPrefsPrefsEditor.apply();
            startStop.setText("start");
            try {
                Runtime.getRuntime().exec("su");
                Runtime.getRuntime().exec(getFilesDir().getPath() + "/capstop").waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void setStartOnBoot(View view){
        if(!isRootGiven()){
            onBoot.setChecked(false);
            Toast.makeText(this,"root permission needed",Toast.LENGTH_SHORT).show();
            return;
        }
        myPrefs=this.getSharedPreferences(myPref,Context.MODE_PRIVATE);
        myPrefsPrefsEditor=myPrefs.edit();
        myPrefsPrefsEditor.putBoolean("setBoot",onBoot.isChecked());
        myPrefsPrefsEditor.apply();

    }


    public static boolean isRootAvailable(){
        for(String pathDir : System.getenv("PATH").split(":")){
            if(new File(pathDir, "su").exists()){
                return true;
            }
        }
        return false;
    }
    /*
    public static boolean createAp(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(ConnectivityManager.class);
        Method method = null;
        try{
            method = manager.getClass().getDeclaredMethod("startTethering",int.class,boolean.class, Handler.class);
            if (method == null){

            }
            else{
                method.invoke(manager,0,false,null,null);
            }
            return true;

        } catch (NoSuchMethodException e){
            e.printStackTrace();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InvocationTargetException e){
            e.printStackTrace();
        }
        return false;

        private Class classOnStartTetheringCallback(){
            try{
                return Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
            } catch (ClassNotFoundException e){
                e.printStackTrace();
            }
            return null;
        }
    }
    */
    public static boolean isRootGiven(){
        if(isRootAvailable()){
            Process process=null;
            try {
                process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = in.readLine();
                if(output != null && output.toLowerCase().contains("uid=0"))
                    return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (process != null)
                    process.destroy();
            }
        }
        return false;
    }



}
