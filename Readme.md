# Consumer API Gateway & Processor Service Architecture

Multi-microservice event-driven architecture built with Kotlin, Spring Boot, Azure Event Hubs local simulator (Kafka protocol), MongoDB, Redis, Helm 3, and ArgoCD GitOps.

---

## Architecture Flow & Distributed Tracing

```
[ HTTP Client ] ──( GET /hello with X-Correlation-ID: <traceId> )──► [ consumer-api-gateway ]
                                                                             │
                                              Produces to `gateway-requests` │ (Header: X-Correlation-ID)
                                                                             ▼
                                                           [ Azure Event Hubs Simulator ]
                                                                             │
                                            Consumes from `gateway-requests` │
                                                                             ▼
                                                                  [ processor-service ]
                                                                             │
                                             Produces to `service-responses` │ (Header: X-Correlation-ID)
                                                                             ▼
                                                           [ Azure Event Hubs Simulator ]
                                                                             │
                                           Consumes from `service-responses` │
                                                                             ▼
                                                                 [ consumer-api-gateway ]
                                                                 (Logs response with traceId)
```

---

## Microservice Repositories & Prerequisites

Before starting, clone both microservice repositories into adjacent folders under your workspace directory (`services/`):

```zsh
# 1. Navigate to workspace services folder
cd services

# 2. Clone Consumer API Gateway repository
git clone https://github.com/akc276/customer-api-gateway.git

# 3. Clone Processor Service repository
git clone https://github.com/akc276/processor-service.git
```

### Git Repository Links:
- **Consumer API Gateway:** [https://github.com/akc276/customer-api-gateway.git](https://github.com/akc276/customer-api-gateway.git)
- **Processor Service:** [https://github.com/akc276/processor-service.git](https://github.com/akc276/processor-service.git)

---

## Option 1: Fast Local Setup with Docker Compose (Recommended)

### Step 1: Build and launch all infrastructure & microservices
```zsh
cd services/consumer-api-gateway
docker compose up -d --build
```

### Step 2: Verify container statuses
```zsh
docker compose ps
```

### Step 3: Trigger Hello World EventHub Workflow
```zsh
curl -i -H "X-Correlation-ID: my-custom-trace-1234" http://localhost:8080/hello
```

### Step 4: Continuous Live Log Streaming Across Microservices

#### 1. Stream continuous live logs from BOTH services simultaneously in real-time:
```zsh
docker compose logs -f gateway processor-service
```

#### 2. Stream live logs filtered by a specific Trace ID across both services:
```zsh
docker compose logs -f gateway processor-service | grep --line-buffered "my-custom-trace-1234"
```

#### 3. Inspect individual service logs:
```zsh
docker logs -f gateway-api
docker logs -f processor-service-api
```

---

## Option 2: Kubernetes Deployment with Helm & ArgoCD

### Step 1: Ensure local k3d Kubernetes cluster is active
```zsh
k3d cluster start gateway-cluster || k3d cluster create gateway-cluster
```

### Step 2: Install ArgoCD into Kubernetes
```zsh
kubectl create namespace argocd || true
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Step 3: Export and format k3d kubeconfig
```zsh
k3d kubeconfig get gateway-cluster > ./k3d-kubeconfig.yaml
sed -i '' 's/0.0.0.0/host.docker.internal/g; s/127.0.0.1/host.docker.internal/g' ./k3d-kubeconfig.yaml
sed -i '' 's/certificate-authority-data:.*/insecure-skip-tls-verify: true/g' ./k3d-kubeconfig.yaml
```

### Step 4: Build and Import local microservice Docker images into k3d
```zsh
# 1. Build Gateway image & import into k3d
docker build -t consumer-api-gateway:local .
k3d image import consumer-api-gateway:local -c gateway-cluster

# 2. Build Processor Service image & import into k3d
cd ../processor-service
docker build -t processor-service:local .
k3d image import processor-service:local -c gateway-cluster
cd ../consumer-api-gateway
```

### Step 5: Register Both Applications in ArgoCD
```zsh
# Deploys both consumer-api-gateway and processor-service Applications into ArgoCD
kubectl apply -f argocd/application.yaml
```

### Step 6: Access ArgoCD Web Dashboard (at http://localhost:9091)
```zsh
# 1. Launch ArgoCD UI port forward in background
kubectl port-forward svc/argocd-server -n argocd 9091:80 &

# 2. Retrieve initial admin password (Username: admin)
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo
```

### Step 7: Deploy Helm Charts Directly via Helm CLI (Alternative to GitOps Sync)
```zsh
# Install consumer-api-gateway Helm chart
helm upgrade --install gateway ./helm/consumer-api-gateway

# Install processor-service Helm chart
helm upgrade --install processor ./helm/processor-service
```

### Step 8: Verify Kubernetes Deployments & Services
```zsh
kubectl get pods -A
kubectl get svc -A
```

---

## How to Update Microservices Code After Edits

### Fast Reload via Docker Compose:
```zsh
# Rebuild and restart consumer-api-gateway container
docker compose up -d --build --force-recreate gateway

# Rebuild and restart processor-service container
docker compose up -d --build --force-recreate processor-service
```

### Update via Kubernetes & ArgoCD:
```zsh
# Rebuild image & re-import into k3d
docker build -t consumer-api-gateway:local .
k3d image import consumer-api-gateway:local -c gateway-cluster
kubectl rollout restart deployment consumer-api-gateway

# Rebuild processor-service & re-import into k3d
docker build -t processor-service:local ../processor-service
k3d image import processor-service:local -c gateway-cluster
kubectl rollout restart deployment processor-service
```