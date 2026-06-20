package com.example.module;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class MainModule implements IXposedHookLoadPackage {
    private static HashMap<String, String> redirectMap = new HashMap<>();

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        loadRedirectRules();

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
    }

    private void loadRedirectRules() {
        try (BufferedReader br = new BufferedReader(new FileReader("/sdcard/redirect_config.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) redirectMap.put(parts[0], parts[1]);
            }
        } catch (Exception e) { 
            // Optional: XposedBridge.log("Failed to load redirect rules: " + e.getMessage());
        }
    }

    // Now correctly inside the class!
    public String getExternalSdCardPath() {
        File storageDir = new File("/storage");
        File[] storageDirs = storageDir.listFiles();
        if (storageDirs == null) return null;
        
        for (File file : storageDirs) {
            if (file.isDirectory() && !file.getName().equals("emulated") && !file.getName().equals("self")) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
}

