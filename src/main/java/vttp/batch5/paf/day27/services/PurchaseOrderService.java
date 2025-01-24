package vttp.batch5.paf.day27.services;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    System.out.println(">>>>> saved to sql!");
    // for mongo Event Store
    Document poEvent = createPoEvent(po);
    Document lineItemEvent = createLineItemsEvent(po.getLineItems(), poId);
    boolean persistedToEventStore = mongoRepo.createPurchaseOrderEvent(lineItemEvent) && mongoRepo.createPurchaseOrderEvent(poEvent);
    if (!persistedToEventStore){
      return null;
    }
    System.out.println(">>>> saved to mongo!");
    // similar code for redis queue
    boolean pushedToRedis = redisQueueRepo.pushEventToQueue(lineItemEvent) && redisQueueRepo.pushEventToQueue(poEvent);
    if (!(pushedToRedis)){
      return null;
    }
    System.out.println(">>>> saved to redis!");
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

  private Document createPoEvent(PurchaseOrder po){
    Map<String, String> fields = new HashMap<>();
    fields.put("po_id", po.getPoId());
    fields.put("name", po.getName());
    fields.put("address", po.getAddress());
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
    String dateString = sdf.format(po.getDeliveryDate());
    fields.put("delivery_date", dateString);

    List<Map<String, String>> fieldList = Arrays.asList(fields);
                        
    Document event = eventBuilder("purchase_order", "insert", new Date(), fieldList);
    return event;
  }

  private Document createLineItemsEvent(List<LineItem> lineItems, String poId){
    List<Map<String, String>> fieldList = new LinkedList<>();
    for (LineItem item : lineItems){
      Map<String, String> fields = new HashMap<>();
      fields.put("name", item.getName());
      fields.put("quantity", Integer.toString(item.getQuantity()));
      fields.put("unit_price", Float.toString(item.getUnitPrice()));
      fields.put("po_id", poId);
      fieldList.add(fields);
    }

    Document event = eventBuilder("line_items", "insert", new Date(), fieldList);
    return event;
  }

  private Document eventBuilder(String tableName, String action, Date eventDate, List<Map<String,String>> fields){
    // parse list of fields to a document array
    List<Document> fieldsDocArray = new LinkedList<>();
    for (Map<String, String> field : fields){
      Document entry = new Document(field);
      fieldsDocArray.add(entry);
    }

    Document event = new Document();
    String eventId = UUID.randomUUID().toString().substring(0, 12);

    event.put("eventId", eventId);
    event.put("tableName", tableName);
    event.put("action", action);
    event.put("eventDate", eventDate);
    event.put("fields", fieldsDocArray);

    return event;
  }


}
