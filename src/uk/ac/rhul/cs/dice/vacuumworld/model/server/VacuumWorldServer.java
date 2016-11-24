package uk.ac.rhul.cs.dice.vacuumworld.model.server;

import java.net.ServerSocket;
import java.util.Map;
import java.util.Set;

import uk.ac.rhul.cs.dice.vacuumworld.VacuumWorldClientListener;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldUniverse;

public class VacuumWorldServer {
    private ServerSocket serverSocket;
    private Set<Client> connectedClients;
    private Map<String, VacuumWorldClientListener> clientsListeners;
    private Map<String, VacuumWorldUniverse> activeUniverses;
    
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
}