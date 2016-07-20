package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.function.Consumer;

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
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.DefaultEvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.DefaultPerceptionRefiner;
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
import uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database.VWMongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.generation.ExperimentConnector;
import uk.ac.rhul.cs.dice.vacuumworld.generation.ExperimentConnector.ConfigFileException;
import uk.ac.rhul.cs.dice.vacuumworld.threading.AgentRunnable;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldAgentThreadExperimentManager;
import uk.ac.rhul.cs.dice.vacuumworld.threading.VacuumWorldAgentThreadManager;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

public class VacuumWorldServer implements Observer {

  /* Control */
  private static final boolean LOAD_BASIC_MONITOR = true;
  private static final boolean LOAD_OBSERVER = false;
  private static final boolean LOAD_EVALUATOR = false;
  private static final boolean LOAD_EVALUATOR_OBSERVER = LOAD_OBSERVER
      || LOAD_EVALUATOR;
  public static final boolean LOG = false;
  public static final boolean PRINTMAP = true;

  private static final String DATABASENAME = "VacuumWorld";
  private static final String COLLECTIONNAME = "collection";
  private final Set<Class<? extends AbstractAction>> vacuumWorldActions;
  private final Set<Class<? extends AbstractAction>> monitoringWorldActions;

  private ServerSocket server;
  private Socket clientSocket;
  private InputStream input;
  private VacuumWorldUniverse universe;
  private VacuumWorldAgentThreadManager threadManager;

  private ExperimentConnector tester;
  private final int TESTCYCLES = 10;

  public VacuumWorldServer(int port) throws IOException {
    this.server = new ServerSocket(port);

    this.vacuumWorldActions = new HashSet<>();
    this.vacuumWorldActions.add(TurnLeftAction.class);
    this.vacuumWorldActions.add(TurnRightAction.class);
    this.vacuumWorldActions.add(MoveAction.class);
    this.vacuumWorldActions.add(CleanAction.class);
    this.vacuumWorldActions.add(PerceiveAction.class);
    this.vacuumWorldActions.add(SpeechAction.class);

    this.monitoringWorldActions = new HashSet<>();
    this.monitoringWorldActions.add(TotalPerceptionAction.class);
  }

  public void test() {
    try {
      threadManager = new VacuumWorldAgentThreadExperimentManager(TESTCYCLES);
      tester = new ExperimentConnector();
      tester.generateTestFiles();
      HashSet<File> files = tester.getFilePaths();
      Iterator<File> iter = files.iterator();
      while (iter.hasNext()) {
        File f = iter.next();
        String name = f.getParent() + "\\" + f.getName();
        ((VacuumWorldAgentThreadExperimentManager) threadManager)
            .setTestCase(name);
        startServerFromFile(f.getAbsolutePath());
        ((VacuumWorldAgentThreadExperimentManager) threadManager).clear();
        System.gc();
      }
    } catch (ConfigFileException e) {
      e.printStackTrace();
    }
  }

  public void startServer(String arg, String fileName) {
    if (arg.equals("test")) {
      test();
    } else {
      this.threadManager = new VacuumWorldAgentThreadManager();
      if (arg.equals("debug")) {
        startServerFromFile(fileName);
      } else {
        startServer();
      }
    }

  }

  private void startServerFromFile(String fileName) {
    try {
      VacuumWorldMonitoringContainer initialState = VacuumWorldParser
          .parseInitialState(fileName);
      constructUniverseAndStart(initialState);
    } catch (IOException e) {
      Utils.log(e);
      stopServer();
    }
  }

  private void startServer() {
    try {
      this.clientSocket = this.server.accept();
      this.input = this.clientSocket.getInputStream();
      manageRequests();
    } catch (IOException e) {
      Utils.log(e);
      stopServer();
    }
  }

  private void manageRequests() throws IOException {
    VacuumWorldMonitoringContainer initialState = VacuumWorldParser
        .parseInitialState(this.input);
    constructUniverseAndStart(initialState);
  }

