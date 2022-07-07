# Hazelcast on Kubernetes

This is just trying
out [Using Kubernetes in DNS Lookup Mode](https://docs.hazelcast.com/hazelcast/5.1/deploy/configuring-kubernetes#using-kubernetes-in-dns-lookup-mode)
from the official docs. It went very smoothly!

[HazelcastKubernetes](HazelcastKubernetes.java) is the main class, nothing fancy, just setting the parameters the doc provides. As a bonus, this
configuration works locally, without Kubernetes as well!

For deploying to Kubernetes, first do a `kubectl create namespace sandbox`, if you don't have one already.

Then, just run [deploy.sh](../../../../../../deploy.sh) and observe the magic :)

See also [deployment.yaml](../../../../../../deployment.yaml), but that's straightforward.
