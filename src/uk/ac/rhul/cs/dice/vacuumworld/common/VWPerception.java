package uk.ac.rhul.cs.dice.vacuumworld.common;

import java.util.List;
import java.util.Map;

import uk.ac.rhul.cs.dice.gawl.interfaces.entities.agents.AbstractAgent;
import uk.ac.rhul.cs.dice.gawl.interfaces.perception.Perception;
import uk.ac.rhul.cs.dice.vacuumworld.actions.MoveAction;
import uk.ac.rhul.cs.dice.vacuumworld.agents.ActorFacingDirection;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldActuatorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldAgentType;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldCleaningAgent;
import uk.ac.rhul.cs.dice.vacuumworld.agents.VacuumWorldSensorRole;
import uk.ac.rhul.cs.dice.vacuumworld.agents.user.User;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.Dirt;
import uk.ac.rhul.cs.dice.vacuumworld.dirt.DirtType;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldLocation;

/**
 * 
 * This interface exposes an API to access various elements in the {@link Perception}.<br/><br/>
 * Known implementations: {@link VacuumWorldPerception}.
 * 
 * @author cloudstrife9999, a.k.a. Emanuele Uliana.
 *
 */
public interface VWPerception extends Perception {
    /**
     * 
     * Returns the size of the perceived grid. It must be 6, 4, 3, 2 or 1.
     * 
     * @return the size of the perceived grid.
     * 
     */
    public abstract int getPerceptionSize();
    
    /**
     * 
     * Returns the perceived map. This is useful only if you need something which you cannot easily get from the other methods of this class. 
     * 
     * @return the perceived map as a {@link Map} from {@link VacuumWorldCoordinates} to {@link VacuumWorldLocation}.
     * 
     */
    public abstract Map<VacuumWorldCoordinates, VacuumWorldLocation> getPerceivedMap();
    
    /**
     * 
     * Returns a {@link List} of the {@link VacuumWorldCoordinates} keys contained in the perceived grid.
     * 
     * @return the {@link List} of the {@link VacuumWorldCoordinates} keys contained in the perceived grid.
     * 
     */
    public abstract List<VacuumWorldCoordinates> getCoordinatesInPerceptionList();
    
    /**
     * 
     * Returns a {@link List} if the {@link VacuumWorldLocation} values contained in the perceived grid.
     * 
     * @return the {@link List} if the {@link VacuumWorldLocation} values contained in the perceived grid.
     * 
     */
    public abstract List<VacuumWorldLocation> getLocationsInPerceptionList();
    
    /**
     * 
     * Returns a specific {@link VacuumWorldLocation} in the perceived grid.
     * 
     * @param coordinates the {@link VacuumWorldCoordinates} key mapped to the location to return.
     * 
     * @return the {@link VacuumWorldLocation} value linked to the key passed as parameter to this method or null if it does not exist.
     * 
     */
    public abstract VacuumWorldLocation getSpecificPerceivedLocation(VacuumWorldCoordinates coordinates);
    
    /**
     * 
     * Returns the current actor {@link VacuumWorldCoordinates}.
     * 
     * @return the current actor {@link VacuumWorldCoordinates}.
     * 
     */
    public abstract VacuumWorldCoordinates getActorCoordinates();
    
    /**
     * 
     * Returns the current actor {@link VacuumWorldLocation}.
     * 
     * @return the current actor {@link VacuumWorldLocation}.
     * 
     */
    public abstract VacuumWorldLocation getCurrentActorLocation();
    
    /**
     * 
     * Returns the current actor {@link ActorFacingDirection}.
     * 
     * @return the current actor {@link ActorFacingDirection}.
     * 
     */
    public abstract ActorFacingDirection getActorCurrentFacingDirection();
    
    /**
     * 
     * Returns whether there is {@link Dirt} on the current actor location.
     * 
     * @return true if there is {@link Dirt} on the current actor location, false otherwise.
     * 
     */
    public abstract boolean isDirtOnCurrentActorLocation();
    
    /**
     * 
     * Returns the current actor color if the current actor is an agent.
     * 
     * @return the current actor color if the current actor is an agent, null otherwise.
     *
     */
    public abstract VacuumWorldAgentType getCurrentActorColorIfAgent();
    
    /**
     * 
     * Returns whether there is compatible (i.e., of a color which allows cleaning by the current actor) dirt within the perceived grid.
     * 
     * @return true if there is compatible dirt within the perceived grid, false otherwise.
     * 
     */
    public abstract boolean canCurrentActorSpotCompatibleDirt();
    
    /**
     * 
     * Returns whether the current actor can attempt a successful cleaning action on its current location (which means the actor is a cleaning agent and there is compatible dirt on its current location).
     * 
     * @return true the current actor can attempt a successful cleaning action on its current location, false otherwise.
     * 
     */
    public abstract boolean canCurrentActorCleanOnHisCurrentLocation();
    
