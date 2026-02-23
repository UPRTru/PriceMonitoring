package prices.agent;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public final class WebDriverSupport implements AutoCloseable {

    private static final Duration IMPLICIT_WAIT = Duration.ofSeconds(5);
    private static final Duration EXPLICIT_WAIT = Duration.ofSeconds(10);

    private final ChromeOptions options;
    private WebDriver driver;
    private WebDriverWait webDriverWait;

    public WebDriverSupport() {
        this.options = configureChromeOptions();
    }

    private ChromeOptions configureChromeOptions() {
        ChromeOptions opts = new ChromeOptions();
        opts.setBinary("/usr/bin/google-chrome");
        opts.addArguments("--headless=new");
        opts.addArguments("--no-sandbox");
        opts.addArguments("--disable-dev-shm-usage");
        opts.addArguments("--disable-gpu");
        opts.addArguments("--window-size=1920,1080");
        opts.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        opts.setExperimentalOption("useAutomationExtension", false);
        opts.setExperimentalOption("excludeSwitches", new String[]{"enable-automation", "disable-extensions"});
        return opts;
    }

    public void createDriver() {
        if (driver == null) {
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        }
    }

    public WebDriverWait getWebDriver() {
        if (webDriverWait == null && driver != null) {
            webDriverWait = new WebDriverWait(driver, EXPLICIT_WAIT);
        }
        return webDriverWait;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void goToPage(String url) {
        if (driver == null) {
            createDriver();
        }
        driver.get(url);
        webDriverWait = new WebDriverWait(driver, EXPLICIT_WAIT);
    }

    public boolean isInitialized() {
        return driver != null;
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.quit();
            driver = null;
            webDriverWait = null;
        }
    }
}