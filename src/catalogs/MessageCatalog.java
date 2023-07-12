package src.catalogs;

import src.objects.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MessageCatalog {
	// estrutura User:Sender-Message,Sender-Message,...

	List<Message> messageCatalog = new ArrayList<>();

	public MessageCatalog() {
		readMessagesDB();
	}

	public void addMessage(Message message) {
		messageCatalog.add(message);
	}

	public List<String> getMessagesByUser(String user) {
		for (Message message : messageCatalog) {
			if (message.getReceiver().equals(user)) {
				return message.getMessages();
			}
		}
		return null;
	}

	public List<byte[]> getEncrypedMsgsByUser(String user) {
		for (Message message : messageCatalog) {
			if (message.getReceiver().equals(user)) {
				return message.getEncryptedMsgs();
			}
		}
		return null;
	}

	public List<String> getMessageSendersByUser(String user) {
		for (Message message : messageCatalog) {
			if (message.getReceiver().equals(user)) {
				return message.getSenders();
			}
		}
		return null;
	}

	public boolean userHasMessages(String user) {
		for (Message message : messageCatalog) {
			if (message.getReceiver().equals(user)) {
				return message.hasMessages();
			}
		}
		return false;
	}

	public void clearUserMessages(String user) {
		for (Message message : messageCatalog) {
			if (message.getReceiver().equals(user)) {
				message.clearMessages();
			}
		}
	}

	public void writeMessagesDB() {

		File messagesDB = new File("messages.txt");

		try {
			FileWriter fileWriter = new FileWriter(messagesDB, false);

			for (Message message : messageCatalog) {

				if (!(message.getSenders().isEmpty() && message.getMessages().isEmpty())) {
					fileWriter.write(message.getReceiver() + ":");
					int index = 0;
					for (String sender : message.getSenders()) {
						fileWriter.write(sender + "-" + message.getMessages().get(index));
						if (index != message.getSenders().size() - 1) {
							fileWriter.append(",");
						} else
							fileWriter.write(System.lineSeparator());
						index++;
					}
				} else {
					fileWriter.write("");
				}
			}
			fileWriter.close();
		} catch (IOException e) {
			System.out.println("Couldn't find messages database file!");
			e.printStackTrace();
		}
	}

	public void readMessagesDB() {

		File messagesDB = new File("messages.txt");

		try {
			if (messagesDB.createNewFile()) {
				System.out.println("Messages database created");
			}
			Scanner dbSc = new Scanner(messagesDB);

			while (dbSc.hasNextLine()) {

				String line = dbSc.nextLine();
				String[] atributes = line.split(":");

				String user = atributes[0];
				String rawSenderAndMessages = atributes[1];

				String[] individualSenderAndMessage = rawSenderAndMessages.split(",");

				List<String> senders = new ArrayList<>();
				List<String> messages = new ArrayList<>();

				for (String senderAndMessage : individualSenderAndMessage) {
					String[] senderAndMessageSplit = senderAndMessage.split("-");
					senders.add(senderAndMessageSplit[0]);
					messages.add(senderAndMessageSplit[1]);
				}
				Message message = new Message(user, senders, messages);
				messageCatalog.add(message);
			}
			dbSc.close();
		} catch (IOException e) {
			System.out.println("Couldn't find messages database file!");
			e.printStackTrace();
		}
	}

	public void sendMessage(String user, String sender, String msg) {
		for (Message message : messageCatalog) {
			if (message.getReceiver().equals(user)) {
				message.addMessage(sender, msg);
			}
		}
	}
}