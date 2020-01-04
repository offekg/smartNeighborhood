package src;

import java.util.HashMap;
import java.util.Random;

public class Pedestrian {
	int id;
	int position;
	boolean isInTheNorth;
	boolean isOnCrosswalk;
	boolean allowedToCross;
	boolean isAppeared;

	Random randomMoving = new Random();

	public Pedestrian(int id, int position, boolean isInTheNorth) {
		this.id = id;
		this.position = position;
		this.isInTheNorth = isInTheNorth;
		this.isOnCrosswalk = false;
		this.allowedToCross = true;
		this.isAppeared = false;
	}

	public void move(int isFreezeCrosswalk) {
		int nextPosition;
		if (isOnCrosswalk) {
			cross();
			nextPosition = randomMoving.nextInt() + 1;
		} else {
			if (!isAppeared) {
				if (position == -1)
					nextPosition = 0;
				else
					nextPosition = 3;
				isAppeared = true;
			}
			else {
				nextPosition = position - (randomMoving.nextInt(3) - 1);
				if (allowedToCross) {
					if (!(isFreezeCrosswalk == 0 || isFreezeCrosswalk == 1)
							&& (nextPosition == 1 || nextPosition == 2)) {
						int shouldCross = randomMoving.nextInt(2);
						if (shouldCross == 1) {
							isOnCrosswalk = true;
						}
					}
				}
			}
		}
		position = nextPosition;
	}

	public void cross() {
		isInTheNorth = !isInTheNorth;
		isOnCrosswalk = false;
		allowedToCross = false;
	}

	public boolean isPedestrianExists() {
		if ((this.position < 0 || this.position > 3) && isAppeared)
			return false;
		return true;
	}

	public HashMap<String, Object> getPedestrianState() {
		HashMap<String, Object> pedestrianData = new HashMap<String, Object>();
		pedestrianData.put("id", this.id);
		pedestrianData.put("position", this.position);
		pedestrianData.put("isInNorth", this.isInTheNorth);
		pedestrianData.put("isOnCrosswalk", this.isOnCrosswalk);
		return pedestrianData;
	}
}
