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




#include "Win32FSHook.h"
#include <stdio.h>
#include <windows.h>
#include <winbase.h>
#include <winnt.h>
#include <time.h>
#include <stdio.h>
#include "Lock.h"
#include "WatchData.h"
#include "Logger.h"


Win32FSHook::Win32FSHook() 
{
	_isRunning = false;
	InitializeCriticalSection(&_cSection);
	_mainLoopEvent = CreateEvent(NULL, FALSE,FALSE, NULL);
}

void Win32FSHook::init(ChangeCallback callback)
{
	if (!_isRunning)
	{
		_isRunning = true;
		
	    DWORD dwThreadId;
	    LPVOID dwThrdParam = (LPVOID)this; 
	    _mainLoopThreadHandle = CreateThread( 
	        NULL,                        // default security attributes 
	        0,                           // use default stack size  
	        Win32FSHook::mainLoop,       // thread function 
	        dwThrdParam,                // argument to thread function 
	        0,                           // use default creation flags 
	        &dwThreadId);                // returns the thread identifier 
	 
		if (_mainLoopThreadHandle == NULL) 
		{
			throw ERR_INIT_THREAD;
		}	
	}
}

Win32FSHook::~Win32FSHook()
{
	// terminate thread.
	_isRunning = false;
	SetEvent(_mainLoopEvent);

	// cleanup
	CloseHandle(_mainLoopThreadHandle);
	CloseHandle(_mainLoopEvent);
	DeleteCriticalSection(&_cSection);
}

void Win32FSHook::remove_watch(int watchId)
{
	Lock lock(&_cSection, true);
	_pendingActions.push(make_pair(CANCEL, watchId));
	SetEvent(_mainLoopEvent);
}

int Win32FSHook::add_watch(const WCHAR* path, long notifyFilter, bool watchSubdirs, DWORD &error, ChangeCallback changeCallback)
{
	// locks this scope so that only one thread can access it at once.
	Lock lock(&_cSection,true);
	WatchData *watchData;
	
	
	try
	{
		watchData = new WatchData(path, notifyFilter, watchSubdirs, Win32FSHook::changeCallback, changeCallback);
	}
	catch (DWORD err)
	{
		error = err;
		return 0;	
	}
	
	int watchId = watchData->getId();
	_wid2WatchData[watchId] = watchData;
	_pendingActions.push(make_pair(WATCH, watchId));
	SetEvent(_mainLoopEvent);
	
	return watchId;
}

void CALLBACK Win32FSHook::changeCallback(DWORD dwErrorCode, DWORD dwNumberOfBytesTransfered,  LPOVERLAPPED lpOverlapped)
{
	WatchData* wd = (WatchData*)lpOverlapped->hEvent;
	char* events=(char*)wd->getNotifyInfo();
	FILE_NOTIFY_INFORMATION *event;
	int i=0;
	do
	{
		event = (FILE_NOTIFY_INFORMATION*)(events+i);
		int action = event->Action;
		int len = event->FileNameLength / sizeof(WCHAR);

		WCHAR *name = new WCHAR[len + 1];
		for (int k=0;k<len;k++)
		{
			name[k] = event->FileName[k];
		}
		name[len] = 0;
		
		// log("%ls : Event %d on %ls (len =%d)",wd->getPath(), action, name, len);
		wd->getCallback()(wd->getId(), action, wd->getPath(), name);
		
		delete[] name;
		i = event->NextEntryOffset;
	}
	while (event->NextEntryOffset);	
	
	int res = wd->watchDirectory();
	if (res != 0)
	{
		log("Error watching dir %s : %d",wd->getPath(), res);
	}
}

DWORD WINAPI Win32FSHook::mainLoop( LPVOID lpParam )
{
	Win32FSHook* _this = (Win32FSHook*)lpParam;
	while (_this->_isRunning)
	{
		{ // lock scope
			Lock lock(&_this->_cSection,true);
			while (_this->_isRunning && _this->_pendingActions.size() > 0)
			{
				pair<ACTION, int> action = _this->_pendingActions.front();
				_this->_pendingActions.pop();
				switch (action.first)
				{
					case WATCH:
					{
						int wd = action.second;
						map <int, WatchData*>::const_iterator i = _this->_wid2WatchData.find(wd);
						if (i == _this->_wid2WatchData.end())
						{
							log("WATCH: watch id %d not found", wd);
						}
						else
						{
							_this->watchDirectory(i->second);
						}
					}
					break;
					case CANCEL:
					{
						int wd = action.second;
						map <int, WatchData*>::const_iterator i = _this->_wid2WatchData.find(wd);
						if (i == _this->_wid2WatchData.end())
						{
							log("CANCEL: watch id %d not found", wd);
						}
						else
						{
							int watchId = i->first;
							_this->unwatchDirectory(watchId);
						}
					}
					break;
				}	
			}
		}
		
		if (_this->_isRunning)
		{
			WaitForSingleObjectEx(_this->_mainLoopEvent, INFINITE, TRUE);
		}
	}
	
	return 0;
}


void Win32FSHook::unwatchDirectory(int wd)
{
	map <int, WatchData*>::const_iterator i = _wid2WatchData.find(wd);
	if (i == _wid2WatchData.end())
	{
		log("UnwatchDirectory: watch id %d not found", wd);
	}
	else
	{
		WatchData *watchData = i->second;
		log("Stop watching %ls", watchData->getPath());
		int res = watchData->unwatchDirectory();
		if (res != 0)
		{
			log("Error canceling watch on dir %ls : %d",watchData->getPath(), res);
		}
		else
		{
			if (_wid2WatchData.erase(wd) != 1)
			{
				delete watchData;
			}
			else
			{
				log("Error deleting watch %d from map",wd);
			}
		}
	}
	
}

void Win32FSHook::watchDirectory(WatchData* wd)
{
	log("Watching %ls", wd->getPath());
	int res = wd->watchDirectory();
	if (res != 0)
	{
		log("Error watching dir %ls : %d",wd->getPath(), res);
	}
	
}
