document.addEventListener('DOMContentLoaded', async function () {

    // 페이지 로드 시 카테고리 선택된 값 설정
    const urlParams = new URLSearchParams(window.location.search);
    const flag = urlParams.get('flag');
    if (flag) {
        const missionCategory = document.getElementById('missionCategory');
        missionCategory.value = flag;
    }

    try {
        await loadKakaoMapScript();
    } catch (error) {
        console.error('카카오맵 로드 실패:', error);
    }

    // 즉시/예약 신청 구분 라디오 버튼 스타일 변경
    document.querySelectorAll('input[name="missionReservation"]').forEach(radio => {
        radio.addEventListener('change', function() {
            document.querySelectorAll('.radio-item').forEach(item => {
                item.classList.remove('active');
            });
            this.closest('.radio-item').classList.add('active');
        });
    });
});



// 날짜 입력 토글
function toggleDateInput() {
    const dateInput = document.getElementById('dateInput');
    const reservationRadio = document.querySelector('input[name="missionReservation"][value="2"]');

    if (reservationRadio.checked) {
        dateInput.classList.add('show');
    } else {
        dateInput.classList.remove('show');
    }
}


// 파일 업로드 피드백
document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('missionAttachFile');
    const selectedFileNameDiv = document.getElementById('selectedFileName');
    const fileNameText = document.getElementById('fileNameText');

    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];

            if (file) {
                // 파일이 선택되면 파일명 표시
                fileNameText.textContent = '선택된 파일: ' + file.name;
                selectedFileNameDiv.style.display = 'block';
            } else {
                // 파일 선택 취소 시 숨김
                selectedFileNameDiv.style.display = 'none';
            }
        });
    }
});


// 폼 유효성 검사
const submitBtn = document.getElementById('submitBtn');
if(submitBtn) {
    submitBtn.addEventListener('submit', function(e) {
        const price = parseInt(document.querySelector('input[name="mission_cost"]').value);
        if (price < 3000) {
            e.preventDefault();
            alert('최소 금액은 3,000원입니다.');
            return false;
        }
    });
}


// 카카오 우편번호 API
async function loadKakaoMapScript() {
    return new Promise((resolve, reject) => {
        if (window.kakao && window.kakao.maps) {
            resolve();
            return;
        }

        const script = document.createElement('script');
        script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=fd6202fdf742e1c361e44f8a65bdba05&libraries=services&autoload=false';
        script.onload = () => {
            kakao.maps.load(resolve);
        };
        script.onerror = reject;
        document.head.appendChild(script);
    });
}

/* 위도,경도 가져오기 */
/*const geocoder = new kakao.maps.services.Geocoder();*/

<!-- 카카오 우편번호 검색 api  -->
//본 예제에서는 도로명 주소 표기 방식에 대한 법령에 따라, 내려오는 데이터를 조합하여 올바른 주소를 구성하는 방법을 설명합니다.
function wayPostCode() {
    new daum.Postcode({
        oncomplete: function(data) {

            settingAdress(data);

            // 좌표 변환 (카카오맵이 로드된 경우에만)
            if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
                const geocoder = new kakao.maps.services.Geocoder();
                geocoder.addressSearch(data.address, function(result, status) {
                    if (status === kakao.maps.services.Status.OK) {

                        // // 필요시 좌표를 hidden input에 저장
                        const latField = document.getElementById('wayLatitude');
                        const lngField = document.getElementById('wayLongitude');

                        if (latField) latField.value = result[0].y;
                        if (lngField) lngField.value = result[0].x;
                    }
                });
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('wayPostcode').value = data.zonecode;
            document.getElementById("wayAddress1").value = data.address;
            document.getElementById("wayAddress2").focus();
        }
    }).open();
}

function endPostCode() {
    new daum.Postcode({
        oncomplete: function(data) {

            var callback = function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                }
            };

            settingAdress(data);

            // 좌표 변환 (카카오맵이 로드된 경우에만)
            if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
                const geocoder = new kakao.maps.services.Geocoder();
                geocoder.addressSearch(data.address, function(result, status) {
                    if (status === kakao.maps.services.Status.OK) {

                        // // 필요시 좌표를 hidden input에 저장
                        const latField = document.getElementById('endLatitude');
                        const lngField = document.getElementById('endLongitude');

                        if (latField) latField.value = result[0].y;
                        if (lngField) lngField.value = result[0].x;
                    }
                });
            }

            // 우편번호와 주소 정보를 해당 필드에 넣는다.
            document.getElementById('endPostcode').value = data.zonecode;
            document.getElementById("endAddress1").value = data.address;
            document.getElementById("endAddress2").focus();
        }
    }).open();
}

function settingAdress(data){

    // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.

    // 도로명 주소의 노출 규칙에 따라 주소를 표시한다.
    // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
    var extraRoadAddr = ''; // 참고 항목 변수

    // 법정동명이 있을 경우 추가한다. (법정리는 제외)
    // 법정동의 경우 마지막 문자가 "동/로/가"로 끝난다.
    if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
        extraRoadAddr += data.bname;
    }
    // 건물명이 있고, 공동주택일 경우 추가한다.
    if(data.buildingName !== '' && data.apartment === 'Y'){
        extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
    }

}

//심부름 등록하기 버튼 클릭
async function missionRegister(form){
    event.preventDefault(); // 기본 submit 방지

    try {
        const formData = new FormData(form);

        const response = await fetch('./request/register', {
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
        alert('error :' + error);
        hideLoading()
    }
}


//심부름 수정하기 버튼 클릭
async function missionUpdate(form){
    event.preventDefault(); // 기본 submit 방지

    try {
        const formData = new FormData(form);

        const response = await fetch('./update', {
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
        alert('error :' + error);
        hideLoading();
    }
}

// 심부름 수행하기 함수
async function updateMissionStatus(status) {
    event.preventDefault(); // 기본 submit 방지
    const missionSeq = document.getElementById('missionSeq').value;

    // 확인 메시지
    const confirmMsg = getConfirmMessage(status);
    if (!confirm(confirmMsg)) {
        return;
    }

    showLoading();

    try {
        const response = await fetch(`../status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body : JSON.stringify({ missionSeq: missionSeq, missionStatus: status })
        });

        const data = await response.json();

        if (data.success) {
            alert(data.message);
            location.href = data.redirectUrl;
        } else {
            alert(data.message);
        }

        hideLoading();

    } catch (error) {
        console.error('심부름 수행 오류:', error);
        alert('오류가 발생했습니다.');
    }
}

// 심부름 상태에 따른 메시지 출력
function getConfirmMessage(status){
    switch(status) {
        case 1: return '이 심부름을 수행하시겠습니까?';
        case 2: return '이 심부름을 완료하시겠습니까?';
        case 9: return '이 심부름을 취소/삭제하시겠습니까?';
        default: return '상태를 변경하시겠습니까?';
    }
}

