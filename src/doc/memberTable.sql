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
create or replace table board_file
(
    file_id    int auto_increment
        primary key,
    board_type varchar(20)                           not null,
    board_seq  int                                   not null,
    file_name  varchar(255)                          null,
    file_etx   varchar(20)                           null,
    board_file longblob                              null,
    create_dt  timestamp default current_timestamp() not null
);

create or replace table board_notice
(
    notice_seq     int auto_increment
        primary key,
    notice_type    char                                 null comment '공지사항 타입(''I'' : 중요공지 ''N'' : 일반공지',
    notice_title   varchar(100)                         not null,
    notice_content varchar(2000)                        not null,
    member_seq     int                                  not null,
    notice_date    datetime default current_timestamp() not null,
    notice_view    int      default 0                   not null
);

create or replace table code_group
(
    group_code  varchar(20)                           not null comment '그룹 코드'
        primary key,
    group_name  varchar(100)                          not null comment '그룹명',
    description text                                  null comment '설명',
    use_yn      char      default 'Y'                 null comment '사용여부',
    created_at  timestamp default current_timestamp() not null comment '생성일시',
    updated_at  timestamp default current_timestamp() not null on update current_timestamp() comment '수정일시'
)
    comment '공통 코드 그룹';

create or replace table code_detail
(
    group_code  varchar(20)                           not null comment '그룹 코드',
    code        varchar(20)                           not null comment '코드',
    code_name   varchar(100)                          not null comment '코드명',
    description text                                  null comment '설명',
    sort_order  int       default 0                   null comment '정렬순서',
    use_yn      char      default 'Y'                 null comment '사용여부',
    created_at  timestamp default current_timestamp() not null comment '생성일시',
    updated_at  timestamp default current_timestamp() not null on update current_timestamp() comment '수정일시',
    primary key (group_code, code),
    constraint code_detail_ibfk_1
        foreign key (group_code) references code_group (group_code)
            on delete cascade
)
    comment '공통 코드 상세';

create or replace index idx_code_detail_group_use
    on code_detail (group_code, use_yn);

create or replace index idx_code_detail_sort
    on code_detail (group_code, sort_order);

create or replace table member
(
    member_seq      int auto_increment
        primary key,
    member_id       varchar(20)                          not null,
    member_pass     varchar(60)                          null,
    member_name     varchar(50)                          not null,
    member_email    varchar(50)                          not null,
    member_age      varchar(10)                          not null,
    member_gender   int                                  not null,
    member_phone    varchar(20)                          not null,
    member_missionN int                                  null,
    member_status   int      default 1                   not null,
    member_useYn    char     default 'Y'                 not null,
    create_dt       datetime default current_timestamp() null,
    constraint member_id
        unique (member_id),
    constraint uk_member_email
        unique (member_email)
);

create or replace table helper
(
    member_seq       int          not null
        primary key,
    member_bank      char(3)      null,
    member_account   varchar(50)  null,
    member_vehicle   int          null,
    member_introduce varchar(500) null,
    member_review    int          null,
    member_missionC  int          null,
    member_point     int          null,
    constraint helper_ibfk_1
        foreign key (member_seq) references member (member_seq)
            on delete cascade
);

create or replace table helper_image
(
    image_seq  int auto_increment
        primary key,
    member_seq int                                   not null,
    image_name varchar(255)                          null,
    image_etx  varchar(20)                           null,
    image_file longblob                              null,
    create_dt  timestamp default current_timestamp() not null,
    constraint helper_image_ibfk_1
        foreign key (member_seq) references helper (member_seq)
            on delete cascade
);

create or replace index member_seq
    on helper_image (member_seq);

create or replace table mission
(
    mission_seq            int auto_increment
        primary key,
    member_seq             int           not null,
    mission_category       varchar(50)   not null,
    mission_title          varchar(50)   not null,
    mission_content        varchar(300)  not null,
    mission_gender         int           null,
    helper_seq             int           null,
    mission_reservation    char          not null,
    mission_reservation_dt varchar(50)   null,
    mission_time           varchar(20)   null,
    mission_cost           int           not null,
    mission_status         int default 1 not null
);

create or replace index idx_mission_helper_seq
    on mission (helper_seq);

create or replace index idx_mission_member_seq
    on mission (member_seq);

create or replace table mission_image
(
    mission_seq int                                   not null,
    image_name  varchar(255)                          null,
    image_etx   varchar(20)                           null,
    image_file  longblob                              null,
    create_dt   timestamp default current_timestamp() not null,
    constraint mission_image_ibfk_1
        foreign key (mission_seq) references mission (mission_seq)
            on delete cascade
);

create or replace table mission_location
(
    location_seq  int auto_increment comment '위치 ID'
        primary key,
    mission_seq   int                                   not null comment '심부름 ID',
    location_type varchar(10)                           not null comment '위치 타입 (START, WAY, END)',
    postcode      varchar(10)                           null comment '우편번호',
    address1      varchar(200)                          not null comment '기본 주소 (도로명/지번)',
    address2      varchar(200)                          null comment '상세 주소',
    latitude      decimal(10, 8)                        not null comment '위도',
    longitude     decimal(11, 8)                        not null comment '경도',
    created_at    timestamp default current_timestamp() not null,
    updated_at    timestamp default current_timestamp() not null on update current_timestamp(),
    constraint mission_location_ibfk_1
        foreign key (mission_seq) references mission (mission_seq)
            on delete cascade
)
    comment '심부름 위치 정보';

create or replace index idx_location_coordinates
    on mission_location (latitude, longitude);

create or replace index idx_mission_location
    on mission_location (mission_seq, location_type);

create or replace table qboard
(
    num        int auto_increment
        primary key,
    title      varchar(30)                          not null,
    content    varchar(200)                         not null,
    id         varchar(20)                          not null,
    postdate   datetime default current_timestamp() not null,
    visitcount int      default 0                   not null
);

create or replace table qreview
(
    num     int          not null
        primary key,
    content varchar(200) not null,
    id      varchar(20)  not null
);

create or replace table review
(
    review_num     int auto_increment
        primary key,
    mission_num    int                                  not null,
    review_id      varchar(10)                          not null,
    review_content varchar(200)                         not null,
    review_point   int                                  not null,
    review_date    datetime default current_timestamp() not null
);

create or replace table sns_login
(
    sns_seq    int auto_increment comment '소셜 로그인 시퀀스'
        primary key,
    member_seq int                                  not null comment '회원 시퀀스',
    sns_type   varchar(20)                          not null comment '제공자(kakao, naver, google 등)',
    sns_id     varchar(100)                         not null comment '제공자에서 발급한 고유 ID',
    create_dt  datetime default current_timestamp() null comment '생성일시',
    update_dt  datetime default current_timestamp() null on update current_timestamp() comment '수정일시',
    constraint uk_provider_id
        unique (sns_type, sns_id),
    constraint fk_social_login_member
        foreign key (member_seq) references member (member_seq)
            on delete cascade
)
    comment '소셜 로그인 연동 테이블' collate = utf8mb4_unicode_ci;

create or replace index idx_member_seq
    on sns_login (member_seq);

