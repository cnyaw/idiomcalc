
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ic
LOCAL_SRC_FILES := ic.cpp

include $(BUILD_SHARED_LIBRARY)
