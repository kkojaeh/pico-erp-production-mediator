package pico.erp.production.mediator;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.val;
import pico.erp.outsourcing.request.OutsourcingRequestData;
import pico.erp.outsourcing.request.OutsourcingRequestId;
import pico.erp.outsourcing.request.OutsourcingRequestRequests;
import pico.erp.outsourcing.request.material.OutsourcingRequestMaterialData;
import pico.erp.outsourcing.request.material.OutsourcingRequestMaterialId;
import pico.erp.outsourcing.request.material.OutsourcingRequestMaterialRequests;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Cancel;
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
public class OutsourcingRequestProductionPlanDetailMediator implements
  ProductionPlanDetailMediator {

  private static final long serialVersionUID = 1L;

  ProductionPlanDetailData productionPlanDetail;

  OutsourcingRequestData outsourcingRequest;

  List<OutsourcingRequestMaterialData> outsourcingRequestMaterials;

  @Override
  public ProductionPlanDetailMediatorMessages.Create.Response apply(
    ProductionPlanDetailMediatorMessages.Create.Request request) {
    val plan = request.getPlan();
    val detail = request.getPlanDetail();
    val dependencies = request.getDependencies();
    productionPlanDetail = detail;
    val outsourcingRequestId = OutsourcingRequestId.generate();
    outsourcingRequest = OutsourcingRequestData.builder()
      .id(outsourcingRequestId)
      .itemId(detail.getItemId())
      .itemSpecCode(detail.getItemSpecCode())
      .processId(detail.getProcessId())
      .quantity(detail.getQuantity())
      .spareQuantity(detail.getSpareQuantity())
      .unit(detail.getUnit())
      .projectId(plan.getProjectId())
      .dueDate(detail.getEndDate())
      .supplierId(detail.getActorId())
      .receiverId(detail.getReceiverId())
      .remark(null)
      .requesterId(plan.getPlannerId())
      .build();

    outsourcingRequestMaterials = dependencies.stream().map(dependency -> {
      val materialId = OutsourcingRequestMaterialId.from(dependency.getId().getValue());
      return OutsourcingRequestMaterialData.builder()
        .id(materialId)
        .requestId(outsourcingRequestId)
        .itemId(dependency.getItemId())
        .itemSpecCode(dependency.getItemSpecCode())
        .quantity(dependency.getPlannedQuantity())
        .unit(dependency.getUnit())
        .supplierId(dependency.getActorId())
        .remark(null)
        .estimatedSupplyDate(dependency.getEndDate())
        .build();
    })
      .collect(Collectors.toList());

    val createRequest = OutsourcingRequestRequests.CreateRequest.from(outsourcingRequest);
    createRequest.setMaterialsManually(true);
    request.getContext().getOutsourcingRequestService().create(
      createRequest
    );
    outsourcingRequestMaterials.forEach(material ->
      request.getContext().getOutsourcingRequestMaterialService().create(
        OutsourcingRequestMaterialRequests.CreateRequest.from(material)
      )
    );
    request.getContext().getOutsourcingRequestService().commit(
      OutsourcingRequestRequests.CommitRequest.builder()
        .id(outsourcingRequestId)
        .committerId(plan.getPlannerId())
        .build()
    );
    request.getContext().getOutsourcingRequestService().accept(
      OutsourcingRequestRequests.AcceptRequest.builder()
        .id(outsourcingRequestId)
        .accepterId(plan.getPlannerId())
        .build()
    );
    return new ProductionPlanDetailMediatorMessages.Create.Response(
      Collections.emptyList()
    );
  }

  @Override
  public Cancel.Response apply(Cancel.Request request) {
    request.getContext().getOutsourcingRequestService().cancel(
      OutsourcingRequestRequests.CancelRequest.builder()
        .id(outsourcingRequest.getId())
        .build()
    );
    return new Cancel.Response(
      Collections.emptyList()
    );
  }

  @Override
  public UUID getLinkedId() {
    return outsourcingRequest.getId().getValue();
  }

  @Override
  public boolean isCancelable() {
    return outsourcingRequest.isCancelable();
  }
}
