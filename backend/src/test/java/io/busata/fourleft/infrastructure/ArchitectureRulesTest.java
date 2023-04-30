package io.busata.fourleft.infrastructure;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchitectureRulesTest {
    private final JavaClasses classes = new ClassFileImporter().importPackages("io.busata.fourleft");

    @Test
    public void respectLayers() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Endpoints").definedBy("io.busata.fourleft.endpoints..")
                .layer("Application").definedBy("io.busata.fourleft.application..")
                .layer("API").definedBy("io.busata.fourleft.api..")
                .layer("Common").definedBy("io.busata.fourleft.common..")
                .layer("Domain").definedBy("io.busata.fourleft.domain..")
                .layer("Infrastructure").definedBy("io.busata.fourleft.infrastructure..")
                .whereLayer("Endpoints").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Endpoints", "Infrastructure")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .whereLayer("Domain").mayOnlyAccessLayers("Common")
                .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application");
        rule.check(classes);
    }

    @Test
    public void serviceAnnotationOnServices() {
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith("org.springframework.stereotype.Service");


        rule.check(classes);
    }
    @Test
    public void factoryAnnotationOnFactories() {
        ArchRule rule = classes().that().haveSimpleNameEndingWith("Factory")
                .and().resideOutsideOfPackages("io.busata.fourleft.infrastructure.common")
                .should().beAnnotatedWith("io.busata.fourleft.infrastructure.common.Factory");

        rule.check(classes);
    }

    @Test
    public void testRabbitMQOnlyInfrastructure() {
        ArchRule rule = noClasses().that().resideOutsideOfPackage("io.busata.fourleft.infrastructure..")
                .should().dependOnClassesThat().resideInAPackage("..rabbit..");

        rule.check(classes);
    }

    @Test
    public void noTransactionalEndpoints() {
        ArchRule rule = noMethods().that().areDeclaredInClassesThat().resideInAPackage("..endpoints..")
                .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional");

        rule.check(classes);
    }
}
