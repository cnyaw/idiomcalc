//
// main.cpp
// idiomcalc web C API.
//
// 2020/7/2 Waync created.
//

#include <time.h>

#include <emscripten.h>

#define IC_SUPPORT_LOAD_FROM_STREAM
#include "../dat.h"
#include "../ic.h"

extern "C" int EMSCRIPTEN_KEEPALIVE cPickQuiz(int type)
{
  std::string s = PickQuiz(type % 4);
  EM_ASM_ARGS({
    var u8 = new Uint8Array($1);
    for (var i = 0; i < $1; i++) {
      u8[i] = getValue($0 + i, 'i8');
    }
    var d = new TextDecoder("big5");
    var s = d.decode(u8);
    addNewQuiz(s);
  }, s.c_str(), (int)s.size());
  return (int)s.size();
}

int main(int argc, char* argv[])
{
  srand(time(0));
  LoadData();
  emscripten_exit_with_live_runtime();
}
