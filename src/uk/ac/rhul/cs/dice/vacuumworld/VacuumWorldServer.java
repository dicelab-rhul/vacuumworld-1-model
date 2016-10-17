package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.SpaceCoordinates;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.monitor.agents.AgentClassModel;
import uk.ac.rhul.cs.dice.monitor.agents.DefaultAgentAppearance;
import uk.ac.rhul.cs.dice.monitor.common.DefaultPerceptionRefiner;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.monitor.mongo.MongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.DropDirtAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultMind;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.UserMind;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorActuator;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorAgent;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorBrain;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorMind;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldMonitorSensor;
import uk.ac.rhul.cs.dice.vacuumworld.basicmonitor.VacuumWorldStepEvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldUniverse;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWEvaluatorActuator;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWEvaluatorAgent;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWEvaluatorBrain;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWEvaluatorMind;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWEvaluatorSensor;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWObserverActuator;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWObserverBrain;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWObserverMind;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VWObserverSensor;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.VacuumWorldDatatbaseStepEvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.AgentDatabaseRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.DirtDatabaseRepresentation;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.VacuumWorldMongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.VacuumWorldMongoConnector;
import uk.ac.rhul.cs.dice.vacuumworld.generation.ConfigFileException;
import uk.ac.rhul.cs.dice.vacuumworld.generation.ExperimentConnector;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldAgentThreadExperimentManager;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldAgentThreadManager;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldActorRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.utils.ConfigData;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.HandshakeCodes;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.HandshakeException;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ModelMessagesEnum;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ModelUpdate;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.StopSignal;
import uk.ac.rhul.cs.dice.vacuumworld.wvcommon.ViewRequestsEnum;

public class VacuumWorldServer implements Observer {
	private final Set<Class<? extends AbstractAction>> vacuumWorldActions;
	private final Set<Class<? extends AbstractAction>> monitoringWorldActions;

	private ServerSocket server;
	private Socket clientSocket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private VacuumWorldUniverse universe;
	private VacuumWorldAgentThreadManager threadManager;
	private ExecutorService executor;

	private static final int TEST_CYCLES = 100;
	
	private Semaphore listeningThreadSemaphore;
	private volatile StopSignal sharedStopSignal;

	public VacuumWorldServer() throws IOException {		
		this.server = new ServerSocket(ConfigData.getModelPort());

		this.vacuumWorldActions = new HashSet<>();
		this.vacuumWorldActions.add(TurnLeftAction.class);
		this.vacuumWorldActions.add(TurnRightAction.class);
		this.vacuumWorldActions.add(MoveAction.class);
		this.vacuumWorldActions.add(CleanAction.class);
		this.vacuumWorldActions.add(PerceiveAction.class);
		this.vacuumWorldActions.add(SpeechAction.class);
		this.vacuumWorldActions.add(DropDirtAction.class);

		this.monitoringWorldActions = new HashSet<>();
		this.monitoringWorldActions.add(TotalPerceptionAction.class);
		
		this.sharedStopSignal = new StopSignal();
	}

	public boolean isStopSignalOn() {
		return this.sharedStopSignal.mustStop();
	}
	
