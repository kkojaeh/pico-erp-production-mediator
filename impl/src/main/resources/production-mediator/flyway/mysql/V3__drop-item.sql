drop table pcr_production_mediator_item;
ALTER TABLE pcr_production_mediator DROP name;

ALTER TABLE pcr_production_mediator ADD item_id binary(16);
ALTER TABLE pcr_production_mediator ADD item_spec_id binary(16);
ALTER TABLE pcr_production_mediator ADD item_spec_code varchar(20);
ALTER TABLE pcr_production_mediator ADD quantity decimal(19,2);