    /**
     * 
     * Returns a {@link List} of {@link VacuumWorldLocation} values in the perceived grid where each location contains compatible dirt with the current actor. An empty list can be returned.
     * 
     * @return the {@link List} of {@link VacuumWorldLocation} values in the perceived grid where each location contains compatible dirt with the current actor.
     * 
     */
    public abstract List<VacuumWorldLocation> getLocationsWithDirtCompatibleWithCurrentActor();
    
    /**
     * 
     * Returns a {@link List} of {@link VacuumWorldCoordinates} keys in the perceived grid where each mapped location contains compatible dirt with the current actor. An empty list can be returned.
     * 
     * @return the {@link List} of {@link VacuumWorldCoordinates} keys in the perceived grid where each mapped location contains compatible dirt with the current actor.
     * 
     */
    public abstract List<VacuumWorldCoordinates> getCoordinatesWithDirtCompatibleWithCurrentActor();
    
    /**
     * 
     * Returns a {@link List} of {@link VacuumWorldLocation} values in the perceived grid where each location contains a piece of dirt. An empty list can be returned.
     * 
     * @return the {@link List} of {@link VacuumWorldLocation} values in the perceived grid where each location contains a piece of dirt.
     * 
     */
    public abstract List<VacuumWorldLocation> getLocationsWithDirtInPerception();
    
    /**
     * 
     * Returns a {@link List} of {@link VacuumWorldCoordinates} keys in the perceived grid where each mapped location contains a piece of dirt. An empty list can be returned.
     * 
     * @return the {@link List} of {@link VacuumWorldCoordinates} keys in the perceived grid where each mapped location contains a piece of dirt.
     * 
     */
    public abstract List<VacuumWorldCoordinates> getCoordinatesWithDirtInPerception();
    
    /**
     * 
     * Returns a {@link List} of actors in the perceived grid. The current actor is not included. An empty list can be returned.
     * 
     * @return the {@link List} of actors in the perceived grid. The current actor is not included.
     * 
     */
    public abstract List<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>> getActorsInPerception();
    
    /**
     * 
     * Returns a {@link List} of actors in the perceived grid, including the current actor.
     * 
     * @return the {@link List} of actors in the perceived grid, including the current actor.
     * 
     */
    public abstract List<AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole>> getActorsInPerceptionIncludingSelf();
    
    /**
     * 
     * Returns a {@link List} of the IDs of the actors in the perceived grid. The current actor ID is not included. An empty list can be returned.
     * 
     * @return the {@link List} of the IDs of the actors in the perceived grid. The current actor ID is not included.
     * 
     */
    public abstract List<String> getActorsIdsInPerception();
    
    /**
     * 
     * Returns a {@link List} of the IDs of the actors in the perceived grid, including the current actor ID.
     *  
     * @return the {@link List} of the IDs of the actors in the perceived grid, including the current actor ID.
     * 
     */
    public abstract List<String> getActorsIdsInPerceptionIncludingSelf();
    
    /**
     * 
     * Returns a {@link List} of the green agents in the perceived grid. The current actor is not included even if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the green agents in the perceived grid. The current actor is not included even if the color matches.
     * 
     */
    public abstract List<VacuumWorldCleaningAgent> getGreenAgentsInPerception();
    
    /**
     * 
     * Returns a {@link List} of the green agents in the perceived grid, including the current actor if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the green agents in the perceived grid, including the current actor if the color matches.
     * 
     */
    public abstract List<VacuumWorldCleaningAgent> getGreenAgentsInPerceptionIncludingSelfIfApplicable();
    
    /**
     * 
     * Returns a {@link List} of the IDs of the green agents in the perceived grid. The current actor ID is not included even if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the IDs of the green agents in the perceived grid. The current actor ID is not included even if the color matches.
     * 
     */
    public abstract List<String> getGreenAgentsIdsInPerception();
    
    /**
     * 
     * Returns a {@link List} of the IDs of the green agents in the perceived grid, including the current actor ID if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the IDs of the green agents in the perceived grid, including the current actor ID if the color matches.
     * 
     */
    public abstract List<String> getGreenAgentsIdsInPerceptionIncludingSelfIfApplicable();
    
    /**
     * 
     * Returns a {@link List} of the orange agents in the perceived grid. The current actor is not included even if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the orange agents in the perceived grid. The current actor is not included even if the color matches.
     * 
     */
    public abstract List<VacuumWorldCleaningAgent> getOrangeAgentsInPerception();
    
