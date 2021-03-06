package src;

import java.util.Random;
import java.util.HashMap;

public class Pedestrian {
	int id;
	int position;
	int crossAlot;
	boolean isAppeared;
	boolean isInTheNorth;
	boolean isOnCrosswalk;
	boolean allowedToCross;

	Random randomMoving = new Random();

	public Pedestrian(int id, int position, boolean isInTheNorth) {
		this.id = id;
		this.position = position;
		this.isInTheNorth = isInTheNorth;
		this.isOnCrosswalk = false;
		this.allowedToCross = true;
		this.isAppeared = false;
		this.crossAlot = 0;
	}
	
	public Pedestrian(int id, int position, boolean isInTheNorth, int crossAlot) {
		this(id, position, isInTheNorth);
		this.crossAlot = crossAlot;
	}

	public void move(int isFreezeCrosswalk) {
		int nextPosition;
		if (isOnCrosswalk) {
			if (allowedToCross) // means that pedestrian did not crossed yet.
				nextPosition = startCross();
			else 
				nextPosition = finishCross();
		} else {
			if (!isAppeared) {
				if (position < 1)
					nextPosition = position + 1;
				else
					nextPosition = position - 1;
				
				if (nextPosition >= 1 && nextPosition <= 2)
					isAppeared = true;
			} else {
				if (allowedToCross && (position == 1 || position == 2)
						&& !(isFreezeCrosswalk == 0 || isFreezeCrosswalk == 1)) {
					int shouldCross = randomMoving.nextInt(3);
					if (crossAlot > 0)
						shouldCross = 1;
					
					if (shouldCross == 1) {
						isOnCrosswalk = true;
						nextPosition = position;
					} else
						nextPosition = position - (randomMoving.nextInt(3) - 1);
				} else
					nextPosition = position - (randomMoving.nextInt(3) - 1);
			}
		}
		
		position = nextPosition;
	}

	public int startCross() {
		isInTheNorth = !isInTheNorth;
		allowedToCross = false;
		
		return position;
	}
	
	public int finishCross() {
		isOnCrosswalk = false;
		if (crossAlot-- > 0)
			allowedToCross = true;
		
		return randomMoving.nextInt(2) + 1;
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
