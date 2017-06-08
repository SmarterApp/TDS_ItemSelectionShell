package tds.itemselection.model;

/**
 * Response when adding off grades
 */
public class OffGradeResponse {
  public static final String SUCCESS = "success";
  public static final String FAILED = "failed";

  private final String status;
  private final String reason;

  public OffGradeResponse(final String status, final String reason) {
    this.status = status;
    this.reason = reason;
  }

  /**
   * @return either the constant SUCCESS or FAILED
   */
  public String getStatus() {
    return status;
  }

  /**
   * @return the reason it was successful or failed
   */
  public String getReason() {
    return reason;
  }
}
