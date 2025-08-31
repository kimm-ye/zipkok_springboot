/****************************
    zipkok 데이터베이스
*****************************/

-- 데이터베이스 생성 및 사용자 설정
CREATE DATABASE IF NOT EXISTS zipkok CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'zipkok'@'%' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON zipkok.* TO 'zipkok'@'%';
FLUSH PRIVILEGES;

-- zipkok 데이터베이스 사용
USE zipkok;

-- 기존 테이블 삭제 (외래키 때문에 순서 중요)
DROP TABLE IF EXISTS helper;
DROP TABLE IF EXISTS member;

-- 기본 회원 테이블 (공통 정보)
CREATE TABLE member (
    member_seq INT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(20) UNIQUE NOT NULL,
    member_pass VARCHAR(20) NOT NULL,
    member_name VARCHAR(50) NOT NULL,
    member_email VARCHAR(50) NOT NULL,
    member_age VARCHAR(10) NOT NULL,
    member_gender INT NOT NULL,
    member_phone VARCHAR(20) NOT NULL,
    member_missionN INT,
    member_status INT DEFAULT 1 NOT NULL -- 관리자(0) / 일반사용자(1) / 헬퍼(2) / 블랙리스트(3)
);

ALTER TABLE member ADD CONSTRAINT uk_member_email UNIQUE (member_email);

-- 헬퍼 추가 정보 테이블
CREATE TABLE helper (
    member_seq INT PRIMARY KEY,
    member_bank VARCHAR(50),
    member_account VARCHAR(50),
    member_vehicle INT,
    member_introduce VARCHAR(500),
    member_ofile VARCHAR(200),
    member_sfile VARCHAR(200),
    member_review INT,
    member_missionC INT,
    member_point INT,
    FOREIGN KEY (member_seq) REFERENCES member(member_seq) ON DELETE CASCADE
);

CREATE TABLE helper_image (
    image_seq INT AUTO_INCREMENT PRIMARY KEY,
    member_seq INT NOT NULL,
    image_name VARCHAR(255),
    image_etx VARCHAR(20),
    image_file LONGBLOB,
    create_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_seq) REFERENCES helper(member_seq) ON DELETE CASCADE
);


-- 관리자
INSERT INTO member (member_id, member_pass, member_name, member_email, member_age, member_gender, member_phone, member_status)
VALUES ('admin', '1234', '관리자', 'tigsnor@naver.com', '0', 1, '010-1111-2222', 0);

-- 일반사용자 데이터
INSERT INTO member (member_id, member_pass, member_name, member_email, member_age, member_gender, member_phone, member_status)
VALUES ('hong', '1234', '홍길동', 'hong@naver.com', '0', 1, '01022223333', 1);

INSERT INTO member (member_id, member_pass, member_name, member_email, member_age, member_gender, member_phone, member_status)
VALUES ('sim', '1234', '심청이', 'sim@naver.com', '0', 2, '01033334444', 1);

-- 헬퍼 기본 정보
INSERT INTO member (member_id, member_pass, member_name, member_email, member_age, member_gender, member_phone, member_status)
VALUES ('helper', '1234', '이태우', 'helper@naver.com', '24', 1, '01055556666', 2);

-- 헬퍼 추가 정보 (helper의 member_seq는 4번이 됩니다)
INSERT INTO helper (member_seq, member_bank, member_account, member_vehicle, member_introduce, member_review, member_missionC, member_point)
VALUES (4, '국민은행', '12341241242', 0, '최선을다하겠습니다.', 4, 10, 2000);

-- 블랙리스트 회원
INSERT INTO member (member_id, member_pass, member_name, member_email, member_age, member_gender, member_phone, member_status)
VALUES ('black', '1234', '악', 'black@naver.com', '0', 1, '01066667777', 3);

------------------------------------------------------------------------------------------------------------

CREATE TABLE qboard (
    num INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(30) NOT NULL,
    content VARCHAR(200) NOT NULL,
    id VARCHAR(20) NOT NULL,
    postdate DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    visitcount INT DEFAULT 0 NOT NULL
);

-- QnA게시판 더미데이터
INSERT INTO qboard (title, content, id, postdate, visitcount)
VALUES ('큐보드1', '큐보드1', '사용자아이디', NOW(), 0);

INSERT INTO qboard (title, content, id, postdate, visitcount)
VALUES ('큐보드2', '큐보드2', '사용자아이디', NOW(), 0);

INSERT INTO qboard (title, content, id, postdate, visitcount)
VALUES ('큐보드3', '큐보드3', '사용자아이디', NOW(), 0);

INSERT INTO qboard (title, content, id, postdate, visitcount)
VALUES ('큐보드4', '큐보드4', '사용자아이디', NOW(), 0);

CREATE TABLE qreview (
    num INT PRIMARY KEY,
    content VARCHAR(200) NOT NULL,
    id VARCHAR(20) NOT NULL
);

INSERT INTO qreview (num, content, id)
VALUES (1, '답글내용1', '관리자아이디');

------------------------------------------------------------------------------------------------------------

