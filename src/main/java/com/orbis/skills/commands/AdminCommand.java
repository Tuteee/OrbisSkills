package com.orbis.skills.commands;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.skills.Skill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final OrbisSkills plugin;

    

    public AdminCommand(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
       

        if (!sender.hasPermission("orbisskills.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
               

                plugin.getConfigManager().reload();
                sender.sendMessage(ChatColor.GREEN + "OrbisSkills config reloaded!");
                break;
            case "reset":
               

                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin reset <player> <skill>");
                    return true;
                }

                resetPlayerSkill(sender, args[1], args[2]);
                break;
            case "setlevel":
               

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin setlevel <player> <skill> <level>");
                    return true;
                }

                setPlayerSkillLevel(sender, args[1], args[2], args[3]);
                break;
            case "addexp":
               

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin addexp <player> <skill> <amount>");
                    return true;
                }

                addPlayerSkillExp(sender, args[1], args[2], args[3]);
                break;
            case "removeexp":
               

                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /skillsadmin removeexp <player> <skill> <amount>");
                    return true;
                }

                removePlayerSkillExp(sender, args[1], args[2], args[3]);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                showAdminHelp(sender);
                break;
        }

        return true;
    }

    

    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "==== OrbisSkills Admin Help ====");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin reload " + ChatColor.WHITE + "- Reload config");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin reset <player> <skill> " + ChatColor.WHITE + "- Reset player skill");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin setlevel <player> <skill> <level> " + ChatColor.WHITE + "- Set player skill level");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin addexp <player> <skill> <amount> " + ChatColor.WHITE + "- Add experience to player skill");
        sender.sendMessage(ChatColor.YELLOW + "/skillsadmin removeexp <player> <skill> <amount> " + ChatColor.WHITE + "- Remove experience from player skill");
    }

    

    private void resetPlayerSkill(CommandSender sender, String playerName, String skillName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }

        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + skillName);
            return;
        }

        UUID targetUuid = target.getUniqueId();
        plugin.getDataManager().resetSkill(targetUuid, skill.getName());

        sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName() + "'s " + skill.getDisplayName() + " skill!");
        target.sendMessage(ChatColor.YELLOW + "Your " + skill.getDisplayName() + " skill has been reset by an admin.");
    }

    

    private void setPlayerSkillLevel(CommandSender sender, String playerName, String skillName, String levelStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }

        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + skillName);
            return;
        }

        int level;
        try {
            level = Integer.parseInt(levelStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level: " + levelStr);
            return;
        }

        int maxLevel = plugin.getConfig().getInt("settings.max-level", 100);
        if (level < 0 || level > maxLevel) {
            sender.sendMessage(ChatColor.RED + "Level must be between 0 and " + maxLevel);
            return;
        }

        UUID targetUuid = target.getUniqueId();
        plugin.getDataManager().setLevel(targetUuid, skill.getName(), level);

        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s " + skill.getDisplayName() + " level to " + level + "!");
        target.sendMessage(ChatColor.YELLOW + "Your " + skill.getDisplayName() + " level has been set to " + level + " by an admin.");
    }

    

    private void addPlayerSkillExp(CommandSender sender, String playerName, String skillName, String amountStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }

        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + skillName);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + amountStr);
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be positive!");
            return;
        }

        UUID targetUuid = target.getUniqueId();
        boolean leveledUp = plugin.getDataManager().addExperience(targetUuid, skill.getName(), amount);

        sender.sendMessage(ChatColor.GREEN + "Added " + amount + " experience to " + target.getName() + "'s " + skill.getDisplayName() + " skill!");

        if (leveledUp) {
            int newLevel = plugin.getDataManager().getPlayerData(targetUuid).getSkillLevel(skill.getName());
            sender.sendMessage(ChatColor.GREEN + target.getName() + " leveled up to " + skill.getDisplayName() + " level " + newLevel + "!");
        }
    }

    

    private void removePlayerSkillExp(CommandSender sender, String playerName, String skillName, String amountStr) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }

        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            sender.sendMessage(ChatColor.RED + "Unknown skill: " + skillName);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount: " + amountStr);
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be positive!");
            return;
        }

        UUID targetUuid = target.getUniqueId();
        double currentExp = plugin.getDataManager().getPlayerData(targetUuid).getSkillExp(skill.getName());

       

        amount = Math.min(amount, currentExp);

        plugin.getDataManager().addExperience(targetUuid, skill.getName(), -amount);

        sender.sendMessage(ChatColor.GREEN + "Removed " + amount + " experience from " + target.getName() + "'s " + skill.getDisplayName() + " skill!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
           

            completions.addAll(Arrays.asList("reload", "reset", "setlevel", "addexp", "removeexp"));

           

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
           

            if (args[0].equalsIgnoreCase("reset") ||
                    args[0].equalsIgnoreCase("setlevel") ||
                    args[0].equalsIgnoreCase("addexp") ||
                    args[0].equalsIgnoreCase("removeexp")) {

                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
           

            if (args[0].equalsIgnoreCase("reset") ||
                    args[0].equalsIgnoreCase("setlevel") ||
                    args[0].equalsIgnoreCase("addexp") ||
                    args[0].equalsIgnoreCase("removeexp")) {

                plugin.getSkillManager().getAllSkills().forEach(skill ->
                        completions.add(skill.getName().toLowerCase()));

                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 4) {
           

            if (args[0].equalsIgnoreCase("setlevel")) {
                completions.addAll(Arrays.asList("1", "10", "25", "50", "75", "100"));

                return completions.stream()
                        .filter(s -> s.startsWith(args[3]))
                        .collect(Collectors.toList());
            }

           

            if (args[0].equalsIgnoreCase("addexp") || args[0].equalsIgnoreCase("removeexp")) {
                completions.addAll(Arrays.asList("10", "100", "1000", "10000"));

                return completions.stream()
                        .filter(s -> s.startsWith(args[3]))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}