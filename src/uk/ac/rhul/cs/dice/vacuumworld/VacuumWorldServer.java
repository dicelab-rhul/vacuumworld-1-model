package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.SpaceCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserActuator;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldUniverse;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.agents.VacuumWorldMonitoringAgentActuator;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.physics.VacuumWorldMonitoringPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitoring.threading.VacuumWorldMonitoringActorRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldActorRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldAgentThreadManager;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.HandshakeCodes;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.HandshakeException;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ModelMessagesEnum;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ModelUpdate;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.StopSignal;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequestsEnum;

public class VacuumWorldServer implements Observer {
	private ServerSocket server;
	private Socket clientSocket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private VacuumWorldUniverse universe;
	private VacuumWorldAgentThreadManager threadManager;
	private ExecutorService executor;
	
	private Semaphore listeningThreadSemaphore;
	private volatile StopSignal sharedStopSignal;

	public VacuumWorldServer() throws IOException {		
		this.server = new ServerSocket(ConfigData.getModelPort());		
		this.sharedStopSignal = new StopSignal();
	}

	public void startServer(double delayInSeconds) {
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Starting server...");
		this.threadManager = new VacuumWorldAgentThreadManager(this.sharedStopSignal);
		
		try {
			doHandshake();
			manageRequests(delayInSeconds);
		}
		catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		catch (Exception e) {
			manageExceptionInStartup(e);
		}
	}
	
	public boolean isStopSignalOn() {
		return this.sharedStopSignal.mustStop();
	}

	private void manageExceptionInStartup(Exception e) {
		if(VWUtils.INVALID_INITIAL_STATE.equals(e.getMessage())) {
			sendErrorToView();
		}
		else {
			VWUtils.log(e);
			stopServer();
		}
	}

	private void sendErrorToView() {
		try {
			ModelUpdate update = new ModelUpdate(ModelMessagesEnum.BAD_INITIAL_STATE, null);
			this.output.writeObject(update);
			this.output.flush();
		}
		catch(IOException e) {
			VWUtils.log(e);
			stopSystem(ModelMessagesEnum.STOP_FORWARD);
		}
	}

	private void doHandshake() throws IOException, ClassNotFoundException, HandshakeException {
		Socket candidate = this.server.accept();
		ObjectOutputStream o = new ObjectOutputStream(candidate.getOutputStream());
		ObjectInputStream i = new ObjectInputStream(candidate.getInputStream());

		HandshakeCodes codeFromController = HandshakeCodes.fromString((String) i.readObject());
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Received " + (codeFromController == null ? null : codeFromController.toString()) + " from controller.");  //CHCM
		
		if(Handshake.attemptHanshakeWithController(o, i, codeFromController)) {
			this.clientSocket = candidate;
			this.output = o;
			this.input = i;
		}
	}

	private void manageRequests(double delayInSeconds) throws IOException, ClassNotFoundException, InterruptedException {
		VacuumWorldSpace initialState = InitialStateParser.parseInitialState(this.input);
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Parser suceeded in parsing the initial state.\n");
		constructUniverseAndStart(initialState, delayInSeconds);
	}

	private void constructUniverseAndStart(VacuumWorldSpace initialState, double delayInSeconds) throws InterruptedException {
		VacuumWorldPhysics physics = new VacuumWorldPhysics();
		int[] dimensions = initialState.getDimensions();
		Map<SpaceCoordinates, Double[]> dimensionsMap = createDimensionsMap(dimensions);
		VacuumWorldAppearance appearance = new VacuumWorldAppearance("VacuumWorld", dimensionsMap, initialState);
		VacuumWorldMonitoringContainer monitoringContainer = InitialStateParser.createMonitoringContainer();
		VacuumWorldMonitoringPhysics monitoringPhysics = new VacuumWorldMonitoringPhysics();
		
		this.universe = new VacuumWorldUniverse(initialState, physics, monitoringContainer, monitoringPhysics, appearance);

		startSimulation(delayInSeconds);
	}

	private void startSimulation(double delayInSeconds) throws InterruptedException {
		setupAgents();
		setupUser();
		setupMonitoringAgents();
		startListeningService();
		
		// START!
		this.threadManager.addObserver(this);
		this.threadManager.start(delayInSeconds);
	}

	private void setupMonitoringAgents() {
		for(VacuumWorldMonitoringAgent agent : this.universe.getMonitoringContainer().getMonitoringAgentsMap().values()) {
			setUpVacuumWorldMonitoringAgent(agent);
		}
	}

	private void setUpVacuumWorldMonitoringAgent(VacuumWorldMonitoringAgent agent) {
		VacuumWorldSpace space = this.universe.getState();
		
		agent.getSeeingSensors().forEach(space::addObserver);
		agent.getListeningSensors().forEach(space::addObserver);
		agent.getPhysicalActuators().forEach((VacuumWorldMonitoringAgentActuator actuator) -> actuator.addObserver(space));
		agent.getSpeakingActuators().forEach((VacuumWorldMonitoringAgentActuator actuator) -> actuator.addObserver(space));
		
		agent.getMind().loadAvailableActionsForThisMindFromArbitraryParameters();

		this.threadManager.addMonitoringAgent(new VacuumWorldMonitoringActorRunnable(agent.getMind()));
	}

	private void setupUser() {
		User user = this.universe.getState().getUser();
		
		if(user != null) {
			setupUser(user);
		}
	}

