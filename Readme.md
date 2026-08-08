# Local Setup
### Step 1: Start local infrastructure containers (MongoDB, Redis, Azurite, EventHubs, Gateway, Artifactory)
docker compose up -d

### Step 2: Ensure local k3d Kubernetes cluster is active
k3d cluster start gateway-cluster || k3d cluster create gateway-cluster

### Step 3: Export and format k3d kubeconfig
k3d kubeconfig get gateway-cluster > ./k3d-kubeconfig.yaml
sed -i '' 's/0.0.0.0/host.docker.internal/g; s/127.0.0.1/host.docker.internal/g' ./k3d-kubeconfig.yaml
sed -i '' 's/certificate-authority-data:.*/insecure-skip-tls-verify: true/g' ./k3d-kubeconfig.yaml

### Step 4: Import local gateway Docker image into k3d cluster
k3d image import consumer-api-gateway:local -c gateway-cluster

### Step 5: Register application in ArgoCD
kubectl apply -f argocd/application.yaml

### Step 6: Access ArgoCD Web UI (Runs in background at https://localhost:9091)
kubectl port-forward svc/argocd-server -n argocd 9091:80 &

### Step 7: Access Gateway Kubernetes Service (Runs in background at http://localhost:8085)
kubectl port-forward svc/consumer-api-gateway 8085:8080 &