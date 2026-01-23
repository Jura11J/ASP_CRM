(function () {
    document.addEventListener('click', function (e) {
        if (e.target.matches('[data-confirm]')) {
            const message = e.target.getAttribute('data-confirm');
            if (!confirm(message)) {
                e.preventDefault();
            }
        }
    });

    // auto hide bootstrap alerts after 4s
    setTimeout(function () {
        document.querySelectorAll('.alert').forEach(function (el) {
            el.classList.add('fade');
            el.classList.add('show');
            setTimeout(() => el.remove(), 4000);
        });
    }, 500);

    // auto-close dropdown when clicking inside form submit
    document.querySelectorAll('.dropdown-menu form').forEach(function (form) {
        form.addEventListener('submit', function () {
            const dropdown = bootstrap.Dropdown.getInstance(form.closest('.dropdown').querySelector('[data-bs-toggle="dropdown"]'));
            if (dropdown) dropdown.hide();
        });
    });
})();
