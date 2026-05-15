package net.watchbox.domain.content.sub.person.repository;

import net.watchbox.domain.content.sub.person.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

}
