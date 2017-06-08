package tds.itemselection.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tds.dll.api.IItemSelectionDLL;
import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.model.AlgorithmType;
import tds.itemselection.model.ItemResponse;
import tds.itemselection.selectors.ItemSelector;
import tds.itemselection.selectors.impl.FieldTestSelector;
import tds.itemselection.selectors.impl.FixedFormSelector;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.MsbAssessmentSelectionService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static tds.itemselection.model.ItemResponse.Status.SATISFIED;

@RunWith(MockitoJUnitRunner.class)
public class ItemSelectionServiceImplTest {

    private Map<AlgorithmType, ItemSelector> adaptiveItemSelectors;

    @Mock
    private ItemCandidatesService mockItemCandidatesService;

    @Mock
    private MsbAssessmentSelectionService mockMsbAssessmentSelectionService;

    @Mock
    private FixedFormSelector mockFixedFormSelector;

    @Mock
    private FieldTestSelector mockFieldTestSelector;

    private ItemSelectionServiceImpl service;

    @Before
    public void setup() {
        adaptiveItemSelectors = new HashMap<>();
        service = new ItemSelectionServiceImpl(adaptiveItemSelectors, mockItemCandidatesService, mockMsbAssessmentSelectionService, mockFixedFormSelector, mockFieldTestSelector);
    }

    @Test
    public void itShouldReturnASatisfiedResponseIfThereAreNoAdditionaItems() throws Exception {
        final UUID examId = UUID.randomUUID();
        final ItemCandidatesData satisfiedData = new ItemCandidatesData(examId, IItemSelectionDLL.SATISFIED);

        when(mockItemCandidatesService.getItemCandidates(examId)).thenReturn(satisfiedData);

        final ItemResponse<ItemGroup> response = service.getNextItemGroup(examId, false);
        assertThat(response.getResponseStatus()).isEqualTo(SATISFIED);
    }
}