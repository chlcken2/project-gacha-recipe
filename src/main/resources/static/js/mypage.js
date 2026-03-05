(function() {
  var activeTab = 'recipes';
  var viewMode = 'grid';

  var tabContentMap = {
    recipes: { grid: 'view-grid', list: 'view-list' },
    liked: { grid: 'view-liked-grid', list: 'view-liked-list' }
  };

  function showTab(tab) {
    activeTab = tab;
    document.querySelectorAll('.mypage-tab-btn').forEach(function(btn) {
      if (btn.getAttribute('data-tab') === tab) {
        btn.classList.remove('mypage-tab-inactive');
        btn.classList.add('mypage-tab-active');
      } else {
        btn.classList.remove('mypage-tab-active');
        btn.classList.add('mypage-tab-inactive');
      }
    });
    updateView();
  }

  function updateView() {
    var content = tabContentMap[activeTab];
    if (!content) return;
    document.querySelectorAll('[id^="view-"]').forEach(function(el) {
      el.classList.add('hidden');
    });
    var targetId = viewMode === 'grid' ? content.grid : content.list;
    var target = document.getElementById(targetId);
    if (target) target.classList.remove('hidden');
  }

  document.querySelectorAll('.mypage-tab-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      showTab(btn.getAttribute('data-tab'));
    });
  });

  document.querySelectorAll('.view-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      viewMode = btn.getAttribute('data-view');
      document.querySelectorAll('.view-btn').forEach(function(b) {
        b.classList.remove('view-active');
        b.classList.add('view-inactive');
      });
      btn.classList.remove('view-inactive');
      btn.classList.add('view-active');
      updateView();
    });
  });
})();
