document.addEventListener('DOMContentLoaded', function() {

    // 이메일 주소 세팅
    const emailSelects = document.getElementsByName('email_check');

    const domains = ['gmail.com', 'kakao.net', 'naver.com', 'nate.com'];

    for (let i = 0; i < emailSelects.length; i++) {
        domains.forEach(domain => {
            const option = new Option(domain, domain);
            emailSelects[i].add(option);
        });
    }
});

function setDisplay() {
    // 화면 무조건 초기화
    document.forms['findForm'].reset();

    const isIdMode = $('input:radio[value=id]').is(':checked');

    if (isIdMode) {
        $('.id_field').hide();
        $('#btnText').text('아이디 찾기');
        $('#memberId').removeAttr('required');
        $('#memberId').val('');
    } else {
        $('.id_field').show();
        $('#btnText').text('비밀번호 찾기');
        $('#memberId').attr('required', true);
    }
}

function handleSubmit() {
    const mode = $('input[name="idpw"]:checked').val();
    console.log('mode : ' + mode);
    if (mode === 'id') {
        findIdRequest(); // 아이디 찾기
    } else {
        findPwd(); // 비밀번호 찾기
    }
    return false;
}

// 아이디 찾기
async function findIdRequest() {
    const form = document.querySelector("form[name='findForm']");
    
    const formData = new FormData();
    formData.append('name', form.memberName.value);
    formData.append('email_1', form.email_1.value);
    formData.append('email_2', form.email_2.value);

    try {
        const response = await fetch('./find/id', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.text();
        
        if(data === '') {
            alert("일치하는 회원이 없습니다.");
        } else {
            alert("아이디는 '" + data + "'입니다.");
        }
    } catch (error) {
        console.error('아이디 찾기 오류:', error);
        alert("오류가 발생하였습니다. \n동일한 증상 발생시 관리자에게 문의바랍니다.");
    }
}

// 비밀번호 찾기
async function findPwd() {
    console.log("비번 찾기 실행");

    const form = document.querySelector("form[name='findForm']");

    const data = {
        id: form.memberId.value,
        name: form.memberName.value,
        email_1: form.email_1.value,
        email_2: form.email_2.value
    };

    try {
        const response = await fetch('./find/pw', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.text();
        
        if(result === '') {
            alert("일치하는 회원이 없습니다.");
        } else {
            alert("패스워드는 '" + result + "'입니다.");
        }
    } catch (error) {
        console.error('비밀번호 찾기 오류:', error);
        alert("오류가 발생하였습니다. \n 동일한 증상 발생시 관리자에게 문의바랍니다.");
    }
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

// 로그인 폼 유효성 검사
async function validateLoginForm(form) {

    const id = form.id.value.trim();
    const pass = form.pass.value.trim();

    console.log(id);
    console.log(pass);

    if (!id) {
        alert('아이디를 입력해주세요.');
        form.id.focus();
        return false;
    }

    if (!pass) {
        alert('비밀번호를 입력해주세요.');
        form.pass.focus();
        return false;
    }

    const data = {
        memberId: id,
        memberPass: pass,
        kakaoemail: form.kakaoemail.value,
        kakaoname: form.kakaoname.value
    };

    try {
        const response = await fetch('./login/action', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {

            localStorage.setItem('memberId', result.memberId || id);
            localStorage.setItem('memberName', result.memberName || '');
            
            alert(result.message);
            // 로그인 성공 후 메인 페이지로 이동
            window.location.href = '/zipkok';

        } else {
            alert(result.message);
        }
    } catch (error) {
        console.error('catch 블록에서 잡힌 에러:');
        console.error('에러 타입:', error.constructor.name);
        console.error('에러 메시지:', error.message);
        console.error('전체 에러 객체:', error);

        // 네트워크 에러인지 확인
        if (error instanceof TypeError && error.message.includes('fetch')) {
            alert('네트워크 연결을 확인해주세요.');
        } else {
            alert('요청 처리 중 오류: ' + error.message);
        }
    }
}

