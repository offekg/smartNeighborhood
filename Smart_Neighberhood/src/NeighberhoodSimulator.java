package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import src.ScenarioManager;

import tau.smlab.syntech.executor.ControllerExecutor;
import tau.smlab.syntech.executor.ControllerExecutorException;

public class NeighberhoodSimulator {
	/**
	 * 
	 */

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
	private boolean[] garbageCansSouth = new boolean[N];
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
			// TODO: handle it gracefully.
		}

	}
	
	private void printPedestLights() {
		if(dayTime == DayTimeMode.DAY)
			return;
		String spaces;
		System.out.println();
		System.out.print("	");
		for(int i = 0; i < N-1; i++) {
			if (lightsNorth[i]) 
				System.out.print(String.format("LN%d+		",i));
			else
				System.out.print(String.format("LN%d-		",i));
		}
		System.out.println();
		for(Pedestrian p : pedestrians) {
			if(p.isInTheNorth && !p.isOnCrosswalk ) {
				
				if(p.position == -1)
					spaces = "";
				else
					spaces = String.join("",Collections.nCopies((p.position)*4, "    "));
				System.out.println(spaces + "P" + p.position);
			}
		}
		System.out.println();
		for(Pedestrian p3 : pedestrians) {
			if(p3.isOnCrosswalk)
				System.out.println("				P in Crosswalk");
		System.out.println();
		}
		System.out.print("	");
		for(int i = 0; i < N-1; i++) {
			if (lightsSouth[i]) 
				System.out.print(String.format("LS%d+		",i));
			else
				System.out.print(String.format("LS%d-		",i));
		}
		System.out.println();
		for(Pedestrian p2 : pedestrians) {
			if(!p2.isInTheNorth && !p2.isOnCrosswalk && p2.position != -1) {
				if(p2.position == -1)
					spaces = "";
				else
					spaces = String.join("",Collections.nCopies((p2.position)*4, "    "));
				System.out.println(spaces + "P" + p2.position);
			}
		}
		System.out.println();
	}

	public HashMap<String, HashMap<String, Object>> getNextState(int scenario_num,
			HashMap<String, Object> dataFromClient) {
		if (!canScenarioStart) {
			dayTime = DayTimeMode.DAY;
			energyEfficiencyMode = false;
			scenario_num = 11;
			dataFromClient = null;
		}
		
		switch (scenario_num) {
		case 0:
			return null;
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
		case 6: // initiate scenario number 6
			scenario = new ScenarioManager(6);
			break;
		case 7: // initiate scenario number 7
			scenario = new ScenarioManager(7);
			break;
		case 8: // initiate scenario number 8
			scenario = new ScenarioManager(8);
			break;
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
		case 100: // get scenario next state or random when scenario done
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
			System.out.println("Update to spectra failed due to an unknown reason");
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

		sim_itter = 0;
		canScenarioStart = true;
	}

	private void setEnvVarsToDefault() {
		for (int i = 0; i < N; i++) {
			garbageCansNorth[i] = false;
			garbageCansSouth[i] = false;
		}
		
		dayTime = DayTimeMode.DAY;
		energyEfficiencyMode = false;
		pedestrians = new ArrayList<>();

		sim_itter = 0;
	}

	private void updateEnvVarsInSpectra() throws ControllerExecutorException {
		System.out.println("Updating environment variables in Spectra");

		for (int i = 0; i < N; i++) {
			executor.setInputValue(String.format("garbageCansNorth[%d]", i), String.valueOf(garbageCansNorth[i]));
			executor.setInputValue(String.format("garbageCansSouth[%d]", i), String.valueOf(garbageCansSouth[i]));
			if (garbageCansNorth[i] == true && garbageTruckNorth_location == i && isCleaningN)
				garbageCansNorth[i] = false;
			if (garbageCansSouth[i] == true && garbageTruckSouth_location == i && isCleaningS)
				garbageCansSouth[i] = false;
		}

		executor.setInputValue("dayTime", String.valueOf(dayTime));
		executor.setInputValue("energyEfficiencyMode", String.valueOf(energyEfficiencyMode));
		
		//first set all spectra pedestrian spaces to false, and then update from all pedestrians
		for (int i = 0; i < N*2 + 1; i++) {
			executor.setInputValue(String.format("pedestrians[%d]", i), "false");
		}
		for (Pedestrian pedestrian : pedestrians) {
			if (pedestrian.isOnCrosswalk) {
				executor.setInputValue(String.format("pedestrians[%d]", N*2), "true");
			}
			else if (pedestrian.position >= 0 && pedestrian.position <= 3){
				if(pedestrian.isInTheNorth)
					executor.setInputValue(String.format("pedestrians[%d]", pedestrian.position), "true");
				else
					executor.setInputValue(String.format("pedestrians[%d]", pedestrian.position + N), "true");
			}
		}
		
		System.out.println(Arrays.toString(garbageCansNorth));
		System.out.println(Arrays.toString(garbageCansSouth));
		System.out.println(Arrays.toString(pedestrians.toArray()));
		System.out.println(garbageTruckNorth_location);
		System.out.println(garbageTruckSouth_location);
		System.out.println(isCleaningN);
		System.out.println(isCleaningS);

		
		executor.updateState(true, true);
	}

	private void updateSystemVarsFromSpectra() throws ControllerExecutorException {
		System.out.println("Getting system variables from Spectra");

		isCleaningN = Boolean.parseBoolean(executor.getCurValue("isCleaningN"));
		isCleaningS = Boolean.parseBoolean(executor.getCurValue("isCleaningS"));
		
		for (int i = 0; i < N - 1; i++) {
			lightsNorth[i] = Boolean.parseBoolean(executor.getCurValue(String.format("lights[%d]",i)));
			lightsSouth[i] = Boolean.parseBoolean(executor.getCurValue(String.format("lights[%d]",i + N-1)));
		}
		
		garbageTruckNorth_location = Integer.parseInt(executor.getCurValue("garbageTruckNorth_location"));
		garbageTruckSouth_location = Integer.parseInt(executor.getCurValue("garbageTruckSouth_location"));
	}
	
	private void addRandomPedestrian() {
		int startPosition = random.nextInt(2);
		if (startPosition == 0)
			addNewPedestrian(-1, random.nextBoolean());
		else
			addNewPedestrian(4, random.nextBoolean());
	}
	
	private void addRandomPedestrianFromClient() {
		int startPosition = random.nextInt(2);
		if (startPosition == 0)
			addNewPedestrian(-2, random.nextBoolean());
		else
			addNewPedestrian(5, random.nextBoolean());
	}

	private void addNewPedestrian(int position, boolean isInNorth) {
		Pedestrian p = new Pedestrian(++lastPedestrianId, position, isInNorth);
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
				addRandomPedestrianFromClient();
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
				dayTime = (DayTimeMode.valueOf((String) dataFromClient.get("dayTime"))) ;
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
