package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.xml.internal.ws.util.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import src.ScenarioManager;

import tau.smlab.syntech.executor.ControllerExecutor;
import tau.smlab.syntech.executor.ControllerExecutorException;

public class NeighberhoodSimulator {
	/**
	 * 
	 */

	ControllerExecutor executor;
	Random random = new Random();
	ReentrantLock lock = new ReentrantLock();

	enum DayTimeMode {
		DAY, NIGHT
	};

	int N = 4;
	ScenarioManager scenario;
	int lastPedestrianId = 0;
	int isFreezeCrosswalk = 0;

	/***** Environment variables *****/
//	boolean sidewalkNorth;
//	boolean sidewalkSouth;
//	boolean crossingCrosswalkNS;
//	boolean crossingCrosswalkSN;
	boolean[] garbageCansNorth = new boolean[N];
	boolean[] garbageCansSouth = new boolean[N];
	DayTimeMode dayTime;
	boolean energyEfficiencyMode;
	List<Pedestrian> pedestrians = new ArrayList<>();

	/* dayTimeRelated */
	int countHours = 0;

	/*-------------------------------*/

	/***** System variables *****/
	boolean isCleaningN;
	boolean isCleaningS;
	boolean[] lightsNorth = new boolean[N - 1];
	boolean[] lightsSouth = new boolean[N - 1];
//	boolean lightNorth;
//	boolean lightSouth;
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
		switch (scenario_num) {
		case 0:
			setEnvVarsToDefault();
			break;
		case 1: // initiate scenario number 1
			setEnvVarsToDefault();
			scenario = new ScenarioManager(1);
			break;
		case 2: // initiate scenario number 2
			setEnvVarsToDefault();
			scenario = new ScenarioManager(2);
			break;
		case 3: // initiate scenario number 3
			setEnvVarsToDefault();
			scenario = new ScenarioManager(3);
			break;
		case 4: // initiate scenario number 4
			setEnvVarsToDefault();
			scenario = new ScenarioManager(4);
			break;
		case 5: // initiate scenario number 5
			setEnvVarsToDefault();
			scenario = new ScenarioManager(5);
			break;
		case 6: // initiate scenario number 6
			setEnvVarsToDefault();
			scenario = new ScenarioManager(6);
			break;
		case 7: // initiate scenario number 7
			setEnvVarsToDefault();
			scenario = new ScenarioManager(7);
			break;
		case 8: // initiate scenario number 8
			setEnvVarsToDefault();
			scenario = new ScenarioManager(8);
			break;
		case 9: // random mode.
			setEnvVarsToDefault();
			moveAllPedestrians();
			randomNextState();
			break;
		case 10: // semi-automatic mode.
			setEnvVarsToDefault();
			moveAllPedestrians();
			randomNextState();
			updateEnvVarsFromClient(dataFromClient);
			break;
		case 11: // manual mode.
			setEnvVarsToDefault();
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

		return getSpectraVarsAsDict();
	}

	private void setEnvVarsToDefault() {
		// Instantiate a new controller executor
		executor = new ControllerExecutor(true, false);

//		sidewalkNorth = false;
//		sidewalkSouth = false;
//		crossingCrosswalkNS = false;
//		 crossingCrosswalkSN;
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
//		executor.setInputValue("sidewalkSouth", String.valueOf(sidewalkSouth));
//		executor.setInputValue("sidewalkNorth", String.valueOf(sidewalkNorth));
//		executor.setInputValue("crossingCrosswalkNS", String.valueOf(crossingCrosswalkNS));
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
			else if (pedestrian.position != -1 && pedestrian.position != 4){
				if(pedestrian.isInTheNorth)
					executor.setInputValue(String.format("pedestrians[%d]", pedestrian.position), "true");
				else
					executor.setInputValue(String.format("pedestrians[%d]", pedestrian.position + N), "true");
			}
		}
		
