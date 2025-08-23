document.addEventListener('DOMContentLoaded', function() {

    // 통합 회원가입 폼이 있는 경우 초기 설정
    if (document.forms['joinForm']) {
        toggleMemberType();
    }
});


// 회원가입 유효성 검사
function joinValidate(form) {
    // ID 길이 체크
    if (!(form.member_id.value.length >= 4 && form.member_id.value.length <= 12)) {
        alert("4자 이상 12자 이내의 값만 입력하세요");
        form.member_id.value = '';
        form.member_id.focus();
        return false;
    }

    // ID 영문/숫자 체크
    var whatType = form.member_id.value;
    for (var i = 0; i < whatType.length; i++) {
        if (!((whatType[i] >= 'a' && whatType[i] <= 'z') ||
            (whatType[i] >= 'A' && whatType[i] <= 'Z') ||
            (whatType[i] >= '0' && whatType[i] <= '9'))) {
            alert("아이디는 숫자랑 영문자만 입력가능합니다");
            form.member_id.value = '';
            form.member_id.focus();
            return false;
        }
    }

    // 패스워드 일치 체크
    if (form.pass1.value !== form.pass2.value) {
        alert('입력한 패스워드가 일치하지 않습니다.');
        form.pass1.value = "";
        form.pass2.value = "";
        form.pass1.focus();
        return false;
    }

    // 중복체크 확인
    if (form.idDuplication.value !== "idCheck") {
        alert("아이디 중복체크를 해주세요.");
        return false;
    }

    // 통합 폼에서 헬퍼 선택시 추가 유효성 검사
    var memberStatusInput = document.querySelector('input[name="member_status"]:checked');
    if (memberStatusInput && memberStatusInput.value === "2") {
        if (form.member_bank && !form.member_bank.value) {
            alert("은행을 선택해주세요.");
            return false;
        }
        if (form.member_account && !form.member_account.value) {
            alert("계좌번호를 입력해주세요.");
            return false;
        }
        if (form.member_introduce && !form.member_introduce.value) {
            alert("자기소개를 입력해주세요.");
            return false;
        }
    }

    return true; // 모든 검증 통과
}


// ID 중복 확인
function id_check_person(form) {
    try {
        console.log("ID 중복 확인 시작");

        if (form.member_id.value === "") {
            console.error("아이디가 비어있음");
            alert("아이디를 입력후 중복확인을 누르세요");
            form.member_id.focus();
            return false;
        }

        if (!(form.member_id.value.length >= 4 && form.member_id.value.length <= 12)) {
            console.error("아이디 길이 오류: " + form.member_id.value.length);
            alert("4자 이상 12자 이내의 값만 입력하세요");
            form.member_id.value = '';
            form.member_id.focus();
            return false;
        }

        form.member_id.readOnly = true;

        // 팝업 창 중앙 정렬
        var popupX = (window.screen.width / 2) - (250);
        var popupY = (window.screen.height / 2) - (150);

        // 동적 폼 생성
        var popupForm = document.createElement("form");
        popupForm.method = "post";
        popupForm.action = "./join/check";
        popupForm.target = "idover";

        var input = document.createElement("input");
        input.type = "hidden";
        input.name = "memberId";
        input.value = form.member_id.value;
        popupForm.appendChild(input);

        document.body.appendChild(popupForm);

        // 팝업 창 열기
        var popup = window.open("", "idover", 'status=no, height=300, width=500, left=' + popupX + ', top=' + popupY);

        if (!popup) {
            console.error("팝업이 차단되었습니다");
            alert("팝업이 차단되었습니다. 팝업을 허용해주세요.");
            form.member_id.readOnly = false;
            document.body.removeChild(popupForm);
            return false;
        }

        console.log("팝업 창 열기 성공");

        // 폼 제출 시도
        try {
            popupForm.submit();
            console.log("폼 제출 성공");
        } catch (submitError) {
            console.error("폼 제출 실패:", submitError);
            alert("중복 확인 요청 중 오류가 발생했습니다: " + submitError.message);
            popup.close();
            form.member_id.readOnly = false;
        }

        // 폼 제거
        document.body.removeChild(popupForm);
        console.log("임시 폼 제거 완료");

    } catch (error) {
        console.error("ID 중복 확인 중 오류 발생:", error);
        alert("중복 확인 중 오류가 발생했습니다: " + error.message);

        // 오류 시 상태 복구
        if (form.member_id) {
            form.member_id.readOnly = false;
        }
    }
}

