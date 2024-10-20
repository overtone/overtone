 param (
    [Parameter(Mandatory=$true)][string]$clojure,
 )

clojure "-M:test:test-runner:$clojure"
