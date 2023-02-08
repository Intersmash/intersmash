/**
 * <h2>Client for the Hyperfoil Custom Resource</h2>
 * <p>
 *     This package contains the necessary classes to handle <code>Hyperfoil</code> CRDs (Custom Resource Definitions);
 * </p>
 * <p>
 *     The <a href="https://github.com/Hyperfoil/hyperfoil-operator">hyperfoil-operator</a> is distributed as an
 *     <a href="https://github.com/redhat-openshift-ecosystem/community-operators-prod/tree/main/operators/hyperfoil-bundle">
 *         Openshift Community Operator</a>;
 *     <a href="https://github.com/redhat-openshift-ecosystem/community-operators-prod">Openshift Community Operators</a>
 *     extend <a href="https://github.com/k8s-operatorhub/community-operators">Kubernetes Community Operators</a>;
 * </p>
 * <h3>Client code generation</h3>
 * <p>
 *     Most classes in this package are generated using
 *     <a href="https://github.com/fabric8io/kubernetes-client/tree/master/java-generator">java-generator</a>;
 *     <br>
 *     Since, at the time of writing, we are using <a href="https://cloud.redhat.com/blog/introducing-red-hat-openshift-4.10">
 *         OpenShift 4.10</a> which is based on Kubernetes 1.23, we are going to use
 *         <a href="https://github.com/fabric8io/kubernetes-client/tree/v6.0.0/java-generator">java-generator 15.0.1</a>
 *         according to the
 *         <a href="https://github.com/fabric8io/kubernetes-client/blob/master/README.md#kubernetes-compatibility-matrix">
 *             Kubernetes Compatibility Matrix</a>;
 *     <br>
 *     The input to <code>java-generator</code> comes from the CRDs defined in
 *     <a href="https://github.com/Hyperfoil/hyperfoil-operator/tree/master/bundle/manifests">hyperfoil-operator/bundle/manifest</a>;
 *     <br>
 *     Note you can also obtain the CRD from your OpenShift cluster after installing Hyperfoil Community Operator;
 *     <br>
 *     The commands to generate the Java classes to map the CRD, are the following:
 *     <code>
 *         git clone https://github.com/fabric8io/kubernetes-client.git
 *         cd kubernetes-client
 *         mvn install -DskipTests
 *         cd java-generator
 *         mvn install -DskipTests
 *         cd cli/target/
 *         wget https://raw.githubusercontent.com/Hyperfoil/hyperfoil-operator/master/bundle/manifests/hyperfoil.io_hyperfoils.yaml
 *         ./java-gen -s=hyperfoil.io_hyperfoils.yaml -t=hyperfoil-git
 *     </code>
 *     <br>
 *     alternatively, you can get the <code>hyperfoils.hyperfoil.io</code> Custom Resource Definition from you OpenShift cluster
 *     <code>
 *         oc get crds/hyperfoils.hyperfoil.io -o yaml &gt; oc-hyperfoil.io_hyperfoils.yaml
 *         ./java-gen -s=oc-hyperfoil.io_hyperfoils.yaml -t=hyperfoil-oc
 *     </code>
 *     <br>
 *     The first method produces java classes with additional <code>additionalArgs</code> in class <code>HyperfoilSpec</code>;
 * </p>
 * <h3>Additional code</h3>
 * <p>
 *     After generating the Java classes to map the CRD with
 *     <a href="https://github.com/fabric8io/kubernetes-client/tree/master/java-generator">java-generator</a>, you need
 *     to add the following:
 * </p>
 * <ul>
 *         <li>HyperfoilList: needed in the HyperfoilOperatorProvisioner#hyperfoilClient method</li>
 *         <li>HyperfoilBuilder: used for testing purposes</li>
 *         <li>HyperfoilOperatorProvisioner#hyperfoilClient: this method assembles a client capable of working with the
 *         Hyperfoil Custom Resource <b>hyperfoils.hyperfoil.io</b></li>
 * </ul>
 **/
package io.hyperfoil.v1alpha2;