  private void constructUniverseAndStart(
      VacuumWorldMonitoringContainer initialState) {
    Physics physics = new VacuumWorldPhysics();
    initialState.getPhysics().setMonitoredContainerPhysics(physics);
    int[] dimensions = initialState.getSubContainerSpace().getDimensions();
    Map<SpaceCoordinates, Double[]> dimensionsMap = createDimensionsMap(dimensions);
    VacuumWorldAppearance appearance = new VacuumWorldAppearance("VacuumWorld",
        dimensionsMap, initialState.getSubContainerSpace());
    this.universe = new VacuumWorldUniverse(initialState,
        this.vacuumWorldActions, null, null, appearance);

    prepareAndStartSimulation();
  }

  private void prepareAndStartSimulation() {
    VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.universe
        .getState();
    container.createVacuumWorldSpaceRepresentation();

    // how will the system be monitored
    if (LOAD_BASIC_MONITOR) {
      this.loadBasicMonitorModel(container);
    }
    if (LOAD_EVALUATOR_OBSERVER) {
      this.loadEvaluatorObserverModel(container);
    }

    startSimulation(container);
  }

  private void startSimulation(VacuumWorldMonitoringContainer container) {
    Set<VacuumWorldCleaningAgent> agents = container.getSubContainerSpace()
        .getAgents();

    agents.forEach(new Consumer<VacuumWorldCleaningAgent>() {
      @Override
      public void accept(VacuumWorldCleaningAgent agent) {
        setUpVacuumWorldCleaningAgent(agent);
      }
    });
    // START!
    this.threadManager.addObserver(this);
    this.threadManager.start();
  }

  /**
   * Called from notify in the {@link VacuumWorldThreadManager} showing that a
   * cycle has ended and that the view should be updated.
   */
  @Override
  public void update(Observable arg0, Object arg1) {
    Utils.increaseCycleNumber();
    if (PRINTMAP) {
      printState();
    }
  }

  // ***** SET UP VACUUM WORLD CLEANING AGENT ***** //

  private void setUpVacuumWorldCleaningAgent(VacuumWorldCleaningAgent agent) {
    VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.universe
        .getState();

    container.getSubContainerSpace().addObserver(
        agent.getSensors().get(agent.getActionResultSensorIndex()));

    VacuumWorldDefaultActuator actuator = (VacuumWorldDefaultActuator) agent
        .getActuators().get(agent.getActionActuatorIndex());
    actuator.addObserver(container.getSubContainerSpace());

    ((VacuumWorldDefaultMind) agent.getMind()).setCanSeeBehind(agent
        .canSeeBehind());
    ((VacuumWorldDefaultMind) agent.getMind()).setPerceptionRange(agent
        .getPerceptionRange());
    ((VacuumWorldDefaultMind) agent.getMind())
        .setAvailableActions(vacuumWorldActions);

    this.threadManager.addAgent(new AgentRunnable(agent.getMind()));
  }

  // ******** LOAD BASIC MONITOR MODEL ******** //

  private void loadBasicMonitorModel(VacuumWorldMonitoringContainer container) {
    createBasicMonitor(container);

    List<VacuumWorldMonitorAgent> mas = container.getMonitorAgents();
    Set<EnvironmentalAction> monitoringActions = new HashSet<EnvironmentalAction>();
    monitoringActions.add(new TotalPerceptionAction());

    for (VacuumWorldMonitorAgent m : mas) {
      Utils.log("Starting monitor agent thread");
      this.threadManager.addAgent(new AgentRunnable(m.getMind()));
      ((VacuumWorldMonitorMind) m.getMind())
          .setAvailableActions(this.monitoringWorldActions);
    }
  }

  private void createBasicMonitor(VacuumWorldMonitoringContainer container) {
    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    sensors.add(new VacuumWorldMonitorSensor());
    actuators.add(new VacuumWorldMonitorActuator());

    VacuumWorldMonitorAgent a = new VacuumWorldMonitorAgent(
        new DefaultAgentAppearance(null, null), sensors, actuators,
        new VacuumWorldMonitorMind(new VacuumWorldStepEvaluationStrategy()),
        new VacuumWorldMonitorBrain());

    container.addMonitorAgent(a);
  }

  // ******** LOAD OBSERVER EVAULATOR MONITOR MODEL ******** //

  private void loadEvaluatorObserverModel(
      VacuumWorldMonitoringContainer container) {
    CollectionRepresentation collectionRepresentation = new CollectionRepresentation(
        COLLECTIONNAME, null);
    MongoBridge bridge = createDatabase(container, collectionRepresentation);
    if (LOAD_OBSERVER) {
      createObserver(container, bridge, collectionRepresentation);
    }
    if (LOAD_EVALUATOR) {
      createEvaluator(container, bridge, collectionRepresentation);
    }
    List<VWObserverAgent> oas = container.getObserverAgents();
    List<VWEvaluatorAgent> eas = container.getEvaluatorAgents();

    addMonitoringAgents(oas, eas);
  }

