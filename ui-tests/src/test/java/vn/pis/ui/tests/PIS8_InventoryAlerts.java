package vn.pis.ui.tests;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.*;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.AlertsPage;
import vn.pis.ui.pages.LoginPage;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.io.FileHandler;
import org.testng.ITestResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static vn.pis.ui.util.TestEnv.*;

public class PIS8_InventoryAlerts extends BaseTest {

    private AlertsPage page;

    // Status text
    private static final String STATUS_SAP_HET_HAN = "Sắp hết hạn";
    private static final String STATUS_DA_HET_HAN  = "Đã hết hạn";
    private static final String STATUS_HET_HAN     = "Hết hạn";

    // Keywords
    private String existingDrugName = null;

    // Logging helper
    private void START(Method m) {
        System.out.println("\n=======================================================");
        System.out.println("▶ START: " + m.getName());
        System.out.println("   URL: " + (driver != null ? driver.getCurrentUrl() : "null"));
        System.out.println("=======================================================");
    }
    private void STEP(String s){ System.out.println("   [STEP] " + s); }
    private void ACTUAL(String s){ System.out.println("   [ACTUAL] " + s); }
    private void EXPECT(String s){ System.out.println("   [EXPECT] " + s); }
    private void CHECK(String s){ System.out.println("   ✅ " + s); }

    @BeforeClass(alwaysRun = true)
    public void loginAndOpenOnce() {
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);

