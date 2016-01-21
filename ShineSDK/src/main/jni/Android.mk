# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

# CRCCalculator
include $(CLEAR_VARS)
LOCAL_MODULE    := CRCCalculator
LOCAL_SRC_FILES := CRCCalculator.cpp
include $(BUILD_SHARED_LIBRARY)

# SwimLap
include $(CLEAR_VARS)
LOCAL_MODULE := SwimLap
LOCAL_SRC_FILES := refine_lap_count.cpp refine_lap_count_wrap.cpp

#  Enable C++11. 
#LOCAL_CPPFLAGS += -std=c++11

LOCAL_CPP_FEATURES += exceptions
#LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
