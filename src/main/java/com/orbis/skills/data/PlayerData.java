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

    

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    

    public PlayerData(UUID uuid, Map<String, Object> map) {
        this.uuid = uuid;

       

        Object levelsObj = map.get("levels");
        if (levelsObj instanceof Map) {
            Map<?, ?> levelsMap = (Map<?, ?>) levelsObj;
            for (Map.Entry<?, ?> entry : levelsMap.entrySet()) {
                if (entry.getKey() instanceof String && entry.getValue() instanceof Integer) {
                    skillLevels.put((String) entry.getKey(), (Integer) entry.getValue());
                }
            }
        }

       

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

    

    public UUID getUuid() {
        return uuid;
    }

    

    public int getSkillLevel(String skillName) {
        return skillLevels.getOrDefault(skillName.toLowerCase(), 0);
    }

    

    public double getSkillExp(String skillName) {
        return skillExperience.getOrDefault(skillName.toLowerCase(), 0.0);
    }

    

    public double getExpToNextLevel(String skillName) {
        int currentLevel = getSkillLevel(skillName);
        double currentExp = getSkillExp(skillName);

        return ExperienceUtil.getExpToNextLevel(currentLevel) - currentExp;
    }

    

    public double getLevelProgress(String skillName) {
        int currentLevel = getSkillLevel(skillName);
        double currentExp = getSkillExp(skillName);
        double expNeeded = ExperienceUtil.getExpToNextLevel(currentLevel);

        if (expNeeded <= 0) {
            return 1.0;
        }

        return Math.min(1.0, currentExp / expNeeded);
    }

    

    public String getSkillRank(String skillName) {
        int level = getSkillLevel(skillName);

        if (level >= 90) return "Master";
        if (level >= 75) return "Expert";
        if (level >= 50) return "Adept";
        if (level >= 25) return "Skilled";
        if (level >= 10) return "Apprentice";
        return "Novice";
    }

    

    public void setSkillLevel(String skillName, int level) {
        skillLevels.put(skillName.toLowerCase(), Math.max(0, level));
        setDirty(true);
    }

    

    public void setSkillExp(String skillName, double experience) {
        skillExperience.put(skillName.toLowerCase(), Math.max(0, experience));
        setDirty(true);
    }

    

    public boolean addSkillExp(String skillName, double amount) {
        String key = skillName.toLowerCase();
        int oldLevel = getSkillLevel(key);
        double newExp = getSkillExp(key) + amount;

       

        int newLevel = oldLevel;
        while (newExp >= ExperienceUtil.getExpToNextLevel(newLevel)) {
            newExp -= ExperienceUtil.getExpToNextLevel(newLevel);
            newLevel++;
        }

       

        skillExperience.put(key, newExp);
        skillLevels.put(key, newLevel);
        setDirty(true);

        return newLevel > oldLevel;
    }

    

    public void resetSkill(String skillName) {
        String key = skillName.toLowerCase();
        skillLevels.put(key, 0);
        skillExperience.put(key, 0.0);
        setDirty(true);
    }

    

    public void resetAllSkills() {
        for (String key : skillLevels.keySet()) {
            skillLevels.put(key, 0);
            skillExperience.put(key, 0.0);
        }
        setDirty(true);
    }

    

    public int getTotalLevel() {
        int total = 0;
        for (int level : skillLevels.values()) {
            total += level;
        }
        return total;
    }

    

    public boolean isDirty() {
        return dirty;
    }

    

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    

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