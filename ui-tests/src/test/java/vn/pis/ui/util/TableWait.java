package vn.pis.ui.util;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public final class TableWait {

    private TableWait() {}

    private static String norm(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }

    private static String signature(WebDriver driver, By rangeLocator, By totalLocator, By dataRowsLocator) {
        String range = "";
        String total = "";
        try { range = norm(driver.findElement(rangeLocator).getText()); } catch (Exception ignored) {}
        try { total = norm(driver.findElement(totalLocator).getText()); } catch (Exception ignored) {}

        StringBuilder top = new StringBuilder();
        List<WebElement> rows = driver.findElements(dataRowsLocator);
        int take = Math.min(rows.size(), 3);
        for (int i = 0; i < take; i++) {
            try { top.append("|").append(norm(rows.get(i).getText())); }
            catch (StaleElementReferenceException e) { return "STALE"; }
        }
        return range + "||" + total + "||" + top;
    }

    public static void waitStable(
            WebDriver driver,
            long stableMillis,
            long timeoutMillis,
            By rangeLocator,
            By totalLocator,
            By dataRowsLocator
    ) {
        long end = System.currentTimeMillis() + timeoutMillis;
        String last = signature(driver, rangeLocator, totalLocator, dataRowsLocator);
        long stableStart = System.currentTimeMillis();

        while (System.currentTimeMillis() < end) {
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}

            String now = signature(driver, rangeLocator, totalLocator, dataRowsLocator);
            if ("STALE".equals(now) || "STALE".equals(last)) {
                last = now;
                stableStart = System.currentTimeMillis();
                continue;
            }

            if (now.equals(last)) {
                if (System.currentTimeMillis() - stableStart >= stableMillis) return;
            } else {
                last = now;
                stableStart = System.currentTimeMillis();
            }
        }

        Assert.fail("Table không ổn định trong thời gian chờ. Last signature=" + last);
    }
}
