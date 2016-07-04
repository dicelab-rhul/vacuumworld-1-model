package uk.ac.rhul.cs.dice.vacuumworld;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import uk.ac.rhul.cs.dice.gawl.interfaces.actions.Action;
import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
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
import uk.ac.rhul.cs.dice.vacuumworld.actions.TotalPerceptionAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnLeftAction;
import uk.ac.rhul.cs.dice.vacuumworld.actions.TurnRightAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldDefaultActuator;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldAppearance;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldMonitoringContainer;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldSpace;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldUniverse;
import uk.ac.rhul.cs.dice.vacuumworld.environment.physics.VacuumWorldPhysics;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.DefaultEvaluationStrategy;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.DefaultPerceptionRefiner;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWEvaluatorActuator;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWEvaluatorAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWEvaluatorBrain;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWEvaluatorMind;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWEvaluatorSensor;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWMongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWObserverActuator;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWObserverBrain;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWObserverMind;
import uk.ac.rhul.cs.dice.vacuumworld.monitor.VWObserverSensor;

public class VacuumWorldServer {
  // counts the number of cycles that have happened (used for updating dirt in
  // database)
  private static int cycleNumber = 1;

  private ServerSocket server;
  private Socket clientSocket;
  private InputStream input;
  private VacuumWorldUniverse universe;
  private int threadsAfterDecide;
  private int threadsAfterPerceive;
  private Map<Long, AgentRunnable> runnableAgents;
  private List<Thread> activeThreads;

  public VacuumWorldServer(int port) throws IOException {
    this.server = new ServerSocket(port);
    this.threadsAfterDecide = 0;
    this.threadsAfterPerceive = 0;
    this.runnableAgents = new HashMap<>();
    this.activeThreads = new ArrayList<>();
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
      VacuumWorldMonitoringContainer initialState = VacuumWorldParser
          .parseInitialState(filename);
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
    VacuumWorldMonitoringContainer initialState = VacuumWorldParser
        .parseInitialState(this.input);

    constructUniverseAndStart(initialState);
  }

  private void constructUniverseAndStart(
      VacuumWorldMonitoringContainer initialState) {
    Set<Action> availableActions = getAvailableActions();
    Physics physics = new VacuumWorldPhysics();
    initialState.getPhysics().setMonitoredContainerPhysics(physics);
    int[] dimensions = initialState.getSubContainerSpace().getDimensions();
    Map<SpaceCoordinates, Double[]> dimensionsMap = createDimensionsMap(dimensions);
    VacuumWorldAppearance appearance = new VacuumWorldAppearance("VacuumWorld",
        dimensionsMap, initialState.getSubContainerSpace());
    this.universe = new VacuumWorldUniverse(initialState, availableActions,
        null, null, appearance);

    startSimulation(availableActions);
  }

  private Set<Action> getAvailableActions() {
    Set<Action> availableActions = new HashSet<>();

    availableActions.add(new TurnLeftAction());
    availableActions.add(new TurnRightAction());
    availableActions.add(new MoveAction());
    availableActions.add(new CleanAction());
    availableActions.add(new PerceiveAction());

    return availableActions;
  }

  private void startSimulation(Set<Action> availableActions) {
    VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.universe
        .getState();

    container.createVacuumWorldSpaceRepresentation();
    createTestObserver(container);

    List<VacuumWorldCleaningAgent> agents = container.getSubContainerSpace()
        .getAgents();

    for (VacuumWorldCleaningAgent agent : agents) {
      startAgentThread(agent, availableActions);
    }

    List<VWObserverAgent> oas = container.getObserverAgents();
    List<VWEvaluatorAgent> eas = container.getEvaluatorAgents();

    System.out.println("TEST" + oas.size());
    Set<Action> observerActions = new HashSet<Action>();
    observerActions.add(new TotalPerceptionAction());
    for (VWObserverAgent oa : oas) {
      startAgentThread(oa, observerActions);
    }

    for (VWEvaluatorAgent ea : eas) {
      System.out.println("Starting evaluator agent thread");
      startAgentThread(ea, new HashSet<Action>());
    }
    startMonitoring();
  }

