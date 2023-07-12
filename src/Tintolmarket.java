/*
	@authors Rodrigo Silva 54416 | Gonçalo Cardoso 54415 | Pedro Correia 54570
 */

package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Tintolmarket {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		String serverAddress = args[0];
		int port = 12345;
		String truststore = args[1];
		String keystore = args[2];
		String keystorePassword = args[3];
		String userID = args[4];

		String alias = "";

		if (args.length < 5) {
			System.out.println("Tintolmarket <serverAddress> <truststore> <keystore> <password-keystore> <userID>");
			System.exit(1);
		}

		if (args[0].contains(":")) {
			String tempSv = args[0];
			String[] svpt = tempSv.split(":");
			serverAddress = svpt[0];
			port = Integer.parseInt(svpt[1]);
		}

		try {
			System.setProperty("javax.net.ssl.trustStore", truststore);
			System.setProperty("javax.net.ssl.trustStorePassword", "password");

			System.setProperty("javax.net.ssl.keyStore", keystore);
			System.setProperty("javax.net.ssl.keyStorePassword", keystorePassword);

			FileInputStream tfile = new FileInputStream(truststore);
			KeyStore tstore = KeyStore.getInstance(KeyStore.getDefaultType());
			tstore.load(tfile, "password".toCharArray());

			KeyStore kstore = KeyStore.getInstance(KeyStore.getDefaultType()); // keystore
			kstore.load(new FileInputStream(keystore), keystorePassword.toCharArray()); // password da keystore

			SocketFactory ssf = SSLSocketFactory.getDefault();
			SSLSocket socket = (SSLSocket) ssf.createSocket(serverAddress, port);

			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

			out.writeObject(userID);

			System.out.println("Autenticating...");

			byte[] nonce = (byte[]) in.readObject();
			boolean userExists = (Boolean) in.readObject();

			PrivateKey privateKey = (PrivateKey) kstore.getKey(kstore.aliases().nextElement().toString(),
					keystorePassword.toCharArray());
			Signature signature = Signature.getInstance("MD5withRSA");

			if (userExists) {

				SignedObject signedObject = new SignedObject(nonce, privateKey, signature);
				out.writeObject(signedObject);

				if ((boolean) in.readObject()) {
					System.out.println("User autenticated!");
					System.out.println("Welcome to Tintolmarket " + userID + "!\n\nChose an operation:");
					printMenu();

					String directoryName = "client/" + userID;
					File directory = new File(directoryName);
					directory.mkdirs();

				} else {
					System.out.println("Erro ao autenticar!");
					socket.close();
					System.exit(1);
				}

			} else {

				out.writeObject(nonce);

				signature.initSign(privateKey);

				out.writeObject(signature.sign());
				System.out.println(keystore);
				String[] name = keystore.split("\\.");
				System.out.println(name);
				Certificate certificate = tstore.getCertificate(name[0]);
				out.writeObject(certificate);

				if ((Boolean) in.readObject()) {
					System.out.println("Registo bem sucedido!");
					System.out.println("Welcome to Tintolmarket " + userID + "!\n\nChose an operation:");
					printMenu();

					String directoryName = "client/" + userID;
					File directory = new File(directoryName);
					directory.mkdirs();

				} else {
					System.out.println("O registo e autenticação não foi bem sucedido!");
					socket.close();
					System.exit(1);
				}
			}

			String op = "";

			while (true) {
				op = sc.nextLine();

				String serverResponse = choseOperation(userID, op, out, in, tstore, kstore, keystorePassword);

				if (serverResponse.equals("q")) {

					break;
				}

				System.out.println(serverResponse);
				if (!serverResponse.equals("")) {
					System.out.println("(Type \"help\" or \"h\" to show the menu.)");
					System.out.print(">");
				}
			}
			sc.close();
			socket.close();
		} catch (ClassNotFoundException e) {
			System.out.println("Failed to connect!");
			e.printStackTrace();
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException | InvalidKeyException | SignatureException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String choseOperation(String userID, String op, ObjectOutputStream objectOutputStream,
			ObjectInputStream objectInputStream, KeyStore truststore, KeyStore keystore, String keystorePassword) {

		String[] mike = op.split(" ");
		String result = "";

		try {
			switch (mike[0]) {
			case "a", "add":
				if (mike.length == 3) {

					String path = "client/" + userID + "/" + mike[2];

					File file = new File(path);

					if (!file.exists()) {
						result = "Ficheiro não existe!";
					} else {

						objectOutputStream.writeObject(op);

						FileInputStream fileInputStream = new FileInputStream(path);
						byte[] buffer = new byte[1024];
						int bytesRead;

						while ((bytesRead = fileInputStream.read(buffer)) != -1) {
							objectOutputStream.write(buffer, 0, bytesRead);
							objectOutputStream.flush();
						}
						objectOutputStream.flush();
						fileInputStream.close();

						result = (String) objectInputStream.readObject();
					}
				} else {
					result = "add <wine> <image>";
				}
				break;
			case "s", "sell":
				if (mike.length == 4) {
					objectOutputStream.writeObject(op);
					result = (String) objectInputStream.readObject();
				} else {
					result = "sell <wine> <value> <quantity>";
				}
				break;
			case "v", "view":
				if (mike.length == 2) {

					objectOutputStream.writeObject(op);
					if ((Boolean) objectInputStream.readObject()) {
						String path = "client/" + userID + "/" + mike[1] + ".jpg";
						File newImage = new File(path);
						if (newImage.createNewFile()) {

							objectOutputStream.writeObject(true);

							FileOutputStream fileOutputStream = new FileOutputStream(newImage);

							byte[] buffer = new byte[1024];
							int bytesRead;

							while ((bytesRead = objectInputStream.read(buffer)) != -1) {
								fileOutputStream.write(buffer, 0, bytesRead);
								fileOutputStream.flush();

								if (bytesRead != 1024) {
									break;
								}
							}
							fileOutputStream.flush();
							fileOutputStream.close();

							System.out.println("Imagem transferida para \"" + path + "\"");
						} else {
							objectOutputStream.writeObject(false);
							System.out.println("Imagem ja existente em \"" + path + "\"");
						}
					}

					result = (String) objectInputStream.readObject();
				} else {
					result = "view <wine>";
				}

				break;
			case "b", "buy":
				if (mike.length == 4) {
					objectOutputStream.writeObject(op);
					result = (String) objectInputStream.readObject();
				} else {
					result = "buy <wine> <seller> <quantity>";
				}
				break;
			case "w", "wallet":
				objectOutputStream.writeObject(op);
				result = (String) objectInputStream.readObject();
				break;
			case "c", "classify":
				if (mike.length == 3) {
					if (Integer.parseInt(mike[2]) > 0 && Integer.parseInt(mike[2]) <= 5) {
						objectOutputStream.writeObject(op);
						result = (String) objectInputStream.readObject();
					} else {
						result = "Ratings are from 1 to 5";
					}
				} else {
					result = "classify <wine> <stars>";

				}
				break;
			case "t", "talk":
				if (mike.length >= 3) {
					String message = "";
					for (int i = 2; i < mike.length; i++) {
						if (i == mike.length - 1) {
							message += mike[i];
						} else {
							message += mike[i] + " ";
						}
					}
					String encryptedMessage = mike[0] + " " + mike[1] + " ";
					Cipher c = Cipher.getInstance("RSA");

					Certificate cert = truststore.getCertificate(mike[1]);

					PublicKey publicKey = cert.getPublicKey();
					c.init(Cipher.ENCRYPT_MODE, publicKey);

					byte[] encrypted = c.doFinal(message.getBytes());
					String encodedData = Base64.getEncoder().encodeToString(encrypted);
					encryptedMessage += encodedData;

					objectOutputStream.writeObject(encryptedMessage);
					result = (String) objectInputStream.readObject();
				} else {
					result = "talk <user> <message>";
				}
				break;
			case "r", "read":
				objectOutputStream.writeObject(op);
				PrivateKey privateKey = (PrivateKey) keystore.getKey(keystore.aliases().nextElement().toString(), keystorePassword.toCharArray());
				objectOutputStream.writeObject(privateKey);

				result = (String) objectInputStream.readObject();
				break;
			case "q", "quit":
				result = "q";
				break;
			case "h", "help":
				result = "";
				printMenu();
				break;
			default:
				result = "Invalid command!";
				break;
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Not enough details.");
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
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static void printMenu() {
		System.out.println("add <wine> <image>");
		System.out.println("sell <wine> <value> <quantity>");
		System.out.println("view <wine>");
		System.out.println("buy <wine> <seller> <quantity>");
		System.out.println("wallet");
		System.out.println("classify <wine> <stars>");
		System.out.println("talk <user> <message>");
		System.out.println("read");
		System.out.println("quit");
		System.out.print(">");
	}
}
