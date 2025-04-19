package cz.cvut.fit.household.repository.maintenance;

import cz.cvut.fit.household.datamodel.entity.maintenance.MaintenanceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {

    @Query("select m from MaintenanceTask m where m.maintenance.assignee.id = :assignee")
    List<MaintenanceTask> getMaintenanceTasksByAssignee(@Param("assignee") Long id);

}
