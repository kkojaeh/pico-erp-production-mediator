package pico.erp.production.mediator;

import java.util.Collections;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Cancel;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Create;
import pico.erp.production.order.ProductionOrderData;
import pico.erp.production.order.ProductionOrderId;
import pico.erp.production.order.ProductionOrderRequests;
import pico.erp.production.plan.detail.ProductionPlanDetailData;

/**
 * 주문 접수
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "productionPlanDetail")
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionOrderProductionPlanDetailMediator implements ProductionPlanDetailMediator {

  private static final long serialVersionUID = 1L;

  ProductionPlanDetailData productionPlanDetail;

  ProductionOrderData productionOrder;

  public Create.Response apply(
    ProductionPlanDetailMediatorMessages.Create.Request request) {
    val plan = request.getPlan();
    productionPlanDetail = request.getPlanDetail();
    val productionOrderId = ProductionOrderId.generate();
    productionOrder = ProductionOrderData.builder()
      .id(productionOrderId)
      .itemId(productionPlanDetail.getItemId())
      .processId(productionPlanDetail.getProcessId())
      .itemSpecCode(productionPlanDetail.getItemSpecCode())
      .quantity(productionPlanDetail.getQuantity())
      .spareQuantity(productionPlanDetail.getSpareQuantity())
      .unit(productionPlanDetail.getUnit())
      .projectId(plan.getProjectId())
      .dueDate(productionPlanDetail.getEndDate())
      .receiverId(productionPlanDetail.getReceiverId())
      .remark(null)
      .ordererId(plan.getPlannerId())
      .build();

    request.getContext().getProductionOrderService().create(
      ProductionOrderRequests.CreateRequest.from(productionOrder)
    );
    request.getContext().getProductionOrderService().commit(
      ProductionOrderRequests.CommitRequest.builder()
        .id(productionOrderId)
        .committerId(plan.getPlannerId())
        .build()
    );
    return new Create.Response(
      Collections.emptyList()
    );
  }

  @Override
  public Cancel.Response apply(Cancel.Request request) {
    request.getContext().getProductionOrderService().cancel(
      ProductionOrderRequests.CancelRequest.builder()
        .id(productionOrder.getId())
        .build()
    );
    return new Cancel.Response(
      Collections.emptyList()
    );
  }

  @Override
  public UUID getLinkedId() {
    return productionOrder.getId().getValue();
  }

  @Override
  public boolean isCancelable() {
    return productionOrder.isCancelable();
  }
}
