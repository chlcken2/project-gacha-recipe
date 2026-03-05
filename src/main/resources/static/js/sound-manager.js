/* ═══════════════════════════════════════════════════════════
   SoundManager — Web Audio synthesized sounds (no files)
   ── startBGM : idol-pop loop (BPM 144, G-major)
   ── playPull : tension build synced to wow heartbeats
   ── playRare : victory arpeggio at flash reveal
   All sounds are lazy-init and require prior user interaction.
   ═══════════════════════════════════════════════════════════ */
var SoundManager = (function () {
  'use strict';

  var _ac     = null;   // AudioContext (lazy)
  var _master = null;   // master GainNode
  var _bgm    = null;   // { stop } handle

  // ── AudioContext: lazy, auto-resume on suspension ──────────
  function ac() {
    var AC = window.AudioContext || window.webkitAudioContext;
    if (!AC) return null;
    if (!_ac) { try { _ac = new AC(); } catch (e) { return null; } }
    if (_ac.state === 'suspended') _ac.resume();
    return _ac;
  }

  // ── Master gain (shared bus) ────────────────────────────────
  function master() {
    var c = ac();
    if (!c) return null;
    if (!_master) {
      _master = c.createGain();
      _master.gain.value = 0.48;
      _master.connect(c.destination);
    }
    return _master;
  }

  // ── Low-level: schedule one oscillator note ─────────────────
  function note(type, freq, t, dur, vol, dst) {
    var c = ac(); if (!c) return;
    var o = c.createOscillator(), g = c.createGain();
    o.type = type;
    o.frequency.setValueAtTime(freq, t);
    g.gain.setValueAtTime(0.001, t);
    g.gain.linearRampToValueAtTime(vol, t + Math.min(0.018, dur * 0.1));
    g.gain.setValueAtTime(vol, t + dur * 0.76);
    g.gain.linearRampToValueAtTime(0.001, t + dur);
    o.connect(g); g.connect(dst);
    o.start(t); o.stop(t + dur + 0.01);
  }

  // ── Low-level: filtered noise burst ────────────────────────
  function noise(t, dur, vol, lp, dst) {
    var c = ac(); if (!c) return;
    var buf = c.createBuffer(1, ~~(c.sampleRate * dur), c.sampleRate);
    var d   = buf.getChannelData(0);
    for (var i = 0; i < d.length; i++) d[i] = Math.random() * 2 - 1;
    var src = c.createBufferSource(), f = c.createBiquadFilter(), g = c.createGain();
    f.type = 'lowpass'; f.frequency.value = lp;
    g.gain.setValueAtTime(vol, t);
    g.gain.linearRampToValueAtTime(0.001, t + dur);
    src.buffer = buf; src.connect(f); f.connect(g); g.connect(dst);
    src.start(t); src.stop(t + dur + 0.01);
  }

  // ════════════════════════════════════════════════════════════
  //  BGM — Idol-pop synth loop
  //  G major, BPM 144. Melody + harmony + bass + drums.
  // ════════════════════════════════════════════════════════════
  function startBGM() {
    var c = ac(), m = master();
    if (!c || !m) return null;

    // Bring master back to full if it was faded
    m.gain.cancelScheduledValues(c.currentTime);
    m.gain.setValueAtTime(0.48, c.currentTime);

    var alive = true;
    var bpm   = 144, b = 60 / bpm;

    // ── 16-note idol hook (G5 major — bright, punchy) ──
    var MEL = [
      [784,.5],[880,.5],[988,.5],[880,.5],
      [784,.75],[659,.25],[659,.5],[784,.5],
      [880,.5],[988,.5],[1175,.5],[988,.5],
      [880,.75],[784,.25],[784,.5],[659,.5]
    ];
    // Harmony: major 3rd above (× 1.26)
    var HAR = MEL.map(function(n){ return [n[0]*1.26, n[1]]; });
    // Bass: G2 / A2 rooting (root + relative)
    var BAS = [[98,2],[110,2],[98,2],[110,2]];

    var loopDur = MEL.reduce(function(s,n){ return s + n[1]*b; }, 0); // ≈3.33s

    function sched(t0) {
      if (!alive) return;

      // Melody — sawtooth + slight detune for shimmer
      var t = t0;
      MEL.forEach(function(n) {
        var d = n[1] * b;
        note('sawtooth', n[0],         t, d * 0.82, 0.065, m);
        note('sawtooth', n[0] * 1.004, t, d * 0.82, 0.030, m);
        t += d;
      });
      // Harmony — softer triangle
      t = t0;
      HAR.forEach(function(n) {
        note('triangle', n[0], t, n[1]*b*0.78, 0.028, m);
        t += n[1]*b;
      });
      // Bass — warm triangle
      t = t0;
      BAS.forEach(function(n) {
        note('triangle', n[0], t, n[1]*b*0.88, 0.19, m);
        t += n[1]*b;
      });
      // Hi-hat — noise click every half-beat (16 per loop)
      for (var i = 0; i < 16; i++) noise(t0 + i*b*0.5, 0.028, 0.048, 9000, m);
      // Kick — sine sweep on beats 0,2,4,6
      [0,2,4,6].forEach(function(beat) {
        var kt = t0 + beat*b;
        var ko = c.createOscillator(), kg = c.createGain();
        ko.type = 'sine';
        ko.frequency.setValueAtTime(115, kt);
        ko.frequency.exponentialRampToValueAtTime(38, kt + 0.13);
        kg.gain.setValueAtTime(0.38, kt);
        kg.gain.exponentialRampToValueAtTime(0.001, kt + 0.19);
        ko.connect(kg); kg.connect(m);
        ko.start(kt); ko.stop(kt + 0.22);
      });

      setTimeout(function(){ if (alive) sched(ac().currentTime); },
        (loopDur - 0.28) * 1000);
    }

    sched(c.currentTime + 0.05);

    _bgm = {
      stop: function() {
        alive = false;
        var now = ac().currentTime;
        m.gain.linearRampToValueAtTime(0, now + 0.55);
        setTimeout(function(){ m.gain.setValueAtTime(0.48, ac().currentTime); }, 700);
      }
    };
    return _bgm;
  }

  function stopBGM() { if (_bgm) { _bgm.stop(); _bgm = null; } }

  // ════════════════════════════════════════════════════════════
  //  SFX: Pull — tension build (start of wow sequence, 2.85s)
  //  Rising sweep + heartbeat kicks synced to visual rings.
  // ════════════════════════════════════════════════════════════
  function playPull() {
    var c = ac(), m = master();
    if (!c || !m) return;
    var now = c.currentTime;

    // Rising sawtooth sweep
    var sw = c.createOscillator(), sg = c.createGain();
    sw.type = 'sawtooth';
    sw.frequency.setValueAtTime(52, now);
    sw.frequency.linearRampToValueAtTime(680, now + 2.5);
    sg.gain.setValueAtTime(0.001, now);
    sg.gain.linearRampToValueAtTime(0.13, now + 0.4);
    sg.gain.linearRampToValueAtTime(0.21, now + 2.2);
    sg.gain.linearRampToValueAtTime(0.001, now + 2.85);
    sw.connect(sg); sg.connect(m);
    sw.start(now); sw.stop(now + 2.9);

    // Heartbeat kicks — synced to visual rings at 250 / 630 / 1010ms
    [0.25, 0.63, 1.01].forEach(function(t, i) {
      var ko = c.createOscillator(), kg = c.createGain();
      ko.type = 'sine';
      ko.frequency.setValueAtTime(125 + i * 22, now + t);
      ko.frequency.exponentialRampToValueAtTime(35, now + t + 0.14);
      kg.gain.setValueAtTime(0.42 + i * 0.13, now + t);
      kg.gain.exponentialRampToValueAtTime(0.001, now + t + 0.21);
      ko.connect(kg); kg.connect(m);
      ko.start(now + t); ko.stop(now + t + 0.24);
    });
  }

  // ════════════════════════════════════════════════════════════
  //  SFX: Rare — victory fanfare (at flash, 2.5s into wow)
  //  7-note rising arpeggio + shimmer + sustained chord.
  // ════════════════════════════════════════════════════════════
  function playRare() {
    var c = ac(), m = master();
    if (!c || !m) return;
    var now = c.currentTime;

    // Rapid ascending arpeggio (idol victory!)
    [523,659,784,988,1319,1568,2093].forEach(function(f, i) {
      var t = now + i * 0.062;
      note('square',   f,       t, 0.22, 0.09, m);
      note('triangle', f * 0.5, t, 0.22, 0.05, m);
    });

    // Shimmer noise at the top
    noise(now + 0.08, 0.42, 0.065, 14000, m);

    // Sustained C-major chord — holds through machine reveal
    [523, 659, 784, 1047].forEach(function(f) {
      var o = c.createOscillator(), g = c.createGain();
      o.type = 'sine'; o.frequency.value = f;
      g.gain.setValueAtTime(0.001, now + 0.38);
      g.gain.linearRampToValueAtTime(0.062, now + 0.50);
      g.gain.linearRampToValueAtTime(0.001, now + 1.45);
      o.connect(g); g.connect(m);
      o.start(now + 0.38); o.stop(now + 1.5);
    });
  }

  // ── Public API ──────────────────────────────────────────────
  return { startBGM: startBGM, stopBGM: stopBGM, playPull: playPull, playRare: playRare };
}());
