package net.watchbox.domain.content.sub.person.repository;

import net.watchbox.domain.content.sub.person.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByTmdbId(Long tmdbId);

    @Query("SELECT p FROM Person p JOIN FETCH p.personDetail WHERE p.tmdbId = :tmdbId")
    Optional<Person> findWithDetailByTmdbId(@Param("tmdbId") Long tmdbId);

}
