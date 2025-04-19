package cz.cvut.fit.household.service;

import cz.cvut.fit.household.daos.interfaces.MaintenanceDAO;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.datamodel.entity.household.Household;
import cz.cvut.fit.household.datamodel.entity.maintenance.*;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.enums.MembershipRole;
import cz.cvut.fit.household.datamodel.enums.MembershipStatus;
import cz.cvut.fit.household.datamodel.enums.RecurringType;
import cz.cvut.fit.household.exception.NonExistentEntityException;
import cz.cvut.fit.household.service.interfaces.MaintenanceTaskService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MaintenanceServiceImplTest {
    @Mock
    MaintenanceDAO mockMaintenanceDAO;
    @Mock
    ApplicationEventPublisher mockEventPublisher;
    @Mock
    MaintenanceTaskService mockmaintenanceTaskService;
    @InjectMocks
    MaintenanceServiceImpl maintenanceService;
    Household household = new Household(1L, "testing", "testing", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    User user1 = new User("creator", "testing", "A", "A", "creator@gmail.com", new ArrayList<>());
    User user2 = new User("assignee", "testing", "B", "B", "assignee@gmail.com", new ArrayList<>());
    User user3 = new User("assignee2", "testing", "C", "C", "assigneetwo@gmail.com", new ArrayList<>());
    Membership creator = new Membership(1L, MembershipStatus.ACTIVE, MembershipRole.OWNER, user1, household);
    Membership assignee1 = new Membership(2L, MembershipStatus.ACTIVE, MembershipRole.REGULAR, user2, household);
    Membership assignee2 = new Membership(2L, MembershipStatus.ACTIVE, MembershipRole.REGULAR, user3, household);
    Maintenance maintenance1 = new Maintenance(1L, "ME1", "testing", assignee1, creator, household,
            Date.from(LocalDate.now().atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()), false, true,
            new ArrayList<>(), new ArrayList<>());
    Maintenance maintenance2 = new Maintenance(2L, "ME2", "testing", assignee2, creator, household,
            Date.from(LocalDate.now().atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()), false, true,
            new ArrayList<>(), new ArrayList<>());
    Maintenance updatedMaintenance1 = new Maintenance(1L, "Update ME1", "update testing", assignee1, creator, household,
            Date.from(LocalDate.now().atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()),
            Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()), false, true,
            new ArrayList<>(), new ArrayList<>());
    MaintenanceCreationDTO maintenanceCreationDTO1 = new MaintenanceCreationDTO("ME1", "testing", assignee1, creator,
            RecurringType.DAILY, Date.from(LocalDate.now().atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()), 1, LocalTime.now(),
            1, "", 1, Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()), new List<MaintenanceTaskCreationDTO>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<MaintenanceTaskCreationDTO> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(MaintenanceTaskCreationDTO maintenanceTaskCreationDTO) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends MaintenanceTaskCreationDTO> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends MaintenanceTaskCreationDTO> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public MaintenanceTaskCreationDTO get(int index) {
            return null;
        }

        @Override
        public MaintenanceTaskCreationDTO set(int index, MaintenanceTaskCreationDTO element) {
            return null;
        }

        @Override
        public void add(int index, MaintenanceTaskCreationDTO element) {

        }

        @Override
        public MaintenanceTaskCreationDTO remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @Override
        public ListIterator<MaintenanceTaskCreationDTO> listIterator() {
            return null;
        }

        @Override
        public ListIterator<MaintenanceTaskCreationDTO> listIterator(int index) {
            return null;
        }

        @Override
        public List<MaintenanceTaskCreationDTO> subList(int fromIndex, int toIndex) {
            return null;
        }
    }, new List<LocalTime>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<LocalTime> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(LocalTime localTime) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends LocalTime> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends LocalTime> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public LocalTime get(int index) {
            return null;
        }

        @Override
        public LocalTime set(int index, LocalTime element) {
            return null;
        }

        @Override
        public void add(int index, LocalTime element) {

        }

        @Override
        public LocalTime remove(int index) {
            return null;
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @Override
        public ListIterator<LocalTime> listIterator() {
            return null;
        }

        @Override
        public ListIterator<LocalTime> listIterator(int index) {
            return null;
        }

        @Override
        public List<LocalTime> subList(int fromIndex, int toIndex) {
            return null;
        }
    }, new ArrayList<>(), new ArrayList<>());
    @Captor
    ArgumentCaptor<Maintenance> argumentCaptor;
    RecurringPattern recurringPattern = new RecurringPattern(1L, 1, 1, 1, 1, LocalTime.now(), maintenance1, RecurringType.DAILY);

    @Before
    public void setUp() {
        household.setMemberships(Arrays.asList(creator, assignee1, assignee2));
        household.setMaintenances(Arrays.asList(maintenance1, maintenance2));

        when(mockMaintenanceDAO.findMaintenanceById(maintenance1.getId())).thenReturn(Optional.of(maintenance1));
        when(mockMaintenanceDAO.getAllMaintenances()).thenReturn(Arrays.asList(maintenance1, maintenance2));
        doNothing().when(mockMaintenanceDAO).deleteMaintenanceById(maintenance1.getId());
        doNothing().when(mockEventPublisher).publishEvent(any());
    }

    @Test
    public void addMaintenance() {
        when(mockMaintenanceDAO.saveMaintenance(any(Maintenance.class))).thenReturn(maintenance1);
        Maintenance maintenance = maintenanceService.addMaintenance(maintenanceCreationDTO1, household, creator, assignee1);
        assertEquals(maintenance1, maintenance);
        verify(mockMaintenanceDAO).saveMaintenance(any(Maintenance.class));
        verify(mockEventPublisher).publishEvent(any());
    }

    @Test
    public void updateMaintenance_ok() {
        when(mockMaintenanceDAO.saveMaintenance(any(Maintenance.class))).thenReturn(updatedMaintenance1);
        MaintenanceCreationDTO updateMaintenanceCreationDTO = new MaintenanceCreationDTO("Update ME1", "update testing", assignee1, creator,
                RecurringType.DAILY, Date.from(LocalDate.now().atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault()).toInstant()), 1, LocalTime.now(),
                1, "", 1, Date.from(LocalDate.now().atStartOfDay().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()), new List<MaintenanceTaskCreationDTO>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<MaintenanceTaskCreationDTO> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(MaintenanceTaskCreationDTO maintenanceTaskCreationDTO) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends MaintenanceTaskCreationDTO> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends MaintenanceTaskCreationDTO> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public MaintenanceTaskCreationDTO get(int index) {
                return null;
            }

            @Override
            public MaintenanceTaskCreationDTO set(int index, MaintenanceTaskCreationDTO element) {
                return null;
            }

            @Override
            public void add(int index, MaintenanceTaskCreationDTO element) {

            }

            @Override
            public MaintenanceTaskCreationDTO remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<MaintenanceTaskCreationDTO> listIterator() {
                return null;
            }

            @Override
            public ListIterator<MaintenanceTaskCreationDTO> listIterator(int index) {
                return null;
            }

            @Override
            public List<MaintenanceTaskCreationDTO> subList(int fromIndex, int toIndex) {
                return null;
            }
        }, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Maintenance maintenance = maintenanceService.updateMaintenance(maintenance1.getId(), updateMaintenanceCreationDTO, household, creator, assignee1);
        verify(mockMaintenanceDAO).saveMaintenance(any(Maintenance.class));
        //want to test the maintenance passsed has the same values as updatedMaintenanceDTO
        verify(mockMaintenanceDAO).saveMaintenance(argumentCaptor.capture());
        Maintenance capturedMaintenance = argumentCaptor.getValue();
        assertEquals(maintenance.getTitle(), capturedMaintenance.getTitle());
        assertEquals(maintenance.getDescription(), capturedMaintenance.getDescription());
        assertEquals(maintenance.getAssignee(), capturedMaintenance.getAssignee());
        assertEquals(maintenance.getCreator(), capturedMaintenance.getCreator());
        assertEquals(maintenance.getHouseHoLD(), capturedMaintenance.getHouseHoLD());
    }

    @Test
    public void updateMaintenance_NotExist() {
        try {
            maintenanceService.updateMaintenance(3L, maintenanceCreationDTO1, household, creator, assignee2);
            fail("Throwing no Exception");
        } catch (Exception e) {
            assertTrue(e instanceof NonExistentEntityException, "Throwing Incorrect Exception");
        }

    }

    @Test
    public void deleteMaintenance_ok() {
        maintenanceService.deleteMaintenance(maintenance1.getId());
        verify(mockMaintenanceDAO).deleteMaintenanceById(maintenance1.getId());
    }

    @Test
    public void deleteMaintenance_NotExists() {
        try {
            maintenanceService.deleteMaintenance(3L);
            fail("Not Throwing Exception");
        } catch (Exception e) {
            assertTrue(e instanceof NonExistentEntityException, "Throwing Incorrect Exception");
        }
    }

    @Test
    public void addRecurringPattern_ok() {
        maintenanceService.addRecurringPattern(1L, recurringPattern);
        verify(mockMaintenanceDAO).saveMaintenance(maintenance1);
        assertTrue(maintenance1.getRecurringPatterns().contains(recurringPattern));
    }

    @Test
    public void addRecurringPattern_NotExist() {
        try {
            maintenanceService.addRecurringPattern(3L, recurringPattern);
            fail("Not Throwing Exception");
        } catch (Exception e) {
            assertTrue(e instanceof NonExistentEntityException, "Throwing Incorrect Exception");
        }
    }

    @Test
    public void changeState_NotExist() {
        try {
            maintenanceService.changeState(new MaintenanceStateDTO(), 3L);
            fail("Not Throwing Exception");
        } catch (Exception e) {
            assertTrue(e instanceof NonExistentEntityException, "Throwing Incorrect Exception");
        }
    }

    @Test
    public void findMaintenanceByID() {
        maintenanceService.findMaintenanceById(1L);
        verify(mockMaintenanceDAO).findMaintenanceById(1L);
    }

    @Test
    public void getAll() {
        List<Maintenance> maintenanceList = maintenanceService.getAll();
        verify(mockMaintenanceDAO).getAllMaintenances();
        assertTrue(maintenanceList.contains(maintenance1));
        assertTrue(maintenanceList.contains(maintenance2));
    }

}