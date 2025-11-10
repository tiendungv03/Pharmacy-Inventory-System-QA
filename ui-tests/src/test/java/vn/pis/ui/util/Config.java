package vn.pis.ui.util;

import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties P = new Properties();

    static {
        try {
            load("config/common.properties");
            safeLoad("secret/creds.properties");
            String env = System.getProperty("ENV", "").trim();
            if (!env.isEmpty()) safeLoad("env/" + env + ".properties");
        } catch (Exception e) {
            throw new RuntimeException("Config init failed", e);
        }
    }
    private static void load(String path) throws Exception {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Missing: " + path);
            P.load(in);
        }
    }
    private static void safeLoad(String path) {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(path)) {
            if (in != null) P.load(in);
        } catch (Exception ignored) {}
    }
    public static String get(String key) {
        String sys = System.getProperty(key);
        if (sys != null) return sys;
        String v = P.getProperty(key);
        if (v != null) return v;
        String envVar = System.getenv(key.replace('.', '_').toUpperCase());
        return envVar;
    }
    public static int getInt(String key, int def) {
        try { return Integer.parseInt(get(key)); } catch (Exception e) { return def; }
    }
}