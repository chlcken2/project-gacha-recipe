(function() {
  const allowedChars = ['0','1','2','3','4','5','6','7','8','9','#','*','^',')','(','@'];
  let password = '';
  let showPassword = false;

  const displayEl = document.getElementById('password-display');
  const toggleBtn = document.getElementById('toggle-password');
  const loginBtn = document.getElementById('login-btn');
  const emailInput = document.getElementById('login-email');

  function renderPassword() {
    displayEl.textContent = '';
    password.split('').forEach(function(char) {
      const span = document.createElement('span');
      span.textContent = showPassword ? char : '•';
      span.className = 'text-green-400 text-2xl font-mono';
      displayEl.appendChild(span);
    });
  }

  function updateLoginBtn() {
    if (emailInput.value.trim() && password.length >= 4 && password.length <= 8) {
      loginBtn.classList.remove('pointer-events-none', 'opacity-60');
      loginBtn.href = '/';
    } else {
      loginBtn.classList.add('pointer-events-none', 'opacity-60');
      loginBtn.removeAttribute('href');
      loginBtn.href = '#';
    }
  }

  document.querySelectorAll('.keypad-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      var char = btn.textContent.trim();
      if (password.length < 8 && allowedChars.includes(char)) {
        password += char;
        renderPassword();
        updateLoginBtn();
      }
    });
  });

  document.getElementById('delete-password').addEventListener('click', function() {
    password = password.slice(0, -1);
    renderPassword();
    updateLoginBtn();
  });

  toggleBtn.addEventListener('click', function() {
    showPassword = !showPassword;
    toggleBtn.textContent = showPassword ? '非表示' : '表示';
    renderPassword();
  });

  emailInput.addEventListener('input', updateLoginBtn);
})();
