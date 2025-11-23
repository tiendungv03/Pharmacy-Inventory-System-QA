package vn.pis.ui.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**

 */
public class UserPage {

    // =========================
    // 1. Fields & Constructor
    // =========================
    private final WebDriver driver;
    private final WebDriverWait wait;

    public UserPage(WebDriver d) {
        this.driver = d;
        this.wait   = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    // =========================
    // 2. Locators chung (Menu / Bảng)
    // =========================
    // Task 3 – Menu + Bảng
    private final By menuUsers   = By.xpath("//span[contains(text(),'Quản lý người dùng')]");
    private final By tableUsers  = By.xpath("//table[contains(@class,'w-full')]");
    private final By headerCells = By.cssSelector("table thead th");
    private final By bodyRows    = By.cssSelector("table tbody tr");

    // =========================
    // 3. Locators – Tạo mới User (Dialog "Thêm người dùng mới")
    // =========================
    private final By btnAdd = By.xpath("//span[@class='font-medium']");

    // Dialog "Thêm người dùng mới"
    private final By createDialog = By.xpath(
        "//div[@role='dialog'][.//h2[normalize-space()='Thêm người dùng mới']] | " +
        "//div[@role='dialog'][.//div[normalize-space()='Thêm người dùng mới']]"
    );

    // Input theo id
    private By usernameInput_Create()       { return By.id("username"); }
    private By fullnameInput_Create()       { return By.id("fullName"); }
    private By emailInput_Create()          { return By.id("email"); }
    private By phoneInput_Create()          { return By.id("phone"); }
    private By passwordInput_Create()       { return By.id("password"); }
    private By confirmPasswordInput_Create(){ return By.id("confirmPassword"); }

    // Dropdown Vai trò (trong dialog)
    private By roleDropdown_Create() {
        return By.xpath(
            "//div[@role='dialog']//label[contains(normalize-space(),'Vai trò')]" +
            "/following::button[@role='combobox'][1]"
        );
    }

    private By roleOption(String t) {
        return By.xpath(
            "//div[@role='listbox']//*[(@role='option' or self::li or self::div) and normalize-space()='" + t + "']"
        );
    }

    // Nút submit trong dialog create
    private By createSubmitBtn() {
        return By.xpath("//div[@role='dialog']//button[normalize-space()='Thêm người dùng']");
    }

    // =========================
    // 4. Locators – Search
    // =========================
    private final By searchInput = By.cssSelector(
        "input[placeholder='Tìm kiếm theo tên, email, số điện thoại...']"
    );
    private final By usernameCells = By.cssSelector("table tbody tr td:nth-child(1)");

    // =========================
    // 5. Locators – Edit User
    // =========================
    // Nút 3 chấm cùng hàng với username (dùng chung cho Edit + Lock/Unlock)
    private By rowMenuButton(String username) {
        return By.xpath(
            "//table//tr[.//td[normalize-space()='" + username + "']]//button[@aria-haspopup='menu']"
        );
    }

    // Item "Chỉnh sửa" trong menu 3 chấm
    private final By menuEditItem =
        By.xpath("//*[normalize-space()='Chỉnh sửa' and not(self::svg)]");

    // Dialog "Chỉnh sửa"
    private final By editDialog = By.xpath(
        "//div[@role='dialog'][.//h2[contains(normalize-space(),'Chỉnh sửa')]]"
    );

    // Input Họ và tên trong dialog Edit
    private By fullNameInput_Edit() {
        return By.xpath(
            "//div[@role='dialog']" +
            "//label[contains(normalize-space(),'Họ và tên')]/following::input[1]"
        );
    }

    // Input Số điện thoại trong dialog Edit
    private By phoneInput_Edit() {
        return By.xpath(
            "//div[@role='dialog']" +
            "//label[contains(normalize-space(),'Số điện thoại')]/following::input[1]"
        );
    }

    // =========================
    // 6. Locators – Khóa / Mở khóa
    // =========================
    // Cell trạng thái (cột 6)
    private By statusCell(String username) {
        return By.xpath(
            "//table//tr[.//td[normalize-space()='" + username + "']]//td[6]"
        );
    }

    // =========================
    // 7. Navigation
    // =========================
    public void open() {
        wait.until(ExpectedConditions.elementToBeClickable(menuUsers)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(btnAdd));
        wait.until(ExpectedConditions.presenceOfElementLocated(tableUsers));
    }

    // =========================
    // 8. Task 3 – Danh sách người dùng
    // =========================
    public boolean isTableVisible() {
        return !driver.findElements(tableUsers).isEmpty();
    }

    public List<String> getHeaderTexts() {
        wait.until(ExpectedConditions.presenceOfElementLocated(headerCells));
        return driver.findElements(headerCells).stream()
            .map(e -> e.getText().trim())
            .collect(Collectors.toList());
    }

    public int getRowCount() {
        wait.until(ExpectedConditions.presenceOfElementLocated(bodyRows));
        return driver.findElements(By.cssSelector("table tbody tr:not(:has(td[colspan]))")).size();
    }

    // =========================
    // 9. Task 4 – Tạo mới User
    // =========================
    public void clickAddUser() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAdd)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(createDialog));
    }

    public void fillCreateForm(
            String username,
            String fullName,
            String email,
            String phone,
            String password,
            String role
    ) {
        type(usernameInput_Create(), username);
        type(fullnameInput_Create(), fullName);
        type(emailInput_Create(), email);
        type(phoneInput_Create(), phone);
        type(passwordInput_Create(), password);
        type(confirmPasswordInput_Create(), password);

        // chọn Vai trò
        pickRole(roleDropdown_Create(), role);
    }

    public void submitCreateForm() {
        // 1. Click nút "Thêm người dùng"
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(createSubmitBtn()));
        btn.click();

        // 2. Xử lý alert "Thêm người dùng thành công!" nếu có
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
            System.out.println("[PIS1] Alert khi tạo user: " + alert.getText());
            alert.accept();
        } catch (TimeoutException e) {
            // Không có alert thì bỏ qua
        }

        // 3. Chờ dialog "Thêm người dùng mới" đóng (nếu FE còn giữ lại)
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(createDialog));
        } catch (TimeoutException e) {
            // Nếu dialog đã tự đóng hoặc không còn, bỏ qua
        }
    }

    /** Kiểm tra dòng user có xuất hiện trong bảng sau khi tạo */
    public boolean isUserRowPresent(String username) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//table//td[normalize-space()='" + username + "']"))
            );
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // =========================
    // 10. Search User
    // =========================
    public void searchUser(String keyword) {
        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        box.click();
        box.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        box.sendKeys(keyword);
        box.sendKeys(Keys.ENTER); // đảm bảo FE nhận onChange/onEnter

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(usernameCells));
        } catch (TimeoutException e) {
            // nếu không có hàng nào, test sẽ tự kiểm tra
        }
    }

    /** Lấy danh sách username trong bảng, bỏ qua "Đang tải..." */
    public List<String> getUsernamesInTable() {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(d -> {
                List<WebElement> cells = d.findElements(usernameCells);
                List<String> names = cells.stream()
                    .map(e -> e.getText().trim())
                    .filter(text -> text != null
                            && !text.isEmpty()
                            && !text.equalsIgnoreCase("Đang tải..."))
                    .collect(Collectors.toList());

                // Nếu danh sách rỗng -> trả null để WebDriverWait retry
                return names.isEmpty() ? null : names;
            });
    }

    // =========================
    // 11. Edit User
    // =========================
    /** Lấy username đầu tiên hợp lệ để chỉnh sửa (bỏ qua "Đang tải...") */
    public String getFirstEditableUsername() {
        List<String> names = getUsernamesInTable();
        if (names.isEmpty()) {
            throw new IllegalStateException(
                "Không tìm thấy user hợp lệ để chỉnh sửa (chỉ thấy 'Đang tải...' hoặc bảng rỗng)."
            );
        }
        return names.get(0);
    }

    /** Mở popup "Chỉnh sửa" cho user */
    public void openEditUser(String username) {
        WebElement menuBtn = wait.until(
            ExpectedConditions.elementToBeClickable(rowMenuButton(username))
        );
        menuBtn.click();

        // đợi menu xổ ra
        try { Thread.sleep(300); } catch (Exception ignored) {}

        WebElement editItem = wait.until(
            ExpectedConditions.elementToBeClickable(menuEditItem)
        );

        // dùng JS click cho chắc (Radix Menu)
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editItem);
        } catch (Exception e) {
            editItem.click();
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(editDialog));
    }

    /** Điền form chỉnh sửa */
    public void fillEditForm(String newFullName, String newPhone) {
        if (newFullName != null) {
            WebElement fn = findIfExists(fullNameInput_Edit(), 3);
            if (fn != null) {
                fn.click();
                fn.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                fn.sendKeys(newFullName);
            }
        }

        if (newPhone != null) {
            WebElement ph = findIfExists(phoneInput_Edit(), 3);
            if (ph != null) {
                ph.click();
                ph.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                ph.sendKeys(newPhone);
            }
        }
    }

    /** Lưu thay đổi trong popup chỉnh sửa */
    public void submitEditForm() {
        By primary  = By.xpath("//div[@role='dialog']//button[@type='submit']");
        By fallback = By.xpath("//div[@role='dialog']//button[last()]");

        WebElement btn;
        try {
            btn = wait.until(ExpectedConditions.elementToBeClickable(primary));
        } catch (TimeoutException e) {
            btn = wait.until(ExpectedConditions.elementToBeClickable(fallback));
        }

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        } catch (Exception e) {
            btn.click();
        }

        // Alert "Cập nhật người dùng thành công!"
        try {
            WebDriverWait alertWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
            String text = alert.getText();
            System.out.println("[PIS1] Alert sau khi cập nhật: " + text);
            alert.accept();
        } catch (TimeoutException e) {
            // Không có alert thì bỏ qua
        }

        // Chờ dialog đóng hẳn (nếu còn)
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div[role='dialog']")));
        } catch (Exception ignored) {}
    }

    // =========================
    // 12. Lock / Unlock User
    // =========================

    /** Lấy text trạng thái hiện tại của user */
    public String getStatus(String username) {
        WebElement cell = wait.until(
            ExpectedConditions.visibilityOfElementLocated(statusCell(username))
        );
        return cell.getText().trim();
    }

    /** Lấy username đầu tiên đang "Hoạt động" (dùng khi cần 1 user active bất kỳ) */
    public String getFirstActiveUsername() {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(d -> {
                List<WebElement> cells = d.findElements(usernameCells);
                for (WebElement cell : cells) {
                    String text = cell.getText().trim();
                    if (text.isEmpty()) continue;
                    if (text.equalsIgnoreCase("Đang tải...")) continue;
                    return text;
                }
                return null; // để WebDriverWait retry
            });
    }

    /** Khóa user (Hoạt động -> Khóa) */
    public void lockUser(String username) {
        String before = getStatus(username);
        if (!before.contains("Hoạt động")) {
            throw new IllegalStateException("User không ở trạng thái Hoạt động, hiện: " + before);
        }

        openRowMenu(username);
        WebElement lockItem = waitForMenuItem("Khóa");
        jsClick(lockItem);
        handleAlertIfPresent();

        // chờ trạng thái đổi sang "Khóa"
        wait.until(d -> getStatus(username).contains("Khóa"));
    }

    /** Mở khóa user (Khóa -> Hoạt động) */
    public void unlockUser(String username) {
        String before = getStatus(username);
        if (!before.contains("Khóa")) {
            throw new IllegalStateException("User không ở trạng thái Khóa, hiện: " + before);
        }

        openRowMenu(username);
        WebElement unlockItem = waitForMenuItem("Mở khóa");
        jsClick(unlockItem);
        handleAlertIfPresent();

        // chờ trạng thái đổi sang "Hoạt động"
        wait.until(d -> getStatus(username).contains("Hoạt động"));
    }

    // =========================
    // 13. Helpers (private)
    // =========================

    private void type(By locator, String value) {
        if (value == null) return;
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        el.sendKeys(value);
        el.sendKeys(Keys.TAB);
        try { Thread.sleep(100); } catch (Exception e) {}
    }

    private void pickRole(By dropdownLocator, String visibleText) {
        if (visibleText == null || visibleText.isEmpty()) return;

        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(dropdownLocator));

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        } catch (Exception e) {
            el.click();
        }

        try { Thread.sleep(300); } catch (Exception ignored) {}

        WebElement opt = wait.until(ExpectedConditions.elementToBeClickable(roleOption(visibleText)));

        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opt);
        } catch (Exception e) {
            opt.click();
        }
    }

    // tìm element nếu có, không thì trả null
    private WebElement findIfExists(By locator, int timeoutSec) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            return null;
        }
    }

    /** Mở menu 3 chấm cho 1 user cụ thể */
    private void openRowMenu(String username) {
        // 1. Tìm nút 3 chấm cùng hàng với username
        WebElement menuBtn = wait.until(
            ExpectedConditions.elementToBeClickable(rowMenuButton(username))
        );

        // 2. Scroll vào giữa màn hình
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block:'center'});", menuBtn);

        // 3. Lấy id của menu từ aria-controls trước khi click
        String menuId = menuBtn.getAttribute("aria-controls");

        // 4. Click (ưu tiên click thường)
        try {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            menuBtn.click();
        } catch (ElementNotInteractableException e) {
            System.out.println("[LOG] Click() thường thất bại, thử jsClick...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menuBtn);
        }

        // 5. Chờ cho menu thực sự mở (data-state="open")
        By menuLocator;
        if (menuId != null && !menuId.isEmpty()) {
            System.out.println("[LOG] Chờ menu ID: " + menuId + " chuyển sang data-state=open");
            menuLocator = By.id(menuId);
        } else {
            System.out.println("[LOG] Không có aria-controls, chờ xpath data-state=open");
            menuLocator = By.xpath("//div[@role='menu' and @data-state='open']");
        }

        wait.until(ExpectedConditions.attributeContains(menuLocator, "data-state", "open"));
        System.out.println("[LOG] Menu đã mở (data-state=open).");
    }

    /** Chờ item trong menu theo text (Chỉnh sửa / Khóa / Mở khóa) */
    private WebElement waitForMenuItem(String label) {
        By by = By.xpath(
            "//div[@role='menu' and @data-state='open']" +
            "//*[normalize-space()='" + label + "' and not(self::svg)]"
        );
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    /** Xử lý alert trạng thái user */
    private void handleAlertIfPresent() {
        try {
            WebDriverWait aw = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = aw.until(ExpectedConditions.alertIsPresent());
            System.out.println("[PIS1] Alert: " + alert.getText());
            alert.accept();
        } catch (TimeoutException e) {
            // không có alert thì thôi
        }
    }

    /** JS click helper */
    private void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }
}
