package pico.erp.production.mediator

import kkojaeh.spring.boot.component.SpringBootTestComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Lazy
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import pico.erp.bom.BomApplication
import pico.erp.company.CompanyApplication
import pico.erp.company.CompanyId
import pico.erp.invoice.InvoiceApplication
import pico.erp.item.ItemApplication
import pico.erp.item.ItemService
import pico.erp.outsourced.invoice.OutsourcedInvoiceApplication
import pico.erp.outsourcing.request.OutsourcingRequestApplication
import pico.erp.process.ProcessApplication
import pico.erp.process.ProcessService
import pico.erp.production.order.ProductionOrderApplication
import pico.erp.production.plan.ProductionPlanApplication
import pico.erp.production.plan.ProductionPlanId
import pico.erp.production.plan.detail.ProductionPlanDetailProgressTypeKind
import pico.erp.production.plan.detail.ProductionPlanDetailRequests
import pico.erp.production.plan.detail.ProductionPlanDetailService
import pico.erp.project.ProjectApplication
import pico.erp.purchase.request.PurchaseRequestApplication
import pico.erp.shared.TestParentApplication
import pico.erp.user.UserApplication
import pico.erp.warehouse.WarehouseApplication
import spock.lang.Specification

@SpringBootTest(classes = [ProductionMediatorApplication, TestConfig])
@SpringBootTestComponent(parent = TestParentApplication, siblings = [
  UserApplication, ItemApplication, ProjectApplication, ProcessApplication, CompanyApplication, BomApplication,
  ProductionOrderApplication, ProductionPlanApplication, OutsourcingRequestApplication, PurchaseRequestApplication,
  OutsourcedInvoiceApplication, WarehouseApplication, InvoiceApplication
])
@Transactional
@Rollback
@ActiveProfiles("test")
class ProductionPlanMediatorServiceSpec extends Specification {

  @Lazy
  @Autowired
  ProductionPlanMediatorService productionPlanMediatorService

  @Lazy
  @Autowired
  ProductionPlanDetailService productionPlanDetailService

  @Lazy
  @Autowired
  ItemService itemService

  @Lazy
  @Autowired
  ProcessService processService

  def actorId = CompanyId.from("CUST1")

  def productionPlanId = ProductionPlanId.from("production-plan-2")

  def "구매로 시작"() {
    when:
    def progressTypes = [
      0: ProductionPlanDetailProgressTypeKind.PURCHASE,
      1: ProductionPlanDetailProgressTypeKind.OUTSOURCING,
      2: ProductionPlanDetailProgressTypeKind.PRODUCE,
      3: ProductionPlanDetailProgressTypeKind.PRODUCE
    ]
    def details = productionPlanDetailService.getAll(productionPlanId)
    details.eachWithIndex({
      detail, index ->
        detail.actorId = actorId
        detail.progressType = progressTypes[index]
        productionPlanDetailService.update(
          ProductionPlanDetailRequests.UpdateRequest.from(detail)
        )
    })
    details.forEach({
      detail ->
        productionPlanDetailService.determine(
          new ProductionPlanDetailRequests.DetermineRequest(
            id: detail.id
          )
        )
    })
    productionPlanMediatorService.create(
      new ProductionPlanMediatorRequests.CreateRequest(
        id: productionPlanId
      )
    )

    then:
    true == true
  }

  def "입고로 시작"() {
    when:
    def progressTypes = [
      0: ProductionPlanDetailProgressTypeKind.WAREHOUSING,
      1: ProductionPlanDetailProgressTypeKind.OUTSOURCING,
      2: ProductionPlanDetailProgressTypeKind.PRODUCE,
      3: ProductionPlanDetailProgressTypeKind.PRODUCE
    ]
    def details = productionPlanDetailService.getAll(productionPlanId)
    details.eachWithIndex({
      detail, index ->
        detail.actorId = actorId
        detail.progressType = progressTypes[index]
        productionPlanDetailService.update(
          ProductionPlanDetailRequests.UpdateRequest.from(detail)
        )
    })
    details.forEach({
      detail ->
        productionPlanDetailService.determine(
          new ProductionPlanDetailRequests.DetermineRequest(
            id: detail.id
          )
        )
    })
    productionPlanMediatorService.create(
      new ProductionPlanMediatorRequests.CreateRequest(
        id: productionPlanId
      )
    )

    then:
    true == true
  }

}
