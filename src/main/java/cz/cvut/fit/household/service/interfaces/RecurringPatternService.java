package cz.cvut.fit.household.service.interfaces;

import cz.cvut.fit.household.daos.interfaces.RecurringPatternDAO;
import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;

public interface RecurringPatternService {
    RecurringPattern saveRecurringPattern(RecurringPattern recurringPattern);

    void deleteRecurringPattern(RecurringPattern recurringPattern);
}
