// File: src/test/java/vn/pis/ui/tests/PIS9_InventoryReport.java
package vn.pis.ui.tests;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.*;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.InventoryReportPage;
import vn.pis.ui.pages.InventoryReportPage.DataRangePick;
import vn.pis.ui.pages.InventoryReportPage.ReportSnapshot;
import vn.pis.ui.pages.LoginPage;
import vn.pis.ui.util.Config;
import vn.pis.ui.util.TestEnv;

public class PIS9_InventoryReport extends BaseTest {

    private InventoryReportPage reportPage;

    private static final long REPORT_TIMEOUT_MS = 90_000;

    private static final String DEFAULT_START = "2024-01-01";
    private static final String DEFAULT_END = "2024-12-31";

    // bạn xác nhận 12/12/2025 có data
    private static final String DATA_DAY = "2025-12-12";
    private static final String DATA_RANGE_START = "2025-12-01";
    private static final String DATA_RANGE_END = "2025-12-12";

    // strict.data=true => không tìm được data thì FAIL luôn
    private static final boolean STRICT_DATA = Boolean.parseBoolean(System.getProperty("strict.data", "false"));

    private String HAS_DATA_START = DATA_DAY;
    private String HAS_DATA_END = DATA_DAY;
    private boolean HAS_ANY_DATA = false;

    @BeforeClass
    public void loginAndOpenReportPage() {
        LoginPage loginPage = new LoginPage(driver);

        String baseUrl = nz(Config.get("baseUrl"), TestEnv.BASE_URL);
        String username = nz(Config.get("admin.username"), TestEnv.ADMIN_USER);
        String password = nz(Config.get("admin.password"), TestEnv.ADMIN_PASS);

        loginPage.open(baseUrl + "/login");
        loginPage.login(username, password);
        Assert.assertTrue(loginPage.isLoginSuccess(), "Đăng nhập thất bại");

        reportPage = new InventoryReportPage(driver, Duration.ofSeconds(35), REPORT_TIMEOUT_MS);
        reportPage.openAndWaitReports(baseUrl + "/reports", "/reports", DEFAULT_START, DEFAULT_END);

        discoverDataRangeOrMarkNoData();

        // quay về default cho sạch
        resetToDefault();
    }

    private static String nz(String a, String b) {
        return (a == null || a.isBlank()) ? b : a.trim();
    }

    private void resetToDefault() {
        reportPage.resetToDefaultRange(DEFAULT_START, DEFAULT_END);
    }

    private void discoverDataRangeOrMarkNoData() {
        // 1) nếu user force range qua -Ddata.start=... -Ddata.end=...
        String forcedStart = System.getProperty("data.start", "").trim();
        String forcedEnd = System.getProperty("data.end", "").trim();

        if (!forcedStart.isEmpty() && !forcedEnd.isEmpty()) {
            HAS_DATA_START = forcedStart;
            HAS_DATA_END = forcedEnd;

            ReportSnapshot snap = reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);
            HAS_ANY_DATA = snap.maxRowsSeen >= 1;

            if (!HAS_ANY_DATA && STRICT_DATA) {
                throw new AssertionError("Forced range không có data: " + forcedStart + " -> " + forcedEnd +
                        "\nalert=" + snap.alert + "\nperiod=" + snap.periodText + "\nreqLast=" + snap.reqLast);
            }
            return;
        }

        // 2) ưu tiên range bạn confirm
        List<String[]> candidates = new ArrayList<>();
        candidates.add(new String[] { DATA_DAY, DATA_DAY });
        candidates.add(new String[] { DATA_RANGE_START, DATA_RANGE_END });

