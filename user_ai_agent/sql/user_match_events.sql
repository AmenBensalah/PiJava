-- user_match_events.sql
-- Reference extraction query skeleton.
-- This SQL is intentionally generic and may need adjustment to your exact schema.

SELECT
  u.id AS user_id,
  m.id AS match_id,
  m.tournoi_id,
  t.nom AS tournoi_name,
  m.type_game,
  m.type_tournoi,
  m.played_at AS timestamp,
  DATE(m.played_at) AS date,
  m.playerA_id,
  m.playerB_id,
  m.home_name,
  m.away_name,
  m.home_score,
  m.away_score,
  m.status,
  p.placement,
  p.points AS placement_points
FROM users u
JOIN matches m ON (m.playerA_id = u.id OR m.playerB_id = u.id)
LEFT JOIN tournois t ON t.id = m.tournoi_id
LEFT JOIN match_participants p ON p.match_id = m.id AND p.user_id = u.id;
