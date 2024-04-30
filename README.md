# Keycloak Event Listener

Some demo event listeners for Keycloak.

## Last Login Time

Save the last (most recent) login time in an attribute of the user.

And log the login event at INFO level


### Running Testcontainers from within a java/maven container

In order to run Testcontainers tests, from within a container, add the following arguments to the `docker run` command :

```shell
-e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal -v /var/run/docker.sock:/var/run/docker.sock 
```

Example with the *maven:3.6.3-openjdk-17* image (with Docker Desktop for Windows, from the `cmd` prompt):

```
docker run -it --rm -v "%cd%":/usr/src/mymaven -w /usr/src/mymaven -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal -v /var/run/docker.sock:/var/run/docker.sock maven:3.6.3-openjdk-17 mvn clean package
```