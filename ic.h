//
// ic.h
// Idiom calc implementation.
//
// Copyright (c) 2016 Waync Cheng.
// All Rights Reserved.
//
// 2016/7/27 Waync created.
//

#pragma once

#include <algorithm>
#include <iterator>
#include <map>
#include <sstream>
#include <string>
#include <vector>

enum QUIZ_TYPE {
  QUIZ_TYPE_ADD = 0,
  QUIZ_TYPE_SUB,
  QUIZ_TYPE_MUL,
  QUIZ_TYPE_DIV,
  QUIZ_TYPE_NUM
};

#define LAST_IDIOM_COUNT 24
#define PICK_QUIZ_MAX_TRY_COUNT 100
#define MAX_STR_BUFF 1024

std::vector<std::pair<std::string, std::string> > idiom; // All idiom db, <idiom, desc>

typedef std::pair<int, std::string> idiom_type; // <idiom index, char index(s) to idiom>
typedef std::vector<idiom_type> idiom_list_type; // [idiom_type]
typedef std::map<int, idiom_list_type> num_type; // <number, idiom_list_type>
num_type num;                           // All possible number db.

typedef std::vector<std::vector<num_type::const_iterator> > quiz_list_type; // [a,b,c] where a [op] b=c
quiz_list_type quiz[QUIZ_TYPE_NUM];     // All quiz db.
int total_quiz_count[QUIZ_TYPE_NUM];    // Number of max possible quiz.
int quiz_index[QUIZ_TYPE_NUM];          // Current index to quiz db. (loopped)

std::vector<int> last_idiom;            // Last idioms used. This is used to avoid see the same idiom so freq.

std::string GetBracketsIdiom(const idiom_type it)
{
  std::string s = idiom[it.first].first;
  for (int i = (int)it.second.size() - 1; 0 <= i; i--) {
    int idx = it.second[i] - '0' - 1;
    s.insert(2 * idx + 2, ")");
    s.insert(2 * idx, "(");
  }
  return s;
}

std::string GetNumFromIdiom(const idiom_type it, bool bBracket)
{
  const std::string &src = idiom[it.first].first;
  std::string s;
  for (int i = 0; i < (int)it.second.size(); i++) {
    int idx = it.second[i] - '0' - 1;
    if (bBracket) {
      s += "(";
    }
    s += src[2 * idx];
    s += src[2 * idx + 1];
    if (bBracket) {
      s += ")";
    }
  }
  return s;
}

std::string GetDesc(const idiom_type it)
{
  return idiom[it.first].second;
}

void GenQuiz(quiz_list_type &quiz_list, int type) // type:0add,1sub,2mul,3div.
{
  total_quiz_count[type] = 0;
  quiz_index[type] = 0;

  for (num_type::const_iterator it = num.begin(); num.end() != it; ++it) {
    for (num_type::const_iterator it2 = num.begin(); num.end() != it2; ++it2) {
      if (it == it2) {
        continue;
      }
      if ((1 == it->first || 1 == it2->first) &&
          (QUIZ_TYPE_MUL == type || QUIZ_TYPE_DIV == type)) { // Skip mul 1 && div 1.
        continue;
      }
      int result;
      switch (type)
      {
      case QUIZ_TYPE_ADD:
        result = it->first + it2->first;
        break;
      case QUIZ_TYPE_SUB:
        result = it->first - it2->first;
        break;
      case QUIZ_TYPE_MUL:
        result = it->first * it2->first;
        break;
      case QUIZ_TYPE_DIV:
        result = (int)(it->first / it2->first);
        break;
      default:
        return;
      }
      num_type::const_iterator it3 = num.find(result);
      if (num.end() == it3) {
        continue;
      }
      if (QUIZ_TYPE_DIV == type &&
          it2->first * result != it->first) { // Validate div quiz a / b = c -> a = b * c.
        continue;
      }
      std::vector<num_type::const_iterator> q;
      q.push_back(it);
      q.push_back(it2);
      q.push_back(it3);
      quiz_list.push_back(q);
      int quiz_count = (int)(it->second.size() * it2->second.size() * it3->second.size());
      total_quiz_count[type] += quiz_count;
    }
  }

  if (!quiz_list.empty()) {
    quiz_index[type] = rand() % quiz_list.size();
  }
}

