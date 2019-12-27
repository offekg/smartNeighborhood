package src;

import java.util.Arrays;
import java.util.Random;
import java.util.HashMap;

import src.ScenarioManager;

import tau.smlab.syntech.executor.ControllerExecutor;
import tau.smlab.syntech.executor.ControllerExecutorException;

public class NeighberhoodSimulator {
	/**
	 * 
	 */

	ControllerExecutor executor;
	Random random = new Random();
	enum DayTimeMode {DAY, NIGHT};
	int N = 4;
	ScenarioManager scenario;

	/***** Environment variables *****/
	boolean sidewalkNorth;
	boolean sidewalkSouth;
	boolean crossingCrosswalkNS;
	// boolean crossingCrosswalkSN;
	boolean[] garbageCansNorth = new boolean[N];
	boolean[] garbageCansSouth = new boolean[N];
	DayTimeMode dayTime;
	boolean energyEfficiencyMode;
	
	/* dayTimrRelated */
	int countHours = 0;

	/*-------------------------------*/

	/***** System variables *****/
	boolean isCleaningN;
	boolean isCleaningS;
	boolean lightNorth;
	boolean lightSouth;
	int garbageTruckNorth_location; // location N means not on the street.
	int garbageTruckSouth_location;
	/*-------------------------------*/

	int sim_itter;
	
	public NeighberhoodSimulator() {
		setEnvVarsToDefault();

		try {
			updateEnvVarsInSpectra();

			updateSystemVarsFromSpectra();
		} catch (ControllerExecutorException e) {
			// TODO: handle it gracefully.
		}

	}

	public HashMap<String, HashMap<String, Object>> getNextState(int scenario_num, HashMap<String, Object> dataFromClient) {
		switch (scenario_num) {
		case -1:
			setEnvVarsToDefault();
			break;
		case 0: // random mode.
			randomNextState();
			break;
		case 1: // semi-automatic mode.
			randomNextState();
			updateEnvVarsFromClient(dataFromClient);
			break;
		case 2: // manual mode.
			updateEnvVarsFromClient(dataFromClient);
			break;
		case 3: // initiate scenario number 1
			setEnvVarsToDefault();
			scenario = new ScenarioManager(1);
			break;
		case 4: // initiate scenario number 2
			setEnvVarsToDefault();
			scenario = new ScenarioManager(2);
			break;
		case 5: // initiate scenario number 3
			setEnvVarsToDefault();
			scenario = new ScenarioManager(3);
			break;
		case 6: // initiate scenario number 4
			setEnvVarsToDefault();
			scenario = new ScenarioManager(4);
			break;
		case 7: // initiate scenario number 5
			setEnvVarsToDefault();
			scenario = new ScenarioManager(5);
			break;
		case 8: // initiate scenario number 6
			setEnvVarsToDefault();
			scenario = new ScenarioManager(6);
			break;
		case 9: // initiate scenario number 7
			setEnvVarsToDefault();
			scenario = new ScenarioManager(7);
			break;
		case 10: // initiate scenario number 8
			setEnvVarsToDefault();
			scenario = new ScenarioManager(8);
			break;
		case 11: // get scenario next state or random when scenario done
			HashMap<String, Object> nextState = scenario.getNextState();
			if (nextState != null)
				updateEnvVarsFromClient(nextState);
			else
				randomNextState();
		}

		try {
			updateEnvVarsInSpectra();
			updateSystemVarsFromSpectra();

		} catch (ControllerExecutorException e) {
			// TODO: handle it gracefully.h{
			System.out.println("Heyo");
		}

		return getSpectraVarsAsDict();
	}
	
	private void setEnvVarsToDefault() {
		// Instantiate a new controller executor
		executor = new ControllerExecutor(true, false);
		
		sidewalkNorth = false;
		sidewalkSouth = false;
		crossingCrosswalkNS = false;
		// crossingCrosswalkSN;
		for (int i = 0; i < N; i++) {
			garbageCansNorth[i] = false;
			garbageCansSouth[i] = false;
		}
		dayTime = DayTimeMode.DAY;
		energyEfficiencyMode = false;
		
		sim_itter = 0;
	}

	private void updateEnvVarsInSpectra() throws ControllerExecutorException {
		System.out.println("Updating environment variables in Spectra");

		for (int i = 0; i < N; i++) {
			executor.setInputValue(String.format("garbageCansNorth[%d]", i), String.valueOf(garbageCansNorth[i]));
			executor.setInputValue(String.format("garbageCansSouth[%d]", i), String.valueOf(garbageCansSouth[i]));
		}
		executor.setInputValue("sidewalkSouth", String.valueOf(sidewalkSouth));
		executor.setInputValue("sidewalkNorth", String.valueOf(sidewalkNorth));
		executor.setInputValue("crossingCrosswalkNS", String.valueOf(crossingCrosswalkNS));
		executor.setInputValue("dayTime", String.valueOf(dayTime));
		executor.setInputValue("energyEfficiencyMode", String.valueOf(energyEfficiencyMode));


		executor.updateState(true, true); // TODO: I changed it to false due to many errors it gave me, check what is
											// right
	}

