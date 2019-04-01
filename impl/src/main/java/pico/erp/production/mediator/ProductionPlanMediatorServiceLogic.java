package pico.erp.production.mediator;

import java.util.UUID;
import java.util.stream.Collectors;
import kkojaeh.spring.boot.component.Give;
import kkojaeh.spring.boot.component.Take;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pico.erp.company.CompanyService;
import pico.erp.company.address.CompanyAddressService;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Complete;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Create;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Prepare;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.ProductionPlanDetailContext;
import pico.erp.production.mediator.ProductionPlanDetailMediatorMessages.Progress;
import pico.erp.production.mediator.ProductionPlanMediatorRequests.CancelRequest;
import pico.erp.production.mediator.ProductionPlanMediatorRequests.CompleteRequest;
import pico.erp.production.mediator.ProductionPlanMediatorRequests.CreateRequest;
import pico.erp.production.mediator.ProductionPlanMediatorRequests.PrepareRequest;
import pico.erp.production.mediator.ProductionPlanMediatorRequests.ProgressRequest;
import pico.erp.production.mediator.ProductionPlanMediatorRequests.RecreateRequest;
import pico.erp.production.plan.ProductionPlanService;
import pico.erp.production.plan.detail.ProductionPlanDetailId;
import pico.erp.production.plan.detail.ProductionPlanDetailService;
import pico.erp.shared.data.Address;
import pico.erp.shared.event.EventPublisher;

@SuppressWarnings("Duplicates")
@Service
@Give
@Transactional
@Validated
public class ProductionPlanMediatorServiceLogic implements ProductionPlanMediatorService {


  @Take
  protected ProductionPlanDetailService productionPlanDetailService;

  @Take
  protected CompanyService companyService;

  @Take
  protected CompanyAddressService companyAddressService;

  @Autowired
  private ProductionPlanDetailMediatorRepository productionPlanDetailMediatorRepository;

  @Autowired
  private EventPublisher eventPublisher;

  @Take
  private ProductionPlanService productionPlanService;

  @Autowired
  private ProductionPlanDetailContext context;

  @Autowired
  private ProductionPlanDetailMediatorMapper mapper;

  @Override
  public void cancel(CancelRequest request) {
    val plan = productionPlanService.get(request.getId());
    val details = productionPlanDetailService.getAll(plan.getId());
    details.forEach(detail -> {
      productionPlanDetailMediatorRepository.findBy(detail.getId())
        .ifPresent(mediator -> {
          if (mediator.isCancelable()) {
            val response = mediator.apply(
              ProductionPlanDetailMediatorMessages.Cancel.Request.builder()
                .context(context)
                .build()
            );
            productionPlanDetailMediatorRepository.update(mediator);
            eventPublisher.publishEvents(response.getEvents());
          }
        });
    });
  }

  @Override
  public void complete(CompleteRequest request) {
    val mediator = productionPlanDetailMediatorRepository.findBy(request.getLinkedId())
      .orElseThrow(ProductionPlanMediatorExceptions.NotFoundException::new);
    val message = Complete.Request.builder()
      .context(context)
      .build();
    val response = mediator.apply(message);
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public void create(CreateRequest request) {
    val plan = productionPlanService.get(request.getId());
    val details = productionPlanDetailService.getAll(plan.getId());
    details.forEach(detail -> {
      ProductionPlanDetailMediator mediator = null;
      val message = Create.Request.builder()
        .plan(plan)
        .planDetail(detail)
        .context(context)
        .companyAddressResolver((companyId) -> {
          val addresses = companyAddressService.getAll(companyId);
          return addresses.stream()
            .filter(address -> address.isRepresented())
            .map(data -> data.getAddress())
            .findFirst()
            .orElse(new Address());
        })
        .dependencies(
          detail.getDependencies().stream()
            .map(productionPlanDetailService::get)
            .collect(Collectors.toList())
        )
        .build();
      switch (detail.getProgressType()) {
        case PRODUCE:
          mediator = new ProductionOrderProductionPlanDetailMediator();
          break;
        case PURCHASE:
          mediator = new PurchaseRequestProductionPlanDetailMediator();
          break;
        case OUTSOURCING:
          mediator = new OutsourcingRequestProductionPlanDetailMediator();
          break;
        case WAREHOUSING:
          mediator = new OutsourcedInvoiceProductionPlanDetailMediator();
          break;
        default:
          throw new RuntimeException("not supported progress type");
      }
      val response = mediator.apply(message);
      productionPlanDetailMediatorRepository.create(mediator);
      eventPublisher.publishEvents(response.getEvents());
    });

  }

  @Override
  public boolean exists(UUID linkedId) {
    return productionPlanDetailMediatorRepository.exists(linkedId);
  }

  @Override
  public ProductionPlanDetailMediatorData get(ProductionPlanDetailId id) {
    val mediator = productionPlanDetailMediatorRepository.findBy(id)
      .orElseThrow(ProductionPlanMediatorExceptions.NotFoundException::new);
    return mapper.map(mediator);
  }

  @Override
  public ProductionPlanDetailMediatorData get(UUID linkedId) {
    val mediator = productionPlanDetailMediatorRepository.findBy(linkedId)
      .orElseThrow(ProductionPlanMediatorExceptions.NotFoundException::new);
    return mapper.map(mediator);
  }

  @Override
  public void progress(ProgressRequest request) {
    val mediator = productionPlanDetailMediatorRepository.findBy(request.getLinkedId())
      .orElseThrow(ProductionPlanMediatorExceptions.NotFoundException::new);
    val message = Progress.Request.builder()
      .context(context)
      .progressQuantity(request.getProgressQuantity())
      .build();
    val response = mediator.apply(message);
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public void prepare(PrepareRequest request) {
    val mediator = productionPlanDetailMediatorRepository.findBy(request.getId())
      .orElseThrow(ProductionPlanMediatorExceptions.NotFoundException::new);
    val message = Prepare.Request.builder()
      .context(context)
      .build();
    val response = mediator.apply(message);
    eventPublisher.publishEvents(response.getEvents());
  }

  @Override
  public void recreate(RecreateRequest request) {
    val mediator = productionPlanDetailMediatorRepository.findBy(request.getLinkedId())
      .orElseThrow(ProductionPlanMediatorExceptions.NotFoundException::new);
    val plan = productionPlanService.get(mediator.getProductionPlanDetail().getPlanId());

    val message = Create.Request.builder()
      .plan(plan)
      .planDetail(mediator.getProductionPlanDetail())
      .context(context)
      .companyAddressResolver((companyId) -> {
        val addresses = companyAddressService.getAll(companyId);
        return addresses.stream()
          .filter(address -> address.isRepresented())
          .map(data -> data.getAddress())
          .findFirst()
          .orElse(new Address());
      })
      .dependencies(
        mediator.getProductionPlanDetail().getDependencies().stream()
          .map(productionPlanDetailService::get)
          .collect(Collectors.toList())
      )
      .build();

    val response = mediator.apply(message);
    productionPlanDetailMediatorRepository.update(mediator);
    eventPublisher.publishEvents(response.getEvents());
  }


}