	public void startServer(String[] args, double delayInSeconds) throws HandshakeException {
		Utils.logWithClass(this.getClass().getSimpleName(), "Starting server...");
		
		try {
			startServerHelper(args, delayInSeconds);
		}
		catch(ClassNotFoundException e) {
			throw new HandshakeException(e);
		}
		catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void startServerHelper(String[] args, double delayInSeconds) throws ClassNotFoundException, HandshakeException, InterruptedException {
		if (args[0].equals(Main.TEST)) {
			test(args);
		} 
		else {
			startWithoutTest(args, delayInSeconds);
		}
	}

	private void startWithoutTest(String[] args, double delayInSeconds) throws ClassNotFoundException, HandshakeException, InterruptedException {
		this.threadManager = new VacuumWorldAgentThreadManager(this.sharedStopSignal);
		
		if (args[0].equals(Main.DEBUG)) {
			startServerFromFile(args[1]);
		}
		else {
			startServer(delayInSeconds);
		}
	}

	public boolean checkTestArgs(String[] args, String toCheck) {
		for (int i = 1; i < args.length; i++) {
			if (args[i].equals(toCheck)) {
				return true;
			}
		}
		return false;
	}

	public void test(String[] args) {
		try {
			this.threadManager = new VacuumWorldAgentThreadExperimentManager(TEST_CYCLES, this.sharedStopSignal);
			
			ExperimentConnector tester = new ExperimentConnector();
			
			if (checkTestArgs(args, Main.GENERATEFILES)) {
				tester.generateTestFiles();
			}
			
			Set<File> files = tester.getFilePaths();
			
			for(File file : files) {
				String name = file.getParent() + "/" + file.getName();
				((VacuumWorldAgentThreadExperimentManager) this.threadManager).setTestCase(name);
				startServerFromFile(file.getAbsolutePath());
				((VacuumWorldAgentThreadExperimentManager) this.threadManager).clear();
			}
		} catch (ConfigFileException e) {
			Utils.log(e);
		}
	}

	private void startServerFromFile(String fileName) {
		try {
			FileInputStream inputFileStream = new FileInputStream(fileName);
			VacuumWorldMonitoringContainer initialState = InitialStateParser.parseInitialState(inputFileStream);
			inputFileStream.close();
			constructUniverseAndStart(initialState, 1);
		} 
		catch (Exception e) {
			Utils.log(e);
		}
	}

	private void manageExceptionInStartup(Exception e) {
		if(Utils.INVALID_INITIAL_STATE.equals(e.getMessage())) {
			sendErrorToView();
		}
		else {
			Utils.log(e);
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
			Utils.log(e);
			stopSystem(ModelMessagesEnum.STOP_FORWARD);
		}
	}

	private void startServer(double delayInSeconds) throws ClassNotFoundException, HandshakeException, InterruptedException {
		try {
			doHandshake();
			manageRequests(delayInSeconds);
		}
		catch (IOException e) {
			manageExceptionInStartup(e);
		}
	}

	private void doHandshake() throws IOException, ClassNotFoundException, HandshakeException {
		Socket candidate = this.server.accept();
		ObjectOutputStream o = new ObjectOutputStream(candidate.getOutputStream());
		ObjectInputStream i = new ObjectInputStream(candidate.getInputStream());

		HandshakeCodes codeFromController = HandshakeCodes.fromString((String) i.readObject());
		Utils.logWithClass(this.getClass().getSimpleName(), "Received " + (codeFromController == null ? null : codeFromController.toString()) + " from controller.");  //CHCM
		
		if(Handshake.attemptHanshakeWithController(o, i, codeFromController)) {
			this.clientSocket = candidate;
			this.output = o;
			this.input = i;
		}
	}

	private void manageRequests(double delayInSeconds) throws IOException, ClassNotFoundException, InterruptedException {
		VacuumWorldMonitoringContainer initialState = InitialStateParser.parseInitialState(this.input);
		Utils.logWithClass(this.getClass().getSimpleName(), "Parser suceeded in parsing the initial state.\n");
		constructUniverseAndStart(initialState, delayInSeconds);
	}

	private void constructUniverseAndStart(VacuumWorldMonitoringContainer initialState, double delayInSeconds) throws InterruptedException {
		Physics physics = new VacuumWorldPhysics();
		initialState.getPhysics().setMonitoredContainerPhysics(physics);
		int[] dimensions = initialState.getSubContainerSpace().getDimensions();
		Map<SpaceCoordinates, Double[]> dimensionsMap = createDimensionsMap(dimensions);
		VacuumWorldAppearance appearance = new VacuumWorldAppearance("VacuumWorld", dimensionsMap, initialState.getSubContainerSpace());
		this.universe = new VacuumWorldUniverse(initialState, this.vacuumWorldActions, null, null, appearance);

		prepareAndStartSimulation(delayInSeconds);
	}

	private void prepareAndStartSimulation(double delayInSeconds) throws InterruptedException {
		VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.universe.getState();
		container.createVacuumWorldSpaceRepresentation();

		// how will the system be monitored
		if (ConfigData.getMonitoringFlag()) {
			this.loadBasicMonitorModel(container);
		}
		
		if (ConfigData.getEvaluateFlag() || ConfigData.getObserveFlag()) {
			this.loadEvaluatorObserverModel(container);
		}

		startSimulation(container, delayInSeconds);
	}

	private void startSimulation(VacuumWorldMonitoringContainer container, double delayInSeconds) throws InterruptedException {
		setupAgents(container);
		setupUser(container);		
		startListeningService(container);
		
		// START!
		this.threadManager.addObserver(this);
		this.threadManager.start(delayInSeconds);
	}

	private void setupUser(User user, VacuumWorldMonitoringContainer container) {
		VacuumWorldSpace space = container.getSubContainerSpace();
		
		user.getSeeingSensors().forEach(space::addObserver);
		user.getListeningSensors().forEach(space::addObserver);
		user.getPhysicalActuators().forEach((UserActuator actuator) -> actuator.addObserver(container.getSubContainerSpace()));
		user.getSpeakingActuators().forEach((UserActuator actuator) -> actuator.addObserver(container.getSubContainerSpace()));
		
		((UserMind) user.getMind()).setVacuumWorldActions(this.vacuumWorldActions);

		this.threadManager.addActor(new VacuumWorldActorRunnable(user.getMind()));
	}

	private void setupUser(VacuumWorldMonitoringContainer container) {
		User user = container.getSubContainerSpace().getUser();
		
		if(user != null) {
			setupUser(user, container);
		}
	}

	private void setupAgents(VacuumWorldMonitoringContainer container) {
		Set<VacuumWorldCleaningAgent> agents = container.getSubContainerSpace().getAgents();
		
		for(VacuumWorldCleaningAgent agent : agents) {
			setUpVacuumWorldCleaningAgent(agent);
		}
	}

	private void startListeningService(VacuumWorldMonitoringContainer initialState) {
		this.listeningThreadSemaphore = new Semaphore(0);
		
		VacuumWorldClientListener listener = new VacuumWorldClientListener(this.input, this.output, this.listeningThreadSemaphore, this.sharedStopSignal);
		this.threadManager.setClientListener(listener, initialState.getSubContainerSpace());
		
		this.executor = Executors.newSingleThreadExecutor();
		this.executor.execute(this.threadManager.getClientListener());
	}

	/**
	 * Called from notify in the {@link VacuumWorldThreadManager} showing that a
	 * cycle has ended and that the view should be updated.
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		Utils.increaseCycleNumber();
		
		if (ConfigData.getPrintGridFlag()) {
			printState();
		}
		
		manageViewRequest();
	}

	private void manageViewRequest() {
		Utils.logWithClass(this.getClass().getSimpleName(), "Waiting for view request.");
		this.listeningThreadSemaphore.release();
		
		ViewRequestsEnum code;
		
		do {
			code = this.threadManager.getClientListener().getRequestCode();
		}
		while(code == null);
		
		manageViewRequest(code);
	}

	// ***** SET UP VACUUM WORLD CLEANING AGENT ***** //

	private void manageViewRequest(ViewRequestsEnum code) {
		Utils.logWithClass(this.getClass().getSimpleName(), "Got " + code + " from view through controller.");
		
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
			Utils.log(e);
		}
	}

	private void stopSystem(ModelMessagesEnum code) {
		Utils.logWithClass(this.getClass().getSimpleName(), "Stopping the system and forwarding the stop request to the controller for him to shutdown...");
		
		try {
			ModelUpdate update = new ModelUpdate(code, null);
			this.threadManager.getClientListener().getOutputStream().writeObject(update);
			this.threadManager.getClientListener().getOutputStream().flush();
			
			this.server.close();
		}
		catch(IOException e) {
			Utils.log(e);
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
			Utils.logWithClass(this.getClass().getSimpleName(), "Requests listener termination complete.");
		}
		catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void setUpVacuumWorldCleaningAgent(VacuumWorldCleaningAgent agent) {
		VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.universe.getState();
		VacuumWorldSpace space = container.getSubContainerSpace();
		
		agent.getSeeingSensors().forEach(space::addObserver);
		agent.getListeningSensors().forEach(space::addObserver);
		agent.getPhysicalActuators().forEach((VacuumWorldDefaultActuator actuator) -> actuator.addObserver(container.getSubContainerSpace()));
		agent.getSpeakingActuators().forEach((VacuumWorldDefaultActuator actuator) -> actuator.addObserver(container.getSubContainerSpace()));
		
		((VacuumWorldDefaultMind) agent.getMind()).setCanSeeBehind(agent.canSeeBehind());
		((VacuumWorldDefaultMind) agent.getMind()).setPerceptionRange(agent.getPerceptionRange());
		((VacuumWorldDefaultMind) agent.getMind()).setVacuumWorldActions(this.vacuumWorldActions);

		this.threadManager.addActor(new VacuumWorldActorRunnable(agent.getMind()));
	}

	// ******** LOAD BASIC MONITOR MODEL ******** //

	private void loadBasicMonitorModel(VacuumWorldMonitoringContainer container) {
		createBasicMonitor(container);

		List<VacuumWorldMonitorAgent> mas = container.getMonitorAgents();
		Set<EnvironmentalAction> monitoringActions = new HashSet<>();
		monitoringActions.add(new TotalPerceptionAction());
		int counter = 0;
		
		for (VacuumWorldMonitorAgent m : mas) {			
			Utils.logWithClass(this.getClass().getSimpleName(), "Starting monitor agent thread #" + ++counter + "...");
			this.threadManager.addActor(new VacuumWorldActorRunnable(m.getMind()));
			((VacuumWorldMonitorMind) m.getMind()).setAvailableActions(this.monitoringWorldActions);
		}
	}

	private void createBasicMonitor(VacuumWorldMonitoringContainer container) {
		List<Sensor<VacuumWorldSensorRole>> sensors = new ArrayList<>();
		List<Actuator<VacuumWorldActuatorRole>> actuators = new ArrayList<>();
		String id = UUID.randomUUID().toString();
		
		sensors.add(new VacuumWorldMonitorSensor(id, VacuumWorldSensorRole.UNDEFINED));
		actuators.add(new VacuumWorldMonitorActuator(id, VacuumWorldActuatorRole.UNDEFINED));

		VacuumWorldMonitorAgent a = new VacuumWorldMonitorAgent(new DefaultAgentAppearance(null, null), sensors, actuators, new VacuumWorldMonitorMind(new VacuumWorldStepEvaluationStrategy()), new VacuumWorldMonitorBrain());
		a.setId(id);
		
		container.addMonitorAgent(a);
	}

	// ******** LOAD OBSERVER EVAULATOR MONITOR MODEL ******** //

	private void loadEvaluatorObserverModel(VacuumWorldMonitoringContainer container) {
		CollectionRepresentation agentCollection = new CollectionRepresentation(ConfigData.getAgentsCollection(), AgentDatabaseRepresentation.class);
		CollectionRepresentation dirtCollection = new CollectionRepresentation(ConfigData.getDirtsCollection(), DirtDatabaseRepresentation.class);
		MongoBridge bridge = createDatabase(container, dirtCollection, agentCollection);

		if (ConfigData.getObserveFlag()) {
			createObserver(container, bridge, dirtCollection, agentCollection);
		}
		if (ConfigData.getEvaluateFlag()) {
			createEvaluator(container, bridge, dirtCollection, agentCollection);
		}
		
		List<VWObserverAgent> oas = container.getObserverAgents();
		List<VWEvaluatorAgent> eas = container.getEvaluatorAgents();

		addMonitoringAgents(oas, eas);
	}

	private void addMonitoringAgents(List<VWObserverAgent> oas, List<VWEvaluatorAgent> eas) {
		int counter = 0;
		
		for (VWObserverAgent oa : oas) {
			Utils.logWithClass(this.getClass().getSimpleName(), "Starting observer agent thread #" + ++counter + "...");
			this.threadManager.addActor(new VacuumWorldActorRunnable(oa.getMind()));
			((VWObserverMind) oa.getMind()).setAvailableActions(this.monitoringWorldActions);
		}

		counter = 0;
		
		for (VWEvaluatorAgent ea : eas) {
			Utils.logWithClass(this.getClass().getSimpleName(), "Starting evaluator agent thread #" + ++counter + "...");
			this.threadManager.addActor(new VacuumWorldActorRunnable(ea.getMind()));
		}
	}

	private MongoBridge createDatabase(VacuumWorldMonitoringContainer container, CollectionRepresentation dirtCollection, CollectionRepresentation agentCollection) {
		VacuumWorldMongoConnector connector = new VacuumWorldMongoConnector();
		
		connector.connect(ConfigData.getDbHostname(), ConfigData.getDbPort(), null, null);
		connector.setDatabase(ConfigData.getDbName());
		connector.dropCollection(agentCollection.getCollectionName());
		connector.dropCollection(dirtCollection.getCollectionName());

		return new VacuumWorldMongoBridge(connector, container.getVacuumWorldSpaceRepresentation(), dirtCollection, agentCollection);
	}

	private void createEvaluator(VacuumWorldMonitoringContainer container, MongoBridge bridge, CollectionRepresentation dirtCollection, CollectionRepresentation agentCollection) {
		String id = UUID.randomUUID().toString();
		
		List<Sensor<VacuumWorldSensorRole>> sensors = new ArrayList<>();
		List<Actuator<VacuumWorldActuatorRole>> actuators = new ArrayList<>();
		sensors.add(new VWEvaluatorSensor(id, VacuumWorldSensorRole.UNDEFINED));
		actuators.add(new VWEvaluatorActuator(id, VacuumWorldActuatorRole.UNDEFINED));
		AgentClassModel evaluatorClassModel = new AgentClassModel(VWEvaluatorBrain.class, VWEvaluatorAgent.class, VWEvaluatorMind.class, VWEvaluatorSensor.class, VWEvaluatorActuator.class);

		VWEvaluatorAgent e = new VWEvaluatorAgent(new DefaultAgentAppearance(null, null), sensors, actuators, new VWEvaluatorMind(new VacuumWorldDatatbaseStepEvaluationStrategy(), dirtCollection, agentCollection), new VWEvaluatorBrain(), evaluatorClassModel, (AbstractMongoBridge) bridge);
		e.setId(id);
		
		container.addEvaluatorAgent(e);
	}

	private void createObserver(VacuumWorldMonitoringContainer container, MongoBridge bridge, CollectionRepresentation dirtCollection, CollectionRepresentation agentCollection) {
		String id = UUID.randomUUID().toString();
		
		List<Sensor<VacuumWorldSensorRole>> sensors = new ArrayList<>();
		List<Actuator<VacuumWorldActuatorRole>> actuators = new ArrayList<>();
		sensors.add(new VWObserverSensor(id, VacuumWorldSensorRole.UNDEFINED));
		actuators.add(new VWObserverActuator(id, VacuumWorldActuatorRole.UNDEFINED));

		AgentClassModel observerClassModel = new AgentClassModel(VWObserverBrain.class, VWObserverAgent.class, VWObserverMind.class, VWObserverSensor.class, VWObserverActuator.class);

		VWObserverAgent a = new VWObserverAgent(new DefaultAgentAppearance(null, null), sensors, actuators, new VWObserverMind(new DefaultPerceptionRefiner(), dirtCollection, agentCollection), new VWObserverBrain(), observerClassModel, (AbstractMongoBridge) bridge);
		a.setId(id);
		
		container.addObserverAgent(a);
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
			Utils.log(e);
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
		((VacuumWorldAppearance) this.universe.getAppearance()).updateRepresentation((VacuumWorldSpace) ((VacuumWorldMonitoringContainer) this.universe.getState()).getSubContainerSpace());
		Utils.logState(this.universe.getAppearance().represent());
	}
}