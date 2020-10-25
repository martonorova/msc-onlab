# msc-onlab

## Start on Docker Compose
1. Run `docker-compose up -d` in root folder.

## Start on Minikube

1. Apply all k8s files in `k8s-deployment` in order.
2. In a separate terminal, open the Minikube tunnel with `minikube tunnel`
3. Get the external IP of the `kubedepend-proxy` service with `kubectl -n kubedepend get svc`
4. Open the service IP in browser.
