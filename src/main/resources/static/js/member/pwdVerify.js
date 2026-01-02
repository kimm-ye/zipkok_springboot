let attemptCount = 0;
const maxAttempts = 5;

// 비밀번호 보기/숨기기
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const toggleIcon = document.getElementById('toggleIcon');

    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        toggleIcon.className = 'fas fa-eye-slash';
    } else {
        passwordInput.type = 'password';
        toggleIcon.className = 'fas fa-eye';
    }
}

// 폼 제출
async function submitForm(e) {
    e.preventDefault();

    const password = document.getElementById('password').value;
    const submitBtn = document.getElementById('submitBtn');

    // 버튼 로딩 상태
    submitBtn.classList.add('loading');
    submitBtn.disabled = true;

    try {
        // 서버에 비밀번호 확인 요청
        const response = await fetch('/zipkok/member/mypage/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({password: password})
        });

        const result = await response.json();
        if (result.success) {
            // 성공 시 프로필 수정 페이지로 이동
            window.location.href = '/zipkok/member/mypage/modify';
        } else {
            // 실패 시 에러 표시
            showError('비밀번호가 일치하지 않습니다.');
            attemptCount++;

            // 시도 횟수 표시
            if (attemptCount >= 3) {
                showAttemptWarning();
            }

            // 최대 시도 횟수 초과
            if (attemptCount >= maxAttempts) {
                showError('최대 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.');
                submitBtn.disabled = true;
                setTimeout(() => {
                    window.location.href = '/zipkok/member/mypage';
                }, 3000);
            }
        }
    } catch (error) {
        console.error('Error:', error);
        showError('오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
        submitBtn.classList.remove('loading');
        if (attemptCount < maxAttempts) {
            submitBtn.disabled = false;
        }

        hideLoading();
    }
}

// 에러 메시지 표시
function showError(message) {
    const errorMessage = document.getElementById('errorMessage');
    const errorText = document.getElementById('errorText');
    const passwordInput = document.getElementById('password');

    errorText.textContent = message;
    errorMessage.classList.add('show');
    passwordInput.classList.add('error');
    passwordInput.value = '';
    passwordInput.focus();

    // 3초 후 에러 스타일 제거
    setTimeout(() => {
        passwordInput.classList.remove('error');
    }, 3000);
}

// 시도 횟수 경고 표시
function showAttemptWarning() {
    const attemptInfo = document.getElementById('attemptInfo');
    const attemptText = document.getElementById('attemptText');

    attemptText.textContent = `${attemptCount}회 실패. ${maxAttempts - attemptCount}회 남았습니다.`;
    attemptInfo.classList.add('show');
}

// Enter 키 처리
document.getElementById('password').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        document.getElementById('verifyForm').dispatchEvent(new Event('submit'));
    }
});


