package pico.erp.production.mediator;

import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pico.erp.production.plan.detail.ProductionPlanDetailId;

@Repository
interface ProductionPlanDetailMediatorEntityRepository extends
  CrudRepository<ProductionPlanDetailMediatorEntity, ProductionPlanDetailId> {

  @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM ProductionPlanDetailMediator m WHERE m.linkedId = :linkedId")
  boolean exists(@Param("linkedId") UUID linkedId);

  @Query("SELECT m FROM ProductionPlanDetailMediator m WHERE m.linkedId = :linkedId")
  ProductionPlanDetailMediatorEntity findBy(@Param("linkedId") UUID linkedId);

}

@Repository
@Transactional
public class ProductionPlanDetailMediatorRepositoryJpa implements
  ProductionPlanDetailMediatorRepository {

  @Autowired
  private ProductionPlanDetailMediatorEntityRepository repository;

  @Autowired
  private ProductionPlanDetailMediatorMapper mapper;

  @Override
  public ProductionPlanDetailMediator create(
    ProductionPlanDetailMediator mediator) {
    val entity = mapper.jpa(mediator);
    val created = repository.save(entity);
    return mapper.jpa(created);
  }

  @Override
  public void deleteBy(ProductionPlanDetailId id) {
    repository.deleteById(id);
  }

  @Override
  public boolean exists(ProductionPlanDetailId id) {
    return repository.existsById(id);
  }

  @Override
  public boolean exists(UUID linkedId) {
    return repository.exists(linkedId);
  }

  @Override
  public Optional<ProductionPlanDetailMediator> findBy(ProductionPlanDetailId id) {
    return repository.findById(id)
      .map(mapper::jpa);
  }

  @Override
  public Optional<ProductionPlanDetailMediator> findBy(UUID linkedId) {
    return Optional.ofNullable(repository.findBy(linkedId))
      .map(mapper::jpa);
  }

  @Override
  public void update(ProductionPlanDetailMediator mediator) {
    val entity = repository.findById(mediator.getProductionPlanDetail().getId()).get();
    mapper.pass(mapper.jpa(mediator), entity);
    repository.save(entity);
  }


}
