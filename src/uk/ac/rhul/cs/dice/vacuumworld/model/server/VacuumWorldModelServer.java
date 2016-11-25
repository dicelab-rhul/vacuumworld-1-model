package uk.ac.rhul.cs.dice.vacuumworld.model.server;

import java.io.FileInputStream;
import java.net.ServerSocket;
import java.util.EnumMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.json.JsonObject;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.SpaceCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldClientListener;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldUniverse;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading.VacuumWorldMonitoringActorRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldActorRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldAgentThreadManager;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.utils.parser.InitialStateParser;
import uk.ac.rhul.cs.dice.vacuumworld.utils.parser.StateRepresentationBuilder;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.StopSignal;

public class VacuumWorldModelServer implements Observer {
    private ServerSocket serverSocket;
    private Set<Client> connectedClients;
    private Map<String, VacuumWorldClientListener> clientsListeners;
    private Map<String, VacuumWorldUniverse> activeUniverses;
    private VacuumWorldAgentThreadManager manager;
    private StopSignal sharedStopSignal;
    private VacuumWorldUniverse universe;
    private int maximumNumberOfCycles;
    
    public VacuumWorldModelServer(int maximumNumberOfCycles) {
	this.sharedStopSignal = new StopSignal();
	this.maximumNumberOfCycles = maximumNumberOfCycles == -1 ? 101 : maximumNumberOfCycles;
    }
    
    public void startServer(String initialStateFilePath, double delayInSeconds) {
	VWUtils.logWithClass(getClass().getSimpleName(), "Starting server...");
	
	try (FileInputStream input = new FileInputStream(initialStateFilePath)) {
	    VacuumWorldSpace initialState = InitialStateParser.parseInitialState(input);
	    VWUtils.logWithClass(InitialStateParser.class.getSimpleName(), "Successfully created initial state.");
	    startSystem(initialState, delayInSeconds);
	}
	catch(InterruptedException e) {
	    VWUtils.logWithClass(getClass().getSimpleName(), "Received interrupt...");
	    stopSimulation();
	    Thread.currentThread().interrupt();
	}
	catch(Exception e) {
	    VWUtils.log(e);
	    VWUtils.logWithClass(getClass().getSimpleName(), "Stopping server due to an error...");
	    stopSimulation();
	}
    }
    
    private void stopSimulation() {
	this.sharedStopSignal.stop();
	
	if(this.manager != null) {
	    waitForActorsThreadsManagerTerminationIfNecessary();
	}
    }

    private void waitForActorsThreadsManagerTerminationIfNecessary() {
	VWUtils.logWithClass(getClass().getSimpleName(), "Attempt to shutdown actors threads in a clean way...");

	if (this.manager == null) {
	    VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager does not exists: no actors threads to terminate.");
	}
	if (this.manager.isTerminated()) {
	    VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager was already terminated: no actors threads to terminate.");
	}

	terminateActorsThreads();
	waitForActorsThreadsManagerTermination();
    }

    private void terminateActorsThreads() {
	try {
	    this.manager.shutdownExecutors();
	}
	catch (InterruptedException e) {
	    VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager is still pending. A forcefully JVM termination will be needed.");
	    Thread.currentThread().interrupt();
	}
    }

    private void waitForActorsThreadsManagerTermination() {
	long time = System.currentTimeMillis();

	while (true) {
	    if (System.currentTimeMillis() - time > 10000) {
		VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager is still pending. A forcefully JVM termination will be needed.");

		return;
	    }

	    if (this.manager.isTerminated()) {
		VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager correctly terminated: assuming all the actors threads have been terminated in a clean way.");

		return;
	    }
	}
    }

    private void startSystem(VacuumWorldSpace initialState, double delayInSeconds) throws InterruptedException {
	this.manager = new VacuumWorldAgentThreadManager(this.sharedStopSignal);
	
	constructUniverseAndStart(initialState, delayInSeconds);
    }

    private void constructUniverseAndStart(VacuumWorldSpace initialState, double delayInSeconds) throws InterruptedException {
	VacuumWorldPhysics physics = new VacuumWorldPhysics();
	int[] dimensions = initialState.getDimensions();
	Map<SpaceCoordinates, Double[]> dimensionsMap = createDimensionsMap(dimensions);
	VacuumWorldAppearance appearance = new VacuumWorldAppearance("VacuumWorld", dimensionsMap, initialState);
	JsonObject initialStateRepresentation = StateRepresentationBuilder.buildCompactStateRepresentation(initialState.getFullGrid(), 0);
	VacuumWorldMonitoringContainer monitoringContainer = InitialStateParser.createMonitoringContainer(initialStateRepresentation);
	VacuumWorldMonitoringPhysics monitoringPhysics = new VacuumWorldMonitoringPhysics();

	this.universe = new VacuumWorldUniverse(initialState, physics, monitoringContainer, monitoringPhysics, appearance);

	startSimulation(delayInSeconds);
    }

