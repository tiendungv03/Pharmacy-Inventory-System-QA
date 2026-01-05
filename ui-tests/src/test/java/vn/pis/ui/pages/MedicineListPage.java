package vn.pis.ui.pages;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import vn.pis.ui.util.TableWait;

public class MedicineListPage {

    // ===== CONFIG =====
    public static final long TABLE_STABLE_MS = 600;
    public static final long TABLE_TIMEOUT_MS = 15000;
    public static final long TYPE_DELAY_MS = 250;

    // ===== COLUMN MAP (0-based) =====
    public static final int COL_CODE = 0;
    public static final int COL_NAME = 1;
    public static final int COL_CATEGORY = 2;
    public static final int COL_DESCRIPTION = 3;
    public static final int COL_UNIT = 4;
    public static final int COL_SUPPLIER = 5;
    public static final int COL_EXPIRY = 6;

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;

    // ================== LOCATORS (PUBLIC để test dùng thẳng) ==================
    public final By pageTitle = By.xpath("//h1[normalize-space()='Danh mục thuốc']");
    public final By pageSubtitle = By.xpath(
            "//p[contains(@class,'text-muted-foreground') and contains(normalize-space(.),'Quản lý toàn bộ thuốc trong kho')]");

    public final By searchInput = By.xpath("//input[@placeholder='Tìm kiếm theo tên thuốc hoặc mã thuốc...']");
    public final By searchIcon = By.cssSelector("svg.lucide-search");

    public final By categoryLabel = By.xpath("//label[normalize-space()='Danh mục thuốc']");
    public final By supplierLabel = By.xpath("//label[normalize-space()='Nhà cung cấp']");
    public final By categoryDropdown = By
            .xpath("//label[normalize-space()='Danh mục thuốc']/following-sibling::button");
    public final By supplierDropdown = By.xpath("//label[normalize-space()='Nhà cung cấp']/following-sibling::button");
    public final By clearAllButton = By.xpath("//button[contains(normalize-space(.),'Xóa tất cả')]");

    public final By table = By.cssSelector("table");
    public final By tableHeaderCells = By.cssSelector("table thead tr th");

    public final By emptyMessageCell = By.xpath(
            "//td[@colspan='7' and contains(@class,'text-center') and normalize-space()='Không có sản phẩm nào']");
    public final By dataRows = By.xpath("//table//tbody//tr[count(td) >= 7]");

    public final By rowsPerPageLabel = By.xpath("//span[normalize-space()='Số dòng/trang:']");
    public final By rowsPerPageButton = By
            .xpath("//span[normalize-space()='Số dòng/trang:']/following-sibling::button");
    public final By rowsPerPageValue = By.xpath(
            "//span[normalize-space()='Số dòng/trang:']/following-sibling::button/span[@style='pointer-events: none;']");

    public final By previousButton = By.xpath("//button[contains(normalize-space(.),'Trước')]");
    public final By nextButton = By.xpath("//button[contains(normalize-space(.),'Sau')]");
    public final By totalItemsText = By.xpath("//div[contains(@class, 'text-muted-foreground')]/span[2]");
    public final By currentRangeSpan = By.xpath("//div[contains(@class,'text-muted-foreground')]/span[1]");

    public final By dropdownOptions = By.xpath("//div[@role='option']");

    // prev/next states
    public final By prevEnabled = By.xpath("//button[contains(normalize-space(.),'Trước') and not(@disabled)]");
    public final By nextEnabled = By.xpath("//button[contains(normalize-space(.),'Sau') and not(@disabled)]");
    public final By prevDisabled = By.xpath("//button[contains(normalize-space(.),'Trước') and @disabled]");
    public final By nextDisabled = By.xpath("//button[contains(normalize-space(.),'Sau') and @disabled]");

    public MedicineListPage(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
        this.actions = new Actions(driver);
    }

    public WebDriverWait getWait() {
        return wait;
    }

    public WebDriver getDriver() {
        return driver;
    }

