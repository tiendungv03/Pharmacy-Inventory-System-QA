package vn.pis.ui.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    // Đọc flag từ system property/env để linh hoạt khi chạy CI: -Dheadless=true -DimagesOff=true -Dpls=eager
    private static boolean sysPropTrue(String key) {
        String v = System.getProperty(key, System.getenv().getOrDefault(key.toUpperCase(), "false"));
        return "true".equalsIgnoreCase(v) || "1".equals(v);
    }

    @BeforeClass
    public void setup() {
        // Tự động khớp chromedriver với Chrome hiện có
        WebDriverManager.chromedriver().setup();

        ChromeOptions opts = new ChromeOptions();

        // --- Preferences: giảm tải & tắt bubble password ---
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("password_manager_leak_detection_enabled", false);
        // Tắt tải ảnh nếu muốn giảm RAM: -DimagesOff=true
        if (sysPropTrue("imagesOff")) {
            prefs.put("profile.managed_default_content_settings.images", 2);
        }
        // Tắt thông báo
        prefs.put("profile.default_content_setting_values.notifications", 2);
        opts.setExperimentalOption("prefs", prefs);

        // --- Mẹo ổn định trên Linux/Ubuntu ---
        opts.addArguments(
                "--incognito",                 // profile sạch
                "--no-sandbox",                // tránh sandbox crash trong 1 số môi trường
                "--disable-dev-shm-usage",     // không dùng /dev/shm (thường quá nhỏ) -> giảm crash
                "--disable-gpu",               // tránh xung đột driver GPU
                "--disable-extensions",
                "--disable-notifications",
                "--disable-infobars",
                "--window-size=1366,768"
        );

        // Headless mặc định nên bật trên CI: -Dheadless=true
        if (sysPropTrue("headless")) {
            opts.addArguments("--headless=new");
        }

        // PageLoadStrategy: có thể chuyển qua -Dpls=eager để thao tác sớm khi DOM chính sẵn sàng
        String pls = System.getProperty("pls", "normal").toLowerCase();
        opts.setPageLoadStrategy(
                "eager".equals(pls) ? PageLoadStrategy.EAGER : PageLoadStrategy.NORMAL
        );

        // (tuỳ chọn) giảm log noise của driver:
        // System.setProperty("webdriver.chrome.silentOutput", "true");

        driver = new ChromeDriver(opts);

        // --- Timeouts: vô hiệu implicit, set page/script rõ ràng ---
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

        // Headed thì maximize; headless đã set window-size ở trên
        if (!sysPropTrue("headless")) {
            try { driver.manage().window().maximize(); } catch (Exception ignored) {}
        }

        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
        }
    }
}
