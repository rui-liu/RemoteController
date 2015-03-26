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

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.IRemoteControlDisplay;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.woodblockwithoutco.remotecontroller.MediaCommand;
import com.woodblockwithoutco.remotecontroller.OnArtworkChangeListener;
import com.woodblockwithoutco.remotecontroller.OnMetadataChangeListener;
import com.woodblockwithoutco.remotecontroller.OnPlaybackStateChangeListener;
import com.woodblockwithoutco.remotecontroller.OnRemoteControlFeaturesChangeListener;
import com.woodblockwithoutco.remotecontroller.RemoteControlDisplay;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RemoteControlDisplayImplV14 implements RemoteControlDisplay {

	private static final String TAG = "RemoteControlDisplayImplV14";
	private Context mContext;
	private PendingIntent mClientIntent;
	private AudioManager mAudioManager;
	private RemoteControlDisplayV14 mRcd;
	private Handler mHandler;
	private MetadataUpdaterCallbackV14 mCallback;
	private boolean mIsRegistered;
	private Method mRegisterRcdMethod;
	private Method mUnregisterRcdMethod;

	public RemoteControlDisplayImplV14(Context context) {
		initHiddenMethods();
		
		mContext = context;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mCallback = new MetadataUpdaterCallbackV14(this);
		mHandler = new Handler(mCallback);
		mRcd = new RemoteControlDisplayV14(mHandler);
	}
	
	private void initHiddenMethods() {
		Class<AudioManager> audioManagerClass = AudioManager.class;
		try {
			mRegisterRcdMethod = audioManagerClass.getDeclaredMethod("registerRemoteControlDisplay", IRemoteControlDisplay.class);
			mUnregisterRcdMethod = audioManagerClass.getDeclaredMethod("unregisterRemoteControlDisplay", IRemoteControlDisplay.class);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void registerRemoteControlDisplay(IRemoteControlDisplay rcd) {
		try {
			mRegisterRcdMethod.invoke(mAudioManager, rcd);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void unregisterRemoteControlDisplay(IRemoteControlDisplay rcd) {
		try {
			mUnregisterRcdMethod.invoke(mAudioManager, rcd);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean registerRemoteControls() {
		if(mRcd == null) {
			mRcd = new RemoteControlDisplayV14(mHandler);
		}
		mIsRegistered = true;
		registerRemoteControlDisplay(mRcd);
		return true;
	}

	@Override
	public boolean registerRemoteControls(int width, int height) {
		return registerRemoteControls();
	}

	@Override
	public void unregisterRemoteControls() {
		mIsRegistered = false;
		unregisterRemoteControlDisplay(mRcd);
	}

	@Override
	public void unregisterAndDestroyRemoteControls() {
		mIsRegistered = false;
		unregisterRemoteControlDisplay(mRcd);
		mRcd = null;
	}

	@Override
	public Intent getCurrentClientIntent() {
		if (mClientIntent == null) return null;
		return mContext.getPackageManager().getLaunchIntentForPackage(mClientIntent.getTargetPackage());
	}

	@Override
	public boolean isClientActive() {
		return !(mClientIntent == null);
	}



	@Override
	public boolean sendMediaCommand(MediaCommand command) {
		if (mClientIntent != null && mContext != null) {
			switch (command) {
			case REWIND:
				return sendButton(KeyEvent.KEYCODE_MEDIA_REWIND);
			case PREVIOUS:
				return sendButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			case PLAY:
				return sendButton(KeyEvent.KEYCODE_MEDIA_PLAY);
			case PAUSE:
				return sendButton(KeyEvent.KEYCODE_MEDIA_PAUSE);
			case PLAY_PAUSE:
				return sendButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
			case STOP:
				return sendButton(KeyEvent.KEYCODE_MEDIA_STOP);
			case NEXT:
				return sendButton(KeyEvent.KEYCODE_MEDIA_NEXT);
			case FAST_FORWARD:
				return sendButton(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
			}
		}
		// will silently fail with false return if client is missing
		return false;
	}

	private boolean sendButton(int keyCode) {
		KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		try {
			if (mClientIntent == null) return false;
			mClientIntent.send(mContext, 0, intent);
		} catch (CanceledException e) {
			// will silently fail with false return
			return false;
		}
		keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
		intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		try {
			if (mClientIntent == null) return false;
			mClientIntent.send(mContext, 0, intent);
		} catch (CanceledException e) {
			// will silently fail with false return
			return false;
		}
		return true;
	}

	protected void setCurrentClientPendingIntent(PendingIntent clientIntent) {
		mClientIntent = clientIntent;
	}

	@Override
	public void sendBroadcastMediaCommand(MediaCommand command) {
		switch (command) {
		case REWIND:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_REWIND);
			break;
		case PREVIOUS:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			break;
		case PLAY:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PLAY);
			break;
		case PAUSE:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PAUSE);
			break;
		case PLAY_PAUSE:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
			break;
		case STOP:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_STOP);
			break;
		case NEXT:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_NEXT);
			break;
		case FAST_FORWARD:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
			break;
		}
	}

	private void sendBroadcastButton(int keyCode) {
		if (mContext != null) {
			long eventtime = SystemClock.uptimeMillis();
			Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, keyCode, 0);
			keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			mContext.sendOrderedBroadcast(keyIntent, null);
			keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
			keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			mContext.sendOrderedBroadcast(keyIntent, null);
		}
	}
	
	private void sendBroadcastButton(int keyCode, PendingIntent pintent) {
		if(mContext != null) {
			try {
				long eventtime = SystemClock.uptimeMillis();
				Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
				KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, keyCode, 0);
				keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
				pintent.send(mContext, 0, keyIntent);
				keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
				keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
				pintent.send(mContext, 0, keyIntent);
			} catch (CanceledException e) {
				Log.e(TAG, "Sending broadcast to specific player has been cancelled");
			}
		}
	}

	@Override
	public long getPosition() {
		return -1;
	}

	@Override
	public void setArtworkChangeListener(OnArtworkChangeListener l) {
		mCallback.setArtworkChangeListener(l);
	}

	@Override
	public void setMetadataChangeListener(OnMetadataChangeListener l) {
		mCallback.setMetadataChangeListener(l);
	}

	@Override
	public void setPlaybackStateChangeListener(OnPlaybackStateChangeListener l) {
		mCallback.setPlaybackStateChangeListener(l);
	}

	@Override
	public void setRemoteControlFeaturesChangeListener(OnRemoteControlFeaturesChangeListener l) {
		mCallback.setRemoteControlFeaturesChangeListener(l);
	}

	@Override
	public boolean seekTo(long position) {
		return false;
	}

	@Override
	public boolean setSynchronizationEnabled(boolean enabled) {
		return false;
	}

	@Override
	public boolean isRegistered() {
		return mIsRegistered;
	}

	@Override
	public boolean pingService() {
		return true;
	}

	@Override
	public void sendBroadcastMediaCommand(MediaCommand command, PendingIntent intent) {
		switch (command) {
		case REWIND:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_REWIND, intent);
			break;
		case PREVIOUS:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS, intent);
			break;
		case PLAY:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PLAY, intent);
			break;
		case PAUSE:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PAUSE, intent);
			break;
		case PLAY_PAUSE:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, intent);
			break;
		case STOP:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_STOP, intent);
			break;
		case NEXT:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_NEXT, intent);
			break;
		case FAST_FORWARD:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, intent);
			break;
		}
	}
}
