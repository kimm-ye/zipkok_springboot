///////////////////////////////////////////
let noticeListEl;
let currentPage = 1;
const pageSize = 5;

// 페이지 로딩 시 초기 호출
document.addEventListener('DOMContentLoaded', () => {
    noticeListEl = document.getElementById("noticeList");
    loadIndexNotices(currentPage);
});

// 공지사항 로딩 되기 전에 스켈레톤 렌더링
function renderNoticeSkeleton(count = 5) {
    if (!noticeListEl) return;
    const skeletonItems = Array.from({ length: count })
        .map(() => `
            <div class="notice-item skeleton">
                <div class="notice-content">
                    <div class="skeleton-badge"></div>
                    <div class="skeleton-title"></div>
                </div>
                <div class="skeleton-date"></div>
            </div>
        `).join("");

    noticeListEl.innerHTML = `
        <div class="notice-list">
            ${skeletonItems}
        </div>
    `;
}

// 공지사항 불러오기
async function loadIndexNotices(page = 1) {
    renderNoticeSkeleton(); // 로딩 전 스켈레톤을 표시

    currentPage = page;

    try {
        const response = await fetch(`/zipkok/notice/history?page=${page}&size=${pageSize}`, {
            method: 'GET',
            credentials: 'same-origin'
        });
        const result = await response.json();

        if (result.success) {
            renderIndexNoticeList(result.data);    // data가 NoticeDTO 리스트
        } else {
            alert(result.message || "공지사항 조회 실패");
        }
    } catch (err) {
        console.error(err);
        // alert("공지사항 조회 중 오류가 발생했습니다.");
    }
}

// 메인화면 공지사항 리스트 출력
function renderIndexNoticeList(notices) {
    const noticeListEl = document.getElementById("noticeList");
    if (!noticeListEl) return;

    // 없는 경우 처리
    if (!notices || notices.length === 0) {
        noticeListEl.innerHTML = `
            <div class="notice-list">
                <div class="notice-item text-center">등록된 공지사항이 없습니다.</div>
            </div>
            <div class="notice-more">
                <button class="btn-more" onclick="location.href='./notice'">
                    더보기 <i class="fas fa-arrow-right"></i>
                </button>
            </div>
        `;
        return;
    }

    const itemsHtml = notices.map(notice => {
        // badge: I -> 중요, N -> 일반
        let badgeHtml = "";
        if (notice.noticeType === 'I') {
            badgeHtml = `<span class="notice-badge badge-important">중요</span>`;
        } else if (notice.noticeType === 'N') {
            badgeHtml = `<span class="notice-badge badge-normal">NEW</span>`;
        }

        return `
            <div class="notice-item" onclick="location.href='/zipkok/notice/detail?noticeId=${notice.noticeSeq}'">
                <div class="notice-content">
                    ${badgeHtml}
                    <span class="notice-title">${notice.noticeTitle}</span>
                </div>
                <span class="notice-date">${notice.createDt}</span>
            </div>
        `;
    }).join("");

    noticeListEl.innerHTML = `
        <div class="notice-list">
            ${itemsHtml}
        </div>
        <div class="notice-more">
            <button class="btn-more" onclick="location.href='./notice'">
                더보기 <i class="fas fa-arrow-right"></i>
            </button>
        </div>
    `;
}


// 심부름 카테고리 선택
function selectService(element, service) {
    //const serviceName = element.querySelector("h3").innerText;
    //alert(serviceName + " 카테고리가 선택되었습니다.");

    window.location.href = '/zipkok/mission/select?flag=' + service;
}

// 스크롤 애니메이션
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -100px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

document.querySelectorAll('.feature-card, .service-card, .notice-item').forEach(el => {
    el.style.opacity = '0';
    el.style.transform = 'translateY(30px)';
    el.style.transition = 'all 0.6s ease-out';
    observer.observe(el);
});