let selectedRating = 0;
let missionSeq = 0;

document.addEventListener('DOMContentLoaded', async function () {

    // 별점 클릭 이벤트
    document.querySelectorAll('.star-rating i').forEach(star => {
        star.addEventListener('click', function() {
            selectedRating = parseInt(this.getAttribute('data-rating'));
            updateStars(selectedRating);
            document.getElementById('ratingScore').textContent = `${selectedRating}점`;
            document.getElementById('submitRatingBtn').disabled = false;
        });

        // 마우스 오버 효과
        star.addEventListener('mouseenter', function() {
            const rating = parseInt(this.getAttribute('data-rating'));
            updateStars(rating);
        });
    });

    // 별점 영역에서 마우스 나갈 때 선택된 별점으로 복구
    document.querySelector('.star-rating').addEventListener('mouseleave', function() {
        updateStars(selectedRating);
    });

    // 닫기 버튼 클릭
    document.querySelector('.rating-close').addEventListener('click', closeRatingModal);
});


// 평가 모달 열기
function openRatingModal() {
    missionSeq = document.getElementById('missionSeq').value;
    selectedRating = 0;
    document.getElementById('ratingModal').style.display = 'block';
    document.getElementById('ratingComment').value = '';
    document.getElementById('ratingScore').textContent = '별점을 선택해주세요';
    document.getElementById('submitRatingBtn').disabled = true;

    // 별점 초기화
    document.querySelectorAll('.star-rating i').forEach(star => {
        star.classList.remove('active', 'fas');
        star.classList.add('far');
    });
}

// 평가 모달 닫기
function closeRatingModal() {
    document.getElementById('ratingModal').style.display = 'none';
}


// 별점 업데이트
function updateStars(rating) {
    document.querySelectorAll('.star-rating i').forEach((star, index) => {
        if (index < rating) {
            star.classList.remove('far');
            star.classList.add('fas', 'active');
        } else {
            star.classList.remove('fas', 'active');
            star.classList.add('far');
        }
    });
}


// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('ratingModal');
    if (event.target === modal) {
        closeRatingModal();
    }
}

// 평가 제출
async function submitRating() {
    if (selectedRating === 0) {
        alert('별점을 선택해주세요.');
        return;
    }

    try {
        const response = await fetch('/zipkok/mission/rating', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                memberSeq : document.getElementById('memberSeq').value,
                missionSeq: missionSeq,
                rating: selectedRating,
                ratingComment: document.getElementById('ratingComment').value
            })
        });

        const result = await response.json();

        if (result.success) {
            alert(result.message);
            window.location.href = result.redirectUrl;
        } else {
            alert(result.message );
        }

    } catch (error) {
        console.error('Error:', error);
        alert('평가 등록 중 오류가 발생했습니다.');
    } finally {
        closeRatingModal()
        hideLoading();
    }
}
