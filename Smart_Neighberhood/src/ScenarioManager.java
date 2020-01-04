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
		case 6:
			loadScenario6();
			break;
		case 7:
			loadScenario7();
			break;
		case 8:
			loadScenario8();
			break;
		}
	}

	private void addStateToQueue(int pedestrianStartPos, boolean pedestrianIsNorth, boolean[] garbageCansNorth,
			boolean[] garbageCansSouth, DayTimeMode dayTime, boolean energyEfficiencyMode) {

		HashMap<String, Object> state = new HashMap<String, Object>();

		if (pedestrianStartPos != -1)
			state.put("pedestrian", new Object[] { pedestrianStartPos, pedestrianIsNorth });
		state.put("garbageCansNorth", garbageCansNorth);
		state.put("garbageCansSouth", garbageCansSouth);
		state.put("dayTime", dayTime);
		state.put("energyEfficiencyMode", energyEfficiencyMode);

		scenarioQueue.add(state);
	}

	public HashMap<String, Object> getNextState() {
		return scenarioQueue.poll();
	}

	private void loadScenario1() {
		// example
		addStateToQueue(0, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, DayTimeMode.DAY, false);
	}

	private void loadScenario2() {
		// example
		addStateToQueue(-1, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, DayTimeMode.DAY, false);
	}

	private void loadScenario3() {
		// example
		addStateToQueue(0, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, DayTimeMode.DAY, false);
	}

	private void loadScenario4() {
		// example
		addStateToQueue(0, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, DayTimeMode.DAY, false);
	}

	private void loadScenario5() {
		// example
		addStateToQueue(-1, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, DayTimeMode.DAY, false);
	}

	private void loadScenario6() {
		// example
		addStateToQueue(-1, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, DayTimeMode.DAY, false);
	}

	private void loadScenario7() {
		// example
		addStateToQueue(0, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, DayTimeMode.DAY, false);
	}

	private void loadScenario8() {
		// example
		addStateToQueue(0, true, new boolean[] { false, false, false, false },
				new boolean[] { false, false, false, false }, DayTimeMode.DAY, false);
	}
}
