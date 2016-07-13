package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.AbstractAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.actions.EnvironmentalAction;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Actuator;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.Sensor;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.SpaceCoordinates;
import uk.ac.rhul.cs.dice.gawl.interfaces.environment.physics.Physics;
import uk.ac.rhul.cs.dice.monitor.agents.AgentClassModel;
import uk.ac.rhul.cs.dice.monitor.agents.DefaultAgentAppearance;
import uk.ac.rhul.cs.dice.monitor.mongo.AbstractMongoBridge;
import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.monitor.mongo.MongoBridge;
import uk.ac.rhul.cs.dice.monitor.mongo.MongoConnector;
import uk.ac.rhul.cs.dice.vacuumworld.actions.CleanAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.PerceiveAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.SpeechAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultMind;
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
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.DefaultEvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.DefaultPerceptionRefiner;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWEvaluatorActuator;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWEvaluatorAgent;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWEvaluatorBrain;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWEvaluatorMind;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWEvaluatorSensor;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverActuator;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverBrain;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverMind;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverSensor;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.database.VWMongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.threading.AgentRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldAgentThreadManager;

public class VacuumWorldServer implements Observer {
	// counts the number of cycles that have happened (used for updating dirt in
	// database)
	private static int cycleNumber = 1;

	private final boolean loadBasicMonitor = false;
	private final boolean loadEvaluatorObserver = true;
	private final Set<Class<? extends AbstractAction>> VACUUMWORLDACTIONS;

	private final Set<Class<? extends AbstractAction>> MONITORINGWORLDACTIONS;

	private ServerSocket server;
	private Socket clientSocket;
	private InputStream input;
	private VacuumWorldUniverse universe;
	private VacuumWorldAgentThreadManager threadManager;

	public VacuumWorldServer(int port) throws IOException {
		this.server = new ServerSocket(port);
		threadManager = new VacuumWorldAgentThreadManager();

		VACUUMWORLDACTIONS = new HashSet<>();
		VACUUMWORLDACTIONS.add(TurnLeftAction.class);
		VACUUMWORLDACTIONS.add(TurnRightAction.class);
		VACUUMWORLDACTIONS.add(MoveAction.class);
		VACUUMWORLDACTIONS.add(CleanAction.class);
		VACUUMWORLDACTIONS.add(PerceiveAction.class);
		VACUUMWORLDACTIONS.add(SpeechAction.class);

		MONITORINGWORLDACTIONS = new HashSet<>();
		MONITORINGWORLDACTIONS.add(TotalPerceptionAction.class);
	}

	public void startServer(boolean debug) {
		if (debug) {
			startServerFromFile();
		} else {
			startServer();
		}
	}

	private void startServerFromFile() {
		try {
			String filename = "state_example.json";
			VacuumWorldMonitoringContainer initialState = VacuumWorldParser.parseInitialState(filename);
			constructUniverseAndStart(initialState);
		} catch (IOException e) {
			Logger.getGlobal().log(Level.INFO, e.getMessage(), e);
			stopServer();
		}
	}

	private void startServer() {
		try {
			this.clientSocket = this.server.accept();
			this.input = this.clientSocket.getInputStream();
			manageRequests();
		} catch (IOException e) {
			Logger.getGlobal().log(Level.INFO, e.getMessage(), e);
			stopServer();
		}
	}

	private void manageRequests() throws IOException {
		VacuumWorldMonitoringContainer initialState = VacuumWorldParser.parseInitialState(this.input);
		constructUniverseAndStart(initialState);
	}

	private void constructUniverseAndStart(VacuumWorldMonitoringContainer initialState) {
		Physics physics = new VacuumWorldPhysics();
		initialState.getPhysics().setMonitoredContainerPhysics(physics);
		int[] dimensions = initialState.getSubContainerSpace().getDimensions();
		Map<SpaceCoordinates, Double[]> dimensionsMap = createDimensionsMap(dimensions);
		VacuumWorldAppearance appearance = new VacuumWorldAppearance("VacuumWorld", dimensionsMap,
				initialState.getSubContainerSpace());
		this.universe = new VacuumWorldUniverse(initialState, VACUUMWORLDACTIONS, null, null, appearance);
		startSimulation();
	}

	private void startSimulation() {
		VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.universe.getState();

		container.createVacuumWorldSpaceRepresentation();
		// how will the system be monitored
		if (loadBasicMonitor) {
			this.loadBasicMonitorModel(container);
		}
		if (loadEvaluatorObserver) {
			this.loadEvaluatorObserverModel(container);
		}

		List<VacuumWorldCleaningAgent> agents = container.getSubContainerSpace().getAgents();

		for (VacuumWorldCleaningAgent agent : agents) {
			setUpVacuumWorldCleaningAgent(agent);
		}

		// START!
		threadManager.addObserver(this);
		threadManager.start();
	}

