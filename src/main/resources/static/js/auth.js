// JWT 인증 관련 JavaScript 함수들

// 로그인 함수
async function login(username, password) {
    try {
        const response = await fetch('/zipkok/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        const data = await response.json();
        
        if (response.ok) {
            // 토큰을 localStorage에 저장
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            localStorage.setItem('username', data.username);
            
            alert('로그인 성공!');
            updateLoginStatus();
        } else {
            alert('로그인 실패: ' + data.message);
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('로그인 중 오류가 발생했습니다.');
    }
}

// 토큰 갱신 함수
async function refreshToken() {
    try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
            throw new Error('리프레시 토큰이 없습니다.');
        }

        const response = await fetch('/zipkok/api/auth/refresh', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + refreshToken
            }
        });

        const data = await response.json();
        
        if (response.ok) {
            localStorage.setItem('accessToken', data.accessToken);
            alert('토큰이 갱신되었습니다.');
        } else {
            alert('토큰 갱신 실패: ' + data.message);
            logout();
        }
    } catch (error) {
        console.error('Token refresh error:', error);
        alert('토큰 갱신 중 오류가 발생했습니다.');
        logout();
    }
}

// 로그아웃 함수
async function logout() {
    try {
        const accessToken = localStorage.getItem('accessToken');
        if (accessToken) {
            await fetch('/zipkok/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + accessToken
                }
            });
        }
    } catch (error) {
        console.error('Logout error:', error);
    } finally {
        // localStorage에서 토큰 제거
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('username');
        
        alert('로그아웃되었습니다.');
        updateLoginStatus();
    }
}

// 인증된 요청 예시
async function makeAuthenticatedRequest() {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
        alert('로그인이 필요합니다.');
        return;
    }

    try {
        const response = await fetch('/zipkok/api/some-protected-endpoint', {
            headers: {
                'Authorization': 'Bearer ' + accessToken
            }
        });

        if (response.ok) {
            const data = await response.json();
            alert('인증된 요청 성공: ' + JSON.stringify(data));
        } else {
            alert('인증된 요청 실패');
        }
    } catch (error) {
        console.error('Authenticated request error:', error);
        alert('요청 중 오류가 발생했습니다.');
    }
}

// 로그인 상태 업데이트
function updateLoginStatus() {
    const username = localStorage.getItem('username');
    const loginStatus = document.getElementById('loginStatus');
    
    if (loginStatus) {
        if (username) {
            loginStatus.innerHTML = `
                <p>로그인된 사용자: ${username}</p>
                <button onclick="logout()">로그아웃</button>
                <button onclick="refreshToken()">토큰 갱신</button>
                <button onclick="makeAuthenticatedRequest()">인증된 요청 테스트</button>
            `;
        } else {
            loginStatus.innerHTML = `
                <p>로그인되지 않음</p>
                <form id="loginForm">
                    <input type="text" id="username" placeholder="사용자명" required><br>
                    <input type="password" id="password" placeholder="비밀번호" required><br>
                    <button type="submit">로그인</button>
                </form>
            `;
            
            // 로그인 폼 이벤트 리스너 추가
            document.getElementById('loginForm').addEventListener('submit', function(e) {
                e.preventDefault();
                const username = document.getElementById('username').value;
                const password = document.getElementById('password').value;
                login(username, password);
            });
        }
    }
}

// 페이지 로드 시 로그인 상태 확인
document.addEventListener('DOMContentLoaded', function() {
    updateLoginStatus();
});

