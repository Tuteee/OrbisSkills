package com.orbis.skills.util;

import com.orbis.skills.OrbisSkills;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ExperienceUtil {

    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    /**
     * Get the experience required to reach the next level
     * @param currentLevel the current level
     * @return the experience required
     */
    public static double getExpToNextLevel(int currentLevel) {
        // Default formula: 100 * (1 + (currentLevel * 0.1))
        return 100 * (1 + (currentLevel * 0.1));
    }

    /**
     * Calculate experience using a custom formula
     * @param formula the formula string
     * @param baseExp the base experience
     * @param level the player's current level
     * @param multiplier the experience multiplier
     * @return the calculated experience
     */
    public static double calculateExp(String formula, double baseExp, int level, double multiplier) {
        try {
            // Replace variables in formula
            String processedFormula = formula
                    .replace("base", String.valueOf(baseExp))
                    .replace("level", String.valueOf(level))
                    .replace("multiplier", String.valueOf(multiplier));

            // Evaluate formula
            Object result = engine.eval(processedFormula);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
            return baseExp * multiplier;
        } catch (ScriptException e) {
            OrbisSkills.getInstance().getLogger().warning("Error evaluating exp formula: " + formula);
            OrbisSkills.getInstance().getLogger().warning(e.getMessage());
            return baseExp * multiplier;
        }
    }

    /**
     * Get the total experience required to reach a level
     * @param level the target level
     * @return the total experience required
     */
    public static double getTotalExpToLevel(int level) {
        double total = 0;
        for (int i = 0; i < level; i++) {
            total += getExpToNextLevel(i);
        }
        return total;
    }

    /**
     * Get the level for a given amount of total experience
     * @param totalExp the total experience
     * @return the level
     */
    public static int getLevelForExp(double totalExp) {
        int level = 0;
        double expNeeded = 0;

        while (true) {
            expNeeded += getExpToNextLevel(level);
            if (expNeeded > totalExp) {
                return level;
            }
            level++;
        }
    }

    /**
     * Format experience value for display
     * @param exp the experience value
     * @return the formatted string
     */
    public static String formatExp(double exp) {
        if (exp >= 1000000) {
            return String.format("%.2fM", exp / 1000000);
        } else if (exp >= 1000) {
            return String.format("%.2fK", exp / 1000);
        } else {
            return String.format("%.1f", exp);
        }
    }
}