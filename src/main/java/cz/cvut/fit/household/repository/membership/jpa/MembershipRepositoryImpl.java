package cz.cvut.fit.household.repository.membership.jpa;

import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.repository.filter.MembershipFilter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class MembershipRepositoryImpl implements MembershipCustomRepository {
    
    private EntityManager entityManager;

    @Override
    public List<Membership> filterMemberships(MembershipFilter membershipFilter) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Membership> criteriaQuery = criteriaBuilder.createQuery(Membership.class);
        Root<Membership> root = criteriaQuery.from(Membership.class);
        List<Predicate> predicates = new ArrayList<>();

        if (membershipFilter.getId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("id"), membershipFilter.getId()));
        }

        if (membershipFilter.getUsername() != null) {
            predicates.add(criteriaBuilder.equal(root.get("user").get("username"), membershipFilter.getUsername()));
        }

        if (membershipFilter.getHouseholdId() != null) {
            predicates.add(criteriaBuilder.equal(root.get("household").get("id"), membershipFilter.getHouseholdId()));
        }

        if (membershipFilter.getStatus() != null) {
            predicates.add(criteriaBuilder.equal(root.get("status"), membershipFilter.getStatus()));
        }

        return entityManager.createQuery(criteriaQuery.where(predicates.toArray(new Predicate[]{}))).getResultList();
    }
}

