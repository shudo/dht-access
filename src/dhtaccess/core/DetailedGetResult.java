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

package dhtaccess.core;

public class DetailedGetResult {
	private byte[] value;
	private int ttl;
	private String hashType;
	private byte[] hashedSecret;

	public DetailedGetResult(byte[] value, int ttl, String hashType, byte[] hashedSecret) {
		this.value = value;
		this.ttl = ttl;
		this.hashType = hashType;
		this.hashedSecret = hashedSecret;
	}

	public byte[] getValue() { return this.value; }
	public int getTTL() { return this.ttl; }
	public String getHashType() { return this.hashType; }
	public byte[] getHashedSecret() { return this.hashedSecret; }
}
