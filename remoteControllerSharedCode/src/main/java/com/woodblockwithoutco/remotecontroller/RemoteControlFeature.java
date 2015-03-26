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
package com.woodblockwithoutco.remotecontroller;

public enum RemoteControlFeature {
	
	/**
	 *  FAST_FORWARD button is supported by client.
	 */
	USES_FAST_FORWARD, 
	
	/**
	 * NEXT button is supported by client.
	 */
	USES_NEXT, 
	
	/**
	 *  PAUSE button is supported by client.
	 */
	USES_PAUSE, 
	
	/**
	 *  PLAY button is supported by client.
	 */
	USES_PLAY, 
	
	/**
	 *  PLAY/PAUSE button is supported by client.
	 */
	USES_PLAY_PAUSE, 
	
	/**
	 *  PREVIOUS button is supported by client.
	 */
	USES_PREVIOUS, 
	
	/**
	 *  REWIND button is supported by client.
	 */
	USES_REWIND, 
	
	/**
	 *  STOP  button is supported by client.
	 */
	USES_STOP,
	
	/**
	 * Client uses positioning. Please note this is unsupported on API lower than API 19. 
	 */
	USES_POSITION,
	
	/**
	 * Client uses position scrubbing. Please note this is unsupported on API lower than API 19.
	 */
	USES_SCRUBBING
}

