package de.vinz.openfls.architecture

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaCodeUnit
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.domain.JavaMethodCall
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import jakarta.persistence.Entity
import org.junit.jupiter.api.Test
import org.springframework.data.repository.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

/**
 * Wiederverwendbare Architekturregeln je Domäne. Eine migrierte Domäne bekommt eine
 * eigene Testklasse, die nur den Domänen-Package-Namen übergibt:
 *
 *     class AbsenceArchitectureTest : DomainArchitectureTest("de.vinz.openfls.domains.absence")
 */
abstract class DomainArchitectureTest(private val domainPackage: String) {

    private val scope = "$domainPackage.."

    @Test
    fun restControllersDoNotCallInternalEntityApi() {
        noClasses()
            .that().resideInAPackage(scope).and().areAnnotatedWith(RestController::class.java)
            .should().callMethodWhere(targetIsInternalEntityApi)
            .allowEmptyShould(true)
            .check(importedClasses)
    }

    @Test
    fun internalEntityApiIsDeclaredInServicesOnly() {
        methods()
            .that().areAnnotatedWith(InternalEntityApi::class.java)
            .and().areDeclaredInClassesThat().resideInAPackage(scope)
            .should().beDeclaredInClassesThat().areAnnotatedWith(Service::class.java)
            .allowEmptyShould(true)
            .check(importedClasses)
    }

    @Test
    fun restControllersUseNoEntitiesProjectionsOrRepositories() {
        noClasses()
            .that().resideInAPackage(scope).and().areAnnotatedWith(RestController::class.java)
            .should().dependOnClassesThat(isEntity.or(isProjection).or(isRepository))
            .allowEmptyShould(true)
            .check(importedClasses)
    }

    @Test
    fun serviceMethodsExposeEntitiesOnlyWithInternalEntityApi() {
        publicServiceMethods()
            .should(exposeEntitiesOnlyWhenAnnotated)
            .allowEmptyShould(true)
            .check(importedClasses)
    }

    @Test
    fun serviceMethodsNeverExposeProjections() {
        publicServiceMethods()
            .should(exposeNoProjections)
            .allowEmptyShould(true)
            .check(importedClasses)
    }

    @Test
    fun requestBodyTypesAreNamedRequest() {
        methods()
            .that().areDeclaredInClassesThat().resideInAPackage(scope)
            .and().areDeclaredInClassesThat().areAnnotatedWith(RestController::class.java)
            .should(haveRequestBodiesNamedRequest)
            .allowEmptyShould(true)
            .check(importedClasses)
    }

    @Test
    fun legacyDtoVocabularyIsNotUsed() {
        classes()
            .that().resideInAPackage(scope)
            .should(notContainLegacyVocabulary)
            .allowEmptyShould(true)
            .check(importedClasses)
    }

    private fun publicServiceMethods() = methods()
        .that().arePublic()
        .and().areDeclaredInClassesThat().resideInAPackage(scope)
        .and().areDeclaredInClassesThat().areAnnotatedWith(Service::class.java)
        .and(notSynthetic)

    private val importedClasses by lazy {
        ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("de.vinz.openfls")
    }

    companion object {
        private val isEntity = object : DescribedPredicate<JavaClass>("are JPA entities") {
            override fun test(input: JavaClass) = input.isAnnotatedWith(Entity::class.java)
        }

        private val isProjection = object : DescribedPredicate<JavaClass>("are projections") {
            override fun test(input: JavaClass) =
                input.packageName.contains(".projections") || input.simpleName.endsWith("Projection")
        }

        private val isRepository = object : DescribedPredicate<JavaClass>("are repositories") {
            override fun test(input: JavaClass) = input.isAssignableTo(Repository::class.java)
        }

        private val notSynthetic = object : DescribedPredicate<JavaMethod>("are not synthetic") {
            override fun test(input: JavaMethod) = !input.modifiers.contains(JavaModifier.SYNTHETIC)
        }

        private val targetIsInternalEntityApi =
            object : DescribedPredicate<JavaMethodCall>("target is annotated with @InternalEntityApi") {
                override fun test(input: JavaMethodCall) = input.target.resolveMember()
                    .map { it.isAnnotatedWith(InternalEntityApi::class.java) }
                    .orElse(false)
            }

        private fun involvedTypes(method: JavaCodeUnit): Set<JavaClass> =
            (method.returnType.allInvolvedRawTypes + method.parameters.flatMap { it.type.allInvolvedRawTypes }).toSet()

        private val exposeEntitiesOnlyWhenAnnotated =
            object : ArchCondition<JavaMethod>("expose entities only when annotated with @InternalEntityApi") {
                override fun check(item: JavaMethod, events: ConditionEvents) {
                    val entityTypes = involvedTypes(item).filter { isEntity.test(it) }
                    val annotated = item.isAnnotatedWith(InternalEntityApi::class.java)
                    if (entityTypes.isNotEmpty() && !annotated) {
                        events.add(SimpleConditionEvent.violated(item,
                            "${item.fullName} exposes entities ${entityTypes.map { it.simpleName }} without @InternalEntityApi"))
                    }
                    if (entityTypes.isEmpty() && annotated) {
                        events.add(SimpleConditionEvent.violated(item,
                            "${item.fullName} is annotated with @InternalEntityApi but exposes no entity"))
                    }
                }
            }

        private val exposeNoProjections =
            object : ArchCondition<JavaMethod>("expose no projections") {
                override fun check(item: JavaMethod, events: ConditionEvents) {
                    val projections = involvedTypes(item).filter { isProjection.test(it) }
                    if (projections.isNotEmpty()) {
                        events.add(SimpleConditionEvent.violated(item,
                            "${item.fullName} exposes projections ${projections.map { it.simpleName }}"))
                    }
                }
            }

        private val haveRequestBodiesNamedRequest =
            object : ArchCondition<JavaMethod>("name their @RequestBody types '...Request'") {
                override fun check(item: JavaMethod, events: ConditionEvents) {
                    item.parameters
                        .filter { it.isAnnotatedWith(RequestBody::class.java) }
                        .filterNot { it.rawType.simpleName.endsWith("Request") }
                        .forEach {
                            events.add(SimpleConditionEvent.violated(item,
                                "${item.fullName} uses @RequestBody type ${it.rawType.simpleName} which does not end with 'Request'"))
                        }
                }
            }

        private val notContainLegacyVocabulary =
            object : ArchCondition<JavaClass>("not use Solo/Simple/XL in class names") {
                override fun check(item: JavaClass, events: ConditionEvents) {
                    if (Regex("Solo|Simple|XL").containsMatchIn(item.simpleName)) {
                        events.add(SimpleConditionEvent.violated(item,
                            "${item.name} uses legacy DTO vocabulary (Solo/Simple/XL)"))
                    }
                }
            }
    }
}
