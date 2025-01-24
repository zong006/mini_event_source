package vttp.batch5.paf.day27.repositories;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MongoRepo {
    
    @Autowired
    private MongoTemplate mongoTemplate;

    // insert event document
    public boolean createPurchaseOrderEvent(Document event){
        try {
            mongoTemplate.insert(event, MongoConstants.MONGO_C_NAME);
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
