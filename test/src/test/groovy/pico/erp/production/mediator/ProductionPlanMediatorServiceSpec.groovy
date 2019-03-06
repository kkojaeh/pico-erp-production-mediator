package pico.erp.production.mediator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import pico.erp.company.CompanyId
import pico.erp.item.ItemService
import pico.erp.process.ProcessService
import pico.erp.production.plan.ProductionPlanId
import pico.erp.production.plan.detail.ProductionPlanDetailProgressTypeKind
import pico.erp.production.plan.detail.ProductionPlanDetailRequests
import pico.erp.production.plan.detail.ProductionPlanDetailService
import pico.erp.shared.IntegrationConfiguration
import spock.lang.Specification

@SpringBootTest(classes = [IntegrationConfiguration])
@Transactional
@Rollback
@ActiveProfiles("test")
@Configuration
@ComponentScan("pico.erp.config")
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
