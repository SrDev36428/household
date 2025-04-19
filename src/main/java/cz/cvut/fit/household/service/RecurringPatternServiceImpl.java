package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.interfaces.RecurringPatternDAO;
import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;
import cz.cvut.fit.household.service.interfaces.RecurringPatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecurringPatternServiceImpl implements RecurringPatternService {

    private final RecurringPatternDAO recurringPatternDAO;
    @Override
    public RecurringPattern saveRecurringPattern(RecurringPattern recurringPattern) {
        return recurringPatternDAO.saveRecurringPattern(recurringPattern);
    }

    @Override
    public void deleteRecurringPattern(RecurringPattern recurringPattern){
        recurringPatternDAO.deleteRecurringPattern(recurringPattern.getId());
    }
}
