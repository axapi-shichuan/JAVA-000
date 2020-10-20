package com.shichuan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * project_name: prictice
 * package: com.shichuan.week1
 * description:
 * 自定义ClassLoader
 *
 * @author: 史川
 * @Date: 2020/10/17 14:20
 **/
public class CustomClassLoader extends ClassLoader {

	public static void main(String[] args) {
		try {
			CustomClassLoader customClassLoader = new CustomClassLoader();
			Class helloClass = customClassLoader.findClass("Hello");
			Object obj = helloClass.newInstance();
			Method method = helloClass.getMethod("hello");
			method.invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final String CLASS_PATH = "classes/";

	private static final String CLASS_POST_FIX = ".xlass";

	private String buildPath(String name) {
		return CLASS_PATH + name + CLASS_POST_FIX;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] clazzBytes;
		try {
			clazzBytes = loadClassByName(name);
		} catch (IOException e) {
			throw new ClassNotFoundException();
		}
		byte[] decodedClassBytes = decodeClassBytes(clazzBytes);
		return defineClass(name, decodedClassBytes, 0, decodedClassBytes.length);
	}

	private byte[] loadClassByName(String name) throws IOException {
		String path = buildPath(name);
		InputStream inputStream = getResourceAsStream(path);
		if (inputStream == null) {
			throw new IOException();
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[1024];
			int readLen;
			while ((readLen = inputStream.read(buffer)) != -1) {
				bos.write(buffer, 0, readLen);
			}
		} finally {
			inputStream.close();
			bos.close();
		}
		return bos.toByteArray();
	}

	private byte[] decodeClassBytes(byte[] encodedClassBytes) {
		byte[] decodedClassBytes = new byte[encodedClassBytes.length];
		for (int i = 0; i < encodedClassBytes.length; i++) {
			decodedClassBytes[i] = decode(encodedClassBytes[i]);
		}
		return decodedClassBytes;
	}

	private byte decode(byte param) {
		return (byte) (255 - param);
	}
}
