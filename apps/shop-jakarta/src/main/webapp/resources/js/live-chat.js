(() => {
    const messagesContainer = document.getElementById('shopChatMessages');
    const form = document.getElementById('shopChatForm');
    const input = document.getElementById('shopChatInput');
    const sendBtn = document.getElementById('shopChatSendBtn');
    const refreshBtn = document.getElementById('shopChatRefreshBtn');
    const closedAlert = document.getElementById('shopChatClosedAlert');
    const closedBadge = document.getElementById('shopChatClosedBadge');
    const unreadBadge = document.getElementById('shopChatUnreadBadge');

    if (!messagesContainer || !form || !input) {
        return;
    }

    let conversationId = Number(messagesContainer.dataset.conversationId || '0');
    if (!conversationId) {
        return;
    }

    let lastId = Number(messagesContainer.dataset.lastId || '0');

    const formatter = new Intl.DateTimeFormat('pl-PL', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });

    const formatDate = (iso) => {
        if (!iso) return '';
        const date = new Date(iso);
        return Number.isNaN(date.getTime()) ? '' : formatter.format(date);
    };

    const scrollToBottom = () => {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    };

    const clearPlaceholders = () => {
        const placeholders = messagesContainer.querySelectorAll(':scope > div:not([data-message-id])');
        placeholders.forEach((node) => node.remove());
    };

    const renderMessage = (message) => {
        const own = Boolean(message.own);
        const bubble = document.createElement('div');
        bubble.className = `chat-bubble ${own ? 'own align-self-end bg-primary text-white' : 'align-self-start bg-white border'} rounded-3 px-3 py-2 shadow-sm`;
        bubble.dataset.messageId = message.id;

        const sender = document.createElement('div');
        sender.className = `small fw-semibold ${own ? 'text-white-50' : 'text-muted'}`;
        sender.textContent = message.senderLabel || (own ? 'Ja' : 'CRM');

        const content = document.createElement('div');
        content.className = 'white-space-prewrap';
        content.textContent = message.content || '';

        const time = document.createElement('div');
        time.className = `text-end small ${own ? 'text-white-50' : 'text-muted'}`;
        time.dataset.role = 'time';
        time.dataset.sentAt = message.sentAt || '';
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
            if (!message?.id) return;
            if (messagesContainer.querySelector(`[data-message-id="${message.id}"]`)) {
                lastId = Math.max(lastId, Number(message.id));
                return;
            }
            messagesContainer.appendChild(renderMessage(message));
            lastId = Math.max(lastId, Number(message.id));
            appended += 1;
        });

        if (appended > 0) {
            messagesContainer.dataset.lastId = String(lastId);
            scrollToBottom();
        }
    };

    const updateSummary = (conversation) => {
        if (!conversation) return;
        conversationId = Number(conversation.conversationId || conversationId);
        const closed = Boolean(conversation.isClosed);
        messagesContainer.dataset.isClosed = String(closed);

        if (closedAlert) {
            closedAlert.classList.toggle('d-none', !closed);
        }
        if (closedBadge) {
            closedBadge.classList.toggle('d-none', !closed);
        }
        if (unreadBadge) {
            const unread = Number(conversation.unreadCount || 0);
            unreadBadge.textContent = String(unread);
            unreadBadge.classList.toggle('d-none', unread <= 0);
        }

        input.placeholder = closed
            ? 'Rozmowa jest zamknięta. Wyślij wiadomość, aby ją otworzyć...'
            : 'Opisz problem lub zadaj pytanie...';
    };

    const formatExistingMessages = () => {
        const timeNodes = messagesContainer.querySelectorAll('[data-role="time"][data-sent-at]');
        timeNodes.forEach((node) => {
            const iso = node.getAttribute('data-sent-at');
            node.textContent = formatDate(iso);
        });
    };

    const fetchMessages = async () => {
        try {
            const url = new URL('/api/chat/messages', window.location.origin);
            url.searchParams.set('conversationId', String(conversationId));
            if (lastId > 0) {
                url.searchParams.set('afterId', String(lastId));
            }

            const response = await fetch(`${url.pathname}${url.search}`, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            if (!response.ok) return;
            const data = await response.json();
            updateSummary(data.conversation);
            appendMessages(data.messages);
        } catch (err) {
            console.warn('Nie udało się pobrać wiadomości czatu.', err);
        }
    };

    const sendMessage = async (content) => {
        const params = new URLSearchParams();
        params.set('content', content);
        params.set('conversationId', String(conversationId));

        const response = await fetch('/api/chat/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: params
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Błąd wysyłania wiadomości');
        }

        const data = await response.json();
        updateSummary(data.conversation);
        if (data.message) {
            appendMessages([data.message]);
        }
    };

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const content = input.value.trim();
        if (!content) {
            input.focus();
            return;
        }
        sendBtn?.setAttribute('disabled', 'disabled');
        try {
            await sendMessage(content);
            input.value = '';
            input.focus();
        } catch (err) {
            console.error(err);
            window.alert('Nie udało się wysłać wiadomości.');
        } finally {
            sendBtn?.removeAttribute('disabled');
        }
    });

    refreshBtn?.addEventListener('click', () => fetchMessages());

    formatExistingMessages();
    scrollToBottom();
    fetchMessages();
    window.setInterval(fetchMessages, 4000);
})();
