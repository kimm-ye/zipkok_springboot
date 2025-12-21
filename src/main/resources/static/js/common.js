// common.js - 기본 동작
document.addEventListener('keypress', function(e) {
    console.log("엔터 입력");

    if (e.key === 'Enter' && !e.target.hasAttribute('data-no-enter')) {
        // textarea는 기본 제외
        if (e.target.tagName.toLowerCase() === 'textarea') {
            return;
        }

        // 기본: 폼 제출
        const form = e.target.closest('form');
        if (form && !form.hasAttribute('data-custom-enter')) {
            e.preventDefault();
            const submitBtn = form.querySelector('[type="submit"]');
            if (submitBtn) {
                submitBtn.click();
            }
        }
    }
});


// 로딩 오버레이 표시
function showLoading() {
    document.getElementById('loadingOverlay').classList.add('active');
}

// 로딩 오버레이 숨김
function hideLoading() {
    document.getElementById('loadingOverlay').classList.remove('active');
}

// 데이터 로딩 시뮬레이션
function handleLoadData() {
    showLoading();

    // 실제 프로젝트에서는 여기에 Ajax 호출이나 폼 제출 등을 넣으세요
    setTimeout(() => {
        hideLoading();
        alert('데이터 로딩 완료!');
    }, 3000);
}