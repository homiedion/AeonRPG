package com.gmail.alexdion93.aeonrpg.util;

/**
 * Allows for the translation of numbers into roman numerals
 *
 * @author Alex Dion
 *
 */
public class RomanNumeral {

  private final static String[] symbol = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
  private final static int[] values = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };

  /**
   * Parses out a Roman numeral from the provided Arabic numeral.
   *
   * @param number The integer we're trying to parse.
   * @return a roman numeral
   * @throws IllegalArgumentException if number is greater than 5000
   * @throws IllegalArgumentException if number is zero or negative
   */
  public static String parse(int number) {

    if (number > 5000) {
      throw new IllegalArgumentException("Number cannot be greater than 5000");
    }

    // Ignore negatives and zero
    if (number <= 0) {
      throw new IllegalArgumentException("Number cannot be zero or lower");
    }

    // Variables
    String value = "";

    // Loop all values.
    for (int i = 0; i < values.length; i++) {
      // While we exceed the value.
      while (number >= values[i]) {
        value += symbol[i];
        number -= values[i];
      }
    }

    // Return value.
    return value;
  }
}
