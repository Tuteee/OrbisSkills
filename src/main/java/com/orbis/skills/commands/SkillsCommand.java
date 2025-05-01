package com.orbis.skills.commands;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.data.PlayerData;
import com.orbis.skills.skills.Skill;
import com.orbis.skills.util.ExperienceUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SkillsCommand implements CommandExecutor, TabCompleter {

    private final OrbisSkills plugin;

    

    public SkillsCommand(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

       

        if (!player.hasPermission("orbisskills.user")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

       

        if (args.length == 0) {
           

            showSkillsMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
               

                showHelp(player);
                break;
            case "info":
               

                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /skills info <skill>");
                    return true;
                }
                showSkillInfo(player, args[1]);
                break;
            case "stats":
               

                showStats(player);
                break;
            case "reset":
               

                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /skills reset <skill>");
                    return true;
                }

               

                if (!player.hasPermission("orbisskills.reset")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to reset skills!");
                    return true;
                }

                resetSkill(player, args[1]);
                break;
            default:
               

                Skill skill = plugin.getSkillManager().getSkill(subCommand);
                if (skill != null) {
                    showSkillInfo(player, subCommand);
                } else {
                    player.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                    showHelp(player);
                }
                break;
        }

        return true;
    }

    

    private void showSkillsMenu(Player player) {
       

       

        player.sendMessage(ChatColor.GOLD + "==== OrbisSkills ====");

        PlayerData playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "Error loading your skill data!");
            return;
        }

        for (Skill skill : plugin.getSkillManager().getAllSkills()) {
            int level = playerData.getSkillLevel(skill.getName());
            double exp = playerData.getSkillExp(skill.getName());
            double expToNext = ExperienceUtil.getExpToNextLevel(level);
            double progress = Math.min(exp / expToNext, 1.0) * 100;

            player.sendMessage(String.format(
                    ChatColor.YELLOW + "%s: " + ChatColor.WHITE + "Level %d (%.1f%%)",
                    skill.getDisplayName(), level, progress
            ));
        }

        player.sendMessage(ChatColor.GOLD + "Total Level: " + ChatColor.WHITE + playerData.getTotalLevel());
        player.sendMessage(ChatColor.GRAY + "Use /skills help for commands");
    }

    

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "==== OrbisSkills Help ====");
        player.sendMessage(ChatColor.YELLOW + "/skills " + ChatColor.WHITE + "- Show skills menu");
        player.sendMessage(ChatColor.YELLOW + "/skills help " + ChatColor.WHITE + "- Show this help message");
        player.sendMessage(ChatColor.YELLOW + "/skills <skill> " + ChatColor.WHITE + "- Show info for a specific skill");
        player.sendMessage(ChatColor.YELLOW + "/skills info <skill> " + ChatColor.WHITE + "- Show detailed info for a specific skill");
        player.sendMessage(ChatColor.YELLOW + "/skills stats " + ChatColor.WHITE + "- Show your stats for all skills");
        player.sendMessage(ChatColor.YELLOW + "/skills reset <skill> " + ChatColor.WHITE + "- Reset a skill (if allowed)");
    }

    

    private void showSkillInfo(Player player, String skillName) {
        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            player.sendMessage(ChatColor.RED + "Unknown skill: " + skillName);
            return;
        }

        PlayerData playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "Error loading your skill data!");
            return;
        }

        int level = playerData.getSkillLevel(skill.getName());
        double exp = playerData.getSkillExp(skill.getName());
        double expToNext = ExperienceUtil.getExpToNextLevel(level);
        double progress = Math.min(exp / expToNext, 1.0) * 100;

        player.sendMessage(ChatColor.GOLD + "==== " + skill.getDisplayName() + " ====");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + level);
        player.sendMessage(ChatColor.YELLOW + "Experience: " + ChatColor.WHITE +
                String.format("%.1f / %.1f (%.1f%%)", exp, expToNext, progress));
        player.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + playerData.getSkillRank(skill.getName()));

       

        player.sendMessage(ChatColor.GOLD + "Abilities:");
       


        player.sendMessage(ChatColor.GRAY + "Use /skills stats for more info");
    }

    

    private void showStats(Player player) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "Error loading your skill data!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "==== OrbisSkills Stats ====");
        player.sendMessage(ChatColor.YELLOW + "Total Level: " + ChatColor.WHITE + playerData.getTotalLevel());

       

        for (Skill skill : plugin.getSkillManager().getAllSkills()) {
            int level = playerData.getSkillLevel(skill.getName());
            double exp = playerData.getSkillExp(skill.getName());
            double expToNext = ExperienceUtil.getExpToNextLevel(level);
            String rank = playerData.getSkillRank(skill.getName());

            player.sendMessage(String.format(
                    ChatColor.YELLOW + "%s: " + ChatColor.WHITE + "Level %d - %s - %.1f/%.1f EXP",
                    skill.getDisplayName(), level, rank, exp, expToNext
            ));
        }
    }

    

    private void resetSkill(Player player, String skillName) {
        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            player.sendMessage(ChatColor.RED + "Unknown skill: " + skillName);
            return;
        }

       

        player.sendMessage(ChatColor.RED + "Are you sure you want to reset your " +
                skill.getDisplayName() + ChatColor.RED + " skill? Type '/skills confirm' to confirm.");

       

       

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
           

            completions.addAll(Arrays.asList("help", "info", "stats", "reset"));

           

            plugin.getSkillManager().getAllSkills().forEach(skill ->
                    completions.add(skill.getName().toLowerCase()));

           

            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
           

            if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("reset")) {
                plugin.getSkillManager().getAllSkills().forEach(skill ->
                        completions.add(skill.getName().toLowerCase()));

                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return completions;
    }
}