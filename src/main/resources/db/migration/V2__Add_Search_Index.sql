-- 1. 성능 최적화를 위한 데이터 반정규화 (가격 및 평점 컬럼 추가)
-- 이미 존재한다면 생략 가능하지만, 커버링 인덱스 구성을 위해 필수적입니다.
ALTER TABLE hotel ADD COLUMN min_price INT DEFAULT 0;
ALTER TABLE hotel ADD COLUMN average_rating DECIMAL(3,2) DEFAULT 0.0;

-- 2. 커버링 인덱스 및 복합 커서(No-Offset)를 위한 복합 인덱스 생성
-- 정렬 조건인 가격(min_price) 또는 평점(average_rating)을 선두 컬럼으로 배치
-- 고유 값인 ID를 포함하여 복합 커서 기반의 No-Offset 페이징을 지원
-- DTO Projection에 필요한 모든 컬럼을 포함하여 디스크 I/O 없이 인덱스만 스캔(Covering Index)하도록 구성

-- [가격 기준 정렬용 커버링 인덱스]
CREATE INDEX idx_hotel_search_price ON hotel (min_price, id, average_rating, hotel_name, image);

-- [평점 기준 정렬용 커버링 인덱스]
CREATE INDEX idx_hotel_search_rating ON hotel (average_rating DESC, id, min_price, hotel_name, image);
