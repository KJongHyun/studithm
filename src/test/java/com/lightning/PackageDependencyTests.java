package com.lightning;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SliceRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;


@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTests {

    private static final String MAIN = "..modules.main..";
    private static final String STUDY = "..modules.gathering..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";
    private static final String MODULES = "com.studithm.modules..";


    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage(MODULES)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(MODULES);

    @ArchTest
    ArchRule gatheringPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY, EVENT, MAIN);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat()
            .resideInAnyPackage(STUDY, ACCOUNT, EVENT);

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat()
            .resideInAnyPackage(TAG, ZONE, ACCOUNT);

    @ArchTest
    SliceRule cycleCheck = slices().matching("com.studithm.modules.(*)..").should().beFreeOfCycles();

}
