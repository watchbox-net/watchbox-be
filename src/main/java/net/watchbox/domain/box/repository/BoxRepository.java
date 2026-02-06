package net.watchbox.domain.box.repository;

import net.watchbox.domain.box.entity.Box;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {

}
