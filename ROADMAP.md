# Roadmap

Planned work, roughly in priority order.

## Up next
- **Fix the "daily" earnings cap** — `dailyEarningsCap` currently caps each habit completion, not the total a goal can earn in a day. With several habits feeding one goal, a goal can earn more than its cap per day. Decide the intended rule and track per-day totals if needed.
- **Reset streaks on missed days** — a streak currently only resets the next time the habit is completed, so a habit last done days ago still shows its old streak. Reset (or display as broken) once a day is missed, and decide how `WEEKLY` / `CUSTOM` frequencies count as "missed".
- **Complete goals that reach their target** — goals past their target stay `ACTIVE` (e.g. $162 of $150). Mark them `COMPLETED`, decide whether they keep earning, and decide how completed goals appear on Home and the Goals screen.
- **Reorder goals** — let the Goals screen reorder goals (e.g. drag handles) to control the order they appear in the Home carousel after the pinned goal.

## Done
- **Multiple savings goals** — habits link to one goal each (unassigned habits feed the pinned goal); Home shows a swipeable carousel of goals with the pinned goal first, and the habit list follows the visible goal.

## Later
- **Many-to-many habit ↔ goal links** — let one habit contribute to several goals via a join table. The earnings path (`recordEarningsForHabitCompletion`) is already the single place that resolves which goal(s) a habit pays into.
