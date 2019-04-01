package pico.erp.production.mediator;

import java.math.BigDecimal;
import java.util.UUID;
import kkojaeh.spring.boot.component.Take;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import pico.erp.outsourced.invoice.OutsourcedInvoiceEvents;
import pico.erp.outsourced.invoice.OutsourcedInvoiceService;
import pico.erp.outsourced.invoice.OutsourcedInvoiceStatusKind;
import pico.erp.outsourced.invoice.item.OutsourcedInvoiceItemId;
import pico.erp.outsourced.invoice.item.OutsourcedInvoiceItemService;
import pico.erp.outsourcing.request.OutsourcingRequestEvents;
import pico.erp.production.order.ProductionOrderEvents;
import pico.erp.production.plan.ProductionPlanEvents;
import pico.erp.production.plan.detail.ProductionPlanDetailEvents;
import pico.erp.purchase.request.PurchaseRequestEvents;

@SuppressWarnings("unused")
@Component
public class ProductionPlanDetailMediatorEventListener {

  private static final String LISTENER_NAME = "listener.production-plan-detail-mediator-event-listener";

  @Take
  protected OutsourcedInvoiceService outsourcedInvoiceService;

  @Autowired
  protected ProductionPlanMediatorService productionPlanMediatorService;

  @Take
  protected OutsourcedInvoiceItemService outsourcedInvoiceItemService;

  protected void complete(UUID linkedId) {
    val exists = productionPlanMediatorService.exists(linkedId);
    if (exists) {
      productionPlanMediatorService.complete(
        ProductionPlanMediatorRequests.CompleteRequest.builder()
          .linkedId(linkedId)
          .build()
      );
    }
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + OutsourcedInvoiceEvents.CanceledEvent.CHANNEL)
  public void onOutsourcedInvoiceCanceled(OutsourcedInvoiceEvents.CanceledEvent event) {
    recreateIfNotCanceled(event.getId().getValue());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + OutsourcedInvoiceEvents.InvoicedEvent.CHANNEL)
  public void onOutsourcedInvoiceInvoiced(OutsourcedInvoiceEvents.InvoicedEvent event) {
    val exists = productionPlanMediatorService.exists(event.getId().getValue());
    if (exists) {
      // TODO: 해당 내용에 종속된 대상의 도착 시간을 수정
    }
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + OutsourcedInvoiceEvents.ReceivedEvent.CHANNEL)
  public void onOutsourcedInvoiceReceived(OutsourcedInvoiceEvents.ReceivedEvent event) {

    val outsourcedInvoiceItemId = OutsourcedInvoiceItemId.from(event.getId().getValue());
    if (outsourcedInvoiceItemService.exists(outsourcedInvoiceItemId)) {
      val item = outsourcedInvoiceItemService.get(outsourcedInvoiceItemId);
      progress(event.getId().getValue(), item.getQuantity());
      complete(event.getId().getValue());
    }
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + OutsourcedInvoiceEvents.InvoicedEvent.CHANNEL)
  public void onOutsourcedInvoiceUpdated(OutsourcedInvoiceEvents.UpdatedEvent event) {
    val exists = productionPlanMediatorService.exists(event.getId().getValue());
    if (exists) {
      val outsourcedInvoice = outsourcedInvoiceService.get(event.getId());
      if (outsourcedInvoice.getStatus() == OutsourcedInvoiceStatusKind.DETERMINED) {
        // TODO: 해당 내용에 종속된 대상의 도착 시간을 수정
      }
    }
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + OutsourcingRequestEvents.CanceledEvent.CHANNEL)
  public void onOutsourcingRequestCanceled(OutsourcingRequestEvents.CanceledEvent event) {
    recreateIfNotCanceled(event.getId().getValue());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + OutsourcingRequestEvents.CompletedEvent.CHANNEL)
  public void onOutsourcingRequestCompleted(OutsourcingRequestEvents.CompletedEvent event) {
    complete(event.getId().getValue());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + OutsourcingRequestEvents.ProgressedEvent.CHANNEL)
  public void onOutsourcingRequestProgressed(OutsourcingRequestEvents.ProgressedEvent event) {
    progress(event.getId().getValue(), event.getQuantity());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + ProductionOrderEvents.CanceledEvent.CHANNEL)
  public void onProductionOrderCanceled(ProductionOrderEvents.CanceledEvent event) {
    recreateIfNotCanceled(event.getId().getValue());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + ProductionOrderEvents.CompletedEvent.CHANNEL)
  public void onProductionOrderCompleted(ProductionOrderEvents.CompletedEvent event) {
    complete(event.getId().getValue());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + ProductionOrderEvents.ProgressedEvent.CHANNEL)
  public void onProductionOrderProgressed(ProductionOrderEvents.ProgressedEvent event) {
    progress(event.getId().getValue(), event.getProgressedQuantity());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + ProductionPlanEvents.CanceledEvent.CHANNEL)
  public void onProductionPlanCanceled(ProductionPlanEvents.CanceledEvent event) {
    productionPlanMediatorService.cancel(
      ProductionPlanMediatorRequests.CancelRequest.builder()
        .id(event.getId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + PurchaseRequestEvents.CanceledEvent.CHANNEL)
  public void onPurchaseRequestCanceled(PurchaseRequestEvents.CanceledEvent event) {
    recreateIfNotCanceled(event.getId().getValue());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + PurchaseRequestEvents.CompletedEvent.CHANNEL)
  public void onPurchaseRequestCompleted(PurchaseRequestEvents.CompletedEvent event) {
    complete(event.getId().getValue());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + PurchaseRequestEvents.ProgressedEvent.CHANNEL)
  public void onPurchaseRequestProgressed(PurchaseRequestEvents.ProgressedEvent event) {
    progress(event.getId().getValue(), event.getQuantity());
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + ProductionPlanEvents.DeterminedEvent.CHANNEL)
  public void onProductionPlanDetermined(ProductionPlanEvents.DeterminedEvent event) {
    productionPlanMediatorService.create(
      ProductionPlanMediatorRequests.CreateRequest.builder()
        .id(event.getId())
        .build()
    );
  }

  @EventListener
  @JmsListener(destination = LISTENER_NAME + "."
    + ProductionPlanDetailEvents.DependenciesCompletedEvent.CHANNEL)
  public void onProductionPlanDetailDependenciesCompleted(
    ProductionPlanDetailEvents.DependenciesCompletedEvent event) {
    productionPlanMediatorService.prepare(
      ProductionPlanMediatorRequests.PrepareRequest.builder()
        .id(event.getId())
        .build()
    );
  }

  protected void progress(UUID linkedId, BigDecimal quantity) {
    val exists = productionPlanMediatorService.exists(linkedId);
    if (exists) {
      productionPlanMediatorService.progress(
        ProductionPlanMediatorRequests.ProgressRequest.builder()
          .linkedId(linkedId)
          .progressQuantity(quantity)
          .build()
      );
    }
  }

  protected void recreateIfNotCanceled(UUID linkedId) {
    val exists = productionPlanMediatorService.exists(linkedId);
    if (exists) {
      val mediator = productionPlanMediatorService.get(linkedId);
      if (!mediator.isCanceled()) {
        productionPlanMediatorService.recreate(
          ProductionPlanMediatorRequests.RecreateRequest.builder()
            .linkedId(linkedId)
            .build()
        );
      }
    }
  }
}
