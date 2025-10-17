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
    member_status INT DEFAULT 1 NOT NULL, -- 관리자(0) / 일반사용자(1) / 헬퍼(2) / 블랙리스트(3),
    member_useYn CHAR(1) DEFAULT 1 not null ,
    create_dt datetime default CURRENT_TIMESTAMP
);

ALTER TABLE member ADD CONSTRAINT uk_member_email UNIQUE (member_email);

-- 헬퍼 추가 정보 테이블
CREATE TABLE helper (
    member_seq INT PRIMARY KEY,
    member_bank CHAR(3),
    member_account VARCHAR(50),
    member_vehicle INT,
    member_introduce VARCHAR(500),
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
    mission_seq INT AUTO_INCREMENT PRIMARY KEY,
    member_seq INT NOT NULL,
    mission_category VARCHAR(50) NOT NULL,
    mission_title VARCHAR(50) NOT NULL,
    mission_content VARCHAR(300) NOT NULL,
    mission_gender INT ,
    helper_seq INT,
    mission_reservation CHAR(1) NOT NULL,
    mission_reservation_dt VARCHAR(50),
    mission_time VARCHAR(20),
    mission_cost INT NOT NULL,
    mission_status INT DEFAULT 1 NOT NULL 
);


CREATE INDEX idx_mission_member_seq ON mission(member_seq);
CREATE INDEX idx_mission_helper_seq ON mission(helper_seq);
----------------------------------------------------------------------------------------------

CREATE TABLE board_notice (
    notice_seq INT AUTO_INCREMENT PRIMARY KEY,
    notice_title VARCHAR(100) NOT NULL,
    notice_content VARCHAR(2000) NOT NULL,
    member_seq INT NOT NULL,
    notice_date DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    notice_view INT DEFAULT 0 NOT NULL
); 

INSERT INTO board_notice (notice_title, notice_content, member_seq, notice_date, notice_view)
VALUES ('이것 뭐에요?', '이것좀알려주세요너무궁금해요', 1, NOW(), 0);

INSERT INTO board_notice (notice_title, notice_content, member_seq, notice_date, notice_view)
VALUES ('이것 뭐에요?1', '이것좀알려주세요너무궁금해요1', 1, NOW(), 0);

INSERT INTO board_notice (notice_title, notice_content, member_seq, notice_date, notice_view)
VALUES ('이것 뭐에요?2', '이것좀알려주세요너무궁금해요2',1, NOW(), 0);

INSERT INTO board_notice (notice_title, notice_content, member_seq, notice_date, notice_view)
VALUES ('이것 뭐에요?3', '이것좀알려주세요너무궁금해요3',1, NOW(), 0);

INSERT INTO board_notice (notice_title, notice_content, member_seq, notice_date, notice_view)
VALUES ('이것 뭐에요?4', '이것좀알려주세요너무궁금해요4',1, NOW(), 0);

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





-- 공통 코드 그룹 테이블
CREATE TABLE code_group (
                            group_code VARCHAR(20) PRIMARY KEY COMMENT '그룹 코드',
                            group_name VARCHAR(100) NOT NULL COMMENT '그룹명',
                            description TEXT COMMENT '설명',
                            use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용여부',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시'
) COMMENT '공통 코드 그룹';

-- 공통 코드 상세 테이블
CREATE TABLE code_detail (
                             group_code VARCHAR(20) NOT NULL COMMENT '그룹 코드',
                             code VARCHAR(20) NOT NULL COMMENT '코드',
                             code_name VARCHAR(100) NOT NULL COMMENT '코드명',
                             description TEXT COMMENT '설명',
                             sort_order INT DEFAULT 0 COMMENT '정렬순서',
                             use_yn CHAR(1) DEFAULT 'Y' COMMENT '사용여부',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
                             PRIMARY KEY (group_code, code),
                             FOREIGN KEY (group_code) REFERENCES code_group(group_code) ON DELETE CASCADE
) COMMENT '공통 코드 상세';

-- 인덱스 생성
CREATE INDEX idx_code_detail_group_use ON code_detail(group_code, use_yn);
CREATE INDEX idx_code_detail_sort ON code_detail(group_code, sort_order);

-- 그룹 코드 데이터 입력
INSERT INTO code_group (group_code, group_name, description) VALUES
    ('MISSION_STATUS', '심부름 상태', '심부름의 진행 상태를 관리'),
('MISSION_TIME', '소요 시간', '심부름 예상 소요 시간'),
('GENDER', '성별', '사용자 성별'),
('MISSION_CATEGORY', '심부름 카테고리', '심부름 종류 분류');

-- 상세 코드 데이터 입력
-- 심부름 상태
INSERT INTO code_detail (group_code, code, code_name, description, sort_order) VALUES
    ('MISSION_STATUS', '0', '신청(대기)', '심부름 신청 후 대기 상태', 1),
('MISSION_STATUS', '1', '진행중', '심부름이 진행중인 상태', 2),
('MISSION_STATUS', '2', '완료', '심부름이 완료된 상태', 3),
('MISSION_STATUS', '3', '취소', '심부름이 취소된 상태', 4);

-- 소요 시간
INSERT INTO code_detail (group_code, code, code_name, description, sort_order) VALUES
    ('MISSION_TIME', '1', '10분 이내', '10분 이내 소요', 1),
('MISSION_TIME', '2', '10~20분', '10분에서 20분 소요', 2),
('MISSION_TIME', '3', '20~40분', '20분에서 40분 소요', 3),
('MISSION_TIME', '4', '40~60분', '40분에서 60분 소요', 4),
('MISSION_TIME', '5', '60분 이상', '60분 이상 소요', 5);

-- 성별
INSERT INTO code_detail (group_code, code, code_name, description, sort_order) VALUES
    ('GENDER', '1', '남성', '남성', 1),
