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

    /**
     * Create a new skills command
     * @param plugin the plugin instance
     */
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

        // Check permission
        if (!player.hasPermission("orbisskills.user")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Handle subcommands
        if (args.length == 0) {
            // Show main skills menu
            showSkillsMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                // Show help
                showHelp(player);
                break;
            case "info":
                // Show skill info
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /skills info <skill>");
                    return true;
                }
                showSkillInfo(player, args[1]);
                break;
            case "stats":
                // Show stats
                showStats(player);
                break;
            case "reset":
                // Reset skill
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /skills reset <skill>");
                    return true;
                }

                // Check permission
                if (!player.hasPermission("orbisskills.reset")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to reset skills!");
                    return true;
                }

                resetSkill(player, args[1]);
                break;
            default:
                // Check if argument is a skill name
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

    /**
     * Show skills menu
     * @param player the player
     */
    private void showSkillsMenu(Player player) {
        // In a real implementation, this would open a GUI
        // For now, just list available skills
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

    /**
     * Show help
     * @param player the player
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "==== OrbisSkills Help ====");
        player.sendMessage(ChatColor.YELLOW + "/skills " + ChatColor.WHITE + "- Show skills menu");
        player.sendMessage(ChatColor.YELLOW + "/skills help " + ChatColor.WHITE + "- Show this help message");
        player.sendMessage(ChatColor.YELLOW + "/skills <skill> " + ChatColor.WHITE + "- Show info for a specific skill");
        player.sendMessage(ChatColor.YELLOW + "/skills info <skill> " + ChatColor.WHITE + "- Show detailed info for a specific skill");
        player.sendMessage(ChatColor.YELLOW + "/skills stats " + ChatColor.WHITE + "- Show your stats for all skills");
        player.sendMessage(ChatColor.YELLOW + "/skills reset <skill> " + ChatColor.WHITE + "- Reset a skill (if allowed)");
    }

    /**
     * Show skill info
     * @param player the player
     * @param skillName the skill name
     */
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

        // Show abilities
        player.sendMessage(ChatColor.GOLD + "Abilities:");
        // This would list abilities in a real implementation

        player.sendMessage(ChatColor.GRAY + "Use /skills stats for more info");
    }

    /**
     * Show stats
     * @param player the player
     */
    private void showStats(Player player) {
        PlayerData playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            player.sendMessage(ChatColor.RED + "Error loading your skill data!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "==== OrbisSkills Stats ====");
        player.sendMessage(ChatColor.YELLOW + "Total Level: " + ChatColor.WHITE + playerData.getTotalLevel());

        // Show detailed stats for each skill
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

    /**
     * Reset skill
     * @param player the player
     * @param skillName the skill name
     */
    private void resetSkill(Player player, String skillName) {
        Skill skill = plugin.getSkillManager().getSkill(skillName);
        if (skill == null) {
            player.sendMessage(ChatColor.RED + "Unknown skill: " + skillName);
            return;
        }

        // Ask for confirmation
        player.sendMessage(ChatColor.RED + "Are you sure you want to reset your " +
                skill.getDisplayName() + ChatColor.RED + " skill? Type '/skills confirm' to confirm.");

        // Store confirmation request (this would be implemented elsewhere)
        // In a real implementation, this would use a Map to store pending confirmations
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Add subcommands
            completions.addAll(Arrays.asList("help", "info", "stats", "reset"));

            // Add skill names
            plugin.getSkillManager().getAllSkills().forEach(skill ->
                    completions.add(skill.getName().toLowerCase()));

            // Filter by current input
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // If first argument is "info" or "reset", suggest skill names
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