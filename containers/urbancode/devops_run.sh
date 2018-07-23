

helm init
helm repo remove mycharts
helm repo add mycharts https://raw.githubusercontent.com/niklaushirt/charts/master/helm/charts/repo/stable/


helm fetch mycharts/gitlab-ce
helm install --name gitlab mycharts/gitlab-ce

helm fetch mycharts/jenkins
helm install --name gitlab mycharts/jenkins

helm fetch mycharts/ucds
helm install --name gitlab mycharts/ucds




helm delete --tls --purge jenkins
helm delete --tls --purge gitlab
helm delete --tls --purge ucds
