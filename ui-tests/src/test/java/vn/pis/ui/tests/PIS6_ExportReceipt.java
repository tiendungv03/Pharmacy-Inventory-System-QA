//package vn.pis.ui.tests;
//
//import org.testng.Assert;
//import org.testng.Reporter;
//import org.testng.annotations.*;
//import vn.pis.ui.base.BaseTest;
//import vn.pis.ui.pages.ExportReceipt;
//import vn.pis.ui.pages.LoginPage;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//
//import static vn.pis.ui.util.TestEnv.*;
//
//@Listeners(vn.pis.ui.tests.PIS2_ConsoleLogger.class) 
//public class PIS6_ExportReceipt extends BaseTest {
//    
//    // Biến toàn cục, khởi tạo 1 lần dùng mãi mãi
//    private ExportReceipt page;
//
//    private void log(String msg){
//        System.out.println("[PIS6] " + msg);         
//        Reporter.log("[PIS6] " + msg, true);         
//    }
//
//    // ============================================================
//    // 1. SETUP TOÀN BỘ (Chạy 1 lần duy nhất)
//    // ============================================================
//    @BeforeClass(alwaysRun = true)
//    public void setupOnce() {
//        log("=== SETUP: Đăng nhập & Khởi tạo Page Object ===");
//        
//        // 1. Đăng nhập
//        new LoginPage(driver).open(BASE_URL + "/login");
//        new LoginPage(driver).login(ADMIN_USER, ADMIN_PASS);
//        
//        // 2. Khởi tạo biến 'page' TẠI ĐÂY (Chỉ 1 lần)
//        page = new ExportReceipt(driver);
//    }
//
//    // ============================================================
//    // 2. RESET TRANG (Trước mỗi TC)
//    // ============================================================
//    @BeforeMethod(alwaysRun = true)
//    public void resetPage() {
//        // Bước 1: Cố gắng mở trang Export
//        page.open(BASE_URL); 
//        
//        // Bước 2: Kiểm tra xem có bị Web "đá" về trang Login không?
//        // (Dấu hiệu: URL chứa chữ "login" hoặc có nút Đăng nhập)
//        if (driver.getCurrentUrl().contains("login")) {
//            log("⚠️ CẢNH BÁO: Phiên đăng nhập bị ngắt! Hệ thống đang tự động đăng nhập lại...");
//            
//            // Thực hiện đăng nhập lại để cứu vãn các test case sau
//            new LoginPage(driver).login(ADMIN_USER, ADMIN_PASS);
//            
//            // Đăng nhập xong thì vào lại trang Export
//            page.open(BASE_URL);
//        }
//    }
//
//    // ============================================================
//    // TEST CASES
//    // ============================================================
//
//    @Test(priority = 1, description = "PIS-6-TC-01: Mở màn hình")
//    public void TC01_OpenPage() {
//        Assert.assertTrue(page.getTitle().contains("Tạo phiếu xuất kho"));
//    }
//
//    @Test(priority = 2, description = "PIS-6-TC-02: Chọn Khoa")
//    public void TC02_SelectDepartment() {
//        page.selectDepartment("Khoa Dược"); 
//        Assert.assertNotEquals(page.getSelectedDepartment(), "Chọn khoa/phòng");
//    }
//
//    @Test(priority = 3, description = "PIS-6-TC-03: Ngày xuất")
//    public void TC03_Date() {
//        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        Assert.assertEquals(page.getDateValue(), today);
//        
//        page.setDate("2025-12-01");
//        Assert.assertEquals(page.getDateValue(), "2025-12-01");
//    }
//
//    @Test(priority = 4, description = "PIS-6-TC-04: Ghi chú")
//    public void TC04_Notes() {
//        page.setNotes("Test Note");
//        Assert.assertEquals(page.getNotesValue(), "Test Note");
//    }
//
//    @Test(priority = 5, description = "PIS-6-TC-05,10: Thêm/Xóa dòng")
//    public void TC05_AddDeleteLine() {
//        int initial = page.getRowCount();
//        page.clickAddLine();
//        Assert.assertEquals(page.getRowCount(), initial + 1, "Thêm thất bại");
//        
//        page.deleteLine(0);
//        Assert.assertEquals(page.getRowCount(), initial, "Xóa thất bại");
//    }
//
//    @Test(priority = 6, description = "PIS-6-TC-06,07: Gợi ý & Auto-fill")
//    public void TC06_AutoFill() {
//        page.fillMedicine(0, "Paraxetamol", true);
//        Assert.assertNotEquals(page.getCellText(0, 5), "-", "Giá chưa load");
//        Assert.assertNotEquals(page.getCellText(0, 7), "-", "HSD chưa load");
//    }
//
//    @Test(priority = 7, description = "PIS-6-TC-08,09: Tính tiền")
//    public void TC07_Calculation() {
//        page.fillMedicine(0, "Paracetamol", true);
//        page.fillQuantity(0, "10");
//        Assert.assertNotEquals(page.getCellText(0, 8), "0 ₫", "Thành tiền sai");
//
//        page.clickAddLine();
//        page.fillMedicine(1, "Ibuprofen", true);
//        page.fillQuantity(1, "5");
//        Assert.assertNotEquals(page.getGrandTotal(), "0 ₫", "Tổng tiền sai");
//    }
//
//    @Test(priority = 8, description = "PIS-6-TC-11,12: Validate Tồn kho")
//    public void TC08_StockValidation() {
//        page.fillMedicine(0, "Paracetamol", true);
//        
//        page.fillQuantity(0, "999999"); 
//        page.clickSave();
//        Assert.assertTrue(page.isErrorMessageDisplayed("quá số lượng") || page.isErrorMessageDisplayed("tồn"), 
//            "Không bắt lỗi quá tồn");
//
//        page.fillQuantity(0, "1"); 
//        Assert.assertTrue(page.isSaveButtonEnabled(), "Nút lưu bị disable");
//    }
//
//    @Test(priority = 9, description = "PIS-6-TC-13: Validate bắt buộc")
//    public void TC09_Required() {
//        page.fillMedicine(0, "Paracetamol", true);
//        page.fillQuantity(0, ""); 
//        page.clickSave();
//        Assert.assertTrue(page.isErrorMessageDisplayed("bắt buộc") || page.isErrorMessageDisplayed("required"), 
//            "Không bắt lỗi trống");
//    }
//
//    @Test(priority = 10, description = "PIS-6-TC-14: Lưu thành công")
//    public void TC10_SaveSuccess() {
//        page.selectDepartment("Khoa Dược");
//        page.fillMedicine(0, "Paracetamol", true);
//        page.fillQuantity(0, "1"); 
//        
//        page.clickSave();
//        Assert.assertTrue(page.isSuccessModalDisplayed(), "Modal thành công không hiện!");
//        page.closeSuccessModal();
//    }
//    
//    @Test(priority = 11, description = "PIS-6-TC-15: Lưu thất bại")
//    public void TC11_SaveFail() {
//        // Refresh để xóa dữ liệu cũ từ TC10
//        driver.navigate().refresh();
//        
//        page.clickSave(); 
//        Assert.assertTrue(page.getTitle().contains("Tạo phiếu xuất kho"), "Trang bị chuyển sai");
//    }
//}

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
public class PIS6_ExportReceipt extends BaseTest {
    
