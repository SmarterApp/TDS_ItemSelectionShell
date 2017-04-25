package tds.itemselection.model;

import com.google.common.base.Optional;

/**
 * Represents a response when finding items
 * @param <T>
 */
public class ItemResponse<T> {
  private final T responseData;
  private final String errorMessage;

  /**
   * @param responseData the data to be included in the response when there are no errors
   */
  public ItemResponse(final T responseData) {
    this.responseData = responseData;
    this.errorMessage = null;
  }

  /**
   * @param errorMessage the error message to explain why there is not data
   */
  public ItemResponse(final String errorMessage) {
    this.responseData = null;
    this.errorMessage = errorMessage;
  }

  /**
   * @return With contiain T if response doesn't have an error
   */
  public Optional<T> getResponseData() {
    return Optional.fromNullable(responseData);
  }

  /**
   * @return the error message if action could not be processed
   */
  public Optional<String> getErrorMessage() {
    return Optional.fromNullable(errorMessage);
  }
}