	private void setupUser(User user) {
		VacuumWorldSpace space = this.universe.getState();
		
		user.getSeeingSensors().forEach(space::addObserver);
		user.getListeningSensors().forEach(space::addObserver);
		user.getPhysicalActuators().forEach((UserActuator actuator) -> actuator.addObserver(space));
		user.getSpeakingActuators().forEach((UserActuator actuator) -> actuator.addObserver(space));
		
		user.getMind().loadAvailableActionsForThisMindFromArbitraryParameters();

		this.threadManager.addActor(new VacuumWorldActorRunnable(user.getMind()));
	}

	private void setupAgents() {
		Set<VacuumWorldCleaningAgent> agents = this.universe.getState().getAgents();
		
		for(VacuumWorldCleaningAgent agent : agents) {
			setUpVacuumWorldCleaningAgent(agent);
		}
	}
	
	private void setUpVacuumWorldCleaningAgent(VacuumWorldCleaningAgent agent) {
		VacuumWorldSpace space = this.universe.getState();
		
		agent.getSeeingSensors().forEach(space::addObserver);
		agent.getListeningSensors().forEach(space::addObserver);
		agent.getPhysicalActuators().forEach((VacuumWorldDefaultActuator actuator) -> actuator.addObserver(space));
		agent.getSpeakingActuators().forEach((VacuumWorldDefaultActuator actuator) -> actuator.addObserver(space));
		
		agent.getMind().setCanSeeBehind(agent.canSeeBehind());
		agent.getMind().setPerceptionRange(agent.getPerceptionRange());
		agent.getMind().loadAvailableActionsForThisMindFromArbitraryParameters();

		this.threadManager.addActor(new VacuumWorldActorRunnable(agent.getMind()));
	}

	private void startListeningService() {
		this.listeningThreadSemaphore = new Semaphore(0);
		
		VacuumWorldClientListener listener = new VacuumWorldClientListener(this.input, this.output, this.listeningThreadSemaphore, this.sharedStopSignal);
		this.threadManager.setClientListener(listener, this.universe.getState());
		
		this.executor = Executors.newSingleThreadExecutor();
		this.executor.execute(this.threadManager.getClientListener());
	}

	/**
	 * Called from notify in the {@link VacuumWorldThreadManager} showing that a
	 * cycle has ended and that the view should be updated.
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		VWUtils.increaseCycleNumber();
		
		if (ConfigData.getPrintGridFlag()) {
			printState();
		}
		
		manageViewRequest();
	}

	private void manageViewRequest() {
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Waiting for view request.");
		this.listeningThreadSemaphore.release();
		
		ViewRequestsEnum code;
		
		do {
			code = this.threadManager.getClientListener().getRequestCode();
		}
		while(code == null);
		
		manageViewRequest(code);
	}

	private void manageViewRequest(ViewRequestsEnum code) {
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Got " + code + " from view through controller.");
		
		switch(code) {
		case GET_STATE:
			sendUpdate();
			break;
		case STOP_FORWARD:
			stopSystem(ModelMessagesEnum.STOP_CONTROLLER);
			break;
		default:
			break;	
		}
	}

	private void sendUpdate() {
		try {
			ModelUpdate update = JsonForControllerBuilder.createModelUpdate(this.threadManager.getState());
			this.threadManager.getClientListener().getOutputStream().writeObject(update);
			this.threadManager.getClientListener().getOutputStream().flush();
		}
		catch(IOException e) {
			VWUtils.log(e);
		}
	}

	private void stopSystem(ModelMessagesEnum code) {
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Stopping the system and forwarding the stop request to the controller for him to shutdown...");
		
		try {
			ModelUpdate update = new ModelUpdate(code, null);
			this.threadManager.getClientListener().getOutputStream().writeObject(update);
			this.threadManager.getClientListener().getOutputStream().flush();
			
			this.server.close();
		}
		catch(Exception e) {
			VWUtils.log(e);
		}
		finally {			
			this.sharedStopSignal.stop();
			killThreads();
		}
	}

	private void killThreads() {
		try {
			this.executor.shutdownNow();
			this.executor.awaitTermination(2, TimeUnit.SECONDS);
			VWUtils.logWithClass(this.getClass().getSimpleName(), "Requests listener termination complete.");
		}
		catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		catch(Exception e) {
			VWUtils.log(e);
		}
	}

	private Map<SpaceCoordinates, Double[]> createDimensionsMap(int[] dimensions) {
		Map<SpaceCoordinates, Double[]> dimensionsMap = new EnumMap<>(SpaceCoordinates.class);

		dimensionsMap.put(SpaceCoordinates.NORTH, new Double[] { Double.valueOf(0), null });
		dimensionsMap.put(SpaceCoordinates.SOUTH, new Double[] { Double.valueOf(dimensions[1]), null });
		dimensionsMap.put(SpaceCoordinates.WEST, new Double[] { Double.valueOf(0), null });
		dimensionsMap.put(SpaceCoordinates.EAST, new Double[] { Double.valueOf(dimensions[0]), null });

		return dimensionsMap;
	}

	private void stopServer() {
		try {
			closeClientSocket();
			closeServerSocket();
		}
		catch (Exception e) {
			VWUtils.log(e);
			Thread.currentThread().interrupt();
		}
	}

	private void closeClientSocket() throws IOException {
		if (!this.clientSocket.isClosed()) {
			this.clientSocket.close();
		}
	}

	private void closeServerSocket() throws IOException {
		if (!this.server.isClosed()) {
			this.server.close();
		}
	}

	public void printState() {
		((VacuumWorldAppearance) this.universe.getAppearance()).updateRepresentation(this.universe.getState());
		VWUtils.logState(this.universe.getAppearance().represent());
	}
}