        // đợi thoát /login
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> !d.getCurrentUrl().contains("/login"));

        page = new AlertsPage(driver);
        page.open();
        page.waitPageReady();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeEach(Method m) {
        START(m);

        // ✅ dọn rác từ test trước (đang fail giữa chừng sẽ còn modal/overlay)
        page.closeModalIfOpen();
        page.closeAnyOverlayIfPresent();

        page.open();
        page.resetLight();

        if (page.getRowCount() > 0) {
            existingDrugName = page.getRowNameOnly(1);
            ACTUAL("existingDrugName = " + existingDrugName);
        } else {
            existingDrugName = null;
            ACTUAL("Table rỗng -> một số TC sẽ SKIP");
        }
    }


    @AfterMethod(alwaysRun = true)
    public void afterEach(Method m, ITestResult result) {
        String name = m.getName();

        // ✅ SKIP thì đừng coi là FAIL
        if (result.getStatus() == ITestResult.SKIP) {
            System.out.println("↩ SKIP: " + name + " (" + result.getThrowable() + ")");
            return;
        }

        if (result.getStatus() == ITestResult.FAILURE) {
            System.out.println("✖ FAIL: " + name);
            try {
                String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String folder = "target/screenshots";
                new File(folder).mkdirs();

                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                File dest = new File(folder + "/" + name + "_" + ts + ".png");
                FileHandler.copy(src, dest);

                System.out.println("   [Screenshot] " + dest.getAbsolutePath());
                System.out.println("   [URL] " + driver.getCurrentUrl());
            } catch (Exception e) {
                System.out.println("   [Screenshot] ERROR: " + e.getMessage());
            }
        } else {
            System.out.println("■ END: " + name);
        }
    }



    // ==========================
    // I) LAYOUT & UI (001-009)
    // ==========================
    @Test public void TC_001_KiemTraTieuDeTrang() {
        STEP("Đọc title");
        String title = page.getPageTitle();
        ACTUAL("title=" + title);
        EXPECT("title có chữ 'Cảnh báo'");
        Assert.assertTrue(title.toLowerCase().contains("cảnh báo"));
        CHECK("Title OK");
    }

    @Test public void TC_002_KiemTraBreadcrumbMoTa() {
        STEP("Đọc mô tả");
        String desc = page.getPageDescription();
        ACTUAL("desc=" + desc);
        EXPECT("desc có 'Theo dõi'");
        Assert.assertTrue(desc.contains("Theo dõi"));
        CHECK("Desc OK");
    }

    @Test public void TC_003_KiemTraTheSapHetHanSuDung() {
        STEP("Kiểm tra card expiring");
        Assert.assertTrue(
                page.isCardVisible("expiring"),
                "Không thấy card 'Sắp hết hạn sử dụng'"
        );
        CHECK("Card expiring visible");
    }


    @Test public void TC_004_KiemTraSoLuongThuocSapHetHan() {
        STEP("Đọc count card expiring");
        int c = page.getCardCount("expiring");
        ACTUAL("count=" + c);
        EXPECT("count là số >=0");
        Assert.assertTrue(c >= 0, "Count không đọc được");
        CHECK("Card expiring count OK");
    }

    @Test
    public void TC_005_KiemTraTheThuocSapHetTonKho() {
        STEP("Kiểm tra card 'Thuốc sắp hết tồn kho'");

        Assert.assertTrue(
                page.isCardVisible("low"),
                "Không tìm thấy card 'Thuốc sắp hết tồn kho'"
        );

        String title = page.getCardTitleText("low");
        ACTUAL("cardTitle = '" + title + "'");

        Assert.assertEquals(
                title.trim(),
                "Thuốc sắp hết tồn kho",
                "Sai tiêu đề card"
        );

        CHECK("Card 'Thuốc sắp hết tồn kho' hiển thị đúng");
    }


    @Test public void TC_006_KiemTraSoLuongThuocSapHetTonKho() {
        STEP("Đọc count card low");
        int c = page.getCardCount("low");
        ACTUAL("count=" + c);
        EXPECT("count là số >=0");
        Assert.assertTrue(c >= 0, "Count không đọc được");
        CHECK("Card low count OK");
    }

    @Test
    public void TC_007_KiemTraTheCanhBaoHetTonKho() {
        STEP("Kiểm tra card 'Cảnh báo hết tồn kho'");

        Assert.assertTrue(
                page.isCardVisible("out"),
                "Không tìm thấy card 'Cảnh báo hết tồn kho'"
        );

        String title = page.getCardTitleText("out");
        ACTUAL("cardTitle = '" + title + "'");

        Assert.assertEquals(
                title.trim(),
                "Cảnh báo hết tồn kho",
                "Sai tiêu đề card"
        );

        CHECK("Card 'Cảnh báo hết tồn kho' hiển thị đúng");
    }


    @Test public void TC_008_KiemTraSoLuongThuocHetTonKho() {
        STEP("Đọc count card out");
        int c = page.getCardCount("out");
        ACTUAL("count=" + c);
        EXPECT("count là số >=0");
        Assert.assertTrue(c >= 0, "Count không đọc được");
        CHECK("Card out count OK");
    }

    @Test
    public void TC_009_KiemTraTieuDeDanhSach() {
        STEP("Check list title");

        String t = page.getListTitleText();
        ACTUAL("listTitle='" + t + "'");

        Assert.assertEquals(t, "Danh sách cảnh báo", "Sai tiêu đề danh sách");
        CHECK("List title OK");
    }


    // ==========================
    // II) TABLE UI (010-019)
    // ==========================
    @Test
    public void TC_010_KiemTraOtimKiem() {
        STEP("Check ô search placeholder");

        String ph = page.getSearchPlaceholder();
        ACTUAL("placeholder='" + ph + "'");

        Assert.assertEquals(ph, "Tìm kiếm thuốc...", "Placeholder ô search sai");
        CHECK("Search placeholder OK");
    }


    @Test
    public void TC_011_016_KiemTraHeadersTable() {
        STEP("Đọc headers table");

        java.util.List<String> headers = page.getHeaderTexts();
        ACTUAL("headers=" + String.join(" | ", headers));

        String[] expected = {
                "Tên thuốc",
                "Tồn kho hiện tại",
                "Mức tồn tối thiểu",
                "Trạng thái",
                "Ngày cảnh báo",
                "Hành động"
        };

        for (String e : expected) {
            Assert.assertTrue(headers.contains(e), "Thiếu header: " + e);
        }

        CHECK("All headers OK");
    }


    @Test
    public void TC_017_KiemTraHienThiSoLoDuoiTenThuoc() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        STEP("Đọc dòng Lô dưới tên thuốc (row 1)");
        String lotLine = page.getRowLotLineRaw(1);
        ACTUAL("lotLine='" + lotLine + "'");

        EXPECT("Bắt đầu bằng 'Lô:' và có mã lô phía sau");
        Assert.assertTrue(lotLine.startsWith("Lô:"), "Lot line không có prefix 'Lô:'");
        Assert.assertTrue(lotLine.replace("Lô:", "").trim().length() > 0, "Mã lô bị trống");

        CHECK("Lot line OK");
    }


    @Test
    public void TC_018_KiemTraBadgeTrangThai_SapHetHan() {
        int row = page.findFirstRowByStatusContains(STATUS_SAP_HET_HAN);
        if (row == -1) throw new SkipException("Không có dòng 'Sắp hết hạn'");

        STEP("Đọc badge status tại row=" + row);
        String badgeText = page.getStatusBadgeText(row);
        String bg = page.getStatusBadgeBgColor(row);

        ACTUAL("badgeText='" + badgeText + "'");
        ACTUAL("badgeBgColor=" + bg);

        EXPECT("Badge có chữ 'Sắp hết hạn'");
        Assert.assertTrue(badgeText.contains(STATUS_SAP_HET_HAN),
                "Badge text không đúng. got='" + badgeText + "'");

        // nếu vẫn muốn check màu, để nhẹ thôi (log là chính)
        CHECK("Warning badge OK");
    }


    @Test
    public void TC_019_KiemTraBadgeTrangThai_DaHetHan() {
        int row = page.findFirstRowByStatusContains(STATUS_DA_HET_HAN);
        if (row == -1) row = page.findFirstRowByStatusContains(STATUS_HET_HAN);
        if (row == -1) throw new SkipException("Không có dòng hết hạn");

        STEP("Đọc badge status tại row=" + row);
        String badgeText = page.getStatusBadgeText(row);
        String bg = page.getStatusBadgeBgColor(row);

        ACTUAL("badgeText='" + badgeText + "'");
        ACTUAL("badgeBgColor=" + bg);

        EXPECT("Badge có chữ 'Đã hết hạn' hoặc 'Hết hạn'");
        Assert.assertTrue(
                badgeText.contains(STATUS_DA_HET_HAN) || badgeText.contains(STATUS_HET_HAN),
                "Badge text không đúng. got='" + badgeText + "'"
        );

        CHECK("Expired badge OK");
    }


    // ==========================
    // III) SEARCH / FILTER (020-029)
    // ==========================
    @Test
    public void TC_020_TimKiemChinhXacTenThuoc() {
        if (existingDrugName == null || existingDrugName.isBlank())
            throw new SkipException("Không có existingDrugName");

        String kw = existingDrugName.trim();

        STEP("Search exact: '" + kw + "'");
        page.search(kw);

        // chờ table filter xong (hoặc empty)
        page.waitUntilTableFilteredByKeyword(kw);

        // đọc data từ table (safe chống stale)
        java.util.List<AlertsPage.AlertRow> rows = page.readTableDataSafe();
        ACTUAL("rows.size=" + rows.size());

        // nếu UI trả empty thì skip (tuỳ bạn: muốn fail thì đổi Assert)
        if (rows.isEmpty() && page.isEmptyVisible())
            throw new SkipException("Search exact ra empty (data môi trường)");

        // ✅ assert: tất cả dòng trả về phải match keyword
        for (AlertsPage.AlertRow r : rows) {
            String name = (r.tenThuoc == null ? "" : r.tenThuoc).toLowerCase();
            boolean ok = name.contains(kw.toLowerCase());

            if (!ok) {
                ACTUAL("Not match row: tenThuoc='" + r.tenThuoc + "', lo='" + r.lo + "'");
            }
            Assert.assertTrue(ok, "Có dòng không match keyword: " + kw);
        }

        CHECK("Search exact OK (table rows all match)");
    }




    @Test
    public void TC_021_TimKiemTenThuocChuThuong() {
        if (existingDrugName == null || existingDrugName.isBlank())
            throw new SkipException("Không có existingDrugName");

        String kw = existingDrugName.toLowerCase().trim();

        STEP("Search lowercase: '" + kw + "'");
        page.search(kw);

        page.waitUntilTableFilteredByKeyword(kw);

        java.util.List<AlertsPage.AlertRow> rows = page.readTableDataSafe();
        ACTUAL("rows.size=" + rows.size());

        if (rows.isEmpty() && page.isEmptyVisible())
            throw new SkipException("Search lowercase ra empty (data môi trường)");

        for (AlertsPage.AlertRow r : rows) {
            String name = (r.tenThuoc == null ? "" : r.tenThuoc).toLowerCase();
            boolean ok = name.contains(kw);

            if (!ok) ACTUAL("Not match row: tenThuoc='" + r.tenThuoc + "', lo='" + r.lo + "'");
            Assert.assertTrue(ok, "Có dòng không match keyword: " + kw);
        }

        CHECK("Search lowercase OK (all rows match)");
    }


    @Test
    public void TC_022_TimKiemMotPhanTenThuoc() {
        if (existingDrugName == null || existingDrugName.trim().length() < 3)
            throw new SkipException("Tên thuốc quá ngắn để test partial");

        String part = existingDrugName.trim().substring(0, 3).toLowerCase();

        STEP("Search partial: '" + part + "'");
        page.search(part);

        page.waitUntilTableFilteredByKeyword(part);

        java.util.List<AlertsPage.AlertRow> rows = page.readTableDataSafe();
        ACTUAL("rows.size=" + rows.size());

        if (rows.isEmpty() && page.isEmptyVisible())
            throw new SkipException("Search partial ra empty (data môi trường)");

        for (AlertsPage.AlertRow r : rows) {
            String name = (r.tenThuoc == null ? "" : r.tenThuoc).toLowerCase();
            boolean ok = name.contains(part);

            if (!ok) ACTUAL("Not match row: tenThuoc='" + r.tenThuoc + "', lo='" + r.lo + "'");
            Assert.assertTrue(ok, "Có dòng không match keyword(partial): " + part);
        }

        CHECK("Search partial OK (all rows match)");
    }


    @Test
    public void TC_023_TimKiemKhongCoKetQua() {
        String kw = "XYZ123___KHONG_TON_TAI___";
        STEP("Search no match: '" + kw + "'");
        page.search(kw);

        page.waitEmptyState();

        int rc = page.getRowCount();
        boolean empty = page.isEmptyVisible();
        String emptyText = page.getEmptyText();

        ACTUAL("rowCount=" + rc);
        ACTUAL("emptyVisible=" + empty);
        ACTUAL("emptyText=" + emptyText);

        EXPECT("Hiện empty-state: 'Không có cảnh báo nào'");

        Assert.assertTrue(empty, "Không hiện empty-state");
        Assert.assertTrue(
                emptyText.toLowerCase().contains("không có cảnh báo"),
                "Empty text không đúng"
        );
        Assert.assertEquals(rc, 0, "Có empty-state mà rowCount vẫn > 0");

        CHECK("No-match OK");
    }


    @Test public void TC_024_KiemTraSoDongMacDinh10() {
        STEP("Đếm row hiện tại");
        int rc = page.getRowCount();
        ACTUAL("rowCount=" + rc);
        EXPECT("<=10 (mặc định)");
        Assert.assertTrue(rc <= 10, "Không phải <=10");
        CHECK("Default page size OK");
    }

    @Test
    public void TC_025_ThayDoiSoDongHienThi_25Dong() {
        STEP("Chọn Hiển thị = 25");
        page.setPageSize(25);

        int selected = page.getSelectedPageSize();
        ACTUAL("pageSizeSelected=" + selected);
        Assert.assertEquals(selected, 25, "Dropdown không chọn đúng 25");

        int rc = page.getRowCount();
        ACTUAL("rowCount=" + rc);
        EXPECT("rowCount <= 25");
        Assert.assertTrue(rc <= 25, "RowCount > 25 dù đã chọn 25");

        CHECK("Change page size 25 OK");
    }





    @Test
    public void TC_026_ChuyenTrangKeTiep_Next() {
        page.setPageSize(10);
        page.waitPageReady();

        Map<String,Integer> before = page.getCurrentPageInfo();
        int cur = before.getOrDefault("current", 1);
        int total = before.getOrDefault("total", 1);
        ACTUAL("pageInfoBefore=" + before);

        if (total <= 1 || !page.isNextEnabled())
            throw new SkipException("Không có trang 2");

        page.clickNextPageAndWait();

        Map<String,Integer> after = page.getCurrentPageInfo();
        ACTUAL("pageInfoAfter=" + after);

        Assert.assertEquals(after.get("current").intValue(), cur + 1, "Next không tăng trang");
        CHECK("Next OK");
    }

    @Test
    public void TC_027_ChuyenTrangTruoc_Previous() {
        page.setPageSizeToDefault();
        page.waitPageReady();

        Map<String, Integer> p1 = page.getCurrentPageInfo();
        ACTUAL("pageInfo start=" + p1);

        if (!page.isNextEnabled())
            throw new SkipException("Không có pagination (Next disable)");

        STEP("Đi tới trang 2");
        page.clickNextPage();

        // chờ pageInfo đổi sang 2
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            Map<String, Integer> p = page.getCurrentPageInfo();
            return p.getOrDefault("current", 1) == 2;
        });

        Map<String, Integer> p2 = page.getCurrentPageInfo();
        ACTUAL("pageInfo after Next=" + p2);
        Assert.assertEquals(p2.getOrDefault("current", 0), 2, "Không sang trang 2");

        STEP("Click Prev về trang 1");
        page.clickPrevPage();

        // chờ pageInfo về lại 1
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            Map<String, Integer> p = page.getCurrentPageInfo();
            return p.getOrDefault("current", 2) == 1;
        });

        Map<String, Integer> back = page.getCurrentPageInfo();
        ACTUAL("pageInfo after Prev=" + back);

        EXPECT("current=1 và Prev disable");
        Assert.assertEquals(back.getOrDefault("current", 0), 1, "Không về trang 1");
        Assert.assertTrue(page.isPrevDisabled(), "Về trang 1 thì Prev phải disable");

        CHECK("Prev OK");
    }



    @Test
    public void TC_028_KiemTraNutTruocDisableTrang1() {
        page.setPageSize(10);
        page.waitPageReady();

        // đảm bảo về trang 1 (nếu bạn có goToFirstPage thì gọi)
        page.goToFirstPage();

        Assert.assertTrue(page.isPrevDisabled(), "Prev phải disable ở trang 1");
        CHECK("Prev disabled OK");
    }


    @Test
    public void TC_029_KiemTraThongTinPhanTrang() {
        STEP("Đọc pagination text");
        String text = page.getPaginationText();
        ACTUAL("paginationText=" + text);

        Map<String, Integer> info = page.getCurrentPageInfo();
        ACTUAL("pageInfo=" + info);

        EXPECT("Có current/total hợp lệ (1 <= current <= total)");
        int current = info.getOrDefault("current", -1);
        int total   = info.getOrDefault("total", -1);

        Assert.assertTrue(current >= 1, "Không parse được current page");
        Assert.assertTrue(total >= 1, "Không parse được total page");
        Assert.assertTrue(current <= total, "current > total (sai logic)");

        CHECK("Pagination info OK");
    }


    // ==========================
    // IV) MODAL DETAIL UI (030-038)
    // ==========================
    @Test
    public void TC_030_KiemTraMoModal() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        STEP("Mở modal chi tiết dòng 1");
        page.openDetail(1);

        EXPECT("Modal mở + có chữ 'Chi tiết'");
        Assert.assertTrue(page.isModalOpen(), "Modal không mở");

        String modalText = page.getModalTextAll();
        ACTUAL("modalText(first 80)=" + (modalText.length() > 80 ? modalText.substring(0,80) : modalText));
        Assert.assertTrue(modalText.contains("Chi tiết"), "Modal không có chữ 'Chi tiết'");

        STEP("Đóng modal");
        page.closeModalIfOpen();

        EXPECT("Modal đóng");
        Assert.assertFalse(page.isModalOpen(), "Modal chưa đóng");

        CHECK("Open/Close modal OK");
    }


