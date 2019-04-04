package pico.erp.production.mediator;

import kkojaeh.spring.boot.component.ComponentAutowired;
import lombok.val;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import pico.erp.outsourced.invoice.OutsourcedInvoiceId;
import pico.erp.outsourced.invoice.OutsourcedInvoiceService;
import pico.erp.outsourced.invoice.item.OutsourcedInvoiceItemId;
import pico.erp.outsourced.invoice.item.OutsourcedInvoiceItemService;
import pico.erp.outsourcing.request.OutsourcingRequestId;
import pico.erp.outsourcing.request.OutsourcingRequestService;
import pico.erp.outsourcing.request.material.OutsourcingRequestMaterialService;
import pico.erp.production.order.ProductionOrderId;
import pico.erp.production.order.ProductionOrderService;
import pico.erp.production.plan.detail.ProductionPlanDetailService;
import pico.erp.purchase.request.PurchaseRequestId;
import pico.erp.purchase.request.PurchaseRequestService;

@Mapper
public abstract class ProductionPlanDetailMediatorMapper {

  @ComponentAutowired
  protected ProductionPlanDetailService productionPlanDetailService;

  @ComponentAutowired
  protected ProductionOrderService productionOrderService;

  @ComponentAutowired
  protected PurchaseRequestService purchaseRequestService;

  @ComponentAutowired
  protected OutsourcingRequestService outsourcingRequestService;

  @ComponentAutowired
  protected OutsourcingRequestMaterialService outsourcingRequestMaterialService;

  @ComponentAutowired
  protected OutsourcedInvoiceService outsourcedInvoiceService;

  @ComponentAutowired
  protected OutsourcedInvoiceItemService outsourcedInvoiceItemService;

  public ProductionPlanDetailMediatorEntity jpa(
    ProductionPlanDetailMediator data) {
    return ProductionPlanDetailMediatorEntity.builder()
      .id(data.getProductionPlanDetail().getId())
      .linkedId(data.getLinkedId())
      .build();
  }

  public ProductionPlanDetailMediator jpa(ProductionPlanDetailMediatorEntity entity) {
    val detail = productionPlanDetailService.get(entity.getId());
    switch (detail.getProgressType()) {
      case PRODUCE:
        return ProductionOrderProductionPlanDetailMediator.builder()
          .productionPlanDetail(detail)
          .productionOrder(productionOrderService.get(ProductionOrderId.from(entity.getLinkedId())))
          .build();
      case PURCHASE:
        return PurchaseRequestProductionPlanDetailMediator.builder()
          .productionPlanDetail(detail)
          .purchaseRequest(purchaseRequestService.get(PurchaseRequestId.from(entity.getLinkedId())))
          .build();
      case OUTSOURCING:
        return OutsourcingRequestProductionPlanDetailMediator.builder()
          .productionPlanDetail(detail)
          .outsourcingRequest(
            outsourcingRequestService.get(OutsourcingRequestId.from(entity.getLinkedId())))
          .outsourcingRequestMaterials(outsourcingRequestMaterialService
            .getAll(OutsourcingRequestId.from(entity.getLinkedId())))
          .build();
      case WAREHOUSING:
        return OutsourcedInvoiceProductionPlanDetailMediator.builder()
          .productionPlanDetail(detail)
          .outsourcedInvoice(
            outsourcedInvoiceService.get(OutsourcedInvoiceId.from(entity.getLinkedId())))
          .outsourcedInvoiceItem(
            outsourcedInvoiceItemService.get(OutsourcedInvoiceItemId.from(entity.getLinkedId())))
          .build();
      default:
        throw new RuntimeException("not supported progress type");
    }

  }

  @Mappings({
    @Mapping(target = "id", source = "productionPlanDetail.id"),
  })
  public abstract ProductionPlanDetailMediatorData map(ProductionPlanDetailMediator mediator);

  public abstract void pass(
    ProductionPlanDetailMediatorEntity from, @MappingTarget ProductionPlanDetailMediatorEntity to);


}


