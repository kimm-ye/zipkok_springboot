let isAdmin;
let noticeId;
let currentFileId = null; // 현재 파일 ID 추적

document.addEventListener('DOMContentLoaded', () => {
    const mode = document.getElementById('pageMode')?.value;
    noticeId = document.getElementById('noticeId')?.value;
    isAdmin = document.getElementById('isAdmin')?.value === 'true';

    console.log(mode, noticeId, isAdmin);

    // 권한 체크
    if (mode === 'edit' && !isAdmin) {
        alert('수정 권한이 없습니다.');
        location.href = `/zipkok/notice/form?noticeId=${noticeId}&mode=view`;
        return;
    }

    if ((mode === 'view' || mode === 'edit') && noticeId) {
        loadNoticeDetail(noticeId, isAdmin, mode);
    }
});

////////////////////////* 공지사항 작성 *//////////////////////

function updateBadgePreview() {
    const type = document.getElementById('noticeType').value;
    const preview = document.getElementById('badgePreview');
    const badges = {
        I: '<span class="badge-preview-item badge-important">중요</span>',
        N: '<span class="badge-preview-item badge-normal">일반</span>'
    };
    preview.innerHTML = type ? '미리보기: ' + badges[type] : '';
}

async function submitNotice(form, mode) {
    event.preventDefault();

    try {
        showLoading();
        const formData = new FormData(form);
        let response;

        if (mode === 'edit') {
            formData.append("noticeSeq", noticeId);
            response = await fetch('/zipkok/api/notice/update', {
                method: 'PATCH',
                body: formData,
                credentials: 'same-origin'
            });
        } else {
            response = await fetch('./write/action', {
                method: 'POST',
                body: formData,
                credentials: 'same-origin'
            });
        }

        let result = null;
        try {
            result = await response.json();
        } catch (e) {
            console.error('JSON 파싱 실패', e);
        }

        if (!response.ok) {
            let msg = '서버 오류: ' + response.status;
            if (result && result.message) msg = result.message;
            alert(msg);
            return;
        }

        if (result && result.success) {
            alert(result.message);
            if (result.redirectUrl) {
                window.location.href = result.redirectUrl;
            }
        } else {
            let msg = '처리 실패';
            if (result && result.message) msg = result.message;
            alert(msg);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('처리 중 오류가 발생했습니다.');
    } finally {
        hideLoading();
    }
}

function goBack() {
    if (confirm('작성 중인 내용이 사라집니다. 취소하시겠습니까?')) {
        window.history.back();
    }
}

function handleFileSelect(event) {
    const file = event.target.files[0];
    const maxSize = 10 * 1024 * 1024; // 10MB

    if (file && file.size > maxSize) {
        alert("파일 크기는 10MB를 초과할 수 없습니다.");
        event.target.value = "";
    } else if (file) {
        document.getElementById('fileName').textContent = file.name;
        document.getElementById('selectedFile').classList.add('show');
        document.getElementById('labelBoardFile').style.display = 'none';
    }
}

function removeFile(mode) {
    if (mode === 'edit') {
        // edit 모드: 기존 파일 삭제 표시만
        currentFileId = null; // 파일 ID 제거
        document.getElementById('selectedFile').classList.remove('show');
        document.getElementById('labelBoardFile').style.display = 'block';

        // 새 파일 업로드 input 생성 (없으면)
        if (!document.getElementById('boardAttachFile')) {
            const fileGroup = document.getElementById('fileGroup');
            const input = document.createElement('input');
            input.type = 'file';
            input.id = 'boardAttachFile';
            input.name = 'boardAttachFile';
            input.style.display = 'none';
            input.onchange = handleFileSelect;
            fileGroup.insertBefore(input, document.getElementById('labelBoardFile'));
        }
    } else {
        // write 모드
        document.getElementById('boardAttachFile').value = '';
        document.getElementById('selectedFile').classList.remove('show');
        document.getElementById('labelBoardFile').style.display = 'block';
    }
}

///////////////////////////////공지사항 상세///////////////////////////////////////////

async function loadNoticeDetail(noticeId, isAdmin, mode) {
    showLoading();

    try {
        const response = await fetch(`/zipkok/api/notice/detail?noticeId=${noticeId}`);
        const result = await response.json();

        if (result.success && result.data) {
            renderNoticeForm(result.data, isAdmin, mode);
        } else {
            alert(result.message || '공지사항을 불러오는데 실패했습니다.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('공지사항을 불러오는 중 오류가 발생했습니다.');
    } finally {
        hideLoading();
    }
}

// ⭐ 핵심: DOM을 다시 그리지 않고 데이터만 변경
function renderNoticeForm(data, isAdmin, mode) {
    const form = document.getElementById('noticeForm');
    const isReadOnly = (mode === 'view');

    // 1. 폼 데이터 채우기
    document.getElementById('noticeType').value = data.noticeType;
    document.getElementById('noticeTitle').value = data.noticeTitle;
    document.getElementById('noticeContent').value = data.noticeContent;

    // 2. readonly 설정
    document.getElementById('noticeType').disabled = isReadOnly;
    document.getElementById('noticeTitle').readOnly = isReadOnly;
    document.getElementById('noticeContent').readOnly = isReadOnly;

    // 3. 스타일 적용
    const bgColor = isReadOnly ? '#f7fafc' : '#ffffff';
    document.getElementById('noticeTitle').style.backgroundColor = bgColor;
    document.getElementById('noticeContent').style.backgroundColor = bgColor;
    if (isReadOnly) {
        document.getElementById('noticeContent').style.minHeight = '300px';
    }

    // 4. 첨부파일 처리 (기존 요소 활용)
    handleFileGroupDisplay(data, isReadOnly);

    // 5. 버튼 그룹 (여기는 innerHTML 사용 - 구조가 완전히 다르므로)
    updateButtonGroup(data, isAdmin, mode);

    // 6. 폼 submit 이벤트
    if (isReadOnly) {
        form.onsubmit = (e) => {
            e.preventDefault();
            return false;
        };
    } else {
        form.onsubmit = (e) => {
            e.preventDefault();
            submitNotice(form, 'edit');
            return false;
        };
    }
}

// 첨부파일 영역 처리 (DOM 재사용)
function handleFileGroupDisplay(data, isReadOnly) {
    const fileInput = document.getElementById('boardAttachFile');
    const fileLabel = document.getElementById('labelBoardFile');
    const selectedFile = document.getElementById('selectedFile');
    const fileName = document.getElementById('fileName');

    if (isReadOnly) {
        // view 모드: 모든 업로드 요소 숨기고 정보만 표시
        if (fileInput) fileInput.style.display = 'none';
        if (fileLabel) fileLabel.style.display = 'none';

        if (data.fileId && data.boardFileName) {
            currentFileId = data.fileId;
            selectedFile.innerHTML = `
                <i class="fas fa-file-${data.boardFileEtx === 'pdf' ? 'pdf' : 'alt'}" style="color: #4299e1;"></i>
                <a href="/zipkok/file/download?fileId=${data.fileId}" 
                   download="${data.boardFileName}"
                   style="color: #2d3748; text-decoration: none; flex: 1;">
                    ${data.boardFileName}
                </a>
                <i class="fas fa-download" style="color: #718096;"></i>
            `;
            selectedFile.classList.add('show');
        } else {
            selectedFile.innerHTML = '<span style="color: #a0aec0;">첨부파일이 없습니다</span>';
            selectedFile.classList.add('show');
        }
    } else {
        // edit 모드: 파일 있으면 표시, 없으면 업로드 UI
        if (data.fileId && data.boardFileName) {
            currentFileId = data.fileId;

            // 기존 파일 표시
            if (fileInput) fileInput.style.display = 'none';
            if (fileLabel) fileLabel.style.display = 'none';

            // fileName 요소가 없으면 다시 생성
            if (fileName) {
                fileName.textContent = data.boardFileName;
            } else {
                // innerHTML으로 완전히 재구성 (view에서 edit로 전환 시)
                selectedFile.innerHTML = `
                    <i class="fas fa-file-image"></i>
                    <span id="fileName">${data.boardFileName}</span>
                    <button type="button" onclick="removeFile('edit')"
                            style="margin-left: auto; background: none; border: none; color: #f56565; cursor: pointer;">
                        <i class="fas fa-times"></i>
                    </button>
                `;
            }
            selectedFile.classList.add('show');
        } else {
            // 파일 없음: 업로드 UI 표시
            currentFileId = null;

            if (fileInput) {
                fileInput.style.display = 'none';
                fileInput.value = '';
            }
            if (fileLabel) fileLabel.style.display = 'block';

            selectedFile.classList.remove('show');
        }
    }
}

// 버튼 그룹 업데이트
function updateButtonGroup(data, isAdmin, mode) {
    const buttonGroup = document.querySelector('.button-group');

    if (mode === 'view') {
        if (isAdmin) {
            buttonGroup.innerHTML = `
                <div>
                    <button type="button" class="cancel-btn" onclick="goToList()">
                        <i class="fas fa-list"></i> 목록
                    </button>
                    <button type="button" class="submit-btn" onclick="switchToEditMode('${data.noticeSeq}')" 
                            style="background: #4299e1;">
                        <i class="fas fa-edit"></i> 수정
                    </button>
                    <button type="button" class="submit-btn" onclick="deleteNotice('${data.noticeSeq}')"
                            style="background: #f56565;">
                        <i class="fas fa-trash"></i> 삭제
                    </button>
                </div>
            `;
        } else {
            buttonGroup.innerHTML = `
                <div>
                    <button type="button" class="cancel-btn" onclick="goToList()">
                        <i class="fas fa-list"></i> 목록
                    </button>
                </div>
            `;
        }
    } else if (mode === 'edit') {
        buttonGroup.innerHTML = `
            <div style="display: flex; gap: 10px;">
                <button type="button" class="cancel-btn" onclick="goBack()">
                    <i class="fas fa-times"></i> 취소
                </button>
                <button type="submit" class="submit-btn">
                    <i class="fas fa-save"></i> 저장
                </button>
            </div>
        `;
    }
}

function goToList() {
    location.href = "/zipkok/notice";
}

function switchToEditMode(noticeId) {
    loadNoticeDetail(noticeId, isAdmin, 'edit');
}

async function deleteNotice(noticeId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    showLoading();

    try {
        const response = await fetch('/zipkok/api/notice/delete', {
            method: 'DELETE',
            body: noticeId,
            credentials: 'same-origin'
        });

        const result = await response.json();
        if (result.success) {
            alert('삭제되었습니다.');
            goToList();
        } else {
            alert(result.message || '삭제 실패');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('삭제 중 오류가 발생했습니다.');
    } finally {
        hideLoading();
    }
}