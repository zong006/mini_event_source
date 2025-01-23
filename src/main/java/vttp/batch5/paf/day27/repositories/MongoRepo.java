package vttp.batch5.paf.day27.repositories;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonObject;

@Repository
public class MongoRepo {
    
    @Autowired
    private MongoTemplate mongoTemplate;

    // insert event document
    public boolean createPurchaseOrderEvent(JsonObject event){
        Document d = Document.parse(event.toString());
        try {
            mongoTemplate.insert(d, MongoConstants.MONGO_C_NAME);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
