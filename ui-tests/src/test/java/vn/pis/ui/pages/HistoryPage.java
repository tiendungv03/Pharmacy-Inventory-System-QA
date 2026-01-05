package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HistoryPage {

 
    private final WebDriver driver;
    private final WebDriverWait wait;

    public HistoryPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(15));
    }

    private void log(String msg) {
        System.out.println("[PIS7][PAGE] " + msg);
    }

    // ===================== MENU / TITLE =====================
    private final By menuHistoryLink = By.xpath(
            "//a[contains(@href,'/history') or contains(@href,'/transactions')][.//span[contains(.,'Lịch sử') or contains(.,'Giao dịch')]]"
    );
    private final By pageTitle = By.xpath("//h1[contains(.,'Lịch sử') or contains(.,'Danh sách giao dịch')]");

    // ===================== FILTERS / SEARCH =====================
    private final By searchInput = By.xpath("//input[@placeholder='Tìm theo số lô: LOT...']");

    private final By filterTypeBtn = By.xpath("(//button[@role='combobox'])[1]");
    private final By filterWarehouseBtn = By.xpath("(//button[@role='combobox'])[2]");
    private final By filterTimeBtn = By.xpath("(//button[@role='combobox'])[3]");

    // ===================== TABLE =====================
    private final By tableHeaders = By.xpath("//table//thead//th");
    private final By tableRows    = By.xpath("//table//tbody/tr");

    private final By emptyStateCell = By.xpath(
            "//table//tbody//td[contains(.,'Không có') or contains(.,'No data') or contains(.,'No results') or contains(.,'Không tìm thấy')]"
    );

    private final By lotCells = By.xpath("//table//tbody/tr/td[3]");

    private final By emptyStateAny = By.xpath(
            "//*[contains(.,'Không có giao dịch') or contains(.,'Không có dữ liệu') or contains(.,'Không có') " +
                    "or contains(.,'No data') or contains(.,'No results') or contains(.,'Không tìm thấy')]"
    );

    // ===================== PAGINATION =====================
    private final By paginationNext = By.xpath("//button[contains(.,'Sau') or contains(@aria-label,'Next')]");
    private final By pageSizeBtn = By.xpath("//div[contains(@class,'pagination')]//div[contains(@role,'button') or contains(@class,'select')]");
    private final By pageSizeSelect = By.xpath("//select[./option[@value='10' or normalize-space(.)='10']]");

    private By previousButton = By.xpath("//button[normalize-space(.)='Trước' or .//*[normalize-space(.)='Trước']]");
    private By nextButton     = By.xpath("//button[normalize-space(.)='Sau'   or .//*[normalize-space(.)='Sau']]");

    // ===================== MODAL DETAIL =====================
    private final By modalDialog = By.xpath("//*[@role='dialog' or @data-state='open']");
    private final By modalContent = By.xpath("//div[@role='dialog']//div[contains(@class,'body') or contains(@class,'content')]");
    private final By closeModalBtn = By.xpath("//div[@role='dialog']//button[contains(@aria-label,'Close') or contains(.,'Đóng')]");

    // ===================== OVERLAY / BLOCKING =====================
    private final By blockingOverlay = By.xpath(
            "//div[contains(@class,'fixed') and contains(@class,'inset-0') and contains(@class,'z-50')]"
    );

    private final By anyCloseBtn = By.xpath(
            "//button[contains(@aria-label,'Close') or contains(.,'Đóng') or contains(.,'Close')]"
    );

    // ===================== UTILS / HELPERS =====================
    private void dismissBlockingOverlayIfAny() {
        try {
            for (int i = 0; i < 3; i++) {
                List<WebElement> overlays = driver.findElements(blockingOverlay);
                if (overlays.isEmpty()) return;

                WebElement ov = overlays.get(0);
                if (!ov.isDisplayed()) return;

                log("⚠ Found blocking overlay -> try close/ESC (attempt " + (i + 1) + ")");

                List<WebElement> closes = driver.findElements(anyCloseBtn);
                if (!closes.isEmpty() && closes.get(0).isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", closes.get(0));
                } else {
                    new Actions(driver).sendKeys(Keys.ESCAPE).perform();
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ov);
                }

                wait.withTimeout(Duration.ofSeconds(3))
                        .until(ExpectedConditions.invisibilityOfElementLocated(blockingOverlay));
            }
        } catch (Exception ignored) {
        } finally {
            wait.withTimeout(Duration.ofSeconds(15));
        }
    }

    private String origin() {
        try {
            Object o = ((JavascriptExecutor) driver).executeScript("return window.location.origin");
            return String.valueOf(o);
        } catch (Exception e) {
            return "http://localhost:5173";
        }
    }

    private String firstRowTextSafe(WebDriver d) {
        try {
            String t = d.findElement(By.xpath("//table//tbody/tr[1]")).getText();
            return t == null ? "" : t.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private int getRowCountDom() {
        try { return driver.findElements(tableRows).size(); } catch (Exception e) { return 0; }
    }

    public String getTbodyText() {
        try {
            return driver.findElement(By.xpath("//table//tbody")).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isEmptyStateDisplayed() {
        try {
            return driver.findElements(emptyStateAny).stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    // ===================== NAVIGATION =====================
    public void open() {
        log("Mở menu Lịch sử giao dịch");
        dismissBlockingOverlayIfAny();

        try {
            WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(menuHistoryLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);

            try {
                link.click();
            } catch (ElementClickInterceptedException ice) {
                log("⚠ Click bị chặn -> dismiss overlay + mở trực tiếp /history");
                dismissBlockingOverlayIfAny();
                driver.get(origin() + "/history");
            }

        } catch (TimeoutException e) {
            log("Không thấy menu, mở trực tiếp URL /history");
            driver.get(origin() + "/history");
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        log("Đã vào màn hình Lịch sử");
    }

    // ===================== TABLE - GETTERS =====================
    public List<String> getTableHeaders() {
        List<WebElement> headers = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(tableHeaders));
        List<String> headerTexts = new ArrayList<>();
        for (WebElement h : headers) headerTexts.add(h.getText().trim());
        return headerTexts;
    }

    public String getCellText(int rowIndex, int colIndex) {
        By cell = By.xpath("//tbody/tr[" + rowIndex + "]/td[" + colIndex + "]");
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(cell)).getText();
        } catch (Exception e) { return ""; }
    }

    public int getRowCount() {
        try {
            return driver.findElements(tableRows).size();
        } catch (Exception e) {
            return 0;
        }
    }

    // FIX 3: getRowTypeText không giữ WebElement cũ (tránh stale / đọc text cũ)
    public String getRowTypeText(int rowIndex) {
        By cellXpath = By.xpath("//tbody/tr[" + rowIndex + "]/td[1]");
        try {
            return wait.withTimeout(Duration.ofSeconds(10)).until(d -> {
                try {
                    String text = d.findElement(cellXpath).getText();
                    if (text == null) return null;
                    text = text.trim();
                    if (text.isEmpty()) return null;
                    if (text.contains("Đang tải")) return null;
                    return text;
                } catch (StaleElementReferenceException se) {
                    return null;
                } catch (NoSuchElementException ne) {
                    return null;
                }
            });
        } catch (TimeoutException e) {
            log("Cảnh báo: Dữ liệu tải lâu quá 10s.");
            try { return driver.findElement(cellXpath).getText().trim(); } catch (Exception ex) { return ""; }
        }
    }

    public String getRowTime(int rowIndex) {
        final int TIME_COL_INDEX = 2;
        log("Lấy thời gian dòng " + rowIndex);
        return getCellText(rowIndex, TIME_COL_INDEX);
    }

    public boolean isKeywordPresentInFirstRow(String keyword) {
        try {
            waitForDataToLoad();
            if (getRowCount() <= 0) return false;

            String firstRowText = driver.findElement(By.xpath("//table//tbody/tr[1]")).getText();
            return firstRowText != null && firstRowText.contains(keyword);
        } catch (Exception e) {
            return false;
        }
    }

    // ===================== SEARCH - STATE/VERIFY =====================
    public int getDataRowCount() {
        try {
            List<WebElement> rows = driver.findElements(tableRows);
            int count = 0;
            for (WebElement r : rows) {
                String t = r.getText();
                if (t == null) continue;
                t = t.trim();
                if (t.contains("Không có") || t.contains("No data") || t.contains("No results") || t.contains("Không tìm thấy"))
                    continue;
                if (!t.isEmpty()) count++;
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean isAnyLotMatched(String keyword) {
        try {
            List<WebElement> cells = driver.findElements(lotCells);
            for (WebElement c : cells) {
                String t = c.getText();
                if (t != null && t.replace("\n"," ").contains(keyword.trim())) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForSearchApplied(String keyword) {
        final String expected = (keyword == null) ? "" : keyword.trim();
        try {
            wait.withTimeout(Duration.ofSeconds(6)).until(d -> {
                try {
                    String v = d.findElement(searchInput).getAttribute("value");
                    v = (v == null) ? "" : v.trim();
                    return v.equals(expected);
                } catch (Exception e) {
                    return false;
                }
            });
            waitForDataToLoad();
        } catch (Exception ignored) {
        } finally {
            wait.withTimeout(Duration.ofSeconds(15));
        }
    }

    public void waitForSearchResultChanged(String keyword) {
        final String kw = (keyword == null) ? "" : keyword.trim();
        final String before = getTbodyText();

        try {
            wait.withTimeout(Duration.ofSeconds(10)).until(d -> {
                try {
                    String v = d.findElement(searchInput).getAttribute("value");
                    if (v == null || !v.trim().equals(kw)) return false;

                    if (d.findElements(tableRows).size() == 0) return true;
                    if (isEmptyStateDisplayed()) return true;
                    if (getDataRowCount() == 0) return true;
                    if (!kw.isEmpty() && isAnyLotMatched(kw)) return true;

                    String now = getTbodyText();
                    return now != null && !now.equals(before);

                } catch (StaleElementReferenceException e) {
                    return false;
                } catch (Exception e) {
                    return false;
                }
            });
        } finally {
            wait.withTimeout(Duration.ofSeconds(15));
        }
    }

    public boolean isNoResultForLot(String keyword) {
        try {
            if (isEmptyStateDisplayed()) return true;
            if (getRowCountDom() == 0) return true;
            if (getDataRowCount() == 0) return true;
            return !isAnyLotMatched(keyword);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoResultOrEmptyState() {
        if (isEmptyStateDisplayed()) return true;
        return getDataRowCount() == 0;
    }

    // ===================== SEARCH - ACTIONS =====================
    public void searchByLot(String keyword) {
        log("Search theo số lô: " + keyword);
        dismissBlockingOverlayIfAny();

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(Keys.DELETE);

        input.sendKeys(keyword);
        input.sendKeys(Keys.ENTER);
        input.sendKeys(Keys.TAB);

        try {
            waitForSearchResultChanged(keyword);
        } catch (TimeoutException te) {
            log("⚠ Search wait timeout (continue): " + te.getMessage());
        }
        waitForDataToLoad();
    }

    // 3) Hàm clear search (reset)
    public void clearSearch() {
        log("Clear search");
        try {
            dismissBlockingOverlayIfAny();

            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
            input.click();
            input.sendKeys(Keys.CONTROL + "a");
            input.sendKeys(Keys.DELETE);
            input.sendKeys(Keys.ENTER);
            input.sendKeys(Keys.TAB);

            waitForSearchApplied("");
            waitForDataToLoad();
        } catch (Exception e) {
            log("⚠ Không clear được search: " + e.getMessage());
        }
    }

    // ===================== FILTERS =====================

    private void performFilterSelection(By btnLocator, String optionName) {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        By optionLocator = By.xpath(
                "//div[@role='option' or @role='menuitem' or @data-radix-collection-item]" +
                        "[contains(normalize-space(.), '" + optionName + "')]"
        );

        WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(optionLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
    }

    // FIX 2: filterByType đợi rerender (staleness) + đợi đúng token Nhập/Xuất
    public void filterByType(String typeName) {
        log("Thực hiện lọc theo Loại: " + typeName);

        WebElement oldFirstCell = null;
        By firstCell = By.xpath("//table/tbody/tr[1]/td[1]");

        try { oldFirstCell = driver.findElement(firstCell); } catch (Exception ignored) {}

        performFilterSelection(filterTypeBtn, typeName);

        if (oldFirstCell != null) {
            try {
                wait.until(ExpectedConditions.stalenessOf(oldFirstCell));
            } catch (Exception ignored) {}
        }

        String token = typeName.contains("Xuất") ? "Xuất" : "Nhập";

        try {
            wait.withTimeout(Duration.ofSeconds(12)).until(d -> {
                try {
                    if (d.findElements(tableRows).size() == 0) return true;

                    String t = d.findElement(firstCell).getText();
                    if (t == null) return false;
                    t = t.trim();
                    if (t.isEmpty()) return false;
                    if (t.contains("Đang tải")) return false;
                    if (t.contains("Không có giao dịch")) return true;

                    return t.contains(token) || t.contains(token.equals("Xuất") ? "Export" : "Import");
                } catch (StaleElementReferenceException se) {
                    return false;
                } catch (NoSuchElementException ne) {
                    return false;
                }
            });
        } catch (Exception e) {
            log("⚠️ Cảnh báo: Bảng chưa cập nhật theo filter '" + typeName + "' sau timeout.");
        } finally {
            waitForDataToLoad();
        }
    }

    public void filterByWarehouse(String warehouseName) {
        log("Thực hiện lọc theo Kho: " + warehouseName);
        performFilterSelection(filterWarehouseBtn, warehouseName);
        waitForDataToLoad();
    }

    public void filterByTime(String timeRange) {
        log("Lọc thời gian: " + timeRange);
        try {
            performFilterSelection(filterTimeBtn, timeRange);
            waitForDataToLoad();
        } catch (Exception e) {
            log("⚠️ Không tìm thấy nút lọc thời gian (hoặc UI thay đổi).");
        }
    }

    public void changePageSize(String size) {
        log("Đổi số dòng hiển thị: " + size);
        try {
            dismissBlockingOverlayIfAny();

            WebElement selectEl = wait.until(ExpectedConditions.elementToBeClickable(pageSizeSelect));
            Select select = new Select(selectEl);

            try {
                select.selectByValue(size.trim());
            } catch (NoSuchElementException ex) {
                select.selectByVisibleText(size.trim());
            }

            waitForDataToLoad();
        } catch (Exception e) {
            log("⚠️ Không đổi được page size: " + e.getMessage());
        }
    }

    // ===================== MODAL =====================
    private By btnViewDetail(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//button[.//*[name()='svg' and contains(@class,'lucide-eye')]]");
    }

    public void clickViewDetail(int rowIndex) {
        log("Click xem chi tiết dòng " + rowIndex);

        dismissBlockingOverlayIfAny();

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnViewDetail(rowIndex)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(modalDialog));
        } catch (TimeoutException e) {
            dismissBlockingOverlayIfAny();
            throw e;
        }
    }

    public String getDetailModalContent() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(modalContent)).getText();
        } catch (Exception e) { return ""; }
    }

    public void closeModal() {
        try {
            WebElement btn = driver.findElement(closeModalBtn);
            btn.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(modalDialog));
        } catch (Exception ignored) {}
    }

    public boolean isDetailModalDisplayed() {
        try {
            return !driver.findElements(modalDialog).isEmpty()
                    && driver.findElement(modalDialog).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDetailModalClosed() {
        try {
            return driver.findElements(modalDialog).isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    // ===================== PAGINATION =====================
    public boolean isPaginationDisplayed() {
        try {
            return driver.findElement(paginationNext).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void clickNextPage() {
        driver.findElement(paginationNext).click();
        try { Thread.sleep(1000); } catch (Exception ignored) {}
    }

    public void clickPreviousButton() {
        log("Click nút 'Trước'");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(previousButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        waitForDataToLoad();
    }

    public void clickNextButton() {
        log("Click nút 'Sau'");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(nextButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        waitForDataToLoad();
    }

    public boolean isNextButtonDisabled() {
        List<WebElement> els = driver.findElements(nextButton);
        if (els.isEmpty()) return true;
        WebElement btn = els.get(0);

        String ariaDisabled = btn.getAttribute("aria-disabled");
        String disabledAttr = btn.getAttribute("disabled");

        return !btn.isEnabled()
                || "true".equalsIgnoreCase(ariaDisabled)
                || disabledAttr != null
                || (btn.getAttribute("class") != null && btn.getAttribute("class").contains("disabled"));
    }

    public boolean isPreviousButtonDisabled() {
        List<WebElement> els = driver.findElements(previousButton);
        if (els.isEmpty()) return true;
        WebElement btn = els.get(0);

        String ariaDisabled = btn.getAttribute("aria-disabled");
        String disabledAttr = btn.getAttribute("disabled");

        return !btn.isEnabled()
                || "true".equalsIgnoreCase(ariaDisabled)
                || disabledAttr != null
                || (btn.getAttribute("class") != null && btn.getAttribute("class").contains("disabled"));
    }

    // ===================== ICONS =====================
    public boolean isImportIconDisplayed(int rowIndex) {
        try {
            By iconImport = By.xpath("(//tbody/tr)[" + rowIndex + "]/td[1]//*[name()='svg' and contains(@class, 'arrow-down-to-line')]");
            return driver.findElements(iconImport).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExportIconDisplayed(int rowIndex) {
        try {
            By iconExport = By.xpath("(//tbody/tr)[" + rowIndex + "]/td[1]//*[name()='svg' and contains(@class, 'arrow-up-from-line')]");
            return driver.findElements(iconExport).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ===================== WAIT =====================
    public void waitForDataToLoad() {
        try {
            wait.withTimeout(Duration.ofSeconds(8)).until(d -> {
                try {
                    if (d.findElements(tableRows).size() == 0) return true;
                    String text = d.findElement(By.xpath("//tbody/tr[1]/td[1]")).getText();
                    return text != null && !text.contains("Đang tải");
                } catch (StaleElementReferenceException se) {
                    return false;
                } catch (NoSuchElementException ne) {
                    return false;
                }
            });
        } catch (Exception ignored) {}
    }

}
