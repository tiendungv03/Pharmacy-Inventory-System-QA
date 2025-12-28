package vn.pis.ui.tests;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.*;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.LoginPage;
import vn.pis.ui.pages.MedicineListPage;
import vn.pis.ui.util.TestEnv;

public class PIS4_MedicineList extends BaseTest {

    private LoginPage loginPage;
    private MedicineListPage page;

    @BeforeClass
    public void loginAndOpen() {
        loginPage = new LoginPage(driver);
        page = new MedicineListPage(driver, Duration.ofSeconds(15));

        loginPage.open(TestEnv.BASE_URL + "/login");
        loginPage.login(TestEnv.ADMIN_USER, TestEnv.ADMIN_PASS);
        Assert.assertTrue(loginPage.isLoginSuccess(), "Login failed");

        driver.get(TestEnv.BASE_URL + "/inventory");
        page.waitLoaded();
    }

    @BeforeMethod
    public void reset() {
        page.reset();
    }

    // ================== UI BASIC ==================
    @Test(priority = 1)
    public void PIS4_UI_TC_01_pageTitleAndSubtitleVisible() {
        Assert.assertTrue(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.pageTitle)).isDisplayed());
        Assert.assertTrue(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.pageSubtitle)).isDisplayed());
    }

    @Test(priority = 2)
    public void PIS4_UI_TC_02_searchInputVisibleEnabledPlaceholderCorrect() {
        WebElement input = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.searchInput));
        Assert.assertTrue(input.isDisplayed());
        Assert.assertTrue(input.isEnabled());
        Assert.assertEquals(input.getAttribute("placeholder"), "Tìm kiếm theo tên thuốc hoặc mã thuốc...");
    }

    @Test(priority = 3)
    public void PIS4_UI_TC_03_searchIconRenders() {
        List<WebElement> icons = driver.findElements(page.searchIcon);
        Assert.assertTrue(icons.size() >= 1);
        Assert.assertTrue(icons.get(0).isDisplayed());
    }

    @Test(priority = 4)
    public void PIS4_UI_TC_04_filterLabelsAndDropdownsVisible() {
        Assert.assertTrue(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.categoryLabel)).isDisplayed());
        Assert.assertTrue(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.supplierLabel)).isDisplayed());

        WebElement cat = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.categoryDropdown));
        WebElement sup = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.supplierDropdown));
        Assert.assertTrue(cat.isDisplayed() && cat.isEnabled());
        Assert.assertTrue(sup.isDisplayed() && sup.isEnabled());
    }

    @Test(priority = 5)
    public void PIS4_UI_TC_05_clearAllButtonVisible() {
        WebElement btn = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.clearAllButton));
        Assert.assertTrue(btn.isDisplayed());
    }

    @Test(priority = 6)
    public void PIS4_UI_TC_06_tableRenders() {
        Assert.assertTrue(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.table)).isDisplayed());
    }

    @Test(priority = 7)
    public void PIS4_UI_TC_07_tableHeaderHas7ColumnsAndNotEmpty() {
        page.getWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(page.tableHeaderCells));
        List<WebElement> headers = driver.findElements(page.tableHeaderCells);

        Assert.assertEquals(headers.size(), 7);
        for (WebElement h : headers) Assert.assertFalse(page.norm(h.getText()).isEmpty());
    }

    @Test(priority = 8)
    public void PIS4_UI_TC_08_tableRowsEachRowHas7Cells_orEmptyState() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        if (page.isEmptyShown()) {
            Assert.assertEquals(page.norm(driver.findElement(page.emptyMessageCell).getText()), "Không có sản phẩm nào");
            return;
        }

        List<WebElement> rows = driver.findElements(page.dataRows);
        Assert.assertTrue(rows.size() > 0);

        for (WebElement r : rows) {
            int tdCount = r.findElements(By.cssSelector("td")).size();
            Assert.assertEquals(tdCount, 7);
        }
    }

    @Test(priority = 9)
    public void PIS4_UI_TC_09_firstRowMainCellsNotEmpty_ifAnyRow() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        if (page.isEmptyShown()) return;

        WebElement first = page.getFirstDataRowOrNull();
        if (first == null) return;

        Assert.assertFalse(page.getCellText(first, MedicineListPage.COL_CODE).isEmpty());
        Assert.assertFalse(page.getCellText(first, MedicineListPage.COL_NAME).isEmpty());
        Assert.assertFalse(page.getCellText(first, MedicineListPage.COL_CATEGORY).isEmpty());
    }

    @Test(priority = 10)
    public void PIS4_UI_TC_10_rowsPerPageControlRendersAndValueValid() {
        Assert.assertTrue(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.rowsPerPageLabel)).isDisplayed());

        WebElement btn = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.rowsPerPageButton));
        Assert.assertTrue(btn.isDisplayed() && btn.isEnabled());

        String value = page.norm(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.rowsPerPageValue)).getText());
        Assert.assertTrue(page.allowedRowsPerPage().contains(value));
    }

    @Test(priority = 11)
    public void PIS4_UI_TC_11_paginationButtonsRender_andPrevDisabledOnFirstPage() {
        WebElement prev = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.previousButton));
        WebElement next = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.nextButton));

        Assert.assertTrue(prev.isDisplayed());
        Assert.assertTrue(next.isDisplayed());
        Assert.assertTrue(page.isDisabled(prev));
    }

    @Test(priority = 12)
    public void PIS4_UI_TC_12_totalItemsAndRangeTextFormat() {
        String range = page.getRangeText();
        String total = page.norm(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.totalItemsText)).getText());

        Assert.assertTrue(range.contains("-") || range.contains("–"));
        Assert.assertTrue(total.replaceAll("\\D", "").length() > 0);
    }

    @Test(priority = 13)
    public void PIS4_UI_TC_13_openCategoryDropdownShowsOptions() {
        page.openDropdown(page.categoryDropdown);
        Assert.assertTrue(driver.findElements(page.dropdownOptions).size() > 0);
        page.closeDropdownByEscape();
    }

    @Test(priority = 14)
    public void PIS4_UI_TC_14_openSupplierDropdownShowsOptions() {
        page.openDropdown(page.supplierDropdown);
        Assert.assertTrue(driver.findElements(page.dropdownOptions).size() > 0);
        page.closeDropdownByEscape();
    }

    @Test(priority = 15)
    public void PIS4_UI_TC_15_emptyStateMessageRendersCorrectlyWhenNoResult() {
        page.setSearch("XYZ-NON-EXIST-12345");
        WebElement empty = driver.findElement(page.emptyMessageCell);
        Assert.assertTrue(empty.isDisplayed());
        Assert.assertEquals(page.norm(empty.getText()), "Không có sản phẩm nào");
    }

    // ================== SEARCH ==================
    @Test(priority = 16)
    public void PIS4_SEARCH_TC_01_searchByExistingCode_dynamic() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        String code = page.getFirstRowCodeOrNull();
        if (code == null) return;

        page.setSearch(code);

        if (page.isEmptyShown()) Assert.fail("Search theo mã có sẵn nhưng empty: " + code);

        List<WebElement> rows = driver.findElements(page.dataRows);
        Assert.assertFalse(rows.isEmpty());

        String expect = code.toLowerCase();
        for (WebElement r : rows) {
            String actual = page.getCellText(r, MedicineListPage.COL_CODE).toLowerCase();
            Assert.assertTrue(actual.contains(expect));
        }
    }

    @Test(priority = 17)
    public void PIS4_SEARCH_TC_02_searchCaseInsensitive_dynamic() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        String code = page.getFirstRowCodeOrNull();
        if (code == null) return;

        page.setSearch(code.toLowerCase());

        if (page.isEmptyShown()) Assert.fail("Search case-insensitive nhưng empty: " + code);

        List<WebElement> rows = driver.findElements(page.dataRows);
        Assert.assertFalse(rows.isEmpty());

        String expect = code.toLowerCase();
        for (WebElement r : rows) {
            String actual = page.getCellText(r, MedicineListPage.COL_CODE).toLowerCase();
            Assert.assertTrue(actual.contains(expect));
        }
    }

    @Test(priority = 18)
    public void PIS4_SEARCH_TC_03_searchByPartialCode_dynamic() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        String code = page.getFirstRowCodeOrNull();
        if (code == null) return;

        String kw = code.length() >= 4 ? code.substring(0, 4) : code;
        page.setSearch(kw);

        if (page.isEmptyShown()) Assert.fail("Search partial code mà empty: " + kw);

        List<WebElement> rows = driver.findElements(page.dataRows);
        Assert.assertFalse(rows.isEmpty());

        String kwLower = page.norm(kw).toLowerCase();
        for (WebElement r : rows) {
            String c = page.getCellText(r, MedicineListPage.COL_CODE).toLowerCase();
            String n = page.getCellText(r, MedicineListPage.COL_NAME).toLowerCase();
            Assert.assertTrue(c.contains(kwLower) || n.contains(kwLower));
        }
    }

    @Test(priority = 19)
    public void PIS4_SEARCH_TC_04_searchByName_dynamic() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        String name = page.getFirstRowNameOrNull();
        if (name == null || name.isEmpty()) return;

        String kw = page.pickKeywordFromName(name);
        if (kw == null || page.norm(kw).length() < 3) return;

        page.setSearch(kw);

        if (page.isEmptyShown()) Assert.fail("Search by name mà empty: " + kw);

        List<WebElement> rows = driver.findElements(page.dataRows);
        Assert.assertFalse(rows.isEmpty());

        String kwLower = page.norm(kw).toLowerCase();
        for (WebElement r : rows) {
            String c = page.getCellText(r, MedicineListPage.COL_CODE).toLowerCase();
            String n = page.getCellText(r, MedicineListPage.COL_NAME).toLowerCase();
            Assert.assertTrue(c.contains(kwLower) || n.contains(kwLower));
        }
    }

    @Test(priority = 20)
    public void PIS4_SEARCH_TC_05_searchTrimSpaces_dynamic() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        String code = page.getFirstRowCodeOrNull();
        if (code == null) return;

        String kw = code.length() >= 4 ? code.substring(0, 4) : code;
        page.setSearch("   " + kw + "   ");

        if (page.isEmptyShown()) Assert.fail("Search trim spaces mà empty: " + kw);

        List<WebElement> rows = driver.findElements(page.dataRows);
        Assert.assertFalse(rows.isEmpty());

        String kwLower = page.norm(kw).toLowerCase();
        for (WebElement r : rows) {
            String c = page.getCellText(r, MedicineListPage.COL_CODE).toLowerCase();
            String n = page.getCellText(r, MedicineListPage.COL_NAME).toLowerCase();
            Assert.assertTrue(c.contains(kwLower) || n.contains(kwLower));
        }
    }

    @Test(priority = 21)
    public void PIS4_SEARCH_TC_06_searchNoResult_showsEmptyState() {
        page.setSearch("XYZ-NON-EXIST-12345");
        Assert.assertTrue(page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.emptyMessageCell)).isDisplayed());
    }

    @Test(priority = 22)
    public void PIS4_SEARCH_TC_07_clearSearch_restoresDataOrValidEmpty() {
        page.setSearch("XYZ-NON-EXIST-12345");
        Assert.assertTrue(page.isEmptyShown());

        page.setSearch("");
        boolean hasRows = !driver.findElements(page.dataRows).isEmpty();
        boolean hasEmpty = page.isEmptyShown();
        Assert.assertTrue(hasRows || hasEmpty);
    }

    // ================== FILTER ==================
    @Test(priority = 23)
    public void PIS4_FILTER_TC_01_filterByCategory_dynamic() {
        String chosenCategory = page.selectFirstNonAllOption(page.categoryDropdown);
        page.waitForCategoryFilterApplied(chosenCategory);

        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            Assert.assertEquals(page.getCellText(r, MedicineListPage.COL_CATEGORY), chosenCategory);
        }
    }

    @Test(priority = 24)
    public void PIS4_FILTER_TC_02_filterBySupplier_dynamic() {
        String chosenSupplier = page.selectFirstNonAllOption(page.supplierDropdown);
        page.waitForSupplierFilterApplied(chosenSupplier);

        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            Assert.assertEquals(page.getCellText(r, MedicineListPage.COL_SUPPLIER), chosenSupplier);
        }
    }

    @Test(priority = 25)
    public void PIS4_FILTER_TC_03_filterByCategoryAndSupplier_dynamic() {
        String chosenCategory = page.selectFirstNonAllOption(page.categoryDropdown);
        String chosenSupplier = page.selectFirstNonAllOption(page.supplierDropdown);

        page.waitForCategoryAndSupplierApplied(chosenCategory, chosenSupplier);
        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            Assert.assertEquals(page.getCellText(r, MedicineListPage.COL_CATEGORY), chosenCategory);
            Assert.assertEquals(page.getCellText(r, MedicineListPage.COL_SUPPLIER), chosenSupplier);
        }
    }

    @Test(priority = 26)
    public void PIS4_FILTER_TC_04_filterWithSearch_dynamic() {
        String chosenSupplier = page.selectFirstNonAllOption(page.supplierDropdown);
        page.waitForSupplierFilterApplied(chosenSupplier);

        if (page.isEmptyShown()) return;

        WebElement first = page.getFirstDataRowOrNull();
        if (first == null) return;

        String baseName = page.getCellText(first, MedicineListPage.COL_NAME);
        String baseCode = page.getCellText(first, MedicineListPage.COL_CODE);

        String kw = page.pickKeywordFromName(baseName);
        if (kw == null || page.norm(kw).length() < 3) {
            kw = baseCode.length() >= 4 ? baseCode.substring(0, 4) : baseCode;
        }

        page.setSearch(kw);
        page.waitForSupplierAndSearchAppliedStable(chosenSupplier, kw);

        if (page.isEmptyShown()) return;

        String kwLower = page.norm(kw).toLowerCase();
        for (WebElement r : driver.findElements(page.dataRows)) {
            String code = page.getCellText(r, MedicineListPage.COL_CODE).toLowerCase();
            String name = page.getCellText(r, MedicineListPage.COL_NAME).toLowerCase();
            String supp = page.getCellText(r, MedicineListPage.COL_SUPPLIER);
            Assert.assertTrue(code.contains(kwLower) || name.contains(kwLower));
            Assert.assertEquals(supp, chosenSupplier);
        }
    }

    @Test(priority = 27)
    public void PIS4_FILTER_TC_05_clearAllResetsFilters() {
        String chosenSupplier = page.selectFirstNonAllOption(page.supplierDropdown);
        Assert.assertFalse(chosenSupplier.equalsIgnoreCase("Tất cả"));

        WebElement oldFirst = page.getFirstDataRowOrNull();
        page.safeClick(page.clearAllButton);

        page.getWait().until(d -> "Tất cả".equals(page.norm(d.findElement(page.supplierDropdown).getText())));
        page.waitTableChangeAfterAction(oldFirst);

        Assert.assertEquals(page.norm(driver.findElement(page.supplierDropdown).getText()), "Tất cả");
    }

    // ================== PAGING BASIC ==================
    @Test(priority = 28)
    public void PIS4_PAGING_TC_01_changeRowsPerPage50() {
        page.changeRowsPerPage("50");
        Assert.assertEquals(page.norm(driver.findElement(page.rowsPerPageValue).getText()), "50");
    }

    @Test(priority = 29)
    public void PIS4_PAGING_TC_02_changeRowsPerPage100() {
        page.changeRowsPerPage("100");
        Assert.assertEquals(page.norm(driver.findElement(page.rowsPerPageValue).getText()), "100");
    }

    @Test(priority = 30)
    public void PIS4_PAGING_TC_03_changeRowsPerPageBackTo25() {
        page.changeRowsPerPage("50");
        page.changeRowsPerPage("25");
        Assert.assertEquals(page.norm(driver.findElement(page.rowsPerPageValue).getText()), "25");
    }

    @Test(priority = 31)
    public void PIS4_PAGING_TC_04_navigateNextPage_ifAny() {
        int total = page.parseTotalItems();
        if (total <= 25) return;

        int start = page.getCurrentStartIndex();
        page.clickNextStable();
        Assert.assertTrue(page.getCurrentStartIndex() > start);
    }

    @Test(priority = 32)
    public void PIS4_PAGING_TC_05_navigatePreviousPage_ifAny() {
        int total = page.parseTotalItems();
        if (total <= 25) return;

        page.clickNextStable();
        page.clickPrevStable();
        Assert.assertEquals(page.getCurrentStartIndex(), 1);
    }

    @Test(priority = 33)
    public void PIS4_PAGING_TC_06_paginationWithSupplierFilter_persistsAcrossPages() {
        String chosenSupplier = page.selectFirstNonAllOption(page.supplierDropdown);
        page.waitForSupplierFilterApplied(chosenSupplier);

        int total = page.parseTotalItems();
        if (total <= 25) return;
        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            Assert.assertEquals(page.getCellText(r, MedicineListPage.COL_SUPPLIER), chosenSupplier);
        }

        page.clickNextStable();
        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            Assert.assertEquals(page.getCellText(r, MedicineListPage.COL_SUPPLIER), chosenSupplier);
        }
    }

    // ================== TABLE DATA & FORMAT ==================
    @Test(priority = 34)
    public void PIS4_TABLE_TC_01_eachRowHas7Cells_whenHasData() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            Assert.assertEquals(r.findElements(By.cssSelector("td")).size(), 7);
        }
    }

    @Test(priority = 35)
    public void PIS4_TABLE_TC_02_codeCell_notBlank_noSpaces_basicPattern() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        if (page.isEmptyShown()) return;

        String codeRegex = "^[A-Za-z0-9][A-Za-z0-9\\-_.\\/]*$";

        for (WebElement r : driver.findElements(page.dataRows)) {
            String code = page.getCellText(r, MedicineListPage.COL_CODE);
            Assert.assertFalse(code.isEmpty());
            Assert.assertFalse(code.contains(" "));
            Assert.assertTrue(code.matches(codeRegex), "Mã thuốc sai pattern: " + code);
        }
    }

    @Test(priority = 36)
    public void PIS4_TABLE_TC_03_nameCategorySupplier_notBlank() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            Assert.assertFalse(page.getCellText(r, MedicineListPage.COL_NAME).isEmpty());
            Assert.assertFalse(page.getCellText(r, MedicineListPage.COL_CATEGORY).isEmpty());
            Assert.assertFalse(page.getCellText(r, MedicineListPage.COL_SUPPLIER).isEmpty());
        }
    }

    @Test(priority = 37)
    public void PIS4_TABLE_TC_04_unit_notBlank_whenHasData() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            String unit = page.getCellText(r, MedicineListPage.COL_UNIT);
            Assert.assertFalse(unit.isEmpty());
        }
    }

    @Test(priority = 38)
    public void PIS4_TABLE_TC_05_description_valid_orDash() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            String desc = page.getCellText(r, MedicineListPage.COL_DESCRIPTION);
            Assert.assertFalse(desc.isEmpty());
        }
    }

    @Test(priority = 39)
    public void PIS4_TABLE_TC_06_expiryDate_format_orDash() {
        page.waitForTableRefresh();
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            String expiry = page.getCellText(r, MedicineListPage.COL_EXPIRY);
            page.assertExpiryFormatOrDash(expiry);
        }
    }

    @Test(priority = 40)
    public void PIS4_TABLE_TC_07_noCellHasLeadingTrailingSpaces_inVisibleRow() {
        WebElement first = page.getFirstDataRowOrNull();
        if (first == null) return;

        for (WebElement td : first.findElements(By.cssSelector("td"))) {
            String raw = td.getText();
            if (raw == null) continue;
            Assert.assertEquals(raw, raw.trim(), "Cell có space đầu/cuối: '" + raw + "'");
        }
    }

    // ================== PAGING ADV ==================
    @Test(priority = 41)
    public void PIS4_PAGING_ADV_TC_01_rowsPerPageDropdownHasExpectedOptions() {
        WebElement btn = page.getWait().until(ExpectedConditions.elementToBeClickable(page.rowsPerPageButton));
        try { btn.click(); } catch (Exception e) { ((JavascriptExecutor)driver).executeScript("arguments[0].click();", btn); }

        page.getWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(page.dropdownOptions));
        for (String v : page.allowedRowsPerPage()) {
            By opt = By.xpath("//div[@role='option']//span[normalize-space()='" + v + "']");
            Assert.assertTrue(driver.findElements(opt).size() > 0, "Thiếu option rows-per-page: " + v);
        }
        page.closeDropdownByEscape();
    }

    @Test(priority = 42)
    public void PIS4_PAGING_ADV_TC_02_rangeStartEndMustBeValid_andWithinTotal() {
        int total = page.parseTotalItems();
        MedicineListPage.Range r = page.parseRange(page.getRangeText());

        Assert.assertTrue(r.start >= 1 || total == 0);
        Assert.assertTrue(r.end >= r.start || total == 0);
        Assert.assertTrue(r.end <= total || total == 0);
    }

    @Test(priority = 43)
    public void PIS4_PAGING_ADV_TC_03_rowCountMustMatchRange_onCurrentPage_whenHasRows() {
        int total = page.parseTotalItems();
        if (total == 0 || page.isEmptyShown()) return;

        MedicineListPage.Range r = page.parseRange(page.getRangeText());
        int expectedCount = r.end - r.start + 1;

        int rows = driver.findElements(page.dataRows).size();
        Assert.assertEquals(rows, expectedCount);
    }

    @Test(priority = 44)
    public void PIS4_PAGING_ADV_TC_04_rowsCountRespectsRowsPerPage25() {
        page.ensureRowsPerPage("25");
        int total = page.parseTotalItems();
        if (total == 0 || page.isEmptyShown()) return;

        MedicineListPage.Range r = page.parseRange(page.getRangeText());
        int expected = Math.min(25, total - r.start + 1);

        int count = driver.findElements(page.dataRows).size();
        Assert.assertTrue(count <= 25);
        Assert.assertEquals(count, expected);
    }

    @Test(priority = 45)
    public void PIS4_PAGING_ADV_TC_05_rowsCountRespectsRowsPerPage50() {
        page.ensureRowsPerPage("50");
        int total = page.parseTotalItems();
        if (total == 0 || page.isEmptyShown()) return;

        MedicineListPage.Range r = page.parseRange(page.getRangeText());
        int expected = Math.min(50, total - r.start + 1);

        int count = driver.findElements(page.dataRows).size();
        Assert.assertTrue(count <= 50);
        Assert.assertEquals(count, expected);
    }

    @Test(priority = 46)
    public void PIS4_PAGING_ADV_TC_06_rowsCountRespectsRowsPerPage100() {
        page.ensureRowsPerPage("100");
        int total = page.parseTotalItems();
        if (total == 0 || page.isEmptyShown()) return;

        MedicineListPage.Range r = page.parseRange(page.getRangeText());
        int expected = Math.min(100, total - r.start + 1);

        int count = driver.findElements(page.dataRows).size();
        Assert.assertTrue(count <= 100);
        Assert.assertEquals(count, expected);
    }

    @Test(priority = 47)
    public void PIS4_PAGING_ADV_TC_07_changeRowsPerPageShouldResetToFirstPage() {
        int total = page.parseTotalItems();
        if (total <= 25) {
            page.ensureRowsPerPage("50");
            Assert.assertEquals(page.getCurrentStartIndex(), 1);
            return;
        }

        page.ensureRowsPerPage("25");
        page.clickNextStable();
        Assert.assertTrue(page.getCurrentStartIndex() > 1);

        page.ensureRowsPerPage("50");
        Assert.assertEquals(page.getCurrentStartIndex(), 1);

        WebElement prev = driver.findElement(page.previousButton);
        Assert.assertTrue(page.isDisabled(prev));
    }

    @Test(priority = 48)
    public void PIS4_PAGING_ADV_TC_08_nextDisabledOnLastPage_ifMultiplePages() {
        page.ensureRowsPerPage("25");
        int total = page.parseTotalItems();
        if (total <= 25) return;

        page.goToLastPageByRange(total);
        MedicineListPage.Range r = page.parseRange(page.getRangeText());
        Assert.assertEquals(r.end, total);

        WebElement next = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.nextButton));
        Assert.assertTrue(page.isTrulyDisabled(next));
    }

    @Test(priority = 49)
    public void PIS4_PAGING_ADV_TC_09_prevEnabledAfterMovingForward_andBackToFirstPrevDisabled() {
        page.ensureRowsPerPage("25");
        int total = page.parseTotalItems();
        if (total <= 25) return;

        page.getWait().until(d -> !d.findElements(page.prevDisabled).isEmpty());

        String range1 = page.getRangeText();
        page.clickNextReliable();
        String range2 = page.getRangeText();

        Assert.assertNotEquals(range2, range1);
        Assert.assertTrue(page.getCurrentStartIndex() > 1);

        page.getWait().until(d -> !d.findElements(page.prevEnabled).isEmpty());

        page.clickPrevReliable();
        Assert.assertEquals(page.getCurrentStartIndex(), 1);

        page.getWait().until(d -> !d.findElements(page.prevDisabled).isEmpty());
    }

    @Test(priority = 50)
    public void PIS4_PAGING_ADV_TC_10_rangeShouldAdvanceByPageSizeWhenNext_ifNotLast() {
        page.ensureRowsPerPage("25");
        int total = page.parseTotalItems();
        if (total <= 25) return;

        MedicineListPage.Range r1 = page.parseRange(page.getRangeText());
        page.clickNextStable();
        MedicineListPage.Range r2 = page.parseRange(page.getRangeText());

        Assert.assertEquals(r2.start, r1.end + 1);
        Assert.assertTrue(r2.end > r2.start);
        Assert.assertTrue(r2.end <= total);
    }

    // ================== A11Y & KEYBOARD ==================
    @Test(priority = 51)
    public void PIS4_A11Y_TC_01_singleH1Exists() {
        List<WebElement> h1s = driver.findElements(By.tagName("h1"));
        Assert.assertTrue(h1s.size() >= 1);
        Assert.assertEquals(h1s.size(), 1);
        Assert.assertEquals(page.norm(h1s.get(0).getText()), "Danh mục thuốc");
    }

    @Test(priority = 52)
    public void PIS4_A11Y_TC_02_searchInputHasAccessibleName() {
        WebElement input = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.searchInput));
        page.assertHasAccessibleName(input, "Search input");
    }

    @Test(priority = 53)
    public void PIS4_A11Y_TC_03_mainControlsHaveAccessibleName() {
        page.assertHasAccessibleName(driver.findElement(page.categoryDropdown), "Category dropdown trigger");
        page.assertHasAccessibleName(driver.findElement(page.supplierDropdown), "Supplier dropdown trigger");
        page.assertHasAccessibleName(driver.findElement(page.clearAllButton), "Clear all button");
        page.assertHasAccessibleName(driver.findElement(page.rowsPerPageButton), "Rows-per-page button");
        page.assertHasAccessibleName(driver.findElement(page.previousButton), "Previous button");
        page.assertHasAccessibleName(driver.findElement(page.nextButton), "Next button");
    }

    @Test(priority = 54)
    public void PIS4_A11Y_TC_04_disabledButtonsShouldExposeDisabledState() {
        WebElement prev = page.getWait().until(ExpectedConditions.visibilityOfElementLocated(page.previousButton));
        boolean hasDisabledAttr = prev.getAttribute("disabled") != null;
        boolean ariaDisabled = "true".equalsIgnoreCase(prev.getAttribute("aria-disabled"));
        boolean actuallyDisabled = page.isDisabled(prev);

        if (actuallyDisabled) {
            Assert.assertTrue(hasDisabledAttr || ariaDisabled);
        }
    }

    @Test(priority = 55)
    public void PIS4_KEY_TC_01_openCloseCategoryDropdownByKeyboard() {
        page.openDropdownByKeyboard(page.categoryDropdown);
        Assert.assertTrue(driver.findElements(page.dropdownOptions).size() > 0);
        page.closeDropdownByEscape();
    }

    @Test(priority = 56)
    public void PIS4_KEY_TC_02_openCloseSupplierDropdownByKeyboard() {
        page.openDropdownByKeyboard(page.supplierDropdown);
        Assert.assertTrue(driver.findElements(page.dropdownOptions).size() > 0);
        page.closeDropdownByEscape();
    }

    @Test(priority = 57)
    public void PIS4_KEY_TC_03_selectCategoryOptionByKeyboard_appliesFilter() {
        String chosenCategory = page.selectFirstNonAllOptionByKeyboard(page.categoryDropdown);
        page.waitForCategoryFilterApplied(chosenCategory);

        if (page.isEmptyShown()) return;

        for (WebElement r : driver.findElements(page.dataRows)) {
            Assert.assertEquals(page.getCellText(r, MedicineListPage.COL_CATEGORY), chosenCategory);
        }
    }

    @Test(priority = 58)
    public void PIS4_KEY_TC_04_paginationNextByKeyboard_enter() {
        page.ensureRowsPerPage("25");
        int total = page.parseTotalItems();
        if (total <= 25) return;

        String oldRange = page.getRangeText();
        WebElement next = page.findDisplayed(page.nextButton);
        page.focusByJS(next);
        page.sendKeyToActive(Keys.ENTER);

        page.waitForRangeChange(oldRange);
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        Assert.assertNotEquals(page.getRangeText(), oldRange);
    }

    @Test(priority = 59)
    public void PIS4_KEY_TC_05_rowsPerPageChangeByKeyboard_to50() {
        WebElement btn = page.findDisplayed(page.rowsPerPageButton);
        page.focusByJS(btn);

        page.sendKeyToActive(Keys.ENTER);
        if (driver.findElements(page.dropdownOptions).isEmpty()) page.sendKeyToActive(Keys.SPACE);

        By opt50 = By.xpath("//div[@role='option'][normalize-space(.)='50' or .//span[normalize-space(.)='50'] or contains(normalize-space(.),'50')]");
        page.getWait().until(ExpectedConditions.visibilityOfElementLocated(opt50));
        page.getWait().until(d -> d.findElements(page.dropdownOptions).size() >= 3);

        int guard = 30;
        while (guard-- > 0) {
            WebElement fifty = driver.findElement(opt50);
            String ariaSel = fifty.getAttribute("aria-selected");
            String dataState = fifty.getAttribute("data-state");

            boolean isSelected = "true".equalsIgnoreCase(ariaSel) || "checked".equalsIgnoreCase(dataState);

            String activeText = "";
            try { activeText = page.norm(driver.switchTo().activeElement().getText()); } catch (Exception ignored) {}

            if (isSelected || activeText.equals("50") || activeText.contains("50")) {
                page.sendKeyToActive(Keys.ENTER);
                break;
            }
            page.sendKeyToActive(Keys.ARROW_DOWN);
            try { Thread.sleep(60); } catch (InterruptedException ignored) {}
        }

        page.getWait().until(ExpectedConditions.textToBe(page.rowsPerPageValue, "50"));
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        Assert.assertEquals(page.norm(driver.findElement(page.rowsPerPageValue).getText()), "50");
    }

    @Test(priority = 60)
    public void PIS4_KEY_TC_06_clearAllActivatedBySpace() {
        String chosenSupplier = page.selectFirstNonAllOption(page.supplierDropdown);
        Assert.assertFalse(chosenSupplier.equalsIgnoreCase("Tất cả"));

        WebElement clearBtn = page.findDisplayed(page.clearAllButton);
        page.focusByJS(clearBtn);
        page.sendKeyToActive(Keys.SPACE);

        page.getWait().until(d -> "Tất cả".equals(page.norm(d.findElement(page.supplierDropdown).getText())));
        page.waitForTableStable(MedicineListPage.TABLE_STABLE_MS, MedicineListPage.TABLE_TIMEOUT_MS);

        Assert.assertEquals(page.norm(driver.findElement(page.supplierDropdown).getText()), "Tất cả");
    }
}
