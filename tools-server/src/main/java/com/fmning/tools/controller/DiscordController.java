package com.fmning.tools.controller;

import com.fmning.tools.domain.*;
import com.fmning.tools.dto.DiscordAdminDto;
import com.fmning.tools.dto.DiscordRoleDto;
import com.fmning.tools.dto.DiscordRolePositionDto;
import com.fmning.tools.repository.*;
import com.fmning.tools.ToolsProperties;
import com.fmning.tools.dto.DiscordObjectDto;
import com.fmning.tools.service.discord.DiscordService;
import com.fmning.tools.type.DiscordRoleType;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fmning.tools.util.WebUtil.TOTAL_COUNT;

@RestController
@RequestMapping(value = "/api/discord")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@CommonsLog
public class DiscordController {

    private final DiscordService discordService;
    private final ToolsProperties toolsProperties;
    private final DiscordGuildRepo discordGuildRepo;
    private final DiscordUserRepo discordUserRepo;
    private final DiscordRoleRepo discordRoleRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;
    private final DiscordUserLogRepo discordUserLogRepo;

    @GetMapping("/{guildId}/channels")
    @PreAuthorize("hasRole('DC')")
    public List<DiscordObjectDto> getChannels(@PathVariable("guildId") String guildId) {
        if ("default".equalsIgnoreCase(guildId)) {
            return discordService.getTextChannels(toolsProperties.getDcDefaultGuildId());
        } else {
            return discordService.getTextChannels(guildId);
        }
    }

    @GetMapping("/{guildId}/role-configs")
    @PreAuthorize("hasRole('DC')")
    public List<DiscordObjectDto> getRoleConfig(@PathVariable("guildId") String guildId) {
        if ("default".equalsIgnoreCase(guildId)) {
            return discordService.getRoles(toolsProperties.getDcDefaultGuildId());
        } else {
            return discordService.getRoles(guildId);
        }
    }