  private void createTestObserver(VacuumWorldMonitoringContainer container) {
    // create database
    MongoConnector connector = new MongoConnector();
    connector.connect("localhost", "27017", null, null);
    connector.setDatabase("Test");
    connector.dropCollection("Test");
    CollectionRepresentation colrep = new CollectionRepresentation("Test", null);
    MongoBridge bridge = new VWMongoBridge(connector,
        container.getVacuumWorldSpaceRepresentation(), colrep);
    // create observer
    List<Sensor> sensors = new ArrayList<Sensor>();
    List<Actuator> actuators = new ArrayList<Actuator>();
    sensors.add(new VWObserverSensor());
    actuators.add(new VWObserverActuator());

    AgentClassModel observerClassModel = new AgentClassModel(
        VWObserverBrain.class, VWObserverAgent.class, VWObserverMind.class,
        VWObserverSensor.class, VWObserverActuator.class);

    VWObserverAgent a = new VWObserverAgent(new DefaultAgentAppearance(null,
        null), sensors, actuators, new VWObserverMind(
        new DefaultPerceptionRefiner()), new VWObserverBrain(),
        observerClassModel, (AbstractMongoBridge) bridge, colrep);
    container.addObserverAgent(a);

    // create evaluator
    sensors = new ArrayList<Sensor>();
    actuators = new ArrayList<Actuator>();
    sensors.add(new VWEvaluatorSensor());
    actuators.add(new VWEvaluatorActuator());

    AgentClassModel evaluatorClassModel = new AgentClassModel(
        VWEvaluatorBrain.class, VWEvaluatorAgent.class, VWEvaluatorMind.class,
        VWEvaluatorSensor.class, VWEvaluatorActuator.class);

    VWEvaluatorAgent e = new VWEvaluatorAgent(new DefaultAgentAppearance(null,
        null), sensors, actuators, new VWEvaluatorMind(
        new DefaultEvaluationStrategy()), new VWEvaluatorBrain(),
        evaluatorClassModel, (AbstractMongoBridge) bridge, colrep);
    container.addEvaluatorAgent(e);
  }

  private void startAgentThread(AbstractAgent agent,
      Set<Action> availableActions) {
    VacuumWorldMonitoringContainer container = (VacuumWorldMonitoringContainer) this.universe
        .getState();

    if (agent instanceof VacuumWorldCleaningAgent) {
      VacuumWorldCleaningAgent a = (VacuumWorldCleaningAgent) agent;
      container.getSubContainerSpace().addObserver(
          a.getSensors().get(a.getActionResultSensorIndex()));
      ((VacuumWorldDefaultActuator) a.getActuators().get(
          a.getActionActuatorIndex())).addObserver(container
          .getSubContainerSpace());
    }

    AgentRunnable agentRunnable = new AgentRunnable(agent, availableActions);
    this.runnableAgents.put(agentRunnable.getId(), agentRunnable);

    Thread agentThread = new Thread(agentRunnable);
    this.activeThreads.add(agentThread);

    agentThread.start();
  }

  private void startMonitoring() {
    boolean newCycle = true;

    while (newCycle) {
      try {
        monitorAfterDecide();
        monitorAfterPerceive();
        restartThreads();
        cycleNumber++;
      } catch (Exception e) {
        e.printStackTrace();
        newCycle = false;
      }
    }
  }

  private void restartThreads() {
    for (AgentRunnable agentRunnable : this.runnableAgents.values()) {
      Thread thread = new Thread(agentRunnable);
      thread.start();
    }
  }

  private void monitorAfterPerceive() {
    this.threadsAfterPerceive = 0;

    while (this.threadsAfterPerceive < this.runnableAgents.size()) {
      updateThreadsAterPerceive();
    }

    killThreads();
  }

  private void killThreads() {
    for (Thread thread : this.activeThreads) {
      if (!thread.isInterrupted()) {
        thread.interrupt();
      }
    }
  }

  private void resumeThreads() {
    for (AgentRunnable agentRunnable : this.runnableAgents.values()) {
      agentRunnable.resumeAgent();
    }
  }

  private void updateThreadsAterPerceive() {
    for (AgentRunnable agentRunnable : this.runnableAgents.values()) {
      if (agentRunnable.getThreadState() != ThreadState.AFTER_PERCEIVE) {
        this.threadsAfterPerceive = 0;
        continue;
      } else {
        this.threadsAfterPerceive++;
      }
    }
  }

  private void monitorAfterDecide() {
    this.threadsAfterDecide = 0;

    while (this.threadsAfterDecide < this.runnableAgents.size()) {
      updateThreadsAterDecide();
    }

    printState();
    resumeThreads();
  }

  private void updateThreadsAterDecide() {
    for (AgentRunnable agentRunnable : this.runnableAgents.values()) {
      if (agentRunnable.getThreadState() != ThreadState.AFTER_DECIDE) {
        this.threadsAfterDecide = 0;
        continue;
      } else {
        this.threadsAfterDecide++;
      }
    }
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

  private void printState() {
    ((VacuumWorldAppearance) this.universe.getAppearance())
        .updateRepresentation((VacuumWorldSpace) ((VacuumWorldMonitoringContainer) this.universe
            .getState()).getSubContainerSpace());
    System.out.println(this.universe.getAppearance().represent());
  }

  public static int getCycleNumber() {
    return cycleNumber;
  }
}