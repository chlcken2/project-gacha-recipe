/* ═══════════════════════════════════════════════════════════
   CheerFX — Floating idol cheer text during wow sequence.
   Phrases appear at timed intervals, float upward, fade out.
   Requires: #wow-vfx to be visible (display:block) as container.
   ═══════════════════════════════════════════════════════════ */
var CheerFX = (function () {
  'use strict';

  // Phrases synced to wow sequence phases (ms from run() call)
  var LINES = [
    { text: 'ドキドキ…',       t:  310 },   // beat 1 — anticipation
    { text: 'Ganbatte!',       t:  700 },   // beat 2
    { text: 'がんばって！',     t: 1070 },   // beat 3 (final heartbeat)
    { text: '奇跡が起きる！',   t: 1460 },   // light gathers
    { text: 'Shinjite!',       t: 1980 },   // hold / pause
    { text: 'キミの運命は？！', t: 2560 },   // flash
  ];

  var COLORS = ['#f0abfc','#e879f9','#c084fc','#fb7185','#fda4af','#a5f3fc'];

  function spawn(parent, text, delay) {
    setTimeout(function () {
      var node = document.createElement('div');
      // Randomise position so phrases don't cluster
      var x   = 10 + Math.random() * 75;   // % from left
      var y   = 12 + Math.random() * 68;   // % from top
      var col = COLORS[Math.floor(Math.random() * COLORS.length)];
      var sz  = 12 + Math.floor(Math.random() * 8);  // 12–19 px

      node.textContent = text;
      node.style.cssText =
        'position:absolute;pointer-events:none;white-space:nowrap;z-index:9;' +
        'left:' + x + '%;top:' + y + '%;' +
        'color:' + col + ';font-size:' + sz + 'px;font-weight:900;' +
        'font-family:"Hiragino Kaku Gothic Pro","Meiryo","Yu Gothic",sans-serif;' +
        'text-shadow:0 0 10px ' + col + ',0 0 22px rgba(236,72,153,0.65);' +
        'animation:cheerFloat 1.75s ease-out forwards;';

      parent.appendChild(node);
      // Clean up after animation
      setTimeout(function () {
        if (node.parentNode) node.parentNode.removeChild(node);
      }, 1850);
    }, delay);
  }

  return {
    /**
     * Start spawning cheer phrases into `parent` element.
     * Call this at the beginning of WowSequence.run().
     * @param {HTMLElement} parent – the #wow-vfx container
     */
    run: function (parent) {
      LINES.forEach(function (l) { spawn(parent, l.text, l.t); });
    }
  };
}());
