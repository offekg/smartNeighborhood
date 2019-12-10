package src;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.json.*;

public class SocketServer {
	private static enum messageTypes {
		INFO, ERROR, SUCCESS, WAITING
	}

	private static String noDataPayloadMessage = "No data payload attached";
	private static String waitingMessage = "Waiting for client request";
	private static String connectionReceivedMessage = "Request received";
	private static String connectionEndedMessage = "Request fully handled";
	private static String dataReceivingMessage = "Received data:";
	private static String dataSendingMessage = "Sent data:";
	private static String shutdownMessage = "Shutting down Socket server!!";
	private static String notWellFormattedMessage = "Data is not well formated";
	private static String issueWithMessage = "Issue found with variable ";

	private static ServerSocket server;
	private static int port = 9876;
	private static int houseCount = 4;
	private static String[] envVars = new String[] { "sidewalkNorth", "sidewalkSouth", "crossingCrosswalkNS",
			"crossingCrosswalkSN", "garbageCansNorth", "garbageCansSouth" };

	public static void main(String args[]) throws IOException, ClassNotFoundException {
		server = new ServerSocket(port);

		while (true) {
			byte[] messageByte = new byte[1000];
			String inputString = "";

			colorMe(messageTypes.WAITING, waitingMessage);
			Socket socket = server.accept();
			colorMe(messageTypes.SUCCESS, connectionReceivedMessage);

			InputStream ois = socket.getInputStream();
			DataInputStream in = new DataInputStream(ois);

			int bytesRead = in.read(messageByte);
			inputString += new String(messageByte, 0, bytesRead);

			JSONObject data = parseinputToDataJsonObject(inputString);
			HashMap<String, Boolean> dataDict = new HashMap<>();

			if (data != null) {
				dataDict = parseDataToEnvVars(data);
				colorMe(messageTypes.INFO, dataReceivingMessage + dataDict.toString());
			} else {
				colorMe(messageTypes.ERROR, noDataPayloadMessage);
			}
			
			
			JSONObject response = systemVarsToJson();
			NeighberhoodSimulator sim = new NeighberhoodSimulator();
			String test = ("test is " + sim.get_light_north());
			try (OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
				out.write(test.toString());
				colorMe(messageTypes.INFO, dataSendingMessage + test.toString());
			}

			ois.close();
			socket.close();
			colorMe(messageTypes.SUCCESS, connectionEndedMessage);

			if (data.has("exit"))
				break;
		}
		colorMe(messageTypes.INFO, shutdownMessage);
		server.close();
	}

	private static JSONObject parseinputToDataJsonObject(String input) {
		JSONObject js = null;
		String[] splitData = input.split("\n");

		for (String s : splitData) {
			if (s.startsWith("data:")) {
				try {
					js = new JSONObject(s.split("data:")[1]);
				} catch (JSONException e) {
					break;
				}
			}
		}

		return js;
	}

	private static HashMap<String, Boolean> parseDataToEnvVars(JSONObject data) {
		HashMap<String, Boolean> envVarsValues = new HashMap<>();

		for (String envVar : envVars) {
			try {
				Object varValue = data.get(envVar);
				if (envVar == "garbageCansNorth" || envVar == "garbageCansSouth") {
					JSONArray subData = (JSONArray) varValue;

					for (Integer i = 0; i < houseCount; i++) {
						envVarsValues.put(envVar + i.toString(), Boolean.parseBoolean(subData.get(i).toString()));
					}
				} else {
					envVarsValues.put(envVar, (Boolean) varValue);
				}
			} catch (Exception e) {
				colorMe(messageTypes.ERROR, notWellFormattedMessage);
				colorMe(messageTypes.ERROR, issueWithMessage + envVar);
				envVarsValues = new HashMap<>();
				break;
			}

			finally {
			}
		}

		return envVarsValues;
	}

	private static void colorMe(messageTypes type, String pleasePaintMe) {
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
	
	private static JSONObject systemVarsToJson() {
		HashMap<String, Object> sysVars = mocker();
		try {
			return new JSONObject(sysVars.toString());
		} catch (JSONException e) {
			return null;
		}
	}

	private static HashMap<String, Object> mocker() {
		HashMap<String, Object> mock = new HashMap<>();
		mock.put("isCleaning", true);
		mock.put("lightNorth", false);
		mock.put("lightSouth", false);
		mock.put("northTruckPos", 4);
		mock.put("southTruckPos", 1);
		
		return mock;
	}
}