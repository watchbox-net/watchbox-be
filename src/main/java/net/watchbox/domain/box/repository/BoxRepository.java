package net.watchbox.domain.box.repository;

import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.BoxType;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BoxRepository extends JpaRepository<Box, Long> {
    List<Box> findAllByOwnerAndBoxType(Member owner, BoxType boxType);
}
