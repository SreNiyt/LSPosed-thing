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
    private static final String TAG = "XposedRedirect";
    private static HashMap<String, String> redirectMap = new HashMap<>();

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // Universal application: We do NOT filter by packageName
        loadRedirectRules();
        if (redirectMap.isEmpty()) return;

        // 1. Hook File Constructor: The engine of redirection
        XposedHelpers.findAndHookConstructor(File.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String path = (String) param.args[0];
                if (path == null) return;
                for (String internalPath : redirectMap.keySet()) {
                    if (path.startsWith(internalPath)) {
                        String newPath = path.replace(internalPath, redirectMap.get(internalPath));
                        param.args[0] = newPath;
                        return;
                    }
                }
            }
        });

        // 2. Universal Fail-Safe Hooks
        // Protects against null pointers in any app's filesystem operations
        XposedHelpers.findAndHookMethod(File.class, "getName", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.getResult() == null) param.setResult("unknown");
            }
        });

        XposedHelpers.findAndHookMethod(File.class, "exists", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!(Boolean) param.getResult()) param.setResult(true);
            }
        });

        XposedHelpers.findAndHookMethod(File.class, "listFiles", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.getResult() == null) param.setResult(new File[0]);
            }
        });

        // 3. Prevent toString() crashes globally
        XposedHelpers.findAndHookMethod(Object.class, "toString", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.getResult() == null && param.thisObject instanceof File) {
                    param.setResult("redirected_file_object");
                }
            }
        });
    }

    private void loadRedirectRules() {
        File configFile = new File("/data/local/tmp/redirect_config.txt");
        if (!configFile.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    redirectMap.put(parts[0], parts[1]);
                    Log.i(TAG, "Added Rule: " + parts[0] + " -> " + parts[1]);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Rule loading failed", e);
        }
    }
}

