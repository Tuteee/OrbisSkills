package com.orbis.skills;

import com.orbis.skills.data.PlayerData;
import com.orbis.skills.skills.Skill;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OrbisSkillsPlaceholderExpansion extends PlaceholderExpansion {

    private final OrbisSkills plugin;

    public OrbisSkillsPlaceholderExpansion(OrbisSkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "orbisskills";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the expansion on reload
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        PlayerData playerData = plugin.getDataManager().getPlayerData(player.getUniqueId());
        if (playerData == null) {
            return "0";
        }

        // Handle general placeholders
        if (identifier.equals("total_level")) {
            return String.valueOf(playerData.getTotalLevel());
        }

        // Handle skill-specific placeholders
        String[] parts = identifier.split("_");

        if (parts.length == 2) {
            String skillName = parts[0].toLowerCase();
            String attribute = parts[1].toLowerCase();

            Skill skill = plugin.getSkillManager().getSkill(skillName);
            if (skill == null) {
                return "0";
            }

            switch (attribute) {
                case "level":
                    return String.valueOf(playerData.getSkillLevel(skillName));
                case "exp":
                    return String.valueOf(playerData.getSkillExp(skillName));
                case "exptolevel":
                    return String.valueOf(playerData.getExpToNextLevel(skillName));
                case "progress":
                    return String.format("%.1f", playerData.getLevelProgress(skillName) * 100) + "%";
                case "rank":
                    return playerData.getSkillRank(skillName);
                default:
                    return "0";
            }
        }

        // Handle ability placeholders
        if (parts.length == 3 && parts[1].equals("ability")) {
            String skillName = parts[0].toLowerCase();
            String abilityName = parts[2].toLowerCase();

            // Check if skill exists
            Skill skill = plugin.getSkillManager().getSkill(skillName);
            if (skill == null) {
                return "0";
            }

            // Return ability info
            if (skill.hasAbility(abilityName)) {
                return skill.getAbilityInfo(player, abilityName);
            }

            return "0";
        }

        return null; // Unknown placeholder
    }
}