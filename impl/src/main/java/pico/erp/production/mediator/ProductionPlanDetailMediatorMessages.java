package pico.erp.production.mediator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import javax.validation.constraints.NotNull;
import kkojaeh.spring.boot.component.ComponentAutowired;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import org.springframework.stereotype.Component;
import pico.erp.outsourced.invoice.OutsourcedInvoiceService;
import pico.erp.outsourced.invoice.item.OutsourcedInvoiceItemService;
import pico.erp.outsourcing.request.OutsourcingRequestService;
import pico.erp.outsourcing.request.material.OutsourcingRequestMaterialService;
import pico.erp.production.order.ProductionOrderService;
import pico.erp.production.plan.ProductionPlanData;
import pico.erp.production.plan.detail.ProductionPlanDetailData;
import pico.erp.production.plan.detail.ProductionPlanDetailService;
import pico.erp.purchase.request.PurchaseRequestService;
import pico.erp.shared.event.Event;

public interface ProductionPlanDetailMediatorMessages {

  interface Create {

    @Data
    @Builder
    class Request {

      @NotNull
      ProductionPlanData plan;

      @NotNull
      ProductionPlanDetailData planDetail;

      @NotNull
      CompanyAddressResolver companyAddressResolver;

      @NotNull
      List<ProductionPlanDetailData> dependencies;

      @NotNull
      ProductionPlanDetailContext context;

    }

    @Value
    class Response {

      Collection<Event> events;

    }
  }

  interface Cancel {

    @Data
    @Builder
    class Request {

      @NotNull
      ProductionPlanDetailContext context;

    }

    @Value
    class Response {

      Collection<Event> events;

    }
  }

  interface Progress {

    @Data
    @Builder
    class Request {

      @NotNull
      ProductionPlanDetailContext context;

      @NotNull
      BigDecimal progressQuantity;

    }

    @Value
    class Response {

      Collection<Event> events;

    }
  }

  interface Complete {

    @Data
    @Builder
    class Request {

      @NotNull
      ProductionPlanDetailContext context;

    }

    @Value
    class Response {

      Collection<Event> events;

    }
  }

  interface Prepare {

    @Data
    @Builder
    class Request {

      @NotNull
      ProductionPlanDetailContext context;

    }

    @Value
    class Response {

      Collection<Event> events;

    }
  }

  @Getter
  @Component
  class ProductionPlanDetailContext {

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

    @ComponentAutowired
    protected ProductionPlanDetailService productionPlanDetailService;

  }


}
