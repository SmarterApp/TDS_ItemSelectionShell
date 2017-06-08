package tds.builder;

import java.util.UUID;

import tds.itemselection.impl.sets.CsetGroupString;

public final class CsetGroupStringBuilder {
  // chronological order amongst all elements in a collection
  private int                           sequence;
  // the opportunity key from which the itemgroups came
  private UUID oppkey;
  private String groupString;

  private CsetGroupStringBuilder() {
  }

  public static CsetGroupStringBuilder aCsetGroupString() {
    return new CsetGroupStringBuilder();
  }

  public CsetGroupStringBuilder withSequence(int sequence) {
    this.sequence = sequence;
    return this;
  }

  public CsetGroupStringBuilder withOppkey(UUID oppkey) {
    this.oppkey = oppkey;
    return this;
  }

  public CsetGroupStringBuilder withGroupString(String groupString) {
    this.groupString = groupString;
    return this;
  }

  public CsetGroupString build() {
    return new CsetGroupString(oppkey, sequence, groupString);
  }
}
