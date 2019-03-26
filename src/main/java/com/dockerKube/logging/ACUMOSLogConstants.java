/*
===============LICENSE_START=======================================================
Acumos
===================================================================================
Copyright (C) 2019 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
===================================================================================
This Acumos software file is distributed by AT&T and Tech Mahindra
under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
	   http://www.apache.org/licenses/LICENSE-2.0
 
This file is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
===============LICENSE_END=========================================================
*/
package com.dockerKube.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.MDC;


/**
* Constants for standard ACUMOS headers, MDCs, etc.
*
*/
public final class ACUMOSLogConstants {

	/**
	 * Hide and forbid construction.
	 */
	private ACUMOSLogConstants() {
		throw new UnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Inner classes.
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Marker constants.
	 */
	public static final class Markers {

		/** Marker reporting invocation. */
		public static final Marker INVOKE = MarkerFactory.getMarker("INVOKE");

		/** Marker reporting invocation. */
		public static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE_RETURN");

		/** Marker reporting synchronous invocation. */
		public static final Marker INVOKE_SYNCHRONOUS = build("INVOKE", "SYNCHRONOUS");

		/** Marker reporting asynchronous invocation. */
		public static final Marker INVOKE_ASYNCHRONOUS = build("INVOKE", "ASYNCHRONOUS");

		/** Marker reporting entry into a component. */
		public static final Marker ENTRY = MarkerFactory.getMarker("ENTRY");

		/** Marker reporting exit from a component. */
		public static final Marker EXIT = MarkerFactory.getMarker("EXIT");

		/**
		 * Build nested, detached marker.
		 * 
		 * @param m1
		 *            top token.
		 * @param m2
		 *            sub-token.
		 * @return detached Marker.
		 */
		private static Marker build(final String m1, final String m2) {
			final Marker marker = MarkerFactory.getDetachedMarker(m1);
			marker.add(MarkerFactory.getDetachedMarker(m2));
			return marker;
		}

		/**
		 * Hide and forbid construction.
		 */
		private Markers() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * MDC name constants.
	 */
	public static final class MDCs {

		// Tracing. ////////////////////////////////////////////////////////////

		/** MDC correlating messages for a logical transaction. */
		public static final String REQUEST_ID = "RequestID";

		/** MDC recording target service. */
		public static final String TARGET_SERVICE_NAME = "TargetServiceName";

		/** MDC recording target entity. */
		public static final String TARGET_ENTITY = "TargetEntity";

		// Network. ////////////////////////////////////////////////////////////

		/** MDC recording caller address. */
		public static final String CLIENT_IP_ADDRESS = "ClientIPAddress";

		/** MDC recording server address. */
		public static final String SERVER_FQDN = "ServerFQDN";

		/** MDC reporting outcome code. */
		public static final String RESPONSE_CODE = "ResponseCode";

		/** MDC reporting outcome description. */
		public static final String RESPONSE_DESCRIPTION = "ResponseDescription";

		/** MDC reporting outcome error level. */
		public static final String RESPONSE_SEVERITY = "Severity";

		/** MDC reporting outcome status of the request. */
		public static final String STATUS_CODE = "StatusCode";

		// Unsorted. ///////////////////////////////////////////////////////////

		/**
		 * Hide and forbid construction.
		 */
		private MDCs() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Header name constants.
	 */
	public static final class Headers {

		/** HTTP X-ACUMOS-RequestID header. */
		public static final String REQUEST_ID = "X-ACUMOS-RequestID";

		/**
		 * Hide and forbid construction.
		 */
		private Headers() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Overrideable method to set MDCs based on property values.
	 */
	public static void setDefaultMDCs() {
		MDC.put(MDCs.RESPONSE_SEVERITY, ResponseSeverity.DEBUG.toString());
		MDC.put(MDCs.STATUS_CODE, ResponseStatus.INPROGRESS.toString());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Enums.
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Response success or not, for setting StatusCode.
	 */
	public enum ResponseStatus {
		
		COMPLETED,
		ERROR,
		INPROGRESS
	}

	/**
	 * Response of log level, for setting Severity.
	 */
	public enum ResponseSeverity {

		INFO,
		ERROR,
		TRACE,
		DEBUG,
		WARN,
		FATAL
	}

	/**
	 * Synchronous or asynchronous execution, for setting invocation marker.
	 */
	public enum InvocationMode {

		/** Synchronous, blocking. */
		SYNCHRONOUS("SYNCHRONOUS", Markers.INVOKE_SYNCHRONOUS),

		/** Asynchronous, non-blocking. */
		ASYNCHRONOUS("ASYNCHRONOUS", Markers.INVOKE_ASYNCHRONOUS);

		/** Enum value. */
		private String mString;

		/** Corresponding marker. */
		private Marker mMarker;

		/**
		 * Construct enum.
		 *
		 * @param s
		 *            enum value.
		 * @param m
		 *            corresponding Marker.
		 */
		InvocationMode(final String s, final Marker m) {
			this.mString = s;
			this.mMarker = m;
		}

		/**
		 * Get Marker for enum.
		 *
		 * @return Marker.
		 */
		public Marker getMarker() {
			return this.mMarker;
		}

		@Override
		public String toString() {
			return this.mString;
		}
	}

}
