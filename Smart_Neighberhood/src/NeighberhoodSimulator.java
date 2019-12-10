package src;

import java.util.Map;
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
	static String test;
	
	//Environment variables
	int N = 4;
	boolean sidewalkNorth;
	boolean sidewalkSouth;
	boolean crossingCrosswalkNS;
	//boolean crossingCrosswalkSN;
	boolean[] garbageCansNorth = new boolean[N];
	//boolean[N] garbageCansSouth;

	//System variables
	boolean isCleaningN;
	//boolean isCleaningS;
	boolean lightNorth;
	boolean lightSouth;
	int garbageTruckNorth_location; //location N means not on the street.
	//int garbageTruckSouth_location;
	
	int sim_itter = 0;

	public NeighberhoodSimulator() {

		// Instantiate a new controller executor
		executor = new ControllerExecutor(true, true);
		Random random = new Random();
		
		while (true) {
			
			try {
				//executor.updateState(true, true);
				isCleaningN = Boolean.parseBoolean(executor.getCurValue("isCleaning"));
				lightNorth = Boolean.parseBoolean(executor.getCurValue("lightNorth"));
				lightSouth = Boolean.parseBoolean(executor.getCurValue("lightSouth"));
				garbageTruckNorth_location = Integer.parseInt(executor.getCurValue("garbageTruckNorth_location"));
				
				if(sim_itter == 0) {
					for(int i = 0; i < N; i++)
						garbageCansNorth[i] = false;
					sidewalkNorth = false;
					sidewalkSouth = false;
					crossingCrosswalkNS = false;
					
				}
				else {
					for(int i = 0; i < N; i++) {
						if(garbageCansNorth[i] == true && garbageCansNorth[i] && isCleaningN)
							garbageCansNorth[i] = false;
						else {
							if(random.nextInt(5) == 0) //1:5 chance of trash can becoming full
								garbageCansNorth[i] = true; 
							else
								garbageCansNorth[i] = false;	
						}
					}
					
					
					if(crossingCrosswalkNS || random.nextInt(5) == 0) //1:5 chance of pedestrian on south sidewalk
						sidewalkSouth = true;
					else
						sidewalkSouth = false;
					
					if(sidewalkNorth == true)
						crossingCrosswalkNS = true;
					else
						crossingCrosswalkNS = false;
					
					if(random.nextInt(4) == 0) //1:4 chance of pedestrian on north sidewalk
						sidewalkNorth = true;
					else
						sidewalkNorth = false;
							
				}
				for(int i = 0; i < N; i++) 
					executor.setInputValue(String.format("garbageCansNorth[%d]", i), String.valueOf(garbageCansNorth[i]));
				executor.setInputValue("sidewalkSouth", String.valueOf(sidewalkSouth));
				executor.setInputValue("sidewalkNorth", String.valueOf(sidewalkNorth));
				executor.setInputValue("crossingCrosswalkNS", String.valueOf(crossingCrosswalkNS));
				
				executor.updateState(true, true);

				
			} catch (ControllerExecutorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sim_itter += 1;
			}
			
		//}
	}
	
	public String get_light_north() {
		return test;
	}
}
