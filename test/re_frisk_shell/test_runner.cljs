(ns re-frisk-shell.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [re-frisk-shell.filter-parser-test]
            [re-frisk-shell.filter-matcher-test]))

(doo-tests 're-frisk-shell.filter-parser-test
           're-frisk-shell.filter-matcher-test)
