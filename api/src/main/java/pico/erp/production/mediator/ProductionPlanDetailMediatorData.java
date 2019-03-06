package pico.erp.production.mediator;

import java.util.UUID;
import lombok.Data;
import pico.erp.production.plan.detail.ProductionPlanDetailId;

@Data
public class ProductionPlanDetailMediatorData {

  ProductionPlanDetailId id;

  UUID linkedId;

}
