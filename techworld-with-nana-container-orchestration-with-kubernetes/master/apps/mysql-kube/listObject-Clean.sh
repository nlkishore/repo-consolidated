cd /Users/yaswitha/k8s/techworld-with-nana-container-orchestration-with-kubernetes/mysql-kube
kubectl delete service mysql
kubectl delete deployment mysql
kubectl delete pvc mysql-pv-claim
kubectl delete pv mysql-pv