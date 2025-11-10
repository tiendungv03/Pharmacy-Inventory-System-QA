package vn.pis.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private WebDriver d; private WebDriverWait w;
    private By txtUser = By.id("username");
    private By txtPass = By.id("password");
    private By btnLogin = By.cssSelector("button[type='submit']");


    public LoginPage(WebDriver d){ 
        this.d = d;
        this.w = new WebDriverWait(d, Duration.ofSeconds(10));
    }
    public void open(String url){ 
        d.get(url);
        System.out.println("Opened URL: " + url);
     }
    public void login(String u,String p){
        w.until(ExpectedConditions.visibilityOfElementLocated(txtUser)).sendKeys(u);
        d.findElement(txtPass).sendKeys(p);
        d.findElement(btnLogin).click();
    }
}