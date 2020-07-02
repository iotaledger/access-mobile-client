# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# Set distribution path
set(DISTRIBUTION_DIR src/main/cpp/libDacAuth)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/native-lib.cpp)

add_library( # Sets the name of the library.
        dacAuthNative

        # Sets the library as a shared library.
        STATIC

        # Provides a relative path to your source file(s).
        ${DISTRIBUTION_DIR}/libauthdac.c
        ${DISTRIBUTION_DIR}/tinyembedded/libdacUtils.c
        ${DISTRIBUTION_DIR}/tinyembedded/libdacClient.c
        ${DISTRIBUTION_DIR}/tinyembedded/libdacServer.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/aes.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/curve25519-donna.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/sha256.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/sha512.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref/crypto_hash_sha512.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref/fe25519.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref/ge25519.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref/keypair.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref/open.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref/sc25519.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref/sign.c
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref/verify.c)

# Specifies a path to native header files.
include_directories(
        ${DISTRIBUTION_DIR}/include
        ${DISTRIBUTION_DIR}/tinyembedded/include
        ${DISTRIBUTION_DIR}/tinyembedded/external
        ${DISTRIBUTION_DIR}/tinyembedded/external/ref
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib

        dacAuthNative

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})


