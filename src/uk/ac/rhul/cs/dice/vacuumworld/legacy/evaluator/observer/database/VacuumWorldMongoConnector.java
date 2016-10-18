package uk.ac.rhul.cs.dice.vacuumworld.legacy.evaluator.observer.database;

import static com.mongodb.client.model.Projections.slice;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.monitor.mongo.MongoConnector;
import uk.ac.rhul.cs.dice.vacuumworld.utils.VWUtils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;

/**
 * An extension of {@link MongoConnector} providing extra functionality that is
 * more specific to Vacuum World. This class tries to perform an efficient read
 * from a mongodb. <br>
 * NOTES: There is no way (although apparently it is planned) to select from an
 * array only elements that match a condition in mongodb. This means that
 * getting the list of relevant speech actions is done quite inefficiently.
 * There may be a work around but it involves a restructuring of the current
 * implementation of the mongodb.
 * 
 * @author Ben WilkinsS
 *
 */
public class VacuumWorldMongoConnector extends MongoConnector {

	public List<String> efficentAgentRead(CollectionRepresentation collectionRepresentation, int lastCycleRead) {
		MongoCollection<Document> collection = this.database.getCollection(collectionRepresentation.getCollectionName());
		MongoIterable<Document> iter = collection.find().projection(slice("cycleList", -((VWUtils.getCycleNumber() + 1) - lastCycleRead)));
		List<String> jsons = new ArrayList<>();
		
		for(Document document : iter) {
			jsons.add(document.toJson());
		}
		
		return jsons;
	}
}