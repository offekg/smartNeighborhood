package src;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.json.*;

public class SocketServer {
	private static enum messageTypes {
		INFO, ERROR, SUCCESS, WAITING
	}

	private static ServerSocket server;
	private static int port = 9876;
	private static NeighberhoodSimulator sim;
	private static final File WEB_ROOT = new File(".");
	private static final String DEFAULT_FILE = "index.html";

	private static String headerPrefixSuffix = "================";
	private static String creatingSpectraObject = "Creating Spectra object";
	private static String spectraObjectCreated = "Spectra object created successfully";
	private static String noDataPayloadMessage = "No data payload attached";
	private static String waitingMessage = "Waiting for client request";
	private static String connectionReceivedMessage = "Request received";
	private static String connectionEndedMessage = "Request fully handled";
	private static String dataReceivingMessage = "Received data:";
	private static String dataSendingMessage = "Sent data:";
	private static String shutdownMessage = "Shutting down Socket server!!";
	private static String notWellFormattedMessage = "Data is not well formated";
	private static String issueWithMessage = "Issue found with variable ";
	private static String inputError = "Encountered an issue with request from cloent - " + "ignoring request";
	private static String outputError = "Encountered an issue with response to cloent";

	private static int houseCount = 4;
	private static String[] envVars = new String[] { "sidewalkNorth", "sidewalkSouth", "crossingCrosswalkNS",
			"crossingCrosswalkSN", "garbageCansNorth", "garbageCansSouth" };
	private static String[] sysVars = new String[] { "isCleaningN", "isCleaningS", "lightNorth", 
			"lightSouth", "garbageTruckNorth_location", "garbageTruckSouth_location" };

	private static String input;

	public static void main(String args[]) throws IOException, ClassNotFoundException {
		server = new ServerSocket(port);
		mocker();

		colorMe(messageTypes.INFO, creatingSpectraObject, true);
//		sim = new NeighberhoodSimulator();
		colorMe(messageTypes.SUCCESS, spectraObjectCreated, true);

		while (true) {
			colorMe(messageTypes.WAITING, waitingMessage, false);
			Socket socket = server.accept();
			colorMe(messageTypes.SUCCESS, connectionReceivedMessage, true);

			InputStream ois = socket.getInputStream();
			HashMap<String, Object> dataDict = inputStreamToDict(ois);
			
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client

			if (method.equals("GET"))
				// Used only for creating new session.
				newConnectionFound(socket, parse);
			else {
				if ((Boolean)dataDict.get("data_exists") == true) {
					createAndSendResponseToClient(socket, systemVarsToJson().toString(), (Boolean)dataDict.get("isClientChrome"));
				} else {
					// this is just for mocking
					// createAndSendResponseToClient(socket, systemVarsToJson().toString());
					createAndSendResponseToClient(socket, "Hello new user", (Boolean)dataDict.get("isClientChrome"));
				}
			}

			ois.close();
			socket.close();
			colorMe(messageTypes.SUCCESS, connectionEndedMessage, false);

			// TODO: exit server gracefully
			if ("".contains("1"))
				break;
		}
		colorMe(messageTypes.INFO, shutdownMessage, true);
		server.close();
	}

	private static void newConnectionFound(Socket socket, StringTokenizer parse) throws IOException {
		// we get file requested
		String path = parse.nextToken().toLowerCase();
		if (path.endsWith("/")) {
			path += DEFAULT_FILE;
		}
		File file = new File(WEB_ROOT, path);
		int fileLength = (int) file.length();
		String content = getContentType(path);

		byte[] fileData = readFileData(file, fileLength);

		PrintWriter out = new PrintWriter(socket.getOutputStream());
		// get binary output stream to client (for requested data)
		BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());
		// send HTTP Headers
		out.println("HTTP/1.1 200 OK");
		out.println("Server: Java HTTP Server from SSaurel : 1.0");
		out.println("Date: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println(); // blank line between headers and content, very important !
		out.flush(); // flush character output stream buffer

		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();

		out.close();
		dataOut.close();

	}

