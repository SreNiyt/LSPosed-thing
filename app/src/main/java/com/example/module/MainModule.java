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
        Log.i(TAG, "Hooking package: " + lpparam.packageName);
        loadRedirectRules();

        XposedHelpers.findAndHookConstructor(File.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String path = (String) param.args[0];
                if (path == null) return;

                for (String internalPath : redirectMap.keySet()) {
                    if (path.startsWith(internalPath)) {
                        String newPath = path.replace(internalPath, redirectMap.get(internalPath));
                        Log.d(TAG, "Redirecting: " + path + " -> " + newPath);
                        param.args[0] = newPath;
                        return;
                    }
                }
            }
        });
    }

    private void loadRedirectRules() {
        File configFile = new File("/data/local/tmp/redirect_config.txt");
        if (!configFile.exists()) {
            Log.e(TAG, "Config file not found at " + configFile.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    redirectMap.put(parts[0], parts[1]);
                    Log.i(TAG, "Loaded rule: " + parts[0] + " to " + parts[1]);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load redirect rules", e);
        }
    }
}

