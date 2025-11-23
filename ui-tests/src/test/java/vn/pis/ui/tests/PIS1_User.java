package vn.pis.ui.tests;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.LoginPage;
import vn.pis.ui.pages.UserPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.time.Instant;
import java.util.List;

import static vn.pis.ui.util.TestEnv.*;

@Listeners(vn.pis.ui.tests.PIS2_ConsoleLogger.class)
public class PIS1_User extends BaseTest {

    // ===== Helpers =====
    private void log(String msg){
        String line = "[PIS1] " + msg;
        System.out.println(line);         
        Reporter.log(line, true);         
    }
    
    // Tạo chuỗi unique để tránh trùng lặp dữ liệu test
    private String unique(String p){ return p + "_" + Instant.now().toEpochMilli(); }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(java.lang.reflect.Method m){
        log("▶ BẮT ĐẦU TC: " + m.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(java.lang.reflect.Method m){
        log("■ KẾT THÚC TC: " + m.getName());
    }

    // ====== TESTS ======

    /**
     * Task 3: Kiểm thử chức năng Hiển thị danh sách người dùng
     */
    @Test(priority = 1)
    public void TC01_Display_User_List() {
        log("Bước 1: Mở trang login và đăng nhập Admin");
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);

        log("Bước 2: Đi tới trang Quản lý người dùng");
        UserPage page = new UserPage(driver);
        page.open();

        log("Bước 3: Xác minh bảng hiển thị");
        Assert.assertTrue(page.isTableVisible(), "Bảng người dùng không hiển thị");
        
        List<String> headers = page.getHeaderTexts();
        log("Các cột tìm thấy: " + headers);
        
        Assert.assertTrue(headers.stream().anyMatch(h -> h.matches("(?i).*tên đăng nhập.*")), "Thiếu cột Tên đăng nhập");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.matches("(?i).*email.*")), "Thiếu cột Email");
        Assert.assertTrue(page.getRowCount() > 0, "Bảng không có dữ liệu user nào");
    }

    /**
     * [NEW] Test Case phụ để chụp ảnh màn hình Popup (Cho Hình 4.4.3 trong báo cáo)
     */

    
    /**
     * Test Case chụp hình Popup (Hình 4.4.3)
     */
    @Test(priority = 2)
    public void TC02_A_Verify_Create_User_Popup_UI() {
        log("Bước 1: Mở trang Quản lý người dùng");
        UserPage page = new UserPage(driver);
        page.open();

        log("Bước 2: Click nút 'Thêm người dùng mới'");
        page.clickAddUser();

        // Đợi xíu cho popup hiện rõ
        try { Thread.sleep(1000); } catch (Exception e) {}

        // === GỌI HÀM CHỤP ẢNH ===
        captureScreenshot("Hinh_4_4_3_Popup_Them_Nguoi_Dung");
        // ========================
        
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

        UserPage page = new UserPage(driver);
        page.open();

        page.clickAddUser();
        page.fillCreateForm(uname, fullname, email, phone, pass, role);
        page.submitCreateForm(); 

        Assert.assertTrue(page.isUserRowPresent(uname), "User mới tạo không xuất hiện!");
        
        // Đợi xíu cho thông báo/bảng hiện rõ
        try { Thread.sleep(1000); } catch (Exception e) {}

        // === GỌI HÀM CHỤP ẢNH ===
        captureScreenshot("Hinh_4_4_4_Them_Thanh_Cong");
        // ========================
        
        log("TC02_B OK");
    }
    
    
    /**
     * Task 7: Kiểm thử chức năng Tìm kiếm người dùng
     */
    @Test(priority = 4)
    public void TC03_Search_User_Success() {
        UserPage page = new UserPage(driver);
        page.open();

        List<String> allUsers = page.getUsernamesInTable();
        if (allUsers == null || allUsers.isEmpty()) {
            log("SKIP: Không có user nào để test search");
            return;
        }
        
        String keyword = allUsers.get(0); // Lấy tên user đầu tiên để tìm
        log("Từ khóa tìm kiếm: " + keyword);

        page.searchUser(keyword);

        List<String> results = page.getUsernamesInTable();
        boolean anyMatch = results.stream().anyMatch(u -> u.contains(keyword));
        Assert.assertTrue(anyMatch, "Kết quả tìm kiếm không chứa từ khóa: " + keyword);
        
        log("TC03 OK - Tìm kiếm hoạt động tốt.");
    }

    /**
     * QA-Task 5: Kiểm thử chức năng Chỉnh sửa người dùng
     */
    @Test(priority = 5)
    public void TC04_Edit_User_Success() {
        UserPage page = new UserPage(driver);
        page.open();
        
        // Refresh để đảm bảo bảng tải lại đầy đủ sau khi search
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
        log("Kiểm tra thông tin sau khi sửa");
        Assert.assertTrue(page.isUserRowPresent(username), "Không tìm thấy user sau khi sửa");
        
        // Logic verify phone (đơn giản hóa)
        Assert.assertTrue(driver.getPageSource().contains(newPhone), "Số điện thoại mới chưa được cập nhật hiển thị");
        
        log("TC04 OK - Chỉnh sửa thành công.");
    }

    /**
     * QA-Task 6: Kiểm thử chức năng Khóa / Mở khóa
     */
    @Test(priority = 6)
    public void TC05_Lock_Unlock_User() {
        UserPage page = new UserPage(driver);
        page.open();
        driver.navigate().refresh();

        String username = page.getFirstActiveUsername();
        if (username == null) {
            log("SKIP: Không tìm thấy user nào đang Hoạt động để khóa");
            return;
        }
        
        log("1. Thực hiện KHÓA user: " + username);
        page.lockUser(username);
        Assert.assertTrue(page.getStatus(username).contains("Khóa"), "Trạng thái chưa đổi sang Khóa");

        log("2. Thực hiện MỞ KHÓA user: " + username);
        page.unlockUser(username);
        Assert.assertTrue(page.getStatus(username).contains("Hoạt động"), "Trạng thái chưa đổi sang Hoạt động");
        
        log("TC05 OK - Khóa/Mở khóa hoạt động tốt.");
    }
    
 // ==========================================
 // HÀM HỖ TRỢ CHỤP MÀN HÌNH (TỰ ĐỘNG TẠO FOLDER)
 // ==========================================
 public void captureScreenshot(String fileName) {
     try {
         // 1. Tạo tên file ảnh (thêm đuôi .png)
         String path = "./ScreenShots/" + fileName + ".png";
         
         // 2. Thực hiện chụp
         org.openqa.selenium.TakesScreenshot ts = (org.openqa.selenium.TakesScreenshot) driver;
         java.io.File source = ts.getScreenshotAs(org.openqa.selenium.OutputType.FILE);
         java.io.File destination = new java.io.File(path);
         
         // 3. Tạo thư mục nếu chưa có
         if (!destination.getParentFile().exists()) {
             destination.getParentFile().mkdirs();
         }
         
         // 4. Lưu file
         org.openqa.selenium.io.FileHandler.copy(source, destination);
         log("📸 Đã lưu ảnh tại: " + path);
         
     } catch (Exception e) {
         log("⚠️ Lỗi chụp màn hình: " + e.getMessage());
     }
 }
    
}