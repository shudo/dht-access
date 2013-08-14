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

package dhtaccess.tools;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import dhtaccess.core.DHTAccessor;

public class Remove {
	private static final String COMMAND = "rm";
	private static final String ENCODE = "UTF-8";
	private static final String DEFAULT_GATEWAY = "http://opendht.nyuld.net:5851/";

	private static void usage(String command) {
		System.out.println("usage: " + command
				+ " [-h] [-g <gateway>] [-t <ttl (sec)>] <key> <value> <secret>");
	}

	public static void main(String[] args) {
		int ttl = 3600;

		// parse properties
		Properties prop = System.getProperties();
		String gateway = prop.getProperty("dhtaccess.gateway");
		if (gateway == null || gateway.length() <= 0) {
			gateway = DEFAULT_GATEWAY;
		}

		// parse options
		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("g", "gateway", true, "gateway URI, list at http://opendht.org/servers.txt");
		options.addOption("t", "ttl", true, "how long (in seconds) to store the value");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println("There is an invalid option.");
			e.printStackTrace();
			System.exit(1);
		}


		String optVal;
		if (cmd.hasOption('h')) {
			usage(COMMAND);
			System.exit(1);
		}
		optVal = cmd.getOptionValue('g');
		if (optVal != null) {
			gateway = optVal;
		}
		optVal = cmd.getOptionValue('t');
		if (optVal != null) {
			ttl = Integer.parseInt(optVal);
		}

		args = cmd.getArgs();

		// parse arguments
		if (args.length < 3) {
			usage(COMMAND);
			System.exit(1);
		}

		byte[] key = null, value = null, secret = null;
		try {
			key = args[0].getBytes(ENCODE);
			value = args[1].getBytes(ENCODE);
			secret = args[2].getBytes(ENCODE);
		} catch (UnsupportedEncodingException e1) {
			// NOTREACHED
		}

		// prepare for RPC
		DHTAccessor accessor = null;
		try {
			accessor = new DHTAccessor(gateway);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// RPC
		int res = accessor.remove(key, value, ttl, secret);

		String resultString;
		switch (res) {
		case 0:
			resultString = "Success";
			break;
		case 1:
			resultString = "Capacity";
			break;
		case 2:
			resultString = "Again";
			break;
		default:
			resultString = "???";
		}
		System.out.println(resultString);
	}
}
