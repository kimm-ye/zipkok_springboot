// =======================
// 로딩 유틸
// =======================

function showLoading() {
    document.getElementById('loadingOverlay')?.classList.add('active');
}

function hideLoading() {
    document.getElementById('loadingOverlay')?.classList.remove('active');
}

// =======================
// 메인 트리거
// =======================

// a 태그 이동
document.addEventListener('click', function (e) {
    const link = e.target.closest('a[href]');
    if (!link) return;

    if (
        link.target === '_blank' ||
        link.getAttribute('href')?.startsWith('#') ||
        link.getAttribute('href')?.startsWith('javascript:')
    ) {
        return;
    }

    showLoading();
});

// form submit
document.addEventListener('submit', function () {
    showLoading();
});

// =======================
// 페이지 진입 / 복원 시 hide
// =======================

// 최초 페이지 로드
window.addEventListener('load', function () {
    hideLoading();
});

// BFCache (뒤로/앞으로)
window.addEventListener('pageshow', function (event) {
    if (event.persisted) {
        hideLoading();
    }
});

// =======================
// 보조 트리거 (보험)
// =======================

window.addEventListener('beforeunload', function () {
    const overlay = document.getElementById('loadingOverlay');
    if (!overlay || overlay.classList.contains('active')) return;

    showLoading();
});
