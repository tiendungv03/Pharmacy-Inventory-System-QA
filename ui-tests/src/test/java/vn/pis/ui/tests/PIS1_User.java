package vn.pis.ui.tests;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.*;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.time.Duration;
import java.time.Instant; // Thêm lại
import java.util.List;
import java.util.NoSuchElementException;

import static vn.pis.ui.util.TestEnv.*;


@Listeners(vn.pis.ui.tests.PIS2_ConsoleLogger.class)
public class PIS1_User extends BaseTest {

    // ===== Helpers (Thêm lại unique) =====
    private void log(String msg){
        String line = "[PIS1] " + msg;
        System.out.println(line);         
        Reporter.log(line, true);         
    }
    
    // Thêm lại hàm unique() để tạo test data
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
     * Task 3 (SCRUM-232): Kiểm thử chức năng Hiển thị danh sách người dùng
     */
    @Test(priority = 1)
    public void TC01_Display_User_List() {
        log("MỞ trang login và đăng nhập");
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);

        log("Đi tới trang Quản lý người dùng");
        UserPage page = new UserPage(driver);
        page.open();

        log("Xác minh các cột cơ bản và số lượng hàng");
        Assert.assertTrue(page.isTableVisible(), "Bảng người dùng không hiển thị");
        
        List<String> headers = page.getHeaderTexts();
        Assert.assertTrue(headers.stream().anyMatch(h -> h.matches("(?i).*tên đăng nhập.*")), "Thiếu cột Tên đăng nhập");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.matches("(?i).*email.*")), "Thiếu cột Email");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.matches("(?i).*vai trò.*")), "Thiếu cột Vai trò");
        Assert.assertTrue(headers.stream().anyMatch(h -> h.matches("(?i).*trạng thái.*")), "Thiếu cột Trạng thái");
        
        Assert.assertTrue(page.getRowCount() > 0, "Bảng không có dữ liệu");
        log("TC01 OK - Hiển thị danh sách người dùng thành công");
    }

    /**
     * Task 4 (SCRUM-166): Kiểm thử chức năng Thêm mới người dùng
     */
    @Test(priority = 2)
    public void TC02_Create_New_User_Success() {
        log("Chuẩn bị test data (username, email...)");
        String uname = unique("auto_user");
        String fullname = "Auto User";
        String email = uname + "@example.com";
        String phone = "090" + ((int)(Math.random() * 9_000_000) + 1_000_000);
        String role = "Dược sĩ"; // Lấy từ ảnh (image_f75b7f.png)

        log("Mở trang Quản lý người dùng (Nếu chưa mở)");
        UserPage page = new UserPage(driver);
        // Giả định login từ TC01 vẫn còn session
        // (Nếu không, chúng ta cần thêm code login.open() và login.login() ở đây)
        page.open(); 

        log("Mở dialog 'Thêm người dùng mới'");
        page.clickAddUser();

        log("Điền form với Tên đăng nhập: " + uname);
        page.fillCreateForm(uname, fullname, email, phone, "Password123!", role);
        
        log("Submit form");
        page.submitCreateForm(); // Hàm này sẽ chờ dialog biến mất

        log("Xác minh: Kiểm tra user mới xuất hiện trong bảng");
        // Hàm isUserRowPresent đã bao gồm wait
        Assert.assertTrue(page.isUserRowPresent(uname), "Không thấy user mới sau khi tạo: " + uname);
        log("TC02 OK - Thêm mới thành công " + uname);
    }
    
    

    /**
     * QA-Task 5: Kiểm thử chức năng Chỉnh sửa người dùng
     */
    @Test(priority = 3)
    public void TC04_Edit_User_Success() {
        log("MỞ trang Quản lý người dùng");
        UserPage page = new UserPage(driver);
        page.open();

        log("Chọn user đầu tiên để chỉnh sửa (bỏ qua 'Đang tải...')");
        // DÙNG helper đã sửa trong UserPage, KHÔNG dùng get(0) trực tiếp nữa
        String username = page.getFirstEditableUsername();

        log("Mở popup Chỉnh sửa user: " + username);
        page.openEditUser(username);

        String updatedName  = "Updated Name " + System.currentTimeMillis();
        String updatedPhone = "09" + ((int)(Math.random() * 90000000) + 10000000);

        log("Điền form chỉnh sửa: " + updatedName + ", " + updatedPhone);
        page.fillEditForm(updatedName, updatedPhone);

        log("Lưu thay đổi");
        page.submitEditForm();

        // 🚩 Thêm 1 wait nhỏ: chờ phone trong bảng đổi sang giá trị mới
        log("KIỂM TRA: Dòng user đã có giá trị cập nhật");
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        shortWait.until(d -> {
            try {
                String phoneInRow = d.findElement(
                        By.xpath("//table//tr[.//td[normalize-space()='" + username + "']]//td[4]")
                ).getText().trim();
                return updatedPhone.equals(phoneInRow);
            } catch (NoSuchElementException e) {
                return false;
            }
        });

        // Vẫn giữ assert tìm lại user
        Assert.assertTrue(
            page.isUserRowPresent(username),
            "Không tìm thấy lại user sau khi chỉnh sửa"
        );

        // Kiểm tra SĐT đã update đúng cột 4 (username=1, họ tên=2, email=3, phone=4, role=5, trạng thái=6)
        String rowPhone = driver.findElement(
                By.xpath("//table//tr[.//td[normalize-space()='" + username + "']]//td[4]")
        ).getText().trim();

        Assert.assertEquals(rowPhone, updatedPhone, "Số điện thoại không được cập nhật chính xác");

        log("TC04 OK - Chỉnh sửa người dùng thành công");
    }


    /**
     * QA-Task 6 (Functional - Lock): Kiểm thử chức năng Khóa người dùng
     */
    /**
     * QA-Task 6 (Functional - Lock): Kiểm thử chức năng Khóa người dùng
     */
    @Test(priority = 5)
    public void TC05_Lock_User_Success() {
        log("MỞ trang Quản lý người dùng");
        UserPage page = new UserPage(driver);
        page.open();

        log("Lấy 1 user đang Hoạt động để test khóa");
        String username = page.getFirstActiveUsername();
        log("Thực hiện Khóa user: " + username);

        page.lockUser(username);

        log("KIỂM TRA: trạng thái đã đổi sang Khóa");
        Assert.assertTrue(page.getStatus(username).contains("Khóa"));
    }

    @Test(priority = 6)
    public void TC06_Unlock_User_Success() {
        log("MỞ trang Quản lý người dùng");
        UserPage page = new UserPage(driver);
        page.open();

        // chuẩn bị: nếu đang Hoạt động thì khóa trước 1 lần
        String username = page.getFirstActiveUsername();
        if (page.getStatus(username).contains("Hoạt động")) {
            page.lockUser(username);
        }

        log("Mở khóa user: " + username);
        page.unlockUser(username);

        log("KIỂM TRA: trạng thái đã đổi sang Hoạt động");
        Assert.assertTrue(page.getStatus(username).contains("Hoạt động"));
    }


    
    /**
     * 
	Task 7: Kiểm thử chức năng Tìm kiếm người dùng.
	
     */
    /**
     * Task 7 (QA-Task 7): Kiểm thử chức năng Tìm kiếm người dùng
     */
    @Test(priority = 3)
    public void TC03_Search_User_Success() {
        log("Đi tới trang Quản lý người dùng");
        UserPage page = new UserPage(driver);
        page.open();

        log("Lấy username ở hàng đầu tiên làm dữ liệu tìm kiếm");
        java.util.List<String> allUsers = page.getUsernamesInTable();
        Assert.assertTrue(allUsers.size() > 0, "Không có user nào trong bảng để test search");
        String keyword = allUsers.get(0);      // vd: auto_user_...

        int beforeCount = allUsers.size();

        log("Nhập keyword vào ô tìm kiếm: " + keyword);
        page.searchUser(keyword);

        log("Kiểm tra kết quả sau khi search");
        java.util.List<String> filtered = page.getUsernamesInTable();
        Assert.assertTrue(filtered.size() > 0, "Search trả về 0 kết quả, không đúng mong đợi");
        Assert.assertTrue(filtered.size() <= beforeCount,
                "Sau khi search, số dòng không được nhiều hơn trước");

        // Chỉ cần ÍT NHẤT 1 dòng khớp keyword là pass
        boolean anyMatch = filtered.stream().anyMatch(u -> u.contains(keyword));
        Assert.assertTrue(anyMatch,
                "Không có dòng nào khớp keyword. Expected chứa: " + keyword);

        log("TC03 OK - Tìm kiếm người dùng hoạt động (có trả về kết quả khớp keyword)");
    }
  



   
}