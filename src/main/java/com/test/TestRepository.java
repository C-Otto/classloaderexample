package com.test;

import org.springframework.data.repository.CrudRepository;

interface TestRepository extends CrudRepository<BaseEntity, Long> {
}
