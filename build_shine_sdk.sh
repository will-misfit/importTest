#!/bin/bash

# validating inputs
if [ -z "$1" ]
	then
	echo "ERROR: No target chosen: Develop, Misfit"
	echo "Run me with: sh build [target]"
	exit 1
fi

# copy obfuscated library & relevant NATIVE libraries to Release folder
SDK_RELEASE_PATH="Release/SDK"
SDK_BUILD_OUTPUT_PATH="ShineSDK/build/outputs/aar"

# clean up output folders
rm -rf $SDK_RELEASE_PATH
mkdir -p "${SDK_RELEASE_PATH}"
rm -rf "${SDK_BUILD_OUTPUT_PATH}/*"

# replace the list of supported architectures
if [ $1 == "Misfit" ]; then 
    echo "Misfit Flagship App Build, only support armeabi and armeabi-v7a"
    MF_ARCHITECTURE_MARKER="all"
	MF_ARCHITECTURE_FLAGSHIP="armeabi armeabi-v7a"
	MF_APPLICATION_MKFILE="ShineSDK/src/main/jni/Application.mk"
	sed -i "" \
		-e "s#${MF_ARCHITECTURE_MARKER}#${MF_ARCHITECTURE_FLAGSHIP}#g" \
		"${MF_APPLICATION_MKFILE}"
fi

# build target, both Debug and Release version
gradle clean
gradle assemble$1

# copy outputs to release folder
cp -r "${SDK_BUILD_OUTPUT_PATH}/"* "${SDK_RELEASE_PATH}/"

# Sample project
SAMPLE_PATH="ShineSample"
SAMPLE_RELEASE_PATH="Release/ShineSample"
SAMPLE_RELEASE_BUILD_PATH="Release/ShineSample/build"
cp -r "${SAMPLE_PATH}" "${SAMPLE_RELEASE_PATH}"
rm -rf "${SAMPLE_RELEASE_BUILD_PATH}"

