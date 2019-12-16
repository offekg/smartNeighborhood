package src;

import java.util.Random;

import javax.swing.JComponent;

import tau.smlab.syntech.executor.ControllerExecutor;
import tau.smlab.syntech.executor.ControllerExecutorException;

public class NeighberhoodSimulator extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ControllerExecutor executor;

	/***** Environment variables *****/
	int N = 4;
	boolean sidewalkNorth = false;
	boolean sidewalkSouth = false;
	boolean crossingCrosswalkNS = fasle;
	// boolean crossingCrosswalkSN;
	boolean[] garbageCansNorth = new boolean[N];
	for (int i = 0; i < N; i++)
		garbageCansNorth[i] = false;
	
	// boolean[N] garbageCansSouth;
	/*-------------------------------*/

	
	/***** System variables *****/
	boolean isCleaningN;
	// boolean isCleaningS;
	boolean lightNorth;
	boolean lightSouth;
	int garbageTruckNorth_location; // location N means not on the street.
	// int garbageTruckSouth_location;
	/*-------------------------------*/

	int sim_itter = 0;

	/*ReentrantLock output_queue_lock = new ReentrantLock();
	Queue<HashMap<String, HashMap<String, Object>>> output_next_states = new LinkedList<>();
	HashMap<String, HashMap<String, Object>> last_state_sent = new HashMap<>();
	
	ReentrantLock input_queue_lock = new ReentrantLock();
	Queue<HashMap<String, Object>> input_next_states = new LinkedList<>();*/

	public NeighberhoodSimulator() {

		// Instantiate a new controller executor
		executor = new ControllerExecutor(true, true);
		Random random = new Random();
		
		for (int i = 0; i < N; i++)
			executor.setInputValue(String.format("garbageCansNorth[%d]", i),
					String.valueOf(garbageCansNorth[i]));
		executor.setInputValue("sidewalkSouth", String.valueOf(sidewalkSouth));
		executor.setInputValue("sidewalkNorth", String.valueOf(sidewalkNorth));
		executor.setInputValue("crossingCrosswalkNS", String.valueOf(crossingCrosswalkNS));
		
		executor.updateState(true, true);
		
		isCleaningN = Boolean.parseBoolean(executor.getCurValue("isCleaning"));
		lightNorth = Boolean.parseBoolean(executor.getCurValue("lightNorth"));
		lightSouth = Boolean.parseBoolean(executor.getCurValue("lightSouth"));
		garbageTruckNorth_location = Integer.parseInt(executor.getCurValue("garbageTruckNorth_location"));
		
	}
	
	public HashMap<String, HashMap<String, Object>> get_next_state(int scenario_num){
		isCleaningN = Boolean.parseBoolean(executor.getCurValue("isCleaning"));
		lightNorth = Boolean.parseBoolean(executor.getCurValue("lightNorth"));
		lightSouth = Boolean.parseBoolean(executor.getCurValue("lightSouth"));
		garbageTruckNorth_location = Integer.parseInt(executor.getCurValue("garbageTruckNorth_location"));
		
		switch (scenario_num) {
		case 0:  //random scenario
			random_next_state();
			break;
		case 1:
			break;
		}
		
		//update 
		for (int i = 0; i < N; i++)
			executor.setInputValue(String.format("garbageCansNorth[%d]", i),
					String.valueOf(garbageCansNorth[i]));
		executor.setInputValue("sidewalkSouth", String.valueOf(sidewalkSouth));
		executor.setInputValue("sidewalkNorth", String.valueOf(sidewalkNorth));
		executor.setInputValue("crossingCrosswalkNS", String.valueOf(crossingCrosswalkNS));

		executor.updateState(true, true);
		
		isCleaningN = Boolean.parseBoolean(executor.getCurValue("isCleaning"));
		lightNorth = Boolean.parseBoolean(executor.getCurValue("lightNorth"));
		lightSouth = Boolean.parseBoolean(executor.getCurValue("lightSouth"));
		garbageTruckNorth_location = Integer.parseInt(executor.getCurValue("garbageTruckNorth_location"));
		
		return add_current_state_to_outputs_states_queue();
	}
	
	public void random_next_state() {
		for (int i = 0; i < N; i++) {
			if (garbageCansNorth[i] == true && garbageCansNorth[i] && isCleaningN)
				garbageCansNorth[i] = false;
			else {
				if (random.nextInt(5) == 0) // 1:5 chance of trash can becoming full
					garbageCansNorth[i] = true;
				else
					garbageCansNorth[i] = false;
			}
		}

		if (crossingCrosswalkNS || random.nextInt(5) == 0) // 1:5 chance of pedestrian on south sidewalk
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

	private void add_current_state_to_outputs_states_queue() {
		HashMap<String, Object> current_environment_state = new HashMap<>();
		current_environment_state.put("sidewalkNorth", sidewalkNorth);
		current_environment_state.put("sidewalkSouth", sidewalkSouth);
		current_environment_state.put("crossingCrosswalkNS", crossingCrosswalkNS);
		// current_environment_state.put("crossingCrosswalkSN", crossingCrosswalkSN);
		current_environment_state.put("garbageCansNorth", garbageCansNorth);
		current_environment_state.put("garbageCansSouth", sidewalkNorth);

		HashMap<String, Object> current_system_state = new HashMap<>();
		current_system_state.put("isCleaningN", isCleaningN);
		// current_system_state.put("isCleaningS", isCleaningS);
		current_system_state.put("lightNorth", lightNorth);
		// current_system_state.put("lightSouth", lightSouth);
		current_system_state.put("garbageTruckNorth_location", garbageTruckNorth_location);
		// current_system_state.put("garbageTruckSouth_location", garbageTruckSouth_location);

		HashMap<String, HashMap<String, Object>> current_full_state = new HashMap<>();
		current_full_state.put("environment", current_environment_state);
		current_full_state.put("system", current_system_state);

		
		return current_full_state;
	}
	/*
	public HashMap<String, HashMap<String, Object>> get_output_next_state() {
		output_queue_lock.lock();
		
		if (output_next_states.size() > 0)
			last_state_sent = output_next_states.remove();
		
		output_queue_lock.unlock();
		
		return last_state_sent;
	}
	
	public void add_input_state_to_queue(HashMap<String, Object> state) {
		input_queue_lock.lock();
		
		input_next_states.add(state);
		
		input_queue_lock.unlock();
	}
	
	public void set_environment_next_state() {
		HashMap<String, Object> next_state = null;
		
		input_queue_lock.lock();
		if (input_next_states.size() > 0)
			next_state = input_next_states.remove();
		input_queue_lock.unlock();
		
		if (next_state == null)
			return;
		
		/*
		 * TODO: Need to do the updates here
		 */
	//}
}
