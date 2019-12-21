package src;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JComponent;

import tau.smlab.syntech.executor.ControllerExecutor;
import tau.smlab.syntech.executor.ControllerExecutorException;

public class NeighberhoodSimulator extends JComponent {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */

	ControllerExecutor executor;
	Random random = new Random();

	/***** Environment variables *****/
	int N = 4;
	boolean sidewalkNorth;
	boolean sidewalkSouth;
	boolean crossingCrosswalkNS;
	// boolean crossingCrosswalkSN;
	boolean[] garbageCansNorth = new boolean[N];
	boolean[] garbageCansSouth = new boolean[N];

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

	public HashMap<String, HashMap<String, Object>> getNextState(int scenario_num) {
		switch (scenario_num) {
		case -1:
			setEnvVarsToDefault();
			break;
		case 0: // random scenario
			randomNextState();
			break;
		case 1:
			break;
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
	
	public void setEnvVarsToDefault() {
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

		executor.updateState(true, false); // TODO: I changed it to false due to many errors it gave me, check what is
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

	public void randomNextState() {
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

		if (crossingCrosswalkNS || random.nextInt(1) == 0) // 1:5 chance of pedestrian on south sidewalk
			sidewalkSouth = true;
		else
			sidewalkSouth = false;

		if (sidewalkNorth == true)
			crossingCrosswalkNS = true;
		else
			crossingCrosswalkNS = false;

		if (random.nextInt(4) == 0) // 1:4 chance of pedestrian on north sidewalk
			sidewalkNorth = true;
		else
			sidewalkNorth = false;

	}

	private HashMap<String, HashMap<String, Object>> getSpectraVarsAsDict() {
		HashMap<String, Object> current_environment_state = new HashMap<>();
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
}
