package com.fmning.tools.service.discord;

import com.fmning.tools.domain.DiscordRole;
import com.fmning.tools.repository.DiscordRoleMappingRepo;
import com.fmning.tools.repository.DiscordRoleRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static com.fmning.tools.util.DiscordUtil.fromRole;
import static com.fmning.tools.util.DiscordUtil.toHexString;

@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class RoleEventListener extends BaseEventListener {

    private final DiscordRoleRepo discordRoleRepo;
    private final DiscordRoleMappingRepo discordRoleMappingRepo;

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
}