	/**
	 * Called from notify in the {@link VacuumWorldThreadManager} showing that a
	 * cycle has ended and that the view should be updated.
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		cycleNumber++;
		printState();
	}

	// ***** SET UP VACUUM WORLD CLEANING AGENT ***** //

	private void setUpVacuumWorldCleaningAgent(VacuumWorldCleaningAgent agent) {
		VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.universe.getState();

		container.getSubContainerSpace().addObserver(agent.getSensors().get(agent.getActionResultSensorIndex()));

		VacuumWorldDefaultActuator actuator = (VacuumWorldDefaultActuator) agent.getActuators()
				.get(agent.getActionActuatorIndex());
		actuator.addObserver(container.getSubContainerSpace());

		((VacuumWorldDefaultMind) agent.getMind()).setCanSeeBehind(agent.canSeeBehind());
		((VacuumWorldDefaultMind) agent.getMind()).setPerceptionRange(agent.getPerceptionRange());
		((VacuumWorldDefaultMind) agent.getMind()).setAvailableActions(VACUUMWORLDACTIONS);

		threadManager.addAgent(new AgentRunnable(agent.getMind()));
	}

	// ******** LOAD BASIC MONITOR MODEL ******** //

	private void loadBasicMonitorModel(VacuumWorldMonitoringContainer container) {
		createBasicMonitor(container);

		List<VacuumWorldMonitorAgent> mas = container.getMonitorAgents();
		Set<EnvironmentalAction> monitoringActions = new HashSet<EnvironmentalAction>();
		monitoringActions.add(new TotalPerceptionAction());

		for (VacuumWorldMonitorAgent m : mas) {
			System.out.println("Starting monitor agent thread");
			threadManager.addAgent(new AgentRunnable(m.getMind()));
			((VacuumWorldMonitorMind) m.getMind()).setAvailableActions(MONITORINGWORLDACTIONS);
		}
	}

	private void createBasicMonitor(VacuumWorldMonitoringContainer container) {
		List<Sensor> sensors = new ArrayList<>();
		List<Actuator> actuators = new ArrayList<>();
		sensors.add(new VacuumWorldMonitorSensor());
		actuators.add(new VacuumWorldMonitorActuator());

		VacuumWorldMonitorAgent a = new VacuumWorldMonitorAgent(new DefaultAgentAppearance(null, null), sensors,
				actuators, new VacuumWorldMonitorMind(new VacuumWorldStepEvaluationStrategy()),
				new VacuumWorldMonitorBrain());
		container.addMonitorAgent(a);
	}

	// ******** LOAD OBSERVER EVAULATOR MONITOR MODEL ******** //

	private void loadEvaluatorObserverModel(VacuumWorldMonitoringContainer container) {
		CollectionRepresentation collectionRepresentation = new CollectionRepresentation("Test", null);
		MongoBridge bridge = createDatabase(container, collectionRepresentation);
		createObserver(container, bridge, collectionRepresentation);
		createEvaluator(container, bridge, collectionRepresentation);

		List<VWObserverAgent> oas = container.getObserverAgents();
		List<VWEvaluatorAgent> eas = container.getEvaluatorAgents();

		for (VWObserverAgent oa : oas) {
			System.out.println("Starting observer agent thread");
			threadManager.addAgent(new AgentRunnable(oa.getMind()));
			((VWObserverMind) oa.getMind()).setAvailableActions(MONITORINGWORLDACTIONS);
		}

		for (VWEvaluatorAgent ea : eas) {
			System.out.println("Starting evaluator agent thread");
			threadManager.addAgent(new AgentRunnable(ea.getMind()));
		}
	}

	private MongoBridge createDatabase(VacuumWorldMonitoringContainer container,
			CollectionRepresentation collectionRepresentation) {
		MongoConnector connector = new MongoConnector();
		connector.connect("localhost", "27017", null, null);
		connector.setDatabase("Test");
		connector.dropCollection("Test");

		return new VWMongoBridge(connector, container.getVacuumWorldSpaceRepresentation(), collectionRepresentation);
	}

	private void createEvaluator(VacuumWorldMonitoringContainer container, MongoBridge bridge,
			CollectionRepresentation collectionRepresentation) {
		List<Sensor> sensors = new ArrayList<>();
		List<Actuator> actuators = new ArrayList<>();
		sensors.add(new VWEvaluatorSensor());
		actuators.add(new VWEvaluatorActuator());
		AgentClassModel evaluatorClassModel = new AgentClassModel(VWEvaluatorBrain.class, VWEvaluatorAgent.class,
				VWEvaluatorMind.class, VWEvaluatorSensor.class, VWEvaluatorActuator.class);

		VWEvaluatorAgent e = new VWEvaluatorAgent(new DefaultAgentAppearance(null, null), sensors, actuators,
				new VWEvaluatorMind(new DefaultEvaluationStrategy()), new VWEvaluatorBrain(), evaluatorClassModel,
				(AbstractMongoBridge) bridge, collectionRepresentation);
		container.addEvaluatorAgent(e);
	}

	private void createObserver(VacuumWorldMonitoringContainer container, MongoBridge bridge,
			CollectionRepresentation collectionRepresentation) {
		List<Sensor> sensors = new ArrayList<>();
		List<Actuator> actuators = new ArrayList<>();
		sensors.add(new VWObserverSensor());
		actuators.add(new VWObserverActuator());

		AgentClassModel observerClassModel = new AgentClassModel(VWObserverBrain.class, VWObserverAgent.class,
				VWObserverMind.class, VWObserverSensor.class, VWObserverActuator.class);

		VWObserverAgent a = new VWObserverAgent(new DefaultAgentAppearance(null, null), sensors, actuators,
				new VWObserverMind(new DefaultPerceptionRefiner()), new VWObserverBrain(), observerClassModel,
				(AbstractMongoBridge) bridge, collectionRepresentation);
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
		} catch (IOException e) {
			Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
			return;
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
		((VacuumWorldAppearance) this.universe.getAppearance()).updateRepresentation(
				(VacuumWorldSpace) ((VacuumWorldMonitoringContainer) this.universe.getState()).getSubContainerSpace());
		System.out.println(this.universe.getAppearance().represent());
	}

	public static int getCycleNumber() {
		return cycleNumber;
	}
}