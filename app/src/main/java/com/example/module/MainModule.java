import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;

public class MainModule implements IXposedHookLoadPackage {
    // Map to hold your redirect rules: <InternalPath, ExternalPath>
    private static HashMap<String, String> redirectMap = new HashMap<>();

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // 1. Load rules from a config file (on boot)
        loadRedirectRules();

        // 2. Hook File constructor
        XposedHelpers.findAndHookConstructor(File.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String path = (String) param.args[0];
                
                // 3. Dynamic Check: Does this path need to be redirected?
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
        // Simple file reader that reads: /internal/path=/external/path
        try (BufferedReader br = new BufferedReader(new FileReader("/sdcard/redirect_config.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) redirectMap.put(parts[0], parts[1]);
            }
        } catch (Exception e) { /* Log error */ }
    }
}
public String getExternalSdCardPath() {
    File[] storageDirs = new File("/storage").listFiles();
    for (File file : storageDirs) {
        // Look for the UUID-style folder (not "emulated", not "self")
        if (file.isDirectory() && !file.getName().equals("emulated") && !file.getName().equals("self")) {
            return file.getAbsolutePath();
        }
    }
    return null;
}

