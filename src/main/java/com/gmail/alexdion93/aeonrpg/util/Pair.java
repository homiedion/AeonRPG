package com.gmail.alexdion93.aeonrpg.util;

/**
 * A class used to hold multiple variables for access.
 * @author Alex Dion
 *
 * @param <A> An class of the user's choice to represent the key
 * @param <B> An class of the user's choice to represent the value
 */
public class Pair<A, B> {

  private A key;
  private B value;
  
  /**
   * Constructor
   * @param key The key for this tuple
   * @param value The value for this tuple
   */
  public Pair(A key, B value) {
    this.key = key;
    this.value = value;
  }
  
  /**
   * Returns the key stored within the tuple
   * @return the key stored within the tuple
   */
  public A getKey() {
    return key;
  }
  
  /**
   * Returns the value stored within the tuple
   * @return the value stored within the tuple
   */
  public B getValue() {
    return value;
  }
}