	private void updateSystemVarsFromSpectra() throws ControllerExecutorException {
		System.out.println("Getting system variables from Spectra");

		isCleaningN = Boolean.parseBoolean(executor.getCurValue("isCleaningN"));
		isCleaningS = Boolean.parseBoolean(executor.getCurValue("isCleaningS"));
		lightNorth = Boolean.parseBoolean(executor.getCurValue("lightNorth"));
		lightSouth = Boolean.parseBoolean(executor.getCurValue("lightSouth"));
		garbageTruckNorth_location = Integer.parseInt(executor.getCurValue("garbageTruckNorth_location"));
		garbageTruckSouth_location = Integer.parseInt(executor.getCurValue("garbageTruckSouth_location"));
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
				if (garbageCansNorth[i] == false && random.nextInt(10) == 0) // 1:10 chance of trash can becoming full
					garbageCansNorth[i] = true;
			}

			if (garbageCansSouth[i] == true && garbageTruckSouth_location == i && isCleaningS)
				garbageCansSouth[i] = false;
			else {
				if (garbageCansSouth[i] == false && random.nextInt(10) == 0) // 1:10 chance of trash can becoming full
					garbageCansSouth[i] = true;
			}
		}

		if (crossingCrosswalkNS)// || random.nextInt(1) == 0) // 1:5 chance of pedestrian on south sidewalk
			sidewalkSouth = true;
		else
			sidewalkSouth = false;

		if (sidewalkNorth == true)
			crossingCrosswalkNS = true;
		else
			crossingCrosswalkNS = false;

		if (sidewalkSouth == false && crossingCrosswalkNS == false && random.nextInt(6) == 0) // 1:10 chance of pedestrian on north sidewalk
			sidewalkNorth = true;
		else
			sidewalkNorth = false;
		
		if (countHours != 0 && countHours % 12 == 0) {
			dayTime = DayTimeMode.values()[(dayTime.ordinal() + 1) % 2];
			if (dayTime == DayTimeMode.DAY)
				energyEfficiencyMode = false;
			else {
				if (countHours == 24) {
					energyEfficiencyMode = true;
					countHours = 0;
				}
			}
			countHours = 0;
		}
		countHours++;

	}

	private HashMap<String, HashMap<String, Object>> getSpectraVarsAsDict() {
		HashMap<String, Object> current_environment_state = new HashMap<>();
		current_environment_state.put("isNight",dayTime == DayTimeMode.NIGHT);
		current_environment_state.put("sidewalkNorth", sidewalkNorth);
		current_environment_state.put("sidewalkSouth", sidewalkSouth);
		current_environment_state.put("crossingCrosswalkNS", crossingCrosswalkNS);
		// current_environment_state.put("crossingCrosswalkSN", crossingCrosswalkSN);
		current_environment_state.put("garbageCansNorth", Arrays.toString(garbageCansNorth));
		current_environment_state.put("garbageCansSouth", Arrays.toString(garbageCansSouth));

		HashMap<String, Object> current_system_state = new HashMap<>();
		current_system_state.put("isCleaningN", isCleaningN);
		current_system_state.put("isCleaningS", isCleaningS);
		current_system_state.put("lightNorth", lightNorth);
		current_system_state.put("lightSouth", lightSouth);
		current_system_state.put("garbageTruckNorth_location", garbageTruckNorth_location);
		current_system_state.put("garbageTruckSouth_location", garbageTruckSouth_location);

		HashMap<String, HashMap<String, Object>> current_full_state = new HashMap<>();
		current_full_state.put("environment", current_environment_state);
		current_full_state.put("system", current_system_state);

		return current_full_state;
	}
	
	private void updateEnvVarsFromClient(HashMap<String, Object> dataFromClient) {
		if (dataFromClient== null)
			return;
		
		for (String var : dataFromClient.keySet()) {
			switch (var) {
			case "sidewalkNorth":
				sidewalkNorth = (boolean) dataFromClient.get("sidewalkNorth");
				break;
			case "sidewalkSouth":
				sidewalkSouth = (boolean) dataFromClient.get("sidewalkSouth");
				break;
			case "crossingCrosswalkNS":
				crossingCrosswalkNS = (boolean) dataFromClient.get("crossingCrosswalkNS");
				break;
//			case "crossingCrosswalkSN":
//				crossingCrosswalkSN = (boolean) dataFromClient.get("crossingCrosswalkSN");
//				break;
			case "garbageCansNorth":
				garbageCansNorth = (boolean[]) dataFromClient.get("garbageCansNorth");
				break;
			case "garbageCansSouth":
				garbageCansSouth = (boolean[]) dataFromClient.get("garbageCansSouth");
				break;
			case "dayTime":
				dayTime = (DayTimeMode) dataFromClient.get("dayTime");
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
