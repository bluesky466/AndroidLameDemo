#include <jni.h>
#include <string>
#include "android/log.h"

static const char *TAG = "LameMp3";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)

extern "C" {
#include <lame/lame.h>
}

/**
 * 第一步: 创建lame_global_flags*
 * lame_global_flags *client = lame_init();
 *
 * 第二步: 配置参数
 * lame_set_in_samplerate(client, 44100);    // 输入采样率
 * lame_set_out_samplerate(client, 44100);   // 输出采样率44100
 * lame_set_num_channels(client, 2);         // 通道数2
 * lame_set_brate(client, 128);              // 比特率128kbps
 * lame_set_quality(client, quality);        // 设置编码质量
 * lame_init_params(client);                 // 初始化参数
 *
 * 第三步: 编码
 * // 左右通道分别输入,numSamples为每个通道的采样点数
 * int encodeSize = lame_encode_buffer(client, left, right, numSamples, result, resultSize);
 * // 单通道,numSamples为采样点数
 * int encodeSize = lame_encode_buffer(client, left, null, numSamples, result, resultSize);
 * // 多通道通道混合输入,numSamples为每个通道的采样点数,而不是pcm的总采样点数
 * int encodeSize = lame_encode_buffer_interleaved(client, pcm, numSamples, result, resultSize);
 *
 * 第四步: 清理
 * lame_close(client);
 */

extern "C" JNIEXPORT jstring JNICALL
Java_me_linjw_lib_lame_LameMp3_getLameMp3Version(
        JNIEnv *env,
        jobject thiz) {
    return env->NewStringUTF(get_lame_version());
}

extern "C" JNIEXPORT jlong JNICALL
Java_me_linjw_lib_lame_LameMp3_createLameMp3Client(
        JNIEnv *env,
        jobject thiz) {
    lame_global_flags *client = lame_init();
    LOGD("createLameMp3Client: %ld", (long) client);
    return reinterpret_cast<jlong>(client);
}

extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_setInSampleRate(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr,
        jint sampleRate) {
    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    LOGD("setInSampleRate(%ld): sampleRate=%d", client, sampleRate);
    return lame_set_in_samplerate(client, sampleRate);
}

extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_setOutSampleRate(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr,
        jint sampleRate) {
    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    LOGD("setOutSampleRate(%ld): sampleRate=%d", client, sampleRate);
    return lame_set_out_samplerate(client, sampleRate);
}

extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_setNumChannels(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr,
        jint numChannels) {
    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    LOGD("setNumChannels(%ld): numChannels=%d", client, numChannels);
    return lame_set_num_channels(client, numChannels);
}

extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_setBitRate(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr,
        jint bitRate) {
    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    LOGD("setBitRate(%ld): bitRate=%d", client, bitRate);
    return lame_set_brate(client, bitRate);
}

extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_initParams(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr) {
    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    LOGD("initParams(%ld)", client);
    return lame_init_params(client);
}

static short *GetShortArrayElements(
        JNIEnv *env,
        jbyteArray buff) {
    if (buff == NULL) {
        return NULL;
    }
    return (short *) (*env).GetByteArrayElements(buff, NULL);
}

static void ReleaseByteArrayElements(
        JNIEnv *env,
        jbyteArray array,
        jbyte *elems) {
    if (array != NULL) {
        (*env).ReleaseByteArrayElements(array, elems, 0);
    }
}

extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_encode(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr,
        jbyteArray leftBuff,
        jbyteArray rightBuff,
        jint numSamples,
        jbyteArray resultBuff) {
    if (leftBuff == NULL && rightBuff == NULL) {
        LOGD("leftBuff(%ld) and rightBuff(%ld) can not all be null",
             (long) leftBuff, (long) rightBuff);
        return -1;
    }
    if (resultBuff == NULL) {
        LOGD("resultBuff(%ld) can not be null", (long) resultBuff);
        return -1;
    }

    short *left = GetShortArrayElements(env, leftBuff);
    short *right = GetShortArrayElements(env, rightBuff);

    unsigned char *result = (unsigned char *) (*env).GetByteArrayElements(resultBuff, NULL);
    jsize resultSize = (*env).GetArrayLength(resultBuff);

    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    int encodeSize = lame_encode_buffer(client, left, right, numSamples, result, resultSize);

    ReleaseByteArrayElements(env, leftBuff, (jbyte *) left);
    ReleaseByteArrayElements(env, rightBuff, (jbyte *) left);
    ReleaseByteArrayElements(env, resultBuff, (jbyte *) result);
    return encodeSize;
}


extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_encodeInterleaved(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr,
        jbyteArray pcmBuff,
        jint numSamples,
        jbyteArray resultBuff) {
    if (pcmBuff == NULL || resultBuff == NULL) {
        LOGD("pcmBuff(%ld) or resultBuff(%ld) is null",
             (long) pcmBuff, (long) resultBuff);
        return -1;
    }

    short *pcm = GetShortArrayElements(env, pcmBuff);

    unsigned char *result = (unsigned char *) (*env).GetByteArrayElements(resultBuff, NULL);
    jsize resultSize = (*env).GetArrayLength(resultBuff);

    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    int encodeSize = lame_encode_buffer_interleaved(client, pcm, numSamples, result, resultSize);

    ReleaseByteArrayElements(env, pcmBuff, (jbyte *) pcm);
    ReleaseByteArrayElements(env, resultBuff, (jbyte *) result);
    return encodeSize;
}

extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_setQuality(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr,
        jint quality) {
    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    LOGD("setQuality(%ld): quality=%d", client, quality);
    return lame_set_quality(client, quality);
}

extern "C" JNIEXPORT int JNICALL
Java_me_linjw_lib_lame_LameMp3_close(
        JNIEnv *env,
        jobject thiz,
        jlong clientPtr) {
    lame_global_flags *client = reinterpret_cast<lame_global_flags *>(clientPtr);
    LOGD("close(%ld)", client);
    return lame_close(client);
}