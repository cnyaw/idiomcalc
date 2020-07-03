//
// ic.js
// idiomcalc web.
//
// 2020/7/3 Waync created.
//

var CHECK_MARK = "<span style='color:lawngreen'><big><big>&#x2714;</big></big></span>";
var ERROR_MARK = "<span style='color:tomato'><big><big>&#x2715;</big></big></span>";
var EMPTY_CHAR = String.fromCharCode(0x25a1);

var quizs = null;
var output = document.getElementById('output');
var kb_shit = 0;
var btn1 = document.getElementById('btn1');
var btn2 = document.getElementById('btn2');
var btn5 = document.getElementById('btn5');
var btn6 = document.getElementById('btn6');

function addNewQuiz(s) {
  s = transMathOp(s);
  var ch = s.substring(0, s.indexOf('#')).split('');
  for (var i = 0; i < ch.length; i++) {
    if ('(' == ch[i]) {
      ch[i + 1] = EMPTY_CHAR;
    }
  }
  var qs = ch.join('');
  writeOutput(qs);
  quizs = s;
}

function checkQuiz() {
  var pre = output.lastElementChild;
  if (-1 != pre.innerHTML.indexOf(EMPTY_CHAR)) {
    return;
  }
  var s = validateQuiz(pre) + getQuizTips();
  pre.innerHTML = s;
  pickQuit(Math.floor(Math.random() * 4));
}

function fillNum(btn) {
  var pre = output.lastElementChild;
  var ch = pre.innerHTML.split('');
  for (var i = 0; i < ch.length; i++) {
    if ('(' == ch[i] && EMPTY_CHAR == ch[i + 1]) {
      ch[i + 1] = btn.innerHTML;
      break;
    }
  }
  pre.innerHTML = ch.join('');
  checkQuiz();
}

function getQuizTips() {
  var tips = quizs.substr(quizs.indexOf('#')).split('#');
  var s = '<ol>';
  for (var i = 1; i < tips.length; i++) {
    s = s + '<li>' + tips[i] + '</li>';
  }
  s = s + '</ol>';
  return s;
}

function pickQuit(type) {
  Module.ccall('cPickQuiz', 'number', ['number'], [type]);
}

function shiftKb() {
  kb_shit = (kb_shit + 1) % 3;
  if (0 == kb_shit) {
    btn1.innerHTML = '一';
    btn2.innerHTML = '二';
    btn5.innerHTML = '五';
    btn6.innerHTML = '六';
  } else if (1 == kb_shit) {
    btn1.innerHTML = '么';
    btn2.innerHTML = '兩';
    btn5.innerHTML = '伍';
    btn6.innerHTML = '陸';
  } else if (2 == kb_shit) {
    btn1.innerHTML = '一';
    btn2.innerHTML = '雙';
    btn5.innerHTML = '五';
    btn6.innerHTML = '六';
  }
}

function transMathOp(s) {
  var ch = s.split('');
  for (var i = 0; i < ch.length; i++) {
    if ('+' == ch[i]) {
      ch[i] = String.fromCharCode(0x002b);
    } else if ('-' == ch[i]) {
      ch[i] = String.fromCharCode(0x2212);
    } else if ('*' == ch[i]) {
      ch[i] = String.fromCharCode(0x00d7);
    } else if ('/' == ch[i]) {
      ch[i] = String.fromCharCode(0x00f7);
    } else if ('=' == ch[i]) {
      ch[i] = String.fromCharCode(0x003d);
    }
  }
  return ch.join('');
}

function undoFillNum() {
  var pre = output.lastElementChild;
  var ch = pre.innerHTML.split('');
  for (var i = ch.length - 1; 0 <= i; i--) {
    if ('(' == ch[i] && EMPTY_CHAR != ch[i + 1]) {
      ch[i + 1] = EMPTY_CHAR;
      break;
    }
  }
  pre.innerHTML = ch.join('');
}

function validateQuiz(pre) {
  var ch1 = pre.innerHTML.split('');
  var ch2 = quizs.split('');
  var error = false;
  var s = '';
  for (var i = 0; i < ch1.length; i++) {
    if (ch1[i] != ch2[i]) {
      s = s + "<span style='color:red'>" + ch2[i] + '</span>';
      error = true;
    } else {
      s = s + ch1[i];
    }
  }
  if (error) {
    s = s + ERROR_MARK;
  } else {
    s = s + CHECK_MARK;
  }
  return s;
}

function writeOutput(message) {
  var pre = document.createElement('p');
  pre.style.wordWrap = 'break-word';
  pre.innerHTML = message;
  output.appendChild(pre);
  window.scrollTo(0, document.body.scrollHeight);
}
