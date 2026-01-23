document.addEventListener('DOMContentLoaded', () => {
    hookDeleteModal();
    setupOrderItems();
});

function hookDeleteModal() {
    const modalEl = document.getElementById('confirmDeleteModal');
    if (!modalEl) return;

    modalEl.addEventListener('show.bs.modal', (event) => {
        const button = event.relatedTarget;
        const url = button?.getAttribute('data-url');
        const form = modalEl.querySelector('#deleteForm');
        if (form && url) {
            form.setAttribute('action', url);
        }
    });
}

function setupOrderItems() {
    const container = document.getElementById('orderItemsContainer');
    const template = document.getElementById('orderItemTemplate');
    const addBtn = document.getElementById('addOrderItem');

    if (!container || !template || !addBtn) return;

    let index = container.querySelectorAll('.order-item-row').length || 0;

    const bindRow = (row) => {
        const removeBtn = row.querySelector('.remove-item');
        if (!removeBtn) return;
        removeBtn.addEventListener('click', () => {
            const rows = container.querySelectorAll('.order-item-row');
            if (rows.length === 1) return;
            row.remove();
            toggleRemoveState();
        });
    };

    const toggleRemoveState = () => {
        const rows = container.querySelectorAll('.order-item-row');
        rows.forEach(r => {
            const btn = r.querySelector('.remove-item');
            if (btn) {
                btn.disabled = rows.length === 1;
            }
        });
    };

    container.querySelectorAll('.order-item-row').forEach(bindRow);
    toggleRemoveState();

    addBtn.addEventListener('click', () => {
        const html = template.innerHTML.replace(/__index__/g, index);
        const wrapper = document.createElement('div');
        wrapper.innerHTML = html.trim();
        const row = wrapper.firstElementChild;
        if (row) {
            container.appendChild(row);
            bindRow(row);
            toggleRemoveState();
            index++;
        }
    });
}
