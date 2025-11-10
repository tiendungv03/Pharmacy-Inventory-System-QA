package vn.pis.ui.util;

/** Dùng chung cho toàn bộ test */
public final class TestEnv {
    // Có thể override nhanh bằng VM option: -Dbase.url=http://127.0.0.1:5174
    public static final String BASE_URL   = System.getProperty("base.url", "http://localhost:5173");

    // Nếu muốn hardcode luôn thì bỏ System.getProperty đi:
    // public static final String BASE_URL = "http://localhost:5173";

    public static final String ADMIN_USER = "dung09";
    public static final String ADMIN_PASS = "123456";

    private TestEnv() {} // chặn khởi tạo
}
