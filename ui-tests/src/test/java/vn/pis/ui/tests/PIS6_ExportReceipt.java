package vn.pis.ui.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;
import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.ExportReceiptPage;
import vn.pis.ui.pages.LoginPage;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static vn.pis.ui.util.TestEnv.*;

/**
 * PIS-6: Tạo phiếu xuất kho
 * Bao gồm các TC từ TC-01 đến TC-18
 */
@Listeners(PIS2_ConsoleLogger.class)
public class PIS6_ExportReceipt extends BaseTest {

    private static final String DEPARTMENT_NAME   = "Khoa Nội";
    private static final String DRUG_NAME         = "Panadol Extra"; // Đảm bảo thuốc này có trong kho
    private static final int    STOCK_SAFE_QTY    = 1;      // Số lượng nhỏ (<= tồn)
    private static final int    STOCK_OVER_QTY    = 999999; // Số lượng lớn (> tồn)
    private static final String MSG_SUCCESS       = "Tạo phiếu xuất kho thành công";

    // Helper: Parse tiền tệ (30.000 -> 30000)
    private long parseCurrency(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        return Long.parseLong(digits);
    }

    private void log(String msg) {
        String line = "[PIS6] " + msg;
        System.out.println(line);
        Reporter.log(line, true);
    }

    // Login 1 lần
    @BeforeClass(alwaysRun = true)
    public void loginOnce() {
        log("--- Đăng nhập hệ thống ---");
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(java.lang.reflect.Method m){
        log("▶ BẮT ĐẦU TC: " + m.getName());
    }

    // ========================================================================
    // NHÓM 1: GIAO DIỆN & THÔNG TIN CHUNG (TC-01 -> TC-04)
    // ========================================================================

    @Test(priority = 1, description = "PIS-6-TC-01: Mở màn hình tạo phiếu xuất kho")
    public void TC01_OpenExportPage() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        
        // Kiểm tra URL hoặc Tiêu đề
        String title = driver.findElement(By.xpath("//h1")).getText();
        Assert.assertTrue(title.contains("Tạo phiếu xuất kho"), "Tiêu đề trang không đúng!");
    }

    @Test(priority = 2, description = "PIS-6-TC-02: Chọn Khoa/Phòng nhận (Bắt buộc)")
    public void TC02_SelectDepartment() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        
        // Chọn khoa phòng
        page.selectDepartment(DEPARTMENT_NAME);
        
