package net.watchbox.domain.box.repository.content;

import net.watchbox.domain.box.entity.Box;
import net.watchbox.domain.box.entity.content.BoxContent;
import net.watchbox.domain.content.common.entity.Content;
import net.watchbox.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoxContentRepository extends JpaRepository<BoxContent, Long> {
    boolean existsByPublisherAndContent(Member publisher, Content content);

    Long countByBox(Box box);

    List<BoxContent> findAllByBox(Box box);

    boolean existsByBoxAndContent(Box box, Content content);

    boolean existsByPublisherAndBoxAndContent(Member publisher, Box box, Content content);

    void deleteAllByBox(Box box);

    Optional<BoxContent> findByBoxAndContent(Box box, Content content);

    // 박스 컨텐츠 조회 시, SubContent인 영화/TV/인물 엔티티도 함께 조회
    @Query("SELECT bc FROM BoxContent bc " +
            "JOIN FETCH bc.content c " +
            "LEFT JOIN FETCH c.movie " +
            "LEFT JOIN FETCH c.tv " +
            "LEFT JOIN FETCH c.person " +
            "WHERE bc.box = :box")
    List<BoxContent> findAllWithContentDetailsByBox(@Param("box") Box box);
}
