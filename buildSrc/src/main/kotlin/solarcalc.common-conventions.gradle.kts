// Shared configuration applied to every solarcalc subproject. Order matters:
// java-conventions applies the java/kotlin plugins the others build upon.
plugins {
    id("solarcalc.java-conventions")
    id("solarcalc.detekt-conventions")
    id("solarcalc.tests-conventions")
    id("solarcalc.owasp-conventions")
    id("solarcalc.kover-conventions")
}
