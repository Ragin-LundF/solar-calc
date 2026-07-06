import nl.littlerobots.vcu.plugin.versionSelector

plugins {
    id("nl.littlerobots.version-catalog-update")
}

val unstableVersionKeywords = listOf("-beta", "dev", "-rc", "-m", "-SNAPSHOT", "-ALPHA")

versionCatalogUpdate {
    // sort the catalog by key (default is true)
    sortByKey.set(false)
    versionSelector { candidate ->
        unstableVersionKeywords.none { keyword ->
            candidate.candidate.version.contains(keyword, ignoreCase = true)
        }
    }
}