    private void startSimulation(double delayInSeconds) throws InterruptedException {
	setupAgents();
	setupUser();
	setupMonitoringAgents();

	this.manager.addObserver(this);
	this.manager.start(delayInSeconds);
    }
    
    private void setupMonitoringAgents() {
	this.universe.getMonitoringContainer().getMonitoringAgentsMap().values().forEach(this::setupVacuumWorldMonitoringAgent);
    }

    private void setupVacuumWorldMonitoringAgent(VacuumWorldMonitoringAgent agent) {
	VacuumWorldMonitoringContainer monitoringSpace = this.universe.getMonitoringContainer();

	agent.getSeeingSensors().forEach(monitoringSpace::addObserver);
	agent.getListeningSensors().forEach(monitoringSpace::addObserver);
	agent.getDatabaseSensors().forEach(monitoringSpace::addObserver);

	agent.getPhysicalActuators().forEach(actuator -> actuator.addObserver(monitoringSpace));
	agent.getSpeakingActuators().forEach(actuator -> actuator.addObserver(monitoringSpace));
	agent.getDatabaseActuators().forEach(actuator -> actuator.addObserver(monitoringSpace));

	agent.getMind().loadAvailableActionsForThisMindFromArbitraryParameters();

	this.manager.addMonitoringAgent(new VacuumWorldMonitoringActorRunnable(agent.getMind()));
    }

    private void setupUser() {
	User user = this.universe.getState().getUser();

	if (user != null) {
	    setupUser(user);
	}
    }

    private void setupUser(User user) {
	VacuumWorldSpace space = this.universe.getState();

	user.getSeeingSensors().forEach(space::addObserver);
	user.getListeningSensors().forEach(space::addObserver);
	user.getPhysicalActuators().forEach(actuator -> actuator.addObserver(space));
	user.getSpeakingActuators().forEach(actuator -> actuator.addObserver(space));

	user.getMind().loadAvailableActionsForThisMindFromArbitraryParameters();

	this.manager.addActor(new VacuumWorldActorRunnable(user.getMind()));
    }

    private void setupAgents() {
	this.universe.getState().getAgents().forEach(this::setUpVacuumWorldCleaningAgent);
    }

    private void setUpVacuumWorldCleaningAgent(VacuumWorldCleaningAgent agent) {
	VacuumWorldSpace space = this.universe.getState();

	agent.getSeeingSensors().forEach(space::addObserver);
	agent.getListeningSensors().forEach(space::addObserver);
	agent.getPhysicalActuators().forEach(actuator -> actuator.addObserver(space));
	agent.getSpeakingActuators().forEach(actuator -> actuator.addObserver(space));

	agent.getMind().setCanSeeBehind(agent.canSeeBehind());
	agent.getMind().setPerceptionRange(agent.getPerceptionRange());
	agent.getMind().loadAvailableActionsForThisMindFromArbitraryParameters();

	this.manager.addActor(new VacuumWorldActorRunnable(agent.getMind()));
    }

    private Map<SpaceCoordinates, Double[]> createDimensionsMap(int[] dimensions) {
	Map<SpaceCoordinates, Double[]> dimensionsMap = new EnumMap<>(SpaceCoordinates.class);

	dimensionsMap.put(SpaceCoordinates.NORTH, new Double[] { Double.valueOf(0), null });
	dimensionsMap.put(SpaceCoordinates.SOUTH, new Double[] { Double.valueOf(dimensions[1]), null });
	dimensionsMap.put(SpaceCoordinates.WEST, new Double[] { Double.valueOf(0), null });
	dimensionsMap.put(SpaceCoordinates.EAST, new Double[] { Double.valueOf(dimensions[0]), null });

	return dimensionsMap;
    }

    public ServerSocket getServerSocket() {
        return this.serverSocket;
    }
    
    public Set<Client> getConnectedClients() {
        return this.connectedClients;
    }
    
    public Map<String, VacuumWorldClientListener> getClientsListeners() {
        return this.clientsListeners;
    }
    
    public Map<String, VacuumWorldUniverse> getActiveUniverses() {
        return this.activeUniverses;
    }

    @Override
    public void update(Observable o, Object arg) {
	if(o instanceof VacuumWorldAgentThreadManager) {
	    VWUtils.increaseCycleNumber();
	    
	    if (ConfigData.getPrintGridFlag()) {
		printState();
	    }
	    
	    if(VWUtils.getCycleNumber() == this.maximumNumberOfCycles) {
		stopSimulation();
	    }
	}
    }
    
    public void printState() {
	this.universe.getAppearance().updateRepresentation(this.universe.getState());
	VWUtils.logState(this.universe.getAppearance().represent());
    }
}