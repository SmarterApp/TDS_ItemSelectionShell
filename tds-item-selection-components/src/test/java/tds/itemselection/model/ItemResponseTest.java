package tds.itemselection.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tds.itemselection.model.ItemResponse.Status.FAILURE;
import static tds.itemselection.model.ItemResponse.Status.SATISFIED;
import static tds.itemselection.model.ItemResponse.Status.SUCCESS;

public class ItemResponseTest {

    @Test
    public void itShouldCreateASuccessfulResponse() {
        final Object response = new Object();
        final ItemResponse<Object> itemResponse = new ItemResponse<>(response);
        assertThat(itemResponse.getResponseData().orNull()).isEqualTo(response);
        assertThat(itemResponse.getErrorMessage().isPresent()).isFalse();
        assertThat(itemResponse.getResponseStatus()).isEqualTo(SUCCESS);
    }

    @Test
    public void itShouldCreateAFailureResponse() {
        final String message = "A Failure Message";
        final ItemResponse<Object> itemResponse = new ItemResponse<>(message);
        assertThat(itemResponse.getResponseData().isPresent()).isFalse();
        assertThat(itemResponse.getErrorMessage().orNull()).isEqualTo(message);
        assertThat(itemResponse.getResponseStatus()).isEqualTo(FAILURE);
    }

    @Test
    public void itShouldCreateASatisfiedResponse() {
        final ItemResponse<Object> itemResponse = new ItemResponse<>(SATISFIED);
        assertThat(itemResponse.getResponseData().isPresent()).isFalse();
        assertThat(itemResponse.getErrorMessage().isPresent()).isFalse();
        assertThat(itemResponse.getResponseStatus()).isEqualTo(SATISFIED);
    }

    @Test
    public void itShouldCreateAFailureResponseFromANullItem() {
        final ItemResponse<Object> itemResponse = new ItemResponse<>((Object)null);
        assertThat(itemResponse.getResponseData().isPresent()).isFalse();
        assertThat(itemResponse.getErrorMessage().isPresent()).isTrue();
        assertThat(itemResponse.getResponseStatus()).isEqualTo(FAILURE);
    }
}