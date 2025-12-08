package net.watchpeople.domain.box.repository;

import net.watchpeople.domain.box.entity.SharedBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedBoxRepository extends JpaRepository<SharedBox, Long> {
}
