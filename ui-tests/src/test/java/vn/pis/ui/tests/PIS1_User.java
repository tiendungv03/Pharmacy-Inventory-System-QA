package vn.pis.ui.tests;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.LoginPage;
import vn.pis.ui.pages.UserPage;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.time.Instant;
import java.util.List;

import static vn.pis.ui.util.TestEnv.*;

@Listeners(vn.pis.ui.tests.PIS2_ConsoleLogger.class)
public class PIS1_User extends BaseTest {

    // Biến toàn cục (dùng chung cho cả class)
    private UserPage page;

    // ===== Helpers =====
    private void log(String msg){
        String line = "[PIS1] " + msg;
        System.out.println(line);         
        Reporter.log(line, true);         
    }
    
    // Tạo chuỗi unique để tránh trùng lặp dữ liệu test
    private String unique(String p){ return p + "_" + Instant.now().toEpochMilli(); }

    // ============================================================
    // 1. SETUP: CHẠY 1 LẦN DUY NHẤT
    // ============================================================
    @BeforeClass(alwaysRun = true)
    public void setupOnce() {
        log("=== SETUP: Đăng nhập Admin & Khởi tạo Page ===");
        
        // 1. Đăng nhập
        new LoginPage(driver).open(BASE_URL + "/login");
        new LoginPage(driver).login(ADMIN_USER, ADMIN_PASS);

        // 2. Khởi tạo Page Object (1 lần)
        page = new UserPage(driver);
        
        // 3. Mở trang User
        page.open();
    }

    // ============================================================
    // 2. CHECK STATE: CHẠY TRƯỚC MỖI TEST CASE (Cơ chế phục hồi)
    // ============================================================
    @BeforeMethod(alwaysRun = true)
    public void checkPageState(java.lang.reflect.Method m) {
        log("▶ BẮT ĐẦU TC: " + m.getName());
        
        String currentUrl = driver.getCurrentUrl();
        
        // 1. Kiểm tra nếu bị văng ra Login -> Đăng nhập lại
        boolean isLoginPage = currentUrl.contains("login") || 
                              !driver.findElements(By.cssSelector("button[type='submit']")).isEmpty();

        if (isLoginPage) {
            log("⚠️ Session chết! Đang đăng nhập lại...");
            new LoginPage(driver).login(ADMIN_USER, ADMIN_PASS);
            page.open(); 
            return;
        }
        
        // 2. Nếu đang ở trang khác -> Quay về trang User
        if (!currentUrl.contains("users")) { // Giả định URL là /users
             page.open();
             return;
        }
        
        // 3. (Optional) Nếu có dialog đang mở -> Đóng hoặc Refresh
        // driver.navigate().refresh(); // Refresh để xóa sạch trạng thái cũ (nếu muốn an toàn tuyệt đối)
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(java.lang.reflect.Method m){
        log("■ KẾT THÚC TC: " + m.getName());
    }

    // ============================================================
    // TEST CASES (Đã bỏ các dòng khởi tạo lặp lại)
    // ============================================================

    /**
     * Task 3: Kiểm thử chức năng Hiển thị danh sách người dùng
     */
    @Test(priority = 1)
    public void TC01_Display_User_List() {
        log("Xác minh bảng hiển thị");
        Assert.assertTrue(page.isTableVisible(), "Bảng người dùng không hiển thị");
        
        List<String> headers = page.getHeaderTexts();
        log("Các cột tìm thấy: " + headers);
        
        Assert.assertTrue(headers.stream().anyMatch(h -> h.matches("(?i).*tên đăng nhập.*")), "Thiếu cột Tên đăng nhập");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.matches("(?i).*email.*")), "Thiếu cột Email");
        Assert.assertTrue(page.getRowCount() > 0, "Bảng không có dữ liệu user nào");
        
