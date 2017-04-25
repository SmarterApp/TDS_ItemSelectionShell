package tds.itemselection.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import tds.itemselection.model.AlgorithmType;
import tds.itemselection.selectors.ItemSelector;
import tds.itemselection.selectors.impl.FieldTestSelector;
import tds.itemselection.selectors.impl.FixedFormSelector;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.ItemSelectionService;
import tds.itemselection.services.MsbAssessmentSelectionService;
import tds.itemselection.services.SegmentService;
import tds.itemselection.services.impl.ItemSelectionServiceImpl;
import tds.itemselection.services.impl.MsbAssessmentSelectionServiceImpl;

@Configuration
public class ItemSelectionConfiguration {
  @Bean
  public ItemSelectionService getItemSelectionService(final ItemCandidatesService itemCandidatesService,
                                                      final MsbAssessmentSelectionService msbAssessmentSelectionService,
                                                      @Qualifier("adaptiveSelector") ItemSelector adaptiveSelector,
                                                      FieldTestSelector fieldTestSelector,
                                                      FixedFormSelector fixedFormSelector) {

    Map<AlgorithmType, ItemSelector> adaptiveSelectors = new HashMap<>();

    adaptiveSelectors.put(AlgorithmType.ADAPTIVE, adaptiveSelector);
    adaptiveSelectors.put(AlgorithmType.ADAPTIVE2, adaptiveSelector);

    return new ItemSelectionServiceImpl(
      adaptiveSelectors,
      itemCandidatesService,
      msbAssessmentSelectionService,
      fixedFormSelector,
      fieldTestSelector
    );
  }

  @Bean
  public MsbAssessmentSelectionService getMsbAssessmentService(@Qualifier("adaptiveSelector") ItemSelector adaptiveSelector,
                                                               ItemCandidatesService itemCandidatesService,
                                                               SegmentService segmentService) {
    return new MsbAssessmentSelectionServiceImpl(adaptiveSelector, itemCandidatesService, segmentService);
  }

  @Bean
  public FieldTestSelector getFieldTestSelector(final ItemCandidatesService itemCandidatesService) {
    return new FieldTestSelector(itemCandidatesService);
  }

  @Bean
  public FixedFormSelector getFixedFormSelector(final ItemCandidatesService itemCandidatesService) {
    return new FixedFormSelector(itemCandidatesService);
  }
}
