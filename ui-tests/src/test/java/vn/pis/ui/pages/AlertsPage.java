package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class AlertsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl = vn.pis.ui.util.TestEnv.BASE_URL;

    // =======================
    // LOCATORS
    // =======================

    // Menu
    private final By menuAlertsLink = By.xpath("//a[contains(@href,'/alerts')]");

    // Header
    public final By pageTitle = By.xpath("//h1[contains(text(), 'Cảnh báo') or contains(@class, 'title')]");
    public final By pageDesc  = By.xpath("//p[contains(text(), 'Theo dõi') or contains(@class, 'subtitle')]");
    private final By listTitle = By.xpath(
            "//*[self::h2 or self::h3 or self::p or self::div]" +
                    "[normalize-space()='Danh sách cảnh báo']"
    );



    // Cards title
    private final By cardExpiringTitle = By.xpath("//*[contains(normalize-space(.), 'Sắp hết hạn')]");
    private final By cardExpiringCount = By.xpath(
            "//p[normalize-space()='Sắp hết hạn sử dụng']" +
                    "/ancestor::div[contains(@class,'border-card')]" +
                    "//p[contains(@class,'text-4xl')]"
    );

    private final By cardLowTitle =
            By.xpath("//div[contains(@class,'rounded')]//p[normalize-space()='Thuốc sắp hết tồn kho']");

    private final By cardOutTitle =
            By.xpath("//p[normalize-space()='Cảnh báo hết tồn kho']");


    // Search
    private final By searchInput = By.xpath("//input[@placeholder='Tìm kiếm thuốc...']");

    // Table
    private final By tableHeader = By.xpath("//table//thead//th");
    private final By tableRows = By.cssSelector("table tbody tr");


    // Empty state (UI bạn có 2 kiểu: giữa màn hình hoặc trong table)
    private final By emptyTextAny = By.xpath("//*[contains(normalize-space(.),'Không có cảnh báo nào')]");
    private final By emptyCellAny = By.xpath(
            "//table//tbody//td[" +
                    "contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'không có')" +
                    " or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'không tìm thấy')" +
                    " or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no data')" +
                    " or contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no results')" +
                    "]"
    );

    // Row action
    private final By btnDetailsInRow = By.xpath(".//button[contains(.,'Xem chi tiết') or contains(.,'Chi tiết') or contains(@class,'btn-detail')]");

    // Pagination
    private final By btnPrev = By.xpath("//button[normalize-space()='Trước']");
    private final By btnNext = By.xpath("//button[normalize-space()='Sau']");
    private final By pageInfoText = By.xpath("//button[normalize-space()='Trước']/following-sibling::span[1]");

    private final By paginationInfo = By.xpath("//*[contains(.,'Trang') or contains(.,'Hiển thị')]");

    // Page size (nếu có)
    private final By pageSizeSelect = By.xpath("//span[normalize-space()='Hiển thị:']/following-sibling::select[1]");

    private final By pageSizeButton = By.xpath("//button[contains(.,'Hiển thị') or contains(.,'dòng') or contains(.,'Hiển thị:')]");

    // Modal
    private final By modalRoot = By.xpath("//div[@role='dialog' or contains(@class,'modal')]");
    private final By modalTitle = By.xpath("//div[@role='dialog' or contains(@class,'modal')]//*[contains(.,'Chi tiết cảnh báo') or contains(.,'Chi tiết')]");
    private final By btnCloseBottom = By.xpath("//button[contains(., 'Đóng')]");
    private final By btnCloseIcon = By.xpath("//button[contains(@class,'absolute') and (contains(@class,'right') or contains(@class,'top'))]");
    // Radix Dialog overlay/backdrop
    private final By modalBackdrop = By.cssSelector(
            "div[data-state='open'].fixed.inset-0.z-50.bg-black\\/80"
    );


    private final By inputNote = By.tagName("textarea");
    private final By btnConfirm = By.xpath("//button[contains(.,'Xác nhận') or contains(.,'đã biết') or contains(.,'Đã biết')]");

    // Overlay (shadcn/radix hay dùng data-state=open)
    private final By overlayOpen = By.cssSelector("div[data-state='open'].fixed.inset-0");

    private final By skuValueBy = By.xpath(
            "//*[self::div or self::p or self::span][normalize-space()='SKU' or normalize-space()='SKU:']" +
                    "/following::*[self::div or self::p or self::span][1]"
    );

    private final By modalSkuBlock = By.xpath(
            "//div[@role='dialog' or contains(@class,'modal')]//p[contains(normalize-space(.),'SKU')]"
    );

    // Radix Dialog backdrop
    private final By dialogBackdrop = By.cssSelector(
            "div[data-state='open'].fixed.inset-0.bg-black\\/80"
    );



    // =======================
    // Column map cache
    // =======================
    private Map<String, Integer> colMapCache = null;

    public AlertsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // =======================
    // LOG
    // =======================
    private void STEP(String msg) { System.out.println("   [STEP] " + msg); }
    private void ACTUAL(String msg) { System.out.println("   [ACTUAL] " + msg); }
    private void EXPECT(String msg) { System.out.println("   [EXPECT] " + msg); }

    private void sleep(long ms) {
        try { Thread.sleep(Math.min(ms, 800)); }
        catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }




    private String deriveBaseUrl() {
        try {
            String cur = driver.getCurrentUrl();
            if (cur != null && cur.startsWith("http")) {
                URL u = new URL(cur);
                String port = (u.getPort() == -1) ? "" : (":" + u.getPort());
                return u.getProtocol() + "://" + u.getHost() + port;
            }
        } catch (Exception ignored) {}
        return baseUrl;
    }

    // =======================
    // NAVIGATION
    // =======================
    public void open() {
        String cur = driver.getCurrentUrl();
        STEP("URL hiện tại: " + cur);

        if (cur != null && cur.contains("/alerts")) {
            waitPageReady();
            return;
        }

        STEP("Mở /alerts qua menu (fallback: direct /alerts)");
        try {
            WebElement m = wait.until(ExpectedConditions.elementToBeClickable(menuAlertsLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", m);
            m.click();
        } catch (Exception e) {
            STEP("Click menu fail -> open direct /alerts. Reason: " + e.getClass().getSimpleName());
            driver.get(deriveBaseUrl() + "/alerts");
        }

        waitPageReady();
    }

    public void waitPageReady() {
        String cur = driver.getCurrentUrl();
        if (cur != null && cur.contains("/login")) {
            throw new AssertionError("Bị redirect về /login (session hết hạn / login fail)");
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));

        // chờ có table header/rows/empty/search
        wait.until(d ->
                !d.findElements(tableHeader).isEmpty()
                        || !d.findElements(tableRows).isEmpty()
                        || !d.findElements(emptyCellAny).isEmpty()
                        || !d.findElements(emptyTextAny).isEmpty()
                        || !d.findElements(searchInput).isEmpty()
        );

        colMapCache = null;
    }

    // reset nhẹ, gọi trước mỗi test
    public void resetLight() {
        STEP("Reset: đóng overlay + đóng modal + clear search + về trang 1");
        closeAnyOverlayIfPresent();
        closeModalIfOpen();
        clearSearch();
        goToFirstPage();
        waitPageReady();
    }

    // =======================
    // HEADER
    // =======================
    public String getPageTitle() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).getText().trim();
    }

    public String getPageDescription() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageDesc)).getText().trim();
    }

    public boolean isListTitleVisible() {
        return !driver.findElements(listTitle).isEmpty();
    }

    // =======================
    // OVERLAY / MODAL SAFETY
    // =======================
    public void closeAnyOverlayIfPresent() {
        try {
            // 1) nếu đang có modal -> đóng modal trước
            if (isModalOpen()) closeModalIfOpen();

            // 2) rồi mới xử lý overlay khác
            if (!driver.findElements(overlayOpen).isEmpty()) {
                STEP("Có overlay -> ESC");
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);

                new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(d -> d.findElements(overlayOpen).isEmpty());
            }
        } catch (Exception ignored) {}
    }


    // =======================
    // CARDS
    // =======================
    private By cardTitleByType(String type) {
        return switch (type) {
            case "expiring" -> cardExpiringTitle;
            case "low" -> cardLowTitle;
            case "out" -> cardOutTitle;
            default -> null;
        };
    }

    public boolean isCardVisible(String type) {
        String title;
        if ("expiring".equals(type)) {
            title = "Sắp hết hạn sử dụng";
        } else if ("low".equals(type)) {
            title = "Thuốc sắp hết tồn kho";
        } else if ("out".equals(type)) {
            title = "Cảnh báo hết tồn kho";
        } else {
            return false;
        }

        try {
            // 🔒 chỉ tìm đúng thẻ có tiêu đề
            By cardTitle = By.xpath(
                    "//div[.//p[normalize-space()='" + title + "']]"
            );

            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(cardTitle)
            ) != null;

        } catch (Exception e) {
            return false;
        }
    }

    public String getListTitleText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(listTitle))
                .getText().trim();
    }



    public String getCardTitleText(String type) {
        By by = cardTitleByType(type);
        if (by == null) return "";
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }


    public int getCardCount(String type) {
        String title;
        if ("expiring".equals(type)) {
            title = "Sắp hết hạn";
        } else if ("low".equals(type)) {
            title = "Thuốc sắp hết tồn kho";
        } else if ("out".equals(type)) {
            title = "Cảnh báo hết tồn kho";
        } else {
            return -1;
        }

        try {
            // 🔒 CHỈ LẤY DIV CHA GẦN NHẤT CỦA TITLE
            By cardBox = By.xpath(
                    "//p[contains(normalize-space(.),'" + title + "')]" +
                            "/parent::div"
            );

            WebElement card = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(cardBox)
            );

            String allText = card.getText();
            ACTUAL("Card raw text = " + allText.replace("\n", " | "));

            // lấy số đầu tiên trong card (luôn đúng với UI này)
            Matcher m = Pattern.compile("(\\d+)").matcher(allText);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }

            return -1;

        } catch (Exception e) {
            ACTUAL("Không đọc được card count: " + e.getMessage());
            return -1;
        }
    }





    public int getRowCountInTable() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
        int n = driver.findElements(tableRows).size();
        System.out.println("   [Read] Table row count = " + n);
        return n;
    }


    // =======================
    // SEARCH
    // =======================
    public boolean isSearchVisible() {
        List<WebElement> els = driver.findElements(searchInput);
        if (els.isEmpty()) return false;
        WebElement el = els.get(0);
        String ph = el.getAttribute("placeholder");
        return el.isDisplayed() && ph != null && ph.contains("Tìm kiếm thuốc");
    }

    public void clearSearch() {
        try {
            if (driver.findElements(searchInput).isEmpty()) return;
            search(""); // dùng chung cơ chế
        } catch (Exception ignored) {}
    }

    public String getSearchPlaceholder() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        String ph = el.getAttribute("placeholder");
        return ph == null ? "" : ph.trim();
    }

    public java.util.List<String> getHeaderTexts() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableHeader));
        java.util.List<WebElement> ths = driver.findElements(tableHeader);

        java.util.List<String> out = new java.util.ArrayList<>();
        for (WebElement th : ths) {
            String t = th.getText();
            if (t != null) out.add(t.trim());
        }
        return out;
    }


    // Search kiểu chắc ăn: set value bằng JS + dispatch event (tránh click bị overlay)
    public void search(String keyword) {
        closeAnyOverlayIfPresent();

        STEP("Search keyword = '" + keyword + "'");
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));

        // clear + gõ như user thật
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.BACK_SPACE);
        if (keyword != null) input.sendKeys(keyword);

        // nhiều UI cần Enter để apply
        input.sendKeys(Keys.ENTER);

        ACTUAL("Input value = '" + input.getAttribute("value") + "'");
    }


    public String getRowLotLineRaw(int rowIndex1Based) {
        try {
            WebElement row = driver.findElement(By.xpath("(//table//tbody/tr)[" + rowIndex1Based + "]"));
            WebElement col0 = row.findElements(By.cssSelector("td")).get(0);
            // dòng nhỏ màu xám thường là text-xs
            return col0.findElement(By.cssSelector("p.text-xs")).getText().trim(); // "Lô: SL05"
        } catch (Exception e) {
            return "";
        }
    }


    private void waitTableUpdate(String beforeSig) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
                // empty thì coi như update xong
                if (isEmptyVisible()) return true;

                // có row data thật
                int rc = getRowCount();
                if (rc <= 0) return false;

                String afterSig = tableSignature();
                return !Objects.equals(afterSig, beforeSig);
            });
        } catch (TimeoutException e) {
            STEP("Timeout chờ table update (UI debounce / data không đổi).");
        }
    }

    // =======================
    // EMPTY
    // =======================
    public boolean isEmptyVisible() {
        try {
            List<WebElement> t1 = driver.findElements(emptyTextAny);
            if (!t1.isEmpty() && t1.get(0).isDisplayed()) return true;

            List<WebElement> t2 = driver.findElements(emptyCellAny);
            return !t2.isEmpty() && t2.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // =======================
    // TABLE
    // =======================
    public List<String> getTableHeaders() {
        List<WebElement> ths = driver.findElements(tableHeader);
        List<String> headers = new ArrayList<>();
        for (WebElement th : ths) headers.add(th.getText().trim());
        return headers;
    }

    public boolean hasHeader(String expected) {
        String ex = expected.trim().toLowerCase();
        for (String h : getTableHeaders()) {
            String t = (h == null ? "" : h.trim().toLowerCase());
            if (t.equals(ex) || t.contains(ex)) return true;
        }
        return false;
    }

    private List<WebElement> getVisibleDataRows() {
        List<WebElement> rows = driver.findElements(tableRows);
        List<WebElement> data = new ArrayList<>();

        for (WebElement r : rows) {
            try {
                if (!r.isDisplayed()) continue;

                String text = (r.getText() == null ? "" : r.getText()).toLowerCase();

                // loại row empty
                if (text.contains("không có cảnh báo") || text.contains("không tìm thấy")
                        || text.contains("no data") || text.contains("no results")) {
                    continue;
                }

                // data row thật thường có nút xem chi tiết
                if (!r.findElements(btnDetailsInRow).isEmpty()) {
                    data.add(r);
                    continue;
                }

                // fallback: có nhiều td thì coi là data
                List<WebElement> tds = r.findElements(By.cssSelector("td"));
                if (tds.size() >= 5) data.add(r);

            } catch (StaleElementReferenceException ignored) {}
        }
        return data;
    }

    public int getRowCount() {
        if (isEmptyVisible()) return 0;
        return getVisibleDataRows().size();
    }

    private String tableSignature() {
        if (isEmptyVisible()) return "EMPTY";
        List<WebElement> rows = getVisibleDataRows();
        if (rows.isEmpty()) return "0";
        return rows.size() + "|" + rows.get(0).getText().trim();
    }

    private Map<String, Integer> resolveColumnMap() {
        if (colMapCache != null) return colMapCache;

        Map<String, Integer> map = new HashMap<>();
        List<WebElement> ths = driver.findElements(tableHeader);
        for (int i = 0; i < ths.size(); i++) {
            String t = ths.get(i).getText().trim().toLowerCase();
            if (t.contains("tên thuốc")) map.put("name", i);
            else if (t.contains("tồn kho")) map.put("stock", i);
            else if (t.contains("mức tồn")) map.put("min", i);
            else if (t.contains("trạng thái")) map.put("status", i);
            else if (t.contains("ngày cảnh báo")) map.put("date", i);
            else if (t.contains("hành động")) map.put("action", i);
        }

        map.putIfAbsent("name", 0);
        map.putIfAbsent("status", 3);

        colMapCache = map;
        return map;
    }

    public Map<String, String> getRowData(int rowIndex1Based) {
        Map<String, String> data = new HashMap<>();
        try {
            WebElement row = driver.findElement(By.xpath("(//table//tbody/tr)[" + rowIndex1Based + "]"));
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() <= 1) return data;

            Map<String, Integer> cm = resolveColumnMap();
            int cName = cm.getOrDefault("name", 0);
            int cStatus = cm.getOrDefault("status", 3);

            if (cName < cells.size()) data.put("name", cells.get(cName).getText());
            if (cStatus < cells.size()) data.put("status", cells.get(cStatus).getText());
        } catch (Exception ignored) {}
        return data;
    }

    public String getRowNameOnly(int rowIndex1Based) {
        String raw = getRowData(rowIndex1Based).getOrDefault("name", "");
        if (raw == null) return "";
        String[] lines = raw.split("\n");
        return lines.length > 0 ? lines[0].trim() : raw.trim();
    }

    public String getRowLotLine(int rowIndex1Based) {
        String raw = getRowData(rowIndex1Based).getOrDefault("name", "");
        if (raw == null) return "";
        String[] lines = raw.split("\n");
        return (lines.length >= 2) ? lines[1].trim() : "";
    }

    public String getRowStatus(int rowIndex1Based) {
        return getRowData(rowIndex1Based).getOrDefault("status", "");
    }

    // badge check: lấy class span trong cột status
    public String getStatusBadgeClass(int rowIndex1Based) {
        try {
            Map<String, Integer> cm = resolveColumnMap();
            int cStatus = cm.getOrDefault("status", 3);

            WebElement row = driver.findElement(By.xpath("(//table//tbody/tr)[" + rowIndex1Based + "]"));
            List<WebElement> tds = row.findElements(By.tagName("td"));
            if (tds.size() <= cStatus) return "";

            WebElement cell = tds.get(cStatus);
            WebElement badge = cell.findElement(By.xpath(".//*[self::span or contains(@class,'badge') or contains(@class,'rounded-full')]"));
            return Optional.ofNullable(badge.getAttribute("class")).orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    public WebElement getStatusBadgeEl(int rowIndex1Based) {
        Map<String, Integer> cm = resolveColumnMap();
        int cStatus = cm.getOrDefault("status", 3);

        WebElement row = driver.findElement(By.xpath("(//table//tbody/tr)[" + rowIndex1Based + "]"));
        List<WebElement> tds = row.findElements(By.tagName("td"));
        if (tds.size() <= cStatus) throw new NoSuchElementException("No status column");

        WebElement cell = tds.get(cStatus);

        // ưu tiên đúng cái badge UI đang dùng: div.inline-flex / span.badge / pill
        List<By> candidates = List.of(
                By.cssSelector("div.inline-flex"),
                By.cssSelector("span.badge"),
                By.cssSelector("span.rounded-full"),
                By.xpath(".//*[self::span or self::div][contains(@class,'inline-flex') or contains(@class,'badge') or contains(@class,'rounded')]")
        );

        for (By by : candidates) {
            List<WebElement> els = cell.findElements(by);
            if (!els.isEmpty()) return els.get(0);
        }

        // fallback: lấy luôn cell
        return cell;
    }

    public String getStatusBadgeText(int rowIndex1Based) {
        try {
            return getStatusBadgeEl(rowIndex1Based).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public String getStatusBadgeBgColor(int rowIndex1Based) {
        try {
            return getStatusBadgeEl(rowIndex1Based).getCssValue("background-color"); // ví dụ: rgba(...)
        } catch (Exception e) {
            return "";
        }
    }

    public void waitUntilTableFilteredByKeyword(String keyword) {
        String kw = (keyword == null ? "" : keyword.trim().toLowerCase());

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            try {
                if (isEmptyVisible()) return true;

                List<AlertRow> rows = readTableDataSafe();
                if (rows.isEmpty()) return true;

                if (kw.isEmpty()) return true;

                for (AlertRow r : rows) {
                    String name = (r.tenThuoc == null ? "" : r.tenThuoc.toLowerCase());
                    if (!name.contains(kw)) return false;
                }
                return true;

            } catch (StaleElementReferenceException e) {
                // ✅ nếu stale trong lúc wait, trả false để vòng sau đọc lại
                return false;
            }
        });
    }




    public boolean hasWarningBadge(int rowIndex1Based) {
        String cls = getStatusBadgeClass(rowIndex1Based).toLowerCase();
        return cls.contains("yellow") || cls.contains("amber") || cls.contains("warning");
    }

    public boolean hasExpiredBadge(int rowIndex1Based) {
        String cls = getStatusBadgeClass(rowIndex1Based).toLowerCase();
        return cls.contains("red") || cls.contains("danger") || cls.contains("error");
    }

    public int findFirstRowByStatusContains(String keyword) {
        int rows = getRowCount();
        for (int i = 1; i <= rows; i++) {
            String st = getRowStatus(i);
            if (st != null && st.contains(keyword)) return i;
        }
        return -1;
    }

    // =======================
    // PAGINATION
    // =======================
    public boolean isNextEnabled() {
        try {
            WebElement btn = driver.findElement(btnNext);
            return btn.isDisplayed() && btn.getAttribute("disabled") == null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPrevDisabled() {
        try {
            WebElement btn = driver.findElement(btnPrev);
            return btn.getAttribute("disabled") != null;
        } catch (Exception e) {
            return true;
        }
    }

    public void clickNextPageAndWait() {
        Map<String,Integer> before = getCurrentPageInfo();
        int cur = before.getOrDefault("current", -1);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnNext));
        btn.click();

        // chờ current page thay đổi
        wait.until(d -> {
            Map<String,Integer> after = getCurrentPageInfo();
            int curAfter = after.getOrDefault("current", -1);
            return curAfter != -1 && curAfter != cur;
        });
    }

    public void clickPrevPageAndWait() {
        Map<String,Integer> before = getCurrentPageInfo();
        int cur = before.getOrDefault("current", -1);

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnPrev));
        btn.click();

        wait.until(d -> {
            Map<String,Integer> after = getCurrentPageInfo();
            int curAfter = after.getOrDefault("current", -1);
            return curAfter != -1 && curAfter != cur;
        });
    }

    public void clickNextPage() {
        STEP("Click Next (Sau)");
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            sleep(250);
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnNext));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            btn.click();
        } catch (Exception e) {
            ACTUAL("Next click fail: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    public void clickPrevPage() {
        STEP("Click Prev (Trước)");
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            sleep(250);
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnPrev));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            btn.click();
        } catch (Exception e) {
            ACTUAL("Prev click fail: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    public String getPaginationText() {
        try {
            List<WebElement> els = driver.findElements(paginationInfo);
            if (!els.isEmpty()) return els.get(0).getText().trim();
        } catch (Exception ignored) {}
        return "";
    }

    public Map<String, Integer> getCurrentPageInfo() {
        Map<String, Integer> info = new HashMap<>();
        try {
            WebElement sp = wait.until(ExpectedConditions.visibilityOfElementLocated(pageInfoText));
            String raw = sp.getText();                    // có thể chứa \n
            String text = raw.replace("\n", " ").trim();  // normalize

            // bắt 2 số đầu tiên theo pattern "Trang 1 / 3"
            Matcher m = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)").matcher(text);
            if (m.find()) {
                info.put("current", Integer.parseInt(m.group(1)));
                info.put("total", Integer.parseInt(m.group(2)));
            }
        } catch (Exception ignored) {}
        return info;
    }



    public void goToFirstPage() {
        STEP("Về trang 1 (bấm Prev tới khi disable)");
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        sleep(200);

        int guard = 0;
        while (!isPrevDisabled() && guard < 10) {
            clickPrevPage();
            guard++;
            sleep(250);
        }

        ACTUAL("PrevDisabled=" + isPrevDisabled());
    }

    // locator bạn có rồi: pageSizeSelect


    public void setPageSize(int size) {
        STEP("Set page size = " + size);

        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(pageSizeSelect));
        Select s = new Select(sel);

        String before = tableSignature();     // bạn có sẵn
        s.selectByValue(String.valueOf(size)); // value="25"...

        // chờ dropdown reflect
        wait.until(d -> getSelectedPageSize() == size);

        // chờ table update (nếu có)
        waitTableUpdate(before);              // bạn có sẵn
    }

    public int getSelectedPageSize() {
        try {
            WebElement sel = wait.until(ExpectedConditions.visibilityOfElementLocated(pageSizeSelect));
            Select s = new Select(sel);
            return Integer.parseInt(s.getFirstSelectedOption().getText().trim());
        } catch (Exception e) {
            return -1;
        }
    }



    public void setPageSizeToMaxCommon() {
        STEP("Set page size max common (100/50/20)");
        int[] candidates = new int[]{100, 50, 20};

        List<WebElement> selects = driver.findElements(pageSizeSelect);
        if (!selects.isEmpty()) {
            Select s = new Select(selects.get(0));
            for (int c : candidates) {
                try {
                    s.selectByVisibleText(String.valueOf(c));
                    sleep(300);
                    return;
                } catch (Exception ignored) {}
            }
            return;
        }

        List<WebElement> btns = driver.findElements(pageSizeButton);
        if (!btns.isEmpty()) {
            WebElement btn = btns.get(0);
            btn.click();
            for (int c : candidates) {
                By opt = By.xpath("//*[(@role='menuitem' or @role='option' or self::button or self::div or self::li) and contains(.,'" + c + "')]");
                List<WebElement> options = driver.findElements(opt);
                if (!options.isEmpty()) {
                    options.get(0).click();
                    sleep(300);
                    return;
                }
            }
            btn.click();
        }
    }

    public void setPageSizeToDefault() {
        STEP("Set page size default (10)");
        int[] candidates = new int[]{10, 20, 25};

        List<WebElement> selects = driver.findElements(pageSizeSelect);
        if (!selects.isEmpty()) {
            Select s = new Select(selects.get(0));
            for (int c : candidates) {
                try {
                    s.selectByVisibleText(String.valueOf(c));
                    sleep(300);
                    return;
                } catch (Exception ignored) {}
            }
            return;
        }

        List<WebElement> btns = driver.findElements(pageSizeButton);
        if (!btns.isEmpty()) {
            WebElement btn = btns.get(0);
            btn.click();
            for (int c : candidates) {
                By opt = By.xpath("//*[(@role='menuitem' or @role='option' or self::button or self::div or self::li) and contains(.,'" + c + "')]");
                List<WebElement> options = driver.findElements(opt);
                if (!options.isEmpty()) {
                    options.get(0).click();
                    sleep(300);
                    return;
                }
            }
            btn.click();
        }
    }

    public int countRowsAcrossAllPagesByStatus(String keyword) {
        STEP("Count rows across pages: status contains '" + keyword + "'");
        setPageSizeToMaxCommon();
        goToFirstPage();

        int total = 0;
        int guard = 0;

        while (guard++ < 30) {
            int rows = getRowCount();
            for (int i = 1; i <= rows; i++) {
                String st = getRowStatus(i);
                if (st != null && st.contains(keyword)) total++;
            }

            if (!isNextEnabled()) break;

            String before = rows > 0 ? getRowNameOnly(1) : "";
            clickNextPage();

            try {
                wait.until(d -> {
                    String after = getRowCount() > 0 ? getRowNameOnly(1) : "";
                    return !Objects.equals(after, before) || isEmptyVisible();
                });
            } catch (Exception ignored) {}
        }

        ACTUAL("Total rows match status = " + total);
        return total;
    }

    // =======================
    // MODAL
    // =======================
    public void openDetail(int rowIndex1Based) {
        closeAnyOverlayIfPresent();
        STEP("Open detail row=" + rowIndex1Based);

        By rowBy = By.xpath("(//table//tbody/tr)[" + rowIndex1Based + "]");
        WebElement row = wait.until(ExpectedConditions.visibilityOfElementLocated(rowBy));

        // tìm nút trong row
        WebElement btn = row.findElement(btnDetailsInRow);

        // scroll + click JS cho chắc (tránh overlay/element not clickable)
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        sleep(150);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        // ✅ WAIT: chỉ cần modalRoot xuất hiện là được (đừng wait text "Chi tiết...")
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalRoot));

        // optional: chắc chắn modal “đúng” bằng việc có textarea hoặc nút đóng
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(inputNote),
                ExpectedConditions.visibilityOfElementLocated(btnCloseBottom),
                ExpectedConditions.visibilityOfElementLocated(btnCloseIcon)
        ));

        ACTUAL("Modal open = " + isModalOpen());
    }


    public boolean isModalOpen() {
        return !driver.findElements(modalRoot).isEmpty();
    }


    public void closeModalIfOpen() {
        if (!isModalOpen()) return;

        STEP("Close modal (X / Đóng / ESC)");
        try {
            List<WebElement> x = driver.findElements(btnCloseIcon);
            if (!x.isEmpty()) x.get(0).click();
            else {
                List<WebElement> b = driver.findElements(btnCloseBottom);
                if (!b.isEmpty()) b.get(0).click();
                else driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            }
        } catch (Exception ignored) {}

        try { wait.until(d -> d.findElements(modalRoot).isEmpty()); }
        catch (Exception ignored) {}




        closeAnyOverlayIfPresent();

        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(d -> d.findElements(modalBackdrop).isEmpty());
        } catch (Exception ignored) {}

    }

    public void closeModalByBackdrop() {
        if (!isModalOpen()) return;

        STEP("Close modal by Radix backdrop");

        WebElement back = wait.until(ExpectedConditions.visibilityOfElementLocated(modalBackdrop));
        new Actions(driver).moveToElement(back, 5, 5).click().perform();

        // nếu chưa đóng, thử ESC
        if (isModalOpen()) {
            STEP("Backdrop click chưa đóng -> thử ESC");
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        }

        wait.until(d -> d.findElements(modalRoot).isEmpty());
    }




    public void closeModalByBottomButton() {
        if (!isModalOpen()) return;
        STEP("Close modal by bottom button");
        try {
            WebElement b = wait.until(ExpectedConditions.elementToBeClickable(btnCloseBottom));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", b);
        } catch (Exception ignored) {}

        try { wait.until(d -> d.findElements(modalRoot).isEmpty()); }
        catch (Exception ignored) {}

        closeAnyOverlayIfPresent();
    }


    public void closeModalByXOnly() {
        if (!isModalOpen()) return;
        STEP("Close modal by X icon only");

        List<WebElement> x = driver.findElements(btnCloseIcon);
        if (x.isEmpty()) throw new NoSuchElementException("Không thấy nút X");
        x.get(0).click();

        wait.until(d -> d.findElements(modalRoot).isEmpty());
    }

    public void closeModalByEscOnly() {
        if (!isModalOpen()) return;
        STEP("Close modal by ESC only");
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        wait.until(d -> d.findElements(modalRoot).isEmpty());
    }

    public void closeModalByBottomOnly() {
        if (!isModalOpen()) return;
        STEP("Close modal by bottom 'Đóng' only");
        WebElement b = wait.until(ExpectedConditions.elementToBeClickable(btnCloseBottom));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", b);
        wait.until(d -> d.findElements(modalRoot).isEmpty());
    }


    public void enterNoteAndConfirm(String note) {
        if (!isModalOpen()) throw new IllegalStateException("Modal chưa mở");

        STEP("Nhập note: '" + note + "'");
        WebElement txt = wait.until(ExpectedConditions.visibilityOfElementLocated(inputNote));

        txt.click();
        txt.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        txt.sendKeys(Keys.BACK_SPACE);
        if (note != null) txt.sendKeys(note);

        ACTUAL("Note length = " + (note == null ? 0 : note.length()));
        STEP("Click confirm");

        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(btnConfirm));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        EXPECT("Modal đóng");
        try { new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.findElements(modalRoot).isEmpty()); }
        catch (Exception ignored) {}

        closeAnyOverlayIfPresent();
        ACTUAL("Modal open after confirm = " + isModalOpen());
    }

    // simple read text blocks (nếu bạn cần)
    public String getModalTextAll() {
        if (!isModalOpen()) return "";
        try {
            return driver.findElement(modalRoot).getText();
        } catch (Exception e) {
            return "";
        }
    }

    // Tìm value nằm cạnh label trong modal (label có thể nằm trong <p>, <span>, <div>...)
    public String getModalValueByLabel(String labelText) {
        if (!isModalOpen()) return "";

        // label: contains text, rồi lấy element kế/cha phổ biến để ra value
        By valueBy = By.xpath(
                "//div[@role='dialog' or contains(@class,'modal')]"
                        + "//*[self::p or self::div or self::span][contains(normalize-space(.),'" + labelText + "')]"
                        + "/following::*[self::p or self::div or self::span][1]"
        );

        try {
            WebElement valueEl = wait.until(ExpectedConditions.visibilityOfElementLocated(valueBy));
            return valueEl.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }


    public String getModalInlineValue(String labelText) {
        if (!isModalOpen()) return "";
        By inlineBy = By.xpath(
                "//div[@role='dialog' or contains(@class,'modal')]"
                        + "//*[self::p or self::div or self::span][contains(normalize-space(.),'" + labelText + "')]"
        );
        try {
            String txt = wait.until(ExpectedConditions.visibilityOfElementLocated(inlineBy)).getText().trim();
            // ví dụ: "Số lượng trong lô: 50" -> "50"
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(?i)" + java.util.regex.Pattern.quote(labelText) + "\\s*:?\\s*(.+)$")
                    .matcher(txt);
            if (m.find()) return m.group(1).trim();
            return txt;
        } catch (Exception e) {
            return "";
        }
    }



    // =======================
// TABLE DATA EXTRACT
// =======================
    public static class AlertRow {
        public String tenThuoc;
        public String lo;
        public String tonKho;
        public String mucTonToiThieu;
        public String trangThai;
        public String ngayCanhBao;

        @Override
        public String toString() {
            return "AlertRow{" +
                    "tenThuoc='" + tenThuoc + '\'' +
                    ", lo='" + lo + '\'' +
                    ", tonKho='" + tonKho + '\'' +
                    ", mucTonToiThieu='" + mucTonToiThieu + '\'' +
                    ", trangThai='" + trangThai + '\'' +
                    ", ngayCanhBao='" + ngayCanhBao + '\'' +
                    '}';
        }
    }

    public java.util.List<AlertRow> readTableData() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(tableRows));
        java.util.List<WebElement> rows = driver.findElements(tableRows);

        java.util.List<AlertRow> out = new java.util.ArrayList<>();
        for (WebElement r : rows) {
            java.util.List<WebElement> tds = r.findElements(By.cssSelector("td"));
            if (tds.size() < 5) continue;
            
            String rowText = (r.getText() == null ? "" : r.getText()).toLowerCase();
            if (rowText.contains("không có") || rowText.contains("không tìm thấy")
                    || rowText.contains("no data") || rowText.contains("no results")) {
                continue;
            }


            AlertRow row = new AlertRow();

            // cột 1: tên + lô
            WebElement col0 = tds.get(0);
            row.tenThuoc = safeText(col0, By.cssSelector("p.font-medium"));
            String loText = safeText(col0, By.cssSelector("p.text-xs"));
            row.lo = loText.replace("Lô:", "").trim();

            // cột 2
            row.tonKho = tds.get(1).getText().trim();

            // cột 3
            row.mucTonToiThieu = tds.get(2).getText().trim();

            // cột 4: badge trạng thái
            row.trangThai = safeText(tds.get(3), By.cssSelector("div.inline-flex"));

            // cột 5: ngày cảnh báo
            row.ngayCanhBao = tds.get(4).getText().trim();

            out.add(row);
        }

        System.out.println("   [Read] Parsed rows = " + out.size());
        if (!out.isEmpty()) System.out.println("   [Read] First row = " + out.get(0));
        return out;
    }

    public List<AlertRow> readTableDataSafe() {
        int tries = 0;
        while (tries++ < 5) {
            try {
                return readTableData(); // hàm bạn đã có
            } catch (StaleElementReferenceException e) {
                STEP("readTableData stale -> retry " + tries);
                sleep(150);
            }
        }
        // thử lần cuối cho rõ lỗi nếu vẫn fail
        return readTableData();
    }


    private String safeText(WebElement root, By by) {
        try {
            return root.findElement(by).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }



    private String safeAttr(WebElement el, String attr) {
        try {
            String v = el.getAttribute(attr);
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }


    public void logTableSnapshot() {
        List<AlertRow> rows = readTableData();
        System.out.println("   [TABLE] rows=" + rows.size());
        for (int i = 0; i < rows.size(); i++) {
            AlertRow r = rows.get(i);
            System.out.println("   [ROW " + (i+1) + "] "
                    + "tenThuoc='" + r.tenThuoc + "', lo='" + r.lo
                    + "', tonKho='" + r.tonKho + "', mucTonToiThieu='" + r.mucTonToiThieu
                    + "', trangThai='" + r.trangThai + "', ngayCanhBao='" + r.ngayCanhBao + "'");
        }
    }

    public int countRowsOnCurrentPageByStatusText(String statusText) {
        int c = 0;
        for (AlertRow r : readTableData()) {
            if (r.trangThai != null && r.trangThai.equals(statusText)) c++;
        }
        return c;
    }


    public boolean hasDrugNameContains(String keyword) {
        String k = keyword.toLowerCase();
        for (AlertRow r : readTableData()) {
            if (r.tenThuoc != null && r.tenThuoc.toLowerCase().contains(k)) return true;
        }
        return false;
    }

    public String getEmptyText() {
        try {
            List<WebElement> t1 = driver.findElements(emptyTextAny);
            if (!t1.isEmpty()) return t1.get(0).getText().trim();

            // nếu empty nằm trong table cell
            List<WebElement> t2 = driver.findElements(emptyCellAny);
            if (!t2.isEmpty()) return t2.get(0).getText().trim();
        } catch (Exception ignored) {}
        return "";
    }

    public void waitEmptyState() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                isEmptyVisible() || getRowCount() == 0
        );
    }


    public String getModalSkuValue() {
        if (!isModalOpen()) return "";
        try {
            WebElement skuP = wait.until(ExpectedConditions.visibilityOfElementLocated(modalSkuBlock));
            String txt = skuP.getText().trim(); // có thể "SKU: SKU-xxx" hoặc "SKU:\nSKU-xxx"
            ACTUAL("skuBlockText=" + txt.replace("\n", " | "));

            // 1) Bắt dạng "SKU: SKU-xxxx" hoặc "SKU:\nSKU-xxxx"
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("SKU\\s*:?\\s*(?:\\R\\s*)?(SKU[-\\w]+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(txt);
            if (m.find()) return m.group(1).trim();

            // 2) fallback: lấy token chứa "SKU-"
            java.util.regex.Matcher m2 = java.util.regex.Pattern
                    .compile("(SKU-\\S+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(txt);
            if (m2.find()) return m2.group(1).trim();

            // 3) fallback cuối: bỏ "SKU:" rồi trim
            String cleaned = txt.replaceFirst("(?i)SKU\\s*:\\s*", "").trim();
            return cleaned;
        } catch (Exception e) {
            return "";
        }
    }



    public String getRowWarnDate(int rowIndex1Based) {
        try {
            List<WebElement> rows = getVisibleDataRows();
            if (rowIndex1Based < 1 || rowIndex1Based > rows.size()) return "";

            WebElement row = rows.get(rowIndex1Based - 1);
            List<WebElement> tds = row.findElements(By.cssSelector("td"));
            if (tds.size() < 5) return "";

            return tds.get(4).getText().trim(); // cột 5
        } catch (StaleElementReferenceException e) {
            // fallback: đọc lại bằng readTableDataSafe luôn cho chắc
            List<AlertRow> rs = readTableDataSafe();
            if (rowIndex1Based < 1 || rowIndex1Based > rs.size()) return "";
            return rs.get(rowIndex1Based - 1).ngayCanhBao == null ? "" : rs.get(rowIndex1Based - 1).ngayCanhBao.trim();
        } catch (Exception e) {
            return "";
        }
    }


    private String safeText(By by) {
        int tries = 0;
        while (tries++ < 5) {
            try {
                WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
                return el.getText().trim();
            } catch (StaleElementReferenceException e) {
                STEP("safeText(By) stale -> retry " + tries);
                sleep(120);
            }
        }
        // lần cuối để lỗi rõ nếu có
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        return el.getText().trim();
    }


    public String getModalValueUnderLabelExact(String labelExact) {
        if (!isModalOpen()) return "";

        By valueBy = By.xpath(
                "//div[@role='dialog' or contains(@class,'modal')]"
                        + "//*[self::p or self::div or self::span][normalize-space()='" + labelExact + "']"
                        + "/following::*[self::p or self::div or self::span][1]"
        );

        try {
            WebElement v = wait.until(ExpectedConditions.visibilityOfElementLocated(valueBy));
            return v.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }


    public boolean modalHasLabelExact(String labelExact) {
        if (!isModalOpen()) return false;
        By labelBy = By.xpath(
                "//div[@role='dialog' or contains(@class,'modal')]"
                        + "//*[self::p or self::div or self::span][normalize-space()='" + labelExact + "']"
        );
        return !driver.findElements(labelBy).isEmpty();
    }


    private String normalizeVNDate(String dmy) {
        if (dmy == null) return "";
        String s = dmy.trim();
        Matcher m = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})").matcher(s);
        if (!m.find()) return s;
        int d = Integer.parseInt(m.group(1));
        int mth = Integer.parseInt(m.group(2));
        String y = m.group(3);
        return String.format("%02d/%02d/%s", d, mth, y); // 1/1/2026 -> 01/01/2026
    }



}
