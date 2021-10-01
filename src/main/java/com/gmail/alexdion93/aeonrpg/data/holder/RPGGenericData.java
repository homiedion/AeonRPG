package com.gmail.alexdion93.aeonrpg.data.holder;

/**
 * A data container used to hold attribute data.
 * @author Alex Dion
 */
public class RPGGenericData {
  private int primary = 0;
  private int secondary = 0;
  private String string = null;
  
  /**
   * Constructor
   * @param flat The flat value
   * @param scaling The scaling value
   */
  public RPGGenericData(int primary, int secondary, String string) {
    this.primary = primary;
    this.secondary = secondary;
    this.string = string;
  }
  
  /**
   * Returns the primary value.
   * @return the primary value.
   */
  public int getPrimary() { return primary; }
  
  /**
   * Returns the secondary value.
   * @return the secondary value.
   */
  public int getSecondary() { return secondary; }
  
  /**
   * Returns the string value.
   * @return the string value.
   */
  public String getString() { return string; }
  
  /**
   * Modifies the primary value.
   * @param primary The new value.
   */
  public void setPrimary(int primary) { this.primary = primary; }
  
  /**
   * Modifies the secondary value.
   * @param secondary The new value.
   */
  public void setSecondary(int secondary) { this.secondary = secondary; }
  
  /**
   * Modifies the string value.
   * @param string The new value.
   */
  public void setString(String string) { this.string = string; }
}
