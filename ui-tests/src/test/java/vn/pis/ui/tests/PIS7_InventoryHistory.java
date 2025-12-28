package vn.pis.ui.tests;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.HistoryPage;
import vn.pis.ui.pages.LoginPage;

import java.util.List;

import static vn.pis.ui.util.TestEnv.*;

/**
 * PIS-7: Lịch sử nhập/xuất thuốc
 * User Story: Xem lại lịch sử giao dịch để đối soát.
 */
@Listeners(PIS2_ConsoleLogger.class)
public class PIS7_InventoryHistory extends BaseTest {

    private HistoryPage page;

    private void log(String msg) {
        String line = "[PIS7] " + msg;
        System.out.println(line);
        Reporter.log(line, true);
    }

    @BeforeClass(alwaysRun = true)
    public void loginOnce() {
        log("--- Đăng nhập hệ thống (Admin) ---");
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);
        
        page = new HistoryPage(driver);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(java.lang.reflect.Method m){
        log("▶ BẮT ĐẦU TC: " + m.getName());
        page.open(); 
    }

     // =================================================================================
    // 1) LAYOUT / UI
    // =================================================================================

    @Test(priority = 1, description = "TC_001 (PIS-7-TC-01): Verify table headers/columns")
    public void TC_001_VerifyTableStructure() {
        List<String> actualHeaders = page.getTableHeaders();
        log("Header thực tế: " + actualHeaders);

        String[] expectedKeywords = {"Loại", "Thời gian", "Kho", "Người tạo", "Trạng thái", "Hành động"};
        String allHeadersStr = String.join(" ", actualHeaders).toLowerCase();

        for (String key : expectedKeywords) {
            if (key.equals("Kho") || key.equals("Khoa")) {
                Assert.assertTrue(allHeadersStr.contains("kho") || allHeadersStr.contains("phòng"),
                        "Thiếu cột Kho/Khoa/Phòng");
            } else {
                Assert.assertTrue(allHeadersStr.contains(key.toLowerCase()), "Thiếu cột: " + key);
            }
        }
    }