        captureScreenshot("PIS1_TC01_UI_Display");
    }

    /**
     * Test Case chụp hình Popup (Hình 4.4.3)
     */
    @Test(priority = 2)
    public void TC02_A_Verify_Create_User_Popup_UI() {
        log("Click nút 'Thêm người dùng mới'");
        page.clickAddUser();

        try { Thread.sleep(1000); } catch (Exception e) {}
        captureScreenshot("Hinh_4_4_3_Popup_Them_Nguoi_Dung");
        
        Assert.assertTrue(driver.getPageSource().contains("Tên đăng nhập"), "Thiếu label Tên đăng nhập");
        log("TC02_A OK");
        
        driver.navigate().refresh(); // Đóng popup
    }

    /**
     * Test Case chụp hình Thêm thành công (Hình 4.4.4)
     */
    @Test(priority = 3)
    public void TC02_B_Create_New_User_Success() {
        String uname = unique("user");
        String fullname = "Auto Tester";
        String email = uname + "@test.com";
        String phone = "09" + ((int)(Math.random() * 90000000) + 10000000);
        String pass = "Password123!";
        String role = "Dược sĩ";

        page.clickAddUser();
        page.fillCreateForm(uname, fullname, email, phone, pass, role);
        page.submitCreateForm(); 

        // [FIX] Search user vừa tạo để verify (tránh phân trang)
        page.searchUser(uname);
        try { Thread.sleep(1500); } catch (Exception e) {}

        Assert.assertTrue(page.isUserRowPresent(uname), "User mới tạo không xuất hiện!");
        
        captureScreenshot("Hinh_4_4_4_Them_Thanh_Cong");
        log("TC02_B OK");
        
        driver.navigate().refresh(); // Reset search
    }
    
    @Test(priority = 4, description = "PIS-1-TC-09: Validate trường bắt buộc (Để trống)")
    public void TC05_Create_Validate_Required() {
        page.clickAddUser();
        // Không điền gì cả, bấm Lưu luôn
        page.submitCreateForm();
        
        boolean hasError = !driver.findElements(By.xpath("//*[contains(@class,'text-red') or contains(@class,'error')]")).isEmpty();
        boolean isDialogStillOpen = !driver.findElements(By.xpath("//div[@role='dialog']")).isEmpty();
        
        Assert.assertTrue(hasError || isDialogStillOpen, "Hệ thống không báo lỗi khi để trống!");
        
        captureScreenshot("PIS1_TC05_Validate_Required");
        driver.navigate().refresh(); 
        log("TC05 OK");
    }

    @Test(priority = 5, description = "PIS-1-TC-12: Validate trùng Tên đăng nhập")
    public void TC06_Create_Validate_Duplicate() {
        // Lấy user có sẵn (dòng đầu tiên)
        String existingUser = page.getUsernamesInTable().get(0);
        log("Thử tạo trùng user: " + existingUser);

        page.clickAddUser();
        page.fillCreateForm(existingUser, "Duplicate User", "dup@test.com", "0912312312", "Pass123!", "Dược sĩ");
        page.submitCreateForm();

        boolean isDialogStillOpen = !driver.findElements(By.xpath("//div[@role='dialog']")).isEmpty();
        Assert.assertTrue(isDialogStillOpen, "Dialog đã đóng -> Có thể hệ thống đã cho phép tạo trùng!");

        captureScreenshot("PIS1_TC06_Validate_Duplicate");
        driver.navigate().refresh(); 
        log("TC06 OK");
    }
    
    /**
     * Task 7: Kiểm thử chức năng Tìm kiếm người dùng
     */
    
    
  
    @Test(priority = 6)
    public void TC03_Search_User_Success() {
        List<String> allUsers = page.getUsernamesInTable();
        if (allUsers == null || allUsers.isEmpty()) {
            log("SKIP: Không có user nào để test search");
            return;
        }
        
        String keyword = allUsers.get(0); 
        log("Từ khóa tìm kiếm: " + keyword);

        page.searchUser(keyword);

        List<String> results = page.getUsernamesInTable();
        boolean anyMatch = results.stream().anyMatch(u -> u.contains(keyword));
        Assert.assertTrue(anyMatch, "Kết quả tìm kiếm không chứa từ khóa: " + keyword);
        
        log("TC03 OK");
        driver.navigate().refresh();
    }
    
    
    @Test(priority = 7, description = "PIS-1-TC-05: Tìm kiếm user KHÔNG tồn tại")
    public void TC03_Search_NonExisting_User() {
        String invalidUser = "ten_khong_ton_tai_" + System.currentTimeMillis();
        log("Tìm kiếm user ảo: " + invalidUser);
        
        page.searchUser(invalidUser);
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        
        int rowCount = page.getRowCount();
        Assert.assertEquals(rowCount, 0, "Lẽ ra không được tìm thấy user nào");
        
        captureScreenshot("PIS1_TC03_Search_Empty");
        log("TC03 OK");
        
        driver.navigate().refresh();
    }

    /**
     * QA-Task 5: Kiểm thử chức năng Chỉnh sửa người dùng
     */
    @Test(priority = 8)
    public void TC04_Edit_User_Success() {
        // Refresh để đảm bảo bảng tải lại đầy đủ
        driver.navigate().refresh(); 

        String username = page.getFirstEditableUsername();
        log("User được chọn để sửa: " + username);

        page.openEditUser(username);

        String newName  = "Updated " + System.currentTimeMillis();
        String newPhone = "03" + ((int)(Math.random() * 90000000) + 10000000);

        log("Nhập thông tin mới: " + newName);
        page.fillEditForm(newName, newPhone);
        page.submitEditForm();

        // Verify
        page.searchUser(username); // Tìm lại user đó để check
        try { Thread.sleep(1000); } catch (Exception e) {}
        
        Assert.assertTrue(page.isUserRowPresent(username), "Không tìm thấy user sau khi sửa");
        Assert.assertTrue(driver.getPageSource().contains(newPhone), "Số điện thoại mới chưa được cập nhật hiển thị");
        
        captureScreenshot("PIS1_TC07_Edit_Success");
        log("TC04 OK");
    }

    /**
     * QA-Task 6: Kiểm thử chức năng Khóa / Mở khóa
     */
    @Test(priority = 9)
    public void TC05_Lock_Unlock_User() {
        driver.navigate().refresh();

        String username = page.getFirstActiveUsername();
        if (username == null) {
            log("SKIP: Không tìm thấy user nào đang Hoạt động để khóa");
            return;
        }
        
        log("1. Thực hiện KHÓA user: " + username);
        page.lockUser(username);
        Assert.assertTrue(page.getStatus(username).contains("Khóa"), "Trạng thái chưa đổi sang Khóa");
        captureScreenshot("PIS1_TC08_Lock_Success");

        log("2. Thực hiện MỞ KHÓA user: " + username);
        page.unlockUser(username);
        Assert.assertTrue(page.getStatus(username).contains("Hoạt động"), "Trạng thái chưa đổi sang Hoạt động");
        
        log("TC05 OK");
    }
    
    // ==========================================
    // HÀM HỖ TRỢ CHỤP MÀN HÌNH
    // ==========================================
    public void captureScreenshot(String fileName) {
        try {
            String path = "./ScreenShots/" + fileName + ".png";
            org.openqa.selenium.TakesScreenshot ts = (org.openqa.selenium.TakesScreenshot) driver;
            java.io.File source = ts.getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            java.io.File destination = new java.io.File(path);
            
            if (!destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }
            org.openqa.selenium.io.FileHandler.copy(source, destination);
            log("📸 Đã lưu ảnh tại: " + path);
        } catch (Exception e) {
            log("⚠️ Lỗi chụp màn hình: " + e.getMessage());
        }
    }
}