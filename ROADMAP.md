# Roadmap

Planned work, roughly in priority order.

## In progress
- **Multiple savings goals** — habits link to one goal each (unassigned habits feed the pinned goal); Home shows a swipeable carousel of goals with the pinned goal first, and the habit list follows the visible goal.

## Up next
- **Fix the "daily" earnings cap** — `dailyEarningsCap` currently caps each habit completion, not the total a goal can earn in a day. With several habits feeding one goal, a goal can earn more than its cap per day. Decide the intended rule and track per-day totals if needed.
- **Reorder goals** — let the Goals screen reorder goals (e.g. drag handles) to control the order they appear in the Home carousel after the pinned goal.

## Later
- **Many-to-many habit ↔ goal links** — let one habit contribute to several goals via a join table. The earnings path (`recordEarningsForHabitCompletion`) is already the single place that resolves which goal(s) a habit pays into.
