#!/bin/bash
if [ ! -d "$(pwd)/cloud" ]
then mkdir $(pwd)/cloud
mkdir "cloud/cloudserver"
cd "cloud"
cd cloudserver
mkdir storage utilities watch
fi

cd ../../
unzip demo_dataset.zip

cd SemanticSearchClient
mkdir input
cd ../
mv demo_dataset SemanticSearchClient/input/demo_dataset


