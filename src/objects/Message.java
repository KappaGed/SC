package src.objects;

import java.util.ArrayList;
import java.util.List;

public class Message {
	private String receiver;
	private List<String> senders;
	private List<String> messages;
	private List<byte[]> encryptedMsgs;

	public Message(String user, List<String> senders, List<String> messages) {
		this.receiver = user;
		this.senders = senders;
		this.messages = messages;
	}

	public Message(String user) {
		this.receiver = user;
		senders = new ArrayList<>();
		messages = new ArrayList<>();
		encryptedMsgs = new ArrayList<>();
	}

	public String getReceiver() {
		return receiver;
	}

	public List<String> getSenders() {
		return senders;
	}

	public List<String> getMessages() {
		return messages;
	}

	public List<byte[]> getEncryptedMsgs() {
		return encryptedMsgs;
	}

	public boolean hasMessages() {
		return !(senders.isEmpty() && messages.isEmpty());

	}

	public void clearMessages() {
		senders.clear();
		messages.clear();
	}

	public void addMessage(String sender, String message) {
		senders.add(sender);
		messages.add(message);
	}
}
