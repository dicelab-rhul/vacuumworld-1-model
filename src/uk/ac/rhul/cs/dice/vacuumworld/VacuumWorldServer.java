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
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWMongoBridge;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverActuator;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverAgent;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverBrain;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverMind;
import uk.ac.rhul.cs.dice.vacuumworld.evaluatorObserver.VWObserverSensor;

public class VacuumWorldServer {
  // counts the number of cycles that have happened (used for updating dirt in
  // database)
  private static int cycleNumber = 1;
  
  private static boolean loadBasicMonitor = false;
  private static boolean loadEvaluatorObserver = false;

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
    //how will the system be monitored
    if (loadBasicMonitor) {
      this.loadBasicMonitorModel(container);
    }
    if (loadEvaluatorObserver) {
      this.loadEvaluatorObserverModel(container);
    }

    List<VacuumWorldCleaningAgent> agents = container.getSubContainerSpace()
        .getAgents();

    for (VacuumWorldCleaningAgent agent : agents) {
      startAgentThread(agent, availableActions);
    }

    startMonitoring();
  }

  private void loadBasicMonitorModel(VacuumWorldMonitoringContainer container) {
    createBasicMonitor(container);
    
    List<VacuumWorldMonitorAgent> mas = container.getMonitorAgents();

    Set<Action> monitoringActions = new HashSet<Action>();
    monitoringActions.add(new TotalPerceptionAction());
    for (VacuumWorldMonitorAgent m : mas) {
      System.out.println("Starting monitor agent thread");
      startAgentThread(m, monitoringActions);
    }
  }

  private void createBasicMonitor(VacuumWorldMonitoringContainer container) {
    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    sensors.add(new VacuumWorldMonitorSensor());
    actuators.add(new VacuumWorldMonitorActuator());

    VacuumWorldMonitorAgent a = new VacuumWorldMonitorAgent(new DefaultAgentAppearance(null,
        null), sensors, actuators, new VacuumWorldMonitorMind(new VacuumWorldStepEvaluationStrategy()), new VacuumWorldMonitorBrain());
    container.addMonitorAgent(a);
  }

  private void loadEvaluatorObserverModel(
      VacuumWorldMonitoringContainer container) {
    CollectionRepresentation collectionRepresentation = new CollectionRepresentation(
        "Test", null);
    MongoBridge bridge = createDatabase(container, collectionRepresentation);
    //createObserver(container, bridge, collectionRepresentation);
    //TODO major hang bug when evaluator is added!!!! FIX !!!!
    createEvaluator(container, bridge, collectionRepresentation);
    
    List<VWObserverAgent> oas = container.getObserverAgents();
    List<VWEvaluatorAgent> eas = container.getEvaluatorAgents();

    Set<Action> observerActions = new HashSet<Action>();
    observerActions.add(new TotalPerceptionAction());
    for (VWObserverAgent oa : oas) {
      startAgentThread(oa, observerActions);
    }

    for (VWEvaluatorAgent ea : eas) {
      System.out.println("Starting evaluator agent thread");
      startAgentThread(ea, new HashSet<Action>());
    }
  }
  
  private MongoBridge createDatabase(VacuumWorldMonitoringContainer container,
      CollectionRepresentation collectionRepresentation) {
    MongoConnector connector = new MongoConnector();
    connector.connect("localhost", "27017", null, null);
    connector.setDatabase("Test");
    connector.dropCollection("Test");

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
        //Logger.getGlobal().log(Level.INFO, "Before Decide");
        monitorAfterDecide();
        
        //Logger.getGlobal().log(Level.INFO, "Before perceive");
        monitorAfterPerceive();
        
        //Logger.getGlobal().log(Level.INFO, "Before restart");
        restartThreads();
        
        //Logger.getGlobal().log(Level.INFO, "after restart");
        //cycleNumber++;
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
    	System.out.println("Threads after perceive: " + threadsAfterPerceive + "/" + runnableAgents.size());
      updateThreadsAfterPerceive();
      try {
		Thread.sleep(2000);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    }
    System.out.println("Threads after perceive: " + threadsAfterPerceive + "/" + runnableAgents.size());
    killThreads();
  }

  private void updateThreadsAfterPerceive() {
	  this.threadsAfterPerceive = 0;
    for (AgentRunnable agentRunnable : this.runnableAgents.values()) {
      //System.out.println(agentRunnable.getThreadState());
      
      
      
      if (agentRunnable.getThreadState() != ThreadState.AFTER_PERCEIVE) {
        this.threadsAfterPerceive = 0;
        break;
      } else {
        this.threadsAfterPerceive++;
      }
    }
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

  private void monitorAfterDecide() {
    this.threadsAfterDecide = 0;

    while (this.threadsAfterDecide < this.runnableAgents.size()) {
      System.out.println("Threads after decide: " + threadsAfterDecide + "/" + runnableAgents.size());
      updateThreadsAfterDecide();
    }
    System.out.println("Threads after decide: " + threadsAfterDecide + "/" + runnableAgents.size());
    printState();
    resumeThreads();
  }

  private void updateThreadsAfterDecide() {
    for (AgentRunnable agentRunnable : this.runnableAgents.values()) {
      if (agentRunnable.getThreadState() != ThreadState.AFTER_DECIDE) {
        this.threadsAfterDecide = 0;
        break;
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