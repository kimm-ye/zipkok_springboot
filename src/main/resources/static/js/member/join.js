document.addEventListener('DOMContentLoaded', function() {

    // 회원정보 수정인 경우 thymeleaf로 정보 전달
    if (window.memberData && window.memberData.isModify) {
        // 회원 유형에 따른 헬퍼 필드 표시
        const memberRole = window.memberData.info.memberRole;
        if (memberRole === 2) {
            document.getElementById('helper_fields').style.display = 'block';
        }

        // 은행이 선택되어 있으면 계좌번호 입력 활성화
        const bankSelect = document.getElementById('memberBank');
        const accountInput = document.getElementById('account');
        if (bankSelect && bankSelect.value && accountInput) {
            accountInput.readOnly = false;
        }

        document.getElementById("attachFile").addEventListener("change", previewImage);
    }

    // 가입 유형 토글설정
    toggleMemberType();

    // 아이디 글자수 && 중복확인 검사
    idDoubleCheck();

    // 비밀번호 글자수 && 유효성 검사
    pwdDoubleCheck();

    // 이메일 도메인 목록 TODO 나중에 db에서 가져오는 걸로 변경하자
    const domains = ['gmail.com', 'naver.com', 'kakao.com', 'nate.com'];

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

// 아이디 글자수 검사 && 중복확인 검사
function idDoubleCheck() {
    let debounceTimer = null;
    let currentController = null;

    const memberInput = document.getElementById("memberId");
    if (memberInput) {
        memberInput.addEventListener("input", function(e) {
            // 영문만 입력되도록한다.
            e.target.value = e.target.value.replace(/[^a-zA-Z]/g, "");
            // 공백을 제거한다.
            const memberId = e.target.value.trim();

            // 이전 요청들 정리
            clearTimeout(debounceTimer);
            if (currentController) currentController.abort();

            // 4글자 미만이면 체크 안함
            if (memberId.length < 4) {
                showMsg("아이디는 4글자 이상 입력하세요", "gray");
                return;
            } else if(memberId.length > 13) {
                showMsg("아이디는 13글자 이하로 입력하세요", "gray");
                return;
            }

            // ✅ 입력 즉시 "확인 중" 메시지 표시
            showMsg("중복 확인 중...", "blue");

            // 100ms 디바운스 후 서버 요청
            debounceTimer = setTimeout(async () => {
                try {
                    currentController = new AbortController();

                    const response = await fetch(`./join/check?memberId=${memberId}`, {
                        method: "POST",
                        signal: currentController.signal,
                    });
                    const data = await response.json();

                    if (data.exists) {
                        showMsg("이미 사용 중인 아이디입니다 ❌", "red");
                    } else {
                        showMsg("사용 가능한 아이디입니다 ✅", "green");
                    }
                } catch (err) {
                    if (err.name !== "AbortError") {
                        showMsg("중복 확인 중 오류 발생", "red");
                    }
                }
            }, 100);
        });
    }
}

// 아이디 중복체크 여부 메세지 표시
function showMsg(msg, color) {
    const msgEl = document.getElementById("idCheckMsg");
    msgEl.innerText = msg;
    msgEl.style.display = "block";
    msgEl.style.color = color;
}


// 비밀번호 글자수 && 유효성 검사
function pwdDoubleCheck() {

    const memberPass = document.getElementById('memberPass');
    const memberPass2 = document.getElementById('memberPass2');
    const passwordHint = document.getElementById('passwordHint');
    const confirmHint = document.getElementById('confirmHint');
    const alertTxt = document.getElementById('alertTxt');

    memberPass.addEventListener('input', function() {
        const password = this.value;

        if (password.length === 0) {
            passwordHint.textContent = '8자 이상, 특수문자(!@#$%^&*), 숫자, 영문자 포함 필수';
            passwordHint.className = 'password-hint';
            this.classList.remove('valid', 'invalid');
            alertTxt.style.display = 'none';
            return;
        }

        const result = validatePassword(password);

        if (result.isValid) {
            passwordHint.textContent = '✓ 사용 가능한 비밀번호입니다';
            passwordHint.className = 'password-hint success';
            this.classList.add('valid');
            this.classList.remove('invalid');
            alertTxt.style.display = 'none';
        } else {
            let errors = [];
            if (!result.length) errors.push('8자 이상');
            if (!result.special) errors.push('특수문자');
            if (!result.number) errors.push('숫자');
            if (!result.letter) errors.push('영문자');

            passwordHint.textContent = '✗ 필요: ' + errors.join(', ');
            passwordHint.className = 'password-hint error';
            this.classList.add('invalid');
            this.classList.remove('valid');
            alertTxt.style.display = 'block';
        }

        if (memberPass2.value.length > 0) checkPasswordMatch();
    });

    // 비밀번호 확인
    memberPass2.addEventListener('input', checkPasswordMatch);

    function checkPasswordMatch() {
        const password = memberPass.value;
        const confirmPwd = memberPass2.value;

        if (confirmPwd.length === 0) {
            confirmHint.textContent = '';
            memberPass2.classList.remove('valid', 'invalid');
            return;
        }

        const result = validatePassword(password);

        if (!result.isValid) {
            confirmHint.textContent = '✗ 먼저 유효한 비밀번호를 입력하세요';
            confirmHint.className = 'password-hint error';
            memberPass2.classList.add('invalid');
            return;
        }

        if (password === confirmPwd) {
            confirmHint.textContent = '✓ 비밀번호가 일치합니다';
            confirmHint.className = 'password-hint success';
            memberPass2.classList.add('valid');
            memberPass2.classList.remove('invalid');
        } else {
            confirmHint.textContent = '✗ 비밀번호가 일치하지 않습니다';
            confirmHint.className = 'password-hint error';
            memberPass2.classList.add('invalid');
            memberPass2.classList.remove('valid');
        }
    }
}


// 비밀번호 유효성 검사 (각각 true/false를 반환)
function validatePassword(password) {
    return {
        isValid: password.length >= 8 &&
            /[!@#$%^&*(),.?":{}|<>]/.test(password) &&
            /[0-9]/.test(password) &&
            /[a-zA-Z]/.test(password),
        length: password.length >= 8,
        special: /[!@#$%^&*(),.?":{}|<>]/.test(password),
        number: /[0-9]/.test(password),
        letter: /[a-zA-Z]/.test(password)
    };
}

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

// 은행 선택시 계좌번호 입력 활성화 (통합 폼용)
function input_bank(frm) {
    var bank = frm.memberBank.value;

    if (bank === "") {
        frm.memberAccount.readOnly = true;
        frm.memberAccount.value = '';
    }
    else {
        frm.memberAccount.readOnly = false;
        frm.memberAccount.focus();
    }
}


/*===========================================================================*/

// 회원가입 유효성 검사
function joinValidate(form) {

    const isModifyMode = window.memberData && window.memberData.isModify;
    if (!isModifyMode) {
        idDoubleCheck();
    }

    // 비밀번호 검사 (수정 모드에서는 입력했을 때만)
    const hasPassword = form.memberPass.value || form.memberPass2.value;
    if (!isModifyMode || hasPassword) {
        pwdDoubleCheck();
    }

    // 통합 폼에서 헬퍼 선택시 추가 유효성 검사
    var memberRoleInput = document.querySelector('input[name="memberRole"]:checked');
    if (memberRoleInput && memberRoleInput.value === "2") {
        if (form.memberBank && !form.memberBank.value) {
            alert("은행을 선택해주세요.");
            return false;
        }
        if (form.memberAccount && !form.memberAccount.value) {
            alert("계좌번호를 입력해주세요.");
            return false;
        }
        if (form.memberIntroduce && !form.memberIntroduce.value) {
            alert("자기소개를 입력해주세요.");
            return false;
        }
    }
    return true; // 모든 검증 통과
}


// 폼 제출을 fetch로 처리하는 공통 함수
async function submitFormWithFetch(form) {
    event.preventDefault(); // 기본 submit 방지
    // 유효성 검사를 진행한다.
    if (!joinValidate(form)) {
        return false;
    }

    try {
        showLoading();

        const formData = new FormData(form);

        const email1 = formData.get('email_1');
        const email2 = formData.get('email_2');
        if (email1 && email2) {
            formData.set('memberEmail', email1 + '@' + email2);
            formData.delete('email_1');
            formData.delete('email_2');
        }

        // ✅ isModify 여부에 따라 url 분기
        let url = "/zipkok/member/join/action"; // 신규가입
        let method = "POST";
        if (window.memberData && window.memberData.isModify) {
            url = "/zipkok/member/mypage/modify/action"; // 수정
            method = "PATCH"
        }

        const response = await fetch(url, {
            method: method,
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            alert(result.message);
            if (result.redirectUrl) {
                window.location.href = result.redirectUrl;
            }
        } else {
            alert(result.message);
        }

    } catch (error) {
        console.error('Error:', error);
        alert('처리 중 오류가 발생했습니다.');
        alert('error :' + error);
    } finally {
        hideLoading();
    }
}

// 회원 유형 변경시 폼 표시/숨김 처리 (통합 폼용)
function toggleMemberType() {
    var memberRoleInput = document.querySelector('input[name="memberRole"]:checked');
    if (!memberRoleInput) return; // 통합 폼이 아닌 경우 실행하지 않음

    var memberRole = memberRoleInput.value;
    var helperFields = document.getElementById('helper_fields');
    var form = document.forms['joinForm'];

    if (memberRole === "2") { // 헬퍼 선택
        if (helperFields) helperFields.style.display = 'block';

        // 헬퍼 필수 필드 설정
        if (form.memberBank) form.memberBank.required = true;
        if (form.memberAccount) form.memberAccount.required = true;
        if (form.memberIntroduce) form.memberIntroduce.required = true;

    } else { // 일반 사용자 선택 (memberRole == "1")
        if (helperFields) helperFields.style.display = 'none';

        // 헬퍼 필드 필수 해제
        if (form.memberBank) form.memberBank.required = false;
        if (form.memberAccount) form.memberAccount.required = false;
        if (form.memberIntroduce) form.memberIntroduce.required = false;

        // 헬퍼 필드 값 초기화
        if (form.memberBank) form.memberBank.value = '';
        if (form.memberAccount) form.memberAccount.value = '';
        if (form.memberIntroduce) form.memberIntroduce.value = '';

        // 차량 라디오 버튼 초기화
        var vehicleRadios = document.querySelectorAll('input[name="memberVehicle"]');
        if (vehicleRadios.length > 0) {
            vehicleRadios.forEach(function(radio) {
                radio.checked = false;
            });
            vehicleRadios[0].checked = true; // 첫 번째 옵션 선택
        }
    }
}

// 프로필 이미지 사진 미리보기
function previewImage(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = function(e) {
        const preview = document.getElementById('previewImage');
        preview.src = e.target.result;

        const container = document.getElementById('previewContainer');
        container.style.display = 'block';

        const current = document.getElementById('currentProfileContainer');
        if (current) current.style.display = 'none';
    };
    reader.readAsDataURL(file);
}

// 폼 전체 초기화 함수
function resetJoinForm() {
    const form = document.forms['joinForm'];
    if (!form) return;

    // form의 모든 입력 초기화
    form.reset();

    // 파일 입력 초기화 (보안 정책상 직접 처리 필요)
    const fileInput = document.getElementById("attachFile");
    if (fileInput) {
        try {
            fileInput.value = "";
        } catch (e) {
            const newInput = fileInput.cloneNode(true);
            fileInput.parentNode.replaceChild(newInput, fileInput);
            newInput.addEventListener("change", previewImage);
        }
    }

    // 3️⃣ 미리보기/기존 프로필 표시 복원
    const previewContainer = document.getElementById("previewContainer");
    const previewImageEl = document.getElementById("previewImage");
    const currentProfile = document.getElementById("currentProfileContainer");

    if (previewImageEl) previewImageEl.src = "";
    if (previewContainer) previewContainer.style.display = "none";
    if (currentProfile) currentProfile.style.display = "block";
}


// 취소버튼 클릭
function cancelModify() {
    if (confirm('수정을 취소하고 마이페이지로 돌아가시겠습니까?')) {
        resetJoinForm();
        window.location.href = '/zipkok/member/mypage';
    }
}


// 정보를 수정하지 않고 홈버튼을 누를 경우 폼 초기화 하고 메인으로 돌아간다.
function goHome() {
    // 프로필 이미지 선택 취소 (미리보기 제거, 파일 입력 초기화)
    resetJoinForm();

    // 약간의 지연 후 메인으로 이동 (UI 변경이 반영되도록)
    setTimeout(() => {
        window.location.href = '/zipkok';
    }, 100);
}


