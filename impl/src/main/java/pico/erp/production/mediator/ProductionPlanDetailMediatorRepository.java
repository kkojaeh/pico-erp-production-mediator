package pico.erp.production.mediator;

import java.util.Optional;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;
import pico.erp.production.plan.detail.ProductionPlanDetailId;

@Repository
public interface ProductionPlanDetailMediatorRepository {

  ProductionPlanDetailMediator create(@NotNull ProductionPlanDetailMediator mediator);

  void deleteBy(@NotNull ProductionPlanDetailId id);

  boolean exists(@NotNull ProductionPlanDetailId id);

  boolean exists(@NotNull UUID linkedId);

  Optional<ProductionPlanDetailMediator> findBy(@NotNull ProductionPlanDetailId id);

  Optional<ProductionPlanDetailMediator> findBy(@NotNull UUID linkedId);

  void update(@NotNull ProductionPlanDetailMediator mediator);

}
