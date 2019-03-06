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
import pico.erp.outsourced.invoice.OutsourcedInvoiceData;
import pico.erp.outsourced.invoice.OutsourcedInvoiceId;
import pico.erp.outsourced.invoice.OutsourcedInvoiceRequests;
import pico.erp.outsourced.invoice.item.OutsourcedInvoiceItemData;
import pico.erp.outsourced.invoice.item.OutsourcedInvoiceItemId;
import pico.erp.outsourced.invoice.item.OutsourcedInvoiceItemRequests;
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
public class OutsourcedInvoiceProductionPlanDetailMediator implements ProductionPlanDetailMediator {

  private static final long serialVersionUID = 1L;

  ProductionPlanDetailData productionPlanDetail;

  OutsourcedInvoiceData outsourcedInvoice;

  OutsourcedInvoiceItemData outsourcedInvoiceItem;

  @Override
  public ProductionPlanDetailMediatorMessages.Create.Response apply(
    ProductionPlanDetailMediatorMessages.Create.Request request) {
    val plan = request.getPlan();
    val companyAddressResolver = request.getCompanyAddressResolver();
    this.productionPlanDetail = request.getPlanDetail();
    val outsourcedInvoiceId = OutsourcedInvoiceId.generate();
    val outsourcedInvoiceItemId = OutsourcedInvoiceItemId.from(outsourcedInvoiceId.getValue());
    outsourcedInvoice = OutsourcedInvoiceData.builder()
      .id(outsourcedInvoiceId)
      .receiverId(productionPlanDetail.getReceiverId())
      .senderId(productionPlanDetail.getActorId())
      .receiveAddress(companyAddressResolver.resolve(productionPlanDetail.getReceiverId()))
      .dueDate(productionPlanDetail.getEndDate())
      .projectId(plan.getProjectId())
      .remark(null)
      .build();

    outsourcedInvoiceItem = OutsourcedInvoiceItemData.builder()
      .id(outsourcedInvoiceItemId)
      .invoiceId(outsourcedInvoiceId)
      .itemId(productionPlanDetail.getItemId())
      .itemSpecCode(productionPlanDetail.getItemSpecCode())
      .quantity(productionPlanDetail.getPlannedQuantity())
      .unit(productionPlanDetail.getUnit())
      .build();

    request.getContext().getOutsourcedInvoiceService().create(
      OutsourcedInvoiceRequests.CreateRequest.from(outsourcedInvoice)
    );
    request.getContext().getOutsourcedInvoiceItemService().create(
      OutsourcedInvoiceItemRequests.CreateRequest.from(outsourcedInvoiceItem)
    );

    return new ProductionPlanDetailMediatorMessages.Create.Response(
      Collections.emptyList()
    );
  }

  @Override
  public Cancel.Response apply(Cancel.Request request) {
    request.getContext().getOutsourcedInvoiceService().cancel(
      OutsourcedInvoiceRequests.CancelRequest.builder()
        .id(outsourcedInvoice.getId())
        .build()
    );
    return new Cancel.Response(
      Collections.emptyList()
    );
  }

  @Override
  public UUID getLinkedId() {
    return outsourcedInvoice.getId().getValue();
  }

  @Override
  public boolean isCancelable() {
    return outsourcedInvoice.isCancelable();
  }
}
