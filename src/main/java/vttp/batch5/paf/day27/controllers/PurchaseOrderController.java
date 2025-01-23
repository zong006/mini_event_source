package vttp.batch5.paf.day27.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import vttp.batch5.paf.day27.models.PurchaseOrder;
import vttp.batch5.paf.day27.services.PurchaseOrderService;
import vttp.batch5.paf.day27.utils.Utils;

@Controller
@RequestMapping(path="/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class PurchaseOrderController {

  @Autowired
  private PurchaseOrderService poSvc;

  @PostMapping(path="/purchaseorder", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<String> postPurchaseOrder(@RequestBody String payload) throws Exception {

    PurchaseOrder po = Utils.toPurchaseOrder(payload);
    String poId = poSvc.createPurchaseOrder(po);

    System.out.printf(">> po: %s\n", po);
    
    if (poId!=null){
      // Returns the poId as JSON object
      JsonObject resp = Json.createObjectBuilder()
      .add("poId", poId)
      .build();

    return ResponseEntity.ok(resp.toString());
    }

    return ResponseEntity.badRequest().body("Failed to process transaction");
  }



}
