package vn.pis.ui.tests;

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
}
