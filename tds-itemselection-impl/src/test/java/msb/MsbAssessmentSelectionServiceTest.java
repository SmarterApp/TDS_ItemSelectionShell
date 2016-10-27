package msb;

import org.junit.Before;
import org.junit.Test;
import tds.itemselection.msb.MsbAssessmentSelectionService;
import tds.itemselection.msb.MsbAssessmentSelectionServiceImpl;

/**
 * Created by fairway on 10/27/16.
 */
public class MsbAssessmentSelectionServiceTest {

    private MsbAssessmentSelectionService msbAssessmentSelectionService;

    @Before
    public void setup() {
        msbAssessmentSelectionService = new MsbAssessmentSelectionServiceImpl();
    }

    @Test
    public void msbSelectorShouldReturnSegment1() {

    }
}
