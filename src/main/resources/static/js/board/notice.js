///////////////////////////////////////////
let noticeListEl;
let paginationEl;

let currentPage = 1;
const pageSize = 10;

// 페이지 로딩 시 초기 호출
document.addEventListener('DOMContentLoaded', () => {
    noticeListEl = document.getElementById("noticeList");
    paginationEl = document.querySelector(".pagination");
    loadNotices(currentPage);
});

// 공지사항 불러오기
async function loadNotices(page = 1) {
    currentPage = page;
    try {
        showLoading();

        const response = await fetch(`/zipkok/notice/history?page=${page}&size=${pageSize}`, {
            method: 'GET',
            credentials: 'same-origin'
        });
        const result = await response.json();

        if (result.success) {
            renderNoticeList(result.data);    // data가 NoticeDTO 리스트
            renderPagination(result.paging);  // paging 정보
        } else {
            alert(result.message || "공지사항 조회 실패");
        }
    } catch (err) {
        console.error(err);
        alert("공지사항 조회 중 오류가 발생했습니다.");
    } finally {
        hideLoading();
    }
}

// 공지사항 리스트 렌더링
function renderNoticeList(notices) {
    noticeListEl.innerHTML = "";

    if (!notices || notices.length === 0) {
        noticeListEl.innerHTML = `<div class="notice-item">등록된 공지사항이 없습니다.</div>`;
        return;
    }

    notices.forEach(notice => {
        const item = document.createElement("div");
        item.className = "notice-item";
        item.onclick = () => location.href = `/zipkok/notice/detail?noticeId=${notice.noticeSeq}`;

        // noticeType에 따른 badge 결정
        let badgeHTML = '';
        if (notice.noticeType === 'I') {
            badgeHTML = '<span class="badge-preview-item badge-important">중요</span>';
        } else if (notice.noticeType === 'N') {
            badgeHTML = '<span class="badge-preview-item badge-normal">일반</span>';
        }

        item.innerHTML = `
            <i class="fas fa-bullhorn notice-icon"></i>
            <div class="notice-content">
                <div class="notice-title">
                    ${badgeHTML} 
                    <span>${notice.noticeTitle}</span>
                </div>
                <div class="notice-meta">
                    <span><i class="fas fa-calendar"></i> ${notice.createDt}</span>
                    <span><i class="fas fa-eye"></i> ${notice.noticeView}</span>
                </div>
            </div>
        `;
        noticeListEl.appendChild(item);
    });
}

// 페이징 렌더링
function renderPagination(paging) {
    paginationEl.innerHTML = "";

    const createBtn = (text, page, disabled = false, active = false) => {
        const btn = document.createElement("button");
        btn.className = `page-btn ${active ? "active" : ""}`;
        btn.disabled = disabled;
        btn.textContent = text;
        btn.onclick = () => loadNotices(page);
        return btn;
    };

    // 이전
    paginationEl.appendChild(createBtn("«", paging.page - 1, !paging.hasPrev));

    // 페이지 번호
    for (let p = paging.startPage; p <= paging.endPage; p++) {
        paginationEl.appendChild(createBtn(p, p, false, p === paging.page));
    }

    // 다음
    paginationEl.appendChild(createBtn("»", paging.page + 1, !paging.hasNext));
}




// 검색
document.getElementById('searchInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') searchNotice();
});

function searchNotice() {
    const keyword = document.getElementById('searchInput').value;
    if (keyword.trim()) {
        alert('"' + keyword + '"로 검색합니다.');
    } else {
        alert('검색어를 입력해주세요.');
    }
}

// 검색어 입력창에서 엔터클릭시 검색어로 공지사항을 검색한다.
document.getElementById('searchInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        searchNotice();
    }
});


// 패이지 이동
function changePage(page) {
    const buttons = document.querySelectorAll('.page-btn');
    buttons.forEach(btn => btn.classList.remove('active'));

    if (typeof page === 'number') {
        event.target.classList.add('active');
        alert(page + '페이지로 이동합니다.');
    } else if (page === 'prev') {
        alert('이전 페이지로 이동합니다.');
    } else if (page === 'next') {
        alert('다음 페이지로 이동합니다.');
    }
}



