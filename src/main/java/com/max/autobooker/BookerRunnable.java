/*******************************************************************************
 * Copyright(c) FriarTuck Pte Ltd ("FriarTuck"). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of FriarTuck.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with FriarTuck.
 *
 * FriarTuck MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-
 * INFRINGEMENT. FriarTuck SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 ******************************************************************************/
package com.max.autobooker;

import com.max.autobooker.dto.BookingInfo;
import com.max.autobooker.dto.Climber;
import com.max.autobooker.utils.DateUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Date;

/**
 * @author Maxime Rocchia
 */
public class BookerRunnable implements Runnable {
    private static final long TIMEOUT_SECONDS = 5L;
    private static final String BASE_URL = "https://www.picktime.com/566fe29b-2e46-4a73-ad85-c16bfc64b34b";

    private BookingInfo bookingInfo;
    private Climber climber;
    private ChromeDriver driver;
    private WebDriverWait webDriverWait;
    private boolean isRealBooking;

    public BookerRunnable(BookingInfo bookingInfo, Climber climber, boolean isRealBooking) {
        this.bookingInfo = bookingInfo;
        this.climber = climber;
        this.isRealBooking = isRealBooking;
        driver = new ChromeDriver();
        webDriverWait = new WebDriverWait(driver, TIMEOUT_SECONDS);
    }

    @Override
    public void run() {
        try {
            this.bookSlot(bookingInfo, climber, this.isRealBooking);
            Thread.sleep(60000);
        } catch (Exception e) {
            System.out.println("Error occured: " + e.getMessage());
        }
//        finally {
//            if (driver != null) {
//                driver.close();
//            }
//        }
    }

    private void bookSlot(BookingInfo bookingInfo, Climber climber, boolean realBooking) throws InterruptedException {
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
    private void waitAndThenClick(By by) throws TimeoutException, InterruptedException {
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
    public void clickAndInputText(By by, String inputText) {
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
    private void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }
}