    /**
     * 
     * Returns a {@link List} of the orange agents in the perceived grid, including the current actor if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the orange agents in the perceived grid, including the current actor if the color matches.
     * 
     */
    public abstract List<VacuumWorldCleaningAgent> getOrangeAgentsInPerceptionIncludingSelfIfApplicable();
    
    /**
     * 
     * Returns a {@link List} of the IDs of the orange agents in the perceived grid. The current actor ID is not included even if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the IDs of the orange agents in the perceived grid. The current actor ID is not included even if the color matches.
     * 
     */
    public abstract List<String> getOrangeAgentsIdsInPerception();
    
    /**
     * 
     * Returns a {@link List} of the IDs of the orange agents in the perceived grid, including the current actor ID if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the IDs of the orange agents in the perceived grid, including the current actor ID if the color matches.
     * 
     */
    public abstract List<String> getOrangeAgentsIdsInPerceptionIncludingSelfIfApplicable();
    
    /**
     * 
     * Returns a {@link List} of the white agents in the perceived grid. The current actor is not included even if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the white agents in the perceived grid. The current actor is not included even if the color matches.
     * 
     */
    public abstract List<VacuumWorldCleaningAgent> getWhiteAgentsInPerception();
    
    /**
     * 
     * Returns a {@link List} of the white agents in the perceived grid, including the current actor if the color matches.  The list can be empty.
     * 
     * @return the {@link List} of the white agents in the perceived grid, including the current actor if the color matches.
     * 
     */
    public abstract List<VacuumWorldCleaningAgent> getWhiteAgentsInPerceptionIncludingSelfIfApplicable();
    
    /**
     * 
     * Returns a {@link List} of the IDs of the white agents in the perceived grid. The current actor ID is not included even if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the IDs of the white agents in the perceived grid. The current actor ID is not included even if the color matches.
     * 
     */
    public abstract List<String> getWhiteAgentsIdsInPerception();
    
    /**
     * 
     * Returns a {@link List} of the IDs of the white agents in the perceived grid, including the current actor ID if the color matches. The list can be empty.
     * 
     * @return the {@link List} of the IDs of the white agents in the perceived grid, including the current actor ID if the color matches.
     * 
     */
    public abstract List<String> getWhiteAgentsIdsInPerceptionIncludingSelfIfApplicable();
    
    /**
     * 
     * Return the {@link User}, if it exists and if it is in the perception.
     * 
     * @return the {@link User}, if it exists and if it is in the perception, null otherwise.
     * 
     */
    public abstract User getUserInPerceptionIfPresent();
    
    /**
     * 
     * Return the {@link User} ID, if the user exists and if it is in the perception.
     * 
     * @return the {@link User} ID, if the user exists and if it is in the perception, null otherwise.
     * 
     */
    public abstract String getUserIdInPerceptionIfPresent();
    
    /**
     * 
     * Returns whether the current actor is a green agent or not.
     * 
     * @return true if the current actor is a green agent, false otherwise.
     * 
     */
    public abstract boolean isCurrentActorAGreenAgent();
    
    /**
     * 
     * Returns whether the current actor is an orange agent or not.
     * 
     * @return true if the current actor is an orange agent, false otherwise.
     * 
     */
    public abstract boolean isCurrentActorAnOrangeAgent();
    
    /**
     * 
     * Returns whether the current actor is a white agent or not.
     * 
     * @return true if the current actor is a white agent, false otherwise.
     * 
     */
    public abstract boolean isCurrentActorAWhiteAgent();
    
    /**
     * 
     * Returns whether the current actor is a user or not.
     * 
     * @return true if the current actor is a user, false otherwise.
     * 
     */
    public abstract boolean isCurrentActorAUser();
    
    /**
     * 
     * Returns the current actor.
     * 
     * @return the current actor.
     * 
     */
    public abstract AbstractAgent<VacuumWorldSensorRole, VacuumWorldActuatorRole> getCurentActor();
    
    /**
     * 
     * Returns the current actor id.
     * 
     * @return the current actor id.
     * 
     */
    public abstract String getCurentActorId();
    
    /**
     * 
     * Returns a {@link List} of {@link Dirt} pieces (of any color) in the perceived grid. An empty list can be returned.
     * 
     * @return the {@link List} of {@link Dirt} pieces (of any color) in the perceived grid.
     * 
     */
    public abstract List<Dirt> getDirtsInPerception();
    
    /**
     * 
     * Returns a {@link List} of {@link Dirt} pieces (of the color specified by the parameter) in the perceived grid. An empty list can be returned.
     * 
     * @param type the {@link DirtType} of all the returned pieces of {@link Dirt}.
     * @return the {@link List} of {@link Dirt} pieces (of the color specified by the parameter) in the perceived grid.
     * 
     */
    public abstract List<Dirt> getDirtsOfSpecificTypeInPerception(DirtType type);
    
