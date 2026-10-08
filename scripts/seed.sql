-- Test seed data for Habit Piggy (Room schema version 3).
-- Dates are relative to today so "done today" / "streak alive" states stay valid whenever this runs.
-- Covers: several goals at different fill levels (0%, ~1%, partial, ~full, over 100%), a long goal name,
-- an archived goal (hidden from Home), assigned / unassigned / untracked habits, completed-today habits,
-- streaks that are alive vs broken, a weekly habit, and a habit with a description.

DELETE FROM habits;
DELETE FROM savings_goals;
DELETE FROM sqlite_sequence WHERE name IN ('habits', 'savings_goals');

-- id, name, targetAmount, currentAmount, dueDate, dailyEarningsCap, status, createdAt, pinned
INSERT INTO savings_goals (id, name, targetAmount, currentAmount, dueDate, dailyEarningsCap, status, createdAt, pinned) VALUES
  (1, 'Danger Noodle', '2000', '412.50', NULL, '5', 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-128 days'), 1),
  (2, 'New Bike', '300', '247', NULL, NULL, 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-84 days'), 0),
  (3, 'Concert Tickets', '150', '162', NULL, '3', 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-60 days'), 0),
  (4, 'Weekend Trip', '900', '0', NULL, NULL, 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-17 days'), 0),
  (5, 'Emergency fund for vet visits and other surprises', '1000', '9', NULL, '2', 'ACTIVE', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-5 days'), 0),
  (6, 'Old Laptop Fund', '800', '800', NULL, NULL, 'ARCHIVED', strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-300 days'), 0);

-- id, title, description, frequency, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId
INSERT INTO habits (id, title, description, frequency, currentStreak, longestStreak, createdAt, lastCompletedOn, tracking, goalId) VALUES
  -- Pinned goal's page: assigned + unassigned habits
  (1, 'Read 20 pages', '', 'DAILY', 12, 15, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-120 days'), date('now', 'localtime', '-1 day'), 1, 1),
  (2, 'Morning stretch', 'Five minutes before coffee', 'DAILY', 6, 21, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-110 days'), date('now', 'localtime'), 1, NULL),
  (3, 'Drink water', '', 'DAILY', 0, 0, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-2 days'), NULL, 1, NULL),
  -- Other goals
  (4, 'Bike to work', '', 'DAILY', 3, 9, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-80 days'), date('now', 'localtime', '-1 day'), 1, 2),
  (5, 'Vitamins', '', 'DAILY', 30, 30, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-75 days'), date('now', 'localtime'), 1, 2),
  (6, 'Practice guitar', '', 'DAILY', 4, 11, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-58 days'), date('now', 'localtime', '-3 days'), 1, 3),
  (7, 'Meal prep', 'Sunday batch cooking', 'WEEKLY', 2, 2, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-16 days'), date('now', 'localtime', '-3 days'), 1, 4),
  (8, 'Walk the dog around the big park loop', '', 'DAILY', 1, 1, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-4 days'), date('now', 'localtime', '-1 day'), 1, 5),
  -- Not tracked: appears on the Habits screen only
  (9, 'Floss', '', 'DAILY', 0, 4, strftime('%Y-%m-%dT%H:%M:%S', 'now', 'localtime', '-90 days'), date('now', 'localtime', '-40 days'), 0, NULL);
