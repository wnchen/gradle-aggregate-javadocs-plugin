package nebula.plugin.javadoc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.javadoc.Javadoc

class NebulaAggregateJavadocPlugin implements Plugin<Project> {
    static final String AGGREGATE_JAVADOCS_TASK_NAME = 'aggregateJavadocs'

    @Override
    void apply(Project project) {
        Project rootProject = project.rootProject
        rootProject.gradle.projectsEvaluated {
            Set<Project> javaSubprojects = getJavaSubprojects(rootProject)
            Set<Project> librarySubprojects = getLibrarySubprojects(rootProject)
            if (!javaSubprojects.isEmpty() || !librarySubprojects.isEmpty()) {
                rootProject.task(AGGREGATE_JAVADOCS_TASK_NAME, type: Javadoc) {
                    description = 'Aggregates Javadoc API documentation of all subprojects.'
                    group = JavaBasePlugin.DOCUMENTATION_GROUP
                    dependsOn javaSubprojects.javadoc
                    dependsOn librarySubprojects.generateReleaseJavadoc

                    source javaSubprojects.javadoc.source
                    source librarySubprojects.generateReleaseJavadoc.source
                    destinationDir rootProject.file("$rootProject.buildDir/docs/javadoc")
                    classpath = rootProject.files(javaSubprojects.javadoc.classpath)
                    classpath += rootProject.files(librarySubprojects.generateReleaseJavadoc.classpath)
                }
            }
        }
    }

    private Set<Project> getLibrarySubprojects(Project rootProject) {
        rootProject.subprojects.findAll { subproject ->
            (subproject.plugins.findPlugin("com.android.library") &&
                    subproject.hasProperty('generateReleaseJavadoc'))}
    }

    private Set<Project> getJavaSubprojects(Project rootProject) {
        rootProject.subprojects.findAll { subproject -> subproject.plugins.hasPlugin(JavaPlugin)}
    }


}