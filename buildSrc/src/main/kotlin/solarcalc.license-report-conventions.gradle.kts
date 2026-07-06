import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.LicenseBundleNormalizer
import com.github.jk1.license.render.InventoryHtmlReportRenderer
import com.github.jk1.license.render.ReportRenderer

plugins {
    base // provides the `assemble` lifecycle task wired to below
    id("com.github.jk1.dependency-license-report")
}

licenseReport {
    allowedLicensesFile = File("$projectDir/config/licenses-allowed.json")
    outputDir = "$projectDir/solarcalc-server/build/resources/main/static/licenses/"
    renderers = arrayOf<ReportRenderer>(InventoryHtmlReportRenderer())
    filters = arrayOf<DependencyFilter>(
        LicenseBundleNormalizer("$projectDir/config/licenses-normalizer.json", true),
    )
}

tasks.named("checkLicense") {
    dependsOn(tasks.named("generateLicenseReport"))
    mustRunAfter(":solarcalc-server:processResources")
    mustRunAfter(":solarcalc-server:bootJar")
}
tasks.named("assemble") {
    dependsOn(tasks.named("checkLicense"))
}