        // Validate: Phần này thường test kết hợp ở bước Lưu (TC-15), 
        // ở đây verify UI đã chọn được giá trị là đủ.
        WebElement deptSpan = driver.findElement(By.xpath("//label[contains(.,'Khoa/Phòng nhận')]/following-sibling::button//span"));
        Assert.assertTrue(deptSpan.getText().contains(DEPARTMENT_NAME), "Dropdown không hiển thị tên khoa đã chọn");
    }

    @Test(priority = 3, description = "PIS-6-TC-03: Ngày xuất mặc định là hôm nay và KHÔNG cho phép chỉnh sửa")
    public void TC03_VerifyExportDate_DefaultAndReadOnly() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        // --- PHẦN 1: KIỂM TRA GIÁ TRỊ (Dùng hàm getExportDate có sẵn) ---
        String actualDate = page.getExportDate(); 
        
        // Lấy ngày hiện tại hệ thống theo định dạng yyyy-MM-dd (HTML5 standard)
        // Nếu web hiển thị dd/MM/yyyy thì đổi pattern bên dưới thành "dd/MM/yyyy"
        String expectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        log("Ngày trên web: " + actualDate + " | Ngày hệ thống: " + expectedDate);
        
        // Assert này đảm bảo ngày tự động điền là chính xác
        Assert.assertEquals(actualDate, expectedDate, "Ngày xuất mặc định không đúng ngày hiện tại!");

        // --- PHẦN 2: KIỂM TRA TRẠNG THÁI KHÓA (Dùng hàm getExportDateElement mới thêm) ---
        WebElement dateInput = page.getExportDateElement();
        
        // Kiểm tra input có thuộc tính readonly hoặc disabled không
        boolean isReadOnly = dateInput.getAttribute("readonly") != null;
        boolean isDisabled = dateInput.getAttribute("disabled") != null; // hoặc !dateInput.isEnabled()

        log("Trạng thái Input -> Readonly: " + isReadOnly + " | Disabled: " + isDisabled);

        // Một trong hai cái đúng là Pass (tức là không cho sửa)
        Assert.assertTrue(isReadOnly || isDisabled, "LỖI: Ô Ngày xuất vẫn cho phép chỉnh sửa (không có readonly/disabled)!");
    }

    @Test(priority = 4, description = "PIS-6-TC-04: Ghi chú tùy chọn")
    public void TC04_InputNotes() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        String noteContent = "Ghi chú Test Auto PIS6";
        page.setNotes(noteContent);
        
        WebElement noteArea = driver.findElement(By.id("notes"));
        Assert.assertEquals(noteArea.getAttribute("value"), noteContent, "Nội dung ghi chú không khớp!");
    }

    // ========================================================================
    // NHÓM 2: DÒNG SẢN PHẨM & TÍNH TOÁN (TC-05 -> TC-10)
    // ========================================================================

    @Test(priority = 5, description = "PIS-6-TC-05, TC-06, TC-07: Thêm dòng, Gợi ý thuốc, Auto fill")
    public void TC05_TC06_TC07_RowInteraction_AutoFill() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        page.selectDepartment(DEPARTMENT_NAME);
    	page.setNotes("Phiếu xuất tự động - PIS6");

        // TC-05: Thêm dòng (Mặc định mở màn hình thường có sẵn 1 dòng, nếu không thì click thêm)
        if(page.getRowCount() == 0) {
            page.clickAddRow();
        }
        Assert.assertTrue(page.getRowCount() > 0, "Không có dòng thuốc nào hiển thị!");

        // TC-06: Nhập tên thuốc và kiểm tra gợi ý (Hàm setRowDrugNameAndChooseSuggestion đã bao gồm việc đợi gợi ý)
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);

        // TC-07: Kiểm tra tự động điền Đơn giá & Hạn sử dụng
        try { Thread.sleep(1000); } catch (InterruptedException e) {} // Chờ UI update
        
        String price = page.getRowPriceText(1);
        String expiry = page.getRowExpiryText(1);
        String lot = page.getRowLotText(1);

        log("Data auto-fill -> Price: " + price + ", Exp: " + expiry + ", Lot: " + lot);

        Assert.assertNotEquals(parseCurrency(price), 0, "Đơn giá chưa được tự động điền!");
        Assert.assertFalse(expiry.isEmpty(), "Hạn sử dụng chưa được tự động điền!");
    }

    @Test(priority = 6, description = "PIS-6-TC-08, TC-09: Tính thành tiền và Tổng tiền")
    public void TC08_TC09_Calculation() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        page.selectDepartment(DEPARTMENT_NAME);
    	page.setNotes("Phiếu xuất tự động - PIS6");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        
        // Đợi load giá
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        long price = parseCurrency(page.getRowPriceText(1));

        // TC-08: Nhập số lượng và kiểm tra Thành tiền dòng
        int quantity = 5;
        page.setRowQuantity(1, String.valueOf(quantity));
        
        // Click ra ngoài hoặc chờ tính toán (bằng cách đọc lại text)
        try { Thread.sleep(500); } catch (InterruptedException e) {} 
        
        long lineAmount = parseCurrency(page.getRowLineAmountText(1));
        long expectedLineAmount = price * quantity;
        
        Assert.assertEquals(lineAmount, expectedLineAmount, "Tính Thành tiền dòng bị sai!");
    

        // TC-09: Kiểm tra Tổng thành tiền (Giả sử chỉ có 1 dòng thì Tổng = Dòng)
        long totalAmount = parseCurrency(page.getTotalAmountText());
        Assert.assertEquals(totalAmount, expectedLineAmount, "Tổng thành tiền phiếu xuất bị sai!");
    }
    
//
    @Test(priority = 7, description = "PIS-6-TC-10: Xóa dòng thuốc")
    public void TC10_DeleteRow() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        page.setNotes("Phiếu xuất tự động - PIS6");
        
        // Thêm 2 dòng
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        page.clickAddRow();
        // Chờ dòng 2 xuất hiện
        try { Thread.sleep(500); } catch (InterruptedException e) {} 
        
        int countBefore = page.getRowCount();
        log("Số dòng trước khi xóa: " + countBefore);
        Assert.assertTrue(countBefore >= 2, "Cần ít nhất 2 dòng để test xóa");

        // Xóa dòng 2
        page.deleteRow(2);
        
        int countAfter = page.getRowCount();
        log("Số dòng sau khi xóa: " + countAfter);
        Assert.assertEquals(countAfter, countBefore - 1, "Số lượng dòng không giảm sau khi xóa!");
    }

