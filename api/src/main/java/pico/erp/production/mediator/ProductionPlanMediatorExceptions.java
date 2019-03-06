package pico.erp.production.mediator;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface ProductionPlanMediatorExceptions {


  @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "production-plan-mediator.cannot.create.plan.not.determined.exception")
  class CannotCreatePlanNotDeterminedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
  }

  @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "production-plan-mediator.not.found.exception")
  class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

  }

}
