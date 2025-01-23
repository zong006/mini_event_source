package vttp.batch5.paf.day27.repositories;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import vttp.batch5.paf.day27.models.LineItem;
import vttp.batch5.paf.day27.models.PurchaseOrder;

@Repository
public class SqlRepo {
    
    @Autowired
    private JdbcTemplate sqlTemplate;

    @Autowired 
    MongoRepo mongoRepo;


    public boolean createPurchaseOrder(PurchaseOrder po){

        int rowsUpdated = sqlTemplate.update(SqlQueries.SQL_CREATE_PURCHASE_ORDER,
                            po.getPoId(),
                            po.getName(),
                            po.getAddress(),
                            po.getDeliveryDate()
                            );
        return rowsUpdated==1;
    }

    public boolean createLineItemOrder(List<LineItem> lineItems, String poId){

        int rowsUpdated = 0;
        
        for (LineItem item : lineItems){
            rowsUpdated += sqlTemplate.update(SqlQueries.SQL_CREATE_LINE_ITEM,
                                            item.getName(),
                                            item.getQuantity(),
                                            item.getUnitPrice(),
                                            poId
                                            );
        }

        return rowsUpdated==lineItems.size();
    }

    
}
