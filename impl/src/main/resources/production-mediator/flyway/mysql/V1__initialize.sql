create table pdm_production_plan_detail_mediator (
	id binary(16) not null,
	linked_id binary(16),
	primary key (id)
) engine=InnoDB;

create index IDX3jiepqqw1fua3h1bkfdvpi4x2
	on pdm_production_plan_detail_mediator (linked_id);
