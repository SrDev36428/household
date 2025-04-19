package cz.cvut.fit.household.repository;

import cz.cvut.fit.household.datamodel.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findByHouseholdIdOrderByIdDesc(Long householdId);

    void deleteAllByHouseholdId(Long householdId);
}
