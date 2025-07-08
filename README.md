# Runways 🏃‍♀️  

사용자의 컨디션과 날씨에 맞춘  
**Spring Boot 기반 러닝·도보 코스 추천 시스템**

---

## 📌 프로젝트 개요

**Runways**는 사용자 컨디션, 날씨, 거리, 경사도 등을 반영하여  
맞춤형 러닝·산책 코스를 추천하는 **Spring Boot 기반 백엔드 시스템**입니다.

- 대한민국 전역의 도보 도로 및 경사도 데이터를 기반으로 실제 난이도 반영
- 프론트엔드 앱(React Native)과 연동되어 사용자 피드백 및 코스 시각화 제공

---
---

## ⚙️ 프로젝트 구조

![image](https://github.com/user-attachments/assets/64a9c359-957f-4053-b0f8-0960acb0a306)

---
## 🛠 사용 기술

| 구분 | 기술 |
|------|------|
| Language | Kotlin |
| Framework | Spring Boot |
| ORM | Spring Data JPA (Hibernate) |
| DB | PostgreSQL + PostGIS |
| 분석 | Python |
| 지도 데이터 | OpenStreetMap, GeoTIFF |

---

## 🔧 데이터 소스 및 처리

| 종류 | 설명 |
|------|------|
| GeoTIFF | 대한민국 고도 데이터 → 경사도 분석 |
| OpenStreetMap | 도보 도로, 산책로 데이터 |
| Weather API | 기온, 강수량 등 실시간 날씨 데이터 |
| 사용자 입력 | 컨디션, 거리, 태그 등 추천 조건 |

---

## 🧠 추천 알고리즘 개요

1. 사용자 활동 로그 수집  
   - 코스 검색, 북마크, 사용 등의 행동 기록을 저장
     
2. 태그 선호도 계산  
   - 태그별 사용 빈도와 최근 사용 시점을 기반으로 가중치 점수 산정  
   - 시간 가중치 적용 (최근 사용된 태그에 높은 점수 부여)
     
3. 날씨 및 컨디션 필터링  
   - 현재 위치의 날씨 정보(기온, 강수, 미세먼지 등)와 사용자 컨디션에 따라  
     부적합한 코스 유형(예: 더운 날 경사 많은 코스)을 사전 필터링

4. 공간 필터링 (PostGIS)  
   - 사용자의 위치와 거리 범위에 따라 주변 코스 후보 추출

5. 난이도 및 태그 기반 평가  
   - 각 코스의 경사도(사전 계산된 DB 값)와 태그를 기반으로  
     사용자 선호도 점수와 결합하여 최종 점수 산정

---

## 📂 디렉토리 구조

```
Runways-Backend/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── syntax/backend/runways/
│   │   │       ├── config/         # 소켓, Redis, OAuth 등 전역 설정 관리
│   │   │       ├── controller/     # API 요청을 처리하는 컨트롤러 클래스
│   │   │       ├── dto/            # 클라이언트와 주고받는 데이터 전송 객체 (DTO)
│   │   │       ├── entity/         # DB 테이블과 매핑되는 JPA 엔티티 클래스
│   │   │       ├── event/          # 도메인 이벤트 기반 비동기 후처리 로직 (@TransactionalEventListener 등)
│   │   │       ├── interceptor/    # 인증, 로깅 등을 처리하는 요청 인터셉터 클래스
│   │   │       ├── repository/     # 데이터베이스 접근을 위한 JPA 레포지토리 인터페이스
│   │   │       ├── scheduler/      # 정기 작업(Scheduler) 처리 클래스 (예: 코스 이용 통계 집계)
│   │   │       ├── service/        # 핵심 비즈니스 로직을 담당하는 서비스 계층
│   │   │       └── util/           # 거리 계산, 사용자 정보 처리 등 공통 유틸리티 클래스
│   │   ├── resources/
│   │   │   └── application.properties  # Spring Boot 애플리케이션 설정 파일
├── build.gradle.kts                 # Gradle 빌드 설정 파일 (Kotlin DSL)
├── settings.gradle.kts              # Gradle 프로젝트 구성 설정
└── README.md                        # 프로젝트 소개 및 사용법 설명 문서

```

---

## 🚀 실행 방법

Runways 백엔드는 Spring Boot(Kotlin) 기반으로 동작하며, `.env`와 `application.properties`를 기반으로 설정된 환경변수들을 사용합니다.

---

### 1️⃣ 필수 세팅

- ✅ **Java 17 이상**
- ✅ **Gradle (Wrapper 사용 가능)**
- ✅ **PostgreSQL + PostGIS**
- ✅ **Redis** 

---

### 2️⃣ `.env` 파일 생성

루트 디렉토리에 `.env` 파일을 생성하고 아래와 같이 설정합니다:

```env
# server
server-port=8606

# DB Configuration
DB_URL=jdbc:postgresql://<DB_HOST>:<PORT>/<DB_NAME>
DB_USERNAME=<DB_USER>
DB_PASSWORD=<DB_PASSWORD>

# Google Oauth
google-client-id=<GOOGLE_CLIENT_ID>
google-client-secret=<GOOGLE_CLIENT_SECRET>
google-redirect-uri=https://<DOMAIN>/login/oauth2/code/google

# Kakao Oauth
kakao-client-id=<KAKAO_CLIENT_ID>
kakao-client-secret=<KAKAO_CLIENT_SECRET>
kakao-redirect-uri=https://<DOMAIN>/login/oauth2/code/kakao

# JWT
jwt-expiration-time=604800000
jwt-secret=<JWT_SECRET_KEY>

# Weather & Air APIs
api-forecast-weather-url=http://apis.data.go.kr/...
api-now-weather-url=http://apis.data.go.kr/...
api-finedust-url=http://apis.data.go.kr/...
api-key=<OPEN_API_KEY>

# Naver API
naver-client-id=<NAVER_CLIENT_ID>

# Redis 설정
redis-host=<REDIS_HOST>
redis-port=6379
redis-password=<REDIS_PASSWORD>

# LLM 서버
llm-server-url=https://<LLM_SERVER_URL>:<PORT>/api
```

---

### 3️⃣ PostgreSQL 및 PostGIS 설정

```sql
CREATE DATABASE runways;
\c runways
CREATE EXTENSION postgis;
```

---

### 4️⃣ Redis 실행

```bash
docker run -d -p 6379:6379 --name runways-redis redis
```

---

### 5️⃣ 서버 실행

```bash
./gradlew bootRun
```

## 📄 라이선스

이 프로젝트는 [MIT License](./LICENSE)를 따릅니다.

---

## 📬 문의

- 담당자: [이석진](seokjin6635@gmail.com)
