-- ============================================================
-- 《今天学点啥》建表脚本 - SQLite 版
--
-- 迁移到 PostgreSQL 时的改动点（已在注释中标出）：
--   1. TEXT → VARCHAR(n) 或 UUID
--   2. INTEGER → BOOLEAN（布尔字段）
--   3. 去掉 STRICT
--   4. DEFAULT (datetime('now','localtime')) → DEFAULT NOW()
--   5. 添加 SERIAL / GENERATED 主键（如需自增）
--   6. 外键语法保持一致，无需修改
-- ============================================================

PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS users
(
    id           TEXT    NOT NULL,   -- PG迁移: CHAR(32) NOT NULL
    email        TEXT    NOT NULL,
    password     TEXT    NOT NULL,
    display_name TEXT,
    avatar_url   TEXT,
    timezone     TEXT    NOT NULL DEFAULT 'Asia/Shanghai',
    created_at   TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),  -- PG: DEFAULT NOW()
    updated_at   TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    PRIMARY KEY (id),
    UNIQUE (email)
);

-- ============================================================
-- 2. 学习方向表（支持父子层级）
-- ============================================================
CREATE TABLE IF NOT EXISTS topics
(
    id          TEXT    NOT NULL,
    name        TEXT    NOT NULL,
    slug        TEXT    NOT NULL,
    parent_id   TEXT,
    description TEXT,
    icon        TEXT,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    is_active   INTEGER NOT NULL DEFAULT 1,  -- PG迁移: BOOLEAN DEFAULT TRUE
    created_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    PRIMARY KEY (id),
    UNIQUE (slug),
    FOREIGN KEY (parent_id) REFERENCES topics (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_topics_parent   ON topics (parent_id);
CREATE INDEX IF NOT EXISTS idx_topics_sort     ON topics (is_active, sort_order);

-- ============================================================
-- 3. 用户偏好表（每人一条）
-- ============================================================
CREATE TABLE IF NOT EXISTS user_preferences
(
    id                    TEXT    NOT NULL,
    user_id               TEXT    NOT NULL,
    enable_auto_recommend INTEGER NOT NULL DEFAULT 1,
    created_at            TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at            TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    PRIMARY KEY (id),
    UNIQUE (user_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ============================================================
-- 4. 用户偏好方向表（多对多）
-- ============================================================
CREATE TABLE IF NOT EXISTS user_preference_topics
(
    user_id    TEXT NOT NULL,
    topic_id   TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    PRIMARY KEY (user_id, topic_id),
    FOREIGN KEY (user_id)  REFERENCES users  (id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topics (id) ON DELETE CASCADE
);

-- ============================================================
-- 5. 知识点库（去重核心）
-- ============================================================
CREATE TABLE IF NOT EXISTS knowledge_points
(
    id                TEXT    NOT NULL,
    topic_id          TEXT    NOT NULL,
    title             TEXT    NOT NULL,
    content           TEXT    NOT NULL,
    difficulty        TEXT    NOT NULL,  -- easy / medium / hard
    estimated_minutes INTEGER NOT NULL,
    content_hash      TEXT    NOT NULL,  -- MD5(lower(title)::topic_id)
    source            TEXT    NOT NULL DEFAULT 'ai_generated',  -- preset / ai_generated
    is_active         INTEGER NOT NULL DEFAULT 1,
    created_at        TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    PRIMARY KEY (id),
    UNIQUE (topic_id, content_hash),
    FOREIGN KEY (topic_id) REFERENCES topics (id)
);

CREATE INDEX IF NOT EXISTS idx_kp_topic_difficulty ON knowledge_points (topic_id, difficulty);

-- ============================================================
-- 6. 每日学习计划
-- ============================================================
CREATE TABLE IF NOT EXISTS daily_plans
(
    id                TEXT    NOT NULL,
    user_id           TEXT    NOT NULL,
    plan_date         TEXT    NOT NULL,  -- 格式: YYYY-MM-DD
    available_minutes INTEGER NOT NULL,
    is_auto_topic     INTEGER NOT NULL DEFAULT 0,
    status            TEXT    NOT NULL DEFAULT 'active',  -- active / completed / skipped
    completed_at      TEXT,
    created_at        TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at        TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    PRIMARY KEY (id),
    UNIQUE (user_id, plan_date),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_plan_user_status ON daily_plans (user_id, status);

-- ============================================================
-- 7. 计划-方向关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS plan_topics
(
    plan_id             TEXT    NOT NULL,
    topic_id            TEXT    NOT NULL,
    is_auto_recommended INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (plan_id, topic_id),
    FOREIGN KEY (plan_id)  REFERENCES daily_plans (id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topics      (id)
);

-- ============================================================
-- 8. 学习任务表（completed 状态即为已归档）
-- ============================================================
CREATE TABLE IF NOT EXISTS tasks
(
    id                 TEXT    NOT NULL,
    plan_id            TEXT    NOT NULL,
    user_id            TEXT    NOT NULL,   -- 冗余，避免 JOIN daily_plans
    knowledge_point_id TEXT,
    topic_id           TEXT    NOT NULL,   -- 冗余，方便归档按方向筛选
    title              TEXT    NOT NULL,
    content            TEXT    NOT NULL,
    estimated_minutes  INTEGER NOT NULL,
    difficulty         TEXT    NOT NULL,
    status             TEXT    NOT NULL DEFAULT 'pending',  -- pending / completed / skipped
    sort_order         INTEGER NOT NULL DEFAULT 0,
    completed_at       TEXT,
    created_at         TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    updated_at         TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
    PRIMARY KEY (id),
    FOREIGN KEY (plan_id) REFERENCES daily_plans (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users       (id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES topics     (id)
);

CREATE INDEX IF NOT EXISTS idx_tasks_plan_id      ON tasks (plan_id);
CREATE INDEX IF NOT EXISTS idx_tasks_user_status  ON tasks (user_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_user_kp      ON tasks (user_id, knowledge_point_id);
CREATE INDEX IF NOT EXISTS idx_tasks_completed_at ON tasks (user_id, completed_at);
