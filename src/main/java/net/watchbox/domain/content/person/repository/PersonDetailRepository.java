package net.watchbox.domain.content.person.repository;

import net.watchbox.domain.content.person.entity.PersonDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonDetailRepository extends JpaRepository<PersonDetail, Long> {
}
