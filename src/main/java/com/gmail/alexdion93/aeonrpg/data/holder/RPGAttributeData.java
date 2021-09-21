package com.gmail.alexdion93.aeonrpg.data.holder;

/**
 * A data container used to hold attribute data.
 * @author Alex Dion
 */
public class RPGAttributeData {
  private int flat = 0;
  private int scaling = 0;
  
  /**
   * Constructor
   * @param flat The flat value
   * @param scaling The scaling value
   */
  public RPGAttributeData(int flat, int scaling) {
    this.flat = flat;
    this.scaling = scaling;
  }
  
  /**
   * Returns the flat value.
   * @return the flat value.
   */
  public int getFlat() { return flat; }
  
  /**
   * Returns the scaling value.
   * @return the scaling value.
   */
  public int getScaling() { return scaling; }
  
  /**
   * Modifies the flat value.
   * @param flat The new value.
   */
  public void setFlat(int flat) { this.flat = flat; }
  
  /**
   * Modifies the scaling value.
   * @param scaling The new value.
   */
  public void setScaling(int scaling) { this.scaling = scaling; }
}
