### Step 1: Start local infrastructure containers (MongoDB, Redis, Azurite, EventHubs, Gateway, Artifactory)
```zsh
docker compose up -d
```
### Step 2: Ensure local k3d Kubernetes cluster is active
```zsh
k3d cluster start gateway-cluster || k3d cluster create gateway-cluster
```
### Step 3: Install ArgoCD into Kubernetes (Automated for fresh setups)
```zsh
kubectl create namespace argocd || true
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```
### Step 4: Export and format k3d kubeconfig
```zsh
k3d kubeconfig get gateway-cluster > ./k3d-kubeconfig.yaml
sed -i '' 's/0.0.0.0/host.docker.internal/g; s/127.0.0.1/host.docker.internal/g' ./k3d-kubeconfig.yaml
sed -i '' 's/certificate-authority-data:.*/insecure-skip-tls-verify: true/g' ./k3d-kubeconfig.yaml
```
### Step 5: Import local gateway Docker image into k3d cluster
```zsh
k3d image import consumer-api-gateway:local -c gateway-cluster
```
### Step 6: Register application in ArgoCD
```zsh
kubectl apply -f argocd/application.yaml
```

### Step 7: Access ArgoCD Web UI (Runs in background at http://localhost:9091)

#### 1. Launch UI port forward

```zsh
 kubectl port-forward svc/argocd-server -n argocd 9091:80 &
 ```
#### 2. Retrieve initial admin password for username: admin
```zsh
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo
```

### Step 8: Access Gateway Kubernetes Service (Runs in background at http://localhost:8085)
```zsh
kubectl port-forward svc/consumer-api-gateway 8085:8080 &
```

## Here are the exact commands to use after updating code:

Option 1: Update via Docker Compose (Recommended for Fast Testing)
Run in your terminal:

```zsh
docker compose up --build --force-recreate gateway -d
```
Option 2: Update via Kubernetes & ArgoCD
If testing inside your local Kubernetes cluster:
```zsh
# Step 1: Rebuild local Docker image
docker build -t consumer-api-gateway:local .
# Step 2: Import new image into k3d cluster
k3d image import consumer-api-gateway:local -c gateway-cluster
# Step 3: Rolling restart Kubernetes pods
kubectl rollout restart deployment consumer-api-gateway
```