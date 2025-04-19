package cz.cvut.fit.household.repository.maintenance;

import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringPatternRepository extends JpaRepository<RecurringPattern,Long> {
}
