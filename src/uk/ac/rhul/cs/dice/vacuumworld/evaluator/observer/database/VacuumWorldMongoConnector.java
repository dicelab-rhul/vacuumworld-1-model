package uk.ac.rhul.cs.dice.vacuumworld.evaluator.observer.database;

import static com.mongodb.client.model.Projections.slice;

import java.util.ArrayList;

import org.bson.Document;

import uk.ac.rhul.cs.dice.monitor.mongo.CollectionRepresentation;
import uk.ac.rhul.cs.dice.monitor.mongo.MongoConnector;
import uk.ac.rhul.cs.dice.vacuumworld.utils.Utils;

import com.mongodb.Block;
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

  public ArrayList<String> efficentAgentRead(CollectionRepresentation collectionRepresentation,
      int lastCycleRead) {
    MongoCollection<Document> collection = database
        .getCollection(collectionRepresentation.getCollectionName());
    MongoIterable<Document> iter = collection.find().projection(
        slice("cycleList", -((Utils.getCycleNumber() + 1) - lastCycleRead)));
    ArrayList<String> jsons = new ArrayList<>();
    iter.forEach(new Block<Document>() {
      @Override
      public void apply(Document d) {
        jsons.add(d.toJson());
      }
    });
    return jsons;
  }
}
