/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.vault.repository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.vault.core.VaultIntegrationTestConfiguration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.domain.Person;
import org.springframework.vault.repository.VaultRepositoryIntegrationTests.VaultRepositoryTestConfiguration;
import org.springframework.vault.repository.configuration.EnableVaultRepositories;
import org.springframework.vault.util.IntegrationTestSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.data.domain.Sort.Order.asc;

/**
 * Integration tests for Vault repositories.
 *
 * @author Mark Paluch
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = VaultRepositoryTestConfiguration.class)
class VaultRepositoryIntegrationTests extends IntegrationTestSupport {

	@Configuration
	@EnableVaultRepositories(considerNestedRepositories = true)
	static class VaultRepositoryTestConfiguration
			extends VaultIntegrationTestConfiguration {
	}

	@Autowired
	VaultRepository vaultRepository;

	@Autowired
	VaultTemplate vaultTemplate;

	@BeforeEach
	void before() {
		vaultRepository.deleteAll();
	}

	@Test
	void loadAndSave() {

		Person person = new Person();
		person.setId("foo-key");
		person.setFirstname("bar");

		vaultRepository.save(person);

		Iterable<Person> all = vaultRepository.findAll();

		assertThat(all).contains(person);
		assertThat(vaultRepository.findById("foo-key")).contains(person);
	}

	@Test
	void shouldApplyQueryMethod() {

		Person walter = new Person();
		walter.setId("walter");
		walter.setFirstname("Walter");

		vaultRepository.save(walter);

		Person skyler = new Person();
		skyler.setId("skyler");
		skyler.setFirstname("Skyler");

		vaultRepository.save(skyler);

		Iterable<Person> all = vaultRepository.findByIdStartsWith("walt");

		assertThat(all).contains(walter).doesNotContain(skyler);
	}

	@Test
	void shouldApplyQueryMethodWithSorting() {

		Person walter = new Person();
		walter.setId("walter");
		walter.setFirstname("Walter");

		vaultRepository.save(walter);

		Person skyler = new Person();
		skyler.setId("skyler");
		skyler.setFirstname("Skyler");

		vaultRepository.save(skyler);

		assertThat(vaultRepository.findAllByOrderByFirstnameAsc())
				.containsSequence(skyler, walter);
		assertThat(vaultRepository.findAllByOrderByFirstnameDesc())
				.containsSequence(walter, skyler);
	}

	@Test
	void shouldApplyLimiting() {

		Person walter = new Person();
		walter.setId("walter");
		walter.setFirstname("Walter");

		vaultRepository.save(walter);

		Person skyler = new Person();
		skyler.setId("skyler");
		skyler.setFirstname("Skyler");

		vaultRepository.save(skyler);

		assertThat(vaultRepository.findTop1By(Sort.by(asc("firstname"))))
				.containsOnly(skyler);
	}

	@Test
	void shouldFailForNonIdCriteria() {
		assertThatExceptionOfType(InvalidDataAccessApiUsageException.class)
				.isThrownBy(() -> vaultRepository.findInvalidByFirstname("foo"));
	}

	interface VaultRepository extends CrudRepository<Person, String> {

		List<Person> findByIdStartsWith(String prefix);

		List<Person> findAllByOrderByFirstnameAsc();

		List<Person> findAllByOrderByFirstnameDesc();

		List<Person> findTop1By(Sort sort);

		List<Person> findInvalidByFirstname(String name);
	}
}
