package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.LogDAO;
import cz.cvut.fit.household.datamodel.entity.Log;
import cz.cvut.fit.household.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LogDAOImpl implements LogDAO {

    private final LogRepository logRepository;

    @Override
    public Log saveLog(Log log) {
        return logRepository.save(log);
    }

    @Override
    public void deleteAllLogsByHouseholdId(Long id) {
        logRepository.deleteAllByHouseholdId(id);
    }

    @Override
    public List<Log> findLogByHouseholdIdOrderByIdDesc(Long id) {
        return logRepository.findByHouseholdIdOrderByIdDesc(id);
    }

}
