package com.fmning.tools.repository;

import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.dto.DiscordRoleDto;
import com.fmning.tools.type.DiscordRoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordRoleRepo extends CrudRepository<DiscordRole, String> {

//    Page<DiscordRoleDto> findAllBySpec(Specification<DiscordRole> spec, Pageable pageable);

    @Query("select new com.fmning.tools.dto.DiscordRoleDto(a.id, a.guildId, a.name, a.color, a.position, a.created, b.ownerId, b.type) FROM DiscordRole a left join DiscordRoleMapping b on a.id = b.roleId where b.type != 'SHARE' or b.type is null")
    Page<DiscordRoleDto> findAll(Pageable pageable);

    @Query("select new com.fmning.tools.dto.DiscordRoleDto(a.id, a.guildId, a.name, a.color, a.position, a.created, b.ownerId, b.type) FROM DiscordRole a left join DiscordRoleMapping b on a.id = b.roleId where (b.type != 'SHARE' or b.type is null) and upper(a.name) like CONCAT('%', :keyword,'%')")
    Page<DiscordRoleDto> findAllByKeyword(String keyword, Pageable pageable);

    @Query("select new com.fmning.tools.dto.DiscordRoleDto(a.id, a.guildId, a.name, a.color, a.position, a.created, b.ownerId, b.type) FROM DiscordRole a left join DiscordRoleMapping b on a.id = b.roleId where b.type = :type")
    Page<DiscordRoleDto> findAllByType(DiscordRoleType type, Pageable pageable);

    @Query("select new com.fmning.tools.dto.DiscordRoleDto(a.id, a.guildId, a.name, a.color, a.position, a.created, b.ownerId, b.type) FROM DiscordRole a left join DiscordRoleMapping b on a.id = b.roleId where upper(a.name) like CONCAT('%',:keyword,'%') and b.type = :type")
    Page<DiscordRoleDto> findAllByKeywordAndType(String keyword, DiscordRoleType type, Pageable pageable);

    default String getNameById(String id) {
        DiscordRole role = findById(id).orElse(null);
        return role == null ? null : role.getName();
    }
}
