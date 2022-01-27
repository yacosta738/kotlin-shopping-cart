package com.tul

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import org.junit.jupiter.api.Test

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses

class ArchTest {

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.tul")

        noClasses()
            .that()
                .resideInAnyPackage("com.tul.service..")
            .or()
                .resideInAnyPackage("com.tul.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..com.tul.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses)
    }
}
