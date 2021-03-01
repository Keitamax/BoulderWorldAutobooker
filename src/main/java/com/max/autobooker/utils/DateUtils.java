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
package com.max.autobooker.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Maxime Rocchia
 */
public class DateUtils {

    public static int getMonth(Date date) {
        DateFormat monthExtractor = new SimpleDateFormat("MM");
        return Integer.valueOf(monthExtractor.format(date));
    }

    public static int getDayOfMonth(Date date) {
        DateFormat dayExtractor = new SimpleDateFormat("dd");
        return Integer.valueOf(dayExtractor.format(date));
    }

    public static String getFormattedDate(Date date) {
        DateFormat monthAndYearExtractor = new SimpleDateFormat("MMM yyyy");
        int dayOfMonth = getDayOfMonth(date);
        return dayOfMonth
                + getDayOfMonthSuffix(dayOfMonth)
                + " "
                + monthAndYearExtractor.format(date);
    }

    private static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}
