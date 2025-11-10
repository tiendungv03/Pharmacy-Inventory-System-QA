package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

/**
 * CategoriesPage – Page Object cho màn Quản lý danh mục
 * Nhóm theo: Locators → Ctor → Navigation → Create → Read/Search → Update → Delete
 * → Pagination → Table utils → Feedback/Validation → Dialog control → Assertions → Helpers
 */
public class CategoriesPage {
    // =========================
    // Locators
    // =========================
    private final WebDriver driver;
    private final WebDriverWait wait;

    // --- MENU / NÚT ---
    private final By menuCategories = By.xpath("//span[contains(text(),'Quản lý danh mục')]");
    private final By btnAdd         = By.xpath("//button[contains(text(),'Thêm danh mục')]");

    // --- FORM INPUTS ---
    private final By iptName = By.xpath("//input[@id='name' or @name='name']");
    private final By iptDesc = By.xpath("//textarea[@id='description' or @name='description']");

    // --- DIALOG / OVERLAY ---
    // Lưu ý: id radix có thể thay đổi theo phiên.
    private final By dialogRoot = By.xpath("//div[starts-with(@id, 'radix-:r')]");
    // private final By dialogRoot = By.cssSelector("[role='dialog'], .ant-modal, .modal.show, .v-overlay__content, [data-state='open']");

    // --- SUBMIT BUTTON ---
    private final By btnSubmit = By.xpath("//button[@type='submit']");

    // --- TOAST/ALERT ---
    private final By anyToast = By.cssSelector(
            ".swal2-toast .swal2-title, .swal2-container .swal2-html-container," +
            ".toast-success, .alert-success, .alert.alert-success, [role='alert']"
    );

    // --- BẢNG / SEARCH / PHÂN TRANG ---
    private final By tableCategories = By.xpath("//table[@class='w-full']");
    private final By searchBox       = By.xpath("//input[@placeholder='Tìm kiếm danh mục...']");
    private final By pageSizeSelect  = By.xpath("//select[contains(@class,'page-size') or @name='pageSize' or @data-testid='page-size']");

    // Footer phân trang (giữ các thành phần thực sự dùng)
    private final By btnPrev   = By.xpath("//button[normalize-space()='Trước']");
    private final By btnNext   = By.xpath("//button[normalize-space()='Sau']");
    private final By pageLabel = By.xpath("//span[contains(@class,'text-sm')][contains(.,'Trang')]");
    private final By pageStats = By.xpath("//div[contains(@class,'text-sm')][contains(.,'Hiển thị')]");

    // Bảng và cells
    private final By tbodyRows = By.cssSelector("table.w-full tbody tr");

    // Popup menu & item trong mỗi dòng
    private final By actionMenuRoot = By.xpath("//*[(@role='menu') or contains(@class,'menu') or contains(@class,'DropdownMenu')]");

