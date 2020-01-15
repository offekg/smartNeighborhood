package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import src.ScenarioManager;
import tau.smlab.syntech.executor.ControllerExecutor;
import tau.smlab.syntech.executor.ControllerExecutorException;

public class NeighberhoodSimulator {
	private static String initiationFailed = "Error occured when trying to initiate controller";
	private static String scenarioFinished = "Scenario inputs are done, it will run until default state";
	private static String failedUpdateToSpectra = "Update to spectra failed due to an unknown reason";
	private static String updaingToSpectra = "Updating environment variables in Spectra";
	private static String fetchingFromSpectra = "Fetching system variables from Spectra";

	private ControllerExecutor executor;
	private Random random = new Random();
	private ReentrantLock lock = new ReentrantLock();

	public enum DayTimeMode {
		DAY, NIGHT
	};

	private int N = 4;
	private ScenarioManager scenario;
	public boolean canScenarioStart = false;
	private int lastPedestrianId = 0;
	private int isFreezeCrosswalk = 0;

	/***** Environment variables *****/
	private boolean[] garbageCansNorth = new boolean[N];
	private boolean[] garbageCansNorthJustCleaned = new boolean[N];
	private boolean[] garbageCansSouth = new boolean[N];
	private boolean[] garbageCansSouthJustCleaned = new boolean[N];
	private DayTimeMode dayTime;
	private boolean energyEfficiencyMode;
	private List<Pedestrian> pedestrians = new ArrayList<>();

	/* dayTimeRelated */
	private int countHours = 0;

	/*-------------------------------*/

	/***** System variables *****/
	private boolean isCleaningN;
	private boolean isCleaningS;
	private boolean[] lightsNorth = new boolean[N - 1];
	private boolean[] lightsSouth = new boolean[N - 1];
	private int garbageTruckNorth_location; // location N means not on the street.
	private int garbageTruckSouth_location;
	/*-------------------------------*/

	private int sim_itter;

	public NeighberhoodSimulator() {
		executor = new ControllerExecutor(true, false);
		setEnvVarsToDefault();

		try {
			updateEnvVarsInSpectra();
			updateSystemVarsFromSpectra();
		} catch (ControllerExecutorException e) {
			SocketServer.colorMe(SocketServer.messageTypes.ERROR, initiationFailed, false);
			System.exit(1);
		}

	}

	public void initiateScenarios(int scenario_num) {
		switch (scenario_num) {
		case 1: // initiate scenario number 1
			scenario = new ScenarioManager(1);
			break;
		case 2: // initiate scenario number 2
			scenario = new ScenarioManager(2);
			break;
		case 3: // initiate scenario number 3
			scenario = new ScenarioManager(3);
			break;
		case 4: // initiate scenario number 4
			scenario = new ScenarioManager(4);
			break;
		case 5: // initiate scenario number 5
			scenario = new ScenarioManager(5);
			break;
		}
	}

	public HashMap<String, HashMap<String, Object>> getNextState(int mode, HashMap<String, Object> dataFromClient) {
		if (!canScenarioStart)
			mode = 12;

		switch (mode) {
		case 9: // random mode.
			moveAllPedestrians();
			randomNextState();
			break;
		case 10: // semi-automatic mode.
			moveAllPedestrians();
			randomNextState();
			updateEnvVarsFromClient(dataFromClient);
			break;
		case 11: // manual mode.
			moveAllPedestrians();
			updateEnvVarsFromClient(dataFromClient);
			break;
		case 12: // wait for default state
			moveAllPedestrians();
			break;
		case 100: // get scenario next state or random when scenario done
			HashMap<String, Object> nextState = scenario.getNextState();
			moveAllPedestrians();
			updateEnvVarsFromClient(nextState);

			if (scenario.scenarioQueue.size() == 0) {
				SocketServer.colorMe(SocketServer.messageTypes.INFO, scenarioFinished, false);
				canScenarioStart = false;
				SocketServer.setNewUserMode("automatic");
			}
			
			break;
		}

		try {
			updateEnvVarsInSpectra();
			updateSystemVarsFromSpectra();

		} catch (ControllerExecutorException e) {
			SocketServer.colorMe(SocketServer.messageTypes.ERROR, failedUpdateToSpectra, false);
			System.exit(1);
		}

		if (!canScenarioStart)
			isInDefaultState();
		return getSpectraVarsAsDict();
	}

	private void isInDefaultState() {
		for (int i = 0; i < N; i++) {
			if (garbageCansNorth[i] == true)
				return;
			if (garbageCansSouth[i] == true)
				return;
		}

		if (pedestrians.size() != 0)
			return;

		if (garbageTruckNorth_location != N)
			return;

		if (garbageTruckSouth_location != N)
			return;

		canScenarioStart = true;
		dayTime = DayTimeMode.DAY;
		energyEfficiencyMode = false;
		sim_itter = 0;
	}

