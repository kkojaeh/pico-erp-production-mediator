package pico.erp.production.mediator;


import java.io.Serializable;
import java.util.UUID;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import pico.erp.production.plan.detail.ProductionPlanDetailId;
import pico.erp.shared.TypeDefinitions;

@Entity(name = "ProductionPlanDetailMediator")
@Table(name = "PDM_PRODUCTION_PLAN_DETAIL_MEDIATOR", indexes = {
  @Index(columnList = "linkedId")
})
@Data
@EqualsAndHashCode(of = "id")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductionPlanDetailMediatorEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @AttributeOverrides({
    @AttributeOverride(name = "value", column = @Column(name = "ID", length = TypeDefinitions.UUID_BINARY_LENGTH))
  })
  ProductionPlanDetailId id;

  @Column(name = "linkedId", length = TypeDefinitions.UUID_BINARY_LENGTH)
  UUID linkedId;

}
