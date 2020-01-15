package src;

import java.util.List;
import java.util.HashMap;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.json.JSONObject;
import org.json.JSONException;

public class Logger {
	private static String failedLog = "Failed creating LOG file";

	public static List<JSONObject[]> spectraVarsStates = new ArrayList();
	HashMap<String, String> lastState = new HashMap<String, String>();
	PrintWriter htmlWriter;
	String[] envEntities = new String[] { "dayTime", "energyEfficiencyMode", "garbageCansNorth[0]",
			"garbageCansNorth[1]", "garbageCansNorth[2]", "garbageCansNorth[3]", "garbageCansSouth[0]",
			"garbageCansSouth[1]", "garbageCansSouth[2]", "garbageCansSouth[3]", "pedestrians[0]", "pedestrians[1]",
			"pedestrians[2]", "pedestrians[3]", "pedestrians[4]", "pedestrians[5]", "pedestrians[6]", "pedestrians[7]",
			"pedestrians[8]" };
	String[] sysEntities = new String[] { "garbageTruckNorth_location", "isCleaningN", "garbageTruckSouth_location",
			"isCleaningS", "lights[0]", "lights[1]", "lights[2]", "lights[3]", "lights[4]", "lights[5]" };

	private static String htmlStart = "<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "<style>\n" + "table {\n"
			+ "  font-family: arial, sans-serif;\n" + "  border-collapse: collapse;\n" + "  width: 100%;\n" + "}\n"
			+ "\n" + "td, th {\n" + "  border: 1px solid #dddddd;\n" + "  text-align: left;\n" + "  padding: 8px;\n"
			+ "}\n" + "</style>" + "<body>\n" + "\n" + "<h2>Smart Neighborhood Simulator Spectra Log</h2>\n" + "\n"
			+ "<table >\n" + "  <tr>\n" + "    <th style=\"text-align:center\" colspan=\"19\">Environment</th>\n"
			+ "    <th style=\"text-align:center\" colspan=\"10\">System</th>\n" + "  </tr>\n" + "  <tr>\n" + "    <th>dayTime</th>\n"
			+ "    <th>energyEfficiencyMode</th> \n" + "    <th>garbageCansNorth[0]</th>\n"
			+ "    <th>garbageCansNorth[1]</th>\n" + "    <th>garbageCansNorth[2]</th>\n"
			+ "    <th>garbageCansNorth[3]</th>\n" + "    <th>garbageCansSouth[0]</th>\n"
			+ "    <th>garbageCansSouth[1]</th>\n" + "    <th>garbageCansSouth[2]</th>\n"
			+ "    <th>garbageCansSouth[3]</th>\n" + "    <th>pedestrians[0]</th>\n" + "    <th>pedestrians[1]</th>\n"
			+ "    <th>pedestrians[2]</th>\n" + "    <th>pedestrians[3]</th>\n" + "    <th>pedestrians[4]</th>\n"
			+ "    <th>pedestrians[5]</th>\n" + "    <th>pedestrians[6]</th>\n" + "    <th>pedestrians[7]</th>\n"
			+ "    <th>pedestrians[8]</th>\n" + "    <th>garbageTruckNorth_location</th> \n"
			+ "    <th>isCleaningN</th> \n" + "    <th>garbageTruckSouth_location</th> \n"
			+ "    <th>isCleaningS</th> \n" + "    <th>lights[0]</th>\n" + "    <th>lights[1]</th>\n"
			+ "    <th>lights[2]</th>\n" + "    <th>lights[3]</th>\n" + "    <th>lights[4]</th>\n"
			+ "    <th>lights[5]</th>\n" + "  </tr>\n";

	private static String htmlEnd = "</table>\n" + "\n" + "</body>\n" + "</html>";

	public Logger() {
		lastState.put("dayTime", "DAY");
		lastState.put("energyEfficiencyMode", "false");
		lastState.put("garbageCansNorth[0]", "false");
		lastState.put("garbageCansNorth[1]", "false");
		lastState.put("garbageCansNorth[2]", "false");
		lastState.put("garbageCansNorth[3]", "false");
		lastState.put("garbageCansSouth[0]", "false");
		lastState.put("garbageCansSouth[1]", "false");
		lastState.put("garbageCansSouth[2]", "false");
		lastState.put("garbageCansSouth[3]", "false");
		lastState.put("pedestrians[0]", "false");
		lastState.put("pedestrians[1]", "false");
		lastState.put("pedestrians[2]", "false");
		lastState.put("pedestrians[3]", "false");
		lastState.put("pedestrians[4]", "false");
		lastState.put("pedestrians[5]", "false");
		lastState.put("pedestrians[6]", "false");
		lastState.put("pedestrians[7]", "false");
		lastState.put("pedestrians[8]", "false");

		lastState.put("garbageTruckNorth_location", "4");
		lastState.put("isCleaningN", "false");
		lastState.put("garbageTruckSouth_location", "4");
		lastState.put("isCleaningS", "false");
		lastState.put("lights[0]", "false");
		lastState.put("lights[1]", "false");
		lastState.put("lights[2]", "false");
		lastState.put("lights[3]", "false");
		lastState.put("lights[4]", "false");
		lastState.put("lights[5]", "false");
	}

	public void createLog() {
		createFile();

		boolean isFirst = true;

		for (JSONObject[] state : spectraVarsStates) {
			if (isFirst) {
				isFirst = false;
				continue;
			}

			String stateString = "\t<tr>\n";

			try {
				for (String envEntity : envEntities) {
					String newValue = state[0].getString(envEntity);
					if (!newValue.equals(lastState.get(envEntity))) {
						lastState.put(envEntity, newValue);
						stateString += createHtmlCell(newValue, true);
					} else
						stateString += createHtmlCell(newValue, false);
				}

				for (String sysEntity : sysEntities) {
					String newValue = state[1].getString(sysEntity);
					if (!newValue.equals(lastState.get(sysEntity))) {
						lastState.put(sysEntity, newValue);
						stateString += createHtmlCell(newValue, true);
					} else
						stateString += createHtmlCell(newValue, false);
				}

			} catch (JSONException e) {
				SocketServer.colorMe(SocketServer.messageTypes.ERROR, failedLog, false);
			}

			stateString += "\t</tr>";
			htmlWriter.write(stateString);
		}

		htmlWriter.write(htmlEnd);
		htmlWriter.close();
	}

	private String createHtmlCell(String value, boolean isChanged) {
		if (isChanged)
			return "\t\t<td style=\"color:red\">" + value + "</td>\n";

		return "\t\t<td>" + value + "</td>\n";
	}

	private void createFile() {
		try {
			htmlWriter = new PrintWriter("Log.html", "UTF-8");
			htmlWriter.write(htmlStart);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