  private void addMonitoringAgents(List<VWObserverAgent> oas,
      List<VWEvaluatorAgent> eas) {
    for (VWObserverAgent oa : oas) {
      Utils.log("Starting observer agent thread");
      this.threadManager.addAgent(new AgentRunnable(oa.getMind()));
      ((VWObserverMind) oa.getMind())
          .setAvailableActions(this.monitoringWorldActions);
    }

    for (VWEvaluatorAgent ea : eas) {
      Utils.log("Starting evaluator agent thread");
      this.threadManager.addAgent(new AgentRunnable(ea.getMind()));
    }
  }

  private MongoBridge createDatabase(VacuumWorldMonitoringContainer container,
      CollectionRepresentation collectionRepresentation) {
    MongoConnector connector = new MongoConnector();
    connector.connect("localhost", "27017", null, null);
    connector.setDatabase(DATABASENAME);
    connector.dropCollection(collectionRepresentation.getCollectionName());

    return new VWMongoBridge(connector,
        container.getVacuumWorldSpaceRepresentation(), collectionRepresentation);
  }

  private void createEvaluator(VacuumWorldMonitoringContainer container,
      MongoBridge bridge, CollectionRepresentation collectionRepresentation) {
    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    sensors.add(new VWEvaluatorSensor());
    actuators.add(new VWEvaluatorActuator());
    AgentClassModel evaluatorClassModel = new AgentClassModel(
        VWEvaluatorBrain.class, VWEvaluatorAgent.class, VWEvaluatorMind.class,
        VWEvaluatorSensor.class, VWEvaluatorActuator.class);

    VWEvaluatorAgent e = new VWEvaluatorAgent(new DefaultAgentAppearance(null,
        null), sensors, actuators, new VWEvaluatorMind(
        new DefaultEvaluationStrategy()), new VWEvaluatorBrain(),
        evaluatorClassModel, (AbstractMongoBridge) bridge,
        collectionRepresentation);

    container.addEvaluatorAgent(e);
  }

  private void createObserver(VacuumWorldMonitoringContainer container,
      MongoBridge bridge, CollectionRepresentation collectionRepresentation) {
    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    sensors.add(new VWObserverSensor());
    actuators.add(new VWObserverActuator());

    AgentClassModel observerClassModel = new AgentClassModel(
        VWObserverBrain.class, VWObserverAgent.class, VWObserverMind.class,
        VWObserverSensor.class, VWObserverActuator.class);

    VWObserverAgent a = new VWObserverAgent(new DefaultAgentAppearance(null,
        null), sensors, actuators, new VWObserverMind(
        new DefaultPerceptionRefiner()), new VWObserverBrain(),
        observerClassModel, (AbstractMongoBridge) bridge,
        collectionRepresentation);

    container.addObserverAgent(a);
  }

  private Map<SpaceCoordinates, Double[]> createDimensionsMap(int[] dimensions) {
    Map<SpaceCoordinates, Double[]> dimensionsMap = new EnumMap<>(
        SpaceCoordinates.class);

    dimensionsMap.put(SpaceCoordinates.NORTH, new Double[] { Double.valueOf(0),
        null });
    dimensionsMap.put(SpaceCoordinates.SOUTH,
        new Double[] { Double.valueOf(dimensions[1]), null });
    dimensionsMap.put(SpaceCoordinates.WEST, new Double[] { Double.valueOf(0),
        null });
    dimensionsMap.put(SpaceCoordinates.EAST,
        new Double[] { Double.valueOf(dimensions[0]), null });

    return dimensionsMap;
  }

  private void stopServer() {
    try {
      closeClientSocket();
      closeServerSocket();
    } catch (IOException e) {
      Utils.log(e);
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
    ((VacuumWorldAppearance) this.universe.getAppearance())
        .updateRepresentation((VacuumWorldSpace) ((VacuumWorldMonitoringContainer) this.universe
            .getState()).getSubContainerSpace());
    System.out.println(this.universe.getAppearance().represent());
  }
}