    // ================== BASIC HELPERS ==================
    public String norm(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }

    private void jsScrollIntoView(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    private void jsFocus(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", el);
    }

    private void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    public void safeClick(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        try {
            el.click();
        } catch (Exception e) {
            jsClick(el);
        }
    }

    public void safeClickIfPresent(By locator) {
        try {
            List<WebElement> els = driver.findElements(locator);
            if (els.isEmpty())
                return;
            WebElement el = els.get(0);
            if (!el.isDisplayed() || !el.isEnabled())
                return;
            try {
                el.click();
            } catch (Exception e) {
                jsClick(el);
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isEmptyShown() {
        return !driver.findElements(emptyMessageCell).isEmpty()
                && driver.findElement(emptyMessageCell).isDisplayed();
    }

    public void waitLoaded() {
        wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
        waitForTableRefresh();
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void reset() {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
        clearSearchInput();
        safeClickIfPresent(clearAllButton);
        waitForTableRefresh();
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    // ================== TABLE WAIT ==================
    public void waitForTableRefresh() {
        wait.until(d -> isEmptyShown() || !d.findElements(dataRows).isEmpty());
    }

    public void waitForTableStable(long stableMs, long timeoutMs) {
        TableWait.waitStable(driver, stableMs, timeoutMs, currentRangeSpan, totalItemsText, dataRows);
    }

    public WebElement getFirstDataRowOrNull() {
        List<WebElement> rows = driver.findElements(dataRows);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int getDataRowCount() {
        return driver.findElements(dataRows).size();
    }

    public String getCellText(WebElement row, int colIndex) {
        List<WebElement> tds = row.findElements(By.cssSelector("td"));
        if (tds.size() <= colIndex)
            return "";
        return norm(tds.get(colIndex).getText());
    }

    public void waitTableChangeAfterAction(WebElement oldFirstRow) {
        if (oldFirstRow != null) {
            try {
                wait.until(ExpectedConditions.stalenessOf(oldFirstRow));
            } catch (TimeoutException ignored) {
            }
        }
        waitForTableRefresh();
    }

    // ================== RANGE / TOTAL ==================
    public String getRangeText() {
        return norm(wait.until(ExpectedConditions.visibilityOfElementLocated(currentRangeSpan)).getText());
    }

    public void waitForRangeChange(String oldRangeText) {
        wait.until(d -> {
            try {
                String now = norm(d.findElement(currentRangeSpan).getText());
                return !now.isEmpty() && !now.equals(oldRangeText);
            } catch (Exception e) {
                return false;
            }
        });
    }

    public int parseTotalItems() {
        String total = norm(wait.until(ExpectedConditions.visibilityOfElementLocated(totalItemsText)).getText());
        String digits = total.replaceAll("\\D", "");
        if (digits.isEmpty())
            return 0;
        return Integer.parseInt(digits);
    }

    public static class Range {
        public int start, end;

        public Range(int s, int e) {
            start = s;
            end = e;
        }
    }

    public Range parseRange(String rangeText) {
        String cleaned = norm(rangeText).replace("–", "-");
        String digitsOnly = cleaned.replaceAll("[^0-9\\-]", "");
        String[] parts = digitsOnly.split("-");
        if (parts.length < 2)
            return new Range(0, 0);
        int s = parts[0].isEmpty() ? 0 : Integer.parseInt(parts[0]);
        int e = parts[1].isEmpty() ? 0 : Integer.parseInt(parts[1]);
        return new Range(s, e);
    }

    public int getCurrentStartIndex() {
        return parseRange(getRangeText()).start;
    }

    // ================== SEARCH ==================
    public void clearSearchInput() {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    }

    public String getFirstRowCodeOrNull() {
        WebElement row = getFirstDataRowOrNull();
        if (row == null)
            return null;
        String code = getCellText(row, COL_CODE);
        return code.isEmpty() ? null : code;
    }

    public String getFirstRowNameOrNull() {
        WebElement row = getFirstDataRowOrNull();
        if (row == null)
            return null;
        String name = getCellText(row, COL_NAME);
        return name.isEmpty() ? null : name;
    }

    public String pickKeywordFromName(String name) {
        String[] parts = norm(name).split(" ");
        for (String p : parts) {
            String cleaned = p.replaceAll("[^\\p{L}\\p{N}-]", "");
            if (cleaned.length() >= 3)
                return cleaned;
        }
        String cleanedAll = norm(name).replaceAll("[^\\p{L}\\p{N}-]", "");
        if (cleanedAll.length() >= 3)
            return cleanedAll.substring(0, 3);
        return norm(name);
    }

    public void setSearch(String keyword) {
        WebElement oldFirst = getFirstDataRowOrNull();
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));

        actions.moveToElement(input)
                .click()
                .keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
                .sendKeys(Keys.BACK_SPACE)
                .build().perform();

        String kw = keyword == null ? "" : keyword;
        for (char c : kw.toCharArray()) {
            actions.sendKeys(String.valueOf(c))
                    .pause(Duration.ofMillis(TYPE_DELAY_MS))
                    .build().perform();
        }
        actions.sendKeys(Keys.ENTER).build().perform();

        waitTableChangeAfterAction(oldFirst);
        waitForSearchAppliedStable(kw);
    }

    public void waitForSearchAppliedStable(String keyword) {
        String kw = norm(keyword).toLowerCase();

        wait.until(d -> {
            if (isEmptyShown())
                return true;
            List<WebElement> rows = d.findElements(dataRows);
            if (rows.isEmpty())
                return false;

            int sample = Math.min(rows.size(), 5);
            for (int i = 0; i < sample; i++) {
                WebElement r = rows.get(i);
                String code = getCellText(r, COL_CODE).toLowerCase();
                String name = getCellText(r, COL_NAME).toLowerCase();
                if (!(code.contains(kw) || name.contains(kw)))
                    return false;
            }
            return true;
        });

        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    // ================== DROPDOWN ==================
    public void openDropdown(By trigger) {
        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(trigger));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
        } catch (Exception e) {
            jsClick(btn);
        }
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(dropdownOptions));
    }

    public void closeDropdownByEscape() {
        actions.sendKeys(Keys.ESCAPE).perform();
        wait.until(d -> {
            List<WebElement> opts = d.findElements(dropdownOptions);
            for (WebElement o : opts) {
                try {
                    if (o.isDisplayed())
                        return false;
                } catch (Exception ignored) {
                }
            }
            return true;
        });
    }

    public void clickDropdownOptionContains(String optionText) {
        By option = By.xpath("//div[@role='option'][contains(normalize-space(.), \"" + optionText + "\")]");
        WebElement opt = wait.until(ExpectedConditions.elementToBeClickable(option));
        jsScrollIntoView(opt);
        jsClick(opt);
    }

    public String selectFirstNonAllOption(By dropdownTrigger) {
        WebElement oldFirst = getFirstDataRowOrNull();
        openDropdown(dropdownTrigger);

        List<WebElement> opts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(dropdownOptions));
        String chosen = null;
        for (WebElement o : opts) {
            String t = norm(o.getText());
            if (t.isEmpty())
                continue;
            if (t.equalsIgnoreCase("Tất cả"))
                continue;
            chosen = t;
            break;
        }
        if (chosen == null)
            Assert.fail("Không tìm được option nào khác 'Tất cả'");

        clickDropdownOptionContains(chosen);
        waitTableChangeAfterAction(oldFirst);
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
        return chosen;
    }

    // ================== FILTER WAIT ==================
    public void waitForCategoryFilterApplied(String chosenCategory) {
        wait.until(d -> {
            if (isEmptyShown())
                return true;
            List<WebElement> rows = d.findElements(dataRows);
            if (rows.isEmpty())
                return false;

            int checked = 0;
            for (WebElement r : rows) {
                String cat = getCellText(r, COL_CATEGORY);
                if (cat.isEmpty())
                    continue;
                checked++;
                if (!chosenCategory.equals(cat))
                    return false;
            }
            return checked > 0;
        });
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void waitForSupplierFilterApplied(String chosenSupplier) {
        wait.until(d -> {
            if (isEmptyShown())
                return true;
            List<WebElement> rows = d.findElements(dataRows);
            if (rows.isEmpty())
                return false;

            int checked = 0;
            for (WebElement r : rows) {
                String supp = getCellText(r, COL_SUPPLIER);
                if (supp.isEmpty())
                    continue;
                checked++;
                if (!chosenSupplier.equals(supp))
                    return false;
            }
            return checked > 0;
        });
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void waitForCategoryAndSupplierApplied(String chosenCategory, String chosenSupplier) {
        wait.until(d -> {
            if (isEmptyShown())
                return true;
            List<WebElement> rows = d.findElements(dataRows);
            if (rows.isEmpty())
                return false;

            int checked = 0;
            for (WebElement r : rows) {
                String cat = getCellText(r, COL_CATEGORY);
                String supp = getCellText(r, COL_SUPPLIER);
                if (cat.isEmpty() || supp.isEmpty())
                    continue;
                checked++;
                if (!chosenCategory.equals(cat))
                    return false;
                if (!chosenSupplier.equals(supp))
                    return false;
            }
            return checked > 0;
        });
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void waitForSupplierAndSearchAppliedStable(String chosenSupplier, String keyword) {
        String kw = norm(keyword).toLowerCase();

        wait.until(d -> {
            if (isEmptyShown())
                return true;
            List<WebElement> rows = d.findElements(dataRows);
            if (rows.isEmpty())
                return false;

            int sample = Math.min(rows.size(), 5);
            for (int i = 0; i < sample; i++) {
                WebElement r = rows.get(i);
                String supp = getCellText(r, COL_SUPPLIER);
                String code = getCellText(r, COL_CODE).toLowerCase();
                String name = getCellText(r, COL_NAME).toLowerCase();
                if (!chosenSupplier.equals(supp))
                    return false;
                if (!(code.contains(kw) || name.contains(kw)))
                    return false;
            }
            return true;
        });

        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    // ================== ROWS PER PAGE & PAGINATION ==================
    public void changeRowsPerPage(String value) {
        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(rowsPerPageButton));
        WebElement oldFirst = getFirstDataRowOrNull();
        String oldRange = getRangeText();

        try {
            wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
        } catch (Exception e) {
            jsClick(btn);
        }

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(dropdownOptions));

        By opt = By.xpath("//div[@role='option']//span[normalize-space()='" + value + "']");
        WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(opt));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(option)).click();
        } catch (Exception e) {
            jsClick(option);
        }

        wait.until(ExpectedConditions.textToBe(rowsPerPageValue, value));
        try {
            waitForRangeChange(oldRange);
        } catch (Exception ignored) {
        }

        waitTableChangeAfterAction(oldFirst);
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void ensureRowsPerPage(String value) {
        String cur = "";
        try {
            cur = norm(driver.findElement(rowsPerPageValue).getText());
        } catch (Exception ignored) {
        }
        if (!value.equals(cur))
            changeRowsPerPage(value);
        else
            waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void clickNextStable() {
        WebElement oldFirst = getFirstDataRowOrNull();
        String oldRange = getRangeText();
        safeClick(nextButton);
        waitForRangeChange(oldRange);
        waitTableChangeAfterAction(oldFirst);
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void clickPrevStable() {
        WebElement oldFirst = getFirstDataRowOrNull();
        String oldRange = getRangeText();
        safeClick(previousButton);
        waitForRangeChange(oldRange);
        waitTableChangeAfterAction(oldFirst);
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public WebElement findDisplayedEnabled(By locator) {
        List<WebElement> els = driver.findElements(locator);
        for (WebElement e : els) {
            try {
                if (e.isDisplayed() && e.isEnabled())
                    return e;
            } catch (Exception ignored) {
            }
        }
        throw new NoSuchElementException("No displayed+enabled element for: " + locator);
    }

    public void clickNextReliable() {
        String before = getRangeText();
        WebElement oldFirst = getFirstDataRowOrNull();

        WebElement next = findDisplayedEnabled(nextEnabled);
        try {
            next.click();
        } catch (Exception e) {
            jsClick(next);
        }

        waitForRangeChange(before);
        if (oldFirst != null) {
            try {
                wait.until(ExpectedConditions.stalenessOf(oldFirst));
            } catch (Exception ignored) {
            }
        }
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void clickPrevReliable() {
        String before = getRangeText();
        WebElement oldFirst = getFirstDataRowOrNull();

        WebElement prev = findDisplayedEnabled(prevEnabled);
        try {
            prev.click();
        } catch (Exception e) {
            jsClick(prev);
        }

        waitForRangeChange(before);
        if (oldFirst != null) {
            try {
                wait.until(ExpectedConditions.stalenessOf(oldFirst));
            } catch (Exception ignored) {
            }
        }
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);
    }

    public void goToLastPageByRange(int total) {
        int guard = 0;
        while (guard++ < 60) {
            Range r = parseRange(getRangeText());
            if (r.end >= total)
                return;
            clickNextReliable();
        }
        Assert.fail("Không thể đi tới trang cuối. total=" + total + ", range=" + getRangeText());
    }

    // ================== DISABLED STATE ==================
    public boolean isDisabled(WebElement el) {
        String disabled = el.getAttribute("disabled");
        String aria = el.getAttribute("aria-disabled");
        String cls = el.getAttribute("class");
        return (disabled != null)
                || ("true".equalsIgnoreCase(aria))
                || (cls != null && cls.toLowerCase().contains("disabled"))
                || !el.isEnabled();
    }

    public boolean isTrulyDisabled(WebElement el) {
        if (el == null)
            return true;
        try {
            if (!el.isDisplayed())
                return true;
        } catch (Exception e) {
            return true;
        }

        String disabled = el.getAttribute("disabled");
        String aria = el.getAttribute("aria-disabled");
        String cls = el.getAttribute("class");
        String pe = "";
        try {
            pe = el.getCssValue("pointer-events");
        } catch (Exception ignored) {
        }

        if (disabled != null)
            return true;
        if ("true".equalsIgnoreCase(aria))
            return true;
        if (cls != null && cls.toLowerCase().contains("disabled"))
            return true;
        if ("none".equalsIgnoreCase(pe))
            return true;

        return !el.isEnabled();
    }

    // ================== A11Y ==================
    public String getAccessibleName(WebElement el) {
        String ariaLabel = el.getAttribute("aria-label");
        if (!norm(ariaLabel).isEmpty())
            return norm(ariaLabel);

        String ariaLabelledBy = el.getAttribute("aria-labelledby");
        if (!norm(ariaLabelledBy).isEmpty()) {
            try {
                WebElement ref = driver.findElement(By.id(ariaLabelledBy.trim()));
                String t = norm(ref.getText());
                if (!t.isEmpty())
                    return t;
            } catch (Exception ignored) {
            }
        }

        String id = el.getAttribute("id");
        if (!norm(id).isEmpty()) {
            List<WebElement> labels = driver.findElements(By.cssSelector("label[for='" + id.trim() + "']"));
            if (!labels.isEmpty()) {
                String t = norm(labels.get(0).getText());
                if (!t.isEmpty())
                    return t;
            }
        }

        String text = norm(el.getText());
        if (!text.isEmpty())
            return text;

        String ph = el.getAttribute("placeholder");
        if (!norm(ph).isEmpty())
            return norm(ph);

        return "";
    }

    public void assertHasAccessibleName(WebElement el, String hint) {
        Assert.assertFalse(norm(getAccessibleName(el)).isEmpty(), "Thiếu accessible name cho: " + hint);
    }

    // ================== KEYBOARD HELPERS ==================
    public WebElement findDisplayed(By locator) {
        List<WebElement> els = driver.findElements(locator);
        for (WebElement e : els) {
            try {
                if (e.isDisplayed())
                    return e;
            } catch (Exception ignored) {
            }
        }
        throw new NoSuchElementException("No displayed element for: " + locator);
    }

    public void focusByJS(WebElement el) {
        jsScrollIntoView(el);
        jsFocus(el);
    }

    public void sendKeyToActive(Keys key) {
        new Actions(driver).sendKeys(key).perform();
    }

    public void openDropdownByKeyboard(By dropdownTrigger) {
        WebElement btn = findDisplayed(dropdownTrigger);
        focusByJS(btn);

        sendKeyToActive(Keys.ENTER);
        if (driver.findElements(dropdownOptions).isEmpty())
            sendKeyToActive(Keys.SPACE);

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(dropdownOptions));
    }

    public String selectFirstNonAllOptionByKeyboard(By dropdownTrigger) {
        WebElement oldFirst = getFirstDataRowOrNull();
        openDropdownByKeyboard(dropdownTrigger);

        List<WebElement> opts = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(dropdownOptions));

        int targetIndex = -1;
        String chosen = null;
        for (int i = 0; i < opts.size(); i++) {
            String t = norm(opts.get(i).getText());
            if (t.isEmpty())
                continue;
            if (t.equalsIgnoreCase("Tất cả"))
                continue;
            targetIndex = i;
            chosen = t;
            break;
        }
        Assert.assertTrue(targetIndex >= 0, "Không tìm thấy option khác 'Tất cả' để chọn bằng keyboard");

        for (int k = 0; k < targetIndex; k++) {
            sendKeyToActive(Keys.ARROW_DOWN);
            try {
                Thread.sleep(60);
            } catch (InterruptedException ignored) {
            }
        }

        sendKeyToActive(Keys.ENTER);
        closeDropdownByEscape();

        waitTableChangeAfterAction(oldFirst);
        waitForTableStable(TABLE_STABLE_MS, TABLE_TIMEOUT_MS);

        return chosen;
    }

    // ================== TABLE FORMAT VALIDATION HELPERS ==================
    private static String showCodepoints(String s) {
        if (s == null)
            return "null";
        StringBuilder sb = new StringBuilder();
        s.codePoints().forEach(cp -> sb.append(String.format("U+%04X ", cp)));
        return sb.toString().trim();
    }

    public void assertExpiryFormatOrDash(String expiry) {
        String raw = expiry == null ? "" : expiry;

        // normalize mấy ký tự hay bị “ảo” trên UI
        String v = raw
                .replace('\u00A0', ' ') // NBSP
                .replaceAll("[\\u200B-\\u200F\\uFEFF]", "") // zero-width/BOM
                .trim()
                .replace('／', '/')
                .replace('∕', '/')
                .replace('⁄', '/');

        if ("-".equals(v) || v.isEmpty())
            return;

        Assert.assertTrue(v.matches("^[0-9]{1,2}/[0-9]{1,2}/[0-9]{4}$"),
                "Hạn sử dụng sai format (d/M/yyyy hoặc dd/MM/yyyy). raw=[" + raw + "], norm=[" + v + "], cps="
                        + showCodepoints(raw));

        String[] p = v.split("/");
        int d = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        int y = Integer.parseInt(p[2]);

        try {
            LocalDate.of(y, m, d); // STRICT: sai ngày/tháng sẽ ném exception
        } catch (Exception e) {
            Assert.fail("Hạn sử dụng không phải ngày hợp lệ. raw=[" + raw + "], norm=[" + v + "], cps="
                    + showCodepoints(raw), e);
        }
    }

    public List<String> allowedRowsPerPage() {
        return Arrays.asList("25", "50", "100");
    }
}