    private ExportReceipt page;

    private void log(String msg){
        System.out.println("[PIS6] " + msg);          
        Reporter.log("[PIS6] " + msg, true);          
    }
    
    // 1. SETUP: CHỈ ĐĂNG NHẬP 1 LẦN DUY NHẤT Ở ĐÂY
    @BeforeClass(alwaysRun = true)
    public void setupOnce() {
        log("=== SETUP: Đăng nhập Admin & Khởi tạo Page ===");
        new LoginPage(driver).open(BASE_URL + "/login");
        new LoginPage(driver).login(ADMIN_USER, ADMIN_PASS);
        
        // Khởi tạo page object
        page = new ExportReceipt(driver);
        // Mở trang đích
        page.open(BASE_URL); 
    }

    // 2. CHECK STATE: CHỈ RESET TRANG, TUYỆT ĐỐI KHÔNG ĐĂNG NHẬP LẠI
    @BeforeMethod(alwaysRun = true)
    public void checkPageState(java.lang.reflect.Method m) {
        log("▶ BẮT ĐẦU TC: " + m.getName());
        
        // Chỉ đơn giản là mở lại trang Export để reset form
        page.open(BASE_URL);
        
        // Nếu bị kẹt Modal thành công của bài trước thì đóng nó đi
        if (page.isSuccessModalDisplayed()) {
            page.closeSuccessModal();
        }
    }
    
    @AfterMethod(alwaysRun = true)
    public void afterMethod(java.lang.reflect.Method m){
        log("■ KẾT THÚC TC: " + m.getName());
    }

    // ============================================================
    // TEST CASES
    // ============================================================

    @Test(priority = 1, description = "PIS-6-TC-01: Mở màn hình")
    public void TC01_OpenPage() {
        Assert.assertTrue(page.getTitle().contains("Tạo phiếu xuất kho"));
    }

    @Test(priority = 2, description = "PIS-6-TC-02: Chọn Khoa")
    public void TC02_SelectDepartment() {
        page.selectDepartment("Khoa Dược"); 
        Assert.assertNotEquals(page.getSelectedDepartment(), "Chọn khoa/phòng");
    }

    @Test(priority = 3, description = "PIS-6-TC-03: Ngày xuất")
    public void TC03_Date() {
        page.setDate("2025-12-01");
        Assert.assertEquals(page.getDateValue(), "2025-12-01");
    }

