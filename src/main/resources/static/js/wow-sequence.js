/* ═══════════════════════════════════════════════════════════
   WowSequence — Gacha pull dramatic reveal
   Sequence: darken → heartbeat × 3 → gather → pause → flash
   Total: 2900 ms, then calls onDone() (GachaMachine.open)
   ═══════════════════════════════════════════════════════════ */
var WowSequence = (function () {
  'use strict';

  var TOTAL_MS = 2900;

  function el(id) { return document.getElementById(id); }

  // Restart a CSS animation cleanly
  function restart(node, anim) {
    node.style.animation = 'none';
    void node.offsetWidth;          // force reflow
    node.style.animation = anim;
  }

  // Fire one heartbeat ring at `delay` ms
  function ring(node, delay, durationMs) {
    setTimeout(function () {
      restart(node,
        'wowRingExpand ' + durationMs + 'ms cubic-bezier(0.12,0,0.6,1) forwards');
      node.style.opacity = '1';
    }, delay);
  }

  // ── PUBLIC ──────────────────────────────────────────────
  function run(onDone) {
    var overlay  = el('gacha-overlay');
    var machBody = el('gacha-machine-body');
    var phaseMsg = el('gacha-phase-msg');
    var wowVfx   = el('wow-vfx');
    var orb      = el('wow-gather-orb');
    var flash    = el('wow-flash-layer');

    // ① Dark overlay visible; machine stays hidden during wow
    overlay.classList.remove('hidden');
    machBody.style.visibility = 'hidden';
    phaseMsg.style.visibility = 'hidden';
    wowVfx.style.display      = 'block';

    // ★ Sound + cheer text fire immediately
    if (typeof SoundManager !== 'undefined') SoundManager.playPull();
    if (typeof CheerFX      !== 'undefined') CheerFX.run(wowVfx);

    // ② Heartbeat × 3  (lub-dub timing, intensity rises each beat)
    //    Beat 1 — subtle
    ring(el('wow-ring-a'),  250, 720);
    ring(el('wow-ring-b'),  345, 720);
    //    Beat 2 — medium
    ring(el('wow-ring-a'),  630, 760);
    ring(el('wow-ring-b'),  720, 760);
    //    Beat 3 — strong (triple ring = max energy)
    ring(el('wow-ring-a'), 1010, 900);
    ring(el('wow-ring-b'), 1095, 900);
    ring(el('wow-ring-c'), 1055, 1000);

    // ③ Light gathers to center (starts right after final beat settles)
    //    wowGather: 0→38% contract, 38→80% hold (~0.55 s pause), 80→100% hold
    setTimeout(function () {
      restart(orb, 'wowGather 1300ms cubic-bezier(0.7,0,0.2,1) forwards');
    }, 1400);

    // ④ Flash + rare sound emerges from the gathered orb
    setTimeout(function () {
      restart(flash, 'wowFlashBurst 480ms ease-out forwards');
      if (typeof SoundManager !== 'undefined') SoundManager.playRare();
    }, 2500);

    // ⑤ Tear down wow layer → hand off to GachaMachine
    setTimeout(function () {
      wowVfx.style.display      = 'none';
      orb.style.animation       = '';
      flash.style.animation     = '';
      machBody.style.visibility = '';
      phaseMsg.style.visibility = '';
      onDone();
    }, TOTAL_MS);
  }

  return { run: run };
}());
