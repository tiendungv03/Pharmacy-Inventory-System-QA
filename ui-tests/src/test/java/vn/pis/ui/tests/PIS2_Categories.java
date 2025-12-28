package vn.pis.ui.tests;

import org.openqa.selenium.WebElement;
import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.*;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.time.Duration;
import java.time.Instant;

import static vn.pis.ui.util.TestEnv.*;

// Đăng ký listener để log start/finish/fail
@Listeners(PIS2_ConsoleLogger.class)
public class PIS2_Categories extends BaseTest {

    // ===== Helpers for logging =====
    private void log(String msg){
        String line = "[PIS2] " + msg;
        System.out.println(line);         // hiện ngay trên console khi chạy mvn test
        Reporter.log(line, true);         // hiện cả trong report TestNG
    }
    private String unique(String p){ return p + "-" + Instant.now().toEpochMilli(); }

    private void assertContains(String actual, String... needles) {
        String a = actual == null ? "" : actual.toLowerCase();
        for (String n : needles) { if (a.contains(n.toLowerCase())) return; }
        Assert.fail("Chuỗi không chứa từ khoá mong đợi. Actual: " + actual);
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(java.lang.reflect.Method m){
        log("▶ BẮT ĐẦU TC: " + m.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(java.lang.reflect.Method m){
        log("■ KẾT THÚC TC: " + m.getName());
    }

    // ====== TESTS ======
    @Test(priority = 1)
    public void TC01_OpenCategoriesPage() {
        log("MỞ trang login và đăng nhập");
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL + "/login");
        login.login(ADMIN_USER, ADMIN_PASS);

        log("Đi tới trang Quản lý danh mục");
        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        log("Xác minh cột và nút cơ bản xuất hiện");
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[contains(.,'Tên danh mục')]")).size() > 0,
                "Không thấy cột 'Tên danh mục'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//button[contains(.,'Thêm danh mục')]")).size() > 0,
                "Không thấy nút 'Thêm danh mục'"
        );
        log("TC01 OK");
    }

    @Test(priority = 2)
    public void TC02_AddCategory_Success() {
        String name = unique("Kháng sinh");
        log("Thêm danh mục mới: " + name);

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();
        cat.clickAddCategory();
        cat.fillCategoryForm(name, "Mô tả cho " + name);

        String msg = cat.submitCategoryForm();
        log("Feedback khi submit: " + msg);

        Assert.assertTrue(
                msg.toLowerCase().contains("thành công") || msg.toLowerCase().contains("success"),
                "Không thấy thông báo thành công. Thực tế: " + msg
        );
        Assert.assertTrue(cat.isAdded(name), "Không thấy dòng vừa thêm: " + name);
        log("TC02 OK - Đã thêm thành công " + name);
    }

    @Test(priority = 3)
    public void TC03_AddCategory_EmptyName() {
        log("Kiểm tra validate tên rỗng");
        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();
        cat.clickAddCategory();
        cat.fillCategoryForm("", "desc");

        String msg = cat.submitCategoryForm();
        log("Thông báo validate: " + msg);

        String low = msg.toLowerCase();
        Assert.assertTrue(
                low.contains("không được để trống") || low.contains("bắt buộc") || low.contains("required"),
                "Không thấy thông báo lỗi required. Thực tế: " + msg
        );
        cat.closeDialogIfOpen();
        log("TC03 OK - Validate tên rỗng hoạt động");
    }

    @Test(priority = 4)
    public void TC04_AddCategory_DuplicateName() {
        String name = unique("Cat-Dup");
        log("Seed danh mục lần 1: " + name);

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();
        cat.clickAddCategory();
        cat.fillCategoryForm(name,"desc1");
        String msg1 = cat.submitCategoryForm();
        log("Seed feedback: " + msg1);
        Assert.assertTrue(cat.isAdded(name), "Seed thất bại: không thấy " + name);

        log("Thêm trùng tên để kiểm tra duplicate");
        cat.clickAddCategory();
        cat.fillCategoryForm(name,"desc2");
        String dupMsg = cat.submitCategoryForm();
        log("Duplicate feedback: " + dupMsg);

        String low = dupMsg.toLowerCase();
        Assert.assertTrue(
                low.contains("tồn tại") || low.contains("đã tồn tại") ||
                        low.contains("trùng")   || low.contains("duplicate") ||
                        low.contains("already exists"),
                "Không thấy thông báo trùng. Thực tế: " + dupMsg
        );
        cat.closeDialogIfOpen();
        log("TC04 OK - Validate trùng tên hoạt động");
    }

    @Test(priority = 5)
    public void TC05_EditCategory_Success() {
        String name = unique("Kháng sinh");
        String newName = name + " (đã cập nhật)";
        log("Seed danh mục để edit: " + name);

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        cat.clickAddCategory();
        cat.fillCategoryForm(name, "Mô tả danh mục gốc");
        String seedMsg = cat.submitCategoryForm();
        log("Seed feedback: " + seedMsg);
        cat.assertContainsAny(seedMsg, "thành công", "success", "created");
        cat.waitRowPresent(name);
        Assert.assertTrue(cat.isAdded(name), "Seed thất bại: không thấy " + name);

        log("Mở popup Edit cho: " + name);
        cat.clickEdit(name);
        cat.fillCategoryForm(newName, "Mô tả mới cho danh mục này");
        String updMsg = cat.submitCategoryForm();
        log("Update feedback: " + updMsg);
        cat.assertContainsAny(updMsg, "cập nhật", "updated", "success");

        log("Xác minh tên mới xuất hiện và tên cũ biến mất");
        cat.waitRowPresent(newName);
        Assert.assertTrue(cat.isAdded(newName), "Không thấy tên mới: " + newName);
        Assert.assertEquals(
                driver.findElements(By.xpath("//table//td[normalize-space()='" + name + "']")).size(),
                0,
                "Tên cũ vẫn còn: " + name
        );
        cat.closeDialogIfOpen();
        log("TC05 OK - Cập nhật thành công");
    }

    @Test(priority = 6)
    public void TC06_DeleteCategory_WhenZeroDrug() {
        String name = unique("Cat-Del0");
        log("Seed danh mục để xoá: " + name);

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        cat.clickAddCategory();
        cat.fillCategoryForm(name,"desc");
        String seedMsg = cat.submitCategoryForm();
        log("Seed feedback: " + seedMsg);
        cat.assertContainsAny(seedMsg, "thành công", "success", "created");
        cat.waitRowPresent(name);
        Assert.assertTrue(cat.isAdded(name));

        log("Thực hiện xoá danh mục: " + name);
        String del = cat.deleteCategory(name);
        log("Delete feedback: " + del);
        cat.assertContainsAny(del, "xóa", "xoá", "đã xóa", "đã xoá", "deleted", "success");

        Assert.assertEquals(
                driver.findElements(By.xpath("//table//td[normalize-space()='" + name + "']")).size(), 0,
                "Dòng vẫn còn sau khi xoá"
        );
        log("TC06 OK - Đã xoá thành công");
    }

    @Test(priority = 7)
    public void TC07_SearchByName() {
        String name = unique("Cat-Search");
        log("Seed danh mục để search: " + name);

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        cat.clickAddCategory();
        cat.fillCategoryForm(name,"desc");
        String seed = cat.submitCategoryForm();
        log("Seed feedback: " + seed);
        cat.assertContainsAny(seed, "thành công", "success", "created");
        cat.waitRowPresent(name);

        log("Tìm kiếm theo tên: " + name);
        cat.search(name);
        By cell = By.xpath("//table//td[contains(normalize-space(),'" + name + "')]");
        new WebDriverWait(driver, Duration.ofSeconds(6))
                .until(ExpectedConditions.presenceOfElementLocated(cell));

        Assert.assertTrue(driver.findElements(cell).size() >= 1, "Tìm tên không thấy trong bảng.");
        log("TC07 OK - Search hoạt động");
    }

    @Test(priority = 8)
    public void TC08_Pagination_PageSize() {
        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        int[] pos1 = cat.getPagePosition();
        int[] rg1  = cat.getVisibleRange();
        log(String.format("Trang hiện tại: %d/%d | Hiển thị %d-%d / %d",
                pos1[0], pos1[1], rg1[0], rg1[1], rg1[2]));

        boolean movedNext = cat.nextPage();
        log("Bấm 'Sau' → " + (movedNext ? "đã chuyển trang" : "không thể chuyển (vị trí cuối?)"));
        if (movedNext) {
            int[] pos2 = cat.getPagePosition();
            Assert.assertEquals(pos2[0], pos1[0] + 1, "Không tăng số trang khi bấm Sau");
            log("Trang mới: " + pos2[0] + "/" + pos2[1]);
        }

        boolean movedPrev = cat.prevPage();
        log("Bấm 'Trước' → " + (movedPrev ? "đã quay lại" : "không thể quay (vị trí đầu?)"));
        if (movedPrev && movedNext) {
            int[] pos3 = cat.getPagePosition();
            Assert.assertEquals(pos3[0], pos1[0], "Không quay về trang ban đầu khi bấm Trước");
            log("Trang quay về: " + pos3[0] + "/" + pos3[1]);
        }
        log("TC08 OK - Phân trang vận hành");
    }

    @Test(priority = 9)
    public void TC09_View_AllColumns() {
        log("TC09 - Kiểm tra đủ các cột trên bảng danh mục");

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Mã danh mục']")).size() > 0,
                "Không thấy cột 'Mã danh mục'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Tên danh mục']")).size() > 0,
                "Không thấy cột 'Tên danh mục'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Mô tả']")).size() > 0,
                "Không thấy cột 'Mô tả'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Số loại thuốc']")).size() > 0,
                "Không thấy cột 'Số loại thuốc'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Ngày tạo']")).size() > 0,
                "Không thấy cột 'Ngày tạo'"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//th[normalize-space()='Hành động']")).size() > 0,
                "Không thấy cột 'Hành động'"
        );

        log("TC09 OK - Đã thấy đủ 6 cột trên header");
    }

    @Test(priority = 10)
    public void TC10_StatsLabel_Correct() {
        log("TC10 - Kiểm tra label thống kê hiển thị");

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        int[] range = cat.getVisibleRange();   // {from, to, total}
        int visible = cat.visibleRowCount();

        log(String.format("Range: %d-%d / %d, visible rows = %d",
                range[0], range[1], range[2], visible));

        // Trang đầu tiên thì from phải là 1
        Assert.assertTrue(range[0] == 1, "from != 1 trên trang đầu. Thực tế: " + range[0]);
        // to >= from
        Assert.assertTrue(range[1] >= range[0], "to < from, range sai");
        // tổng phải >= to
        Assert.assertTrue(range[2] >= range[1], "total < to, range sai");

        // Số dòng nhìn thấy phải khớp với (to - from + 1) trên trang không phải trang cuối
        int expectedRows = range[1] - range[0] + 1;
        Assert.assertEquals(visible, expectedRows,
                "visibleRowCount không khớp với khoảng hiển thị");

        log("TC10 OK - Label 'Hiển thị x-y trong tổng số z danh mục' khớp với số dòng thực tế");
    }


    @Test(priority = 11)
    public void TC11_Pagination_PageSize_25_50_100() {
        log("TC11 - Kiểm tra số dòng hiển thị khi chọn 25/50/100");

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        int[] sizes = {25, 50, 100};

        for (int size : sizes) {
            log("Đổi page size = " + size);
            boolean ok = cat.setPageSize(size);
            Assert.assertTrue(ok, "Không set được page size = " + size +
                    " (có thể UI chưa có dropdown page size)");

            int[] range = cat.getVisibleRange();
            int visible = cat.visibleRowCount();
            int expectedMax = size;
            int actualCount = range[1] - range[0] + 1;

            log(String.format("Size %d → range %d-%d (=%d items), visible rows = %d",
                    size, range[0], range[1], actualCount, visible));

            // Trừ trường hợp trang cuối cùng ít hơn size, còn lại phải <= size
            Assert.assertTrue(actualCount <= expectedMax,
                    "Số bản ghi trên trang > page size. actual=" + actualCount + ", size=" + size);
            Assert.assertEquals(visible, actualCount,
                    "visibleRowCount không khớp với range cho size " + size);
        }

        log("TC11 OK - Page size 25/50/100 hiển thị đúng số dòng (nếu UI đã hỗ trợ)");
    }

    @Test(priority = 12)
    public void TC12_Add_PopupLayout() {
        log("TC12 - Kiểm tra layout popup 'Thêm danh mục thuốc'");

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();
        cat.clickAddCategory();

        // Tiêu đề dialog
        By dlgTitle = By.xpath(
                "//*[@role='dialog']//h1[contains(.,'Thêm danh mục')]" +
                        " | //*[@role='dialog']//h2[contains(.,'Thêm danh mục')]"
        );
        Assert.assertTrue(
                driver.findElements(dlgTitle).size() > 0,
                "Không thấy tiêu đề 'Thêm danh mục thuốc' trong dialog"
        );

        // Field Tên danh mục & Mô tả
        Assert.assertTrue(
                driver.findElements(By.xpath("//*[@role='dialog']//*[contains(.,'Tên danh mục')]")).size() > 0,
                "Không thấy label 'Tên danh mục' trong popup"
        );
        Assert.assertTrue(
                driver.findElements(By.xpath("//*[@role='dialog']//*[contains(.,'Mô tả')]")).size() > 0,
                "Không thấy label 'Mô tả' trong popup"
        );

        cat.closeDialogIfOpen();
        log("TC12 OK - Popup Thêm danh mục hiển thị đúng tiêu đề & field");
    }


    @Test(priority = 13)
    public void TC13_ActionMenu_HasEditAndDelete() {
        log("TC13 - Kiểm tra menu Hành động có 'Chỉnh sửa' & 'Xóa'");

        CategoriesPage cat = new CategoriesPage(driver);
        cat.open();

        // Lấy tên danh mục ở dòng đầu tiên
        WebElement firstNameCell = driver.findElement(
                By.xpath("(//table//tbody/tr)[1]/td[2]//p")
        );
        String name = firstNameCell.getText().trim();
        log("Dùng dòng đầu tiên với tên: " + name);

        // Mở menu '...'
        WebElement kebab = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(
                        By.xpath("(//table//tbody/tr)[1]//button[@aria-haspopup='menu']")
                ));
        kebab.click();

        // Chờ menu xuất hiện
        By menuRoot = By.xpath("//*[(@role='menu') or contains(@class,'DropdownMenu') or contains(@class,'menu')]");
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.visibilityOfElementLocated(menuRoot));

        // Kiểm tra item Chỉnh sửa & Xóa
        By editItem = By.xpath("//*[(@role='menu') or contains(@class,'menu')]//*[contains(normalize-space(),'Chỉnh sửa')]");
        By deleteItem = By.xpath("//*[(@role='menu') or contains(@class,'menu')]//*[contains(normalize-space(),'Xóa') or contains(normalize-space(),'Xoá')]");

        Assert.assertTrue(
                driver.findElements(editItem).size() > 0,
                "Không thấy item 'Chỉnh sửa' trong menu Hành động"
        );
        Assert.assertTrue(
                driver.findElements(deleteItem).size() > 0,
                "Không thấy item 'Xóa' trong menu Hành động"
        );

        log("TC13 OK - Menu Hành động có đủ 'Chỉnh sửa' & 'Xóa'");
    }


