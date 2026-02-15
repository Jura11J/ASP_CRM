(() => {
    const root = document.getElementById('liveChatRoot');
    const messagesContainer = document.getElementById('chatMessages');
    const conversationList = document.getElementById('conversationList');
    const sendForm = document.getElementById('chatSendForm');
    const messageInput = document.getElementById('chatMessageInput');
    const sendBtn = document.getElementById('chatSendBtn');
    const refreshBtn = document.getElementById('chatRefreshBtn');
    const toggleBtn = document.getElementById('toggleConversationBtn');
    const closedAlert = document.getElementById('conversationClosedAlert');
    const tokenInput = document.querySelector('#liveChatTokenForm input[name="__RequestVerificationToken"]');

    if (!root || !messagesContainer || !conversationList || !tokenInput) {
        return;
    }

    const conversationId = Number(root.dataset.selectedId || '0');
    if (!conversationId) {
        return;
    }

    const token = tokenInput.value;
    let lastId = Number(root.dataset.lastId || '0');
    let isClosed = root.dataset.isClosed === 'true';

    const dateFormatter = new Intl.DateTimeFormat('pl-PL', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });

    const escapeHtml = (value) => {
        const div = document.createElement('div');
        div.textContent = value ?? '';
        return div.innerHTML;
    };

    const formatDate = (iso) => {
        if (!iso) return '';
        const date = new Date(iso);
        return Number.isNaN(date.getTime()) ? '' : dateFormatter.format(date);
    };

    const buildUrl = (path, params = {}) => {
        const url = new URL(path, window.location.origin);
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                url.searchParams.set(key, String(value));
            }
        });
        return `${url.pathname}${url.search}`;
    };

    const scrollToBottom = () => {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    };

    const clearPlaceholders = () => {
        const placeholders = messagesContainer.querySelectorAll(':scope > div:not([data-message-id])');
        placeholders.forEach((node) => node.remove());
    };

    const renderMessage = (message) => {
        const isOwn = Boolean(message.isOwn);
        const bubble = document.createElement('div');
        bubble.className = `chat-bubble ${isOwn ? 'align-self-end bg-primary text-white' : 'align-self-start bg-white border'} rounded-3 px-3 py-2 shadow-sm`;
        bubble.dataset.messageId = message.id;

        const sender = document.createElement('div');
        sender.className = `small fw-semibold ${isOwn ? 'text-white-50' : 'text-muted'}`;
        sender.textContent = message.senderLabel || (isOwn ? 'Ja' : 'Klient');

        const content = document.createElement('div');
        content.className = 'white-space-prewrap';
        content.textContent = message.content;

        const time = document.createElement('div');
        time.className = `text-end small ${isOwn ? 'text-white-50' : 'text-muted'}`;
        time.textContent = formatDate(message.sentAt);

        bubble.append(sender, content, time);
        return bubble;
    };

    const appendMessages = (messages) => {
        if (!Array.isArray(messages) || messages.length === 0) {
            return;
        }

        clearPlaceholders();

        let appended = 0;
        messages.forEach((message) => {
            if (!message || !message.id) return;
            if (messagesContainer.querySelector(`[data-message-id="${message.id}"]`)) {
                lastId = Math.max(lastId, Number(message.id));
                return;
            }
            const bubble = renderMessage(message);
            messagesContainer.appendChild(bubble);
            lastId = Math.max(lastId, Number(message.id));
            appended += 1;
        });

        if (appended > 0) {
            root.dataset.lastId = String(lastId);
            scrollToBottom();
        }
    };

    const setClosedState = (closed) => {
        isClosed = closed;
        root.dataset.isClosed = String(closed);

        if (closedAlert) {
            closedAlert.classList.toggle('d-none', !closed);
        }
        if (messageInput) {
            messageInput.disabled = closed;
        }
        if (sendBtn) {
            sendBtn.disabled = closed;
        }
        if (toggleBtn) {
            toggleBtn.textContent = closed ? 'Otwórz rozmowę' : 'Zamknij rozmowę';
        }
    };

    const fetchMessages = async () => {
        try {
            const url = buildUrl('/LiveChat/ConversationMessages', { id: conversationId, afterId: lastId });
            const response = await fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
            if (!response.ok) return;
            const data = await response.json();
            appendMessages(data.messages);
            if (typeof data.isClosed === 'boolean') {
                setClosedState(data.isClosed);
            }
        } catch (err) {
            console.warn('Nie udało się odświeżyć wiadomości.', err);
        }
    };

    const renderConversationItem = (convo) => {
        const isActive = Number(convo.conversationId) === conversationId;
        const preview = convo.lastMessagePreview && convo.lastMessagePreview.trim().length > 0
            ? convo.lastMessagePreview
            : '(brak wiadomości)';
        const unreadCount = Number(convo.unreadCount || 0);

        return `
<a class="list-group-item list-group-item-action d-flex flex-column gap-1 ${isActive ? 'active' : ''}"
   href="/LiveChat?conversationId=${convo.conversationId}"
   data-conversation-id="${convo.conversationId}"
   data-last-message-at="${convo.lastMessageAt}">
    <div class="d-flex justify-content-between align-items-center">
        <span class="fw-semibold">${escapeHtml(convo.customerName)}</span>
        <small class="${isActive ? 'text-white-50' : 'text-muted'}" data-role="time">${formatDate(convo.lastMessageAt)}</small>
    </div>
    <div class="d-flex justify-content-between align-items-center gap-2">
        <small class="text-truncate ${isActive ? 'text-white-50' : 'text-muted'}" data-role="preview">${escapeHtml(preview)}</small>
        <div class="d-flex align-items-center gap-1">
            ${convo.isClosed ? '<span class="badge rounded-pill text-bg-secondary" title="Zamknięta"><i class="bi bi-lock"></i></span>' : ''}
            <span class="badge rounded-pill text-bg-warning text-dark ${unreadCount > 0 ? '' : 'd-none'}" data-role="unread">${unreadCount}</span>
        </div>
    </div>
    <small class="${isActive ? 'text-white-50' : 'text-muted'}">${escapeHtml(convo.customerEmail || '')}</small>
</a>`;
    };

    const fetchSummary = async () => {
        try {
            const response = await fetch('/LiveChat/ConversationsSummary', { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
            if (!response.ok) return;
            const data = await response.json();
            if (!Array.isArray(data.conversations)) return;
            conversationList.innerHTML = data.conversations.length === 0
                ? '<div class="p-3 text-muted small">Brak aktywnych rozmów.</div>'
                : data.conversations.map(renderConversationItem).join('');
        } catch (err) {
            console.warn('Nie udało się odświeżyć listy rozmów.', err);
        }
    };

    const sendMessage = async (content) => {
        const formData = new FormData();
        formData.set('content', content);

        const response = await fetch(buildUrl('/LiveChat/SendMessage', { id: conversationId }), {
            method: 'POST',
            headers: {
                'RequestVerificationToken': token,
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: formData
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Błąd podczas wysyłania wiadomości.');
        }

        const data = await response.json();
        if (data?.message) {
            appendMessages([data.message]);
            fetchSummary();
        }
    };

    if (sendForm && messageInput) {
        sendForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            if (isClosed) {
                return;
            }

            const content = messageInput.value.trim();
            if (!content) {
                messageInput.focus();
                return;
            }

            sendBtn?.setAttribute('disabled', 'disabled');
            try {
                await sendMessage(content);
                messageInput.value = '';
                messageInput.focus();
            } catch (err) {
                console.error(err);
                window.alert('Nie udało się wysłać wiadomości.');
            } finally {
                if (!isClosed) {
                    sendBtn?.removeAttribute('disabled');
                }
            }
        });
    }

    refreshBtn?.addEventListener('click', () => {
        fetchMessages();
        fetchSummary();
    });

    toggleBtn?.addEventListener('click', async () => {
        try {
            const response = await fetch(buildUrl('/LiveChat/ToggleClosed', { id: conversationId }), {
                method: 'POST',
                headers: {
                    'RequestVerificationToken': token,
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            if (!response.ok) return;
            const data = await response.json();
            if (typeof data.isClosed === 'boolean') {
                setClosedState(data.isClosed);
                fetchSummary();
            }
        } catch (err) {
            console.warn('Nie udało się zmienić statusu rozmowy.', err);
        }
    });

    scrollToBottom();
    fetchSummary();
    fetchMessages();

    window.setInterval(fetchMessages, 4000);
    window.setInterval(fetchSummary, 10000);
})();
