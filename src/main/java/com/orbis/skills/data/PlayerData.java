package com.orbis.skills.data;

import com.orbis.skills.OrbisSkills;
import com.orbis.skills.skills.Skill;
import com.orbis.skills.util.ExperienceUtil;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData implements ConfigurationSerializable {

    private final UUID uuid;
    private final Map<String, Integer> skillLevels = new HashMap<>();
    private final Map<String, Double> skillExperience = new HashMap<>();
    private boolean dirty = false;

    /**
     * Create new player data
     * @param uuid the player UUID
     */
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Create player data from a serialized map
     * @param uuid the player UUID
     * @param map the serialized map
     */
    public PlayerData(UUID uuid, Map<String, Object> map) {
        this.uuid = uuid;

        // Load skill levels
        Object levelsObj = map.get("levels");
        if (levelsObj instanceof Map) {
            Map<?, ?> levelsMap = (Map<?, ?>) levelsObj;
            for (Map.Entry<?, ?> entry : levelsMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Integer) {
                    skillLevels.put((String) entry.getKey(), (Integer) entry.getValue());
                }
            }
        }

        // Load skill experience
        Object expObj = map.get("experience");
        if (expObj instanceof Map) {
            Map<?, ?> expMap = (Map<?, ?>) expObj;
            for (Map.Entry<?, ?> entry : expMap.entrySet()) {
                if (entry.getKey() instanceof String &&
                        (entry.getValue() instanceof Double || entry.getValue() instanceof Integer)) {
                    if (entry.getValue() instanceof Integer) {
                        skillExperience.put((String) entry.getKey(), ((Integer) entry.getValue()).doubleValue());
                    } else {
                        skillExperience.put((String) entry.getKey(), (Double) entry.getValue());
                    }
                }
            }
        }
    }

    /**
     * Get the player UUID
     * @return the UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get skill level
     * @param skillName the skill name
     * @return the skill level
     */
    public int getSkillLevel(String skillName) {
        return skillLevels.getOrDefault(skillName.toLowerCase(), 0);
    }

    /**
     * Get skill experience
     * @param skillName the skill name
     * @return the skill experience
     */
    public double getSkillExp(String skillName) {
        return skillExperience.getOrDefault(skillName.toLowerCase(), 0.0);
    }

    /**
     * Get experience needed for next level
     * @param skillName the skill name
     * @return the experience needed
     */
    public double getExpToNextLevel(String skillName) {
        int currentLevel = getSkillLevel(skillName);
        double currentExp = getSkillExp(skillName);

        return ExperienceUtil.getExpToNextLevel(currentLevel) - currentExp;
    }

    /**
     * Get level progress as a decimal (0.0 - 1.0)
     * @param skillName the skill name
     * @return the level progress
     */
    public double getLevelProgress(String skillName) {
        int currentLevel = getSkillLevel(skillName);
        double currentExp = getSkillExp(skillName);
        double expNeeded = ExperienceUtil.getExpToNextLevel(currentLevel);

        if (expNeeded <= 0) {
            return 1.0;
        }

        return Math.min(1.0, currentExp / expNeeded);
    }

    /**
     * Get skill rank (based on level)
     * @param skillName the skill name
     * @return the skill rank
     */
    public String getSkillRank(String skillName) {
        int level = getSkillLevel(skillName);

        if (level >= 90) return "Master";
        if (level >= 75) return "Expert";
        if (level >= 50) return "Adept";
        if (level >= 25) return "Skilled";
        if (level >= 10) return "Apprentice";
        return "Novice";
    }

    /**
     * Set skill level
     * @param skillName the skill name
     * @param level the level
     */
    public void setSkillLevel(String skillName, int level) {
        skillLevels.put(skillName.toLowerCase(), Math.max(0, level));
        setDirty(true);
    }

    /**
     * Set skill experience
     * @param skillName the skill name
     * @param experience the experience
     */
    public void setSkillExp(String skillName, double experience) {
        skillExperience.put(skillName.toLowerCase(), Math.max(0, experience));
        setDirty(true);
    }

    /**
     * Add skill experience
     * @param skillName the skill name
     * @param amount the amount to add
     * @return true if leveled up
     */
    public boolean addSkillExp(String skillName, double amount) {
        String key = skillName.toLowerCase();
        int oldLevel = getSkillLevel(key);
        double newExp = getSkillExp(key) + amount;

        // Apply experience and check for level ups
        int newLevel = oldLevel;
        while (newExp >= ExperienceUtil.getExpToNextLevel(newLevel)) {
            newExp -= ExperienceUtil.getExpToNextLevel(newLevel);
            newLevel++;
        }

        // Update data
        skillExperience.put(key, newExp);
        skillLevels.put(key, newLevel);
        setDirty(true);

        return newLevel > oldLevel;
    }

    /**
     * Reset skill
     * @param skillName the skill name
     */
    public void resetSkill(String skillName) {
        String key = skillName.toLowerCase();
        skillLevels.put(key, 0);
        skillExperience.put(key, 0.0);
        setDirty(true);
    }

    /**
     * Reset all skills
     */
    public void resetAllSkills() {
        for (String key : skillLevels.keySet()) {
            skillLevels.put(key, 0);
            skillExperience.put(key, 0.0);
        }
        setDirty(true);
    }

    /**
     * Get total level (sum of all skill levels)
     * @return the total level
     */
    public int getTotalLevel() {
        int total = 0;
        for (int level : skillLevels.values()) {
            total += level;
        }
        return total;
    }

    /**
     * Check if data is dirty (needs saving)
     * @return true if dirty
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Set dirty flag
     * @param dirty the dirty flag
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Initialize a skill if it doesn't exist
     * @param skillName the skill name
     */
    public void initializeSkill(String skillName) {
        String key = skillName.toLowerCase();
        if (!skillLevels.containsKey(key)) {
            skillLevels.put(key, 0);
            skillExperience.put(key, 0.0);
            setDirty(true);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("levels", new HashMap<>(skillLevels));
        result.put("experience", new HashMap<>(skillExperience));
        return result;
    }
}