package io.busata.wrcserver.importer.client;

import io.busata.wrcserver.importer.client.models.WRCSeasonDataTo;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WRCImportService {

    private WebDriver driver;
    private final WRCApi wrcApi;


    public WRCSeasonDataTo importEvents() {
        WRCSeasonKeys key = getWRCSeasonKey();
        return wrcApi.getSeasonData(key.contelPageId(), key.season(), key.competition());
    }

    public WRCSeasonKeys getWRCSeasonKey() {
        driver = createFirefoxDriver();
        driver.get("https://www.wrc.com/en/results-standings/rally-results/");

        WebElement calendar = driver.findElement(By.id("calendar"));
        String contelPageId = calendar.getAttribute("data-contelpageid");
        String competition = calendar.getAttribute("data-competition");
        String season = calendar.getAttribute("data-season");

        return new WRCSeasonKeys(contelPageId, season, competition);
    }


    private FirefoxDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxBinary binary = new FirefoxBinary();
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary(binary);
        options.setHeadless(true);
        options.addArguments("--window-size=1920,1080");
        return new FirefoxDriver(options);
    }

}
