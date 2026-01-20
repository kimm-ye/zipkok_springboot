// 전역(혹은 함수 스코프) 변수로 새로 추가된 파일들을 관리
let newFilesArr = [];

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
        console.log('카카오맵 로드 성공');  // 로드 확인
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

///////////////////////// 파일 ///////////////////////////////////////
// 파일 업로드 - 단일
/*document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('attachFiles');
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
});*/

// 파일 업로드 (다중 파일 지원)
document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('attachFiles');

    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const selectedFiles = Array.from(e.target.files); // 방금 선택한 파일들

            // 유효성 검사 및 배열 추가
            addFiles(selectedFiles);

            // 중요: input 값을 초기화해야 동일한 파일을 다시 선택해도 change 이벤트가 발생함
            // 또한 "취소"를 눌렀을 때 기존 목록이 날아가는 문제를 방지함 (우리는 배열로 관리하니까)
            fileInput.value = '';
        });
    }
});

// 파일 추가 처리 함수
function addFiles(files) {
    // 1. 현재 갯수 체크 (기존 파일 + 이미 추가된 새 파일)
    const existingCount = document.querySelectorAll('.existing-file-item').length;
    const currentTotal = existingCount + newFilesArr.length;

    if (currentTotal + files.length > 5) {
        alert('최대 5개까지만 업로드 가능합니다.');
        return;
    }

    for (const file of files) {
        // 2. 파일 타입 검사 (이미지)
        if (!file.type.match('image.*')) {
            alert('이미지 파일만 업로드 가능합니다: ' + file.name);
            continue;
        }

        // 3. 용량 검사 (5MB)
        if (file.size > 5 * 1024 * 1024) {
            alert('파일 사이즈는 5MB를 초과할 수 없습니다: ' + file.name);
            continue;
        }

        // 4. 중복 검사 (이름과 사이즈가 같으면 중복으로 간주)
        const isDuplicate = newFilesArr.some(f => f.name === file.name && f.size === file.size);
        if (isDuplicate) {
            continue;
        }

        // 통과된 파일만 배열에 저장
        newFilesArr.push(file);
    }

    // UI 다시 그리기
    renderNewFiles();
}

// 새 파일 목록 UI 렌더링
function renderNewFiles() {
    const newFileArea = document.getElementById('newFileArea');
    const newFileList = document.getElementById('newFileList');

    newFileList.innerHTML = ''; // 초기화

    if (newFilesArr.length === 0) {
        newFileArea.style.display = 'none';
        return;
    }

    newFileArea.style.display = 'block';

    newFilesArr.forEach((file, index) => {
        const div = document.createElement('div');
        div.className = 'current-file';
        div.innerHTML = `
            <div class="file-info">
                <i class="fas fa-file-image" style="color: #48bb78;"></i> 
                <span>${file.name}</span>
                <span style="font-size:0.8em; color:#888;">(${(file.size/1024/1024).toFixed(2)}MB)</span>
            </div>
            <button type="button" class="btn-delete-img" onclick="removeNewFile(${index})">
                <i class="fas fa-times"></i>
            </button>
        `;
        newFileList.appendChild(div);
    });


}

// 신규 추가된 파일 삭제 (배열에서 제거)
function removeNewFile(index) {
    newFilesArr.splice(index, 1); // 배열에서 해당 인덱스 삭제
    renderNewFiles(); // UI 갱신
}

// 기존 파일 삭제 (서버 전송용 hidden input 생성 + 화면 숨김)
function removeExistingImage(btn) {
    if(!confirm('등록된 파일을 삭제하시겠습니까?')) return;

    const seq = btn.getAttribute('data-seq');
    const row = document.getElementById('img-row-' + seq);

    // 화면에서 제거
    row.remove();

    // 서버로 보낼 삭제 리스트에 추가
    const container = document.getElementById('deleteImageContainer');
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'deleteImageSeqs'; // DTO의 필드명과 일치해야 함
    input.value = seq;
    container.appendChild(input);
}

/////////////////////////////////////////////


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

function wayPostCode() {
    new daum.Postcode({
        oncomplete: function(data) {
            settingAdress(data);

            // 좌표 변환 (카카오맵이 로드된 경우에만)
            if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
                const geocoder = new kakao.maps.services.Geocoder();  // ✅ 여기서 생성
                geocoder.addressSearch(data.address, function(result, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        const latField = document.getElementById('wayLatitude');
                        const lngField = document.getElementById('wayLongitude');

                        if (latField) latField.value = result[0].y;
                        if (lngField) lngField.value = result[0].x;

                        console.log('Way 좌표:', result[0].y, result[0].x);  // 확인용
                    }
                });
            }

            document.getElementById('wayPostcode').value = data.zonecode;
            document.getElementById("wayAddress1").value = data.address;
            document.getElementById("wayAddress2").focus();
        }
    }).open();
}

function endPostCode() {
    new daum.Postcode({
        oncomplete: function(data) {
            settingAdress(data);

            // 좌표 변환 (카카오맵이 로드된 경우에만)
            if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
                const geocoder = new kakao.maps.services.Geocoder();  // ✅ 여기서 생성
                geocoder.addressSearch(data.address, function(result, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        const latField = document.getElementById('endLatitude');
                        const lngField = document.getElementById('endLongitude');

                        if (latField) latField.value = result[0].y;
                        if (lngField) lngField.value = result[0].x;

                        console.log('End 좌표:', result[0].y, result[0].x);  // 확인용
                    }
                });
            }

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


////////////////////// 버튼/////////////////////////////////

//심부름 등록하기 버튼 클릭
async function missionRegister(form){
    event.preventDefault(); // 기본 submit 방지

    try {
        const formData = new FormData(form);

        // 신규 이미지 파일 추가되는 부분은 dto로 넘길 수 없어서 별도로 append 한다
        if (newFilesArr.length > 0) {
            newFilesArr.forEach(file => {
                formData.append("attachFiles", file);
            });
        }

        const response = await fetch('/zipkok/mission/request/register', {
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
        /*alert('error :' + error);*/
    } finally {
        hideLoading();
    }
}


//심부름 수정하기 버튼 클릭
async function missionUpdate(form){
    event.preventDefault(); // 기본 submit 방지

    try {
        const formData = new FormData(form);
        if (newFilesArr.length > 0) {
            newFilesArr.forEach(file => {
                formData.append("attachFiles", file);
            });
        }

        const response = await fetch('/zipkok/mission/request/update', {
            method: 'PATCH',
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
    } catch (error) {
        console.error('심부름 수행 오류:', error);
        alert('오류가 발생했습니다.');

    } finally {
        hideLoading();
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

