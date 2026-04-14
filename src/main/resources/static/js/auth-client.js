window.AuthClient = (() => {
    const TOKEN_KEY = 'resume_agent_token';
    const USERNAME_KEY = 'resume_agent_username';
    const USER_ID_KEY = 'resume_agent_user_id';
    const TOKEN_COOKIE_NAME = 'RA_TOKEN';

    function getCookie(name) {
        const pair = document.cookie
            .split(';')
            .map((item) => item.trim())
            .find((item) => item.startsWith(name + '='));
        return pair ? decodeURIComponent(pair.substring(name.length + 1)) : null;
    }

    function setCookie(name, value, maxAgeSeconds) {
        const maxAge = Number.isFinite(maxAgeSeconds) ? `; max-age=${Math.max(1, Math.floor(maxAgeSeconds))}` : '';
        document.cookie = `${name}=${encodeURIComponent(value)}; path=/${maxAge}; SameSite=Lax`;
    }

    function clearCookie(name) {
        document.cookie = `${name}=; path=/; max-age=0; SameSite=Lax`;
    }

    function getToken() {
        return localStorage.getItem(TOKEN_KEY) || getCookie(TOKEN_COOKIE_NAME);
    }

    function setToken(token, expiresAt) {
        if (!token) {
            clearToken();
            return;
        }
        localStorage.setItem(TOKEN_KEY, token);
        let maxAgeSeconds = 7 * 24 * 3600;
        if (expiresAt) {
            const expireMs = new Date(expiresAt).getTime() - Date.now();
            if (expireMs > 0) {
                maxAgeSeconds = Math.floor(expireMs / 1000);
            }
        }
        setCookie(TOKEN_COOKIE_NAME, token, maxAgeSeconds);
    }

    function clearToken() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USERNAME_KEY);
        localStorage.removeItem(USER_ID_KEY);
        clearCookie(TOKEN_COOKIE_NAME);
    }

    function loginRedirectUrl() {
        return `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
    }

    function requireAuth() {
        if (!getToken()) {
            window.location.href = loginRedirectUrl();
            return false;
        }
        return true;
    }

    function getAuthHeaders(extraHeaders) {
        const headers = Object.assign({}, extraHeaders || {});
        const token = getToken();
        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }
        return headers;
    }

    async function authFetch(url, options) {
        const opts = Object.assign({}, options || {});
        opts.headers = getAuthHeaders(opts.headers);
        const response = await fetch(url, opts);
        if (response.status === 401) {
            clearToken();
            window.location.href = loginRedirectUrl();
            throw new Error('登录状态失效，请重新登录');
        }
        return response;
    }

    async function fetchMe() {
        const token = getToken();
        if (!token) {
            return null;
        }
        const response = await fetch('/api/auth/me', {
            method: 'GET',
            headers: getAuthHeaders()
        });
        if (response.status === 401) {
            return null;
        }
        if (!response.ok) {
            throw new Error('获取用户信息失败');
        }
        const data = await response.json();
        if (data.username) {
            localStorage.setItem(USERNAME_KEY, data.username);
        }
        if (data.id !== undefined && data.id !== null) {
            localStorage.setItem(USER_ID_KEY, String(data.id));
        }
        return data;
    }

    async function logout() {
        const token = getToken();
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: token ? { Authorization: `Bearer ${token}` } : {}
            });
        } catch (e) {
            // ignore network errors on logout cleanup
        } finally {
            clearToken();
            window.location.href = '/login';
        }
    }

    function renderAuthActions() {
        const container = document.getElementById('authActions');
        if (!container) {
            return;
        }
        const token = getToken();
        const username = localStorage.getItem(USERNAME_KEY);
        if (!token) {
            container.innerHTML = `
                <a href="/login" class="auth-link">登录 / 注册</a>
            `;
            return;
        }
        container.innerHTML = `
            <a href="/profile" class="auth-link">${username || '个人中心'}</a>
            <button id="logoutBtn" class="auth-link auth-button" type="button">退出</button>
        `;
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', logout);
        }
    }

    async function initPage(options) {
        const opts = options || {};
        if (opts.requireAuth && !requireAuth()) {
            return;
        }
        renderAuthActions();
        if (getToken()) {
            try {
                const me = await fetchMe();
                if (!me) {
                    clearToken();
                    if (opts.requireAuth) {
                        window.location.href = loginRedirectUrl();
                        return;
                    }
                }
            } catch (e) {
                if (opts.requireAuth) {
                    clearToken();
                    window.location.href = loginRedirectUrl();
                    return;
                }
            }
            renderAuthActions();
        }
    }

    return {
        initPage,
        requireAuth,
        authFetch,
        fetchMe,
        getToken,
        setToken,
        clearToken,
        logout
    };
})();
