package cz.cvut.fit.household.daos;

import cz.cvut.fit.household.daos.interfaces.MembershipDAO;
import cz.cvut.fit.household.datamodel.entity.Membership;
import cz.cvut.fit.household.repository.filter.MembershipFilter;
import cz.cvut.fit.household.repository.membership.jpa.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MembershipDAOImpl implements MembershipDAO {

    private final MembershipRepository membershipRepository;

    @Override
    public Membership saveMembership (Membership membership) {
        return membershipRepository.save(membership);
    }

    @Override
    public List<Membership> filterMemberships (MembershipFilter membershipFilter) {
        return membershipRepository.filterMemberships(membershipFilter);
    }

    @Override
    public Optional<Membership> findMembershipById (Long id) {
        return membershipRepository.findById(id);
    }

    @Override
    public List<Membership> findAllMemberships() {
        return membershipRepository.findAll();
    }

    @Override
    public List<Membership> findMembershipsByUsername(String username) {
        return membershipRepository.findMembershipsByUsername(username);
    }

}