    /**
     * 
     * Returns a {@link List} of {@link Dirt} pieces compatible with the current actor in the perceived grid. An empty list can be returned.
     * 
     * @return the {@link List} of {@link Dirt} pieces compatible with the current actor in the perceived grid.
     * 
     */
    public abstract List<Dirt> getCompatibleDirtsInPerception();
    
    /**
     * 
     * Returns the number of pieces of {@link Dirt} (of any color) in the perceived grid. An empty list can be returned.
     * 
     * @return the number of pieces of {@link Dirt} (of any color) in the perceived grid.
     * 
     */
    public abstract int countDirtsInPerception();
    
    /**
     * 
     * Returns the number of pieces of {@link Dirt} (of the color specified by the parameter) in the perceived grid.
     * 
     * @return the number of pieces of {@link Dirt} (of the color specified by the parameter) in the perceived grid.
     * 
     */
    public abstract int countDirtsOfSpecificTypeInPerception(DirtType type);
    
    /**
     * 
     * Returns the number of pieces of {@link Dirt} compatible with the current actor in the perceived grid.
     * 
     * @return the number of pieces of {@link Dirt} compatible with the current actor in the perceived grid.
     * 
     */
    public abstract int countCompatibleDirtsInPerception();
    
    /**
     * 
     * Returns whether the location in front of the current actor location exists or not, which means, is the actor immediately in front of a wall or not?
     * 
     * @return true if the current actor is immediately in front of a wall, false otherwise.
     * 
     */
    public abstract boolean doesCurrentActorHaveWallInFront();
    
    /**
     * 
     * Returns whether the location on the left of the current actor location (w.r.t. the current actor perspective) exists or not, which means, has the actor a wall on its immediate left or not?
     * 
     * @return true if the current actor has a wall on its immediate left, false otherwise.
     * 
     */
    public abstract boolean doesCurrentActorHaveWallOnHisLeft();
    
    /**
     * 
     * Returns whether the location on the right of the current actor location (w.r.t. the current actor perspective) exists or not, which means, has the actor a wall on its immediate right or not?
     * 
     * @return true if the current actor has a wall on its immediate right, false otherwise.
     * 
     */
    public abstract boolean doesCurrentActorHaveWallOnHisRight();
    
    /**
     * 
     * Returns whether the location on the back of the current actor location (w.r.t. the current actor perspective) exists or not, which means, has the actor a wall on its immediate back or not?
     * 
     * @return true if the current actor has a wall on its immediate back, false otherwise.
     * 
     */
    public abstract boolean doesCurrentActorHaveWallOnHisBack();
    
    /**
     * 
     * Returns whether the location on the North of the current actor location (w.r.t. the external perspective) exists or not, which means, is there a wall on the immediate North of the actor or not?
     * 
     * @return true if there is a wall on the immediate North of the actor, false otherwise.
     * 
     */
    public abstract boolean doesCurrentActorHaveWallOnNorth();
    
    /**
     * 
     * Returns whether the location on the South of the current actor location (w.r.t. the external perspective) exists or not, which means, is there a wall on the immediate South of the actor or not?
     * 
     * @return true if there is a wall on the immediate South of the actor, false otherwise.
     * 
     */
    public abstract boolean doesCurrentActorHaveWallOnSouth();
    
    /**
     * 
     * Returns whether the location on the West of the current actor location (w.r.t. the external perspective) exists or not, which means, is there a wall on the immediate West of the actor or not?
     * 
     * @return true if there is a wall on the immediate West of the actor, false otherwise.
     * 
     */
    public abstract boolean doesCurrentActorHaveWallOnWest();
    
    /**
     * 
     * Returns whether the location on the East of the current actor location (w.r.t. the external perspective) exists or not, which means, is there a wall on the immediate East of the actor or not?
     * 
     * @return true if there is a wall on the immediate North of the actor, false otherwise.
     * 
     */
    public abstract boolean doesCurrentActorHaveWallOnEast();
    
    /**
     * 
     * Returns whether the farthest location in front of the current actor (within the perception) has a wall ahead of it or not.
     * 
     * @return true if the farthest location in front of the current actor (within the perception) has a wall ahead of it, false otherwise.
     * 
     */
    public abstract boolean isWallStraightAtTheEndOfPerception();
    
    /**
     * 
     * Returns the number of {@link MoveAction}s which can be safely performed without bumping into a wall, according to the perception. Note that the result is a lower bound for the actual number of
     * move actions which can be safely performed on the grid, and can be strictly less that the real value if the perception range is limited. 
     * 
     * @return a lower bound (which depends on the perception range) for the number of {@link MoveAction}s which can be safely performed without bumping into a wall.
     * 
     */
    public abstract int countNumberOfSafeMoveActionsWithinPerception();
}