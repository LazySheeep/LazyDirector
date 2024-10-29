package io.lazysheeep.lazydirector.command;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.ContextResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerSelectorResolver implements ContextResolver<Player[], BukkitCommandExecutionContext>
{
    @Override
    public Player[] getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument
    {
        String input = context.popFirstArg();
        if (input == null)
        {
            return new Player[0];
        }
        return Bukkit.selectEntities(context.getSender(), input)
                       .stream()
                       .filter(entity -> entity instanceof Player)
                       .map(entity -> (Player) entity)
                       .toArray(Player[]::new);
    }
}