package prices.agent;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WebDriverSupport {

    private final ChromeOptions options;
    private WebDriver driver;
    private WebDriverWait webDriver;

    public WebDriverSupport() {
        this.options = new ChromeOptions();
        System.setProperty("webdriver.chrome.driver", "C:/chrome-win64/chromedriver.exe");
        this.options.addArguments("--headless=new");
        this.options.addArguments("--no-sandbox");
        this.options.addArguments("--disable-dev-shm-usage");
        this.options.addArguments("--disable-gpu");
        this.options.addArguments("--window-size=1920,1080");
        this.options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        this.options.setExperimentalOption("useAutomationExtension", false);
        this.options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
    }

    public WebDriverWait getWebDriver() {
        return webDriver;
    }

    public void createDriver() {
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    public void goToPage(String url) {
        driver.get(url);
        webDriver = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void closeDriver() {
        driver.quit();
    }
}
