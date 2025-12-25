document.addEventListener('DOMContentLoaded', function() {
    /*document.getElementById("memberUnregister").addEventListener("click", async function (e) {
        e.preventDefault();
        await handleMemberUnregister();
    });
*/

    // 탭 버튼 활성화 스타일
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            this.classList.add('active');
        });
    });

});


// 회원 탈퇴
async function handleMemberUnregister(){
    // 확인 절차 추가
    if (!confirm('정말로 회원탈퇴를 하시겠습니까?\n탈퇴 후에는 복구가 불가능합니다.')) {
        return;
    }

    try{
        const response = await fetch('./unregister', {
            method : 'GET'
        });

        if(!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {
            alert(result.message);
            // 로그인 성공 후 메인 페이지로 이동
            window.location.href = '/zipkok';
        } else {
            alert(result.message);
        }

    } catch (error) {
        console.error('아이디 찾기 오류:', error);
        alert("오류가 발생하였습니다. \n동일한 증상 발생시 관리자에게 문의바랍니다.");
    }

}

// 요청/수행 탭 전환 로직
function switchTab(type) {
    const requestList = document.getElementById('requestList');
    const performList = document.getElementById('performList');

    if (type === 'request') {
        requestList.style.display = 'block';
        if(performList) performList.style.display = 'none';
    } else {
        requestList.style.display = 'none';
        if(performList) performList.style.display = 'block';
    }
}


// 헬퍼용 더보기 버튼
function showMoreMenu() {
    const currentTab = document.querySelector('.tab-btn.active').textContent.trim();
    if(currentTab === '요청') {
        location.href = '/zipkok/mission/add?flag=request';
    } else {
        location.href = '/zipkok/mission/add?flag=perform';
    }
}

// 심부름 상세 페이지로 이동
function goToDetail(element) {
    const missionSeq = element.getAttribute('data-mission-seq');
    location.href = '/zipkok/mission/request/detail?missionSeq=' + missionSeq;
}
