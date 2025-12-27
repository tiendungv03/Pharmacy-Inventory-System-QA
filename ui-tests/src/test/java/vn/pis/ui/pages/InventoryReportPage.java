// File: src/test/java/vn/pis/ui/pages/InventoryReportPage.java
package vn.pis.ui.pages;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InventoryReportPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final long reportWaitMs;
    private String pageUrl;

    // ===== keywords (không hard-code xpath text quá nhiều) =====
    private static final String[] CREATE_REPORT_KEYS = { "tạo báo cáo", "tao bao cao", "create report" };
    private static final String[] EXPORT_PDF_KEYS = { "xuất pdf", "xuat pdf", "export pdf", "pdf" };

    private static final String[] NO_DATA_KEYS = {
            "không có dữ liệu", "khong co du lieu", "chưa có", "chua co", "no data"
    };

    private static final String[] END_BEFORE_START_KEYS = {
            "ngày kết thúc không được nhỏ hơn ngày bắt đầu",
            "ngay ket thuc khong duoc nho hon ngay bat dau",
            "end date must not be before start date",
            "end date must be after start date"
    };

    private static final String[] GENERIC_ALERT_KEYS = {
            "không hợp lệ", "khong hop le", "invalid",
            "bắt buộc", "bat buoc",
            "không được để trống", "khong duoc de trong",
            "vui lòng", "vui long", "error"
    };

    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public InventoryReportPage(WebDriver driver, Duration seleniumTimeout, long reportWaitMs) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, seleniumTimeout);
        this.reportWaitMs = reportWaitMs;
    }

    // ================== LOCATORS ==================
    public final By pageTitle = By.xpath("//h1[normalize-space()='Báo cáo Xuất-Nhập-Tồn']");
    public final By pageSubtitle = By.xpath(
            "//p[contains(@class,'text-muted-foreground') and contains(normalize-space(.),'Theo dõi và phân tích hoạt động kho dược')]");

    public final By startDateLabel = By.cssSelector("label[for='startDate']");
    public final By endDateLabel = By.cssSelector("label[for='endDate']");
    public final By startDateInput = By.id("startDate");
    public final By endDateInput = By.id("endDate");

    public final By detailReportTitle = By.xpath("//*[contains(normalize-space(.),'Báo cáo chi tiết')]");
    public final By monthlyTrendTitle = By.xpath("//*[contains(normalize-space(.),'Xu hướng theo tháng')]");
    public final By statusDistTitle = By.xpath("//*[contains(normalize-space(.),'Phân bổ trạng thái giao dịch')]");

    private final By anyAlertLikeInMain = By.xpath(
            "//main//*[@role='alert' or @aria-live='polite' or @aria-live='assertive' " +
                    "or contains(@class,'destructive') or contains(@class,'text-destructive') " +
                    "or contains(@class,'text-red') or contains(@class,'error')]");

    // ================== OPEN / RESET ==================
    public void openAndWaitReports(String url, String mustContainPath, String defaultStart, String defaultEnd) {
        this.pageUrl = url;
        driver.get(url);

        new WebDriverWait(driver, Duration.ofMillis(reportWaitMs))
                .until(d -> d.getCurrentUrl().contains(mustContainPath) || d.getCurrentUrl().contains("/login"));

        if (driver.getCurrentUrl().contains("/login")) {
            throw new AssertionError("Login required to open reports.");
        }

        waitDomReady(reportWaitMs);
        waitLoaded();
        // default date hydrate (nếu FE set)
        waitDefaultDatesHydrated(defaultStart, defaultEnd, reportWaitMs);
        waitUiSettled();
    }

    public void resetToDefaultRange(String defaultStart, String defaultEnd) {
        reset();
        waitDefaultDatesHydrated(defaultStart, defaultEnd, reportWaitMs);
        waitUiSettled();
    }

    public void reset() {
        if (pageUrl != null)
            driver.get(pageUrl);
        else
            driver.navigate().refresh();
        waitDomReady(reportWaitMs);
        waitLoaded();
    }

    private void waitDomReady(long timeoutMs) {
        new WebDriverWait(driver, Duration.ofMillis(timeoutMs)).until(d -> {
            try {
                Object rs = ((JavascriptExecutor) d).executeScript("return document.readyState");
                return "complete".equals(String.valueOf(rs));
            } catch (Exception e) {
                return true;
            }
        });
    }

    private void waitDefaultDatesHydrated(String start, String end, long timeoutMs) {
        new WebDriverWait(driver, Duration.ofMillis(timeoutMs)).until(d -> {
            try {
                WebElement s = d.findElement(startDateInput);
                WebElement e = d.findElement(endDateInput);
                String sv = String.valueOf(s.getAttribute("value"));
                String ev = String.valueOf(e.getAttribute("value"));

                // input phải hiện + enable
                boolean ok = (Boolean) ((JavascriptExecutor) d).executeScript(
                        "const a=document.getElementById('startDate');" +
                                "const b=document.getElementById('endDate');" +
                                "return !!(a&&b&&a.type==='date'&&b.type==='date'&&a.offsetParent!==null&&b.offsetParent!==null&&!a.disabled&&!b.disabled);");
                return ok && start.equals(sv) && end.equals(ev);
            } catch (Exception ex) {
                return false;
            }
        });
    }

    public void waitLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        wait.until(ExpectedConditions.visibilityOfElementLocated(startDateInput));
        wait.until(ExpectedConditions.visibilityOfElementLocated(endDateInput));

        // nút theo keyword (không phụ thuộc text exact)
        findCreateReportButton();
        findExportPdfButton();

        ensureNetworkSpyInstalled();
        ensureCreateClickSpyInstalled();
        ensureExportSpyInstalled();
        ensureExportClickSpyInstalled();

        waitUiSettled();
    }

    // ================== TEXT UTILS ==================
    public String norm(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    public String normFold(String s) {
        if (s == null)
            return "";
        String lower = s.toLowerCase(Locale.ROOT);
        String noAccent = Normalizer.normalize(lower, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        noAccent = noAccent.replace('đ', 'd');
        return noAccent.replaceAll("\\s+", " ").trim();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    public void waitUiSettled() {
        // chờ: pendingReq = 0 + 2 frame (tránh “re-render” làm mất value)
        long end = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < end) {
            if (getPendingReq() == 0) {
                try {
                    ((JavascriptExecutor) driver).executeAsyncScript(
                            "const done = arguments[arguments.length-1];" +
                                    "requestAnimationFrame(()=>requestAnimationFrame(()=>done(true)));");
                } catch (Exception ignored) {
                }
                return;
            }
            sleep(80);
        }
    }

    private boolean isDisplayedSafe(WebElement el) {
        try {
            return el != null && el.isDisplayed();
        } catch (Exception ignored) {
            return false;
        }
    }

    // ================== BASIC FINDERS ==================
    public WebElement findDisplayed(By by) {
        List<WebElement> els = driver.findElements(by);
        for (WebElement e : els) {
            if (isDisplayedSafe(e))
                return e;
        }
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(by));
        if (!isDisplayedSafe(el))
            throw new AssertionError("Element not displayed: " + by);
        return el;
    }

    public boolean isDisplayed(By by) {
        for (WebElement e : driver.findElements(by)) {
            if (isDisplayedSafe(e))
                return true;
        }
        return false;
    }

    private String textForSearch(WebElement el) {
        if (el == null)
            return "";
        String t = el.getText();
        if (t == null || t.trim().isEmpty())
            t = el.getAttribute("aria-label");
        if (t == null || t.trim().isEmpty())
            t = el.getAttribute("title");
        return t == null ? "" : t;
    }

    private boolean containsAnyFolded(String text, String... keywords) {
        String t = normFold(text);
        for (String kw : keywords) {
            if (kw == null || kw.isBlank())
                continue;
            if (t.contains(normFold(kw)))
                return true;
        }
        return false;
    }

    private WebElement findButtonByKeywordsNoWait(String... keywords) {
        for (WebElement b : driver.findElements(By.tagName("button"))) {
            if (!isDisplayedSafe(b))
                continue;
            if (containsAnyFolded(textForSearch(b), keywords))
                return b;
        }
        return null;
    }

    private WebElement waitForButtonByKeywords(String... keywords) {
        return wait.until(d -> findButtonByKeywordsNoWait(keywords));
    }

    public WebElement findCreateReportButton() {
        return waitForButtonByKeywords(CREATE_REPORT_KEYS);
    }

    public WebElement findExportPdfButton() {
        return waitForButtonByKeywords(EXPORT_PDF_KEYS);
    }

    // ================== ICON near input ==================
    public boolean hasSvgIconNearInput(By inputBy) {
        WebElement input = findDisplayed(inputBy);
        try {
            WebElement wrap = input
                    .findElement(By.xpath("ancestor::div[contains(@class,'relative') or contains(@class,'flex')][1]"));
            return wrap.findElements(By.xpath(".//*[local-name()='svg']")).size() > 0;
        } catch (Exception e) {
            try {
                WebElement parent = input.findElement(By.xpath(".."));
                return parent.findElements(By.xpath(".//*[local-name()='svg']")).size() > 0;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    // ================== RECT/OVERLAP ==================
    public Rectangle rect(By by) {
        return findDisplayed(by).getRect();
    }

    public boolean overlap(Rectangle a, Rectangle b) {
        int ax2 = a.getX() + a.getWidth();
        int ay2 = a.getY() + a.getHeight();
        int bx2 = b.getX() + b.getWidth();
        int by2 = b.getY() + b.getHeight();
        return a.getX() < bx2 && ax2 > b.getX() && a.getY() < by2 && ay2 > b.getY();
    }

    // ================== FOCUS / KEY ==================
    public void clickOutside() {
        ((JavascriptExecutor) driver).executeScript("document.body.click();");
        waitUiSettled();
    }

    public void focus(WebElement el) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].focus();", el);
    }

    public void focusClick(By by) {
        WebElement el = findDisplayed(by);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
        waitUiSettled();
    }

    public void pressKey(Keys key) {
        new Actions(driver).sendKeys(key).perform();
    }

    private WebElement activeButtonLikeElement() {
        try {
            Object el = ((JavascriptExecutor) driver).executeScript(
                    "var a=document.activeElement; if(!a) return null;" +
                            "var b=a.closest('button,[role=\"button\"]'); return b?b:a;");
            return (WebElement) el;
        } catch (Exception e) {
            return driver.switchTo().activeElement();
        }
    }

    public String activeButtonLikeText() {
        return norm(textForSearch(activeButtonLikeElement()));
    }

    public void tabUntilActiveId(String id, int maxTabs) {
        for (int i = 0; i < maxTabs; i++) {
            WebElement ae = driver.switchTo().activeElement();
            if (id.equals(ae.getAttribute("id")))
                return;
            pressKey(Keys.TAB);
            sleep(25);
        }
        throw new AssertionError("Tab không tới được element id=" + id);
    }

    public void tabUntilActiveButtonLikeContainsText(String text, int maxTabs) {
        String t = normFold(text);
        for (int i = 0; i < maxTabs; i++) {
            WebElement b = activeButtonLikeElement();
            String tt = normFold(textForSearch(b));
            String tag = (b.getTagName() == null) ? "" : b.getTagName().toLowerCase(Locale.ROOT);
            String role = String.valueOf(b.getAttribute("role"));
            boolean isButtonLike = "button".equals(tag) || "button".equalsIgnoreCase(role);
            if (isButtonLike && tt.contains(t))
                return;
            pressKey(Keys.TAB);
            sleep(25);
        }
        throw new AssertionError(
                "Tab không tới được button-like chứa text: " + text + ". active=" + activeButtonLikeText());
    }

    // ================== BUTTON TYPE SAFE ==================
    public boolean isButtonTypeSafe(WebElement btn) {
        String type = btn.getAttribute("type");
        String t = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);

        boolean hasForm = false;
        try {
            Object v = ((JavascriptExecutor) driver).executeScript("return arguments[0].closest('form') != null;", btn);
            hasForm = (v instanceof Boolean) && (Boolean) v;
        } catch (Exception ignored) {
        }

        if (!hasForm)
            return true;
        return t.isEmpty() || "button".equals(t);
    }

    public long getLabelsCountFor(String inputId) {
        Object v = ((JavascriptExecutor) driver).executeScript(
                "var el=document.getElementById(arguments[0]);" +
                        "if(!el) return 0;" +
                        "return (el.labels && el.labels.length) ? el.labels.length : 0;",
                inputId);
        if (v instanceof Number)
            return ((Number) v).longValue();
        return Long.parseLong(String.valueOf(v));
    }

    // ================== DATE INPUT (fix triệt để: JS + blur + fallback sendKeys)
    // ==================
    private String toIsoDate(String raw) {
        if (raw == null)
            return "";
        raw = raw.trim();
        if (raw.matches("\\d{4}-\\d{2}-\\d{2}"))
            return raw;

        // mm/dd/yyyy
        if (raw.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            String[] p = raw.split("/");
            int mm = Integer.parseInt(p[0]);
            int dd = Integer.parseInt(p[1]);
            int yy = Integer.parseInt(p[2]);
            return String.format("%04d-%02d-%02d", yy, mm, dd);
        }

        // dd/MM/yyyy (nếu lỡ nhập theo VN)
        if (raw.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
            String[] p = raw.split("-");
            int dd = Integer.parseInt(p[0]);
            int mm = Integer.parseInt(p[1]);
            int yy = Integer.parseInt(p[2]);
            return String.format("%04d-%02d-%02d", yy, mm, dd);
        }

        return raw; // fail-fast
    }

    private String isoToUs(String iso) {
        try {
            LocalDate d = LocalDate.parse(iso, ISO);
            return String.format("%02d/%02d/%04d", d.getMonthValue(), d.getDayOfMonth(), d.getYear());
        } catch (Exception e) {
            return iso;
        }
    }

    private String getValue(By inputBy) {
        try {
            return String.valueOf(driver.findElement(inputBy).getAttribute("value"));
        } catch (Exception e) {
            return "";
        }
    }

    private void setDateByJs(WebElement input, String iso) {
        ((JavascriptExecutor) driver).executeScript(
                "const el = arguments[0]; const val = arguments[1];" +
                        "el.scrollIntoView({block:'center'});" +
                        "el.focus();" +
                        "const proto = Object.getPrototypeOf(el);" +
                        "const desc = Object.getOwnPropertyDescriptor(proto,'value') || Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value');"
                        +
                        "if(desc && desc.set) desc.set.call(el, val); else el.value = val;" +
                        "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                        "el.dispatchEvent(new Event('change',{bubbles:true}));" +
                        "el.dispatchEvent(new Event('blur',{bubbles:true}));" +
                        "el.dispatchEvent(new Event('focusout',{bubbles:true}));",
                input, iso);
    }

    private void clearAndType(WebElement input, String text) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].focus();", input);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(text);
        input.sendKeys(Keys.TAB); // nhiều FE chỉ commit khi blur/tab
    }

    public void setDate(By inputBy, String anyDate) {
        String iso = toIsoDate(anyDate);
        if (!iso.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new AssertionError("Date format invalid (need yyyy-MM-dd or mm/dd/yyyy): " + anyDate);
        }

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(inputBy));
        if (!input.isEnabled())
            throw new AssertionError("Date input disabled: " + inputBy);

        // 1) JS set (React controlled)
        setDateByJs(input, iso);
        waitUiSettled();
        if (iso.equals(getValue(inputBy)))
            return;

        // 2) fallback: sendKeys ISO
        try {
            input = driver.findElement(inputBy);
            clearAndType(input, iso);
            waitUiSettled();
            if (iso.equals(getValue(inputBy)))
                return;
        } catch (Exception ignored) {
        }

        // 3) fallback: sendKeys mm/dd/yyyy (Edge locale)
        try {
            input = driver.findElement(inputBy);
            clearAndType(input, isoToUs(iso));
            waitUiSettled();
            if (iso.equals(getValue(inputBy)))
                return;
        } catch (Exception ignored) {
        }

        // 4) cuối cùng: JS set lại và fail nếu vẫn không dính
        input = driver.findElement(inputBy);
        setDateByJs(input, iso);
        waitUiSettled();

        String finalVal = getValue(inputBy);
        if (!iso.equals(finalVal)) {
            throw new AssertionError("Cannot set date input. want=" + iso + " but actual=" + finalVal +
                    " | input=" + inputBy);
        }
    }

    public void clearDate(By inputBy) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(inputBy));
        if (!input.isEnabled())
            return;
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "const el=arguments[0];" +
                            "el.focus();" +
                            "const proto=Object.getPrototypeOf(el);" +
                            "const desc=Object.getOwnPropertyDescriptor(proto,'value') || Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value');"
                            +
                            "if(desc && desc.set) desc.set.call(el,''); else el.value='';" +
                            "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                            "el.dispatchEvent(new Event('change',{bubbles:true}));" +
                            "el.dispatchEvent(new Event('blur',{bubbles:true}));" +
                            "el.dispatchEvent(new Event('focusout',{bubbles:true}));",
                    input);
        } catch (Exception ignored) {
        }

        waitUiSettled();
        String v = getValue(inputBy);
        if (v != null && !v.isEmpty()) {
            // fallback typing clear
            try {
                clearAndType(driver.findElement(inputBy), "");
            } catch (Exception ignored) {
            }
            waitUiSettled();
        }
    }

    public void setRangeAndSettle(String start, String end) {
        setDate(startDateInput, start);
        setDate(endDateInput, end);
        waitUiSettled();
    }

    // ================== PERIOD TEXT ==================
    private static int[] ymd(String yyyyMmDd) {
        String[] p = yyyyMmDd.split("-");
        return new int[] { Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]) };
    }

    private static String dmyNoZero(String yyyyMmDd) {
        int[] a = ymd(yyyyMmDd);
        return a[2] + "/" + a[1] + "/" + a[0];
    }

    public String getPeriodTextOrEmpty() {
        List<WebElement> els = driver.findElements(By.xpath(
                "//main//*[contains(normalize-space(.),'Khoảng thời gian') or contains(normalize-space(.),'Khoang thoi gian')]"));
        for (WebElement e : els) {
            if (!isDisplayedSafe(e))
                continue;
            String t = norm(e.getText());
            if (!t.isEmpty())
                return t;
        }
        return "";
    }

    public boolean isPeriodTextShown(String start, String end) {
        String s = toIsoDate(start);
        String e = toIsoDate(end);
        String a = dmyNoZero(s);
        String b = dmyNoZero(e);

        String p = getPeriodTextOrEmpty();
        if (!p.isEmpty())
            return p.contains(a) && p.contains(b);

        // fallback: search main text
        String body = String.valueOf(((JavascriptExecutor) driver).executeScript(
                "return document.querySelector('main') ? document.querySelector('main').innerText : '';"));
        body = norm(body);
        return body.contains(a) && body.contains(b);
    }

    // ================== ALERTS ==================
    private boolean looksLikeRealAlertText(String text) {
        String t = norm(text);
        if (t.isEmpty())
            return false;
        if (DIGITS_ONLY.matcher(t).matches())
            return false;
        return t.length() >= 4;
    }

    private String firstVisibleAlertTextInMainOrEmpty() {
        for (WebElement e : driver.findElements(anyAlertLikeInMain)) {
            if (!isDisplayedSafe(e))
                continue;
            String t = norm(e.getText());
            if (!looksLikeRealAlertText(t))
                continue;
            return t;
        }
        return "";
    }

    public String getAnyVisibleAlertTextOrEmpty() {
        String t = firstVisibleAlertTextInMainOrEmpty();
        if (t.isEmpty())
            return "";
        if (containsAnyFolded(t, END_BEFORE_START_KEYS) || containsAnyFolded(t, GENERIC_ALERT_KEYS))
            return t;
        return "";
    }

    public boolean isEndBeforeStartErrorShown() {
        String alert = firstVisibleAlertTextInMainOrEmpty();
        if (!alert.isEmpty() && containsAnyFolded(alert, END_BEFORE_START_KEYS))
            return true;

        String body = normFold(String.valueOf(((JavascriptExecutor) driver).executeScript(
                "return document.querySelector('main') ? document.querySelector('main').innerText : '';")));
        return containsAnyFolded(body, END_BEFORE_START_KEYS);
    }

    // ================== CREATE DISABLED ==================
    public boolean isCreateReportDisabled() {
        WebElement btn = findCreateReportButton();
        String dis = btn.getAttribute("disabled");
        String aria = btn.getAttribute("aria-disabled");
        return dis != null || "true".equalsIgnoreCase(String.valueOf(aria));
    }

    public void waitUntilCreateDisabledOrAnyAlert(long timeoutMs) {
        new WebDriverWait(driver, Duration.ofMillis(timeoutMs)).until(d -> {
            try {
                WebElement btn = findButtonByKeywordsNoWait(CREATE_REPORT_KEYS);
                if (btn == null)
                    return false;
                boolean disabled = btn.getAttribute("disabled") != null ||
                        "true".equalsIgnoreCase(String.valueOf(btn.getAttribute("aria-disabled")));
                boolean alert = !getAnyVisibleAlertTextOrEmpty().isEmpty();
                return disabled || alert;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public void waitUntilEndBeforeStartErrorShown(long timeoutMs) {
        new WebDriverWait(driver, Duration.ofMillis(timeoutMs))
                .until(d -> isEndBeforeStartErrorShown() || !getAnyVisibleAlertTextOrEmpty().isEmpty());
    }

    // ================== DETAIL: table + role-row + title count ==================
    private WebElement findDetailTitleOrNull() {
        try {
            return findDisplayed(detailReportTitle);
        } catch (Exception e) {
            return null;
        }
    }

    public void scrollToDetailSection() {
        WebElement t = findDetailTitleOrNull();
        if (t == null)
            return;
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", t);
        } catch (Exception ignored) {
        }
    }

    public String getDetailTitleTextOrEmpty() {
        WebElement t = findDetailTitleOrNull();
        if (t == null)
            return "";
        try {
            return norm(t.getText());
        } catch (Exception e) {
            return "";
        }
    }

    public int getDetailCountFromTitleOrZero() {
        String txt = getDetailTitleTextOrEmpty();
        if (txt.isEmpty())
            return 0;

        java.util.regex.Matcher m1 = java.util.regex.Pattern
                .compile("\\((\\d+)\\s*(sản phẩm|san pham|items?|products?)\\)",
                        java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(txt);
        if (m1.find())
            return Integer.parseInt(m1.group(1));

        java.util.regex.Matcher m2 = java.util.regex.Pattern
                .compile("(\\d+)\\s*(sản phẩm|san pham|items?|products?)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(txt);
        if (m2.find())
            return Integer.parseInt(m2.group(1));

        return 0;
    }

    private WebElement findDetailSectionRootOrNull() {
        WebElement title = findDetailTitleOrNull();
        if (title == null)
            return null;

        try {
            return title.findElement(By.xpath(
                    "ancestor::*[self::div or self::section]" +
                            "[contains(@class,'bg-card') or contains(@class,'card') or contains(@class,'rounded') " +
                            "or contains(@class,'border') or contains(@class,'shadow')][1]"));
        } catch (Exception ignored) {
        }

        try {
            return title.findElement(By.xpath("ancestor::*[self::div or self::section][1]"));
        } catch (Exception ignored2) {
        }

        return null;
    }

    private WebElement findDetailTableOrNull() {
        try {
            WebElement root = findDetailSectionRootOrNull();
            if (root == null)
                return null;
            List<WebElement> tables = root.findElements(By.tagName("table"));
            for (WebElement t : tables) {
                if (isDisplayedSafe(t))
                    return t;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isNoDataText(String text) {
        return containsAnyFolded(text, NO_DATA_KEYS);
    }

    public boolean isNoDataShown() {
        try {
            WebElement root = findDetailSectionRootOrNull();
            if (root == null)
                return false;
            String t = normFold(root.getText());
            return containsAnyFolded(t, NO_DATA_KEYS);
        } catch (Exception e) {
            return false;
        }
    }

    public int countDetailDataRows() {
        WebElement table = findDetailTableOrNull();
        if (table == null)
            return 0;

        int c = 0;
        try {
            List<WebElement> rows = table.findElements(By.cssSelector("tbody tr"));
            for (WebElement r : rows) {
                if (!isDisplayedSafe(r))
                    continue;
                String t = norm(r.getText());
                if (t.isEmpty())
                    continue;
                if (isNoDataText(t))
                    continue;
                int tdCount = r.findElements(By.cssSelector("td,th")).size();
                if (tdCount < 1)
                    continue;
                c++;
            }
        } catch (Exception ignored) {
        }
        return c;
    }

    public int countDetailRowsFlexible() {
        WebElement root = findDetailSectionRootOrNull();
        if (root == null)
            return 0;

        int best = 0;

        // table rows
        best = Math.max(best, countDetailDataRows());

        // role rows (grid)
        try {
            List<WebElement> rows = root.findElements(By.cssSelector("[role='row']"));
            int c = 0;
            for (WebElement r : rows) {
                if (!isDisplayedSafe(r))
                    continue;
                String t = norm(r.getText());
                if (t.isEmpty())
                    continue;
                if (isNoDataText(t))
                    continue;
                c++;
            }
            best = Math.max(best, c);
        } catch (Exception ignored) {
        }

        return best;
    }

    public List<String> getDetailRowKeys(int max) {
        WebElement table = findDetailTableOrNull();
        if (table == null)
            return new ArrayList<>();

        ArrayList<String> out = new ArrayList<>();
        try {
            for (WebElement r : table.findElements(By.cssSelector("tbody tr"))) {
                if (!isDisplayedSafe(r))
                    continue;
                String t = norm(r.getText());
                if (t.isEmpty())
                    continue;
                if (isNoDataText(t))
                    continue;
                out.add(t);
                if (out.size() >= max)
                    break;
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    // ================== NET SPY ==================
    private void ensureNetworkSpyInstalled() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "if (!window.__pisNetSpyInstalled) {" +
                            " window.__pisNetSpyInstalled = true;" +
                            " window.__pisPendingReq = 0;" +
                            " window.__pisReqLog = [];" +
                            " function inc(){ window.__pisPendingReq = (window.__pisPendingReq||0)+1; }" +
                            " function dec(){ window.__pisPendingReq = Math.max(0,(window.__pisPendingReq||0)-1); }" +
                            " function log(u){ try{ window.__pisReqLog.push({url:String(u||''),t:Date.now()}); if(window.__pisReqLog.length>800) window.__pisReqLog.shift(); }catch(e){} }"
                            +
                            " if (window.fetch && !window.__pisFetchWrapped) {" +
                            "  window.__pisFetchWrapped=true;" +
                            "  const _f=window.fetch;" +
                            "  window.fetch=function(){" +
                            "   const u=(arguments[0]&&arguments[0].url)?arguments[0].url:arguments[0];" +
                            "   log(u); inc();" +
                            "   try{ const p=_f.apply(this,arguments); return p.then(r=>{dec();return r;}).catch(e=>{dec();throw e;}); }"
                            +
                            "   catch(e){ dec(); throw e; }" +
                            "  }" +
                            " }" +
                            " if (window.XMLHttpRequest && !window.__pisXhrWrapped) {" +
                            "  window.__pisXhrWrapped=true;" +
                            "  const X=window.XMLHttpRequest;" +
                            "  const o=X.prototype.open;" +
                            "  const s=X.prototype.send;" +
                            "  X.prototype.open=function(){ this.__pisTracked=true; this.__pisUrl=arguments[1]; return o.apply(this,arguments); };"
                            +
                            "  X.prototype.send=function(){ if(this.__pisTracked){ log(this.__pisUrl); inc(); } this.addEventListener('loadend',()=>{ if(this.__pisTracked) dec(); }); return s.apply(this,arguments); };"
                            +
                            " }" +
                            "}");
        } catch (Exception ignored) {
        }
    }

    private long jsLong(String script) {
        Object v = ((JavascriptExecutor) driver).executeScript(script);
        if (v == null)
            return 0;
        if (v instanceof Number)
            return ((Number) v).longValue();
        return Long.parseLong(String.valueOf(v));
    }

    public long getReqLogCount() {
        return jsLong("return (window.__pisReqLog||[]).length;");
    }

    public long getPendingReq() {
        return jsLong("return window.__pisPendingReq || 0;");
    }

    @SuppressWarnings("unchecked")
    public List<String> getReqLast(int lastN) {
        Object v = ((JavascriptExecutor) driver).executeScript(
                "var a=(window.__pisReqLog||[]);" +
                        "var n=Math.min(arguments[0], a.length);" +
                        "return a.slice(a.length-n).map(x=>String(x.url||''));",
                lastN);
        if (v instanceof List)
            return (List<String>) v;
        return Collections.singletonList(String.valueOf(v));
    }

    public boolean recentReqUrlsContain(String keyword, int lastN) {
        String kw = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);
        for (String u : getReqLast(lastN)) {
            if (u == null)
                continue;
            if (u.toLowerCase(Locale.ROOT).contains(kw))
                return true;
        }
        return false;
    }

    // ================== CLICK SPY: CREATE ==================
    private void ensureCreateClickSpyInstalled() {
        WebElement btn = findCreateReportButton();
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.__pisCreateClicks = window.__pisCreateClicks || 0;" +
                            "if (!arguments[0].dataset.pisClickSpy) {" +
                            " arguments[0].dataset.pisClickSpy='1';" +
                            " arguments[0].addEventListener('click', function(){ window.__pisCreateClicks++; }, true);"
                            +
                            "}",
                    btn);
        } catch (Exception ignored) {
        }
    }

    public long getCreateClickCountPublic() {
        return jsLong("return window.__pisCreateClicks || 0;");
    }

    public void waitCreateClickCountGreaterThan(long before, long maxMs) {
        long end = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < end) {
            if (getCreateClickCountPublic() > before)
                return;
            sleep(60);
        }
        throw new AssertionError("Create click count không tăng (Space/Enter không trigger click).");
    }

    // ================== EXPORT SPY ==================
    private void ensureExportSpyInstalled() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "if (!window.__pisExportSpyInstalled) {" +
                            " window.__pisExportSpyInstalled=true;" +
                            " window.__pisExportClicks = window.__pisExportClicks || 0;" +
                            " window.__pisPrints = window.__pisPrints || 0;" +
                            " window.__pisOpens  = window.__pisOpens  || 0;" +
                            " const _p=window.print;" +
                            " window.print=function(){ window.__pisPrints++; return _p?_p.apply(this,arguments):undefined; };"
                            +
                            " const _o=window.open;" +
                            " window.open=function(){ window.__pisOpens++; return _o?_o.apply(this,arguments):null; };"
                            +
                            "}");
        } catch (Exception ignored) {
        }
    }

    private void ensureExportClickSpyInstalled() {
        WebElement btn = findExportPdfButton();
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.__pisExportClicks = window.__pisExportClicks || 0;" +
                            "if (!arguments[0].dataset.pisExportClickSpy) {" +
                            " arguments[0].dataset.pisExportClickSpy='1';" +
                            " arguments[0].addEventListener('click', function(){ window.__pisExportClicks++; }, true);"
                            +
                            "}",
                    btn);
        } catch (Exception ignored) {
        }
    }

    public long getPrintCount() {
        return jsLong("return window.__pisPrints || 0;");
    }

    public long getOpenCount() {
        return jsLong("return window.__pisOpens || 0;");
    }

    public long exportActivitySignature() {
        return getReqLogCount() + getPrintCount() + getOpenCount() + jsLong("return window.__pisExportClicks||0;");
    }

    // ================== ACTIONS: CREATE / EXPORT ==================
    private void waitRequestFired(long reqBefore, long pendingBefore, long maxMs) {
        long end = System.currentTimeMillis() + maxMs;
        while (System.currentTimeMillis() < end) {
            if (getReqLogCount() > reqBefore)
                return;
            if (getPendingReq() > pendingBefore)
                return;
            sleep(50);
        }
    }

    private void waitPendingZeroStable(long stableMs, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        long stableStart = -1;

        while (System.currentTimeMillis() < end) {
            long p = getPendingReq();
            if (p == 0) {
                if (stableStart < 0)
                    stableStart = System.currentTimeMillis();
                if (System.currentTimeMillis() - stableStart >= stableMs)
                    return;
            } else {
                stableStart = -1;
            }
            sleep(70);
        }
    }

    public void clickCreateReportAndWaitAny() {
        ensureNetworkSpyInstalled();
        ensureCreateClickSpyInstalled();

        if (isCreateReportDisabled())
            throw new AssertionError("Create report button is disabled.");

        WebElement btn = findCreateReportButton();
        long clicksBefore = getCreateClickCountPublic();
        long reqBefore = getReqLogCount();
        long pendBefore = getPendingReq();

        try {
            wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        waitCreateClickCountGreaterThan(clicksBefore, 4000);
        waitRequestFired(reqBefore, pendBefore, 8000);
        waitPendingZeroStable(400, 25_000);
        waitUiSettled();

        // kích hoạt lazy render
        scrollToDetailSection();
        waitUiSettled();
    }

    public void clickExportPdfAndWaitTriggered() {
        ensureNetworkSpyInstalled();
        ensureExportSpyInstalled();
        ensureExportClickSpyInstalled();

        long before = exportActivitySignature();
        long reqBefore = getReqLogCount();
        long pendBefore = getPendingReq();

        WebElement btn = findExportPdfButton();
        try {
            wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        waitRequestFired(reqBefore, pendBefore, 6000);

        long end = System.currentTimeMillis() + 12000;
        while (System.currentTimeMillis() < end) {
            if (exportActivitySignature() > before)
                break;
            sleep(90);
        }

        waitPendingZeroStable(400, 25_000);
        waitUiSettled();
    }

    // ================== SNAPSHOT + DISCOVERY ==================
    public static class ReportSnapshot {
        public final String start;
        public final String end;
        public final long seenAtMs;
        public final long maxRowsSeen;
        public final List<String> rowKeysSample;
        public final String periodText;
        public final long pending;
        public final List<String> reqLast;
        public final String alert;

        public ReportSnapshot(String start, String end, long seenAtMs, long maxRowsSeen,
                List<String> rowKeysSample, String periodText, long pending,
                List<String> reqLast, String alert) {
            this.start = start;
            this.end = end;
            this.seenAtMs = seenAtMs;
            this.maxRowsSeen = maxRowsSeen;
            this.rowKeysSample = rowKeysSample;
            this.periodText = periodText;
            this.pending = pending;
            this.reqLast = reqLast;
            this.alert = alert;
        }
    }

    private ReportSnapshot sampleEverRows(String start, String end, int minRows, long timeoutMs) {
        long endAt = System.currentTimeMillis() + timeoutMs;

        int maxEff = 0;
        List<String> keys = new ArrayList<>();

        while (System.currentTimeMillis() < endAt) {
            scrollToDetailSection();

            int titleN = getDetailCountFromTitleOrZero();
            int tableRows = countDetailDataRows();
            int flexRows = countDetailRowsFlexible();
            int eff = Math.max(titleN, Math.max(tableRows, flexRows));

            if (eff > maxEff) {
                maxEff = eff;
                if (keys.isEmpty() && tableRows > 0)
                    keys = getDetailRowKeys(12);
            }

            if (maxEff >= minRows)
                break;

            if (maxEff == 0 && isNoDataShown()) {
                sleep(250);
                int again = Math.max(getDetailCountFromTitleOrZero(),
                        Math.max(countDetailDataRows(), countDetailRowsFlexible()));
                if (again == 0 && isNoDataShown())
                    break;
            }

            if (!getAnyVisibleAlertTextOrEmpty().isEmpty())
                break;

            sleep(120);
        }

        return new ReportSnapshot(
                toIsoDate(start), toIsoDate(end),
                System.currentTimeMillis(),
                maxEff,
                keys,
                getPeriodTextOrEmpty(),
                getPendingReq(),
                getReqLast(6),
                getAnyVisibleAlertTextOrEmpty());
    }

    public ReportSnapshot generateReportEverRowsByClick(String start, String end, int minRows) {
        setRangeAndSettle(start, end);
        clickCreateReportAndWaitAny();
        return sampleEverRows(start, end, minRows, reportWaitMs);
    }

    public static class DataRangePick {
        public final String start;
        public final String end;
        public final long rows;
        public final String debug;

        public DataRangePick(String start, String end, long rows, String debug) {
            this.start = start;
            this.end = end;
            this.rows = rows;
            this.debug = debug;
        }

        @Override
        public String toString() {
            return "DataRangePick{start=" + start + ", end=" + end + ", rows=" + rows + ", debug=" + debug + "}";
        }
    }

    public DataRangePick discoverFirstRangeWithUiProbe(List<String[]> ranges, int minRows, long perRangeTimeoutMs) {
        if (ranges == null || ranges.isEmpty())
            return null;

        ensureNetworkSpyInstalled();
        ensureCreateClickSpyInstalled();

        int idx = 0;
        for (String[] r : ranges) {
            idx++;
            if (r == null || r.length < 2)
                continue;

            String s = String.valueOf(r[0]);
            String e = String.valueOf(r[1]);

            try {
                setRangeAndSettle(s, e);

                // nếu input không set được -> bỏ luôn, để debug dễ
                String sv = getValue(startDateInput);
                String ev = getValue(endDateInput);
                if (sv == null || ev == null || sv.isEmpty() || ev.isEmpty()) {
                    continue;
                }

                if (isCreateReportDisabled())
                    continue;

                long reqBefore = getReqLogCount();
                clickCreateReportAndWaitAny();

                ReportSnapshot snap = sampleEverRows(s, e, minRows, perRangeTimeoutMs);
                if (snap.maxRowsSeen >= minRows) {
                    String dbg = "idx=" + idx +
                            ", maxRows=" + snap.maxRowsSeen +
                            ", reqDelta=" + (getReqLogCount() - reqBefore) +
                            ", period=" + snap.periodText +
                            ", alert=" + snap.alert;
                    return new DataRangePick(snap.start, snap.end, snap.maxRowsSeen, dbg);
                }
            } catch (Exception ignored) {
                // thử range khác
            }
        }
        return null;
    }

    // ================== CHART DETECT ==================
    private WebElement findSectionTitleByKeywords(String... keywords) {
        if (keywords == null || keywords.length == 0)
            return null;
        List<WebElement> els = driver
                .findElements(By.xpath("//*[self::h2 or self::h3 or self::p or self::span or self::div]"));
        for (WebElement e : els) {
            if (!isDisplayedSafe(e))
                continue;
            String t = textForSearch(e);
            if (containsAnyFolded(t, keywords))
                return e;
        }
        return null;
    }

    private WebElement findCardAncestor(WebElement title) {
        try {
            return title.findElement(By.xpath(
                    "ancestor::*[self::div or self::section]" +
                            "[contains(@class,'bg-card') or contains(@class,'card') or contains(@class,'rounded') " +
                            "or contains(@class,'border') or contains(@class,'shadow')][1]"));
        } catch (Exception ignored) {
            try {
                return title.findElement(By.xpath("ancestor::*[self::div or self::section][1]"));
            } catch (Exception ignored2) {
                return null;
            }
        }
    }

    public boolean sectionHasChartOrNoData(String titleContains) {
        try {
            WebElement title = findSectionTitleByKeywords(titleContains);
            if (title == null)
                return false;

            WebElement scope = findCardAncestor(title);
            if (scope == null)
                scope = title;

            int charts = scope.findElements(By.xpath(
                    ".//*[local-name()='svg' or self::canvas " +
                            "or contains(@class,'recharts') or contains(@class,'apexcharts') or contains(@class,'chart')]"))
                    .size();
            if (charts > 0)
                return true;

            for (WebElement e : scope.findElements(By.xpath(
                    ".//*[contains(normalize-space(.),'Không có dữ liệu') or contains(normalize-space(.),'No data') or contains(normalize-space(.),'Chưa có')]"))) {
                if (isDisplayedSafe(e))
                    return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // ================== STAT PARSE ==================
    private long parseLongNumber(String raw) {
        if (raw == null)
            return 0;
        String cleaned = raw.replaceAll("[^0-9\\-]", "");
        if (cleaned.isEmpty() || "-".equals(cleaned))
            return 0;
        try {
            return Long.parseLong(cleaned);
        } catch (Exception e) {
            return 0;
        }
    }

    private WebElement findVisibleTextNodeInMain(String containsText) {
        List<WebElement> els = driver.findElements(By.xpath(
                "//main//*[normalize-space(.)!='' and contains(normalize-space(.),'" + containsText + "')]"));
        for (WebElement e : els) {
            if (!isDisplayedSafe(e))
                continue;
            String t = norm(e.getText());
            if (t.length() > 150)
                continue;
            return e;
        }
        return null;
    }

    private WebElement nearestStatContainer(WebElement labelEl) {
        String[] xps = new String[] {
                "ancestor::*[self::div or self::section][contains(@class,'bg-card') or contains(@class,'card') or contains(@class,'rounded') or contains(@class,'border') or contains(@class,'shadow')][1]",
                "ancestor::*[self::div or self::section][1]"
        };
        for (String xp : xps) {
            try {
                return labelEl.findElement(By.xpath(xp));
            } catch (Exception ignored) {
            }
        }
        return labelEl;
    }

    private Long extractNumberNear(WebElement scope, WebElement labelEl) {
        List<WebElement> near;
        try {
            near = scope.findElements(By.xpath(".//*[self::div or self::p or self::span]"));
        } catch (Exception e) {
            near = new ArrayList<>();
        }

        String labelTxt = norm(labelEl.getText());
        String best = null;
        int bestDigits = -1;

        for (WebElement c : near) {
            if (!isDisplayedSafe(c))
                continue;
            String txt = norm(c.getText());
            if (txt.isEmpty())
                continue;
            if (txt.equals(labelTxt))
                continue;
            int digits = txt.replaceAll("[^0-9]", "").length();
            if (digits > bestDigits) {
                bestDigits = digits;
                best = txt;
            }
        }
        if (best != null && bestDigits > 0)
            return parseLongNumber(best);
        return null;
    }

    public long getStatNumberByLabelCandidates(String... labels) {
        for (String lb : labels) {
            try {
                WebElement labelEl = findVisibleTextNodeInMain(lb);
                if (labelEl == null)
                    continue;
                WebElement scope = nearestStatContainer(labelEl);
                Long v = extractNumberNear(scope, labelEl);
                if (v != null)
                    return v;
            } catch (Exception ignored) {
            }
        }
        throw new AssertionError("Không tìm được stat card theo label: " + String.join(", ", labels));
    }
}
