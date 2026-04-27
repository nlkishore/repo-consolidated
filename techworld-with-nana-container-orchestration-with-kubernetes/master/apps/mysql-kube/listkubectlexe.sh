cd /Users/yaswitha/k8s/techworld-with-nana-container-orchestration-with-kubernetes/mysql-kube
kubectl apply -f mysql-pv.yaml
kubectl apply -f mysql-pvc.yaml
kubectl apply -f mysql-deployment.yaml
kubectl apply -f mysql-service.yaml