package pico.erp.production.mediator;

import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import pico.erp.production.plan.detail.ProductionPlanDetailId;

public interface ProductionPlanMediatorService {

  void cancel(@Valid @NotNull ProductionPlanMediatorRequests.CancelRequest request);

  void complete(@Valid @NotNull ProductionPlanMediatorRequests.CompleteRequest request);

  void create(@Valid @NotNull ProductionPlanMediatorRequests.CreateRequest request);

  boolean exists(@Valid @NotNull UUID linkedId);

  ProductionPlanDetailMediatorData get(@Valid @NotNull ProductionPlanDetailId id);

  ProductionPlanDetailMediatorData get(@Valid @NotNull UUID linkedId);

  void progress(@Valid @NotNull ProductionPlanMediatorRequests.ProgressRequest request);

  void recreate(@Valid @NotNull ProductionPlanMediatorRequests.RecreateRequest request);

}
