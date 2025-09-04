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


document.addEventListener('DOMContentLoaded', function() {
    // 이메일 도메인 목록
    const domains = ['gmail.com', 'naver.com', 'daum.net', 'kakao.com', 'hotmail.com'];

    // 이메일 select 찾기
    const emailSelect = document.querySelector('select[name="email_check"]');

    if (emailSelect) {
        // 기본 옵션들 추가
        domains.forEach(domain => {
            const option = document.createElement('option');
            option.value = domain;
            option.textContent = domain;
            emailSelect.appendChild(option);
        });

        // 수정 모드일 때 기존 값 선택
        if (window.memberData && window.memberData.info && window.memberData.info.memberEmail) {
            const email = window.memberData.info.memberEmail;
            const domain = email.split('@')[1]; // @ 뒤의 도메인 추출

            // 해당 도메인으로 선택
            emailSelect.value = domain;

            // email_input 함수 호출해서 email_2 필드도 채우기
            email_input(emailSelect.form);
        }
    }
});


// 이메일 도메인 선택 처리
function email_input(form) {
    const domain = form.email_check.value;
    if (domain === '') {
        form.email_2.value = '';
    } else if (domain === '1') {
        form.email_2.readOnly = false;
        form.email_2.value = '';
        form.email_2.focus();
    } else {
        form.email_2.value = domain;
        form.email_2.readOnly = true;
    }
}