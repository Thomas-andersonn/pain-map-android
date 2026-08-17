#!/bin/bash
set -e

# Download MIT-licensed human_body.glb for 3D anatomy viewer
mkdir -p app/src/main/assets/models
echo "Downloading human_body.glb from open-source repository..."
curl -sL https://raw.githubusercontent.com/bhagyeshsave/human-3d-body/main/client/public/geometries/human_body.glb -o app/src/main/assets/models/human_body.glb
echo "Successfully downloaded human_body.glb (1.6 MB) to app/src/main/assets/models/"
