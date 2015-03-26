/*******************************************************************************
 * Copyright 2014 Alexander Leontyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.woodblockwithoutco.remotecontroller.impl;

import java.lang.ref.WeakReference;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.media.IRemoteControlDisplay;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;

/**
 * Class for receiving and dispatching events of RemoteControlClient.
 */
class RemoteControlDisplayV14 extends IRemoteControlDisplay.Stub  {
	
	private final static int MSG_BASE = 100;
	protected final static int MSG_SET_GENERATION_ID = MSG_BASE + 1;
	protected final static int MSG_SET_METADATA = MSG_BASE + 2;
	protected final static int MSG_SET_TRANSPORT_CONTROLS = MSG_BASE + 3;
	protected final static int MSG_SET_ARTWORK = MSG_BASE + 4;
	protected final static int MSG_UPDATE_STATE = MSG_BASE + 5;
	


	/*
	 * The reference should be weak as we can't predict when the process of GC
	 * will happen in remote object.
	 */
	private WeakReference<Handler> mLocalHandler;

	public RemoteControlDisplayV14(Handler handler) {
		mLocalHandler = new WeakReference<Handler>(handler);
	}

	public void setAllMetadata(int generationId, Bundle metadata, Bitmap bitmap) {
		Handler handler = mLocalHandler.get();
		if (handler != null) {
			handler.obtainMessage(MSG_SET_METADATA, generationId, 0, metadata).sendToTarget();
			handler.obtainMessage(MSG_SET_ARTWORK, generationId, 0, bitmap).sendToTarget();
		}
	}

	public void setArtwork(int generationId, Bitmap bitmap) {
		Handler handler = mLocalHandler.get();
		if (handler != null) {
			handler.obtainMessage(MSG_SET_ARTWORK, generationId, 0, bitmap).sendToTarget();
		}
	}

	public void setCurrentClientId(int clientGeneration, PendingIntent mediaIntent, boolean clearing) throws RemoteException {
		Handler handler = mLocalHandler.get();
		if (handler != null) {
			handler.obtainMessage(MSG_SET_GENERATION_ID, clientGeneration, (clearing ? 1 : 0), mediaIntent).sendToTarget();
		}
	}

	public void setMetadata(int generationId, Bundle metadata) {
		Handler handler = mLocalHandler.get();
		if (handler != null) {
			handler.obtainMessage(MSG_SET_METADATA, generationId, 0, metadata).sendToTarget();
		}
	}

	public void setPlaybackState(int generationId, int state, long stateChangeTimeMs) {
		Handler handler = mLocalHandler.get();
		if (handler != null) {
			handler.obtainMessage(MSG_UPDATE_STATE, generationId, state).sendToTarget();
		}
	}

	public void setTransportControlFlags(int generationId, int flags) {
		Handler handler = mLocalHandler.get();
		if (handler != null) {
			handler.obtainMessage(MSG_SET_TRANSPORT_CONTROLS, generationId, flags).sendToTarget();
		}
	}
}
