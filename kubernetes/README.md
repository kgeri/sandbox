# Kubernetes sandbox

This is a sample project to test the development and deployment of a simple Java app to a Kubernetes cluster.

My provider of choice was DigitalOcean, because as of this writing they:
* Have a fairly feature-complete managed Kubernetes implementation (called DOKS)
* Only charging for the infra (droplets)
* Not enforcing credit cards

## Creating the cluster

Run `doks-create.sh`. This will take ~5 mins.

## Deployment

Just run `deploy.sh`.

Note that it'll build a GraalVM native image, using ~6GB RAM in the process, and it'll take a while.
In return, the resulting image is just 74MB and it's blazing fast!

## Verification

DOKS creates firewall rules to expose NodePorts automatically.

You can test the service by getting the public IPs first:
`doctl compute droplet list --tag-name k8s:worker --format ID,PublicIPv4`

Then:
`curl http://<public IP>:30080/hello`

## Destroying the cluster

You're billed by the hour, so don't forget to drop the resources when you're done!

Run `doks-destroy.sh`.
