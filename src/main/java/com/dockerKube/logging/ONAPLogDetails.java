/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
 * ===================================================================================
 * This Acumos software file is distributed by AT&T and Tech Mahindra
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */
package com.dockerKube.logging;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.slf4j.MDC;

public class ONAPLogDetails {
	
	public static void setMDCDetails(String requestId,String user) {

		// Extract MDC values from standard HTTP headers.
		final String requestID = requestId;
		final String invocationID = getUUID();
		
		// Get the UserName
		final String userName = user;
		MDC.put(ONAPLogConstants.MDCs.USER,userName);
				

		// Set standard MDCs. 
		MDC.put(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP,
				ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT));
		MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestID);
		MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, invocationID);
		/*if (!partnerName.isEmpty())
			MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
		MDC.put(ONAPLogConstants.MDCs.CLIENT_IP_ADDRESS, defaultToEmpty(request.getClientAddress()));
		MDC.put(ONAPLogConstants.MDCs.SERVER_FQDN, defaultToEmpty(request.getServerAddress()));	*/

		
	}
	protected static String getUUID() {
		return UUID.randomUUID().toString();
	}
	
	protected static String defaultToEmpty(final Object in) {
		if (in == null) {
			return "";
		}
		return in.toString();
	}
   public static void clearMDCDetails() {
		MDC.clear();
	}

}