	private static byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null)
				fileIn.close();
		}

		return fileData;
	}

	private static String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}

	public static HashMap<String, Object> inputStreamToDict(InputStream ois) {
		byte[] messageByte = new byte[10000];
		String inputString = "";
		int bytesRead = 0;

		try {
			DataInputStream in = new DataInputStream(ois);
			bytesRead = in.read(messageByte);
		} catch (IOException e) {
			colorMe(messageTypes.ERROR, inputError, true);
			return null;
		}

		inputString = new String(messageByte, 0, bytesRead);

		JSONObject data = parseinputToDataJsonObject(inputString);
		HashMap<String, Object> dataDict = new HashMap<>();

		input = inputString;

		if (data != null) {
			dataDict = parseDataToEnvVars(data);
			dataDict.put("data_exists", true);
			colorMe(messageTypes.INFO, dataReceivingMessage + dataDict.toString(), false);
		} else {
			dataDict.put("data_exists", false);

			colorMe(messageTypes.ERROR, noDataPayloadMessage, false);
		}

		if (inputString.toLowerCase().contains("chrome"))
			dataDict.put("isClientChrome", true);
		else
			dataDict.put("isClientChrome", false);

		return dataDict;
	}

	private static JSONObject parseinputToDataJsonObject(String input) {
		JSONObject js = null;
		String[] splitData = input.split("\n");

		for (String s : splitData) {
			if (s.startsWith("data:")) {
				try {
					js = new JSONObject(s.split("data:")[1]);
				} catch (JSONException e) {
					return null;
				}
			}
		}

		return js;
	}

	private static void createAndSendResponseToClient(Socket socket, String content, boolean isChrome) {
		String response = "";
		if (isChrome) {
			response = "HTTP/1.1 200 OK\r\n" + "Content-Type: application/json\r\n\r\n" + "data: " + content;
		} else
			response = "data: " + content;

		try (OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
			out.write(response);
			colorMe(messageTypes.INFO, dataSendingMessage + content, false);
		} catch (Exception e) {
			colorMe(messageTypes.ERROR, outputError, true);
		}
	}

	private static JSONObject systemVarsToJson() {
		int temp = (mocker_pos++ + 1) % 7;
		HashMap<String, Object> sysVars = mock_list.get(temp);
		try {
			return new JSONObject(sysVars.toString());
		} catch (JSONException e) {
			return null;
		}
	}

	private static HashMap<String, Object> parseDataToEnvVars(JSONObject data) {
		HashMap<String, Object> envVarsValues = new HashMap<>();

		for (String envVar : envVars) {
			try {
				Object varValue = data.get(envVar);
				if (envVar == "garbageCansNorth" || envVar == "garbageCansSouth") {
					/*JSONArray subData = (JSONArray) varValue;

					for (Integer i = 0; i < houseCount; i++) {
						envVarsValues.put(envVar + i.toString(), Boolean.parseBoolean(subData.get(i).toString()));
					}*/
					envVarsValues.put(envVar, (Boolean[]) varValue);
				} else {
					envVarsValues.put(envVar, (Boolean) varValue);
				}
			} catch (Exception e) {
				colorMe(messageTypes.ERROR, notWellFormattedMessage, false);
				colorMe(messageTypes.ERROR, issueWithMessage + envVar, false);
				envVarsValues = new HashMap<>();
				break;
			}

			finally {
			}
		}

		return envVarsValues;
	}

	private static void colorMe(messageTypes type, String pleasePaintMe, boolean isHeader) {
		if (isHeader)
			pleasePaintMe = headerPrefixSuffix + pleasePaintMe + headerPrefixSuffix;

		switch (type) {
		case ERROR:
			System.out.println(ConsoleColors.RED_BRIGHT + pleasePaintMe + ConsoleColors.RESET);
			break;
		case SUCCESS:
			System.out.println(ConsoleColors.GREEN_BRIGHT + pleasePaintMe + ConsoleColors.RESET);
			break;
		case INFO:
			System.out.println(ConsoleColors.CYAN_BRIGHT + pleasePaintMe + ConsoleColors.RESET);
			break;
		case WAITING:
			System.out.println(ConsoleColors.PURPLE_BRIGHT + pleasePaintMe + ConsoleColors.RESET);
			break;
		}
	}

	private static List<HashMap<String, Object>> mock_list = new ArrayList<HashMap<String, Object>>();
	private static int mocker_pos = 0;

	private static void mocker() {
		HashMap<String, Object> mock = new HashMap<>();
		mock.put("isCleaningN", false);
		mock.put("isCleaningS", false);
		mock.put("lightNorth", true);
		mock.put("lightSouth", false);
		mock.put("garbageTruckNorth_location", 0);
		mock.put("garbageTruckSouth_location", 3);
		mock_list.add(mock);

		mock = new HashMap<>();
		mock.put("isCleaningN", false);
		mock.put("isCleaningS", false);
		mock.put("lightNorth", true);
		mock.put("lightSouth", true);
		mock.put("garbageTruckNorth_location", 1);
		mock.put("garbageTruckSouth_location", 2);
		mock_list.add(mock);

		mock = new HashMap<>();
		mock.put("isCleaningN", true);
		mock.put("isCleaningS", false);
		mock.put("lightNorth", true);
		mock.put("lightSouth", false);
		mock.put("garbageTruckNorth_location", 1);
		mock.put("garbageTruckSouth_location", 1);
		mock_list.add(mock);

		mock = new HashMap<>();
		mock.put("isCleaningN", false);
		mock.put("isCleaningS", true);
		mock.put("lightNorth", true);
		mock.put("lightSouth", true);
		mock.put("garbageTruckNorth_location", 2);
		mock.put("garbageTruckSouth_location", 1);
		mock_list.add(mock);

		mock = new HashMap<>();
		mock.put("isCleaningN", false);
		mock.put("isCleaningS", false);
		mock.put("lightNorth", true);
		mock.put("lightSouth", false);
		mock.put("garbageTruckNorth_location", 3);
		mock.put("garbageTruckSouth_location", 0);
		mock_list.add(mock);

		mock = new HashMap<>();
		mock.put("isCleaningN", false);
		mock.put("isCleaningS", true);
		mock.put("lightNorth", true);
		mock.put("lightSouth", true);
		mock.put("garbageTruckNorth_location", 4);
		mock.put("garbageTruckSouth_location", 0);
		mock_list.add(mock);

		mock = new HashMap<>();
		mock.put("isCleaningN", false);
		mock.put("isCleaningS", false);
		mock.put("lightNorth", true);
		mock.put("lightSouth", false);
		mock.put("garbageTruckNorth_location", 4);
		mock.put("garbageTruckSouth_location", -1);
		mock_list.add(mock);
	}
}