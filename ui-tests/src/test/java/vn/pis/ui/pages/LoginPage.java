package vn.pis.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPage {
    private WebDriver d;
    private WebDriverWait w;
    
    private By txtUser = By.id("username");
    private By txtPass = By.id("password");
    private By btnLogin = By.cssSelector("button[type='submit']");
    private By linkRegister = By.xpath("//a[contains(text(),'Đăng ký') or contains(text(),'Register')]");
    
    // Error messages
    private By errorUserEmpty = By.xpath("//*[contains(text(),'Tên đăng nhập không được để trống') or contains(text(),'Username is required')]");
    private By errorPassEmpty = By.xpath("//*[contains(text(),'Mật khẩu không được để trống') or contains(text(),'Password is required')]");
    private By errorInvalid = By.xpath("//*[contains(text(),'không đúng') or contains(text(),'invalid') or contains(text(),'không hợp lệ')]");
    private By errorUserMinLength = By.xpath("//*[contains(text(),'ít nhất') or contains(text(),'minimum')]");
    
    public LoginPage(WebDriver d) { 
        this.d = d;
        this.w = new WebDriverWait(d, Duration.ofSeconds(10));
    }
    
    public void open(String url) { 
        d.get(url);
        System.out.println("Opened URL: " + url);
    }
    
    public void login(String u, String p) {
        w.until(ExpectedConditions.visibilityOfElementLocated(txtUser)).sendKeys(u);
        d.findElement(txtPass).sendKeys(p);
        d.findElement(btnLogin).click();
    }
    
    public void clearUsername() {
        d.findElement(txtUser).clear();
    }
    
    public void clearPassword() {
        d.findElement(txtPass).clear();
    }
    
    public void enterUsername(String username) {
        d.findElement(txtUser).sendKeys(username);
    }
    
    public void enterPassword(String password) {
        d.findElement(txtPass).sendKeys(password);
    }
    
    public void clickLogin() {
        d.findElement(btnLogin).click();
    }
    
    public void clickRegisterLink() {
        d.findElement(linkRegister).click();
    }
    
    public String getUsernameFieldValue() {
        return d.findElement(txtUser).getAttribute("value");
    }
    
    public String getPasswordFieldValue() {
        return d.findElement(txtPass).getAttribute("value");
    }
    
    public boolean hasErrorUsernameEmpty() {
        try {
            List<WebElement> els = d.findElements(errorUserEmpty);
            return !els.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasErrorPasswordEmpty() {
        try {
            List<WebElement> els = d.findElements(errorPassEmpty);
            return !els.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasErrorInvalid() {
        try {
            List<WebElement> els = d.findElements(errorInvalid);
            return !els.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean hasErrorMinLength() {
        try {
            List<WebElement> els = d.findElements(errorUserMinLength);
            return !els.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getFirstErrorText() {
        try {
            List<WebElement> els = d.findElements(By.xpath("//div[contains(@class,'error') or contains(@class,'text-red') or @role='alert']//*[text()]"));
            if (!els.isEmpty()) return els.get(0).getText().trim();
            return "";
        } catch (Exception e) {
            return "";
        }
    }
    
    public boolean isLoginSuccess() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
        return !d.getCurrentUrl().contains("/login");
    }
    
    public String getCurrentUrl() {
        return d.getCurrentUrl();
    }
}