// 은행 선택시 계좌번호 입력 활성화 (통합 폼용)
function input_bank(frm) {
    var bank = frm.member_bank.value;

    if (bank == "") {
        frm.member_account.readOnly = true;
        frm.member_account.value = '';
    }
    else {
        frm.member_account.readOnly = false;
        frm.member_account.focus();
    }
}

// 회원 유형 변경시 폼 표시/숨김 처리 (통합 폼용)
function toggleMemberType() {
    var memberStatusInput = document.querySelector('input[name="member_status"]:checked');
    if (!memberStatusInput) return; // 통합 폼이 아닌 경우 실행하지 않음

    var memberStatus = memberStatusInput.value;
    var helperFields = document.getElementById('helper_fields');
    var form = document.forms['joinForm'];

    if (memberStatus == "2") { // 헬퍼 선택
        if (helperFields) helperFields.style.display = 'block';
        form.action = '/zipkok/helper.do';

        // 헬퍼 필수 필드 설정
        if (form.member_bank) form.member_bank.required = true;
        if (form.member_account) form.member_account.required = true;
        if (form.member_introduce) form.member_introduce.required = true;

    } else { // 일반 사용자 선택 (memberStatus == "1")
        if (helperFields) helperFields.style.display = 'none';
        form.action = '/zipkok/member.do';

        // 헬퍼 필드 필수 해제
        if (form.member_bank) form.member_bank.required = false;
        if (form.member_account) form.member_account.required = false;
        if (form.member_introduce) form.member_introduce.required = false;

        // 헬퍼 필드 값 초기화
        if (form.member_bank) form.member_bank.value = '';
        if (form.member_account) form.member_account.value = '';
        if (form.member_introduce) form.member_introduce.value = '';

        // 차량 라디오 버튼 초기화
        var vehicleRadios = document.querySelectorAll('input[name="member_vehicle"]');
        if (vehicleRadios.length > 0) {
            vehicleRadios.forEach(function(radio) {
                radio.checked = false;
            });
            vehicleRadios[0].checked = true; // 첫 번째 옵션 선택
        }
    }
}

// 아이디 중복확인 후 사용하기 버튼 클릭시 호출되는 함수 (팝업창용)
function useThisId(memberId) {
    // 부모 창의 폼에서 아이디 필드 찾기
    var parentForm = window.opener.document.forms['joinForm'] ||
        window.opener.document.forms['Hjoin'] ||
        window.opener.document.forms['Ujoin'];

    if (parentForm) {
        parentForm.member_id.value = memberId;
        parentForm.member_id.readOnly = true;
        parentForm.idDuplication.value = "idCheck";

        // 팝업 창 닫기
        window.close();

        // 부모 창에 포커스
        window.opener.focus();

        alert("아이디 사용이 확정되었습니다.");
    }
}

// 아이디 재입력시 호출되는 함수 (팝업창용)
function reInputId() {
    // 부모 창의 폼에서 아이디 필드 찾기
    var parentForm = window.opener.document.forms['joinForm'] ||
        window.opener.document.forms['Hjoin'] ||
        window.opener.document.forms['Ujoin'];

    if (parentForm) {
        parentForm.member_id.readOnly = false;
        parentForm.member_id.value = '';
        parentForm.member_id.focus();
        parentForm.idDuplication.value = "idUncheck";

        // 팝업 창 닫기
        window.close();

        // 부모 창에 포커스
        window.opener.focus();
    }
}