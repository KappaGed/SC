/*
	@authors Rodrigo Silva 54416 | Gonçalo Cardoso 54415 | Pedro Correia 54570
 */

package src;

import src.catalogs.MessageCatalog;
import src.catalogs.SecureUserCatalog;
import src.catalogs.UserCatalog;
import src.catalogs.WineCatalog;
import src.catalogs.WineForSaleCatalog;
import src.objects.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class TintolmarketServer {

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {

		System.out.println("Server starting...");
		File dir = new File("Server");
		dir.mkdirs();

		TintolmarketServer server;

		int port = 12345;
		String passwordCifra = "";
		String keystore = "";
		String keystorePassword = "";

		if (args.length == 3) {
			System.out.println("Server started.");
			passwordCifra = args[0];
			keystore = args[1];
			keystorePassword = args[2];
		} else if (args.length == 4) {
			try {
				port = Integer.parseInt(args[0]);
				passwordCifra = args[1];
				keystore = args[2];
				keystorePassword = args[3];
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.out.println("Port with invalid characters!");
			}
		} else {
			System.out.println("TintolmarketServer <port> <password-cifra> <keystore> <password-keystore>");
			System.out.println("Server shutdown!");
			System.exit(-1);
		}

		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea,
				(byte) 0xf2 };
		PBEKeySpec keySpec = new PBEKeySpec(passwordCifra.toCharArray(), salt, 20);
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		SecretKey key = secretKeyFactory.generateSecret(keySpec);

		server = new TintolmarketServer();
		server.startServer(port, passwordCifra, keystore, keystorePassword, key);
	}

	public void startServer(int port, String passwordCifra, String keystore, String keystorePassword,
			SecretKey secretKey) {
		try {
			FileInputStream kfile = new FileInputStream(keystore); // keystore
			KeyStore kstore = KeyStore.getInstance(KeyStore.getDefaultType());
			kstore.load(kfile, keystorePassword.toCharArray()); // password da keystore
		} catch (FileNotFoundException e) {
			System.err.println("No KeyStore file");
			e.printStackTrace();
		} catch (KeyStoreException e) {
			System.err.println("KeyStore Error");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("No KeyStoreAlgorithm error");
			e.printStackTrace();
		} catch (CertificateException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		System.setProperty("javax.net.ssl.keyStore", keystore);
		System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);

		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
		SSLServerSocket sSoc = null;

		String pwCifra = passwordCifra;

		try {
			sSoc = (SSLServerSocket) ssf.createServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		while (true) {
			try {
				new ServerThread(sSoc.accept(), secretKey).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public class ServerThread extends Thread {

		UserCatalog userCatalog = new UserCatalog();
		WineCatalog wineCatalog = new WineCatalog();
		WineForSaleCatalog wineForSaleCatalog = new WineForSaleCatalog();
		MessageCatalog messageCatalog = new MessageCatalog();
		SecureUserCatalog secureUserCatalog;
		SecretKey secretKey;
		User currentUser;
		private final Socket socket;

		ServerThread(Socket inSoc, SecretKey secretKey) {
			socket = inSoc;
			this.secretKey = secretKey;
		}

		@Override
		public void run() {
			try {
				System.out.println("Running...");

				File authFile = new File("users.txt");
				if (!authFile.createNewFile()) {
					decrypt();
				}
				secureUserCatalog = new SecureUserCatalog();

				encrypt();

				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String user = (String) inStream.readObject();

				SecureRandom secureRandom = new SecureRandom();
				byte[] nonce = new byte[8];
				secureRandom.nextBytes(nonce);

				SecureUser currentSecureUser = secureUserCatalog.getUser(user);
				boolean userExists = currentSecureUser != null;

				outStream.writeObject(nonce);
				outStream.writeObject(userExists);

				Signature signature = Signature.getInstance("MD5withRSA");

				if (userExists) {
					SignedObject signedObject = (SignedObject) inStream.readObject();
					FileInputStream fileInputStream = new FileInputStream(currentSecureUser.getPublicKey());
					CertificateFactory cf = CertificateFactory.getInstance("X509");
					Certificate certificate = cf.generateCertificate(fileInputStream);
					PublicKey publicKey = certificate.getPublicKey();

					if (Arrays.equals((byte[]) signedObject.getObject(), nonce)
							&& signedObject.verify(publicKey, signature)) {
						currentUser = userCatalog.getUser(user);
						outStream.writeObject(true);
					} else {
						outStream.writeObject(false);
					}

				} else {

					byte[] receivedNonce = (byte[]) inStream.readObject();
					byte[] signatureBytes = (byte[]) inStream.readObject();
					Certificate certificate = (Certificate) inStream.readObject();
					PublicKey publicKey = certificate.getPublicKey();
					signature.initVerify(publicKey);

					if (Arrays.equals(receivedNonce, nonce) && signature.verify(signatureBytes)) {

						SecureUser secureUser = new SecureUser(user, user + "RSApub.cer");

						decrypt();
						secureUserCatalog.addUser(secureUser);
						encrypt();

						userCatalog.addUser(user);
						currentUser = userCatalog.getUser(user);
						outStream.writeObject(true);
					} else {
						outStream.writeObject(false);
					}
				}

				String operation = "";
				do {
					operation = (String) inStream.readObject();
					outStream.writeObject(processReq(operation, outStream, inStream));

				} while (!(operation.equals("q") || operation.equals("quit")));

				outStream.close();
				inStream.close();

				socket.close();

			} catch (IOException e) {
				System.out.println("Connection to client lost");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("Failed to connect to client");
				e.printStackTrace();
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | CertificateException e) {
				throw new RuntimeException(e);
			}
		}

		private String processReq(String op, ObjectOutputStream outputStream, ObjectInputStream inputStream) {

			String[] splittedOp = op.split(" ");
			String result = "";

			switch (splittedOp[0]) {

			case "a", "add":
				Wine wine = new Wine(splittedOp[1], splittedOp[2]);

				if (!wineCatalog.getWineCatalog().contains(wine)) {

					File wineImage = new File("Server/" + wine.getImagePath());

					try {
						if (!wineImage.createNewFile()) {
							result = "Image file already exists!";
							System.out.println("Failed to add wine");
						} else {
							wineCatalog.addWine(wine);
							wineCatalog.writeWineDB();
							result = "Wine was addded successfuly.";
							System.out.println("Wine was added.");
							FileOutputStream fileOutputStream = new FileOutputStream(wineImage);

							byte[] buffer = new byte[1024];
							int bytesRead;

							while ((bytesRead = inputStream.read(buffer)) != -1) {
								fileOutputStream.write(buffer, 0, bytesRead);
								fileOutputStream.flush();
								if (bytesRead != 1024) {
									break;
								}
							}
							fileOutputStream.flush();
							fileOutputStream.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else {
					result = "Wine already exists in the database!";
					System.out.println("Failed to add wine");
				}
				break;

			case "s", "sell":
				if (wineCatalog.getWine(splittedOp[1]) != null) {
					Wine sellWine = wineCatalog.getWine(splittedOp[1]);
					double price = Double.parseDouble(splittedOp[2]);
					int quantity = Integer.parseInt(splittedOp[3]);

					boolean exists = false;
					for (WineForSale wineSales : wineForSaleCatalog.getWineForSaleList()) {
						if (wineSales.getUsername().equals(currentUser.getUsername())
								&& (wineCatalog.getWine(wineSales.getWine()).equals(sellWine))) {
							exists = true;
							if (wineSales.getPrice() == price) {
								wineSales.addQuantity(quantity);
								sellWine.addQuantity(quantity);
							} else {

								int deltaQuantity = (wineSales.getQuantity() - quantity);
								wineSales.newPrice(price);
								wineSales.newQuantity(quantity);
								sellWine.addQuantity(deltaQuantity);

							}
						}
					}
					if (!exists) {
						WineForSale wineForSale = new WineForSale(currentUser.getUsername(), splittedOp[1], quantity,
								price);
						wineForSaleCatalog.add(wineForSale);
						sellWine.addQuantity(quantity);
					}

					wineCatalog.writeWineDB();
					wineForSaleCatalog.writeDatabase();
					result = "Wine was listed successfuly.";
					System.out.println("Listed wine");
				} else {
					result = "Wine doesn't exist in the database!";
					System.out.println("Failed to list wine");
				}
				break;

			case "v", "view":
				if (wineCatalog.getWine(splittedOp[1]) != null) {
					Wine viewWine = wineCatalog.getWine(splittedOp[1]);
					try {
						outputStream.writeObject(true);
						if ((Boolean) inputStream.readObject()) {
							File serverFile = new File("Server/" + viewWine.getImagePath());
							if (serverFile.exists()) {
								FileInputStream fileInputStream = new FileInputStream(serverFile);
								byte[] buffer = new byte[1024];
								int bytesRead;

								while ((bytesRead = fileInputStream.read(buffer)) != -1) {
									outputStream.write(buffer, 0, bytesRead);
									outputStream.flush();
								}
								outputStream.flush();
								fileInputStream.close();
							}
						}
					} catch (IOException | ClassNotFoundException e) {
						e.printStackTrace();
					}

					List<WineForSale> viewWFS = wineForSaleCatalog.getAllByWineName(viewWine.getName());
					String details = System.lineSeparator() + "Wine details:" + System.lineSeparator();
					details += "Image: " + viewWine.getImagePath() + System.lineSeparator() + "Rating: "
							+ String.format("%.2f", viewWine.getClassification()) + System.lineSeparator();

					if (!viewWFS.isEmpty()) {
						for (WineForSale winies : viewWFS) {
							details += System.lineSeparator() + "Seller: " + winies.getUsername()
									+ System.lineSeparator() + "Price: " + winies.getPrice() + System.lineSeparator()
									+ "Quantity: " + winies.getQuantity() + System.lineSeparator();
						}
					} else {
						details += "Quantity: " + viewWine.getQuantity() + System.lineSeparator();
					}

					result = details;
				} else {
					result = "Wine doesn't exist in the database!";
				}
				break;

			case "b", "buy":
				result = "fica para a proxima fase ;)";
				System.out.println(result);
				break;

			case "w", "wallet":
				result = "Your balance: " + currentUser.getBalance() + System.lineSeparator();
				System.out.println("Balance shown");
				break;
			case "c", "classify":
				String wineName = splittedOp[1];
				int rating = Integer.parseInt(splittedOp[2]);

				if (wineCatalog.getWine(wineName) != null) {

					wineCatalog.getWine(wineName).setRating(currentUser.getUsername(), rating);
					wineCatalog.writeWineDB();
					result = "Wine was rated successfuly." + System.lineSeparator() + "Thank you for your rating :)";
					System.out.println("Rated wine");
				} else {
					result = "Wine doesn't exist in the database!";
					System.out.println("Failed to rate wine");
				}
				break;

			case "t", "talk":
				String messageBuilder = "";
				for (int i = 2; i < splittedOp.length; i++) {
					if (i == splittedOp.length - 1) {
						messageBuilder += splittedOp[i];
					} else {
						messageBuilder += splittedOp[i] + " ";
					}
				}

				if (userCatalog.getUser(splittedOp[1]) != null) { // se rodas existe
					if (messageCatalog.getMessagesByUser(splittedOp[1]) != null) { // se rodas tem msgs
						messageCatalog.sendMessage(splittedOp[1], currentUser.getUsername(), messageBuilder);
					} else {
						Message message = new Message(splittedOp[1]);
						message.addMessage(currentUser.getUsername(), messageBuilder);
						messageCatalog.addMessage(message);
					}
					result = "Message sent to " + splittedOp[1] + " successfuly.";
					messageCatalog.writeMessagesDB();
					System.out.println("Message sent");
				} else {
					result = "User doesn't exist!";
					System.out.println("Failed to send message");
				}
				break;
			case "r", "read":
				try {
					String senderAndMessage = "";
					PrivateKey privateKey = (PrivateKey) inputStream.readObject();
					Cipher c = Cipher.getInstance("RSA");
					c.init(Cipher.DECRYPT_MODE, privateKey);

					if (messageCatalog.userHasMessages(currentUser.getUsername())) {
						senderAndMessage += "Your Inbox:" + System.lineSeparator();
						int i = 0;
						for (String senders : messageCatalog.getMessageSendersByUser(currentUser.getUsername())) {
							byte[] decodedData = Base64.getDecoder()
									.decode(messageCatalog.getMessagesByUser(currentUser.getUsername()).get(i));
							byte[] decryptedData = c.doFinal(decodedData);
							senderAndMessage += senders + " sent:" + System.lineSeparator() + new String(decryptedData)
									+ System.lineSeparator();
							i++;
						}
					} else {
						senderAndMessage = "Your inbox is empty :(";
					}

					result = senderAndMessage;
					messageCatalog.clearUserMessages(currentUser.getUsername());
					messageCatalog.writeMessagesDB();
					System.out.println("Messasges read");
					break;
				} catch (IllegalBlockSizeException | BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			case "l", "list":
				// TODO Obter a lista de todas as transacoes que ja foram efetuadas que se
				// encontram armazenadas na blockchain
			}
			return result;
		}

		private void encrypt() {
			try {

				File fileUsers = new File("users.txt");
				FileOutputStream fileUsersOS = new FileOutputStream(fileUsers);

				Cipher cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);
				fileUsersOS.write(cipher.doFinal(secureUserCatalog.toString().getBytes()));
				byte[] params = cipher.getParameters().getEncoded();

				File fileParams = new File("params.txt");
				FileOutputStream fileOutputStream = new FileOutputStream(fileParams, false);
				fileOutputStream.write(params);
				fileOutputStream.close();

			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException
					| IllegalBlockSizeException | IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		private void decrypt() {

			try {
				File fileParams = new File("params.txt");
				byte[] params = Files.readAllBytes(fileParams.toPath());

				File fileUsers = new File("users.txt");
				FileInputStream fis = new FileInputStream(fileUsers);

				AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
				p.init(params);
				Cipher cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
				cipher.init(Cipher.DECRYPT_MODE, secretKey, p);

				byte[] fileBytes = new byte[(int) fileUsers.length()];
				fis.read(fileBytes);

				byte[] decryptedData = cipher.doFinal(fileBytes);

				FileOutputStream fos = new FileOutputStream(fileUsers);
				fos.write(decryptedData);

				fileParams.delete();

			} catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException
					| BadPaddingException e) {
				throw new RuntimeException(e);
			}

		}
	}
}