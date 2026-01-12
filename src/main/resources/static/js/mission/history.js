// ===== 전역 상태 관리 =====
let currentStatus = '';

// 심부름 상태 필터 활성화 표시
document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    currentStatus = urlParams.get('missionStatus');

    document.getElementById('searchInput')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            searchMissions();
        }
    });
});

// ===== 검색 =====
function searchMissions() {
    loadMissions(1);  // 검색하면 1페이지로
}

// ===== 상태 필터 =====
function filterByStatus(status) {
    // active 토글
    document.querySelectorAll('.status-chip').forEach(chip => {
        chip.classList.remove('active');
    });
    event.target.classList.add('active');

    currentStatus = status;
    loadMissions(1);  // 필터 변경하면 1페이지로
}

// 심부름 상태 필터에 따른 심부름 요청 내역 조회를 위한 url 세팅
async function loadMissions(page) {

    showSkeleton();

    const flag = document.getElementById('flag').value;
    const search = document.getElementById('search').value;
    const sort = document.getElementById('sortSelect').value;

    // URL 파라미터 생성
    const params = new URLSearchParams({
        flag: flag,
        page: page,
        search: search || '',
        missionStatus: currentStatus,
        sort: sort,
        isAjax: true
    });

    // URL 업데이트
    const newUrl = `/zipkok/mission/history?${params.toString().replace('&isAjax=true', '')}`;
    window.history.pushState({}, '', newUrl);

    try {
        const response = await fetch(`/zipkok/mission/history?${params}`);

        if (!response.ok) {
            throw new Error('서버 응답 오류');
        }

        const html = await response.text();
        document.getElementById('missionListContainer').innerHTML = html;

    } catch (error) {
        console.error('목록 로드 실패:', error);
        alert('목록을 불러오는데 실패했습니다.');
    }
}


// 심부름 상세 페이지로 이동
function goToMissionDetail(element) {
    const missionSeq = element.getAttribute('data-mission-seq');
    location.href = `/zipkok/mission/request/detail?missionSeq=${missionSeq}`;
}

// ===== 페이지 변경 =====
function changePage(page) {
    loadMissions(page);

    // 페이지 변경 시 스크롤 상단으로 (선택사항)
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ㅅ
function showSkeleton() {
    const skeleton = document.getElementById('skeletonTemplate').innerHTML;
    document.getElementById('missionListContainer').innerHTML = skeleton;
}
