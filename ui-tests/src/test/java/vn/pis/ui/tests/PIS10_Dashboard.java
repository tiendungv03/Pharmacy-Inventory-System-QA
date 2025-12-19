package vn.pis.ui.tests;

import static vn.pis.ui.util.TestEnv.ADMIN_PASS;
import static vn.pis.ui.util.TestEnv.ADMIN_USER;
import static vn.pis.ui.util.TestEnv.BASE_URL;

import java.util.List;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import vn.pis.ui.base.BaseTest;
import vn.pis.ui.pages.DashboardPage;
import vn.pis.ui.pages.LoginPage;

public class PIS10_Dashboard extends BaseTest {

  private DashboardPage page;

  private void log(String msg) {
    String line = "[PIS10] " + msg;
    System.out.println(line);
    Reporter.log(line, true);
  }

  @BeforeClass(alwaysRun = true)
  public void loginOnce() {
    log("--- Đăng nhập hệ thống (Admin) ---");
    LoginPage login = new LoginPage(driver);
    login.open(BASE_URL + "/login");
    login.login(ADMIN_USER, ADMIN_PASS);
    page = new DashboardPage(driver);
  }

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod(java.lang.reflect.Method m) {
    log("▶ BẮT ĐẦU TC: " + m.getName());
    page.open();
  }

  @Test(priority = 1, description = "TC_001 - Load trang Dashboard thành công")
  public void TC_001_load_dashboard_success() {
    Assert.assertTrue(page.isDashboardTitleVisible(), "Không thấy title Dashboard => chưa vào đúng trang");
  }

  @Test(priority = 2, description = "TC_002 - Hiển thị tiêu đề Dashboard + mô tả tổng quan")
  public void TC_002_show_dashboard_title_and_subtitle() {
    Assert.assertTrue(page.isDashboardTitleVisible(), "Không thấy title Dashboard");
    Assert.assertTrue(page.isSubtitleVisible(), "Không thấy subtitle: Tổng quan hệ thống quản lý kho dược");
  }

  @Test(priority = 3, description = "TC_009 - Hiển thị đủ 4 thẻ KPI")
  public void TC_009_show_all_kpi_cards() {
    page.waitKpiLoaded();
    Assert.assertTrue(page.isKpiTotalTypesVisible(), "Không thấy KPI Tổng số loại thuốc");
    Assert.assertTrue(page.isKpiTotalValueVisible(), "Không thấy KPI Tổng giá trị tồn kho");
    Assert.assertTrue(page.isKpiExpiringVisible(), "Không thấy KPI Thuốc sắp hết hạn");
    Assert.assertTrue(page.isKpiBelowMinVisible(), "Không thấy KPI Thuốc dưới tồn tối thiểu");
  }

  @Test(priority = 4, description = "TC_010 - KPI Tổng số loại thuốc hiển thị đúng format (>=0)")
  public void TC_010_kpi_total_types_non_negative() {
    page.waitKpiLoaded();
    Assert.assertTrue(page.getKpiTotalTypes() >= 0, "KPI Tổng số loại thuốc bị âm / parse lỗi");
  }

  @Test(priority = 5, description = "TC_011 - KPI Tổng giá trị tồn kho đúng format tiền (₫/đ)")
  public void TC_011_kpi_total_value_has_vnd_format() {
    page.waitKpiLoaded();
    Assert.assertTrue(page.isCurrencyVND(page.getKpiTotalValueText()), "KPI Tổng giá trị tồn kho không có ₫/đ");
  }

  @Test(priority = 6, description = "TC_012 - KPI Thuốc sắp hết hạn hiển thị đúng (>=0)")
  public void TC_012_kpi_expiring_non_negative() {
    page.waitKpiLoaded();
    Assert.assertTrue(page.getKpiExpiring() >= 0, "KPI Thuốc sắp hết hạn bị âm / parse lỗi");
  }

  @Test(priority = 7, description = "TC_013 - KPI Thuốc dưới tồn tối thiểu hiển thị đúng (>=0)")
  public void TC_013_kpi_below_min_non_negative() {
    page.waitKpiLoaded();
    Assert.assertTrue(page.getKpiBelowMin() >= 0, "KPI Thuốc dưới tồn tối thiểu bị âm / parse lỗi");
  }

  @Test(priority = 8, description = "TC_014 - Icon KPI hiển thị + đúng màu theo thiết kế")
  public void TC_014_kpi_icon_visible_and_color_correct() {
    page.waitKpiLoaded();
    Assert.assertTrue(page.isKpiIconTotalTypesVisible(), "Không thấy icon KPI Tổng số loại thuốc");

    // NOTE: check class màu rất dễ fail nếu dev đổi theme/class
    Assert.assertTrue(page.isKpiIconTotalTypesColorCorrect(), "Icon KPI Tổng số loại thuốc sai class màu");
  }

  @Test(priority = 9, description = "TC_015 - Hiển thị biểu đồ Hoạt động Nhập/Xuất kho")
  public void TC_015_chart_visible() {
    Assert.assertTrue(page.isChartVisible(), "Chart không mount/không hiển thị");
  }

  @Test(priority = 10, description = "TC_016 - Trục X hiển thị label (>=1). (NOTE: thứ tự 7 ngày cần assert riêng)")
  public void TC_016_chart_xaxis_has_labels() {
    page.waitChartMounted();
    List<String> labels = page.getChartXLabels();
    Assert.assertTrue(labels.size() > 0, "Không có label trục X (chart/data chưa load): " + labels);
  }

  @Test(priority = 11, description = "TC_019 - Chart có ít nhất 1 bar (Nhập hoặc Xuất)")
  public void TC_019_chart_has_at_least_one_bar() {
    page.waitChartMounted();
    int importCount = page.getImportBarsCount();
    int exportCount = page.getExportBarsCount();
    Assert.assertTrue((importCount + exportCount) > 0, "Không có bar nào (Nhập/Xuất đều 0) => chart rỗng");
  }

  @Test(priority = 12, description = "TC_018 - Hover bar => tooltip hiện (Skip nếu chart không có bar)")
  public void TC_018_chart_tooltip_on_hover() {
    page.waitChartMounted();
    int totalBars = page.getImportBarsCount() + page.getExportBarsCount();
    if (totalBars == 0) {
      throw new SkipException("Skip TC_018: Chart không có bar để hover (data = 0).");
    }

    page.hoverAnyBarUntilTooltipVisible();
    Assert.assertTrue(page.isChartTooltipVisible(), "Tooltip không hiện sau khi hover bar");
  }

  @Test(priority = 13, description = "TC_020 - Hiển thị panel Cảnh báo")
  public void TC_020_alert_panel_visible() {
    Assert.assertTrue(page.isAlertPanelVisible(), "Không thấy panel cảnh báo");
  }

  @Test(priority = 14, description = "TC_021 + TC_023 - Alert: có item thì có severity; không có item thì hợp lệ")
  public void TC_021_023_alert_items_have_severity_or_empty() {
    Assert.assertTrue(page.isAlertPanelVisible(), "Không thấy panel cảnh báo");

    int n = page.getAlertItemsCount();
    if (n == 0) {
      Assert.assertTrue(true);
      return;
    }

    Assert.assertTrue(page.hasAnySeverityTag(), "Có item cảnh báo nhưng không thấy severity tag");
  }

  @Test(priority = 15, description = "TC_029 - Không xuất hiện thanh cuộn ngang")
  public void TC_029_no_horizontal_scroll() {
    Assert.assertFalse(page.hasHorizontalScroll(), "Có scroll ngang (UI bị tràn layout)");
  }
}


