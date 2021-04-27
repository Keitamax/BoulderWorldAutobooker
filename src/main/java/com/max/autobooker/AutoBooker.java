package com.max.autobooker;

import com.max.autobooker.dto.BookingInfo;
import com.max.autobooker.dto.Climber;
import com.max.autobooker.utils.ConsoleUtils;
import com.max.autobooker.utils.JsonParser;
import com.max.autobooker.utils.OsUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class AutoBooker {

    public static void main(String[] args) throws IOException {
        String userDirectory = System.getProperty("user.dir");

        String chromeVersion = getChromeVersion();
        boolean realBooking = "B".equalsIgnoreCase(ConsoleUtils.ask("Run for test (T) or real booking (B)? (if test, the bot will not click on the Book button)"));
        System.out.println("Real booking: " + realBooking);
        String jsonContent = readJsonFile();

        configureSelenium(userDirectory, chromeVersion);

        Collection<BookingInfo> bookingInfos = JsonParser.parseJson(jsonContent);
        for (BookingInfo bookingInfo : bookingInfos) {
            for(Climber climber : bookingInfo.getClimbers()) {
                BookerRunnable booker = new BookerRunnable(bookingInfo, climber, realBooking);
                Thread thread = new Thread(booker);
                thread.start();
            }
        }
    }

    private static void configureSelenium(String userDirectory, String chromeVersion) {
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
            System.err.println("Incompatible OS. This tool can only run on Windows or Mac.");
            throw new RuntimeException();
        }
        String driverPath = userDirectory + File.separator + driverRelativeFilePath;
        System.setProperty("webdriver.chrome.driver", driverPath);
    }

    private static String getChromeVersion() {
        String chromeVersion = ConsoleUtils.ask("This tool works only with Chrome. Input chrome version (88 or 89 or 90): ");
        if (!chromeVersion.equals("88") && !chromeVersion.equals("89") && !chromeVersion.equals("90")) {
            System.out.println("Incompatible Chrome version.");
            return getChromeVersion();
        } else {
            return chromeVersion;
        }
    }

    private static String readJsonFile() {
        String jsonPath = ConsoleUtils.ask("Input JSON path: (e.g. C:\\Me\\bookings.json or bookings.json if in same directory): ");
        try {
            FileInputStream fis = new FileInputStream(jsonPath);
            return IOUtils.toString(fis, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Cannot read JSON file.");
            return readJsonFile();
        }
    }
}
