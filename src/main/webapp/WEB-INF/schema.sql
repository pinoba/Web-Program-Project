-- 밥심 DB 스키마 (서버 시작 시 InitServlet 의 init() 이 실행, 이미 있으면 건너뜀)

CREATE TABLE IF NOT EXISTS recipe (
    id            INT PRIMARY KEY,          -- API의 RCP_SEQ
    name          VARCHAR(200) NOT NULL,    -- RCP_NM
    category      VARCHAR(50),              -- RCP_PAT2 (밥, 국&찌개, 반찬, 일품, 후식, 기타)
    cook_way      VARCHAR(50),              -- RCP_WAY2 (끓이기, 굽기 ...)
    weight        VARCHAR(50),              -- INFO_WGT
    kcal          DOUBLE DEFAULT 0,         -- INFO_ENG
    carb          DOUBLE DEFAULT 0,         -- INFO_CAR
    protein       DOUBLE DEFAULT 0,         -- INFO_PRO
    fat           DOUBLE DEFAULT 0,         -- INFO_FAT
    sodium        DOUBLE DEFAULT 0,         -- INFO_NA
    hash_tag      VARCHAR(200),             -- HASH_TAG
    img_main      VARCHAR(500),             -- ATT_FILE_NO_MAIN
    parts_raw     CLOB,                     -- RCP_PARTS_DTLS 원문 (분량·그룹 포함)
    tip           CLOB,                     -- RCP_NA_TIP 저감 팁
    kcal_mismatch BOOLEAN DEFAULT FALSE,    -- 표기 열량과 탄단지 환산 열량 차이가 큰지
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recipe_step (
    recipe_id   INT NOT NULL,
    step_no     INT NOT NULL,
    description VARCHAR(1000) NOT NULL,
    PRIMARY KEY (recipe_id, step_no),
    FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS member (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    VARCHAR(30) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,       -- SHA-256 해시 (Base64)
    salt       VARCHAR(50) NOT NULL,
    nickname   VARCHAR(30) NOT NULL,
    role       VARCHAR(10) DEFAULT 'USER' NOT NULL,  -- USER / ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS favorite (
    member_id  INT NOT NULL,
    recipe_id  INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id, recipe_id),
    FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_recipe_name ON recipe(name);
CREATE INDEX IF NOT EXISTS idx_recipe_category ON recipe(category);