    // =========================
    // Constructor
    // =========================
    public CategoriesPage(WebDriver d) {
        this.driver = d;
        this.wait   = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    // =========================
    // Navigation
    // =========================
    public void open() {
        wait.until(ExpectedConditions.elementToBeClickable(menuCategories)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(btnAdd));
        wait.until(ExpectedConditions.presenceOfElementLocated(tableCategories));
    }

    // =========================
    // Create (Add)
    // =========================
    public void clickAddCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAdd)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(dialogRoot));
        wait.until(ExpectedConditions.visibilityOfElementLocated(iptName));
    }

    public void fillCategoryForm(String name, String desc) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(iptName)).clear();
        driver.findElement(iptName).sendKeys(name);

        wait.until(ExpectedConditions.visibilityOfElementLocated(iptDesc)).clear();
        driver.findElement(iptDesc).sendKeys(desc);
    }

    /** Submit form trong dialog, trả về message (alert/toast/validate). */
    public String submitCategoryForm() {
        WebElement dialog = driver.findElement(dialogRoot);
        WebElement submit = dialog.findElement(btnSubmit);
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        String message = "";

        // 1) Nếu có JS alert -> lấy text và accept
        try {
            Alert a = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.alertIsPresent());
            message = (a.getText() == null) ? "" : a.getText().trim();
            a.accept();
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//table[contains(@class,'w-full') or @data-testid='categories-table']")));
            return message;
        } catch (TimeoutException | NoAlertPresentException ignored) {}

        // 2) Kiểm tra lỗi validate ngay trong dialog (tên rỗng/trùng)
        try {
            By nearNameError = By.xpath("//div[@class='flex items-center gap-2 text-red-500 text-sm']");
            WebElement err = new WebDriverWait(driver, Duration.ofSeconds(1))
                    .until(ExpectedConditions.visibilityOfElementLocated(nearNameError));
            return err.getText() == null ? "" : err.getText().trim();
        } catch (TimeoutException ignored) {}

        // 3) Không có lỗi -> chờ dialog đóng (flow thành công bằng toast)
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(dialogRoot));
        } catch (UnhandledAlertException ua) {
            try {
                Alert late = driver.switchTo().alert();
                message = (late.getText() == null) ? "" : late.getText().trim();
                late.accept();
            } catch (NoAlertPresentException ignored2) {}
        }

        // 4) Thử đọc toast nếu có
        try {
            String toast = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(anyToast))
                    .getText();
            if (toast != null && !toast.isBlank()) message = toast.trim();
        } catch (TimeoutException ignored2) {}

        // 5) Chờ bảng (nếu flow thành công)
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody/tr[1]")));
        } catch (TimeoutException ignored3) {}

        return message;
    }

    // =========================
    // Read / Search
    // =========================
    public boolean isAdded(String name) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(6))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//table//td[normalize-space()='" + name + "']")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void search(String kw) {
        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox));
        box.clear();
        box.sendKeys(kw);
        box.sendKeys(Keys.ENTER);

        // chờ bảng refresh
        wait.until(ExpectedConditions.presenceOfElementLocated(tableCategories));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tbody/tr")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tbody[not(tr)]"))
        ));
    }

    public java.util.List<String> getAllCategoryNames() {
        wait.until(ExpectedConditions.presenceOfElementLocated(tableCategories));
        java.util.List<WebElement> rows = driver.findElements(tbodyRows);
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int i = 1; i <= rows.size(); i++) {
            String name = driver.findElement(cellByIndex(i, 2)).getText().trim();
            if (!name.isBlank()) names.add(name);
        }
        return names;
    }

    public boolean hasCategoryName(String name) {
        return driver.findElements(By.xpath("//table//td[normalize-space()='" + name + "']")).size() > 0;
    }

    public java.util.List<CategoryRow> getAllRows() {
        wait.until(ExpectedConditions.presenceOfElementLocated(tableCategories));
        java.util.List<WebElement> rows = driver.findElements(tbodyRows);
        java.util.List<CategoryRow> list = new java.util.ArrayList<>();
        for (int i = 1; i <= rows.size(); i++) {
            String code  = driver.findElement(cellByIndex(i, 1)).getText().trim();
            String name  = driver.findElement(cellByIndex(i, 2)).getText().trim();
            String desc  = driver.findElement(cellByIndex(i, 3)).getText().trim();
            String count = driver.findElement(cellByIndex(i, 4)).getText().trim();
            String date  = driver.findElement(cellByIndex(i, 5)).getText().trim();
            list.add(new CategoryRow(code, name, desc, count, date));
        }
        return list;
    }

    public void waitRowPresent(String name){
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table//td[normalize-space()='" + name + "']")));
    }

    // =========================
    // Update (Edit)
    // =========================
    public void clickEdit(String name) {
        openRowMenuAndChoose(name, "Chỉnh sửa"); // đổi "Edit" nếu UI tiếng Anh
        wait.until(ExpectedConditions.visibilityOfElementLocated(iptName));
    }

    // =========================
    // Delete
    // =========================
    /** Xóa danh mục theo tên, trả về message (alert/toast nếu có). */
    public String deleteCategory(String name) {
        WebElement kebab = wait.until(ExpectedConditions.elementToBeClickable(kebabInRow(name)));
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView({block:'center'});", kebab);
        kebab.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(actionMenuRoot));

        // chọn Xóa/Xoá/Delete
        By xoa = By.xpath(
            "//*[@role='menu']//*[contains(normalize-space(.),'Xóa') or contains(normalize-space(.),'Xoá') or contains(translate(normalize-space(.),'DELETE','delete'),'delete')]" +
            " | //div[contains(@class,'menu') or contains(@class,'DropdownMenu')]//*[contains(normalize-space(.),'Xóa') or contains(normalize-space(.),'Xoá') or contains(translate(normalize-space(.),'DELETE','delete'),'delete')]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(xoa)).click();

        // xác nhận
        By btnConfirm = By.xpath("//button[.='Xác nhận' or .='Xóa' or .='Xoá' or contains(@data-testid,'btn-confirm')]");
        wait.until(ExpectedConditions.elementToBeClickable(btnConfirm)).click();

        String message = "";
        // 1) ưu tiên alert
        try {
            Alert a = new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.alertIsPresent());
            message = (a.getText() == null) ? "" : a.getText().trim();
            a.accept();
        } catch (TimeoutException | NoAlertPresentException ignored) {
            // 2) fallback toast
            try {
                message = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(anyToast))
                        .getText().trim();
            } catch (TimeoutException ignored2) {}
        }

        // đợi bảng + row biến mất
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(tableCategories));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//table//td[normalize-space()='" + name + "']")));
        } catch (TimeoutException ignored3) {}

        return message;
    }

    // =========================
    // Pagination
    // =========================
    public boolean setPageSize(int size) {
        try {
            WebElement sel = wait.until(ExpectedConditions.visibilityOfElementLocated(pageSizeSelect));
            new Select(sel).selectByValue(String.valueOf(size));

            // chờ bảng refresh
            List<WebElement> before = driver.findElements(By.xpath("//table//tbody/tr"));
            if (!before.isEmpty()) {
                wait.until(ExpectedConditions.stalenessOf(before.get(0)));
            }
            wait.until(ExpectedConditions.presenceOfElementLocated(tableCategories));
            return true;
        } catch (NoSuchElementException | TimeoutException e) {
            return false;
        }
    }

    public boolean nextPage() {
        if (!canNext()) return false;
        String before = wait.until(ExpectedConditions.visibilityOfElementLocated(pageLabel)).getText();
        wait.until(ExpectedConditions.elementToBeClickable(btnNext)).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(pageLabel, before)));
        wait.until(ExpectedConditions.presenceOfElementLocated(tableCategories));
        return true;
    }

    public boolean prevPage() {
        if (!canPrev()) return false;
        String before = wait.until(ExpectedConditions.visibilityOfElementLocated(pageLabel)).getText();
        wait.until(ExpectedConditions.elementToBeClickable(btnPrev)).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(pageLabel, before)));
        wait.until(ExpectedConditions.presenceOfElementLocated(tableCategories));
        return true;
    }

    public int visibleRowCount() {
        return driver.findElements(By.xpath("//table//tbody/tr")).size();
    }

    /** "Hiển thị 1-10 trong tổng số 82 danh mục" -> {1,10,82} */
    public int[] getVisibleRange() {
        String txt = wait.until(ExpectedConditions.visibilityOfElementLocated(pageStats)).getText();
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("Hiển thị\\s+(\\d+)-(\\d+)\\s+trong tổng số\\s+(\\d+)\\s+danh mục")
            .matcher(txt);
        if (m.find()) {
            int from = Integer.parseInt(m.group(1));
            int to   = Integer.parseInt(m.group(2));
            int total= Integer.parseInt(m.group(3));
            return new int[]{from, to, total};
        }
        return new int[]{-1,-1,-1};
    }

    /** "Trang 1 / 9" -> {1, 9} */
    public int[] getPagePosition() {
        String lbl = wait.until(ExpectedConditions.visibilityOfElementLocated(pageLabel)).getText().trim();
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("Trang\\s+(\\d+)\\s*/\\s*(\\d+)")
            .matcher(lbl);
        if (m.find()) return new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))};
        return new int[]{-1,-1};
    }

    public boolean canPrev() {
        WebElement prev = wait.until(ExpectedConditions.visibilityOfElementLocated(btnPrev));
        String dis = prev.getAttribute("disabled");
        return dis == null || dis.isBlank();
    }

    public boolean canNext() {
        WebElement next = wait.until(ExpectedConditions.visibilityOfElementLocated(btnNext));
        String dis = next.getAttribute("disabled");
        return dis == null || dis.isBlank();
    }

    // =========================
    // Feedback / Validation
    // =========================
    /** Đọc message ưu tiên Alert, sau đó Toast nếu có. */
    public String readFeedbackMessage() {
        try {
            Alert a = new WebDriverWait(driver, Duration.ofSeconds(1))
                    .until(ExpectedConditions.alertIsPresent());
            String text = a.getText();
            a.accept();
            return text == null ? "" : text.trim();
        } catch (TimeoutException | NoAlertPresentException ignored) {}

        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(anyToast))
                    .getText().trim();
        } catch (TimeoutException e) {
            return "";
        }
    }

    /** Lỗi ngay dưới ô Name (rỗng/trùng). */
    public String getNameErrorText() {
        By nearNameError = By.xpath("//div[@class='flex items-center gap-2 text-red-500 text-sm']");
        try {
            WebElement dialog = driver.findElement(dialogRoot);
            WebElement err = dialog.findElement(nearNameError);
            System.out.println("Found name error: " + err.getText().trim());
            return err.getText().trim();
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    // =========================
    // Dialog control
    // =========================
    public boolean isDialogOpen() {
        try {
            WebElement dlg = driver.findElement(dialogRoot);
            return dlg.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void closeDialogIfOpen() {
        if (!isDialogOpen()) return;

        // 1) Hủy
        By btnCancel = By.xpath("//button[.='Hủy' or .='Huỷ' or .='Cancel' or contains(@class,'cancel')]");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(btnCancel)).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(dialogRoot));
            return;
        } catch (Exception ignored) {}

        // 2) Nút X
        By btnClose = By.xpath("//button[@aria-label='Close' or @aria-label='Đóng' or contains(@class,'close') or normalize-space()='×' or normalize-space()='✕']");
        try {
            wait.until(ExpectedConditions.elementToBeClickable(btnClose)).click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(dialogRoot));
            return;
        } catch (Exception ignored) {}

        // 3) ESC
        try {
            new org.openqa.selenium.interactions.Actions(driver).sendKeys(Keys.ESCAPE).perform();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(dialogRoot));
            return;
        } catch (Exception ignored) {}

        // 4) Click ngoài overlay
        try {
            By overlay = By.cssSelector(".ant-modal-wrap, .ant-modal-root, .modal, [role='dialog']");
            WebElement ov = driver.findElement(overlay);
            ov.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(dialogRoot));
        } catch (Exception ignored) {}
    }

    // =========================
    // Assertions tiện dụng
    // =========================
    public void assertContainsAny(String actual, String... needles){
        String a = norm(actual);
        for (String n : needles) if (a.contains(norm(n))) return;
        Assert.fail("Chuỗi không chứa từ khoá mong đợi. Actual: " + actual);
    }

    // =========================
    // DTO & Helpers
    // =========================
    public static class CategoryRow {
        public final String code, name, desc, count, date;
        public CategoryRow(String code, String name, String desc, String count, String date) {
            this.code = code; this.name = name; this.desc = desc; this.count = count; this.date = date;
        }
        @Override public String toString() {
            return code + " | " + name + " | " + desc + " | " + count + " | " + date;
        }
    }

    private static String norm(String s){
        if (s == null) return "";
        String lower = s.toLowerCase();
        String noAccent = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccent.replaceAll("\\s+", " ").trim();
    }

    // --- Cell/Row helpers ---
    private By cellByIndex(int rowIdx, int colIdx) {
        return By.xpath("(//table[contains(@class,'w-full')]//tbody/tr)[" + rowIdx + "]/td[" + colIdx + "]");
    }

    private By kebabInRow(String name) {
        return By.xpath(
            "//tr[td[normalize-space()='" + name + "']]//button[" +
                "@aria-haspopup='menu' or @aria-expanded or contains(@aria-label,'Hành động') or contains(@aria-label,'Action') " +
                "or contains(@class,'kebab') or contains(@class,'more') or normalize-space()='…' or normalize-space()='...']"
        );
    }

    private By actionItem(String label) {
        return By.xpath(
            "//*[@role='menu']//*[normalize-space()='" + label + "']" +
            " | //div[contains(@class,'menu') or contains(@class,'DropdownMenu')]//*[normalize-space()='" + label + "']" +
            " | //button[normalize-space()='" + label + "']"
        );
    }

    private void openRowMenuAndChoose(String name, String itemLabel) {
        wait.until(ExpectedConditions.elementToBeClickable(kebabInRow(name))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(actionMenuRoot));
        wait.until(ExpectedConditions.elementToBeClickable(actionItem(itemLabel))).click();
    }
}
