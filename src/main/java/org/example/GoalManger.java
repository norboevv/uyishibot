package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalManger {
    private final Map<Long, List<Goal>> userGoals = new HashMap<>();

    public void addGoal(long chatId, String goalName) {
        Goal goal = new Goal(goalName);
        userGoals.computeIfAbsent(chatId, k -> new ArrayList<>()).add(goal);
    }


    public List<Goal> getGoals(long chatId) {
        return userGoals.get(chatId);
    }


    public void deleteGoal(long chatId, int goalIndex) {
        List<Goal> goals = userGoals.get(chatId);
        if (goals != null && goalIndex >= 0 && goalIndex < goals.size()) {
            goals.remove(goalIndex);
        }
    }


    public void markGoalAsCompleted(long chatId, int goalIndex) {
        List<Goal> goals = userGoals.get(chatId);
        if (goals != null && goalIndex >= 0 && goalIndex < goals.size()) {
            goals.get(goalIndex).setCompleted(true);
        }
    }
}


