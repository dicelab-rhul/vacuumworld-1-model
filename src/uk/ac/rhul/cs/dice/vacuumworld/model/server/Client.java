package uk.ac.rhul.cs.dice.vacuumworld.model.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

public class Client {
    private String id;
    private String ip;
    private int clientRemotePort;
    private int clientLocalPort;
    private Socket clientSocket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    
    public Client(Socket clientSocket) throws IOException {
	init(clientSocket, null, null);
    }
    
    public Client(Socket clientSocket, ObjectOutputStream output, ObjectInputStream input) {
	try {
	    init(clientSocket, output, input);
	}
	catch(Exception e) {
	    throw new IllegalArgumentException(e);
	}
    }
    
    private void init(Socket clientSocket, ObjectOutputStream output, ObjectInputStream input) throws IOException {
	this.id = "Client-" + UUID.randomUUID().toString();
	this.clientSocket = clientSocket;
	this.output = output == null ? new ObjectOutputStream(clientSocket.getOutputStream()) : output;
	this.input = input == null ? new ObjectInputStream(clientSocket.getInputStream()) : input;
	this.ip = clientSocket.getInetAddress().getHostAddress();
	this.clientLocalPort = clientSocket.getLocalPort();
	this.clientRemotePort = clientSocket.getPort();
    }

    public String getId() {
        return this.id;
    }
    
    public String getIp() {
        return this.ip;
    }

    public int getClientRemotePort() {
        return this.clientRemotePort;
    }

    public int getClientLocalPort() {
        return this.clientLocalPort;
    }

    public Socket getClientSocket() {
        return this.clientSocket;
    }

    public ObjectOutputStream getOutput() {
        return this.output;
    }

    public ObjectInputStream getInput() {
        return this.input;
    }
    
    public void disconnect() {
	closeOutputStream();
	closeInputStream();
	closeSocket();
	reset();
    }

    private void reset() {
	this.ip = null;
	this.clientLocalPort = -1;
	this.clientRemotePort = -1;
	this.clientSocket = null;
	this.output = null;
	this.input = null;
    }

    private void closeSocket() {
	try {
	    this.clientSocket.close();
	}
	catch(Exception e) {
	    VWUtils.fakeLog(e);
	}
    }

    private void closeInputStream() {
	try {
	    this.input.close();
	}
	catch(Exception e) {
	    VWUtils.fakeLog(e);
	}
    }

    private void closeOutputStream() {
	try {
	    this.output.close();
	}
	catch(Exception e) {
	    VWUtils.fakeLog(e);
	}
    }
}