//    @Test(priority = 14)
//    public void TC14_Delete_ShowConfirmDialog() {
//        String name = unique("Cat-ConfirmDel");
//        log("TC14 - Seed danh mục để test confirm delete: " + name);
//
//        CategoriesPage cat = new CategoriesPage(driver);
//        cat.open();
//
//        // Seed 1 danh mục
//        cat.clickAddCategory();
//        cat.fillCategoryForm(name, "desc");
//        String seedMsg = cat.submitCategoryForm();
//        log("Seed feedback: " + seedMsg);
//        cat.assertContainsAny(seedMsg, "thành công", "success", "created");
//        cat.waitRowPresent(name);
//
//        // Mở menu xoá
//        WebElement kebab = new WebDriverWait(driver, Duration.ofSeconds(10))
//                .until(ExpectedConditions.elementToBeClickable(
//                        By.xpath("//tr[td[normalize-space()='" + name + "']]//button[@aria-haspopup='menu']")
//                ));
//        kebab.click();
//
//        By deleteItem = By.xpath("//*[(@role='menu') or contains(@class,'menu')]//*[contains(normalize-space(),'Xóa') or contains(normalize-space(),'Xoá')]");
//        new WebDriverWait(driver, Duration.ofSeconds(5))
//                .until(ExpectedConditions.elementToBeClickable(deleteItem))
//                .click();
//
//        // Hộp thoại confirm
//        By confirmDialog = By.xpath("//*[contains(@role,'alertdialog') or @role='dialog']");
//        new WebDriverWait(driver, Duration.ofSeconds(5))
//                .until(ExpectedConditions.visibilityOfElementLocated(confirmDialog));
//
//        // Text confirm + nút Hủy / Xác nhận
//        By msg = By.xpath("//*[contains(@role,'alertdialog') or @role='dialog']//*[contains(.,'xóa') or contains(.,'xoá')]");
//        Assert.assertTrue(
//                driver.findElements(msg).size() > 0,
//                "Không thấy message xác nhận xóa"
//        );
//
//        By btnCancel = By.xpath("//button[.='Hủy' or .='Huỷ' or .='Cancel']");
//        By btnConfirm = By.xpath("//button[.='Xác nhận' or .='Xóa' or .='Xoá']");
//
//        Assert.assertTrue(driver.findElements(btnCancel).size() > 0, "Không thấy nút Hủy trong confirm");
//        Assert.assertTrue(driver.findElements(btnConfirm).size() > 0, "Không thấy nút Xác nhận trong confirm");
//
//        // Bấm Hủy → danh mục vẫn còn
//        driver.findElement(btnCancel).click();
//        new WebDriverWait(driver, Duration.ofSeconds(5))
//                .until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
//
//        Assert.assertTrue(cat.isAdded(name), "Sau khi Hủy confirm mà danh mục bị mất");
//        log("TC14 OK - Confirm delete hiển thị đúng, Hủy không xoá dữ liệu");
//    }
//
//    @Test(priority = 15)
//    public void TC15_Delete_Cancel_ShouldKeepRow() {
//        String name = unique("Cat-DelCancel");
//        log("TC15 - Seed danh mục để test cancel delete: " + name);
//
//        CategoriesPage cat = new CategoriesPage(driver);
//        cat.open();
//
//        // 1) Seed 1 danh mục
//        cat.clickAddCategory();
//        cat.fillCategoryForm(name, "desc");
//        String seedMsg = cat.submitCategoryForm();
//        log("Seed feedback: " + seedMsg);
//        cat.assertContainsAny(seedMsg, "thành công", "success", "created");
//        cat.waitRowPresent(name);
//
//        // 2) Mở menu Hành động → Xóa
//        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(10));
//        WebElement kebab = w.until(ExpectedConditions.elementToBeClickable(
//                By.xpath("//tr[td[normalize-space()='" + name + "']]//button[@aria-haspopup='menu']")));
//        kebab.click();
//
//        By deleteItem = By.xpath(
//                "//*[(@role='menu') or contains(@class,'menu')]//*[contains(normalize-space(),'Xóa') or contains(normalize-space(),'Xoá')]");
//        w.until(ExpectedConditions.elementToBeClickable(deleteItem)).click();
//
//        // 3) Hộp thoại confirm xuất hiện
//        By confirmDialog = By.xpath("//*[contains(@role,'alertdialog') or @role='dialog']");
//        w.until(ExpectedConditions.visibilityOfElementLocated(confirmDialog));
//
//        // 4) Bấm Hủy
//        By btnCancel = By.xpath("//button[.='Hủy' or .='Huỷ' or .='Cancel']");
//        w.until(ExpectedConditions.elementToBeClickable(btnCancel)).click();
//        w.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
//
//        // 5) Xác minh danh mục vẫn còn
//        Assert.assertTrue(cat.isAdded(name),
//                "Sau khi bấm Hủy confirm mà danh mục '" + name + "' bị mất");
//        log("TC15 OK - Cancel trong confirm delete không xoá danh mục");
//    }
//
//    @Test(priority = 16)
//    public void TC16_Delete_ConfirmYes_ShouldRemoveRow() {
//        String name = unique("Cat-DelYes");
//        log("TC16 - Seed danh mục để test confirm YES delete: " + name);
//
//        CategoriesPage cat = new CategoriesPage(driver);
//        cat.open();
//
//        // 1) Seed 1 danh mục với Số loại thuốc = 0
//        cat.clickAddCategory();
//        cat.fillCategoryForm(name, "desc");
//        String seedMsg = cat.submitCategoryForm();
//        log("Seed feedback: " + seedMsg);
//        cat.assertContainsAny(seedMsg, "thành công", "success", "created");
//        cat.waitRowPresent(name);
//
//        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//        // 2) Mở menu Hành động → chọn Xóa
//        WebElement kebab = w.until(ExpectedConditions.elementToBeClickable(
//                By.xpath("//tr[td[normalize-space()='" + name + "']]//button[@aria-haspopup='menu']")));
//        kebab.click();
//
//        By deleteItem = By.xpath(
//                "//*[(@role='menu') or contains(@class,'menu')]//*[contains(normalize-space(),'Xóa') or contains(normalize-space(),'Xoá')]");
//        w.until(ExpectedConditions.elementToBeClickable(deleteItem)).click();
//
//        // 3) Hộp thoại confirm
//        By confirmDialog = By.xpath("//*[contains(@role,'alertdialog') or @role='dialog']");
//        w.until(ExpectedConditions.visibilityOfElementLocated(confirmDialog));
//
//        // 4) Bấm Xác nhận
//        By btnConfirm = By.xpath("//button[.='Xác nhận' or .='Xóa' or .='Xoá']");
//        w.until(ExpectedConditions.elementToBeClickable(btnConfirm)).click();
//
//        // 5) Chờ dialog biến mất + toast (nếu có)
//        w.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
//
//        String msg = cat.readFeedbackMessage();
//        log("Delete feedback: " + msg);
//        // Không bắt buộc phải đúng từng chữ, chỉ cần có từ khoá
//        cat.assertContainsAny(msg,
//                "xóa", "xoá", "đã xóa", "đã xoá", "deleted", "success");
//
//        // 6) Xác minh row không còn
//        Assert.assertEquals(
//                driver.findElements(By.xpath("//table//td[normalize-space()='" + name + "']")).size(),
//                0,
//                "Danh mục '" + name + "' vẫn còn sau khi xác nhận Xóa"
//        );
//
//        log("TC16 OK - Confirm YES delete xoá danh mục khỏi bảng");
//    }


    // @Test(priority = 17)
