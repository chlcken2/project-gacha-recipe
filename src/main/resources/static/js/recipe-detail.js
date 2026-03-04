(function() {
  var likeCount = 245;
  var isLiked = false;
  var likeBtn = document.getElementById('like-btn');
  var likeBtnIcon = document.getElementById('like-btn-icon');
  var likeBtnText = document.getElementById('like-btn-text');
  var likeCountEl = document.getElementById('like-count');
  var commentList = document.getElementById('comment-list');
  var commentCount = document.getElementById('comment-count');
  var newCommentInput = document.getElementById('new-comment');
  var commentSubmit = document.getElementById('comment-submit');

  function updateLikeButton() {
    likeCountEl.textContent = likeCount;
    if (likeBtnText) likeBtnText.textContent = isLiked ? 'いいね取消' : 'いいね';
    likeBtn.className = 'py-3 rounded-lg flex items-center justify-center gap-2 transition-colors ' + (isLiked ? 'bg-red-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200');
    if (likeBtnIcon) {
      likeBtnIcon.setAttribute('fill', isLiked ? 'currentColor' : 'none');
    }
  }

  if (likeBtn) {
    likeBtn.addEventListener('click', function() {
      isLiked = !isLiked;
      likeCount += isLiked ? 1 : -1;
      updateLikeButton();
    });
  }

  function addComment() {
    var text = newCommentInput.value.trim();
    if (!text) return;
    var div = document.createElement('div');
    div.className = 'bg-gray-50 p-4 rounded-lg';
    div.innerHTML = '<div class="flex items-center justify-between mb-2"><span class="font-medium text-amber-700">私</span><span class="text-sm text-gray-500">たった今</span></div><p class="text-gray-700">' + text.replace(/</g, '&lt;') + '</p>';
    commentList.insertBefore(div, commentList.firstChild);
    commentCount.textContent = parseInt(commentCount.textContent, 10) + 1;
    newCommentInput.value = '';
  }

  if (commentSubmit) commentSubmit.addEventListener('click', addComment);
  if (newCommentInput) {
    newCommentInput.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') addComment();
    });
  }
})();
