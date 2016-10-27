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

import javax.json.JsonObject;

import uk.ac.rhul.cs.dice.gawl.interfaces.environment.SpaceCoordinates;
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
import uk.ac.rhul.cs.dice.vacuumworld.utils.parser.JsonForControllerBuilder;
import uk.ac.rhul.cs.dice.vacuumworld.utils.parser.StateRepresentationBuilder;
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
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Starting Model server...");
		this.threadManager = new VacuumWorldAgentThreadManager(this.sharedStopSignal);
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Model server started.");
		
		doHandshakePhase();
		
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Handshake with Controller and View succesfully completed.");
		
		startManagingRequests(delayInSeconds);
	}
	
	private void doHandshakePhase() {
		try {
			doHandshake();
		}
		catch(Exception e) {
			manageExceptionInHandshake(e);
		}
	}
	
	private void manageExceptionInHandshake(Exception e) {
		VWUtils.log(e);
		VWUtils.logWithClass(getClass().getSimpleName(), "Error in handshake.");
		stopModelServer();
	}
	
	private void startManagingRequests(double delayInSeconds) {
		try {
			manageRequests(delayInSeconds);
		}
		catch(Exception e) {
			manageExceptionInExecution(e);
		}
	}

	private void manageExceptionInExecution(Exception e) {
		if(VWUtils.isInitialStateInvalid(e)) {
			manageInvalidInitialState();
		}
		else {
			sendStopSignalToController(ModelMessagesEnum.STOP_FORWARD);
			stopModelServer();
		}
	}

	private void manageInvalidInitialState() {
		VWUtils.logWithClass(getClass().getSimpleName(), "Bad initial state. Forwarding error to View...");
		sendInitialStateErrorToView();
		stopModelServer();
	}
	
	private void stopModelServer() {
		VWUtils.logWithClass(getClass().getSimpleName(), "Attempt to shutdown the model in a clean way...");
		this.sharedStopSignal.stop();
		
		shutdownClientListenerIfNecessary();
		waitForActorsThreadsManagerTerminationIfNecessary();
		closeSocketsIfNecessary();
		
		VWUtils.logWithClass(getClass().getSimpleName(), "Done.");
	}

	private void shutdownClientListenerIfNecessary() {
		VWUtils.logWithClass(getClass().getSimpleName(), "Attempt to shutdown Controller Listener in a clean way...");
		
		if(this.executor == null) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Controller Listener does not exist: no need for termination.");
			
			return;
		}
		if(this.executor.isTerminated()) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Controller Listener was already terminated.");
			
			return;
		}
		
		shutdownClientListener();
	}

	private void shutdownClientListener() {
		try {
			this.executor.shutdownNow();
			this.executor.awaitTermination(2, TimeUnit.SECONDS);
			checkClisternerTermination();
		}
		catch(InterruptedException e) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Controller Listener was not correctly terminated. A forcefully JVM termination will be needed.");
			Thread.currentThread().interrupt();
		}
	}

	private void checkClisternerTermination() {
		if(this.executor.isTerminated()) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Controller Listener correctly terminated.");
		}
		else {
			VWUtils.logWithClass(getClass().getSimpleName(), "Controller Listener was not correctly terminated. A forcefully JVM termination will be needed.");
		}
	}

	private void waitForActorsThreadsManagerTerminationIfNecessary() {
		VWUtils.logWithClass(getClass().getSimpleName(), "Attempt to shutdown actors threads in a clean way...");
		
		if(this.threadManager == null) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager does not exists: no actors threads to terminate.");
		}
		if(this.threadManager.isTerminated()) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager was already terminated: no actors threads to terminate.");
		}
		
		terminateActorsThreads();
		waitForActorsThreadsManagerTermination();
	}

	private void terminateActorsThreads() {
		try {
			this.threadManager.shutdownExecutors();
		}
		catch(InterruptedException e) {
			VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager is still pending. A forcefully JVM termination will be needed.");
			Thread.currentThread().interrupt();
		}
	}

	private void waitForActorsThreadsManagerTermination() {
		long time = System.currentTimeMillis();
		
		while(true) {
			if(System.currentTimeMillis() - time > 10000) {
				VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager is still pending. A forcefully JVM termination will be needed.");
				
				return;
			}
			
			if(this.threadManager.isTerminated()) {
				VWUtils.logWithClass(getClass().getSimpleName(), "Thread Manager correctly terminated: assuming all the actors threads have been terminated in a clean way.");
				
				return;
			}
		}
	}

	private void closeSocketsIfNecessary() {
		VWUtils.logWithClass(getClass().getSimpleName(), "Attempting to close the socket with the Controller....");
		closeClientSocket();
		VWUtils.logWithClass(getClass().getSimpleName(), "Attempting to close the server socket...");
		closeServerSocket();
	}
	
	private void closeClientSocket() {
		try {
			if (!this.clientSocket.isClosed()) {
				this.clientSocket.close();
				VWUtils.logWithClass(getClass().getSimpleName(), "Socket with Controller correctly closed.");
			}
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			VWUtils.logWithClass(getClass().getSimpleName(), "Socket with Controller was already closed.");
		}
	}

	private void closeServerSocket() {
		try {
			if (!this.server.isClosed()) {
				this.server.close();
				VWUtils.logWithClass(getClass().getSimpleName(), "Server socket correctly closed.");
			}
		}
		catch(Exception e) {
			VWUtils.fakeLog(e);
			VWUtils.logWithClass(getClass().getSimpleName(), "Server socket was already closed.");
		}
	}

	public boolean isStopSignalOn() {
		return this.sharedStopSignal.mustStop();
	}

	private void sendInitialStateErrorToView() {
		try {
			ModelUpdate update = new ModelUpdate(ModelMessagesEnum.BAD_INITIAL_STATE, null);
			this.output.writeObject(update);
			this.output.flush();
		}
		catch(IOException e) {
			VWUtils.log(e);
		}
		finally {
			sendStopSignalToController(ModelMessagesEnum.STOP_FORWARD);
		}
	}

	private void sendStopSignalToController(ModelMessagesEnum code) {
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Sending a stop request to the controller for him to shutdown...");
		
		try {
			ModelUpdate update = new ModelUpdate(code, null);
			this.threadManager.getClientListener().getOutputStream().writeObject(update);
			this.threadManager.getClientListener().getOutputStream().flush();
		}
		catch(Exception e) {
			VWUtils.log(e);
		}
	}
	
	private void doHandshake() throws IOException, ClassNotFoundException, HandshakeException {
		Socket candidate = this.server.accept();
		ObjectOutputStream o = new ObjectOutputStream(candidate.getOutputStream());
		ObjectInputStream i = new ObjectInputStream(candidate.getInputStream());

		VWUtils.logWithClass(this.getClass().getSimpleName(), "Model server connected with (presumably the Controller server) " + candidate.getInetAddress().getHostAddress() + ":" + candidate.getPort() + ".");
		
		HandshakeCodes codeFromController = HandshakeCodes.fromString((String) i.readObject());
		VWUtils.logWithClass(this.getClass().getSimpleName(), "Received " + (codeFromController == null ? null : codeFromController.toString()) + " from controller.");  //CHCM
		
		finalizeHandshake(o, i, codeFromController, candidate);
	}

	private void finalizeHandshake(ObjectOutputStream o, ObjectInputStream i, HandshakeCodes codeFromController, Socket candidate) throws HandshakeException {
		if(Handshake.attemptHanshakeWithController(o, i, codeFromController)) {
			this.clientSocket = candidate;
			this.output = o;
			this.input = i;
		}
		else {
			throw new HandshakeException("Failed handshake.");
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
		startListeningService();
		
		this.threadManager.addObserver(this);
		this.threadManager.start(delayInSeconds);
	}

	private void setupMonitoringAgents() {
		for(VacuumWorldMonitoringAgent agent : this.universe.getMonitoringContainer().getMonitoringAgentsMap().values()) {
			setUpVacuumWorldMonitoringAgent(agent);
		}
	}

	private void setUpVacuumWorldMonitoringAgent(VacuumWorldMonitoringAgent agent) {
		VacuumWorldMonitoringContainer monitoringSpace = this.universe.getMonitoringContainer();
		
		agent.getSeeingSensors().forEach(monitoringSpace::addObserver);
		agent.getListeningSensors().forEach(monitoringSpace::addObserver);
		agent.getDatabaseSensors().forEach(monitoringSpace::addObserver);
		
		agent.getPhysicalActuators().forEach(actuator -> actuator.addObserver(monitoringSpace));
		agent.getSpeakingActuators().forEach(actuator -> actuator.addObserver(monitoringSpace));
		agent.getDatabaseActuators().forEach(actuator -> actuator.addObserver(monitoringSpace));
		
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
		user.getPhysicalActuators().forEach(actuator -> actuator.addObserver(space));
		user.getSpeakingActuators().forEach(actuator -> actuator.addObserver(space));
		
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
		agent.getPhysicalActuators().forEach(actuator -> actuator.addObserver(space));
		agent.getSpeakingActuators().forEach(actuator -> actuator.addObserver(space));
		
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
			sendStopSignalToController(ModelMessagesEnum.STOP_CONTROLLER);
			stopModelServer();
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

	private Map<SpaceCoordinates, Double[]> createDimensionsMap(int[] dimensions) {
		Map<SpaceCoordinates, Double[]> dimensionsMap = new EnumMap<>(SpaceCoordinates.class);

		dimensionsMap.put(SpaceCoordinates.NORTH, new Double[] { Double.valueOf(0), null });
		dimensionsMap.put(SpaceCoordinates.SOUTH, new Double[] { Double.valueOf(dimensions[1]), null });
		dimensionsMap.put(SpaceCoordinates.WEST, new Double[] { Double.valueOf(0), null });
		dimensionsMap.put(SpaceCoordinates.EAST, new Double[] { Double.valueOf(dimensions[0]), null });

		return dimensionsMap;
	}

	public void printState() {
		this.universe.getAppearance().updateRepresentation(this.universe.getState());
		VWUtils.logState(this.universe.getAppearance().represent());
	}
}