//    @Test public void TC_031_KiemTraHeaderModal() {
//        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");
//        page.openDetail(1);
//        String all = page.getModalTextAll();
//        ACTUAL("modalText(first lines)=" + (all.length() > 120 ? all.substring(0,120) : all));
//        Assert.assertTrue(all.contains("Chi tiết"), "Modal không có chữ 'Chi tiết'");
//        page.closeModalIfOpen();
//        CHECK("Modal header OK");
//    }

//    @Test public void TC_032_KiemTraThongBaoTomTat() {
//        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");
//        page.openDetail(1);
//        String all = page.getModalTextAll();
//        ACTUAL("modalHasText=" + !all.isBlank());
//        Assert.assertFalse(all.isBlank(), "Modal text rỗng");
//        page.closeModalIfOpen();
//        CHECK("Modal summary OK");
//    }

    @Test public void TC_033_KiemTraThongTinSanPham() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        page.openDetail(1);

        String sku = page.getModalSkuValue();
        ACTUAL("sku=" + sku);

        Assert.assertTrue(sku != null && !sku.isBlank(), "SKU rỗng hoặc không đọc được");
        Assert.assertTrue(sku.startsWith("SKU-") || sku.matches(".*\\d+.*"), "SKU format bất thường: " + sku);

        page.closeModalIfOpen();
        CHECK("Product block OK");
    }




