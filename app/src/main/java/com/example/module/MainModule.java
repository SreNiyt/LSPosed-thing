package com.example.module;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class MainModule implements IXposedHookLoadPackage {
    private static final String TAG = "MyXposedModule";
    private static HashMap<String, String> redirectMap = new HashMap<>();

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // We only care about our target app
        if (!lpparam.packageName.equals("alan.sdcardsize.free")) return;

        Log.i(TAG, "Hooking package: " + lpparam.packageName);
        loadRedirectRules();

        // 1. Hook File Constructor: The core redirection
        XposedHelpers.findAndHookConstructor(File.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String path = (String) param.args[0];
                if (path == null) return;
                for (String internalPath : redirectMap.keySet()) {
                    if (path.startsWith(internalPath)) {
                        param.args[0] = path.replace(internalPath, redirectMap.get(internalPath));
                        return;
                    }
                }
            }
        });

        // 2. Hook exists(): Trick the app into thinking files exist on the SD card
        XposedHelpers.findAndHookMethod(File.class, "exists", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!(Boolean) param.getResult()) {
                    String path = ((File) param.thisObject).getAbsolutePath();
                    for (String newPath : redirectMap.values()) {
                        if (path.startsWith(newPath)) {
                            param.setResult(true);
                            break;
                        }
                    }
                }
            }
        });

        // 3. Hook listFiles(): Stop the NullPointerException by returning an empty array
        XposedHelpers.findAndHookMethod(File.class, "listFiles", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.getResult() == null) {
                    param.setResult(new File[0]);
                }
            }
        });
    }

    private void loadRedirectRules() {
        File configFile = new File("/data/local/tmp/redirect_config.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    redirectMap.put(parts[0], parts[1]);
                    Log.i(TAG, "Rule: " + parts[0] + " -> " + parts[1]);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Config load failed", e);
        }
    }
}

