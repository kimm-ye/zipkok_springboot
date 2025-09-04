document.addEventListener('DOMContentLoaded', function() {

    // 수정인 경우 thymeleaf로 정보 전달
    if (window.memberData && window.memberData.isModify) {
        // 회원 유형에 따른 헬퍼 필드 표시
        const memberStatus = window.memberData.info.memberStatus;
        if (memberStatus === 2) {
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

    // 타입 토글설정
    toggleMemberType();

    // 아이디 중복확인 검사
    let debounceTimer = null;
    let currentController = null;

    const memberInput = document.querySelector("#memberId");
    if (memberInput) {
        memberInput.addEventListener("input", function(e) {
            const memberId = e.target.value.trim();

            // 이전 요청들 정리
            clearTimeout(debounceTimer);
            if (currentController) currentController.abort();

            // 4글자 미만이면 체크 안함
            if (memberId.length < 4) {
                showMsg("아이디는 4글자 이상 입력하세요", "gray");
                return;
            }

            // 유효성 검사...

            // 500ms 후 중복체크 (디바운싱)
            debounceTimer = setTimeout(async () => {
                try {
                    currentController = new AbortController();

                    const response = await fetch(`./join/check?memberId=${memberId}`, {
                        method: "POST",
                        signal: currentController.signal
                    });
                    const data = await response.json();

                    if (data.exists) {
                        showMsg("이미 사용 중인 아이디입니다 ❌", "red");
                    } else {
                        showMsg("사용 가능한 아이디입니다 ✅", "green");
                    }
                } catch (err) {
                    if (err.name !== 'AbortError') {
                        showMsg("중복 확인 중 오류 발생", "red");
                    }
                }
            }, 500);
        });
    }
});

// 아이디 중복체크 여부 메세지 표시
function showMsg(msg, color) {
    const msgEl = document.getElementById("idCheckMsg");
    msgEl.innerText = msg;
    msgEl.style.display = "block";
    msgEl.style.color = color;
}


// 회원가입 유효성 검사
function joinValidate(form) {

    const isModifyMode = window.memberData && window.memberData.isModify;
    if (!isModifyMode) {
        // ID 길이 체크
        if (!(form.memberId.value.length >= 4 && form.memberId.value.length <= 12)) {
            alert("4자 이상 12자 이내의 값만 입력하세요");
            form.memberId.value = '';
            form.memberId.focus();
            return false;
        }

        // ID 영문/숫자 체크
        var whatType = form.memberId.value;
        for (var i = 0; i < whatType.length; i++) {
            if (!((whatType[i] >= 'a' && whatType[i] <= 'z') ||
                (whatType[i] >= 'A' && whatType[i] <= 'Z') ||
                (whatType[i] >= '0' && whatType[i] <= '9'))) {
                alert("아이디는 숫자랑 영문자만 입력가능합니다");
                form.memberId.value = '';
                form.memberId.focus();
                return false;
            }
        }

        // 중복체크 확인
        if (form.idDuplication.value !== "idCheck") {
            alert("이미 사용 중인 아이디입니다");
            return false;
        }
    }

    // 패스워드 검사 (수정 모드에서는 입력했을 때만)
    const hasPassword = form.memberPass.value || form.memberPass2.value;
    if (!isModifyMode || hasPassword) {
        if (form.memberPass.value !== form.memberPass2.value) {
            alert('입력한 패스워드가 일치하지 않습니다.');
            form.memberPass.value = "";
            form.memberPass2.value = "";
            form.memberPass.focus();
            return false;
        }

        // 신규 가입시에는 비밀번호 필수
        if (!isModifyMode && !form.memberPass.value) {
            alert("비밀번호를 입력해주세요");
            form.memberPass.focus();
            return false;
        }
    }

    // 통합 폼에서 헬퍼 선택시 추가 유효성 검사
    var memberStatusInput = document.querySelector('input[name="memberStatus"]:checked');
    if (memberStatusInput && memberStatusInput.value === "2") {
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



// 은행 선택시 계좌번호 입력 활성화 (통합 폼용)
function input_bank(frm) {
    var bank = frm.memberBank.value;

    if (bank == "") {
        frm.memberAccount.readOnly = true;
        frm.memberAccount.value = '';
    }
    else {
        frm.memberAccount.readOnly = false;
        frm.memberAccount.focus();
    }
}

// 폼 제출을 fetch로 처리하는 공통 함수
async function submitFormWithFetch(form) {
    event.preventDefault(); // 기본 submit 방지

    if (!joinValidate(form)) {
        return false;
    }

    try {
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
        console.log(result)

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
    }
}

// 회원 유형 변경시 폼 표시/숨김 처리 (통합 폼용)
function toggleMemberType() {
    var memberStatusInput = document.querySelector('input[name="memberStatus"]:checked');
    if (!memberStatusInput) return; // 통합 폼이 아닌 경우 실행하지 않음

    var memberStatus = memberStatusInput.value;
    var helperFields = document.getElementById('helper_fields');
    var form = document.forms['joinForm'];

    if (memberStatus == "2") { // 헬퍼 선택
        if (helperFields) helperFields.style.display = 'block';

        // 헬퍼 필수 필드 설정
        if (form.memberBank) form.memberBank.required = true;
        if (form.memberAccount) form.memberAccount.required = true;
        if (form.memberIntroduce) form.memberIntroduce.required = true;

    } else { // 일반 사용자 선택 (memberStatus == "1")
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

// 취소버튼 클릭
function cancelModify() {
    if (confirm('수정을 취소하고 마이페이지로 돌아가시겠습니까?')) {
        window.location.href = '/zipkok/member/mypage';
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
        preview.style.display = 'block';

        const container = document.getElementById('previewContainer');
        container.style.display = 'block';


        const current = document.getElementById('currentProfileContainer');
        if (current) current.style.display = 'none';
    };
    reader.readAsDataURL(file);
}



