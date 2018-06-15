package no.sample.buffer;

import lombok.Getter;

@Getter
public class PushMessage {
	private final String topic;
	private final String content;

	public PushMessage(String topic, String content) {
		this.topic = topic;
		this.content = content;
	}

	public String json() {
		return "{\"topic\": \"" + topic + "\", \"content\": " + content + "}";
	}
}