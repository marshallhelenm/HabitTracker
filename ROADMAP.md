# Roadmap

Planned work, roughly in priority order.

## Up next
- **Reset streaks on missed days** — a streak currently only resets the next time the habit is completed, so a habit last done days ago still shows its old streak. Reset (or display as broken) once a day is missed, and decide how `WEEKLY` / `CUSTOM` frequencies count as "missed".
- **Reorder goals** — let the Goals screen reorder goals (e.g. drag handles) to control the order they appear in the Home carousel after the pinned goal.

## Later
- **Many-to-many habit ↔ goal links** — let one habit contribute to several goals via a join table. The earnings path (`recordEarningsForHabitCompletion`) is already the single place that resolves which goal(s) a habit pays into.
- **Daily reminder notification** — a once-a-day notification prompting the user to mark today's habits complete. Likely needs a user-chosen reminder time, the `POST_NOTIFICATIONS` runtime permission (Android 13+), and a scheduler such as WorkManager (new dependency). Consider skipping the reminder when every tracked habit is already done.
- **Home screen widgets** — e.g. a pinned-goal piggy bank progress widget and a checklist for marking today's habits complete. Likely built with Jetpack Glance (new dependency); widgets read the Room database outside the app, so completing a habit from a widget must go through the same earnings path as the app.

## Done
- **Multiple savings goals** — habits link to one goal each (unassigned habits feed the pinned goal); Home shows a swipeable carousel of goals with the pinned goal first, and the habit list follows the visible goal.
- **Daily earnings cap per goal** — a goal's cap limits what it receives per day across all habits; completions past the cap still count for streaks but earn nothing. Deposits are logged in a `deposits` table.
- **Goal completion** — goals complete automatically at their target, extra earnings overflow to the pinned goal, completed goals sit at the end of the Home carousel, and changing a target reopens or completes a goal.
- **Goals screen sorting** — sort by pinned/A–Z/progress (tap again to reverse) and hide completed goals.