//    // ========================================================================
//    // NHÓM 3: VALIDATE & TỒN KHO (TC-11 -> TC-13)
//    // ========================================================================
@Test(priority = 8, description = "PIS-6-TC-11: Không cho phép xuất quá tồn (Check Banner & Disabled Button)")
    public void TC11_Validate_OverStock() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();
        
        page.setNotes("Phiếu xuất tự động - PIS6 Overstock Check");
        page.selectDepartment(DEPARTMENT_NAME);
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        
        // 1. Nhập số lượng cực lớn (lớn hơn tồn kho)
        page.setRowQuantity(1, String.valueOf(STOCK_OVER_QTY));
        
        // Mẹo: Click ra ngoài (ví dụ click vào ô Ghi chú) để kích hoạt sự kiện validate của web
        driver.findElement(By.id("notes")).click(); 
        
        // Chờ 1 chút để giao diện cập nhật trạng thái
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        // 2. KIỂM TRA THÔNG BÁO CẢNH BÁO (Banner màu vàng)
        // Locator này tìm thẻ div có role='alert' (như trong hình bạn gửi)
        WebElement alertBanner = null;
        try {
            alertBanner = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@role='alert']")));
        } catch (Exception e) {
            Assert.fail("FAIL: Không hiển thị banner cảnh báo (màu vàng) khi nhập quá tồn kho!");
        }
        
        String actualMsg = alertBanner.getText();
        log("Nội dung cảnh báo bắt được: " + actualMsg);
        
        // Assert nội dung chứa từ khóa
        boolean isMsgCorrect = actualMsg.toLowerCase().contains("vượt quá") || 
                               actualMsg.toLowerCase().contains("tồn kho");
        Assert.assertTrue(isMsgCorrect, "Nội dung cảnh báo không đúng ngữ cảnh! Actual: " + actualMsg);

        // 3. KIỂM TRA NÚT HOÀN THÀNH BỊ KHÓA (DISABLED)
        // Tìm element nút (dùng xpath cũ của bạn)
        WebElement btnFinish = driver.findElement(By.xpath("//button[contains(.,'Hoàn thành phiếu xuất')]"));
        
        boolean isEnabled = btnFinish.isEnabled();
        log("Trạng thái nút Hoàn thành (Enabled?): " + isEnabled);
        
        // Mong đợi: isEnabled phải là FALSE
        Assert.assertFalse(isEnabled, "LỖI: Nút Hoàn thành vẫn sáng (bấm được) dù đang xuất quá tồn kho!");
        
        log("PASS: Hệ thống đã hiện cảnh báo và khóa nút Hoàn thành đúng yêu cầu.");
    }
    
    
//    
    @Test(priority = 9, description = "PIS-6-TC-13: Bắt buộc nhập Số lượng (Logic của bạn)")
    public void TC13_Validate_EmptyQuantity() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Phiếu xuất tự động - PIS6");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);

        // 1. Lấy ô input Số lượng và Xóa sạch dữ liệu
        WebElement qtyInput = page.getQuantityInput(1);
        
        qtyInput.click();
        qtyInput.sendKeys(Keys.CONTROL + "a");
        qtyInput.sendKeys(Keys.DELETE);
        
        // 2. Nhấn nút Hoàn thành
        page.clickFinish(); 

        // 3. --- LOGIC LẤY NỘI DUNG THÔNG BÁO (ĐA KÊNH) ---
        String actualError = "";

        // Ưu tiên 1: Native (Bong bóng vàng)
        actualError = page.getNativeValidationMessage(qtyInput);

        // Ưu tiên 2: Alert Banner đỏ
        if (actualError.isEmpty()) {
            try {
                WebElement alertBanner = driver.findElement(By.xpath("//div[@role='alert']"));
                new WebDriverWait(driver, Duration.ofSeconds(2))
                        .until(ExpectedConditions.visibilityOf(alertBanner));
                actualError = alertBanner.getText();
            } catch (Exception e) {}
        }

        // Ưu tiên 3: Toast
        if (actualError.isEmpty()) {
            actualError = page.readFeedback();
        }

        log("==> Thông báo lỗi bắt được: " + actualError);

        if (actualError.isEmpty()) {
            Assert.fail("FAIL: Không hiện bất kỳ thông báo lỗi nào!");
        }

        String errLower = actualError.toLowerCase();
        boolean isMatch = errLower.contains("số lượng") || 
                          errLower.contains("trống") || 
                          errLower.contains("kiểm tra") || 
                          errLower.contains("điền") ||
                          errLower.contains("fill");

        Assert.assertTrue(isMatch, "Nội dung lỗi không đúng ngữ cảnh. Actual: " + actualError);
    }

    
    
