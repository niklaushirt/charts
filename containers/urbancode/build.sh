git clone https://github.com/niklaushirt/charts.git
cd charts/containers/urbancode

wget https://www.dropbox.com/s/2t00hzfn7p694av/URBANCODE_DEPLOY_V7.0.0_MP_ML.zip?dl=0
mv URBANCODE_DEPLOY_V7.0.0_MP_ML.zip\?dl=0 ./server/common/URBANCODE_DEPLOY_V7.0.0_MP_ML.zip

docker build -t "ucds:7.0.0" ./server
docker build -t "ucda:7.0.0" ./agent


docker login mycluster.icp:8500 -u admin -p admin
docker tag ucds:7.0.0 mycluster.icp:8500/default/ucds:7.0.0
docker tag ucda:7.0.0 mycluster.icp:8500/default/ucda:7.0.0

docker push mycluster.icp:8500/default/ucds:7.0.0
docker push mycluster.icp:8500/default/ucda:7.0.0
