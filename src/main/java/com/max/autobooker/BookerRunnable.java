package com.max.autobooker;

import com.max.autobooker.dto.BookingInfo;
import com.max.autobooker.dto.Climber;
import com.max.autobooker.exceptions.BookingException;
import com.max.autobooker.utils.DateUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Maxime Rocchia
 * Runnable that will try to book for a climber on a specific date and timeslot.
 * If the date is too far (monday of current week is more than 1 week before monday of booking date), the thread will end without booking
 * The thread will sleep until the time of bookings opening
 */
public class BookerRunnable implements Runnable {
    private static final long TIMEOUT_SECONDS = 4L;
    private static final String BASE_URL = "https://www.picktime.com/566fe29b-2e46-4a73-ad85-c16bfc64b34b";
    private static final LocalTime BOOKINGS_OPENING_TIME = LocalTime.of(12, 0);

    private BookingInfo bookingInfo;
    private Climber climber;
    private ChromeDriver driver;
    private WebDriverWait webDriverWait;
    private boolean isRealBooking;

    public BookerRunnable(BookingInfo bookingInfo, Climber climber, boolean isRealBooking) {
        this.bookingInfo = bookingInfo;
        this.climber = climber;
        this.isRealBooking = isRealBooking;
    }

    @Override
    public void run() {
        try {
            checkBookingDay();

            driver = new ChromeDriver();
            webDriverWait = new WebDriverWait(driver, TIMEOUT_SECONDS);
            this.bookSlot(bookingInfo, climber, this.isRealBooking);
            System.out.println("Booking for " + climber.getName() + " SUCCESS");
        } catch (Exception e) {
            System.err.println("Booking for climber " + climber.getName() + " FAIL");
            System.err.println("Error occured: " + e.getMessage());
        }
//        finally {
//            if (driver != null) {
//                driver.close();
//            }
//        }
    }

    /**
     * Check that the today is not too early for booking
     * @throws InterruptedException
     */
    private void checkBookingDay() throws InterruptedException, BookingException {
        if(LocalDate.now().isAfter(bookingInfo.getDate())) {
            throw new BookingException(getBookingInfoString() + ": " + "The booking date is in the past");
        }

        LocalDate bookingOpeningDay;
        if(DateUtils.isDateOnWeekend(bookingInfo.getDate())) {
            LocalDate saturdayOfBookingWeek = DateUtils.getSaturdayOfSameWeek(bookingInfo.getDate());
            bookingOpeningDay = saturdayOfBookingWeek.minusDays(7);
            long daysUntilBookingsOpen = ChronoUnit.DAYS.between(LocalDate.now(), bookingOpeningDay);
            if(daysUntilBookingsOpen > 0) {
                throw new BookingException(getBookingInfoString() + ": " + "Bookings open on saturday 1 week before the booking date for weekend slots. It's too early today!");
            }
        } else {
            LocalDate mondayOfBookingWeek = DateUtils.getMondayOfSameWeek(bookingInfo.getDate());
            bookingOpeningDay = mondayOfBookingWeek.minusDays(7);
            long daysUntilBookingsOpen = ChronoUnit.DAYS.between(LocalDate.now(), bookingOpeningDay);
            if(daysUntilBookingsOpen > 0) {
                throw new BookingException(getBookingInfoString() + ": " + "Bookings open on monday 1 week before the booking date for weekdays slots. It's too early today!");
            }
        }

        if(ChronoUnit.DAYS.between(LocalDate.now(), bookingInfo.getDate()) > 6 && bookingOpeningDay.equals(LocalDate.now())) {
            waitUntilBookingsOpenToday();
        }
    }

    private void bookSlot(BookingInfo bookingInfo, Climber climber, boolean realBooking) throws InterruptedException {
        driver.get(BASE_URL);
        waitAndThenClick(By.cssSelector(".modal-header span"));
        waitAndThenClick(By.cssSelector("li:nth-child(1) > .bl > div:nth-child(1)"));

        int currentMonth = LocalDate.now().getMonthValue();
        int bookingMonth = bookingInfo.getDate().getMonthValue();
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
            System.out.println(getBookingInfoString() + ": " + "Could not find timeslot " + timeSlotLabel + ". Refreshing the page...");
            bookSlot(bookingInfo, climber, realBooking);
        }

        waitAndThenClick(By.cssSelector(".firstname"));
        clickAndInputText(By.cssSelector(".firstname"), climber.getName());
        clickAndInputText(By.cssSelector(".custemail"), climber.getEmail());
        clickAndInputText(By.cssSelector(".custmobile"), climber.getPhone());
        clickAndInputText(By.cssSelector(".other_of1"), bookingInfo.getPass());
        clickAndInputText(By.cssSelector(".other_of0"), bookingInfo.getPassHolder());

        if (realBooking) {
            driver.findElement(By.cssSelector(".booknow")).click();
        }
    }

    private String getBookingInfoString() {
        return "Booking " + bookingInfo.getDate() + " at " + bookingInfo.getTimeslot();
    }

    /**
     * Will put the thread to sleep until the time of the bookings come.
     */
    private void waitUntilBookingsOpenToday() throws InterruptedException {
        long timeDifference = Duration.between(LocalTime.now(), BOOKINGS_OPENING_TIME).getSeconds();
        if(timeDifference < 0) {
            return;
        }
        System.out.println(getBookingInfoString() + ": "
                + timeDifference + " seconds "
                + "(" + timeDifference / 60 + " mn) "
                + "until bookings open at " + BOOKINGS_OPENING_TIME.toString()
                + ". Sleeping now...");
        Thread.sleep((timeDifference - 1) * 1000);
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
     */
    private void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }
}
