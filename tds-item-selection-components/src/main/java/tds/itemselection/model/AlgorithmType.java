package tds.itemselection.model;

/**
 * The available algorithm types that are supported by item selection
 */
public enum AlgorithmType {
  FIXED_FORM("fixedform"),
  FIELD_TEST("fieldtest"),
  ADAPTIVE2("adaptive2"),
  ADAPTIVE("adaptive"),
  SATISFIED("satisfied");

  private final String type;

  AlgorithmType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public static AlgorithmType fromType(String algorithmType) {
    if (algorithmType == null) throw new IllegalArgumentException("The algorithm type cannot be null");

    for (AlgorithmType algorithm : values()) {
      if (algorithmType.equalsIgnoreCase(algorithm.getType())) {
        return algorithm;
      }
    }
    // No Algorithm found for algorithm type
    throw new IllegalArgumentException(String.format("No Algorithm found with the name %s", algorithmType));
  }
}