void LoadData()
{
#ifdef IC_SUPPORT_LOAD_FROM_STREAM
  std::string stream((const char*)IDIOM_ANSI_MOD, sizeof(IDIOM_ANSI_MOD));
  std::istringstream ss(stream);
#else
  FILE *inf = fopen("idiom_ansi.txt", "rt");
  if (!inf) {
    return;
  }
#endif

  idiom.clear();
  num.clear();
  last_idiom.clear();

  char line[MAX_STR_BUFF];
  while (true) {
#ifdef IC_SUPPORT_LOAD_FROM_STREAM
    if (!ss.getline(line, sizeof(line)) || '#' == line[0]) {
#else
    if (!fgets(line, sizeof(line), inf) || '#' == line[0]) {
#endif
      break;
    }
    std::vector<std::string> token;
    std::stringstream ss(line);
    token.assign(std::istream_iterator<std::string>(ss), std::istream_iterator<std::string>());

    if (1 >= token.size() || 0 != (token.size() - 1) % 2) { // At least has one number defined.
      continue;
    }

    std::string desc = "";

    for (int i = 1; i < (int)token.size(); i += 2) { // Store numbers of idiom.
      if ('#' == token[i][0]) {
        desc = token[i + 1];
      } else {
        int n = atoi(token[i].c_str());
        num[n].push_back(std::make_pair((int)idiom.size(), token[i + 1]));
      }
    }

    idiom.push_back(std::make_pair(token[0], desc));
  }

#ifndef IC_SUPPORT_LOAD_FROM_STREAM
  fclose(inf);
#endif

  //
  // Create quiz.
  //

  for (int i = 0; i < QUIZ_TYPE_NUM; i++) {
    quiz[i].clear();
    GenQuiz(quiz[i], i);
  }
}

bool InLastIdiomList(int IdiomIndex)
{
  return last_idiom.end() != std::find(last_idiom.begin(), last_idiom.end(), IdiomIndex);
}

std::string PickQuiz(int type)
{
  type %= QUIZ_TYPE_NUM;

  if (0 >= total_quiz_count[type]) {
    return "empty quiz";
  }

  for (int k = 0; k < PICK_QUIZ_MAX_TRY_COUNT; k++) { // Try PICK_QUIZ_MAX_TRY_COUNT times to find a quiz;
    int i = quiz_index[type];
    quiz_index[type] = (quiz_index[type] + 1) % quiz[type].size();
    const std::vector<num_type::const_iterator> &q = quiz[type][i];
    const idiom_list_type &la = q[0]->second, &lb = q[1]->second, &lc = q[2]->second;
    const idiom_type &a = la[rand() % la.size()];
    if (InLastIdiomList(a.first)) {
      continue;
    }
    const idiom_type &b = lb[rand() % lb.size()];
    if (InLastIdiomList(b.first)) {
      continue;
    }
    const idiom_type &c = lc[rand() % lc.size()];
    if (InLastIdiomList(c.first)) {
      continue;
    }
    if (a.first == b.first || a.first == c.first || b.first == c.first) {
      continue;
    }
    last_idiom.push_back(a.first);
    last_idiom.push_back(b.first);
    last_idiom.push_back(c.first);
    if (last_idiom.size() > LAST_IDIOM_COUNT) {
      last_idiom.erase(last_idiom.begin(), last_idiom.begin() + last_idiom.size() - LAST_IDIOM_COUNT);
    }
    std::string sa = GetBracketsIdiom(a);
    std::string sb = GetBracketsIdiom(b);
    std::string sc = GetBracketsIdiom(c);
    std::string da = GetDesc(a);
    std::string db = GetDesc(b);
    std::string dc = GetDesc(c);
    static const char *typech = "+-*/";
    char buff[MAX_STR_BUFF];
    sprintf(buff, "%s %c %s = %s #%s#%s#%s", sa.c_str(), typech[type], sb.c_str(), sc.c_str(), da.c_str(), db.c_str(), dc.c_str());
    return buff;
  }

  return "cannot pick";
}

std::string PickNumQuiz(int type, bool bAddBracketToAnswer)
{
  type %= QUIZ_TYPE_NUM;

  if (0 >= total_quiz_count[type]) {
    return "empty quiz";
  }

  for (int k = 0; k < PICK_QUIZ_MAX_TRY_COUNT; k++) { // Try PICK_QUIZ_MAX_TRY_COUNT times to find a quiz;
    int i = quiz_index[type];
    quiz_index[type] = (quiz_index[type] + 1) % quiz[type].size();
    const std::vector<num_type::const_iterator> &q = quiz[type][i];
    const idiom_list_type &la = q[0]->second, &lb = q[1]->second, &lc = q[2]->second;
    const idiom_type &a = la[rand() % la.size()];
    const idiom_type &b = lb[rand() % lb.size()];
    const idiom_type &c = lc[rand() % lc.size()];
    std::string sa = GetNumFromIdiom(a, false);
    std::string sb = GetNumFromIdiom(b, false);
    std::string sc = GetNumFromIdiom(c, bAddBracketToAnswer);
    static const char *typech = "+-*/";
    char buff[MAX_STR_BUFF];
    sprintf(buff, "%s %c %s = %s #", sa.c_str(), typech[type], sb.c_str(), sc.c_str());
    return buff;
  }

  return "cannot pick";
}

// end of ic.h