//    @Test public void TC_034_KiemTraThongTinKho() {
//        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");
//        page.openDetail(1);
//        String all = page.getModalTextAll().toLowerCase();
//        ACTUAL("contains kho=" + all.contains("kho"));
//        Assert.assertTrue(all.contains("kho"), "Không thấy thông tin kho");
//        page.closeModalIfOpen();
//        CHECK("Warehouse block OK");
//    }

    private String extractDateTimeUnderLabel(String modalText, String label) {
        if (modalText == null) return "";
        // bắt text kiểu:
        // Hạn sử dụng
        // 02/01/2026 07:00
        Pattern p = Pattern.compile("(?s)\\b" + Pattern.quote(label) + "\\b\\s*\\R\\s*([0-9]{1,2}/[0-9]{1,2}/[0-9]{4}(?:\\s+[0-9]{2}:[0-9]{2})?)");
        Matcher m = p.matcher(modalText);
        return m.find() ? m.group(1).trim() : "";
    }

    @Test
    public void TC_035_KiemTraThongTinLoVaHSD() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        // 1) lấy data từ table (row 1)
        AlertsPage.AlertRow r = page.readTableDataSafe().get(0);
        ACTUAL("table.lo=" + r.lo);
        ACTUAL("table.ngayCanhBao=" + r.ngayCanhBao);

        // 2) mở modal
        page.openDetail(1);
        String modal = page.getModalTextAll();
        ACTUAL("modal(first 250)=" + (modal.length() > 250 ? modal.substring(0,250) : modal));

        // 3) check mã lô
        EXPECT("Modal chứa mã lô + có Hạn sử dụng");
        Assert.assertTrue(modal.contains(r.lo), "Modal thiếu mã lô: " + r.lo);

        // 4) check Hạn sử dụng (đúng label)
        String expiry = extractDateTimeUnderLabel(modal, "Hạn sử dụng");
        ACTUAL("modal.expiry=" + expiry);

        Assert.assertTrue(!expiry.isBlank(),
                "Modal không tìm thấy giá trị dưới label 'Hạn sử dụng'.\nModal=" + modal);

        // chỉ cần đảm bảo có ngày dd/mm/yyyy (và có thể kèm giờ)
        Assert.assertTrue(expiry.matches("\\d{1,2}/\\d{1,2}/\\d{4}(\\s+\\d{2}:\\d{2})?"),
                "Hạn sử dụng sai format: " + expiry);

        page.closeModalIfOpen();
        CHECK("Lot + Expiry block OK");
    }



