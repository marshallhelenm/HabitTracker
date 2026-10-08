package com.example.habittracker.viewmodel

import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus
import java.math.BigDecimal

data class PlannedDeposit(
    val goalId: Long,
    val amount: BigDecimal,
    val overflowFromGoalId: Long?
)

data class EarningsPlan(
    val deposits: List<PlannedDeposit>,
    // Goals this plan fills to their target, in the order they filled.
    val completedGoalIds: List<Long>,
    // Goals whose daily cap stopped part (or all) of a deposit.
    val cappedGoalIds: List<Long>
)

// Splits one habit completion's earnings across goals:
//  - a goal never receives more per day than its daily cap (leftover beyond a cap is not earned);
//  - a goal fills exactly to its target, and anything beyond that overflows to the pinned goal
//    (or, if the goal that just filled was pinned, to the goal that will be pinned next);
//  - overflow is subject to the receiving goal's own cap and target, so it can cascade.
fun planEarnings(
    amount: BigDecimal,
    target: SavingsGoal?,
    goals: List<SavingsGoal>,
    earnedToday: Map<Long, BigDecimal>
): EarningsPlan {
    val deposits = mutableListOf<PlannedDeposit>()
    val completed = mutableListOf<Long>()
    val capped = mutableListOf<Long>()
    var remaining = amount
    var current = target?.takeIf { it.status == SavingsGoalStatus.ACTIVE }
    var overflowFrom: Long? = null

    while (current != null && remaining > BigDecimal.ZERO) {
        val goal = current
        val capRoom = goal.dailyEarningsCap
            ?.let { (it - (earnedToday[goal.id] ?: BigDecimal.ZERO)).max(BigDecimal.ZERO) }
        val targetRoom = (goal.targetAmount - goal.currentAmount).max(BigDecimal.ZERO)

        if (capRoom == null || targetRoom <= capRoom) {
            if (targetRoom <= remaining) {
                if (targetRoom > BigDecimal.ZERO) {
                    deposits += PlannedDeposit(goal.id, targetRoom, overflowFrom)
                }
                completed += goal.id
                remaining -= targetRoom
                overflowFrom = goal.id
                current = nextPinnedGoal(goals, excluding = completed)
                continue
            }
        } else if (capRoom < remaining) {
            if (capRoom > BigDecimal.ZERO) {
                deposits += PlannedDeposit(goal.id, capRoom, overflowFrom)
            }
            capped += goal.id
            break
        }

        deposits += PlannedDeposit(goal.id, remaining, overflowFrom)
        break
    }

    return EarningsPlan(deposits, completed, capped)
}

// The goal that is (or will become) pinned once `excluding` goals are no longer active:
// the current pinned goal if it's still eligible, otherwise the oldest remaining active goal.
fun nextPinnedGoal(goals: List<SavingsGoal>, excluding: Collection<Long> = emptyList()): SavingsGoal? {
    val candidates = goals.filter { it.status == SavingsGoalStatus.ACTIVE && it.id !in excluding }
    return candidates.firstOrNull { it.pinned }
        ?: candidates.minWithOrNull(compareBy<SavingsGoal> { it.createdAt }.thenBy { it.id })
}
