package src;

import java.util.Map;

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

	public NeighberhoodSimulator() {

		// Instantiate a new controller executor
		executor = new ControllerExecutor(true, true);

		//while (true) {
			try {
				executor.updateState(true, true);

				Map<String, String> sys_vars = executor.getCurOutputs();

				test = sys_vars.get("lightNorth");
				//greenSide = Boolean.parseBoolean(sys_vars.get("greenSide"));
				
			} catch (ControllerExecutorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}
	}
	
	public String get_light_north() {
		return test;
	}
}