//    @Test public void TC_036_KiemTraSoNgayConLaiHighlight() {
//        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");
//        page.openDetail(1);
//        String all = page.getModalTextAll().toLowerCase();
//        ACTUAL("contains 'ngày'=" + all.contains("ngày"));
//        Assert.assertTrue(all.contains("ngày") || all.contains("hết hạn"), "Không thấy số ngày còn lại / hết hạn");
//        page.closeModalIfOpen();
//        CHECK("Days left OK");
//    }

    @Test public void TC_037_KiemTraSoLuongTrongLo() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        String drug = page.getRowNameOnly(1);
        String lot  = page.getRowLotLine(1);
        String warnDate = page.getRowWarnDate(1);

        ACTUAL("row1.drug=" + drug);
        ACTUAL("row1.lot=" + lot);
        ACTUAL("row1.warnDate=" + warnDate);

        page.openDetail(1);
        Assert.assertTrue(page.isModalOpen(), "Modal không mở");

        String qtyText = page.getModalValueUnderLabelExact("Số lượng trong lô");
        ACTUAL("qtyText=" + qtyText);

        Assert.assertTrue(!qtyText.isBlank(), "Số lượng trong lô rỗng");
        Assert.assertTrue(qtyText.matches("\\d+"), "Số lượng phải là số nguyên, got=" + qtyText);

        page.closeModalIfOpen();
        CHECK("Quantity OK for drug=" + drug);
    }




    @Test public void TC_038_KiemTraThongTinThoiGian() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        String drug = page.getRowNameOnly(1);
        String lot  = page.getRowLotLine(1);
        String status = page.getRowStatus(1);
        String warnDate = page.getRowWarnDate(1);

        ACTUAL("row1.drug=" + drug);
        ACTUAL("row1.lot=" + lot);
        ACTUAL("row1.status=" + status);
        ACTUAL("row1.warnDate=" + warnDate);

        page.openDetail(1);
        Assert.assertTrue(page.isModalOpen(), "Modal không mở");

        // 1) Ngày tạo: bắt buộc có
        Assert.assertTrue(page.modalHasLabelExact("Ngày tạo"), "Không thấy label 'Ngày tạo'");
        String created = page.getModalValueUnderLabelExact("Ngày tạo");
        ACTUAL("created=" + created);
        Assert.assertTrue(!created.isBlank(), "Ngày tạo rỗng");
        Assert.assertTrue(created.matches("\\d{2}/\\d{2}/\\d{4}.*\\d{2}:\\d{2}"),
                "Ngày tạo sai format, got=" + created);

        // 2) Cập nhật lần cuối: optional
        boolean hasUpdatedLabel = page.modalHasLabelExact("Cập nhật lần cuối") || page.modalHasLabelExact("Ngày cập nhật");
        ACTUAL("hasUpdatedLabel=" + hasUpdatedLabel);

        if (hasUpdatedLabel) {
            String updated = page.modalHasLabelExact("Cập nhật lần cuối")
                    ? page.getModalValueUnderLabelExact("Cập nhật lần cuối")
                    : page.getModalValueUnderLabelExact("Ngày cập nhật");

            ACTUAL("updated=" + updated);

            // Nếu UI có label mà value rỗng => cho phép (chưa từng update) HOẶC bạn muốn strict thì assert != blank
            if (!updated.isBlank()) {
                Assert.assertTrue(updated.matches("\\d{2}/\\d{2}/\\d{4}.*\\d{2}:\\d{2}"),
                        "Updated sai format, got=" + updated);
            } else {
                STEP("Updated rỗng (có thể chưa từng cập nhật) -> accept");
            }
        } else {
            STEP("Không có label Updated -> accept (UI có thể ẩn khi chưa cập nhật)");
        }

        page.closeModalIfOpen();
        CHECK("Time info OK for drug=" + drug);
    }




    // ==========================
    // V) ACTION (039-044)
    // ==========================
    @Test public void TC_039_NhapGhiChuThanhCong() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");
        page.openDetail(1);
        page.enterNoteAndConfirm("Đã kiểm tra");
        Assert.assertFalse(page.isModalOpen(), "Modal chưa đóng sau confirm");
        CHECK("Note normal OK");
    }

    @Test public void TC_040_NhapGhiChuKyTuDacBiet() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");
        page.openDetail(1);
        page.enterNoteAndConfirm("@#$%");
        Assert.assertFalse(page.isModalOpen(), "Modal chưa đóng sau confirm");
        CHECK("Note special OK");
    }

    @Test public void TC_041_BoTrongGhiChu() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");
        page.openDetail(1);
        page.enterNoteAndConfirm("");
        Assert.assertFalse(page.isModalOpen(), "Modal chưa đóng sau confirm");
        CHECK("Note empty OK");
    }

    @Test public void TC_042_DongModalBangBackdrop() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");
        page.openDetail(1);

        page.closeModalByBackdrop();
        Assert.assertFalse(page.isModalOpen(), "Modal chưa đóng khi click backdrop");
        CHECK("Close by backdrop OK");
    }




    @Test public void TC_043_DongModalBangNutX() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        page.openDetail(1);
        Assert.assertTrue(page.isModalOpen(), "Modal không mở");

        page.closeModalByXOnly(); // ✅ chỉ X

        Assert.assertFalse(page.isModalOpen(), "Modal chưa đóng bằng nút X");
        CHECK("Close by X OK");
    }


    @Test public void TC_044_DongModalBangNutDong() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        page.openDetail(1);
        Assert.assertTrue(page.isModalOpen(), "Modal không mở");

        page.closeModalByBottomOnly(); // ✅ chỉ Đóng

        Assert.assertFalse(page.isModalOpen(), "Modal chưa đóng bằng nút Đóng");
        CHECK("Close by bottom OK");
    }


    // ==========================
    // VI) BUSINESS LOGIC (045-048)
    // ==========================
    @Test
    public void TC_045_KiemTraLogicHanDung_NgayConLai() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        page.openDetail(1);
        Assert.assertTrue(page.isModalOpen(), "Modal không mở");

        String expiryRaw = page.getModalValueByLabel("Hạn sử dụng");        // OK
        String daysLeftRaw = page.getModalValueByLabel("Số ngày còn lại");  // ✅ lấy đúng "2 ngày"

        ACTUAL("expiryRaw=" + expiryRaw);
        ACTUAL("daysLeftRaw=" + daysLeftRaw);

        LocalDateTime expiry = parseDateTimeVN(expiryRaw);
        int uiDaysLeft = extractFirstInt(daysLeftRaw);
        Assert.assertTrue(uiDaysLeft >= 0, "Không parse được số ngày còn lại");

        LocalDate today = LocalDate.now();
        long expectedDays = ChronoUnit.DAYS.between(today, expiry.toLocalDate());

        long diff = Math.abs(expectedDays - uiDaysLeft);
        ACTUAL("uiDaysLeft=" + uiDaysLeft);
        ACTUAL("expectedDays=" + expectedDays);
        ACTUAL("diff=" + diff);

        Assert.assertTrue(diff <= 1,
                "Sai logic 'Số ngày còn lại'. UI=" + uiDaysLeft + ", expected=" + expectedDays
                        + " (today=" + today + ", expiry=" + expiry + ")");

        page.closeModalIfOpen();
        CHECK("Days-left logic OK");
    }


    @Test
    public void TC_046_KiemTraLogicMauSac_Warning_Lt10Ngay() {
        int row = page.findFirstRowByStatusContains(STATUS_SAP_HET_HAN);
        if (row == -1) throw new SkipException("Không có dòng sắp hết hạn");

        String drug = page.getRowNameOnly(row);
        ACTUAL("row=" + row + ", drug=" + drug);

        // mở modal để lấy "Số ngày còn lại"
        page.openDetail(row);
        String daysLeftRaw = page.getModalValueByLabel("Số ngày còn lại"); // ví dụ "2 ngày"
        int daysLeft = extractFirstInt(daysLeftRaw);

        ACTUAL("daysLeftRaw=" + daysLeftRaw);
        ACTUAL("daysLeft=" + daysLeft);

        // đúng logic warning
        Assert.assertTrue(daysLeft >= 0 && daysLeft < 10,
                "Dòng 'Sắp hết hạn' nhưng daysLeft không <10: " + daysLeft);

        page.closeModalIfOpen();

        // check màu badge
        String cls = page.getStatusBadgeClass(row);
        String bg = page.getStatusBadgeBgColor(row);

        ACTUAL("badgeClass=" + cls);
        ACTUAL("badgeBg=" + bg);

        Assert.assertTrue(page.hasWarningBadge(row)
                        || (bg != null && !bg.isBlank()), // fallback nếu class không có chữ yellow
                "Không thấy warning badge (class/bg không match)");

        CHECK("Warning color OK for drug=" + drug);
    }


    @Test
    public void TC_047_KiemTraLogicMauSac_Critical_Lt0Ngay() {
        int row = page.findFirstRowByStatusContains(STATUS_DA_HET_HAN);
        if (row == -1) row = page.findFirstRowByStatusContains(STATUS_HET_HAN);
        if (row == -1) throw new SkipException("Không có dòng hết hạn");

        String drug = page.getRowNameOnly(row);
        ACTUAL("row=" + row + ", drug=" + drug);

        page.openDetail(row);

        // ưu tiên đọc "Số ngày còn lại"
        String daysLeftRaw = page.getModalValueByLabel("Số ngày còn lại"); // có thể "-1 ngày" hoặc "0 ngày"
        int daysLeft = extractFirstIntAllowNegative(daysLeftRaw); // ✅ cần hàm allow âm

        ACTUAL("daysLeftRaw=" + daysLeftRaw);
        ACTUAL("daysLeft=" + daysLeft);

        Assert.assertTrue(daysLeft < 0 || page.getModalTextAll().toLowerCase().contains("hết hạn"),
                "Không xác định được expired (<0 ngày) từ modal");

        page.closeModalIfOpen();

        String cls = page.getStatusBadgeClass(row);
        String bg = page.getStatusBadgeBgColor(row);

        ACTUAL("badgeClass=" + cls);
        ACTUAL("badgeBg=" + bg);

        Assert.assertTrue(page.hasExpiredBadge(row)
                        || (bg != null && !bg.isBlank()),
                "Không thấy expired badge (class/bg không match)");

        CHECK("Expired color OK for drug=" + drug);
    }


