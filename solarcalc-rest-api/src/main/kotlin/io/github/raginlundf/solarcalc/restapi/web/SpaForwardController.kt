package io.github.raginlundf.solarcalc.restapi.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/** Forwards Angular deep-links to index.html. Does not intercept /api/** or static files. */
@Controller
class SpaForwardController {

    @GetMapping(value = ["/{path:[^\\.]*}", "/**/{path:[^\\.]*}"])
    fun forward(): String {
        return "forward:/index.html"
    }
}
