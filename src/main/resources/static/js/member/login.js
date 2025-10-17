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

// 체크여부에 따라 아이디 혹은 비밀번호 찾기 이동
function handleSubmit() {
    const mode = $('input[name="idpw"]:checked').val();
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
        alert("오류가 발생하였습니다. \n 동일한 증상 발생시 관리자에게 문의바랍니다.");
    }
}

// ==================== 일반 로그인 ====================
async function validateLoginForm(form) {
    const id = form.id.value.trim();
    const pass = form.pass.value.trim();

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

    await processLogin({
        type: 'normal',
        memberId: id,
        memberPass: pass
    });
}

// ==================== 카카오 로그인 ====================
function loginWithKakao() {
    Kakao.Auth.login({
        success: function (authObj) {
            Kakao.Auth.setAccessToken(authObj.access_token);
            getKakaoInfo();
        },
        fail: function (err) {
            console.error('카카오 로그인 실패:', err);
            alert('카카오 로그인에 실패했습니다.');
        }
    });
}

function getKakaoInfo() {
    Kakao.API.request({
        url: '/v2/user/me',
        success: async function (res) {
            const account = res.kakao_account;

            await processLogin({
                type: 'kakao',
                email: account.email,
                name: account.profile.nickname,
                snsId: res.id
            });
        },
        fail: function (error) {
            console.error('카카오 정보 조회 실패:', error);
            alert('카카오 로그인에 실패했습니다. 관리자에게 문의하세요.');
        }
    });
}

// ==================== 공통 로그인 처리 함수 ====================
async function processLogin(loginData) {
    try {
        // 로그인 타입에 따라 엔드포인트 분리
        const endpoint = loginData.type === 'kakao'
            ? './login/action/kakao'
            : './login/action';

        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {
            alert(result.message);
            window.location.href = result.redirectUrl;
        } else {
            alert(result.message);
            window.location.href = result.redirectUrl;
        }
    } catch (error) {
        console.error('로그인 처리 중 에러:', error);

        if (error instanceof TypeError && error.message.includes('fetch')) {
            alert('네트워크 연결을 확인해주세요.');
        } else {
            alert('요청 처리 중 오류: ' + error.message);
        }
    }
}