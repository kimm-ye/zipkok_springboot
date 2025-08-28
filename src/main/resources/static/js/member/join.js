document.addEventListener('DOMContentLoaded', function() {

    // 통합 회원가입 폼이 있는 경우 초기 설정
    if (document.forms['joinForm']) {
        toggleMemberType();
    }

    // 아이디 중복확인 검사
    const memberInput = document.querySelector("#memberId");
    if (memberInput) {
        memberInput.addEventListener("input", async function(e) {
            const memberId = e.target.value.trim();

            // 유효성 검사
            if (!(memberId.length >= 4 && memberId.length <= 12)) {
                showMsg("아이디는 4~12자 이내여야 합니다.", "red");
                return;
            }
            if (!/^[a-zA-Z0-9]+$/.test(memberId)) {
                showMsg("영문자와 숫자만 입력 가능합니다.", "red");
                return;
            }

            try {
                const response = await fetch(`./join/check?memberId=${encodeURIComponent(memberId)}`, {
                    method: "POST"
                });
                const data = await response.json();

                if (data.exists) {
                    showMsg("이미 사용 중인 아이디입니다 ❌", "red");
                    document.querySelector("input[name=idDuplication]").value = "idUncheck";
                } else {
                    showMsg("사용 가능한 아이디입니다 ✅", "green");
                    document.querySelector("input[name=idDuplication]").value = "idCheck";
                }
            } catch (err) {
                showMsg("중복 확인 중 오류 발생", "red");
            }
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

    // 패스워드 일치 체크
    if (form.memberPass.value !== form.memberPass2.value) {
        alert('입력한 패스워드가 일치하지 않습니다.');
        form.memberPass.value = "";
        form.memberPass2.value = "";
        form.memberPass.focus();
        return false;
    }

    // 중복체크 확인
    if (form.idDuplication.value !== "idCheck") {
        alert("이미 사용 중인 아이디입니다");
        return false;
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
async function submitFormWithFetch(form, url) {
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

        const response = await fetch(url, {
            method: 'POST',
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
    }
}

// 회원 유형 변경시 폼 표시/숨김 처리 (통합 폼용)
function toggleMemberType() {
    var memberStatusInput = document.querySelector('input[name="memberStatus"]:checked');
    if (!memberStatusInput) return; // 통합 폼이 아닌 경우 실행하지 않음

    var memberStatus = memberStatusInput.value;
    var helperFields = document.getElementById('helper_fields');
    var form = document.forms['joinForm'];

    form.onsubmit = async function(event) {
        event.preventDefault();
        await submitFormWithFetch(form, './join/action');
    };

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


