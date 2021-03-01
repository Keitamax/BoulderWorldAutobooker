package com.max.autobooker;

import com.max.autobooker.utils.ConsoleUtils;
import com.max.autobooker.utils.DateUtils;
import com.max.autobooker.utils.JsonParser;
import com.max.autobooker.utils.OsUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class AutoBooker {

    private static final long TIMEOUT_SECONDS = 5L;

    private static WebDriver driver;
    private static WebDriverWait webDriverWait;

    private static final String BASE_URL = "https://www.picktime.com/566fe29b-2e46-4a73-ad85-c16bfc64b34b";

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            String userDirectory = System.getProperty("user.dir");
//        ConsoleUtils.ask("Please copy all files from autobooker_drivers folder into "
//                + userDirectory
//                + "/autobooker_drivers");

            String chromeVersion = getChromeVersion();
            boolean realBooking = "B".equalsIgnoreCase(ConsoleUtils.ask("Run for test (T) or real booking (B)? "));
            String jsonContent = readJsonFile();

            if (!configureSelenium(userDirectory, chromeVersion)) {
                return;
            }

            BookingInfo bookingInfo = JsonParser.parseJson(jsonContent);
            for (Climber climber : bookingInfo.getClimbers()) {
                bookSlot(bookingInfo, climber, realBooking);
                Thread.sleep(2000); //Pause to allow loading of booking confirmation
            }
        } finally {
            if(driver != null) {
                driver.close();
            }
        }
    }

    private static boolean configureSelenium(String userDirectory, String chromeVersion) {
        String driverRelativeFilePath = "";
        if (OsUtils.isWindows()) {
            System.out.println("WINDOWS OS detected, loading driver");
            driverRelativeFilePath = "autobooker_drivers"
                    + File.separator + "windows"
                    + File.separator + "chromedriver" + chromeVersion + ".exe";
        } else if (OsUtils.isMac()) {
            System.out.println("MAC OS detected, loading driver");
            driverRelativeFilePath = "autobooker_drivers"
                    + File.separator + "mac"
                    + File.separator + "chromedriver" + chromeVersion;
        } else {
            System.out.println("Incompatible OS. This tool can only run on Windows or Mac.");
            return false;
        }
        String driverPath = userDirectory + File.separator + driverRelativeFilePath;
        System.setProperty("webdriver.chrome.driver", driverPath);
        driver = new ChromeDriver();
        webDriverWait = new WebDriverWait(driver, TIMEOUT_SECONDS);
        return true;
    }

    private static String getChromeVersion() {
        String chromeVersion = ConsoleUtils.ask("This tool works only with Chrome. Input chrome version (88 or 89): ");
        if (!chromeVersion.equals("88") && !chromeVersion.equals("89")) {
            System.out.println("Incompatible Chrome version.");
            return getChromeVersion();
        } else {
            return chromeVersion;
        }
    }

    private static String readJsonFile() {
        String jsonPath = ConsoleUtils.ask("Input JSON path: (e.g. C:\\Me\\example.json or example.json if in same dir): ");
        try {
            FileInputStream fis = new FileInputStream(jsonPath);
            return IOUtils.toString(fis, "UTF-8");
        } catch (IOException e) {
            System.out.println("Cannot read JSON file.");
            return readJsonFile();
        }
    }

    private static void bookSlot(BookingInfo bookingInfo, Climber climber, boolean realBooking) throws InterruptedException {
        driver.get(BASE_URL);

        waitAndThenClick(By.cssSelector(".modal-header span"));
        waitAndThenClick(By.cssSelector("li:nth-child(1) > .bl > div:nth-child(1)"));

        int currentMonth = DateUtils.getMonth(new Date());
        int bookingMonth = DateUtils.getMonth(bookingInfo.getDate());
        if (bookingMonth > currentMonth) { //change month if booking date is on next month
            waitAndThenClick(By.cssSelector(".date-right"));
        }

        String timeSlotLabel = "\"" + DateUtils.getFormattedDate(bookingInfo.getDate()) + ", "
                + bookingInfo.getTimeslot()
                + "\"";
        try {
            waitAndThenClick(By.xpath("//li[div[contains(string(), "
                    + timeSlotLabel
                    + ")]]"));
        } catch (TimeoutException e) {
            System.out.println("Could not find timeslot " + timeSlotLabel + ". Refreshing the page...");
            bookSlot(bookingInfo, climber, realBooking);
        }

        clickAndInputText(By.cssSelector(".firstname"), climber.getName());
        clickAndInputText(By.cssSelector(".custemail"), climber.getEmail());
        clickAndInputText(By.cssSelector(".custmobile"), climber.getPhone());
        clickAndInputText(By.cssSelector(".other_of1"), bookingInfo.getPass());
        clickAndInputText(By.cssSelector(".other_of0"), bookingInfo.getPassHolder());

        if (realBooking) {
            driver.findElement(By.cssSelector(".booknow")).click();
        }
    }

    /**
     * Waits until the element appears then clicks on it
     *
     * @param by Description of the element to look for
     * @throws TimeoutException if element didn't appear
     */
    private static void waitAndThenClick(By by) throws TimeoutException, InterruptedException {
        webDriverWait.until((ExpectedConditions.visibilityOfElementLocated(by)));
        try {
            WebElement element = driver.findElement(by);
            scrollToElement(element);
            element.click();
        } catch (org.openqa.selenium.StaleElementReferenceException ex) {
            //workaround to prevent error from element not being in yet DOM
            System.out.println("Click on element failed. Trying again");
            Thread.sleep(500);
            WebElement element = driver.findElement(by);
            scrollToElement(element);
            element.click();
        }
    }

    /**
     * Clicks on an element and then input given text
     *
     * @param by        Description of the element to look for
     * @param inputText Text to input in the element
     */
    public static void clickAndInputText(By by, String inputText) {
        WebElement element = driver.findElement(by);
        scrollToElement(element);
        element.click();
        element.sendKeys(inputText);
    }

    /**
     * Scrolls to the given element to ensure it is displayed on current screen
     *
     * @param element
     */
    private static void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }
}
