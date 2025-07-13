#!/bin/bash

set -e

# 사용자 입력 받기
echo "[1] GitHub Personal Access Token(PAT)을 입력하세요:"
read -s GITHUB_PAT

# cert-manager 설치
echo "[2] Installing cert-manager..."
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/latest/download/cert-manager.yaml

# Helm repo 추가 및 업데이트
echo "[3] Adding Helm repo for actions-runner-controller..."
helm repo add actions-runner-controller https://actions-runner-controller.github.io/actions-runner-controller
helm repo update

# Namespace 생성
echo "[4] Creating namespace 'actions-runner-system'..."
kubectl create namespace actions-runner-system --dry-run=client -o yaml | kubectl apply -f -

# GitHub PAT secret 생성
echo "[5] Creating GitHub PAT secret..."
kubectl create secret generic controller-manager \
  -n actions-runner-system \
  --from-literal=github_token=${GITHUB_PAT} \
  --dry-run=client -o yaml | kubectl apply -f -

# actions-runner-controller Helm 설치
echo "[6] Installing actions-runner-controller via Helm..."
helm upgrade --install actions-runner-controller actions-runner-controller/actions-runner-controller \
  --namespace actions-runner-system \
  --set authSecret.create=false \
  --set githubToken.enabled=true

# RunnerDeployment manifest 적용
echo "[7] Applying runner-deployment.yaml..."
kubectl apply -f script/manifests/runner-deployment.yaml

echo "완료되었습니다."