('GENDER', '2', '여성', '여성', 2),
('GENDER', '0', '무관', '성별 무관', 0);

-- 심부름 카테고리
INSERT INTO code_detail (group_code, code, code_name, description, sort_order) VALUES
    ('MISSION_CATEGORY', 'delivery', '배달/장보기', '음식 배달, 장보기 등', 1),
('MISSION_CATEGORY', 'clean', '청소/집안일', '청소, 정리정돈 등', 2),
('MISSION_CATEGORY', 'installation', '설치/조립/운반', '가구 조립, 물건 운반 등', 3),
('MISSION_CATEGORY', 'together', '돌봄/동행', '반려동물 돌봄, 동행 서비스 등', 4),
('MISSION_CATEGORY', 'bug', '벌레/쥐잡기', '해충 방제 서비스', 5),
('MISSION_CATEGORY', 'role', '역할대행', '대리 출석, 줄서기 등', 6),
('MISSION_CATEGORY', 'lesson', '과외/공부', '학습 지도, 과외 등', 7),
('MISSION_CATEGORY', 'etc', '기타', '기타 심부름', 8);



-- 은행 코드 추가
INSERT INTO code_group (group_code, group_name, description) VALUES
    ('BANK_CODE', '은행 코드', '금융기관 코드');

INSERT INTO code_group (group_code, group_name, description) VALUES
    ('VEHICLE_TYPE', '탈것 코드', '탈것 코드');

INSERT INTO code_detail (group_code, code, code_name, sort_order) VALUES
    ('BANK_CODE', '001', 'KB국민은행', 1),
('BANK_CODE', '002', '한국산업은행', 2),
('BANK_CODE', '003', '기업은행', 3),
('BANK_CODE', '004', 'NH농협은행', 4),
('BANK_CODE', '011', '신한은행', 5),
('BANK_CODE', '020', '우리은행', 6),
('BANK_CODE', '023', 'SC제일은행', 7),
('BANK_CODE', '027', '씨티은행', 8),
('BANK_CODE', '032', '대구은행', 9),
('BANK_CODE', '045', '새마을금고', 10),
('BANK_CODE', '099', '카카오뱅크', 11);

-- 차량 종류 코드 추가
INSERT INTO code_detail (group_code, code, code_name, sort_order) VALUES
('VEHICLE_TYPE', '1', '승용차', 1),
('VEHICLE_TYPE', '2', '오토바이', 2),
('VEHICLE_TYPE', '3', '자전거', 3),
('VEHICLE_TYPE', '4', '도보', 4),
('VEHICLE_TYPE', '5', '기타', 5);



CREATE TABLE mission_location (
                                  location_seq INT AUTO_INCREMENT PRIMARY KEY COMMENT '위치 ID',
                                  mission_seq INT NOT NULL COMMENT '심부름 ID',
                                  location_type VARCHAR(10) NOT NULL COMMENT '위치 타입 (START, WAY, END)',
                                  postcode VARCHAR(10) COMMENT '우편번호',
                                  address1 VARCHAR(200) NOT NULL COMMENT '기본 주소 (도로명/지번)',
                                  address2 VARCHAR(200) COMMENT '상세 주소',
                                  latitude DECIMAL(10, 8) NOT NULL COMMENT '위도',
                                  longitude DECIMAL(11, 8) NOT NULL COMMENT '경도',
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  INDEX idx_mission_location (mission_seq, location_type),
                                  INDEX idx_location_coordinates (latitude, longitude),
                                  FOREIGN KEY (mission_seq) REFERENCES mission(mission_seq) ON DELETE CASCADE
) COMMENT '심부름 위치 정보';


INSERT INTO code_group (group_code, group_name, description) VALUES
    ('LOCATION_TYPE', '위치 타입 코드', '위치 타입 코드');

-- 위치 타입 코드 추가
INSERT INTO code_detail (group_code, code, code_name, description, sort_order) VALUES
('LOCATION_TYPE', 'START', '시작점', '심부름 시작 위치', 1),
('LOCATION_TYPE', 'WAY', '경유지', '심부름 경유 위치', 2),
('LOCATION_TYPE', 'END', '도착지', '심부름 도착 위치', 3);





create table mission_image
(
    mission_seq int                                   not null,
    image_name varchar(255)                          null,
    image_etx  varchar(20)                           null,
    image_file longblob                              null,
    create_dt  timestamp default current_timestamp() not null,
    constraint mission_image_ibfk_1
        foreign key (mission_seq) references zipkok.mission (mission_seq)
            on delete cascade
);

CREATE TABLE board_file (
    file_id INT AUTO_INCREMENT PRIMARY KEY,
    board_type VARCHAR(20) NOT NULL,  -- NOTICE, QNA 등
    board_seq INT NOT NULL,
    file_name VARCHAR(255),
    file_etx VARCHAR(20),
    board_file LONGBLOB,
    create_dt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);




CREATE TABLE sns_login(
   sns_seq int NOT NULL AUTO_INCREMENT COMMENT '소셜 로그인 시퀀스',
   member_seq INT NOT NULL COMMENT '회원 시퀀스',
   sns_type VARCHAR(20) NOT NULL COMMENT '제공자(kakao, naver, google 등)',
   sns_id VARCHAR(100) NOT NULL COMMENT '제공자에서 발급한 고유 ID',
   create_dt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
   update_dt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
   PRIMARY KEY (sns_seq),
   UNIQUE KEY `uk_provider_id` (`sns_type`, `sns_id`),
   KEY `idx_member_seq` (`member_seq`),
   CONSTRAINT `fk_social_login_member`
       FOREIGN KEY (`member_seq`)
           REFERENCES `member` (`member_seq`)
           ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='소셜 로그인 연동 테이블';