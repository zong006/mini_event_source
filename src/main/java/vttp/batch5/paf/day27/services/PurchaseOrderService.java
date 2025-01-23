package vttp.batch5.paf.day27.services;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import vttp.batch5.paf.day27.models.LineItem;
import vttp.batch5.paf.day27.models.PurchaseOrder;
import vttp.batch5.paf.day27.repositories.MongoRepo;
import vttp.batch5.paf.day27.repositories.RedisQueueRepo;
import vttp.batch5.paf.day27.repositories.SqlRepo;

@Service
public class PurchaseOrderService {

  @Autowired
  private SqlRepo sqlRepo;

  @Autowired
  private MongoRepo mongoRepo;

  @Autowired
  private RedisQueueRepo redisQueueRepo;

  public String createPurchaseOrder(PurchaseOrder po) throws Exception {
    String poId = UUID.randomUUID().toString().substring(0, 8);
    po.setPoId(poId);
    // for sql db
    if (!persistToDB(po)){
      return null;
    }
    // for mongo Event Store
    JsonObject poEvent = createPoEvent(po);
    JsonObject lineItemEvent = createLineItemsEvent(po.getLineItems(), poId);
    boolean persistedToEventStore = mongoRepo.createPurchaseOrderEvent(lineItemEvent) && mongoRepo.createPurchaseOrderEvent(poEvent);
    if (!persistedToEventStore){
      return null;
    }
    // similar code for redis queue
    if (!(pushEventToRedisQueue(lineItemEvent) && pushEventToRedisQueue(poEvent))){
      return null;
    }
    return poId;
  }

  @Transactional
  private boolean persistToDB(PurchaseOrder po) throws Exception{
    List<LineItem> lineItems = po.getLineItems();

    try {
      boolean poPersisted = sqlRepo.createPurchaseOrder(po);
      boolean lineItemsPersisted = sqlRepo.createLineItemOrder(lineItems, po.getPoId());

      return (poPersisted && lineItemsPersisted);

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to complete transaction");
    }
  }

  private JsonObject createPoEvent(PurchaseOrder po){
    Map<String, String> fields = new HashMap<>();
    fields.put("po_id", po.getPoId());
    fields.put("name", po.getName());
    fields.put("address", po.getAddress());
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    String dateString = sdf.format(po.getDeliveryDate());
    fields.put("delivery_date", dateString);

    System.out.println("delivery date string: " + dateString);
    List<Map<String, String>> fieldList = Arrays.asList(fields);
                        
    JsonObject event = eventBuilder("purchase_order", "insert", new Date(), fieldList);
    return event;
  }

  private JsonObject createLineItemsEvent(List<LineItem> lineItems, String poId){
    List<Map<String, String>> fieldList = new LinkedList<>();
    for (LineItem item : lineItems){
      Map<String, String> fields = new HashMap<>();
      fields.put("name", item.getName());
      fields.put("quantity", Integer.toString(item.getQuantity()));
      fields.put("unit_price", Float.toString(item.getUnitPrice()));
      fields.put("po_id", poId);
      fieldList.add(fields);
    }

    JsonObject event = eventBuilder("line_items", "insert", new Date(), fieldList);
   return event;
  }

  private boolean pushEventToRedisQueue(JsonObject event){
    return redisQueueRepo.pushEventToQueue(event);
  }


  private JsonObject eventBuilder(String tableName, String action, Date eventDate, List<Map<String,String>> fields){
    JsonArrayBuilder jab = Json.createArrayBuilder();
  
    for (Map<String, String> field : fields){
        JsonObjectBuilder fieldBuilder = Json.createObjectBuilder();

        for (Map.Entry<String, String> entry : field.entrySet()){
            fieldBuilder.add(entry.getKey(), entry.getValue());
        }
        JsonObject fieldJsonObject = fieldBuilder.build();
        jab.add(fieldJsonObject);
    }
    JsonArray fieldsArray = jab.build();

    JsonObjectBuilder job = Json.createObjectBuilder();
    JsonObject jsonData = job.add("tableName", tableName)
                                .add("action", action)
                                .add("eventDate", eventDate.toString())
                                .add("fields", fieldsArray)
                                .build();
    return jsonData;
  }


}
