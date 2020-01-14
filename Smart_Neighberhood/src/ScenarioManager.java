package src;

import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;
import src.NeighberhoodSimulator.DayTimeMode;

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

	private void addStateToQueue(int pedestrianStartPos, boolean pedestrianIsNorth, boolean[] garbageCansNorth,
			boolean[] garbageCansSouth, String dayTime, boolean energyEfficiencyMode) {

		HashMap<String, Object> state = new HashMap<String, Object>();

		if (pedestrianStartPos != 100)
			state.put("pedestrian", new Object[] { pedestrianStartPos, pedestrianIsNorth });
		
		if (garbageCansNorth != null)
			state.put("garbageCansNorth", garbageCansNorth);
		
		if (garbageCansSouth != null)
			state.put("garbageCansSouth", garbageCansSouth);
		
		if (dayTime != "")
			state.put("dayTime", dayTime);
		
		state.put("energyEfficiencyMode", energyEfficiencyMode);

		scenarioQueue.add(state);
	}

	public HashMap<String, Object> getNextState() {
		return scenarioQueue.poll();
	}

	private void loadScenario1() {
		// example
		addStateToQueue(-1, true, null, null, "NIGHT", true);
		addStateToQueue(4, false, null, null, "NIGHT", true);
		addStateToQueue(4, true, null, null, "NIGHT", true);
		addStateToQueue(-1, false, null, null, "NIGHT", true);
		
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
		addStateToQueue(100, true, null, null, "", true);
	}

	private void loadScenario2() {
		// example
		addStateToQueue(-1, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, "DAY", false);
	}

	private void loadScenario3() {
		// example
		addStateToQueue(0, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, "DAY", false);
	}

	private void loadScenario4() {
		// example
		addStateToQueue(0, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, "DAY", false);
	}

	private void loadScenario5() {
		// example
		addStateToQueue(-1, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, "DAY", false);
	}
}
