
package com.github.ulwx.aka.dbutils.springboot.redis;


class RedisUrlSyntaxException extends RuntimeException {

	private final String url;

	RedisUrlSyntaxException(String url, Exception cause) {
		super(buildMessage(url), cause);
		this.url = url;
	}

	RedisUrlSyntaxException(String url) {
		super(buildMessage(url));
		this.url = url;
	}

	String getUrl() {
		return this.url;
	}

	private static String buildMessage(String url) {
		return "Invalid Redis URL '" + url + "'";
	}

}
