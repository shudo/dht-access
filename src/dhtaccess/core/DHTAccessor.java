/*
 * Copyright 2006-2008 Kazuyuki Shudo.
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
 */

package dhtaccess.core;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class DHTAccessor {
	private static final int DEFAULT_TTL = 3600;	// second
	private static final int NUM_ITEMS_TO_GET = 10;

	private static final String PUT_TOOL_NAME = "put.py";
	private static final String GET_TOOL_NAME = "get.py";
	private static final String REMOVE_TOOL_NAME = "rm.py";

	private String gateway;
	XmlRpcClient client;

	public DHTAccessor(String gateway) throws MalformedURLException {
		this.setGateway(gateway);
	}

	/**
	 * Gets the URL of the gateway.
	 */
	public String getGateway() { return this.gateway; }

	/**
	 * Set the URL of the gateway.
	 */
	public void setGateway(String gateway) throws MalformedURLException {
		this.gateway = gateway;	// save

		URL gatewayURL = new URL(this.gateway);

		this.client = getXmlRpcClient(gatewayURL);
	}

	private static XmlRpcClient getXmlRpcClient(URL gateway) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(gateway);
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		return client;
	}

	/**
	 * Puts a key-value pair.
	 */
	public int put(byte[] key, byte[] value, int ttl /* sec */) {
		return this.put(key, value, ttl, null);
	}

	/**
	 * Puts a key-value pair with a secret, which is required to remove the pair.
	 */
	public int put(byte[] key, byte[] value, int ttl /* sec */, byte[] secret) {
		Object[] params;
		String methodName;

		if (ttl <= 0) {
			ttl = DEFAULT_TTL;
		}

		if (secret == null) {
			methodName = "put";

			params = new Object[4];
			params[0] = Util.hashWithSHA1(key);
			params[1] = value;
			params[2] = ttl;
			params[3] = PUT_TOOL_NAME;
		}
		else {
			methodName = "put_removable";

			params = new Object[6];
			params[0] = Util.hashWithSHA1(key);
			params[1] = value;
			params[2] = "SHA";
			params[3] = Util.hashWithSHA1(secret);
			params[4] = ttl;
			params[5] = PUT_TOOL_NAME;
		}

		int res = -1;
		try {
			res = (Integer)this.client.execute(methodName, params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}

		return res;
	}

	/**
	 * Gets a key-value pair.
	 */
	public Set<byte[]> get(byte[] key) {
		String methodName = "get";

		byte[] pm = null;
		try {
			pm = "".getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// NOTREACHED
		}

		Object[] params = new Object[4];
		params[0] = Util.hashWithSHA1(key);
		params[1] = NUM_ITEMS_TO_GET;
		params[2] = pm;
		params[3] = GET_TOOL_NAME;

		Set<byte[]> results = new HashSet<byte[]>();

		while (true) {
			Object[] rpcResults = null;
			try {
				rpcResults = (Object[])this.client.execute(methodName, params);
			} catch (XmlRpcException e) {
				e.printStackTrace();
				break;
			}

			Object[] values = (Object[])rpcResults[0];

			for (Object o: values) {
				results.add((byte[])o);
			}

			pm = (byte[])rpcResults[1];
			if (pm.length <= 0) break;
		}

		return results;
	}

	/**
	 * Gets a key-value pair in detail.
	 */
	public Set<DetailedGetResult> getDetails(byte[] key) {
		String methodName = "get_details";

		byte[] pm = null;
		try {
			pm = "".getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// NOTREACHED
		}

		Object[] params = new Object[4];
		params[0] = Util.hashWithSHA1(key);
		params[1] = NUM_ITEMS_TO_GET;
		params[2] = pm;
		params[3] = GET_TOOL_NAME;

		Set<DetailedGetResult> results = new HashSet<DetailedGetResult>();

		while (true) {
			Object[] rpcResults = null;
			try {
				rpcResults = (Object[])this.client.execute(methodName, params);
			} catch (XmlRpcException e) {
				e.printStackTrace();
				break;
			}

			Object[] values = (Object[])rpcResults[0];

			for (Object o: values) {
				Object[] v = (Object[])o;

				DetailedGetResult getResult = new DetailedGetResult(
						(byte[])v[0], (Integer)v[1], (String)v[2], (byte[])v[3]);

				results.add(getResult);
			}

			pm = (byte[])rpcResults[1];
			if (pm.length <= 0) break;
		}

		return results;
	}

	/**
	 * Removes a key-value pair.
	 */
	public int remove(byte[] key, byte[] value, byte[] secret) {
		return this.remove(key, value, DEFAULT_TTL, secret);
	}

	/**
	 * Removes a key-value pair.
	 */
	public int remove(byte[] key, byte[] value, int ttl, byte[] secret) {
		String methodName = "rm";

		Object[] params = new Object[6];
		params[0] = Util.hashWithSHA1(key);
		params[1] = Util.hashWithSHA1(value);
		params[2] = "SHA";
		params[3] = secret;
		params[4] = ttl;
		params[5] = REMOVE_TOOL_NAME;

		int res = -1;
		try {
			res = (Integer)this.client.execute(methodName, params);
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}

		return res;
	}
}