    @GetMapping("/{guildId}/user-logs")
    @PreAuthorize("hasRole('DC')")
    public ResponseEntity<List<DiscordUserLog>> getUserLogs(@PathVariable("guildId") String guildId, @RequestParam(value = "limit", defaultValue = "15") int limit,
                                                            @RequestParam(value = "offset", defaultValue = "0") int offset, @RequestParam(value = "keyword", required = false) String keyword,
                                                            @RequestParam(value = "from", required = false) Instant from, @RequestParam(value = "to", required = false) Instant to,
                                                            @RequestParam(value = "action", required = false) DiscordUserLogActionType action) {
        if (!"default".equalsIgnoreCase(guildId)) return ResponseEntity.ok(Collections.emptyList());
        Specification<DiscordUserLog> spec = (Specification<DiscordUserLog>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null) {
                List<Predicate> or = new ArrayList<>();
                or.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("name")), "%" + keyword.trim().toUpperCase() + "%"));
                or.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("nickname")), "%" + keyword.trim().toUpperCase() + "%"));
                predicates.add(criteriaBuilder.or(or.toArray(new Predicate[0])));
            }

            if (from != null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("created"), from));
            if (to != null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("created"), to));
            if (action != null) predicates.add(criteriaBuilder.equal(root.get("action"), action));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<DiscordUserLog> page = discordUserLogRepo.findAll(spec, PageRequest.of(offset/limit, limit, Sort.by(Sort.Direction.DESC, "id")));
        return ResponseEntity.ok()
                .header(TOTAL_COUNT, String.valueOf(page.getTotalElements()))
                .body(page.getContent());
    }

    @GetMapping("/{guildId}/users")
    @PreAuthorize("hasRole('DC')")
    public ResponseEntity<List<DiscordUser>> getUsers(@PathVariable("guildId") String guildId, @RequestParam(value = "limit", defaultValue = "15") int limit,
                                                            @RequestParam(value = "offset", defaultValue = "0") int offset, @RequestParam(value = "keyword", required = false) String keyword) {
        if (!"default".equalsIgnoreCase(guildId)) return ResponseEntity.ok(Collections.emptyList());
        Specification<DiscordUser> spec = (Specification<DiscordUser>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("name")), "%" + keyword.trim().toUpperCase() + "%"));
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("nickname")), "%" + keyword.trim().toUpperCase() + "%"));
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("apexId")), "%" + keyword.trim().toUpperCase() + "%"));
            }
            return predicates.size() == 0 ? criteriaBuilder.and(predicates.toArray(new Predicate[0])) : criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };

        Page<DiscordUser> page = discordUserRepo.findAll(spec, PageRequest.of(offset/limit, limit, Sort.by(Sort.Direction.ASC, "id")));
        return ResponseEntity.ok()
                .header(TOTAL_COUNT, String.valueOf(page.getTotalElements()))
                .body(page.getContent());
    }

    @GetMapping("/{guildId}/roles")
    @PreAuthorize("hasRole('DC')")
    public  ResponseEntity<List<DiscordRoleDto>> getRoles(@PathVariable("guildId") String guildId, @RequestParam(value = "limit", defaultValue = "15") int limit,
                                      @RequestParam(value = "offset", defaultValue = "0") int offset, @RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "type", required = false) DiscordRoleType type) {
        if (!"default".equalsIgnoreCase(guildId)) return ResponseEntity.ok(Collections.emptyList());
        Page<DiscordRoleDto> page;
        if (keyword != null && type != null) {
            page = discordRoleRepo.findAllByKeywordAndType(keyword.toUpperCase(), type, PageRequest.of(offset/limit, limit, Sort.by(Sort.Direction.DESC, "position")));
        } else if (keyword != null) {
            page = discordRoleRepo.findAllByKeyword(keyword.toUpperCase(), PageRequest.of(offset/limit, limit, Sort.by(Sort.Direction.DESC, "position")));
        } else if (type != null) {
            page = discordRoleRepo.findAllByType(type, PageRequest.of(offset/limit, limit, Sort.by(Sort.Direction.DESC, "position")));
        } else {
            page = discordRoleRepo.findAll(PageRequest.of(offset / limit, limit, Sort.by(Sort.Direction.DESC, "position")));
        }
        return ResponseEntity.ok()
                .header(TOTAL_COUNT, String.valueOf(page.getTotalElements()))
                .body(page.getContent().stream().map(r -> {
                    if (r.getOwnerId() == null) return r;
                    DiscordUser user = discordUserRepo.findById(r.getOwnerId()).orElse(null);
                    if (user != null) return r.withOwnerName(user.getNickname());
                    return r;
                }).collect(Collectors.toList()));
    }

    @GetMapping("/{guildId}/config")
    @PreAuthorize("hasRole('DC')")
    public DiscordGuild getConfig(@PathVariable("guildId") String guildId) {
        Optional<DiscordGuild> discordGuildOptional;
        if ("default".equalsIgnoreCase(guildId)) {
            discordGuildOptional = discordGuildRepo.findById(toolsProperties.getDcDefaultGuildId());
        } else {
            discordGuildOptional = discordGuildRepo.findById(guildId);
        }

        return discordGuildOptional.orElseGet(() -> DiscordGuild.builder().build());
    }

    @PostMapping("/{guildId}/config")
    @PreAuthorize("hasRole('DC')")
    public DiscordGuild updateConfig(@PathVariable("guildId") String guildId, @RequestBody DiscordGuild discordGuild) {

        // Validate welcome settings
        if (StringUtils.isBlank(discordGuild.getId())) {
            throw new IllegalArgumentException("Id is required for configuration update.");
        } else if (StringUtils.isBlank(discordGuild.getWelcomeTitle())) {
            throw new IllegalArgumentException("Title for welcome message is required.");
        } else if (StringUtils.isBlank(discordGuild.getWelcomeDescription())) {
            throw new IllegalArgumentException("Description for welcome message is required.");
        } else if (StringUtils.isBlank(discordGuild.getWelcomeFooter())) {
            throw new IllegalArgumentException("Footer for welcome message is required.");
        } else if (discordGuild.isWelcomeEnabled() && discordGuild.getWelcomeChannelId() == null) {
            throw new IllegalArgumentException("Welcome announcement channel has to be set when welcome message is turned on.");
        }

        // Validate role settings
        if (discordGuild.isRoleEnabled()) {
            if (discordGuild.getRoleLevelRequirement() < 0) {
                throw new IllegalArgumentException("Level requirement cannot be a negative number");
            } else if (StringUtils.isBlank(discordGuild.getRoleLevelRankRoleId())) {
                throw new IllegalArgumentException("Level role location must be selected.");
            } else if (StringUtils.isBlank(discordGuild.getRoleBoostRankRoleId())) {
                throw new IllegalArgumentException("Boost role location must be selected.");
            }
        }

        // Validate birthday settings
        if (discordGuild.isBirthdayEnabled()) {
            if (discordGuild.getBirthdayChannelId() == null) {
                throw new IllegalArgumentException("Birthday announcement channel has to be set when birthday blessing is turned on.");
            } else if (StringUtils.isBlank(discordGuild.getBirthdayMessage())) {
                throw new IllegalArgumentException("Birthday blessing message is required.");
            }
        }

        Optional<DiscordGuild> discordGuildOptional;
        if ("default".equalsIgnoreCase(guildId)) {
            discordGuildOptional = discordGuildRepo.findById(toolsProperties.getDcDefaultGuildId());
        } else {
            discordGuildOptional = discordGuildRepo.findById(guildId);
        }

        if (discordGuildOptional.isPresent()) {
            discordGuildRepo.save(discordGuild);
            return discordGuild;
        } else {
            throw new IllegalArgumentException("Id is required for configuration update.");
        }
    }

    @GetMapping("/admin/birthday")
    @PreAuthorize("hasRole('ADMIN')")
    public void runBirthday() {
        discordService.announceBirthDay();
    }

    @GetMapping("/admin/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DiscordRoleDto>> sync() {
//        discordService.seedRoles(toolsProperties.getDcDefaultGuildId());
//        discordService.seedMembers(toolsProperties.getDcDefaultGuildId());

//        DiscordRole a;
//        DiscordRoleDto d = new DiscordRoleDto(a.Id, a.);

        Page<DiscordRoleDto> roles = discordRoleRepo.findAll(PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "position")));
        System.out.println(1);
        return ResponseEntity.ok(roles.getContent());
    }

    @PostMapping("/admin/add-role")
    @PreAuthorize("hasRole('ADMIN')")
    public void addRole(@RequestBody DiscordAdminDto discordAdminDto) {
        discordService.addRole(discordAdminDto.getMemberId(), discordAdminDto.getRoleId());
    }

    @PostMapping("/admin/remove-role")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeRole(@RequestBody DiscordAdminDto discordAdminDto) {
        discordService.removeRole(discordAdminDto.getMemberId(), discordAdminDto.getRoleId());
    }

    @PostMapping("/admin/move-role")
    @PreAuthorize("hasRole('ADMIN')")
    public void moveRole(@RequestBody DiscordAdminDto discordAdminDto) {
        discordService.moveRole(discordAdminDto.getMemberId(), discordAdminDto.getPosition());
    }

    @GetMapping("/admin/role-position")
    @PreAuthorize("hasRole('ADMIN')")
    public String position() {
        StringBuilder sb = new StringBuilder();
        List<DiscordRolePositionDto> positionDtos = discordRoleMappingRepo.findAllBoostRoleByPosition();
        for (DiscordRolePositionDto p : positionDtos) {
            DiscordRole role = discordRoleRepo.findById(p.getRoleId()).orElse(null);
            if (role != null) {
                sb.append(role.getPosition()).append(" = ").append(role.getName()).append(" = ").append(p.getRoleId())
                        .append(" = ").append(p.getOwnerName()).append(" = ").append(p.getOwnerBoostTime()).append("<br />");
            } else {
                sb.append("FAILED").append(" = ").append(p.getOwnerName()).append("<br />");
            }
        }

        return sb.toString();

//        return RandomStringUtils.randomAlphanumeric(6);
    }

    @GetMapping("/admin/role-fix-preview")
    @PreAuthorize("hasRole('ADMIN')")
    public String rolePreview(@RequestParam("roleId") String roleId) {
        List<DiscordUser> users = discordUserRepo.findByRolesContaining(roleId);

        StringBuilder existedSb = new StringBuilder();
        StringBuilder missingSb = new StringBuilder();
        DiscordRoleMapping owner = discordRoleMappingRepo.findByTypeAndRoleId(DiscordRoleType.LEVEL, roleId);
        for (DiscordUser u : users) {
            if (!u.getId().equals(owner.getOwnerId())) {
                DiscordRoleMapping mapping = discordRoleMappingRepo.findByOwnerIdAndTypeAndRoleId(u.getId(), DiscordRoleType.SHARE, roleId);
                if (mapping == null) {
                    missingSb.append("Missing share to ").append(u.getNickname()).append("<br />");
                } else {
                    existedSb.append("Already have share to ").append(u.getNickname()).append("<br />");
                }
            }
        }

        return "Already existed mappings: <br /><br />" + existedSb.toString() + "<br /><br />" + "Missing mappings:<br />" + missingSb.toString();
    }

    @GetMapping("/admin/role-fix")
    @PreAuthorize("hasRole('ADMIN')")
    public String roleFix(@RequestParam("roleId") String roleId) {
        List<DiscordUser> users = discordUserRepo.findByRolesContaining(roleId);
        StringBuilder sb = new StringBuilder();
        DiscordRoleMapping owner = discordRoleMappingRepo.findByTypeAndRoleId(DiscordRoleType.LEVEL, roleId);
        for (DiscordUser u : users) {
            if (!u.getId().equals(owner.getOwnerId())) {
                DiscordRoleMapping mapping = discordRoleMappingRepo.findByOwnerIdAndTypeAndRoleId(u.getId(), DiscordRoleType.SHARE, roleId);
                if (mapping == null) {
                    sb.append("Added share to ").append(u.getNickname()).append("<br />");
                    discordRoleMappingRepo.save(DiscordRoleMapping.builder()
                            .guildId(toolsProperties.getDcDefaultGuildId())
                            .roleId(roleId)
                            .enabled(true)
                            .code(RandomStringUtils.randomAlphanumeric(6))
                            .type(DiscordRoleType.SHARE)
                            .ownerId(u.getId())
                            .approverId(null)
                            .created(Instant.now())
                            .build());
                }
            }
        }
        return sb.toString();
    }

    @GetMapping("/admin/test")
    @PreAuthorize("hasRole('ADMIN')")
    public void roleFix() {
        discordRoleMappingRepo.deleteByCreated(Instant.now());
    }




}
