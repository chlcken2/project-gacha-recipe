(function() {
  var ingredients = [];
  var sauces = [];
  var gachaCount = 0;
  var maxDaily = 3;
  var currentRecipeId = null;

  var ingredientList    = document.getElementById('ingredient-list');
  var sauceList         = document.getElementById('sauce-list');
  var gachaCountEl      = document.getElementById('gacha-count');
  var ingredientCountEl = document.getElementById('ingredient-count');
  var sauceCountEl      = document.getElementById('sauce-count');
  var gachaLimitMsg     = document.getElementById('gacha-limit-msg');
  var gachaBtn          = document.getElementById('gacha-btn');
  var gachaBtnText      = document.getElementById('gacha-btn-text');
  var gachaWarning      = document.getElementById('gacha-warning');
  var gachaResult       = document.getElementById('gacha-result');
  var errorToast        = document.getElementById('error-toast');
  var errorToastMsg     = document.getElementById('error-toast-msg');

  function esc(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  // ═══════════════════════════════════════════════════════════════
  //  G A C H A   M A C H I N E   M O D U L E
  // ═══════════════════════════════════════════════════════════════
  var GachaMachine = (function() {
    var overlay      = document.getElementById('gacha-overlay');
    var phaseMsg     = document.getElementById('gacha-phase-msg');
    var lcdEl        = document.getElementById('gacha-lcd');
    var spinner      = document.getElementById('capsule-spinner');
    var dialEl       = document.getElementById('gacha-dial');
    var coinDisplay  = document.getElementById('coin-display');
    var particles    = document.getElementById('gacha-particles');
    var outputIdle   = document.getElementById('output-idle');
    var capsuleOut   = document.getElementById('capsule-result');
    var machineBody  = document.getElementById('gacha-machine-body');

    var musicRef   = null;
    var timers     = [];

    // ── Web Audio 8-bit BGM (Twinkle Twinkle - Public Domain) ──
    function startMusic() {
      var AC = window.AudioContext || window.webkitAudioContext;
      if (!AC) return null;
      try {
        var ctx = new AC();
        var master = ctx.createGain();
        master.gain.setValueAtTime(0.07, ctx.currentTime);
        master.connect(ctx.destination);
        var looping = true;

        var bpm    = 152;
        var beat   = 60 / bpm;
        // Twinkle Twinkle Little Star – public domain melody
        // [frequency_hz, duration_beats]
        var mel = [
          [523.25,0.5],[523.25,0.5],[784.00,0.5],[784.00,0.5],
          [880.00,0.5],[880.00,0.5],[784.00,1.0],
          [698.46,0.5],[698.46,0.5],[659.25,0.5],[659.25,0.5],
          [587.33,0.5],[587.33,0.5],[523.25,1.0],
          [784.00,0.5],[784.00,0.5],[698.46,0.5],[698.46,0.5],
          [659.25,0.5],[659.25,0.5],[587.33,1.0],
          [784.00,0.5],[784.00,0.5],[698.46,0.5],[698.46,0.5],
          [659.25,0.5],[659.25,0.5],[587.33,1.0],
          [523.25,0.5],[523.25,0.5],[784.00,0.5],[784.00,0.5],
          [880.00,0.5],[880.00,0.5],[784.00,1.0],
          [698.46,0.5],[698.46,0.5],[659.25,0.5],[659.25,0.5],
          [587.33,0.5],[587.33,0.5],[523.25,1.0]
        ];
        var totalDur = mel.reduce(function(s,n){ return s + n[1]*beat; }, 0);

        function scheduleLoop(t0) {
          if (!looping) return;
          var t = t0;
          mel.forEach(function(note) {
            var dur = note[1] * beat;
            var osc = ctx.createOscillator();
            osc.type = 'square';
            osc.frequency.setValueAtTime(note[0], t);
            var env = ctx.createGain();
            env.gain.setValueAtTime(0.001, t);
            env.gain.linearRampToValueAtTime(1, t + 0.015);
            env.gain.setValueAtTime(1, t + dur - 0.06);
            env.gain.linearRampToValueAtTime(0.001, t + dur - 0.01);
            osc.connect(env);
            env.connect(master);
            osc.start(t);
            osc.stop(t + dur);
            t += dur;
          });
          var tid = setTimeout(function(){
            if (looping) scheduleLoop(ctx.currentTime);
          }, (totalDur - 0.25) * 1000);
          timers.push(tid);
        }
        scheduleLoop(ctx.currentTime + 0.1);

        return {
          stop: function() {
            looping = false;
            master.gain.linearRampToValueAtTime(0, ctx.currentTime + 0.6);
            setTimeout(function(){ try { ctx.close(); } catch(e){} }, 800);
          }
        };
      } catch(e) { return null; }
    }

    // ── Sound Effects ──
    function sfx(type) {
      var AC = window.AudioContext || window.webkitAudioContext;
      if (!AC) return;
      try {
        var ctx = new AC();
        var g = ctx.createGain();
        g.connect(ctx.destination);
        if (type === 'coin') {
          var o = ctx.createOscillator();
          o.type = 'sine';
          o.frequency.setValueAtTime(900, ctx.currentTime);
          o.frequency.exponentialRampToValueAtTime(1500, ctx.currentTime + 0.12);
          g.gain.setValueAtTime(0.3, ctx.currentTime);
          g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.22);
          o.connect(g); o.start(ctx.currentTime); o.stop(ctx.currentTime + 0.22);
        } else if (type === 'fanfare') {
          [[523.25,0],[659.25,0.12],[783.99,0.24],[1046.5,0.36],[1318.5,0.50]].forEach(function(n){
            var o = ctx.createOscillator();
            o.type = 'square';
            o.frequency.setValueAtTime(n[0], ctx.currentTime + n[1]);
            var ng = ctx.createGain();
            ng.gain.setValueAtTime(0.15, ctx.currentTime + n[1]);
            ng.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + n[1] + 0.18);
            o.connect(ng); ng.connect(ctx.destination);
            o.start(ctx.currentTime + n[1]); o.stop(ctx.currentTime + n[1] + 0.18);
          });
        }
        setTimeout(function(){ try { ctx.close(); } catch(e){} }, 1200);
      } catch(e) {}
    }

    // ── Particle burst ──
    function burstParticles() {
      if (!particles) return;
      particles.innerHTML = '';
      var emojis = ['⭐','✨','🌟','💫','🎊','🎉','🎁','🍀','🎆','🎇'];
      for (var i = 0; i < 28; i++) {
        (function(idx){
          var p = document.createElement('div');
          p.textContent = emojis[idx % emojis.length];
          var tx = (Math.random() * 240 - 120) + 'px';
          var ty = (Math.random() * 220 - 110) + 'px';
          p.style.cssText = [
            'position:absolute','font-size:'+(14+Math.random()*24)+'px',
            'left:50%','top:50%','pointer-events:none',
            '--tx:'+tx,'--ty:'+ty,
            'animation:starFloat 1.1s ease-out '+(Math.random()*0.5)+'s forwards'
          ].join(';');
          particles.appendChild(p);
        })(i);
      }
    }

    // ── Dial spin ──
    function spinDial(turns) {
      if (!dialEl) return;
      var deg = 360 * (turns || 2);
      dialEl.style.transition = 'transform ' + (0.75 * (turns||2)) + 's cubic-bezier(0.4,0,0.2,1)';
      dialEl.style.transform  = 'rotate(' + deg + 'deg)';
      var t = setTimeout(function(){
        dialEl.style.transition = '';
        dialEl.style.transform  = 'rotate(0deg)';
      }, 820 * (turns||2));
      timers.push(t);
    }

    // ── Coin drop ──
    function dropCoin() {
      if (!coinDisplay) return;
      sfx('coin');
      coinDisplay.style.animation = 'none';
      void coinDisplay.offsetWidth;
      coinDisplay.style.animation = 'coinFall 0.65s ease-in-out';
      var t = setTimeout(function(){ coinDisplay.style.animation = ''; }, 750);
      timers.push(t);
    }

    // ── Machine shake ──
    function shake() {
      if (!machineBody) return;
      machineBody.style.animation = 'none';
      void machineBody.offsetWidth;
      machineBody.style.animation = 'shakeX 0.45s ease-in-out';
      var t = setTimeout(function(){ machineBody.style.animation = ''; }, 550);
      timers.push(t);
    }

    // ── Phase transitions ──
    var phaseData = [
      { msg:'🎰 マシン起動中...',           lcd:'▶ LOADING... ◀',   speed:'4s'   },
      { msg:'💰 コインを投入しています...', lcd:'💰 INSERT COIN 💰', speed:'3.5s' },
      { msg:'🌀 ハンドルを回しています...', lcd:'🌀 TURNING... 🌀',  speed:'1.5s' },
      { msg:'🎲 カプセルが転がっています！', lcd:'🎲 GACHA!!! 🎲',   speed:'0.8s' },
      { msg:'✨ もうすぐレシピが出ます...',  lcd:'✨ ALMOST... ✨',   speed:'0.8s' },
      { msg:'🎉 レシピが出た！',             lcd:'🎉 SUCCESS!! 🎉',  speed:'0.5s' }
    ];
    function setPhase(idx) {
      var p = phaseData[Math.min(idx, phaseData.length - 1)];
      if (phaseMsg) phaseMsg.textContent = p.msg;
      if (lcdEl)    lcdEl.textContent    = p.lcd;
      if (spinner)  spinner.style.animationDuration = p.speed;
    }

    // ── Open ──
    function open() {
      if (!overlay) return;
      overlay.classList.remove('hidden');
      setPhase(0);
      musicRef = startMusic();

      var t1 = setTimeout(function(){ dropCoin(); setPhase(1); }, 600);
      var t2 = setTimeout(function(){ dropCoin(); }, 1300);
      var t3 = setTimeout(function(){ spinDial(2); shake(); setPhase(2); }, 2300);
      var t4 = setTimeout(function(){ spinDial(3); shake(); setPhase(3); }, 3900);
      var t5 = setTimeout(function(){ setPhase(4); shake(); }, 5600);
      timers.push(t1,t2,t3,t4,t5);
    }

    // ── Reveal Result (returns Promise) ──
    function revealResult() {
      setPhase(5);
      if (musicRef) { musicRef.stop(); musicRef = null; }
      sfx('fanfare');
      burstParticles();
      shake();

      if (outputIdle)  outputIdle.style.display  = 'none';
      if (capsuleOut) {
        capsuleOut.style.display   = 'block';
        capsuleOut.style.animation = 'none';
        void capsuleOut.offsetWidth;
        capsuleOut.style.animation = 'capsuleReveal 0.9s cubic-bezier(0.175,0.885,0.32,1.275) forwards';
      }

      return new Promise(function(resolve) {
        var t = setTimeout(function(){
          overlay.classList.add('hidden');
          _reset();
          resolve();
        }, 2400);
        timers.push(t);
      });
    }

    // ── Close (on error) ──
    function close() {
      timers.forEach(function(t){ clearTimeout(t); });
      timers = [];
      if (musicRef) { musicRef.stop(); musicRef = null; }
      overlay.classList.add('hidden');
      _reset();
    }

    function _reset() {
      timers.forEach(function(t){ clearTimeout(t); });
      timers = [];
      if (capsuleOut) { capsuleOut.style.display = 'none'; capsuleOut.style.animation = ''; }
      if (outputIdle) outputIdle.style.display = '';
      if (spinner)    spinner.style.animationDuration = '4s';
      if (dialEl)     { dialEl.style.transition = ''; dialEl.style.transform = ''; }
      if (particles)  particles.innerHTML = '';
    }

    return { open: open, revealResult: revealResult, close: close };
  })();

  // ═══════════════════════════════════════════════════════════════
  //  D A I L Y   C O U N T
  // ═══════════════════════════════════════════════════════════════
  function loadDailyCount() {
    fetch('/gacha/count')
      .then(function(r){ return r.json(); })
      .then(function(res){
        if (!res.success) return;
        var d = res.data;
        gachaCount = d.count;
        maxDaily   = d.max;
        gachaCountEl.textContent = gachaCount;
        if (!d.canGenerate) gachaLimitMsg.classList.remove('hidden');
        updateGachaBtn();
      })
      .catch(function(){});
  }

  function showError(msg) {
    errorToastMsg.textContent = msg;
    errorToast.classList.remove('hidden');
    setTimeout(function(){ errorToast.classList.add('hidden'); }, 6000);
  }

  // ═══════════════════════════════════════════════════════════════
  //  I N G R E D I E N T  /  S A U C E   R E N D E R I N G
  // ═══════════════════════════════════════════════════════════════
  function renderIngredientList() {
    if (ingredients.length === 0) {
      ingredientList.innerHTML = '<p class="text-center text-gray-400 py-8">冷蔵庫が空っぽです 🥲</p>';
    } else {
      ingredientList.innerHTML = ingredients.map(function(item){
        return '<div class="flex items-center justify-between bg-amber-50 p-3 rounded-lg">'
          + '<span>' + esc(item.name) + ' - ' + esc(item.amount) + item.unit + '</span>'
          + '<button type="button" class="remove-ingredient text-red-500 hover:text-red-700" data-id="'+item.id+'">'
          + '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
          + '</button></div>';
      }).join('');
      ingredientList.querySelectorAll('.remove-ingredient').forEach(function(btn){
        btn.addEventListener('click', function(){
          ingredients = ingredients.filter(function(i){ return i.id !== btn.getAttribute('data-id'); });
          renderIngredientList(); updateGachaBtn();
        });
      });
    }
    ingredientCountEl.textContent = ingredients.length + '個';
  }

  function renderSauceList() {
    if (sauces.length === 0) {
      sauceList.innerHTML = '<p class="text-center text-gray-400 py-8">調味料が空です (任意)</p>';
    } else {
      sauceList.innerHTML = sauces.map(function(item){
        return '<div class="flex items-center justify-between bg-orange-50 p-3 rounded-lg">'
          + '<span>' + esc(item.name) + ' - ' + esc(item.amount) + item.unit + '</span>'
          + '<button type="button" class="remove-sauce text-red-500 hover:text-red-700" data-id="'+item.id+'">'
          + '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>'
          + '</button></div>';
      }).join('');
      sauceList.querySelectorAll('.remove-sauce').forEach(function(btn){
        btn.addEventListener('click', function(){
          sauces = sauces.filter(function(s){ return s.id !== btn.getAttribute('data-id'); });
          renderSauceList();
        });
      });
    }
    sauceCountEl.textContent = sauces.length + '個';
  }

  function updateGachaBtn() {
    var canGen = gachaCount < maxDaily && ingredients.length > 0;
    gachaBtn.disabled = !canGen;
    if (ingredients.length === 0) gachaWarning.classList.remove('hidden');
    else                          gachaWarning.classList.add('hidden');
  }

  // ═══════════════════════════════════════════════════════════════
  //  A D D   I N G R E D I E N T  /  S A U C E
  // ═══════════════════════════════════════════════════════════════
  document.getElementById('add-ingredient').addEventListener('click', function(){
    var name   = document.getElementById('ingredient-name').value.trim();
    var amount = document.getElementById('ingredient-amount').value.trim();
    var unit   = document.getElementById('ingredient-unit').value;
    if (name && amount) {
      ingredients.push({ id: Date.now().toString(), name: name, amount: amount, unit: unit });
      document.getElementById('ingredient-name').value   = '';
      document.getElementById('ingredient-amount').value = '';
      renderIngredientList(); updateGachaBtn();
    }
  });
  document.getElementById('ingredient-name').addEventListener('keypress', function(e){
    if (e.key === 'Enter') document.getElementById('add-ingredient').click();
  });

  document.getElementById('add-sauce').addEventListener('click', function(){
    var name   = document.getElementById('sauce-name').value.trim();
    var amount = document.getElementById('sauce-amount').value.trim();
    var unit   = document.getElementById('sauce-unit').value;
    if (name && amount) {
      sauces.push({ id: Date.now().toString(), name: name, amount: amount, unit: unit });
      document.getElementById('sauce-name').value   = '';
      document.getElementById('sauce-amount').value = '';
      renderSauceList();
    }
  });
  document.getElementById('sauce-name').addEventListener('keypress', function(e){
    if (e.key === 'Enter') document.getElementById('add-sauce').click();
  });

  // ═══════════════════════════════════════════════════════════════
  //  G A C H A   G E N E R A T E
  // ═══════════════════════════════════════════════════════════════
  gachaBtn.addEventListener('click', function(){
    if (gachaCount >= maxDaily || ingredients.length === 0) return;

    gachaResult.classList.add('hidden');
    gachaBtn.disabled = true;
    gachaBtnText.textContent = '🍳 料理中...';

    // Open machine overlay immediately
    GachaMachine.open();

    var MIN_ANIM_MS = 5800;  // minimum exciting wait time
    var apiDone  = false;
    var animDone = false;
    var apiPayload = null;
    var apiErr     = null;

    function tryReveal() {
      if (!apiDone || !animDone) return;
      if (apiErr) {
        GachaMachine.close();
        showError(apiErr);
        gachaBtn.disabled = (gachaCount >= maxDaily || ingredients.length === 0);
        gachaBtnText.textContent = '🎲 食事ガチャを回す！';
        return;
      }
      // Reveal capsule, then show result card
      GachaMachine.revealResult().then(function(){
        gachaCount++;
        gachaCountEl.textContent = gachaCount;
        if (gachaCount >= maxDaily) gachaLimitMsg.classList.remove('hidden');
        currentRecipeId = apiPayload.id;
        showResult(apiPayload);
        gachaBtn.disabled = (gachaCount >= maxDaily || ingredients.length === 0);
        gachaBtnText.textContent = '🎲 食事ガチャを回す！';
      });
    }

    // Minimum animation timer (excitement buffer)
    var animTimer = setTimeout(function(){
      animDone = true;
      tryReveal();
    }, MIN_ANIM_MS);

    // API call
    var purpose    = document.querySelector('input[name="purpose"]:checked').value;
    var cuisine    = document.querySelector('input[name="cuisine"]:checked').value;
    var difficulty = document.querySelector('input[name="difficulty"]:checked').value;

    fetch('/gacha/generate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ingredients: ingredients.map(function(i){ return { name:i.name, amount:i.amount, unit:i.unit }; }),
        sauces:      sauces.map(function(s){ return { name:s.name, amount:s.amount, unit:s.unit }; }),
        purpose: purpose, cuisine: cuisine, difficulty: difficulty
      })
    })
    .then(function(r){ return r.json(); })
    .then(function(res){
      if (!res.success) {
        apiErr = res.message || 'レシピ生成に失敗しました。';
      } else {
        apiPayload = res.data.recipe;
      }
      apiDone = true;
      tryReveal();
    })
    .catch(function(){
      apiErr = '現在サービスが遅延しております。再読み込みしてからご利用ください。';
      apiDone = true;
      tryReveal();
    });
  });

  // ═══════════════════════════════════════════════════════════════
  //  S H O W   R E S U L T   C A R D
  // ═══════════════════════════════════════════════════════════════
  function showResult(recipe) {
    gachaResult.classList.remove('hidden');

    document.getElementById('result-image').src = recipe.titleImage || '';
    document.getElementById('result-title').textContent    = recipe.title || '';
    document.getElementById('result-summary').textContent  = recipe.summary || '';
    document.getElementById('result-difficulty').textContent = '難易度: ' + (recipe.difficultyLabel || recipe.difficulty || '');
    document.getElementById('result-time-text').textContent  = (recipe.cookingTime || 0) + '分';

    // Nutrients
    var nutrientsEl = document.getElementById('result-nutrients');
    nutrientsEl.innerHTML = '';
    if (recipe.nutrients) {
      var n = recipe.nutrients;
      [
        { label:'カロリー',   value:(n.calories||0)+'kcal', color:'bg-red-50 text-red-700'    },
        { label:'タンパク質', value:(n.protein ||0)+'g',    color:'bg-blue-50 text-blue-700'  },
        { label:'脂質',       value:(n.fat     ||0)+'g',    color:'bg-yellow-50 text-yellow-700' },
        { label:'炭水化物',   value:(n.carbs   ||0)+'g',    color:'bg-green-50 text-green-700' }
      ].forEach(function(item){
        nutrientsEl.innerHTML += '<div class="'+item.color+' p-2 rounded-lg text-center"><div class="text-xs">'+item.label+'</div><div class="font-bold text-sm">'+item.value+'</div></div>';
      });
    }

    // Ingredients
    var ingEl = document.getElementById('result-ingredients-list');
    ingEl.innerHTML = '';
    if (recipe.ingredients) {
      recipe.ingredients.forEach(function(ing){
        ingEl.innerHTML += '<span class="px-2 py-1 bg-amber-100 text-amber-800 rounded-full text-xs">'+esc(ing.name)+' '+(ing.amount||'')+(ing.unit||'')+'</span>';
      });
    }

    document.getElementById('btn-detail').href = '/gacha/recipe/' + recipe.id;
    gachaResult.scrollIntoView({ behavior:'smooth', block:'center' });
  }

  // ═══════════════════════════════════════════════════════════════
  //  P U B L I S H   B U T T O N S
  // ═══════════════════════════════════════════════════════════════
  document.getElementById('btn-private').addEventListener('click', function(){
    if (!currentRecipeId) return;
    fetch('/gacha/publish/'+currentRecipeId+'?isPublic=false', { method:'POST' })
      .then(function(r){ return r.json(); })
      .then(function(res){
        if (res.success) window.location.href = '/mypage';
        else showError(res.message);
      })
      .catch(function(){ showError('保存に失敗しました。'); });
  });

  document.getElementById('btn-public').addEventListener('click', function(){
    if (!currentRecipeId) return;
    fetch('/gacha/publish/'+currentRecipeId+'?isPublic=true', { method:'POST' })
      .then(function(r){ return r.json(); })
      .then(function(res){
        if (res.success) alert('みんなの食卓に登録されました！🎉');
        else showError(res.message);
      })
      .catch(function(){ showError('登録に失敗しました。'); });
  });

  // ═══════════════════════════════════════════════════════════════
  //  I N I T
  // ═══════════════════════════════════════════════════════════════
  renderIngredientList();
  renderSauceList();
  updateGachaBtn();
  loadDailyCount();
})();
