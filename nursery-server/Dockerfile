FROM eclipse-temurin:11.0.13_8-jre-alpine

ARG BUILD_VERSION
COPY fiddle-coordinator-$BUILD_VERSION.sls.tgz /
RUN mkdir -p /opt/fern/services \
    && tar -xvf /fiddle-coordinator-$BUILD_VERSION.sls.tgz -C /opt/fern/services \
    && mv /opt/fern/services/fiddle-coordinator-$BUILD_VERSION /opt/fern/services/fiddle-coordinator \
    && rm -rf fiddle-coordinator-$BUILD_VERSION.sls.tgz

# Run server
ENTRYPOINT cd /opt/fern/services/fiddle-coordinator/ \
    && ./service/bin/linux-amd64/go-java-launcher service/bin/launcher-static.yml var/conf/launcher-custom.yml