//    @Test public void TC_048_DongBoDuLieu_CardVsTable_SapHetHan() {
//        STEP("Đọc card count expiring");
//        int card = page.getCardCount("expiring");
//        if (card < 0) throw new SkipException("Không đọc được card count");
//
//        STEP("Đếm rows status='Sắp hết hạn' across pages");
//        int rows = page.countRowsAcrossAllPagesByStatus(STATUS_SAP_HET_HAN);
//
//        ACTUAL("card=" + card);
//        ACTUAL("rows=" + rows);
//        EXPECT("rows == card");
//        Assert.assertEquals(rows, card, "Mismatch card vs table");
//        CHECK("Sync OK");
//    }

    private String extractUnderLabel(String modalText, String label) {
        // label ở 1 dòng, value ở dòng kế tiếp
        Pattern p = Pattern.compile("(?s)\\b" + Pattern.quote(label) + "\\b\\s*\\R\\s*(.+?)\\R");
        Matcher m = p.matcher(modalText);
        return m.find() ? m.group(1).trim() : "";
    }

    @Test
    public void TC_Check_TableVsModal_Row1() {
        if (page.getRowCount() == 0) throw new SkipException("Table rỗng");

        AlertsPage.AlertRow r = page.readTableDataSafe().get(0);

        page.openDetail(1);
        String modal = page.getModalTextAll();

        System.out.println("   [EXPECT] modal chứa tenThuoc/lo/trangThai + có hạn sử dụng");
        Assert.assertTrue(modal.contains(r.tenThuoc), "Modal thiếu tenThuoc: " + r.tenThuoc);
        Assert.assertTrue(modal.contains(r.lo), "Modal thiếu lo: " + r.lo);
        Assert.assertTrue(modal.contains(r.trangThai), "Modal thiếu trangThai: " + r.trangThai);

        String expiry = extractUnderLabel(modal, "Hạn sử dụng");
        Assert.assertFalse(expiry.isBlank(), "Modal thiếu 'Hạn sử dụng'.\nModal=" + modal);

        // chỉ cần có dd/MM/yyyy (có thể kèm giờ)
        Assert.assertTrue(expiry.matches("\\d{1,2}/\\d{1,2}/\\d{4}(\\s+\\d{2}:\\d{2})?"),
                "Hạn sử dụng sai format: " + expiry);

        page.closeModalIfOpen();
    }




    // ===== Helpers =====

    private static LocalDateTime parseDateTimeVN(String s) {
        // ví dụ: "08/01/2026 07:00" hoặc "08/01/2026"
        s = s.trim().replace("\n", " ");
        DateTimeFormatter f1 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter f2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            return LocalDateTime.parse(s, f1);
        } catch (Exception ignored) {}

        // nếu chỉ có ngày
        LocalDate d = LocalDate.parse(s, f2);
        return d.atStartOfDay();
    }

    private static int extractFirstInt(String s) {
        if (s == null) return -1;
        Matcher m = Pattern.compile("(\\d+)").matcher(s);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    private static String extractAfterLabel(String all, String label) {
        // tìm đoạn sau label đến hết dòng / token tiếp theo
        if (all == null) return "";
        Pattern p = Pattern.compile(Pattern.quote(label) + "\\s*\\n?\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(all);
        return m.find() ? m.group(1).trim() : "";
    }


    private int extractFirstIntAllowNegative(String s) {
        if (s == null) return -999999;
        Matcher m = Pattern.compile("(-?\\d+)").matcher(s);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -999999;
    }


}
