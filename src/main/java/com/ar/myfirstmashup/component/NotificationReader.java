package com.ar.myfirstmashup.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.util.CaseInsensitiveHashMap;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.instance.Notification;
import com.twilio.sdk.resource.list.NotificationList;

public class NotificationReader implements Callable {
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		CaseInsensitiveHashMap payload = (CaseInsensitiveHashMap) eventContext
				.getMessage().getPayload();
		TwilioRestClient client = new TwilioRestClient( 
				(String) payload.get("sid"), (String) payload.get("authtoken"));
		NotificationList notifications = client.getAccount().getNotifications();
		List<Map<String, String>> list = new ArrayList();
		// Loop over notifications and print out a property for each one.
		for (Notification notification : notifications) {
			Map<String, String> maps = new HashMap<String, String>();
			maps.put("number", notification.getCallSid());
			maps.put("text", notification.getMessageText());
			System.out.println(notification.getMessageText());
			System.out.println(notification.getCallSid());
			list.add(maps);
		}
		return list;
	}

}