    @Test(priority = 4, description = "PIS-6-TC-04: Ghi chú")
    public void TC04_Notes() {
        String content = "Thuốc nhẹ liệu cho người bệnh";
        page.setNotes(content);
        Assert.assertEquals(page.getNotesValue(), content);
    }

    @Test(priority = 5, description = "PIS-6-TC-05,10: Thêm/Xóa dòng")
    public void TC05_AddDeleteLine() {
        int initial = page.getRowCount();
        page.clickAddLine();
        Assert.assertEquals(page.getRowCount(), initial + 1, "Thêm thất bại");
        
        page.deleteLine(0);
        Assert.assertEquals(page.getRowCount(), initial, "Xóa thất bại");
    }

    @Test(priority = 6, description = "PIS-6-TC-06,07: Gợi ý & Auto-fill")
    public void TC06_AutoFill() {
        page.clickAddLine(); 
        
        // [LƯU Ý] Thay "ok14" bằng tên thuốc CÓ THẬT trong kho của bạn
        String tenThuocCoThat = "thuốc đỏ"; 
        
        page.fillMedicine(0, tenThuocCoThat, true); 
        
        Assert.assertNotEquals(page.getCellText(0, 5), "-", "Giá chưa load"); 
        Assert.assertNotEquals(page.getCellText(0, 7), "-", "HSD chưa load");
        log("TC06 OK");
    }

//    @Test(priority = 7, description = "PIS-6-TC-08,09: Tính tiền")
//    public void TC07_Calculation() {
//        // --- Dòng 1 ---
//        page.fillMedicine(0, "Paracetamol", true);
//        page.fillQuantity(0, "10");
//        
//        page.waitForLineCalculation(0);
//        Assert.assertNotEquals(page.getCellText(0, 8), "0 ₫", "Thành tiền dòng 1 chưa tính");
//
//        // --- Dòng 2 ---
//        page.clickAddLine();
//        page.fillMedicine(1, "Ibuprofen", true);
//        page.fillQuantity(1, "5");
//        
//        page.waitForLineCalculation(1);
//        
//        // Chờ tổng tiền cập nhật
//        try { Thread.sleep(500); } catch (Exception e) {}
//        
//        Assert.assertNotEquals(page.getGrandTotal(), "0 ₫", "Tổng tiền phiếu chưa tính");
//    }
//
//    @Test(priority = 8, description = "PIS-6-TC-11,12: Validate Tồn kho")
//    public void TC08_StockValidation() {
//        driver.navigate().refresh(); 
//        
//        page.clickAddLine();
//        page.fillMedicine(0, "ok14", true); 
//        
//        // Case nhập quá tồn
//        page.fillQuantity(0, "999999"); 
//        page.clickSave();
//        
//        boolean isErrorShown = page.isErrorMessageDisplayed("quá số lượng") || page.isErrorMessageDisplayed("tồn");
//        boolean isButtonDisabled = !page.isSaveButtonEnabled();
//        Assert.assertTrue(isErrorShown || isButtonDisabled, "Hệ thống không chặn khi xuất quá tồn!");
//
//        // Case nhập đúng
//        page.fillQuantity(0, "1"); 
//        Assert.assertTrue(page.isSaveButtonEnabled(), "Nút lưu bị disable dù nhập đúng");
//    }
//
//    @Test(priority = 9, description = "PIS-6-TC-13: Validate bắt buộc")
//    public void TC09_Required() {
//        driver.navigate().refresh(); 
//        page.clickAddLine();
//        
//        page.fillMedicine(0, "ok14", true);
//        page.fillQuantity(0, ""); 
//        
//        page.clickSave();
//        Assert.assertTrue(page.isErrorMessageDisplayed("bắt buộc") || page.isErrorMessageDisplayed("required"), 
//            "Không bắt lỗi trống số lượng");
//    }
//
//    @Test(priority = 10, description = "PIS-6-TC-14: Lưu thành công")
//    public void TC10_SaveSuccess() {
//        driver.navigate().refresh(); 
//        
//        page.selectDepartment("Khoa Dược");
//        page.clickAddLine();
//        page.fillMedicine(0, "ok14", true);
//        page.fillQuantity(0, "1"); 
//        
//        page.clickSave();
//        
//        Assert.assertTrue(page.isSuccessModalDisplayed(), "Modal thành công không hiện!");
//        page.closeSuccessModal();
//    }
//    
//    @Test(priority = 11, description = "PIS-6-TC-15: Lưu thất bại")
//    public void TC11_SaveFail() {
//        driver.navigate().refresh(); 
//        page.clickSave(); 
//        Assert.assertTrue(page.getTitle().contains("Tạo phiếu xuất kho"), "Trang bị chuyển sai");
//    }
}