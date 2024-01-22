/**
 * <h2>Client for the Hyperfoil server</h2>
 * <p>
 *     This package contains the necessary classes to interact with a running instance of an <code>Hyperfoil</code> server;
 *     <br>
 *     You can do the following:
 * </p>
 * <ul>
 *         <li>Ask Hyperfoil to generate some load on a target resource, e.g. a WILDFLY server</li>
 *         <li>Ask Hyperfoil for the statistics it collected while generating load on a target resource; e.g. you can
 *         obtain the average response time or the percentage of failed call</li>
 * </ul>
 * <p>
 *     In other words, this package contains the necessary classes to consume:
 * </p>
 * <ul>
 *         <li><code>Hyperfoil</code> APIs</li>
 *         <li><code>Hyperfoil</code> statistics</li>
 * </ul>
 * <h3>Client code generation- Hyperfoil APIs</h3>
 * <p>
 *     All classes in this package were generated using
 *     <a href="https://github.com/OpenAPITools/openapi-generator">openapi-generator</a>;
 *     <br>
 *     Note: don't use <a href="https://swagger.io/tools/swagger-codegen/">swagger-codegen</a> because it uses an old
 *     version of the <b>okhttp</b> client which conflicts with the project's dependencies;
 *     <br>
 *     The input to <a href="https://github.com/OpenAPITools/openapi-generator">openapi-generator</a> is
 *     <a href="https://raw.githubusercontent.com/Hyperfoil/Hyperfoil/release-0.23/controller-api/src/main/resources/openapi.yaml">Hyperfoil/release-0.23/openapi.yaml</a>;
 *     at the time of writing, the Operator deploys <b>quay.io/hyperfoil/hyperfoil:latest</b> which corresponds to
 *     <a href="https://github.com/Hyperfoil/Hyperfoil/tree/release-0.23">Hyperfoil version 0.21</a>; hence, we use file
 *     <a href="https://github.com/Hyperfoil/Hyperfoil/blob/release-0.23/controller-api/src/main/resources/openapi.yaml">openapi.yaml</a>
 *     as input to <a href="https://github.com/OpenAPITools/openapi-generator">openapi-generator</a>;
 *     <br>
 *     E.g.:
 *     <code>
 *          wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/6.0.1/openapi-generator-cli-6.0.1.jar
 *          java -jar openapi-generator-cli-6.0.1.jar generate \
 *              -i https://raw.githubusercontent.com/Hyperfoil/Hyperfoil/release-0.23/controller-api/src/main/resources/openapi.yaml \
 *              -g java \
 *              -o openapi-generator-cli-6.0.1 \
 *              --api-package org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05 \
 *              --invoker-package org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.invoker \
 *              --model-package org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.model \
 *              --package-name org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05
 *     </code>
 *     After code generation, you need to manually fix the following:
 * </p>
 * <ul>
 *     <li>HyperfoilApi: delete `import org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.model.AddBenchmarkRequest;`</li>
 *     <li>fix all imports</li>
 *     <li>ApiClient: change parameters order in all `RequestBody.create` calls</li>
 *     <li>ApiClient: fix javadoc in requestBodyToString</li>
 *     <li>remove all tables in javadoc containing <code>summary="Response Details"</code> occurrences</li>
 *     <li>remove all <code>@http.response.details</code></li>
 *     <li>fix method
 *     {@link org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.model.Run#validateJsonObject(com.google.gson.JsonObject)}
 *     </li>
 *     <li>fix method
 *     {@link org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.model.Agent#validateJsonObject(com.google.gson.JsonObject)}
 *     </li>
 *     <li>fix method
 *     {@link org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.model.Phase#validateJsonObject(com.google.gson.JsonObject)}
 *     </li>
 *     <li>fix method
 *     {@link org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.model.Histogram#validateJsonObject(com.google.gson.JsonObject)}
 *     </li>
 *     <li>fix method
 *     {@link org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.v05.invoker.JSON.OffsetDateTimeTypeAdapter#read(com.google.gson.stream.JsonReader)}
 *     </li>
 *     <li>fix
 *     {@link org.jboss.intersmash.provision.openshift.operator.hyperfoil.client.runschema.v06}: use
 *     Long instead of Integer in all classes
 *     </li>
 * </ul>
 * <h3>Client code generation - Hyperfoil statistics</h3>
 * <p>
 *     Classes in packages <code>runschema</code> are generated using <a href="https://www.jsonschema2pojo.org/">jsonschema2pojo</a>
 *     using <code>Class name: RunStatisticsWrapper</code> and <code>Source type: JSON Schema</code>
 *     <br>
 *     The input to <code>jsonschema2pojo</code> is
 *     <a href="https://raw.githubusercontent.com/Hyperfoil/Hyperfoil/release-0.23/distribution/src/main/resources/run-schema.json">Hyperfoil run-schema.json</a>
 *     <br>
 *     After code generation, you need to manually fix the following:
 * </p>
 * <ul>
 *     <li>Add constructor to class <code>RunStatisticsWrapper</code></li>
 * </ul>
 */
package org.jboss.intersmash.provision.openshift.operator.hyperfoil.client;