CREATE TABLE mission (
    mission_num INT AUTO_INCREMENT PRIMARY KEY,
    mission_id VARCHAR(20) NOT NULL,
    mission_category VARCHAR(50) NOT NULL,
    mission_name VARCHAR(50) NOT NULL,
    mission_content VARCHAR(300) NOT NULL,
    mission_ofile VARCHAR(200),
    mission_sfile VARCHAR(200),
    mission_sex INT DEFAULT 1 NOT NULL,
    mission_Hid VARCHAR(20),
    mission_start VARCHAR(200),
    mission_waypoint VARCHAR(200),
    mission_end VARCHAR(200) NOT NULL,
    mission_mission INT NOT NULL,
    mission_reservation VARCHAR(50),
    mission_time VARCHAR(20) NOT NULL,
    mission_cost INT NOT NULL,
    mission_status INT DEFAULT 1 NOT NULL 
);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_time, mission_cost, mission_status)
VALUES ('test1', '청소,대리점업', '청소도우미구하는이벤트글올려주세요', '청소좀 도와주세요~~!!', '출발역', '도착역', 1, '1', 4000, 1);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_time, mission_cost, mission_status)
VALUES ('test1', '청소,대리점업', '청소좀도와주세요', '청소좀 도와주세요~~^^', '출발역', '도착역', 1, '1', 4000, 1);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_time, mission_cost, mission_status)
VALUES ('test1', '청소,대리점업', '청소좀도와주세요2', '청소좀 도와주세요~~2', '출발역', '도착역', 1, '1', 4000, 1);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_time, mission_cost, mission_status)
VALUES ('test1', '청소,대리점업', '청소좀도와주세요3', '청소좀 도와주세요~~3', '출발역', '도착역', 1, '1', 4000, 1);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_time, mission_cost, mission_status)
VALUES ('test1', '청소,대리점업', '청소좀도와주세요4', '청소좀 도와주세요~~4', '출발역', '도착역', 1, '1', 4000, 1);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_time, mission_cost, mission_status)
VALUES ('test1', '청소,대리점업', '청소좀도와주세요5', '청소좀 도와주세요~~5', '출발역', '도착역', 1, '1', 4000, 1);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_time, mission_cost, mission_status)
VALUES ('test1', '청소,대리점업', '청소좀도와주세요6', '청소좀 도와주세요~~6', '출발역', '도착역', 1, '1', 4000, 1);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_time, mission_cost, mission_status)
VALUES ('test1', '청소,대리점업', '청소좀도와주세요7', '청소좀 도와주세요~~7', '출발역', '도착역', 1, '1', 4000, 1);

INSERT INTO mission 
(mission_id, mission_category, mission_name, mission_content,
mission_start, mission_end, mission_mission, mission_reservation, mission_time, mission_cost, mission_status)
VALUES ('hong', '청소,대리운전', '청소요청!', '급하고 청소부탁드려요^^', '출발역', '도착역', 2, '2022-02-20', '4', 10000, 2);

----------------------------------------------------------------------------------------------

CREATE TABLE mboard (
    mboard_num INT AUTO_INCREMENT PRIMARY KEY,
    mboard_id VARCHAR(20) NOT NULL,
    mboard_title VARCHAR(30) NOT NULL,
    mboard_content VARCHAR(200) NOT NULL,
    mboard_date DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    mboard_count INT DEFAULT 0 NOT NULL
); 

INSERT INTO mboard (mboard_id, mboard_title, mboard_content, mboard_date, mboard_count)
VALUES ('게시자', '이것 뭐에요?', '이것좀알려주세요너무궁금해요', NOW(), 0);

INSERT INTO mboard (mboard_id, mboard_title, mboard_content, mboard_date, mboard_count)
VALUES ('게시자1', '이것 뭐에요?1', '이것좀알려주세요너무궁금해요1', NOW(), 0);

INSERT INTO mboard (mboard_id, mboard_title, mboard_content, mboard_date, mboard_count)
VALUES ('게시자2', '이것 뭐에요?2', '이것좀알려주세요너무궁금해요2', NOW(), 0);

INSERT INTO mboard (mboard_id, mboard_title, mboard_content, mboard_date, mboard_count)
VALUES ('게시자3', '이것 뭐에요?3', '이것좀알려주세요너무궁금해요3', NOW(), 0);

INSERT INTO mboard (mboard_id, mboard_title, mboard_content, mboard_date, mboard_count)
VALUES ('게시자4', '이것 뭐에요?4', '이것좀알려주세요너무궁금해요4', NOW(), 0);

----------------------------------------------------------------------------------------------

CREATE TABLE review(
    review_num INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    mission_num INT NOT NULL,
    review_id VARCHAR(10) NOT NULL,
    review_content VARCHAR(200) NOT NULL,
    review_point INT NOT NULL,
    review_date DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 리뷰 더미데이터
INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (1, 'kosmo', '이것은 좋습니다', 5, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (2, 'kosmo2', '이것은 좋습니다', 2, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (3, 'kosmo3', '이것은 좋습니다', 3, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (1, 'kosmo4', '이것은 좋습니다', 4, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (1, 'kosmo5', '이것은 좋습니다', 1, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (1, 'kosmoh123', '이것은 좋습니다', 3, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (0, 'kosmoh123', '정말 좋아요', 5, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (1, 'kosmoh123', '보통', 3, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (2, 'kosmoh123', '별로', 2, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (2, 'helper', '나쁘지않음', 2, NOW());

INSERT INTO review (mission_num, review_id, review_content, review_point, review_date)
VALUES (2, 'helper', 'good해333', 5, NOW());

-- 확인용 쿼리
SELECT 'member table' as table_name;
SELECT * FROM member;

SELECT 'helper table' as table_name;
SELECT * FROM helper;

SELECT 'member + helper join' as table_name;
SELECT m.*, h.member_bank, h.member_account, h.member_introduce, h.member_point
FROM member m 
LEFT JOIN helper h ON m.member_seq = h.member_seq 
WHERE m.member_status = 2;

SELECT 'all tables created' as status;