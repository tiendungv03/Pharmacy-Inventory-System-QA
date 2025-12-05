package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HistoryPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public HistoryPage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(15));
    }

    private void log(String msg) {
        System.out.println("[PIS7][PAGE] " + msg);
    }

    // ---------- MENU / TITLE ----------
    private final By menuHistoryLink = By.xpath(
            "//a[contains(@href,'/history') or contains(@href,'/transactions')][.//span[contains(.,'Lịch sử') or contains(.,'Giao dịch')]]"
    );
    private final By pageTitle = By.xpath("//h1[contains(.,'Lịch sử') or contains(.,'Danh sách giao dịch')]");

    // ---------- BỘ LỌC (FILTERS) & TÌM KIẾM ----------
    // [NEW] Ô tìm kiếm
    private final By searchInput = By.xpath("//input[@placeholder='Tìm kiếm...' or contains(@class, 'search')]");

    private final By filterTypeBtn = By.xpath("(//button[@role='combobox'])[1]");
    private final By filterWarehouseBtn = By.xpath("(//button[@role='combobox'])[2]");
    
    // [NEW] Giả định bộ lọc thời gian là combobox thứ 3
    private final By filterTimeBtn = By.xpath("(//button[@role='combobox'])[3]");

    // ---------- BẢNG DỮ LIỆU (TABLE) ----------
    private final By tableHeaders = By.xpath("//table//thead//th");
    private final By tableRows    = By.xpath("//table//tbody/tr");

    // [NEW] Lấy text của một ô cụ thể (dòng row, cột col)
    public String getCellText(int rowIndex, int colIndex) {
        By cell = By.xpath("//tbody/tr[" + rowIndex + "]/td[" + colIndex + "]");
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(cell)).getText();
        } catch (Exception e) { return ""; }
    }

    private By btnViewDetail(int rowIndex) {
        return By.xpath("(//table//tbody/tr)[" + rowIndex + "]//button[contains(.,'Xem') or .//*[name()='svg']]");
    }

    // ---------- PHÂN TRANG (PAGINATION) ----------
    private final By paginationNext = By.xpath("//button[contains(.,'Sau') or contains(@aria-label,'Next')]");
    
    // [NEW] Nút chọn số dòng hiển thị (Page Size)
    private final By pageSizeBtn = By.xpath("//div[contains(@class,'pagination')]//div[contains(@role,'button') or contains(@class,'select')]");
    
    
 // Selector cho nút "Trước"
    private By previousButton = By.xpath("//button[contains(text(), 'Trước')]"); 
    // Selector cho nút "Sau"
    private By nextButton = By.xpath("//button[contains(text(), 'Sau')]");


    // ---------- MODAL CHI TIẾT (Cho TC-11, TC-12) ----------
    // [NEW]
    private final By modalDialog = By.xpath("//div[@role='dialog']");
    private final By modalContent = By.xpath("//div[@role='dialog']//div[contains(@class,'body') or contains(@class,'content')]");
    private final By closeModalBtn = By.xpath("//div[@role='dialog']//button[contains(@aria-label,'Close') or contains(.,'Đóng')]");


    // ========= ACTIONS =========

    public void open() {
        log("Mở menu Lịch sử giao dịch");
        try {
            WebElement link = wait.until(ExpectedConditions.visibilityOfElementLocated(menuHistoryLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
            link.click();
        } catch (TimeoutException e) {
            log("Không thấy menu, thử mở trực tiếp URL /history");
            driver.get("http://localhost:3000/history");
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        log("Đã vào màn hình Lịch sử");
    }

    public List<String> getTableHeaders() {
        List<WebElement> headers = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(tableHeaders));
        List<String> headerTexts = new ArrayList<>();
        for (WebElement h : headers) {
            headerTexts.add(h.getText().trim());
        }
        return headerTexts;
    }

    // ---------------------------------------------------------
    //  HELPER FUNCTIONS
    // ---------------------------------------------------------

    private void performFilterSelection(By btnLocator, String optionName) {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnLocator));
        btn.click();
        By optionLocator = By.xpath("//div[@role='option']//span[contains(text(), '" + optionName + "')]");
        WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(optionLocator));
        option.click();
        try { Thread.sleep(500); } catch (InterruptedException e) {} // Chờ load nhẹ
    }

    public void filterByType(String typeName) {
        log("Thực hiện lọc theo Loại: " + typeName);
        performFilterSelection(filterTypeBtn, typeName);
    }

    public void filterByWarehouse(String warehouseName) {
        log("Thực hiện lọc theo Kho: " + warehouseName);
        performFilterSelection(filterWarehouseBtn, warehouseName);
    }
    
  

    // [NEW] Hàm lọc thời gian
    public void filterByTime(String timeRange) {
        log("Lọc thời gian: " + timeRange);
        try {
            performFilterSelection(filterTimeBtn, timeRange);
        } catch (Exception e) {
            log("⚠️ Không tìm thấy nút lọc thời gian (hoặc UI thay đổi).");
        }
    }

    // [NEW] Đổi page size
    public void changePageSize(String size) {
        log("Đổi số dòng hiển thị: " + size);
        try {
            performFilterSelection(pageSizeBtn, size);
        } catch (Exception e) {
            log("⚠️ Không tìm thấy nút đổi page size.");
        }
    }

    public int getRowCount() {
        try {
            return driver.findElements(tableRows).size();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getRowTypeText(int rowIndex) {
        By cellXpath = By.xpath("//tbody/tr[" + rowIndex + "]/td[1]"); 
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(cellXpath));
        try {
            wait.withTimeout(Duration.ofSeconds(5)).until(d -> {
                String text = element.getText();
                return !text.contains("Đang tải") && !text.isEmpty();
            });
        } catch (TimeoutException e) {
            log("Cảnh báo: Dữ liệu tải lâu quá 5s.");
        }
        return element.getText();
    }

    public void clickViewDetail(int rowIndex) {
        log("Click xem chi tiết dòng " + rowIndex);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnViewDetail(rowIndex)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        // [NEW] Chờ modal xuất hiện
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalDialog));
    }

    // [NEW] Lấy nội dung text trong Modal
    public String getDetailModalContent() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(modalContent)).getText();
        } catch (Exception e) { return ""; }
    }
    
    // [NEW] Đóng modal
    public void closeModal() {
        try {
            WebElement btn = driver.findElement(closeModalBtn);
            btn.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(modalDialog));
        } catch (Exception e) {}
    }

    public boolean isPaginationDisplayed() {
        try {
            return driver.findElement(paginationNext).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    // [NEW] Click trang tiếp theo
    public void clickNextPage() {
        driver.findElement(paginationNext).click();
        try { Thread.sleep(1000); } catch (Exception e) {}
    }



 //============KIỂM TRA ICON CỦA NHÂP VÀ XUÁT=============

    // Kiểm tra xem dòng thứ i có phải là Icon Nhập kho không (dựa trên class SVG bạn cung cấp)
    public boolean isImportIconDisplayed(int rowIndex) {
        try {
            // Tìm thẻ SVG hoặc path bên trong cột 1
            By iconImport = By.xpath("(//tbody/tr)[" + rowIndex + "]/td[1]//*[name()='svg' and contains(@class, 'arrow-down-to-line')]");
            return driver.findElements(iconImport).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Kiểm tra xem dòng thứ i có phải là Icon Xuất kho không
    public boolean isExportIconDisplayed(int rowIndex) {
        try {
            By iconExport = By.xpath("(//tbody/tr)[" + rowIndex + "]/td[1]//*[name()='svg' and contains(@class, 'arrow-up-from-line')]");
            return driver.findElements(iconExport).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Hàm chờ dữ liệu tải xong (tránh dòng 'Không có giao dịch nào')
    public void waitForDataToLoad() {
        try {
            // Chờ tối đa 5s để dòng 'Không có giao dịch' biến mất nếu thực sự có data
            wait.withTimeout(Duration.ofSeconds(5)).until(d -> {
                String text = d.findElement(By.xpath("//tbody/tr[1]/td[1]")).getText();
                return !text.contains("Đang tải") && !text.contains("Không có giao dịch");
            });
        } catch (Exception e) {
            // Nếu hết giờ mà vẫn không có data thì chấp nhận (có thể do DB rỗng thật)
        }
    }
    


 
 // Trong class HistoryPage.java

 // Selector cho nút "Trước" (Đã có: private By previousButton = By.xpath("//button[contains(text(), 'Trước')]") )
  
 // ...

 // Hàm bấm nút "Trước"
 public void clickPreviousButton() {
     log("Click nút 'Trước'");
     try {
         // 1. Chờ cho nút được tìm thấy và xuất hiện
         WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(previousButton));
         
         // 2. Sử dụng Javascript Executor để click, bỏ qua lỗi ElementClickIntercepted
         ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
         
     } catch (TimeoutException e) {
         log("Lỗi: Không tìm thấy nút 'Trước' trong thời gian chờ.");
         throw e; // Ném lại lỗi để TestNG báo fail nếu không tìm thấy nút
     }
     
     // Thêm hàm chờ để đảm bảo bảng dữ liệu đã load lại
     waitForDataToLoad(); 
 }

 // Hàm bấm nút "Sau"
//Hàm bấm nút "Sau"
public void clickNextButton() {
  log("Click nút 'Sau'");
  try {
      // 1. Chờ cho nút được tìm thấy (presence)
      WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(nextButton));
      
      // 2. Kiểm tra xem nút có bị disabled không (tùy chọn)
      String isDisabled = btn.getAttribute("disabled");
      if (isDisabled != null && isDisabled.equals("true")) {
          log("⚠ Cảnh báo: Nút 'Sau' đang bị disabled do thiếu dữ liệu hoặc đang ở trang cuối.");
          // Bạn có thể chọn báo lỗi ngay tại đây nếu đây là một điều kiện thất bại
          // throw new ElementNotInteractableException("Nút 'Sau' bị disabled.");
          
          // Nếu bạn vẫn muốn cố gắng click bằng JS, tiếp tục xuống bước 3
      }

      // 3. Sử dụng Javascript Executor để click, bỏ qua lỗi ElementClickIntercepted
      ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
      
  } catch (TimeoutException e) {
      log("Lỗi: Không tìm thấy nút 'Sau' trong thời gian chờ.");
      throw e;
  }
  
  // Thêm hàm chờ để đảm bảo bảng dữ liệu đã load lại
  waitForDataToLoad(); 
}

 // Hàm kiểm tra trạng thái nút "Sau" (ví dụ: bị Disable khi ở trang cuối)
 public boolean isNextButtonDisabled() {
     // Tùy thuộc vào cách nút bị disable trong HTML (ví dụ: dùng thuộc tính 'disabled' hoặc class 'disabled')
     return driver.findElement(nextButton).getAttribute("disabled") != null || 
            driver.findElement(nextButton).getAttribute("class").contains("disabled");
 }
    
 public String getRowTime(int rowIndex) {
	    // Thời gian là cột thứ 2 (colIndex = 2)
	    final int TIME_COL_INDEX = 2; 
	    log("Lấy thời gian dòng " + rowIndex);
	    return getCellText(rowIndex, TIME_COL_INDEX);
	}
    
    
    
    
}