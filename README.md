# 🏠 집콕 (Zipkok) - 온디맨드 심부름 서비스 플랫폼

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Security](https://img.shields.io/badge/Spring%20Security-6.0+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-0.12.3-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-6379-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-10.0+-003545?style=for-the-badge&logo=mariadb&logoColor=white)

**집에서 심부름을 콕! 집콕!** 🎯

온디맨드 심부름 서비스 플랫폼으로, 사용자가 필요한 심부름을 요청하고 헬퍼가 이를 수행할 수 있는 웹 애플리케이션입니다.

[🚀 프로젝트 바로가기](#프로젝트-소개) • [🛠️ 기술 스택](#기술-스택) • [📋 주요 기능](#주요-기능) • [🏗️ 아키텍처](#아키텍처)

</div>

---

## 📋 프로젝트 소개

**집콕(Zipkok)**은 일상생활에서 필요한 다양한 심부름을 온라인으로 요청하고 매칭할 수 있는 플랫폼입니다. 사용자는 배달, 청소, 설치, 동행 등 8가지 카테고리의 심부름을 요청할 수 있으며, 헬퍼는 이를 수행하여 수익을 창출할 수 있습니다.

### 🎯 프로젝트 목표
- **편의성**: 집에서 나가지 않고도 필요한 심부름을 쉽게 요청
- **안전성**: 심부름꾼 후기 시스템을 통한 신뢰성 확보
- **효율성**: 실시간 위치 서비스로 배달 과정 추적
- **경제성**: 헬퍼와 사용자 간의 효율적인 매칭 시스템

---

## 🛠️ 기술 스택

### **Backend**
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Security**: Spring Security 6.0+
- **Authentication**: JWT (JSON Web Token)
- **Database**: MariaDB
- **Cache**: Redis
- **ORM**: MyBatis

### **Frontend**
- **Template Engine**: Thymeleaf
- **CSS Framework**: Bootstrap
- **JavaScript**: jQuery
- **Animation**: WOW.js, Animate.css

### **DevOps & Tools**
- **Build Tool**: Gradle
- **Version Control**: Git
- **IDE**: IntelliJ IDEA / Eclipse
- **Database Tool**: MyBatis

---

## 🚀 주요 기능

### 👤 **사용자 관리**
- 회원가입/로그인 (일반 사용자, 헬퍼, 관리자)
- JWT 기반 인증 시스템
- 아이디/비밀번호 찾기
- 회원정보 관리

### 🎯 **심부름 서비스**
- **8가지 카테고리**:
  - 🚚 배달, 장보기
  - 🧹 청소, 집안일
  - 🔧 설치, 운반
  - 👥 동행, 돌봄
  - 🐛 벌레, 쥐잡기
  - 🎭 역할대행
  - 📚 과외, 알바
  - 📦 기타

### 🔐 **보안 및 인증**
- Spring Security 기반 인증/인가
- JWT 토큰 관리
- Redis를 활용한 세션 관리
- 비밀번호 BCrypt 암호화

### 📱 **사용자 인터페이스**
- 반응형 웹 디자인
- 애니메이션 효과 (WOW.js)
- 직관적인 서비스 선택 UI
- 모바일 친화적 디자인

---

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   (Thymeleaf)   │◄──►│   (Spring Boot) │◄──►│   (MariaDB)     │
│                 │    │                 │    │                 │
│   • Bootstrap   │    │   • Security    │    │   • User Data   │
│   • jQuery      │    │   • JWT         │    │   • Mission     │
│   • WOW.js      │    │   • MyBatis     │    │   • Board       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │     Redis       │
                       │   (Cache)       │
                       │                 │
                       │   • Session     │
                       │   • Token       │
                       └─────────────────┘
```

### **계층 구조**
- **Controller Layer**: 사용자 요청 처리
- **Service Layer**: 비즈니스 로직 처리
- **DAO Layer**: 데이터 접근 객체
- **DTO Layer**: 데이터 전송 객체

---

## 📁 프로젝트 구조

```
src/main/java/com/kosmo/zipkok/
├── config/                 # 설정 클래스
│   ├── SecurityConfig.java        # Spring Security 설정
│   ├── JwtAuthenticationFilter.java # JWT 인증 필터
│   └── RedisConfig.java           # Redis 설정
├── controller/            # 컨트롤러
│   ├── MainController.java        # 메인 페이지
│   ├── MemberController.java      # 회원 관리
│   ├── LoginController.java       # 로그인/인증
│   └── BoardController.java      # 게시판
├── service/              # 서비스 계층
│   ├── MemberService.java        # 회원 서비스
│   ├── CustomUserDetailsService.java # Spring Security 사용자 서비스
│   └── impl/
│       └── MemberServiceImpl.java # 회원 서비스 구현
├── dao/                  # 데이터 접근 계층
│   ├── MemberDAO.java           # 회원 데이터 접근
│   └── AuthDAO.java            # 인증 데이터 접근
├── dto/                  # 데이터 전송 객체
│   ├── MemberDTO.java          # 회원 정보
│   ├── BoardDTO.java           # 게시판 정보
│   └── TokenDTO.java           # 토큰 정보
└── util/                 # 유틸리티
    └── JwtUtil.java            # JWT 유틸리티
```

---

## 🚀 실행 방법

### **사전 요구사항**
- Java 17+
- MariaDB 10.0+
- Redis 6.0+
- Gradle 7.0+

### **1. 저장소 클론**
```bash
git clone https://github.com/yourusername/zipkok.git
cd zipkok
```

### **2. 데이터베이스 설정**
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/zipkok
    username: your_username
    password: your_password
```

### **3. Redis 설정**
```yaml
redis:
  host: localhost
  port: 6379
```

### **4. 애플리케이션 실행**
```bash
./gradlew bootRun
```

### **5. 접속**
- **URL**: http://localhost:8080/zipkok
- **포트**: 8080

---

## 🔧 주요 설정

### **Spring Security 설정**
- JWT 기반 인증
- 세션리스 아키텍처
- CSRF 보호 비활성화
- 정적 리소스 접근 허용

### **JWT 설정**
- **Secret Key**: 64자 이상 보안 키
- **Expiration**: 30분 (1,800,000ms)
- **Algorithm**: HMAC-SHA256

### **데이터베이스 설정**
- **Driver**: MariaDB JDBC Driver
- **Encoding**: UTF-8
- **MyBatis**: Camel Case 자동 변환

---

## 📊 프로젝트 특징

### **🔒 보안성**
- Spring Security 기반 강력한 인증/인가
- JWT 토큰을 통한 안전한 세션 관리
- BCrypt를 이용한 비밀번호 암호화

### **⚡ 성능**
- Redis 캐싱을 통한 빠른 응답 속도
- MyBatis를 이용한 효율적인 데이터베이스 쿼리
- 정적 리소스 최적화

### **📱 사용자 경험**
- 반응형 웹 디자인으로 모든 디바이스 지원
- 직관적인 UI/UX 설계
- 부드러운 애니메이션 효과

### **🔄 확장성**
- 계층별 분리로 유지보수성 향상
- 인터페이스 기반 설계로 확장 용이
- 설정 파일을 통한 환경별 관리

---

## 🎯 향후 개발 계획

### **Phase 1** (현재)
- ✅ 기본 회원 관리 시스템
- ✅ JWT 인증 시스템
- ✅ 심부름 요청 기본 기능

### **Phase 2** (계획)
- 🔄 실시간 채팅 시스템
- 🔄 결제 시스템 연동
- 🔄 모바일 앱 개발

### **Phase 3** (계획)
- 🔄 AI 기반 매칭 알고리즘
- 🔄 리뷰 및 평점 시스템
- 🔄 통계 및 분석 대시보드

---

## 👨‍💻 개발자 정보

**개발자**: [Your Name]  
**이메일**: [your.email@example.com]  
**GitHub**: [@yourusername](https://github.com/yourusername)

### **개발 기간**
- **시작일**: 2024년 1월
- **완료일**: 2024년 12월 (진행 중)

### **개발 환경**
- **OS**: Windows 10
- **IDE**: IntelliJ IDEA / Eclipse
- **Database**: MariaDB
- **Cache**: Redis

---

## 📝 라이선스

이 프로젝트는 **MIT License** 하에 배포됩니다.

---

## 🙏 감사의 말

이 프로젝트를 통해 Spring Boot, Spring Security, JWT, Redis 등 다양한 기술을 학습하고 실무에 적용할 수 있었습니다. 특히 보안과 성능 최적화에 대한 깊은 이해를 얻을 수 있었습니다.

---

<div align="center">

**⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요! ⭐**

**집에서 심부름을 콕! 집콕!** 🏠✨

</div>
