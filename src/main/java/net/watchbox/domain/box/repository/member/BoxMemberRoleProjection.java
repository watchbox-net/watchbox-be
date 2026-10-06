package net.watchbox.domain.box.repository.member;

import net.watchbox.domain.box.entity.member.BoxMemberRole;

/**
 * 박스 목록에서 "이 박스에서 내 권한" 만 뽑는 용도.
 *
 * <p>BoxMember 엔티티를 통째로 올리면 {@code bm.box} 프록시 초기화가 박스 수만큼 일어난다.
 * 필요한 건 boxId 와 role 둘뿐이라 컬럼만 select 한다.
 */
public interface BoxMemberRoleProjection {
    Long getBoxId();
    BoxMemberRole getRole();
}
