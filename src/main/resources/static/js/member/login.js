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
        $('#member_id').removeAttr('required');
        $('#member_id').val('');
    } else {
        $('.id_field').show();
        $('#btnText').text('비밀번호 찾기');
        $('#member_id').attr('required', true);
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
function findIdRequest() {

    const form = document.querySelector("form[name='findForm']");

    $.ajax({
        type: 'post',
        url: './find/id',
        data: { 'name': form.member_name.value, 'email_1': form.email_1.value, 'email_2': form.email_2.value },
        success: function(data) {
            if(data === '') {
                alert("일치하는 회원이 없습니다.");
            } else {
                alert("아이디는 '" + data + "'입니다.");
            }
        },
        error: function() {
            alert("오류가 발생하였습니다. \n동일한 증상 발생시 관리자에게 문의바랍니다.");
        }
    });
}

// 비밀번호 찾기
function findPwd() {
    console.log("비번 찾기 실행")

    const form = document.querySelector("form[name='findForm']");

    const data = {
        id : form.member_id.value,
        name : form.member_name.value,
        email_1 : form.email_1.value,
        email_2 : form.email_2.value
    }

    $.ajax({
        type: 'post',
        url: './find/pw',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(data) {
            if(data === '') {
                alert("일치하는 회원이 없습니다.");
            } else {
                alert("패스워드는 '" + data + "'입니다.");
            }
        },
        error: function() {
            alert("오류가 발생하였습니다. \n 동일한 증상 발생시 관리자에게 문의바랍니다.");
        }
    });
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