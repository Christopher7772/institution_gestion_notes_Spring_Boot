document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('form.card');
    const submitBtn = document.getElementById('submitBtn');
    const email = document.getElementById('email');
    const password = document.getElementById('password');

    form.addEventListener('submit', function (e) {
        const emailVal = email.value.trim();
        const pwVal = password.value;

        let ok = true;

        if (!emailVal || !pwVal) {
            alert('Email et mot de passe requis');
            ok = false;
        }

        if (!ok) {
            e.preventDefault();
            return;
        }

        // Show loading state
        submitBtn.disabled = true;
        submitBtn.textContent = 'Connexion...';
    });

    // Optional: toggle password visibility
    const toggleBtn = document.querySelector('.pass-toggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            if (password.type === 'password') {
                password.type = 'text';
                toggleBtn.textContent = '🙈';
            } else {
                password.type = 'password';
                toggleBtn.textContent = '👁️';
            }
        });
    }
});
