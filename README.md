### Yang2Swagger generator configured for TAPI ###

This is a simple util project that provides CLI yang2swagger generator interface preconfigured to handle TAPI Yang modules.


Contact:

 * Bartosz Michalik bartosz.michalik@amartus.com


### How do I get set up? ###
First you need to build https://github.com/bartoszm/yang2swagger.
Then you simply run:
``mvn clean install``

### Command-line Execution ###
There are two CLI tools ``Generator`` and ``Converter``. ``Generator`` allows for generating Swagger from YANG modules thus it is similar to original 
https://github.com/bartoszm/yang2swagger CLI generator. However, the pruning mechanism was configured to reduce number of path generated for TAPI modules.
In addition extra parameters were added.
``Converter`` can convert _any_ Swagger definition. What it does it flattens hierarchy in the input Swagger. 

#### Hierarchy flattening ####
Swagger allows combining and extending model definitions using the ``allOf`` property of JSON Schema, in effect offering model composition. allOf takes in an array of object definitions that are validated independently but together compose a single object.
However most of the default code generators do not handle definitions that contain more than one simple model and a single reference.

To allow TAPI swagger definition to be consumed by default code generator we need to flatten data model, for example:

```
  tapi.connectivity.deleteconnectivityservice.output.Service:
    allOf:
    - $ref: "#/definitions/nrp.interface.ServiceAugmentation1"
    - $ref: "#/definitions/tapi.common.AdminStatePac"
    - $ref: "#/definitions/tapi.common.GlobalClass"
    - $ref: "#/definitions/tapi.connectivity.ConnectivityConstraint"
    - $ref: "#/definitions/tapi.connectivity.ResilienceConstraint"
    - $ref: "#/definitions/tapi.connectivity.TopologyConstraint"
    - type: "object"
      properties:
        layer-protocol-name:
          description: "none"
          $ref: "#/definitions/tapi.common.LayerProtocolName"
        connection:
          type: "array"
          description: "none"
          items:
            type: "string"
            x-path: "/tapi-common:context/tapi-connectivity:connection/tapi-connectivity:uuid"
        end-point:
          type: "array"
          description: "none"
          items:
            $ref: "#/definitions/tapi.connectivity.deleteconnectivityservice.output.service.EndPoint"
        direction:
          description: "none"
          $ref: "#/definitions/tapi.common.ForwardingDirection"
      description: "none"
```
is converted to:
```
tapi.connectivity.deleteconnectivityservice.output.Service:
    allOf:
    - $ref: "#/definitions/tapi.common.GlobalClass"
    - properties:
        schedule:
          description: "none"
          $ref: "#/definitions/tapi.common.TimeRange"
        requested-capacity:
          description: "none"
          $ref: "#/definitions/tapi.common.Capacity"
        is-exclusive:
          type: "boolean"
          description: "To distinguish if the resources are exclusive to the service\
            \  - for example between EPL(isExclusive=true) and EVPL (isExclusive=false),\
            \ or between EPLAN (isExclusive=true) and EVPLAN (isExclusive=false)"
          default: true
        diversity-exclusion:
          type: "array"
          description: "none"
          items:
            $ref: "#/definitions/tapi.connectivity.ConnectivityServiceRef"
        service-level:
          type: "string"
          description: "An abstract value the meaning of which is mutually agreed\
            \ – typically represents metrics such as - Class of service, priority,\
            \ resiliency, availability"
        service-type:
          description: "none"
          $ref: "#/definitions/tapi.connectivity.ServiceType"
        cost-characteristic:
          type: "array"
          description: "The list of costs where each cost relates to some aspect of\
            \ the TopologicalEntity."
          items:
            $ref: "#/definitions/tapi.topology.CostCharacteristic"
        latency-characteristic:
          type: "array"
          description: "The effect on the latency of a queuing process. This only\
            \ has significant effect for packet based systems and has a complex characteristic."
          items:
            $ref: "#/definitions/tapi.topology.LatencyCharacteristic"
        coroute-inclusion:
          type: "string"
          description: "none"
          
          [... CUT ...]
```
In other words properties from most of the referenced models got unpacked to definition using it.    
You can directly generate swagger with simple hierarch using ``-simplify-hierarchy`` CLI argument.

#### Usage ####
You can easily run ```Generator``` from the command-line:
```
 java -cp yang2swagger-tapi-cli-1.1-cli.jar com.amartus.y2s.Generator --help
 module ...                            : List of YANG module names to generate
                                         in swagger output
 -api-version file                     : Version of api generated - default 1.0
                                         (default: 1.0)
 -authentication [BASIC | NONE]        : Authentication definition (default:
                                         NONE)
 -content-type VAL                     : Content type the API generates /
                                         consumes - default application/yang-dat
                                         a+json (default: application/yang-data+
                                         json)
 -elements [DATA | RPC | DATA_AND_RPC] : Define YANG elements to focus on.
                                         Defaul DATA + RPC (default:
                                         DATA_AND_RPC)
 -format [YAML | JSON]                 : Output format of generated file -
                                         defaults to yaml with options of json
                                         or yaml (default: YAML)
 -output file                          : File to generate, containing the
                                         output - defaults to stdout (default: )
 -simplify-hierarchy                   : Use it to generate Swagger which with
                                         simplified inheritence model which can
                                         be used with standard code generators.
                                         Default false (default: false)
 -use-namespaces                       : Use namespaces in resource URI
                                         (default: false)
 -yang-dir path                        : Directory to search for YANG modules -
                                         defaults to current directory.
                                         Multiple dirs might be separated by
                                         system path separator (default: )

```

For example:
```
java -cp yang2swagger-tapi-cli-1.1-cli.jar com.amartus.y2s.Generator -yang-dir yang -use-namespaces -elements RPC -output test.yaml
```

To run ``Converter``:


```
java -cp yang2swagger-tapi-cli-1.1-cli.jar com.amartus.y2s.Converter  --help
 -input file  : File with original swagger, containing the input - defaults to
                stdin (default: )
 -output file : File to generate, containing the output - defaults to stdout
                (default: )
```
