package io.github.raginlundf.solarcalc.restapi.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

// Forwards Angular deep-links to index.html. Skips paths with file extensions.
@Controller
@RequestMapping("/")
class SpaForwardController {

    @Suppress("FunctionOnlyReturningConstant")
    @GetMapping(value = ["/{path:[^\\.]*}", "/{a:[^\\.]*}/{b:[^\\.]*}", "/{a:[^\\.]*}/{b:[^\\.]*}/{c:[^\\.]*}"])
    fun forward(): String {
        return "forward:/index.html"
    }
}
