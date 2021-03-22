package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.repository.DiscordRoleMappingRepo;
import com.fmning.tools.repository.DiscordRoleRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.List;

import static com.fmning.tools.util.DiscordUtil.fromRole;
import static com.fmning.tools.util.DiscordUtil.toHexString;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class RoleEventListener extends BaseEventListener {

    private final DiscordRoleRepo discordRoleRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;

    private final List<String> rankRoleList = Arrays.asList("784949763685875723", "784949724721578034",
            "784949693499179048", "784949655406510122", "784949613928120360", "784949540485595137", "784949499310899200");

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        DiscordRole role = discordRoleRepo.findById(event.getRole().getId()).orElse(null);
        if (role == null) discordRoleRepo.save(fromRole(event.getRole()));
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        discordRoleRepo.findById(event.getRole().getId()).ifPresent(discordRoleRepo::delete);
        discordRoleMappingRepo.deleteByRoleId(event.getRole().getId());
    }

    @Override
    public void onGenericRoleUpdate(@Nonnull GenericRoleUpdateEvent event) {
        DiscordRole role = discordRoleRepo.findById(event.getRole().getId()).orElse(fromRole(event.getRole()));
        role.setName(event.getRole().getName());
        role.setColor(toHexString(event.getRole().getColor()));
        role.setPosition(event.getRole().getPositionRaw());

        discordRoleRepo.save(role);
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        for (Role r : event.getRoles()) {
            if (rankRoleList.contains(r.getId())) {
                Role deliminator = event.getGuild().getRoleById("797734233073385472");
                if (deliminator != null) {
                    event.getGuild().addRoleToMember(event.getMember(), deliminator).queue();
                }
                return;
            }
        }

    }
}
