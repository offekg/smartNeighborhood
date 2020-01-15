package src;

import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;

public class ScenarioManager {
	Queue<HashMap<String, Object>> scenarioQueue;

	public ScenarioManager(int scenarioNum) {
		scenarioQueue = new LinkedList<>();
		switch (scenarioNum) {
		case 1:
			loadScenario1();
			break;
		case 2:
			loadScenario2();
			break;
		case 3:
			loadScenario3();
			break;
		case 4:
			loadScenario4();
			break;
		case 5:
			loadScenario5();
			break;
		}
	}

	private void addStateToQueue(int pedestrianStartPos, boolean pedestrianIsNorth, int pedestrianCrossAlot,
			boolean[] garbageCansNorth, int singleCanNorth, boolean[] garbageCansSouth, int singleCanSouth,
			String dayTime, boolean energyEfficiencyMode) {

		HashMap<String, Object> state = new HashMap<String, Object>();

		if (pedestrianStartPos != 100)
			state.put("pedestrian", new Object[] { pedestrianStartPos, pedestrianIsNorth, pedestrianCrossAlot });

		if (garbageCansNorth != null)
			state.put("garbageCansNorth", garbageCansNorth);

		if (singleCanNorth != 100)
			state.put("garbageCansNorth", singleCanNorth);

		if (garbageCansSouth != null)
			state.put("garbageCansSouth", garbageCansSouth);

		if (singleCanSouth != 100)
			state.put("garbageCansSouth", singleCanSouth);

		if (dayTime != "")
			state.put("dayTime", dayTime);

		state.put("energyEfficiencyMode", energyEfficiencyMode);

		scenarioQueue.add(state);
	}

	public HashMap<String, Object> getNextState() {
		return scenarioQueue.poll();
	}

	private void addBlankStates(int amount, boolean energyEfficiencyMode) {
		for (; amount > 0; amount--)
			addStateToQueue(100, true, 0, null, 100, null, 100, "", energyEfficiencyMode);
	}

	private void loadScenario1() {
		addStateToQueue(-1, true, 0, null, 100, null, 100, "NIGHT", true);
		addStateToQueue(4, false, 0, null, 100, null, 100, "NIGHT", true);
		addStateToQueue(4, true, 0, null, 100, null, 100, "NIGHT", true);
		addStateToQueue(-1, false, 0, null, 100, null, 100, "NIGHT", true);

		//addBlankStates(50, true);
	}

	private void loadScenario2() {
		addStateToQueue(100, true, 0, null, 3, null, 0, "DAY", false);
		addStateToQueue(100, true, 0, null, 2, null, 1, "DAY", false);
		addStateToQueue(100, true, 0, null, 1, null, 2, "DAY", false);
		addStateToQueue(100, true, 0, null, 0, null, 3, "DAY", false);

		addBlankStates(50, false);
	}

	private void loadScenario3() {
		addStateToQueue(-1, false, 3, null, 3, null, 0, "DAY", false);
		addStateToQueue(-1, true, 3, null, 100, null, 100, "DAY", false);
		
		addBlankStates(5, false);
		
		addStateToQueue(4, false, 2, null, 100, null, 100, "NIGHT", false);
		addStateToQueue(4, true, 2, null, 2, null, 1, "NIGHT", false);
		
		addBlankStates(2, false);
		addBlankStates(50, true);
	}

	private void loadScenario4() {
		addStateToQueue(-1, false, 3, null, 3, null, 0, "DAY", false);
		addStateToQueue(-1, true, 3, null, 100, null, 100, "DAY", false);
		
		addBlankStates(5, false);
		
		addStateToQueue(4, false, 5, null, 100, null, 100, "NIGHT", false);
		addStateToQueue(4, true, 5, null, 2, null, 1, "NIGHT", false);
		
		for (int i = 0; i < 5; i++) {
			addStateToQueue(100, true, 5, null, 1, null, 2, "NIGHT", false);
			addBlankStates(3, false);
		}
	}

	private void loadScenario5() {
		for (int i = 0; i < 10; i++) {
			addStateToQueue(-1, false, 0, null, 100, null, 100, "DAY", false);
			addStateToQueue(-1, true, 0, null, 100, null, 100, "DAY", false);
		}
		
		addBlankStates(10, false);
		
		for (int i = 0; i < 10; i++) {
			addStateToQueue(-1, false, 0, null, 100, null, 100, "NIGHT", false);
			addStateToQueue(-1, true, 0, null, 100, null, 100, "NIGHT", false);
		}
		
		addBlankStates(15,  false);
	}
}
