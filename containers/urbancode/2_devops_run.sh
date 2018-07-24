

helm init
helm repo remove mycharts
helm repo add mycharts https://raw.githubusercontent.com/niklaushirt/charts/master/helm/charts/repo/stable/


helm fetch mycharts/devops-gitlab
helm install --tls --name gitlab mycharts/devops-gitlab

helm fetch mycharts/devops-jenkins
helm install --tls --name jenkins mycharts/devops-jenkins

helm fetch mycharts/devops-ucd-server
helm install --tls --name ucds mycharts/devops-ucd-server --set server.serviceType=LoadBalancer --set server.nodeport.ui=30123 --set server.nodeport.jms=30124 --set server.nodeport.http=30125



helm delete --tls --purge jenkins
helm delete --tls --purge gitlab
helm delete --tls --purge ucds
