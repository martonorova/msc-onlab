# msc-onlab

## Start on Docker Compose
1. Run `docker-compose up -d` in root folder.

## Start on Minikube

1. Apply all k8s files in `k8s-deployment` in order.
2. In a separate terminal, open the Minikube tunnel with `minikube tunnel`
3. Get the external IP of the `kubedepend-proxy` service with `kubectl -n kubedepend get svc`
4. Open the service IP in browser.

## Install Chaos Mesh

1. Install Helm. [link](https://helm.sh/docs/intro/install/)
2. Add Chaos Mesh repository to `helm` repos `helm repo add chaos-mesh https://charts.chaos-mesh.org`. [link](https://chaos-mesh.org/docs/get_started/installation#step-1-add-chaos-mesh-repository-to-helm-repos)
3. Create Chaos Mesh related K8s Custom Resource Definitions. [link](https://chaos-mesh.org/docs/get_started/installation/#step-2-create-custom-resource-type)
    ```bash
    curl -sSL https://mirrors.chaos-mesh.org/v1.0.1/crd.yaml > chaos-mesh-crd.yaml
    kubectl apply -f chaos-mesh-crd.yaml
    ```
4. Install Chaos Mesh using helm. [link](https://chaos-mesh.org/docs/get_started/installation/#step-3-install-chaos-mesh)

    `helm install chaos-mesh chaos-mesh/chaos-mesh --namespace=chaos-testing`
    

To delete Chaos Mesh, delete the namsespace containing it.
