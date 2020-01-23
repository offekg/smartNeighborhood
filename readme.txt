In order to run the simulation:
1. Synthesis a controller (with energy bound of at least 3)
2. Start the server by running the main function in SockerServer.java
3. Navigate to http://localhost:9876/ to see the simulation. It is recommended
   to open it in a new "incognito" window using Chrome. If port 9876 is not available,
   choose a different port and change the "port" value in line 47 of "SockerServer.java".

Using the control panel:
The simulation should run automatically, though the default running mode is
manual, so nothing will change until a button is pressed. The simulation contains
three modes:
* Manual - The control panel fully controls the environment. Changes to the
  environment variables will only occur as a result of a press on one of the
  buttons (with the exception of changes forcing the environment to hold its
  guarantees, such as the cans becoming empty after they were cleaned).
* Semi - The panel is in control of the environment, but changes to the
  environment will naturally occur without intervention.
* Automatic - The panel has no control. All the environment variables are randomly
  generated.

The simulation contains 5 predefined scenarios. These scenarios can be run by
clicking on the "SCENARIOS" menu, and choosing a specific one. During the execution
of the scenario the simulation will automatically change to the "Automatic" mode,
preventing user intervention.

We've also provided a log, automatically generated for every run, which can be accessed
by clicking on the "VIEW LOG" button. In the log appear the values of all variables
during the current run, both system and environment, and changes to the variables
are marked in red.

NOTICE: changes to the environment variables do not take effect immediately after
        the button press. After clicking a button, wait 1-2 seconds, and the effect
        will appear.