	private void setEnvVarsToDefault() {
		for (int i = 0; i < N; i++) {
			garbageCansNorth[i] = false;
			garbageCansNorthJustCleaned[i] = false;
			garbageCansSouth[i] = false;
			garbageCansSouthJustCleaned[i] = false;
		}

		dayTime = DayTimeMode.DAY;
		energyEfficiencyMode = false;
		pedestrians = new ArrayList<>();

		sim_itter = 0;
	}

	private void updateEnvVarsInSpectra() throws ControllerExecutorException {
		SocketServer.colorMe(SocketServer.messageTypes.INFO, updaingToSpectra, false);

		for (int i = 0; i < N; i++) {
			garbageCansNorthJustCleaned[i] = false;
			if (garbageCansNorth[i] == true && garbageTruckNorth_location == i && isCleaningN) {
				garbageCansNorth[i] = false;
				garbageCansNorthJustCleaned[i] = true;
			}
			garbageCansSouthJustCleaned[i] = false;
			if (garbageCansSouth[i] == true && garbageTruckSouth_location == i && isCleaningS) {
				garbageCansSouthJustCleaned[i] = true;
				garbageCansSouth[i] = false;
			}
			executor.setInputValue(String.format("garbageCansNorth[%d]", i), String.valueOf(garbageCansNorth[i]));
			executor.setInputValue(String.format("garbageCansSouth[%d]", i), String.valueOf(garbageCansSouth[i]));
		}

		executor.setInputValue("dayTime", String.valueOf(dayTime));
		executor.setInputValue("energyEfficiencyMode", String.valueOf(energyEfficiencyMode));

		// first set all spectra pedestrian spaces to false, and then update from all pedestrians
		for (int i = 0; i < N * 2 + 1; i++) {
			executor.setInputValue(String.format("pedestrians[%d]", i), "false");
		}
		for (Pedestrian pedestrian : pedestrians) {
			if (pedestrian.isOnCrosswalk) {
				executor.setInputValue(String.format("pedestrians[%d]", N * 2), "true");
			} else if (pedestrian.position >= 0 && pedestrian.position <= 3) {
				if (pedestrian.isInTheNorth)
					executor.setInputValue(String.format("pedestrians[%d]", pedestrian.position), "true");
				else
					executor.setInputValue(String.format("pedestrians[%d]", pedestrian.position + N), "true");
			}
		}

		JSONObject inputs = new JSONObject(executor.getCurInputs());
		
		executor.updateState(true, true);
		
		JSONObject outputs = new JSONObject(executor.getCurOutputs());
		
		Logger.spectraVarsStates.add(new JSONObject[] {inputs, outputs});
	}

	private void updateSystemVarsFromSpectra() throws ControllerExecutorException {
		SocketServer.colorMe(SocketServer.messageTypes.INFO, fetchingFromSpectra, false);

		isCleaningN = Boolean.parseBoolean(executor.getCurValue("isCleaningN"));
		isCleaningS = Boolean.parseBoolean(executor.getCurValue("isCleaningS"));

		for (int i = 0; i < N - 1; i++) {
			lightsNorth[i] = Boolean.parseBoolean(executor.getCurValue(String.format("lights[%d]", i)));
			lightsSouth[i] = Boolean.parseBoolean(executor.getCurValue(String.format("lights[%d]", i + N - 1)));
		}

		garbageTruckNorth_location = Integer.parseInt(executor.getCurValue("garbageTruckNorth_location"));
		garbageTruckSouth_location = Integer.parseInt(executor.getCurValue("garbageTruckSouth_location"));
	}

	private void addRandomPedestrian() {
		int startPosition = random.nextInt(2);
		if (startPosition == 0)
			addNewPedestrian(-1, random.nextBoolean(), 0);
		else
			addNewPedestrian(4, random.nextBoolean(), 0);
	}

	private void addRandomPedestrianFromClient() {
		int startPosition = random.nextInt(2);
		if (startPosition == 0)
			addNewPedestrian(-2, random.nextBoolean(), 0);
		else
			addNewPedestrian(5, random.nextBoolean(), 0);
	}

	private void addNewPedestrian(int position, boolean isInNorth, int crossAlot) {
		Pedestrian p = new Pedestrian(++lastPedestrianId, position, isInNorth, crossAlot);
		lock.lock();
		pedestrians.add(p);
		lock.unlock();

		if (lastPedestrianId == 200)
			lastPedestrianId = 0;
	}

