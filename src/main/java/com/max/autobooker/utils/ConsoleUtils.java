package com.max.autobooker.utils;

import java.util.Scanner;

/**
 * @author Maxime Rocchia
 */
public class ConsoleUtils {

    private static final Scanner sc = new Scanner(System.in);

    public static String ask(String instruction) {
        System.out.println(instruction);
        return sc.nextLine();
    }
}
