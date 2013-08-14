/*
 * Copyright 2006 Kazuyuki Shudo.
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

package dhtaccess.benchmark;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import dhtaccess.core.DHTAccessor;
import dhtaccess.core.DetailedGetResult;

public class LatencyMeasure {
	private static final String COMMAND = "benchmark-latency";

	private static int KEY_PREFIX_LENGTH = 3;
	private static String VALUE_PREFIX = "value";
	private static final String ENCODE = "US-ASCII";
	private static final int TTL = 900;

	private static final int DEFAULT_REPEATS = 1000;

	private static void usage(String command) {
		System.out.println("usage: " + command
				+ " [-h] [-d] [-r <repeats>] [-n] <gateway> ... (e.g. http://localhost:5851/)");
	}

	public static void main(String[] args) {
		boolean details = false;
		int repeats = DEFAULT_REPEATS;
		boolean doPut = true;

		// parse options
		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("d", "details", false, "requests secret hash and TTL");
		options.addOption("r", "repeats", true, "number of requests");
		options.addOption("n", "no-put", false, "does not put");

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
		if (cmd.hasOption('d')) {
			details = true;
		}
		optVal = cmd.getOptionValue('r');
		if (optVal != null) {
			repeats = Integer.parseInt(optVal);
		}
		if (cmd.hasOption('n')) {
			doPut = false;
		}

		args = cmd.getArgs();

		// parse arguments
		if (args.length < 1) {
			usage(COMMAND);
			System.exit(1);
		}

		// prepare for RPC
		int numAccessor = args.length;
		DHTAccessor[] accessorArray = new DHTAccessor[numAccessor];
		try {
			for (int i = 0; i < numAccessor; i++) {
				accessorArray[i] = new DHTAccessor(args[i]);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// generate key prefix
		Random rnd = new Random(System.currentTimeMillis());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < KEY_PREFIX_LENGTH; i++) {
			sb.append((char)('a' + rnd.nextInt(26)));
		}
		String keyPrefix = sb.toString();
		String valuePrefix = VALUE_PREFIX;

		// benchmarking
		System.out.println("Repeats " + repeats + " times.");

		if (doPut) {
			System.out.println("Putting: " + keyPrefix + "<number>");

			for (int i = 0; i < repeats; i++) {
				byte[] key = null, value = null;
				try {
					key = (keyPrefix + i).getBytes(ENCODE);
					value = (valuePrefix + i).getBytes(ENCODE);
				}
				catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					System.exit(1);
				}

				int accIndex = rnd.nextInt(numAccessor);
				DHTAccessor acc = accessorArray[accIndex];
				acc.put(key, value, TTL);
			}
		}

		System.out.println("Benchmarking by getting.");

		int count = 0;
		long startTime = System.currentTimeMillis();

		if (details) {
			for (int i = 0; i < repeats; i++) {
				byte[] key = null;
				try {
					key = (keyPrefix + i).getBytes(ENCODE);
				}
				catch (UnsupportedEncodingException e) {
					e.printStackTrace(); System.exit(1);
				}

				int accIndex = rnd.nextInt(numAccessor);
				DHTAccessor acc = accessorArray[accIndex];
				Set<DetailedGetResult> valueSet = acc.getDetails(key);
				if (valueSet != null && !valueSet.isEmpty()) {
					count++;
				}
			}
		}
		else {
			for (int i = 0; i < repeats; i++) {
				byte[] key = null;
				try {
					key = (keyPrefix + i).getBytes(ENCODE);
				}
				catch (UnsupportedEncodingException e) {
					e.printStackTrace(); System.exit(1);
				}

				int accIndex = rnd.nextInt(numAccessor);
				DHTAccessor acc = accessorArray[accIndex];
				Set<byte[]> valueSet = acc.get(key);
				if (valueSet != null && !valueSet.isEmpty()) {
					count++;
				}
			}
		}

		System.out.println(System.currentTimeMillis() - startTime + " msec.");
		System.out.println("Rate of successful gets: " + count + " / " + repeats);
	}
}
