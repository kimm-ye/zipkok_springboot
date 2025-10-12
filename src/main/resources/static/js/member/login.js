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

// 로그인 폼 유효성 검사
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

    const data = {
        memberId: id,
        memberPass: pass
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

           /* localStorage.setItem('memberId', result.memberId || id);
            localStorage.setItem('memberName', result.memberName || '');*/
            
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





// ==================== 카카오 로그인 버튼 클릭 ====================


//카카오 로그인 후 토근 값 저장.
function loginWithKakao() {

    Kakao.Auth.login({
        success: function (authObj) {
            console.log(authObj); // access토큰 값
            Kakao.Auth.setAccessToken(authObj.access_token); // access토큰값 저장

            getInfo();
        },
        fail: function (err) {
            console.log(err);
        }
    });
}

// 엑세스 토큰을 발급받고, 아래 함수를 호출시켜서 사용자 정보를 받아옴.
function getInfo() {
    Kakao.API.request({
        url: '/v2/user/me',
        success: function (res) {
            var account = res.kakao_account;

            document.getElementById('kakaoemail').val(account.email);
            document.getElementById('kakaoname').val(account.profile.nickname);
            // 사용자 정보가 포함된 폼을 서버로 제출한다.
            document.querySelector('#form-kakao-login').submit();
        },
        fail: function (error) {
            alert('카카오 로그인에 실패했습니다. 관리자에게 문의하세요.' + JSON.stringify(error));
        }
    });
}