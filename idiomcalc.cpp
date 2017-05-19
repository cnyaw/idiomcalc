// idiomcalc.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"

#include "ic.h"
#include "time.h"

int main(int argc, char* argv[])
{
  srand(time(0));

  LoadData();

  printf("add %d(%d)\n", total_quiz_count[0], quiz[0].size());
  printf("sub %d(%d)\n", total_quiz_count[1], quiz[1].size());
  printf("mul %d(%d)\n", total_quiz_count[2], quiz[2].size());
  printf("div %d(%d)\n", total_quiz_count[3], quiz[3].size());
  printf("total %d, idiom x%d\n", total_quiz_count[0] + total_quiz_count[1] + total_quiz_count[2] + total_quiz_count[3], idiom.size());

  for (int i = 0; i < 4; i++) {
    printf("%s\n", PickQuiz(i).c_str());
  }

  return 0;
}
