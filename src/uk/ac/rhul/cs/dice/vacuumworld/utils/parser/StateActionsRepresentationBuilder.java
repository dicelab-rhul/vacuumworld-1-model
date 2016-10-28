package uk.ac.rhul.cs.dice.vacuumworld.utils.parser;

import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import uk.ac.rhul.cs.dice.vacuumworld.actions.result.report.AbstractActionReport;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.report.CleanActionReport;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.report.DropDirtActionReport;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.report.SpeechActionReport;
import uk.ac.rhul.cs.dice.vacuumworld.actions.result.report.TurnActionReport;
import uk.ac.rhul.cs.dice.vacuumworld.environment.VacuumWorldCoordinates;

public class StateActionsRepresentationBuilder {
	private StateActionsRepresentationBuilder() {}
	
	public static JsonObject buildStateActionsRepresentation(Map<String, AbstractActionReport> actionsMap, int cycle) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("cycle", cycle);
		builder.add("actors_number", actionsMap.size());
		builder.add("actions", buildActions(actionsMap));
		
		return builder.build();
	}

	private static JsonArray buildActions(Map<String, AbstractActionReport> actionsMap) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		actionsMap.forEach((key, value) -> builder.add(buildAction(key, value)));
		
		return builder.build();
	}

	private static JsonObject buildAction(String actorId, AbstractActionReport actionReport) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("actor_id", actorId);
		builder.add("action", actionReport.getAction().getCanonicalName());
		builder.add("outcome", actionReport.getActionResult().toString());
		builder.add("actor_old_facing_direction", actionReport.getActorOldDirection().toString());
		builder.add("actor_new_facing_direction", actionReport.getActorNewDirection().toString());
		builder.add("actor_old_location", actionReport.getActorOldCoordinates().toString());
		builder.add("actor_new_location", actionReport.getActorNewCoordinates().toString());
		
		addAdditionalParametersIfNecessary(builder, actionReport);
		builder.add("perceived_locations", buildPerceivedLocations(actionReport.getPerceptionKeys()));
		
		return builder.build();
	}

	private static JsonArray buildPerceivedLocations(Set<VacuumWorldCoordinates> perceptionKeys) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		perceptionKeys.stream().map(coordinates -> coordinates.toString()).forEach(builder::add);
		
		return builder.build();
	}

	private static void addAdditionalParametersIfNecessary(JsonObjectBuilder builder, AbstractActionReport actionReport) {
		if(actionReport instanceof TurnActionReport) {
			builder.add("turning_direction", ((TurnActionReport) actionReport).getTurningDirection().toString());
		}
		else if(actionReport instanceof CleanActionReport) {
			builder.add("cleaned_dirt", ((CleanActionReport) actionReport).getCleanedDirt() == null ? null : ((CleanActionReport) actionReport).getCleanedDirt().toString());
		}
		else if(actionReport instanceof DropDirtActionReport) {
			builder.add("dropped_dirt", ((DropDirtActionReport) actionReport).getDroppedDirtType() == null ? null :((DropDirtActionReport) actionReport).getDroppedDirtType().toString());
		}
		else if(actionReport instanceof SpeechActionReport) {
			builder.add("speech", buildSpeech(((SpeechActionReport) actionReport).getSpeeches()));
		}
	}

	private static JsonArray buildSpeech(Map<String, String> speeches) {
		JsonArrayBuilder builder = Json.createArrayBuilder();
		speeches.forEach((key, value) -> builder.add(buildMessage(key, value)));
		
		return builder.build();
	}

	private static JsonObject buildMessage(String recipientId, String payload) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		
		builder.add("recipient", recipientId);
		builder.add("message", payload);
		
		return builder.build();
	}
}