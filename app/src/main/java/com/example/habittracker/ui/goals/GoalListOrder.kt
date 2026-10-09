package com.example.habittracker.ui.goals

import com.example.habittracker.model.SavingsGoal
import com.example.habittracker.model.SavingsGoalStatus
import com.example.habittracker.viewmodel.userOrder

enum class GoalSort(val label: String) {
    // The Home carousel's order: pinned goal, then the order the user dragged goals into.
    MY_ORDER("My order"),
    NAME("A–Z"),
    NAME_DESCENDING("Z–A"),
    // Direction is shown with an icon on the chip rather than in the text.
    PROGRESS("Progress"),
    PROGRESS_ASCENDING("Progress");

    // Sorts that share a chip with this one; tapping the selected chip again switches to the other.
    val reversed: GoalSort?
        get() = when (this) {
            NAME -> NAME_DESCENDING
            NAME_DESCENDING -> NAME
            PROGRESS -> PROGRESS_ASCENDING
            PROGRESS_ASCENDING -> PROGRESS
            MY_ORDER -> null
        }
}

// Goals still earning always come first, then completed, then archived; the chosen sort orders goals
// within each group. With hideFinished, completed and archived goals are left out.
fun goalsForList(goals: List<SavingsGoal>, sort: GoalSort, hideFinished: Boolean): List<SavingsGoal> {
    val withinGroup = when (sort) {
        GoalSort.MY_ORDER -> compareByDescending<SavingsGoal> { it.pinned }.then(userOrder)
        GoalSort.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        GoalSort.NAME_DESCENDING -> compareBy(String.CASE_INSENSITIVE_ORDER.reversed()) { it.name }
        GoalSort.PROGRESS -> compareByDescending { it.progress() }
        GoalSort.PROGRESS_ASCENDING -> compareBy { it.progress() }
    }
    return goals
        .filter { !hideFinished || !it.isFinished() }
        .sortedWith(compareBy<SavingsGoal> { it.listGroup() }.then(withinGroup).thenBy { it.id })
}

private fun SavingsGoal.progress() = currentAmount.toDouble() / targetAmount.toDouble()

private fun SavingsGoal.isFinished() =
    status == SavingsGoalStatus.COMPLETED || status == SavingsGoalStatus.ARCHIVED

private fun SavingsGoal.listGroup() = when (status) {
    SavingsGoalStatus.ACTIVE, SavingsGoalStatus.PAUSED -> 0
    SavingsGoalStatus.COMPLETED -> 1
    SavingsGoalStatus.ARCHIVED -> 2
}