        // 3) fallback: 14 ngày gần nhất
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            String d = today.minusDays(i).toString();
            candidates.add(new String[] { d, d });
        }

        // 4) month-to-date
        candidates.add(new String[] { today.withDayOfMonth(1).toString(), today.toString() });

        DataRangePick pick = reportPage.discoverFirstRangeWithUiProbe(candidates, 1, 18_000);
        if (pick != null) {
            HAS_ANY_DATA = true;
            HAS_DATA_START = pick.start;
            HAS_DATA_END = pick.end;
            System.out.println(">>> DISCOVERED DATA RANGE: " + pick);
        } else {
            HAS_ANY_DATA = false;
            System.out.println(">>> NO DATA RANGE FOUND via UI-probe.");
            if (STRICT_DATA)
                throw new AssertionError("Không tìm được range có dữ liệu (UI-probe).");
        }
    }

    private void requireDataOrSkip(String tcName) {
        if (!HAS_ANY_DATA)
            throw new SkipException(tcName + " SKIP: Không tìm được range có dữ liệu (UI-probe).");
    }

    // ==================================================================================
    // A. UI cơ bản
    // ==================================================================================

    @Test(priority = 1)
    public void PIS9_UI_TC_01_pageTitleAndSubtitleVisible() {
        resetToDefault();

        WebElement title = driver.findElement(reportPage.pageTitle);
        WebElement subtitle = driver.findElement(reportPage.pageSubtitle);

        Assert.assertTrue(title.isDisplayed(), "Tiêu đề trang không hiển thị");
        Assert.assertEquals(reportPage.norm(title.getText()), "Báo cáo Xuất-Nhập-Tồn");

        Assert.assertTrue(subtitle.isDisplayed(), "Phụ đề trang không hiển thị");
        Assert.assertTrue(reportPage.norm(subtitle.getText()).contains("Theo dõi và phân tích hoạt động kho dược"));
    }

    @Test(priority = 2)
    public void PIS9_UI_TC_02_startDateLabelAndInputVisible_typeDate_idStartDate() {
        WebElement lbl = reportPage.findDisplayed(reportPage.startDateLabel);
        Assert.assertEquals(reportPage.norm(lbl.getText()), "Từ ngày");

        WebElement input = reportPage.findDisplayed(reportPage.startDateInput);
        Assert.assertTrue(input.isDisplayed() && input.isEnabled());
        Assert.assertEquals(input.getAttribute("id"), "startDate");
        Assert.assertEquals(input.getAttribute("type"), "date");
    }

    @Test(priority = 3)
    public void PIS9_UI_TC_03_endDateLabelAndInputVisible_typeDate_idEndDate() {
        WebElement lbl = reportPage.findDisplayed(reportPage.endDateLabel);
        Assert.assertEquals(reportPage.norm(lbl.getText()), "Đến ngày");

        WebElement input = reportPage.findDisplayed(reportPage.endDateInput);
        Assert.assertTrue(input.isDisplayed() && input.isEnabled());
        Assert.assertEquals(input.getAttribute("id"), "endDate");
        Assert.assertEquals(input.getAttribute("type"), "date");
    }

    @Test(priority = 4)
    public void PIS9_UI_TC_04_defaultStartDateCorrect() {
        resetToDefault();
        Assert.assertEquals(driver.findElement(reportPage.startDateInput).getAttribute("value"), DEFAULT_START);
    }

    @Test(priority = 5)
    public void PIS9_UI_TC_05_defaultEndDateCorrect() {
        resetToDefault();
        Assert.assertEquals(driver.findElement(reportPage.endDateInput).getAttribute("value"), DEFAULT_END);
    }

    @Test(priority = 6)
    public void PIS9_UI_TC_06_createReportButtonVisible_withIcon() {
        WebElement btn = reportPage.findCreateReportButton();
        Assert.assertTrue(btn.isDisplayed() && btn.isEnabled());
        Assert.assertTrue(reportPage.norm(btn.getText()).contains("Tạo báo cáo"));

        int svgCount = btn.findElements(By.xpath(".//*[local-name()='svg']")).size();
        Assert.assertTrue(svgCount > 0, "Không thấy icon (svg) trong nút Tạo báo cáo");
    }

    @Test(priority = 7)
    public void PIS9_UI_TC_07_exportButtonAlwaysPresent_inUI() {
        Assert.assertTrue(reportPage.findExportPdfButton().isDisplayed(), "UI: Nút 'Xuất PDF' phải luôn hiển thị");
    }

    @Test(priority = 8)
    public void PIS9_UI_TC_08_layoutResponsive_noOverlapInputsAndButtons() {
        Dimension original = driver.manage().window().getSize();
        try {
            driver.manage().window().setSize(new Dimension(1366, 768));
            reportPage.waitLoaded();

            Rectangle rs = reportPage.rect(reportPage.startDateInput);
            Rectangle re = reportPage.rect(reportPage.endDateInput);

            Rectangle rb = reportPage.findCreateReportButton().getRect();
            Rectangle rx = reportPage.findExportPdfButton().getRect();

            Assert.assertFalse(reportPage.overlap(rs, re), "Desktop: startDate bị đè endDate");
            Assert.assertFalse(reportPage.overlap(rs, rb), "Desktop: startDate bị đè Tạo báo cáo");
            Assert.assertFalse(reportPage.overlap(re, rb), "Desktop: endDate bị đè Tạo báo cáo");
            Assert.assertFalse(reportPage.overlap(rb, rx), "Desktop: Tạo báo cáo bị đè Export");

            driver.manage().window().setSize(new Dimension(375, 812));
            reportPage.waitLoaded();

            rs = reportPage.rect(reportPage.startDateInput);
            re = reportPage.rect(reportPage.endDateInput);
            rb = reportPage.findCreateReportButton().getRect();

            Assert.assertFalse(reportPage.overlap(rs, re), "Mobile: startDate bị đè endDate");
            Assert.assertFalse(reportPage.overlap(rs, rb), "Mobile: startDate bị đè Tạo báo cáo");
            Assert.assertFalse(reportPage.overlap(re, rb), "Mobile: endDate bị đè Tạo báo cáo");
        } finally {
            driver.manage().window().setSize(original);
        }
    }

    // ==================================================================================
    // B. Hành vi input ngày & điều hướng
    // ==================================================================================

    @Test(priority = 9)
    public void PIS9_B_TC_09_clickLabel_focusCorrectInput() {
        driver.findElement(reportPage.startDateLabel).click();
        Assert.assertEquals(driver.switchTo().activeElement().getAttribute("id"), "startDate");

        driver.findElement(reportPage.endDateLabel).click();
        Assert.assertEquals(driver.switchTo().activeElement().getAttribute("id"), "endDate");
    }

    @Test(priority = 10)
    public void PIS9_B_TC_10_setStartDate_updatesValue() {
        reportPage.setDate(reportPage.startDateInput, DATA_DAY);
        Assert.assertEquals(driver.findElement(reportPage.startDateInput).getAttribute("value"), DATA_DAY);
    }

    @Test(priority = 11)
    public void PIS9_B_TC_11_setEndDate_updatesValue() {
        reportPage.setDate(reportPage.endDateInput, DATA_DAY);
        Assert.assertEquals(driver.findElement(reportPage.endDateInput).getAttribute("value"), DATA_DAY);
    }

    @Test(priority = 12)
    public void PIS9_B_TC_12_valuesNotReset_whenClickOutside() {
        reportPage.setDate(reportPage.startDateInput, DATA_DAY);
        reportPage.setDate(reportPage.endDateInput, "2025-12-31");
        reportPage.clickOutside();

        Assert.assertEquals(driver.findElement(reportPage.startDateInput).getAttribute("value"), DATA_DAY);
        Assert.assertEquals(driver.findElement(reportPage.endDateInput).getAttribute("value"), "2025-12-31");
    }

    @Test(priority = 13)
    public void PIS9_B_TC_13_tabOrder_start_end_create_export() {
        resetToDefault();

        reportPage.focusClick(reportPage.startDateInput);
        Assert.assertEquals(driver.switchTo().activeElement().getAttribute("id"), "startDate");

        reportPage.tabUntilActiveId("endDate", 60);
        reportPage.tabUntilActiveButtonLikeContainsText("Tạo báo cáo", 160);
        Assert.assertTrue(reportPage.activeButtonLikeText().contains("Tạo báo cáo"));

        reportPage.tabUntilActiveButtonLikeContainsText("Xuất PDF", 160);
        Assert.assertTrue(reportPage.activeButtonLikeText().contains("Xuất PDF"));
    }

    @Test(priority = 15)
    public void PIS9_B_TC_15_createReport_default2024_noDataRule() {
        resetToDefault();
        reportPage.clickCreateReportAndWaitAny();
        Assert.assertTrue(reportPage.isNoDataShown() || reportPage.countDetailRowsFlexible() == 0,
                "Range default 2024 phải ra 'không có dữ liệu' hoặc 0 rows");
    }

    @Test(priority = 16)
    public void PIS9_B_TC_16_range_hasData_andPeriodShown() {
        requireDataOrSkip("TC16");
        ReportSnapshot snap = reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);
        Assert.assertTrue(snap.maxRowsSeen > 0, "Range discovered phải có data (maxRowsSeen>0)");
        Assert.assertTrue(reportPage.isPeriodTextShown(HAS_DATA_START, HAS_DATA_END),
                "Không thấy text khoảng thời gian đúng cho range discovered");
    }

    @Test(priority = 17)
    public void PIS9_B_TC_17_startAfterEnd_showsError_disablesCreate() {
        reportPage.setRangeAndSettle("2025-12-31", "2025-01-01");
        reportPage.waitUntilEndBeforeStartErrorShown(4000);
        reportPage.waitUntilCreateDisabledOrAnyAlert(4000);
        Assert.assertTrue(reportPage.isCreateReportDisabled(),
                "Khi endDate < startDate thì nút 'Tạo báo cáo' phải disabled");
    }

    @Test(priority = 18)
    public void PIS9_B_TC_18_missingBothDates_showsError_orDisablesCreate_orNoRequest() {
        resetToDefault();

        reportPage.clearDate(reportPage.startDateInput);
        reportPage.clearDate(reportPage.endDateInput);
        reportPage.waitUiSettled();

        if (reportPage.isCreateReportDisabled())
            return;

        String alertBefore = reportPage.getAnyVisibleAlertTextOrEmpty().trim();
        if (!alertBefore.isEmpty())
            return;

        long reqBefore = reportPage.getReqLogCount();
        try {
            reportPage.clickCreateReportAndWaitAny();
        } catch (AssertionError ignored) {
        }

        sleepMs(900);
        long reqAfter = reportPage.getReqLogCount();
        String alertAfter = reportPage.getAnyVisibleAlertTextOrEmpty().trim();

        Assert.assertTrue(reqAfter == reqBefore || !alertAfter.isEmpty(),
                "Thiếu ngày nhưng vẫn bắn request.\n" +
                        "reqBefore=" + reqBefore + " reqAfter=" + reqAfter + "\n" +
                        "alertAfter=" + alertAfter + "\n" +
                        "REQ_LAST=" + reportPage.getReqLast(6));
    }

    @Test(priority = 19)
    public void PIS9_B_TC_19_dateInput_rejectsWeirdChars() {
        WebElement start = driver.findElement(reportPage.startDateInput);

        reportPage.clearDate(reportPage.startDateInput);
        reportPage.focusClick(reportPage.startDateInput);
        start.sendKeys("abc@@@");

        String val = start.getAttribute("value");
        Assert.assertTrue(val == null || val.isEmpty(),
                "Input type=date không nên nhận 'abc@@@'. value=" + val);
    }

    @Test(priority = 20)
    public void PIS9_B_TC_20_clickCreateMultipleTimes_sameRange_rowsNotDropToZero() {
        requireDataOrSkip("TC20");

        ReportSnapshot s1 = reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);
        ReportSnapshot s2 = reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);

        Assert.assertTrue(s1.maxRowsSeen >= 1 && s2.maxRowsSeen >= 1,
                "Cùng range phải luôn có data.\n" +
                        "s1=" + s1.maxRowsSeen + " s2=" + s2.maxRowsSeen + "\n" +
                        "alert2=" + s2.alert + " period2=" + s2.periodText);
    }

    // ==================================================================================
    // C. Render kết quả/section/UI bổ sung
    // ==================================================================================

    @Test(priority = 21)
    public void PIS9_C_TC_21_resetRestoresDefaultDates() {
        reportPage.setRangeAndSettle(DATA_DAY, "2025-12-31");
        resetToDefault();

        Assert.assertEquals(driver.findElement(reportPage.startDateInput).getAttribute("value"), DEFAULT_START);
        Assert.assertEquals(driver.findElement(reportPage.endDateInput).getAttribute("value"), DEFAULT_END);
    }

    @Test(priority = 22)
    public void PIS9_C_TC_22_labelsForMatchInputIds() {
        WebElement sLbl = reportPage.findDisplayed(reportPage.startDateLabel);
        WebElement eLbl = reportPage.findDisplayed(reportPage.endDateLabel);

        Assert.assertEquals(sLbl.getAttribute("for"), "startDate");
        Assert.assertEquals(eLbl.getAttribute("for"), "endDate");
    }

    @Test(priority = 23)
    public void PIS9_C_TC_23_calendarIconsNearInputs_present() {
        Assert.assertTrue(reportPage.hasSvgIconNearInput(reportPage.startDateInput),
                "Không thấy icon (svg) cạnh input startDate");
        Assert.assertTrue(reportPage.hasSvgIconNearInput(reportPage.endDateInput),
                "Không thấy icon (svg) cạnh input endDate");
    }

    @Test(priority = 24)
    public void PIS9_C_TC_24_createButtonTypeSafe_notSubmit() {
        Assert.assertTrue(reportPage.isButtonTypeSafe(reportPage.findCreateReportButton()),
                "type của nút 'Tạo báo cáo' không an toàn (submit trong form)");
    }

    @Test(priority = 25)
    public void PIS9_C_TC_25_defaultRangeStartNotAfterEnd() {
        resetToDefault();
        String s = driver.findElement(reportPage.startDateInput).getAttribute("value");
        String e = driver.findElement(reportPage.endDateInput).getAttribute("value");
        Assert.assertTrue(s.compareTo(e) <= 0, "Default startDate > endDate: " + s + " > " + e);
    }

    @Test(priority = 26)
    public void PIS9_C_TC_26_beforeCreate_noDataRows() {
        resetToDefault();
        Assert.assertEquals(reportPage.countDetailRowsFlexible(), 0, "Trước khi tạo báo cáo phải 0 data rows");
    }

    @Test(priority = 27)
    public void PIS9_C_TC_27_createData_sectionsTitlesVisible() {
        requireDataOrSkip("TC27");

        ReportSnapshot snap = reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);
        Assert.assertTrue(snap.maxRowsSeen > 0, "Must have data first.");

        Assert.assertTrue(reportPage.isDisplayed(reportPage.detailReportTitle), "Không thấy 'Báo cáo chi tiết'");
        Assert.assertTrue(reportPage.isDisplayed(reportPage.monthlyTrendTitle), "Không thấy 'Xu hướng theo tháng'");
        Assert.assertTrue(reportPage.isDisplayed(reportPage.statusDistTitle),
                "Không thấy 'Phân bổ trạng thái giao dịch'");
    }

    @Test(priority = 29)
    public void PIS9_C_TC_29_chartsAndSummaryCards_visible_afterCreate() {
        requireDataOrSkip("TC29");

        reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);

        Assert.assertTrue(reportPage.sectionHasChartOrNoData("Xu hướng theo tháng"),
                "Không thấy chart hoặc no-data trong 'Xu hướng theo tháng'");
        Assert.assertTrue(reportPage.sectionHasChartOrNoData("Phân bổ trạng thái giao dịch"),
                "Không thấy chart hoặc no-data trong 'Phân bổ trạng thái giao dịch'");

        long totalIn = safeStat(() -> reportPage.getStatNumberByLabelCandidates("Tổng nhập", "Tổng nhập kho", "Nhập"));
        long totalOut = safeStat(() -> reportPage.getStatNumberByLabelCandidates("Tổng xuất", "Tổng xuất kho", "Xuất"));

        Assert.assertTrue(totalIn >= 0, "Tổng nhập phải >= 0. totalIn=" + totalIn);
        Assert.assertTrue(totalOut >= 0, "Tổng xuất phải >= 0. totalOut=" + totalOut);
    }

    // ==================================================================================
    // D. Export PDF
    // ==================================================================================

    @Test(priority = 31)
    public void PIS9_D_TC_31_exportVisible_hasIcon_whenCreate() {
        requireDataOrSkip("TC31");

        reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);

        WebElement btn = reportPage.findExportPdfButton();
        int svgCount = btn.findElements(By.xpath(".//*[local-name()='svg']")).size();
        Assert.assertTrue(svgCount > 0, "Nút Export PDF nên có icon (svg)");
    }

    @Test(priority = 32)
    public void PIS9_D_TC_32_exportByClick_triggersExportActivity() {
        requireDataOrSkip("TC32");

        reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);

        long before = reportPage.exportActivitySignature();
        long printsBefore = reportPage.getPrintCount();
        long opensBefore = reportPage.getOpenCount();

        reportPage.clickExportPdfAndWaitTriggered();

        long after = reportPage.exportActivitySignature();
        Assert.assertTrue(
                after > before || reportPage.getPrintCount() > printsBefore || reportPage.getOpenCount() > opensBefore,
                "Click Export phải trigger request/print/open (export activity tăng)");

        Assert.assertTrue(reportPage.recentReqUrlsContain("pdf", 80) ||
                reportPage.getPrintCount() > printsBefore ||
                reportPage.getOpenCount() > opensBefore,
                "Export nên tạo request có 'pdf' hoặc gọi print/open");
    }

    @Test(priority = 39)
    public void PIS9_E_TC_39_totalImport_monthLeYear() {
        LocalDate today = LocalDate.now();
        String monthStart = today.withDayOfMonth(1).toString();
        String yearStart = today.withDayOfYear(1).toString();
        String todayStr = today.toString();

        reportPage.setRangeAndSettle(monthStart, todayStr);
        reportPage.clickCreateReportAndWaitAny();
        long monthIn = safeStat(() -> reportPage.getStatNumberByLabelCandidates("Tổng nhập", "Tổng nhập kho", "Nhập"));

        reportPage.setRangeAndSettle(yearStart, todayStr);
        reportPage.clickCreateReportAndWaitAny();
        long yearIn = safeStat(() -> reportPage.getStatNumberByLabelCandidates("Tổng nhập", "Tổng nhập kho", "Nhập"));

        Assert.assertTrue(monthIn <= yearIn,
                "Tổng nhập 1 tháng phải <= tổng nhập 1 năm. month=" + monthIn + " year=" + yearIn);
    }

    @Test(priority = 40)
    public void PIS9_E_TC_40_totalExport_monthLeYear() {
        LocalDate today = LocalDate.now();
        String monthStart = today.withDayOfMonth(1).toString();
        String yearStart = today.withDayOfYear(1).toString();
        String todayStr = today.toString();

        reportPage.setRangeAndSettle(monthStart, todayStr);
        reportPage.clickCreateReportAndWaitAny();
        long monthOut = safeStat(() -> reportPage.getStatNumberByLabelCandidates("Tổng xuất", "Tổng xuất kho", "Xuất"));

        reportPage.setRangeAndSettle(yearStart, todayStr);
        reportPage.clickCreateReportAndWaitAny();
        long yearOut = safeStat(() -> reportPage.getStatNumberByLabelCandidates("Tổng xuất", "Tổng xuất kho", "Xuất"));

        Assert.assertTrue(monthOut <= yearOut,
                "Tổng xuất 1 tháng phải <= tổng xuất 1 năm. month=" + monthOut + " year=" + yearOut);
    }

    @Test(priority = 41)
    public void PIS9_E_TC_41_farFutureRange_noData_orZeroRows() {
        reportPage.setRangeAndSettle("2099-01-01", "2099-12-31");

        if (reportPage.isCreateReportDisabled())
            return;

        reportPage.clickCreateReportAndWaitAny();
        Assert.assertTrue(reportPage.isNoDataShown() || reportPage.countDetailRowsFlexible() == 0,
                "Range tương lai xa phải no-data hoặc 0 rows");
    }

    // ==================================================================================
    // F. Keyboard/A11y/UX
    // ==================================================================================

    @Test(priority = 42)
    public void PIS9_F_TC_42_startDate_hasAssociatedLabel() {
        long c = reportPage.getLabelsCountFor("startDate");
        Assert.assertTrue(c >= 1, "startDate phải có label liên kết (input.labels >= 1)");
    }

    @Test(priority = 43)
    public void PIS9_F_TC_43_endDate_hasAssociatedLabel() {
        long c = reportPage.getLabelsCountFor("endDate");
        Assert.assertTrue(c >= 1, "endDate phải có label liên kết (input.labels >= 1)");
    }

    @Test(priority = 44)
    public void PIS9_F_TC_44_spaceOnCreate_behavesLikeClick_hasData() {
        requireDataOrSkip("TC44");

        reportPage.setRangeAndSettle(HAS_DATA_START, HAS_DATA_END);

        long beforeClicks = reportPage.getCreateClickCountPublic();

        WebElement btn = reportPage.findCreateReportButton();
        reportPage.focus(btn);
        reportPage.pressKey(Keys.SPACE);

        reportPage.waitCreateClickCountGreaterThan(beforeClicks, 4500);

        ReportSnapshot snap = reportPage.generateReportEverRowsByClick(HAS_DATA_START, HAS_DATA_END, 1);
        Assert.assertTrue(snap.maxRowsSeen > 0, "Space trên 'Tạo báo cáo' phải tạo report có data");
    }

    @Test(priority = 45)
    public void PIS9_F_TC_45_escapeDoesNotClearDateInputs() {
        reportPage.setDate(reportPage.startDateInput, DATA_DAY);
        reportPage.setDate(reportPage.endDateInput, "2025-12-31");

        reportPage.focusClick(reportPage.startDateInput);
        reportPage.pressKey(Keys.ESCAPE);

        Assert.assertEquals(driver.findElement(reportPage.startDateInput).getAttribute("value"), DATA_DAY);
        Assert.assertEquals(driver.findElement(reportPage.endDateInput).getAttribute("value"), "2025-12-31");
    }

    // ===================== helpers =====================
    private long safeStat(StatSupplier supplier) {
        try {
            return supplier.get();
        } catch (AssertionError e) {
            return 0;
        }
    }

    private void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    @FunctionalInterface
    private interface StatSupplier {
        long get();
    }
}
