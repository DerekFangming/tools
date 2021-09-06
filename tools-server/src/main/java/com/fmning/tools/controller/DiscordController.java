package com.fmning.tools.controller;

import com.fmning.tools.domain.*;
import com.fmning.tools.dto.DiscordRoleDto;
import com.fmning.tools.repository.*;
import com.fmning.tools.ToolsProperties;
import com.fmning.tools.dto.DiscordObjectDto;
import com.fmning.tools.service.discord.DiscordService;
import com.fmning.tools.type.DiscordRoleType;
import com.fmning.tools.type.DiscordUserLogActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import net.dv8tion.jda.api.entities.ChannelType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    private final DiscordUserLogRepo discordUserLogRepo;
    private final DiscordCategoryRepo discordCategoryRepo;
    private final DiscordChannelRepo discordChannelRepo;

    @GetMapping("/{guildId}/text-channels")
    @PreAuthorize("hasRole('DC')")
    public List<DiscordObjectDto> getTextChannels(@PathVariable("guildId") String guildId) {
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

    @GetMapping("/{guildId}/categories")
    @PreAuthorize("hasRole('DC')")
    public  ResponseEntity<List<DiscordCategory>> getCategories(@PathVariable("guildId") String guildId) {
        if (!"default".equalsIgnoreCase(guildId)) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(discordCategoryRepo.findAll());
    }

    @GetMapping("/{guildId}/channels")
    @PreAuthorize("hasRole('DC')")
    public  ResponseEntity<List<DiscordChannel>> getChannels(@PathVariable("guildId") String guildId, @RequestParam(value = "limit", defaultValue = "15") int limit,
    @RequestParam(value = "offset", defaultValue = "0") int offset, @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "type", required = false) ChannelType type) {
        if (!"default".equalsIgnoreCase(guildId)) return ResponseEntity.ok(Collections.emptyList());

        Specification<DiscordChannel> spec = (Specification<DiscordChannel>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null) predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("name")), "%" + keyword.trim().toUpperCase() + "%"));
            if (type != null) predicates.add(criteriaBuilder.equal(root.get("type"), type));

            return predicates.size() == 0 ? criteriaBuilder.and(predicates.toArray(new Predicate[0])) : criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };

        Page<DiscordChannel> page = discordChannelRepo.findAll(spec, PageRequest.of(offset/limit, limit, Sort.by(Sort.Direction.ASC, "created")));
        long total = page.getTotalElements();
        List<DiscordChannel> result = page.getContent().stream().map(c -> {
            if (c.getType() == ChannelType.VOICE) {
                DiscordUser owner = discordUserRepo.findByBoostChannelIdOrTempChannelId(c.getId(), c.getId());
                if (owner != null) {
                    return c.withOwnerName(owner.getNickname());
                }
            }
            return c;
        }).collect(Collectors.toList());

        return ResponseEntity.ok()
                .header(TOTAL_COUNT, String.valueOf(total))
                .body(result);
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

    @GetMapping("/{guildId}/calculate-states")
    @PreAuthorize("hasRole('DC')")
    public void calculateStats(@PathVariable("guildId") String guildId) {
        discordService.calculateStates();
    }


}
