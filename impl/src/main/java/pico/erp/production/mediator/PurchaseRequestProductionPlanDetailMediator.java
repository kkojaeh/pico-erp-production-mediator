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
import pico.erp.production.plan.detail.ProductionPlanDetailData;
import pico.erp.purchase.request.PurchaseRequestData;
import pico.erp.purchase.request.PurchaseRequestId;
import pico.erp.purchase.request.PurchaseRequestRequests;

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
public class PurchaseRequestProductionPlanDetailMediator implements ProductionPlanDetailMediator {

  private static final long serialVersionUID = 1L;

  ProductionPlanDetailData productionPlanDetail;

  PurchaseRequestData purchaseRequest;

  @Override
  public ProductionPlanDetailMediatorMessages.Create.Response apply(
    ProductionPlanDetailMediatorMessages.Create.Request request) {
    val plan = request.getPlan();
    val detail = request.getPlanDetail();
    productionPlanDetail = detail;
    val purchaseRequestId = PurchaseRequestId.generate();
    purchaseRequest = PurchaseRequestData.builder()
      .id(purchaseRequestId)
      .itemId(detail.getItemId())
      .itemSpecId(detail.getItemSpecId())
      .itemSpecCode(detail.getItemSpecCode())
      .quantity(detail.getPlannedQuantity())
      .unit(detail.getUnit())
      .projectId(plan.getProjectId())
      .dueDate(detail.getEndDate())
      .supplierId(detail.getActorId())
      .receiverId(detail.getReceiverId())
      .remark(null)
      .requesterId(plan.getPlannerId())
      .build();
    request.getContext().getPurchaseRequestService().create(
      PurchaseRequestRequests.CreateRequest.from(purchaseRequest)
    );

    return new ProductionPlanDetailMediatorMessages.Create.Response(
      Collections.emptyList()
    );
  }

  @Override
  public Cancel.Response apply(Cancel.Request request) {
    request.getContext().getPurchaseRequestService().cancel(
      PurchaseRequestRequests.CancelRequest.builder()
        .id(purchaseRequest.getId())
        .build()
    );
    return new Cancel.Response(
      Collections.emptyList()
    );
  }

  @Override
  public UUID getLinkedId() {
    return purchaseRequest.getId().getValue();
  }

  @Override
  public boolean isCancelable() {
    return purchaseRequest.isCancelable();
  }
}
