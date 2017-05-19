//
// ic.cpp
// Idiom calc android jni implementation.
//
// Copyright (c) 2016 Waync Cheng.
// All Rights Reserved.
//
// 2016/7/27 Waync created.
//

#include <string.h>
#include <jni.h>

#include "dat.h"

#define IC_SUPPORT_LOAD_FROM_STREAM
#include "../../ic.h"

extern "C" {

void Java_weilican_ic_IdiomCalcActivity_initQuiz(JNIEnv* env, jobject thiz, jint seed)
{
  srand(seed);
  LoadData();
}

jbyteArray Java_weilican_ic_IdiomCalcActivity_pickQuiz(JNIEnv* env, jobject thiz, jint type)
{
  std::string s = PickQuiz(type % 4);
  jbyteArray ret = env->NewByteArray(s.length());
  env->SetByteArrayRegion(ret, 0, s.length(), (const jbyte*)s.c_str());
  return ret;
}

jbyteArray Java_weilican_ic_IdiomCalcActivity_pickNumQuiz(JNIEnv* env, jobject thiz, jint type)
{
  std::string s = PickNumQuiz(type % 4, false);
  jbyteArray ret = env->NewByteArray(s.length());
  env->SetByteArrayRegion(ret, 0, s.length(), (const jbyte*)s.c_str());
  return ret;
}

int Java_weilican_ic_IdiomCalcActivity_getIdiomNum(JNIEnv* env, jobject thiz)
{
  return idiom.size();
}

jbyteArray Java_weilican_ic_IdiomCalcActivity_getIdiom(JNIEnv* env, jobject thiz, int index)
{
  if (0 > index || idiom.size() <= index) {
    return 0;
  }
  char buff[MAX_STR_BUFF];
  sprintf(buff, "%s#%s", idiom[index].first.c_str(), idiom[index].second.c_str());
  size_t len = strlen(buff);
  jbyteArray ret = env->NewByteArray(len);
  env->SetByteArrayRegion(ret, 0, len, (const jbyte*)buff);
  return ret;
}

} // extern "C"
