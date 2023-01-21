# NATS sandbox

I tried out NATS just for fun, and tested the following:

* [Setting up a 2-node NATS server on Kubernetes (manually, without Helm)](deployment.yaml)
* [Performance tests](src/main/java/org/ogreg/nats/PerfTest.java):

Deploying the cluster:
`./deploy.sh`

Running the performance test:
* `./perftest.sh simple` - Publishes 100k 4096 byte messages five times, awaiting their arrival
* `./perftest.sh stream` - Publishes a stream of 10k 4096 byte messages with 2x replication, and reads them with an [Ephemeral push consumer](https://docs.nats.io/using-nats/developer/develop_jetstream/consumers#ephemeral-consumers).

Port-forwarding on K8S (for debugging - ):
`kubectl port-forward --namespace sandbox service/mynats 4222`
