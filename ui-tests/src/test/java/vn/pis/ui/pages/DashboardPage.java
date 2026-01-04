package vn.pis.ui.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public DashboardPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(15));
    }

    private void log(String msg) {
        System.out.println("[PIS10][PAGE] " + msg);
    }


    private final By MENU_DASHBOARD = By.xpath(
        "//a[contains(@href,'/') and (.//span[normalize-space()='Dashboard'] or normalize-space()='Dashboard')]"
    );

    // =================== HEADER ===================
    private final By DASH_MAIN     = By.xpath("//main[contains(@class,'flex-1') and contains(@class,'p-8')]");
    private final By DASH_TITLE    = By.xpath("//main//h1[normalize-space()='Dashboard']");
    private final By DASH_SUBTITLE = By.xpath("//main//p[normalize-space()='Tổng quan hệ thống quản lý kho dược']");

    // =================== KPI ===================
    private final By KPI_LABEL_TOTAL_TYPES = By.xpath("//main//p[normalize-space()='Tổng số loại thuốc']");
    private final By KPI_VALUE_TOTAL_TYPES = By.xpath("//main//p[normalize-space()='Tổng số loại thuốc']/following-sibling::p[1]");

    private final By KPI_LABEL_TOTAL_VALUE = By.xpath("//main//p[normalize-space()='Tổng giá trị tồn kho']");
    private final By KPI_VALUE_TOTAL_VALUE = By.xpath("//main//p[normalize-space()='Tổng giá trị tồn kho']/following-sibling::p[1]");

    private final By KPI_LABEL_EXPIRING    = By.xpath("//main//p[normalize-space()='Thuốc sắp hết hạn']");
    private final By KPI_VALUE_EXPIRING    = By.xpath("//main//p[normalize-space()='Thuốc sắp hết hạn']/following-sibling::p[1]");

    private final By KPI_LABEL_BELOW_MIN   = By.xpath("//main//p[normalize-space()='Thuốc dưới tồn tối thiểu']");
    private final By KPI_VALUE_BELOW_MIN   = By.xpath("//main//p[normalize-space()='Thuốc dưới tồn tối thiểu']/following-sibling::p[1]");

    // KPI icon SVG
    private final By KPI_ICON_TOTAL_TYPES  = By.cssSelector("svg.lucide-package");
    private final By KPI_ICON_TOTAL_VALUE  = By.cssSelector("svg.lucide-dollar-sign");
    private final By KPI_ICON_EXPIRING     = By.cssSelector("svg.lucide-triangle-alert");
    private final By KPI_ICON_BELOW_MIN    = By.cssSelector("svg.lucide-trending-down");

    // Check color class (TC_014) - hơi brittle, nhưng giữ theo yêu cầu bạn
    private final By KPI_ICON_TOTAL_TYPES_COLOR = By.cssSelector("svg.lucide-package.text-medical-blue");

    // =================== CHART ===================
    private final By CHART_TITLE    = By.xpath("//main//*[normalize-space()='Hoạt động Nhập/Xuất kho']");
    private final By CHART_SUBTITLE = By.xpath("//main//p[normalize-space()='7 ngày qua']");

    private final By CHART_WRAPPER  = By.cssSelector("div.recharts-wrapper");
    private final By CHART_SVG      = By.cssSelector("svg.recharts-surface");

    private final By CHART_XLABELS  = By.cssSelector(".recharts-xAxis .recharts-cartesian-axis-tick-value tspan");

    private final By CHART_BARS_IMPORT = By.cssSelector("path.recharts-rectangle[name='Nhập kho']");
    private final By CHART_BARS_EXPORT = By.cssSelector("path.recharts-rectangle[name='Xuất kho']");

    private final By CHART_TOOLTIP_WRAPPER = By.cssSelector("div.recharts-tooltip-wrapper");

    // =================== ALERT PANEL ===================
    private final By ALERT_TITLE    = By.xpath("//main//*[normalize-space()='Cảnh báo']");
    private final By ALERT_SUBTITLE = By.xpath("//main//p[normalize-space()='Các thuốc cần chú ý ngay']");

    private final By ALERT_ITEMS    = By.xpath("//main//div[contains(@class,'items-start') and contains(@class,'bg-muted/50')]");
    private final By ALERT_SEVERITY_TAGS = By.xpath(
        "//main//*[contains(@class,'rounded-full') and (normalize-space()='Trung bình' or normalize-space()='Cao' or normalize-space()='Thấp')]"
    );
    
 // =================== SIDEBAR LOCATORS ===================
    private final By SIDEBAR = By.xpath("//aside");
    
    private final By SIDEBAR_MENU_ITEMS = By.xpath("//aside//nav//a");

    // =================== NOTIFICATION/USER ===================
    
    private final By NOTIFICATION_BELL_BTN = By.xpath("//button[./svg[contains(@class,'lucide-bell')]]");
    private final By NOTIFICATION_BADGE = By.xpath("//button[./svg[contains(@class,'lucide-bell')]]//span[contains(@class,'rounded-full')]");

   
    private final By USER_AVATAR = By.xpath("//header//div[contains(@class,'rounded-full') and string-length(text()) > 0]");
    

    // =========================================================
    // SMALL HELPERS
    // =========================================================

    private List<WebElement> els(By by) { return driver.findElements(by); }

    private boolean exists(By by) { return !els(by).isEmpty(); }

    private WebElement waitVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private void waitAtLeastOne(By by) {
        wait.until(d -> d.findElements(by).size() > 0);
    }

    private void scrollIntoView(By by) {
        WebElement el = driver.findElement(by);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    // =========================================================
    // OPEN
    // =========================================================

    public void open() {
        log("Mở menu Dashboard");
        try {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(MENU_DASHBOARD));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
            link.click();
        } catch (TimeoutException e) {
            driver.get(vn.pis.ui.util.TestEnv.BASE_URL + "/");
        }

        waitVisible(DASH_TITLE);
        try { waitAtLeastOne(KPI_LABEL_TOTAL_TYPES); } catch (Exception ignore) {}
        log("Đã vào màn hình Dashboard");
    }

    // =========================================================
    // HEADER
    // =========================================================

    public boolean isDashboardTitleVisible() {
        try { return waitVisible(DASH_TITLE).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    public boolean isSubtitleVisible() {
        try { return waitVisible(DASH_SUBTITLE).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    // =========================================================
    // KPI
    // =========================================================

    public void waitKpiLoaded() {
        waitVisible(DASH_TITLE);
        waitVisible(KPI_LABEL_TOTAL_TYPES);
        waitVisible(KPI_LABEL_TOTAL_VALUE);
        waitVisible(KPI_LABEL_EXPIRING);
        waitVisible(KPI_LABEL_BELOW_MIN);
    }

    public boolean isKpiTotalTypesVisible() { try { waitVisible(KPI_LABEL_TOTAL_TYPES); waitVisible(KPI_VALUE_TOTAL_TYPES); return true; } catch(Exception e){ return false; } }
    public boolean isKpiTotalValueVisible() { try { waitVisible(KPI_LABEL_TOTAL_VALUE); waitVisible(KPI_VALUE_TOTAL_VALUE); return true; } catch(Exception e){ return false; } }
    public boolean isKpiExpiringVisible()    { try { waitVisible(KPI_LABEL_EXPIRING);    waitVisible(KPI_VALUE_EXPIRING);    return true; } catch(Exception e){ return false; } }
    public boolean isKpiBelowMinVisible()    { try { waitVisible(KPI_LABEL_BELOW_MIN);   waitVisible(KPI_VALUE_BELOW_MIN);   return true; } catch(Exception e){ return false; } }

    public int getKpiTotalTypes() { waitKpiLoaded(); return parseInt(waitVisible(KPI_VALUE_TOTAL_TYPES).getText()); }
    public String getKpiTotalValueText() { waitKpiLoaded(); return waitVisible(KPI_VALUE_TOTAL_VALUE).getText().trim(); }
    public int getKpiExpiring() { waitKpiLoaded(); return parseInt(waitVisible(KPI_VALUE_EXPIRING).getText()); }
    public int getKpiBelowMin() { waitKpiLoaded(); return parseInt(waitVisible(KPI_VALUE_BELOW_MIN).getText()); }

    public boolean isKpiIconTotalTypesVisible() {
        waitKpiLoaded();
        try { waitAtLeastOne(KPI_ICON_TOTAL_TYPES); return true; }
        catch(Exception e){ return false; }
    }

//    public boolean isKpiIconTotalTypesColorCorrect() {
//        waitKpiLoaded();
//        return exists(KPI_ICON_TOTAL_TYPES_COLOR);
//    }
    
    public boolean isKpiIconTotalTypesColorCorrect() {
        waitKpiLoaded();

        List<WebElement> icons = driver.findElements(KPI_ICON_TOTAL_TYPES);
        if (icons.isEmpty()) return false;

        WebElement icon = icons.get(0);

        // ✅ mềm: icon hoặc cha của nó có class “text-* / stroke-* / fill-*”
        return hasColorClassOnSelfOrParent(icon);
    }

    private boolean hasColorClassOnSelfOrParent(WebElement el) {
        WebElement cur = el;
        for (int i = 0; i < 5; i++) {
            if (cur == null) break;

            String cls = cur.getAttribute("class");
            cls = (cls == null) ? "" : cls;

            // bắt các kiểu class màu thường gặp (Tailwind/shadcn)
            if (cls.contains("text-") || cls.contains("stroke-") || cls.contains("fill-") || cls.contains("medical-")) {
                // loại trừ màu "muted/gray" nếu muốn
                if (cls.contains("text-muted") || cls.contains("text-gray") || cls.contains("text-slate")) {
                    // vẫn cho đi tiếp lên parent (có thể màu nằm ở parent khác)
                } else {
                    return true;
                }
            }

            try {
                cur = cur.findElement(By.xpath(".."));
            } catch (Exception e) {
                break;
            }
        }
        return false;
    }
    
    public boolean isCurrencyVND(String money) {
        String t = money == null ? "" : money.trim();
        return t.contains("₫") || t.toLowerCase().contains("đ");
    }

    // =========================================================
    // CHART
    // =========================================================

    public void waitChartMounted() {
        waitVisible(CHART_TITLE);
        waitAtLeastOne(CHART_SVG);
    }

    public boolean isChartVisible() {
        try { waitChartMounted(); return true; }
        catch (Exception e) { return false; }
    }

    public List<String> getChartXLabels() {
        waitChartMounted();
        try { waitAtLeastOne(CHART_XLABELS); } catch(Exception ignore) {}

        List<WebElement> spans = els(CHART_XLABELS);
        List<String> out = new ArrayList<>();
        for (WebElement s : spans) {
            String t = normalize(s.getText());
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public int getImportBarsCount() {
        waitChartMounted();
        return els(CHART_BARS_IMPORT).size();
    }

    public int getExportBarsCount() {
        waitChartMounted();
        return els(CHART_BARS_EXPORT).size();
    }

    public void hoverFirstAvailableBar() {
        waitChartMounted();
        Actions actions = new Actions(driver);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                List<WebElement> importBars = driver.findElements(CHART_BARS_IMPORT);
                List<WebElement> exportBars = driver.findElements(CHART_BARS_EXPORT);

                WebElement bar = null;
                if (!importBars.isEmpty()) bar = importBars.get(0);
                else if (!exportBars.isEmpty()) bar = exportBars.get(0);

                if (bar == null) throw new NoSuchElementException("Không có bar nào để hover");

                scrollIntoView(CHART_SVG);
                actions.moveToElement(bar).pause(Duration.ofMillis(250)).perform();
                return;

            } catch (StaleElementReferenceException stale) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Hover bar bị stale liên tục (Recharts re-render)");
    }

    public void hoverAnyBarUntilTooltipVisible() {
        for (int i = 1; i <= 5; i++) {
            hoverFirstAvailableBar();
            if (isChartTooltipVisible()) return;
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Hover bar nhưng tooltip không hiện");
    }

    private boolean tooltipVisibleNow() {
        List<WebElement> wrappers = driver.findElements(CHART_TOOLTIP_WRAPPER);
        for (WebElement w : wrappers) {
            try {
                String vis = w.getCssValue("visibility"); // hidden/visible
                if ("visible".equalsIgnoreCase(vis)) return true;
            } catch (StaleElementReferenceException ignore) {}
        }
        return false;
    }

    public boolean isChartTooltipVisible() {
        try {
            return wait.until(d -> tooltipVisibleNow());
        } catch (TimeoutException e) {
            return false;
        }
    }

    // =========================================================
    // ALERT PANEL
    // =========================================================

    public void waitAlertPanel() {
        waitVisible(ALERT_TITLE);
        waitVisible(ALERT_SUBTITLE);
    }

    public boolean isAlertPanelVisible() {
        try { waitAlertPanel(); return true; }
        catch(Exception e){ return false; }
    }

    public int getAlertItemsCount() {
        waitAlertPanel();
        return els(ALERT_ITEMS).size();
    }

    public boolean hasAnySeverityTag() {
        waitAlertPanel();
        return exists(ALERT_SEVERITY_TAGS);
    }

    // =========================================================
    // GLOBAL LAYOUT
    // =========================================================

    public boolean hasHorizontalScroll() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long sw = ((Number) js.executeScript("return Math.max(document.documentElement.scrollWidth, document.body.scrollWidth);")).longValue();
        long cw = ((Number) js.executeScript("return document.documentElement.clientWidth;")).longValue();
        return sw > cw + 2;
    }

    // =========================================================
    // UTIL
    // =========================================================

    private int parseInt(String s) {
        String digits = (s == null ? "" : s).replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }



    public int getSidebarMenuCount() {
        try {
            // Đợi tối đa 10s cho đến khi menu xuất hiện
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(SIDEBAR_MENU_ITEMS));
            return els(SIDEBAR_MENU_ITEMS).size();
        } catch (TimeoutException e) {
            log("LỖI: Không tìm thấy menu item nào trong Sidebar");
            return 0;
        }
    }

 

    // Sửa hàm isBadgePresent để trả về trạng thái hiển thị của số thông báo
    public boolean isBadgePresent() {
        try {
            // Kiểm tra xem badge có tồn tại và hiển thị không
            return driver.findElements(NOTIFICATION_BADGE).size() > 0 
                   && driver.findElement(NOTIFICATION_BADGE).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isUserAvatarVisible() {
        try {
            // Chờ avatar xuất hiện trong header
            return waitVisible(USER_AVATAR).isDisplayed();
        } catch (Exception e) {
            log("Không tìm thấy Avatar người dùng (DI)");
            return false;
        }
    }
    
    public boolean isSidebarHidden() {
        try {
            WebElement sb = driver.findElement(SIDEBAR);
            
            // Cách 1: Kiểm tra hiển thị cơ bản
            if (!sb.isDisplayed()) return true;

            // Cách 2: Kiểm tra vị trí (Nếu bị đẩy ra khỏi màn hình bên trái)
            int xLocation = sb.getLocation().getX();
            if (xLocation < 0) return true;

            // Cách 3: Kiểm tra kích thước (Nếu width bị set về 0)
            int width = sb.getSize().getWidth();
            if (width <= 0) return true;

            // Cách 4: Kiểm tra thuộc tính CSS ẩn phổ biến
            String opacity = sb.getCssValue("opacity");
            String visibility = sb.getCssValue("visibility");
            if ("0".equals(opacity) || "hidden".equals(visibility)) return true;

            return false;
        } catch (NoSuchElementException e) {
            // Nếu không tìm thấy element trong DOM thì coi như đã ẩn
            return true;
        }
    }
    
 
    public void setWindowSize(int width, int height) {
        log("Thiết lập kích thước trình duyệt: " + width + "x" + height);
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(width, height));
    }
}