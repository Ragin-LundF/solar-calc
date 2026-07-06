plugins {
    id("org.jetbrains.kotlinx.kover")
}

// Root-level aggregated coverage report. Subprojects register themselves into
// the `kover` configuration created here via solarcalc.kover-conventions.
kover {
    reports {
        filters {
            excludes {}
        }
        total {
            xml {
                onCheck = true
            }
            html {
                onCheck = true
            }
        }
    }
}
