package vn.pis.ui.tests;

import org.testng.*;

public class PIS2_ConsoleLogger implements ITestListener, ISuiteListener {

    private void say(String msg){
        String line = "[PIS2][Listener] " + msg;
        System.out.println(line);
        Reporter.log(line, true);
    }

    // Suite level
    @Override public void onStart(ISuite suite){ say("BẮT ĐẦU SUITE: " + suite.getName()); }
    @Override public void onFinish(ISuite suite){ say("KẾT THÚC SUITE: " + suite.getName()); }

    // Test method level
    @Override public void onTestStart(ITestResult r){ say("START: " + r.getMethod().getMethodName()); }
    @Override public void onTestSuccess(ITestResult r){ say("PASS : " + r.getMethod().getMethodName()); }
    @Override public void onTestFailure(ITestResult r){
        say("FAIL : " + r.getMethod().getMethodName() + " | " + r.getThrowable());
    }
    @Override public void onTestSkipped(ITestResult r){ say("SKIP : " + r.getMethod().getMethodName()); }
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult r){}

    @Override public void onStart(ITestContext ctx){ say("Context start: " + ctx.getName()); }
    @Override public void onFinish(ITestContext ctx){ say("Context finish: " + ctx.getName()); }
}
