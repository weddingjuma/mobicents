/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_mobicents_gct_InterProcessCommunicator */

#ifndef _Included_org_mobicents_gct_InterProcessCommunicator
#define _Included_org_mobicents_gct_InterProcessCommunicator
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_mobicents_gct_InterProcessCommunicator
 * Method:    receive
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_org_mobicents_gct_InterProcessCommunicator_receive
  (JNIEnv *, jobject, jint, jbyteArray);

/*
 * Class:     org_mobicents_gct_InterProcessCommunicator
 * Method:    send
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_org_mobicents_gct_InterProcessCommunicator_send
  (JNIEnv *, jobject, jint, jint, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
