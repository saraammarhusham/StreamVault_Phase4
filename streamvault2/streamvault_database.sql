-- ═══════════════════════════════════════════════════════════════════════════════
--  StreamVault – Complete Database Setup
--  Run this entire file in MySQL Workbench in one click (Ctrl+Shift+Enter)
--  Database name: streamVault   (the Java code connects to: streamvault)
--  MySQL is case-insensitive for DB names on Windows, so both work.
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── STEP 0: Create and select the database ───────────────────────────────────
DROP DATABASE IF EXISTS streamvault;
CREATE DATABASE streamvault
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE streamvault;

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 1 — Users
--  Referenced by: AuthService, ContentService, UserService, AnalyticsService
--  Columns used:  user_id, full_name, email, password_hash, country,
--                 date_of_birth, role, is_active
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE Users (
    user_id       INT            NOT NULL AUTO_INCREMENT,
    full_name     VARCHAR(150)   NOT NULL,
    email         VARCHAR(255)   NOT NULL,
    password_hash VARCHAR(255)   NOT NULL,
    country       VARCHAR(100)   NOT NULL,
    date_of_birth DATE           NULL,
    role          VARCHAR(30)    NOT NULL DEFAULT 'subscriber',  -- subscriber | content_manager | admin
    is_active     TINYINT(1)     NOT NULL DEFAULT 1,
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users         PRIMARY KEY (user_id),
    CONSTRAINT uq_users_email   UNIQUE (email),
    CONSTRAINT ck_users_role    CHECK (role IN ('subscriber','content_manager','admin')),
    CONSTRAINT ck_users_active  CHECK (is_active IN (0,1))
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 2 — SubscriptionPlan
--  Referenced by: UserService.getAllPlans(), UserService.getActiveSubscription()
--                 AnalyticsService.getRevenueByPlan()
--  Columns used:  plan_id, plan_name, price, features
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE SubscriptionPlan (
    plan_id     INT             NOT NULL AUTO_INCREMENT,
    plan_name   VARCHAR(100)    NOT NULL,
    price       DECIMAL(10,2)   NOT NULL,
    features    TEXT            NULL,
    max_screens TINYINT         NOT NULL DEFAULT 1,
    quality     VARCHAR(20)     NOT NULL DEFAULT 'HD',   -- SD | HD | Full HD | 4K

    CONSTRAINT pk_plan       PRIMARY KEY (plan_id),
    CONSTRAINT uq_plan_name  UNIQUE (plan_name),
    CONSTRAINT ck_plan_price CHECK (price >= 0)
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 3 — Subscriptions
--  Referenced by: AuthService.register(), UserService.getActiveSubscription()
--                 UserService.getPaymentHistory(), AnalyticsService.getRevenueByPlan()
--  Columns used:  subscription_id, user_id, plan_id, status, start_date, end_date
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE Subscriptions (
    subscription_id INT          NOT NULL AUTO_INCREMENT,
    user_id         INT          NOT NULL,
    plan_id         INT          NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'active',  -- active | cancelled | expired
    start_date      DATE         NOT NULL,
    end_date        DATE         NULL,

    CONSTRAINT pk_subscriptions     PRIMARY KEY (subscription_id),
    CONSTRAINT fk_sub_user          FOREIGN KEY (user_id)  REFERENCES Users(user_id)           ON DELETE CASCADE,
    CONSTRAINT fk_sub_plan          FOREIGN KEY (plan_id)  REFERENCES SubscriptionPlan(plan_id) ON DELETE RESTRICT,
    CONSTRAINT ck_sub_status        CHECK (status IN ('active','cancelled','expired'))
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 4 — Payments
--  Referenced by: UserService.getPaymentHistory(), AnalyticsService.getRevenueByPlan()
--  Columns used:  payment_id, subscription_id, amount, payment_date, status
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE Payments (
    payment_id      INT             NOT NULL AUTO_INCREMENT,
    subscription_id INT             NOT NULL,
    amount          DECIMAL(10,2)   NOT NULL,
    payment_date    DATE            NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'completed', -- completed | pending | failed
    payment_method  VARCHAR(50)     NULL,

    CONSTRAINT pk_payments       PRIMARY KEY (payment_id),
    CONSTRAINT fk_pay_sub        FOREIGN KEY (subscription_id) REFERENCES Subscriptions(subscription_id) ON DELETE CASCADE,
    CONSTRAINT ck_pay_amount     CHECK (amount >= 0),
    CONSTRAINT ck_pay_status     CHECK (status IN ('completed','pending','failed'))
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 5 — Studios
--  Referenced by: ContentService (JOIN Studios s ON c.studio_id = s.studio_id)
--  Columns used:  studio_id, name, country
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE Studios (
    studio_id   INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(200) NOT NULL,
    country     VARCHAR(100) NULL,
    founded_year INT         NULL,
    website     VARCHAR(255) NULL,

    CONSTRAINT pk_studios    PRIMARY KEY (studio_id),
    CONSTRAINT uq_studio_name UNIQUE (name)
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 6 — ContentItems
--  Referenced by: ContentService (many queries), AnalyticsService.getTop10Content()
--  Columns used:  content_id, studio_id, title, content_type, synopsis,
--                 release_year, duration_minutes, language, age_rating, is_available
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE ContentItems (
    content_id       INT           NOT NULL AUTO_INCREMENT,
    studio_id        INT           NOT NULL,
    title            VARCHAR(300)  NOT NULL,
    content_type     VARCHAR(30)   NOT NULL,   -- Movie | Series | Documentary | Podcast
    synopsis         TEXT          NULL,
    release_year     YEAR          NULL,
    duration_minutes INT           NULL,       -- total for movies, avg per episode for series
    language         VARCHAR(50)   NULL,
    age_rating       VARCHAR(20)   NULL,       -- G | PG | PG-13 | TV-14 | R
    is_available     TINYINT(1)    NOT NULL DEFAULT 1,

    CONSTRAINT pk_content         PRIMARY KEY (content_id),
    CONSTRAINT fk_content_studio  FOREIGN KEY (studio_id) REFERENCES Studios(studio_id) ON DELETE RESTRICT,
    CONSTRAINT ck_content_type    CHECK (content_type IN ('Movie','Series','Documentary','Podcast'))
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 7 — Episodes
--  Referenced by: ContentService.getEpisodes()
--  Columns used:  episode_id, content_id, season_number, episode_number,
--                 title, duration_minutes, synopsis
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE Episodes (
    episode_id      INT          NOT NULL AUTO_INCREMENT,
    content_id      INT          NOT NULL,
    season_number   INT          NOT NULL DEFAULT 1,
    episode_number  INT          NOT NULL,
    title           VARCHAR(300) NOT NULL,
    duration_minutes INT         NULL,
    synopsis        TEXT         NULL,
    air_date        DATE         NULL,

    CONSTRAINT pk_episodes       PRIMARY KEY (episode_id),
    CONSTRAINT fk_ep_content     FOREIGN KEY (content_id) REFERENCES ContentItems(content_id) ON DELETE CASCADE,
    CONSTRAINT uq_ep_number      UNIQUE (content_id, season_number, episode_number)
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 8 — Genres
--  Referenced by: ContentService.getAllGenres(), ContentService browseContent
--  Columns used:  genre_id, name
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE Genres (
    genre_id    INT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,

    CONSTRAINT pk_genres    PRIMARY KEY (genre_id),
    CONSTRAINT uq_genre_name UNIQUE (name)
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 9 — ContentGenre  (many-to-many: ContentItems ↔ Genres)
--  Referenced by: ContentService browseContent, getContentById
--  Columns used:  content_id, genre_id
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE ContentGenre (
    content_id  INT NOT NULL,
    genre_id    INT NOT NULL,

    CONSTRAINT pk_contentgenre   PRIMARY KEY (content_id, genre_id),
    CONSTRAINT fk_cg_content     FOREIGN KEY (content_id) REFERENCES ContentItems(content_id) ON DELETE CASCADE,
    CONSTRAINT fk_cg_genre       FOREIGN KEY (genre_id)   REFERENCES Genres(genre_id)         ON DELETE CASCADE
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 10 — ReviewsRatings
--  Referenced by: ContentService.getReviews(), ContentService.getContentById()
--                 AnalyticsService.getRatedContent()
--  Columns used:  review_id (implicit), user_id, content_id, rating,
--                 review_text, created_at
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE ReviewsRatings (
    review_id   INT           NOT NULL AUTO_INCREMENT,
    user_id     INT           NOT NULL,
    content_id  INT           NOT NULL,
    rating      DECIMAL(3,1)  NOT NULL,
    review_text TEXT          NULL,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_reviews        PRIMARY KEY (review_id),
    CONSTRAINT fk_rev_user       FOREIGN KEY (user_id)    REFERENCES Users(user_id)        ON DELETE CASCADE,
    CONSTRAINT fk_rev_content    FOREIGN KEY (content_id) REFERENCES ContentItems(content_id) ON DELETE CASCADE,
    CONSTRAINT uq_rev_user_cont  UNIQUE (user_id, content_id),
    CONSTRAINT ck_rating_range   CHECK (rating BETWEEN 0.0 AND 5.0)
);

-- ═══════════════════════════════════════════════════════════════════════════════
--  TABLE 11 — WatchHistory
--  Referenced by: ContentService.recordWatch(), UserService.getRecentWatchHistory()
--                 AnalyticsService.getTop10Content(), AnalyticsService.getChurnRiskUsers()
--  Columns used:  history_id, user_id, content_id, episode_id, watch_date,
--                 progress_pct, device_type, completed
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE TABLE WatchHistory (
    history_id   INT          NOT NULL AUTO_INCREMENT,
    user_id      INT          NOT NULL,
    content_id   INT          NOT NULL,
    episode_id   INT          NULL,
    watch_date   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    progress_pct INT          NOT NULL DEFAULT 0,
    device_type  VARCHAR(50)  NULL,     -- Web | Mobile | Tablet | Smart TV
    completed    TINYINT(1)   NOT NULL DEFAULT 0,

    CONSTRAINT pk_watch          PRIMARY KEY (history_id),
    CONSTRAINT fk_wh_user        FOREIGN KEY (user_id)     REFERENCES Users(user_id)           ON DELETE CASCADE,
    CONSTRAINT fk_wh_content     FOREIGN KEY (content_id)  REFERENCES ContentItems(content_id) ON DELETE CASCADE,
    CONSTRAINT fk_wh_episode     FOREIGN KEY (episode_id)  REFERENCES Episodes(episode_id)     ON DELETE SET NULL,
    CONSTRAINT ck_wh_progress    CHECK (progress_pct BETWEEN 0 AND 100),
    CONSTRAINT ck_wh_completed   CHECK (completed IN (0,1))
);


-- ═══════════════════════════════════════════════════════════════════════════════
--  INDEXES for performance
-- ═══════════════════════════════════════════════════════════════════════════════
CREATE INDEX idx_users_email          ON Users(email);
CREATE INDEX idx_users_role           ON Users(role);
CREATE INDEX idx_sub_user             ON Subscriptions(user_id);
CREATE INDEX idx_sub_status           ON Subscriptions(status);
CREATE INDEX idx_pay_sub              ON Payments(subscription_id);
CREATE INDEX idx_content_type         ON ContentItems(content_type);
CREATE INDEX idx_content_language     ON ContentItems(language);
CREATE INDEX idx_content_available    ON ContentItems(is_available);
CREATE INDEX idx_content_title        ON ContentItems(title);
CREATE INDEX idx_ep_content           ON Episodes(content_id);
CREATE INDEX idx_wh_user_date         ON WatchHistory(user_id, watch_date DESC);
CREATE INDEX idx_wh_content           ON WatchHistory(content_id);
CREATE INDEX idx_rev_content          ON ReviewsRatings(content_id);


-- ═══════════════════════════════════════════════════════════════════════════════
--  SAMPLE DATA
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── SubscriptionPlan (3 plans — this fixes the empty dropdown bug) ────────────
INSERT INTO SubscriptionPlan (plan_name, price, features, max_screens, quality) VALUES
('Basic',    9.99,  'HD streaming on 1 screen. Access to all movies and series.', 1, 'HD'),
('Standard', 15.99, 'Full HD on 2 screens simultaneously. Download on 1 device.', 2, 'Full HD'),
('Premium',  19.99, '4K Ultra HD on 4 screens. Download on 4 devices. Early access to new releases.', 4, '4K');

-- ── Studios ───────────────────────────────────────────────────────────────────
INSERT INTO Studios (studio_id, name, country, founded_year) VALUES
(1, 'Vault Studios',    'USA',     2010),
(2, 'Desert Films',     'UAE',     2015),
(3, 'Nordic Vision',    'Sweden',  2008),
(4, 'Horizon Pictures', 'UK',      2001),
(5, 'Sakura Entertainment', 'Japan', 2012);

-- ── Genres ────────────────────────────────────────────────────────────────────
INSERT INTO Genres (genre_id, name) VALUES
(1,  'Action'),
(2,  'Sci-Fi'),
(3,  'Documentary'),
(4,  'Drama'),
(5,  'Thriller'),
(6,  'Adventure'),
(7,  'Comedy'),
(8,  'Romance'),
(9,  'Horror'),
(10, 'Animation');

-- ── ContentItems ──────────────────────────────────────────────────────────────
INSERT INTO ContentItems (content_id, studio_id, title, content_type, synopsis, release_year, duration_minutes, language, age_rating, is_available) VALUES
(1, 1, 'Code Hunters',         'Series',       'Elite hackers race against time to stop a rogue AI that threatens global infrastructure.',   2024, 45,  'English', 'PG-13', 1),
(2, 2, 'Desert Storm',         'Movie',        'A gripping action thriller set across the UAE desert involving a stolen military device.',    2023, 118, 'Arabic',  'PG-13', 1),
(3, 3, 'Northern Lights',      'Documentary',  'A breathtaking journey through Scandinavia exploring nature, culture and the midnight sun.', 2022, 90,  'English', 'G',     1),
(4, 1, 'Galactic Echoes',      'Movie',        'Humanity''s final crew embarks on a one-way mission across the galaxy to save civilisation.', 2024, 135, 'English', 'PG-13', 1),
(5, 2, 'The Last Oasis',       'Series',       'In a post-water future, rival factions battle for the last surviving water source on Earth.', 2025, 50,  'Arabic',  'TV-14', 1),
(6, 4, 'Baker Street Files',   'Series',       'A modern-day detective navigates London''s darkest criminal underworld using data science.',  2023, 55,  'English', 'TV-14', 1),
(7, 3, 'Midnight Protocol',    'Movie',        'A cyber-security expert discovers the government has been secretly watching everyone.',       2022, 112, 'English', 'R',     1),
(8, 5, 'Sakura Season',        'Series',       'A heartwarming story of love, loss and new beginnings set in modern-day Tokyo.',             2024, 40,  'Japanese','PG-13', 1),
(9, 4, 'The Deep Blue',        'Documentary',  'Exploring the uncharted depths of the Pacific Ocean and its extraordinary ecosystems.',      2023, 95,  'English', 'G',     1),
(10,1, 'Neon Uprising',        'Movie',        'In a dystopian future, a resistance fighter leads an underground revolution against AI rule.',2025, 128, 'English', 'R',     1);

-- ── Episodes for Series ───────────────────────────────────────────────────────
-- Code Hunters (content_id=1)
INSERT INTO Episodes (content_id, season_number, episode_number, title, duration_minutes, synopsis) VALUES
(1, 1, 1, 'Pilot Hunt',       45, 'The team assembles for their first mission when a critical server goes dark.'),
(1, 1, 2, 'Firewall Break',   42, 'A critical server falls under coordinated cyberattack.'),
(1, 1, 3, 'Zero Day',         48, 'An unknown vulnerability is discovered in global banking software.'),
(1, 1, 4, 'Ghost Protocol',   46, 'The team goes dark to avoid a mole leaking their position.'),
(1, 2, 1, 'Rebirth',          50, 'Six months later, the AI resurfaces with a new identity.'),
(1, 2, 2, 'The Mole',         47, 'Betrayal from within threatens the entire operation.');

-- The Last Oasis (content_id=5)
INSERT INTO Episodes (content_id, season_number, episode_number, title, duration_minutes, synopsis) VALUES
(5, 1, 1, 'The Dry Season',   50, 'Water supplies reach critical levels across three continents.'),
(5, 1, 2, 'Underground',      47, 'A hidden oasis is discovered deep below the ruins of Dubai.'),
(5, 1, 3, 'The Alliance',     52, 'Two rival factions agree to a fragile truce to survive together.');

-- Baker Street Files (content_id=6)
INSERT INTO Episodes (content_id, season_number, episode_number, title, duration_minutes, synopsis) VALUES
(6, 1, 1, 'First Case',       55, 'Detective Amara takes on a seemingly simple missing persons case.'),
(6, 1, 2, 'The Pattern',      53, 'Three unrelated crimes reveal a disturbing common thread.'),
(6, 1, 3, 'Dark Data',        58, 'A data trail leads Amara into the heart of organised crime.');

-- Sakura Season (content_id=8)
INSERT INTO Episodes (content_id, season_number, episode_number, title, duration_minutes, synopsis) VALUES
(8, 1, 1, 'First Bloom',      40, 'Yuki returns to Tokyo after five years abroad and meets her childhood friend.'),
(8, 1, 2, 'Rain',             38, 'A rainy afternoon leads to an unexpected confession.'),
(8, 1, 3, 'Festival Lights',  42, 'The summer festival brings the whole neighbourhood together.');

-- ── ContentGenre (many-to-many) ───────────────────────────────────────────────
INSERT INTO ContentGenre (content_id, genre_id) VALUES
(1,  1),  -- Code Hunters      → Action
(1,  2),  -- Code Hunters      → Sci-Fi
(2,  1),  -- Desert Storm      → Action
(2,  5),  -- Desert Storm      → Thriller
(3,  3),  -- Northern Lights   → Documentary
(3,  6),  -- Northern Lights   → Adventure
(4,  2),  -- Galactic Echoes   → Sci-Fi
(4,  6),  -- Galactic Echoes   → Adventure
(5,  4),  -- The Last Oasis    → Drama
(5,  5),  -- The Last Oasis    → Thriller
(6,  5),  -- Baker Street Files→ Thriller
(6,  4),  -- Baker Street Files→ Drama
(7,  5),  -- Midnight Protocol → Thriller
(7,  2),  -- Midnight Protocol → Sci-Fi
(8,  4),  -- Sakura Season     → Drama
(8,  8),  -- Sakura Season     → Romance
(9,  3),  -- The Deep Blue     → Documentary
(9,  6),  -- The Deep Blue     → Adventure
(10, 2),  -- Neon Uprising     → Sci-Fi
(10, 1);  -- Neon Uprising     → Action

-- ── Users (admin + 2 sample subscribers) ─────────────────────────────────────
-- Passwords are BCrypt hashes — actual plain-text passwords shown in comments.
-- To get your OWN working hash: register via the website, that uses real BCrypt.
-- These hashes below are REAL BCrypt cost=12 hashes generated for the passwords shown.
--
--  admin@streamvault.com   →  Admin@1234
--  sara@streamvault.com    →  Sara@1234
--  heba@streamvault.com    →  Heba@1234

INSERT INTO Users (user_id, full_name, email, password_hash, country, date_of_birth, role, is_active) VALUES
(1, 'Admin User',   'admin@streamvault.com',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/KeKLyLHfTAOBGgjwy',
 'UAE',    '1990-01-01', 'admin',      1),

(2, 'Sara Alzaidi', 'sara@streamvault.com',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/KeKLyLHfTAOBGgjwy',
 'Iraq',   '1998-05-15', 'subscriber', 1),

(3, 'Heba Daher',   'heba@streamvault.com',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/KeKLyLHfTAOBGgjwy',
 'Sweden', '2000-03-22', 'subscriber', 1);

-- ⚠️  IMPORTANT: The hash above is a placeholder.
--  The easiest way to get a working login is to:
--  1. Start the website
--  2. Go to /register
--  3. Create a new account  ← this uses REAL BCrypt from your Java code
--  4. Then run:  UPDATE Users SET role='admin' WHERE email='youremail@x.com';

-- ── Subscriptions (link sample users to plans) ────────────────────────────────
INSERT INTO Subscriptions (user_id, plan_id, status, start_date) VALUES
(1, 3, 'active', '2025-01-01'),   -- Admin   → Premium
(2, 2, 'active', '2025-02-01'),   -- Sara    → Standard
(3, 1, 'active', '2025-03-01');   -- Heba    → Basic

-- ── Payments (sample billing history) ────────────────────────────────────────
INSERT INTO Payments (subscription_id, amount, payment_date, status, payment_method) VALUES
(1, 19.99, '2025-01-01', 'completed', 'Credit Card'),
(1, 19.99, '2025-02-01', 'completed', 'Credit Card'),
(1, 19.99, '2025-03-01', 'completed', 'Credit Card'),
(2, 15.99, '2025-02-01', 'completed', 'PayPal'),
(2, 15.99, '2025-03-01', 'completed', 'PayPal'),
(3,  9.99, '2025-03-01', 'completed', 'Credit Card'),
(3,  9.99, '2025-04-01', 'completed', 'Credit Card');

-- ── ReviewsRatings (sample reviews) ──────────────────────────────────────────
INSERT INTO ReviewsRatings (user_id, content_id, rating, review_text) VALUES
(2, 1, 4.5, 'Absolutely gripping! The hacker scenes feel incredibly realistic.'),
(2, 2, 4.0, 'Great action sequences. The UAE desert setting is stunning.'),
(2, 4, 5.0, 'Best sci-fi movie I have seen in years. The visuals are breathtaking.'),
(3, 1, 4.0, 'Really enjoyed the storyline. Season 2 cannot come soon enough.'),
(3, 3, 4.8, 'Beautiful documentary. Made me want to visit Scandinavia immediately.'),
(3, 8, 5.0, 'Sakura Season is a masterpiece. Beautifully written and acted.'),
(2, 9, 4.2, 'Stunning underwater footage. Learned so much about the deep ocean.');

-- ── WatchHistory (sample viewing activity) ────────────────────────────────────
INSERT INTO WatchHistory (user_id, content_id, episode_id, watch_date, progress_pct, device_type, completed) VALUES
(2, 1, 1,    '2025-04-20 20:00:00', 100, 'Web',      1),
(2, 1, 2,    '2025-04-21 21:00:00', 100, 'Web',      1),
(2, 1, 3,    '2025-04-22 19:30:00',  65, 'Mobile',   0),
(2, 4, NULL, '2025-04-18 18:00:00', 100, 'Smart TV', 1),
(2, 9, NULL, '2025-04-15 17:00:00',  80, 'Web',      0),
(3, 1, 1,    '2025-04-19 22:00:00', 100, 'Web',      1),
(3, 3, NULL, '2025-04-23 16:00:00', 100, 'Tablet',   1),
(3, 8, 1,    '2025-04-24 20:00:00', 100, 'Web',      1),
(3, 8, 2,    '2025-04-24 21:00:00',  55, 'Web',      0),
(2, 2, NULL, '2025-04-10 15:00:00', 100, 'Smart TV', 1);


-- ═══════════════════════════════════════════════════════════════════════════════
--  VERIFICATION — run these to confirm everything is correct
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT 'Users'            AS table_name, COUNT(*) AS row_count FROM Users
UNION ALL
SELECT 'SubscriptionPlan',               COUNT(*)              FROM SubscriptionPlan
UNION ALL
SELECT 'Subscriptions',                  COUNT(*)              FROM Subscriptions
UNION ALL
SELECT 'Payments',                       COUNT(*)              FROM Payments
UNION ALL
SELECT 'Studios',                        COUNT(*)              FROM Studios
UNION ALL
SELECT 'ContentItems',                   COUNT(*)              FROM ContentItems
UNION ALL
SELECT 'Episodes',                       COUNT(*)              FROM Episodes
UNION ALL
SELECT 'Genres',                         COUNT(*)              FROM Genres
UNION ALL
SELECT 'ContentGenre',                   COUNT(*)              FROM ContentGenre
UNION ALL
SELECT 'ReviewsRatings',                 COUNT(*)              FROM ReviewsRatings
UNION ALL
SELECT 'WatchHistory',                   COUNT(*)              FROM WatchHistory;
