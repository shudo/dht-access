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
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import dhtaccess.core.DHTAccessor;
import dhtaccess.core.DetailedGetResult;

public class Get {
	private static final String COMMAND = "get";
	private static final String ENCODE = "UTF-8";
	private static final String DEFAULT_GATEWAY = "http://opendht.nyuld.net:5851/";

	private static void usage(String command) {
		System.out.println("usage: " + command
				+ " [-h] [-g <gateway>] [-d] <key> [<key> ...]");
	}

	public static void main(String[] args) {
		boolean details = false;

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
		options.addOption("d", "details", false, "print secret hash and TTL");

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
		if (cmd.hasOption('d')) {
			details = true;
		}

		args = cmd.getArgs();

		// parse arguments
		if (args.length < 1) {
			usage(COMMAND);
			System.exit(1);
		}

		// prepare for RPC
		DHTAccessor accessor = null;
		try {
			accessor = new DHTAccessor(gateway);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		for (int index = 0; index < args.length; index++) {
			byte[] key = null;
			try {
				key = args[index].getBytes(ENCODE);
			} catch (UnsupportedEncodingException e1) {
				// NOTREACHED
			}

			// RPC
			if (args.length > 1) {
				System.out.println(args[index] + ":");
			}

			if (details) {
				Set<DetailedGetResult> results = accessor.getDetails(key);

				for (DetailedGetResult r: results) {
					String valString = null;
					try {
						valString = new String((byte[])r.getValue(), ENCODE);
					} catch (UnsupportedEncodingException e) {
						// NOTREACHED
					}

					BigInteger hashedSecure = new BigInteger(1, (byte[])r.getHashedSecret());

					System.out.println(valString
							+ " " + r.getTTL() + " " + r.getHashType()
							+ " 0x" + ("0000000" + hashedSecure.toString(16)).substring(0, 8));
				}
			}
			else {
				Set<byte[]> results = accessor.get(key);

				for (byte[] valBytes: results) {
					try {
						System.out.println(new String((byte[])valBytes, ENCODE));
					}
					catch (UnsupportedEncodingException e) {
						// NOTREACHED
					}
				}
			}
		}	// for (int index = 0...
	}
}
