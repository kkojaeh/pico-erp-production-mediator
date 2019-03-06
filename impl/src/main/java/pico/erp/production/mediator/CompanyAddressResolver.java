package pico.erp.production.mediator;

import pico.erp.company.CompanyId;
import pico.erp.shared.data.Address;

public interface CompanyAddressResolver {

  Address resolve(CompanyId companyId);

}