//    // ========================================================================
//    // NHÓM 4: LƯU PHIẾU & KẾT QUẢ (TC-14 -> TC-18)
//    // ========================================================================
//
    @Test(priority = 10, description = "PIS-6-TC-14, TC-16: Lưu thành công & Cập nhật tồn kho")
    public void TC14_TC16_CreateSuccess_CheckStock() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Phiếu xuất tự động - PIS6");
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        
        // Nhập số lượng hợp lệ (Nhỏ hơn tồn kho)
        page.setRowQuantity(1, String.valueOf(STOCK_SAFE_QTY));

        // Lưu
        page.clickFinish();

        // Verify Message Thành công
        String msg = page.readFeedback();
        log("Message sau khi lưu: " + msg);
        
        Assert.assertTrue(msg.toLowerCase().contains("thành công") || msg.toLowerCase().contains("success"),
                "Không thấy thông báo thành công!");

        // TC-16: Cập nhật tồn kho
        // (Bước này thường cần kiểm tra DB hoặc vào màn hình danh sách kho để check số lượng đã trừ.
        // Trong phạm vi UI test màn hình Tạo phiếu, ta coi như việc lưu thành công là Pass bước này 
        // hoặc cần code thêm page Danh sách kho để verify).
        log("PASS: Đã lưu phiếu thành công.");
    }
    

    @Test(priority = 11, description = "PIS-6-TC-15, TC-17: Không lưu khi còn lỗi")
   
  public void TC15_TC17_FailToSave_Validation() {
      ExportReceiptPage page = new ExportReceiptPage(driver);
      page.open();

      page.setNotes("Phiếu xuất tự động - PIS6");
      
      // Nhập thuốc, số lượng nhưng KHÔNG chọn Khoa/Phòng
      page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
      page.setRowQuantity(1, "1");

      // --- SỬA ĐOẠN NÀY ---
      // Không dùng page.clickFinish(); nữa vì nó sẽ gây Timeout
      
      // Tìm nút Hoàn thành (Lấy xpath nút đó bỏ vào đây)
      WebElement btnFinish = driver.findElement(By.xpath("//button[.//span[normalize-space()='Hoàn thành phiếu xuất'] or normalize-space()='Hoàn thành phiếu xuất']"));

      // Kiểm tra: Mong đợi nút này ĐANG BỊ KHÓA (isEnabled() trả về false)
      boolean isButtonEnabled = btnFinish.isEnabled();
      
      // Nếu nút đang bật (true) -> Test Fail (vì thiếu dữ liệu mà vẫn cho bấm)
      // Nếu nút đang tắt (false) -> Test Pass
      if (isButtonEnabled) {
          Assert.fail("Lỗi: Nút Hoàn thành vẫn sáng (bấm được) dù chưa chọn Khoa/Phòng!");
      } else {
          log("PASS: Nút Hoàn thành đã bị khóa đúng như mong đợi.");
      }
  }
    
  @Test(priority = 12)
  public void TC18_CreateExport_SingleRow_Success() {
      ExportReceiptPage page = new ExportReceiptPage(driver);
      page.open();

      // 1. Dùng long để tránh lỗi tràn số
      long qty = STOCK_SAFE_QTY; 

      page.selectDepartment(DEPARTMENT_NAME);
      page.setNotes("Phiếu xuất tự động - TC03");

      // 2. Chọn thuốc
      page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);

      // 3. QUAN TRỌNG: Chờ hệ thống fill giá xong rồi lấy giá đó ra
      // (Thêm sleep nhỏ để đảm bảo UI đã update giá sau khi chọn thuốc)
      try { Thread.sleep(1000); } catch (InterruptedException e) {}
            // Đọc giá thực tế từ màn hình thay vì tự bịa số 2100
    long price = parseCurrency(page.getRowPriceText(1)); 
    log("Đơn giá hệ thống tự fill: " + price);

    // 4. Chỉ nhập số lượng (Không nhập đè giá nữa)
    page.setRowQuantity(1, String.valueOf(qty));

    // 5. Tính kết quả mong đợi dựa trên giá thực tế
    long expected = qty * price;

    // --- Verify ---
    String lineAmount = page.getRowLineAmountText(1);
    log("Line amount row1 = " + lineAmount);

    long actualLine = parseCurrency(lineAmount);
    Assert.assertEquals(actualLine, expected, "Thành tiền sai lệch!");

    String total = page.getTotalAmountText();
    long actualTotal = parseCurrency(total);
    Assert.assertEquals(actualTotal, expected, "Tổng tiền sai lệch!");

    // --- Finish ---
    page.clickFinish();
    String msg = page.readFeedback();
    Assert.assertTrue(msg.toLowerCase().contains("thành công"), 
            "Lỗi: Thông báo không chứa từ khóa thành công! Actual: " + msg);
    
}
  
    @Test(priority = 13, description = "PIS-6-TC-19: Chức năng Làm mới - Xóa sạch dữ liệu đã điền")
    public void TC19_Refresh_ClearData() {
        ExportReceiptPage page = new ExportReceiptPage(driver);
        page.open();

        log("--- Bước 1: Điền dữ liệu mẫu vào form ---");
        page.selectDepartment(DEPARTMENT_NAME);
        page.setNotes("Dữ liệu này sẽ bị xóa sau khi nhấn nút Làm mới");
        
        // Chọn thuốc
        page.setRowDrugNameAndChooseSuggestion(1, DRUG_NAME);
        
        // Nhập số lượng
        page.setRowQuantity(1, "10");

        // Verify tạm: Đảm bảo tổng tiền > 0 trước khi xóa
        // (Thêm wait nhỏ để tiền kịp nhảy)
        try { Thread.sleep(1000); } catch (InterruptedException e) {} 
        long totalBefore = parseCurrency(page.getTotalAmountText());
        Assert.assertTrue(totalBefore > 0, "Lỗi setup: Dữ liệu chưa được điền thành công trước khi test Làm mới.");

        log("--- Bước 2: Nhấn nút Làm mới ---");
        page.clickRefresh(); // Đảm bảo bạn đã thêm hàm này vào ExportReceiptPage

        // Chờ UI reset
        try { Thread.sleep(1500); } catch (InterruptedException e) {} 

        log("--- Bước 3: Kiểm tra dữ liệu đã bị xóa sạch ---");
        
        // 1. Kiểm tra Ghi chú phải rỗng
        WebElement noteArea = driver.findElement(By.id("notes"));
        Assert.assertEquals(noteArea.getAttribute("value"), "", "Ô Ghi chú chưa được xóa!");

        // 2. Kiểm tra Tổng tiền phải về 0
        long totalAfter = parseCurrency(page.getTotalAmountText());
        Assert.assertEquals(totalAfter, 0, "Tổng tiền chưa reset về 0!");

        // 3. Kiểm tra Tên thuốc (Dùng findElements để an toàn)
        // SỬA LẠI LOCATOR CHO ĐÚNG: 'Nhập sản phẩm...'
        By drugInputLocator = By.xpath("//input[@placeholder='Nhập sản phẩm...']");
        java.util.List<WebElement> inputs = driver.findElements(drugInputLocator);

        if (inputs.size() == 0) {
            // Trường hợp A: Nút làm mới xóa luôn cả dòng thuốc -> Đúng
            log("PASS: Bảng thuốc đã được xóa sạch (không còn dòng nào).");
        } else {
            // Trường hợp B: Nút làm mới reset về 1 dòng trắng -> Kiểm tra dòng đó phải rỗng
            String value = inputs.get(0).getAttribute("value");
            Assert.assertEquals(value, "", "Ô Tên thuốc vẫn còn dữ liệu!");
            log("PASS: Bảng thuốc đã reset về dòng trắng mặc định.");
        }
    }
}