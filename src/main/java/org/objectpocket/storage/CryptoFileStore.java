/*
 * Copyright (C) 2016 Edmund Klaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.objectpocket.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * @author Edmund Klaus
 *
 */
public class CryptoFileStore extends FileStore {

	private SecretKey secret = null;

	public CryptoFileStore(String directory, String password) {
		super(directory);
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec keySpec = new PBEKeySpec(password.toCharArray(), new byte[]{13,-45,89,-63,-76,78,9,101}, 65536, 128);
			SecretKey secretKey = factory.generateSecret(keySpec);
			secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Could not instanciate " + this.getClass().getName(), e);
		}
	}

	@Override
	protected OutputStreamWriter getOutputStreamWriterWriter(File file) throws IOException {
		try {
			Cipher cipherWrite = Cipher.getInstance("AES");
			cipherWrite.init(Cipher.ENCRYPT_MODE, secret);
			return new OutputStreamWriter(new CipherOutputStream(new FileOutputStream(file), cipherWrite));
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw new IOException("Could not instanciate OutputStreamWriter for file " + file.getPath(), e);
		}
	}

	@Override
	protected BufferedReader getBufferedReader(File file) throws IOException {
		try {
			Cipher cipherRead = Cipher.getInstance("AES");
			cipherRead.init(Cipher.DECRYPT_MODE, secret);
			return new BufferedReader(new InputStreamReader(new CipherInputStream(new FileInputStream(file), cipherRead)));
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw new IOException("Could not instanciate BufferedReader for file " + file.getPath(), e);
		}
	}

	protected String getReadErrorMessage() {
		return "The given password might be wrong.";
	}

}
