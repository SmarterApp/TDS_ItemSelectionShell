package tds.itemselection.impl.sets;

import tds.itemselection.impl.blueprint.Blueprint;

/**
 * NOTE - The CsetFactory's blueprint is used throughout the selection process.  This allows us to roll with other
 * class implementations
 */
public abstract class BlueprintEnabledCsetFactory {
  protected Blueprint bp;

  public Blueprint getBp() {
    return bp;
  }

  public void setBp(Blueprint bp) {
    this.bp = bp;
  }
}