		System.out.println("updating to spectra");
		executor.updateState(true, true);
		for (int i = 0; i < N*2 + 1; i++) {
			String s = String.format("pedestrians[%d]", i);
			System.out.print(s + ": " + executor.getCurValue((String.format("pedestrians[%d]", i))) + "; ");
		}
		System.out.println();
		Map<String, String> sysValues2 = executor.getCurOutputs();
		sysValues2.entrySet().forEach(entry->{
		    System.out.print(entry.getKey() + " " + entry.getValue() + "; ");  
		 });
		System.out.println();
	}

	private void updateSystemVarsFromSpectra() throws ControllerExecutorException {
		System.out.println("Getting system variables from Spectra");

		isCleaningN = Boolean.parseBoolean(executor.getCurValue("isCleaningN"));
		isCleaningS = Boolean.parseBoolean(executor.getCurValue("isCleaningS"));
//		System.out.println(executor.getCurValue("lights[0]"));
//		lightNorth = Boolean.parseBoolean(executor.getCurValue("lightNorth"));
//		lightSouth = Boolean.parseBoolean(executor.getCurValue("lightSouth"));
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

//		if (crossingCrosswalkNS)// || random.nextInt(1) == 0) // 1:5 chance of pedestrian on south sidewalk
//			sidewalkSouth = true;
//		else
//			sidewalkSouth = false;
//
//		if (sidewalkNorth == true)
//			crossingCrosswalkNS = true;
//		else
//			crossingCrosswalkNS = false;
//
//		if (sidewalkSouth == false && crossingCrosswalkNS == false && random.nextInt(6) == 0) // 1:10 chance of pedestrian on north sidewalk
//			sidewalkNorth = true;
//		else
//			sidewalkNorth = false;
		if (random.nextInt(5) == 0) {
			addRandomPedestrian();
		}

		if (countHours != 0 && countHours % 12 == 0) {
			dayTime = DayTimeMode.values()[(dayTime.ordinal() + 1) % 2];
			if (dayTime == DayTimeMode.DAY)
				energyEfficiencyMode = false;
			else {
				/*if (countHours == 24) {
					energyEfficiencyMode = true;
					countHours = 0;
				}*/
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
//		current_environment_state.put("sidewalkNorth", sidewalkNorth);
//		current_environment_state.put("sidewalkSouth", sidewalkSouth);
//		current_environment_state.put("crossingCrosswalkNS", crossingCrosswalkNS);
//		current_environment_state.put("crossingCrosswalkSN", crossingCrosswalkSN);
		current_environment_state.put("garbageCansNorth", Arrays.toString(garbageCansNorth));
		current_environment_state.put("garbageCansSouth", Arrays.toString(garbageCansSouth));
		current_environment_state.put("pedestrians", getPedestriansData());

		HashMap<String, Object> current_system_state = new HashMap<>();
		current_system_state.put("isCleaningN", isCleaningN);
		current_system_state.put("isCleaningS", isCleaningS);
		current_system_state.put("lightsNorth", Arrays.toString(lightsNorth));
		current_system_state.put("lightsSouth", Arrays.toString(lightsSouth));
//		current_system_state.put("lightNorth", lightNorth);
//		current_system_state.put("lightSouth", lightSouth);
		current_system_state.put("garbageTruckNorth_location", garbageTruckNorth_location);
		current_system_state.put("garbageTruckSouth_location", garbageTruckSouth_location);

		HashMap<String, HashMap<String, Object>> current_full_state = new HashMap<>();
		current_full_state.put("environment", current_environment_state);
		current_full_state.put("system", current_system_state);

		printPedestLights();
		
		return current_full_state;
	}

	public void updateEnvVarsFromClient(HashMap<String, Object> dataFromClient) {
		if (dataFromClient == null)
			return;

		for (String var : dataFromClient.keySet()) {
			switch (var) {
//			case "sidewalkNorth":
//				sidewalkNorth = (boolean) dataFromClient.get("sidewalkNorth");
//				break;
//			case "sidewalkSouth":
//				sidewalkSouth = (boolean) dataFromClient.get("sidewalkSouth");
//				break;
//			case "crossingCrosswalkNS":
//				crossingCrosswalkNS = (boolean) dataFromClient.get("crossingCrosswalkNS");
//				break;
//			case "crossingCrosswalkSN":
//				crossingCrosswalkSN = (boolean) dataFromClient.get("crossingCrosswalkSN");
//				break;
			case "pedestrian":
				addRandomPedestrian();
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
