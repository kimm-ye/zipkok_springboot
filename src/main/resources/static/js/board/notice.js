function viewNotice(id) {
    alert('공지사항 ' + id + '번을 조회합니다.');
}

function searchNotice() {
    const keyword = document.getElementById('searchInput').value;
    if (keyword.trim()) {
        alert('"' + keyword + '"로 검색합니다.');
    } else {
        alert('검색어를 입력해주세요.');
    }
}

function changePage(page) {
    const buttons = document.querySelectorAll('.page-btn');
    buttons.forEach(btn => btn.classList.remove('active'));

    if (typeof page === 'number') {
        event.target.classList.add('active');
        alert(page + '페이지로 이동합니다.');
    } else if (page === 'prev') {
        alert('이전 페이지로 이동합니다.');
    } else if (page === 'next') {
        alert('다음 페이지로 이동합니다.');
    }
}

document.getElementById('searchInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        searchNotice();
    }
});



////////////////////////*  공지사항 작성  *//////////////////////

// 공지유형 미리보기
function updateBadgePreview() {
    const type = document.getElementById('noticeType').value;
    const preview = document.getElementById('badgePreview');

    const badges = {
        important: '<span class="badge-preview-item badge-important">중요</span>',
        normal: '<span class="badge-preview-item badge-normal">일반</span>'
    };

    preview.innerHTML = type ? '미리보기: ' + badges[type] : '';
}


// 공지사항 등록
async function submitNotice(form) {
    event.preventDefault();

    const type = document.getElementById('noticeType').value;
    const title = document.getElementById('noticeTitle').value;
    const content = document.getElementById('noticeContent').value;
    const file = document.getElementById('boardAttachFile').files[0];

    // 실제로는 서버로 전송X
    console.log({
        type,
        title,
        content,
        file: file ? file.name : null
    });

    try {
        const formData = new FormData(form);

        const response = await fetch('./write/action', {
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
    }

}

// 취소버튼 클릭시
function goBack() {
    if (confirm('작성 중인 내용이 사라집니다. 취소하시겠습니까?')) {
        window.history.back();
    }
}


// 첨부파일 체인지 이벤트
function handleFileSelect(event) {
    const file = event.target.files[0];
    if (file) {
        const fileName = file.name;
        document.getElementById('fileName').textContent = fileName;
        document.getElementById('selectedFile').classList.add('show');
    }
}

// 첨부파일 제거 이벤트
function removeFile() {
    document.getElementById('boardAttachFile').value = '';
    document.getElementById('selectedFile').classList.remove('show');
}
