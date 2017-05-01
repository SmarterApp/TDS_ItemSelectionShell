package tds.itemselection.services.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import tds.itemselection.base.ItemCandidatesData;
import tds.itemselection.base.ItemGroup;
import tds.itemselection.model.AlgorithmType;
import tds.itemselection.model.ItemResponse;
import tds.itemselection.selectors.ItemSelector;
import tds.itemselection.selectors.impl.FieldTestSelector;
import tds.itemselection.selectors.impl.FixedFormSelector;
import tds.itemselection.services.ItemCandidatesService;
import tds.itemselection.services.ItemSelectionService;
import tds.itemselection.services.MsbAssessmentSelectionService;

import static tds.itemselection.model.ItemResponse.Status.FAILURE;
import static tds.itemselection.model.ItemResponse.Status.SATISFIED;

/**
 * NOTE - Port of {@link AIROnline2013}
 */
public class ItemSelectionServiceImpl implements ItemSelectionService {
  private static final Logger logger = LoggerFactory.getLogger(ItemSelectionServiceImpl.class);

  private final Map<AlgorithmType, ItemSelector> adaptiveItemSelectors;
  private final ItemCandidatesService itemCandidatesService;
  private final MsbAssessmentSelectionService msbAssessmentSelectionService;
  private final FixedFormSelector fixedFormSelector;
  private final FieldTestSelector fieldTestSelector;

  public ItemSelectionServiceImpl(Map<AlgorithmType, ItemSelector> adaptiveItemSelectors,
                                  ItemCandidatesService itemCandidatesService,
                                  MsbAssessmentSelectionService msbAssessmentSelectionService,
                                  FixedFormSelector fixedFormSelector,
                                  FieldTestSelector fieldTestSelector) {
    this.adaptiveItemSelectors = adaptiveItemSelectors;
    this.itemCandidatesService = itemCandidatesService;
    this.msbAssessmentSelectionService = msbAssessmentSelectionService;
    this.fixedFormSelector = fixedFormSelector;
    this.fieldTestSelector = fieldTestSelector;
  }

  @Override
  public ItemResponse<ItemGroup> getNextItemGroup(UUID examId, boolean isMsb) {
    ItemGroup result = null;
    ItemCandidatesData itemCandidates;
    ItemSelector selector;
    String algorithm;
    String message = "";

    try {
      if (isMsb) {
        itemCandidates = msbAssessmentSelectionService.selectFixedMsbSegment(examId);
      } else {
        itemCandidates = itemCandidatesService.getItemCandidates(examId);
      }

      if (!itemCandidates.getIsSimulation()) {
        itemCandidates.setSession(null);
      }
      algorithm = itemCandidates.getAlgorithm();

      AlgorithmType algorithmType = AlgorithmType.fromType(algorithm);

      switch (algorithmType) {
        case FIXED_FORM:
          result = fixedFormSelector.getNextItemGroup(itemCandidates);
          break;
        case FIELD_TEST:
          result = fieldTestSelector.getNextItemGroup(itemCandidates);
          break;
        case ADAPTIVE:
        case ADAPTIVE2:
          selector = adaptiveItemSelectors.get(algorithmType);
          if (selector == null) {
            message = String.format("Unsupported adaptive algorithm: %s", algorithm);
          } else {
            result = selector.getNextItemGroup(itemCandidates);
            if (selector.getItemSelectorError() != null) {
              message = selector.getItemSelectorError();
            } else if (result == null) {
              if (selector.isSegmentCompleted()) {
                // this segment has been terminated based on configured conditions.
                //  Call recursively in case there are more segments to administer.
                //  Eventually we'll drop down into the SATISFIED case.
                if (logger.isDebugEnabled()) {
                  logger.debug("Recursing to select next item group for exam: {}", examId);
                }
                return getNextItemGroup(examId, isMsb);
              } else {
                message = "Adaptive item selection failed: Segment is not completed";
              }
            }
          }
          break;
        case SATISFIED:
          return new ItemResponse<>(SATISFIED);
        default:
          message = String.format("Unknown algorithm:  %s", itemCandidates.getAlgorithm());
      }


      if (result != null) {
        /*
         * These null checks are required due to the recursive call in selector.isSegmentCompleted and was added
         * after the addition of support for Multi-Stage Braille Assessments.
         * Without these checks, the values returned from the Braille selector will be overridden.
         */
        if (result.getSegmentID() == null) {
          result.setSegmentID(itemCandidates.getSegmentID());
        }
        if (result.getSegmentKey() == null) {
          result.setSegmentKey(itemCandidates.getSegmentKey());
        }
        if (result.getSegmentPosition() == 0) {
          result.setSegmentPosition(itemCandidates.getSegmentPosition());
        }
      } else if (StringUtils.isNotEmpty(message)) {
        logger.error("Unable to select next ItemGroup because " + message);
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return new ItemResponse<>(e.getMessage());
    }
    return new ItemResponse<>(result);
  }
}
