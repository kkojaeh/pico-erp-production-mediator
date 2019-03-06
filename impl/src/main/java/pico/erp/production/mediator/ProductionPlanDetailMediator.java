package pico.erp.production.mediator;

import java.io.Serializable;
import java.util.Collections;
import java.util.UUID;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Cancel;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Complete;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Create;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Progress;
import pico.erp.production.plan.detail.ProductionPlanDetailData;
import pico.erp.production.plan.detail.ProductionPlanDetailRequests;

public interface ProductionPlanDetailMediator extends Serializable {

  Create.Response apply(Create.Request request);

  Cancel.Response apply(Cancel.Request request);

  default Progress.Response apply(Progress.Request request) {
    request.getContext().getProductionPlanDetailService().progress(
      ProductionPlanDetailRequests.ProgressRequest.builder()
        .id(getProductionPlanDetail().getId())
        .progressedQuantity(request.getProgressQuantity())
        .build()
    );
    return new Progress.Response(
      Collections.emptyList()
    );
  }

  default Complete.Response apply(Complete.Request request) {
    request.getContext().getProductionPlanDetailService().complete(
      ProductionPlanDetailRequests.CompleteRequest.builder()
        .id(getProductionPlanDetail().getId())
        .build()
    );
    return new Complete.Response(
      Collections.emptyList()
    );
  }


  UUID getLinkedId();

  ProductionPlanDetailData getProductionPlanDetail();

  boolean isCancelable();

}