//    public void TC17_Delete_Disabled_WhenHasDrugs() {
//        String catName = unique("Cat-HasDrug");
//        log("TC17 - (Skeleton) Không cho xoá khi Số loại thuốc > 0: " + catName);
//
//        CategoriesPage cat = new CategoriesPage(driver);
//        cat.open();
//
//        // 1) Seed danh mục
//        cat.clickAddCategory();
//        cat.fillCategoryForm(catName, "desc");
//        String seedMsg = cat.submitCategoryForm();
//        cat.assertContainsAny(seedMsg, "thành công", "success", "created");
//        cat.waitRowPresent(catName);
//
//        // 2) Gắn thuốc vào danh mục này (TODO: tự implement)
//        // TestDataSeeder.attachDrugToCategory(catName);
//
//        // 3) Mở menu
//        WebElement kebab = new WebDriverWait(driver, Duration.ofSeconds(10))
//                .until(ExpectedConditions.elementToBeClickable(
//                        By.xpath("//tr[td[normalize-space()='" + catName + "']]//button[@aria-haspopup='menu']")
//                ));
//        kebab.click();
//
//        By deleteItem = By.xpath("//*[(@role='menu') or contains(@class,'menu')]//*[contains(normalize-space(),'Xóa') or contains(normalize-space(),'Xoá')]");
//        WebElement delBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
//                .until(ExpectedConditions.presenceOfElementLocated(deleteItem));
//
//        String ariaDisabled = delBtn.getAttribute("aria-disabled");
//        boolean disabled = !delBtn.isEnabled() || "true".equalsIgnoreCase(ariaDisabled);
//
//        Assert.assertTrue(disabled,
//                "Nút Xóa không bị vô hiệu hoá khi Số loại thuốc > 0 (cần xử lý BE/UI)");
//
//        log("TC17 OK - Nút Xóa bị vô hiệu hóa khi danh mục đã có thuốc");
//    }

}
