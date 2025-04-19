package cz.cvut.fit.household.daos.interfaces;

import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;

import java.util.List;
import java.util.Optional;

public interface RecurringPatternDAO {

    RecurringPattern saveRecurringPattern(RecurringPattern recurringPattern);

    void deleteRecurringPattern(Long id);

    Optional<RecurringPattern> findRecurringPatternByID(Long id);

    List<RecurringPattern> findAllRecurringPattern();

}
