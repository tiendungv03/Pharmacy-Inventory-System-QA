package vn.pis.ui.tests;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.ExportReceipt;
import vn.pis.ui.pages.LoginPage;

import static vn.pis.ui.util.TestEnv.*;

@Listeners(vn.pis.ui.tests.PIS2_ConsoleLogger.class) 
public class PIS6_Export extends BaseTest {
    
    private ExportReceipt page;

    private void log(String msg){
        System.out.println("[PIS6] " + msg);          
        Reporter.log("[PIS6] " + msg, true);          
    }

    @BeforeClass(alwaysRun = true)
    public void setupOnce() {
        log("=== SETUP: Login & Init ===");
        new LoginPage(driver).open(BASE_URL + "/login");
        new LoginPage(driver).login(ADMIN_USER, ADMIN_PASS);
        page = new ExportReceipt(driver);
        page.open(BASE_URL); 
    }

    @BeforeMethod(alwaysRun = true)
    public void checkPageState(java.lang.reflect.Method m) {
        log("▶ START TC: " + m.getName());
        
        String currentUrl = driver.getCurrentUrl();
        // Tự động đăng nhập lại nếu bị văng
        if (currentUrl.contains("login")) {
            log("⚠️ Re-logging in...");
            if (driver.findElements(By.cssSelector("button[type='submit']")).size() > 0) {
                new LoginPage(driver).login(ADMIN_USER, ADMIN_PASS);
            }
            page.open(BASE_URL);
            return;
        }
        if (!currentUrl.contains("export")) {
             page.open(BASE_URL);
             return;
        }
        if (page.isSuccessModalDisplayed()) {
            page.closeSuccessModal();
        }
    }
    
    @AfterMethod(alwaysRun = true)
    public void afterMethod(java.lang.reflect.Method m){
        log("■ END TC: " + m.getName());
    }

    // ============================================================
    // TEST CASES THEO KỊCH BẢN THỰC TẾ
    // ============================================================

    @Test(priority = 1, description = "PIS-6-TC-01: Mở màn hình")
    public void TC01_OpenPage() {
        Assert.assertTrue(page.getTitle().contains("Tạo phiếu xuất kho"));
    }

    @Test(priority = 2, description = "PIS-6-TC-02: Điền Thông tin chung")
    public void TC02_FillGeneralInfo() {
        // 1. Chọn Khoa
        page.selectDepartment("Khoa Hồi sức tích cực"); // Chọn đúng tên khoa trong video
        
        // 2. Chọn Ngày
        page.setDate("2025-01-12"); // Format yyyy-MM-dd (12/01/2025)
        Assert.assertEquals(page.getDateValue(), "2025-01-12");
        
        // 3. Nhập Ghi chú
        page.setNotes("Ghi chú thêm về phiếu xuất...");
        Assert.assertEquals(page.getNotesValue(), "Ghi chú thêm về phiếu xuất...");
        
        log("General Info OK");
    }

    @Test(priority = 3, description = "PIS-6-TC-03: Thêm thuốc & Kiểm tra tính tiền")
    public void TC03_AddMedicineAndCalc() {
        page.clickAddLine();
        
        // Nhập tên thuốc "Sal" -> Chọn "Salonpas"
        // (Lưu ý: Đảm bảo trong kho có Salonpas, nếu không dùng ok14 như cũ)
        String tenThuoc = "Salonpas"; 
        page.fillMedicine(0, "Salonpas", true); 
        
        // Kiểm tra auto-fill (Đơn vị, Giá)
        Assert.assertNotEquals(page.getCellText(0, 3), "-", "Đơn vị chưa load");
        Assert.assertNotEquals(page.getCellText(0, 5), "-", "Đơn giá chưa load");
        
        // Nhập số lượng
        page.fillQuantity(0, "10");
        
        // Chờ tính tiền
        page.waitForLineCalculation(0);
        
        // Kiểm tra Thành tiền dòng
        String lineTotal = page.getCellText(0, 8);
        Assert.assertNotEquals(lineTotal, "0 ₫", "Thành tiền chưa tính");
        
        // Kiểm tra Tổng tiền
        String grandTotal = page.getGrandTotal();
        Assert.assertEquals(grandTotal, lineTotal, "Tổng tiền sai lệch");
        
        log("Calculation OK");
    }

    @Test(priority = 4, description = "PIS-6-TC-04: Lưu phiếu thành công")
    public void TC04_SaveSuccess() {
        // Bấm Hoàn thành
        page.clickSave();
        
        // Kiểm tra Modal thành công hiện ra
        Assert.assertTrue(page.isSuccessModalDisplayed(), "Modal thành công không hiện!");
        
        // Đóng modal
        page.closeSuccessModal();
        log("Save Success OK");
    }
}