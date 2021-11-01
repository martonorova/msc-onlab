FROM wurstmeister/kafka:2.12-2.5.0

ADD prom-jmx-agent-config.yml /usr/app/prom-jmx-agent-config.yml

RUN wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.16.1/jmx_prometheus_javaagent-0.16.1.jar -O /usr/app/jmx_prometheus_javaagent.jar