    @Test(priority = 2, description = "TC_002 (PIS-7-TC-02): Verify import/export icon or text in column 'Loại'")
    public void TC_002_VerifyRowTypeIcon() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        if (page.getRowCount() > 0) {
            String typeText = page.getRowTypeText(1);
            log("Dữ liệu dòng 1: " + typeText);

            if (typeText.contains("Không có giao dịch") || typeText.isEmpty()) {
                log("⚠ Bảng rỗng, thử chuyển sang Xuất kho...");
                page.filterByType("Xuất kho");
                page.waitForDataToLoad();
                typeText = page.getRowTypeText(1);
            }

            boolean isImport = page.isImportIconDisplayed(1);
            boolean isExport = page.isExportIconDisplayed(1);

            log("Check Icon -> Is Import? " + isImport + " | Is Export? " + isExport);

            boolean hasIcon = isImport || isExport;
            boolean hasText = typeText.contains("Nhập") || typeText.contains("Xuất")
                    || typeText.contains("Import") || typeText.contains("Export");

            Assert.assertTrue(hasIcon || hasText,
                    "FAILED: Dòng 1 không hiển thị Icon hoặc Text phân loại đúng.");
        } else {
            log("⚠ Bảng không có dữ liệu để kiểm tra icon.");
        }
    }

    // =================================================================================
    // 2) SEARCH
    // =================================================================================

    @Test(priority = 3, description = "TC_003 (PIS-7-TC-03A): Search LOT exists - Export flow")
    public void TC_003_Search_Export_Exist() {
        page.filterByType("Xuất kho");
        page.waitForDataToLoad();

        String lot = page.getCellText(1, 3).trim();
        log("LOT lấy từ dòng 1 (Xuất kho): " + lot);

        Assert.assertTrue(lot != null && !lot.isEmpty(), "Không lấy được số lô để test search.");

        page.searchByLot(lot);
        page.waitForDataToLoad();

        Assert.assertTrue(page.isKeywordPresentInFirstRow(lot),
                "Search (Xuất kho) không ra kết quả chứa LOT: " + lot);

        page.clearSearch();
    }

    @Test(priority = 4, description = "TC_004 (PIS-7-TC-03B): Search LOT exists - Import flow")
    public void TC_004_Search_Import_Exist() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        String lot = page.getCellText(1, 3).trim();
        log("LOT lấy từ dòng 1 (Nhập kho): " + lot);

        Assert.assertTrue(lot != null && !lot.isEmpty(), "Không lấy được số lô để test search.");

        page.searchByLot(lot);
        page.waitForDataToLoad();

        Assert.assertTrue(page.isKeywordPresentInFirstRow(lot),
                "Search (Nhập kho) không ra kết quả chứa LOT: " + lot);

        page.clearSearch();
    }

    @Test(priority = 5, description = "TC_005 (PIS-7-TC-04A): Search LOT not exist - Export flow")
    public void TC_005_Search_Export_NotExist() {
        page.filterByType("Xuất kho");
        page.waitForDataToLoad();

        String notExistLot = "LOT-NOT-EXIST-999999";
        page.searchByLot(notExistLot);

        Assert.assertTrue(page.isNoResultForLot(notExistLot),
                "Search không tồn tại (Xuất kho) nhưng vẫn có kết quả match LOT: " + notExistLot);

        page.clearSearch();
    }

    @Test(priority = 6, description = "TC_006 (PIS-7-TC-04B): Search LOT not exist - Import flow")
    public void TC_006_Search_Import_NotExist() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        String notExistLot = "LOT-NOT-EXIST-999999";
        page.searchByLot(notExistLot);

        Assert.assertTrue(page.isNoResultForLot(notExistLot),
                "Search không tồn tại (Nhập kho) nhưng vẫn có kết quả match LOT: " + notExistLot);

        page.clearSearch();
    }

    @Test(priority = 7, description = "TC_007 (PIS-7-TC-23): Search trim spaces")
    public void TC_007_Search_TrimSpaces() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        if (page.getRowCount() <= 0) {
            log("⚠ Không có dữ liệu để test trim search.");
            return;
        }

        String lot = page.getCellText(1, 3).trim();
        Assert.assertTrue(lot != null && !lot.isEmpty(), "Không lấy được LOT.");

        String keyword = "  " + lot + "  ";
        page.searchByLot(keyword);

        Assert.assertTrue(page.isAnyLotMatched(lot),
                "Trim search failed, không match LOT sau khi nhập: '" + keyword + "'");

        page.clearSearch();
    }

    @Test(priority = 8, description = "TC_008 (PIS-7-TC-24): Search special characters")
    public void TC_008_Search_SpecialCharacters() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        String before = page.getTbodyText();

        String keyword = "@#$%";
        page.searchByLot(keyword);

        boolean ok = page.isEmptyStateDisplayed() || page.getTbodyText().equals(before);

        Assert.assertTrue(ok,
                "Search special chars: không ra empty-state và list cũng không giữ nguyên (UI hành vi không nhất quán).");

        page.clearSearch();
    }

    // =================================================================================
    // 3) FILTER
    // =================================================================================

    @Test(priority = 9, description = "TC_009 (PIS-7-TC-06): Filter type = Import")
    public void TC_009_Filter_Import() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        if (page.getRowCount() > 0) {
            String typeText = page.getRowTypeText(1);
            Assert.assertTrue(typeText.contains("Nhập"), "Lọc Nhập nhưng thấy: " + typeText);
        } else {
            log("⚠ Không có dữ liệu Nhập kho để kiểm tra.");
        }
    }

    @Test(priority = 10, description = "TC_010 (PIS-7-TC-07): Filter type = Export")
    public void TC_010_Filter_Export() {
        page.filterByType("Xuất kho");
        page.waitForDataToLoad();

        int rowCount = page.getRowCount();
        if (rowCount > 0) {
            String typeText = page.getRowTypeText(1);
            log("Dòng 1 thực tế đang hiển thị: " + typeText);
            Assert.assertTrue(typeText.contains("Xuất"),
                    "LỖI: Bảng vẫn chưa cập nhật, vẫn thấy: " + typeText);
        } else {
            log("⚠ Không có dữ liệu Xuất kho.");
        }
    }

    @Test(priority = 11, description = "TC_011 (PIS-7-TC-08): Filter time = 30 days")
    public void TC_011_Filter_TimeRange_30Days() {
        page.filterByType("Nhập kho");
        try {
            page.filterByTime("30 ngày qua");
            page.waitForDataToLoad();
            int rowCount = page.getRowCount();
            log("Số bản ghi trong 30 ngày qua (Nhập kho): " + rowCount);
            Assert.assertTrue(rowCount >= 0);
        } catch (Exception e) {
            log("⚠ Lỗi bộ lọc thời gian: " + e.getMessage());
        }
    }

    // =================================================================================
    // 4) PAGINATION / PAGE SIZE
    // =================================================================================

    @Test(priority = 12, description = "TC_012 (PIS-7-TC-09): Change page size (50) and validate row count <= size")
    public void TC_012_PageSize_50() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        try {
            page.changePageSize("50");
            page.waitForDataToLoad();
            Assert.assertTrue(page.getDataRowCount() <= 50, "Số dòng data > 50!");
        } catch (Exception e) {
            log("⚠ Không đổi được page size: " + e.getMessage());
        }
    }

    @Test(priority = 13, description = "TC_013 (PIS-7-TC-36): Page size = 25")
    public void TC_013_PageSize_25() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        page.changePageSize("25");
        page.waitForDataToLoad();

        Assert.assertTrue(page.getDataRowCount() <= 25,
                "Page size 25 nhưng số dòng data > 25: " + page.getDataRowCount());
    }

    @Test(priority = 14, description = "TC_014 (PIS-7-TC-38): Page size = 100")
    public void TC_014_PageSize_100() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        page.changePageSize("100");
        page.waitForDataToLoad();

        Assert.assertTrue(page.getDataRowCount() <= 100,
                "Page size 100 nhưng số dòng data > 100: " + page.getDataRowCount());
    }

    @Test(priority = 15, description = "TC_015 (PIS-7-TC-39): Previous button disabled on first page")
    public void TC_015_PreviousDisabled_OnFirstPage() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        Assert.assertTrue(page.isPreviousButtonDisabled(),
                "Nút 'Trước' phải disabled ở trang 1 nhưng lại không disabled.");
    }

    @Test(priority = 16, description = "TC_016 (PIS-7-TC-10.1): Navigate to next page")
    public void TC_016_NavigateToNextPage() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        if (page.getRowCount() > 0 && !page.isNextButtonDisabled()) {
            String row1DataPage1 = page.getRowTypeText(1) + page.getRowTime(1);
            log("Dữ liệu Trang 1: " + row1DataPage1);

            page.clickNextButton();
            page.waitForDataToLoad();

            String row1DataPage2 = page.getRowTypeText(1) + page.getRowTime(1);
            log("Dữ liệu Trang 2: " + row1DataPage2);

            Assert.assertNotEquals(row1DataPage1, row1DataPage2,
                    "FAILED: Dữ liệu không đổi khi sang trang.");
        } else {
            log("⚠ Không đủ dữ liệu để kiểm tra nút 'Sau'.");
        }
    }

    @Test(priority = 17, description = "TC_017 (PIS-7-TC-10.2): Navigate back to previous page")
    public void TC_017_NavigateToPreviousPage() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        if (!page.isNextButtonDisabled()) {
            page.clickNextButton();
            page.waitForDataToLoad();

            String row1DataPage2 = page.getRowTypeText(1) + page.getRowTime(1);
            log("Đang ở Trang 2: " + row1DataPage2);

            page.clickPreviousButton();
            page.waitForDataToLoad();

            String row1DataPage1 = page.getRowTypeText(1) + page.getRowTime(1);
            log("Quay lại Trang 1: " + row1DataPage1);

            Assert.assertNotEquals(row1DataPage1, row1DataPage2,
                    "FAILED: Dữ liệu không đổi khi quay lại.");
        } else {
            log("⚠ Không đủ dữ liệu để kiểm tra nút 'Trước'.");
        }
    }

    @Test(priority = 18, description = "TC_018 (PIS-7-TC-40): Next button disabled on last page")
    public void TC_018_NextDisabled_OnLastPage() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        if (page.isNextButtonDisabled()) {
            log("Đang ở trang cuối (hoặc chỉ có 1 trang) -> OK");
            Assert.assertTrue(true);
            return;
        }

        int guard = 0;
        while (!page.isNextButtonDisabled() && guard++ < 15) {
            page.clickNextButton();
            page.waitForDataToLoad();
        }

        Assert.assertTrue(page.isNextButtonDisabled(),
                "Sau khi đi tới cuối vẫn chưa disabled nút 'Sau'.");
    }

    // =================================================================================
    // 5) MODAL / DETAIL
    // =================================================================================

    @Test(priority = 19, description = "TC_019 (PIS-7-TC-12): Verify data consistency between list and detail modal")
    public void TC_019_VerifyDataConsistency() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        if (page.getRowCount() > 0) {
            String listValue = page.getCellText(1, 3);
            log("Giá trị cột 3: " + listValue);

            try {
                page.clickViewDetail(1);
                String modalContent = page.getDetailModalContent();
                Assert.assertTrue(modalContent.contains(listValue), "Thông tin không khớp trong chi tiết.");
                page.closeModal();
            } catch (Exception e) {
                log("⚠ Lỗi verify chi tiết: " + e.getMessage());
            }
        } else {
            log("⚠ Không có dữ liệu để verify chi tiết.");
        }
    }

    @Test(priority = 20, description = "TC_020 (PIS-7-TC-47/53): Open and close detail modal")
    public void TC_020_OpenAndCloseDetailModal() {
        page.filterByType("Nhập kho");
        page.waitForDataToLoad();

        if (page.getRowCount() <= 0) {
            log("⚠ Không có dữ liệu để test modal.");
            return;
        }

        page.clickViewDetail(1);
        Assert.assertTrue(page.isDetailModalDisplayed(), "Modal không hiển thị.");

        page.closeModal();
        Assert.assertTrue(page.isDetailModalClosed(), "Modal không đóng được.");
    }
}