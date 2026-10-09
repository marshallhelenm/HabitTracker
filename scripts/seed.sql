-- Test seed data for Habit Piggy (Room schema version 2).
-- Dates are relative to today so "done today" / "streak alive" states stay valid whenever this runs.
-- Covers: several goals at different fill levels (0%, ~1%, partial, ~full, over 100%), a long goal name,
-- an archived goal (hidden from Home), assigned / unassigned / untracked habits, completed-today habits,
-- streaks that are alive vs broken, custom-interval habits (every 7 and 3 days), reward rates from $0.25 to $3, a goal order that differs from creation order, and a habit with a description.
-- Money-rule scenarios (completing a habit today):
--   * "Bike to work" ($4) fills New Bike ($297 of $300) and overflows $1 to the pinned Danger Noodle.
--   * Danger Noodle has a $5 daily cap with $3 already deposited today, so "Read 20 pages" ($1.25 × 13 = $16.25) hits the cap.
--   * Concert Tickets is saved past its target and becomes Completed on load.

DELETE FROM deposits;
DELETE FROM habits;
DELETE FROM savings_goals;
DELETE FROM sqlite_sequence WHERE name IN ('habits', 'savings_goals', 'deposits');

-- id, name, targetAmount, currentAmount, dueDate, dailyEarningsCap, status, createdAt, pinned, sortOrder
-- The emergency fund is newest but was moved up to sit right after the pinned goal.
INSERT INTO savings_goals (id, name, targetAmount, currentAmount, dueDate, dailyEarningsCap, status, createdAt, pinned, sortOrder) VALUES
  (1, 'Danger Noodle', '2000', '412.50', NULL, '5', 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-128 days'), 1, 0),
  (2, 'New Bike', '300', '297', NULL, NULL, 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-84 days'), 0, 2),
  (3, 'Concert Tickets', '150', '162', NULL, '3', 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-60 days'), 0, 3),
  (4, 'Weekend Trip', '900', '0', NULL, NULL, 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-17 days'), 0, 4),
  (5, 'Emergency fund for vet visits and other surprises', '1000', '9', NULL, '2', 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-5 days'), 0, 1),
  (6, 'Old Laptop Fund', '800', '800', NULL, NULL, 'ARCHIVED', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-300 days'), 0, 5);

-- id, title, description, intervalDays, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId, rewardRate
INSERT INTO habits (id, title, description, intervalDays, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId, rewardRate) VALUES
  -- Pinned goal's page: assigned + unassigned habits
  (1, 'Read 20 pages', '', 1, 12, 15, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-120 days'), date('now', 'localtime', '-1 day'), 1, 1, '1.25'),
  (2, 'Morning stretch', 'Five minutes before coffee', 1, 6, 21, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-110 days'), date('now', 'localtime'), 1, NULL, '1'),
  (3, 'Drink water', '', 1, 0, 0, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-2 days'), NULL, 1, NULL, '0.25'),
  -- Other goals
  (4, 'Bike to work', '', 1, 3, 9, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-80 days'), date('now', 'localtime', '-1 day'), 1, 2, '1'),
  (5, 'Vitamins', '', 1, 30, 30, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-75 days'), date('now', 'localtime'), 1, 2, '0.50'),
  (6, 'Practice guitar', '', 1, 4, 11, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-58 days'), date('now', 'localtime', '-3 days'), 1, 3, '2.50'),
  (7, 'Meal prep', 'Sunday batch cooking', 7, 2, 2, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-16 days'), date('now', 'localtime', '-3 days'), 1, 4, '1'),
  (8, 'Walk the dog around the big park loop', '', 1, 1, 1, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-4 days'), date('now', 'localtime', '-1 day'), 1, 5, '1'),
  -- Not tracked: appears on the Habits screen only
  (9, 'Floss', '', 1, 0, 4, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-90 days'), date('now', 'localtime', '-40 days'), 0, NULL, '1'),
  -- Every 3 days, last done 2 days ago: shows "Due tomorrow"
  (10, 'Call grandma', '', 3, 5, 5, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-30 days'), date('now', 'localtime', '-2 days'), 1, NULL, '3');

-- id, goalId, habitId, overflowFromGoalId, amount, depositedOn, createdAt
-- "Morning stretch" (done today) already put $3 toward Danger Noodle's $5 daily cap.
INSERT INTO deposits (id, goalId, habitId, overflowFromGoalId, amount, depositedOn, createdAt) VALUES
  (1, 1, 2, NULL, '3', date('now', 'localtime'), strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-3 hours'));
