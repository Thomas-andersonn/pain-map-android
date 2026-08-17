#!/bin/bash
set -e

# Setup 3D assets for PainMapAI (CC-BY-SA 4.0 Z-Anatomy dataset & base geometry)
mkdir -p app/src/main/assets/models

echo "Downloading Z-Anatomy 3D fullbody muscular model..."
curl -sL https://raw.githubusercontent.com/sesgigikimo/gym-muscle/main/fullbody.glb -o app/src/main/assets/models/z_anatomy_fullbody.glb

echo "Downloading Z-Anatomy 3D skeletal system model..."
curl -sL https://raw.githubusercontent.com/sesgigikimo/gym-muscle/main/skeleton.glb -o app/src/main/assets/models/z_anatomy_skeleton.glb

echo "Downloading base anatomical model..."
curl -sL https://raw.githubusercontent.com/bhagyeshsave/human-3d-body/main/client/public/geometries/human_body.glb -o app/src/main/assets/models/human_body.glb

echo "All 3D models successfully configured in app/src/main/assets/models/"
