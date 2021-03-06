/*******************************************************************************
 * JNotify - Allow java applications to register to File system events.
 * 
 * Copyright (C) 2005 - Content Objects
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 ******************************************************************************
 *
 * Content Objects, Inc., hereby disclaims all copyright interest in the
 * library `JNotify' (a Java library for file system events). 
 * 
 * Yahali Sherman, 21 November 2005
 *    Content Objects, VP R&D.
 *    
 ******************************************************************************
 * Author : Omry Yadan
 ******************************************************************************/


#include "net_contentobjects_jnotify_win32_JNotify_win32.h"

#include <windows.h>
#include <winbase.h>
#include <winnt.h>
#include <string>
#include "Win32FSHook.h"
#include "Logger.h"
#include "Lock.h"

Win32FSHook *_win32FSHook;

enum INIT_STATE
{
	NOT_INITIALIZED,
	PRE_INITIALIZED,
	INITIALIZED,
	FAILED
} 
_initialized = NOT_INITIALIZED;
static JavaVM *_jvm = 0;
static jmethodID _mid;
static jclass _clazz;
static JNIEnv* _env;

void getErrorDescription(int errorCode, WCHAR *buffer, int len);

void ChangeCallbackImpl(int watchID, int action, const WCHAR* rootPath, const WCHAR* filePath)
{	
	if(_initialized == NOT_INITIALIZED || _initialized == FAILED)
	{
		return;
	}
	if(_initialized == PRE_INITIALIZED)
	{
		// attach daemon thread to running JVM once
		_jvm->AttachCurrentThreadAsDaemon((void**)&_env, NULL);
		_initialized = INITIALIZED;
	}
	jstring jRootPath = _env->NewString((jchar*)rootPath, wcslen(rootPath));
	jstring jFilePath = _env->NewString((jchar*)filePath, wcslen(filePath));
	_env->CallStaticVoidMethod(_clazz, _mid, watchID, action, jRootPath, jFilePath);
}




/*
 * Class:     net_contentobjects_fshook_win32_Win32FSHook
 * Method:    nativeInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_contentobjects_jnotify_win32_JNotify_1win32_nativeInit
  (JNIEnv *env, jclass clazz)
{
	if(_initialized != NOT_INITIALIZED){
		log("nativeInit called more than once");
		return 0;
	}
	try
	{
		// initialize Java Runtime components
		if(_jvm == NULL){
			log("JVM is NULL");
			throw -1;
		}
		
		_env = env;
		if(_env == NULL){
			log("JNI Environment is NULL");
			throw -1;
		}
		
		char className[] = "net/contentobjects/jnotify/win32/JNotify_win32";
		_clazz = _env->FindClass(className);
		if(_clazz == NULL){
			log("could not find net/contentobjects/jnotify/win32/JNotify_win32 in JNI env");
			throw -1;
		}
		
		_mid = _env->GetStaticMethodID(_clazz, "callbackProcessEvent", "(IILjava/lang/String;Ljava/lang/String;)V");
		if(_mid == NULL){
			log("could not find static method callbackProcessEvent in class net/contentobjects/jnotify/win32/JNotify_win32");
			throw -1;
		}
		
		_win32FSHook = new Win32FSHook();
		_win32FSHook->init(&ChangeCallbackImpl);
		
		_initialized = PRE_INITIALIZED;
		
		return 0;
	}
	catch (int err)
	{
		_initialized = FAILED;
		return err;
	}
}

/*
 * Class:     net_contentobjects_fshook_win32_Win32FSHook
 * Method:    nativeAddWatch
 * Signature: (Ljava/lang/String;JZ)I
 */
JNIEXPORT jint JNICALL Java_net_contentobjects_jnotify_win32_JNotify_1win32_nativeAddWatch
  (JNIEnv *env, jclass clazz, jstring path, jlong notifyFilter, jboolean watchSubdir)
{
	
	const WCHAR *cstr = (const WCHAR*)env->GetStringChars(path, NULL);
    if (cstr == NULL) 
    {
    	return -1; /* OutOfMemoryError already thrown */
    }
    DWORD error = 0;
	int watchId = _win32FSHook->add_watch(cstr, notifyFilter, watchSubdir == JNI_TRUE, error,ChangeCallbackImpl);
	env->ReleaseStringChars(path, (const jchar*)cstr);
	if (watchId == 0)
	{
		return -error;
	}
	else
	{
		return watchId;
	}
}

/*
 * Class:     net_contentobjects_fshook_win32_Win32FSHook
 * Method:    nativeRemoveWatch
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_contentobjects_jnotify_win32_JNotify_1win32_nativeRemoveWatch
  (JNIEnv *env, jclass clazz, jint watchId)
{
	_win32FSHook->remove_watch(watchId);
}

/*
 * Class:     net_contentobjects_fshook_win32_Win32FSHook
 * Method:    getErrorDesc
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_contentobjects_jnotify_win32_JNotify_1win32_getErrorDesc
  (JNIEnv *env, jclass clazz, jlong errorCode)
{
	WCHAR buffer[1024];
	getErrorDescription(errorCode, buffer, sizeof(buffer) / sizeof(WCHAR));
	return env->NewString((jchar*)buffer, wcslen(buffer));
}

void getErrorDescription(int errorCode, WCHAR *buffer, int len)
{
	static Lock lock;
	lock.lock();
	
	LPVOID lpMsgBuf;
	FormatMessageW( 
	    FORMAT_MESSAGE_ALLOCATE_BUFFER | 
	    FORMAT_MESSAGE_FROM_SYSTEM | 
	    FORMAT_MESSAGE_IGNORE_INSERTS,
	    NULL,
	    errorCode,
	    0, // Default language
	    (LPWSTR) &lpMsgBuf,
	    0,
	    NULL 
	);

	_snwprintf(buffer, len, L"Error %d : %s", errorCode, (LPCTSTR)lpMsgBuf);
	int len1 = wcslen(buffer);
	if (len1 >= 2)
	{
		buffer[len1 - 2] = '\0';
	}
	
	LocalFree( lpMsgBuf );
	
	lock.unlock();
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	_jvm = jvm;
	return JNI_VERSION_1_2;
}