	private void moveAllPedestrians() {
		if (pedestrians.isEmpty())
			return;

		Iterator<Pedestrian> iter = pedestrians.iterator();
		lock.lock();
		while (iter.hasNext()) {
			Pedestrian p = iter.next();

			if (!p.isPedestrianExists())
				iter.remove();
			else
				p.move(isFreezeCrosswalk);
		}
		lock.unlock();
		isFreezeCrosswalk = (isFreezeCrosswalk + 1) % 200;
	}

	private String getPedestriansData() {
		List<HashMap<String, Object>> pedestriansData = new ArrayList<HashMap<String, Object>>();
		for (Pedestrian p : pedestrians)
			pedestriansData.add(p.getPedestrianState());

		return pedestriansData.toString();
	}

	private void randomNextState() {
		if (sim_itter == 0) {
			sim_itter += 1;
			return;
		}
		for (int i = 0; i < N; i++) {
			if (garbageCansNorth[i] == true && garbageTruckNorth_location == i && isCleaningN)
				garbageCansNorth[i] = false;
			else {
				// 1:10 chance of trash can becoming full
				if (garbageCansNorth[i] == false && !garbageCansNorthJustCleaned[i] && random.nextInt(10) == 0)
					garbageCansNorth[i] = true;
			}

			if (garbageCansSouth[i] == true && garbageTruckSouth_location == i && isCleaningS)
				garbageCansSouth[i] = false;
			else {
				// 1:10 chance of trash can becoming full
				if (garbageCansSouth[i] == false && !garbageCansSouthJustCleaned[i] && random.nextInt(10) == 0)
					garbageCansSouth[i] = true;
			}
		}

		if (random.nextInt(5) == 0) {
			addRandomPedestrian();
		}

		if (countHours != 0 && countHours % 12 == 0) {
			dayTime = DayTimeMode.values()[(dayTime.ordinal() + 1) % 2];
			if (dayTime == DayTimeMode.DAY)
				energyEfficiencyMode = false;
			else {
				energyEfficiencyMode = true;
			}
			countHours = 0;
		}
		countHours++;

	}

	private HashMap<String, HashMap<String, Object>> getSpectraVarsAsDict() {
		HashMap<String, Object> current_environment_state = new HashMap<>();
		current_environment_state.put("isNight", dayTime == DayTimeMode.NIGHT);
		current_environment_state.put("energyEfficiencyMode", energyEfficiencyMode);
		current_environment_state.put("garbageCansNorth", Arrays.toString(garbageCansNorth));
		current_environment_state.put("garbageCansSouth", Arrays.toString(garbageCansSouth));
		current_environment_state.put("pedestrians", getPedestriansData());

		HashMap<String, Object> current_system_state = new HashMap<>();
		current_system_state.put("isCleaningN", isCleaningN);
		current_system_state.put("isCleaningS", isCleaningS);
		current_system_state.put("lightsNorth", Arrays.toString(lightsNorth));
		current_system_state.put("lightsSouth", Arrays.toString(lightsSouth));
		current_system_state.put("garbageTruckNorth_location", garbageTruckNorth_location);
		current_system_state.put("garbageTruckSouth_location", garbageTruckSouth_location);

		HashMap<String, HashMap<String, Object>> current_full_state = new HashMap<>();
		current_full_state.put("environment", current_environment_state);
		current_full_state.put("system", current_system_state);

		return current_full_state;
	}

	public void updateEnvVarsFromClient(HashMap<String, Object> dataFromClient) {
		if (dataFromClient == null)
			return;

		for (String var : dataFromClient.keySet()) {
			switch (var) {
			case "pedestrian":
				try {
					Object[] pedestrian = (Object[]) dataFromClient.get("pedestrian");
					addNewPedestrian((int) pedestrian[0], (boolean) pedestrian[1], (int) pedestrian[2]);
				} catch (ClassCastException e) {
					addRandomPedestrianFromClient();
				}
				break;
			case "garbageCansNorth":
				try {
					garbageCansNorth[(int) dataFromClient.get("garbageCansNorth")] = true;
				} catch (ClassCastException e) {
					garbageCansNorth = (boolean[]) dataFromClient.get("garbageCansNorth");
				}
				break;
			case "garbageCansSouth":
				try {
					int index = Math.abs(3 - (int) dataFromClient.get("garbageCansSouth"));
					garbageCansSouth[index] = true;
				} catch (ClassCastException e) {
					garbageCansSouth = (boolean[]) dataFromClient.get("garbageCansSouth");
				}
				break;
			case "dayTime":
				dayTime = (DayTimeMode.valueOf((String) dataFromClient.get("dayTime")));
				break;
			case "energyEfficiencyMode":
				energyEfficiencyMode = (boolean) dataFromClient.get("energyEfficiencyMode");
				break;
			default:
				break;
			}
		}
	}
}
