package com.orbis.skills.util;

import com.orbis.skills.OrbisSkills;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ExperienceUtil {

    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    

    public static double getExpToNextLevel(int currentLevel) {
       

        return 100 * (1 + (currentLevel * 0.1));
    }

    

    public static double calculateExp(String formula, double baseExp, int level, double multiplier) {
        try {
           

            String processedFormula = formula
                    .replace("base", String.valueOf(baseExp))
                    .replace("level", String.valueOf(level))
                    .replace("multiplier", String.valueOf(multiplier));

           

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

    

    public static double getTotalExpToLevel(int level) {
        double total = 0;
        for (int i = 0; i < level; i++) {
            total += getExpToNextLevel(i);
        }
        return total;
    }

    

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