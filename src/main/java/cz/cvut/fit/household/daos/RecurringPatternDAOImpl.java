package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.RecurringPatternDAO;
import cz.cvut.fit.household.datamodel.entity.maintenance.RecurringPattern;
import cz.cvut.fit.household.repository.maintenance.RecurringPatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecurringPatternDAOImpl implements RecurringPatternDAO {

    private final RecurringPatternRepository recurringPatternRepository;

    @Override
    public RecurringPattern saveRecurringPattern(RecurringPattern recurringPattern) {
        return recurringPatternRepository.save(recurringPattern);
    }

    @Override
    public void deleteRecurringPattern(Long id) {
        recurringPatternRepository.deleteById(id);
    }

    @Override
    public Optional<RecurringPattern> findRecurringPatternByID(Long id) {
        return recurringPatternRepository.findById(id);
    }

    @Override
    public List<RecurringPattern> findAllRecurringPattern() {
        return recurringPatternRepository.findAll();
    }
}
