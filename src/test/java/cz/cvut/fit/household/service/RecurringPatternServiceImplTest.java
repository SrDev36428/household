package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.RecurringPatternDAOImpl;
import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
@RunWith(MockitoJUnitRunner.class)
public class RecurringPatternServiceImplTest {

    @Mock
    private RecurringPatternDAOImpl recurringPatternDAO;

    @InjectMocks
    private RecurringPatternServiceImpl recurringPatternService;

    RecurringPattern testRecurringPattern = new RecurringPattern();

    @Before
    public void setUp()  {
        testRecurringPattern.setId(1L);
        Mockito.when(recurringPatternDAO.saveRecurringPattern(testRecurringPattern)).thenReturn(testRecurringPattern);
        Mockito.doNothing().when(recurringPatternDAO).deleteRecurringPattern(testRecurringPattern.getId());
    }

    @Test
    public void saveRecurringPattern() {
        RecurringPattern result = recurringPatternService.saveRecurringPattern(testRecurringPattern);

        assertEquals(testRecurringPattern, result);
        Mockito.verify(recurringPatternDAO , Mockito.times(1)).saveRecurringPattern(testRecurringPattern);
    }

    @Test
    public void deleteRecurringPattern() {
        recurringPatternService.deleteRecurringPattern(testRecurringPattern);

        Mockito.verify(recurringPatternDAO , Mockito.times(1)) .deleteRecurringPattern(testRecurringPattern.getId());
    }
}